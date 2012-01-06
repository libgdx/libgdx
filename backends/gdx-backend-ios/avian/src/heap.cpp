/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "heap.h"
#include "system.h"
#include "common.h"
#include "arch.h"

using namespace vm;

namespace {

namespace local {

const unsigned Top = ~static_cast<unsigned>(0);

const unsigned InitialGen2CapacityInBytes = 4 * 1024 * 1024;
const unsigned InitialTenuredFixieCeilingInBytes = 4 * 1024 * 1024;

const unsigned LowMemoryPaddingInBytes = 1024 * 1024;

const bool Verbose = false;
const bool Verbose2 = false;
const bool Debug = false;
const bool DebugFixies = false;

#ifdef NDEBUG
const bool DebugAllocation = false;
#else
const bool DebugAllocation = true;
#endif

#define ACQUIRE(x) MutexLock MAKE_NAME(monitorLock_) (x)

class MutexLock {
 public:
  MutexLock(System::Mutex* m): m(m) {
    m->acquire();
  }

  ~MutexLock() {
    m->release();
  }

 private:
  System::Mutex* m;
};

class Context;

void NO_RETURN abort(Context*);
#ifndef NDEBUG
void assert(Context*, bool);
#endif

System* system(Context*);
void* tryAllocate(Context* c, unsigned size);
void* allocate(Context* c, unsigned size);
void free(Context* c, const void* p, unsigned size);

#ifdef USE_ATOMIC_OPERATIONS
inline void
markBitAtomic(uintptr_t* map, unsigned i)
{
  uintptr_t* p = map + wordOf(i);
  uintptr_t v = static_cast<uintptr_t>(1) << bitOf(i);
  for (uintptr_t old = *p;
       not atomicCompareAndSwap(p, old, old | v);
       old = *p)
  { }
}
#endif // USE_ATOMIC_OPERATIONS

inline void*
get(void* o, unsigned offsetInWords)
{
  return mask(cast<void*>(o, offsetInWords * BytesPerWord));
}

inline void**
getp(void* o, unsigned offsetInWords)
{
  return &cast<void*>(o, offsetInWords * BytesPerWord);
}

inline void
set(void** o, void* value)
{
  *o = reinterpret_cast<void*>
    (reinterpret_cast<uintptr_t>(value)
     | (reinterpret_cast<uintptr_t>(*o) & (~PointerMask)));
}

inline void
set(void* o, unsigned offsetInWords, void* value)
{
  set(getp(o, offsetInWords), value);
}

class Segment {
 public:
  class Map {
   public:
    class Iterator {
     public:
      Map* map;
      unsigned index;
      unsigned limit;
      
      Iterator(Map* map, unsigned start, unsigned end):
        map(map)
      {
        assert(map->segment->context, map->bitsPerRecord == 1);
        assert(map->segment->context, map->segment);
        assert(map->segment->context, start <= map->segment->position());

        if (end > map->segment->position()) end = map->segment->position();

        index = map->indexOf(start);
        limit = map->indexOf(end);

        if ((end - start) % map->scale) ++ limit;
      }

      bool hasMore() {
        unsigned word = wordOf(index);
        unsigned bit = bitOf(index);
        unsigned wordLimit = wordOf(limit);
        unsigned bitLimit = bitOf(limit);

        for (; word <= wordLimit and (word < wordLimit or bit < bitLimit);
             ++word)
        {
          uintptr_t w = map->data[word];
          if (2) {
            for (; bit < BitsPerWord and (word < wordLimit or bit < bitLimit);
                 ++bit)
            {
              if (w & (static_cast<uintptr_t>(1) << bit)) {
                index = ::indexOf(word, bit);
                //                 printf("hit at index %d\n", index);
                return true;
              } else {
                //                 printf("miss at index %d\n", indexOf(word, bit));
              }
            }
          }
          bit = 0;
        }

        index = limit;

        return false;
      }
      
      unsigned next() {
        assert(map->segment->context, hasMore());
        assert(map->segment->context, map->segment);

        return (index++) * map->scale;
      }
    };

    Segment* segment;
    Map* child;
    uintptr_t* data;
    unsigned bitsPerRecord;
    unsigned scale;
    bool clearNewData;

    Map(Segment* segment, uintptr_t* data, unsigned bitsPerRecord,
        unsigned scale, Map* child, bool clearNewData):
      segment(segment),
      child(child),
      data(data),
      bitsPerRecord(bitsPerRecord),
      scale(scale),
      clearNewData(clearNewData)
    { }

    Map(Segment* segment, unsigned bitsPerRecord, unsigned scale, Map* child,
        bool clearNewData):
      segment(segment),
      child(child),
      data(0),
      bitsPerRecord(bitsPerRecord),
      scale(scale),
      clearNewData(clearNewData)
    { }

    void init() {
      assert(segment->context, bitsPerRecord);
      assert(segment->context, scale);
      assert(segment->context, powerOfTwo(scale));

      if (data == 0) {
        data = segment->data + segment->capacity()
          + calculateOffset(segment->capacity());
      }

      if (clearNewData) {
        memset(data, 0, size() * BytesPerWord);
      }

      if (child) {
        child->init();
      }
    }

    unsigned calculateOffset(unsigned capacity) {
      unsigned n = 0;
      if (child) n += child->calculateFootprint(capacity);
      return n;
    }

    static unsigned calculateSize(Context* c UNUSED, unsigned capacity,
                                  unsigned scale, unsigned bitsPerRecord)
    {
      unsigned result
        = ceiling(ceiling(capacity, scale) * bitsPerRecord, BitsPerWord);
      assert(c, result);
      return result;
    }

    unsigned calculateSize(unsigned capacity) {
      return calculateSize(segment->context, capacity, scale, bitsPerRecord);
    }

    unsigned size() {
      return calculateSize(segment->capacity());
    }

    unsigned calculateFootprint(unsigned capacity) {
      unsigned n = calculateSize(capacity);
      if (child) n += child->calculateFootprint(capacity);
      return n;
    }

    void replaceWith(Map* m) {
      assert(segment->context, bitsPerRecord == m->bitsPerRecord);
      assert(segment->context, scale == m->scale);

      data = m->data;

      m->segment = 0;
      m->data = 0;
      
      if (child) child->replaceWith(m->child);
    }

    unsigned indexOf(unsigned segmentIndex) {
      return (segmentIndex / scale) * bitsPerRecord;
    }

    unsigned indexOf(void* p) {
      assert(segment->context, segment->almostContains(p));
      assert(segment->context, segment->capacity());
      return indexOf(segment->indexOf(p));
    }

    void clearBit(unsigned i) {
      assert(segment->context, wordOf(i) < size());

      vm::clearBit(data, i);
    }

    void setBit(unsigned i) {
      assert(segment->context, wordOf(i) < size());

      vm::markBit(data, i);
    }

    void clearOnlyIndex(unsigned index) {
      clearBits(data, bitsPerRecord, index);
    }

    void clearOnly(unsigned segmentIndex) {
      clearOnlyIndex(indexOf(segmentIndex));
    }

    void clearOnly(void* p) {
      clearOnlyIndex(indexOf(p));
    }

    void clear(void* p) {
      clearOnly(p);
      if (child) child->clear(p);
    }

    void setOnlyIndex(unsigned index, unsigned v = 1) {
      setBits(data, bitsPerRecord, index, v);
    }

    void setOnly(unsigned segmentIndex, unsigned v = 1) {
      setOnlyIndex(indexOf(segmentIndex), v);
    }

    void setOnly(void* p, unsigned v = 1) {
      setOnlyIndex(indexOf(p), v);
    }

    void set(void* p, unsigned v = 1) {
      setOnly(p, v);
      assert(segment->context, get(p) == v);
      if (child) child->set(p, v);
    }

#ifdef USE_ATOMIC_OPERATIONS
    void markAtomic(void* p) {
      assert(segment->context, bitsPerRecord == 1);
      markBitAtomic(data, indexOf(p));
      assert(segment->context, getBit(data, indexOf(p)));
      if (child) child->markAtomic(p);
    }
#endif

    unsigned get(void* p) {
      return getBits(data, bitsPerRecord, indexOf(p));
    }
  };

  Context* context;
  uintptr_t* data;
  unsigned position_;
  unsigned capacity_;
  Map* map;

  Segment(Context* context, Map* map, unsigned desired, unsigned minimum):
    context(context),
    data(0),
    position_(0),
    capacity_(0),
    map(map)
  {
    if (desired) {
      if (minimum == 0) {
        minimum = 1;
      }
      
      assert(context, desired >= minimum);

      capacity_ = desired;
      while (data == 0) {
        data = static_cast<uintptr_t*>
          (tryAllocate(context, (footprint(capacity_)) * BytesPerWord));

        if (data == 0) {
          if (capacity_ > minimum) {
            capacity_ = avg(minimum, capacity_);
            if (capacity_ == 0) {
              break;
            }
          } else {
            data = static_cast<uintptr_t*>
              (local::allocate
               (context, (footprint(capacity_)) * BytesPerWord));
          }
        }
      }

      if (map) {
        map->init();
      }
    }
  }

  Segment(Context* context, Map* map, uintptr_t* data, unsigned position,
          unsigned capacity):
    context(context),
    data(data),
    position_(position),
    capacity_(capacity),
    map(map)
  {
    if (map) {
      map->init();
    }
  }

  unsigned footprint(unsigned capacity) {
    return capacity
      + (map and capacity ? map->calculateFootprint(capacity) : 0);
  }

  unsigned capacity() {
    return capacity_;
  }

  unsigned position() {
    return position_;
  }

  unsigned remaining() {
    return capacity() - position();
  }

  void replaceWith(Segment* s) {
    if (data) {
      free(context, data, (footprint(capacity())) * BytesPerWord);
    }
    data = s->data;
    s->data = 0;

    position_ = s->position_;
    s->position_ = 0;

    capacity_ = s->capacity_;
    s->capacity_ = 0;

    if (s->map) {
      if (map) {
        map->replaceWith(s->map);
        s->map = 0;
      } else {
        abort(context);
      }
    } else {
      assert(context, map == 0);
    }    
  }

  bool contains(void* p) {
    return position() and p >= data and p < data + position();
  }

  bool almostContains(void* p) {
    return contains(p) or p == data + position();
  }

  void* get(unsigned offset) {
    assert(context, offset <= position());
    return data + offset;
  }

  unsigned indexOf(void* p) {
    assert(context, almostContains(p));
    return static_cast<uintptr_t*>(p) - data;
  }

  void* allocate(unsigned size) {
    assert(context, size);
    assert(context, position() + size <= capacity());

    void* p = data + position();
    position_ += size;
    return p;
  }

  void dispose() {
    if (data) {
      free(context, data, (footprint(capacity())) * BytesPerWord);
    }
    data = 0;
    map = 0;
  }
};

class Fixie {
 public:
  Fixie(Context* c, unsigned size, bool hasMask, Fixie** handle,
        bool immortal):
    age(immortal ? FixieTenureThreshold + 1 : 0),
    hasMask(hasMask),
    marked(false),
    dirty(false),
    size(size),
    next(0),
    handle(0)
  {
    memset(mask(), 0, maskSize(size, hasMask));
    add(c, handle);
    if (DebugFixies) {
      fprintf(stderr, "make fixie %p of size %d\n", this, totalSize());
    }
  }

  bool immortal() {
    return age == FixieTenureThreshold + 1;
  }

  void add(Context* c UNUSED, Fixie** handle) {
    assert(c, this->handle == 0);
    assert(c, next == 0);

    this->handle = handle;
    if (handle) {
      next = *handle;
      if (next) next->handle = &next;
      *handle = this;
    } else {
      next = 0;
    }
  }

  void remove(Context* c UNUSED) {
    if (handle) {
      assert(c, *handle == this);
      *handle = next;
    }
    if (next) {
      next->handle = handle;
    }
    next = 0;
    handle = 0;
  }

  void move(Context* c, Fixie** handle) {
    if (DebugFixies) {
      fprintf(stderr, "move fixie %p\n", this);
    }

    remove(c);
    add(c, handle);
  }

  void** body() {
    return static_cast<void**>(static_cast<void*>(body_));
  }

  uintptr_t* mask() {
    return body_ + size;
  }

  static unsigned maskSize(unsigned size, bool hasMask) {
    return hasMask * ceiling(size, BitsPerWord) * BytesPerWord;
  }

  static unsigned totalSize(unsigned size, bool hasMask) {
    return sizeof(Fixie) + (size * BytesPerWord) + maskSize(size, hasMask);
  }

  unsigned totalSize() {
    return totalSize(size, hasMask);
  }

  // be sure to update e.g. TargetFixieSizeInBytes in bootimage.cpp if
  // you add/remove/change fields in this class:

  uint8_t age;
  uint8_t hasMask;
  uint8_t marked;
  uint8_t dirty;
  uint32_t size;
  Fixie* next;
  Fixie** handle;
  uintptr_t body_[0];
};

Fixie*
fixie(void* body)
{
  return static_cast<Fixie*>(body) - 1;
}

void
free(Context* c, Fixie** fixies, bool resetImmortal = false);

class Context {
 public:
  Context(System* system, unsigned limit):
    system(system),
    client(0),
    count(0),
    limit(limit),
    lowMemoryThreshold(limit / 2),
    lock(0),
    
    immortalHeapStart(0),
    immortalHeapEnd(0),

    ageMap(&gen1, max(1, log(TenureThreshold)), 1, 0, false),
    gen1(this, &ageMap, 0, 0),

    nextAgeMap(&nextGen1, max(1, log(TenureThreshold)), 1, 0, false),
    nextGen1(this, &nextAgeMap, 0, 0),

    pointerMap(&gen2, 1, 1, 0, true),
    pageMap(&gen2, 1, LikelyPageSizeInBytes / BytesPerWord, &pointerMap, true),
    heapMap(&gen2, 1, pageMap.scale * 1024, &pageMap, true),
    gen2(this, &heapMap, 0, 0),

    nextPointerMap(&nextGen2, 1, 1, 0, true),
    nextPageMap(&nextGen2, 1, LikelyPageSizeInBytes / BytesPerWord,
                &nextPointerMap, true),
    nextHeapMap(&nextGen2, 1, nextPageMap.scale * 1024, &nextPageMap, true),
    nextGen2(this, &nextHeapMap, 0, 0),

    gen2Base(0),
    incomingFootprint(0),
    tenureFootprint(0),
    gen1Padding(0),
    tenurePadding(0),
    gen2Padding(0),

    fixieTenureFootprint(0),
    untenuredFixieFootprint(0),
    tenuredFixieFootprint(0),
    tenuredFixieCeiling(InitialTenuredFixieCeilingInBytes),

    mode(Heap::MinorCollection),

    fixies(0),
    tenuredFixies(0),
    dirtyTenuredFixies(0),
    markedFixies(0),
    visitedFixies(0),

    lastCollectionTime(system->now()),
    totalCollectionTime(0),
    totalTime(0)
  {
    if (not system->success(system->make(&lock))) {
      system->abort();
    }
  }

  void dispose() {
    gen1.dispose();
    nextGen1.dispose();
    gen2.dispose();
    nextGen2.dispose();
    lock->dispose();
  }

  void disposeFixies() {
    free(this, &tenuredFixies, true);
    free(this, &dirtyTenuredFixies, true);
    free(this, &fixies, true);
  }

  System* system;
  Heap::Client* client;

  unsigned count;
  unsigned limit;
  unsigned lowMemoryThreshold;

  System::Mutex* lock;

  uintptr_t* immortalHeapStart;
  uintptr_t* immortalHeapEnd;

  Segment::Map ageMap;
  Segment gen1;

  Segment::Map nextAgeMap;
  Segment nextGen1;

  Segment::Map pointerMap;
  Segment::Map pageMap;
  Segment::Map heapMap;
  Segment gen2;

  Segment::Map nextPointerMap;
  Segment::Map nextPageMap;
  Segment::Map nextHeapMap;
  Segment nextGen2;

  unsigned gen2Base;
  
  unsigned incomingFootprint;
  unsigned tenureFootprint;
  unsigned gen1Padding;
  unsigned tenurePadding;
  unsigned gen2Padding;

  unsigned fixieTenureFootprint;
  unsigned untenuredFixieFootprint;
  unsigned tenuredFixieFootprint;
  unsigned tenuredFixieCeiling;

  Heap::CollectionType mode;

  Fixie* fixies;
  Fixie* tenuredFixies;
  Fixie* dirtyTenuredFixies;
  Fixie* markedFixies;
  Fixie* visitedFixies;

  int64_t lastCollectionTime;
  int64_t totalCollectionTime;
  int64_t totalTime;
};

inline System*
system(Context* c)
{
  return c->system;
}

const char*
segment(Context* c, void* p)
{
  if (c->gen1.contains(p)) {
    return "gen1";
  } else if (c->nextGen1.contains(p)) {
    return "nextGen1";
  } else if (c->gen2.contains(p)) {
    return "gen2";
  } else if (c->nextGen2.contains(p)) {
    return "nextGen2";
  } else {
    return "none";
  }
}

inline void NO_RETURN
abort(Context* c)
{
  abort(c->system);
}

#ifndef NDEBUG
inline void
assert(Context* c, bool v)
{
  assert(c->system, v);
}
#endif

inline unsigned
minimumNextGen1Capacity(Context* c)
{
  return c->gen1.position() - c->tenureFootprint + c->incomingFootprint
    + c->gen1Padding;
}

inline unsigned
minimumNextGen2Capacity(Context* c)
{
  return c->gen2.position() + c->tenureFootprint + c->tenurePadding
    + c->gen2Padding;
}

inline bool
oversizedGen2(Context* c)
{
  return c->gen2.capacity() > (InitialGen2CapacityInBytes / BytesPerWord)
    and c->gen2.position() < (c->gen2.capacity() / 4);
}

inline unsigned
memoryNeeded(Context* c)
{
  return c->count
    + ((c->gen1.footprint(minimumNextGen1Capacity(c))
        + c->gen2.footprint(minimumNextGen2Capacity(c))) * BytesPerWord)
    + LowMemoryPaddingInBytes;
}

inline bool
lowMemory(Context* c)
{
  return memoryNeeded(c) > c->lowMemoryThreshold;
}

inline void
initNextGen1(Context* c)
{
  new (&(c->nextAgeMap)) Segment::Map
    (&(c->nextGen1), max(1, log(TenureThreshold)), 1, 0, false);

  unsigned minimum = minimumNextGen1Capacity(c);
  unsigned desired = minimum;

  new (&(c->nextGen1)) Segment(c, &(c->nextAgeMap), desired, minimum);

  if (Verbose2) {
    fprintf(stderr, "init nextGen1 to %d bytes\n",
            c->nextGen1.capacity() * BytesPerWord);
  }
}

inline void
initNextGen2(Context* c)
{
  new (&(c->nextPointerMap)) Segment::Map
    (&(c->nextGen2), 1, 1, 0, true);

  new (&(c->nextPageMap)) Segment::Map
    (&(c->nextGen2), 1, LikelyPageSizeInBytes / BytesPerWord,
     &(c->nextPointerMap), true);

  new (&(c->nextHeapMap)) Segment::Map
    (&(c->nextGen2), 1, c->pageMap.scale * 1024, &(c->nextPageMap), true);

  unsigned minimum = minimumNextGen2Capacity(c);
  unsigned desired = minimum;

  if (not (lowMemory(c) or oversizedGen2(c))) {
    desired *= 2;
  }

  if (desired < InitialGen2CapacityInBytes / BytesPerWord) {
    desired = InitialGen2CapacityInBytes / BytesPerWord;
  }

  new (&(c->nextGen2)) Segment(c, &(c->nextHeapMap), desired, minimum);

  if (Verbose2) {
    fprintf(stderr, "init nextGen2 to %d bytes\n",
            c->nextGen2.capacity() * BytesPerWord);
  }
}

inline bool
fresh(Context* c, void* o)
{
  return c->nextGen1.contains(o)
    or c->nextGen2.contains(o)
    or (c->gen2.contains(o) and c->gen2.indexOf(o) >= c->gen2Base);
}

inline bool
wasCollected(Context* c, void* o)
{
  return o and (not fresh(c, o)) and fresh(c, get(o, 0));
}

inline void*
follow(Context* c UNUSED, void* o)
{
  assert(c, wasCollected(c, o));
  return cast<void*>(o, 0);
}

inline void*&
parent(Context* c UNUSED, void* o)
{
  assert(c, wasCollected(c, o));
  return cast<void*>(o, BytesPerWord);
}

inline uintptr_t*
bitset(Context* c UNUSED, void* o)
{
  assert(c, wasCollected(c, o));
  return &cast<uintptr_t>(o, BytesPerWord * 2);
}

void
free(Context* c, Fixie** fixies, bool resetImmortal)
{
  for (Fixie** p = fixies; *p;) {
    Fixie* f = *p;

    if (f->immortal()) {
      if (resetImmortal) {
        if (DebugFixies) {
          fprintf(stderr, "reset immortal fixie %p\n", f);
        }
        *p = f->next;
        memset(f->mask(), 0, Fixie::maskSize(f->size, f->hasMask));
        f->next = 0;
        f->handle = 0;
        f->marked = false;
        f->dirty = false;
      } else {      
        p = &(f->next);
      }
    } else {
      *p = f->next;
      if (DebugFixies) {
        fprintf(stderr, "free fixie %p\n", f);
      }
      free(c, f, f->totalSize());
    }
  }
}

void
sweepFixies(Context* c)
{
  assert(c, c->markedFixies == 0);

  if (c->mode == Heap::MajorCollection) {
    free(c, &(c->tenuredFixies));
    free(c, &(c->dirtyTenuredFixies));

    c->tenuredFixieFootprint = 0;
  }
  free(c, &(c->fixies));

  c->untenuredFixieFootprint = 0;

  while (c->visitedFixies) {
    Fixie* f = c->visitedFixies;
    f->remove(c);

    if (not f->immortal()) {
      ++ f->age;
      if (f->age > FixieTenureThreshold) {
        f->age = FixieTenureThreshold;
      } else if (static_cast<unsigned>(f->age + 1) == FixieTenureThreshold) {
        c->fixieTenureFootprint += f->totalSize();
      }
    }

    if (f->age >= FixieTenureThreshold) {
      if (DebugFixies) {
        fprintf(stderr, "tenure fixie %p (dirty: %d)\n", f, f->dirty);
      }

      if (not f->immortal()) {
        c->tenuredFixieFootprint += f->totalSize();
      }

      if (f->dirty) {
        f->add(c, &(c->dirtyTenuredFixies));
      } else {
        f->add(c, &(c->tenuredFixies));
      }
    } else {
      c->untenuredFixieFootprint += f->totalSize();

      f->add(c, &(c->fixies));
    }

    f->marked = false;
  }

  c->tenuredFixieCeiling = max
    (c->tenuredFixieFootprint * 2,
     InitialTenuredFixieCeilingInBytes);
}

inline void*
copyTo(Context* c, Segment* s, void* o, unsigned size)
{
  assert(c, s->remaining() >= size);
  void* dst = s->allocate(size);
  c->client->copy(o, dst);
  return dst;
}

bool
immortalHeapContains(Context* c, void* p)
{
  return p < c->immortalHeapEnd and p >= c->immortalHeapStart;
}

void*
copy2(Context* c, void* o)
{
  unsigned size = c->client->copiedSizeInWords(o);

  if (c->gen2.contains(o)) {
    assert(c, c->mode == Heap::MajorCollection);

    return copyTo(c, &(c->nextGen2), o, size);
  } else if (c->gen1.contains(o)) {
    unsigned age = c->ageMap.get(o);
    if (age == TenureThreshold) {
      if (c->mode == Heap::MinorCollection) {
        assert(c, c->gen2.remaining() >= size);

        if (c->gen2Base == Top) {
          c->gen2Base = c->gen2.position();
        }

        return copyTo(c, &(c->gen2), o, size);
      } else {
        return copyTo(c, &(c->nextGen2), o, size);
      }
    } else {
      o = copyTo(c, &(c->nextGen1), o, size);

      c->nextAgeMap.setOnly(o, age + 1);
      if (age + 1 == TenureThreshold) {
        c->tenureFootprint += size;
      }

      return o;
    }
  } else {
    assert(c, not c->nextGen1.contains(o));
    assert(c, not c->nextGen2.contains(o));
    assert(c, not immortalHeapContains(c, o));

    o = copyTo(c, &(c->nextGen1), o, size);

    c->nextAgeMap.clear(o);

    return o;
  }
}

void*
copy(Context* c, void* o)
{
  void* r = copy2(c, o);

  if (Debug) {
    fprintf(stderr, "copy %p (%s) to %p (%s)\n",
            o, segment(c, o), r, segment(c, r));
  }

  // leave a pointer to the copy in the original
  cast<void*>(o, 0) = r;

  return r;
}

void*
update3(Context* c, void* o, bool* needsVisit)
{
  if (c->client->isFixed(o)) {
    Fixie* f = fixie(o);
    if ((not f->marked)
        and (c->mode == Heap::MajorCollection
             or f->age < FixieTenureThreshold))
    {
      if (DebugFixies) {
        fprintf(stderr, "mark fixie %p\n", f);
      }
      f->marked = true;
      f->move(c, &(c->markedFixies));
    }
    *needsVisit = false;
    return o;
  } else if (immortalHeapContains(c, o)) {
    *needsVisit = false;
    return o;    
  } else if (wasCollected(c, o)) {
    *needsVisit = false;
    return follow(c, o);
  } else {
    *needsVisit = true;
    return copy(c, o);
  }
}

void*
update2(Context* c, void* o, bool* needsVisit)
{
  if (c->mode == Heap::MinorCollection and c->gen2.contains(o)) {
    *needsVisit = false;
    return o;
  }

  return update3(c, o, needsVisit);
}

void
markDirty(Context* c, Fixie* f)
{
  if (not f->dirty) {
#ifdef USE_ATOMIC_OPERATIONS
    ACQUIRE(c->lock);
#endif

    if (not f->dirty) {
      f->dirty = true;
      f->move(c, &(c->dirtyTenuredFixies));
    }
  }
}

void
markClean(Context* c, Fixie* f)
{
  if (f->dirty) {
    f->dirty = false;
    if (f->immortal()) {
      f->remove(c);
    } else {
      f->move(c, &(c->tenuredFixies));
    }
  }
}

void
updateHeapMap(Context* c, void* p, void* target, unsigned offset, void* result)
{
  Segment* seg;
  Segment::Map* map;

  if (c->mode == Heap::MinorCollection) {
    seg = &(c->gen2);
    map = &(c->heapMap);
  } else {
    seg = &(c->nextGen2);
    map = &(c->nextHeapMap);
  }

  if (not (immortalHeapContains(c, result)
           or (c->client->isFixed(result)
               and fixie(result)->age >= FixieTenureThreshold)
           or seg->contains(result)))
  {
    if (target and c->client->isFixed(target)) {
      Fixie* f = fixie(target);
      assert(c, offset == 0 or f->hasMask);

      if (static_cast<unsigned>(f->age + 1) >= FixieTenureThreshold) {
        if (DebugFixies) {
          fprintf(stderr, "dirty fixie %p at %d (%p): %p\n",
                  f, offset, f->body() + offset, result);
        }

        f->dirty = true;
        markBit(f->mask(), offset);
      }
    } else if (seg->contains(p)) {
      if (Debug) {        
        fprintf(stderr, "mark %p (%s) at %p (%s)\n",
                result, segment(c, result), p, segment(c, p));
      }

      map->set(p);
    }
  }
}

void*
update(Context* c, void** p, void* target, unsigned offset, bool* needsVisit)
{
  if (mask(*p) == 0) {
    *needsVisit = false;
    return 0;
  }

  void* result = update2(c, mask(*p), needsVisit);

  if (result) {
    updateHeapMap(c, p, target, offset, result);
  }

  return result;
}

const uintptr_t BitsetExtensionBit
= (static_cast<uintptr_t>(1) << (BitsPerWord - 1));

void
bitsetInit(uintptr_t* p)
{
  memset(p, 0, BytesPerWord);
}

void
bitsetClear(uintptr_t* p, unsigned start, unsigned end)
{
  if (end < BitsPerWord - 1) {
    // do nothing
  } else if (start < BitsPerWord - 1) {
    memset(p + 1, 0, (wordOf(end + (BitsPerWord * 2) + 1)) * BytesPerWord);
  } else {
    unsigned startWord = wordOf(start + (BitsPerWord * 2) + 1);
    unsigned endWord = wordOf(end + (BitsPerWord * 2) + 1);
    if (endWord > startWord) {
      memset(p + startWord + 1, 0, (endWord - startWord) * BytesPerWord);
    }
  }
}

void
bitsetSet(uintptr_t* p, unsigned i, bool v)
{
  if (i >= BitsPerWord - 1) {
    i += (BitsPerWord * 2) + 1;
    if (v) {
      p[0] |= BitsetExtensionBit;
      if (p[2] <= wordOf(i) - 3) p[2] = wordOf(i) - 2;
    }
  }

  if (v) {
    markBit(p, i);
  } else {
    clearBit(p, i);
  }
}

bool
bitsetHasMore(uintptr_t* p)
{
  switch (*p) {
  case 0: return false;

  case BitsetExtensionBit: {
    uintptr_t length = p[2];
    uintptr_t word = wordOf(p[1]);
    for (; word < length; ++word) {
      if (p[word + 3]) {
        p[1] = indexOf(word, 0);
        return true;
      }
    }
    p[1] = indexOf(word, 0);
    return false;
  }

  default: return true;
  }
}

unsigned
bitsetNext(Context* c, uintptr_t* p)
{
  bool more UNUSED = bitsetHasMore(p);
  assert(c, more);

  switch (*p) {
  case 0: abort(c);

  case BitsetExtensionBit: {
    uintptr_t i = p[1];
    uintptr_t word = wordOf(i);
    assert(c, word < p[2]);
    for (uintptr_t bit = bitOf(i); bit < BitsPerWord; ++bit) {
      if (p[word + 3] & (static_cast<uintptr_t>(1) << bit)) {
        p[1] = indexOf(word, bit) + 1;
        bitsetSet(p, p[1] + BitsPerWord - 2, false);
        return p[1] + BitsPerWord - 2;
      }
    }
    abort(c);
  }

  default: {
    for (unsigned i = 0; i < BitsPerWord - 1; ++i) {
      if (*p & (static_cast<uintptr_t>(1) << i)) {
        bitsetSet(p, i, false);
        return i;
      }
    }
    abort(c);
  }
  }
}

void
collect(Context* c, void** p, void* target, unsigned offset)
{
  void* original = mask(*p);
  void* parent_ = 0;
  
  if (Debug) {
    fprintf(stderr, "update %p (%s) at %p (%s)\n",
            mask(*p), segment(c, *p), p, segment(c, p));
  }

  bool needsVisit;
  local::set(p, update(c, mask(p), target, offset, &needsVisit));

  if (Debug) {
    fprintf(stderr, "  result: %p (%s) (visit? %d)\n",
            mask(*p), segment(c, *p), needsVisit);
  }

  if (not needsVisit) return;

 visit: {
    void* copy = follow(c, original);

    class Walker : public Heap::Walker {
     public:
      Walker(Context* c, void* copy, uintptr_t* bitset):
        c(c),
        copy(copy),
        bitset(bitset),
        first(0),
        second(0),
        last(0),
        visits(0),
        total(0)
      { }

      virtual bool visit(unsigned offset) {
        if (Debug) {
          fprintf(stderr, "  update %p (%s) at %p - offset %d from %p (%s)\n",
                  get(copy, offset),
                  segment(c, get(copy, offset)),
                  getp(copy, offset),
                  offset,
                  copy,
                  segment(c, copy));
        }

        bool needsVisit;
        void* childCopy = update
          (c, getp(copy, offset), copy, offset, &needsVisit);
        
        if (Debug) {
          fprintf(stderr, "    result: %p (%s) (visit? %d)\n",
                  childCopy, segment(c, childCopy), needsVisit);
        }

        ++ total;

        if (total == 3) {
          bitsetInit(bitset);
        }

        if (needsVisit) {
          ++ visits;

          if (visits == 1) {
            first = offset;
          } else if (visits == 2) {
            second = offset;
          }
        } else {
          local::set(copy, offset, childCopy);
        }

        if (visits > 1 and total > 2 and (second or needsVisit)) {
          bitsetClear(bitset, last, offset);
          last = offset;

          if (second) {
            bitsetSet(bitset, second, true);
            second = 0;
          }
          
          if (needsVisit) {
            bitsetSet(bitset, offset, true);
          }
        }

        return true;
      }

      Context* c;
      void* copy;
      uintptr_t* bitset;
      unsigned first;
      unsigned second;
      unsigned last;
      unsigned visits;
      unsigned total;
    } walker(c, copy, bitset(c, original));

    if (Debug) {
      fprintf(stderr, "walk %p (%s)\n", copy, segment(c, copy));
    }

    c->client->walk(copy, &walker);

    if (walker.visits) {
      // descend
      if (walker.visits > 1) {
        parent(c, original) = parent_;
        parent_ = original;
      }

      original = get(copy, walker.first);
      local::set(copy, walker.first, follow(c, original));
      goto visit;
    } else {
      // ascend
      original = parent_;
    }
  }

  if (original) {
    void* copy = follow(c, original);

    class Walker : public Heap::Walker {
     public:
      Walker(Context* c, uintptr_t* bitset):
        c(c),
        bitset(bitset),
        next(0),
        total(0)
      { }

      virtual bool visit(unsigned offset) {
        switch (++ total) {
        case 1:
          return true;

        case 2:
          next = offset;
          return true;
          
        case 3:
          next = bitsetNext(c, bitset);
          return false;

        default:
          abort(c);
        }
      }

      Context* c;
      uintptr_t* bitset;
      unsigned next;
      unsigned total;
    } walker(c, bitset(c, original));

    if (Debug) {
      fprintf(stderr, "scan %p\n", copy);
    }

    c->client->walk(copy, &walker);

    assert(c, walker.total > 1);

    if (walker.total == 3 and bitsetHasMore(bitset(c, original))) {
      parent_ = original;
    } else {
      parent_ = parent(c, original);
    }

    if (Debug) {
      fprintf(stderr, "  next is %p (%s) at %p - offset %d from %p (%s)\n",
              get(copy, walker.next),
              segment(c, get(copy, walker.next)),
              getp(copy, walker.next),
              walker.next,
              copy,
              segment(c, copy));
    }

    original = get(copy, walker.next);
    local::set(copy, walker.next, follow(c, original));
    goto visit;
  } else {
    return;
  }
}

void
collect(Context* c, void** p)
{
  collect(c, p, 0, 0);
}

void
collect(Context* c, void* target, unsigned offset)
{
  collect(c, getp(target, offset), target, offset);
}

void
visitDirtyFixies(Context* c, Fixie** p)
{
  while (*p) {
    Fixie* f = *p;

    bool wasDirty UNUSED = false;
    bool clean = true;
    uintptr_t* mask = f->mask();

    unsigned word = 0;
    unsigned bit = 0;
    unsigned wordLimit = wordOf(f->size);
    unsigned bitLimit = bitOf(f->size);

    if (DebugFixies) {
      fprintf(stderr, "clean fixie %p\n", f);
    }

    for (; word <= wordLimit and (word < wordLimit or bit < bitLimit);
         ++ word)
    {
      if (mask[word]) {
        for (; bit < BitsPerWord and (word < wordLimit or bit < bitLimit);
             ++ bit)
        {
          unsigned index = indexOf(word, bit);

          if (getBit(mask, index)) {
            wasDirty = true;

            clearBit(mask, index);

            if (DebugFixies) {
              fprintf(stderr, "clean fixie %p at %d (%p)\n",
                      f, index, f->body() + index);
            }

            collect(c, f->body(), index);

            if (getBit(mask, index)) {
              clean = false;
            }
          }
        }
        bit = 0;
      }
    }

    if (DebugFixies) {
      fprintf(stderr, "done cleaning fixie %p\n", f);
    }

    assert(c, wasDirty);

    if (clean) {
      markClean(c, f);
    } else {
      p = &(f->next);
    }
  }
}

void
visitMarkedFixies(Context* c)
{
  while (c->markedFixies) {
    Fixie* f = c->markedFixies;
    f->remove(c);

    if (DebugFixies) {
      fprintf(stderr, "visit fixie %p\n", f);
    }

    class Walker: public Heap::Walker {
     public:
      Walker(Context* c, void** p):
        c(c), p(p)
      { }

      virtual bool visit(unsigned offset) {
        local::collect(c, p, offset);
        return true;
      }

      Context* c;
      void** p;
    } w(c, f->body());

    c->client->walk(f->body(), &w);

    f->move(c, &(c->visitedFixies));
  }  
}

void
collect(Context* c, Segment::Map* map, unsigned start, unsigned end,
        bool* dirty, bool expectDirty UNUSED)
{
  bool wasDirty UNUSED = false;
  for (Segment::Map::Iterator it(map, start, end); it.hasMore();) {
    wasDirty = true;
    if (map->child) {
      assert(c, map->scale > 1);
      unsigned s = it.next();
      unsigned e = s + map->scale;

      map->clearOnly(s);
      bool childDirty = false;
      collect(c, map->child, s, e, &childDirty, true);
      if (childDirty) {
        map->setOnly(s);
        *dirty = true;
      }
    } else {
      assert(c, map->scale == 1);
      void** p = reinterpret_cast<void**>(map->segment->get(it.next()));

      map->clearOnly(p);
      if (c->nextGen1.contains(*p)) {
        map->setOnly(p);
        *dirty = true;
      } else {
        collect(c, p);

        if (not c->gen2.contains(*p)) {
          map->setOnly(p);
          *dirty = true;
        }
      }
    }
  }

  assert(c, wasDirty or not expectDirty);
}

void
collect2(Context* c)
{
  c->gen2Base = Top;
  c->tenureFootprint = 0;
  c->fixieTenureFootprint = 0;
  c->gen1Padding = 0;
  c->tenurePadding = 0;

  if (c->mode == Heap::MajorCollection) {
    c->gen2Padding = 0;
  }

  if (c->mode == Heap::MinorCollection and c->gen2.position()) {
    unsigned start = 0;
    unsigned end = start + c->gen2.position();
    bool dirty;
    collect(c, &(c->heapMap), start, end, &dirty, false);
  }

  if (c->mode == Heap::MinorCollection) {
    visitDirtyFixies(c, &(c->dirtyTenuredFixies));
  }

  class Visitor : public Heap::Visitor {
   public:
    Visitor(Context* c): c(c) { }

    virtual void visit(void* p) {
      local::collect(c, static_cast<void**>(p));
      visitMarkedFixies(c);
    }

    Context* c;
  } v(c);

  c->client->visitRoots(&v);
}

void
collect(Context* c)
{
  if (lowMemory(c)
      or oversizedGen2(c)
      or c->tenureFootprint + c->tenurePadding > c->gen2.remaining()
      or c->fixieTenureFootprint + c->tenuredFixieFootprint
      > c->tenuredFixieCeiling)
  {
    if (Verbose) {
      if (lowMemory(c)) {
        fprintf(stderr, "low memory causes ");        
      } else if (oversizedGen2(c)) {
        fprintf(stderr, "oversized gen2 causes ");
      } else if (c->tenureFootprint + c->tenurePadding > c->gen2.remaining())
      {
        fprintf(stderr, "undersized gen2 causes ");
      } else {
        fprintf(stderr, "fixie ceiling causes ");
      }
    }

    c->mode = Heap::MajorCollection;
  }

  int64_t then;
  if (Verbose) {
    if (c->mode == Heap::MajorCollection) {
      fprintf(stderr, "major collection\n");
    } else {
      fprintf(stderr, "minor collection\n");
    }

    then = c->system->now();
  }

  unsigned count = memoryNeeded(c);
  if (count > c->lowMemoryThreshold) {
    if (Verbose) {
      fprintf(stderr, "increase low memory threshold from %d to %d\n",
              c->lowMemoryThreshold,
              avg(c->limit, c->lowMemoryThreshold));
    }

    c->lowMemoryThreshold = avg(c->limit, c->lowMemoryThreshold);
  } else if (count + (count / 16) < c->lowMemoryThreshold) {
    if (Verbose) {
      fprintf(stderr, "decrease low memory threshold from %d to %d\n",
              c->lowMemoryThreshold,
              avg(count, c->lowMemoryThreshold));
    }

    c->lowMemoryThreshold = avg(count, c->lowMemoryThreshold);
  }

  initNextGen1(c);

  if (c->mode == Heap::MajorCollection) {
    initNextGen2(c);
  }

  collect2(c);

  c->gen1.replaceWith(&(c->nextGen1));
  if (c->mode == Heap::MajorCollection) {
    c->gen2.replaceWith(&(c->nextGen2));
  }

  sweepFixies(c);

  if (Verbose) {
    int64_t now = c->system->now();
    int64_t collection = now - then;
    int64_t run = then - c->lastCollectionTime;
    c->totalCollectionTime += collection;
    c->totalTime += collection + run;
    c->lastCollectionTime = now;

    fprintf(stderr,
            " - collect: %4dms; "
            "total: %4dms; "
            "run: %4dms; "
            "total: %4dms\n",
            static_cast<int>(collection),
            static_cast<int>(c->totalCollectionTime),
            static_cast<int>(run),
            static_cast<int>(c->totalTime - c->totalCollectionTime));

    fprintf(stderr,
            " -             gen1: %8d/%8d bytes\n",
            c->gen1.position() * BytesPerWord,
            c->gen1.capacity() * BytesPerWord);

    fprintf(stderr,
            " -             gen2: %8d/%8d bytes\n",
            c->gen2.position() * BytesPerWord,
            c->gen2.capacity() * BytesPerWord);

    fprintf(stderr,
            " - untenured fixies:          %8d bytes\n",
            c->untenuredFixieFootprint);

    fprintf(stderr,
            " -   tenured fixies:          %8d bytes\n",
            c->tenuredFixieFootprint);
  }
}

void*
allocate(Context* c, unsigned size, bool limit)
{
  ACQUIRE(c->lock);

  if (DebugAllocation) {
    size = pad(size) + 2 * BytesPerWord;
  }

  if ((not limit) or size + c->count < c->limit) {
    void* p = c->system->tryAllocate(size);
    if (p) {
      c->count += size;
      
      if (DebugAllocation) {
        static_cast<uintptr_t*>(p)[0] = 0x22377322;
        static_cast<uintptr_t*>(p)[(size / BytesPerWord) - 1] = 0x22377322;
        return static_cast<uintptr_t*>(p) + 1;
      } else {
        return p;
      }
    }
  }
  return 0;
}

void*
tryAllocate(Context* c, unsigned size)
{
  return allocate(c, size, true);
}

void*
allocate(Context* c, unsigned size)
{
  void* p = allocate(c, size, false);
  expect(c->system, p);

  return p;
}

void
free(Context* c, const void* p, unsigned size)
{
  ACQUIRE(c->lock);

  if (DebugAllocation) {
    size = pad(size) + 2 * BytesPerWord;

    memset(const_cast<void*>(p), 0xFE, size - (2 * BytesPerWord));

    p = static_cast<const uintptr_t*>(p) - 1;

    expect(c->system, static_cast<const uintptr_t*>(p)[0] == 0x22377322);

    expect(c->system, static_cast<const uintptr_t*>(p)
           [(size / BytesPerWord) - 1] == 0x22377322);
  }

  expect(c->system, c->count >= size);

  c->system->free(p);
  c->count -= size;
}

void
free_(Context* c, const void* p, unsigned size)
{
  free(c, p, size);
}

class MyHeap: public Heap {
 public:
  MyHeap(System* system, unsigned limit):
    c(system, limit)
  { }

  virtual void setClient(Heap::Client* client) {
    assert(&c, c.client == 0);
    c.client = client;
  }

  virtual void setImmortalHeap(uintptr_t* start, unsigned sizeInWords) {
    c.immortalHeapStart = start;
    c.immortalHeapEnd = start + sizeInWords;
  }

  virtual bool limitExceeded() {
    return c.count > c.limit;
  }

  virtual void* tryAllocate(unsigned size) {
    return local::tryAllocate(&c, size);
  }

  virtual void* allocate(unsigned size) {
    return local::allocate(&c, size);
  }

  virtual void free(const void* p, unsigned size) {
    free_(&c, p, size);
  }

  virtual void collect(CollectionType type, unsigned incomingFootprint) {
    c.mode = type;
    c.incomingFootprint = incomingFootprint;

    local::collect(&c);
  }

  virtual void* allocateFixed(Allocator* allocator, unsigned sizeInWords,
                              bool objectMask, unsigned* totalInBytes)
  {
    *totalInBytes = Fixie::totalSize(sizeInWords, objectMask);
    return (new (allocator->allocate(*totalInBytes))
            Fixie(&c, sizeInWords, objectMask, &(c.fixies), false))->body();
  }

  virtual void* allocateImmortalFixed(Allocator* allocator,
                                      unsigned sizeInWords, bool objectMask,
                                      unsigned* totalInBytes)
  {
    *totalInBytes = Fixie::totalSize(sizeInWords, objectMask);
    return (new (allocator->allocate(*totalInBytes))
            Fixie(&c, sizeInWords, objectMask, 0, true))->body();
  }

  bool needsMark(void* p) {
    assert(&c, c.client->isFixed(p) or (not immortalHeapContains(&c, p)));

    if (c.client->isFixed(p)) {
      return fixie(p)->age >= FixieTenureThreshold;
    } else {
      return c.gen2.contains(p) or c.nextGen2.contains(p);
    }
  }

  bool targetNeedsMark(void* target) {
    return target
      and not c.gen2.contains(target)
      and not c.nextGen2.contains(target)
      and not immortalHeapContains(&c, target)
      and not (c.client->isFixed(target)
               and fixie(target)->age >= FixieTenureThreshold);
  }

  virtual void mark(void* p, unsigned offset, unsigned count) {
    if (needsMark(p)) {
#ifndef USE_ATOMIC_OPERATIONS
      ACQUIRE(c.lock);
#endif

      if (c.client->isFixed(p)) {
        Fixie* f = fixie(p);
        assert(&c, offset == 0 or f->hasMask);

        bool dirty = false;
        for (unsigned i = 0; i < count; ++i) {
          void** target = static_cast<void**>(p) + offset + i;
          if (targetNeedsMark(mask(*target))) {
            if (DebugFixies) {
              fprintf(stderr, "dirty fixie %p at %d (%p): %p\n",
                      f, offset, f->body() + offset, mask(*target));
            }

            dirty = true;
#ifdef USE_ATOMIC_OPERATIONS
            markBitAtomic(f->mask(), offset + i);
#else
            markBit(f->mask(), offset + i);
#endif
            assert(&c, getBit(f->mask(), offset + i));
          }
        }

        if (dirty) markDirty(&c, f);
      } else {
        Segment::Map* map;
        if (c.gen2.contains(p)) {
          map = &(c.heapMap);
        } else {
          assert(&c, c.nextGen2.contains(p));
          map = &(c.nextHeapMap);
        }

        for (unsigned i = 0; i < count; ++i) {
          void** target = static_cast<void**>(p) + offset + i;
          if (targetNeedsMark(mask(*target))) {
#ifdef USE_ATOMIC_OPERATIONS
            map->markAtomic(target);
#else
            map->set(target);
#endif
          }
        }
      }
    }
  }

  virtual void pad(void* p) {
    if (c.gen1.contains(p)) {
      if (c.ageMap.get(p) == TenureThreshold) {
        ++ c.tenurePadding;
      } else {
        ++ c.gen1Padding;
      }
    } else if (c.gen2.contains(p)) {
      ++ c.gen2Padding;
    } else {
      ++ c.gen1Padding;
    }
  }

  virtual void* follow(void* p) {
    if (p == 0 or c.client->isFixed(p)) {
      return p;
    } else if (wasCollected(&c, p)) {
      if (Debug) {
        fprintf(stderr, "follow %p (%s) to %p (%s)\n",
                p, segment(&c, p),
                local::follow(&c, p), segment(&c, local::follow(&c, p)));
      }

      return local::follow(&c, p);
    } else {
      return p;
    }
  }

  virtual Status status(void* p) {
    p = mask(p);

    if (p == 0) {
      return Null;
    } else if (c.nextGen1.contains(p)) {
      return Reachable;
    } else if (c.nextGen2.contains(p)
               or immortalHeapContains(&c, p)
               or (c.gen2.contains(p)
                   and (c.mode == Heap::MinorCollection
                        or c.gen2.indexOf(p) >= c.gen2Base)))
    {
      return Tenured;
    } else if (wasCollected(&c, p)) {
      return Reachable;
    } else {
      return Unreachable;
    }
  }

  virtual CollectionType collectionType() {
    return c.mode;
  }

  virtual void disposeFixies() {
    c.disposeFixies();
  }

  virtual void dispose() {
    c.dispose();
    assert(&c, c.count == 0);
    c.system->free(this);
  }

  Context c;
};

} // namespace local

} // namespace

namespace vm {

Heap*
makeHeap(System* system, unsigned limit)
{  
  return new (system->tryAllocate(sizeof(local::MyHeap)))
    local::MyHeap(system, limit);
}

} // namespace vm

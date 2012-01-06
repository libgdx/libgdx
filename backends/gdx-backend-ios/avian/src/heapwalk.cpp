/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "machine.h"
#include "heapwalk.h"

using namespace vm;

namespace {

namespace local {

const uintptr_t PointerShift = log(BytesPerWord);

class Context;

class Set: public HeapMap {
 public:
  class Entry {
   public:
    object value;
    uint32_t number;
    int next;
  };

  static unsigned footprint(unsigned capacity) {
    return sizeof(Set)
      + pad(sizeof(int) * capacity)
      + pad(sizeof(Set::Entry) * capacity);
  }

  Set(Context* context, unsigned capacity):
    context(context),
    index(reinterpret_cast<int*>
          (reinterpret_cast<uint8_t*>(this)
           + sizeof(Set))),
    entries(reinterpret_cast<Entry*>
            (reinterpret_cast<uint8_t*>(index) 
             + pad(sizeof(int) * capacity))),
    size(0),
    capacity(capacity)
  { }

  virtual int find(object value);

  virtual void dispose();

  Context* context;
  int* index;
  Entry* entries;
  unsigned size;
  unsigned capacity;
};

class Stack {
 public:
  class Entry {
   public:
    object value;
    int offset;
  };

  static const unsigned Capacity = 4096;

  Stack(Stack* next): next(next), entryCount(0) { }

  Stack* next;
  unsigned entryCount;
  Entry entries[Capacity];
};

class Context {
 public:
  Context(Thread* thread):
    thread(thread), objects(0), stack(0)
  { }

  void dispose() {
    if (objects) {
      objects->dispose();
    }

    while (stack) {
      Stack* dead = stack;
      stack = dead->next;
      thread->m->heap->free(dead, sizeof(Stack));
    }
  }

  Thread* thread;
  Set* objects;
  Stack* stack;
};

void
push(Context* c, object p, int offset)
{
  if (c->stack == 0 or c->stack->entryCount == Stack::Capacity) {
    c->stack = new (c->thread->m->heap->allocate(sizeof(Stack)))
      Stack(c->stack);
  }
  Stack::Entry* e = c->stack->entries + (c->stack->entryCount++);
  e->value = p;
  e->offset = offset;
}

bool
pop(Context* c, object* p, int* offset)
{
  if (c->stack) {
    if (c->stack->entryCount == 0) {
      if (c->stack->next) {
        Stack* dead = c->stack;
        c->stack = dead->next;
        c->thread->m->heap->free(dead, sizeof(Stack));
      } else {
        return false;
      }
    }
    Stack::Entry* e = c->stack->entries + (--c->stack->entryCount);
    *p = e->value;
    *offset = e->offset;
    return true;
  } else {
    return false;
  }
}

unsigned
hash(object p, unsigned capacity)
{
  return (reinterpret_cast<uintptr_t>(p) >> PointerShift)
    & (capacity - 1);
}

Set::Entry*
find(Context* c, object p)
{
  if (c->objects == 0) return 0;

  for (int i = c->objects->index[hash(p, c->objects->capacity)]; i >= 0;) {
    Set::Entry* e = c->objects->entries + i;
    if (e->value == p) {
      return e;
    }
    i = e->next;
  }

  return 0;
}

int
Set::find(object value)
{
  Set::Entry* e = local::find(context, value);
  if (e) {
    return e->number;
  } else {
    return -1;
  }
}

void
Set::dispose()
{
  context->thread->m->heap->free(this, footprint(capacity));
}

Set::Entry*
add(Context* c UNUSED, Set* set, object p, uint32_t number)
{
  assert(c->thread, set->size < set->capacity);

  unsigned index = hash(p, set->capacity);

  int offset = set->size++;
  Set::Entry* e = set->entries + offset;
  e->value = p;
  e->number = number;
  e->next = set->index[index];
  set->index[index] = offset;
  return e;
}

Set::Entry*
add(Context* c, object p)
{
  if (c->objects == 0 or c->objects->size == c->objects->capacity) {
    unsigned capacity;
    if (c->objects) {
      capacity = c->objects->capacity * 2;
    } else {
      capacity = 4096; // must be power of two
    }

    Set* set = new (c->thread->m->heap->allocate(Set::footprint(capacity)))
      Set(c, capacity);

    memset(set->index, 0xFF, sizeof(int) * capacity);

    if (c->objects) {
      for (unsigned i = 0; i < c->objects->capacity; ++i) {
        for (int j = c->objects->index[i]; j >= 0;) {
          Set::Entry* e = c->objects->entries + j;
          add(c, set, e->value, e->number);
          j = e->next;
        }
      }

      c->thread->m->heap->free
        (c->objects, Set::footprint(c->objects->capacity));
    }

    c->objects = set;
  }

  return add(c, c->objects, p, 0);
}

inline object
get(object o, unsigned offsetInWords)
{
  return static_cast<object>
    (mask(cast<void*>(o, offsetInWords * BytesPerWord)));
}

unsigned
objectSize(Thread* t, object o)
{
  unsigned n = baseSize(t, o, objectClass(t, o));
  if (objectExtended(t, o)) {
    ++ n;
  }
  return n;
}

unsigned
walk(Context* c, HeapVisitor* v, object p)
{
  Thread* t = c->thread;
  object root = p;
  int nextChildOffset;

  v->root();

 visit: {
    Set::Entry* e = find(c, p);
    if (e) {
      v->visitOld(p, e->number);
    } else {
      e = add(c, p);
      e->number = v->visitNew(p);

      nextChildOffset = walkNext(t, p, -1);
      if (nextChildOffset != -1) {
        goto children;
      }
    }
  }

  goto pop;

 children: {
    v->push(p, find(c, p)->number, nextChildOffset);
    push(c, p, nextChildOffset);
    p = get(p, nextChildOffset);
    goto visit;
  }

 pop: {
    if (pop(c, &p, &nextChildOffset)) {
      v->pop();
      nextChildOffset = walkNext(t, p, nextChildOffset);
      if (nextChildOffset >= 0) {
        goto children;
      } else {
        goto pop;
      }
    }
  }

  return find(c, root)->number;
}

class MyHeapWalker: public HeapWalker {
 public:
  MyHeapWalker(Thread* t, HeapVisitor* v):
    context(t), visitor(v)
  {
    add(&context, 0)->number = v->visitNew(0);
  }

  virtual unsigned visitRoot(object root) {
    return walk(&context, visitor, root);
  }

  virtual void visitAllRoots() {
    class Visitor: public Heap::Visitor {
     public:
      Visitor(Context* c, HeapVisitor* v): c(c), v(v) { }

      virtual void visit(void* p) {
        walk(c, v, static_cast<object>(mask(*static_cast<void**>(p))));
      }

      Context* c;
      HeapVisitor* v;
    } v(&context, visitor);

    visitRoots(context.thread->m, &v);
  }

  virtual HeapMap* map() {
    return context.objects;
  }

  virtual void dispose() {
    context.dispose();
    context.thread->m->heap->free(this, sizeof(MyHeapWalker));
  }

  Context context;
  HeapVisitor* visitor;
};

} // namespace local

} // namespace

namespace vm {

HeapWalker*
makeHeapWalker(Thread* t, HeapVisitor* v)
{
  return new (t->m->heap->allocate(sizeof(local::MyHeapWalker)))
    local::MyHeapWalker(t, v);
}

} // namespace vm

/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "compiler.h"
#include "assembler.h"
#include "target.h"

using namespace vm;

namespace {

namespace local {

const bool DebugAppend = false;
const bool DebugCompile = false;
const bool DebugResources = false;
const bool DebugFrame = false;
const bool DebugControl = false;
const bool DebugReads = false;
const bool DebugSites = false;
const bool DebugMoves = false;
const bool DebugBuddies = false;

const int AnyFrameIndex = -2;
const int NoFrameIndex = -1;

const unsigned StealRegisterReserveCount = 2;

// this should be equal to the largest number of registers used by a
// compare instruction:
const unsigned ResolveRegisterReserveCount = (TargetBytesPerWord == 8 ? 2 : 4);

const unsigned RegisterCopyCost = 1;
const unsigned AddressCopyCost = 2;
const unsigned ConstantCopyCost = 3;
const unsigned MemoryCopyCost = 4;
const unsigned CopyPenalty = 10;

class Context;
class Value;
class Stack;
class Site;
class ConstantSite;
class AddressSite;
class RegisterSite;
class MemorySite;
class Event;
class PushEvent;
class Read;
class MultiRead;
class StubRead;
class Block;
class Snapshot;

void NO_RETURN abort(Context*);

void
apply(Context* c, UnaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High);

void
apply(Context* c, BinaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High,
      unsigned s2Size, Site* s2Low, Site* s2High);

void
apply(Context* c, TernaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High,
      unsigned s2Size, Site* s2Low, Site* s2High,
      unsigned s3Size, Site* s3Low, Site* s3High);

class Cell {
 public:
  Cell(Cell* next, void* value): next(next), value(value) { }

  Cell* next;
  void* value;
};

class Local {
 public:
  Value* value;
};

class SiteMask {
 public:
  SiteMask(): typeMask(~0), registerMask(~0), frameIndex(AnyFrameIndex) { }

  SiteMask(uint8_t typeMask, uint32_t registerMask, int frameIndex):
    typeMask(typeMask), registerMask(registerMask), frameIndex(frameIndex)
  { }

  uint8_t typeMask;
  uint32_t registerMask;
  int frameIndex;
};

class Site {
 public:
  Site(): next(0) { }
  
  virtual Site* readTarget(Context*, Read*) { return this; }

  virtual unsigned toString(Context*, char*, unsigned) = 0;

  virtual unsigned copyCost(Context*, Site*) = 0;

  virtual bool match(Context*, const SiteMask&) = 0;

  virtual bool loneMatch(Context*, const SiteMask&) = 0;

  virtual bool matchNextWord(Context*, Site*, unsigned) = 0;
  
  virtual void acquire(Context*, Value*) { }

  virtual void release(Context*, Value*) { }

  virtual void freeze(Context*, Value*) { }

  virtual void thaw(Context*, Value*) { }

  virtual bool frozen(Context*) { return false; }

  virtual OperandType type(Context*) = 0;

  virtual void asAssemblerOperand(Context*, Site*, Assembler::Operand*) = 0;

  virtual Site* copy(Context*) = 0;

  virtual Site* copyLow(Context*) = 0;

  virtual Site* copyHigh(Context*) = 0;

  virtual Site* makeNextWord(Context*, unsigned) = 0;

  virtual SiteMask mask(Context*) = 0;

  virtual SiteMask nextWordMask(Context*, unsigned) = 0;

  virtual unsigned registerSize(Context*) { return TargetBytesPerWord; }

  virtual unsigned registerMask(Context*) { return 0; }

  virtual bool isVolatile(Context*) { return false; }

  Site* next;
};

class Stack {
 public:
  Stack(unsigned index, Value* value, Stack* next):
    index(index), value(value), next(next)
  { }

  unsigned index;
  Value* value;
  Stack* next;
};

class ForkElement {
 public:
  Value* value;
  MultiRead* read;
  bool local;
};

class ForkState: public Compiler::State {
 public:
  ForkState(Stack* stack, Local* locals, Cell* saved, Event* predecessor,
            unsigned logicalIp):
    stack(stack),
    locals(locals),
    saved(saved),
    predecessor(predecessor),
    logicalIp(logicalIp),
    readCount(0)
  { }

  Stack* stack;
  Local* locals;
  Cell* saved;
  Event* predecessor;
  unsigned logicalIp;
  unsigned readCount;
  ForkElement elements[0];
};

class MySubroutine: public Compiler::Subroutine {
 public:
  MySubroutine(): forkState(0) { }

  ForkState* forkState;
};

class LogicalInstruction {
 public:
  LogicalInstruction(int index, Stack* stack, Local* locals):
    firstEvent(0), lastEvent(0), immediatePredecessor(0), stack(stack),
    locals(locals), machineOffset(0), subroutine(0), index(index)
  { }

  Event* firstEvent;
  Event* lastEvent;
  LogicalInstruction* immediatePredecessor;
  Stack* stack;
  Local* locals;
  Promise* machineOffset;
  MySubroutine* subroutine;
  int index;
};

class Resource {
 public:
  Resource(bool reserved = false):
    value(0), site(0), previousAcquired(0), nextAcquired(0), freezeCount(0),
    referenceCount(0), reserved(reserved)
  { }

  virtual void freeze(Context*, Value*) = 0;

  virtual void thaw(Context*, Value*) = 0;

  virtual unsigned toString(Context*, char*, unsigned) = 0;

  Value* value;
  Site* site;
  Resource* previousAcquired;
  Resource* nextAcquired;
  uint8_t freezeCount;
  uint8_t referenceCount;
  bool reserved;
};

class RegisterResource: public Resource {
 public:
  RegisterResource(bool reserved):
    Resource(reserved)
  { }

  virtual void freeze(Context*, Value*);

  virtual void thaw(Context*, Value*);

  virtual unsigned toString(Context* c, char* buffer, unsigned bufferSize) {
    return vm::snprintf(buffer, bufferSize, "register %d", index(c));
  }

  virtual unsigned index(Context*);
};

class FrameResource: public Resource {
 public:
  virtual void freeze(Context*, Value*);

  virtual void thaw(Context*, Value*);

  virtual unsigned toString(Context* c, char* buffer, unsigned bufferSize) {
    return vm::snprintf(buffer, bufferSize, "frame %d", index(c));
  }

  virtual unsigned index(Context*);
};

class ConstantPoolNode {
 public:
  ConstantPoolNode(Promise* promise): promise(promise), next(0) { }

  Promise* promise;
  ConstantPoolNode* next;
};

class Read {
 public:
  Read():
    value(0), event(0), eventNext(0)
  { }

  virtual bool intersect(SiteMask* mask, unsigned depth = 0) = 0;

  virtual Value* high(Context* c) { abort(c); }

  virtual Value* successor() = 0;
  
  virtual bool valid() = 0;

  virtual void append(Context* c, Read* r) = 0;

  virtual Read* next(Context* c) = 0;

  Value* value;
  Event* event;
  Read* eventNext;
};

int
intersectFrameIndexes(int a, int b)
{
  if (a == NoFrameIndex or b == NoFrameIndex) return NoFrameIndex;
  if (a == AnyFrameIndex) return b;
  if (b == AnyFrameIndex) return a;
  if (a == b) return a;
  return NoFrameIndex;
}

SiteMask
intersect(const SiteMask& a, const SiteMask& b)
{
  return SiteMask(a.typeMask & b.typeMask, a.registerMask & b.registerMask,
                  intersectFrameIndexes(a.frameIndex, b.frameIndex));
}

class Value: public Compiler::Operand {
 public:
  Value(Site* site, Site* target, ValueType type):
    reads(0), lastRead(0), sites(site), source(0), target(target), buddy(this),
    nextWord(this), home(NoFrameIndex), type(type), wordIndex(0)
  { }
  
  Read* reads;
  Read* lastRead;
  Site* sites;
  Site* source;
  Site* target;
  Value* buddy;
  Value* nextWord;
  int16_t home;
  ValueType type;
  uint8_t wordIndex;
};

uint32_t
registerMask(Assembler::Architecture* arch)
{
  return arch->generalRegisterMask() | arch->floatRegisterMask();
}

unsigned
maskStart(uint32_t mask)
{
  for (int i = 0; i <= 31; ++i) {
    if (mask & (1 << i)) return i;
  }
  return 32;
}

unsigned
maskLimit(uint32_t mask)
{
  for (int i = 31; i >= 0; --i) {
    if (mask & (1 << i)) return i + 1;
  }
  return 0;
}

class Context {
 public:
  Context(System* system, Assembler* assembler, Zone* zone,
          Compiler::Client* client):
    system(system),
    assembler(assembler),
    arch(assembler->arch()),
    zone(zone),
    client(client),
    stack(0),
    locals(0),
    saved(0),
    predecessor(0),
    logicalCode(0),
    registerStart(maskStart(registerMask(arch))),
    registerLimit(maskLimit(registerMask(arch))),
    generalRegisterStart(maskStart(arch->generalRegisterMask())),
    generalRegisterLimit(maskLimit(arch->generalRegisterMask())),
    floatRegisterStart(maskStart(arch->floatRegisterMask())),
    floatRegisterLimit(maskLimit(arch->floatRegisterMask())),
    registerResources
    (static_cast<RegisterResource*>
     (zone->allocate(sizeof(RegisterResource) * registerLimit))),
    frameResources(0),
    acquiredResources(0),
    firstConstant(0),
    lastConstant(0),
    machineCode(0),
    firstEvent(0),
    lastEvent(0),
    forkState(0),
    subroutine(0),
    firstBlock(0),
    logicalIp(-1),
    constantCount(0),
    logicalCodeLength(0),
    parameterFootprint(0),
    localFootprint(0),
    machineCodeSize(0),
    alignedFrameSize(0),
    availableGeneralRegisterCount(generalRegisterLimit - generalRegisterStart)
  {
    for (unsigned i = generalRegisterStart; i < generalRegisterLimit; ++i) {
      new (registerResources + i) RegisterResource(arch->reserved(i));

      if (registerResources[i].reserved) {
        -- availableGeneralRegisterCount;
      }
    }
    for (unsigned i = floatRegisterStart; i < floatRegisterLimit; ++i) {
      new (registerResources + i) RegisterResource(arch->reserved(i));
    }
  }

  System* system;
  Assembler* assembler;
  Assembler::Architecture* arch;
  Zone* zone;
  Compiler::Client* client;
  Stack* stack;
  Local* locals;
  Cell* saved;
  Event* predecessor;
  LogicalInstruction** logicalCode;
  uint8_t registerStart;
  uint8_t registerLimit;
  uint8_t generalRegisterStart;
  uint8_t generalRegisterLimit;
  uint8_t floatRegisterStart;
  uint8_t floatRegisterLimit;
  RegisterResource* registerResources;
  FrameResource* frameResources;
  Resource* acquiredResources;
  ConstantPoolNode* firstConstant;
  ConstantPoolNode* lastConstant;
  uint8_t* machineCode;
  Event* firstEvent;
  Event* lastEvent;
  ForkState* forkState;
  MySubroutine* subroutine;
  Block* firstBlock;
  int logicalIp;
  unsigned constantCount;
  unsigned logicalCodeLength;
  unsigned parameterFootprint;
  unsigned localFootprint;
  unsigned machineCodeSize;
  unsigned alignedFrameSize;
  unsigned availableGeneralRegisterCount;
};

unsigned
RegisterResource::index(Context* c)
{
  return this - c->registerResources;
}

unsigned
FrameResource::index(Context* c)
{
  return this - c->frameResources;
}

class PoolPromise: public Promise {
 public:
  PoolPromise(Context* c, int key): c(c), key(key) { }

  virtual int64_t value() {
    if (resolved()) {
      return reinterpret_cast<int64_t>
        (c->machineCode + pad(c->machineCodeSize, TargetBytesPerWord)
         + (key * TargetBytesPerWord));
    }
    
    abort(c);
  }

  virtual bool resolved() {
    return c->machineCode != 0;
  }

  Context* c;
  int key;
};

class CodePromise: public Promise {
 public:
  CodePromise(Context* c, CodePromise* next):
    c(c), offset(0), next(next)
  { }

  CodePromise(Context* c, Promise* offset):
    c(c), offset(offset), next(0)
  { }

  virtual int64_t value() {
    if (resolved()) {
      return reinterpret_cast<intptr_t>(c->machineCode + offset->value());
    }
    
    abort(c);
  }

  virtual bool resolved() {
    return c->machineCode != 0 and offset and offset->resolved();
  }

  Context* c;
  Promise* offset;
  CodePromise* next;
};

unsigned
machineOffset(Context* c, int logicalIp)
{
  return c->logicalCode[logicalIp]->machineOffset->value();
}

class IpPromise: public Promise {
 public:
  IpPromise(Context* c, int logicalIp):
    c(c),
    logicalIp(logicalIp)
  { }

  virtual int64_t value() {
    if (resolved()) {
      return reinterpret_cast<intptr_t>
        (c->machineCode + machineOffset(c, logicalIp));
    }

    abort(c);
  }

  virtual bool resolved() {
    return c->machineCode != 0
      and c->logicalCode[logicalIp]->machineOffset->resolved();
  }

  Context* c;
  int logicalIp;
};

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
#endif // not NDEBUG

inline void
expect(Context* c, bool v)
{
  expect(c->system, v);
}

unsigned
count(Cell* c)
{
  unsigned count = 0;
  while (c) {
    ++ count;
    c = c->next;
  }
  return count;
}

Cell*
cons(Context* c, void* value, Cell* next)
{
  return new (c->zone->allocate(sizeof(Cell))) Cell(next, value);
}

Cell*
append(Context* c, Cell* first, Cell* second)
{
  if (first) {
    if (second) {
      Cell* start = cons(c, first->value, second);
      Cell* end = start;
      for (Cell* cell = first->next; cell; cell = cell->next) {
        Cell* n = cons(c, cell->value, second);
        end->next = n;
        end = n;
      }
      return start;
    } else {
      return first;
    }
  } else {
    return second;
  }
}

Cell*
reverseDestroy(Cell* cell)
{
  Cell* previous = 0;
  while (cell) {
    Cell* next = cell->next;
    cell->next = previous;
    previous = cell;
    cell = next;
  }
  return previous;
}

class StubReadPair {
 public:
  Value* value;
  StubRead* read;
};

class JunctionState {
 public:
  JunctionState(unsigned frameFootprint): frameFootprint(frameFootprint) { }

  unsigned frameFootprint;
  StubReadPair reads[0];
};

class Link {
 public:
  Link(Event* predecessor, Link* nextPredecessor, Event* successor,
       Link* nextSuccessor, ForkState* forkState):
    predecessor(predecessor), nextPredecessor(nextPredecessor),
    successor(successor), nextSuccessor(nextSuccessor), forkState(forkState),
    junctionState(0)
  { }

  Event* predecessor;
  Link* nextPredecessor;
  Event* successor;
  Link* nextSuccessor;
  ForkState* forkState;
  JunctionState* junctionState;
};

Link*
link(Context* c, Event* predecessor, Link* nextPredecessor, Event* successor,
     Link* nextSuccessor, ForkState* forkState)
{
  return new (c->zone->allocate(sizeof(Link))) Link
    (predecessor, nextPredecessor, successor, nextSuccessor, forkState);
}

unsigned
countPredecessors(Link* link)
{
  unsigned c = 0;
  for (; link; link = link->nextPredecessor) ++ c;
  return c;
}

Link*
lastPredecessor(Link* link)
{
  while (link->nextPredecessor) link = link->nextPredecessor;
  return link;
}

unsigned
countSuccessors(Link* link)
{
  unsigned c = 0;
  for (; link; link = link->nextSuccessor) ++ c;
  return c;
}

class Event {
 public:
  Event(Context* c):
    next(0), stackBefore(c->stack), localsBefore(c->locals),
    stackAfter(0), localsAfter(0), promises(0), reads(0),
    junctionSites(0), snapshots(0), predecessors(0), successors(0),
    visitLinks(0), block(0), logicalInstruction(c->logicalCode[c->logicalIp]),
    readCount(0)
  { }

  virtual const char* name() = 0;

  virtual void compile(Context* c) = 0;

  virtual bool isBranch() { return false; }

  virtual bool allExits() { return false; }

  Event* next;
  Stack* stackBefore;
  Local* localsBefore;
  Stack* stackAfter;
  Local* localsAfter;
  CodePromise* promises;
  Read* reads;
  Site** junctionSites;
  Snapshot* snapshots;
  Link* predecessors;
  Link* successors;
  Cell* visitLinks;
  Block* block;
  LogicalInstruction* logicalInstruction;
  unsigned readCount;
};

unsigned
totalFrameSize(Context* c)
{
  return c->alignedFrameSize
    + c->arch->frameHeaderSize()
    + c->arch->argumentFootprint(c->parameterFootprint);
}

int
frameIndex(Context* c, int localIndex)
{
  assert(c, localIndex >= 0);

  int index = c->alignedFrameSize + c->parameterFootprint - localIndex - 1;

  if (localIndex < static_cast<int>(c->parameterFootprint)) {
    index += c->arch->frameHeaderSize();
  } else {
    index -= c->arch->frameFooterSize();
  }

  assert(c, index >= 0);
  assert(c, static_cast<unsigned>(index) < totalFrameSize(c));

  return index;
}

unsigned
frameIndexToOffset(Context* c, unsigned frameIndex)
{
  assert(c, frameIndex < totalFrameSize(c));

  return (frameIndex + c->arch->frameFooterSize()) * TargetBytesPerWord;
}

unsigned
offsetToFrameIndex(Context* c, unsigned offset)
{
  assert(c, static_cast<int>
         ((offset / TargetBytesPerWord) - c->arch->frameFooterSize()) >= 0);
  assert(c, ((offset / TargetBytesPerWord) - c->arch->frameFooterSize())
         < totalFrameSize(c));

  return (offset / TargetBytesPerWord) - c->arch->frameFooterSize();
}

unsigned
frameBase(Context* c)
{
  return c->alignedFrameSize
    - c->arch->frameReturnAddressSize()
    - c->arch->frameFooterSize()
    + c->arch->frameHeaderSize();
}

class FrameIterator {
 public:
  class Element {
   public:
    Element(Value* value, unsigned localIndex):
      value(value), localIndex(localIndex)
    { }

    Value* const value;
    const unsigned localIndex;
  };

  FrameIterator(Context* c, Stack* stack, Local* locals,
                bool includeEmpty = false):
    stack(stack), locals(locals), localIndex(c->localFootprint - 1),
    includeEmpty(includeEmpty)
  { }

  bool hasMore() {
    if (not includeEmpty) {
      while (stack and stack->value == 0) stack = stack->next;

      while (localIndex >= 0 and locals[localIndex].value == 0) -- localIndex;
    }

    return stack != 0 or localIndex >= 0;
  }

  Element next(Context* c) {
    Value* v;
    unsigned li;
    if (stack) {
      Stack* s = stack;
      v = s->value;
      li = s->index + c->localFootprint;
      stack = stack->next;
    } else {
      Local* l = locals + localIndex;
      v = l->value;
      li = localIndex;
      -- localIndex;
    }
    return Element(v, li);
  }

  Stack* stack;
  Local* locals;
  int localIndex;
  bool includeEmpty;
};

int
frameIndex(Context* c, FrameIterator::Element* element)
{
  return frameIndex(c, element->localIndex);
}

class SiteIterator {
 public:
  SiteIterator(Context* c, Value* v, bool includeBuddies = true,
               bool includeNextWord = true):
    c(c),
    originalValue(v),
    currentValue(v),
    includeBuddies(includeBuddies),
    includeNextWord(includeNextWord),
    pass(0),
    next_(findNext(&(v->sites))),
    previous(0)
  { }

  Site** findNext(Site** p) {
    while (true) {
      if (*p) {
        if (pass == 0 or (*p)->registerSize(c) > TargetBytesPerWord) {
          return p;
        } else {
          p = &((*p)->next);
        }
      } else {
        if (includeBuddies) {
          Value* v = currentValue->buddy;
          if (v != originalValue) {
            currentValue = v;
            p = &(v->sites);
            continue;
          }
        }

        if (includeNextWord and pass == 0) {
          Value* v = originalValue->nextWord;
          if (v != originalValue) {
            pass = 1;
            originalValue = v;
            currentValue = v;
            p = &(v->sites);
            continue;
          }
        }

        return 0;
      }
    }
  }

  bool hasMore() {
    if (previous) {
      next_ = findNext(&((*previous)->next));
      previous = 0;
    }
    return next_ != 0;
  }

  Site* next() {
    previous = next_;
    return *previous;
  }

  void remove(Context* c) {
    (*previous)->release(c, originalValue);
    *previous = (*previous)->next;
    next_ = findNext(previous);
    previous = 0;
  }

  Context* c;
  Value* originalValue;
  Value* currentValue;
  bool includeBuddies;
  bool includeNextWord;
  uint8_t pass;
  Site** next_;
  Site** previous;
};

bool
hasSite(Context* c, Value* v)
{
  SiteIterator it(c, v);
  return it.hasMore();
}

bool
findSite(Context*, Value* v, Site* site)
{
  for (Site* s = v->sites; s; s = s->next) {
    if (s == site) return true;
  }
  return false;
}

bool
uniqueSite(Context* c, Value* v, Site* s)
{
  SiteIterator it(c, v);
  Site* p UNUSED = it.next();
  if (it.hasMore()) {
    // the site is not this word's only site, but if the site is
    // shared with the next word, it may be that word's only site
    if (v->nextWord != v and s->registerSize(c) > TargetBytesPerWord) {
      SiteIterator nit(c, v->nextWord);
      Site* p = nit.next();
      if (nit.hasMore()) {
        return false;
      } else {
        return p == s;
      }
    } else {
      return false;
    }    
  } else {
    assert(c, p == s);
    return true;
  }
}

void
addSite(Context* c, Value* v, Site* s)
{
  if (not findSite(c, v, s)) {
    if (DebugSites) {
      char buffer[256]; s->toString(c, buffer, 256);
      fprintf(stderr, "add site %s to %p\n", buffer, v);
    }
    s->acquire(c, v);
    s->next = v->sites;
    v->sites = s;
  }
}

void
removeSite(Context* c, Value* v, Site* s)
{
  for (SiteIterator it(c, v); it.hasMore();) {
    if (s == it.next()) {
      if (DebugSites) {
        char buffer[256]; s->toString(c, buffer, 256);
        fprintf(stderr, "remove site %s from %p\n", buffer, v);
      }
      it.remove(c);
      break;
    }
  }
  if (DebugSites) {
    fprintf(stderr, "%p has more: %d\n", v, hasSite(c, v));
  }
  assert(c, not findSite(c, v, s));
}

void
clearSites(Context* c, Value* v)
{
  if (DebugSites) {
    fprintf(stderr, "clear sites for %p\n", v);
  }
  for (SiteIterator it(c, v); it.hasMore();) {
    it.next();
    it.remove(c);
  }
}

bool
valid(Read* r)
{
  return r and r->valid();
}

bool
hasBuddy(Context* c, Value* a, Value* b)
{
  if (a == b) {
    return true;
  }

  int i = 0;
  for (Value* p = a->buddy; p != a; p = p->buddy) {
    if (p == b) {
      return true;
    }
    if (++i > 1000) {
      abort(c);
    }
  }
  return false;
}

Read*
live(Context* c UNUSED, Value* v)
{
  assert(c, hasBuddy(c, v->buddy, v));

  Value* p = v;
  do {
    if (valid(p->reads)) {
      return p->reads;
    }
    p = p->buddy;
  } while (p != v);

  return 0;
}

Read*
liveNext(Context* c, Value* v)
{
  assert(c, hasBuddy(c, v->buddy, v));

  Read* r = v->reads->next(c);
  if (valid(r)) return r;

  for (Value* p = v->buddy; p != v; p = p->buddy) {
    if (valid(p->reads)) return p->reads;
  }

  return 0;
}

unsigned
sitesToString(Context* c, Value* v, char* buffer, unsigned size);

void
deadWord(Context* c, Value* v)
{
  Value* nextWord = v->nextWord;
  assert(c, nextWord != v);

  for (SiteIterator it(c, v, true, false); it.hasMore();) {
    Site* s = it.next();
    
    if (s->registerSize(c) > TargetBytesPerWord) {
      it.remove(c);
      addSite(c, nextWord, s);
    }
  }
}

void
deadBuddy(Context* c, Value* v, Read* r UNUSED)
{
  assert(c, v->buddy != v);
  assert(c, r);

  if (DebugBuddies) {
    fprintf(stderr, "remove dead buddy %p from", v);
    for (Value* p = v->buddy; p != v; p = p->buddy) {
      fprintf(stderr, " %p", p);
    }
    fprintf(stderr, "\n");
  }

  assert(c, v->buddy);

  Value* next = v->buddy;
  v->buddy = v;
  Value* p = next;
  while (p->buddy != v) p = p->buddy;
  p->buddy = next;

  assert(c, p->buddy);

  for (SiteIterator it(c, v, false, false); it.hasMore();) {
    Site* s = it.next();
    it.remove(c);
    
    addSite(c, next, s);
  }
}

void
popRead(Context* c, Event* e UNUSED, Value* v)
{
  assert(c, e == v->reads->event);

  if (DebugReads) {
    fprintf(stderr, "pop read %p from %p next %p event %p (%s)\n",
            v->reads, v, v->reads->next(c), e, (e ? e->name() : 0));
  }

  v->reads = v->reads->next(c);

  if (not valid(v->reads)) {
    Value* nextWord = v->nextWord;
    if (nextWord != v) {
      if (valid(nextWord->reads)) {
        deadWord(c, v);
      } else {
        deadWord(c, nextWord);        
      }
    }

    Read* r = live(c, v);
    if (r) {
      deadBuddy(c, v, r);
    } else {
      clearSites(c, v);
    }
  }
}

bool
buddies(Value* a, Value* b)
{
  if (a == b) return true;
  for (Value* p = a->buddy; p != a; p = p->buddy) {
    if (p == b) return true;
  }
  return false;
}

void
addBuddy(Value* original, Value* buddy)
{
  buddy->buddy = original;
  Value* p = original;
  while (p->buddy != original) p = p->buddy;
  p->buddy = buddy;

  if (DebugBuddies) {
    fprintf(stderr, "add buddy %p to", buddy);
    for (Value* p = buddy->buddy; p != buddy; p = p->buddy) {
      fprintf(stderr, " %p", p);
    }
    fprintf(stderr, "\n");
  }
}

void
decrementAvailableGeneralRegisterCount(Context* c)
{
  assert(c, c->availableGeneralRegisterCount);
  -- c->availableGeneralRegisterCount;
  
  if (DebugResources) {
    fprintf(stderr, "%d registers available\n",
            c->availableGeneralRegisterCount);
  }
}

void
incrementAvailableGeneralRegisterCount(Context* c)
{
  ++ c->availableGeneralRegisterCount;

  if (DebugResources) {
    fprintf(stderr, "%d registers available\n",
            c->availableGeneralRegisterCount);
  }
}

void
increment(Context* c, RegisterResource* r)
{
  if (not r->reserved) {
    if (DebugResources) {
      char buffer[256]; r->toString(c, buffer, 256);
      fprintf(stderr, "increment %s to %d\n", buffer, r->referenceCount + 1);
    }

    ++ r->referenceCount;

    if (r->referenceCount == 1
        and ((1 << r->index(c)) & c->arch->generalRegisterMask()))
    {
      decrementAvailableGeneralRegisterCount(c);
    }
  }
}

void
decrement(Context* c, RegisterResource* r)
{
  if (not r->reserved) {
    if (DebugResources) {
      char buffer[256]; r->toString(c, buffer, 256);
      fprintf(stderr, "decrement %s to %d\n", buffer, r->referenceCount - 1);
    }

    assert(c, r->referenceCount > 0);

    -- r->referenceCount;

    if (r->referenceCount == 0
        and ((1 << r->index(c)) & c->arch->generalRegisterMask()))
    {
      incrementAvailableGeneralRegisterCount(c);
    }
  }
}

void
freezeResource(Context* c, Resource* r, Value* v)
{
  if (DebugResources) {
    char buffer[256]; r->toString(c, buffer, 256);
    fprintf(stderr, "%p freeze %s to %d\n", v, buffer, r->freezeCount + 1);
  }
    
  ++ r->freezeCount;
}

void
RegisterResource::freeze(Context* c, Value* v)
{
  if (not reserved) {
    freezeResource(c, this, v);

    if (freezeCount == 1
        and ((1 << index(c)) & c->arch->generalRegisterMask()))
    {
      decrementAvailableGeneralRegisterCount(c);
    }
  }
}

void
FrameResource::freeze(Context* c, Value* v)
{
  freezeResource(c, this, v);
}

void
thawResource(Context* c, Resource* r, Value* v)
{
  if (not r->reserved) {
    if (DebugResources) {
      char buffer[256]; r->toString(c, buffer, 256);
      fprintf(stderr, "%p thaw %s to %d\n", v, buffer, r->freezeCount - 1);
    }

    assert(c, r->freezeCount);

    -- r->freezeCount;
  }
}

void
RegisterResource::thaw(Context* c, Value* v)
{
  if (not reserved) {
    thawResource(c, this, v);

    if (freezeCount == 0
        and ((1 << index(c)) & c->arch->generalRegisterMask()))
    {
      incrementAvailableGeneralRegisterCount(c);
    }
  }
}

void
FrameResource::thaw(Context* c, Value* v)
{
  thawResource(c, this, v);
}

class Target {
 public:
  static const unsigned MinimumRegisterCost = 0;
  static const unsigned MinimumFrameCost = 1;
  static const unsigned StealPenalty = 2;
  static const unsigned StealUniquePenalty = 4;
  static const unsigned IndirectMovePenalty = 4;
  static const unsigned LowRegisterPenalty = 10;
  static const unsigned Impossible = 20;

  Target(): cost(Impossible) { }

  Target(int index, OperandType type, unsigned cost):
    index(index), type(type), cost(cost)
  { }

  int16_t index;
  OperandType type;
  uint8_t cost;
};

ValueType
valueType(Context* c, Compiler::OperandType type)
{
  switch (type) {
  case Compiler::ObjectType:
  case Compiler::AddressType:
  case Compiler::IntegerType:
  case Compiler::VoidType:
    return ValueGeneral;
  case Compiler::FloatType:
    return ValueFloat;
  default:
    abort(c);
  }
}

class CostCalculator {
 public:
  virtual unsigned cost(Context* c, uint8_t typeMask, uint32_t registerMask,
                        int frameIndex) = 0;
};

unsigned
resourceCost(Context* c, Value* v, Resource* r, uint8_t typeMask,
             uint32_t registerMask, int frameIndex,
             CostCalculator* costCalculator)
{
  if (r->reserved or r->freezeCount or r->referenceCount) {
    return Target::Impossible;
  } else {    
    unsigned baseCost = costCalculator ? costCalculator->cost
      (c, typeMask, registerMask, frameIndex) : 0;

    if (r->value) {
      assert(c, findSite(c, r->value, r->site));
      
      if (v and buddies(r->value, v)) {
        return baseCost;
      } else if (uniqueSite(c, r->value, r->site)) {
        return baseCost + Target::StealUniquePenalty;
      } else {
        return baseCost = Target::StealPenalty;
      }
    } else {
      return baseCost;
    }
  }
}

bool
pickRegisterTarget(Context* c, int i, Value* v, uint32_t mask, int* target,
                   unsigned* cost, CostCalculator* costCalculator = 0)
{
  if ((1 << i) & mask) {
    RegisterResource* r = c->registerResources + i;
    unsigned myCost = resourceCost
      (c, v, r, 1 << RegisterOperand, 1 << i, NoFrameIndex, costCalculator)
      + Target::MinimumRegisterCost;

    if ((static_cast<uint32_t>(1) << i) == mask) {
      *cost = myCost;
      return true;
    } else if (myCost < *cost) {
      *cost = myCost;
      *target = i;
    }
  }
  return false;
}

int
pickRegisterTarget(Context* c, Value* v, uint32_t mask, unsigned* cost,
                   CostCalculator* costCalculator = 0)
{
  int target = NoRegister;
  *cost = Target::Impossible;

  if (mask & c->arch->generalRegisterMask()) {
    for (int i = c->generalRegisterLimit - 1;
         i >= c->generalRegisterStart; --i)
    {
      if (pickRegisterTarget(c, i, v, mask, &target, cost, costCalculator)) {
        return i;
      }
    }
  }

  if (mask & c->arch->floatRegisterMask()) {
    for (int i = c->floatRegisterStart;
         i < static_cast<int>(c->floatRegisterLimit); ++i)
    {
      if (pickRegisterTarget(c, i, v, mask, &target, cost, costCalculator)) {
        return i;
      }
    }
  }

  return target;
}

Target
pickRegisterTarget(Context* c, Value* v, uint32_t mask,
                   CostCalculator* costCalculator = 0)
{
  unsigned cost;
  int number = pickRegisterTarget(c, v, mask, &cost, costCalculator);
  return Target(number, RegisterOperand, cost);
}

unsigned
frameCost(Context* c, Value* v, int frameIndex, CostCalculator* costCalculator)
{
  return resourceCost
    (c, v, c->frameResources + frameIndex, 1 << MemoryOperand, 0, frameIndex,
     costCalculator)
    + Target::MinimumFrameCost;
}

Target
pickFrameTarget(Context* c, Value* v, CostCalculator* costCalculator)
{
  Target best;

  Value* p = v;
  do {
    if (p->home >= 0) {
      Target mine
        (p->home, MemoryOperand, frameCost(c, v, p->home, costCalculator));

      if (mine.cost == Target::MinimumFrameCost) {
        return mine;
      } else if (mine.cost < best.cost) {
        best = mine;
      }
    }
    p = p->buddy;
  } while (p != v);

  return best;
}

Target
pickAnyFrameTarget(Context* c, Value* v, CostCalculator* costCalculator)
{
  Target best;

  unsigned count = totalFrameSize(c);
  for (unsigned i = 0; i < count; ++i) {
    Target mine(i, MemoryOperand, frameCost(c, v, i, costCalculator));
    if (mine.cost == Target::MinimumFrameCost) {
      return mine;
    } else if (mine.cost < best.cost) {
      best = mine;
    }    
  }

  return best;
}

Target
pickTarget(Context* c, Value* value, const SiteMask& mask,
           unsigned registerPenalty, Target best,
           CostCalculator* costCalculator)
{
  if (mask.typeMask & (1 << RegisterOperand)) {
    Target mine = pickRegisterTarget
      (c, value, mask.registerMask, costCalculator);

    mine.cost += registerPenalty;
    if (mine.cost == Target::MinimumRegisterCost) {
      return mine;
    } else if (mine.cost < best.cost) {
      best = mine;
    }
  }

  if (mask.typeMask & (1 << MemoryOperand)) {
    if (mask.frameIndex >= 0) {
      Target mine(mask.frameIndex, MemoryOperand,
                  frameCost(c, value, mask.frameIndex, costCalculator));
      if (mine.cost == Target::MinimumFrameCost) {
        return mine;
      } else if (mine.cost < best.cost) {
        best = mine;
      }
    } else if (mask.frameIndex == AnyFrameIndex) {
      Target mine = pickFrameTarget(c, value, costCalculator);
      if (mine.cost == Target::MinimumFrameCost) {
        return mine;
      } else if (mine.cost < best.cost) {
        best = mine;
      }
    }
  }

  return best;
}

Target
pickTarget(Context* c, Read* read, bool intersectRead,
           unsigned registerReserveCount, CostCalculator* costCalculator)
{
  unsigned registerPenalty
    = (c->availableGeneralRegisterCount > registerReserveCount
       ? 0 : Target::LowRegisterPenalty);

  Value* value = read->value;

  uint32_t registerMask
    = (value->type == ValueFloat ? ~0 : c->arch->generalRegisterMask());

  SiteMask mask(~0, registerMask, AnyFrameIndex);
  read->intersect(&mask);

  if (value->type == ValueFloat) {
    uint32_t floatMask = mask.registerMask & c->arch->floatRegisterMask();
    if (floatMask) {
      mask.registerMask = floatMask;
    }
  }

  Target best;

  Value* successor = read->successor();
  if (successor) {
    Read* r = live(c, successor);
    if (r) {
      SiteMask intersection = mask;
      if (r->intersect(&intersection)) {
        best = pickTarget
          (c, value, intersection, registerPenalty, best, costCalculator);

        if (best.cost <= Target::MinimumFrameCost) {
          return best;
        }
      }
    }
  }

  best = pickTarget(c, value, mask, registerPenalty, best, costCalculator);
  if (best.cost <= Target::MinimumFrameCost) {
    return best;
  }

  if (intersectRead) {
    if (best.cost == Target::Impossible) {
      fprintf(stderr, "mask type %d reg %d frame %d\n",
              mask.typeMask, mask.registerMask, mask.frameIndex);
      abort(c);
    }
    return best;
  }

  { Target mine = pickRegisterTarget(c, value, registerMask, costCalculator);

    mine.cost += registerPenalty;

    if (mine.cost == Target::MinimumRegisterCost) {
      return mine;
    } else if (mine.cost < best.cost) {
      best = mine;
    }
  }

  { Target mine = pickFrameTarget(c, value, costCalculator);
    if (mine.cost == Target::MinimumFrameCost) {
      return mine;
    } else if (mine.cost < best.cost) {
      best = mine;
    }
  }

  if (best.cost >= Target::StealUniquePenalty
      and c->availableGeneralRegisterCount == 0)
  {
    // there are no free registers left, so moving from memory to
    // memory isn't an option - try harder to find an available frame
    // site:
    best = pickAnyFrameTarget(c, value, costCalculator);
    assert(c, best.cost <= 3);
  }

  if (best.cost == Target::Impossible) {
    abort(c);
  }

  return best;
}

void
acquire(Context* c, Resource* resource, Value* value, Site* site);

void
release(Context* c, Resource* resource, Value* value, Site* site);

ConstantSite*
constantSite(Context* c, Promise* value);

ShiftMaskPromise*
shiftMaskPromise(Context* c, Promise* base, unsigned shift, int64_t mask)
{
  return new (c->zone->allocate(sizeof(ShiftMaskPromise)))
    ShiftMaskPromise(base, shift, mask);
}

CombinedPromise*
combinedPromise(Context* c, Promise* low, Promise* high)
{
  return new (c->zone->allocate(sizeof(CombinedPromise)))
    CombinedPromise(low, high);
}

class ConstantSite: public Site {
 public:
  ConstantSite(Promise* value): value(value) { }

  virtual unsigned toString(Context*, char* buffer, unsigned bufferSize) {
    if (value->resolved()) {
      return vm::snprintf
        (buffer, bufferSize, "constant %"LLD, value->value());
    } else {
      return vm::snprintf(buffer, bufferSize, "constant unresolved");
    }
  }

  virtual unsigned copyCost(Context*, Site* s) {
    return (s == this ? 0 : ConstantCopyCost);
  }

  virtual bool match(Context*, const SiteMask& mask) {
    return mask.typeMask & (1 << ConstantOperand);
  }

  virtual bool loneMatch(Context*, const SiteMask&) {
    return true;
  }

  virtual bool matchNextWord(Context* c, Site* s, unsigned) {
    return s->type(c) == ConstantOperand;
  }

  virtual OperandType type(Context*) {
    return ConstantOperand;
  }

  virtual void asAssemblerOperand(Context* c, Site* high,
                                  Assembler::Operand* result)
  {
    Promise* v = value;
    if (high != this) {
      v = combinedPromise(c, value, static_cast<ConstantSite*>(high)->value);
    }
    new (result) Assembler::Constant(v);
  }

  virtual Site* copy(Context* c) {
    return constantSite(c, value);
  }

  virtual Site* copyLow(Context* c) {
    return constantSite(c, shiftMaskPromise(c, value, 0, 0xFFFFFFFF));
  }

  virtual Site* copyHigh(Context* c) {
    return constantSite(c, shiftMaskPromise(c, value, 32, 0xFFFFFFFF));
  }

  virtual Site* makeNextWord(Context* c, unsigned) {
    abort(c);
  }

  virtual SiteMask mask(Context*) {
    return SiteMask(1 << ConstantOperand, 0, NoFrameIndex);
  }

  virtual SiteMask nextWordMask(Context*, unsigned) {
    return SiteMask(1 << ConstantOperand, 0, NoFrameIndex);
  }

  Promise* value;
};

ConstantSite*
constantSite(Context* c, Promise* value)
{
  return new (c->zone->allocate(sizeof(ConstantSite))) ConstantSite(value);
}

ResolvedPromise*
resolved(Context* c, int64_t value)
{
  return new (c->zone->allocate(sizeof(ResolvedPromise)))
    ResolvedPromise(value);
}

ConstantSite*
constantSite(Context* c, int64_t value)
{
  return constantSite(c, resolved(c, value));
}

AddressSite*
addressSite(Context* c, Promise* address);

class AddressSite: public Site {
 public:
  AddressSite(Promise* address): address(address) { }

  virtual unsigned toString(Context*, char* buffer, unsigned bufferSize) {
    if (address->resolved()) {
      return vm::snprintf
        (buffer, bufferSize, "address %"LLD, address->value());
    } else {
      return vm::snprintf(buffer, bufferSize, "address unresolved");
    }
  }

  virtual unsigned copyCost(Context*, Site* s) {
    return (s == this ? 0 : AddressCopyCost);
  }

  virtual bool match(Context*, const SiteMask& mask) {
    return mask.typeMask & (1 << AddressOperand);
  }

  virtual bool loneMatch(Context*, const SiteMask&) {
    return false;
  }

  virtual bool matchNextWord(Context* c, Site*, unsigned) {
    abort(c);
  }

  virtual OperandType type(Context*) {
    return AddressOperand;
  }

  virtual void asAssemblerOperand(Context* c UNUSED, Site* high UNUSED,
                                  Assembler::Operand* result)
  {
    assert(c, high == this);

    new (result) Assembler::Address(address);
  }

  virtual Site* copy(Context* c) {
    return addressSite(c, address);
  }

  virtual Site* copyLow(Context* c) {
    abort(c);
  }

  virtual Site* copyHigh(Context* c) {
    abort(c);
  }

  virtual Site* makeNextWord(Context* c, unsigned) {
    abort(c);
  }

  virtual SiteMask mask(Context*) {
    return SiteMask(1 << AddressOperand, 0, NoFrameIndex);
  }

  virtual SiteMask nextWordMask(Context* c, unsigned) {
    abort(c);
  }

  Promise* address;
};

AddressSite*
addressSite(Context* c, Promise* address)
{
  return new (c->zone->allocate(sizeof(AddressSite))) AddressSite(address);
}

RegisterSite*
freeRegisterSite(Context* c, uint32_t mask);

class RegisterSite: public Site {
 public:
  RegisterSite(uint32_t mask, int number):
    mask_(mask), number(number)
  { }

  virtual unsigned toString(Context*, char* buffer, unsigned bufferSize) {
    if (number != NoRegister) {
      return vm::snprintf(buffer, bufferSize, "%p register %d", this, number);
    } else {
      return vm::snprintf(buffer, bufferSize,
                          "%p register unacquired (mask %d)", this, mask_);
    }
  }

  virtual unsigned copyCost(Context* c, Site* s) {
    assert(c, number != NoRegister);

    if (s and
        (this == s or
         (s->type(c) == RegisterOperand
          and (static_cast<RegisterSite*>(s)->mask_ & (1 << number)))))
    {
      return 0;
    } else {
      return RegisterCopyCost;
    }
  }

  virtual bool match(Context* c UNUSED, const SiteMask& mask) {
    assert(c, number != NoRegister);

    if ((mask.typeMask & (1 << RegisterOperand))) {
      return ((static_cast<uint64_t>(1) << number) & mask.registerMask);
    } else {
      return false;
    }
  }

  virtual bool loneMatch(Context* c UNUSED, const SiteMask& mask) {
    assert(c, number != NoRegister);

    if ((mask.typeMask & (1 << RegisterOperand))) {
      return ((static_cast<uint64_t>(1) << number) == mask.registerMask);
    } else {
      return false;
    }
  }

  virtual bool matchNextWord(Context* c, Site* s, unsigned) {
    assert(c, number != NoRegister);

    if (s->type(c) != RegisterOperand) {
      return false;
    }

    RegisterSite* rs = static_cast<RegisterSite*>(s);
    unsigned size = rs->registerSize(c);
    if (size > TargetBytesPerWord) {
      assert(c, number != NoRegister);
      return number == rs->number;
    } else {
      uint32_t mask = c->arch->generalRegisterMask();
      return ((1 << number) & mask) and ((1 << rs->number) & mask);
    }
  }

  virtual void acquire(Context* c, Value* v) {
    Target target;
    if (number != NoRegister) {
      target = Target(number, RegisterOperand, 0);
    } else {
      target = pickRegisterTarget(c, v, mask_);
      expect(c, target.cost < Target::Impossible);
    }

    RegisterResource* resource = c->registerResources + target.index;
    local::acquire(c, resource, v, this);

    number = target.index;
  }

  virtual void release(Context* c, Value* v) {
    assert(c, number != NoRegister);

    local::release(c, c->registerResources + number, v, this);
  }

  virtual void freeze(Context* c, Value* v) {
    assert(c, number != NoRegister);

    c->registerResources[number].freeze(c, v);
  }

  virtual void thaw(Context* c, Value* v) {
    assert(c, number != NoRegister);

    c->registerResources[number].thaw(c, v);
  }

  virtual bool frozen(Context* c UNUSED) {
    assert(c, number != NoRegister);

    return c->registerResources[number].freezeCount != 0;
  }

  virtual OperandType type(Context*) {
    return RegisterOperand;
  }

  virtual void asAssemblerOperand(Context* c UNUSED, Site* high,
                                  Assembler::Operand* result)
  {
    assert(c, number != NoRegister);

    int highNumber;
    if (high != this) {
      highNumber = static_cast<RegisterSite*>(high)->number;
      assert(c, highNumber != NoRegister);
    } else {
      highNumber = NoRegister;
    }

    new (result) Assembler::Register(number, highNumber);
  }

  virtual Site* copy(Context* c) {
    uint32_t mask;
    
    if (number != NoRegister) {
      mask = 1 << number;
    } else {
      mask = mask_;
    }

    return freeRegisterSite(c, mask);
  }

  virtual Site* copyLow(Context* c) {
    abort(c);
  }

  virtual Site* copyHigh(Context* c) {
    abort(c);
  }

  virtual Site* makeNextWord(Context* c, unsigned) {
    assert(c, number != NoRegister);
    assert(c, ((1 << number) & c->arch->generalRegisterMask()));

    return freeRegisterSite(c, c->arch->generalRegisterMask());    
  }

  virtual SiteMask mask(Context* c UNUSED) {
    return SiteMask(1 << RegisterOperand, mask_, NoFrameIndex);
  }

  virtual SiteMask nextWordMask(Context* c, unsigned) {
    assert(c, number != NoRegister);

    if (registerSize(c) > TargetBytesPerWord) {
      return SiteMask
        (1 << RegisterOperand, number, NoFrameIndex);
    } else {
      return SiteMask
        (1 << RegisterOperand, c->arch->generalRegisterMask(), NoFrameIndex);
    }
  }

  virtual unsigned registerSize(Context* c) {
    assert(c, number != NoRegister);

    if ((1 << number) & c->arch->floatRegisterMask()) {
      return c->arch->floatRegisterSize();
    } else {
      return TargetBytesPerWord;
    }
  }

  virtual unsigned registerMask(Context* c UNUSED) {
    assert(c, number != NoRegister);

    return 1 << number;
  }

  uint32_t mask_;
  int number;
};

RegisterSite*
registerSite(Context* c, int number)
{
  assert(c, number >= 0);
  assert(c, (1 << number) & (c->arch->generalRegisterMask()
                             | c->arch->floatRegisterMask()));

  return new (c->zone->allocate(sizeof(RegisterSite)))
    RegisterSite(1 << number, number);
}

RegisterSite*
freeRegisterSite(Context* c, uint32_t mask)
{
  return new (c->zone->allocate(sizeof(RegisterSite)))
    RegisterSite(mask, NoRegister);
}

MemorySite*
memorySite(Context* c, int base, int offset = 0, int index = NoRegister,
           unsigned scale = 1);

class MemorySite: public Site {
 public:
  MemorySite(int base, int offset, int index, unsigned scale):
    acquired(false), base(base), offset(offset), index(index), scale(scale)
  { }

  virtual unsigned toString(Context*, char* buffer, unsigned bufferSize) {
    if (acquired) {
      return vm::snprintf(buffer, bufferSize, "memory %d 0x%x %d %d",
                      base, offset, index, scale);
    } else {
      return vm::snprintf(buffer, bufferSize, "memory unacquired");
    }
  }

  virtual unsigned copyCost(Context* c, Site* s) {
    assert(c, acquired);    

    if (s and
        (this == s or
         (s->type(c) == MemoryOperand
          and static_cast<MemorySite*>(s)->base == base
          and static_cast<MemorySite*>(s)->offset == offset
          and static_cast<MemorySite*>(s)->index == index
          and static_cast<MemorySite*>(s)->scale == scale)))
    {
      return 0;
    } else {
      return MemoryCopyCost;
    }
  }

  virtual bool match(Context* c, const SiteMask& mask) {
    assert(c, acquired);

    if (mask.typeMask & (1 << MemoryOperand)) {
      if (mask.frameIndex >= 0) {
        if (base == c->arch->stack()) {
          assert(c, index == NoRegister);
          return static_cast<int>(frameIndexToOffset(c, mask.frameIndex))
            == offset;
        } else {
          return false;
        }
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  virtual bool loneMatch(Context* c, const SiteMask& mask) {
    assert(c, acquired);

    if (mask.typeMask & (1 << MemoryOperand)) {
      if (base == c->arch->stack()) {
        assert(c, index == NoRegister);

        if (mask.frameIndex == AnyFrameIndex) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  virtual bool matchNextWord(Context* c, Site* s, unsigned index) {
    if (s->type(c) == MemoryOperand) {
      MemorySite* ms = static_cast<MemorySite*>(s);
      return ms->base == this->base
        and ((index == 1 and ms->offset == static_cast<int>
              (this->offset + TargetBytesPerWord))
             or (index == 0 and this->offset == static_cast<int>
                 (ms->offset + TargetBytesPerWord)))
        and ms->index == this->index
        and ms->scale == this->scale;
    } else {
      return false;
    }
  }

  virtual void acquire(Context* c, Value* v) {
    increment(c, c->registerResources + base);
    if (index != NoRegister) {
      increment(c, c->registerResources + index);
    }

    if (base == c->arch->stack()) {
      assert(c, index == NoRegister);
      assert
        (c, not c->frameResources[offsetToFrameIndex(c, offset)].reserved);

      local::acquire
        (c, c->frameResources + offsetToFrameIndex(c, offset), v, this);
    }

    acquired = true;
  }

  virtual void release(Context* c, Value* v) {
    if (base == c->arch->stack()) {
      assert(c, index == NoRegister);
      assert
        (c, not c->frameResources[offsetToFrameIndex(c, offset)].reserved);

      local::release
        (c, c->frameResources + offsetToFrameIndex(c, offset), v, this);
    }

    decrement(c, c->registerResources + base);
    if (index != NoRegister) {
      decrement(c, c->registerResources + index);
    }

    acquired = false;
  }

  virtual void freeze(Context* c, Value* v) {
    if (base == c->arch->stack()) {
      c->frameResources[offsetToFrameIndex(c, offset)].freeze(c, v);
    } else {
      increment(c, c->registerResources + base);
      if (index != NoRegister) {
        increment(c, c->registerResources + index);
      }
    }
  }

  virtual void thaw(Context* c, Value* v) {
    if (base == c->arch->stack()) {
      c->frameResources[offsetToFrameIndex(c, offset)].thaw(c, v);
    } else {
      decrement(c, c->registerResources + base);
      if (index != NoRegister) {
        decrement(c, c->registerResources + index);
      }
    }
  }

  virtual bool frozen(Context* c) {
    return base == c->arch->stack()
      and c->frameResources[offsetToFrameIndex(c, offset)].freezeCount != 0;
  }

  virtual OperandType type(Context*) {
    return MemoryOperand;
  }

  virtual void asAssemblerOperand(Context* c UNUSED, Site* high UNUSED,
                                  Assembler::Operand* result)
  {
    // todo: endianness?
    assert(c, high == this
           or (static_cast<MemorySite*>(high)->base == base
               and static_cast<MemorySite*>(high)->offset
               == static_cast<int>(offset + TargetBytesPerWord)
               and static_cast<MemorySite*>(high)->index == index
               and static_cast<MemorySite*>(high)->scale == scale));

    assert(c, acquired);

    new (result) Assembler::Memory(base, offset, index, scale);
  }

  virtual Site* copy(Context* c) {
    return memorySite(c, base, offset, index, scale);
  }

  Site* copyHalf(Context* c, bool add) {
    if (add) {
      return memorySite(c, base, offset + TargetBytesPerWord, index, scale);
    } else {
      return copy(c);
    }
  }

  virtual Site* copyLow(Context* c) {
    return copyHalf(c, c->arch->bigEndian());
  }

  virtual Site* copyHigh(Context* c) {
    return copyHalf(c, not c->arch->bigEndian());
  }

  virtual Site* makeNextWord(Context* c, unsigned index) {
    return memorySite
      (c, base, offset + ((index == 1) xor c->arch->bigEndian()
                          ? TargetBytesPerWord : -TargetBytesPerWord),
       this->index, scale);
  }

  virtual SiteMask mask(Context* c) {
    return SiteMask(1 << MemoryOperand, 0, (base == c->arch->stack())
                    ? static_cast<int>(offsetToFrameIndex(c, offset))
                    : NoFrameIndex);
  }

  virtual SiteMask nextWordMask(Context* c, unsigned index) {
    int frameIndex;
    if (base == c->arch->stack()) {
      assert(c, this->index == NoRegister);
      frameIndex = static_cast<int>(offsetToFrameIndex(c, offset))
        + ((index == 1) xor c->arch->bigEndian() ? 1 : -1);
    } else {
      frameIndex = NoFrameIndex;
    }
    return SiteMask(1 << MemoryOperand, 0, frameIndex);
  }

  virtual bool isVolatile(Context* c) {
    return base != c->arch->stack();
  }

  bool acquired;
  int base;
  int offset;
  int index;
  unsigned scale;
};

MemorySite*
memorySite(Context* c, int base, int offset, int index, unsigned scale)
{
  return new (c->zone->allocate(sizeof(MemorySite)))
    MemorySite(base, offset, index, scale);
}

MemorySite*
frameSite(Context* c, int frameIndex)
{
  assert(c, frameIndex >= 0);
  return memorySite
    (c, c->arch->stack(), frameIndexToOffset(c, frameIndex), NoRegister, 0);
}

void
move(Context* c, Value* value, Site* src, Site* dst);

unsigned
sitesToString(Context* c, Site* sites, char* buffer, unsigned size)
{
  unsigned total = 0;
  for (Site* s = sites; s; s = s->next) {
    total += s->toString(c, buffer + total, size - total);

    if (s->next) {
      assert(c, size > total + 2);
      memcpy(buffer + total, ", ", 2);
      total += 2;
    }
  }

  assert(c, size > total);
  buffer[total] = 0;

  return total;
}

unsigned
sitesToString(Context* c, Value* v, char* buffer, unsigned size)
{
  unsigned total = 0;
  Value* p = v;
  do {
    if (total) {
      assert(c, size > total + 2);
      memcpy(buffer + total, "; ", 2);
      total += 2;
    }

    if (p->sites) {
      total += vm::snprintf(buffer + total, size - total, "%p has ", p);
      total += sitesToString(c, p->sites, buffer + total, size - total);
    } else {
      total += vm::snprintf(buffer + total, size - total, "%p has nothing", p);
    }

    p = p->buddy;
  } while (p != v);

  return total;
}

Site*
pickTargetSite(Context* c, Read* read, bool intersectRead = false,
               unsigned registerReserveCount = 0,
               CostCalculator* costCalculator = 0)
{
  Target target
    (pickTarget
     (c, read, intersectRead, registerReserveCount, costCalculator));

  expect(c, target.cost < Target::Impossible);

  if (target.type == MemoryOperand) {
    return frameSite(c, target.index);
  } else {
    return registerSite(c, target.index);
  }
}

class SingleRead: public Read {
 public:
  SingleRead(const SiteMask& mask, Value* successor):
    next_(0), mask(mask), high_(0), successor_(successor)
  { }

  virtual bool intersect(SiteMask* mask, unsigned) {
    *mask = local::intersect(*mask, this->mask);

    return true;
  }

  virtual Value* high(Context*) {
    return high_;
  }

  virtual Value* successor() {
    return successor_;
  }
  
  virtual bool valid() {
    return true;
  }

  virtual void append(Context* c UNUSED, Read* r) {
    assert(c, next_ == 0);
    next_ = r;
  }

  virtual Read* next(Context*) {
    return next_;
  }

  Read* next_;
  SiteMask mask;
  Value* high_;
  Value* successor_;
};

SingleRead*
read(Context* c, const SiteMask& mask, Value* successor = 0)
{
  assert(c, (mask.typeMask != 1 << MemoryOperand) or mask.frameIndex >= 0);

  return new (c->zone->allocate(sizeof(SingleRead)))
    SingleRead(mask, successor);
}

bool
acceptMatch(Context* c, Site* s, Read*, const SiteMask& mask)
{
  return s->match(c, mask);
}

Site*
pickSourceSite(Context* c, Read* read, Site* target = 0,
               unsigned* cost = 0, SiteMask* extraMask = 0,
               bool intersectRead = true, bool includeBuddies = true,
               bool includeNextWord = true,
               bool (*accept)(Context*, Site*, Read*, const SiteMask&)
               = acceptMatch)
{
  SiteMask mask;

  if (extraMask) {
    mask = intersect(mask, *extraMask);
  }

  if (intersectRead) {
    read->intersect(&mask);
  }

  Site* site = 0;
  unsigned copyCost = 0xFFFFFFFF;
  for (SiteIterator it(c, read->value, includeBuddies, includeNextWord);
       it.hasMore();)
  {
    Site* s = it.next();
    if (accept(c, s, read, mask)) {
      unsigned v = s->copyCost(c, target);
      if (v < copyCost) {
        site = s;
        copyCost = v;
      }
    }
  }

  if (DebugMoves and site and target) {
    char srcb[256]; site->toString(c, srcb, 256);
    char dstb[256]; target->toString(c, dstb, 256);
    fprintf(stderr, "pick source %s to %s for %p cost %d\n",
            srcb, dstb, read->value, copyCost);
  }

  if (cost) *cost = copyCost;
  return site;
}

Site*
maybeMove(Context* c, Read* read, bool intersectRead, bool includeNextWord,
          unsigned registerReserveCount = 0)
{
  Value* value = read->value;
  unsigned size = value == value->nextWord ? TargetBytesPerWord : 8;

  class MyCostCalculator: public CostCalculator {
   public:
    MyCostCalculator(Value* value, unsigned size, bool includeNextWord):
      value(value),
      size(size),
      includeNextWord(includeNextWord)
    { }

    virtual unsigned cost(Context* c, uint8_t typeMask, uint32_t registerMask,
                          int frameIndex)
    {
      uint8_t srcTypeMask;
      uint64_t srcRegisterMask;
      uint8_t tmpTypeMask;
      uint64_t tmpRegisterMask;
      c->arch->planMove
        (size, &srcTypeMask, &srcRegisterMask,
         &tmpTypeMask, &tmpRegisterMask,
         typeMask, registerMask);

      SiteMask srcMask(srcTypeMask, srcRegisterMask, AnyFrameIndex);
      SiteMask dstMask(typeMask, registerMask, frameIndex);
      for (SiteIterator it(c, value, true, includeNextWord); it.hasMore();) {
        Site* s = it.next();
        if (s->match(c, srcMask) or s->match(c, dstMask)) {
          return 0;
        }
      }

      return Target::IndirectMovePenalty;
    }

    Value* value;
    unsigned size;
    bool includeNextWord;
  } costCalculator(value, size, includeNextWord);

  Site* dst = pickTargetSite
    (c, read, intersectRead, registerReserveCount, &costCalculator);

  uint8_t srcTypeMask;
  uint64_t srcRegisterMask;
  uint8_t tmpTypeMask;
  uint64_t tmpRegisterMask;
  c->arch->planMove
    (size, &srcTypeMask, &srcRegisterMask,
     &tmpTypeMask, &tmpRegisterMask,
     1 << dst->type(c), dst->registerMask(c));

  SiteMask srcMask(srcTypeMask, srcRegisterMask, AnyFrameIndex);
  unsigned cost = 0xFFFFFFFF;
  Site* src = 0;
  for (SiteIterator it(c, value, true, includeNextWord); it.hasMore();) {
    Site* s = it.next();
    unsigned v = s->copyCost(c, dst);
    if (v == 0) {
      src = s;
      cost = 0;
      break;
    }
    if (not s->match(c, srcMask)) {
      v += CopyPenalty;
    }
    if (v < cost) {
      src = s;
      cost = v;
    }
  }
 
  if (cost) {
    if (DebugMoves) {
      char srcb[256]; src->toString(c, srcb, 256);
      char dstb[256]; dst->toString(c, dstb, 256);
      fprintf(stderr, "maybe move %s to %s for %p to %p\n",
              srcb, dstb, value, value);
    }

    src->freeze(c, value);

    addSite(c, value, dst);
    
    src->thaw(c, value);    

    if (not src->match(c, srcMask)) {
      src->freeze(c, value);
      dst->freeze(c, value);

      SiteMask tmpMask(tmpTypeMask, tmpRegisterMask, AnyFrameIndex);
      SingleRead tmpRead(tmpMask, 0);
      tmpRead.value = value;
      tmpRead.successor_ = value;

      Site* tmp = pickTargetSite(c, &tmpRead, true);

      addSite(c, value, tmp);

      move(c, value, src, tmp);
      
      dst->thaw(c, value);
      src->thaw(c, value);

      src = tmp;
    }

    move(c, value, src, dst);
  }

  return dst;
}

Site*
maybeMove(Context* c, Value* v, const SiteMask& mask, bool intersectMask,
          bool includeNextWord, unsigned registerReserveCount = 0)
{
  SingleRead read(mask, 0);
  read.value = v;
  read.successor_ = v;

  return maybeMove
    (c, &read, intersectMask, includeNextWord, registerReserveCount);
}

Site*
pickSiteOrMove(Context* c, Read* read, bool intersectRead,
               bool includeNextWord, unsigned registerReserveCount = 0)
{
  Site* s = pickSourceSite
    (c, read, 0, 0, 0, intersectRead, true, includeNextWord);
  
  if (s) {
    return s;
  } else {
    return maybeMove
      (c, read, intersectRead, includeNextWord, registerReserveCount);
  }
}

Site*
pickSiteOrMove(Context* c, Value* v, const SiteMask& mask, bool intersectMask,
               bool includeNextWord, unsigned registerReserveCount = 0)
{
  SingleRead read(mask, 0);
  read.value = v;
  read.successor_ = v;

  return pickSiteOrMove
    (c, &read, intersectMask, includeNextWord, registerReserveCount);
}

void
steal(Context* c, Resource* r, Value* thief)
{
  if (DebugResources) {
    char resourceBuffer[256]; r->toString(c, resourceBuffer, 256);
    char siteBuffer[1024]; sitesToString(c, r->value, siteBuffer, 1024);
    fprintf(stderr, "%p steal %s from %p (%s)\n",
            thief, resourceBuffer, r->value, siteBuffer);
  }

  if ((not (thief and buddies(thief, r->value))
       and uniqueSite(c, r->value, r->site)))
  {
    r->site->freeze(c, r->value);

    maybeMove(c, live(c, r->value), false, true, StealRegisterReserveCount);

    r->site->thaw(c, r->value);
  }

  removeSite(c, r->value, r->site);
}

void
acquire(Context* c, Resource* resource, Value* value, Site* site)
{
  assert(c, value);
  assert(c, site);

  if (not resource->reserved) {
    if (DebugResources) {
      char buffer[256]; resource->toString(c, buffer, 256);
      fprintf(stderr, "%p acquire %s\n", value, buffer);
    }

    if (resource->value) {
      assert(c, findSite(c, resource->value, resource->site));
      assert(c, not findSite(c, value, resource->site));

      steal(c, resource, value);
    }

    if (c->acquiredResources) {
      c->acquiredResources->previousAcquired = resource;
      resource->nextAcquired = c->acquiredResources;        
    }
    c->acquiredResources = resource;

    resource->value = value;
    resource->site = site;
  }
}

void
release(Context* c, Resource* resource, Value* value UNUSED, Site* site UNUSED)
{
  if (not resource->reserved) {
    if (DebugResources) {
      char buffer[256]; resource->toString(c, buffer, 256);
      fprintf(stderr, "%p release %s\n", resource->value, buffer);
    }

    assert(c, resource->value);
    assert(c, resource->site);

    assert(c, buddies(resource->value, value));
    assert(c, site == resource->site);

    Resource* next = resource->nextAcquired;
    if (next) {
      next->previousAcquired = resource->previousAcquired;
      resource->nextAcquired = 0;
    }

    Resource* previous = resource->previousAcquired;
    if (previous) {
      previous->nextAcquired = next;
      resource->previousAcquired = 0;
    } else {
      assert(c, c->acquiredResources == resource);
      c->acquiredResources = next;
    }
    
    resource->value = 0;
    resource->site = 0;
  }
}

SiteMask
generalRegisterMask(Context* c)
{
  return SiteMask
    (1 << RegisterOperand, c->arch->generalRegisterMask(), NoFrameIndex);
}

SiteMask
generalRegisterOrConstantMask(Context* c)
{
  return SiteMask
    ((1 << RegisterOperand) | (1 << ConstantOperand),
     c->arch->generalRegisterMask(), NoFrameIndex);
}

SiteMask
fixedRegisterMask(int number)
{
  return SiteMask(1 << RegisterOperand, 1 << number, NoFrameIndex);
}

class MultiRead: public Read {
 public:
  MultiRead():
    reads(0), lastRead(0), firstTarget(0), lastTarget(0), visited(false)
  { }

  virtual bool intersect(SiteMask* mask, unsigned depth) {
    if (depth > 0) {
      // short-circuit recursion to avoid poor performance in
      // deeply-nested branches
      return reads != 0;
    }

    bool result = false;
    if (not visited) {
      visited = true;
      for (Cell** cell = &reads; *cell;) {
        Read* r = static_cast<Read*>((*cell)->value);
        bool valid = r->intersect(mask, depth + 1);
        if (valid) {
          result = true;
          cell = &((*cell)->next);
        } else {
          *cell = (*cell)->next;
        }
      }
      visited = false;
    }
    return result;
  }

  virtual Value* successor() {
    return 0;
  }

  virtual bool valid() {
    bool result = false;
    if (not visited) {
      visited = true;
      for (Cell** cell = &reads; *cell;) {
        Read* r = static_cast<Read*>((*cell)->value);
        if (r->valid()) {
          result = true;
          cell = &((*cell)->next);
        } else {
          *cell = (*cell)->next;
        }
      }
      visited = false;
    }
    return result;
  }

  virtual void append(Context* c, Read* r) {
    Cell* cell = cons(c, r, 0);
    if (lastRead == 0) {
      reads = cell;
    } else {
      lastRead->next = cell;
    }
    lastRead = cell;

//     fprintf(stderr, "append %p to %p for %p\n", r, lastTarget, this);

    lastTarget->value = r;
  }

  virtual Read* next(Context* c) {
    abort(c);
  }

  void allocateTarget(Context* c) {
    Cell* cell = cons(c, 0, 0);

//     fprintf(stderr, "allocate target for %p: %p\n", this, cell);

    if (lastTarget) {
      lastTarget->next = cell;
    } else {
      firstTarget = cell;
    }
    lastTarget = cell;
  }

  Read* nextTarget() {
    //     fprintf(stderr, "next target for %p: %p\n", this, firstTarget);

    Read* r = static_cast<Read*>(firstTarget->value);
    firstTarget = firstTarget->next;
    return r;
  }

  Cell* reads;
  Cell* lastRead;
  Cell* firstTarget;
  Cell* lastTarget;
  bool visited;
};

MultiRead*
multiRead(Context* c)
{
  return new (c->zone->allocate(sizeof(MultiRead))) MultiRead;
}

class StubRead: public Read {
 public:
  StubRead():
    next_(0), read(0), visited(false), valid_(true)
  { }

  virtual bool intersect(SiteMask* mask, unsigned depth) {
    if (not visited) {
      visited = true;
      if (read) {
        bool valid = read->intersect(mask, depth);
        if (not valid) {
          read = 0;
        }
      }
      visited = false;
    }
    return valid_;
  }

  virtual Value* successor() {
    return 0;
  }

  virtual bool valid() {
    return valid_;
  }

  virtual void append(Context* c UNUSED, Read* r) {
    assert(c, next_ == 0);
    next_ = r;
  }

  virtual Read* next(Context*) {
    return next_;
  }

  Read* next_;
  Read* read;
  bool visited;
  bool valid_;
};

StubRead*
stubRead(Context* c)
{
  return new (c->zone->allocate(sizeof(StubRead))) StubRead;
}

Site*
pickSite(Context* c, Value* v, Site* s, unsigned index, bool includeNextWord)
{
  for (SiteIterator it(c, v, true, includeNextWord); it.hasMore();) {
    Site* candidate = it.next();
    if (s->matchNextWord(c, candidate, index)) {
      return candidate;
    }
  }

  return 0;
}

Site*
pickSiteOrMove(Context* c, Value* v, Site* s, unsigned index)
{
  Site* n = pickSite(c, v, s, index, false);
  if (n) {
    return n;
  }

  return maybeMove(c, v, s->nextWordMask(c, index), true, false);
}

Site*
pickSiteOrMove(Context* c, Value* v, Site* s, Site** low, Site** high)
{
  if (v->wordIndex == 0) {
    *low = s;
    *high = pickSiteOrMove(c, v->nextWord, s, 1);
    return *high;
  } else {
    *low = pickSiteOrMove(c, v->nextWord, s, 0);
    *high = s;
    return *low;
  }
}

Site*
pickSiteOrGrow(Context* c, Value* v, Site* s, unsigned index)
{
  Site* n = pickSite(c, v, s, index, false);
  if (n) {
    return n;
  }

  n = s->makeNextWord(c, index);
  addSite(c, v, n);
  return n;
}

Site*
pickSiteOrGrow(Context* c, Value* v, Site* s, Site** low, Site** high)
{
  if (v->wordIndex == 0) {
    *low = s;
    *high = pickSiteOrGrow(c, v->nextWord, s, 1);
    return *high;
  } else {
    *low = pickSiteOrGrow(c, v->nextWord, s, 0);
    *high = s;
    return *low;
  }
}

bool
isHome(Value* v, int frameIndex)
{
  Value* p = v;
  do {
    if (p->home == frameIndex) {
      return true;
    }
    p = p->buddy;
  } while (p != v);

  return false;
}

bool
acceptForResolve(Context* c, Site* s, Read* read, const SiteMask& mask)
{
  if (acceptMatch(c, s, read, mask) and (not s->frozen(c))) {
    if (s->type(c) == RegisterOperand) {
      return c->availableGeneralRegisterCount > ResolveRegisterReserveCount;
    } else {
      assert(c, s->match(c, SiteMask(1 << MemoryOperand, 0, AnyFrameIndex)));

      return isHome(read->value, offsetToFrameIndex
                    (c, static_cast<MemorySite*>(s)->offset));
    }
  } else {
    return false;
  }
}

void
move(Context* c, Value* value, Site* src, Site* dst)
{
  if (DebugMoves) {
    char srcb[256]; src->toString(c, srcb, 256);
    char dstb[256]; dst->toString(c, dstb, 256);
    fprintf(stderr, "move %s to %s for %p to %p\n",
            srcb, dstb, value, value);
  }

  assert(c, findSite(c, value, dst));

  src->freeze(c, value);
  dst->freeze(c, value);
  
  unsigned srcSize;
  unsigned dstSize;
  if (value->nextWord == value) {
    srcSize = TargetBytesPerWord;
    dstSize = TargetBytesPerWord;
  } else {
    srcSize = src->registerSize(c);
    dstSize = dst->registerSize(c);
  }

  if (srcSize == dstSize) {
    apply(c, Move, srcSize, src, src, dstSize, dst, dst);
  } else if (srcSize > TargetBytesPerWord) {
    Site* low, *high, *other = pickSiteOrGrow(c, value, dst, &low, &high);
    other->freeze(c, value->nextWord);

    apply(c, Move, srcSize, src, src, srcSize, low, high);

    other->thaw(c, value->nextWord);
  } else {
    Site* low, *high, *other = pickSiteOrMove(c, value, src, &low, &high);
    other->freeze(c, value->nextWord);

    apply(c, Move, dstSize, low, high, dstSize, dst, dst);

    other->thaw(c, value->nextWord);
  }

  dst->thaw(c, value);
  src->thaw(c, value);
}

void
asAssemblerOperand(Context* c, Site* low, Site* high,
                   Assembler::Operand* result)
{
  low->asAssemblerOperand(c, high, result);
}

class OperandUnion: public Assembler::Operand {
  // must be large enough and aligned properly to hold any operand
  // type (we'd use an actual union type here, except that classes
  // with constructors cannot be used in a union):
  uintptr_t padding[4];
};

void
apply(Context* c, UnaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High)
{
  assert(c, s1Low->type(c) == s1High->type(c));

  OperandType s1Type = s1Low->type(c);
  OperandUnion s1Union; asAssemblerOperand(c, s1Low, s1High, &s1Union);

  c->assembler->apply(op, s1Size, s1Type, &s1Union);
}

void
apply(Context* c, BinaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High,
      unsigned s2Size, Site* s2Low, Site* s2High)
{
  assert(c, s1Low->type(c) == s1High->type(c));
  assert(c, s2Low->type(c) == s2High->type(c));

  OperandType s1Type = s1Low->type(c);
  OperandUnion s1Union; asAssemblerOperand(c, s1Low, s1High, &s1Union);

  OperandType s2Type = s2Low->type(c);
  OperandUnion s2Union; asAssemblerOperand(c, s2Low, s2High, &s2Union);

  c->assembler->apply(op, s1Size, s1Type, &s1Union,
                      s2Size, s2Type, &s2Union);
}

void
apply(Context* c, TernaryOperation op,
      unsigned s1Size, Site* s1Low, Site* s1High,
      unsigned s2Size, Site* s2Low, Site* s2High,
      unsigned s3Size, Site* s3Low, Site* s3High)
{
  assert(c, s1Low->type(c) == s1High->type(c));
  assert(c, s2Low->type(c) == s2High->type(c));
  assert(c, s3Low->type(c) == s3High->type(c));

  OperandType s1Type = s1Low->type(c);
  OperandUnion s1Union; asAssemblerOperand(c, s1Low, s1High, &s1Union);

  OperandType s2Type = s2Low->type(c);
  OperandUnion s2Union; asAssemblerOperand(c, s2Low, s2High, &s2Union);

  OperandType s3Type = s3Low->type(c);
  OperandUnion s3Union; asAssemblerOperand(c, s3Low, s3High, &s3Union);

  c->assembler->apply(op, s1Size, s1Type, &s1Union,
                      s2Size, s2Type, &s2Union,
                      s3Size, s3Type, &s3Union);
}

void
addRead(Context* c, Event* e, Value* v, Read* r)
{
  if (DebugReads) {
    fprintf(stderr, "add read %p to %p last %p event %p (%s)\n",
            r, v, v->lastRead, e, (e ? e->name() : 0));
  }

  r->value = v;
  if (e) {
    r->event = e;
    r->eventNext = e->reads;
    e->reads = r;
    ++ e->readCount;
  }

  if (v->lastRead) {
    //     if (DebugReads) {
    //       fprintf(stderr, "append %p to %p for %p\n", r, v->lastRead, v);
    //     }

    v->lastRead->append(c, r);
  } else {
    v->reads = r;
  }
  v->lastRead = r;
}

void
addRead(Context* c, Event* e, Value* v, const SiteMask& mask,
        Value* successor = 0)
{
  addRead(c, e, v, read(c, mask, successor));
}

void
addReads(Context* c, Event* e, Value* v, unsigned size,
         const SiteMask& lowMask, Value* lowSuccessor,
         const SiteMask& highMask, Value* highSuccessor)
{
  SingleRead* r = read(c, lowMask, lowSuccessor);
  addRead(c, e, v, r);
  if (size > TargetBytesPerWord) {
    r->high_ = v->nextWord;
    addRead(c, e, v->nextWord, highMask, highSuccessor);
  }
}

void
addReads(Context* c, Event* e, Value* v, unsigned size,
         const SiteMask& lowMask, const SiteMask& highMask)
{
  addReads(c, e, v, size, lowMask, 0, highMask, 0);
}

void
clean(Context* c, Value* v, unsigned popIndex)
{
  for (SiteIterator it(c, v); it.hasMore();) {
    Site* s = it.next();
    if (not (s->match(c, SiteMask(1 << MemoryOperand, 0, AnyFrameIndex))
             and offsetToFrameIndex
             (c, static_cast<MemorySite*>(s)->offset)
             >= popIndex))
    {
      if (false and
          s->match(c, SiteMask(1 << MemoryOperand, 0, AnyFrameIndex)))
      {
        char buffer[256]; s->toString(c, buffer, 256);
        fprintf(stderr, "remove %s from %p at %d pop offset 0x%x\n",
                buffer, v, offsetToFrameIndex
                (c, static_cast<MemorySite*>(s)->offset),
                frameIndexToOffset(c, popIndex));
      }
      it.remove(c);
    }
  }
}

void
clean(Context* c, Event* e, Stack* stack, Local* locals, Read* reads,
      unsigned popIndex)
{
  for (FrameIterator it(c, stack, locals); it.hasMore();) {
    FrameIterator::Element e = it.next(c);
    clean(c, e.value, popIndex);
  }

  for (Read* r = reads; r; r = r->eventNext) {
    popRead(c, e, r->value);
  }
}

CodePromise*
codePromise(Context* c, Event* e)
{
  return e->promises = new (c->zone->allocate(sizeof(CodePromise)))
    CodePromise(c, e->promises);
}

CodePromise*
codePromise(Context* c, Promise* offset)
{
  return new (c->zone->allocate(sizeof(CodePromise)))
    CodePromise(c, offset);
}

void
append(Context* c, Event* e);

void
saveLocals(Context* c, Event* e)
{
  for (unsigned li = 0; li < c->localFootprint; ++li) {
    Local* local = e->localsBefore + li;
    if (local->value) {
      if (DebugReads) {
        fprintf(stderr, "local save read %p at %d of %d\n",
                local->value, local::frameIndex(c, li), totalFrameSize(c));
      }

      addRead(c, e, local->value, SiteMask
              (1 << MemoryOperand, 0, local::frameIndex(c, li)));
    }
  }
}

class CallEvent: public Event {
 public:
  CallEvent(Context* c, Value* address, unsigned flags,
            TraceHandler* traceHandler, Value* result, unsigned resultSize,
            Stack* argumentStack, unsigned argumentCount,
            unsigned stackArgumentFootprint):
    Event(c),
    address(address),
    traceHandler(traceHandler),
    result(result),
    returnAddressSurrogate(0),
    framePointerSurrogate(0),
    popIndex(0),
    stackArgumentIndex(0),
    flags(flags),
    resultSize(resultSize),
    stackArgumentFootprint(stackArgumentFootprint)
  {
    uint32_t registerMask = c->arch->generalRegisterMask();

    if (argumentCount) {
      assert(c, (flags & Compiler::TailJump) == 0);
      assert(c, stackArgumentFootprint == 0);

      Stack* s = argumentStack;
      unsigned index = 0;
      unsigned argumentIndex = 0;

      while (true) {
        unsigned footprint
          = (argumentIndex + 1 < argumentCount
             and s->value->nextWord == s->next->value)
          ? 2 : 1;

        if (index % (c->arch->argumentAlignment() ? footprint : 1)) {
          ++ index;
        }

        SiteMask targetMask;
        if (index + (c->arch->argumentRegisterAlignment() ? footprint : 1)
            <= c->arch->argumentRegisterCount())
        {
          int number = c->arch->argumentRegister(index);
        
          if (DebugReads) {
            fprintf(stderr, "reg %d arg read %p\n", number, s->value);
          }

          targetMask = fixedRegisterMask(number);
          registerMask &= ~(1 << number);
        } else {
          if (index < c->arch->argumentRegisterCount()) {
            index = c->arch->argumentRegisterCount();
          }

          unsigned frameIndex = index - c->arch->argumentRegisterCount();

          if (DebugReads) {
            fprintf(stderr, "stack %d arg read %p\n", frameIndex, s->value);
          }

          targetMask = SiteMask(1 << MemoryOperand, 0, frameIndex);
        }

        addRead(c, this, s->value, targetMask);

        ++ index;

        if ((++ argumentIndex) < argumentCount) {
          s = s->next;
        } else {
          break;
        }
      }
    }

    if (DebugReads) {
      fprintf(stderr, "address read %p\n", address);
    }

    { bool thunk;
      uint8_t typeMask;
      uint64_t planRegisterMask;
      c->arch->plan
        ((flags & Compiler::Aligned) ? AlignedCall : Call, TargetBytesPerWord,
         &typeMask, &planRegisterMask, &thunk);

      assert(c, not thunk);

      addRead(c, this, address, SiteMask
               (typeMask, registerMask & planRegisterMask, AnyFrameIndex));
    }

    Stack* stack = stackBefore;

    if (stackArgumentFootprint) {
      RUNTIME_ARRAY(Value*, arguments, stackArgumentFootprint);
      for (int i = stackArgumentFootprint - 1; i >= 0; --i) {
        Value* v = stack->value;
        stack = stack->next;

        if ((TargetBytesPerWord == 8
             and (v == 0 or (i >= 1 and stack->value == 0)))
            or (TargetBytesPerWord == 4 and v->nextWord != v))
        {
          assert(c, TargetBytesPerWord == 8 or v->nextWord == stack->value);

          RUNTIME_ARRAY_BODY(arguments)[i--] = stack->value;
          stack = stack->next;
        }
        RUNTIME_ARRAY_BODY(arguments)[i] = v;
      }

      int returnAddressIndex;
      int framePointerIndex;
      int frameOffset;

      if (TailCalls and (flags & Compiler::TailJump)) {
        assert(c, argumentCount == 0);

        int base = frameBase(c);
        returnAddressIndex = base + c->arch->returnAddressOffset();
        if (UseFramePointer) {
          framePointerIndex = base + c->arch->framePointerOffset();
        } else {
          framePointerIndex = -1;
        }

        frameOffset = totalFrameSize(c)
          - c->arch->argumentFootprint(stackArgumentFootprint);
      } else {
        returnAddressIndex = -1;
        framePointerIndex = -1;
        frameOffset = 0;
      }

      for (unsigned i = 0; i < stackArgumentFootprint; ++i) {
        Value* v = RUNTIME_ARRAY_BODY(arguments)[i];
        if (v) {
          int frameIndex = i + frameOffset;

          if (DebugReads) {
            fprintf(stderr, "stack arg read %p at %d of %d\n",
                    v, frameIndex, totalFrameSize(c));
          }

          if (static_cast<int>(frameIndex) == returnAddressIndex) {
            returnAddressSurrogate = v;
            addRead(c, this, v, generalRegisterMask(c));
          } else if (static_cast<int>(frameIndex) == framePointerIndex) {
            framePointerSurrogate = v;
            addRead(c, this, v, generalRegisterMask(c));
          } else {
            addRead(c, this, v, SiteMask(1 << MemoryOperand, 0, frameIndex));
          }
        }
      }
    }

    if ((not TailCalls) or (flags & Compiler::TailJump) == 0) {
      stackArgumentIndex = c->localFootprint;
      if (stackBefore) {
        stackArgumentIndex += stackBefore->index + 1 - stackArgumentFootprint;
      }

      popIndex
        = c->alignedFrameSize
        + c->parameterFootprint
        - c->arch->frameFooterSize()
        - stackArgumentIndex;

      assert(c, static_cast<int>(popIndex) >= 0);

      while (stack) {
        if (stack->value) {
          unsigned logicalIndex = local::frameIndex
            (c, stack->index + c->localFootprint);

          if (DebugReads) {
            fprintf(stderr, "stack save read %p at %d of %d\n",
                    stack->value, logicalIndex, totalFrameSize(c));
          }

          addRead(c, this, stack->value, SiteMask
                  (1 << MemoryOperand, 0, logicalIndex));
        }

        stack = stack->next;
      }

      saveLocals(c, this);
    }
  }

  virtual const char* name() {
    return "CallEvent";
  }

  virtual void compile(Context* c) {
    UnaryOperation op;

    if (TailCalls and (flags & Compiler::TailJump)) {
      if (flags & Compiler::LongJumpOrCall) {
        if (flags & Compiler::Aligned) {
          op = AlignedLongJump;
        } else {
          op = LongJump;
        }
      } else if (flags & Compiler::Aligned) {
        op = AlignedJump;
      } else {
        op = Jump;
      }

      assert(c, returnAddressSurrogate == 0
             or returnAddressSurrogate->source->type(c) == RegisterOperand);
      assert(c, framePointerSurrogate == 0
             or framePointerSurrogate->source->type(c) == RegisterOperand);

      int ras;
      if (returnAddressSurrogate) {
        returnAddressSurrogate->source->freeze(c, returnAddressSurrogate);

        ras = static_cast<RegisterSite*>
          (returnAddressSurrogate->source)->number;
      } else {
        ras = NoRegister;
      }

      int fps;
      if (framePointerSurrogate) {
        framePointerSurrogate->source->freeze(c, framePointerSurrogate);

        fps = static_cast<RegisterSite*>
          (framePointerSurrogate->source)->number;
      } else {
        fps = NoRegister;
      }

      int offset
        = static_cast<int>(c->arch->argumentFootprint(stackArgumentFootprint))
        - static_cast<int>(c->arch->argumentFootprint(c->parameterFootprint));

      c->assembler->popFrameForTailCall(c->alignedFrameSize, offset, ras, fps);
    } else if (flags & Compiler::LongJumpOrCall) {
      if (flags & Compiler::Aligned) {
        op = AlignedLongCall;
      } else {
        op = LongCall;
      }
    } else if (flags & Compiler::Aligned) {
      op = AlignedCall;
    } else {
      op = Call;
    }

    apply(c, op, TargetBytesPerWord, address->source, address->source);

    if (traceHandler) {
      traceHandler->handleTrace(codePromise(c, c->assembler->offset(true)),
                                stackArgumentIndex);
    }

    if (TailCalls) {
      if (flags & Compiler::TailJump) {
        if (returnAddressSurrogate) {
          returnAddressSurrogate->source->thaw(c, returnAddressSurrogate);
        }

        if (framePointerSurrogate) {
          framePointerSurrogate->source->thaw(c, framePointerSurrogate);
        }
      } else {
        unsigned footprint = c->arch->argumentFootprint
          (stackArgumentFootprint);

        if (footprint > c->arch->stackAlignmentInWords()) {
          c->assembler->adjustFrame
            (footprint - c->arch->stackAlignmentInWords());
        }
      }
    }

    clean(c, this, stackBefore, localsBefore, reads, popIndex);

    if (resultSize and live(c, result)) {
      addSite(c, result, registerSite(c, c->arch->returnLow()));
      if (resultSize > TargetBytesPerWord and live(c, result->nextWord)) {
        addSite(c, result->nextWord, registerSite(c, c->arch->returnHigh()));
      }
    }
  }

  virtual bool allExits() {
    return (flags & Compiler::TailJump) != 0;
  }

  Value* address;
  TraceHandler* traceHandler;
  Value* result;
  Value* returnAddressSurrogate;
  Value* framePointerSurrogate;
  unsigned popIndex;
  unsigned stackArgumentIndex;
  unsigned flags;
  unsigned resultSize;
  unsigned stackArgumentFootprint;
};

void
appendCall(Context* c, Value* address, unsigned flags,
           TraceHandler* traceHandler, Value* result, unsigned resultSize,
           Stack* argumentStack, unsigned argumentCount,
           unsigned stackArgumentFootprint)
{
  append(c, new (c->zone->allocate(sizeof(CallEvent)))
         CallEvent(c, address, flags, traceHandler, result,
                   resultSize, argumentStack, argumentCount,
                   stackArgumentFootprint));
}

bool
unreachable(Event* event)
{
  for (Link* p = event->predecessors; p; p = p->nextPredecessor) {
    if (not p->predecessor->allExits()) return false;
  }
  return event->predecessors != 0;
}

class ReturnEvent: public Event {
 public:
  ReturnEvent(Context* c, unsigned size, Value* value):
    Event(c), value(value)
  {
    if (value) {
      addReads(c, this, value, size, fixedRegisterMask(c->arch->returnLow()),
               fixedRegisterMask(c->arch->returnHigh()));
    }
  }

  virtual const char* name() {
    return "ReturnEvent";
  }

  virtual void compile(Context* c) {
    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }
    
    if (not unreachable(this)) {
      c->assembler->popFrameAndPopArgumentsAndReturn
        (c->alignedFrameSize,
         c->arch->argumentFootprint(c->parameterFootprint));
    }
  }

  Value* value;
};

void
appendReturn(Context* c, unsigned size, Value* value)
{
  append(c, new (c->zone->allocate(sizeof(ReturnEvent)))
         ReturnEvent(c, size, value));
}

void
maybeMove(Context* c, BinaryOperation type, unsigned srcSize,
          unsigned srcSelectSize, Value* src, unsigned dstSize, Value* dst,
          const SiteMask& dstMask)
{
  Read* read = live(c, dst);
  bool isStore = read == 0;

  Site* target;
  if (dst->target) {
    target = dst->target;
  } else if (isStore) {
    return;
  } else {
    target = pickTargetSite(c, read);
  }

  unsigned cost = src->source->copyCost(c, target);

  if (srcSelectSize < dstSize) cost = 1;

  if (cost) {
    // todo: let c->arch->planMove decide this:
    bool useTemporary = ((target->type(c) == MemoryOperand
                          and src->source->type(c) == MemoryOperand)
                         or (srcSelectSize < dstSize
                             and target->type(c) != RegisterOperand));

    src->source->freeze(c, src);

    addSite(c, dst, target);

    src->source->thaw(c, src);

    bool addOffset = srcSize != srcSelectSize
      and c->arch->bigEndian()
      and src->source->type(c) == MemoryOperand;

    if (addOffset) {
      static_cast<MemorySite*>(src->source)->offset
        += (srcSize - srcSelectSize);
    }

    target->freeze(c, dst);

    if (target->match(c, dstMask) and not useTemporary) {
      if (DebugMoves) {
        char srcb[256]; src->source->toString(c, srcb, 256);
        char dstb[256]; target->toString(c, dstb, 256);
        fprintf(stderr, "move %s to %s for %p to %p\n",
                srcb, dstb, src, dst);
      }

      src->source->freeze(c, src);

      apply(c, type, min(srcSelectSize, dstSize), src->source, src->source,
            dstSize, target, target);

      src->source->thaw(c, src);
    } else {
      // pick a temporary register which is valid as both a
      // destination and a source for the moves we need to perform:
      
      removeSite(c, dst, target);

      bool thunk;
      uint8_t srcTypeMask;
      uint64_t srcRegisterMask;

      c->arch->planSource(type, dstSize, &srcTypeMask, &srcRegisterMask,
                          dstSize, &thunk);

      if (src->type == ValueGeneral) {
        srcRegisterMask &= c->arch->generalRegisterMask();
      }

      assert(c, thunk == 0);
      assert(c, dstMask.typeMask & srcTypeMask & (1 << RegisterOperand));

      Site* tmpTarget = freeRegisterSite
        (c, dstMask.registerMask & srcRegisterMask);

      src->source->freeze(c, src);

      addSite(c, dst, tmpTarget);

      tmpTarget->freeze(c, dst);

      if (DebugMoves) {
        char srcb[256]; src->source->toString(c, srcb, 256);
        char dstb[256]; tmpTarget->toString(c, dstb, 256);
        fprintf(stderr, "move %s to %s for %p to %p\n",
                srcb, dstb, src, dst);
      }

      apply(c, type, srcSelectSize, src->source, src->source,
            dstSize, tmpTarget, tmpTarget);

      tmpTarget->thaw(c, dst);

      src->source->thaw(c, src);

      if (useTemporary or isStore) {
        if (DebugMoves) {
          char srcb[256]; tmpTarget->toString(c, srcb, 256);
          char dstb[256]; target->toString(c, dstb, 256);
          fprintf(stderr, "move %s to %s for %p to %p\n",
                  srcb, dstb, src, dst);
        }

        addSite(c, dst, target);

        tmpTarget->freeze(c, dst);

        apply(c, Move, dstSize, tmpTarget, tmpTarget, dstSize, target, target);

        tmpTarget->thaw(c, dst);

        if (isStore) {
          removeSite(c, dst, tmpTarget);
        }
      }
    }

    target->thaw(c, dst);

    if (addOffset) {
      static_cast<MemorySite*>(src->source)->offset
        -= (srcSize - srcSelectSize);
    }
  } else {
    target = src->source;

    if (DebugMoves) {
      char dstb[256]; target->toString(c, dstb, 256);
      fprintf(stderr, "null move in %s for %p to %p\n", dstb, src, dst);
    }
  }

  if (isStore) {
    removeSite(c, dst, target);
  }
}

Site*
pickMatchOrMove(Context* c, Read* r, Site* nextWord, unsigned index,
                bool intersectRead)
{
  Site* s = pickSite(c, r->value, nextWord, index, true);
  SiteMask mask;
  if (intersectRead) {
    r->intersect(&mask);
  }
  if (s and s->match(c, mask)) {
    return s;
  }

  return pickSiteOrMove
    (c, r->value, intersect(mask, nextWord->nextWordMask(c, index)),
     true, true);
}

Site*
pickSiteOrMove(Context* c, Value* src, Value* dst, Site* nextWord,
               unsigned index)
{
  if (live(c, dst)) {
    Read* read = live(c, src);
    Site* s;
    if (nextWord) {
      s = pickMatchOrMove(c, read, nextWord, index, false);
    } else {
      s = pickSourceSite(c, read, 0, 0, 0, false, true, true);

      if (s == 0 or s->isVolatile(c)) {
        s = maybeMove(c, read, false, true);
      }
    }
    assert(c, s);

    addBuddy(src, dst);

    if (src->source->isVolatile(c)) {
      removeSite(c, src, src->source);
    }

    return s;
  } else {
    return 0;
  }
}

Value*
value(Context* c, ValueType type, Site* site = 0, Site* target = 0)
{
  return new (c->zone->allocate(sizeof(Value))) Value(site, target, type);
}

void
grow(Context* c, Value* v)
{
  assert(c, v->nextWord == v);

  Value* next = value(c, v->type);
  v->nextWord = next;
  next->nextWord = v;
  next->wordIndex = 1;
}

void
split(Context* c, Value* v)
{
  grow(c, v);
  for (SiteIterator it(c, v); it.hasMore();) {
    Site* s = it.next();
    removeSite(c, v, s);
    
    addSite(c, v, s->copyLow(c));
    addSite(c, v->nextWord, s->copyHigh(c));
  }
}

void
maybeSplit(Context* c, Value* v)
{
  if (v->nextWord == v) {
    split(c, v);
  }
}

class MoveEvent: public Event {
 public:
  MoveEvent(Context* c, BinaryOperation type, unsigned srcSize,
            unsigned srcSelectSize, Value* src, unsigned dstSize, Value* dst,
            const SiteMask& srcLowMask, const SiteMask& srcHighMask):
    Event(c), type(type), srcSize(srcSize), srcSelectSize(srcSelectSize),
    src(src), dstSize(dstSize), dst(dst)
  {
    assert(c, srcSelectSize <= srcSize);

    bool noop = srcSelectSize >= dstSize;
    
    if (dstSize > TargetBytesPerWord) {
      grow(c, dst);
    }

    if (srcSelectSize > TargetBytesPerWord) {
      maybeSplit(c, src);
    }

    addReads(c, this, src, srcSelectSize, srcLowMask, noop ? dst : 0,
             srcHighMask,
             noop and dstSize > TargetBytesPerWord ? dst->nextWord : 0);
  }

  virtual const char* name() {
    return "MoveEvent";
  }

  virtual void compile(Context* c) {
    uint8_t dstTypeMask;
    uint64_t dstRegisterMask;

    c->arch->planDestination
      (type,
       srcSelectSize,
       1 << src->source->type(c), 
       (static_cast<uint64_t>(src->nextWord->source->registerMask(c)) << 32)
       | static_cast<uint64_t>(src->source->registerMask(c)),
       dstSize,
       &dstTypeMask,
       &dstRegisterMask);

    SiteMask dstLowMask(dstTypeMask, dstRegisterMask, AnyFrameIndex);
    SiteMask dstHighMask(dstTypeMask, dstRegisterMask >> 32, AnyFrameIndex);

    if (srcSelectSize >= TargetBytesPerWord
        and dstSize >= TargetBytesPerWord
        and srcSelectSize >= dstSize)
    {
      if (dst->target) {
        if (dstSize > TargetBytesPerWord
            and src->source->registerSize(c) > TargetBytesPerWord)
        {
          apply(c, Move, srcSelectSize, src->source, src->source,
                dstSize, dst->target, dst->target);

          if (live(c, dst) == 0) {
            removeSite(c, dst, dst->target);
            if (dstSize > TargetBytesPerWord) {
              removeSite(c, dst->nextWord, dst->nextWord->target);
            }
          }
        } else {
          maybeMove(c, Move, TargetBytesPerWord, TargetBytesPerWord, src,
                    TargetBytesPerWord, dst, dstLowMask);
          if (dstSize > TargetBytesPerWord) {
            maybeMove
              (c, Move, TargetBytesPerWord, TargetBytesPerWord, src->nextWord,
               TargetBytesPerWord, dst->nextWord, dstHighMask);
          }
        }
      } else {
        Site* low = pickSiteOrMove(c, src, dst, 0, 0);
        if (dstSize > TargetBytesPerWord) {
          pickSiteOrMove(c, src->nextWord, dst->nextWord, low, 1);
        }
      }
    } else if (srcSelectSize <= TargetBytesPerWord
               and dstSize <= TargetBytesPerWord)
    {
      maybeMove(c, type, srcSize, srcSelectSize, src, dstSize, dst,
                dstLowMask);
    } else {
      assert(c, srcSize == TargetBytesPerWord);
      assert(c, srcSelectSize == TargetBytesPerWord);

      if (dst->nextWord->target or live(c, dst->nextWord)) {
        assert(c, dstLowMask.typeMask & (1 << RegisterOperand));

        Site* low = freeRegisterSite(c, dstLowMask.registerMask);

        src->source->freeze(c, src);

        addSite(c, dst, low);

        low->freeze(c, dst);
          
        if (DebugMoves) {
          char srcb[256]; src->source->toString(c, srcb, 256);
          char dstb[256]; low->toString(c, dstb, 256);
          fprintf(stderr, "move %s to %s for %p\n",
                  srcb, dstb, src);
        }

        apply(c, Move, TargetBytesPerWord, src->source, src->source,
              TargetBytesPerWord, low, low);

        low->thaw(c, dst);

        src->source->thaw(c, src);

        assert(c, dstHighMask.typeMask & (1 << RegisterOperand));

        Site* high = freeRegisterSite(c, dstHighMask.registerMask);

        low->freeze(c, dst);

        addSite(c, dst->nextWord, high);

        high->freeze(c, dst->nextWord);
        
        if (DebugMoves) {
          char srcb[256]; low->toString(c, srcb, 256);
          char dstb[256]; high->toString(c, dstb, 256);
          fprintf(stderr, "extend %s to %s for %p %p\n",
                  srcb, dstb, dst, dst->nextWord);
        }

        apply(c, Move, TargetBytesPerWord, low, low, dstSize, low, high);

        high->thaw(c, dst->nextWord);

        low->thaw(c, dst);
      } else {
        pickSiteOrMove(c, src, dst, 0, 0);
      }
    }

    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }
  }

  BinaryOperation type;
  unsigned srcSize;
  unsigned srcSelectSize;
  Value* src;
  unsigned dstSize;
  Value* dst;
};

void
appendMove(Context* c, BinaryOperation type, unsigned srcSize,
           unsigned srcSelectSize, Value* src, unsigned dstSize, Value* dst)
{
  bool thunk;
  uint8_t srcTypeMask;
  uint64_t srcRegisterMask;

  c->arch->planSource
    (type, srcSelectSize, &srcTypeMask, &srcRegisterMask, dstSize, &thunk);

  assert(c, not thunk);

  append(c, new (c->zone->allocate(sizeof(MoveEvent)))
         MoveEvent
         (c, type, srcSize, srcSelectSize, src, dstSize, dst,
          SiteMask(srcTypeMask, srcRegisterMask, AnyFrameIndex),
          SiteMask(srcTypeMask, srcRegisterMask >> 32, AnyFrameIndex)));
}

ConstantSite*
findConstantSite(Context* c, Value* v)
{
  for (SiteIterator it(c, v); it.hasMore();) {
    Site* s = it.next();
    if (s->type(c) == ConstantOperand) {
      return static_cast<ConstantSite*>(s);
    }
  }
  return 0;
}

void
preserve(Context* c, Value* v, Read* r, Site* s)
{
  s->freeze(c, v);

  maybeMove(c, r, false, true, 0);

  s->thaw(c, v);
}

Site*
getTarget(Context* c, Value* value, Value* result, const SiteMask& resultMask)
{
  Site* s;
  Value* v;
  Read* r = liveNext(c, value);
  if (value->source->match
      (c, static_cast<const SiteMask&>(resultMask))
      and (r == 0 or value->source->loneMatch
           (c, static_cast<const SiteMask&>(resultMask))))
  {
    s = value->source;
    v = value;
    if (r and uniqueSite(c, v, s)) {
      preserve(c, v, r, s);
    }
  } else {
    SingleRead r(resultMask, 0);
    r.value = result;
    r.successor_ = result;
    s = pickTargetSite(c, &r, true);
    v = result;
    addSite(c, result, s);
  }

  removeSite(c, v, s);

  s->freeze(c, v);

  return s;
}

void
freezeSource(Context* c, unsigned size, Value* v)
{
  v->source->freeze(c, v);
  if (size > TargetBytesPerWord) {
    v->nextWord->source->freeze(c, v->nextWord);
  }
}

void
thawSource(Context* c, unsigned size, Value* v)
{
  v->source->thaw(c, v);
  if (size > TargetBytesPerWord) {
    v->nextWord->source->thaw(c, v->nextWord);
  }
}

class CombineEvent: public Event {
 public:
  CombineEvent(Context* c, TernaryOperation type,
               unsigned firstSize, Value* first,
               unsigned secondSize, Value* second,
               unsigned resultSize, Value* result,
               const SiteMask& firstLowMask,
               const SiteMask& firstHighMask,
               const SiteMask& secondLowMask,
               const SiteMask& secondHighMask):
    Event(c), type(type), firstSize(firstSize), first(first),
    secondSize(secondSize), second(second), resultSize(resultSize),
    result(result)
  {
    addReads(c, this, first, firstSize, firstLowMask, firstHighMask);

    if (resultSize > TargetBytesPerWord) {
      grow(c, result);
    }

    bool condensed = c->arch->alwaysCondensed(type);

    addReads(c, this, second, secondSize,
             secondLowMask, condensed ? result : 0,
             secondHighMask, condensed ? result->nextWord : 0);
  }

  virtual const char* name() {
    return "CombineEvent";
  }

  virtual void compile(Context* c) {
    assert(c, first->source->type(c) == first->nextWord->source->type(c));

    // if (second->source->type(c) != second->nextWord->source->type(c)) {
    //   fprintf(stderr, "%p %p %d : %p %p %d\n",
    //           second, second->source, second->source->type(c),
    //           second->nextWord, second->nextWord->source,
    //           second->nextWord->source->type(c));
    // }

    assert(c, second->source->type(c) == second->nextWord->source->type(c));
    
    freezeSource(c, firstSize, first);
    
    uint8_t cTypeMask;
    uint64_t cRegisterMask;

    c->arch->planDestination
      (type,
       firstSize,
       1 << first->source->type(c),
       (static_cast<uint64_t>(first->nextWord->source->registerMask(c)) << 32)
       | static_cast<uint64_t>(first->source->registerMask(c)),
       secondSize,
       1 << second->source->type(c),
       (static_cast<uint64_t>(second->nextWord->source->registerMask(c)) << 32)
       | static_cast<uint64_t>(second->source->registerMask(c)),
       resultSize,
       &cTypeMask,
       &cRegisterMask);

    SiteMask resultLowMask(cTypeMask, cRegisterMask, AnyFrameIndex);
    SiteMask resultHighMask(cTypeMask, cRegisterMask >> 32, AnyFrameIndex);

    Site* low = getTarget(c, second, result, resultLowMask);
    unsigned lowSize = low->registerSize(c);
    Site* high
      = (resultSize > lowSize
         ? getTarget(c, second->nextWord, result->nextWord, resultHighMask)
         : low);

//     fprintf(stderr, "combine %p:%p and %p:%p into %p:%p\n",
//             first, first->nextWord,
//             second, second->nextWord,
//             result, result->nextWord);

    apply(c, type,
          firstSize, first->source, first->nextWord->source,
          secondSize, second->source, second->nextWord->source,
          resultSize, low, high);

    thawSource(c, firstSize, first);

    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }

    low->thaw(c, second);
    if (resultSize > lowSize) {
      high->thaw(c, second->nextWord);
    }

    if (live(c, result)) {
      addSite(c, result, low);
      if (resultSize > lowSize and live(c, result->nextWord)) {
        addSite(c, result->nextWord, high);
      }
    }
  }

  TernaryOperation type;
  unsigned firstSize;
  Value* first;
  unsigned secondSize;
  Value* second;
  unsigned resultSize;
  Value* result;
};

void
removeBuddy(Context* c, Value* v)
{
  if (v->buddy != v) {
    if (DebugBuddies) {
      fprintf(stderr, "remove buddy %p from", v);
      for (Value* p = v->buddy; p != v; p = p->buddy) {
        fprintf(stderr, " %p", p);
      }
      fprintf(stderr, "\n");
    }

    assert(c, v->buddy);

    Value* next = v->buddy;
    v->buddy = v;
    Value* p = next;
    while (p->buddy != v) p = p->buddy;
    p->buddy = next;

    assert(c, p->buddy);

    if (not live(c, next)) {
      clearSites(c, next);
    }

    if (not live(c, v)) {
      clearSites(c, v);
    }
  }
}

Site*
copy(Context* c, Site* s)
{
  Site* start = 0;
  Site* end = 0;
  for (; s; s = s->next) {
    Site* n = s->copy(c);
    if (end) {
      end->next = n;
    } else {
      start = n;
    }
    end = n;
  }
  return start;
}

class Snapshot {
 public:
  Snapshot(Context* c, Value* value, Snapshot* next):
    value(value), buddy(value->buddy), sites(copy(c, value->sites)), next(next)
  { }

  Value* value;
  Value* buddy;
  Site* sites;
  Snapshot* next;
};

Snapshot*
snapshot(Context* c, Value* value, Snapshot* next)
{
  if (DebugControl) {
    char buffer[256]; sitesToString(c, value->sites, buffer, 256);
    fprintf(stderr, "snapshot %p buddy %p sites %s\n",
            value, value->buddy, buffer);
  }

  return new (c->zone->allocate(sizeof(Snapshot))) Snapshot(c, value, next);
}

Snapshot*
makeSnapshots(Context* c, Value* value, Snapshot* next)
{
  next = snapshot(c, value, next);
  for (Value* p = value->buddy; p != value; p = p->buddy) {
    next = snapshot(c, p, next);
  }
  return next;
}

Stack*
stack(Context* c, Value* value, Stack* next)
{
  return new (c->zone->allocate(sizeof(Stack)))
    Stack(next ? next->index + 1 : 0, value, next);
}

Value*
maybeBuddy(Context* c, Value* v);

Value*
pushWord(Context* c, Value* v)
{
  if (v) {
    v = maybeBuddy(c, v);
  }
    
  Stack* s = stack(c, v, c->stack);

  if (DebugFrame) {
    fprintf(stderr, "push %p\n", v);
  }

  if (v) {
    v->home = frameIndex(c, s->index + c->localFootprint);
  }
  c->stack = s;

  return v;
}

void
push(Context* c, unsigned footprint, Value* v)
{
  assert(c, footprint);

  bool bigEndian = c->arch->bigEndian();
  
  Value* low = v;
  
  if (bigEndian) {
    v = pushWord(c, v);
  }

  Value* high;
  if (footprint > 1) {
    assert(c, footprint == 2);

    if (TargetBytesPerWord == 4) {
      maybeSplit(c, low);
      high = pushWord(c, low->nextWord);
    } else {
      high = pushWord(c, 0);
    }
  } else {
    high = 0;
  }
  
  if (not bigEndian) {
    v = pushWord(c, v);
  }

  if (high) {
    v->nextWord = high;
    high->nextWord = v;
    high->wordIndex = 1;
  }
}

void
popWord(Context* c)
{
  Stack* s = c->stack;
  assert(c, s->value == 0 or s->value->home >= 0);

  if (DebugFrame) {
    fprintf(stderr, "pop %p\n", s->value);
  }
    
  c->stack = s->next;  
}

Value*
pop(Context* c, unsigned footprint)
{
  assert(c, footprint);

  Stack* s = 0;

  bool bigEndian = c->arch->bigEndian();

  if (not bigEndian) {
    s = c->stack;
  }

  if (footprint > 1) {
    assert(c, footprint == 2);

#ifndef NDEBUG
    Stack* low;
    Stack* high;
    if (bigEndian) {
      high = c->stack;
      low = high->next;
    } else {
      low = c->stack;
      high = low->next;
    }

    assert(c, (TargetBytesPerWord == 8
               and low->value->nextWord == low->value and high->value == 0)
           or (TargetBytesPerWord == 4 and low->value->nextWord == high->value));
#endif // not NDEBUG

    popWord(c);
  }

  if (bigEndian) {
    s = c->stack;
  }

  popWord(c);

  return s->value;
}

Value*
storeLocal(Context* c, unsigned footprint, Value* v, unsigned index, bool copy)
{
  assert(c, index + footprint <= c->localFootprint);

  if (copy) {
    unsigned sizeInBytes = sizeof(Local) * c->localFootprint;
    Local* newLocals = static_cast<Local*>(c->zone->allocate(sizeInBytes));
    memcpy(newLocals, c->locals, sizeInBytes);
    c->locals = newLocals;
  }

  Value* high;
  if (footprint > 1) {
    assert(c, footprint == 2);

    unsigned highIndex;
    unsigned lowIndex;
    if (c->arch->bigEndian()) {
      highIndex = index + 1;
      lowIndex = index;
    } else {
      lowIndex = index + 1;
      highIndex = index;      
    }

    if (TargetBytesPerWord == 4) {
      assert(c, v->nextWord != v);

      high = storeLocal(c, 1, v->nextWord, highIndex, false);
    } else {
      high = 0;
    }

    index = lowIndex;
  } else {
    high = 0;
  }

  v = maybeBuddy(c, v);

  if (high != 0) {
    v->nextWord = high;
    high->nextWord = v;
    high->wordIndex = 1;
  }

  Local* local = c->locals + index;
  local->value = v;

  if (DebugFrame) {
    fprintf(stderr, "store local %p at %d\n", local->value, index);
  }

  local->value->home = frameIndex(c, index);

  return v;
}

Value*
loadLocal(Context* c, unsigned footprint, unsigned index)
{
  assert(c, index + footprint <= c->localFootprint);

  if (footprint > 1) {
    assert(c, footprint == 2);

    if (not c->arch->bigEndian()) {
      ++ index;
    }
  }

  assert(c, c->locals[index].value);
  assert(c, c->locals[index].value->home >= 0);

  if (DebugFrame) {
    fprintf(stderr, "load local %p at %d\n", c->locals[index].value, index);
  }

  return c->locals[index].value;
}

Value*
register_(Context* c, int number)
{
  assert(c, (1 << number) & (c->arch->generalRegisterMask()
                             | c->arch->floatRegisterMask()));

  Site* s = registerSite(c, number);
  ValueType type = ((1 << number) & c->arch->floatRegisterMask())
    ? ValueFloat: ValueGeneral;

  return value(c, type, s, s);
}

void
appendCombine(Context* c, TernaryOperation type,
              unsigned firstSize, Value* first,
              unsigned secondSize, Value* second,
              unsigned resultSize, Value* result)
{
  bool thunk;
  uint8_t firstTypeMask;
  uint64_t firstRegisterMask;
  uint8_t secondTypeMask;
  uint64_t secondRegisterMask;

  c->arch->planSource(type, firstSize, &firstTypeMask, &firstRegisterMask,
                      secondSize, &secondTypeMask, &secondRegisterMask,
                      resultSize, &thunk);

  if (thunk) {
    Stack* oldStack = c->stack;

    bool threadParameter;
    intptr_t handler = c->client->getThunk
      (type, firstSize, resultSize, &threadParameter);

    unsigned stackSize = ceiling(secondSize, TargetBytesPerWord)
      + ceiling(firstSize, TargetBytesPerWord);

    local::push(c, ceiling(secondSize, TargetBytesPerWord), second);
    local::push(c, ceiling(firstSize, TargetBytesPerWord), first);

    if (threadParameter) {
      ++ stackSize;

      local::push(c, 1, register_(c, c->arch->thread()));
    }

    Stack* argumentStack = c->stack;
    c->stack = oldStack;

    appendCall
      (c, value(c, ValueGeneral, constantSite(c, handler)), 0, 0, result,
       resultSize, argumentStack, stackSize, 0);
  } else {
    append
      (c, new (c->zone->allocate(sizeof(CombineEvent)))
       CombineEvent
       (c, type,
        firstSize, first,
        secondSize, second,
        resultSize, result,
        SiteMask(firstTypeMask, firstRegisterMask, AnyFrameIndex),
        SiteMask(firstTypeMask, firstRegisterMask >> 32, AnyFrameIndex),
        SiteMask(secondTypeMask, secondRegisterMask, AnyFrameIndex),
        SiteMask(secondTypeMask, secondRegisterMask >> 32, AnyFrameIndex)));
  }
}

class TranslateEvent: public Event {
 public:
  TranslateEvent(Context* c, BinaryOperation type, unsigned valueSize,
                 Value* value, unsigned resultSize, Value* result,
                 const SiteMask& valueLowMask,
                 const SiteMask& valueHighMask):
    Event(c), type(type), valueSize(valueSize), resultSize(resultSize),
    value(value), result(result)
  {
    bool condensed = c->arch->alwaysCondensed(type);

    if (resultSize > TargetBytesPerWord) {
      grow(c, result);
    }

    addReads(c, this, value, valueSize, valueLowMask, condensed ? result : 0,
             valueHighMask, condensed ? result->nextWord : 0);
  }

  virtual const char* name() {
    return "TranslateEvent";
  }

  virtual void compile(Context* c) {
    assert(c, value->source->type(c) == value->nextWord->source->type(c));

    uint8_t bTypeMask;
    uint64_t bRegisterMask;
    
    c->arch->planDestination
      (type,
       valueSize,
       1 << value->source->type(c),
       (static_cast<uint64_t>(value->nextWord->source->registerMask(c)) << 32)
       | static_cast<uint64_t>(value->source->registerMask(c)),
       resultSize,
       &bTypeMask,
       &bRegisterMask);

    SiteMask resultLowMask(bTypeMask, bRegisterMask, AnyFrameIndex);
    SiteMask resultHighMask(bTypeMask, bRegisterMask >> 32, AnyFrameIndex);
    
    Site* low = getTarget(c, value, result, resultLowMask);
    unsigned lowSize = low->registerSize(c);
    Site* high
      = (resultSize > lowSize
         ? getTarget(c, value->nextWord, result->nextWord, resultHighMask)
         : low);

    apply(c, type, valueSize, value->source, value->nextWord->source,
          resultSize, low, high);

    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }

    low->thaw(c, value);
    if (resultSize > lowSize) {
      high->thaw(c, value->nextWord);
    }

    if (live(c, result)) {
      addSite(c, result, low);
      if (resultSize > lowSize and live(c, result->nextWord)) {
        addSite(c, result->nextWord, high);
      }
    }
  }

  BinaryOperation type;
  unsigned valueSize;
  unsigned resultSize;
  Value* value;
  Value* result;
  Read* resultRead;
  SiteMask resultLowMask;
  SiteMask resultHighMask;
};

void
appendTranslate(Context* c, BinaryOperation type, unsigned firstSize,
                Value* first, unsigned resultSize, Value* result)
{
  bool thunk;
  uint8_t firstTypeMask;
  uint64_t firstRegisterMask;

  c->arch->planSource(type, firstSize, &firstTypeMask, &firstRegisterMask,
                resultSize, &thunk);

  if (thunk) {
    Stack* oldStack = c->stack;

    local::push(c, ceiling(firstSize, TargetBytesPerWord), first);

    Stack* argumentStack = c->stack;
    c->stack = oldStack;

    appendCall
      (c, value
       (c, ValueGeneral, constantSite
        (c, c->client->getThunk(type, firstSize, resultSize))),
       0, 0, result, resultSize, argumentStack,
       ceiling(firstSize, TargetBytesPerWord), 0);
  } else {
    append(c, new (c->zone->allocate(sizeof(TranslateEvent)))
           TranslateEvent
           (c, type, firstSize, first, resultSize, result,
            SiteMask(firstTypeMask, firstRegisterMask, AnyFrameIndex),
            SiteMask(firstTypeMask, firstRegisterMask >> 32, AnyFrameIndex)));
  }
}

class BarrierEvent: public Event {
 public:
  BarrierEvent(Context* c, Operation op):
    Event(c), op(op)
  { }

  virtual const char* name() {
    return "BarrierEvent";
  }

  virtual void compile(Context* c) {
    c->assembler->apply(op);
  }

  Operation op;
};

void
appendBarrier(Context* c, Operation op)
{
  append(c, new (c->zone->allocate(sizeof(BarrierEvent))) BarrierEvent(c, op));
}

class MemoryEvent: public Event {
 public:
  MemoryEvent(Context* c, Value* base, int displacement, Value* index,
              unsigned scale, Value* result):
    Event(c), base(base), displacement(displacement), index(index),
    scale(scale), result(result)
  {
    addRead(c, this, base, generalRegisterMask(c));
    if (index) {
      addRead(c, this, index, generalRegisterOrConstantMask(c));
    }
  }

  virtual const char* name() {
    return "MemoryEvent";
  }

  virtual void compile(Context* c) {
    int indexRegister;
    int displacement = this->displacement;
    unsigned scale = this->scale;
    if (index) {
      ConstantSite* constant = findConstantSite(c, index);

      if (constant) {
        indexRegister = NoRegister;
        displacement += (constant->value->value() * scale);
        scale = 1;
      } else {
        assert(c, index->source->type(c) == RegisterOperand);
        indexRegister = static_cast<RegisterSite*>(index->source)->number;
      }
    } else {
      indexRegister = NoRegister;
    }
    assert(c, base->source->type(c) == RegisterOperand);
    int baseRegister = static_cast<RegisterSite*>(base->source)->number;

    popRead(c, this, base);
    if (index) {
      if (TargetBytesPerWord == 8 and indexRegister != NoRegister) {
        apply(c, Move, 4, index->source, index->source,
              8, index->source, index->source);
      }

      popRead(c, this, index);
    }

    Site* site = memorySite
      (c, baseRegister, displacement, indexRegister, scale);

    Site* low;
    if (result->nextWord != result) {
      Site* high = site->copyHigh(c);
      low = site->copyLow(c);

      result->nextWord->target = high;
      addSite(c, result->nextWord, high);
    } else {
      low = site;
    }

    result->target = low;
    addSite(c, result, low);
  }

  Value* base;
  int displacement;
  Value* index;
  unsigned scale;
  Value* result;
};

void
appendMemory(Context* c, Value* base, int displacement, Value* index,
             unsigned scale, Value* result)
{
  append(c, new (c->zone->allocate(sizeof(MemoryEvent)))
         MemoryEvent(c, base, displacement, index, scale, result));
}

double
asFloat(unsigned size, int64_t v)
{
  if (size == 4) {
    return bitsToFloat(v);
  } else {
    return bitsToDouble(v);
  }
}

bool
unordered(double a, double b)
{
  return not (a >= b or a < b);
}

bool
shouldJump(Context* c, TernaryOperation type, unsigned size, int64_t b,
           int64_t a)
{
  switch (type) {
  case JumpIfEqual:
    return a == b;

  case JumpIfNotEqual:
    return a != b;

  case JumpIfLess:
    return a < b;

  case JumpIfGreater:
    return a > b;

  case JumpIfLessOrEqual:
    return a <= b;

  case JumpIfGreaterOrEqual:
    return a >= b;

  case JumpIfFloatEqual:
    return asFloat(size, a) == asFloat(size, b);

  case JumpIfFloatNotEqual:
    return asFloat(size, a) != asFloat(size, b);

  case JumpIfFloatLess:
    return asFloat(size, a) < asFloat(size, b);

  case JumpIfFloatGreater:
    return asFloat(size, a) > asFloat(size, b);

  case JumpIfFloatLessOrEqual:
    return asFloat(size, a) <= asFloat(size, b);

  case JumpIfFloatGreaterOrEqual:
    return asFloat(size, a) >= asFloat(size, b);

  case JumpIfFloatLessOrUnordered:
    return asFloat(size, a) < asFloat(size, b)
      or unordered(asFloat(size, a), asFloat(size, b));

  case JumpIfFloatGreaterOrUnordered:
    return asFloat(size, a) > asFloat(size, b)
      or unordered(asFloat(size, a), asFloat(size, b));

  case JumpIfFloatLessOrEqualOrUnordered:
    return asFloat(size, a) <= asFloat(size, b)
      or unordered(asFloat(size, a), asFloat(size, b));

  case JumpIfFloatGreaterOrEqualOrUnordered:
    return asFloat(size, a) >= asFloat(size, b)
      or unordered(asFloat(size, a), asFloat(size, b));

  default:
    abort(c);
  }
}

TernaryOperation
thunkBranch(Context* c, TernaryOperation type)
{
  switch (type) {
  case JumpIfFloatEqual:
    return JumpIfEqual;

  case JumpIfFloatNotEqual:
    return JumpIfNotEqual;

  case JumpIfFloatLess:
  case JumpIfFloatLessOrUnordered:
    return JumpIfLess;

  case JumpIfFloatGreater:
  case JumpIfFloatGreaterOrUnordered:
    return JumpIfGreater;

  case JumpIfFloatLessOrEqual:
  case JumpIfFloatLessOrEqualOrUnordered:
    return JumpIfLessOrEqual;

  case JumpIfFloatGreaterOrEqual:
  case JumpIfFloatGreaterOrEqualOrUnordered:
    return JumpIfGreaterOrEqual;

  default:
    abort(c);
  }
}

class BranchEvent: public Event {
 public:
  BranchEvent(Context* c, TernaryOperation type, unsigned size,
              Value* first, Value* second, Value* address,
              const SiteMask& firstLowMask,
              const SiteMask& firstHighMask,
              const SiteMask& secondLowMask,
              const SiteMask& secondHighMask):
    Event(c), type(type), size(size), first(first), second(second),
    address(address)
  {
    addReads(c, this, first, size, firstLowMask, firstHighMask);
    addReads(c, this, second, size, secondLowMask, secondHighMask);

    uint8_t typeMask;
    uint64_t registerMask;
    c->arch->planDestination(type, size, 0, 0, size, 0, 0, TargetBytesPerWord,
                             &typeMask, &registerMask);

    addRead(c, this, address, SiteMask(typeMask, registerMask, AnyFrameIndex));
  }

  virtual const char* name() {
    return "BranchEvent";
  }

  virtual void compile(Context* c) {
    ConstantSite* firstConstant = findConstantSite(c, first);
    ConstantSite* secondConstant = findConstantSite(c, second);

    if (not unreachable(this)) {
      if (firstConstant
          and secondConstant
          and firstConstant->value->resolved()
          and secondConstant->value->resolved())
      {
        int64_t firstValue = firstConstant->value->value();
        int64_t secondValue = secondConstant->value->value();

        if (size > TargetBytesPerWord) {
          firstValue |= findConstantSite
            (c, first->nextWord)->value->value() << 32;
          secondValue |= findConstantSite
            (c, second->nextWord)->value->value() << 32;
        }

        if (shouldJump(c, type, size, firstValue, secondValue)) {
          apply(c, Jump, TargetBytesPerWord, address->source, address->source);
        }      
      } else {
        freezeSource(c, size, first);
        freezeSource(c, size, second);
        freezeSource(c, TargetBytesPerWord, address);

        apply(c, type, size, first->source, first->nextWord->source,
              size, second->source, second->nextWord->source,
              TargetBytesPerWord, address->source, address->source);

        thawSource(c, TargetBytesPerWord, address);
        thawSource(c, size, second);
        thawSource(c, size, first);
      }
    }

    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }
  }

  virtual bool isBranch() { return true; }

  TernaryOperation type;
  unsigned size;
  Value* first;
  Value* second;
  Value* address;
};

void
appendBranch(Context* c, TernaryOperation type, unsigned size, Value* first,
             Value* second, Value* address)
{
  bool thunk;
  uint8_t firstTypeMask;
  uint64_t firstRegisterMask;
  uint8_t secondTypeMask;
  uint64_t secondRegisterMask;

  c->arch->planSource(type, size, &firstTypeMask, &firstRegisterMask,
                      size, &secondTypeMask, &secondRegisterMask,
                      TargetBytesPerWord, &thunk);

  if (thunk) {
    Stack* oldStack = c->stack;

    bool threadParameter;
    intptr_t handler = c->client->getThunk
      (type, size, size, &threadParameter);

    assert(c, not threadParameter);

    local::push(c, ceiling(size, TargetBytesPerWord), second);
    local::push(c, ceiling(size, TargetBytesPerWord), first);

    Stack* argumentStack = c->stack;
    c->stack = oldStack;

    Value* result = value(c, ValueGeneral);
    appendCall
      (c, value
       (c, ValueGeneral, constantSite(c, handler)), 0, 0, result, 4,
       argumentStack, ceiling(size, TargetBytesPerWord) * 2, 0);

    appendBranch(c, thunkBranch(c, type), 4, value
                 (c, ValueGeneral, constantSite(c, static_cast<int64_t>(0))),
                 result, address);
  } else {
    append
      (c, new (c->zone->allocate(sizeof(BranchEvent)))
       BranchEvent
       (c, type, size, first, second, address,
        SiteMask(firstTypeMask, firstRegisterMask, AnyFrameIndex),
        SiteMask(firstTypeMask, firstRegisterMask >> 32, AnyFrameIndex),
        SiteMask(secondTypeMask, secondRegisterMask, AnyFrameIndex),
        SiteMask(secondTypeMask, secondRegisterMask >> 32, AnyFrameIndex)));
  }
}

class JumpEvent: public Event {
 public:
  JumpEvent(Context* c, UnaryOperation type, Value* address, bool exit,
            bool cleanLocals):
    Event(c), type(type), address(address), exit(exit),
    cleanLocals(cleanLocals)
  {
    bool thunk;
    uint8_t typeMask;
    uint64_t registerMask;
    c->arch->plan(type, TargetBytesPerWord, &typeMask, &registerMask, &thunk);

    assert(c, not thunk);

    addRead(c, this, address, SiteMask(typeMask, registerMask, AnyFrameIndex));
  }

  virtual const char* name() {
    return "JumpEvent";
  }

  virtual void compile(Context* c) {
    if (not unreachable(this)) {
      apply(c, type, TargetBytesPerWord, address->source, address->source);
    }

    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }

    if (cleanLocals) {
      for (FrameIterator it(c, 0, c->locals); it.hasMore();) {
        FrameIterator::Element e = it.next(c);
        clean(c, e.value, 0);
      }
    }
  }

  virtual bool isBranch() { return true; }

  virtual bool allExits() {
    return exit or unreachable(this);
  }

  UnaryOperation type;
  Value* address;
  bool exit;
  bool cleanLocals;
};

void
appendJump(Context* c, UnaryOperation type, Value* address, bool exit = false,
           bool cleanLocals = false)
{
  append(c, new (c->zone->allocate(sizeof(JumpEvent)))
         JumpEvent(c, type, address, exit, cleanLocals));
}

class BoundsCheckEvent: public Event {
 public:
  BoundsCheckEvent(Context* c, Value* object, unsigned lengthOffset,
                   Value* index, intptr_t handler):
    Event(c), object(object), lengthOffset(lengthOffset), index(index),
    handler(handler)
  {
    addRead(c, this, object, generalRegisterMask(c));
    addRead(c, this, index, generalRegisterOrConstantMask(c));
  }

  virtual const char* name() {
    return "BoundsCheckEvent";
  }

  virtual void compile(Context* c) {
    Assembler* a = c->assembler;

    ConstantSite* constant = findConstantSite(c, index);
    CodePromise* outOfBoundsPromise = 0;

    if (constant) {
      if (constant->value->value() < 0) {
        Assembler::Constant handlerConstant(resolved(c, handler));
        a->apply(Call, TargetBytesPerWord, ConstantOperand, &handlerConstant);
      }
    } else {
      outOfBoundsPromise = codePromise(c, static_cast<Promise*>(0));

      ConstantSite zero(resolved(c, 0));
      ConstantSite oob(outOfBoundsPromise);
      apply(c, JumpIfLess, 4, &zero, &zero, 4, index->source, index->source,
            TargetBytesPerWord, &oob, &oob);
    }

    if (constant == 0 or constant->value->value() >= 0) {
      assert(c, object->source->type(c) == RegisterOperand);
      MemorySite length(static_cast<RegisterSite*>(object->source)->number,
                        lengthOffset, NoRegister, 1);
      length.acquired = true;

      CodePromise* nextPromise = codePromise(c, static_cast<Promise*>(0));

      freezeSource(c, TargetBytesPerWord, index);

      ConstantSite next(nextPromise);
      apply(c, JumpIfGreater, 4, index->source, index->source, 4, &length,
            &length, TargetBytesPerWord, &next, &next);

      thawSource(c, TargetBytesPerWord, index);

      if (constant == 0) {
        outOfBoundsPromise->offset = a->offset();
      }

      Assembler::Constant handlerConstant(resolved(c, handler));
      a->apply(Call, TargetBytesPerWord, ConstantOperand, &handlerConstant);

      nextPromise->offset = a->offset();
    }

    popRead(c, this, object);
    popRead(c, this, index);
  }

  Value* object;
  unsigned lengthOffset;
  Value* index;
  intptr_t handler;
};

void
appendBoundsCheck(Context* c, Value* object, unsigned lengthOffset,
                  Value* index, intptr_t handler)
{
  append(c, new (c->zone->allocate(sizeof(BoundsCheckEvent)))
         BoundsCheckEvent(c, object, lengthOffset, index, handler));
}

class FrameSiteEvent: public Event {
 public:
  FrameSiteEvent(Context* c, Value* value, int index):
    Event(c), value(value), index(index)
  { }

  virtual const char* name() {
    return "FrameSiteEvent";
  }

  virtual void compile(Context* c) {
    if (live(c, value)) {
      addSite(c, value, frameSite(c, index));
    }
  }

  Value* value;
  int index;
};

void
appendFrameSite(Context* c, Value* value, int index)
{
  append(c, new (c->zone->allocate(sizeof(FrameSiteEvent)))
         FrameSiteEvent(c, value, index));
}

unsigned
frameFootprint(Context* c, Stack* s)
{
  return c->localFootprint + (s ? (s->index + 1) : 0);
}

void
visit(Context* c, Link* link)
{
  //   fprintf(stderr, "visit link from %d to %d fork %p junction %p\n",
  //           link->predecessor->logicalInstruction->index,
  //           link->successor->logicalInstruction->index,
  //           link->forkState,
  //           link->junctionState);

  ForkState* forkState = link->forkState;
  if (forkState) {
    for (unsigned i = 0; i < forkState->readCount; ++i) {
      ForkElement* p = forkState->elements + i;
      Value* v = p->value;
      v->reads = p->read->nextTarget();
      //       fprintf(stderr, "next read %p for %p from %p\n", v->reads, v, p->read);
      if (not live(c, v)) {
        clearSites(c, v);
      }
    }
  }

  JunctionState* junctionState = link->junctionState;
  if (junctionState) {
    for (unsigned i = 0; i < junctionState->frameFootprint; ++i) {
      StubReadPair* p = junctionState->reads + i;
      
      if (p->value and p->value->reads) {
        assert(c, p->value->reads == p->read);
        popRead(c, 0, p->value);
      }
    }
  }
}

class BuddyEvent: public Event {
 public:
  BuddyEvent(Context* c, Value* original, Value* buddy):
    Event(c), original(original), buddy(buddy)
  {
    addRead(c, this, original, SiteMask(~0, ~0, AnyFrameIndex), buddy);
  }

  virtual const char* name() {
    return "BuddyEvent";
  }

  virtual void compile(Context* c) {
    if (DebugBuddies) {
      fprintf(stderr, "original %p buddy %p\n", original, buddy);
    }

    assert(c, hasSite(c, original));

    assert(c, original);
    assert(c, buddy);

    addBuddy(original, buddy);

    popRead(c, this, original);
  }

  Value* original;
  Value* buddy;
};

void
appendBuddy(Context* c, Value* original, Value* buddy)
{
  append(c, new (c->zone->allocate(sizeof(BuddyEvent)))
         BuddyEvent(c, original, buddy));
}

class SaveLocalsEvent: public Event {
 public:
  SaveLocalsEvent(Context* c):
    Event(c)
  {
    saveLocals(c, this);
  }

  virtual const char* name() {
    return "SaveLocalsEvent";
  }

  virtual void compile(Context* c) {
    for (Read* r = reads; r; r = r->eventNext) {
      popRead(c, this, r->value);
    }
  }
};

void
appendSaveLocals(Context* c)
{
  append(c, new (c->zone->allocate(sizeof(SaveLocalsEvent)))
         SaveLocalsEvent(c));
}

class DummyEvent: public Event {
 public:
  DummyEvent(Context* c):
    Event(c)
  { }

  virtual const char* name() {
    return "DummyEvent";
  }

  virtual void compile(Context*) { }
};

void
appendDummy(Context* c)
{
  Stack* stack = c->stack;
  Local* locals = c->locals;
  LogicalInstruction* i = c->logicalCode[c->logicalIp];

  c->stack = i->stack;
  c->locals = i->locals;

  append(c, new (c->zone->allocate(sizeof(DummyEvent))) DummyEvent(c));

  c->stack = stack;
  c->locals = locals;  
}

void
append(Context* c, Event* e)
{
  LogicalInstruction* i = c->logicalCode[c->logicalIp];
  if (c->stack != i->stack or c->locals != i->locals) {
    appendDummy(c);
  }

  if (DebugAppend) {
    fprintf(stderr, " -- append %s at %d with %d stack before\n",
            e->name(), e->logicalInstruction->index, c->stack ?
            c->stack->index + 1 : 0);
  }

  if (c->lastEvent) {
    c->lastEvent->next = e;
  } else {
    c->firstEvent = e;
  }
  c->lastEvent = e;

  Event* p = c->predecessor;
  if (p) {
    if (DebugAppend) {
      fprintf(stderr, "%d precedes %d\n", p->logicalInstruction->index,
              e->logicalInstruction->index);
    }

    Link* link = local::link
      (c, p, e->predecessors, e, p->successors, c->forkState);
    e->predecessors = link;
    p->successors = link;
  }
  c->forkState = 0;

  c->predecessor = e;

  if (e->logicalInstruction->firstEvent == 0) {
    e->logicalInstruction->firstEvent = e;
  }
  e->logicalInstruction->lastEvent = e;
}

Site*
readSource(Context* c, Read* r)
{
  Value* v = r->value;

  if (DebugReads) {
    char buffer[1024]; sitesToString(c, v, buffer, 1024);
    fprintf(stderr, "read source for %p from %s\n", v, buffer);
  }

  if (not hasSite(c, v)) {
    if (DebugReads) {
      fprintf(stderr, "no sites found for %p\n", v);
    }
    return 0;
  }

  Value* high = r->high(c);
  if (high) {
    return pickMatchOrMove(c, r, high->source, 0, true);
  } else {
    return pickSiteOrMove(c, r, true, true);
  }
}

void
propagateJunctionSites(Context* c, Event* e, Site** sites)
{
  for (Link* pl = e->predecessors; pl; pl = pl->nextPredecessor) {
    Event* p = pl->predecessor;
    if (p->junctionSites == 0) {
      p->junctionSites = sites;
      for (Link* sl = p->successors; sl; sl = sl->nextSuccessor) {
        Event* s = sl->successor;
        propagateJunctionSites(c, s, sites);
      }
    }
  }
}

void
propagateJunctionSites(Context* c, Event* e)
{
  for (Link* sl = e->successors; sl; sl = sl->nextSuccessor) {
    Event* s = sl->successor;
    if (s->predecessors->nextPredecessor) {
      unsigned size = sizeof(Site*) * frameFootprint(c, e->stackAfter);
      Site** junctionSites = static_cast<Site**>
        (c->zone->allocate(size));
      memset(junctionSites, 0, size);

      propagateJunctionSites(c, s, junctionSites);
      break;
    }
  }
}

class SiteRecord {
 public:
  SiteRecord(Site* site, Value* value):
    site(site), value(value)
  { }

  SiteRecord() { }

  Site* site;
  Value* value;
};

class SiteRecordList {
 public:
  SiteRecordList(SiteRecord* records, unsigned capacity):
    records(records), index(0), capacity(capacity)
  { }

  SiteRecord* records;
  unsigned index;
  unsigned capacity;
};

void
freeze(Context* c, SiteRecordList* frozen, Site* s, Value* v)
{
  assert(c, frozen->index < frozen->capacity);

  s->freeze(c, v);
  new (frozen->records + (frozen->index ++)) SiteRecord(s, v);
}

void
thaw(Context* c, SiteRecordList* frozen)
{
  while (frozen->index) {
    SiteRecord* sr = frozen->records + (-- frozen->index);
    sr->site->thaw(c, sr->value);
  }
}

bool
resolveOriginalSites(Context* c, Event* e, SiteRecordList* frozen,
                     Site** sites)
{
  bool complete = true;
  for (FrameIterator it(c, e->stackAfter, e->localsAfter, true);
       it.hasMore();)
  {
    FrameIterator::Element el = it.next(c);
    Value* v = el.value;
    Read* r = v ? live(c, v) : 0;
    Site* s = sites[el.localIndex];

    if (r) {
      if (s) {
        if (DebugControl) {
          char buffer[256];
          s->toString(c, buffer, 256);
          fprintf(stderr, "resolve original %s for %p local %d frame %d\n",
                  buffer, v, el.localIndex, frameIndex(c, &el));
        }

        Site* target = pickSiteOrMove
          (c, v, s->mask(c), true, true, ResolveRegisterReserveCount);

        freeze(c, frozen, target, v);
      } else {
        complete = false;
      }
    } else if (s) {
      if (DebugControl) {
        char buffer[256];
        s->toString(c, buffer, 256);
        fprintf(stderr, "freeze original %s for %p local %d frame %d\n",
                buffer, v, el.localIndex, frameIndex(c, &el));
      }
      
      Value dummy(0, 0, ValueGeneral);
      addSite(c, &dummy, s);
      removeSite(c, &dummy, s);
      freeze(c, frozen, s, 0);
    }
  }

  return complete;
}

bool
resolveSourceSites(Context* c, Event* e, SiteRecordList* frozen, Site** sites)
{
  bool complete = true;
  for (FrameIterator it(c, e->stackAfter, e->localsAfter); it.hasMore();) {
    FrameIterator::Element el = it.next(c);
    Value* v = el.value;
    Read* r = live(c, v);

    if (r and sites[el.localIndex] == 0) {
      SiteMask mask((1 << RegisterOperand) | (1 << MemoryOperand),
                    c->arch->generalRegisterMask(), AnyFrameIndex);

      Site* s = pickSourceSite
        (c, r, 0, 0, &mask, true, false, true, acceptForResolve);

      if (s) {
        if (DebugControl) {
          char buffer[256]; s->toString(c, buffer, 256);
          fprintf(stderr, "resolve source %s from %p local %d frame %d\n",
                  buffer, v, el.localIndex, frameIndex(c, &el));
        }

        freeze(c, frozen, s, v);

        sites[el.localIndex] = s->copy(c);
      } else {
        complete = false;
      }
    }
  }

  return complete;
}

void
resolveTargetSites(Context* c, Event* e, SiteRecordList* frozen, Site** sites)
{
  for (FrameIterator it(c, e->stackAfter, e->localsAfter); it.hasMore();) {
    FrameIterator::Element el = it.next(c);
    Value* v = el.value;
    Read* r = live(c, v);

    if (r and sites[el.localIndex] == 0) {
      SiteMask mask((1 << RegisterOperand) | (1 << MemoryOperand),
                    c->arch->generalRegisterMask(), AnyFrameIndex);

      Site* s = pickSourceSite
        (c, r, 0, 0, &mask, false, true, true, acceptForResolve);

      if (s == 0) {
        s = maybeMove(c, v, mask, false, true, ResolveRegisterReserveCount);
      }

      freeze(c, frozen, s, v);

      sites[el.localIndex] = s->copy(c);

      if (DebugControl) {
        char buffer[256]; sites[el.localIndex]->toString(c, buffer, 256);
        fprintf(stderr, "resolve target %s for %p local %d frame %d\n",
                buffer, el.value, el.localIndex, frameIndex(c, &el));
      }
    }
  }
}

void
resolveJunctionSites(Context* c, Event* e, SiteRecordList* frozen)
{
  bool complete;
  if (e->junctionSites) {
    complete = resolveOriginalSites(c, e, frozen, e->junctionSites);
  } else {
    propagateJunctionSites(c, e);
    complete = false;
  }

  if (e->junctionSites) {
    if (not complete) {
      complete = resolveSourceSites(c, e, frozen, e->junctionSites);
      if (not complete) {
        resolveTargetSites(c, e, frozen, e->junctionSites);
      }
    }

    if (DebugControl) {
      fprintf(stderr, "resolved junction sites %p at %d\n",
              e->junctionSites, e->logicalInstruction->index);
    }
  }
}

void
resolveBranchSites(Context* c, Event* e, SiteRecordList* frozen)
{
  if (e->successors->nextSuccessor and e->junctionSites == 0) {
    unsigned footprint = frameFootprint(c, e->stackAfter);
    RUNTIME_ARRAY(Site*, branchSites, footprint);
    memset(RUNTIME_ARRAY_BODY(branchSites), 0, sizeof(Site*) * footprint);

    if (not resolveSourceSites(c, e, frozen, RUNTIME_ARRAY_BODY(branchSites)))
    {
      resolveTargetSites(c, e, frozen, RUNTIME_ARRAY_BODY(branchSites));
    }
  }
}

void
captureBranchSnapshots(Context* c, Event* e)
{
  if (e->successors->nextSuccessor) {
    for (FrameIterator it(c, e->stackAfter, e->localsAfter); it.hasMore();) {
      FrameIterator::Element el = it.next(c);
      e->snapshots = makeSnapshots(c, el.value, e->snapshots);
    }

    for (Cell* sv = e->successors->forkState->saved; sv; sv = sv->next) {
      e->snapshots = makeSnapshots
        (c, static_cast<Value*>(sv->value), e->snapshots);
    }

    if (DebugControl) {
      fprintf(stderr, "captured snapshots %p at %d\n",
              e->snapshots, e->logicalInstruction->index);
    }
  }
}

void
populateSiteTables(Context* c, Event* e, SiteRecordList* frozen)
{
  resolveJunctionSites(c, e, frozen);

  resolveBranchSites(c, e, frozen);
}

void
setSites(Context* c, Value* v, Site* s)
{
  assert(c, live(c, v));

  for (; s; s = s->next) {
    addSite(c, v, s->copy(c));
  }

  if (DebugControl) {
    char buffer[256]; sitesToString(c, v->sites, buffer, 256);
    fprintf(stderr, "set sites %s for %p\n", buffer, v);
  }
}

void
resetFrame(Context* c, Event* e)
{
  for (FrameIterator it(c, e->stackBefore, e->localsBefore); it.hasMore();) {
    FrameIterator::Element el = it.next(c);
    clearSites(c, el.value);
  }

  while (c->acquiredResources) {
    clearSites(c, c->acquiredResources->value);
  }
}

void
setSites(Context* c, Event* e, Site** sites)
{
  resetFrame(c, e);

  for (FrameIterator it(c, e->stackBefore, e->localsBefore); it.hasMore();) {
    FrameIterator::Element el = it.next(c);
    if (sites[el.localIndex]) {
      if (live(c, el.value)) {
        setSites(c, el.value, sites[el.localIndex]);
      } else if (DebugControl) {
        char buffer[256]; sitesToString(c, sites[el.localIndex], buffer, 256);
        fprintf(stderr, "skip sites %s for %p local %d frame %d\n",
                buffer, el.value, el.localIndex, frameIndex(c, &el));
      }
    } else if (DebugControl) {
      fprintf(stderr, "no sites for %p local %d frame %d\n",
              el.value, el.localIndex, frameIndex(c, &el));
    }
  }
}

void
removeBuddies(Context* c)
{
  for (FrameIterator it(c, c->stack, c->locals); it.hasMore();) {
    FrameIterator::Element el = it.next(c);
    removeBuddy(c, el.value);
  }
}

void
restore(Context* c, Event* e, Snapshot* snapshots)
{
  for (Snapshot* s = snapshots; s; s = s->next) {
    Value* v = s->value;
    Value* next = v->buddy;
    if (v != next) {
      v->buddy = v;
      Value* p = next;
      while (p->buddy != v) p = p->buddy;
      p->buddy = next;
    }
  }

  for (Snapshot* s = snapshots; s; s = s->next) {
    assert(c, s->buddy);

    s->value->buddy = s->buddy;
  }

  resetFrame(c, e);

  for (Snapshot* s = snapshots; s; s = s->next) {
    if (live(c, s->value)) {
      if (live(c, s->value) and s->sites and s->value->sites == 0) {
        setSites(c, s->value, s->sites);
      }
    }

    // char buffer[256]; sitesToString(c, s->sites, buffer, 256);
    // fprintf(stderr, "restore %p buddy %p sites %s live %p\n",
    //         s->value, s->value->buddy, buffer, live(c, s->value));
  }
}

void
populateSources(Context* c, Event* e)
{
  RUNTIME_ARRAY(SiteRecord, frozenRecords, e->readCount);
  SiteRecordList frozen(RUNTIME_ARRAY_BODY(frozenRecords), e->readCount);

  for (Read* r = e->reads; r; r = r->eventNext) {
    r->value->source = readSource(c, r);
    if (r->value->source) {
      if (DebugReads) {
        char buffer[256]; r->value->source->toString(c, buffer, 256);
        fprintf(stderr, "freeze source %s for %p\n",
                buffer, r->value);
      }

      freeze(c, &frozen, r->value->source, r->value);
    }
  }

  thaw(c, &frozen);
}

void
setStubRead(Context* c, StubReadPair* p, Value* v)
{
  if (v) {
    StubRead* r = stubRead(c);
    if (DebugReads) {
      fprintf(stderr, "add stub read %p to %p\n", r, v);
    }
    addRead(c, 0, v, r);

    p->value = v;
    p->read = r;
  }
}

void
populateJunctionReads(Context* c, Link* link)
{
  JunctionState* state = new
    (c->zone->allocate
     (sizeof(JunctionState)
      + (sizeof(StubReadPair) * frameFootprint(c, c->stack))))
    JunctionState(frameFootprint(c, c->stack));

  memset(state->reads, 0, sizeof(StubReadPair) * frameFootprint(c, c->stack));

  link->junctionState = state;

  for (FrameIterator it(c, c->stack, c->locals); it.hasMore();) {
    FrameIterator::Element e = it.next(c);
    setStubRead(c, state->reads + e.localIndex, e.value);
  }
}

void
updateJunctionReads(Context* c, JunctionState* state)
{
  for (FrameIterator it(c, c->stack, c->locals); it.hasMore();) {
    FrameIterator::Element e = it.next(c);
    StubReadPair* p = state->reads + e.localIndex;
    if (p->value and p->read->read == 0) {
      Read* r = live(c, e.value);
      if (r) {
        if (DebugReads) {
          fprintf(stderr, "stub read %p for %p valid: %p\n",
                  p->read, p->value, r);
        }
        p->read->read = r;
      }
    }
  }

  for (unsigned i = 0; i < frameFootprint(c, c->stack); ++i) {
    StubReadPair* p = state->reads + i;
    if (p->value and p->read->read == 0) {
      if (DebugReads) {
        fprintf(stderr, "stub read %p for %p invalid\n", p->read, p->value);
      }
      p->read->valid_ = false;
    }
  }
}

LogicalInstruction*
next(Context* c, LogicalInstruction* i)
{
  for (unsigned n = i->index + 1; n < c->logicalCodeLength; ++n) {
    i = c->logicalCode[n];
    if (i) return i;
  }
  return 0;
}

class Block {
 public:
  Block(Event* head):
    head(head), nextBlock(0), nextInstruction(0), assemblerBlock(0), start(0)
  { }

  Event* head;
  Block* nextBlock;
  LogicalInstruction* nextInstruction;
  Assembler::Block* assemblerBlock;
  unsigned start;
};

Block*
block(Context* c, Event* head)
{
  return new (c->zone->allocate(sizeof(Block))) Block(head);
}

void
compile(Context* c, uintptr_t stackOverflowHandler, unsigned stackLimitOffset)
{
  if (c->logicalCode[c->logicalIp]->lastEvent == 0) {
    appendDummy(c);
  }

  Assembler* a = c->assembler;

  Block* firstBlock = block(c, c->firstEvent);
  Block* block = firstBlock;

  if (stackOverflowHandler) {
    a->checkStackOverflow(stackOverflowHandler, stackLimitOffset);
  }

  a->allocateFrame(c->alignedFrameSize);

  for (Event* e = c->firstEvent; e; e = e->next) {
    if (DebugCompile) {
      fprintf(stderr,
              " -- compile %s at %d with %d preds %d succs %d stack\n",
              e->name(), e->logicalInstruction->index,
              countPredecessors(e->predecessors),
              countSuccessors(e->successors),
              e->stackBefore ? e->stackBefore->index + 1 : 0);
    }

    e->block = block;

    c->stack = e->stackBefore;
    c->locals = e->localsBefore;

    if (e->logicalInstruction->machineOffset == 0) {
      e->logicalInstruction->machineOffset = a->offset();
    }

    if (e->predecessors) {
      visit(c, lastPredecessor(e->predecessors));

      Event* first = e->predecessors->predecessor;
      if (e->predecessors->nextPredecessor) {
        for (Link* pl = e->predecessors;
             pl->nextPredecessor;
             pl = pl->nextPredecessor)
        {
          updateJunctionReads(c, pl->junctionState);
        }

        if (DebugControl) {
          fprintf(stderr, "set sites to junction sites %p at %d\n",
                  first->junctionSites, first->logicalInstruction->index);
        }

        setSites(c, e, first->junctionSites);
        removeBuddies(c);
      } else if (first->successors->nextSuccessor) {
        if (DebugControl) {
          fprintf(stderr, "restore snapshots %p at %d\n",
                  first->snapshots, first->logicalInstruction->index);
        }

        restore(c, e, first->snapshots);
      }
    }

    unsigned footprint = frameFootprint(c, e->stackAfter);
    RUNTIME_ARRAY(SiteRecord, frozenRecords, footprint);
    SiteRecordList frozen(RUNTIME_ARRAY_BODY(frozenRecords), footprint);

    bool branch = e->isBranch();
    if (branch and e->successors) {
      populateSiteTables(c, e, &frozen);
    }

    populateSources(c, e);

    if (branch and e->successors) {
      captureBranchSnapshots(c, e);
    }

    thaw(c, &frozen);

    e->compile(c);

    if ((not branch) and e->successors) {
      populateSiteTables(c, e, &frozen);
      captureBranchSnapshots(c, e);
      thaw(c, &frozen);
    }

    if (e->visitLinks) {
      for (Cell* cell = reverseDestroy(e->visitLinks); cell; cell = cell->next)
      {
        visit(c, static_cast<Link*>(cell->value));
      }
      e->visitLinks = 0;
    }

    for (CodePromise* p = e->promises; p; p = p->next) {
      p->offset = a->offset();
    }
    
    a->endEvent();

    LogicalInstruction* nextInstruction = next(c, e->logicalInstruction);
    if (e->next == 0
        or (e->next->logicalInstruction != e->logicalInstruction
            and (e->next->logicalInstruction != nextInstruction
                 or e != e->logicalInstruction->lastEvent)))
    {
      Block* b = e->logicalInstruction->firstEvent->block;

      while (b->nextBlock) {
        b = b->nextBlock;
      }

      if (b != block) {
        b->nextBlock = block;
      }

      block->nextInstruction = nextInstruction;
      block->assemblerBlock = a->endBlock(e->next != 0);

      if (e->next) {
        block = local::block(c, e->next);
      }
    }
  }

  c->firstBlock = firstBlock;
}

unsigned
count(Stack* s)
{
  unsigned c = 0;
  while (s) {
    ++ c;
    s = s->next;
  }
  return c;
}

void
restore(Context* c, ForkState* state)
{
  for (unsigned i = 0; i < state->readCount; ++i) {
    ForkElement* p = state->elements + i;
    p->value->lastRead = p->read;
    p->read->allocateTarget(c);
  }
}

void
addForkElement(Context* c, Value* v, ForkState* state, unsigned index)
{
  MultiRead* r = multiRead(c);
  if (DebugReads) {
    fprintf(stderr, "add multi read %p to %p\n", r, v);
  }
  addRead(c, 0, v, r);

  ForkElement* p = state->elements + index;
  p->value = v;
  p->read = r;
}

ForkState*
saveState(Context* c)
{
  if (c->logicalCode[c->logicalIp]->lastEvent == 0) {
    appendDummy(c);
  }

  unsigned elementCount = frameFootprint(c, c->stack) + count(c->saved);

  ForkState* state = new
    (c->zone->allocate
     (sizeof(ForkState) + (sizeof(ForkElement) * elementCount)))
    ForkState(c->stack, c->locals, c->saved, c->predecessor, c->logicalIp);

  if (c->predecessor) {
    c->forkState = state;

    unsigned count = 0;

    for (FrameIterator it(c, c->stack, c->locals); it.hasMore();) {
      FrameIterator::Element e = it.next(c);
      addForkElement(c, e.value, state, count++);
    }

    for (Cell* sv = c->saved; sv; sv = sv->next) {
      addForkElement(c, static_cast<Value*>(sv->value), state, count++);
    }

    state->readCount = count;
  }

  c->saved = 0;

  return state;
}

void
restoreState(Context* c, ForkState* s)
{
  if (c->logicalCode[c->logicalIp]->lastEvent == 0) {
    appendDummy(c);
  }

  c->stack = s->stack;
  c->locals = s->locals;
  c->predecessor = s->predecessor;
  c->logicalIp = s->logicalIp;

  if (c->predecessor) {
    c->forkState = s;
    restore(c, s);
  }
}

Value*
maybeBuddy(Context* c, Value* v)
{
  if (v->home >= 0) {
    Value* n = value(c, v->type);
    appendBuddy(c, v, n);
    return n;
  } else {
    return v;
  }
}

void
linkLocals(Context* c, Local* oldLocals, Local* newLocals)
{
  for (int i = 0; i < static_cast<int>(c->localFootprint); ++i) {
    Local* local = oldLocals + i;
    if (local->value) {
      int highOffset = c->arch->bigEndian() ? 1 : -1;

      if (i + highOffset >= 0
          and i + highOffset < static_cast<int>(c->localFootprint)
          and local->value->nextWord == local[highOffset].value)
      {
        Value* v = newLocals[i].value;
        Value* next = newLocals[i + highOffset].value;
        v->nextWord = next;
        next->nextWord = v;
        next->wordIndex = 1;
      }
    }
  }
}

class Client: public Assembler::Client {
 public:
  Client(Context* c): c(c) { }

  virtual int acquireTemporary(uint32_t mask) {
    unsigned cost;
    int r = pickRegisterTarget(c, 0, mask, &cost);
    expect(c, cost < Target::Impossible);
    save(r);
    increment(c, c->registerResources + r);
    return r;
  }

  virtual void releaseTemporary(int r) {
    decrement(c, c->registerResources + r);
  }

  virtual void save(int r) {
    RegisterResource* reg = c->registerResources + r;

    assert(c, reg->referenceCount == 0);
    assert(c, reg->freezeCount == 0);
    assert(c, not reg->reserved);

    if (reg->value) {
      steal(c, reg, 0);
    }
  }

  Context* c;
};

class MyCompiler: public Compiler {
 public:
  MyCompiler(System* s, Assembler* assembler, Zone* zone,
             Compiler::Client* compilerClient):
    c(s, assembler, zone, compilerClient), client(&c)
  {
    assembler->setClient(&client);
  }

  virtual State* saveState() {
    State* s = local::saveState(&c);
    restoreState(s);
    return s;
  }

  virtual void restoreState(State* state) {
    local::restoreState(&c, static_cast<ForkState*>(state));
  }

  virtual Subroutine* startSubroutine() {
    return c.subroutine = new (c.zone->allocate(sizeof(MySubroutine)))
      MySubroutine;
  }

  virtual void returnFromSubroutine(Subroutine* subroutine, Operand* address) {
    appendSaveLocals(&c);
    appendJump(&c, Jump, static_cast<Value*>(address), false, true);
    static_cast<MySubroutine*>(subroutine)->forkState = local::saveState(&c);
  }

  virtual void linkSubroutine(Subroutine* subroutine) {
    Local* oldLocals = c.locals;
    restoreState(static_cast<MySubroutine*>(subroutine)->forkState);
    linkLocals(&c, oldLocals, c.locals);
  }

  virtual void init(unsigned logicalCodeLength, unsigned parameterFootprint,
                    unsigned localFootprint, unsigned alignedFrameSize)
  {
    c.logicalCodeLength = logicalCodeLength;
    c.parameterFootprint = parameterFootprint;
    c.localFootprint = localFootprint;
    c.alignedFrameSize = alignedFrameSize;

    unsigned frameResourceCount = totalFrameSize(&c);

    c.frameResources = static_cast<FrameResource*>
      (c.zone->allocate(sizeof(FrameResource) * frameResourceCount));
    
    for (unsigned i = 0; i < frameResourceCount; ++i) {
      new (c.frameResources + i) FrameResource;
    }

    unsigned base = frameBase(&c);
    c.frameResources[base + c.arch->returnAddressOffset()].reserved = true;
    c.frameResources[base + c.arch->framePointerOffset()].reserved
      = UseFramePointer;

    // leave room for logical instruction -1
    unsigned codeSize = sizeof(LogicalInstruction*) * (logicalCodeLength + 1);
    c.logicalCode = static_cast<LogicalInstruction**>
      (c.zone->allocate(codeSize));
    memset(c.logicalCode, 0, codeSize);
    c.logicalCode++;

    c.locals = static_cast<Local*>
      (c.zone->allocate(sizeof(Local) * localFootprint));

    memset(c.locals, 0, sizeof(Local) * localFootprint);

    c.logicalCode[-1] = new 
      (c.zone->allocate(sizeof(LogicalInstruction)))
      LogicalInstruction(-1, c.stack, c.locals);
  }

  virtual void visitLogicalIp(unsigned logicalIp) {
    assert(&c, logicalIp < c.logicalCodeLength);

    if (c.logicalCode[c.logicalIp]->lastEvent == 0) {
      appendDummy(&c);
    }

    Event* e = c.logicalCode[logicalIp]->firstEvent;

    Event* p = c.predecessor;
    if (p) {
      if (DebugAppend) {
        fprintf(stderr, "visit %d pred %d\n", logicalIp,
                p->logicalInstruction->index);
      }

      p->stackAfter = c.stack;
      p->localsAfter = c.locals;

      Link* link = local::link
        (&c, p, e->predecessors, e, p->successors, c.forkState);
      e->predecessors = link;
      p->successors = link;
      c.lastEvent->visitLinks = cons(&c, link, c.lastEvent->visitLinks);

      if (DebugAppend) {
        fprintf(stderr, "populate junction reads for %d to %d\n",
                p->logicalInstruction->index, logicalIp);
      }

      populateJunctionReads(&c, link);
    }

    if (c.subroutine) {
      c.subroutine->forkState
        = c.logicalCode[logicalIp]->subroutine->forkState;
      c.subroutine = 0;
    }

    c.forkState = 0;
  }

  virtual void startLogicalIp(unsigned logicalIp) {
    assert(&c, logicalIp < c.logicalCodeLength);
    assert(&c, c.logicalCode[logicalIp] == 0);

    if (c.logicalCode[c.logicalIp]->lastEvent == 0) {
      appendDummy(&c);
    }

    Event* p = c.predecessor;
    if (p) {
      p->stackAfter = c.stack;
      p->localsAfter = c.locals;
    }

    c.logicalCode[logicalIp] = new 
      (c.zone->allocate(sizeof(LogicalInstruction)))
      LogicalInstruction(logicalIp, c.stack, c.locals);

    bool startSubroutine = c.subroutine != 0;
    if (startSubroutine) {
      c.logicalCode[logicalIp]->subroutine = c.subroutine;
      c.subroutine = 0;
    }

    c.logicalIp = logicalIp;

    if (startSubroutine) {
      // assume all local variables are initialized on entry to a
      // subroutine, since other calls to the subroutine may
      // initialize them:
      unsigned sizeInBytes = sizeof(Local) * c.localFootprint;
      Local* newLocals = static_cast<Local*>(c.zone->allocate(sizeInBytes));
      memcpy(newLocals, c.locals, sizeInBytes);
      c.locals = newLocals;

      for (unsigned li = 0; li < c.localFootprint; ++li) {
        Local* local = c.locals + li;
        if (local->value == 0) {
          initLocal(1, li, IntegerType); 
        }
      }
    }
  }

  virtual Promise* machineIp(unsigned logicalIp) {
    return new (c.zone->allocate(sizeof(IpPromise))) IpPromise(&c, logicalIp);
  }

  virtual Promise* poolAppend(intptr_t value) {
    return poolAppendPromise(resolved(&c, value));
  }

  virtual Promise* poolAppendPromise(Promise* value) {
    Promise* p = new (c.zone->allocate(sizeof(PoolPromise)))
      PoolPromise(&c, c.constantCount);

    ConstantPoolNode* constant
      = new (c.zone->allocate(sizeof(ConstantPoolNode)))
      ConstantPoolNode(value);

    if (c.firstConstant) {
      c.lastConstant->next = constant;
    } else {
      c.firstConstant = constant;
    }
    c.lastConstant = constant;
    ++ c.constantCount;

    return p;
  }

  virtual Operand* constant(int64_t value, OperandType type) {
    return promiseConstant(resolved(&c, value), type);
  }

  virtual Operand* promiseConstant(Promise* value, OperandType type) {
    return local::value
      (&c, valueType(&c, type), local::constantSite(&c, value));
  }

  virtual Operand* address(Promise* address) {
    return value(&c, ValueGeneral, local::addressSite(&c, address));
  }

  virtual Operand* memory(Operand* base,
                          OperandType type,
                          int displacement = 0,
                          Operand* index = 0,
                          unsigned scale = 1)
  {
    Value* result = value(&c, valueType(&c, type));

    appendMemory(&c, static_cast<Value*>(base), displacement,
                 static_cast<Value*>(index), scale, result);

    return result;
  }

  virtual Operand* register_(int number) {
    return local::register_(&c, number);
  }

  Promise* machineIp() {
    return codePromise(&c, c.logicalCode[c.logicalIp]->lastEvent);
  }

  virtual void push(unsigned footprint UNUSED) {
    assert(&c, footprint == 1);

    Value* v = value(&c, ValueGeneral);
    Stack* s = local::stack(&c, v, c.stack);

    v->home = frameIndex(&c, s->index + c.localFootprint);
    c.stack = s;
  }

  virtual void push(unsigned footprint, Operand* value) {
    local::push(&c, footprint, static_cast<Value*>(value));
  }

  virtual void save(unsigned footprint, Operand* value) {
    c.saved = cons(&c, static_cast<Value*>(value), c.saved);
    if (TargetBytesPerWord == 4 and footprint > 1) {
      assert(&c, footprint == 2);
      assert(&c, static_cast<Value*>(value)->nextWord);

      save(1, static_cast<Value*>(value)->nextWord);
    }
  }

  virtual Operand* pop(unsigned footprint) {
    return local::pop(&c, footprint);
  }

  virtual void pushed() {
    Value* v = value(&c, ValueGeneral);
    appendFrameSite
      (&c, v, frameIndex
       (&c, (c.stack ? c.stack->index : 0) + c.localFootprint));

    Stack* s = local::stack(&c, v, c.stack);
    v->home = frameIndex(&c, s->index + c.localFootprint);
    c.stack = s;
  }

  virtual void popped(unsigned footprint) {
    for (; footprint; -- footprint) {
      assert(&c, c.stack->value == 0 or c.stack->value->home >= 0);

      if (DebugFrame) {
        fprintf(stderr, "popped %p\n", c.stack->value);
      }
      
      c.stack = c.stack->next;
    }
  }

  virtual unsigned topOfStack() {
    return c.stack->index;
  }

  virtual Operand* peek(unsigned footprint, unsigned index) {
    Stack* s = c.stack;
    for (unsigned i = index; i > 0; --i) {
      s = s->next;
    }

    if (footprint > 1) {
      assert(&c, footprint == 2);

      bool bigEndian = c.arch->bigEndian();

#ifndef NDEBUG
      Stack* low;
      Stack* high;
      if (bigEndian) {
        high = s;
        low = s->next;
      } else {
        low = s;
        high = s->next;
      }

      assert(&c, (TargetBytesPerWord == 8
                  and low->value->nextWord == low->value and high->value == 0)
             or (TargetBytesPerWord == 4
                 and low->value->nextWord == high->value));
#endif // not NDEBUG

      if (bigEndian) {
        s = s->next;
      }
    }

    return s->value;
  }

  virtual Operand* call(Operand* address,
                        unsigned flags,
                        TraceHandler* traceHandler,
                        unsigned resultSize,
                        OperandType resultType,
                        unsigned argumentCount,
                        ...)
  {
    va_list a; va_start(a, argumentCount);

    bool bigEndian = c.arch->bigEndian();

    unsigned footprint = 0;
    unsigned size = TargetBytesPerWord;
    RUNTIME_ARRAY(Value*, arguments, argumentCount);
    int index = 0;
    for (unsigned i = 0; i < argumentCount; ++i) {
      Value* o = va_arg(a, Value*);
      if (o) {
        if (bigEndian and size > TargetBytesPerWord) {
          RUNTIME_ARRAY_BODY(arguments)[index++] = o->nextWord;
        }
        RUNTIME_ARRAY_BODY(arguments)[index] = o;
        if ((not bigEndian) and size > TargetBytesPerWord) {
          RUNTIME_ARRAY_BODY(arguments)[++index] = o->nextWord;
        }
        size = TargetBytesPerWord;
        ++ index;
      } else {
        size = 8;
      }
      ++ footprint;
    }

    va_end(a);

    Stack* argumentStack = c.stack;
    for (int i = index - 1; i >= 0; --i) {
      argumentStack = local::stack
        (&c, RUNTIME_ARRAY_BODY(arguments)[i], argumentStack);
    }

    Value* result = value(&c, valueType(&c, resultType));
    appendCall(&c, static_cast<Value*>(address), flags, traceHandler, result,
               resultSize, argumentStack, index, 0);

    return result;
  }

  virtual Operand* stackCall(Operand* address,
                             unsigned flags,
                             TraceHandler* traceHandler,
                             unsigned resultSize,
                             OperandType resultType,
                             unsigned argumentFootprint)
  {
    Value* result = value(&c, valueType(&c, resultType));
    appendCall(&c, static_cast<Value*>(address), flags, traceHandler, result,
               resultSize, c.stack, 0, argumentFootprint);
    return result;
  }

  virtual void return_(unsigned size, Operand* value) {
    appendReturn(&c, size, static_cast<Value*>(value));
  }

  virtual void initLocal(unsigned footprint, unsigned index, OperandType type)
  {
    assert(&c, index + footprint <= c.localFootprint);

    Value* v = value(&c, valueType(&c, type));

    if (footprint > 1) {
      assert(&c, footprint == 2);

      unsigned highIndex;
      unsigned lowIndex;
      if (c.arch->bigEndian()) {
        highIndex = index + 1;
        lowIndex = index;
      } else {
        lowIndex = index + 1;
        highIndex = index;      
      }

      if (TargetBytesPerWord == 4) {
        initLocal(1, highIndex, type);
        Value* next = c.locals[highIndex].value;
        v->nextWord = next;
        next->nextWord = v;
        next->wordIndex = 1;
      }

      index = lowIndex;
    }

    if (DebugFrame) {
      fprintf(stderr, "init local %p at %d (%d)\n",
              v, index, frameIndex(&c, index));
    }

    appendFrameSite(&c, v, frameIndex(&c, index));

    Local* local = c.locals + index;
    local->value = v;
    v->home = frameIndex(&c, index);
  }

  virtual void initLocalsFromLogicalIp(unsigned logicalIp) {
    assert(&c, logicalIp < c.logicalCodeLength);

    unsigned footprint = sizeof(Local) * c.localFootprint;
    Local* newLocals = static_cast<Local*>(c.zone->allocate(footprint));
    memset(newLocals, 0, footprint);
    c.locals = newLocals;

    Event* e = c.logicalCode[logicalIp]->firstEvent;
    for (int i = 0; i < static_cast<int>(c.localFootprint); ++i) {
      Local* local = e->localsBefore + i;
      if (local->value) {
        initLocal
          (1, i, local->value->type == ValueGeneral ? IntegerType : FloatType);
      }
    }

    linkLocals(&c, e->localsBefore, newLocals);
  }

  virtual void storeLocal(unsigned footprint, Operand* src, unsigned index) {
    local::storeLocal(&c, footprint, static_cast<Value*>(src), index, true);
  }

  virtual Operand* loadLocal(unsigned footprint, unsigned index) {
    return local::loadLocal(&c, footprint, index);
  }

  virtual void saveLocals() {
    appendSaveLocals(&c);
  }

  virtual void checkBounds(Operand* object, unsigned lengthOffset,
                           Operand* index, intptr_t handler)
  {
    appendBoundsCheck(&c, static_cast<Value*>(object), lengthOffset,
                      static_cast<Value*>(index), handler);
  }

  virtual void store(unsigned srcSize, Operand* src, unsigned dstSize,
                     Operand* dst)
  {
    appendMove(&c, Move, srcSize, srcSize, static_cast<Value*>(src),
               dstSize, static_cast<Value*>(dst));
  }

  virtual Operand* load(unsigned srcSize, unsigned srcSelectSize, Operand* src,
                        unsigned dstSize)
  {
    assert(&c, dstSize >= TargetBytesPerWord);

    Value* dst = value(&c, static_cast<Value*>(src)->type);
    appendMove(&c, Move, srcSize, srcSelectSize, static_cast<Value*>(src),
               dstSize, dst);
    return dst;
  }

  virtual Operand* loadz(unsigned srcSize, unsigned srcSelectSize,
                         Operand* src, unsigned dstSize)
  {
    assert(&c, dstSize >= TargetBytesPerWord);

    Value* dst = value(&c, static_cast<Value*>(src)->type);
    appendMove(&c, MoveZ, srcSize, srcSelectSize, static_cast<Value*>(src),
               dstSize, dst);
    return dst;
  }

  virtual void jumpIfEqual(unsigned size, Operand* a, Operand* b,
                           Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfNotEqual(unsigned size, Operand* a, Operand* b,
                              Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfNotEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfLess(unsigned size, Operand* a, Operand* b,
                          Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfLess, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfGreater(unsigned size, Operand* a, Operand* b,
                             Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfGreater, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfLessOrEqual(unsigned size, Operand* a, Operand* b,
                                 Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfLessOrEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfGreaterOrEqual(unsigned size, Operand* a, Operand* b,
                                    Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);

    appendBranch(&c, JumpIfGreaterOrEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatEqual(unsigned size, Operand* a, Operand* b,
                           Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatNotEqual(unsigned size, Operand* a, Operand* b,
                                   Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatNotEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatLess(unsigned size, Operand* a, Operand* b,
                               Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatLess, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatGreater(unsigned size, Operand* a, Operand* b,
                                  Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatGreater, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatLessOrEqual(unsigned size, Operand* a, Operand* b,
                                 Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatLessOrEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatGreaterOrEqual(unsigned size, Operand* a, Operand* b,
                                    Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatGreaterOrEqual, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatLessOrUnordered(unsigned size, Operand* a,
                                          Operand* b, Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatLessOrUnordered, size, static_cast<Value*>(a),
                 static_cast<Value*>(b), static_cast<Value*>(address));
  }

  virtual void jumpIfFloatGreaterOrUnordered(unsigned size, Operand* a,
                                             Operand* b, Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatGreaterOrUnordered, size,
                 static_cast<Value*>(a), static_cast<Value*>(b),
                 static_cast<Value*>(address));
  }

  virtual void jumpIfFloatLessOrEqualOrUnordered(unsigned size, Operand* a,
                                                 Operand* b, Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatLessOrEqualOrUnordered, size,
                 static_cast<Value*>(a), static_cast<Value*>(b),
                 static_cast<Value*>(address));
  }

  virtual void jumpIfFloatGreaterOrEqualOrUnordered(unsigned size, Operand* a,
                                                    Operand* b,
                                                    Operand* address)
  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);

    appendBranch(&c, JumpIfFloatGreaterOrEqualOrUnordered, size,
                 static_cast<Value*>(a), static_cast<Value*>(b),
                 static_cast<Value*>(address));
  }

  virtual void jmp(Operand* address) {
    appendJump(&c, Jump, static_cast<Value*>(address));
  }

  virtual void exit(Operand* address) {
    appendJump(&c, Jump, static_cast<Value*>(address), true);
  }

  virtual Operand* add(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Add, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* sub(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Subtract, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* mul(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Multiply, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* div(unsigned size, Operand* a, Operand* b)  {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Divide, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* rem(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral
           and static_cast<Value*>(b)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Remainder, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* fadd(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    static_cast<Value*>(a)->type = static_cast<Value*>(b)->type = ValueFloat;
    appendCombine(&c, FloatAdd, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* fsub(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    static_cast<Value*>(a)->type = static_cast<Value*>(b)->type = ValueFloat;
    appendCombine(&c, FloatSubtract, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* fmul(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    static_cast<Value*>(a)->type = static_cast<Value*>(b)->type = ValueFloat;
    appendCombine(&c, FloatMultiply, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* fdiv(unsigned size, Operand* a, Operand* b)  {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendCombine(&c, FloatDivide, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* frem(unsigned size, Operand* a, Operand* b) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat
           and static_cast<Value*>(b)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendCombine(&c, FloatRemainder, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* shl(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, ShiftLeft, TargetBytesPerWord, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* shr(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, ShiftRight, TargetBytesPerWord, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* ushr(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine
      (&c, UnsignedShiftRight, TargetBytesPerWord, static_cast<Value*>(a),
       size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* and_(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, And, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* or_(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Or, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* xor_(unsigned size, Operand* a, Operand* b) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendCombine(&c, Xor, size, static_cast<Value*>(a),
                  size, static_cast<Value*>(b), size, result);
    return result;
  }

  virtual Operand* neg(unsigned size, Operand* a) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendTranslate(&c, Negate, size, static_cast<Value*>(a), size, result);
    return result;
  }

  virtual Operand* fneg(unsigned size, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendTranslate
      (&c, FloatNegate, size, static_cast<Value*>(a), size, result);
    return result;
  }

  virtual Operand* abs(unsigned size, Operand* a) {
  	assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueGeneral);
    appendTranslate(&c, Absolute, size, static_cast<Value*>(a), size, result);
    return result;
  }

  virtual Operand* fabs(unsigned size, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendTranslate
      (&c, FloatAbsolute, size, static_cast<Value*>(a), size, result);
    return result;
  }

  virtual Operand* fsqrt(unsigned size, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendTranslate
      (&c, FloatSquareRoot, size, static_cast<Value*>(a), size, result);
    return result;
  }
  
  virtual Operand* f2f(unsigned aSize, unsigned resSize, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat);
    Value* result = value(&c, ValueFloat);
    appendTranslate
      (&c, Float2Float, aSize, static_cast<Value*>(a), resSize, result);
    return result;
  }
  
  virtual Operand* f2i(unsigned aSize, unsigned resSize, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueFloat);
    Value* result = value(&c, ValueGeneral);
    appendTranslate
      (&c, Float2Int, aSize, static_cast<Value*>(a), resSize, result);
    return result;
  }
  
  virtual Operand* i2f(unsigned aSize, unsigned resSize, Operand* a) {
    assert(&c, static_cast<Value*>(a)->type == ValueGeneral);
    Value* result = value(&c, ValueFloat);
    appendTranslate
      (&c, Int2Float, aSize, static_cast<Value*>(a), resSize, result);
    return result;
  }

  virtual void loadBarrier() {
    appendBarrier(&c, LoadBarrier);
  }

  virtual void storeStoreBarrier() {
    appendBarrier(&c, StoreStoreBarrier);
  }

  virtual void storeLoadBarrier() {
    appendBarrier(&c, StoreLoadBarrier);
  }

  virtual void compile(uintptr_t stackOverflowHandler,
                       unsigned stackLimitOffset)
  {
    local::compile(&c, stackOverflowHandler, stackLimitOffset);
  }

  virtual unsigned resolve(uint8_t* dst) {
    c.machineCode = dst;
    c.assembler->setDestination(dst);

    Block* block = c.firstBlock;
    while (block->nextBlock or block->nextInstruction) {
      Block* next = block->nextBlock
        ? block->nextBlock
        : block->nextInstruction->firstEvent->block;

      next->start = block->assemblerBlock->resolve
        (block->start, next->assemblerBlock);

      block = next;
    }

    return c.machineCodeSize = block->assemblerBlock->resolve
      (block->start, 0) + c.assembler->footerSize();
  }

  virtual unsigned poolSize() {
    return c.constantCount * TargetBytesPerWord;
  }

  virtual void write() {
    c.assembler->write();

    int i = 0;
    for (ConstantPoolNode* n = c.firstConstant; n; n = n->next) {
      target_intptr_t* target = reinterpret_cast<target_intptr_t*>
        (c.machineCode + pad(c.machineCodeSize, TargetBytesPerWord) + i);

      if (n->promise->resolved()) {
        *target = targetVW(n->promise->value());
      } else {
        class Listener: public Promise::Listener {
         public:
          Listener(target_intptr_t* target): target(target){ }

          virtual bool resolve(int64_t value, void** location) {
            *target = targetVW(value);
            if (location) *location = target;
            return true;
          }

          target_intptr_t* target;
        };
        new (n->promise->listen(sizeof(Listener))) Listener(target);
      }

      i += TargetBytesPerWord;
    }
  }

  virtual void dispose() {
    // ignore
  }

  Context c;
  local::Client client;
};

} // namespace local

} // namespace

namespace vm {

Compiler*
makeCompiler(System* system, Assembler* assembler, Zone* zone,
             Compiler::Client* client)
{
  return new (zone->allocate(sizeof(local::MyCompiler)))
    local::MyCompiler(system, assembler, zone, client);
}

} // namespace vm

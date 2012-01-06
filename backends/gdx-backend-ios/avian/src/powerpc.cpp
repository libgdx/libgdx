/* Copyright (c) 2009-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "assembler.h"
#include "vector.h"

#define CAST1(x) reinterpret_cast<UnaryOperationType>(x)
#define CAST2(x) reinterpret_cast<BinaryOperationType>(x)
#define CAST3(x) reinterpret_cast<TernaryOperationType>(x)
#define CAST_BRANCH(x) reinterpret_cast<BranchOperationType>(x)

using namespace vm;

namespace {

namespace isa {
// INSTRUCTION FORMATS
inline int D(int op, int rt, int ra, int d) { return op<<26|rt<<21|ra<<16|(d & 0xFFFF); }
inline int DS(int op, int rt, int ra, int ds, int xo) { return op<<26|rt<<21|ra<<16|ds<<2|xo; }
inline int I(int op, int li, int aa, int lk) { return op<<26|(li & 0x3FFFFFC)|aa<<1|lk; }
inline int B(int op, int bo, int bi, int bd, int aa, int lk) { return op<<26|bo<<21|bi<<16|(bd & 0xFFFC)|aa<<1|lk; }
inline int SC(int op, int lev) { return op<<26|lev<<5|2; }
inline int X(int op, int rt, int ra, int rb, int xo, int rc) { return op<<26|rt<<21|ra<<16|rb<<11|xo<<1|rc; }
inline int XL(int op, int bt, int ba, int bb, int xo, int lk) { return op<<26|bt<<21|ba<<16|bb<<11|xo<<1|lk; }
inline int XFX(int op, int rt, int spr, int xo) { return op<<26|rt<<21|((spr >> 5) | ((spr << 5) & 0x3E0))<<11|xo<<1; }
inline int XFL(int op, int flm, int frb, int xo, int rc) { return op<<26|flm<<17|frb<<11|xo<<1|rc; }
inline int XS(int op, int rs, int ra, int sh, int xo, int sh2, int rc) { return op<<26|rs<<21|ra<<16|sh<<11|xo<<2|sh2<<1|rc; }
inline int XO(int op, int rt, int ra, int rb, int oe, int xo, int rc) { return op<<26|rt<<21|ra<<16|rb<<11|oe<<10|xo<<1|rc; }
inline int A(int op, int frt, int fra, int frb, int frc, int xo, int rc) { return op<<26|frt<<21|fra<<16|frb<<11|frc<<6|xo<<1|rc; }
inline int M(int op, int rs, int ra, int rb, int mb, int me, int rc) { return op<<26|rs<<21|ra<<16|rb<<11|mb<<6|me<<1|rc; }
inline int MD(int op, int rs, int ra, int sh, int mb, int xo, int sh2, int rc) { return op<<26|rs<<21|ra<<16|sh<<11|mb<<5|xo<<2|sh2<<1|rc; }
inline int MDS(int op, int rs, int ra, int rb, int mb, int xo, int rc) { return op<<26|rs<<21|ra<<16|rb<<11|mb<<5|xo<<1|rc; }
// INSTRUCTIONS
inline int lbz(int rt, int ra, int i) { return D(34, rt, ra, i); }
inline int lbzx(int rt, int ra, int rb) { return X(31, rt, ra, rb, 87, 0); }
inline int lha(int rt, int ra, int i) { return D(42, rt, ra, i); }
inline int lhax(int rt, int ra, int rb) { return X(31, rt, ra, rb, 343, 0); }
inline int lhz(int rt, int ra, int i) { return D(40, rt, ra, i); }
inline int lhzx(int rt, int ra, int rb) { return X(31, rt, ra, rb, 279, 0); }
inline int lwz(int rt, int ra, int i) { return D(32, rt, ra, i); }
inline int lwzx(int rt, int ra, int rb) { return X(31, rt, ra, rb, 23, 0); }
inline int stb(int rs, int ra, int i) { return D(38, rs, ra, i); }
inline int stbx(int rs, int ra, int rb) { return X(31, rs, ra, rb, 215, 0); }
inline int sth(int rs, int ra, int i) { return D(44, rs, ra, i); }
inline int sthx(int rs, int ra, int rb) { return X(31, rs, ra, rb, 407, 0); }
inline int stw(int rs, int ra, int i) { return D(36, rs, ra, i); }
inline int stwu(int rs, int ra, int i) { return D(37, rs, ra, i); }
inline int stwux(int rs, int ra, int rb) { return X(31, rs, ra, rb, 183, 0); }
inline int stwx(int rs, int ra, int rb) { return X(31, rs, ra, rb, 151, 0); }
inline int add(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 266, 0); }
inline int addc(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 10, 0); }
inline int adde(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 138, 0); }
inline int addi(int rt, int ra, int i) { return D(14, rt, ra, i); }
inline int addic(int rt, int ra, int i) { return D(12, rt, ra, i); }
inline int addis(int rt, int ra, int i) { return D(15, rt, ra, i); }
inline int subf(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 40, 0); }
inline int subfc(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 8, 0); }
inline int subfe(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 136, 0); }
inline int subfic(int rt, int ra, int i) { return D(8, rt, ra, i); }
inline int subfze(int rt, int ra) { return XO(31, rt, ra, 0, 0, 200, 0); }
inline int mullw(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 235, 0); }
inline int mulhw(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 75, 0); }
inline int mulhwu(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 11, 0); }
inline int mulli(int rt, int ra, int i) { return D(7, rt, ra, i); }
inline int divw(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 491, 0); }
inline int divwu(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 459, 0); }
inline int divd(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 489, 0); }
inline int divdu(int rt, int ra, int rb) { return XO(31, rt, ra, rb, 0, 457, 0); }
inline int neg(int rt, int ra) { return XO(31, rt, ra, 0, 0, 104, 0); }
inline int and_(int rt, int ra, int rb) { return X(31, ra, rt, rb, 28, 0); }
inline int andi(int rt, int ra, int i) { return D(28, ra, rt, i); }
inline int andis(int rt, int ra, int i) { return D(29, ra, rt, i); }
inline int or_(int rt, int ra, int rb) { return X(31, ra, rt, rb, 444, 0); }
inline int ori(int rt, int ra, int i) { return D(24, rt, ra, i); }
inline int xor_(int rt, int ra, int rb) { return X(31, ra, rt, rb, 316, 0); }
inline int oris(int rt, int ra, int i) { return D(25, rt, ra, i); }
inline int xori(int rt, int ra, int i) { return D(26, rt, ra, i); }
inline int xoris(int rt, int ra, int i) { return D(27, rt, ra, i); }
inline int rlwinm(int rt, int ra, int i, int mb, int me) { return M(21, ra, rt, i, mb, me, 0); }
inline int rlwimi(int rt, int ra, int i, int mb, int me) { return M(20, ra, rt, i, mb, me, 0); }
inline int slw(int rt, int ra, int sh) { return X(31, ra, rt, sh, 24, 0); }
inline int sld(int rt, int ra, int rb) { return X(31, ra, rt, rb, 27, 0); }
inline int srw(int rt, int ra, int sh) { return X(31, ra, rt, sh, 536, 0); }
inline int sraw(int rt, int ra, int sh) { return X(31, ra, rt, sh, 792, 0); }
inline int srawi(int rt, int ra, int sh) { return X(31, ra, rt, sh, 824, 0); }
inline int extsb(int rt, int rs) { return X(31, rs, rt, 0, 954, 0); }
inline int extsh(int rt, int rs) { return X(31, rs, rt, 0, 922, 0); }
inline int mfspr(int rt, int spr) { return XFX(31, rt, spr, 339); }
inline int mtspr(int spr, int rs) { return XFX(31, rs, spr, 467); }
inline int b(int i) { return I(18, i, 0, 0); }
inline int bl(int i) { return I(18, i, 0, 1); }
inline int bcctr(int bo, int bi, int lk) { return XL(19, bo, bi, 0, 528, lk); }
inline int bclr(int bo, int bi, int lk) { return XL(19, bo, bi, 0, 16, lk); }
inline int bc(int bo, int bi, int bd, int lk) { return B(16, bo, bi, bd, 0, lk); }
inline int cmp(int bf, int ra, int rb) { return X(31, bf << 2, ra, rb, 0, 0); }
inline int cmpl(int bf, int ra, int rb) { return X(31, bf << 2, ra, rb, 32, 0); }
inline int cmpi(int bf, int ra, int i) { return D(11, bf << 2, ra, i); }
inline int cmpli(int bf, int ra, int i) { return D(10, bf << 2, ra, i); }
inline int sync(int L) { return X(31, L, 0, 0, 598, 0); }
// PSEUDO-INSTRUCTIONS
inline int li(int rt, int i) { return addi(rt, 0, i); }
inline int lis(int rt, int i) { return addis(rt, 0, i); }
inline int slwi(int rt, int ra, int i) { return rlwinm(rt, ra, i, 0, 31-i); }
inline int srwi(int rt, int ra, int i) { return rlwinm(rt, ra, 32-i, i, 31); }
inline int sub(int rt, int ra, int rb) { return subf(rt, rb, ra); }
inline int subc(int rt, int ra, int rb) { return subfc(rt, rb, ra); }
inline int subi(int rt, int ra, int i) { return addi(rt, ra, -i); }
inline int subis(int rt, int ra, int i) { return addis(rt, ra, -i); }
inline int mr(int rt, int ra) { return or_(rt, ra, ra); }
inline int mflr(int rx) { return mfspr(rx, 8); }
inline int mtlr(int rx) { return mtspr(8, rx); }
inline int mtctr(int rd) { return mtspr(9, rd); }
inline int bctr() { return bcctr(20, 0, 0); }
inline int bctrl() { return bcctr(20, 0, 1); }
inline int blr() { return bclr(20, 0, 0); }
inline int blt(int i) { return bc(12, 0, i, 0); }
inline int bgt(int i) { return bc(12, 1, i, 0); }
inline int bge(int i) { return bc(4, 0, i, 0); }
inline int ble(int i) { return bc(4, 1, i, 0); }
inline int beq(int i) { return bc(12, 2, i, 0); }
inline int bne(int i) { return bc(4, 2, i, 0); }
inline int cmpw(int ra, int rb) { return cmp(0, ra, rb); }
inline int cmplw(int ra, int rb) { return cmpl(0, ra, rb); }
inline int cmpwi(int ra, int i) { return cmpi(0, ra, i); }
inline int cmplwi(int ra, int i) { return cmpli(0, ra, i); }
}

const int64_t MASK_LO32 = 0x0ffffffff;
const int     MASK_LO16 = 0x0ffff;
const int     MASK_LO8  = 0x0ff;
inline int lo32(int64_t i) { return (int)(i & MASK_LO32); }
inline int hi32(int64_t i) { return lo32(i >> 32); }
inline int lo16(int64_t i) { return (int)(i & MASK_LO16); }
inline int hi16(int64_t i) { return lo16(i >> 16); }
inline int lo8(int64_t i) { return (int)(i & MASK_LO8); }
inline int hi8(int64_t i) { return lo8(i >> 8); }

inline int ha16(int32_t i) { 
    return ((i >> 16) + ((i & 0x8000) ? 1 : 0)) & 0xffff;
}

inline int unha16(int32_t high, int32_t low) {
    return ((high - ((low & 0x8000) ? 1 : 0)) << 16) | low; 
}

inline bool
isInt16(target_intptr_t v)
{
  return v == static_cast<int16_t>(v);
}

inline int
carry16(target_intptr_t v)
{
  return static_cast<int16_t>(v) < 0 ? 1 : 0;
}

#ifdef __APPLE__
const unsigned FrameFooterSize = 6;
const unsigned ReturnAddressOffset = 2;
const unsigned AlignArguments = false;
#else
const unsigned FrameFooterSize = 2;
const unsigned ReturnAddressOffset = 1;
const unsigned AlignArguments = true;
#endif

const unsigned StackAlignmentInBytes = 16;
const unsigned StackAlignmentInWords
= StackAlignmentInBytes / TargetBytesPerWord;

const int StackRegister = 1;
const int ThreadRegister = 13;

const bool DebugJumps = false;

class Context;
class MyBlock;
class JumpOffset;
class JumpEvent;

void
resolve(MyBlock*);

unsigned
padding(MyBlock*, unsigned);

class MyBlock: public Assembler::Block {
 public:
  MyBlock(Context* context, unsigned offset):
    context(context), next(0), jumpOffsetHead(0), jumpOffsetTail(0),
    lastJumpOffsetTail(0), jumpEventHead(0), jumpEventTail(0),
    lastEventOffset(0), offset(offset), start(~0), size(0), resolved(false)
  { }

  virtual unsigned resolve(unsigned start, Assembler::Block* next) {
    this->start = start;
    this->next = static_cast<MyBlock*>(next);

    ::resolve(this);

    this->resolved = true;

    return start + size + padding(this, size);
  }

  Context* context;
  MyBlock* next;
  JumpOffset* jumpOffsetHead;
  JumpOffset* jumpOffsetTail;
  JumpOffset* lastJumpOffsetTail;
  JumpEvent* jumpEventHead;
  JumpEvent* jumpEventTail;
  unsigned lastEventOffset;
  unsigned offset;
  unsigned start;
  unsigned size;
  bool resolved;
};

class Task;
class ConstantPoolEntry;

class Context {
 public:
  Context(System* s, Allocator* a, Zone* zone):
    s(s), zone(zone), client(0), code(s, a, 1024), tasks(0), result(0),
    firstBlock(new (zone->allocate(sizeof(MyBlock))) MyBlock(this, 0)),
    lastBlock(firstBlock), jumpOffsetHead(0), jumpOffsetTail(0),
    constantPool(0), constantPoolCount(0)
  { }

  System* s;
  Zone* zone;
  Assembler::Client* client;
  Vector code;
  Task* tasks;
  uint8_t* result;
  MyBlock* firstBlock;
  MyBlock* lastBlock;
  JumpOffset* jumpOffsetHead;
  JumpOffset* jumpOffsetTail;
  ConstantPoolEntry* constantPool;
  unsigned constantPoolCount;
};

class Task {
 public:
  Task(Task* next): next(next) { }

  virtual void run(Context* c) = 0;

  Task* next;
};

typedef void (*OperationType)(Context*);

typedef void (*UnaryOperationType)(Context*, unsigned, Assembler::Operand*);

typedef void (*BinaryOperationType)
(Context*, unsigned, Assembler::Operand*, unsigned, Assembler::Operand*);

typedef void (*TernaryOperationType)
(Context*, unsigned, Assembler::Operand*, Assembler::Operand*,
 Assembler::Operand*);

typedef void (*BranchOperationType)
(Context*, TernaryOperation, unsigned, Assembler::Operand*,
 Assembler::Operand*, Assembler::Operand*);

class ArchitectureContext {
 public:
  ArchitectureContext(System* s): s(s) { }

  System* s;
  OperationType operations[OperationCount];
  UnaryOperationType unaryOperations[UnaryOperationCount
                                     * OperandTypeCount];
  BinaryOperationType binaryOperations
  [BinaryOperationCount * OperandTypeCount * OperandTypeCount];
  TernaryOperationType ternaryOperations
  [NonBranchTernaryOperationCount * OperandTypeCount];
  BranchOperationType branchOperations
  [BranchOperationCount * OperandTypeCount * OperandTypeCount];
};

inline void NO_RETURN
abort(Context* c)
{
  abort(c->s);
}

inline void NO_RETURN
abort(ArchitectureContext* c)
{
  abort(c->s);
}

#ifndef NDEBUG
inline void
assert(Context* c, bool v)
{
  assert(c->s, v);
}

inline void
assert(ArchitectureContext* c, bool v)
{
  assert(c->s, v);
}
#endif // not NDEBUG

inline void
expect(Context* c, bool v)
{
  expect(c->s, v);
}

class Offset: public Promise {
 public:
  Offset(Context* c, MyBlock* block, unsigned offset):
    c(c), block(block), offset(offset)
  { }

  virtual bool resolved() {
    return block->resolved;
  }
  
  virtual int64_t value() {
    assert(c, resolved());

    unsigned o = offset - block->offset;
    return block->start + padding(block, o) + o;
  }

  Context* c;
  MyBlock* block;
  unsigned offset;
};

Promise*
offset(Context* c)
{
  return new (c->zone->allocate(sizeof(Offset)))
    Offset(c, c->lastBlock, c->code.length());
}

bool
bounded(int right, int left, int32_t v)
{
  return ((v << left) >> left) == v and ((v >> right) << right) == v;
}

void*
updateOffset(System* s, uint8_t* instruction, bool conditional, int64_t value,
             void* jumpAddress)
{
  int32_t v = reinterpret_cast<uint8_t*>(value) - instruction;
   
  int32_t mask;
  if (conditional) {
    if (not bounded(2, 16, v)) {
      *static_cast<uint32_t*>(jumpAddress) = isa::b(0);
      updateOffset(s, static_cast<uint8_t*>(jumpAddress), false, value, 0);

      v = static_cast<uint8_t*>(jumpAddress) - instruction;

      expect(s, bounded(2, 16, v));
    }
    mask = 0xFFFC;
  } else {
    expect(s, bounded(2, 6, v));
    mask = 0x3FFFFFC;
  }

  int32_t* p = reinterpret_cast<int32_t*>(instruction);
  *p = targetV4((v & mask) | ((~mask) & targetV4(*p)));

  return instruction + 4;
}

class OffsetListener: public Promise::Listener {
 public:
  OffsetListener(System* s, uint8_t* instruction, bool conditional,
                 void* jumpAddress):
    s(s),
    instruction(instruction),
    jumpAddress(jumpAddress),
    conditional(conditional)
  { }

  virtual bool resolve(int64_t value, void** location) {
    void* p = updateOffset(s, instruction, conditional, value, jumpAddress);
    if (location) *location = p;
    return false;
  }

  System* s;
  uint8_t* instruction;
  void* jumpAddress;
  bool conditional;
};

class OffsetTask: public Task {
 public:
  OffsetTask(Task* next, Promise* promise, Promise* instructionOffset,
             bool conditional):
    Task(next),
    promise(promise),
    instructionOffset(instructionOffset),
    jumpAddress(0),
    conditional(conditional)
  { }

  virtual void run(Context* c) {
    if (promise->resolved()) {
      updateOffset
        (c->s, c->result + instructionOffset->value(), conditional,
         promise->value(), jumpAddress);
    } else {
      new (promise->listen(sizeof(OffsetListener)))
        OffsetListener(c->s, c->result + instructionOffset->value(),
                       conditional, jumpAddress);
    }
  }

  Promise* promise;
  Promise* instructionOffset;
  void* jumpAddress;
  bool conditional;
};

class JumpOffset {
 public:
  JumpOffset(MyBlock* block, OffsetTask* task, unsigned offset):
    block(block), task(task), next(0), offset(offset)
  { }

  MyBlock* block;
  OffsetTask* task;
  JumpOffset* next;
  unsigned offset;  
};

class JumpEvent {
 public:
  JumpEvent(JumpOffset* jumpOffsetHead, JumpOffset* jumpOffsetTail,
            unsigned offset):
    jumpOffsetHead(jumpOffsetHead), jumpOffsetTail(jumpOffsetTail), next(0),
    offset(offset)
  { }

  JumpOffset* jumpOffsetHead;
  JumpOffset* jumpOffsetTail;
  JumpEvent* next;
  unsigned offset;
};

void
appendOffsetTask(Context* c, Promise* promise, Promise* instructionOffset,
                 bool conditional)
{
  OffsetTask* task = new (c->zone->allocate(sizeof(OffsetTask))) OffsetTask
    (c->tasks, promise, instructionOffset, conditional);

  c->tasks = task;

  if (conditional) {
    JumpOffset* offset = new (c->zone->allocate(sizeof(JumpOffset))) JumpOffset
      (c->lastBlock, task, c->code.length() - c->lastBlock->offset);

    if (c->lastBlock->jumpOffsetTail) {
      c->lastBlock->jumpOffsetTail->next = offset;
    } else {
      c->lastBlock->jumpOffsetHead = offset;
    }
    c->lastBlock->jumpOffsetTail = offset;
  }
}

void
appendJumpEvent(Context* c, MyBlock* b, unsigned offset, JumpOffset* head,
                JumpOffset* tail)
{
  JumpEvent* e = new (c->zone->allocate(sizeof(JumpEvent))) JumpEvent
    (head, tail, offset);

  if (b->jumpEventTail) {
    b->jumpEventTail->next = e;
  } else {
    b->jumpEventHead = e;
  }
  b->jumpEventTail = e;
}

bool
needJump(MyBlock* b)
{
  return b->next or (not bounded(2, 16, b->size));
}

unsigned
padding(MyBlock* b, unsigned offset)
{
  unsigned total = 0;
  for (JumpEvent* e = b->jumpEventHead; e; e = e->next) {
    if (e->offset <= offset) {
      for (JumpOffset* o = e->jumpOffsetHead; o; o = o->next) {
        total += TargetBytesPerWord;
      }

      if (needJump(b)) {
        total += TargetBytesPerWord;
      }
    } else {
      break;
    }
  }

  return total;
}

void
resolve(MyBlock* b)
{
  Context* c = b->context;

  for (JumpEvent** e = &(b->jumpEventHead); *e;) {
    for (JumpOffset** o = &((*e)->jumpOffsetHead); *o;) {
      if ((*o)->task->promise->resolved()
          and (*o)->task->instructionOffset->resolved())
      {
        int32_t v = reinterpret_cast<uint8_t*>((*o)->task->promise->value())
          - (c->result + (*o)->task->instructionOffset->value());

        if (bounded(2, 16, v)) {
          // this conditional jump needs no indirection -- a direct
          // jump will suffice
          *o = (*o)->next;
          continue;
        }
      }

      o = &((*o)->next);
    }

    if ((*e)->jumpOffsetHead == 0) {
      *e = (*e)->next;
    } else {
      e = &((*e)->next);
    }
  }

  if (b->jumpOffsetHead) {
    if (c->jumpOffsetTail) {
      c->jumpOffsetTail->next = b->jumpOffsetHead;
    } else {
      c->jumpOffsetHead = b->jumpOffsetHead;
    }
    c->jumpOffsetTail = b->jumpOffsetTail;
  }

  if (c->jumpOffsetHead) {
    bool append;
    if (b->next == 0 or b->next->jumpEventHead) {
      append = true;
    } else {
      int32_t v = (b->start + b->size + b->next->size + TargetBytesPerWord)
        - (c->jumpOffsetHead->offset + c->jumpOffsetHead->block->start);

      append = not bounded(2, 16, v);

      if (DebugJumps) {
        fprintf(stderr,
                "current %p %d %d next %p %d %d\n",
                b, b->start, b->size, b->next, b->start + b->size,
                b->next->size);
        fprintf(stderr,
                "offset %p %d is of distance %d to next block; append? %d\n",
                c->jumpOffsetHead, c->jumpOffsetHead->offset, v, append);
      }
    }

    if (append) {
#ifndef NDEBUG
      int32_t v = (b->start + b->size)
        - (c->jumpOffsetHead->offset + c->jumpOffsetHead->block->start);
      
      expect(c, bounded(2, 16, v));
#endif // not NDEBUG

      appendJumpEvent(c, b, b->size, c->jumpOffsetHead, c->jumpOffsetTail);

      if (DebugJumps) {
        for (JumpOffset* o = c->jumpOffsetHead; o; o = o->next) {
          fprintf(stderr,
                  "include %p %d in jump event %p at offset %d in block %p\n",
                  o, o->offset, b->jumpEventTail, b->size, b);
        }
      }

      c->jumpOffsetHead = 0;
      c->jumpOffsetTail = 0;
    }
  }
}

inline unsigned
index(ArchitectureContext*, UnaryOperation operation, OperandType operand)
{
  return operation + (UnaryOperationCount * operand);
}

inline unsigned
index(ArchitectureContext*,
      BinaryOperation operation,
      OperandType operand1,
      OperandType operand2)
{
  return operation
    + (BinaryOperationCount * operand1)
    + (BinaryOperationCount * OperandTypeCount * operand2);
}

bool
isBranch(TernaryOperation op)
{
  return op > FloatMin;
}

bool
isFloatBranch(TernaryOperation op)
{
  return op > JumpIfNotEqual;
}

inline unsigned
index(ArchitectureContext* c UNUSED,
      TernaryOperation operation,
      OperandType operand1)
{
  assert(c, not isBranch(operation));

  return operation + (NonBranchTernaryOperationCount * operand1);
}

unsigned
branchIndex(ArchitectureContext* c UNUSED, OperandType operand1,
            OperandType operand2)
{
  return operand1 + (OperandTypeCount * operand2);
}

// BEGIN OPERATION COMPILERS

using namespace isa;

inline void emit(Context* con, int code) { con->code.append4(targetV4(code)); }
inline int newTemp(Context* con) { return con->client->acquireTemporary(); }
inline void freeTemp(Context* con, int r) { con->client->releaseTemporary(r); }
inline int64_t getValue(Assembler::Constant* c) { return c->value->value(); }

inline void
write4(uint8_t* dst, uint32_t v)
{
  memcpy(dst, &v, 4);
}

void shiftLeftR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t)
{
  if(size == 8) {
    Assembler::Register Tmp(newTemp(con), newTemp(con)); Assembler::Register* tmp = &Tmp;
    emit(con, subfic(tmp->high, a->low, 32));
    emit(con, slw(t->high, b->high, a->low));
    emit(con, srw(tmp->low, b->low, tmp->high));
    emit(con, or_(t->high, t->high, tmp->low));
    emit(con, addi(tmp->high, a->low, -32));
    emit(con, slw(tmp->low, b->low, tmp->high));
    emit(con, or_(t->high, t->high, tmp->low));
    freeTemp(con, tmp->high); freeTemp(con, tmp->low);
  }
  emit(con, slw(t->low, b->low, a->low));
}

void shiftLeftC(Context* con, unsigned size, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  int sh = getValue(a);
  if (size == 8) {
    if (sh < 32) {
      emit(con, rlwinm(t->high,b->high,sh,0,31-sh));
      emit(con, rlwimi(t->high,b->low,sh,32-sh,31));
      emit(con, slwi(t->low, b->low, sh));
    } else {
      emit(con, rlwinm(t->high,b->low,sh-32,0,63-sh));
      emit(con, li(t->low,0));
    }
  } else {
    emit(con, slwi(t->low, b->low, sh));
  }
}

void shiftRightR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t)
{
  if(size == 8) {
    Assembler::Register Tmp(newTemp(con), newTemp(con)); Assembler::Register* tmp = &Tmp;
    emit(con, subfic(tmp->high, a->low, 32));
    emit(con, srw(t->low, b->low, a->low));
    emit(con, slw(tmp->low, b->high, tmp->high));
    emit(con, or_(t->low, t->low, tmp->low));
    emit(con, addic(tmp->high, a->low, -32));
    emit(con, sraw(tmp->low, b->high, tmp->high));
    emit(con, ble(8));
    emit(con, ori(t->low, tmp->low, 0));
    emit(con, sraw(t->high, b->high, a->low));
    freeTemp(con, tmp->high); freeTemp(con, tmp->low);
  } else {
    emit(con, sraw(t->low, b->low, a->low));
  }
}

void shiftRightC(Context* con, unsigned size, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  int sh = getValue(a);
  if(size == 8) {
    if (sh < 32) {
      emit(con, rlwinm(t->low,b->low,32-sh,sh,31));
      emit(con, rlwimi(t->low,b->high,32-sh,0,sh-1));
      emit(con, srawi(t->high,b->high,sh));
    } else {
      emit(con, srawi(t->high,b->high,31));
      emit(con, srawi(t->low,b->high,sh-32));
    }
  } else {
    emit(con, srawi(t->low, b->low, sh));
  }
}

void unsignedShiftRightR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t)
{
  emit(con, srw(t->low, b->low, a->low));
  if(size == 8) {
    Assembler::Register Tmp(newTemp(con), newTemp(con)); Assembler::Register* tmp = &Tmp;
    emit(con, subfic(tmp->high, a->low, 32));
    emit(con, slw(tmp->low, b->high, tmp->high));
    emit(con, or_(t->low, t->low, tmp->low));
    emit(con, addi(tmp->high, a->low, -32));
    emit(con, srw(tmp->low, b->high, tmp->high));
    emit(con, or_(t->low, t->low, tmp->low));
    emit(con, srw(t->high, b->high, a->low));
    freeTemp(con, tmp->high); freeTemp(con, tmp->low);
  }
}

void
moveRR(Context* c, unsigned srcSize, Assembler::Register* src,
       unsigned dstSize, Assembler::Register* dst);

void unsignedShiftRightC(Context* con, unsigned size, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  int sh = getValue(a);
  if (size == 8) {
    if (sh == 32) {
      Assembler::Register high(b->high);
      moveRR(con, 4, &high, 4, t);
      emit(con, li(t->high,0));
    } else if (sh < 32) {
      emit(con, srwi(t->low, b->low, sh));
      emit(con, rlwimi(t->low,b->high,32-sh,0,sh-1));
      emit(con, rlwinm(t->high,b->high,32-sh,sh,31));
    } else {
      emit(con, rlwinm(t->low,b->high,64-sh,sh-32,31));
      emit(con, li(t->high,0));
    }
  } else {
    emit(con, srwi(t->low, b->low, sh));
  }
}

void
updateImmediate(System* s, void* dst, int32_t src, unsigned size, bool address)
{
  switch (size) {
  case 4: {
    int32_t* p = static_cast<int32_t*>(dst);
    int r = (targetV4(p[1]) >> 21) & 31;

    if (address) {
      p[0] = targetV4(lis(r, ha16(src)));
      p[1] |= targetV4(src & 0xFFFF);
    } else {
      p[0] = targetV4(lis(r, src >> 16));
      p[1] = targetV4(ori(r, r, src));
    }
  } break;

  default: abort(s);
  }
}

class ImmediateListener: public Promise::Listener {
 public:
  ImmediateListener(System* s, void* dst, unsigned size, unsigned offset,
                    bool address):
    s(s), dst(dst), size(size), offset(offset), address(address)
  { }

  virtual bool resolve(int64_t value, void** location) {
    updateImmediate(s, dst, value, size, address);
    if (location) *location = static_cast<uint8_t*>(dst) + offset;
    return false;
  }

  System* s;
  void* dst;
  unsigned size;
  unsigned offset;
  bool address;
};

class ImmediateTask: public Task {
 public:
  ImmediateTask(Task* next, Promise* promise, Promise* offset, unsigned size,
                unsigned promiseOffset, bool address):
    Task(next),
    promise(promise),
    offset(offset),
    size(size),
    promiseOffset(promiseOffset),
    address(address)
  { }

  virtual void run(Context* c) {
    if (promise->resolved()) {
      updateImmediate
        (c->s, c->result + offset->value(), promise->value(), size, address);
    } else {
      new (promise->listen(sizeof(ImmediateListener))) ImmediateListener
        (c->s, c->result + offset->value(), size, promiseOffset, address);
    }
  }

  Promise* promise;
  Promise* offset;
  unsigned size;
  unsigned promiseOffset;
  bool address;
};

void
appendImmediateTask(Context* c, Promise* promise, Promise* offset,
                    unsigned size, unsigned promiseOffset, bool address)
{
  c->tasks = new (c->zone->allocate(sizeof(ImmediateTask))) ImmediateTask
    (c->tasks, promise, offset, size, promiseOffset, address);
}

class ConstantPoolEntry: public Promise {
 public:
  ConstantPoolEntry(Context* c, Promise* constant):
    c(c), constant(constant), next(c->constantPool), address(0)
  {
    c->constantPool = this;
    ++ c->constantPoolCount;
  }

  virtual int64_t value() {
    assert(c, resolved());

    return reinterpret_cast<intptr_t>(address);
  }

  virtual bool resolved() {
    return address != 0;
  }

  Context* c;
  Promise* constant;
  ConstantPoolEntry* next;
  void* address;
};

ConstantPoolEntry*
appendConstantPoolEntry(Context* c, Promise* constant)
{
  return new (c->zone->allocate(sizeof(ConstantPoolEntry)))
    ConstantPoolEntry(c, constant);
}

void
jumpR(Context* c, unsigned size UNUSED, Assembler::Register* target)
{
  assert(c, size == TargetBytesPerWord);

  emit(c, mtctr(target->low));
  emit(c, bctr());
}

void
swapRR(Context* c, unsigned aSize, Assembler::Register* a,
       unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == TargetBytesPerWord);
  assert(c, bSize == TargetBytesPerWord);

  Assembler::Register tmp(c->client->acquireTemporary());
  moveRR(c, aSize, a, bSize, &tmp);
  moveRR(c, bSize, b, aSize, a);
  moveRR(c, bSize, &tmp, bSize, b);
  c->client->releaseTemporary(tmp.low);
}

void
moveRR(Context* c, unsigned srcSize, Assembler::Register* src,
       unsigned dstSize, Assembler::Register* dst)
{
  switch (srcSize) {
  case 1:
    emit(c, extsb(dst->low, src->low));
    break;
    
  case 2:
    emit(c, extsh(dst->low, src->low));
    break;
    
  case 4:
  case 8:
    if (srcSize == 4 and dstSize == 8) {
      moveRR(c, 4, src, 4, dst);
      emit(c, srawi(dst->high, src->low, 31));
    } else if (srcSize == 8 and dstSize == 8) {
      Assembler::Register srcHigh(src->high);
      Assembler::Register dstHigh(dst->high);

      if (src->high == dst->low) {
        if (src->low == dst->high) {
          swapRR(c, 4, src, 4, dst);
        } else {
          moveRR(c, 4, &srcHigh, 4, &dstHigh);
          moveRR(c, 4, src, 4, dst);
        }
      } else {
        moveRR(c, 4, src, 4, dst);
        moveRR(c, 4, &srcHigh, 4, &dstHigh);
      }
    } else if (src->low != dst->low) {
      emit(c, mr(dst->low, src->low));
    }
    break;

  default: abort(c);
  }
}

void
moveZRR(Context* c, unsigned srcSize, Assembler::Register* src,
        unsigned, Assembler::Register* dst)
{
  switch (srcSize) {
  case 2:
    emit(c, andi(dst->low, src->low, 0xFFFF));
    break;

  default: abort(c);
  }
}

void
moveCR2(Context* c, unsigned, Assembler::Constant* src,
       unsigned dstSize, Assembler::Register* dst, unsigned promiseOffset)
{
  if (dstSize <= 4) {
    if (src->value->resolved()) {
      int32_t v = src->value->value();
      if (isInt16(v)) {
        emit(c, li(dst->low, v));
      } else {
        emit(c, lis(dst->low, v >> 16));
        emit(c, ori(dst->low, dst->low, v));
      }
    } else {
      appendImmediateTask
        (c, src->value, offset(c), TargetBytesPerWord, promiseOffset, false);
      emit(c, lis(dst->low, 0));
      emit(c, ori(dst->low, dst->low, 0));
    }
  } else {
    abort(c); // todo
  }
}

void
moveCR(Context* c, unsigned srcSize, Assembler::Constant* src,
       unsigned dstSize, Assembler::Register* dst)
{
  moveCR2(c, srcSize, src, dstSize, dst, 0);
}

void addR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  if(size == 8) {
    emit(con, addc(t->low, a->low, b->low));
    emit(con, adde(t->high, a->high, b->high));
  } else {
    emit(con, add(t->low, a->low, b->low));
  }
}

void addC(Context* con, unsigned size, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t) {
  assert(con, size == TargetBytesPerWord);

  int32_t i = getValue(a);
  if(i) {
    emit(con, addi(t->low, b->low, lo16(i)));
    if(not isInt16(i))
      emit(con, addis(t->low, t->low, hi16(i) + carry16(i)));
  } else {
    moveRR(con, size, b, size, t);
  }
}

void subR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  if(size == 8) {
    emit(con, subfc(t->low, a->low, b->low));
    emit(con, subfe(t->high, a->high, b->high));
  } else {
    emit(con, subf(t->low, a->low, b->low));
  }
}

void subC(Context* c, unsigned size, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t) {
  assert(c, size == TargetBytesPerWord);

  ResolvedPromise promise(- a->value->value());
  Assembler::Constant constant(&promise);
  addC(c, size, &constant, b, t);
}

void multiplyR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  if(size == 8) {
    bool useTemporaries = b->low == t->low;
    int tmpLow;
    int tmpHigh;
    if (useTemporaries) {
      tmpLow = con->client->acquireTemporary();
      tmpHigh = con->client->acquireTemporary();
    } else {
      tmpLow = t->low;
      tmpHigh = t->high;
    }

    emit(con, mullw(tmpHigh, a->high, b->low));
    emit(con, mullw(tmpLow, a->low, b->high));
    emit(con, add(t->high, tmpHigh, tmpLow));
    emit(con, mulhwu(tmpLow, a->low, b->low));
    emit(con, add(t->high, t->high, tmpLow));
    emit(con, mullw(t->low, a->low, b->low));

    if (useTemporaries) {
      con->client->releaseTemporary(tmpLow);
      con->client->releaseTemporary(tmpHigh);
    }
  } else {
    emit(con, mullw(t->low, a->low, b->low));
  }
}

void divideR(Context* con, unsigned size UNUSED, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  assert(con, size == 4);
  emit(con, divw(t->low, b->low, a->low));
}

void remainderR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  bool useTemporary = b->low == t->low;
  Assembler::Register tmp(t->low);
  if (useTemporary) {
    tmp.low = con->client->acquireTemporary();
  }

  divideR(con, size, a, b, &tmp);
  multiplyR(con, size, a, &tmp, &tmp);
  subR(con, size, &tmp, b, t);

  if (useTemporary) {
    con->client->releaseTemporary(tmp.low);
  }
}

int
normalize(Context* c, int offset, int index, unsigned scale, 
          bool* preserveIndex, bool* release)
{
  if (offset != 0 or scale != 1) {
    Assembler::Register normalizedIndex
      (*preserveIndex ? c->client->acquireTemporary() : index);
    
    if (*preserveIndex) {
      *release = true;
      *preserveIndex = false;
    } else {
      *release = false;
    }

    int scaled;

    if (scale != 1) {
      Assembler::Register unscaledIndex(index);

      ResolvedPromise scalePromise(log(scale));
      Assembler::Constant scaleConstant(&scalePromise);
      
      shiftLeftC(c, TargetBytesPerWord, &scaleConstant,
                 &unscaledIndex, &normalizedIndex);

      scaled = normalizedIndex.low;
    } else {
      scaled = index;
    }

    if (offset != 0) {
      Assembler::Register untranslatedIndex(scaled);

      ResolvedPromise offsetPromise(offset);
      Assembler::Constant offsetConstant(&offsetPromise);

      addC(c, TargetBytesPerWord, &offsetConstant,
           &untranslatedIndex, &normalizedIndex);
    }

    return normalizedIndex.low;
  } else {
    *release = false;
    return index;
  }
}

void
store(Context* c, unsigned size, Assembler::Register* src,
      int base, int offset, int index, unsigned scale, bool preserveIndex)
{
  if (index != NoRegister) {
    bool release;
    int normalized = normalize
      (c, offset, index, scale, &preserveIndex, &release);

    switch (size) {
    case 1:
      emit(c, stbx(src->low, base, normalized));
      break;

    case 2:
      emit(c, sthx(src->low, base, normalized));
      break;

    case 4:
      emit(c, stwx(src->low, base, normalized));
      break;

    case 8: {
      Assembler::Register srcHigh(src->high);
      store(c, 4, &srcHigh, base, 0, normalized, 1, preserveIndex);
      store(c, 4, src, base, 4, normalized, 1, preserveIndex);
    } break;

    default: abort(c);
    }

    if (release) c->client->releaseTemporary(normalized);
  } else {
    switch (size) {
    case 1:
      emit(c, stb(src->low, base, offset));
      break;

    case 2:
      emit(c, sth(src->low, base, offset));
      break;

    case 4:
      emit(c, stw(src->low, base, offset));
      break;

    case 8: {
      Assembler::Register srcHigh(src->high);
      store(c, 4, &srcHigh, base, offset, NoRegister, 1, false);
      store(c, 4, src, base, offset + 4, NoRegister, 1, false);
    } break;

    default: abort(c);
    }
  }
}

void
moveRM(Context* c, unsigned srcSize, Assembler::Register* src,
       unsigned dstSize UNUSED, Assembler::Memory* dst)
{
  assert(c, srcSize == dstSize);

  store(c, srcSize, src, dst->base, dst->offset, dst->index, dst->scale, true);
}

void
moveAndUpdateRM(Context* c, unsigned srcSize UNUSED, Assembler::Register* src,
                unsigned dstSize UNUSED, Assembler::Memory* dst)
{
  assert(c, srcSize == TargetBytesPerWord);
  assert(c, dstSize == TargetBytesPerWord);

  if (dst->index == NoRegister) {
    emit(c, stwu(src->low, dst->base, dst->offset));
  } else {
    assert(c, dst->offset == 0);
    assert(c, dst->scale == 1);
    
    emit(c, stwux(src->low, dst->base, dst->index));
  }
}

void
load(Context* c, unsigned srcSize, int base, int offset, int index,
     unsigned scale, unsigned dstSize, Assembler::Register* dst,
     bool preserveIndex, bool signExtend)
{
  if (index != NoRegister) {
    bool release;
    int normalized = normalize
      (c, offset, index, scale, &preserveIndex, &release);

    switch (srcSize) {
    case 1:
      emit(c, lbzx(dst->low, base, normalized));
      if (signExtend) {
        emit(c, extsb(dst->low, dst->low));
      }
      break;

    case 2:
      if (signExtend) {
        emit(c, lhax(dst->low, base, normalized));
      } else {
        emit(c, lhzx(dst->low, base, normalized));
      }
      break;

    case 4:
    case 8: {
      if (srcSize == 4 and dstSize == 8) {
        load(c, 4, base, 0, normalized, 1, 4, dst, preserveIndex, false);
        moveRR(c, 4, dst, 8, dst);
      } else if (srcSize == 8 and dstSize == 8) {
        Assembler::Register dstHigh(dst->high);
        load(c, 4, base, 0, normalized, 1, 4, &dstHigh, preserveIndex, false);
        load(c, 4, base, 4, normalized, 1, 4, dst, preserveIndex, false);
      } else {
        emit(c, lwzx(dst->low, base, normalized));
      }
    } break;

    default: abort(c);
    }

    if (release) c->client->releaseTemporary(normalized);
  } else {
    switch (srcSize) {
    case 1:
      emit(c, lbz(dst->low, base, offset));
      if (signExtend) {
        emit(c, extsb(dst->low, dst->low));
      }
      break;

    case 2:
      if (signExtend) {
        emit(c, lha(dst->low, base, offset));
      } else {
        emit(c, lha(dst->low, base, offset));
      }
      break;

    case 4:
      emit(c, lwz(dst->low, base, offset));
      break;

    case 8: {
      if (dstSize == 8) {
        Assembler::Register dstHigh(dst->high);
        load(c, 4, base, offset, NoRegister, 1, 4, &dstHigh, false, false);
        load(c, 4, base, offset + 4, NoRegister, 1, 4, dst, false, false);
      } else {
        emit(c, lwzx(dst->low, base, offset));
      }
    } break;

    default: abort(c);
    }
  }
}

void
moveMR(Context* c, unsigned srcSize, Assembler::Memory* src,
       unsigned dstSize, Assembler::Register* dst)
{
  load(c, srcSize, src->base, src->offset, src->index, src->scale,
       dstSize, dst, true, true);
}

void
moveZMR(Context* c, unsigned srcSize, Assembler::Memory* src,
        unsigned dstSize, Assembler::Register* dst)
{
  load(c, srcSize, src->base, src->offset, src->index, src->scale,
       dstSize, dst, true, false);
}

void
andR(Context* c, unsigned size, Assembler::Register* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  if (size == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);
    
    andR(c, 4, a, b, dst);
    andR(c, 4, &ah, &bh, &dh);
  } else {
    emit(c, and_(dst->low, a->low, b->low));
  }
}

void
andC(Context* c, unsigned size, Assembler::Constant* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  int64_t v = a->value->value();

  if (size == 8) {
    ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
    Assembler::Constant ah(&high);

    ResolvedPromise low(v & 0xFFFFFFFF);
    Assembler::Constant al(&low);

    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);

    andC(c, 4, &al, b, dst);
    andC(c, 4, &ah, &bh, &dh);
  } else {
    // bitmasks of the form regex 0*1*0* can be handled in a single
    // rlwinm instruction, hence the following:

    uint32_t v32 = static_cast<uint32_t>(v);
    unsigned state = 0;
    unsigned start = 0;
    unsigned end = 31;
    for (unsigned i = 0; i < 32; ++i) {
      unsigned bit = (v32 >> i) & 1;
      switch (state) {
      case 0:
        if (bit) {
          start = i;
          state = 1;
        }
        break;

      case 1:
        if (bit == 0) {
          end = i - 1;
          state = 2;
        }
        break;

      case 2:
        if (bit) {
          // not in 0*1*0* form.  We can only use andi(s) if either
          // the topmost or bottommost 16 bits are zero.

          if ((v32 >> 16) == 0) {
            emit(c, andi(dst->low, b->low, v32));
          } else if ((v32 & 0xFFFF) == 0) {
            emit(c, andis(dst->low, b->low, v32 >> 16));
          } else {
            bool useTemporary = b->low == dst->low;
            Assembler::Register tmp(dst->low);
            if (useTemporary) {
              tmp.low = c->client->acquireTemporary();
            }

            moveCR(c, 4, a, 4, &tmp);
            andR(c, 4, b, &tmp, dst);

            if (useTemporary) {
              c->client->releaseTemporary(tmp.low);
            }
          }
          return;
        }
        break;
      }
    }

    if (state) {
      if (start != 0 or end != 31) {
        emit(c, rlwinm(dst->low, b->low, 0, 31 - end, 31 - start));
      } else {
        moveRR(c, 4, b, 4, dst);
      }
    } else {
      emit(c, li(dst->low, 0));
    }
  }
}

void
orR(Context* c, unsigned size, Assembler::Register* a,
    Assembler::Register* b, Assembler::Register* dst)
{
  if (size == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);
    
    orR(c, 4, a, b, dst);
    orR(c, 4, &ah, &bh, &dh);
  } else {
    emit(c, or_(dst->low, a->low, b->low));
  }
}

void
orC(Context* c, unsigned size, Assembler::Constant* a,
    Assembler::Register* b, Assembler::Register* dst)
{
  int64_t v = a->value->value();

  if (size == 8) {
    ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
    Assembler::Constant ah(&high);

    ResolvedPromise low(v & 0xFFFFFFFF);
    Assembler::Constant al(&low);

    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);

    orC(c, 4, &al, b, dst);
    orC(c, 4, &ah, &bh, &dh);
  } else {
    emit(c, ori(b->low, dst->low, v));
    if (v >> 16) {
      emit(c, oris(dst->low, dst->low, v >> 16));
    }
  }
}

void
xorR(Context* c, unsigned size, Assembler::Register* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  if (size == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);
    
    xorR(c, 4, a, b, dst);
    xorR(c, 4, &ah, &bh, &dh);
  } else {
    emit(c, xor_(dst->low, a->low, b->low));
  }
}

void
xorC(Context* c, unsigned size, Assembler::Constant* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  uint64_t v = a->value->value();

  if (size == 8) {
    ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
    Assembler::Constant ah(&high);

    ResolvedPromise low(v & 0xFFFFFFFF);
    Assembler::Constant al(&low);

    Assembler::Register bh(b->high);
    Assembler::Register dh(dst->high);

    xorC(c, 4, &al, b, dst);
    xorC(c, 4, &ah, &bh, &dh);
  } else {
    if (v >> 16) {
      emit(c, xoris(b->low, dst->low, v >> 16));
      emit(c, xori(dst->low, dst->low, v));
    } else {
      emit(c, xori(b->low, dst->low, v));
    }
  }
}

void
moveAR2(Context* c, unsigned srcSize UNUSED, Assembler::Address* src,
        unsigned dstSize, Assembler::Register* dst, unsigned promiseOffset)
{
  assert(c, srcSize == 4 and dstSize == 4);

  Assembler::Memory memory(dst->low, 0, -1, 0);
  
  appendImmediateTask
    (c, src->address, offset(c), TargetBytesPerWord, promiseOffset, true);
  
  emit(c, lis(dst->low, 0));
  moveMR(c, dstSize, &memory, dstSize, dst);
}

void
moveAR(Context* c, unsigned srcSize, Assembler::Address* src,
       unsigned dstSize, Assembler::Register* dst)
{
  moveAR2(c, srcSize, src, dstSize, dst, 0);
}

void
compareRR(Context* c, unsigned aSize UNUSED, Assembler::Register* a,
          unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);
  
  emit(c, cmpw(b->low, a->low));
}

void
compareCR(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);

  if (a->value->resolved() and isInt16(a->value->value())) {
    emit(c, cmpwi(b->low, a->value->value()));
  } else {
    Assembler::Register tmp(c->client->acquireTemporary());
    moveCR(c, aSize, a, bSize, &tmp);
    compareRR(c, bSize, &tmp, bSize, b);
    c->client->releaseTemporary(tmp.low);
  }
}

void
compareCM(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Memory* b)
{
  assert(c, aSize == 4 and bSize == 4);

  Assembler::Register tmp(c->client->acquireTemporary());
  moveMR(c, bSize, b, bSize, &tmp);
  compareCR(c, aSize, a, bSize, &tmp);
  c->client->releaseTemporary(tmp.low);
}

void
compareRM(Context* c, unsigned aSize, Assembler::Register* a,
          unsigned bSize, Assembler::Memory* b)
{
  assert(c, aSize == 4 and bSize == 4);

  Assembler::Register tmp(c->client->acquireTemporary());
  moveMR(c, bSize, b, bSize, &tmp);
  compareRR(c, aSize, a, bSize, &tmp);
  c->client->releaseTemporary(tmp.low);
}

void
compareUnsignedRR(Context* c, unsigned aSize UNUSED, Assembler::Register* a,
                  unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);
  
  emit(c, cmplw(b->low, a->low));
}

void
compareUnsignedCR(Context* c, unsigned aSize, Assembler::Constant* a,
                  unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);

  if (a->value->resolved() and (a->value->value() >> 16) == 0) {
    emit(c, cmplwi(b->low, a->value->value()));
  } else {
    Assembler::Register tmp(c->client->acquireTemporary());
    moveCR(c, aSize, a, bSize, &tmp);
    compareUnsignedRR(c, bSize, &tmp, bSize, b);
    c->client->releaseTemporary(tmp.low);
  }
}

int32_t
branch(Context* c, TernaryOperation op)
{
  switch (op) {
  case JumpIfEqual:
    return beq(0);
    
  case JumpIfNotEqual:
    return bne(0);
    
  case JumpIfLess:
    return blt(0);
    
  case JumpIfGreater:
    return bgt(0);
    
  case JumpIfLessOrEqual:
    return ble(0);
    
  case JumpIfGreaterOrEqual:
    return bge(0);
    
  default:
    abort(c);
  }
}

void
conditional(Context* c, int32_t branch, Assembler::Constant* target)
{
  appendOffsetTask(c, target->value, offset(c), true);
  emit(c, branch);
}

void
branch(Context* c, TernaryOperation op, Assembler::Constant* target)
{
  conditional(c, branch(c, op), target);
}

void
branchLong(Context* c, TernaryOperation op, Assembler::Operand* al,
           Assembler::Operand* ah, Assembler::Operand* bl,
           Assembler::Operand* bh, Assembler::Constant* target,
           BinaryOperationType compareSigned,
           BinaryOperationType compareUnsigned)
{
  compareSigned(c, 4, ah, 4, bh);

  unsigned next = 0;
  
  switch (op) {
  case JumpIfEqual:
    next = c->code.length();
    emit(c, bne(0));

    compareSigned(c, 4, al, 4, bl);
    conditional(c, beq(0), target);
    break;

  case JumpIfNotEqual:
    conditional(c, bne(0), target);

    compareSigned(c, 4, al, 4, bl);
    conditional(c, bne(0), target);
    break;

  case JumpIfLess:
    conditional(c, blt(0), target);

    next = c->code.length();
    emit(c, bgt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, blt(0), target);
    break;

  case JumpIfGreater:
    conditional(c, bgt(0), target);

    next = c->code.length();
    emit(c, blt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, bgt(0), target);
    break;

  case JumpIfLessOrEqual:
    conditional(c, blt(0), target);

    next = c->code.length();
    emit(c, bgt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, ble(0), target);
    break;

  case JumpIfGreaterOrEqual:
    conditional(c, bgt(0), target);

    next = c->code.length();
    emit(c, blt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, bge(0), target);
    break;

  default:
    abort(c);
  }

  if (next) {
    updateOffset
      (c->s, c->code.data + next, true, reinterpret_cast<intptr_t>
       (c->code.data + c->code.length()), 0);
  }
}

void
branchRR(Context* c, TernaryOperation op, unsigned size,
         Assembler::Register* a, Assembler::Register* b,
         Assembler::Constant* target)
{
  if (size > TargetBytesPerWord) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    branchLong(c, op, a, &ah, b, &bh, target, CAST2(compareRR),
               CAST2(compareUnsignedRR));
  } else {
    compareRR(c, size, a, size, b);
    branch(c, op, target);
  }
}

void
branchCR(Context* c, TernaryOperation op, unsigned size,
         Assembler::Constant* a, Assembler::Register* b,
         Assembler::Constant* target)
{
  if (size > TargetBytesPerWord) {
    int64_t v = a->value->value();

    ResolvedPromise low(v & ~static_cast<target_uintptr_t>(0));
    Assembler::Constant al(&low);

    ResolvedPromise high((v >> 32) & ~static_cast<target_uintptr_t>(0));
    Assembler::Constant ah(&high);

    Assembler::Register bh(b->high);

    branchLong(c, op, &al, &ah, b, &bh, target, CAST2(compareCR),
               CAST2(compareUnsignedCR));
  } else {
    compareCR(c, size, a, size, b);
    branch(c, op, target);
  }
}

void
branchRM(Context* c, TernaryOperation op, unsigned size,
         Assembler::Register* a, Assembler::Memory* b,
         Assembler::Constant* target)
{
  assert(c, size <= TargetBytesPerWord);

  compareRM(c, size, a, size, b);
  branch(c, op, target);
}

void
branchCM(Context* c, TernaryOperation op, unsigned size,
         Assembler::Constant* a, Assembler::Memory* b,
         Assembler::Constant* target)
{
  assert(c, size <= TargetBytesPerWord);

  compareCM(c, size, a, size, b);
  branch(c, op, target);
}

ShiftMaskPromise*
shiftMaskPromise(Context* c, Promise* base, unsigned shift, int64_t mask)
{
  return new (c->zone->allocate(sizeof(ShiftMaskPromise)))
    ShiftMaskPromise(base, shift, mask);
}

void
moveCM(Context* c, unsigned srcSize, Assembler::Constant* src,
       unsigned dstSize, Assembler::Memory* dst)
{
  switch (dstSize) {
  case 8: {
    Assembler::Constant srcHigh
      (shiftMaskPromise(c, src->value, 32, 0xFFFFFFFF));
    Assembler::Constant srcLow
      (shiftMaskPromise(c, src->value, 0, 0xFFFFFFFF));
    
    Assembler::Memory dstLow
      (dst->base, dst->offset + 4, dst->index, dst->scale);
    
    moveCM(c, 4, &srcLow, 4, &dstLow);
    moveCM(c, 4, &srcHigh, 4, dst);
  } break;

  default:
    Assembler::Register tmp(c->client->acquireTemporary());
    moveCR(c, srcSize, src, dstSize, &tmp);
    moveRM(c, dstSize, &tmp, dstSize, dst);
    c->client->releaseTemporary(tmp.low);
  }
}

void
negateRR(Context* c, unsigned srcSize, Assembler::Register* src,
         unsigned dstSize UNUSED, Assembler::Register* dst)
{
  assert(c, srcSize == dstSize);

  if (srcSize == 8) {
    Assembler::Register dstHigh(dst->high);

    emit(c, subfic(dst->low, src->low, 0));
    emit(c, subfze(dst->high, src->high));
  } else {
    emit(c, neg(dst->low, src->low));
  }
}

void
callR(Context* c, unsigned size UNUSED, Assembler::Register* target)
{
  assert(c, size == TargetBytesPerWord);

  emit(c, mtctr(target->low));
  emit(c, bctrl());
}

void
callC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  appendOffsetTask(c, target->value, offset(c), false);
  emit(c, bl(0));
}

void
longCallC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(0);
  moveCR2(c, TargetBytesPerWord, target, TargetBytesPerWord, &tmp, 12);
  callR(c, TargetBytesPerWord, &tmp);
}

void
alignedLongCallC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(c->client->acquireTemporary());
  Assembler::Address address(appendConstantPoolEntry(c, target->value));
  moveAR2(c, TargetBytesPerWord, &address, TargetBytesPerWord, &tmp, 12);
  callR(c, TargetBytesPerWord, &tmp);
  c->client->releaseTemporary(tmp.low);
}

void
longJumpC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(0);
  moveCR2(c, TargetBytesPerWord, target, TargetBytesPerWord, &tmp, 12);
  jumpR(c, TargetBytesPerWord, &tmp);
}

void
alignedLongJumpC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(c->client->acquireTemporary());
  Assembler::Address address(appendConstantPoolEntry(c, target->value));
  moveAR2(c, TargetBytesPerWord, &address, TargetBytesPerWord, &tmp, 12);
  jumpR(c, TargetBytesPerWord, &tmp);
  c->client->releaseTemporary(tmp.low);
}

void
jumpC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  appendOffsetTask(c, target->value, offset(c), false);
  emit(c, b(0));
}

void
return_(Context* c)
{
  emit(c, blr());
}

void
memoryBarrier(Context* c)
{
  emit(c, sync(0));
}

// END OPERATION COMPILERS

unsigned
argumentFootprint(unsigned footprint)
{
  return max(pad(footprint, StackAlignmentInWords), StackAlignmentInWords);
}

void
nextFrame(ArchitectureContext* c UNUSED, int32_t* start, unsigned size,
          unsigned footprint, void* link, void*,
          unsigned targetParameterFootprint, void** ip, void** stack)
{
  assert(c, *ip >= start);
  assert(c, *ip <= start + (size / BytesPerWord));

  int32_t* instruction = static_cast<int32_t*>(*ip);

  if ((*start >> 26) == 32) {
    // skip stack overflow check
    start += 3;
  }

  if (instruction <= start + 2
      or *instruction == lwz(0, 1, 8)
      or *instruction == mtlr(0)
      or *instruction == blr())
  {
    *ip = link;
    return;
  }

  unsigned offset = footprint;

  if (TailCalls) {
    if (argumentFootprint(targetParameterFootprint) > StackAlignmentInWords) {
      offset += argumentFootprint(targetParameterFootprint)
        - StackAlignmentInWords;
    }

    // check for post-non-tail-call stack adjustment of the form "lwzx
    // r0,0(r1); stwu r0,offset(r1)":
    if (instruction < start + (size / BytesPerWord) - 1
        and (static_cast<uint32_t>(instruction[1]) >> 16) == 0x9401)
    {
      offset += static_cast<int16_t>(instruction[1]) / BytesPerWord;
    } else if ((static_cast<uint32_t>(*instruction) >> 16) == 0x9401) {
      offset += static_cast<int16_t>(*instruction) / BytesPerWord;
    }

    // todo: check for and handle tail calls
  }

  *ip = static_cast<void**>(*stack)[offset + ReturnAddressOffset];
  *stack = static_cast<void**>(*stack) + offset;
}

void
populateTables(ArchitectureContext* c)
{
  const OperandType C = ConstantOperand;
  const OperandType A = AddressOperand;
  const OperandType R = RegisterOperand;
  const OperandType M = MemoryOperand;

  OperationType* zo = c->operations;
  UnaryOperationType* uo = c->unaryOperations;
  BinaryOperationType* bo = c->binaryOperations;
  TernaryOperationType* to = c->ternaryOperations;
  BranchOperationType* bro = c->branchOperations;

  zo[Return] = return_;
  zo[LoadBarrier] = memoryBarrier;
  zo[StoreStoreBarrier] = memoryBarrier;
  zo[StoreLoadBarrier] = memoryBarrier;

  uo[index(c, LongCall, C)] = CAST1(longCallC);

  uo[index(c, AlignedLongCall, C)] = CAST1(alignedLongCallC);

  uo[index(c, LongJump, C)] = CAST1(longJumpC);

  uo[index(c, AlignedLongJump, C)] = CAST1(alignedLongJumpC);

  uo[index(c, Jump, R)] = CAST1(jumpR);
  uo[index(c, Jump, C)] = CAST1(jumpC);

  uo[index(c, AlignedJump, R)] = CAST1(jumpR);
  uo[index(c, AlignedJump, C)] = CAST1(jumpC);

  uo[index(c, Call, C)] = CAST1(callC);
  uo[index(c, Call, R)] = CAST1(callR);

  uo[index(c, AlignedCall, C)] = CAST1(callC);
  uo[index(c, AlignedCall, R)] = CAST1(callR);

  bo[index(c, Move, R, R)] = CAST2(moveRR);
  bo[index(c, Move, C, R)] = CAST2(moveCR);
  bo[index(c, Move, C, M)] = CAST2(moveCM);
  bo[index(c, Move, M, R)] = CAST2(moveMR);
  bo[index(c, Move, R, M)] = CAST2(moveRM);
  bo[index(c, Move, A, R)] = CAST2(moveAR);

  bo[index(c, MoveZ, R, R)] = CAST2(moveZRR);
  bo[index(c, MoveZ, M, R)] = CAST2(moveZMR);
  bo[index(c, MoveZ, C, R)] = CAST2(moveCR);

  bo[index(c, Negate, R, R)] = CAST2(negateRR);

  to[index(c, Add, R)] = CAST3(addR);
  to[index(c, Add, C)] = CAST3(addC);

  to[index(c, Subtract, R)] = CAST3(subR);
  to[index(c, Subtract, C)] = CAST3(subC);

  to[index(c, Multiply, R)] = CAST3(multiplyR);

  to[index(c, Divide, R)] = CAST3(divideR);

  to[index(c, Remainder, R)] = CAST3(remainderR);

  to[index(c, ShiftLeft, R)] = CAST3(shiftLeftR);
  to[index(c, ShiftLeft, C)] = CAST3(shiftLeftC);

  to[index(c, ShiftRight, R)] = CAST3(shiftRightR);
  to[index(c, ShiftRight, C)] = CAST3(shiftRightC);

  to[index(c, UnsignedShiftRight, R)] = CAST3(unsignedShiftRightR);
  to[index(c, UnsignedShiftRight, C)] = CAST3(unsignedShiftRightC);

  to[index(c, And, C)] = CAST3(andC);
  to[index(c, And, R)] = CAST3(andR);

  to[index(c, Or, C)] = CAST3(orC);
  to[index(c, Or, R)] = CAST3(orR);

  to[index(c, Xor, C)] = CAST3(xorC);
  to[index(c, Xor, R)] = CAST3(xorR);

  bro[branchIndex(c, R, R)] = CAST_BRANCH(branchRR);
  bro[branchIndex(c, C, R)] = CAST_BRANCH(branchCR);
  bro[branchIndex(c, C, M)] = CAST_BRANCH(branchCM);
  bro[branchIndex(c, R, M)] = CAST_BRANCH(branchRM);
}

class MyArchitecture: public Assembler::Architecture {
 public:
  MyArchitecture(System* system): c(system), referenceCount(0) {
    populateTables(&c);
  }

  virtual unsigned floatRegisterSize() {
    return 0;
  }

  virtual uint32_t generalRegisterMask() {
    return 0xFFFFFFFF;
  }

  virtual uint32_t floatRegisterMask() {
    return 0;
  }

  virtual int scratch() {
    return 31;
  }

  virtual int stack() {
    return StackRegister;
  }

  virtual int thread() {
    return ThreadRegister;
  }

  virtual int returnLow() {
    return 4;
  }

  virtual int returnHigh() {
    return (TargetBytesPerWord == 4 ? 3 : NoRegister);
  }

  virtual int virtualCallTarget() {
    return 4;
  }

  virtual int virtualCallIndex() {
    return 3;
  }

  virtual bool bigEndian() {
    return true;
  }

  virtual uintptr_t maximumImmediateJump() {
    return 0x1FFFFFF;
  }

  virtual bool reserved(int register_) {
    switch (register_) {
    case 0: // r0 has special meaning in addi and other instructions
    case StackRegister:
    case ThreadRegister:
#ifndef __APPLE__
      // r2 is reserved for system uses on SYSV
    case 2:
#endif
      return true;

    default:
      return false;
    }
  }

  virtual unsigned frameFootprint(unsigned footprint) {
    return max(footprint, StackAlignmentInWords);
  }

  virtual unsigned argumentFootprint(unsigned footprint) {
    return ::argumentFootprint(footprint);
  }

  virtual bool argumentAlignment() {
    return AlignArguments;
  }

  virtual bool argumentRegisterAlignment() {
    return true;
  }

  virtual unsigned argumentRegisterCount() {
    return 8;
  }

  virtual int argumentRegister(unsigned index) {
    assert(&c, index < argumentRegisterCount());

    return index + 3;
  }

  virtual bool hasLinkRegister() {
    return true;
  }
  
  virtual unsigned stackAlignmentInWords() {
    return StackAlignmentInWords;
  }

  virtual bool matchCall(void* returnAddress, void* target) {
    uint32_t* instruction = static_cast<uint32_t*>(returnAddress) - 1;

    return *instruction == static_cast<uint32_t>
      (bl(static_cast<uint8_t*>(target)
          - reinterpret_cast<uint8_t*>(instruction)));
  }

  virtual void updateCall(UnaryOperation op UNUSED,
                          void* returnAddress,
                          void* newTarget)
  {
    switch (op) {
    case Call:
    case Jump:
    case AlignedCall:
    case AlignedJump: {
      updateOffset(c.s, static_cast<uint8_t*>(returnAddress) - 4, false,
                   reinterpret_cast<intptr_t>(newTarget), 0);
    } break;

    case LongCall:
    case LongJump: {
      updateImmediate
        (c.s, static_cast<uint8_t*>(returnAddress) - 12,
         reinterpret_cast<intptr_t>(newTarget), TargetBytesPerWord, false);
    } break;

    case AlignedLongCall:
    case AlignedLongJump: {
      uint32_t* p = static_cast<uint32_t*>(returnAddress) - 4;
      *reinterpret_cast<void**>(unha16(p[0] & 0xFFFF, p[1] & 0xFFFF))
        = newTarget;
    } break;

    default: abort(&c);
    }
  }

  virtual unsigned constantCallSize() {
    return 4;
  }

  virtual void setConstant(void* dst, uint64_t constant) {
    updateImmediate(c.s, dst, constant, TargetBytesPerWord, false);
  }

  virtual unsigned alignFrameSize(unsigned sizeInWords) {
    const unsigned alignment = StackAlignmentInWords;
    return (ceiling(sizeInWords + FrameFooterSize, alignment) * alignment);
  }

  virtual void nextFrame(void* start, unsigned size, unsigned footprint,
                         void* link, void* stackLimit,
                         unsigned targetParameterFootprint, void** ip,
                         void** stack)
  {
    ::nextFrame(&c, static_cast<int32_t*>(start), size, footprint, link,
                stackLimit, targetParameterFootprint, ip, stack);
  }

  virtual void* frameIp(void* stack) {
    return stack ? static_cast<void**>(stack)[ReturnAddressOffset] : 0;
  }

  virtual unsigned frameHeaderSize() {
    return 0;
  }

  virtual unsigned frameReturnAddressSize() {
    return 0;
  }

  virtual unsigned frameFooterSize() {
    return FrameFooterSize;
  }

  virtual int returnAddressOffset() {
    return ReturnAddressOffset;
  }

  virtual int framePointerOffset() {
    return 0;
  }

  virtual BinaryOperation hasBinaryIntrinsic(Thread*, object) {
  	return NoBinaryOperation;
  }
  
  virtual TernaryOperation hasTernaryIntrinsic(Thread*, object) {
  	return NoTernaryOperation;
  }
  
  virtual bool alwaysCondensed(BinaryOperation) {
    return false;
  }
  
  virtual bool alwaysCondensed(TernaryOperation) {
    return false;
  }
  
  virtual void plan
  (UnaryOperation,
   unsigned, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   bool* thunk)
  {
    *aTypeMask = (1 << RegisterOperand) | (1 << ConstantOperand);
    *aRegisterMask = ~static_cast<uint64_t>(0);
    *thunk = false;
  }

  virtual void planSource
  (BinaryOperation op,
   unsigned, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   unsigned, bool* thunk)
  {
    *aTypeMask = ~0;
    *aRegisterMask = ~static_cast<uint64_t>(0);

    *thunk = false;

    switch (op) {
    case Negate:
      *aTypeMask = (1 << RegisterOperand);
      break;

    case Absolute:
    case FloatAbsolute:
    case FloatSquareRoot:
    case FloatNegate:
    case Float2Float:
    case Float2Int:
    case Int2Float:
      *thunk = true;
      break;

    default:
      break;
    }
  }
  
  virtual void planDestination
  (BinaryOperation op,
   unsigned, uint8_t, uint64_t,
   unsigned, uint8_t* bTypeMask, uint64_t* bRegisterMask)
  {
    *bTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
    *bRegisterMask = ~static_cast<uint64_t>(0);

    switch (op) {
    case Negate:
      *bTypeMask = (1 << RegisterOperand);
      break;

    default:
      break;
    }
  }

  virtual void planMove
  (unsigned, uint8_t* srcTypeMask, uint64_t* srcRegisterMask,
   uint8_t* tmpTypeMask, uint64_t* tmpRegisterMask,
   uint8_t dstTypeMask, uint64_t)
  {
    *srcTypeMask = ~0;
    *srcRegisterMask = ~static_cast<uint64_t>(0);

    *tmpTypeMask = 0;
    *tmpRegisterMask = 0;

    if (dstTypeMask & (1 << MemoryOperand)) {
      // can't move directly from memory or constant to memory
      *srcTypeMask = 1 << RegisterOperand;
      *tmpTypeMask = 1 << RegisterOperand;
      *tmpRegisterMask = ~static_cast<uint64_t>(0);
    }
  }

  virtual void planSource
  (TernaryOperation op,
   unsigned aSize, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   unsigned, uint8_t* bTypeMask, uint64_t* bRegisterMask,
   unsigned, bool* thunk)
  {
    *aTypeMask = (1 << RegisterOperand) | (1 << ConstantOperand);
    *aRegisterMask = ~static_cast<uint64_t>(0);

    *bTypeMask = (1 << RegisterOperand);
    *bRegisterMask = ~static_cast<uint64_t>(0);

    *thunk = false;

    switch (op) {
    case Add:
    case Subtract:
      if (aSize == 8) {
        *aTypeMask = *bTypeMask = (1 << RegisterOperand);
      }
      break;

    case Multiply:
      *aTypeMask = *bTypeMask = (1 << RegisterOperand);
      break;

    case Divide:
    case Remainder:
      // todo: we shouldn't need to defer to thunks for integers which
      // are smaller than or equal to tne native word size, but
      // PowerPC doesn't generate traps for divide by zero, so we'd
      // need to do the checks ourselves.  Using an inline check
      // should be faster than calling an out-of-line thunk, but the
      // thunk is easier, so they's what we do for now.
      if (true) {//if (TargetBytesPerWord == 4 and aSize == 8) {
        *thunk = true;        
      } else {
        *aTypeMask = (1 << RegisterOperand);
      }
      break;

    case FloatAdd:
    case FloatSubtract:
    case FloatMultiply:
    case FloatDivide:
    case FloatRemainder:
    case JumpIfFloatEqual:
    case JumpIfFloatNotEqual:
    case JumpIfFloatLess:
    case JumpIfFloatGreater:
    case JumpIfFloatLessOrEqual:
    case JumpIfFloatGreaterOrEqual:
    case JumpIfFloatLessOrUnordered:
    case JumpIfFloatGreaterOrUnordered:
    case JumpIfFloatLessOrEqualOrUnordered:
    case JumpIfFloatGreaterOrEqualOrUnordered:
      *thunk = true;
      break;

    default:
      break;
    }
  }

  virtual void planDestination
  (TernaryOperation op,
   unsigned, uint8_t, uint64_t,
   unsigned, uint8_t, const uint64_t,
   unsigned, uint8_t* cTypeMask, uint64_t* cRegisterMask)
  {
    if (isBranch(op)) {
      *cTypeMask = (1 << ConstantOperand);
      *cRegisterMask = 0;
    } else {
      *cTypeMask = (1 << RegisterOperand);
      *cRegisterMask = ~static_cast<uint64_t>(0);
    }
  }

  virtual void acquire() {
    ++ referenceCount;
  }

  virtual void release() {
    if (-- referenceCount == 0) {
      c.s->free(this);
    }
  }

  ArchitectureContext c;
  unsigned referenceCount;
};

class MyAssembler: public Assembler {
 public:
  MyAssembler(System* s, Allocator* a, Zone* zone, MyArchitecture* arch):
    c(s, a, zone), arch_(arch)
  { }

  virtual void setClient(Client* client) {
    assert(&c, c.client == 0);
    c.client = client;
  }

  virtual Architecture* arch() {
    return arch_;
  }

  virtual void checkStackOverflow(uintptr_t handler,
                                  unsigned stackLimitOffsetFromThread)
  {
    Register stack(StackRegister);
    Memory stackLimit(ThreadRegister, stackLimitOffsetFromThread);
    Constant handlerConstant
      (new (c.zone->allocate(sizeof(ResolvedPromise)))
       ResolvedPromise(handler));
    branchRM(&c, JumpIfGreaterOrEqual, TargetBytesPerWord, &stack, &stackLimit,
             &handlerConstant);
  }

  virtual void saveFrame(unsigned stackOffset, unsigned) {
    Register returnAddress(0);
    emit(&c, mflr(returnAddress.low));

    Memory returnAddressDst
      (StackRegister, ReturnAddressOffset * TargetBytesPerWord);
    moveRM(&c, TargetBytesPerWord, &returnAddress, TargetBytesPerWord,
           &returnAddressDst);

    Register stack(StackRegister);
    Memory stackDst(ThreadRegister, stackOffset);
    moveRM(&c, TargetBytesPerWord, &stack, TargetBytesPerWord, &stackDst);
  }

  virtual void pushFrame(unsigned argumentCount, ...) {
    struct {
      unsigned size;
      OperandType type;
      Operand* operand;
    } arguments[argumentCount];

    va_list a; va_start(a, argumentCount);
    unsigned footprint = 0;
    for (unsigned i = 0; i < argumentCount; ++i) {
      arguments[i].size = va_arg(a, unsigned);
      arguments[i].type = static_cast<OperandType>(va_arg(a, int));
      arguments[i].operand = va_arg(a, Operand*);
      footprint += ceiling(arguments[i].size, TargetBytesPerWord);
    }
    va_end(a);

    allocateFrame(arch_->alignFrameSize(footprint));
    
    unsigned offset = 0;
    for (unsigned i = 0; i < argumentCount; ++i) {
      if (i < arch_->argumentRegisterCount()) {
        Register dst(arch_->argumentRegister(i));

        apply(Move,
              arguments[i].size, arguments[i].type, arguments[i].operand,
              pad(arguments[i].size, TargetBytesPerWord), RegisterOperand,
              &dst);

        offset += ceiling(arguments[i].size, TargetBytesPerWord);
      } else {
        Memory dst
          (ThreadRegister, (offset + FrameFooterSize) * TargetBytesPerWord);

        apply(Move,
              arguments[i].size, arguments[i].type, arguments[i].operand,
              pad(arguments[i].size, TargetBytesPerWord), MemoryOperand, &dst);

        offset += ceiling(arguments[i].size, TargetBytesPerWord);
      }
    }
  }

  virtual void allocateFrame(unsigned footprint) {
    Register returnAddress(0);
    emit(&c, mflr(returnAddress.low));

    Memory returnAddressDst
      (StackRegister, ReturnAddressOffset * TargetBytesPerWord);
    moveRM(&c, TargetBytesPerWord, &returnAddress, TargetBytesPerWord,
           &returnAddressDst);

    Register stack(StackRegister);
    Memory stackDst(StackRegister, -footprint * TargetBytesPerWord);
    moveAndUpdateRM
      (&c, TargetBytesPerWord, &stack, TargetBytesPerWord, &stackDst);
  }

  virtual void adjustFrame(unsigned difference) {
    Register nextStack(0);
    Memory stackSrc(StackRegister, 0);
    moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &nextStack);

    Memory stackDst(StackRegister, -difference * TargetBytesPerWord);
    moveAndUpdateRM
      (&c, TargetBytesPerWord, &nextStack, TargetBytesPerWord, &stackDst);
  }

  virtual void popFrame(unsigned) {
    Register stack(StackRegister);
    Memory stackSrc(StackRegister, 0);
    moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &stack);

    Register returnAddress(0);
    Memory returnAddressSrc
      (StackRegister, ReturnAddressOffset * TargetBytesPerWord);
    moveMR(&c, TargetBytesPerWord, &returnAddressSrc, TargetBytesPerWord,
           &returnAddress);
    
    emit(&c, mtlr(returnAddress.low));
  }

  virtual void popFrameForTailCall(unsigned footprint,
                                   int offset,
                                   int returnAddressSurrogate,
                                   int framePointerSurrogate)
  {
    if (TailCalls) {
      if (offset) {
        Register tmp(0);
        Memory returnAddressSrc
          (StackRegister, (ReturnAddressOffset + footprint)
           * TargetBytesPerWord);
        moveMR(&c, TargetBytesPerWord, &returnAddressSrc, TargetBytesPerWord,
               &tmp);
    
        emit(&c, mtlr(tmp.low));

        Memory stackSrc(StackRegister, footprint * TargetBytesPerWord);
        moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &tmp);

        Memory stackDst
          (StackRegister, (footprint - offset) * TargetBytesPerWord);
        moveAndUpdateRM
          (&c, TargetBytesPerWord, &tmp, TargetBytesPerWord, &stackDst);

        if (returnAddressSurrogate != NoRegister) {
          assert(&c, offset > 0);

          Register ras(returnAddressSurrogate);
          Memory dst
            (StackRegister, (ReturnAddressOffset + offset)
             * TargetBytesPerWord);
          moveRM(&c, TargetBytesPerWord, &ras, TargetBytesPerWord, &dst);
        }

        if (framePointerSurrogate != NoRegister) {
          assert(&c, offset > 0);

          Register fps(framePointerSurrogate);
          Memory dst(StackRegister, offset * TargetBytesPerWord);
          moveRM(&c, TargetBytesPerWord, &fps, TargetBytesPerWord, &dst);
        }
      } else {
        popFrame(footprint);
      }
    } else {
      abort(&c);
    }
  }

  virtual void popFrameAndPopArgumentsAndReturn(unsigned frameFootprint,
                                                unsigned argumentFootprint)
  {
    popFrame(frameFootprint);

    assert(&c, argumentFootprint >= StackAlignmentInWords);
    assert(&c, (argumentFootprint % StackAlignmentInWords) == 0);

    if (TailCalls and argumentFootprint > StackAlignmentInWords) {
      Register tmp(0);
      Memory stackSrc(StackRegister, 0);
      moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &tmp);

      Memory stackDst(StackRegister,
                      (argumentFootprint - StackAlignmentInWords)
                      * TargetBytesPerWord);
      moveAndUpdateRM
        (&c, TargetBytesPerWord, &tmp, TargetBytesPerWord, &stackDst);
    }

    return_(&c);
  }

  virtual void popFrameAndUpdateStackAndReturn(unsigned frameFootprint,
                                               unsigned stackOffsetFromThread)
  {
    popFrame(frameFootprint);

    Register tmp1(0);
    Memory stackSrc(StackRegister, 0);
    moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &tmp1);

    Register tmp2(5);
    Memory newStackSrc(ThreadRegister, stackOffsetFromThread);
    moveMR(&c, TargetBytesPerWord, &newStackSrc, TargetBytesPerWord, &tmp2);

    Register stack(StackRegister);
    subR(&c, TargetBytesPerWord, &stack, &tmp2, &tmp2);

    Memory stackDst(StackRegister, 0, tmp2.low);
    moveAndUpdateRM
      (&c, TargetBytesPerWord, &tmp1, TargetBytesPerWord, &stackDst);

    return_(&c);
  }

  virtual void apply(Operation op) {
    arch_->c.operations[op](&c);
  }

  virtual void apply(UnaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand)
  {
    arch_->c.unaryOperations[index(&(arch_->c), op, aType)]
      (&c, aSize, aOperand);
  }

  virtual void apply(BinaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand,
                     unsigned bSize, OperandType bType, Operand* bOperand)
  {
    arch_->c.binaryOperations[index(&(arch_->c), op, aType, bType)]
      (&c, aSize, aOperand, bSize, bOperand);
  }

  virtual void apply(TernaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand,
                     unsigned bSize, OperandType bType UNUSED,
                     Operand* bOperand,
                     unsigned cSize UNUSED, OperandType cType UNUSED,
                     Operand* cOperand)
  {
    if (isBranch(op)) {
      assert(&c, aSize == bSize);
      assert(&c, cSize == TargetBytesPerWord);
      assert(&c, cType == ConstantOperand);

      arch_->c.branchOperations[branchIndex(&(arch_->c), aType, bType)]
        (&c, op, aSize, aOperand, bOperand, cOperand);
    } else {
      assert(&c, bSize == cSize);
      assert(&c, bType == RegisterOperand);
      assert(&c, cType == RegisterOperand);
      
      arch_->c.ternaryOperations[index(&(arch_->c), op, aType)]
        (&c, bSize, aOperand, bOperand, cOperand);
    }
  }

  virtual void setDestination(uint8_t* dst) {
    c.result = dst;
  }

  virtual void write() {
    uint8_t* dst = c.result;
    unsigned dstOffset = 0;
    for (MyBlock* b = c.firstBlock; b; b = b->next) {
      if (DebugJumps) {
        fprintf(stderr, "write block %p\n", b);
      }

      unsigned blockOffset = 0;
      for (JumpEvent* e = b->jumpEventHead; e; e = e->next) {
        unsigned size = e->offset - blockOffset;
        memcpy(dst + dstOffset, c.code.data + b->offset + blockOffset, size);
        blockOffset = e->offset;
        dstOffset += size;

        unsigned jumpTableSize = 0;
        for (JumpOffset* o = e->jumpOffsetHead; o; o = o->next) {
          if (DebugJumps) {
            fprintf(stderr, "visit offset %p %d in block %p\n",
                    o, o->offset, b);
          }

          uint8_t* address = dst + dstOffset + jumpTableSize;

          if (needJump(b)) {
            address += TargetBytesPerWord;
          }

          o->task->jumpAddress = address;

          jumpTableSize += TargetBytesPerWord;
        }

        assert(&c, jumpTableSize);

        bool jump = needJump(b);
        if (jump) {
          write4(dst + dstOffset, ::b(jumpTableSize + TargetBytesPerWord));
        }

        dstOffset += jumpTableSize + (jump ? TargetBytesPerWord : 0);
      }

      unsigned size = b->size - blockOffset;

      memcpy(dst + dstOffset,
             c.code.data + b->offset + blockOffset,
             size);

      dstOffset += size;
    }
    
    unsigned index = dstOffset;
    assert(&c, index % TargetBytesPerWord == 0);
    for (ConstantPoolEntry* e = c.constantPool; e; e = e->next) {
      e->address = dst + index;
      index += TargetBytesPerWord;
    }
    
    for (Task* t = c.tasks; t; t = t->next) {
      t->run(&c);
    }

    for (ConstantPoolEntry* e = c.constantPool; e; e = e->next) {
      *static_cast<uint32_t*>(e->address) = e->constant->value();
//       fprintf(stderr, "constant %p at %p\n", reinterpret_cast<void*>(e->constant->value()), e->address);
    }
  }

  virtual Promise* offset(bool) {
    return ::offset(&c);
  }

  virtual Block* endBlock(bool startNew) {
    MyBlock* b = c.lastBlock;
    b->size = c.code.length() - b->offset;
    if (startNew) {
      c.lastBlock = new (c.zone->allocate(sizeof(MyBlock)))
        MyBlock(&c, c.code.length());
    } else {
      c.lastBlock = 0;
    }
    return b;
  }

  virtual void endEvent() {
    MyBlock* b = c.lastBlock;
    unsigned thisEventOffset = c.code.length() - b->offset;
    if (b->jumpOffsetHead) {
      int32_t v = (thisEventOffset + TargetBytesPerWord)
        - b->jumpOffsetHead->offset;

      if (v > 0 and not bounded(2, 16, v)) {
        appendJumpEvent
          (&c, b, b->lastEventOffset, b->jumpOffsetHead,
           b->lastJumpOffsetTail);

        if (DebugJumps) {
          for (JumpOffset* o = b->jumpOffsetHead;
               o != b->lastJumpOffsetTail->next; o = o->next)
          {
            fprintf(stderr,
                    "in endEvent, include %p %d in jump event %p "
                    "at offset %d in block %p\n",
                    o, o->offset, b->jumpEventTail, b->lastEventOffset, b);
          }
        }

        b->jumpOffsetHead = b->lastJumpOffsetTail->next;
        b->lastJumpOffsetTail->next = 0;
        if (b->jumpOffsetHead == 0) {
          b->jumpOffsetTail = 0;
        }
      }
    }
    b->lastEventOffset = thisEventOffset;
    b->lastJumpOffsetTail = b->jumpOffsetTail;
  }

  virtual unsigned length() {
    return c.code.length();
  }

  virtual unsigned footerSize() {
    return c.constantPoolCount * TargetBytesPerWord;
  }

  virtual void dispose() {
    c.code.dispose();
  }

  Context c;
  MyArchitecture* arch_;
};

} // namespace

namespace vm {

Assembler::Architecture*
makeArchitecture(System* system, bool)
{
  return new (allocate(system, sizeof(MyArchitecture))) MyArchitecture(system);
}

Assembler*
makeAssembler(System* system, Allocator* allocator, Zone* zone,
              Assembler::Architecture* architecture)
{
  return new (zone->allocate(sizeof(MyAssembler)))
    MyAssembler(system, allocator, zone,
                static_cast<MyArchitecture*>(architecture));
}

} // namespace vm

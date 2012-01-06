/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "assembler.h"
#include "target.h"
#include "vector.h"

#define CAST1(x) reinterpret_cast<UnaryOperationType>(x)
#define CAST2(x) reinterpret_cast<BinaryOperationType>(x)
#define CAST_BRANCH(x) reinterpret_cast<BranchOperationType>(x)

using namespace vm;

namespace {

namespace local {

enum {
  rax = 0,
  rcx = 1,
  rdx = 2,
  rbx = 3,
  rsp = 4,
  rbp = 5,
  rsi = 6,
  rdi = 7,
  r8 = 8,
  r9 = 9,
  r10 = 10,
  r11 = 11,
  r12 = 12,
  r13 = 13,
  r14 = 14,
  r15 = 15,
};

enum {
  xmm0 = r15 + 1,
  xmm1,
  xmm2,
  xmm3,
  xmm4,
  xmm5,
  xmm6,
  xmm7,
  xmm8,
  xmm9,
  xmm10,
  xmm11,
  xmm12,
  xmm13,
  xmm14,
  xmm15,
};

const unsigned GeneralRegisterMask
= TargetBytesPerWord == 4 ? 0x000000ff : 0x0000ffff;

const unsigned FloatRegisterMask
= TargetBytesPerWord == 4 ? 0x00ff0000 : 0xffff0000;

const unsigned FrameHeaderSize = (UseFramePointer ? 2 : 1);

const int LongJumpRegister = r10;

const unsigned StackAlignmentInBytes = 16;
const unsigned StackAlignmentInWords = StackAlignmentInBytes / TargetBytesPerWord;

bool
isInt8(target_intptr_t v)
{
  return v == static_cast<int8_t>(v);
}

bool
isInt32(target_intptr_t v)
{
  return v == static_cast<int32_t>(v);
}

class Task;
class AlignmentPadding;

unsigned
padding(AlignmentPadding* p, unsigned index, unsigned offset,
        AlignmentPadding* limit);

class Context;
class MyBlock;

ResolvedPromise*
resolved(Context* c, int64_t value);

class MyBlock: public Assembler::Block {
 public:
  MyBlock(unsigned offset):
    next(0), firstPadding(0), lastPadding(0), offset(offset), start(~0),
    size(0)
  { }

  virtual unsigned resolve(unsigned start, Assembler::Block* next) {
    this->start = start;
    this->next = static_cast<MyBlock*>(next);

    return start + size + padding(firstPadding, start, offset, lastPadding);
  }

  MyBlock* next;
  AlignmentPadding* firstPadding;
  AlignmentPadding* lastPadding;
  unsigned offset;
  unsigned start;
  unsigned size;
};

typedef void (*OperationType)(Context*);

typedef void (*UnaryOperationType)(Context*, unsigned, Assembler::Operand*);

typedef void (*BinaryOperationType)
(Context*, unsigned, Assembler::Operand*, unsigned, Assembler::Operand*);

typedef void (*BranchOperationType)
(Context*, TernaryOperation, unsigned, Assembler::Operand*,
 Assembler::Operand*, Assembler::Operand*);

class ArchitectureContext {
 public:
  ArchitectureContext(System* s, bool useNativeFeatures):
    s(s), useNativeFeatures(useNativeFeatures)
  { }

  System* s;
  bool useNativeFeatures;
  OperationType operations[OperationCount];
  UnaryOperationType unaryOperations[UnaryOperationCount
                                     * OperandTypeCount];
  BinaryOperationType binaryOperations
  [(BinaryOperationCount + NonBranchTernaryOperationCount)
   * OperandTypeCount
   * OperandTypeCount];
  BranchOperationType branchOperations
  [(BranchOperationCount)
   * OperandTypeCount
   * OperandTypeCount];
};

class Context {
 public:
  Context(System* s, Allocator* a, Zone* zone, ArchitectureContext* ac):
    s(s), zone(zone), client(0), code(s, a, 1024), tasks(0), result(0),
    firstBlock(new (zone->allocate(sizeof(MyBlock))) MyBlock(0)),
    lastBlock(firstBlock), ac(ac)
  { }

  System* s;
  Zone* zone;
  Assembler::Client* client;
  Vector code;
  Task* tasks;
  uint8_t* result;
  MyBlock* firstBlock;
  MyBlock* lastBlock;
  ArchitectureContext* ac;
};

void NO_RETURN
abort(Context* c)
{
  abort(c->s);
}

void NO_RETURN
abort(ArchitectureContext* c)
{
  abort(c->s);
}

#ifndef NDEBUG
void
assert(Context* c, bool v)
{
  assert(c->s, v);
}

void
assert(ArchitectureContext* c, bool v)
{
  assert(c->s, v);
}
#endif // not NDEBUG

void
expect(Context* c, bool v)
{
  expect(c->s, v);
}

ResolvedPromise*
resolved(Context* c, int64_t value)
{
  return new (c->zone->allocate(sizeof(ResolvedPromise)))
    ResolvedPromise(value);
}

class CodePromise: public Promise {
 public:
  CodePromise(Context* c, unsigned offset): c(c), offset(offset) { }

  virtual int64_t value() {
    if (resolved()) {
      return reinterpret_cast<intptr_t>(c->result + offset);
    }
    
    abort(c);
  }

  virtual bool resolved() {
    return c->result != 0;
  }

  Context* c;
  unsigned offset;
};

CodePromise*
codePromise(Context* c, unsigned offset)
{
  return new (c->zone->allocate(sizeof(CodePromise))) CodePromise(c, offset);
}

class Offset: public Promise {
 public:
  Offset(Context* c, MyBlock* block, unsigned offset, AlignmentPadding* limit):
    c(c), block(block), offset(offset), limit(limit), value_(-1)
  { }

  virtual bool resolved() {
    return block->start != static_cast<unsigned>(~0);
  }
  
  virtual int64_t value() {
    assert(c, resolved());

    if (value_ == -1) {
      value_ = block->start + (offset - block->offset)
        + padding(block->firstPadding, block->start, block->offset, limit);
    }

    return value_;
  }

  Context* c;
  MyBlock* block;
  unsigned offset;
  AlignmentPadding* limit;
  int value_;
};

Promise*
offset(Context* c)
{
  return new (c->zone->allocate(sizeof(Offset)))
    Offset(c, c->lastBlock, c->code.length(), c->lastBlock->lastPadding);
}

class Task {
 public:
  Task(Task* next): next(next) { }

  virtual void run(Context* c) = 0;

  Task* next;
};

void*
resolveOffset(System* s, uint8_t* instruction, unsigned instructionSize,
              int64_t value)
{
  intptr_t v = reinterpret_cast<uint8_t*>(value)
    - instruction - instructionSize;
    
  expect(s, isInt32(v));
    
  int32_t v4 = v;
  memcpy(instruction + instructionSize - 4, &v4, 4);
  return instruction + instructionSize;
}

class OffsetListener: public Promise::Listener {
 public:
  OffsetListener(System* s, uint8_t* instruction,
                 unsigned instructionSize):
    s(s),
    instruction(instruction),
    instructionSize(instructionSize)
  { }

  virtual bool resolve(int64_t value, void** location) {
    void* p = resolveOffset(s, instruction, instructionSize, value);
    if (location) *location = p;
    return false;
  }

  System* s;
  uint8_t* instruction;
  unsigned instructionSize;
};

class OffsetTask: public Task {
 public:
  OffsetTask(Task* next, Promise* promise, Promise* instructionOffset,
             unsigned instructionSize):
    Task(next),
    promise(promise),
    instructionOffset(instructionOffset),
    instructionSize(instructionSize)
  { }

  virtual void run(Context* c) {
    if (promise->resolved()) {
      resolveOffset
        (c->s, c->result + instructionOffset->value(), instructionSize,
         promise->value());
    } else {
      new (promise->listen(sizeof(OffsetListener)))
        OffsetListener(c->s, c->result + instructionOffset->value(),
                       instructionSize);
    }
  }

  Promise* promise;
  Promise* instructionOffset;
  unsigned instructionSize;
};

void
appendOffsetTask(Context* c, Promise* promise, Promise* instructionOffset,
                 unsigned instructionSize)
{
  OffsetTask* task = new (c->zone->allocate(sizeof(OffsetTask))) OffsetTask
    (c->tasks, promise, instructionOffset, instructionSize);

  c->tasks = task;
}

void
copy(System* s, void* dst, int64_t src, unsigned size)
{
  switch (size) {
  case 4: {
    int32_t v = src;
    memcpy(dst, &v, 4);
  } break;

  case 8: {
    int64_t v = src;
    memcpy(dst, &v, 8);
  } break;

  default: abort(s);
  }
}

class ImmediateListener: public Promise::Listener {
 public:
  ImmediateListener(System* s, void* dst, unsigned size, unsigned offset):
    s(s), dst(dst), size(size), offset(offset)
  { }

  virtual bool resolve(int64_t value, void** location) {
    copy(s, dst, value, size);
    if (location) *location = static_cast<uint8_t*>(dst) + offset;
    return offset == 0;
  }

  System* s;
  void* dst;
  unsigned size;
  unsigned offset;
};

class ImmediateTask: public Task {
 public:
  ImmediateTask(Task* next, Promise* promise, Promise* offset, unsigned size,
                unsigned promiseOffset):
    Task(next),
    promise(promise),
    offset(offset),
    size(size),
    promiseOffset(promiseOffset)
  { }

  virtual void run(Context* c) {
    if (promise->resolved()) {
      copy(c->s, c->result + offset->value(), promise->value(), size);
    } else {
      new (promise->listen(sizeof(ImmediateListener))) ImmediateListener
        (c->s, c->result + offset->value(), size, promiseOffset);
    }
  }

  Promise* promise;
  Promise* offset;
  unsigned size;
  unsigned promiseOffset;
};

void
appendImmediateTask(Context* c, Promise* promise, Promise* offset,
                    unsigned size, unsigned promiseOffset = 0)
{
  c->tasks = new (c->zone->allocate(sizeof(ImmediateTask))) ImmediateTask
    (c->tasks, promise, offset, size, promiseOffset);
}

class AlignmentPadding {
 public:
  AlignmentPadding(Context* c, unsigned instructionOffset, unsigned alignment):
    offset(c->code.length()),
    instructionOffset(instructionOffset),
    alignment(alignment),
    next(0),
    padding(-1)
  {
    if (c->lastBlock->firstPadding) {
      c->lastBlock->lastPadding->next = this;
    } else {
      c->lastBlock->firstPadding = this;
    }
    c->lastBlock->lastPadding = this;
  }

  unsigned offset;
  unsigned instructionOffset;
  unsigned alignment;
  AlignmentPadding* next;
  int padding;
};

unsigned
padding(AlignmentPadding* p, unsigned start, unsigned offset,
        AlignmentPadding* limit)
{
  unsigned padding = 0;
  if (limit) {
    if (limit->padding == -1) {
      for (; p; p = p->next) {
        if (p->padding == -1) {
          unsigned index = p->offset - offset;
          while ((start + index + padding + p->instructionOffset)
                 % p->alignment)
          {
            ++ padding;
          }
      
          p->padding = padding;

          if (p == limit) break;
        } else {
          padding = p->padding;
        }
      }
    } else {
      padding = limit->padding;
    }
  }
  return padding;
}

extern "C" bool
detectFeature(unsigned ecx, unsigned edx);

bool
useSSE(ArchitectureContext* c)
{
  if (TargetBytesPerWord == 8) {
    // amd64 implies SSE2 support
    return true;
  } else if (c->useNativeFeatures) {
    static int supported = -1;
    if (supported == -1) {
      supported = detectFeature(0, 0x2000000) // SSE 1
        and detectFeature(0, 0x4000000); // SSE 2
    }
    return supported;
  } else {
    return false;
  }
}

#define REX_W 0x48
#define REX_R 0x44
#define REX_X 0x42
#define REX_B 0x41
#define REX_NONE 0x40

void maybeRex(Context* c, unsigned size, int a, int index, int base,
              bool always)
{
  if (TargetBytesPerWord == 8) {
    uint8_t byte;
    if (size == 8) {
      byte = REX_W;
    } else {
      byte = REX_NONE;
    }
    if (a != NoRegister and (a & 8)) byte |= REX_R;
    if (index != NoRegister and (index & 8)) byte |= REX_X;
    if (base != NoRegister and (base & 8)) byte |= REX_B;
    if (always or byte != REX_NONE) c->code.append(byte);
  }
}

void
maybeRex(Context* c, unsigned size, Assembler::Register* a,
         Assembler::Register* b)
{
  maybeRex(c, size, a->low, NoRegister, b->low, false);
}

void
alwaysRex(Context* c, unsigned size, Assembler::Register* a,
          Assembler::Register* b)
{
  maybeRex(c, size, a->low, NoRegister, b->low, true);
}

void
maybeRex(Context* c, unsigned size, Assembler::Register* a)
{
  maybeRex(c, size, NoRegister, NoRegister, a->low, false);
}

void
maybeRex(Context* c, unsigned size, Assembler::Register* a,
         Assembler::Memory* b)
{
  maybeRex(c, size, a->low, b->index, b->base, size == 1 and (a->low & 4));
}

void
maybeRex(Context* c, unsigned size, Assembler::Memory* a)
{
  maybeRex(c, size, NoRegister, a->index, a->base, false);
}

int
regCode(int a)
{
  return a & 7;
}

int
regCode(Assembler::Register* a)
{
  return regCode(a->low);
}

void
modrm(Context* c, uint8_t mod, int a, int b)
{
  c->code.append(mod | (regCode(b) << 3) | regCode(a));
}

void
modrm(Context* c, uint8_t mod, Assembler::Register* a, Assembler::Register* b)
{
  modrm(c, mod, a->low, b->low);
}

void
sib(Context* c, unsigned scale, int index, int base)
{
  c->code.append((log(scale) << 6) | (regCode(index) << 3) | regCode(base));
}

void
modrmSib(Context* c, int width, int a, int scale, int index, int base)
{
  if (index == NoRegister) {
    modrm(c, width, base, a);
    if (regCode(base) == rsp) {
      sib(c, 0x00, rsp, rsp);
    }
  } else {
    modrm(c, width, rsp, a);
    sib(c, scale, index, base);
  }
}

void
modrmSibImm(Context* c, int a, int scale, int index, int base, int offset)
{
  if (offset == 0 and regCode(base) != rbp) {
    modrmSib(c, 0x00, a, scale, index, base);
  } else if (isInt8(offset)) {
    modrmSib(c, 0x40, a, scale, index, base);
    c->code.append(offset);
  } else {
    modrmSib(c, 0x80, a, scale, index, base);
    c->code.append4(offset);
  }
}
  

void
modrmSibImm(Context* c, Assembler::Register* a, Assembler::Memory* b)
{
  modrmSibImm(c, a->low, b->scale, b->index, b->base, b->offset);
}

void
opcode(Context* c, uint8_t op)
{
  c->code.append(op);
}

void
opcode(Context* c, uint8_t op1, uint8_t op2)
{
  c->code.append(op1);
  c->code.append(op2);
}

void
opcode(Context* c, uint8_t op1, uint8_t op2, uint8_t op3)
{
  c->code.append(op1);
  c->code.append(op2);
  c->code.append(op3);
}

void
return_(Context* c)
{
  opcode(c, 0xc3);
}

void
ignore(Context*)
{ }

void
storeLoadBarrier(Context* c)
{
  if (useSSE(c->ac)) {
    // mfence:
    c->code.append(0x0f);
    c->code.append(0xae);
    c->code.append(0xf0);
  } else {
    // lock addq $0x0,(%rsp):
    c->code.append(0xf0);
    if (TargetBytesPerWord == 8) {
      c->code.append(0x48);
    }
    c->code.append(0x83);
    c->code.append(0x04);
    c->code.append(0x24);
    c->code.append(0x00);    
  }
}

void
unconditional(Context* c, unsigned jump, Assembler::Constant* a)
{
  appendOffsetTask(c, a->value, offset(c), 5);

  opcode(c, jump);
  c->code.append4(0);
}

void
conditional(Context* c, unsigned condition, Assembler::Constant* a)
{
  appendOffsetTask(c, a->value, offset(c), 6);
  
  opcode(c, 0x0f, condition);
  c->code.append4(0);
}

unsigned
index(ArchitectureContext*, UnaryOperation operation, OperandType operand)
{
  return operation + (UnaryOperationCount * operand);
}

unsigned
index(ArchitectureContext*, BinaryOperation operation,
      OperandType operand1,
      OperandType operand2)
{
  return operation
    + ((BinaryOperationCount + NonBranchTernaryOperationCount) * operand1)
    + ((BinaryOperationCount + NonBranchTernaryOperationCount)
       * OperandTypeCount * operand2);
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

unsigned
index(ArchitectureContext* c UNUSED, TernaryOperation operation,
      OperandType operand1, OperandType operand2)
{
  assert(c, not isBranch(operation));

  return BinaryOperationCount + operation
    + ((BinaryOperationCount + NonBranchTernaryOperationCount) * operand1)
    + ((BinaryOperationCount + NonBranchTernaryOperationCount)
       * OperandTypeCount * operand2);
}

unsigned
branchIndex(ArchitectureContext* c UNUSED, OperandType operand1,
            OperandType operand2)
{
  return operand1 + (OperandTypeCount * operand2);
}

void
moveCR(Context* c, unsigned aSize, Assembler::Constant* a,
       unsigned bSize, Assembler::Register* b);

void
moveCR2(Context*, unsigned, Assembler::Constant*, unsigned,
        Assembler::Register*, unsigned);

void
callR(Context*, unsigned, Assembler::Register*);

void
callC(Context* c, unsigned size UNUSED, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  unconditional(c, 0xe8, a);
}

void
longCallC(Context* c, unsigned size, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  if (TargetBytesPerWord == 8) {
    Assembler::Register r(LongJumpRegister);
    moveCR2(c, size, a, size, &r, 11);
    callR(c, size, &r);
  } else {
    callC(c, size, a);
  }
}

void
jumpR(Context* c, unsigned size UNUSED, Assembler::Register* a)
{
  assert(c, size == TargetBytesPerWord);

  maybeRex(c, 4, a);
  opcode(c, 0xff, 0xe0 + regCode(a));
}

void
jumpC(Context* c, unsigned size UNUSED, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  unconditional(c, 0xe9, a);
}

void
jumpM(Context* c, unsigned size UNUSED, Assembler::Memory* a)
{
  assert(c, size == TargetBytesPerWord);
  
  maybeRex(c, 4, a);
  opcode(c, 0xff);
  modrmSibImm(c, rsp, a->scale, a->index, a->base, a->offset);
}

void
longJumpC(Context* c, unsigned size, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  if (TargetBytesPerWord == 8) {
    Assembler::Register r(LongJumpRegister);
    moveCR2(c, size, a, size, &r, 11);
    jumpR(c, size, &r);
  } else {
    jumpC(c, size, a);
  }
}

void
callR(Context* c, unsigned size UNUSED, Assembler::Register* a)
{
  assert(c, size == TargetBytesPerWord);

  // maybeRex.W has no meaning here so we disable it
  maybeRex(c, 4, a);
  opcode(c, 0xff, 0xd0 + regCode(a));
}

void
callM(Context* c, unsigned size UNUSED, Assembler::Memory* a)
{
  assert(c, size == TargetBytesPerWord);
  
  maybeRex(c, 4, a);
  opcode(c, 0xff);
  modrmSibImm(c, rdx, a->scale, a->index, a->base, a->offset);
}

void
alignedCallC(Context* c, unsigned size, Assembler::Constant* a)
{
  new (c->zone->allocate(sizeof(AlignmentPadding))) AlignmentPadding(c, 1, 4);
  callC(c, size, a);
}

void
alignedLongCallC(Context* c, unsigned size, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  if (TargetBytesPerWord == 8) {
    new (c->zone->allocate(sizeof(AlignmentPadding)))
      AlignmentPadding(c, 2, 8);
    longCallC(c, size, a);
  } else {
    alignedCallC(c, size, a);
  }
}

void
alignedJumpC(Context* c, unsigned size, Assembler::Constant* a)
{
  new (c->zone->allocate(sizeof(AlignmentPadding))) AlignmentPadding(c, 1, 4);
  jumpC(c, size, a);
}

void
alignedLongJumpC(Context* c, unsigned size, Assembler::Constant* a)
{
  assert(c, size == TargetBytesPerWord);

  if (TargetBytesPerWord == 8) {
    new (c->zone->allocate(sizeof(AlignmentPadding)))
      AlignmentPadding(c, 2, 8);
    longJumpC(c, size, a);
  } else {
    alignedJumpC(c, size, a);
  }
}

void
pushR(Context* c, unsigned size, Assembler::Register* a)
{
  if (TargetBytesPerWord == 4 and size == 8) {
    Assembler::Register ah(a->high);

    pushR(c, 4, &ah);
    pushR(c, 4, a);
  } else {
    maybeRex(c, 4, a);
    opcode(c, 0x50 + regCode(a));
  }
}

void
moveRR(Context* c, unsigned aSize, Assembler::Register* a,
       unsigned bSize, Assembler::Register* b);

void
popR(Context* c, unsigned size, Assembler::Register* a)
{
  if (TargetBytesPerWord == 4 and size == 8) {
    Assembler::Register ah(a->high);

    popR(c, 4, a);
    popR(c, 4, &ah);
  } else {
    maybeRex(c, 4, a);
    opcode(c, 0x58 + regCode(a));
    if (TargetBytesPerWord == 8 and size == 4) {
      moveRR(c, 4, a, 8, a);
    }
  }
}

void
addCarryCR(Context* c, unsigned size, Assembler::Constant* a,
           Assembler::Register* b);

void
negateR(Context* c, unsigned size, Assembler::Register* a)
{
  if (TargetBytesPerWord == 4 and size == 8) {
    assert(c, a->low == rax and a->high == rdx);

    ResolvedPromise zeroPromise(0);
    Assembler::Constant zero(&zeroPromise);

    Assembler::Register ah(a->high);

    negateR(c, 4, a);
    addCarryCR(c, 4, &zero, &ah);
    negateR(c, 4, &ah);
  } else {
    maybeRex(c, size, a);
    opcode(c, 0xf7, 0xd8 + regCode(a));
  }
}

void
negateRR(Context* c, unsigned aSize, Assembler::Register* a,
         unsigned bSize UNUSED, Assembler::Register* b UNUSED)
{
  assert(c, aSize == bSize);

  negateR(c, aSize, a);
}

void
moveCR2(Context* c, UNUSED unsigned aSize, Assembler::Constant* a,
        UNUSED unsigned bSize, Assembler::Register* b, unsigned promiseOffset)
{
  if (TargetBytesPerWord == 4 and bSize == 8) {
    int64_t v = a->value->value();

    ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
    Assembler::Constant ah(&high);

    ResolvedPromise low(v & 0xFFFFFFFF);
    Assembler::Constant al(&low);

    Assembler::Register bh(b->high);

    moveCR(c, 4, &al, 4, b);
    moveCR(c, 4, &ah, 4, &bh);
  } else {
    maybeRex(c, TargetBytesPerWord, b);
    opcode(c, 0xb8 + regCode(b));
    if (a->value->resolved()) {
      c->code.appendTargetAddress(a->value->value());
    } else {
      appendImmediateTask
        (c, a->value, offset(c), TargetBytesPerWord, promiseOffset);
      c->code.appendTargetAddress(static_cast<target_uintptr_t>(0));
    }
  }
}

bool
floatReg(Assembler::Register* a)
{
  return a->low >= xmm0;
}

void
sseMoveRR(Context* c, unsigned aSize, Assembler::Register* a,
          unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize >= 4);
  assert(c, aSize == bSize);

  if (floatReg(a) and floatReg(b)) {
    if (aSize == 4) {
      opcode(c, 0xf3);
      maybeRex(c, 4, a, b);
      opcode(c, 0x0f, 0x10);
      modrm(c, 0xc0, a, b);
    } else {
      opcode(c, 0xf2);
      maybeRex(c, 4, b, a);
      opcode(c, 0x0f, 0x10);
      modrm(c, 0xc0, a, b);
    } 
  } else if (floatReg(a)) {
    opcode(c, 0x66);
    maybeRex(c, aSize, a, b);
    opcode(c, 0x0f, 0x7e);
    modrm(c, 0xc0, b, a);  	
  } else {
    opcode(c, 0x66);
    maybeRex(c, aSize, b, a);
    opcode(c, 0x0f, 0x6e);
    modrm(c, 0xc0, a, b);  	
  }
}

void
sseMoveCR(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize <= TargetBytesPerWord);
  Assembler::Register tmp(c->client->acquireTemporary(GeneralRegisterMask));
  moveCR2(c, aSize, a, aSize, &tmp, 0);
  sseMoveRR(c, aSize, &tmp, bSize, b);
  c->client->releaseTemporary(tmp.low);
}

void
moveCR(Context* c, unsigned aSize, Assembler::Constant* a,
       unsigned bSize, Assembler::Register* b)
{
  if (floatReg(b)) {
    sseMoveCR(c, aSize, a, bSize, b);
  } else {
    moveCR2(c, aSize, a, bSize, b, 0);
  }
}

void
swapRR(Context* c, unsigned aSize UNUSED, Assembler::Register* a,
       unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);
  assert(c, aSize == TargetBytesPerWord);
  
  alwaysRex(c, aSize, a, b);
  opcode(c, 0x87);
  modrm(c, 0xc0, b, a);
}

void
moveRR(Context* c, unsigned aSize, Assembler::Register* a,
       UNUSED unsigned bSize, Assembler::Register* b)
{
  if (floatReg(a) or floatReg(b)) {
    sseMoveRR(c, aSize, a, bSize, b);
    return;
  }
  
  if (TargetBytesPerWord == 4 and aSize == 8 and bSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    if (a->high == b->low) {
      if (a->low == b->high) {
        swapRR(c, 4, a, 4, b);
      } else {
        moveRR(c, 4, &ah, 4, &bh);
        moveRR(c, 4, a, 4, b);
      }
    } else {
      moveRR(c, 4, a, 4, b);
      moveRR(c, 4, &ah, 4, &bh);
    }
  } else {
    switch (aSize) {
    case 1:
      if (TargetBytesPerWord == 4 and a->low > rbx) {
        assert(c, b->low <= rbx);

        moveRR(c, TargetBytesPerWord, a, TargetBytesPerWord, b);
        moveRR(c, 1, b, TargetBytesPerWord, b);
      } else {
        alwaysRex(c, aSize, b, a);
        opcode(c, 0x0f, 0xbe);
        modrm(c, 0xc0, a, b);
      }
      break;

    case 2:
      alwaysRex(c, aSize, b, a);
      opcode(c, 0x0f, 0xbf);
      modrm(c, 0xc0, a, b);
      break;

    case 4:
      if (bSize == 8) {
      	if (TargetBytesPerWord == 8) {
          alwaysRex(c, bSize, b, a);
          opcode(c, 0x63);
          modrm(c, 0xc0, a, b);
      	} else {
      	  if (a->low == rax and b->low == rax and b->high == rdx) {
      	  	opcode(c, 0x99); //cdq
      	  } else {
            assert(c, b->low == rax and b->high == rdx);

            moveRR(c, 4, a, 4, b);
            moveRR(c, 4, b, 8, b);
          }
        }
      } else {
        if (a->low != b->low) {
          alwaysRex(c, aSize, a, b);
          opcode(c, 0x89);
          modrm(c, 0xc0, b, a);
        }
      }
      break; 
      
    case 8:
      if (a->low != b->low){
        maybeRex(c, aSize, a, b);
        opcode(c, 0x89);
        modrm(c, 0xc0, b, a);
      }
      break;
    }
  }
}

void
sseMoveMR(Context* c, unsigned aSize, Assembler::Memory* a,
          unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize >= 4);

  if (TargetBytesPerWord == 4 and aSize == 8) {
    opcode(c, 0xf3);
    opcode(c, 0x0f, 0x7e);
    modrmSibImm(c, b, a);
  } else {
    opcode(c, 0x66);
    maybeRex(c, aSize, b, a);
    opcode(c, 0x0f, 0x6e);
    modrmSibImm(c, b, a);
  }
}

void
moveMR(Context* c, unsigned aSize, Assembler::Memory* a,
       unsigned bSize, Assembler::Register* b)
{
  if (floatReg(b)) {
    sseMoveMR(c, aSize, a, bSize, b);
    return;
  }
  
  switch (aSize) {
  case 1:
    maybeRex(c, bSize, b, a);
    opcode(c, 0x0f, 0xbe);
    modrmSibImm(c, b, a);
    break;

  case 2:
    maybeRex(c, bSize, b, a);
    opcode(c, 0x0f, 0xbf);
    modrmSibImm(c, b, a);
    break;

  case 4:
    if (TargetBytesPerWord == 8) {
      maybeRex(c, bSize, b, a);
      opcode(c, 0x63);
      modrmSibImm(c, b, a);
    } else {
      if (bSize == 8) {
        assert(c, b->low == rax and b->high == rdx);
        
        moveMR(c, 4, a, 4, b);
        moveRR(c, 4, b, 8, b);
      } else {
        maybeRex(c, bSize, b, a);
        opcode(c, 0x8b);
        modrmSibImm(c, b, a);
      }
    }
    break;
    
  case 8:
    if (TargetBytesPerWord == 4 and bSize == 8) {
      Assembler::Memory ah(a->base, a->offset + 4, a->index, a->scale);
      Assembler::Register bh(b->high);

      moveMR(c, 4, a, 4, b);    
      moveMR(c, 4, &ah, 4, &bh);
    } else {
      maybeRex(c, bSize, b, a);
      opcode(c, 0x8b);
      modrmSibImm(c, b, a);
    }
    break;

  default: abort(c);
  }
}

void
sseMoveRM(Context* c, unsigned aSize, Assembler::Register* a,
       UNUSED unsigned bSize, Assembler::Memory* b)
{
  assert(c, aSize >= 4);
  assert(c, aSize == bSize);

  if (TargetBytesPerWord == 4 and aSize == 8) {
    opcode(c, 0x66);
    opcode(c, 0x0f, 0xd6);
    modrmSibImm(c, a, b);
  } else {
    opcode(c, 0x66);
    maybeRex(c, aSize, a, b);
    opcode(c, 0x0f, 0x7e);
    modrmSibImm(c, a, b);
  }
}

void
moveRM(Context* c, unsigned aSize, Assembler::Register* a,
       unsigned bSize UNUSED, Assembler::Memory* b)
{
  assert(c, aSize == bSize);
  
  if (floatReg(a)) {
    sseMoveRM(c, aSize, a, bSize, b);
    return;
  }
  
  switch (aSize) {
  case 1:
    maybeRex(c, bSize, a, b);
    opcode(c, 0x88);
    modrmSibImm(c, a, b);
    break;

  case 2:
    opcode(c, 0x66);
    maybeRex(c, bSize, a, b);
    opcode(c, 0x89);
    modrmSibImm(c, a, b);
    break;

  case 4:
    if (TargetBytesPerWord == 8) {
      maybeRex(c, bSize, a, b);
      opcode(c, 0x89);
      modrmSibImm(c, a, b);
      break;
    } else {
      opcode(c, 0x89);
      modrmSibImm(c, a, b);
    }
    break;
    
  case 8:
    if (TargetBytesPerWord == 8) {
      maybeRex(c, bSize, a, b);
      opcode(c, 0x89);
      modrmSibImm(c, a, b);
    } else {
      Assembler::Register ah(a->high);
      Assembler::Memory bh(b->base, b->offset + 4, b->index, b->scale);

      moveRM(c, 4, a, 4, b);    
      moveRM(c, 4, &ah, 4, &bh);
    }
    break;

  default: abort(c);
  }
}

void
moveAR(Context* c, unsigned aSize, Assembler::Address* a,
       unsigned bSize, Assembler::Register* b)
{
  assert(c, TargetBytesPerWord == 8 or (aSize == 4 and bSize == 4));

  Assembler::Constant constant(a->address);
  Assembler::Memory memory(b->low, 0, -1, 0);

  moveCR(c, aSize, &constant, bSize, b);
  moveMR(c, bSize, &memory, bSize, b);
}

ShiftMaskPromise*
shiftMaskPromise(Context* c, Promise* base, unsigned shift, int64_t mask)
{
  return new (c->zone->allocate(sizeof(ShiftMaskPromise)))
    ShiftMaskPromise(base, shift, mask);
}

void
moveCM(Context* c, unsigned aSize UNUSED, Assembler::Constant* a,
       unsigned bSize, Assembler::Memory* b)
{
  switch (bSize) {
  case 1:
    maybeRex(c, bSize, b);
    opcode(c, 0xc6);
    modrmSibImm(c, 0, b->scale, b->index, b->base, b->offset);
    c->code.append(a->value->value());
    break;

  case 2:
    opcode(c, 0x66);
    maybeRex(c, bSize, b);
    opcode(c, 0xc7);
    modrmSibImm(c, 0, b->scale, b->index, b->base, b->offset);
    c->code.append2(a->value->value());
    break;

  case 4:
    maybeRex(c, bSize, b);
    opcode(c, 0xc7);
    modrmSibImm(c, 0, b->scale, b->index, b->base, b->offset);
    if (a->value->resolved()) {
      c->code.append4(a->value->value());
    } else {
      appendImmediateTask(c, a->value, offset(c), 4);
      c->code.append4(0);
    }
    break;

  case 8: {
    if (TargetBytesPerWord == 8) {
      if (a->value->resolved() and isInt32(a->value->value())) {
        maybeRex(c, bSize, b);
        opcode(c, 0xc7);
        modrmSibImm(c, 0, b->scale, b->index, b->base, b->offset);
        c->code.append4(a->value->value());
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, 8, a, 8, &tmp);
        moveRM(c, 8, &tmp, 8, b);
        c->client->releaseTemporary(tmp.low);
      }
    } else {
      Assembler::Constant ah(shiftMaskPromise(c, a->value, 32, 0xFFFFFFFF));
      Assembler::Constant al(shiftMaskPromise(c, a->value, 0, 0xFFFFFFFF));

      Assembler::Memory bh(b->base, b->offset + 4, b->index, b->scale);

      moveCM(c, 4, &al, 4, b);
      moveCM(c, 4, &ah, 4, &bh);
    }
  } break;

  default: abort(c);
  }
}

void
moveZRR(Context* c, unsigned aSize, Assembler::Register* a,
        unsigned bSize UNUSED, Assembler::Register* b)
{
  switch (aSize) {
  case 2:
    alwaysRex(c, aSize, b, a);
    opcode(c, 0x0f, 0xb7);
    modrm(c, 0xc0, a, b);
    break;

  default: abort(c);
  }
}

void
moveZMR(Context* c, unsigned aSize UNUSED, Assembler::Memory* a,
        unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, bSize == TargetBytesPerWord);
  assert(c, aSize == 2);
  
  maybeRex(c, bSize, b, a);
  opcode(c, 0x0f, 0xb7);
  modrmSibImm(c, b->low, a->scale, a->index, a->base, a->offset);
}

void
addCarryRR(Context* c, unsigned size, Assembler::Register* a,
           Assembler::Register* b)
{
  assert(c, TargetBytesPerWord == 8 or size == 4);
  
  maybeRex(c, size, a, b);
  opcode(c, 0x11);
  modrm(c, 0xc0, b, a);
}

void
addRR(Context* c, unsigned aSize, Assembler::Register* a,
      unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  if (TargetBytesPerWord == 4 and aSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    addRR(c, 4, a, 4, b);
    addCarryRR(c, 4, &ah, &bh);
  } else {
    maybeRex(c, aSize, a, b);
    opcode(c, 0x01);
    modrm(c, 0xc0, b, a);
  }
}

void
addCarryCR(Context* c, unsigned size, Assembler::Constant* a,
           Assembler::Register* b)
{
  
  int64_t v = a->value->value();
  maybeRex(c, size, b);
  if (isInt8(v)) {
    opcode(c, 0x83, 0xd0 + regCode(b));
    c->code.append(v);
  } else {
    opcode(c, 0x81, 0xd0 + regCode(b));
    c->code.append4(v);
  }
}

void
addCR(Context* c, unsigned aSize, Assembler::Constant* a,
      unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  int64_t v = a->value->value();
  if (v) {
    if (TargetBytesPerWord == 4 and bSize == 8) {
      ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
      Assembler::Constant ah(&high);

      ResolvedPromise low(v & 0xFFFFFFFF);
      Assembler::Constant al(&low);

      Assembler::Register bh(b->high);

      addCR(c, 4, &al, 4, b);
      addCarryCR(c, 4, &ah, &bh);
    } else {
      if (isInt32(v)) {
        maybeRex(c, aSize, b);
        if (isInt8(v)) {
          opcode(c, 0x83, 0xc0 + regCode(b));
          c->code.append(v);
        } else {
          opcode(c, 0x81, 0xc0 + regCode(b));
          c->code.append4(v);
        }
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, aSize, a, aSize, &tmp);
        addRR(c, aSize, &tmp, bSize, b);
        c->client->releaseTemporary(tmp.low);
      }
    }
  }
}

void
subtractBorrowCR(Context* c, unsigned size UNUSED, Assembler::Constant* a,
                 Assembler::Register* b)
{
  assert(c, TargetBytesPerWord == 8 or size == 4);
  
  int64_t v = a->value->value();
  if (isInt8(v)) {
    opcode(c, 0x83, 0xd8 + regCode(b));
    c->code.append(v);
  } else {
    abort(c);
  }
}

void
subtractRR(Context* c, unsigned aSize, Assembler::Register* a,
           unsigned bSize, Assembler::Register* b);

void
subtractCR(Context* c, unsigned aSize, Assembler::Constant* a,
           unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  int64_t v = a->value->value();
  if (v) {
    if (TargetBytesPerWord == 4 and bSize == 8) {
      ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
      Assembler::Constant ah(&high);

      ResolvedPromise low(v & 0xFFFFFFFF);
      Assembler::Constant al(&low);

      Assembler::Register bh(b->high);

      subtractCR(c, 4, &al, 4, b);
      subtractBorrowCR(c, 4, &ah, &bh);
    } else {
      if (isInt32(v)) {
        maybeRex(c, aSize, b);
        if (isInt8(v)) {
          opcode(c, 0x83, 0xe8 + regCode(b));
          c->code.append(v);
        } else {
          opcode(c, 0x81, 0xe8 + regCode(b));
          c->code.append4(v);
        }
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, aSize, a, aSize, &tmp);
        subtractRR(c, aSize, &tmp, bSize, b);
        c->client->releaseTemporary(tmp.low);
      }
    }
  }
}

void
subtractBorrowRR(Context* c, unsigned size, Assembler::Register* a,
                 Assembler::Register* b)
{
  assert(c, TargetBytesPerWord == 8 or size == 4);
  
  maybeRex(c, size, a, b);
  opcode(c, 0x19);
  modrm(c, 0xc0, b, a);
}

void
subtractRR(Context* c, unsigned aSize, Assembler::Register* a,
           unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);
  
  if (TargetBytesPerWord == 4 and aSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    subtractRR(c, 4, a, 4, b);
    subtractBorrowRR(c, 4, &ah, &bh);
  } else {
    maybeRex(c, aSize, a, b);
    opcode(c, 0x29);
    modrm(c, 0xc0, b, a);
  }
}

void
andRR(Context* c, unsigned aSize, Assembler::Register* a,
      unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);


  if (TargetBytesPerWord == 4 and aSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    andRR(c, 4, a, 4, b);
    andRR(c, 4, &ah, 4, &bh);
  } else {
    maybeRex(c, aSize, a, b);
    opcode(c, 0x21);
    modrm(c, 0xc0, b, a);
  }
}

void
andCR(Context* c, unsigned aSize, Assembler::Constant* a,
      unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  int64_t v = a->value->value();

  if (TargetBytesPerWord == 4 and bSize == 8) {
    ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
    Assembler::Constant ah(&high);

    ResolvedPromise low(v & 0xFFFFFFFF);
    Assembler::Constant al(&low);

    Assembler::Register bh(b->high);

    andCR(c, 4, &al, 4, b);
    andCR(c, 4, &ah, 4, &bh);
  } else {
    if (isInt32(v)) {
      maybeRex(c, aSize, b);
      if (isInt8(v)) {
        opcode(c, 0x83, 0xe0 + regCode(b));
        c->code.append(v);
      } else {
        opcode(c, 0x81, 0xe0 + regCode(b));
        c->code.append4(v);
      }
    } else {
      Assembler::Register tmp
        (c->client->acquireTemporary(GeneralRegisterMask));
      moveCR(c, aSize, a, aSize, &tmp);
      andRR(c, aSize, &tmp, bSize, b);
      c->client->releaseTemporary(tmp.low);
    }
  }
}

void
orRR(Context* c, unsigned aSize, Assembler::Register* a,
     unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  if (TargetBytesPerWord == 4 and aSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    orRR(c, 4, a, 4, b);
    orRR(c, 4, &ah, 4, &bh);
  } else {
    maybeRex(c, aSize, a, b);
    opcode(c, 0x09);
    modrm(c, 0xc0, b, a);
  }
}

void
orCR(Context* c, unsigned aSize, Assembler::Constant* a,
     unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  int64_t v = a->value->value();
  if (v) {
    if (TargetBytesPerWord == 4 and bSize == 8) {
      ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
      Assembler::Constant ah(&high);

      ResolvedPromise low(v & 0xFFFFFFFF);
      Assembler::Constant al(&low);

      Assembler::Register bh(b->high);

      orCR(c, 4, &al, 4, b);
      orCR(c, 4, &ah, 4, &bh);
    } else {
      if (isInt32(v)) {
        maybeRex(c, aSize, b);
        if (isInt8(v)) {
          opcode(c, 0x83, 0xc8 + regCode(b));
          c->code.append(v);
        } else {
          opcode(c, 0x81, 0xc8 + regCode(b));
          c->code.append4(v);        
        }
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, aSize, a, aSize, &tmp);
        orRR(c, aSize, &tmp, bSize, b);
        c->client->releaseTemporary(tmp.low);
      }
    }
  }
}

void
xorRR(Context* c, unsigned aSize, Assembler::Register* a,
      unsigned bSize UNUSED, Assembler::Register* b)
{
  if (TargetBytesPerWord == 4 and aSize == 8) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    xorRR(c, 4, a, 4, b);
    xorRR(c, 4, &ah, 4, &bh);
  } else {
    maybeRex(c, aSize, a, b);
    opcode(c, 0x31);
    modrm(c, 0xc0, b, a);
  }
}

void
xorCR(Context* c, unsigned aSize, Assembler::Constant* a,
      unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  int64_t v = a->value->value();
  if (v) {
    if (TargetBytesPerWord == 4 and bSize == 8) {
      ResolvedPromise high((v >> 32) & 0xFFFFFFFF);
      Assembler::Constant ah(&high);

      ResolvedPromise low(v & 0xFFFFFFFF);
      Assembler::Constant al(&low);

      Assembler::Register bh(b->high);

      xorCR(c, 4, &al, 4, b);
      xorCR(c, 4, &ah, 4, &bh);
    } else {
      if (isInt32(v)) {
        maybeRex(c, aSize, b);
        if (isInt8(v)) {
          opcode(c, 0x83, 0xf0 + regCode(b));
          c->code.append(v);
        } else {
          opcode(c, 0x81, 0xf0 + regCode(b));
          c->code.append4(v);        
        }
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, aSize, a, aSize, &tmp);
        xorRR(c, aSize, &tmp, bSize, b);
        c->client->releaseTemporary(tmp.low);
      }
    }
  }
}

void
multiplyRR(Context* c, unsigned aSize, Assembler::Register* a,
           unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);


  if (TargetBytesPerWord == 4 and aSize == 8) {
    assert(c, b->high == rdx);
    assert(c, b->low != rax);
    assert(c, a->low != rax);
    assert(c, a->high != rax);

    c->client->save(rax);

    Assembler::Register axdx(rax, rdx);
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    Assembler::Register tmp(-1);
    Assembler::Register* scratch;
    if (a->low == b->low) {
      tmp.low = c->client->acquireTemporary
        (GeneralRegisterMask & ~(1 << rax));
      scratch = &tmp;
      moveRR(c, 4, b, 4, scratch);
    } else {
      scratch = b;
    }

    moveRR(c, 4, b, 4, &axdx);
    multiplyRR(c, 4, &ah, 4, scratch);
    multiplyRR(c, 4, a, 4, &bh);
    addRR(c, 4, &bh, 4, scratch);
    
    // mul a->low,%eax%edx
    opcode(c, 0xf7, 0xe0 + a->low);
    
    addRR(c, 4, scratch, 4, &bh);
    moveRR(c, 4, &axdx, 4, b);

    if (tmp.low != -1) {
      c->client->releaseTemporary(tmp.low);
    }
  } else {
    maybeRex(c, aSize, b, a);
    opcode(c, 0x0f, 0xaf);
    modrm(c, 0xc0, a, b);
  }
}

void
branch(Context* c, TernaryOperation op, Assembler::Constant* target)
{
  switch (op) {
  case JumpIfEqual:
    conditional(c, 0x84, target);
    break;

  case JumpIfNotEqual:
    conditional(c, 0x85, target);
    break;

  case JumpIfLess:
    conditional(c, 0x8c, target);
    break;

  case JumpIfGreater:
    conditional(c, 0x8f, target);
    break;

  case JumpIfLessOrEqual:
    conditional(c, 0x8e, target);
    break;

  case JumpIfGreaterOrEqual:
    conditional(c, 0x8d, target);
    break;

  default:
    abort(c);
  }
}

void
branchFloat(Context* c, TernaryOperation op, Assembler::Constant* target)
{
  switch (op) {
  case JumpIfFloatEqual:
    conditional(c, 0x84, target);
    break;

  case JumpIfFloatNotEqual:
    conditional(c, 0x85, target);
    break;

  case JumpIfFloatLess:
    conditional(c, 0x82, target);
    break;

  case JumpIfFloatGreater:
    conditional(c, 0x87, target);
    break;

  case JumpIfFloatLessOrEqual:
    conditional(c, 0x86, target);
    break;

  case JumpIfFloatGreaterOrEqual:
    conditional(c, 0x83, target);
    break;

  case JumpIfFloatLessOrUnordered:
    conditional(c, 0x82, target);
    conditional(c, 0x8a, target);
    break;

  case JumpIfFloatGreaterOrUnordered:
    conditional(c, 0x87, target);
    conditional(c, 0x8a, target);
    break;

  case JumpIfFloatLessOrEqualOrUnordered:
    conditional(c, 0x86, target);
    conditional(c, 0x8a, target);
    break;

  case JumpIfFloatGreaterOrEqualOrUnordered:
    conditional(c, 0x83, target);
    conditional(c, 0x8a, target);
    break;

  default:
    abort(c);
  }
}

void
compareRR(Context* c, unsigned aSize, Assembler::Register* a,
          unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);
  assert(c, aSize <= TargetBytesPerWord);

  maybeRex(c, aSize, a, b);
  opcode(c, 0x39);
  modrm(c, 0xc0, b, a);  
}

void
compareCR(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);
  assert(c, TargetBytesPerWord == 8 or aSize == 4);
  
  if (a->value->resolved() and isInt32(a->value->value())) {
    int64_t v = a->value->value();
    maybeRex(c, aSize, b);
    if (isInt8(v)) {
      opcode(c, 0x83, 0xf8 + regCode(b));
      c->code.append(v);
    } else {
      opcode(c, 0x81, 0xf8 + regCode(b));
      c->code.append4(v);
    }
  } else {
    Assembler::Register tmp(c->client->acquireTemporary(GeneralRegisterMask));
    moveCR(c, aSize, a, aSize, &tmp);
    compareRR(c, aSize, &tmp, bSize, b);
    c->client->releaseTemporary(tmp.low);
  }
}

void
compareRM(Context* c, unsigned aSize, Assembler::Register* a,
          unsigned bSize UNUSED, Assembler::Memory* b)
{
  assert(c, aSize == bSize);
  assert(c, TargetBytesPerWord == 8 or aSize == 4);
  
  if (TargetBytesPerWord == 8 and aSize == 4) {
    moveRR(c, 4, a, 8, a);
  }
  maybeRex(c, bSize, a, b);
  opcode(c, 0x39);
  modrmSibImm(c, a, b);
}

void
compareCM(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Memory* b)
{
  assert(c, aSize == bSize);
  assert(c, TargetBytesPerWord == 8 or aSize == 4);
  
  if (a->value->resolved()) { 
    int64_t v = a->value->value();   
    maybeRex(c, aSize, b);
    opcode(c, isInt8(v) ? 0x83 : 0x81);
    modrmSibImm(c, rdi, b->scale, b->index, b->base, b->offset);
    
    if (isInt8(v)) {
      c->code.append(v);
    } else if (isInt32(v)) {
      c->code.append4(v);
    } else {
      abort(c);
    }
  } else {
    Assembler::Register tmp(c->client->acquireTemporary(GeneralRegisterMask));
    moveCR(c, aSize, a, bSize, &tmp);
    compareRM(c, bSize, &tmp, bSize, b);
    c->client->releaseTemporary(tmp.low);
  }
}

void
compareFloatRR(Context* c, unsigned aSize, Assembler::Register* a,
               unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  if (aSize == 8) {
    opcode(c, 0x66);
  }
  maybeRex(c, 4, a, b);
  opcode(c, 0x0f, 0x2e);
  modrm(c, 0xc0, a, b);
}

void
branchLong(Context* c, TernaryOperation op, Assembler::Operand* al,
           Assembler::Operand* ah, Assembler::Operand* bl,
           Assembler::Operand* bh, Assembler::Constant* target,
           BinaryOperationType compare)
{
  compare(c, 4, ah, 4, bh);
  
  unsigned next = 0;

  switch (op) {
  case JumpIfEqual:
    opcode(c, 0x75); // jne
    next = c->code.length();
    c->code.append(0);

    compare(c, 4, al, 4, bl);
    conditional(c, 0x84, target); // je
    break;

  case JumpIfNotEqual:
    conditional(c, 0x85, target); // jne

    compare(c, 4, al, 4, bl);
    conditional(c, 0x85, target); // jne
    break;

  case JumpIfLess:
    conditional(c, 0x8c, target); // jl

    opcode(c, 0x7f); // jg
    next = c->code.length();
    c->code.append(0);

    compare(c, 4, al, 4, bl);
    conditional(c, 0x82, target); // jb
    break;

  case JumpIfGreater:
    conditional(c, 0x8f, target); // jg

    opcode(c, 0x7c); // jl
    next = c->code.length();
    c->code.append(0);

    compare(c, 4, al, 4, bl);
    conditional(c, 0x87, target); // ja
    break;

  case JumpIfLessOrEqual:
    conditional(c, 0x8c, target); // jl

    opcode(c, 0x7f); // jg
    next = c->code.length();
    c->code.append(0);

    compare(c, 4, al, 4, bl);
    conditional(c, 0x86, target); // jbe
    break;

  case JumpIfGreaterOrEqual:
    conditional(c, 0x8f, target); // jg

    opcode(c, 0x7c); // jl
    next = c->code.length();
    c->code.append(0);

    compare(c, 4, al, 4, bl);
    conditional(c, 0x83, target); // jae
    break;

  default:
    abort(c);
  }  

  if (next) {
    int8_t nextOffset = c->code.length() - next - 1;
    c->code.set(next, &nextOffset, 1);
  }
}

void
branchRR(Context* c, TernaryOperation op, unsigned size,
         Assembler::Register* a, Assembler::Register* b,
         Assembler::Constant* target)
{
  if (isFloatBranch(op)) {
    compareFloatRR(c, size, a, size, b);
    branchFloat(c, op, target);
  } else if (size > TargetBytesPerWord) {
    Assembler::Register ah(a->high);
    Assembler::Register bh(b->high);

    branchLong(c, op, a, &ah, b, &bh, target, CAST2(compareRR));
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
  assert(c, not isFloatBranch(op));

  if (size > TargetBytesPerWord) {
    int64_t v = a->value->value();

    ResolvedPromise low(v & ~static_cast<uintptr_t>(0));
    Assembler::Constant al(&low);
  
    ResolvedPromise high((v >> 32) & ~static_cast<uintptr_t>(0));
    Assembler::Constant ah(&high);
  
    Assembler::Register bh(b->high);

    branchLong(c, op, &al, &ah, b, &bh, target, CAST2(compareCR));
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
  assert(c, not isFloatBranch(op));
  assert(c, size <= TargetBytesPerWord);

  compareRM(c, size, a, size, b);
  branch(c, op, target);
}

void
branchCM(Context* c, TernaryOperation op, unsigned size,
         Assembler::Constant* a, Assembler::Memory* b,
         Assembler::Constant* target)
{
  assert(c, not isFloatBranch(op));
  assert(c, size <= TargetBytesPerWord);

  compareCM(c, size, a, size, b);
  branch(c, op, target);
}

void
multiplyCR(Context* c, unsigned aSize, Assembler::Constant* a,
           unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  if (TargetBytesPerWord == 4 and aSize == 8) {
    const uint32_t mask = GeneralRegisterMask & ~((1 << rax) | (1 << rdx));
    Assembler::Register tmp(c->client->acquireTemporary(mask),
                            c->client->acquireTemporary(mask));

    moveCR(c, aSize, a, aSize, &tmp);
    multiplyRR(c, aSize, &tmp, bSize, b);
    c->client->releaseTemporary(tmp.low);
    c->client->releaseTemporary(tmp.high);
  } else {
    int64_t v = a->value->value();
    if (v != 1) {
      if (isInt32(v)) {
        maybeRex(c, bSize, b, b);
        if (isInt8(v)) {
          opcode(c, 0x6b);
          modrm(c, 0xc0, b, b);
          c->code.append(v);
        } else {
          opcode(c, 0x69);
          modrm(c, 0xc0, b, b);
          c->code.append4(v);        
        }
      } else {
        Assembler::Register tmp
          (c->client->acquireTemporary(GeneralRegisterMask));
        moveCR(c, aSize, a, aSize, &tmp);
        multiplyRR(c, aSize, &tmp, bSize, b);
        c->client->releaseTemporary(tmp.low);      
      }
    }
  }
}

void
divideRR(Context* c, unsigned aSize, Assembler::Register* a,
         unsigned bSize UNUSED, Assembler::Register* b UNUSED)
{
  assert(c, aSize == bSize);

  assert(c, b->low == rax);
  assert(c, a->low != rdx);

  c->client->save(rdx);
    
  maybeRex(c, aSize, a, b);
  opcode(c, 0x99); // cdq
  maybeRex(c, aSize, b, a);
  opcode(c, 0xf7, 0xf8 + regCode(a));
}

void
remainderRR(Context* c, unsigned aSize, Assembler::Register* a,
            unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == bSize);

  assert(c, b->low == rax);
  assert(c, a->low != rdx);

  c->client->save(rdx);
    
  maybeRex(c, aSize, a, b);
  opcode(c, 0x99); // cdq
  maybeRex(c, aSize, b, a);
  opcode(c, 0xf7, 0xf8 + regCode(a));

  Assembler::Register dx(rdx);
  moveRR(c, TargetBytesPerWord, &dx, TargetBytesPerWord, b);
}

void
doShift(Context* c, UNUSED void (*shift)
        (Context*, unsigned, Assembler::Register*, unsigned,
         Assembler::Register*),
        int type, UNUSED unsigned aSize, Assembler::Constant* a,
        unsigned bSize, Assembler::Register* b)
{
  int64_t v = a->value->value();

  if (TargetBytesPerWord == 4 and bSize == 8) {
    c->client->save(rcx);

    Assembler::Register cx(rcx);
    moveCR(c, 4, a, 4, &cx);
    shift(c, aSize, &cx, bSize, b);
  } else {
    maybeRex(c, bSize, b);
    if (v == 1) {
      opcode(c, 0xd1, type + regCode(b));
    } else if (isInt8(v)) {
      opcode(c, 0xc1, type + regCode(b));
      c->code.append(v);
    } else {
      abort(c);
    }
  }
}

void
shiftLeftRR(Context* c, UNUSED unsigned aSize, Assembler::Register* a,
            unsigned bSize, Assembler::Register* b)
{
  assert(c, a->low == rcx);
  
  if (TargetBytesPerWord == 4 and bSize == 8) {
    // shld
    opcode(c, 0x0f, 0xa5);
    modrm(c, 0xc0, b->high, b->low);

    // shl
    opcode(c, 0xd3, 0xe0 + b->low);

    ResolvedPromise promise(32);
    Assembler::Constant constant(&promise);
    compareCR(c, aSize, &constant, aSize, a);

    opcode(c, 0x7c); //jl
    c->code.append(2 + 2);

    Assembler::Register bh(b->high);
    moveRR(c, 4, b, 4, &bh); // 2 bytes
    xorRR(c, 4, b, 4, b); // 2 bytes
  } else {
    maybeRex(c, bSize, a, b);
    opcode(c, 0xd3, 0xe0 + regCode(b));
  }
}

void
shiftLeftCR(Context* c, unsigned aSize, Assembler::Constant* a,
            unsigned bSize, Assembler::Register* b)
{
  doShift(c, shiftLeftRR, 0xe0, aSize, a, bSize, b);
}

void
shiftRightRR(Context* c, UNUSED unsigned aSize, Assembler::Register* a,
             unsigned bSize, Assembler::Register* b)
{
  assert(c, a->low == rcx);
  if (TargetBytesPerWord == 4 and bSize == 8) {
    // shrd
    opcode(c, 0x0f, 0xad);
    modrm(c, 0xc0, b->low, b->high);

    // sar
    opcode(c, 0xd3, 0xf8 + b->high);

    ResolvedPromise promise(32);
    Assembler::Constant constant(&promise);
    compareCR(c, aSize, &constant, aSize, a);

    opcode(c, 0x7c); //jl
    c->code.append(2 + 3);

    Assembler::Register bh(b->high);
    moveRR(c, 4, &bh, 4, b); // 2 bytes

    // sar 31,high
    opcode(c, 0xc1, 0xf8 + b->high);
    c->code.append(31);
  } else {
    maybeRex(c, bSize, a, b);
    opcode(c, 0xd3, 0xf8 + regCode(b));
  }
}

void
shiftRightCR(Context* c, unsigned aSize, Assembler::Constant* a,
             unsigned bSize, Assembler::Register* b)
{
  doShift(c, shiftRightRR, 0xf8, aSize, a, bSize, b);
}

void
unsignedShiftRightRR(Context* c, UNUSED unsigned aSize, Assembler::Register* a,
                     unsigned bSize, Assembler::Register* b)
{
  assert(c, a->low == rcx);

  if (TargetBytesPerWord == 4 and bSize == 8) {
    // shrd
    opcode(c, 0x0f, 0xad);
    modrm(c, 0xc0, b->low, b->high);

    // shr
    opcode(c, 0xd3, 0xe8 + b->high);

    ResolvedPromise promise(32);
    Assembler::Constant constant(&promise);
    compareCR(c, aSize, &constant, aSize, a);

    opcode(c, 0x7c); //jl
    c->code.append(2 + 2);

    Assembler::Register bh(b->high);
    moveRR(c, 4, &bh, 4, b); // 2 bytes
    xorRR(c, 4, &bh, 4, &bh); // 2 bytes
  } else {
    maybeRex(c, bSize, a, b);
    opcode(c, 0xd3, 0xe8 + regCode(b));
  }
}

void
unsignedShiftRightCR(Context* c, unsigned aSize UNUSED, Assembler::Constant* a,
                     unsigned bSize, Assembler::Register* b)
{
  doShift(c, unsignedShiftRightRR, 0xe8, aSize, a, bSize, b);
}

void
floatRegOp(Context* c, unsigned aSize, Assembler::Register* a, unsigned bSize,
           Assembler::Register* b, uint8_t op, uint8_t mod = 0xc0)
{
  if (aSize == 4) {
    opcode(c, 0xf3);
  } else {
    opcode(c, 0xf2);
  }
  maybeRex(c, bSize, b, a);
  opcode(c, 0x0f, op);
  modrm(c, mod, a, b);
}

void
floatMemOp(Context* c, unsigned aSize, Assembler::Memory* a, unsigned bSize,
           Assembler::Register* b, uint8_t op)
{
  if (aSize == 4) {
    opcode(c, 0xf3);
  } else {
    opcode(c, 0xf2);
  }
  maybeRex(c, bSize, b, a);
  opcode(c, 0x0f, op);
  modrmSibImm(c, b, a);
}

void
floatSqrtRR(Context* c, unsigned aSize, Assembler::Register* a,
            unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x51);
}

void
floatSqrtMR(Context* c, unsigned aSize, Assembler::Memory* a,
            unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x51);
}

void
floatAddRR(Context* c, unsigned aSize, Assembler::Register* a,
           unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x58);
}

void
floatAddMR(Context* c, unsigned aSize, Assembler::Memory* a,
           unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x58);
}

void
floatSubtractRR(Context* c, unsigned aSize, Assembler::Register* a,
                unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x5c);
}

void
floatSubtractMR(Context* c, unsigned aSize, Assembler::Memory* a,
                unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x5c);
}

void
floatMultiplyRR(Context* c, unsigned aSize, Assembler::Register* a,
                unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x59);
}

void
floatMultiplyMR(Context* c, unsigned aSize, Assembler::Memory* a,
                unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x59);
}

void
floatDivideRR(Context* c, unsigned aSize, Assembler::Register* a,
              unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x5e);
}

void
floatDivideMR(Context* c, unsigned aSize, Assembler::Memory* a,
              unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x5e);
}

void
float2FloatRR(Context* c, unsigned aSize, Assembler::Register* a,
              unsigned bSize UNUSED, Assembler::Register* b)
{
  floatRegOp(c, aSize, a, 4, b, 0x5a);
}

void
float2FloatMR(Context* c, unsigned aSize, Assembler::Memory* a,
              unsigned bSize UNUSED, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, 4, b, 0x5a);
}

void
float2IntRR(Context* c, unsigned aSize, Assembler::Register* a,
            unsigned bSize, Assembler::Register* b)
{
  assert(c, not floatReg(b));
  floatRegOp(c, aSize, a, bSize, b, 0x2c);
}

void
float2IntMR(Context* c, unsigned aSize, Assembler::Memory* a,
            unsigned bSize, Assembler::Register* b)
{
  floatMemOp(c, aSize, a, bSize, b, 0x2c);
}

void
int2FloatRR(Context* c, unsigned aSize, Assembler::Register* a,
            unsigned bSize, Assembler::Register* b)
{
  floatRegOp(c, bSize, a, aSize, b, 0x2a);
}

void
int2FloatMR(Context* c, unsigned aSize, Assembler::Memory* a,
            unsigned bSize, Assembler::Register* b)
{
  floatMemOp(c, bSize, a, aSize, b, 0x2a);
}

void
floatNegateRR(Context* c, unsigned aSize, Assembler::Register* a,
              unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, floatReg(a) and floatReg(b));
  // unlike most of the other floating point code, this does NOT
  // support doubles:
  assert(c, aSize == 4);
  ResolvedPromise pcon(0x80000000);
  Assembler::Constant con(&pcon);
  if (a->low == b->low) {
    Assembler::Register tmp(c->client->acquireTemporary(FloatRegisterMask));
    moveCR(c, 4, &con, 4, &tmp);
    maybeRex(c, 4, a, &tmp);
    opcode(c, 0x0f, 0x57);
    modrm(c, 0xc0, &tmp, a);
    c->client->releaseTemporary(tmp.low);
  } else {
    moveCR(c, 4, &con, 4, b);
    if (aSize == 8) opcode(c, 0x66);
    maybeRex(c, 4, a, b);
    opcode(c, 0x0f, 0x57);
    modrm(c, 0xc0, a, b);
  }
}

void
floatAbsoluteRR(Context* c, unsigned aSize UNUSED, Assembler::Register* a,
           unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, floatReg(a) and floatReg(b));
  // unlike most of the other floating point code, this does NOT
  // support doubles:
  assert(c, aSize == 4);
  ResolvedPromise pcon(0x7fffffff);
  Assembler::Constant con(&pcon);
  if (a->low == b->low) {
    Assembler::Register tmp(c->client->acquireTemporary(FloatRegisterMask));
    moveCR(c, 4, &con, 4, &tmp);
    maybeRex(c, 4, a, &tmp);
    opcode(c, 0x0f, 0x54);
    modrm(c, 0xc0, &tmp, a);
    c->client->releaseTemporary(tmp.low);
  } else {
    moveCR(c, 4, &con, 4, b);
    maybeRex(c, 4, a, b);
    opcode(c, 0x0f, 0x54);
    modrm(c, 0xc0, a, b);
  }
}

void
absoluteRR(Context* c, unsigned aSize, Assembler::Register* a,
      unsigned bSize UNUSED, Assembler::Register* b UNUSED)
{
  assert(c, aSize == bSize and a->low == rax and b->low == rax);
  Assembler::Register d
    (c->client->acquireTemporary(static_cast<uint64_t>(1) << rdx));
  maybeRex(c, aSize, a, b);
  opcode(c, 0x99);
  xorRR(c, aSize, &d, aSize, a);
  subtractRR(c, aSize, &d, aSize, a);
  c->client->releaseTemporary(rdx);
}

unsigned
argumentFootprint(unsigned footprint)
{
  return max(pad(footprint, StackAlignmentInWords), StackAlignmentInWords);
}

uint32_t
read4(uint8_t* p)
{
  uint32_t v; memcpy(&v, p, 4);
  return v;
}

void
nextFrame(ArchitectureContext* c UNUSED, uint8_t* start, unsigned size UNUSED,
          unsigned footprint, void*, void* stackLimit,
          unsigned targetParameterFootprint, void** ip, void** stack)
{
  assert(c, *ip >= start);
  assert(c, *ip <= start + size);

  uint8_t* instruction = static_cast<uint8_t*>(*ip);

  // skip stack overflow check, if present:
  if (TargetBytesPerWord == 4) {
    if (*start == 0x39) {
      start += 11;
    }
  } else if (*start == 0x48 and start[1] == 0x39) {
    start += 12;
  }

  if (instruction <= start) {
    *ip = static_cast<void**>(*stack)[0];
    return;
  }

  if (UseFramePointer) {
    // skip preamble
    start += (TargetBytesPerWord == 4 ? 3 : 4);

    if (instruction <= start or *instruction == 0x5d) {
      *ip = static_cast<void**>(*stack)[1];
      *stack = static_cast<void**>(*stack) + 1;
      return;
    }
  }

  if (*instruction == 0xc3) { // return
    *ip = static_cast<void**>(*stack)[0];
    return;
  }

  unsigned offset = footprint + FrameHeaderSize
    - (stackLimit == *stack ? 1 : 0);

  if (TailCalls) {
    if (argumentFootprint(targetParameterFootprint) > StackAlignmentInWords) {
      offset += argumentFootprint(targetParameterFootprint)
        - StackAlignmentInWords;
    }

    // check for post-non-tail-call stack adjustment of the form "add
    // $offset,%rsp":
    if (TargetBytesPerWord == 4) {
      if ((*instruction == 0x83 or *instruction == 0x81)
          and instruction[1] == 0xec)
      {
        offset
          -= (*instruction == 0x83 ? instruction[2] : read4(instruction + 2))
          / TargetBytesPerWord;
      }
    } else if (*instruction == 0x48
               and (instruction[1] == 0x83 or instruction[1] == 0x81)
               and instruction[2] == 0xec)
    {
      offset
        -= (instruction[1] == 0x83 ? instruction[3] : read4(instruction + 3))
        / TargetBytesPerWord;
    }

    // todo: check for and handle tail calls
  }

  *ip = static_cast<void**>(*stack)[offset];
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
  BranchOperationType* bro = c->branchOperations;

  zo[Return] = return_;
  zo[LoadBarrier] = ignore;
  zo[StoreStoreBarrier] = ignore;
  zo[StoreLoadBarrier] = storeLoadBarrier;

  uo[index(c, Call, C)] = CAST1(callC);
  uo[index(c, Call, R)] = CAST1(callR);
  uo[index(c, Call, M)] = CAST1(callM);

  uo[index(c, AlignedCall, C)] = CAST1(alignedCallC);

  uo[index(c, LongCall, C)] = CAST1(longCallC);

  uo[index(c, AlignedLongCall, C)] = CAST1(alignedLongCallC);

  uo[index(c, Jump, R)] = CAST1(jumpR);
  uo[index(c, Jump, C)] = CAST1(jumpC);
  uo[index(c, Jump, M)] = CAST1(jumpM);

  uo[index(c, AlignedJump, C)] = CAST1(alignedJumpC);

  uo[index(c, LongJump, C)] = CAST1(longJumpC);

  uo[index(c, AlignedLongJump, C)] = CAST1(alignedLongJumpC);

  bo[index(c, Negate, R, R)] = CAST2(negateRR);

  bo[index(c, FloatNegate, R, R)] = CAST2(floatNegateRR);

  bo[index(c, Move, R, R)] = CAST2(moveRR);
  bo[index(c, Move, C, R)] = CAST2(moveCR);
  bo[index(c, Move, M, R)] = CAST2(moveMR);
  bo[index(c, Move, R, M)] = CAST2(moveRM);
  bo[index(c, Move, C, M)] = CAST2(moveCM);
  bo[index(c, Move, A, R)] = CAST2(moveAR);

  bo[index(c, FloatSquareRoot, R, R)] = CAST2(floatSqrtRR);
  bo[index(c, FloatSquareRoot, M, R)] = CAST2(floatSqrtMR);

  bo[index(c, MoveZ, R, R)] = CAST2(moveZRR);
  bo[index(c, MoveZ, M, R)] = CAST2(moveZMR);

  bo[index(c, Add, R, R)] = CAST2(addRR);
  bo[index(c, Add, C, R)] = CAST2(addCR);

  bo[index(c, Subtract, C, R)] = CAST2(subtractCR);
  bo[index(c, Subtract, R, R)] = CAST2(subtractRR);

  bo[index(c, FloatAdd, R, R)] = CAST2(floatAddRR);
  bo[index(c, FloatAdd, M, R)] = CAST2(floatAddMR);

  bo[index(c, FloatSubtract, R, R)] = CAST2(floatSubtractRR);
  bo[index(c, FloatSubtract, M, R)] = CAST2(floatSubtractMR);

  bo[index(c, And, R, R)] = CAST2(andRR);
  bo[index(c, And, C, R)] = CAST2(andCR);

  bo[index(c, Or, R, R)] = CAST2(orRR);
  bo[index(c, Or, C, R)] = CAST2(orCR);

  bo[index(c, Xor, R, R)] = CAST2(xorRR);
  bo[index(c, Xor, C, R)] = CAST2(xorCR);

  bo[index(c, Multiply, R, R)] = CAST2(multiplyRR);
  bo[index(c, Multiply, C, R)] = CAST2(multiplyCR);

  bo[index(c, Divide, R, R)] = CAST2(divideRR);

  bo[index(c, FloatMultiply, R, R)] = CAST2(floatMultiplyRR);
  bo[index(c, FloatMultiply, M, R)] = CAST2(floatMultiplyMR);

  bo[index(c, FloatDivide, R, R)] = CAST2(floatDivideRR);
  bo[index(c, FloatDivide, M, R)] = CAST2(floatDivideMR);

  bo[index(c, Remainder, R, R)] = CAST2(remainderRR);

  bo[index(c, ShiftLeft, R, R)] = CAST2(shiftLeftRR);
  bo[index(c, ShiftLeft, C, R)] = CAST2(shiftLeftCR);

  bo[index(c, ShiftRight, R, R)] = CAST2(shiftRightRR);
  bo[index(c, ShiftRight, C, R)] = CAST2(shiftRightCR);

  bo[index(c, UnsignedShiftRight, R, R)] = CAST2(unsignedShiftRightRR);
  bo[index(c, UnsignedShiftRight, C, R)] = CAST2(unsignedShiftRightCR);

  bo[index(c, Float2Float, R, R)] = CAST2(float2FloatRR);
  bo[index(c, Float2Float, M, R)] = CAST2(float2FloatMR);

  bo[index(c, Float2Int, R, R)] = CAST2(float2IntRR);
  bo[index(c, Float2Int, M, R)] = CAST2(float2IntMR);

  bo[index(c, Int2Float, R, R)] = CAST2(int2FloatRR);
  bo[index(c, Int2Float, M, R)] = CAST2(int2FloatMR);

  bo[index(c, Absolute, R, R)] = CAST2(absoluteRR);
  bo[index(c, FloatAbsolute, R, R)] = CAST2(floatAbsoluteRR);

  bro[branchIndex(c, R, R)] = CAST_BRANCH(branchRR);
  bro[branchIndex(c, C, R)] = CAST_BRANCH(branchCR);
  bro[branchIndex(c, C, M)] = CAST_BRANCH(branchCM);
  bro[branchIndex(c, R, M)] = CAST_BRANCH(branchRM);
}

class MyArchitecture: public Assembler::Architecture {
 public:
  MyArchitecture(System* system, bool useNativeFeatures):
    c(system, useNativeFeatures), referenceCount(0)
  {
    populateTables(&c);
  }

  virtual unsigned floatRegisterSize() {
    if (useSSE(&c)) {
      return 8;
    } else {
      return 0;
    }
  }
  
  virtual uint32_t generalRegisterMask() {
    return GeneralRegisterMask;
  }
  
  virtual uint32_t floatRegisterMask() {
    return useSSE(&c) ? FloatRegisterMask : 0;
  }

  virtual int scratch() {
    return rax;
  }

  virtual int stack() {
    return rsp;
  }

  virtual int thread() {
    return rbx;
  }

  virtual int returnLow() {
    return rax;
  }

  virtual int returnHigh() {
    return (TargetBytesPerWord == 4 ? rdx : NoRegister);
  }

  virtual int virtualCallTarget() {
    return rax;
  }

  virtual int virtualCallIndex() {
    return rdx;
  }

  virtual bool bigEndian() {
    return false;
  }

  virtual uintptr_t maximumImmediateJump() {
    return 0x7FFFFFFF;
  }

  virtual bool reserved(int register_) {
    switch (register_) {
    case rbp:
      return UseFramePointer;

    case rsp:
    case rbx:
      return true;
   	  
    default:
      return false;
    }
  }

  virtual unsigned frameFootprint(unsigned footprint) {
#ifdef TARGET_PLATFORM_WINDOWS
    return max(footprint, StackAlignmentInWords);
#else
    return max(footprint > argumentRegisterCount() ?
               footprint - argumentRegisterCount() : 0,
               StackAlignmentInWords);
#endif
  }

  virtual unsigned argumentFootprint(unsigned footprint) {
    return local::argumentFootprint(footprint);
  }

  virtual bool argumentAlignment() {
    return false;
  }

  virtual bool argumentRegisterAlignment() {
    return false;
  }

  virtual unsigned argumentRegisterCount() {
#ifdef TARGET_PLATFORM_WINDOWS
    if (TargetBytesPerWord == 8) return 4; else
#else
    if (TargetBytesPerWord == 8) return 6; else
#endif
    return 0;
  }

  virtual int argumentRegister(unsigned index) {
    assert(&c, TargetBytesPerWord == 8);
    switch (index) {
#ifdef TARGET_PLATFORM_WINDOWS
    case 0:
      return rcx;
    case 1:
      return rdx;
    case 2:
      return r8;
    case 3:
      return r9;
#else
    case 0:
      return rdi;
    case 1:
      return rsi;
    case 2:
      return rdx;
    case 3:
      return rcx;
    case 4:
      return r8;
    case 5:
      return r9;
#endif
    default:
      abort(&c);
    }
  }

  virtual bool hasLinkRegister() {
    return false;
  }

  virtual unsigned stackAlignmentInWords() {
    return StackAlignmentInWords;
  }

  virtual bool matchCall(void* returnAddress, void* target) {
    uint8_t* instruction = static_cast<uint8_t*>(returnAddress) - 5;
    int32_t actualOffset; memcpy(&actualOffset, instruction + 1, 4);
    void* actualTarget = static_cast<uint8_t*>(returnAddress) + actualOffset;

    return *instruction == 0xE8 and actualTarget == target;
  }

  virtual void updateCall(UnaryOperation op, void* returnAddress,
                          void* newTarget)
  {
    bool assertAlignment UNUSED;
    switch (op) {
    case AlignedCall:
      op = Call;
      assertAlignment = true;
      break;

    case AlignedJump:
      op = Jump;
      assertAlignment = true;
      break;

    case AlignedLongCall:
      op = LongCall;
      assertAlignment = true;
      break;

    case AlignedLongJump:
      op = LongJump;
      assertAlignment = true;
      break;

    default:
      assertAlignment = false;
    }

    if (TargetBytesPerWord == 4 or op == Call or op == Jump) {
      uint8_t* instruction = static_cast<uint8_t*>(returnAddress) - 5;
      
      assert(&c, ((op == Call or op == LongCall) and *instruction == 0xE8)
             or ((op == Jump or op == LongJump) and *instruction == 0xE9));

      assert(&c, (not assertAlignment)
             or reinterpret_cast<uintptr_t>(instruction + 1) % 4 == 0);
      
      intptr_t v = static_cast<uint8_t*>(newTarget)
        - static_cast<uint8_t*>(returnAddress);

      assert(&c, isInt32(v));

      int32_t v32 = v;

      memcpy(instruction + 1, &v32, 4);
    } else {
      uint8_t* instruction = static_cast<uint8_t*>(returnAddress) - 13;

      assert(&c, instruction[0] == 0x49 and instruction[1] == 0xBA);
      assert(&c, instruction[10] == 0x41 and instruction[11] == 0xFF);
      assert(&c, (op == LongCall and instruction[12] == 0xD2)
             or (op == LongJump and instruction[12] == 0xE2));

      assert(&c, (not assertAlignment)
             or reinterpret_cast<uintptr_t>(instruction + 2) % 8 == 0);
      
      memcpy(instruction + 2, &newTarget, 8);
    }
  }

  virtual void setConstant(void* dst, uint64_t constant) {
    target_uintptr_t v = targetVW(constant);
    memcpy(dst, &v, TargetBytesPerWord);
  }

  virtual unsigned alignFrameSize(unsigned sizeInWords) {
    return pad(sizeInWords + FrameHeaderSize, StackAlignmentInWords)
      - FrameHeaderSize;
  }

  virtual void nextFrame(void* start, unsigned size, unsigned footprint,
                         void* link, void* stackLimit,
                         unsigned targetParameterFootprint, void** ip,
                         void** stack)
  {
    local::nextFrame(&c, static_cast<uint8_t*>(start), size, footprint,
                     link, stackLimit, targetParameterFootprint, ip, stack);
  }

  virtual void* frameIp(void* stack) {
    return stack ? *static_cast<void**>(stack) : 0;
  }

  virtual unsigned frameHeaderSize() {
    return FrameHeaderSize;
  }

  virtual unsigned frameReturnAddressSize() {
    return 1;
  }

  virtual unsigned frameFooterSize() {
    return 0;
  }

  virtual bool alwaysCondensed(BinaryOperation op) {
    switch(op) {
    case Float2Float:
    case Float2Int:
    case Int2Float:
    case FloatAbsolute:
    case FloatNegate:
    case FloatSquareRoot:
      return false;

    case Negate:
    case Absolute:
      return true;

    default:
      abort(&c);
    }
  }
  
  virtual bool alwaysCondensed(TernaryOperation) {
    return true;
  }

  virtual int returnAddressOffset() {
    return 0;
  }

  virtual int framePointerOffset() {
    return UseFramePointer ? -1 : 0;
  }

  virtual void plan
  (UnaryOperation,
   unsigned, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   bool* thunk)
  {
    *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand)
      | (1 << ConstantOperand);
    *aRegisterMask = ~static_cast<uint64_t>(0);
    *thunk = false;
  }

  virtual void planSource
  (BinaryOperation op,
   unsigned aSize, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   unsigned bSize, bool* thunk)
  {
    *aTypeMask = ~0;
    *aRegisterMask = GeneralRegisterMask |
      (static_cast<uint64_t>(GeneralRegisterMask) << 32);

    *thunk = false;

    switch (op) {
    case Negate:
      *aTypeMask = (1 << RegisterOperand);
      *aRegisterMask = (static_cast<uint64_t>(1) << (rdx + 32))
        | (static_cast<uint64_t>(1) << rax);
      break;

    case Absolute:
      if (aSize <= TargetBytesPerWord) {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = (static_cast<uint64_t>(1) << rax);
      } else {
        *thunk = true;
      }
      break;

    case FloatAbsolute:
      if (useSSE(&c)) {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
      } else {
        *thunk = true;
      }
      break;  
  
    case FloatNegate:
      // floatNegateRR does not support doubles
      if (useSSE(&c) and aSize == 4 and bSize == 4) {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = FloatRegisterMask;
      } else {
        *thunk = true;
      }
      break;

    case FloatSquareRoot:
      if (useSSE(&c)) {
        *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        *aRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
      } else {
        *thunk = true;
      }
      break;

    case Float2Float:
      if (useSSE(&c)) {
        *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        *aRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
      } else {
        *thunk = true;
      }
      break;

    case Float2Int:
      if (useSSE(&c) and bSize <= TargetBytesPerWord) {
        *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        *aRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
      } else {
        *thunk = true;
      }
      break;

    case Int2Float:
      if (useSSE(&c) and aSize <= TargetBytesPerWord) {
        *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        *aRegisterMask = GeneralRegisterMask
          | (static_cast<uint64_t>(GeneralRegisterMask) << 32);
      } else {
        *thunk = true;
      }
      break;

    case Move:
      *aTypeMask = ~0;
      *aRegisterMask = ~static_cast<uint64_t>(0);

      if (TargetBytesPerWord == 4) {
        if (aSize == 4 and bSize == 8) {
          *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
          const uint32_t mask
            = GeneralRegisterMask & ~((1 << rax) | (1 << rdx));
          *aRegisterMask = (static_cast<uint64_t>(mask) << 32) | mask;    
        } else if (aSize == 1 or bSize == 1) {
          *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
          const uint32_t mask
            = (1 << rax) | (1 << rcx) | (1 << rdx) | (1 << rbx);
          *aRegisterMask = (static_cast<uint64_t>(mask) << 32) | mask;     
        }
      }
      break;

    default:
      break;
    }
  }

  virtual void planDestination
  (BinaryOperation op, unsigned aSize, uint8_t aTypeMask,
   uint64_t aRegisterMask, unsigned bSize, uint8_t* bTypeMask,
   uint64_t* bRegisterMask)
  {
    *bTypeMask = ~0;
    *bRegisterMask = GeneralRegisterMask
      | (static_cast<uint64_t>(GeneralRegisterMask) << 32);

    switch (op) {
    case Absolute:
      *bTypeMask = (1 << RegisterOperand);
      *bRegisterMask = (static_cast<uint64_t>(1) << rax);
      break;

    case FloatAbsolute:
      *bTypeMask = (1 << RegisterOperand);
      *bRegisterMask = aRegisterMask;
      break;

    case Negate:
      *bTypeMask = (1 << RegisterOperand);
      *bRegisterMask = aRegisterMask;
      break;

    case FloatNegate:
    case FloatSquareRoot:
    case Float2Float:
    case Int2Float:
      *bTypeMask = (1 << RegisterOperand);
      *bRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
        | FloatRegisterMask;
      break;

    case Float2Int:
      *bTypeMask = (1 << RegisterOperand);
      break;

    case Move:
      if (aTypeMask & ((1 << MemoryOperand) | 1 << AddressOperand)) {
        *bTypeMask = (1 << RegisterOperand);
        *bRegisterMask = GeneralRegisterMask
          | (static_cast<uint64_t>(GeneralRegisterMask) << 32)
          | FloatRegisterMask;
      } else if (aTypeMask & (1 << RegisterOperand)) {
        *bTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        if (aRegisterMask & FloatRegisterMask) {
          *bRegisterMask = FloatRegisterMask;          
        } else {
          *bRegisterMask = GeneralRegisterMask
            | (static_cast<uint64_t>(GeneralRegisterMask) << 32);
        }
      } else {
        *bTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
      }

      if (TargetBytesPerWord == 4) {
        if (aSize == 4 and bSize == 8) {
          *bRegisterMask = (static_cast<uint64_t>(1) << (rdx + 32))
            | (static_cast<uint64_t>(1) << rax);
        } else if (aSize == 1 or bSize == 1) {
          const uint32_t mask
            = (1 << rax) | (1 << rcx) | (1 << rdx) | (1 << rbx);
          *bRegisterMask = (static_cast<uint64_t>(mask) << 32) | mask;
        }
      }
      break;

    default:
      break;
    }
  }

  virtual void planMove
  (unsigned size, uint8_t* srcTypeMask, uint64_t* srcRegisterMask,
   uint8_t* tmpTypeMask, uint64_t* tmpRegisterMask,
   uint8_t dstTypeMask, uint64_t dstRegisterMask)
  {
    *srcTypeMask = ~0;
    *srcRegisterMask = ~static_cast<uint64_t>(0);

    *tmpTypeMask = 0;
    *tmpRegisterMask = 0;

    if (dstTypeMask & (1 << MemoryOperand)) {
      // can't move directly from memory to memory
      *srcTypeMask = (1 << RegisterOperand) | (1 << ConstantOperand);
      *tmpTypeMask = 1 << RegisterOperand;
      *tmpRegisterMask = GeneralRegisterMask
        | (static_cast<uint64_t>(GeneralRegisterMask) << 32);
    } else if (dstTypeMask & (1 << RegisterOperand)) {
      if (size > TargetBytesPerWord) {
        // can't move directly from FPR to GPR or vice-versa for
        // values larger than the GPR size
        if (dstRegisterMask & FloatRegisterMask) {
          *srcRegisterMask = FloatRegisterMask
            | (static_cast<uint64_t>(FloatRegisterMask) << 32);
          *tmpTypeMask = 1 << MemoryOperand;          
        } else if (dstRegisterMask & GeneralRegisterMask) {
          *srcRegisterMask = GeneralRegisterMask
            | (static_cast<uint64_t>(GeneralRegisterMask) << 32);
          *tmpTypeMask = 1 << MemoryOperand;
        }
      }
      if (dstRegisterMask & FloatRegisterMask) {
        // can't move directly from constant to FPR
        *srcTypeMask &= ~(1 << ConstantOperand);
        if (size > TargetBytesPerWord) {
          *tmpTypeMask = 1 << MemoryOperand;
        } else {
          *tmpTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
          *tmpRegisterMask = GeneralRegisterMask
            | (static_cast<uint64_t>(GeneralRegisterMask) << 32);
        }
      }
    }
  }

  virtual void planSource
  (TernaryOperation op,
   unsigned aSize, uint8_t *aTypeMask, uint64_t *aRegisterMask,
   unsigned, uint8_t* bTypeMask, uint64_t* bRegisterMask,
   unsigned, bool* thunk)
  {
    *aTypeMask = (1 << RegisterOperand) | (1 << ConstantOperand);
    *aRegisterMask = GeneralRegisterMask
      | (static_cast<uint64_t>(GeneralRegisterMask) << 32);

    *bTypeMask = (1 << RegisterOperand);
    *bRegisterMask = GeneralRegisterMask
      | (static_cast<uint64_t>(GeneralRegisterMask) << 32);

    *thunk = false;

    switch (op) {
    case FloatAdd:
    case FloatSubtract:
    case FloatMultiply:
    case FloatDivide:
      if (useSSE(&c)) {
        *aTypeMask = (1 << RegisterOperand) | (1 << MemoryOperand);
        *bTypeMask = (1 << RegisterOperand);

        const uint64_t mask
          = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
        *aRegisterMask = mask;
        *bRegisterMask = mask;
      } else {
        *thunk = true;
      }
      break;

    case FloatRemainder:
      *thunk = true;
      break;
   	  
    case Multiply:
      if (TargetBytesPerWord == 4 and aSize == 8) { 
        const uint32_t mask = GeneralRegisterMask & ~((1 << rax) | (1 << rdx));
        *aRegisterMask = (static_cast<uint64_t>(mask) << 32) | mask;
        *bRegisterMask = (static_cast<uint64_t>(1) << (rdx + 32)) | mask;
      } else {
        *aRegisterMask = GeneralRegisterMask;
        *bRegisterMask = GeneralRegisterMask;
      }
      break;

    case Divide:
      if (TargetBytesPerWord == 4 and aSize == 8) {
        *thunk = true;        			
      } else {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = GeneralRegisterMask & ~((1 << rax) | (1 << rdx));
        *bRegisterMask = 1 << rax;      
      }
      break;

    case Remainder:
      if (TargetBytesPerWord == 4 and aSize == 8) {
        *thunk = true;
      } else {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = GeneralRegisterMask & ~((1 << rax) | (1 << rdx));
        *bRegisterMask = 1 << rax;
      }
      break;

    case ShiftLeft:
    case ShiftRight:
    case UnsignedShiftRight: {
      *aRegisterMask = (static_cast<uint64_t>(GeneralRegisterMask) << 32)
        | (static_cast<uint64_t>(1) << rcx);
      const uint32_t mask = GeneralRegisterMask & ~(1 << rcx);
      *bRegisterMask = (static_cast<uint64_t>(mask) << 32) | mask;
    } break;

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
      if (useSSE(&c)) {
        *aTypeMask = (1 << RegisterOperand);
        *aRegisterMask = (static_cast<uint64_t>(FloatRegisterMask) << 32)
          | FloatRegisterMask;
        *bTypeMask = *aTypeMask;
        *bRegisterMask = *aRegisterMask;
      } else {
        *thunk = true;
      }
      break;

    default:
      break;
    }
  }

  virtual void planDestination
  (TernaryOperation op, unsigned, uint8_t, uint64_t, unsigned, uint8_t,
   uint64_t bRegisterMask, unsigned, uint8_t* cTypeMask,
   uint64_t* cRegisterMask)
  {
    if (isBranch(op)) {
      *cTypeMask = (1 << ConstantOperand);
      *cRegisterMask = 0;
    } else {
      *cTypeMask = (1 << RegisterOperand);
      *cRegisterMask = bRegisterMask;
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
    c(s, a, zone, &(arch->c)), arch_(arch)
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
    Register stack(rsp);
    Memory stackLimit(rbx, stackLimitOffsetFromThread);
    Constant handlerConstant(resolved(&c, handler));
    branchRM(&c, JumpIfGreaterOrEqual, TargetBytesPerWord, &stack, &stackLimit,
             &handlerConstant);
  }

  virtual void saveFrame(unsigned stackOffset, unsigned) {
    Register stack(rsp);
    Memory stackDst(rbx, stackOffset);
    apply(Move, TargetBytesPerWord, RegisterOperand, &stack,
          TargetBytesPerWord, MemoryOperand, &stackDst);
  }

  virtual void pushFrame(unsigned argumentCount, ...) {
    struct Argument {
      unsigned size;
      OperandType type;
      Operand* operand;
    };
    RUNTIME_ARRAY(Argument, arguments, argumentCount);
    va_list a; va_start(a, argumentCount);
    unsigned footprint = 0;
    for (unsigned i = 0; i < argumentCount; ++i) {
      RUNTIME_ARRAY_BODY(arguments)[i].size = va_arg(a, unsigned);
      RUNTIME_ARRAY_BODY(arguments)[i].type
        = static_cast<OperandType>(va_arg(a, int));
      RUNTIME_ARRAY_BODY(arguments)[i].operand = va_arg(a, Operand*);
      footprint += ceiling
        (RUNTIME_ARRAY_BODY(arguments)[i].size, TargetBytesPerWord);
    }
    va_end(a);

    allocateFrame(arch_->alignFrameSize(footprint));
    
    unsigned offset = 0;
    for (unsigned i = 0; i < argumentCount; ++i) {
      if (i < arch_->argumentRegisterCount()) {
        Register dst(arch_->argumentRegister(i));
        apply(Move,
              RUNTIME_ARRAY_BODY(arguments)[i].size,
              RUNTIME_ARRAY_BODY(arguments)[i].type,
              RUNTIME_ARRAY_BODY(arguments)[i].operand,
              pad(RUNTIME_ARRAY_BODY(arguments)[i].size, TargetBytesPerWord),
              RegisterOperand,
              &dst);
      } else {
        Memory dst(rsp, offset * TargetBytesPerWord);
        apply(Move,
              RUNTIME_ARRAY_BODY(arguments)[i].size,
              RUNTIME_ARRAY_BODY(arguments)[i].type,
              RUNTIME_ARRAY_BODY(arguments)[i].operand,
              pad(RUNTIME_ARRAY_BODY(arguments)[i].size, TargetBytesPerWord),
              MemoryOperand,
              &dst);
        offset += ceiling
          (RUNTIME_ARRAY_BODY(arguments)[i].size, TargetBytesPerWord);
      }
    }
  }

  virtual void allocateFrame(unsigned footprint) {
    Register stack(rsp);

    if (UseFramePointer) {
      Register base(rbp);
      pushR(&c, TargetBytesPerWord, &base);

      apply(Move, TargetBytesPerWord, RegisterOperand, &stack,
            TargetBytesPerWord, RegisterOperand, &base);
    }

    Constant footprintConstant(resolved(&c, footprint * TargetBytesPerWord));
    apply(Subtract, TargetBytesPerWord, ConstantOperand, &footprintConstant,
          TargetBytesPerWord, RegisterOperand, &stack,
          TargetBytesPerWord, RegisterOperand, &stack);
  }

  virtual void adjustFrame(unsigned difference) {
    Register stack(rsp);
    Constant differenceConstant(resolved(&c, difference * TargetBytesPerWord));
    apply(Subtract, TargetBytesPerWord, ConstantOperand, &differenceConstant,
          TargetBytesPerWord, RegisterOperand, &stack,
          TargetBytesPerWord, RegisterOperand, &stack);
  }

  virtual void popFrame(unsigned frameFootprint) {
    if (UseFramePointer) {
      Register base(rbp);
      Register stack(rsp);
      apply(Move, TargetBytesPerWord, RegisterOperand, &base,
            TargetBytesPerWord, RegisterOperand, &stack);

      popR(&c, TargetBytesPerWord, &base);
    } else {
      Register stack(rsp);
      Constant footprint(resolved(&c, frameFootprint * TargetBytesPerWord));
      apply(Add, TargetBytesPerWord, ConstantOperand, &footprint,
            TargetBytesPerWord, RegisterOperand, &stack,
            TargetBytesPerWord, RegisterOperand, &stack);
    }
  }

  virtual void popFrameForTailCall(unsigned frameFootprint,
                                   int offset,
                                   int returnAddressSurrogate,
                                   int framePointerSurrogate)
  {
    if (TailCalls) {
      if (offset) {
        Register tmp(c.client->acquireTemporary());
      
        unsigned baseSize = UseFramePointer ? 1 : 0;

        Memory returnAddressSrc
          (rsp, (frameFootprint + baseSize) * TargetBytesPerWord);
        moveMR(&c, TargetBytesPerWord, &returnAddressSrc, TargetBytesPerWord,
               &tmp);
    
        Memory returnAddressDst
          (rsp, (frameFootprint - offset + baseSize) * TargetBytesPerWord);
        moveRM(&c, TargetBytesPerWord, &tmp, TargetBytesPerWord,
               &returnAddressDst);

        c.client->releaseTemporary(tmp.low);

        if (UseFramePointer) {
          Memory baseSrc(rsp, frameFootprint * TargetBytesPerWord);
          Register base(rbp);
          moveMR(&c, TargetBytesPerWord, &baseSrc, TargetBytesPerWord, &base);
        }

        Register stack(rsp);
        Constant footprint
          (resolved
           (&c, (frameFootprint - offset + baseSize) * TargetBytesPerWord));

        addCR(&c, TargetBytesPerWord, &footprint, TargetBytesPerWord, &stack);

        if (returnAddressSurrogate != NoRegister) {
          assert(&c, offset > 0);

          Register ras(returnAddressSurrogate);
          Memory dst(rsp, offset * TargetBytesPerWord);
          moveRM(&c, TargetBytesPerWord, &ras, TargetBytesPerWord, &dst);
        }

        if (framePointerSurrogate != NoRegister) {
          assert(&c, offset > 0);

          Register fps(framePointerSurrogate);
          Memory dst(rsp, (offset - 1) * TargetBytesPerWord);
          moveRM(&c, TargetBytesPerWord, &fps, TargetBytesPerWord, &dst);
        }
      } else {
        popFrame(frameFootprint);
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
      Register returnAddress(rcx);
      popR(&c, TargetBytesPerWord, &returnAddress);

      Register stack(rsp);
      Constant adjustment
        (resolved(&c, (argumentFootprint - StackAlignmentInWords)
                  * TargetBytesPerWord));
      addCR(&c, TargetBytesPerWord, &adjustment, TargetBytesPerWord, &stack);

      jumpR(&c, TargetBytesPerWord, &returnAddress);
    } else {
      return_(&c);
    }
  }

  virtual void popFrameAndUpdateStackAndReturn(unsigned frameFootprint,
                                               unsigned stackOffsetFromThread)
  {
    popFrame(frameFootprint);

    Register returnAddress(rcx);
    popR(&c, TargetBytesPerWord, &returnAddress);

    Register stack(rsp);
    Memory stackSrc(rbx, stackOffsetFromThread);
    moveMR(&c, TargetBytesPerWord, &stackSrc, TargetBytesPerWord, &stack);

    jumpR(&c, TargetBytesPerWord, &returnAddress);
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
                     unsigned bSize, OperandType bType, Operand* bOperand,
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
      assert(&c, bType == cType);

      arch_->c.binaryOperations[index(&(arch_->c), op, aType, bType)]
        (&c, aSize, aOperand, bSize, bOperand);
    }
  }

  virtual void setDestination(uint8_t* dst) {
    c.result = dst;
  }

  virtual void write() {
    uint8_t* dst = c.result;
    for (MyBlock* b = c.firstBlock; b; b = b->next) {
      unsigned index = 0;
      unsigned padding = 0;
      for (AlignmentPadding* p = b->firstPadding; p; p = p->next) {
        unsigned size = p->offset - b->offset - index;

        memcpy(dst + b->start + index + padding,
               c.code.data + b->offset + index,
               size);

        index += size;

        while ((b->start + index + padding + p->instructionOffset)
               % p->alignment)
        {
          *(dst + b->start + index + padding) = 0x90;
          ++ padding;
        }
      }

      memcpy(dst + b->start + index + padding,
             c.code.data + b->offset + index,
             b->size - index);
    }
    
    for (Task* t = c.tasks; t; t = t->next) {
      t->run(&c);
    }
  }

  virtual Promise* offset(bool) {
    return local::offset(&c);
  }

  virtual Block* endBlock(bool startNew) {
    MyBlock* b = c.lastBlock;
    b->size = c.code.length() - b->offset;
    if (startNew) {
      c.lastBlock = new (c.zone->allocate(sizeof(MyBlock)))
        MyBlock(c.code.length());
    } else {
      c.lastBlock = 0;
    }
    return b;
  }

  virtual void endEvent() {
    // ignore
  }

  virtual unsigned length() {
    return c.code.length();
  }

  virtual unsigned footerSize() {
    return 0;
  }

  virtual void dispose() {
    c.code.dispose();
  }

  Context c;
  MyArchitecture* arch_;
};

} // namespace local

} // namespace

namespace vm {

Assembler::Architecture*
makeArchitecture(System* system, bool useNativeFeatures)
{
  return new (allocate(system, sizeof(local::MyArchitecture)))
    local::MyArchitecture(system, useNativeFeatures);
}

Assembler*
makeAssembler(System* system, Allocator* allocator, Zone* zone,
              Assembler::Architecture* architecture)
{
  return new (zone->allocate(sizeof(local::MyAssembler)))
    local::MyAssembler(system, allocator, zone,
                       static_cast<local::MyArchitecture*>(architecture));
}

} // namespace vm

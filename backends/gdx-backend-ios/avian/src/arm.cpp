/* Copyright (c) 2010-2011, Avian Contributors

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
// INSTRUCTION OPTIONS
enum CONDITION { EQ, NE, CS, CC, MI, PL, VS, VC, HI, LS, GE, LT, GT, LE, AL, NV };
enum SHIFTOP { LSL, LSR, ASR, ROR };
// INSTRUCTION FORMATS
inline int DATA(int cond, int opcode, int S, int Rn, int Rd, int shift, int Sh, int Rm)
{ return cond<<28 | opcode<<21 | S<<20 | Rn<<16 | Rd<<12 | shift<<7 | Sh<<5 | Rm; }
inline int DATAS(int cond, int opcode, int S, int Rn, int Rd, int Rs, int Sh, int Rm)
{ return cond<<28 | opcode<<21 | S<<20 | Rn<<16 | Rd<<12 | Rs<<8 | Sh<<5 | 1<<4 | Rm; }
inline int DATAI(int cond, int opcode, int S, int Rn, int Rd, int rot, int imm)
{ return cond<<28 | 1<<25 | opcode<<21 | S<<20 | Rn<<16 | Rd<<12 | rot<<8 | (imm&0xff); }
inline int BRANCH(int cond, int L, int offset)
{ return cond<<28 | 5<<25 | L<<24 | (offset&0xffffff); }
inline int BRANCHX(int cond, int L, int Rm)
{ return cond<<28 | 0x4bffc<<6 | L<<5 | 1<<4 | Rm; }
inline int MULTIPLY(int cond, int mul, int S, int Rd, int Rn, int Rs, int Rm)
{ return cond<<28 | mul<<21 | S<<20 | Rd<<16 | Rn<<12 | Rs<<8 | 9<<4 | Rm; }
inline int XFER(int cond, int P, int U, int B, int W, int L, int Rn, int Rd, int shift, int Sh, int Rm)
{ return cond<<28 | 3<<25 | P<<24 | U<<23 | B<<22 | W<<21 | L<<20 | Rn<<16 | Rd<<12 | shift<<7 | Sh<<5 | Rm; }
inline int XFERI(int cond, int P, int U, int B, int W, int L, int Rn, int Rd, int offset)
{ return cond<<28 | 2<<25 | P<<24 | U<<23 | B<<22 | W<<21 | L<<20 | Rn<<16 | Rd<<12 | (offset&0xfff); }
inline int XFER2(int cond, int P, int U, int W, int L, int Rn, int Rd, int S, int H, int Rm)
{ return cond<<28 | P<<24 | U<<23 | W<<21 | L<<20 | Rn<<16 | Rd<<12 | 1<<7 | S<<6 | H<<5 | 1<<4 | Rm; }
inline int XFER2I(int cond, int P, int U, int W, int L, int Rn, int Rd, int offsetH, int S, int H, int offsetL)
{ return cond<<28 | P<<24 | U<<23 | 1<<22 | W<<21 | L<<20 | Rn<<16 | Rd<<12 | offsetH<<8 | 1<<7 | S<<6 | H<<5 | 1<<4 | (offsetL&0xf); }
inline int BLOCKXFER(int cond, int P, int U, int S, int W, int L, int Rn, int rlist)
{ return cond<<28 | 4<<25 | P<<24 | U<<23 | S<<22 | W<<21 | L<<20 | Rn<<16 | rlist; }
inline int SWI(int cond, int imm)
{ return cond<<28 | 0x0f<<24 | (imm&0xffffff); }
inline int SWAP(int cond, int B, int Rn, int Rd, int Rm)
{ return cond<<28 | 1<<24 | B<<22 | Rn<<16 | Rd<<12 | 9<<4 | Rm; }
// FIELD CALCULATORS
inline int calcU(int imm) { return imm >= 0 ? 1 : 0; }
// INSTRUCTIONS
// The "cond" and "S" fields are set using the SETCOND() and SETS() functions
inline int b(int offset) { return BRANCH(AL, 0, offset); }
inline int bl(int offset) { return BRANCH(AL, 1, offset); }
inline int bx(int Rm) { return BRANCHX(AL, 0, Rm); }
inline int blx(int Rm) { return BRANCHX(AL, 1, Rm); }
inline int swi(int imm) { return SWI(AL, imm); }
inline int and_(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x0, 0, Rn, Rd, shift, Sh, Rm); }
inline int eor(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x1, 0, Rn, Rd, shift, Sh, Rm); }
inline int sub(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x2, 0, Rn, Rd, shift, Sh, Rm); }
inline int rsb(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x3, 0, Rn, Rd, shift, Sh, Rm); }
inline int add(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x4, 0, Rn, Rd, shift, Sh, Rm); }
inline int adc(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x5, 0, Rn, Rd, shift, Sh, Rm); }
inline int sbc(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x6, 0, Rn, Rd, shift, Sh, Rm); }
inline int rsc(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x7, 0, Rn, Rd, shift, Sh, Rm); }
inline int tst(int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x8, 1, Rn, 0, shift, Sh, Rm); }
inline int teq(int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0x9, 1, Rn, 0, shift, Sh, Rm); }
inline int cmp(int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xa, 1, Rn, 0, shift, Sh, Rm); }
inline int cmn(int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xb, 1, Rn, 0, shift, Sh, Rm); }
inline int orr(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xc, 0, Rn, Rd, shift, Sh, Rm); }
inline int mov(int Rd, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xd, 0, 0, Rd, shift, Sh, Rm); }
inline int bic(int Rd, int Rn, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xe, 0, Rn, Rd, shift, Sh, Rm); }
inline int mvn(int Rd, int Rm, int Sh=0, int shift=0) { return DATA(AL, 0xf, 0, 0, Rd, shift, Sh, Rm); }
inline int andi(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x0, 0, Rn, Rd, rot, imm); }
inline int eori(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x1, 0, Rn, Rd, rot, imm); }
inline int subi(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x2, 0, Rn, Rd, rot, imm); }
inline int rsbi(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x3, 0, Rn, Rd, rot, imm); }
inline int addi(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x4, 0, Rn, Rd, rot, imm); }
inline int adci(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0x5, 0, Rn, Rd, rot, imm); }
inline int bici(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0xe, 0, Rn, Rd, rot, imm); }
inline int cmpi(int Rn, int imm, int rot=0) { return DATAI(AL, 0xa, 1, Rn, 0, rot, imm); }
inline int orri(int Rd, int Rn, int imm, int rot=0) { return DATAI(AL, 0xc, 0, Rn, Rd, rot, imm); }
inline int movi(int Rd, int imm, int rot=0) { return DATAI(AL, 0xd, 0, 0, Rd, rot, imm); }
inline int orrsh(int Rd, int Rn, int Rm, int Rs, int Sh) { return DATAS(AL, 0xc, 0, Rn, Rd, Rs, Sh, Rm); }
inline int movsh(int Rd, int Rm, int Rs, int Sh) { return DATAS(AL, 0xd, 0, 0, Rd, Rs, Sh, Rm); }
inline int mul(int Rd, int Rm, int Rs) { return MULTIPLY(AL, 0, 0, Rd, 0, Rs, Rm); }
inline int mla(int Rd, int Rm, int Rs, int Rn) { return MULTIPLY(AL, 1, 0, Rd, Rn, Rs, Rm); }
inline int umull(int RdLo, int RdHi, int Rm, int Rs) { return MULTIPLY(AL, 4, 0, RdHi, RdLo, Rs, Rm); }
inline int umlal(int RdLo, int RdHi, int Rm, int Rs) { return MULTIPLY(AL, 5, 0, RdHi, RdLo, Rs, Rm); }
inline int smull(int RdLo, int RdHi, int Rm, int Rs) { return MULTIPLY(AL, 6, 0, RdHi, RdLo, Rs, Rm); }
inline int smlal(int RdLo, int RdHi, int Rm, int Rs) { return MULTIPLY(AL, 7, 0, RdHi, RdLo, Rs, Rm); }
inline int ldr(int Rd, int Rn, int Rm, int W=0) { return XFER(AL, 1, 1, 0, W, 1, Rn, Rd, 0, 0, Rm); }
inline int ldri(int Rd, int Rn, int imm, int W=0) { return XFERI(AL, 1, calcU(imm), 0, W, 1, Rn, Rd, abs(imm)); }
inline int ldrb(int Rd, int Rn, int Rm) { return XFER(AL, 1, 1, 1, 0, 1, Rn, Rd, 0, 0, Rm); }
inline int ldrbi(int Rd, int Rn, int imm) { return XFERI(AL, 1, calcU(imm), 1, 0, 1, Rn, Rd, abs(imm)); }
inline int str(int Rd, int Rn, int Rm, int W=0) { return XFER(AL, 1, 1, 0, W, 0, Rn, Rd, 0, 0, Rm); }
inline int stri(int Rd, int Rn, int imm, int W=0) { return XFERI(AL, 1, calcU(imm), 0, W, 0, Rn, Rd, abs(imm)); }
inline int strb(int Rd, int Rn, int Rm) { return XFER(AL, 1, 1, 1, 0, 0, Rn, Rd, 0, 0, Rm); }
inline int strbi(int Rd, int Rn, int imm) { return XFERI(AL, 1, calcU(imm), 1, 0, 0, Rn, Rd, abs(imm)); }
inline int ldrh(int Rd, int Rn, int Rm) { return XFER2(AL, 1, 1, 0, 1, Rn, Rd, 0, 1, Rm); }
inline int ldrhi(int Rd, int Rn, int imm) { return XFER2I(AL, 1, calcU(imm), 0, 1, Rn, Rd, abs(imm)>>4 & 0xf, 0, 1, abs(imm)&0xf); }
inline int strh(int Rd, int Rn, int Rm) { return XFER2(AL, 1, 1, 0, 0, Rn, Rd, 0, 1, Rm); }
inline int strhi(int Rd, int Rn, int imm) { return XFER2I(AL, 1, calcU(imm), 0, 0, Rn, Rd, abs(imm)>>4 & 0xf, 0, 1, abs(imm)&0xf); }
inline int ldrsh(int Rd, int Rn, int Rm) { return XFER2(AL, 1, 1, 0, 1, Rn, Rd, 1, 1, Rm); }
inline int ldrshi(int Rd, int Rn, int imm) { return XFER2I(AL, 1, calcU(imm), 0, 1, Rn, Rd, abs(imm)>>4 & 0xf, 1, 1, abs(imm)&0xf); }
inline int ldrsb(int Rd, int Rn, int Rm) { return XFER2(AL, 1, 1, 0, 1, Rn, Rd, 1, 0, Rm); }
inline int ldrsbi(int Rd, int Rn, int imm) { return XFER2I(AL, 1, calcU(imm), 0, 1, Rn, Rd, abs(imm)>>4 & 0xf, 1, 0, abs(imm)&0xf); }
inline int pop(int Rd) { return XFERI(AL, 0, 1, 0, 0, 1, 13, Rd, 4); }
inline int ldmfd(int Rn, int rlist) { return BLOCKXFER(AL, 0, 1, 0, 1, 1, Rn, rlist); }
inline int stmfd(int Rn, int rlist) { return BLOCKXFER(AL, 1, 0, 0, 1, 0, Rn, rlist); }
inline int swp(int Rd, int Rm, int Rn) { return SWAP(AL, 0, Rn, Rd, Rm); }
inline int swpb(int Rd, int Rm, int Rn) { return SWAP(AL, 1, Rn, Rd, Rm); }
inline int SETCOND(int ins, int cond) { return ((ins&0x0fffffff) | (cond<<28)); }
inline int SETS(int ins) { return ins | 1<<20; }
// PSEUDO-INSTRUCTIONS
inline int nop() { return mov(0, 0); }
inline int lsl(int Rd, int Rm, int Rs) { return movsh(Rd, Rm, Rs, LSL); }
inline int lsli(int Rd, int Rm, int imm) { return mov(Rd, Rm, LSL, imm); }
inline int lsr(int Rd, int Rm, int Rs) { return movsh(Rd, Rm, Rs, LSR); }
inline int lsri(int Rd, int Rm, int imm) { return mov(Rd, Rm, LSR, imm); }
inline int asr(int Rd, int Rm, int Rs) { return movsh(Rd, Rm, Rs, ASR); }
inline int asri(int Rd, int Rm, int imm) { return mov(Rd, Rm, ASR, imm); }
inline int ror(int Rd, int Rm, int Rs) { return movsh(Rd, Rm, Rs, ROR); }
inline int beq(int offset) { return SETCOND(b(offset), EQ); }
inline int bne(int offset) { return SETCOND(b(offset), NE); }
inline int bls(int offset) { return SETCOND(b(offset), LS); }
inline int bhi(int offset) { return SETCOND(b(offset), HI); }
inline int blt(int offset) { return SETCOND(b(offset), LT); }
inline int bgt(int offset) { return SETCOND(b(offset), GT); }
inline int ble(int offset) { return SETCOND(b(offset), LE); }
inline int bge(int offset) { return SETCOND(b(offset), GE); }
inline int blo(int offset) { return SETCOND(b(offset), CC); }
inline int bhs(int offset) { return SETCOND(b(offset), CS); }
}

const uint64_t MASK_LO32 = 0xffffffff;
const unsigned MASK_LO16 = 0xffff;
const unsigned MASK_LO8  = 0xff;
inline unsigned lo32(int64_t i) { return (unsigned)(i&MASK_LO32); }
inline unsigned hi32(int64_t i) { return (unsigned)(i>>32); }
inline unsigned lo16(int64_t i) { return (unsigned)(i&MASK_LO16); }
inline unsigned hi16(int64_t i) { return lo16(i>>16); }
inline unsigned lo8(int64_t i) { return (unsigned)(i&MASK_LO8); }
inline unsigned hi8(int64_t i) { return lo8(i>>8); }

inline int ha16(int32_t i) { 
    return ((i >> 16) + ((i & 0x8000) ? 1 : 0)) & 0xffff;
}
inline int unha16(int32_t high, int32_t low) {
    return ((high - ((low & 0x8000) ? 1 : 0)) << 16) | low; 
}

inline bool isInt8(target_intptr_t v) { return v == static_cast<int8_t>(v); }
inline bool isInt16(target_intptr_t v) { return v == static_cast<int16_t>(v); }
inline bool isInt24(target_intptr_t v) { return v == (v & 0xffffff); }
inline bool isInt32(target_intptr_t v) { return v == static_cast<int32_t>(v); }
inline int carry16(target_intptr_t v) { return static_cast<int16_t>(v) < 0 ? 1 : 0; }

inline bool isOfWidth(int64_t i, int size) { return static_cast<uint64_t>(i) >> size == 0; }
inline bool isOfWidth(int i, int size) { return static_cast<unsigned>(i) >> size == 0; }

const unsigned FrameHeaderSize = 1;

const unsigned StackAlignmentInBytes = 8;
const unsigned StackAlignmentInWords
= StackAlignmentInBytes / TargetBytesPerWord;

const int ThreadRegister = 8;
const int StackRegister = 13;
const int LinkRegister = 14;
const int ProgramCounter = 15;

const int32_t PoolOffsetMask = 0xFFF;

const bool DebugPool = false;

class Context;
class MyBlock;
class PoolOffset;
class PoolEvent;

void
resolve(MyBlock*);

unsigned
padding(MyBlock*, unsigned);

class MyBlock: public Assembler::Block {
 public:
  MyBlock(Context* context, unsigned offset):
    context(context), next(0), poolOffsetHead(0), poolOffsetTail(0),
    lastPoolOffsetTail(0), poolEventHead(0), poolEventTail(0),
    lastEventOffset(0), offset(offset), start(~0), size(0)
  { }

  virtual unsigned resolve(unsigned start, Assembler::Block* next) {
    this->start = start;
    this->next = static_cast<MyBlock*>(next);

    ::resolve(this);

    return start + size + padding(this, size);
  }

  Context* context;
  MyBlock* next;
  PoolOffset* poolOffsetHead;
  PoolOffset* poolOffsetTail;
  PoolOffset* lastPoolOffsetTail;
  PoolEvent* poolEventHead;
  PoolEvent* poolEventTail;
  unsigned lastEventOffset;
  unsigned offset;
  unsigned start;
  unsigned size;
};

class Task;
class ConstantPoolEntry;

class Context {
 public:
  Context(System* s, Allocator* a, Zone* zone):
    s(s), zone(zone), client(0), code(s, a, 1024), tasks(0), result(0),
    firstBlock(new (zone->allocate(sizeof(MyBlock))) MyBlock(this, 0)),
    lastBlock(firstBlock), poolOffsetHead(0), poolOffsetTail(0),
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
  PoolOffset* poolOffsetHead;
  PoolOffset* poolOffsetTail;
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
  Offset(Context* c, MyBlock* block, unsigned offset, bool forTrace):
    c(c), block(block), offset(offset), forTrace(forTrace)
  { }

  virtual bool resolved() {
    return block->start != static_cast<unsigned>(~0);
  }
  
  virtual int64_t value() {
    assert(c, resolved());

    unsigned o = offset - block->offset;
    return block->start + padding
      (block, forTrace ? o - TargetBytesPerWord : o) + o;
  }

  Context* c;
  MyBlock* block;
  unsigned offset;
  bool forTrace;
};

Promise*
offset(Context* c, bool forTrace = false)
{
  return new (c->zone->allocate(sizeof(Offset)))
    Offset(c, c->lastBlock, c->code.length(), forTrace);
}

bool
bounded(int right, int left, int32_t v)
{
  return ((v << left) >> left) == v and ((v >> right) << right) == v;
}

void*
updateOffset(System* s, uint8_t* instruction, int64_t value)
{
  // ARM's PC is two words ahead, and branches drop the bottom 2 bits.
  int32_t v = (reinterpret_cast<uint8_t*>(value) - (instruction + 8)) >> 2;

  int32_t mask;
  expect(s, bounded(0, 8, v));
  mask = 0xFFFFFF;

  int32_t* p = reinterpret_cast<int32_t*>(instruction);
  *p = (v & mask) | ((~mask) & *p);

  return instruction + 4;
}

class OffsetListener: public Promise::Listener {
 public:
  OffsetListener(System* s, uint8_t* instruction):
    s(s),
    instruction(instruction)
  { }

  virtual bool resolve(int64_t value, void** location) {
    void* p = updateOffset(s, instruction, value);
    if (location) *location = p;
    return false;
  }

  System* s;
  uint8_t* instruction;
};

class OffsetTask: public Task {
 public:
  OffsetTask(Task* next, Promise* promise, Promise* instructionOffset):
    Task(next),
    promise(promise),
    instructionOffset(instructionOffset)
  { }

  virtual void run(Context* c) {
    if (promise->resolved()) {
      updateOffset
        (c->s, c->result + instructionOffset->value(), promise->value());
    } else {
      new (promise->listen(sizeof(OffsetListener)))
        OffsetListener(c->s, c->result + instructionOffset->value());
    }
  }

  Promise* promise;
  Promise* instructionOffset;
};

void
appendOffsetTask(Context* c, Promise* promise, Promise* instructionOffset)
{
  c->tasks = new (c->zone->allocate(sizeof(OffsetTask))) OffsetTask
    (c->tasks, promise, instructionOffset);
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

// shortcut functions
inline void emit(Context* con, int code) { con->code.append4(code); }
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
  if (size == 8) {
    int tmp1 = newTemp(con), tmp2 = newTemp(con);
    emit(con, lsl(tmp1, b->high, a->low));
    emit(con, rsbi(tmp2, a->low, 32));
    emit(con, orrsh(tmp1, tmp1, b->low, tmp2, LSR));
    emit(con, SETS(subi(t->high, a->low, 32)));
    emit(con, SETCOND(mov(t->high, tmp1), MI));
    emit(con, SETCOND(lsl(t->high, b->low, t->high), PL));
    emit(con, lsl(t->low, b->low, a->low));
    freeTemp(con, tmp1); freeTemp(con, tmp2);
  } else {
    emit(con, lsl(t->low, b->low, a->low));
  }
}

void shiftLeftC(Context* con, unsigned size UNUSED, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  assert(con, size == TargetBytesPerWord);
  emit(con, lsli(t->low, b->low, getValue(a)));
}

void shiftRightR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t)
{
  if (size == 8) {
    int tmp1 = newTemp(con), tmp2 = newTemp(con);
    emit(con, lsr(tmp1, b->low, a->low));
    emit(con, rsbi(tmp2, a->low, 32));
    emit(con, orrsh(tmp1, tmp1, b->high, tmp2, LSL));
    emit(con, SETS(subi(t->low, a->low, 32)));
    emit(con, SETCOND(mov(t->low, tmp1), MI));
    emit(con, SETCOND(asr(t->low, b->high, t->low), PL));
    emit(con, asr(t->high, b->high, a->low));
    freeTemp(con, tmp1); freeTemp(con, tmp2);
  } else {
    emit(con, asr(t->low, b->low, a->low));
  }
}

void shiftRightC(Context* con, unsigned size UNUSED, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  assert(con, size == TargetBytesPerWord);
  emit(con, asri(t->low, b->low, getValue(a)));
}

void unsignedShiftRightR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t)
{
  emit(con, lsr(t->low, b->low, a->low));
  if (size == 8) {
    int tmpHi = newTemp(con), tmpLo = newTemp(con);
    emit(con, SETS(rsbi(tmpHi, a->low, 32)));
    emit(con, lsl(tmpLo, b->high, tmpHi));
    emit(con, orr(t->low, t->low, tmpLo));
    emit(con, addi(tmpHi, a->low, -32));
    emit(con, lsr(tmpLo, b->high, tmpHi));
    emit(con, orr(t->low, t->low, tmpLo));
    emit(con, lsr(t->high, b->high, a->low));
    freeTemp(con, tmpHi); freeTemp(con, tmpLo);
  }
}

void unsignedShiftRightC(Context* con, unsigned size UNUSED, Assembler::Constant* a, Assembler::Register* b, Assembler::Register* t)
{
  assert(con, size == TargetBytesPerWord);
  emit(con, lsri(t->low, b->low, getValue(a)));
}

class ConstantPoolEntry: public Promise {
 public:
  ConstantPoolEntry(Context* c, Promise* constant, ConstantPoolEntry* next,
                    Promise* callOffset):
    c(c), constant(constant), next(next), callOffset(callOffset),
    address(0)
  { }

  virtual int64_t value() {
    assert(c, resolved());

    return reinterpret_cast<int64_t>(address);
  }

  virtual bool resolved() {
    return address != 0;
  }

  Context* c;
  Promise* constant;
  ConstantPoolEntry* next;
  Promise* callOffset;
  void* address;
  unsigned constantPoolCount;
};

class ConstantPoolListener: public Promise::Listener {
 public:
  ConstantPoolListener(System* s, target_uintptr_t* address,
                       uint8_t* returnAddress):
    s(s),
    address(address),
    returnAddress(returnAddress)
  { }

  virtual bool resolve(int64_t value, void** location) {
    *address = value;
    if (location) {
      *location = returnAddress ? static_cast<void*>(returnAddress) : address;
    }
    return true;
  }

  System* s;
  target_uintptr_t* address;
  uint8_t* returnAddress;
};

class PoolOffset {
 public:
  PoolOffset(MyBlock* block, ConstantPoolEntry* entry, unsigned offset):
    block(block), entry(entry), next(0), offset(offset)
  { }

  MyBlock* block;
  ConstantPoolEntry* entry;
  PoolOffset* next;
  unsigned offset;
};

class PoolEvent {
 public:
  PoolEvent(PoolOffset* poolOffsetHead, PoolOffset* poolOffsetTail,
            unsigned offset):
    poolOffsetHead(poolOffsetHead), poolOffsetTail(poolOffsetTail), next(0),
    offset(offset)
  { }

  PoolOffset* poolOffsetHead;
  PoolOffset* poolOffsetTail;
  PoolEvent* next;
  unsigned offset;
};

void
appendConstantPoolEntry(Context* c, Promise* constant, Promise* callOffset)
{
  if (constant->resolved()) {
    // make a copy, since the original might be allocated on the
    // stack, and we need our copy to live until assembly is complete
    constant = new (c->zone->allocate(sizeof(ResolvedPromise)))
      ResolvedPromise(constant->value());
  }

  c->constantPool = new (c->zone->allocate(sizeof(ConstantPoolEntry)))
    ConstantPoolEntry(c, constant, c->constantPool, callOffset);

  ++ c->constantPoolCount;

  PoolOffset* o = new (c->zone->allocate(sizeof(PoolOffset))) PoolOffset
    (c->lastBlock, c->constantPool, c->code.length() - c->lastBlock->offset);

  if (DebugPool) {
    fprintf(stderr, "add pool offset %p %d to block %p\n",
            o, o->offset, c->lastBlock);
  }

  if (c->lastBlock->poolOffsetTail) {
    c->lastBlock->poolOffsetTail->next = o;
  } else {
    c->lastBlock->poolOffsetHead = o;
  }
  c->lastBlock->poolOffsetTail = o;
}

void
appendPoolEvent(Context* c, MyBlock* b, unsigned offset, PoolOffset* head,
                PoolOffset* tail)
{
  PoolEvent* e = new (c->zone->allocate(sizeof(PoolEvent))) PoolEvent
    (head, tail, offset);

  if (b->poolEventTail) {
    b->poolEventTail->next = e;
  } else {
    b->poolEventHead = e;
  }
  b->poolEventTail = e;
}

bool
needJump(MyBlock* b)
{
  return b->next or b->size != (b->size & PoolOffsetMask);
}

unsigned
padding(MyBlock* b, unsigned offset)
{
  unsigned total = 0;
  for (PoolEvent* e = b->poolEventHead; e; e = e->next) {
    if (e->offset <= offset) {
      if (needJump(b)) {
        total += TargetBytesPerWord;
      }
      for (PoolOffset* o = e->poolOffsetHead; o; o = o->next) {
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

  if (b->poolOffsetHead) {
    if (c->poolOffsetTail) {
      c->poolOffsetTail->next = b->poolOffsetHead;
    } else {
      c->poolOffsetHead = b->poolOffsetHead;
    }
    c->poolOffsetTail = b->poolOffsetTail;
  }

  if (c->poolOffsetHead) {
    bool append;
    if (b->next == 0 or b->next->poolEventHead) {
      append = true;
    } else {
      int32_t v = (b->start + b->size + b->next->size + TargetBytesPerWord - 8)
        - (c->poolOffsetHead->offset + c->poolOffsetHead->block->start);

      append = (v != (v & PoolOffsetMask));

      if (DebugPool) {
        fprintf(stderr,
                "current %p %d %d next %p %d %d\n",
                b, b->start, b->size, b->next, b->start + b->size,
                b->next->size);
        fprintf(stderr,
                "offset %p %d is of distance %d to next block; append? %d\n",
                c->poolOffsetHead, c->poolOffsetHead->offset, v, append);
      }
    }

    if (append) {
#ifndef NDEBUG
      int32_t v = (b->start + b->size - 8)
        - (c->poolOffsetHead->offset + c->poolOffsetHead->block->start);
      
      expect(c, v == (v & PoolOffsetMask));
#endif // not NDEBUG

      appendPoolEvent(c, b, b->size, c->poolOffsetHead, c->poolOffsetTail);

      if (DebugPool) {
        for (PoolOffset* o = c->poolOffsetHead; o; o = o->next) {
          fprintf(stderr,
                  "include %p %d in pool event %p at offset %d in block %p\n",
                  o, o->offset, b->poolEventTail, b->size, b);
        }
      }

      c->poolOffsetHead = 0;
      c->poolOffsetTail = 0;
    }
  }
}

void
jumpR(Context* c, unsigned size UNUSED, Assembler::Register* target)
{
  assert(c, size == TargetBytesPerWord);
  emit(c, bx(target->low));
}

void
moveRR(Context* c, unsigned srcSize, Assembler::Register* src,
       unsigned dstSize, Assembler::Register* dst);

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
    emit(c, lsli(dst->low, src->low, 24));
    emit(c, asri(dst->low, dst->low, 24));
    break;
    
  case 2:
    emit(c, lsli(dst->low, src->low, 16));
    emit(c, asri(dst->low, dst->low, 16));
    break;
    
  case 4:
  case 8:
    if (srcSize == 4 and dstSize == 8) {
      moveRR(c, 4, src, 4, dst);
      emit(c, asri(dst->high, src->low, 31));
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
      emit(c, mov(dst->low, src->low));
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
    emit(c, lsli(dst->low, src->low, 16));
    emit(c, lsri(dst->low, dst->low, 16));
    break;

  default: abort(c);
  }
}

void
moveCR2(Context* c, unsigned, Assembler::Constant* src,
        unsigned dstSize, Assembler::Register* dst, Promise* callOffset)
{
  if (dstSize <= 4) {
    if (src->value->resolved() and isOfWidth(getValue(src), 8)) {
      emit(c, movi(dst->low, lo8(getValue(src))));
    } else {
      appendConstantPoolEntry(c, src->value, callOffset);
      emit(c, ldri(dst->low, ProgramCounter, 0));
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
  if (size == 8) {
    emit(con, SETS(add(t->low, a->low, b->low)));
    emit(con, adc(t->high, a->high, b->high));
  } else {
    emit(con, add(t->low, a->low, b->low));
  }
}

void subR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  if (size == 8) {
    emit(con, SETS(rsb(t->low, a->low, b->low)));
    emit(con, rsc(t->high, a->high, b->high));
  } else {
    emit(con, rsb(t->low, a->low, b->low));
  }
}

void
addC(Context* c, unsigned size, Assembler::Constant* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  assert(c, size == TargetBytesPerWord);

  int32_t v = a->value->value();
  if (v) {
    if (v > 0 and v < 256) {
      emit(c, addi(dst->low, b->low, v));
    } else if (v > 0 and v < 1024 and v % 4 == 0) {
      emit(c, addi(dst->low, b->low, v >> 2, 15));
    } else {
      // todo
      abort(c);
    }
  } else {
    moveRR(c, size, b, size, dst);
  }
}

void
subC(Context* c, unsigned size, Assembler::Constant* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  assert(c, size == TargetBytesPerWord);

  int32_t v = a->value->value();
  if (v) {
    if (v > 0 and v < 256) {
      emit(c, subi(dst->low, b->low, v));
    } else if (v > 0 and v < 1024 and v % 4 == 0) {
      emit(c, subi(dst->low, b->low, v >> 2, 15));
    } else {
      // todo
      abort(c);
    }
  } else {
    moveRR(c, size, b, size, dst);
  }
}

void multiplyR(Context* con, unsigned size, Assembler::Register* a, Assembler::Register* b, Assembler::Register* t) {
  if (size == 8) {
    bool useTemporaries = b->low == t->low;
    int tmpLow  = useTemporaries ? con->client->acquireTemporary() : t->low;
    int tmpHigh = useTemporaries ? con->client->acquireTemporary() : t->high;

    emit(con, umull(tmpLow, tmpHigh, a->low, b->low));
    emit(con, mla(tmpHigh, a->low, b->high, tmpHigh));
    emit(con, mla(tmpHigh, a->high, b->low, tmpHigh));

    if (useTemporaries) {
      emit(con, mov(t->low, tmpLow));
      emit(con, mov(t->high, tmpHigh));
      con->client->releaseTemporary(tmpLow);
      con->client->releaseTemporary(tmpHigh);
    }
  } else {
    emit(con, mul(t->low, a->low, b->low));
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

      Assembler::Register tmp(c->client->acquireTemporary());
      moveCR(c, TargetBytesPerWord, &offsetConstant, TargetBytesPerWord, &tmp);
      addR(c, TargetBytesPerWord, &tmp, &untranslatedIndex, &normalizedIndex);
      c->client->releaseTemporary(tmp.low);
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
      emit(c, strb(src->low, base, normalized));
      break;

    case 2:
      emit(c, strh(src->low, base, normalized));
      break;

    case 4:
      emit(c, str(src->low, base, normalized));
      break;

    case 8: {
      Assembler::Register srcHigh(src->high);
      store(c, 4, &srcHigh, base, 0, normalized, 1, preserveIndex);
      store(c, 4, src, base, 4, normalized, 1, preserveIndex);
    } break;

    default: abort(c);
    }

    if (release) c->client->releaseTemporary(normalized);
  } else if (size == 8
             or abs(offset) == (abs(offset) & 0xFF)
             or (size != 2 and abs(offset) == (abs(offset) & 0xFFF)))
  {
    switch (size) {
    case 1:
      emit(c, strbi(src->low, base, offset));
      break;

    case 2:
      emit(c, strhi(src->low, base, offset));
      break;

    case 4:
      emit(c, stri(src->low, base, offset));
      break;

    case 8: {
      Assembler::Register srcHigh(src->high);
      store(c, 4, &srcHigh, base, offset, NoRegister, 1, false);
      store(c, 4, src, base, offset + 4, NoRegister, 1, false);
    } break;

    default: abort(c);
    }
  } else {
    Assembler::Register tmp(c->client->acquireTemporary());
    ResolvedPromise offsetPromise(offset);
    Assembler::Constant offsetConstant(&offsetPromise);
    moveCR(c, TargetBytesPerWord, &offsetConstant, TargetBytesPerWord, &tmp);
    
    store(c, size, src, base, 0, tmp.low, 1, false);

    c->client->releaseTemporary(tmp.low);
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
    emit(c, stri(src->low, dst->base, dst->offset, dst->offset ? 1 : 0));
  } else {
    assert(c, dst->offset == 0);
    assert(c, dst->scale == 1);
    
    emit(c, str(src->low, dst->base, dst->index, 1));
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
      if (signExtend) {
        emit(c, ldrsb(dst->low, base, normalized));
      } else {
        emit(c, ldrb(dst->low, base, normalized));
      }
      break;

    case 2:
      if (signExtend) {
        emit(c, ldrsh(dst->low, base, normalized));
      } else {
        emit(c, ldrh(dst->low, base, normalized));
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
        emit(c, ldr(dst->low, base, normalized));
      }
    } break;

    default: abort(c);
    }

    if (release) c->client->releaseTemporary(normalized);
  } else if ((srcSize == 8 and dstSize == 8)
             or abs(offset) == (abs(offset) & 0xFF)
             or (srcSize != 2
                 and (srcSize != 1 or not signExtend)
                 and abs(offset) == (abs(offset) & 0xFFF)))
  {
    switch (srcSize) {
    case 1:
      if (signExtend) {
        emit(c, ldrsbi(dst->low, base, offset));
      } else {
        emit(c, ldrbi(dst->low, base, offset));
      }
      break;

    case 2:
      if (signExtend) {
        emit(c, ldrshi(dst->low, base, offset));
      } else {
        emit(c, ldrhi(dst->low, base, offset));
      }
      break;

    case 4:
      emit(c, ldri(dst->low, base, offset));
      break;

    case 8: {
      if (dstSize == 8) {
        Assembler::Register dstHigh(dst->high);
        load(c, 4, base, offset, NoRegister, 1, 4, &dstHigh, false, false);
        load(c, 4, base, offset + 4, NoRegister, 1, 4, dst, false, false);
      } else {
        emit(c, ldri(dst->low, base, offset));
      }
    } break;

    default: abort(c);
    }
  } else {
    Assembler::Register tmp(c->client->acquireTemporary());
    ResolvedPromise offsetPromise(offset);
    Assembler::Constant offsetConstant(&offsetPromise);
    moveCR(c, TargetBytesPerWord, &offsetConstant, TargetBytesPerWord, &tmp);
    
    load(c, srcSize, base, 0, tmp.low, 1, dstSize, dst, false, signExtend);

    c->client->releaseTemporary(tmp.low);
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
  if (size == 8) emit(c, and_(dst->high, a->high, b->high));
  emit(c, and_(dst->low, a->low, b->low));
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
    uint32_t v32 = static_cast<uint32_t>(v);
    if (v32 != 0xFFFFFFFF) {
      if ((v32 & 0xFFFFFF00) == 0xFFFFFF00) {
        emit(c, bici(dst->low, b->low, (~(v32 & 0xFF)) & 0xFF));
      } else if ((v32 & 0xFFFFFF00) == 0) {
        emit(c, andi(dst->low, b->low, v32 & 0xFF));
      } else {
        // todo: there are other cases we can handle in one
        // instruction

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
    } else {
      moveRR(c, size, b, size, dst);
    }
  }
}

void
orR(Context* c, unsigned size, Assembler::Register* a,
    Assembler::Register* b, Assembler::Register* dst)
{
  if (size == 8) emit(c, orr(dst->high, a->high, b->high));
  emit(c, orr(dst->low, a->low, b->low));
}

void
xorR(Context* con, unsigned size, Assembler::Register* a,
     Assembler::Register* b, Assembler::Register* dst)
{
  if (size == 8) emit(con, eor(dst->high, a->high, b->high));
  emit(con, eor(dst->low, a->low, b->low));
}

void
moveAR2(Context* c, unsigned srcSize, Assembler::Address* src,
       unsigned dstSize, Assembler::Register* dst)
{
  assert(c, srcSize == 4 and dstSize == 4);

  Assembler::Constant constant(src->address);
  moveCR(c, srcSize, &constant, dstSize, dst);

  Assembler::Memory memory(dst->low, 0, -1, 0);
  moveMR(c, dstSize, &memory, dstSize, dst);
}

void
moveAR(Context* c, unsigned srcSize, Assembler::Address* src,
       unsigned dstSize, Assembler::Register* dst)
{
  moveAR2(c, srcSize, src, dstSize, dst);
}

void
compareRR(Context* c, unsigned aSize UNUSED, Assembler::Register* a,
          unsigned bSize UNUSED, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);
  assert(c, b->low != a->low);

  emit(c, cmp(b->low, a->low));
}

void
compareCR(Context* c, unsigned aSize, Assembler::Constant* a,
          unsigned bSize, Assembler::Register* b)
{
  assert(c, aSize == 4 and bSize == 4);

  if (a->value->resolved() and isOfWidth(a->value->value(), 8)) {
    emit(c, cmpi(b->low, a->value->value()));
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
  appendOffsetTask(c, target->value, offset(c));
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
    conditional(c, blo(0), target);
    break;

  case JumpIfGreater:
    conditional(c, bgt(0), target);

    next = c->code.length();
    emit(c, blt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, bhi(0), target);
    break;

  case JumpIfLessOrEqual:
    conditional(c, blt(0), target);

    next = c->code.length();
    emit(c, bgt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, bls(0), target);
    break;

  case JumpIfGreaterOrEqual:
    conditional(c, bgt(0), target);

    next = c->code.length();
    emit(c, blt(0));

    compareUnsigned(c, 4, al, 4, bl);
    conditional(c, bhs(0), target);
    break;

  default:
    abort(c);
  }

  if (next) {
    updateOffset
      (c->s, c->code.data + next, reinterpret_cast<intptr_t>
       (c->code.data + c->code.length()));
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
               CAST2(compareRR));
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
               CAST2(compareCR));
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

  emit(c, mvn(dst->low, src->low));
  emit(c, SETS(addi(dst->low, dst->low, 1)));
  if (srcSize == 8) {
    emit(c, mvn(dst->high, src->high));
    emit(c, adci(dst->high, dst->high, 0));
  }
}

void
callR(Context* c, unsigned size UNUSED, Assembler::Register* target)
{
  assert(c, size == TargetBytesPerWord);
  emit(c, blx(target->low));
}

void
callC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  appendOffsetTask(c, target->value, offset(c));
  emit(c, bl(0));
}

void
longCallC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(4);
  moveCR2(c, TargetBytesPerWord, target, TargetBytesPerWord, &tmp, offset(c));
  callR(c, TargetBytesPerWord, &tmp);
}

void
longJumpC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  Assembler::Register tmp(4); // a non-arg reg that we don't mind clobbering
  moveCR2(c, TargetBytesPerWord, target, TargetBytesPerWord, &tmp, offset(c));
  jumpR(c, TargetBytesPerWord, &tmp);
}

void
jumpC(Context* c, unsigned size UNUSED, Assembler::Constant* target)
{
  assert(c, size == TargetBytesPerWord);

  appendOffsetTask(c, target->value, offset(c));
  emit(c, b(0));
}

void
return_(Context* c)
{
  emit(c, bx(LinkRegister));
}

void
memoryBarrier(Context*) {}

// END OPERATION COMPILERS

unsigned
argumentFootprint(unsigned footprint)
{
  return max(pad(footprint, StackAlignmentInWords), StackAlignmentInWords);
}

void
nextFrame(ArchitectureContext* c, uint32_t* start, unsigned size UNUSED,
          unsigned footprint, void* link, void*,
          unsigned targetParameterFootprint UNUSED, void** ip, void** stack)
{
  assert(c, *ip >= start);
  assert(c, *ip <= start + (size / TargetBytesPerWord));

  uint32_t* instruction = static_cast<uint32_t*>(*ip);

  if ((*start >> 20) == 0xe59) {
    // skip stack overflow check
    start += 3;
  }

  if (instruction <= start) {
    *ip = link;
    return;
  }

  unsigned offset = footprint + FrameHeaderSize;

  if (instruction <= start + 2) {
    *ip = link;
    *stack = static_cast<void**>(*stack) + offset;
    return;
  }

  if (*instruction == 0xe12fff1e) { // return
    *ip = link;
    return;
  }

  if (TailCalls) {
    if (argumentFootprint(targetParameterFootprint) > StackAlignmentInWords) {
      offset += argumentFootprint(targetParameterFootprint)
        - StackAlignmentInWords;
    }

    // check for post-non-tail-call stack adjustment of the form "add
    // sp, sp, #offset":
    if ((*instruction >> 12) == 0xe24dd) {
      unsigned value = *instruction & 0xff;
      unsigned rotation = (*instruction >> 8) & 0xf;
      switch (rotation) {
      case  0: offset -= value / TargetBytesPerWord; break;
      case 15: offset -= value; break;
      default: abort(c);
      }
    }

    // todo: check for and handle tail calls
  }

  *ip = static_cast<void**>(*stack)[offset - 1];
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

  uo[index(c, AlignedLongCall, C)] = CAST1(longCallC);

  uo[index(c, LongJump, C)] = CAST1(longJumpC);

  uo[index(c, AlignedLongJump, C)] = CAST1(longJumpC);

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

  to[index(c, Subtract, R)] = CAST3(subR);

  to[index(c, Multiply, R)] = CAST3(multiplyR);

  to[index(c, ShiftLeft, R)] = CAST3(shiftLeftR);
  to[index(c, ShiftLeft, C)] = CAST3(shiftLeftC);

  to[index(c, ShiftRight, R)] = CAST3(shiftRightR);
  to[index(c, ShiftRight, C)] = CAST3(shiftRightC);

  to[index(c, UnsignedShiftRight, R)] = CAST3(unsignedShiftRightR);
  to[index(c, UnsignedShiftRight, C)] = CAST3(unsignedShiftRightC);

  to[index(c, And, R)] = CAST3(andR);
  to[index(c, And, C)] = CAST3(andC);

  to[index(c, Or, R)] = CAST3(orR);

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
    return 0xFFFF;
  }

  virtual uint32_t floatRegisterMask() {
    return 0;
  }

  virtual int scratch() {
    return 5;
  }

  virtual int stack() {
    return StackRegister;
  }

  virtual int thread() {
    return ThreadRegister;
  }

  virtual int returnLow() {
    return 0;
  }

  virtual int returnHigh() {
    return 1;
  }

  virtual int virtualCallTarget() {
    return 4;
  }

  virtual int virtualCallIndex() {
    return 3;
  }

  virtual bool bigEndian() {
    return false;
  }

  virtual uintptr_t maximumImmediateJump() {
    return 0x1FFFFFF;
  }

  virtual bool reserved(int register_) {
    switch (register_) {
    case LinkRegister:
    case StackRegister:
    case ThreadRegister:
    case ProgramCounter:
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
#ifdef __APPLE__
    return false;
#else
    return true;
#endif
  }

  virtual bool argumentRegisterAlignment() {
#ifdef __APPLE__
    return false;
#else
    return true;
#endif
  }

  virtual unsigned argumentRegisterCount() {
    return 4;
  }

  virtual int argumentRegister(unsigned index) {
    assert(&c, index < argumentRegisterCount());

    return index;
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
      updateOffset(c.s, static_cast<uint8_t*>(returnAddress) - 4,
                   reinterpret_cast<intptr_t>(newTarget));
    } break;

    case LongCall:
    case LongJump:
    case AlignedLongCall:
    case AlignedLongJump: {
      uint32_t* p = static_cast<uint32_t*>(returnAddress) - 2;
      *reinterpret_cast<void**>(p + (((*p & PoolOffsetMask) + 8) / 4))
        = newTarget;
    } break;

    default: abort(&c);
    }
  }

  virtual unsigned constantCallSize() {
    return 4;
  }

  virtual void setConstant(void* dst, uint64_t constant) {
    *static_cast<target_uintptr_t*>(dst) = constant;
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
    ::nextFrame(&c, static_cast<uint32_t*>(start), size, footprint, link,
                stackLimit, targetParameterFootprint, ip, stack);
  }

  virtual void* frameIp(void* stack) {
    return stack ? static_cast<void**>(stack)[returnAddressOffset()] : 0;
  }

  virtual unsigned frameHeaderSize() {
    return FrameHeaderSize;
  }

  virtual unsigned frameReturnAddressSize() {
    return 0;
  }

  virtual unsigned frameFooterSize() {
    return 0;
  }

  virtual int returnAddressOffset() {
    return -1;
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
   unsigned, uint8_t* aTypeMask, uint64_t* aRegisterMask,
   unsigned bSize, uint8_t* bTypeMask, uint64_t* bRegisterMask,
   unsigned, bool* thunk)
  {
    *aTypeMask = (1 << RegisterOperand) | (1 << ConstantOperand);
    *aRegisterMask = ~static_cast<uint64_t>(0);

    *bTypeMask = (1 << RegisterOperand);
    *bRegisterMask = ~static_cast<uint64_t>(0);

    *thunk = false;

    switch (op) {
    case ShiftLeft:
    case ShiftRight:
    case UnsignedShiftRight:
      if (bSize == 8) *aTypeMask = *bTypeMask = (1 << RegisterOperand);
      break;

    case Add:
    case Subtract:
    case Or:
    case Xor:
    case Multiply:
      *aTypeMask = *bTypeMask = (1 << RegisterOperand);
      break;

    case Divide:
    case Remainder:
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

  virtual void saveFrame(unsigned stackOffset, unsigned ipOffset) {
    Register link(LinkRegister);
    Memory linkDst(ThreadRegister, ipOffset);
    moveRM(&c, TargetBytesPerWord, &link, TargetBytesPerWord, &linkDst);

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
        Memory dst(StackRegister, offset * TargetBytesPerWord);

        apply(Move,
              arguments[i].size, arguments[i].type, arguments[i].operand,
              pad(arguments[i].size, TargetBytesPerWord), MemoryOperand, &dst);

        offset += ceiling(arguments[i].size, TargetBytesPerWord);
      }
    }
  }

  virtual void allocateFrame(unsigned footprint) {
    footprint += FrameHeaderSize;

    // larger frames may require multiple subtract/add instructions
    // to allocate/deallocate, and nextFrame will need to be taught
    // how to handle them:
    assert(&c, footprint < 256);

    Register stack(StackRegister);
    ResolvedPromise footprintPromise(footprint * TargetBytesPerWord);
    Constant footprintConstant(&footprintPromise);
    subC(&c, TargetBytesPerWord, &footprintConstant, &stack, &stack);

    Register returnAddress(LinkRegister);
    Memory returnAddressDst
      (StackRegister, (footprint - 1) * TargetBytesPerWord);
    moveRM(&c, TargetBytesPerWord, &returnAddress, TargetBytesPerWord,
           &returnAddressDst);
  }

  virtual void adjustFrame(unsigned difference) {
    Register stack(StackRegister);
    ResolvedPromise differencePromise(difference * TargetBytesPerWord);
    Constant differenceConstant(&differencePromise);
    subC(&c, TargetBytesPerWord, &differenceConstant, &stack, &stack);
  }

  virtual void popFrame(unsigned footprint) {
    footprint += FrameHeaderSize;

    Register returnAddress(LinkRegister);
    Memory returnAddressSrc
      (StackRegister, (footprint - 1) * TargetBytesPerWord);
    moveMR(&c, TargetBytesPerWord, &returnAddressSrc, TargetBytesPerWord,
           &returnAddress);
    
    Register stack(StackRegister);
    ResolvedPromise footprintPromise(footprint * TargetBytesPerWord);
    Constant footprintConstant(&footprintPromise);
    addC(&c, TargetBytesPerWord, &footprintConstant, &stack, &stack);
  }

  virtual void popFrameForTailCall(unsigned footprint,
                                   int offset,
                                   int returnAddressSurrogate,
                                   int framePointerSurrogate UNUSED)
  {
    assert(&c, framePointerSurrogate == NoRegister);

    if (TailCalls) {
      if (offset) {
        footprint += FrameHeaderSize;

        Register link(LinkRegister);
        Memory returnAddressSrc
          (StackRegister, (footprint - 1) * TargetBytesPerWord);
        moveMR(&c, TargetBytesPerWord, &returnAddressSrc, TargetBytesPerWord,
               &link);
    
        Register stack(StackRegister);
        ResolvedPromise footprintPromise
          ((footprint - offset) * TargetBytesPerWord);
        Constant footprintConstant(&footprintPromise);
        addC(&c, TargetBytesPerWord, &footprintConstant, &stack, &stack);

        if (returnAddressSurrogate != NoRegister) {
          assert(&c, offset > 0);

          Register ras(returnAddressSurrogate);
          Memory dst(StackRegister, (offset - 1) * TargetBytesPerWord);
          moveRM(&c, TargetBytesPerWord, &ras, TargetBytesPerWord, &dst);
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

    unsigned offset;
    if (TailCalls and argumentFootprint > StackAlignmentInWords) {
      offset = argumentFootprint - StackAlignmentInWords;

      Register stack(StackRegister);
      ResolvedPromise adjustmentPromise(offset * TargetBytesPerWord);
      Constant adjustment(&adjustmentPromise);
      addC(&c, TargetBytesPerWord, &adjustment, &stack, &stack);
    } else {
      offset = 0;
    }

    return_(&c);
  }

  virtual void popFrameAndUpdateStackAndReturn(unsigned frameFootprint,
                                               unsigned stackOffsetFromThread)
  {
    popFrame(frameFootprint);

    Register stack(StackRegister);
    Memory newStackSrc(ThreadRegister, stackOffsetFromThread);
    moveMR(&c, TargetBytesPerWord, &newStackSrc, TargetBytesPerWord, &stack);

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
      if (DebugPool) {
        fprintf(stderr, "write block %p\n", b);
      }

      unsigned blockOffset = 0;
      for (PoolEvent* e = b->poolEventHead; e; e = e->next) {
        unsigned size = e->offset - blockOffset;
        memcpy(dst + dstOffset, c.code.data + b->offset + blockOffset, size);
        blockOffset = e->offset;
        dstOffset += size;

        unsigned poolSize = 0;
        for (PoolOffset* o = e->poolOffsetHead; o; o = o->next) {
          if (DebugPool) {
            fprintf(stderr, "visit pool offset %p %d in block %p\n",
                    o, o->offset, b);
          }

          unsigned entry = dstOffset + poolSize;

          if (needJump(b)) {
            entry += TargetBytesPerWord;
          }

          o->entry->address = dst + entry;

          unsigned instruction = o->block->start
            + padding(o->block, o->offset) + o->offset;

          int32_t v = (entry - 8) - instruction;
          expect(&c, v == (v & PoolOffsetMask));

          int32_t* p = reinterpret_cast<int32_t*>(dst + instruction);
          *p = (v & PoolOffsetMask) | ((~PoolOffsetMask) & *p);

          poolSize += TargetBytesPerWord;
        }

        bool jump = needJump(b);
        if (jump) {
          write4
            (dst + dstOffset, ::b((poolSize + TargetBytesPerWord - 8) >> 2));
        }

        dstOffset += poolSize + (jump ? TargetBytesPerWord : 0);
      }

      unsigned size = b->size - blockOffset;

      memcpy(dst + dstOffset,
             c.code.data + b->offset + blockOffset,
             size);

      dstOffset += size;
    }

    for (Task* t = c.tasks; t; t = t->next) {
      t->run(&c);
    }

    for (ConstantPoolEntry* e = c.constantPool; e; e = e->next) {
      if (e->constant->resolved()) {
        *static_cast<target_uintptr_t*>(e->address) = e->constant->value();
      } else {
        new (e->constant->listen(sizeof(ConstantPoolListener)))
          ConstantPoolListener(c.s, static_cast<target_uintptr_t*>(e->address),
                               e->callOffset
                               ? dst + e->callOffset->value() + 8
                               : 0);
      }
//       fprintf(stderr, "constant %p at %p\n", reinterpret_cast<void*>(e->constant->value()), e->address);
    }
  }

  virtual Promise* offset(bool forTrace) {
    return ::offset(&c, forTrace);
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
    if (b->poolOffsetHead) {
      int32_t v = (thisEventOffset + TargetBytesPerWord - 8)
        - b->poolOffsetHead->offset;

      if (v > 0 and v != (v & PoolOffsetMask)) {
        appendPoolEvent
          (&c, b, b->lastEventOffset, b->poolOffsetHead,
           b->lastPoolOffsetTail);

        if (DebugPool) {
          for (PoolOffset* o = b->poolOffsetHead;
               o != b->lastPoolOffsetTail->next; o = o->next)
          {
            fprintf(stderr,
                    "in endEvent, include %p %d in pool event %p at offset %d "
                    "in block %p\n",
                    o, o->offset, b->poolEventTail, b->lastEventOffset, b);
          }
        }

        b->poolOffsetHead = b->lastPoolOffsetTail->next;
        b->lastPoolOffsetTail->next = 0;
        if (b->poolOffsetHead == 0) {
          b->poolOffsetTail = 0;
        }
      }
    }
    b->lastEventOffset = thisEventOffset;
    b->lastPoolOffsetTail = b->poolOffsetTail;
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

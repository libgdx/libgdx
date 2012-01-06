/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef ASSEMBLER_H
#define ASSEMBLER_H

#include "system.h"
#include "zone.h"

namespace vm {

#ifdef AVIAN_TAILS
const bool TailCalls = true;
#else
const bool TailCalls = false;
#endif

#if (defined AVIAN_USE_FRAME_POINTER) || (defined ARCH_powerpc)
const bool UseFramePointer = true;
#else
const bool UseFramePointer = false;
#endif

enum Operation {
  Return,
  LoadBarrier,
  StoreStoreBarrier,
  StoreLoadBarrier
};

const unsigned OperationCount = StoreLoadBarrier + 1;

enum UnaryOperation {
  Call,
  LongCall,
  AlignedLongCall,
  AlignedCall,
  Jump,
  LongJump,
  AlignedLongJump,
  AlignedJump,
  
  NoUnaryOperation = -1
};

const unsigned UnaryOperationCount = AlignedJump + 1;

enum BinaryOperation {
  Move,
  MoveLow,
  MoveHigh,
  MoveZ,
  Negate,
  FloatNegate,
  Float2Float,
  Float2Int,
  Int2Float,
  FloatSquareRoot,
  FloatAbsolute,
  Absolute,
  
  NoBinaryOperation = -1
};

const unsigned BinaryOperationCount = Absolute + 1;

enum TernaryOperation {
  Add,
  Subtract,
  Multiply,
  Divide,
  Remainder,
  ShiftLeft,
  ShiftRight,
  UnsignedShiftRight,
  And,
  Or,
  Xor,
  FloatAdd,
  FloatSubtract,
  FloatMultiply,
  FloatDivide,
  FloatRemainder,
  FloatMax,
  FloatMin,
  JumpIfLess,
  JumpIfGreater,
  JumpIfLessOrEqual,
  JumpIfGreaterOrEqual,
  JumpIfEqual,
  JumpIfNotEqual,
  JumpIfFloatEqual,
  JumpIfFloatNotEqual,
  JumpIfFloatLess,
  JumpIfFloatGreater,
  JumpIfFloatLessOrEqual,
  JumpIfFloatGreaterOrEqual,
  JumpIfFloatLessOrUnordered,
  JumpIfFloatGreaterOrUnordered,
  JumpIfFloatLessOrEqualOrUnordered,
  JumpIfFloatGreaterOrEqualOrUnordered,
  
  NoTernaryOperation = -1
};

const unsigned TernaryOperationCount
= JumpIfFloatGreaterOrEqualOrUnordered + 1;

const unsigned NonBranchTernaryOperationCount = FloatMin + 1;
const unsigned BranchOperationCount
= JumpIfFloatGreaterOrEqualOrUnordered - FloatMin;

enum OperandType {
  ConstantOperand,
  AddressOperand,
  RegisterOperand,
  MemoryOperand
};

enum ValueType {
  ValueGeneral,
  ValueFloat
};

const unsigned OperandTypeCount = MemoryOperand + 1;

const int NoRegister = -1;

class Promise {
 public:
  class Listener {
   public:
    virtual bool resolve(int64_t value, void** location) = 0;

    Listener* next;
  };

  virtual int64_t value() = 0;
  virtual bool resolved() = 0;
  virtual Listener* listen(unsigned) { return 0; }
};

class ResolvedPromise: public Promise {
 public:
  ResolvedPromise(int64_t value): value_(value) { }

  virtual int64_t value() {
    return value_;
  }

  virtual bool resolved() {
    return true;
  }

  int64_t value_;
};

class ShiftMaskPromise: public Promise {
 public:
  ShiftMaskPromise(Promise* base, unsigned shift, int64_t mask):
    base(base), shift(shift), mask(mask)
  { }

  virtual int64_t value() {
    return (base->value() >> shift) & mask;
  }

  virtual bool resolved() {
    return base->resolved();
  }

  Promise* base;
  unsigned shift;
  int64_t mask;
};

class CombinedPromise: public Promise {
 public:
  CombinedPromise(Promise* low, Promise* high):
    low(low), high(high)
  { }

  virtual int64_t value() {
    return low->value() | (high->value() << 32);
  }

  virtual bool resolved() {
    return low->resolved() and high->resolved();
  }

  Promise* low;
  Promise* high;
};

class OffsetPromise: public Promise {
 public:
  OffsetPromise(Promise* base, int64_t offset):
    base(base), offset(offset)
  { }

  virtual int64_t value() {
    return base->value() + offset;
  }

  virtual bool resolved() {
    return base->resolved();
  }

  Promise* base;
  int64_t offset;
};

class ListenPromise: public Promise {
 public:
  ListenPromise(System* s, Allocator* allocator):
    s(s), allocator(allocator), listener(0)
  { }

  virtual int64_t value() {
    abort(s);
  }

  virtual bool resolved() {
    return false;
  }

  virtual Listener* listen(unsigned sizeInBytes) {
    Listener* l = static_cast<Listener*>(allocator->allocate(sizeInBytes));
    l->next = listener;
    listener = l;
    return l;
  }

  System* s;
  Allocator* allocator;
  Listener* listener;
  Promise* promise;
};

class DelayedPromise: public ListenPromise {
 public:
  DelayedPromise(System* s, Allocator* allocator, Promise* basis,
                 DelayedPromise* next):
    ListenPromise(s, allocator), basis(basis), next(next)
  { }

  virtual int64_t value() {
    abort(s);
  }

  virtual bool resolved() {
    return false;
  }

  virtual Listener* listen(unsigned sizeInBytes) {
    Listener* l = static_cast<Listener*>(allocator->allocate(sizeInBytes));
    l->next = listener;
    listener = l;
    return l;
  }

  Promise* basis;
  DelayedPromise* next;
};

class Assembler {
 public:
  class Operand { };

  class Constant: public Operand {
   public:
    Constant(Promise* value): value(value) { }

    Promise* value;
  };

  class Address: public Operand {
   public:
    Address(Promise* address): address(address) { }

    Promise* address;
  };

  class Register: public Operand {
   public:
    Register(int low, int high = NoRegister): low(low), high(high) { }

    int low;
    int high;
  };

  class Memory: public Operand {
   public:
    Memory(int base, int offset, int index = NoRegister, unsigned scale = 1):
      base(base), offset(offset), index(index), scale(scale)
    { }

    int base;
    int offset;
    int index;
    unsigned scale;
  };

  class Client {
   public:
    virtual int acquireTemporary
    (uint32_t mask = ~static_cast<uint32_t>(0)) = 0;
    virtual void releaseTemporary(int r) = 0;

    virtual void save(int r) = 0;
  };

  class Block {
   public:
    virtual unsigned resolve(unsigned start, Block* next) = 0;
  };

  class Architecture {
   public:
    virtual unsigned floatRegisterSize() = 0;

    virtual uint32_t generalRegisterMask() = 0;
    virtual uint32_t floatRegisterMask() = 0;

    virtual int scratch() = 0;
    virtual int stack() = 0;
    virtual int thread() = 0;
    virtual int returnLow() = 0;
    virtual int returnHigh() = 0;
    virtual int virtualCallTarget() = 0;
    virtual int virtualCallIndex() = 0;

    virtual bool bigEndian() = 0;

    virtual uintptr_t maximumImmediateJump() = 0;

    virtual bool alwaysCondensed(BinaryOperation op) = 0;
    virtual bool alwaysCondensed(TernaryOperation op) = 0;

    virtual bool reserved(int register_) = 0;

    virtual unsigned frameFootprint(unsigned footprint) = 0;
    virtual unsigned argumentFootprint(unsigned footprint) = 0;
    virtual bool argumentAlignment() = 0;
    virtual bool argumentRegisterAlignment() = 0;
    virtual unsigned argumentRegisterCount() = 0;
    virtual int argumentRegister(unsigned index) = 0;

    virtual bool hasLinkRegister() = 0;

    virtual unsigned stackAlignmentInWords() = 0;

    virtual bool matchCall(void* returnAddress, void* target) = 0;

    virtual void updateCall(UnaryOperation op, void* returnAddress,
                            void* newTarget) = 0;

    virtual void setConstant(void* dst, uint64_t constant) = 0;

    virtual unsigned alignFrameSize(unsigned sizeInWords) = 0;

    virtual void nextFrame(void* start, unsigned size, unsigned footprint,
                           void* link, void* stackLimit,
                           unsigned targetParameterFootprint, void** ip,
                           void** stack) = 0;
    virtual void* frameIp(void* stack) = 0;
    virtual unsigned frameHeaderSize() = 0;
    virtual unsigned frameReturnAddressSize() = 0;
    virtual unsigned frameFooterSize() = 0;
    virtual int returnAddressOffset() = 0;
    virtual int framePointerOffset() = 0;

    virtual void plan
    (UnaryOperation op,
     unsigned aSize, uint8_t* aTypeMask, uint64_t* aRegisterMask,
     bool* thunk) = 0;

    virtual void planSource
    (BinaryOperation op,
     unsigned aSize, uint8_t* aTypeMask, uint64_t* aRegisterMask,
     unsigned bSize, bool* thunk) = 0;
     
    virtual void planDestination
    (BinaryOperation op,
     unsigned aSize, uint8_t aTypeMask, uint64_t aRegisterMask,
     unsigned bSize, uint8_t* bTypeMask, uint64_t* bRegisterMask) = 0;

    virtual void planMove
    (unsigned size, uint8_t* srcTypeMask, uint64_t* srcRegisterMask,
     uint8_t* tmpTypeMask, uint64_t* tmpRegisterMask,
     uint8_t dstTypeMask, uint64_t dstRegisterMask) = 0; 

    virtual void planSource
    (TernaryOperation op,
     unsigned aSize, uint8_t* aTypeMask, uint64_t* aRegisterMask,
     unsigned bSize, uint8_t* bTypeMask, uint64_t* bRegisterMask,
     unsigned cSize, bool* thunk) = 0; 

    virtual void planDestination
    (TernaryOperation op,
     unsigned aSize, uint8_t aTypeMask, uint64_t aRegisterMask,
     unsigned bSize, uint8_t bTypeMask, uint64_t bRegisterMask,
     unsigned cSize, uint8_t* cTypeMask, uint64_t* cRegisterMask) = 0; 

    virtual void acquire() = 0;
    virtual void release() = 0;
  };

  virtual void setClient(Client* client) = 0;

  virtual Architecture* arch() = 0;

  virtual void checkStackOverflow(uintptr_t handler,
                                  unsigned stackLimitOffsetFromThread) = 0;
  virtual void saveFrame(unsigned stackOffset, unsigned ipOffset) = 0;
  virtual void pushFrame(unsigned argumentCount, ...) = 0;
  virtual void allocateFrame(unsigned footprint) = 0;
  virtual void adjustFrame(unsigned difference) = 0;
  virtual void popFrame(unsigned footprint) = 0;
  virtual void popFrameForTailCall(unsigned footprint, int offset,
                                   int returnAddressSurrogate,
                                   int framePointerSurrogate) = 0;
  virtual void popFrameAndPopArgumentsAndReturn(unsigned frameFootprint,
                                                unsigned argumentFootprint)
  = 0;
  virtual void popFrameAndUpdateStackAndReturn(unsigned frameFootprint,
                                               unsigned stackOffsetFromThread)
  = 0;

  virtual void apply(Operation op) = 0;

  virtual void apply(UnaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand) = 0;

  virtual void apply(BinaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand,
                     unsigned bSize, OperandType bType, Operand* bOperand) = 0;

  virtual void apply(TernaryOperation op,
                     unsigned aSize, OperandType aType, Operand* aOperand,
                     unsigned bSize, OperandType bType, Operand* bOperand,
                     unsigned cSize, OperandType cType, Operand* cOperand) = 0;

  virtual void setDestination(uint8_t* dst) = 0;

  virtual void write() = 0;

  virtual Promise* offset(bool forTrace = false) = 0;

  virtual Block* endBlock(bool startNew) = 0;

  virtual void endEvent() = 0;

  virtual unsigned length() = 0;

  virtual unsigned footerSize() = 0;

  virtual void dispose() = 0;
};

Assembler::Architecture*
makeArchitecture(System* system, bool useNativeFeatures);

Assembler*
makeAssembler(System* system, Allocator* allocator, Zone* zone,
              Assembler::Architecture* architecture);

} // namespace vm

#endif//ASSEMBLER_H

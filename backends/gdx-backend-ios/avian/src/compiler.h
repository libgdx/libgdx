/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef COMPILER_H
#define COMPILER_H

#include "system.h"
#include "zone.h"
#include "assembler.h"

namespace vm {

class TraceHandler {
 public:
  virtual void handleTrace(Promise* address, unsigned argumentIndex) = 0;
};

class Compiler {
 public:
  class Client {
   public:
    virtual intptr_t getThunk(UnaryOperation op, unsigned size) = 0;
    virtual intptr_t getThunk(BinaryOperation op, unsigned size,
                              unsigned resultSize) = 0;
    virtual intptr_t getThunk(TernaryOperation op, unsigned size,
                              unsigned resultSize, bool* threadParameter) = 0;
  };
  
  static const unsigned Aligned  = 1 << 0;
  static const unsigned NoReturn = 1 << 1;
  static const unsigned TailJump = 1 << 2;
  static const unsigned LongJumpOrCall = 1 << 3;

  enum OperandType {
    ObjectType,
    AddressType,
    IntegerType,
    FloatType,
    VoidType
  };

  class Operand { };
  class State { };
  class Subroutine { };

  virtual State* saveState() = 0;
  virtual void restoreState(State* state) = 0;

  virtual Subroutine* startSubroutine() = 0;
  virtual void returnFromSubroutine(Subroutine* subroutine, Operand* address)
  = 0;
  virtual void linkSubroutine(Subroutine* subroutine) = 0;

  virtual void init(unsigned logicalCodeSize, unsigned parameterFootprint,
                    unsigned localFootprint, unsigned alignedFrameSize) = 0;

  virtual void visitLogicalIp(unsigned logicalIp) = 0;
  virtual void startLogicalIp(unsigned logicalIp) = 0;

  virtual Promise* machineIp(unsigned logicalIp) = 0;

  virtual Promise* poolAppend(intptr_t value) = 0;
  virtual Promise* poolAppendPromise(Promise* value) = 0;

  virtual Operand* constant(int64_t value, OperandType type) = 0;
  virtual Operand* promiseConstant(Promise* value, OperandType type) = 0;
  virtual Operand* address(Promise* address) = 0;
  virtual Operand* memory(Operand* base,
                          OperandType type,
                          int displacement = 0,
                          Operand* index = 0,
                          unsigned scale = 1) = 0;

  virtual Operand* register_(int number) = 0;

  virtual void push(unsigned footprint) = 0;
  virtual void push(unsigned footprint, Operand* value) = 0;
  virtual void save(unsigned footprint, Operand* value) = 0;
  virtual Operand* pop(unsigned footprint) = 0;
  virtual void pushed() = 0;
  virtual void popped(unsigned footprint) = 0;
  virtual unsigned topOfStack() = 0;
  virtual Operand* peek(unsigned footprint, unsigned index) = 0;

  virtual Operand* call(Operand* address,
                        unsigned flags,
                        TraceHandler* traceHandler,
                        unsigned resultSize,
                        OperandType resultType,
                        unsigned argumentCount,
                        ...) = 0;

  virtual Operand* stackCall(Operand* address,
                             unsigned flags,
                             TraceHandler* traceHandler,
                             unsigned resultSize,
                             OperandType resultType,
                             unsigned argumentFootprint) = 0;

  virtual void return_(unsigned size, Operand* value) = 0;

  virtual void initLocal(unsigned size, unsigned index, OperandType type) = 0;
  virtual void initLocalsFromLogicalIp(unsigned logicalIp) = 0;
  virtual void storeLocal(unsigned footprint, Operand* src,
                          unsigned index) = 0;
  virtual Operand* loadLocal(unsigned footprint, unsigned index) = 0;
  virtual void saveLocals() = 0;

  virtual void checkBounds(Operand* object, unsigned lengthOffset,
                           Operand* index, intptr_t handler) = 0;

  virtual void store(unsigned srcSize, Operand* src, unsigned dstSize,
                     Operand* dst) = 0;
  virtual Operand* load(unsigned srcSize, unsigned srcSelectSize, Operand* src,
                        unsigned dstSize) = 0;
  virtual Operand* loadz(unsigned size, unsigned srcSelectSize, Operand* src,
                         unsigned dstSize) = 0;

  virtual void jumpIfEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfNotEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfLess
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfGreater
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfLessOrEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfGreaterOrEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;

  virtual void jumpIfFloatEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatNotEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatLess
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatGreater
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatLessOrEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatGreaterOrEqual
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatLessOrUnordered
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatGreaterOrUnordered
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatLessOrEqualOrUnordered
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;
  virtual void jumpIfFloatGreaterOrEqualOrUnordered
  (unsigned size, Operand* a, Operand* b, Operand* address) = 0;

  virtual void jmp(Operand* address) = 0;
  virtual void exit(Operand* address) = 0;
  virtual Operand* add(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* sub(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* mul(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* div(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* rem(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* fadd(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* fsub(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* fmul(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* fdiv(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* frem(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* shl(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* shr(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* ushr(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* and_(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* or_(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* xor_(unsigned size, Operand* a, Operand* b) = 0;
  virtual Operand* neg(unsigned size, Operand* a) = 0;
  virtual Operand* fneg(unsigned size, Operand* a) = 0;
  virtual Operand* abs(unsigned size, Operand* a) = 0;
  virtual Operand* fabs(unsigned size, Operand* a) = 0;
  virtual Operand* fsqrt(unsigned size, Operand* a) = 0;
  virtual Operand* f2f(unsigned aSize, unsigned resSize, Operand* a) = 0;
  virtual Operand* f2i(unsigned aSize, unsigned resSize, Operand* a) = 0;
  virtual Operand* i2f(unsigned aSize, unsigned resSize, Operand* a) = 0;

  virtual void loadBarrier() = 0;
  virtual void storeStoreBarrier() = 0;
  virtual void storeLoadBarrier() = 0;

  virtual void compile(uintptr_t stackOverflowHandler,
                       unsigned stackLimitOffset) = 0;
  virtual unsigned resolve(uint8_t* dst) = 0;
  virtual unsigned poolSize() = 0;
  virtual void write() = 0;

  virtual void dispose() = 0;
};

Compiler*
makeCompiler(System* system, Assembler* assembler, Zone* zone,
             Compiler::Client* client);

} // namespace vm

#endif//COMPILER_H

/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef POWERPC_H
#define POWERPC_H

#include "types.h"
#include "common.h"

#ifdef __APPLE__
#  include "mach/mach_types.h"
#  include "mach/ppc/thread_act.h"
#  include "mach/ppc/thread_status.h"

#  define THREAD_STATE PPC_THREAD_STATE
#  define THREAD_STATE_TYPE ppc_thread_state_t
#  define THREAD_STATE_COUNT PPC_THREAD_STATE_COUNT

#  if __DARWIN_UNIX03 && defined(_STRUCT_PPC_EXCEPTION_STATE)
#    define FIELD(x) __##x
#  else
#    define FIELD(x) x
#  endif

#  define THREAD_STATE_IP(state) ((state).FIELD(srr0))
#  define THREAD_STATE_STACK(state) ((state).FIELD(r1))
#  define THREAD_STATE_THREAD(state) ((state).FIELD(r13))
#  define THREAD_STATE_LINK(state) ((state).FIELD(lr))

#  define IP_REGISTER(context) \
  THREAD_STATE_IP(context->uc_mcontext->FIELD(ss))
#  define STACK_REGISTER(context) \
  THREAD_STATE_STACK(context->uc_mcontext->FIELD(ss))
#  define THREAD_REGISTER(context) \
  THREAD_STATE_THREAD(context->uc_mcontext->FIELD(ss))
#  define LINK_REGISTER(context) \
  THREAD_STATE_LINK(context->uc_mcontext->FIELD(ss))

#define VA_LIST(x) (&(x))

#else // not __APPLE__
#  define IP_REGISTER(context) (context->uc_mcontext.regs->gpr[32])
#  define STACK_REGISTER(context) (context->uc_mcontext.regs->gpr[1])
#  define THREAD_REGISTER(context) (context->uc_mcontext.regs->gpr[13])
#  define LINK_REGISTER(context) (context->uc_mcontext.regs->gpr[36])

#define VA_LIST(x) (x)

#endif // not __APPLE__

extern "C" uint64_t
vmNativeCall(void* function, unsigned stackTotal, void* memoryTable,
             unsigned memoryCount, unsigned memoryBase,
             void* gprTable, void* fprTable, unsigned returnType);

namespace vm {

inline void
trap()
{
  asm("trap");
}

inline void
memoryBarrier()
{
  __asm__ __volatile__("sync": : :"memory");
}

inline void
storeStoreMemoryBarrier()
{
  memoryBarrier();
}

inline void
storeLoadMemoryBarrier()
{
  memoryBarrier();
}

inline void
loadMemoryBarrier()
{
  memoryBarrier();
}

inline void
syncInstructionCache(const void* start, unsigned size)
{
  const unsigned CacheLineSize = 32;
  const uintptr_t Mask = ~(CacheLineSize - 1);

  uintptr_t cacheLineStart = reinterpret_cast<uintptr_t>(start) & Mask;
  uintptr_t cacheLineEnd
    = (reinterpret_cast<uintptr_t>(start) + size + CacheLineSize - 1) & Mask;

  for (uintptr_t p = cacheLineStart; p < cacheLineEnd; p += CacheLineSize) {
    __asm__ __volatile__("dcbf 0, %0" : : "r" (p));
  }

  __asm__ __volatile__("sync");

  for (uintptr_t p = cacheLineStart; p < cacheLineEnd; p += CacheLineSize) {
    __asm__ __volatile__("icbi 0, %0" : : "r" (p));
  }

  __asm__ __volatile__("isync");
}

#ifdef USE_ATOMIC_OPERATIONS
inline bool
atomicCompareAndSwap32(uint32_t* p, uint32_t old, uint32_t new_)
{
#if (__GNUC__ >= 4) && (__GNUC_MINOR__ >= 1)
  return __sync_bool_compare_and_swap(p, old, new_);
#else // not GCC >= 4.1
  bool result;

  __asm__ __volatile__("  sync\n"
                       "1:\n"
                       "  lwarx  %0,0,%2\n"
                       "  cmpw   %0,%3\n"
                       "  bne-   2f\n"
                       "  stwcx. %4,0,%2\n"
                       "  bne-   1b\n"
                       "  isync  \n"
                       "2:\n"
                       "  xor    %0,%0,%3\n"
                       "  cntlzw %0,%0\n"
                       "  srwi   %0,%0,5\n"
                       : "=&r"(result), "+m"(*p)
                       : "r"(p), "r"(old), "r"(new_)
                       : "cc", "memory");
 
  return result;
#endif // not GCC >= 4.1
}

inline bool
atomicCompareAndSwap(uintptr_t* p, uintptr_t old, uintptr_t new_)
{
  return atomicCompareAndSwap32(reinterpret_cast<uint32_t*>(p), old, new_);
}
#endif // USE_ATOMIC_OPERATIONS

inline uint64_t
dynamicCall(void* function, uintptr_t* arguments, uint8_t* argumentTypes,
            unsigned argumentCount, unsigned argumentsSize,
            unsigned returnType)
{
#ifdef __APPLE__
#  define SKIP(var, count) var += count;
#  define ALIGN(var)
  const unsigned LinkageArea = 24;
  const unsigned FprCount = 13;
#else
#  define SKIP(var, count)
#  define ALIGN(var) if (var & 1) ++var;
  const unsigned LinkageArea = 8;
  const unsigned FprCount = 8;
#endif

  const unsigned GprCount = 8;
  uintptr_t gprTable[GprCount];
  unsigned gprIndex = 0;

  uint64_t fprTable[FprCount];
  unsigned fprIndex = 0;

  uintptr_t stack[argumentsSize / BytesPerWord];
  unsigned stackSkip = 0;
  unsigned stackIndex = 0;

  unsigned ai = 0;
  for (unsigned ati = 0; ati < argumentCount; ++ ati) {
    switch (argumentTypes[ati]) {
    case FLOAT_TYPE: {
      if (fprIndex < FprCount) {
        double d = bitsToFloat(arguments[ai]);
        memcpy(fprTable + fprIndex, &d, 8);
        ++ fprIndex;
        SKIP(gprIndex, 1);
        SKIP(stackSkip, 1);
      } else {
        stack[stackIndex++] = arguments[ai];
      }
      ++ ai;
    } break;

    case DOUBLE_TYPE: {
      if (fprIndex + (8 / BytesPerWord) <= FprCount) {
        memcpy(fprTable + fprIndex, arguments + ai, 8);
        ++ fprIndex;
        SKIP(gprIndex, 8 / BytesPerWord);
        SKIP(stackSkip, 8 / BytesPerWord);
      } else {
        ALIGN(stackIndex);
        memcpy(stack + stackIndex, arguments + ai, 8);
        stackIndex += 8 / BytesPerWord;
      }
      ai += 8 / BytesPerWord;
    } break;

    case INT64_TYPE: {
      if (gprIndex + (8 / BytesPerWord) <= GprCount) {
        ALIGN(gprIndex);
        memcpy(gprTable + gprIndex, arguments + ai, 8);
        gprIndex += 8 / BytesPerWord;
        SKIP(stackSkip, 8 / BytesPerWord);
      } else {
        ALIGN(stackIndex);
        memcpy(stack + stackIndex, arguments + ai, 8);
        stackIndex += 8 / BytesPerWord;
      }
      ai += 8 / BytesPerWord;
    } break;

    default: {
      if (gprIndex < GprCount) {
        gprTable[gprIndex++] = arguments[ai];
        SKIP(stackSkip, 1);
      } else {
        stack[stackIndex++] = arguments[ai];
      }
      ++ ai;
    } break;
    }
  }

  return vmNativeCall
    (function,
     (((1 + stackSkip + stackIndex) * BytesPerWord) + LinkageArea + 15) & -16,
     stack, stackIndex * BytesPerWord,
     LinkageArea + (stackSkip * BytesPerWord),
     (gprIndex ? gprTable : 0),
     (fprIndex ? fprTable : 0), returnType);
}

} // namespace vm

#endif//POWERPC_H

/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef ARM_H
#define ARM_H

#include "types.h"
#include "common.h"

#ifdef __APPLE__
#  include "libkern/OSAtomic.h"
#  include "libkern/OSCacheControl.h"
#  include "mach/mach_types.h"
#  include "mach/arm/thread_act.h"
#  include "mach/arm/thread_status.h"

#  define THREAD_STATE ARM_THREAD_STATE
#  define THREAD_STATE_TYPE arm_thread_state_t
#  define THREAD_STATE_COUNT ARM_THREAD_STATE_COUNT

#  if __DARWIN_UNIX03 && defined(_STRUCT_ARM_EXCEPTION_STATE)
#    define FIELD(x) __##x
#  else
#    define FIELD(x) x
#  endif

#  define THREAD_STATE_IP(state) ((state).FIELD(pc))
#  define THREAD_STATE_STACK(state) ((state).FIELD(sp))
#  define THREAD_STATE_THREAD(state) ((state).FIELD(r[8]))
#  define THREAD_STATE_LINK(state) ((state).FIELD(lr))

#  define IP_REGISTER(context) \
  THREAD_STATE_IP(context->uc_mcontext->FIELD(ss))
#  define STACK_REGISTER(context) \
  THREAD_STATE_STACK(context->uc_mcontext->FIELD(ss))
#  define THREAD_REGISTER(context) \
  THREAD_STATE_THREAD(context->uc_mcontext->FIELD(ss))
#  define LINK_REGISTER(context) \
  THREAD_STATE_LINK(context->uc_mcontext->FIELD(ss))
#else // not __APPLE__
#  define IP_REGISTER(context) (context->uc_mcontext.arm_pc)
#  define STACK_REGISTER(context) (context->uc_mcontext.arm_sp)
#  define THREAD_REGISTER(context) (context->uc_mcontext.arm_ip)
#  define LINK_REGISTER(context) (context->uc_mcontext.arm_lr)
#endif

#define VA_LIST(x) (&(x))

extern "C" uint64_t
vmNativeCall(void* function, unsigned stackTotal, void* memoryTable,
             unsigned memoryCount, void* gprTable);

namespace vm {

inline void
trap()
{
  asm("bkpt");
}

inline void
memoryBarrier()
{
  asm("nop");
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
#ifdef __APPLE__
  sys_icache_invalidate(const_cast<void*>(start), size);
#else
  __clear_cache
    (const_cast<void*>(start),
     const_cast<uint8_t*>(static_cast<const uint8_t*>(start) + size));
#endif
}

#ifndef __APPLE__
typedef int (__kernel_cmpxchg_t)(int oldval, int newval, int *ptr);
#  define __kernel_cmpxchg (*(__kernel_cmpxchg_t *)0xffff0fc0)
#endif

inline bool
atomicCompareAndSwap32(uint32_t* p, uint32_t old, uint32_t new_)
{
#ifdef __APPLE__
  return OSAtomicCompareAndSwap32(old, new_, reinterpret_cast<int32_t*>(p));
#else
  int r = __kernel_cmpxchg(static_cast<int>(old), static_cast<int>(new_), reinterpret_cast<int*>(p));
  return (!r ? true : false);
#endif
}

inline bool
atomicCompareAndSwap(uintptr_t* p, uintptr_t old, uintptr_t new_)
{
  return atomicCompareAndSwap32(reinterpret_cast<uint32_t*>(p), old, new_);
}

inline uint64_t
dynamicCall(void* function, uintptr_t* arguments, uint8_t* argumentTypes,
            unsigned argumentCount, unsigned argumentsSize UNUSED,
            unsigned returnType UNUSED)
{
#ifdef __APPLE__
  const unsigned Alignment = 1;
#else
  const unsigned Alignment = 2;
#endif

  const unsigned GprCount = 4;
  uintptr_t gprTable[GprCount];
  unsigned gprIndex = 0;

  uintptr_t stack[(argumentCount * 8) / BytesPerWord]; // is > argumentSize to account for padding
  unsigned stackIndex = 0;

  unsigned ai = 0;
  for (unsigned ati = 0; ati < argumentCount; ++ ati) {
    switch (argumentTypes[ati]) {
    case DOUBLE_TYPE:
    case INT64_TYPE: {
      if (gprIndex + Alignment <= GprCount) { // pass argument in register(s)
        if (Alignment == 1
            and BytesPerWord < 8
            and gprIndex + Alignment == GprCount)
        {
          gprTable[gprIndex++] = arguments[ai];
          stack[stackIndex++] = arguments[ai + 1];
        } else {
          if (gprIndex % Alignment) {
            ++gprIndex;
          }
          
          memcpy(gprTable + gprIndex, arguments + ai, 8);
          gprIndex += 8 / BytesPerWord;
        }
      } else {                                // pass argument on stack
        gprIndex = GprCount;
        if (stackIndex % Alignment) {
          ++stackIndex;
        }

        memcpy(stack + stackIndex, arguments + ai, 8);
        stackIndex += 8 / BytesPerWord;
      }
      ai += 8 / BytesPerWord;
    } break;

    default: {
      if (gprIndex < GprCount) {
        gprTable[gprIndex++] = arguments[ai];
      } else {
        stack[stackIndex++] = arguments[ai];
      }
      ++ ai;
    } break;
    }
  }

  if (gprIndex < GprCount) { // pad since assembly loads all GPRs
    memset(gprTable + gprIndex, 0, (GprCount-gprIndex)*4);
    gprIndex = GprCount;
  }

  unsigned stackSize = stackIndex*BytesPerWord + ((stackIndex & 1) << 2);
  return vmNativeCall
    (function, stackSize, stack, stackIndex * BytesPerWord,
     (gprIndex ? gprTable : 0));
}

} // namespace vm

#endif // ARM_H

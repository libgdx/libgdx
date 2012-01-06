/* Copyright (c) 2010-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "types.h"

.text

#define BYTES_PER_WORD 4

#define LOCAL(x) .L##x
   
#ifdef __APPLE__
#  define GLOBAL(x) _##x
#else
#  define GLOBAL(x) x   
#endif

#define THREAD_STACK 2148
#define THREAD_SCRATCH 2152
#define THREAD_CONTINUATION 2156
#define THREAD_EXCEPTION 44
#define THREAD_EXCEPTION_STACK_ADJUSTMENT 2160
#define THREAD_EXCEPTION_OFFSET 2164
#define THREAD_EXCEPTION_HANDLER 2168

#define CONTINUATION_NEXT 4
#define CONTINUATION_ADDRESS 16
#define CONTINUATION_RETURN_ADDRESS_OFFSET 20
#define CONTINUATION_FRAME_POINTER_OFFSET 24
#define CONTINUATION_LENGTH 28
#define CONTINUATION_BODY 32
   
.globl GLOBAL(vmInvoke)
GLOBAL(vmInvoke):
  /*
    arguments
    r0       : thread
    r1       : function
    r2       : arguments
    r3       : argumentFootprint
    [sp, #0] : frameSize (not used)
    [sp, #4] : returnType
  */

  // save all non-volatile registers
  stmfd sp!, {r4-r11, lr}

  // save return type
  ldr   r4, [sp, #4]
  str   r4, [sp, #-4]!

  str   sp, [r0, #THREAD_SCRATCH]

  // align stack, if necessary
  eor   r4, sp, r3
  tst   r4, #4
  subne sp, sp, #4
   
  // copy arguments into place
  sub   sp, r3
  mov   r4, #0
  b     LOCAL(vmInvoke_argumentTest)

LOCAL(vmInvoke_argumentLoop):
  ldr   r5, [r2, r4]
  str   r5, [sp, r4]
  add   r4, r4, #BYTES_PER_WORD

LOCAL(vmInvoke_argumentTest):
  cmp   r4, r3
  blt   LOCAL(vmInvoke_argumentLoop)

  // we use r8 to hold the thread pointer, by convention
  mov   r8, r0

  // load and call function address
  blx   r1

.globl GLOBAL(vmInvoke_returnAddress)
GLOBAL(vmInvoke_returnAddress):
  // restore stack pointer
  ldr   sp, [r8, #THREAD_SCRATCH]
   
   // clear MyThread::stack to avoid confusing another thread calling
   // java.lang.Thread.getStackTrace on this one.  See
   // MyProcess::getStackTrace in compile.cpp for details on how we get
   // a reliable stack trace from a thread that might be interrupted at
   // any point in its execution.
   mov  r5, #0
   str  r5, [r8, #THREAD_STACK]

.globl GLOBAL(vmInvoke_safeStack)
GLOBAL(vmInvoke_safeStack):

#ifdef AVIAN_CONTINUATIONS
   // call the next continuation, if any
   ldr  r5,[r8,#THREAD_CONTINUATION]
   cmp  r5,#0
   beq  LOCAL(vmInvoke_exit)

   ldr  r6,[r5,#CONTINUATION_LENGTH]
   lsl  r6,r6,#2
   neg  r7,r6
   add  r7,r7,#-80
   mov  r4,sp
   str  r4,[sp,r7]!

   add  r7,r5,#CONTINUATION_BODY

   mov  r11,#0
   b    LOCAL(vmInvoke_continuationTest)

LOCAL(vmInvoke_continuationLoop):
   ldr  r9,[r7,r11]
   str  r9,[sp,r11]
   add  r11,r11,#4

LOCAL(vmInvoke_continuationTest):
   cmp  r11,r6
   ble  LOCAL(vmInvoke_continuationLoop)

   ldr  r7,[r5,#CONTINUATION_RETURN_ADDRESS_OFFSET]
   ldr  r10,LOCAL(vmInvoke_returnAddress_word)
   ldr  r11,LOCAL(vmInvoke_getAddress_word)
LOCAL(vmInvoke_getAddress):
   add  r11,pc,r11
   ldr  r11,[r11,r10]
   str  r11,[sp,r7]

   ldr  r7,[r5,#CONTINUATION_NEXT]
   str  r7,[r8,#THREAD_CONTINUATION]

   // call the continuation unless we're handling an exception
   ldr  r7,[r8,#THREAD_EXCEPTION]
   cmp  r7,#0
   bne  LOCAL(vmInvoke_handleException)
   ldr  r7,[r5,#CONTINUATION_ADDRESS]
   bx   r7

LOCAL(vmInvoke_handleException):
   // we're handling an exception - call the exception handler instead
   mov  r11,#0
   str  r11,[r8,#THREAD_EXCEPTION]
   ldr  r11,[r8,#THREAD_EXCEPTION_STACK_ADJUSTMENT]
   ldr  r9,[sp]
   neg  r11,r11
   str  r9,[sp,r11]!
   ldr  r11,[r8,#THREAD_EXCEPTION_OFFSET]
   str  r7,[sp,r11]

   ldr  r7,[r8,#THREAD_EXCEPTION_HANDLER]
   bx   r7

LOCAL(vmInvoke_exit):
#endif // AVIAN_CONTINUATIONS

  mov   ip, #0
  str   ip, [r8, #THREAD_STACK]

  // restore return type
  ldr   ip, [sp], #4

  // restore callee-saved registers
  ldmfd sp!, {r4-r11, lr}

LOCAL(vmInvoke_return):
  bx    lr

.globl GLOBAL(vmJumpAndInvoke)
GLOBAL(vmJumpAndInvoke):
#ifdef AVIAN_CONTINUATIONS
   //      r0: thread
   //      r1: address
   //      r2: stack
   //      r3: argumentFootprint
   // [sp,#0]: arguments
   // [sp,#4]: frameSize

   ldr  r5,[sp,#0]
   ldr  r6,[sp,#4]

   // allocate new frame, adding room for callee-saved registers, plus
   // 4 bytes of padding since the calculation of frameSize assumes 4
   // bytes have already been allocated to save the return address,
   // which is not true in this case
   sub  r2,r2,r6
   sub  r2,r2,#84
   
   mov  r8,r0

   // copy arguments into place
   mov  r6,#0
   b    LOCAL(vmJumpAndInvoke_argumentTest)

LOCAL(vmJumpAndInvoke_argumentLoop):
   ldr  r12,[r5,r6]
   str  r12,[r2,r6]
   add  r6,r6,#4

LOCAL(vmJumpAndInvoke_argumentTest):
   cmp  r6,r3
   ble  LOCAL(vmJumpAndInvoke_argumentLoop)

   // the arguments have been copied, so we can set the real stack
   // pointer now
   mov  sp,r2
   
   // set return address to vmInvoke_returnAddress
   ldr  r10,LOCAL(vmInvoke_returnAddress_word)
   ldr  r11,LOCAL(vmJumpAndInvoke_getAddress_word)
LOCAL(vmJumpAndInvoke_getAddress):
   add  r11,pc,r11
   ldr  lr,[r11,r10]

   bx   r1

LOCAL(vmInvoke_returnAddress_word):
   .word GLOBAL(vmInvoke_returnAddress)(GOT)
LOCAL(vmInvoke_getAddress_word):
   .word _GLOBAL_OFFSET_TABLE_-(LOCAL(vmInvoke_getAddress)+8)
LOCAL(vmJumpAndInvoke_getAddress_word):
   .word _GLOBAL_OFFSET_TABLE_-(LOCAL(vmJumpAndInvoke_getAddress)+8)
   
#else // not AVIAN_CONTINUATIONS
   // vmJumpAndInvoke should only be called when continuations are
   // enabled
   bkpt
#endif // not AVIAN_CONTINUATIONS

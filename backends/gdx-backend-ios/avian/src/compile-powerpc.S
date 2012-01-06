/* Copyright (c) 2009-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "types.h"

.text

#define BYTES_PER_WORD 4
   
#ifdef __APPLE__
#  define GLOBAL(x) _##x
#  define LOCAL(x) L##x
#  define LINKAGE_AREA 6
#  define RETURN_ADDRESS_OFFSET 8
#else
#  define GLOBAL(x) x
#  define LOCAL(x) .L##x
#  define LINKAGE_AREA 2
#  define RETURN_ADDRESS_OFFSET 4
#  include "powerpc-regs.S"
#endif

#define ARGUMENT_BASE BYTES_PER_WORD * LINKAGE_AREA
      
#define THREAD_STACK 2148
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
   // save return address
   mflr r0
   stw  r0,RETURN_ADDRESS_OFFSET(r1)

   // r3: thread
   // r4: function
   // r5: arguments
   // r6: argumentFootprint
   // r7: frameSize
   // r8: returnType

   // r9: temporary

   // allocate stack space, adding room for callee-saved registers and
   // return type
   subfic r9,r7,-80
   stwux r1,r1,r9
   
   // save callee-saved registers
   add  r9,r7,r1
   
   stw  r13,0(r9)
   stw  r14,4(r9)
   stw  r15,8(r9)
   stw  r16,12(r9)
   stw  r17,16(r9)
   stw  r18,20(r9)
   stw  r19,24(r9)
   stw  r20,28(r9)
   stw  r21,32(r9)
   stw  r22,36(r9)
   stw  r23,40(r9)
   stw  r24,44(r9)
   stw  r25,48(r9)
   stw  r26,52(r9)
   stw  r27,56(r9)
   stw  r28,60(r9)
   stw  r29,64(r9)
   stw  r30,68(r9)
   stw  r31,72(r9)

   // save return type
   stw  r8,76(r9)

   // we use r13 to hold the thread pointer, by convention
   mr   r13,r3
   
   // copy arguments into place
   li   r16,0
   addi r18,r1,ARGUMENT_BASE
   b    LOCAL(vmInvoke_argumentTest)
   
LOCAL(vmInvoke_argumentLoop):
   lwzx r17,r16,r5
   stwx r17,r16,r18
   addi r16,r16,BYTES_PER_WORD
   
LOCAL(vmInvoke_argumentTest):
   cmplw r16,r6
   blt  LOCAL(vmInvoke_argumentLoop)

   // load and call function address
   mtctr r4
   bctrl

.globl GLOBAL(vmInvoke_returnAddress)
GLOBAL(vmInvoke_returnAddress):
   // restore stack pointer
	 lwz	r1,0(r1)
   
   // clear MyThread::stack to avoid confusing another thread calling
   // java.lang.Thread.getStackTrace on this one.  See
   // MyProcess::getStackTrace in compile.cpp for details on how we get
   // a reliable stack trace from a thread that might be interrupted at
   // any point in its execution.
   li   r5,0
   stw  r5,THREAD_STACK(r13)
   
.globl GLOBAL(vmInvoke_safeStack)
GLOBAL(vmInvoke_safeStack):

#ifdef AVIAN_CONTINUATIONS
   // call the next continuation, if any
   lwz  r5,THREAD_CONTINUATION(r13)
   cmplwi r5,0
   beq  LOCAL(vmInvoke_exit)

   lwz  r6,CONTINUATION_LENGTH(r5)
   slwi r6,r6,2
   subfic r7,r6,-80
   stwux r1,r1,r7

   addi r7,r5,CONTINUATION_BODY

   li   r8,0
   addi r10,r1,ARGUMENT_BASE
   b    LOCAL(vmInvoke_continuationTest)

LOCAL(vmInvoke_continuationLoop):
   lwzx r9,r7,r8
   stwx r9,r10,r8
   addi r8,r8,4

LOCAL(vmInvoke_continuationTest):
   cmplw r8,r6
   ble  LOCAL(vmInvoke_continuationLoop)

   lwz  r7,CONTINUATION_RETURN_ADDRESS_OFFSET(r5)
   bl   LOCAL(vmInvoke_getPC)
   
LOCAL(vmInvoke_getPC):
   mflr r10
#ifdef __APPLE__
   la   r10,lo16(GLOBAL(vmInvoke_returnAddress)-LOCAL(vmInvoke_getPC))(r10)
#else
   lwz  r10,LOCAL(vmInvoke_returnAddress_address)-LOCAL(vmInvoke_getPC)(r10)
#endif
   stwx r10,r1,r7

   lwz  r7,CONTINUATION_FRAME_POINTER_OFFSET(r5)
   lwz  r8,0(r1)
   add  r7,r7,r1
   stw  r8,0(r7)
   stw  r7,0(r1)

   lwz  r7,CONTINUATION_NEXT(r5)
   stw  r7,THREAD_CONTINUATION(r13)

   // call the continuation unless we're handling an exception
   lwz  r7,THREAD_EXCEPTION(r13)
   cmpwi r7,0
   bne  LOCAL(vmInvoke_handleException)
   lwz  r7,CONTINUATION_ADDRESS(r5)
   mtctr r7
   bctr

LOCAL(vmInvoke_handleException):
   // we're handling an exception - call the exception handler instead
   li   r8,0
   stw  r8,THREAD_EXCEPTION(r13)
   lwz  r8,THREAD_EXCEPTION_STACK_ADJUSTMENT(r13)
   lwz  r9,0(r1)
   subfic r8,r8,0
   stwux r9,r1,r8
   lwz  r8,THREAD_EXCEPTION_OFFSET(r13)
   stwx r7,r1,r8

   lwz  r7,THREAD_EXCEPTION_HANDLER(r13)
   mtctr r7
   bctr

LOCAL(vmInvoke_exit):
#endif // AVIAN_CONTINUATIONS
   
   // restore callee-saved registers
   subi r9,r1,80
   
   lwz  r13,0(r9)
   lwz  r14,4(r9)
   lwz  r15,8(r9)
   lwz  r16,12(r9)
   lwz  r17,16(r9)
   lwz  r18,20(r9)
   lwz  r19,24(r9)
   lwz  r20,28(r9)
   lwz  r21,32(r9)
   lwz  r22,36(r9)
   lwz  r23,40(r9)
   lwz  r24,44(r9)
   lwz  r25,48(r9)
   lwz  r26,52(r9)
   lwz  r27,56(r9)
   lwz  r28,60(r9)
   lwz  r29,64(r9)
   lwz  r30,68(r9)
   lwz  r31,72(r9)
   
   // handle return value based on expected type
   lwz  r8,76(r9)
   
LOCAL(vmInvoke_return):
   // load return address
   lwz  r0,RETURN_ADDRESS_OFFSET(r1)
   mtlr r0

   // return
   blr

#ifndef __APPLE__
LOCAL(vmInvoke_returnAddress_address):
   .long GLOBAL(vmInvoke_returnAddress)
#endif

.globl GLOBAL(vmJumpAndInvoke)
GLOBAL(vmJumpAndInvoke):
#ifdef AVIAN_CONTINUATIONS
   // r3: thread
   // r4: address
   // r5: stack
   // r6: argumentFootprint
   // r7: arguments
   // r8: frameSize

   // restore (pseudo)-stack pointer (we don't want to touch the real
   // stack pointer, since we haven't copied the arguments yet)
   lwz  r5,0(r5)

   // make everything between r1 and r5 one big stack frame while we
   // shuffle things around
   stw  r5,0(r1)

   // allocate new frame, adding room for callee-saved registers
   subfic r10,r8,-80
	 stwux r5,r5,r10
   
   mr   r13,r3

   // copy arguments into place
   li   r8,0
   addi r11,r5,ARGUMENT_BASE
   b    LOCAL(vmJumpAndInvoke_argumentTest)

LOCAL(vmJumpAndInvoke_argumentLoop):
   lwzx r12,r7,r8
   stwx r12,r11,r8
   addi r8,r8,4

LOCAL(vmJumpAndInvoke_argumentTest):
   cmplw r8,r6
   ble  LOCAL(vmJumpAndInvoke_argumentLoop)

   // the arguments have been copied, so we can set the real stack
   // pointer now
   mr   r1,r5
   
   // set return address to vmInvoke_returnAddress
   bl   LOCAL(vmJumpAndInvoke_getPC)
   
LOCAL(vmJumpAndInvoke_getPC):
   mflr r10
#ifdef __APPLE__
   la   r10,lo16(GLOBAL(vmInvoke_returnAddress)-LOCAL(vmJumpAndInvoke_getPC))(r10)
#else
   lwz  r10,LOCAL(vmInvoke_returnAddress_address)-LOCAL(vmJumpAndInvoke_getPC)(r10)
#endif
   mtlr r10

   mtctr r4
   bctr
#else // not AVIAN_CONTINUATIONS
   // vmJumpAndInvoke should only be called when continuations are
   // enabled
   trap
#endif // not AVIAN_CONTINUATIONS

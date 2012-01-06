/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "types.h"

.text

#define BYTES_PER_WORD 4
#define GPR_COUNT 8
   
#ifdef __APPLE__
#  define GLOBAL(x) _##x
#  define LOCAL(x) L##x
#  define LINKAGE_AREA 6
#  define MEMORY_BASE BYTES_PER_WORD * (LINKAGE_AREA + GPR_COUNT)
#  define RETURN_ADDRESS_OFFSET 8
#else
#  define GLOBAL(x) x
#  define LOCAL(x) .L##x
#  define LINKAGE_AREA 2
#  define MEMORY_BASE BYTES_PER_WORD * LINKAGE_AREA
#  define RETURN_ADDRESS_OFFSET 4
#  include "powerpc-regs.S"
#endif

.globl GLOBAL(vmNativeCall)
GLOBAL(vmNativeCall):
   // save return address
   mflr r0
   stw  r0,RETURN_ADDRESS_OFFSET(r1)
   
   // r3  aka r13: function
   // r4         : stackTotal
   // r5         : memoryTable
   // r6         : memoryCount
   // r7         : memoryBase
   // r8         : gprTable
   // r9         : fprTable
   // r10 aka r14: returnType
   
   // r15       : stack frame size
   // r16       : temporary
   // r17       : temporary
   // r18       : temporary

   // allocate stack space, adding room for callee-saved registers and
   // scratch space for copying a FP return value into GPRs
   subfic r11,r4,-48
	 stwux r1,r1,r11

   // save callee-saved registers used for local variables
   add  r11,r4,r1

   // save registers used for local variables
   stw  r13,0(r11)
   stw  r14,4(r11)
   stw  r15,8(r11)
   stw  r16,12(r11)
   stw  r17,16(r11)
   stw  r18,20(r11)
   stw  r19,24(r11)

   // remember where we saved the local variables
   mr   r19,r11

   // save our argument registers so we can clobber them
   mr   r13,r3
   mr   r14,r10

   li   r16,0
   b    LOCAL(test)
   
LOCAL(loop):
   lwzx r17,r16,r5
   add  r18,r16,r7
   stwx r17,r18,r1
   addi r16,r16,BYTES_PER_WORD
   
LOCAL(test):
   cmplw r16,r6
   blt  LOCAL(loop)

   // do we need to load the floating point registers?
   cmpwi r9,0
   beq   LOCAL(gpr)

   // yes, we do
   lfd  f1,0(r9)
   lfd  f2,8(r9)
   lfd  f3,16(r9)
   lfd  f4,24(r9)
   lfd  f5,32(r9)
   lfd  f6,40(r9)
   lfd  f7,48(r9)
   lfd  f8,56(r9)
#ifdef __APPLE__
   lfd  f9,64(r9)
   lfd  f10,72(r9)
   lfd  f11,80(r9)
   lfd  f12,88(r9)
   lfd  f13,96(r9)
#endif

LOCAL(gpr):
   // do we need to load the general-purpose registers?
   cmpwi r8,0
   beq  LOCAL(call)

   // yes, we do
   mr   r16,r8
   lwz  r3,0(r16)
   lwz  r4,4(r16)
   lwz  r5,8(r16)
   lwz  r6,12(r16)
   lwz  r7,16(r16)
   lwz  r8,20(r16)
   lwz  r9,24(r16)
   lwz  r10,28(r16)

LOCAL(call):
   // load and call function address
   mtctr r13
   bctrl

   // handle return value based on expected type
   cmpwi r14,VOID_TYPE
   bne  LOCAL(float)
   b    LOCAL(exit)

LOCAL(float):
   cmpwi r14,FLOAT_TYPE
   beq  LOCAL(copy)
   cmpwi r14,DOUBLE_TYPE
   beq  LOCAL(copy)
   cmpwi r14,INT64_TYPE
   beq  LOCAL(exit)
   mr   r4,r3
   b    LOCAL(exit)

LOCAL(copy):
   // move floating point return value to GPRs via memory
   stfd f1,32(r19)
   lwz  r3,32(r19)
   lwz  r4,36(r19)
   b    LOCAL(exit)

LOCAL(exit):
   // restore callee-saved registers used for local variables
   lwz  r13,0(r19)
   lwz  r14,4(r19)
   lwz  r15,8(r19)
   lwz  r16,12(r19)
   lwz  r17,16(r19)
   lwz  r18,20(r19)
   lwz  r19,24(r19)

   // restore stack pointer
	 lwz	r1,0(r1)

   // load return address
   lwz  r0,RETURN_ADDRESS_OFFSET(r1)
   mtlr r0

   // return
   blr
   
.globl GLOBAL(vmJump)
GLOBAL(vmJump):
   mtlr r3
   mr   r1,r5
   mr   r13,r6
   mr   r4,r7
   mr   r3,r8
   blr

#define CHECKPOINT_THREAD 4
#define CHECKPOINT_STACK 24

.globl GLOBAL(vmRun)
GLOBAL(vmRun):
   // r3: function
   // r4: arguments
   // r5: checkpoint

   mflr r0
   stw  r0,RETURN_ADDRESS_OFFSET(r1)

   stwu r1,-(MEMORY_BASE+88)(r1)
      
   stw  r13,MEMORY_BASE+0(r1)
   stw  r14,MEMORY_BASE+4(r1)
   stw  r15,MEMORY_BASE+8(r1)
   stw  r16,MEMORY_BASE+12(r1)
   stw  r17,MEMORY_BASE+16(r1)
   stw  r18,MEMORY_BASE+20(r1)
   stw  r19,MEMORY_BASE+24(r1)
   stw  r20,MEMORY_BASE+28(r1)
   stw  r21,MEMORY_BASE+32(r1)
   stw  r22,MEMORY_BASE+36(r1)
   stw  r23,MEMORY_BASE+40(r1)
   stw  r24,MEMORY_BASE+44(r1)
   stw  r25,MEMORY_BASE+48(r1)
   stw  r26,MEMORY_BASE+52(r1)
   stw  r27,MEMORY_BASE+56(r1)
   stw  r28,MEMORY_BASE+60(r1)
   stw  r29,MEMORY_BASE+64(r1)
   stw  r30,MEMORY_BASE+68(r1)
   stw  r31,MEMORY_BASE+72(r1)

   stw  r1,CHECKPOINT_STACK(r5)

   mr   r6,r3
   lwz  r3,CHECKPOINT_THREAD(r5)

   mtctr r6
   bctrl   
   
.globl GLOBAL(vmRun_returnAddress)
GLOBAL(vmRun_returnAddress):
   lwz  r13,MEMORY_BASE+0(r1)
   lwz  r14,MEMORY_BASE+4(r1)
   lwz  r15,MEMORY_BASE+8(r1)
   lwz  r16,MEMORY_BASE+12(r1)
   lwz  r17,MEMORY_BASE+16(r1)
   lwz  r18,MEMORY_BASE+20(r1)
   lwz  r19,MEMORY_BASE+24(r1)
   lwz  r20,MEMORY_BASE+28(r1)
   lwz  r21,MEMORY_BASE+32(r1)
   lwz  r22,MEMORY_BASE+36(r1)
   lwz  r23,MEMORY_BASE+40(r1)
   lwz  r24,MEMORY_BASE+44(r1)
   lwz  r25,MEMORY_BASE+48(r1)
   lwz  r26,MEMORY_BASE+52(r1)
   lwz  r27,MEMORY_BASE+56(r1)
   lwz  r28,MEMORY_BASE+60(r1)
   lwz  r29,MEMORY_BASE+64(r1)
   lwz  r30,MEMORY_BASE+68(r1)
   lwz  r31,MEMORY_BASE+72(r1)

   lwz	r1,0(r1)
   lwz  r0,RETURN_ADDRESS_OFFSET(r1)
   mtlr r0
   blr

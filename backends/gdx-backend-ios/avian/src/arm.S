/* arm.S: JNI gluecode for ARM/Linux
   Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

.text

#define LOCAL(x) .L##x

#ifdef __APPLE__
#  define GLOBAL(x) _##x
#else
#  define GLOBAL(x) x   
#endif

.globl GLOBAL(vmNativeCall)
GLOBAL(vmNativeCall):
  /*
    arguments:
    r0 -> r4       : function
    r1 -> r5       : stackTotal
    r2             : memoryTable
    r3             : memoryCount
    [sp, #0] -> r6 : gprTable
  */
  mov   ip, sp           // save stack frame
  stmfd sp!, {r4-r6, lr} // save clobbered non-volatile regs

  // mv args into non-volatile regs
  mov   r4, r0
  mov   r5, r1
  ldr   r6, [ip]

  // setup stack arguments if necessary
  sub   sp, sp, r5 // allocate stack
  mov   ip, sp
LOCAL(loop):
  tst   r3, r3
  ldrne r0, [r2], #4
  strne r0, [ip], #4
  subne r3, r3, #4
  bne   LOCAL(loop)

  // setup argument registers if necessary
  tst     r6, r6
  ldmneia r6, {r0-r3}

  blx   r4         // call function
  add   sp, sp, r5 // deallocate stack

  ldmfd sp!, {r4-r6, pc} // restore non-volatile regs and return

.globl GLOBAL(vmJump)
GLOBAL(vmJump):
  mov   lr, r0
  ldr   r0, [sp]
  ldr   r1, [sp, #4]
  mov   sp, r2
  mov   r8, r3
  bx    lr

#define CHECKPOINT_THREAD 4
#define CHECKPOINT_STACK 24

.globl GLOBAL(vmRun)
GLOBAL(vmRun):
  // r0: function
  // r1: arguments
  // r2: checkpoint
  stmfd sp!, {r4-r11, lr}
  // align stack
  sub   sp, sp, #12
   
  str   sp, [r2, #CHECKPOINT_STACK]

  mov   r12, r0
  ldr   r0, [r2, #CHECKPOINT_THREAD]

  blx   r12

.globl GLOBAL(vmRun_returnAddress)
GLOBAL(vmRun_returnAddress):
  add   sp, sp, #12
  ldmfd sp!, {r4-r11, lr}
  bx    lr

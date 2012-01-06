/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "types.h"

#define LOCAL(x) .L##x

#if defined __APPLE__ \
   || ((defined __MINGW32__ || defined __CYGWIN32__) && ! defined __x86_64__)
#  define GLOBAL(x) _##x
#else
#  define GLOBAL(x) x   
#endif

.text

#ifdef __x86_64__

#define THREAD_STACK 2224
#define THREAD_SCRATCH 2232

#ifdef AVIAN_USE_FRAME_POINTER
#  define ALIGNMENT_ADJUSTMENT 0
#else
#  define ALIGNMENT_ADJUSTMENT 8
#endif
   
#if defined __MINGW32__ || defined __CYGWIN32__

#define CALLEE_SAVED_REGISTER_FOOTPRINT 64 + ALIGNMENT_ADJUSTMENT

.globl GLOBAL(vmInvoke)
GLOBAL(vmInvoke):
   pushq  %rbp
   movq   %rsp,%rbp
   
   // %rcx: thread
   // %rdx: function
   // %r8 : arguments
   // %r9 : argumentsFootprint
   // 48(%rbp) : frameSize
   // 56(%rbp) : returnType (ignored)
   
   // allocate stack space for callee-saved registers
   subq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%rsp

   // remember this stack position, since we won't be able to rely on
	 // %rbp being restored when the call returns
   movq   %rsp,THREAD_SCRATCH(%rcx)
   
   // save callee-saved registers
   movq   %rbx,0(%rsp)
   movq   %r12,8(%rsp)
   movq   %r13,16(%rsp)
   movq   %r14,24(%rsp)
   movq   %r15,32(%rsp)
   movq   %rsi,40(%rsp)
   movq   %rdi,48(%rsp)
   
   // allocate stack space for arguments
   movl   48(%rbp),%eax
   subq   %rax,%rsp
   
   // we use rbx to hold the thread pointer, by convention
   mov    %rcx,%rbx

   // copy arguments into place
   movq   $0,%r11
   jmp    LOCAL(vmInvoke_argumentTest)

LOCAL(vmInvoke_argumentLoop):
   movq   (%r8,%r11,1),%rsi
   movq   %rsi,(%rsp,%r11,1)
   addq   $8,%r11

LOCAL(vmInvoke_argumentTest):
   cmpq   %r9,%r11
   jb     LOCAL(vmInvoke_argumentLoop)
   
   // call function
   call   *%rdx

.globl GLOBAL(vmInvoke_returnAddress)
GLOBAL(vmInvoke_returnAddress):
   // restore stack pointer
   movq   THREAD_SCRATCH(%rbx),%rsp

   // clear MyThread::stack to avoid confusing another thread calling
   // java.lang.Thread.getStackTrace on this one.  See
   // MyProcess::getStackTrace in compile.cpp for details on how we get
   // a reliable stack trace from a thread that might be interrupted at
   // any point in its execution.
   movq   $0,THREAD_STACK(%rbx)
   
.globl GLOBAL(vmInvoke_safeStack)
GLOBAL(vmInvoke_safeStack):

#ifdef AVIAN_CONTINUATIONS
#  include "continuations-x86.S"
#endif // AVIAN_CONTINUATIONS

   // restore callee-saved registers
   movq   0(%rsp),%rbx
   movq   8(%rsp),%r12
   movq   16(%rsp),%r13
   movq   24(%rsp),%r14
   movq   32(%rsp),%r15
   movq   40(%rsp),%rsi
   movq   48(%rsp),%rdi

   addq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%rsp
   
   // return
   popq   %rbp
   ret
      
.globl GLOBAL(vmJumpAndInvoke)
GLOBAL(vmJumpAndInvoke):
#ifdef AVIAN_CONTINUATIONS
   //     %rcx: thread
   //     %rdx: address
   //     %r8 : stack
   //     %r9 : argumentFootprint
   // 40(%rsp): arguments
   // 48(%rsp): frameSize
   
   // allocate new frame, adding room for callee-saved registers
   movl   48(%rsp),%eax
   subq   %rax,%r8
   subq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%r8
   
   movq   %rcx,%rbx
   
   // set return address
   leaq   GLOBAL(vmInvoke_returnAddress)(%rip),%r10
   movq   %r10,(%r8)
   
   // copy arguments into place
   movq   $0,%r11
   movl   40(%rsp),%eax
   jmp    LOCAL(vmJumpAndInvoke_argumentTest)

LOCAL(vmJumpAndInvoke_argumentLoop):
   movq   (%rax,%r11,1),%r10
   movq   %r10,8(%r8,%r11,1)
   addq   $8,%r11

LOCAL(vmJumpAndInvoke_argumentTest):
   cmpq   %9,%r11
   jb     LOCAL(vmJumpAndInvoke_argumentLoop)

   // the arguments have been copied, so we can set the real stack
   // pointer now
   movq   %r8,%rsp
   
   jmp    *%rdx
#else // not AVIAN_CONTINUATIONS
   // vmJumpAndInvoke should only be called when continuations are
   // enabled
   int3
#endif // not AVIAN_CONTINUATIONS

#else // not __MINGW32__ || __CYGWIN32__

#define CALLEE_SAVED_REGISTER_FOOTPRINT 48 + ALIGNMENT_ADJUSTMENT

.globl GLOBAL(vmInvoke)
GLOBAL(vmInvoke):
   pushq  %rbp
   movq   %rsp,%rbp
      
   // %rdi: thread
   // %rsi: function
   // %rdx: arguments
   // %rcx: argumentFootprint
   // %r8 : frameSize
   // %r9 : returnType (ignored)
   
   // allocate stack space for callee-saved registers
   subq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%rsp
   
   // remember this stack position, since we won't be able to rely on
	 // %rbp being restored when the call returns
   movq   %rsp,THREAD_SCRATCH(%rdi)
   
   // save callee-saved registers
   movq   %rbx,0(%rsp)
   movq   %r12,8(%rsp)
   movq   %r13,16(%rsp)
   movq   %r14,24(%rsp)
   movq   %r15,32(%rsp)
   
   // allocate stack space for arguments
   subq   %r8,%rsp
   
   // we use rbx to hold the thread pointer, by convention
   mov    %rdi,%rbx

   // copy arguments into place
   movq   $0,%r9
   jmp    LOCAL(vmInvoke_argumentTest)

LOCAL(vmInvoke_argumentLoop):
   movq   (%rdx,%r9,1),%r8
   movq   %r8,(%rsp,%r9,1)
   addq   $8,%r9

LOCAL(vmInvoke_argumentTest):
   cmpq   %rcx,%r9
   jb     LOCAL(vmInvoke_argumentLoop)
   
   // call function
   call   *%rsi
   
.globl GLOBAL(vmInvoke_returnAddress)
GLOBAL(vmInvoke_returnAddress):
   // restore stack pointer
   movq   THREAD_SCRATCH(%rbx),%rsp
   
   // clear MyThread::stack to avoid confusing another thread calling
   // java.lang.Thread.getStackTrace on this one.  See
   // MyProcess::getStackTrace in compile.cpp for details on how we get
   // a reliable stack trace from a thread that might be interrupted at
   // any point in its execution.
   movq   $0,THREAD_STACK(%rbx)
   
.globl GLOBAL(vmInvoke_safeStack)
GLOBAL(vmInvoke_safeStack):

#ifdef AVIAN_CONTINUATIONS
#  include "continuations-x86.S"
#endif // AVIAN_CONTINUATIONS

   // restore callee-saved registers
   movq   0(%rsp),%rbx
   movq   8(%rsp),%r12
   movq   16(%rsp),%r13
   movq   24(%rsp),%r14
   movq   32(%rsp),%r15

   addq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%rsp
   
   // return
   popq   %rbp
   ret
   
.globl GLOBAL(vmJumpAndInvoke)
GLOBAL(vmJumpAndInvoke):
#ifdef AVIAN_CONTINUATIONS
   //    %rdi: thread
   //    %rsi: address
   //    %rdx: stack
   //    %rcx: argumentFootprint
   //    %r8 : arguments
   //    %r9 : frameSize
         
   // allocate new frame, adding room for callee-saved registers
   subq   %r9,%rdx
   subq   $CALLEE_SAVED_REGISTER_FOOTPRINT,%rdx
   
   movq   %rdi,%rbx
   
   // set return address
   movq   GLOBAL(vmInvoke_returnAddress)@GOTPCREL(%rip),%r10
   movq   %r10,(%rdx)
   
   // copy arguments into place
   movq   $0,%r11
   jmp    LOCAL(vmJumpAndInvoke_argumentTest)

LOCAL(vmJumpAndInvoke_argumentLoop):
   movq   (%r8,%r11,1),%r10
   movq   %r10,8(%rdx,%r11,1)
   addq   $8,%r11

LOCAL(vmJumpAndInvoke_argumentTest):
   cmpq   %rcx,%r11
   jb     LOCAL(vmJumpAndInvoke_argumentLoop)

   // the arguments have been copied, so we can set the real stack
   // pointer now
   movq   %rdx,%rsp
   
   jmp    *%rsi
#else // not AVIAN_CONTINUATIONS
   // vmJumpAndInvoke should only be called when continuations are
   // enabled
   int3
#endif // not AVIAN_CONTINUATIONS

#endif // not __MINGW32__ || __CYGWIN32__
   
#elif defined __i386__

#define THREAD_STACK 2148
#define THREAD_SCRATCH 2152

#ifdef AVIAN_USE_FRAME_POINTER
#  define ALIGNMENT_ADJUSTMENT 0
#else
#  define ALIGNMENT_ADJUSTMENT 12
#endif

#define CALLEE_SAVED_REGISTER_FOOTPRINT 16 + ALIGNMENT_ADJUSTMENT

.globl GLOBAL(vmInvoke)
GLOBAL(vmInvoke):
   pushl  %ebp
   movl   %esp,%ebp

   //  8(%ebp): thread
   // 12(%ebp): function
   // 16(%ebp): arguments
   // 20(%ebp): argumentFootprint
   // 24(%ebp): frameSize
   // 28(%ebp): returnType
   
   // allocate stack space for callee-saved registers
   subl   $CALLEE_SAVED_REGISTER_FOOTPRINT,%esp
   
   // remember this stack position, since we won't be able to rely on
	 // %rbp being restored when the call returns
   movl   8(%ebp),%eax
   movl   %esp,THREAD_SCRATCH(%eax)

   movl   %ebx,0(%esp)
   movl   %esi,4(%esp)
   movl   %edi,8(%esp)

   // allocate stack space for arguments
	 subl   24(%ebp),%esp
   
   // we use ebx to hold the thread pointer, by convention
   mov    %eax,%ebx
   
   // copy arguments into place
   movl   $0,%ecx
   movl   16(%ebp),%edx
   jmp    LOCAL(vmInvoke_argumentTest)

LOCAL(vmInvoke_argumentLoop):
   movl   (%edx,%ecx,1),%eax
   movl   %eax,(%esp,%ecx,1)
   addl   $4,%ecx

LOCAL(vmInvoke_argumentTest):
   cmpl   20(%ebp),%ecx
   jb     LOCAL(vmInvoke_argumentLoop)

   // call function
   call   *12(%ebp)

.globl GLOBAL(vmInvoke_returnAddress)
GLOBAL(vmInvoke_returnAddress):
   // restore stack pointer
   movl   THREAD_SCRATCH(%ebx),%esp
   
   // clear MyThread::stack to avoid confusing another thread calling
   // java.lang.Thread.getStackTrace on this one.  See
   // MyProcess::getStackTrace in compile.cpp for details on how we get
   // a reliable stack trace from a thread that might be interrupted at
   // any point in its execution.
   movl   $0,THREAD_STACK(%ebx)
   
.globl GLOBAL(vmInvoke_safeStack)
GLOBAL(vmInvoke_safeStack):

#ifdef AVIAN_CONTINUATIONS
#  include "continuations-x86.S"
#endif // AVIAN_CONTINUATIONS
   
   // restore callee-saved registers
   movl   0(%esp),%ebx
   movl   4(%esp),%esi
   movl   8(%esp),%edi
   
   addl   $CALLEE_SAVED_REGISTER_FOOTPRINT,%esp

   // handle return value based on expected type
   movl   28(%esp),%ecx

   popl   %ebp
   ret

LOCAL(getPC):
   movl   (%esp),%esi
   ret
   
.globl GLOBAL(vmJumpAndInvoke)
GLOBAL(vmJumpAndInvoke):
#ifdef AVIAN_CONTINUATIONS
   //  4(%esp): thread
   //  8(%esp): address
   // 12(%esp): stack
   // 16(%esp): argumentFootprint
   // 20(%esp): arguments
   // 24(%esp): frameSize

   movl   12(%esp),%ecx
   
   // allocate new frame, adding room for callee-saved registers
   subl   24(%esp),%ecx
   subl   $CALLEE_SAVED_REGISTER_FOOTPRINT,%ecx
   
   movl   4(%esp),%ebx
   
   // set return address
#if defined __MINGW32__ || defined __CYGWIN32__
   movl   $GLOBAL(vmInvoke_returnAddress),%esi
#else
   call   LOCAL(getPC)
#  if defined __APPLE__
LOCAL(vmJumpAndInvoke_offset):
   leal   GLOBAL(vmInvoke_returnAddress)-LOCAL(vmJumpAndInvoke_offset)(%esi),%esi
#  else
   addl   $_GLOBAL_OFFSET_TABLE_,%esi
   movl   GLOBAL(vmInvoke_returnAddress)@GOT(%esi),%esi
#  endif
#endif
   movl   %esi,(%ecx)
   
   // copy arguments into place
   movl   $0,%esi
   movl   16(%esp),%edx
   movl   20(%esp),%eax
   jmp    LOCAL(vmJumpAndInvoke_argumentTest)

LOCAL(vmJumpAndInvoke_argumentLoop):
   movl   (%eax,%esi,1),%edi
   movl   %edi,4(%ecx,%esi,1)
   addl   $4,%esi

LOCAL(vmJumpAndInvoke_argumentTest):
   cmpl   %edx,%esi
   jb     LOCAL(vmJumpAndInvoke_argumentLoop)

   movl   8(%esp),%esi
   
   // the arguments have been copied, so we can set the real stack
   // pointer now
   movl   %ecx,%esp

   jmp    *%esi
#else // not AVIAN_CONTINUATIONS
   // vmJumpAndInvoke should only be called when continuations are
   // enabled
   int3
#endif // AVIAN_CONTINUATIONS
   
#else
#error unsupported architecture
#endif //def __x86_64__

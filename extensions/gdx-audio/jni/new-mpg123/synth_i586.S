/*
 decode_i586: asm synth

 copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
 see COPYING and AUTHORS files in distribution or http://mpg123.org
 initially written by Stefan Bieschewski
 
 synth_1to1 works the same way as the c version of this
 file.  only two types of changes have been made:
 - reordered floating point instructions to
   prevent pipline stalls
 - made WRITE_SAMPLE use integer instead of
   (slower) floating point
 all kinds of x86 processors should benefit from these
 modifications.

 useful sources of information on optimizing x86 code include:

     Intel Architecture Optimization Manual
     http://www.intel.com/design/pentium/manuals/242816.htm

     Cyrix 6x86 Instruction Set Summary
     ftp://ftp.cyrix.com/6x86/6x-dbch6.pdf

     AMD-K5 Processor Software Development
     http://www.amd.com/products/cpg/techdocs/appnotes/20007e.pdf

 Stefan Bieschewski <stb@acm.org>

 $Id: decode_i586.s 1 2004-09-18 13:30:08Z thomas $
*/

#include "mangle.h"

.data
#ifndef __APPLE__
.section .rodata
#endif
	ALIGN8
.LC0:
	.long 0x0,0x40dfffc0
	ALIGN8
.LC1:
	.long 0x0,0xc0e00000
	ALIGN8
.text
/* int synth_1to1_i586_asm(real *bandPtr, int channel, unsigned char *out, unsigned char *buffs, int *bo, real *decwin); */
.globl ASM_NAME(synth_1to1_i586_asm)
ASM_NAME(synth_1to1_i586_asm):
	subl $12,%esp
	pushl %ebp
	pushl %edi
	pushl %esi
	pushl %ebx
/* stack: 0=ebx, 4=esi, 8=edi, 12=ebp, 16,20,24=local, 28=back, 32=bandPtr, 36=channel, 40=out, 44=buffs, 48=bo, 52=decwin */
	movl 32(%esp),%eax /* *bandPtr */
	movl 40(%esp),%esi /* *out */
	movl 48(%esp),%edi /* *bo */
	movl (%edi),%ebp   /* store bo value in ebp */
	xorl %edi,%edi
	cmpl %edi,36(%esp)
	jne .L48           /* if(!channel) */
	decl %ebp          /* bo-- */
	andl $15,%ebp      /* bo &= 0xf */
	movl 48(%esp),	%edi /* *bo */
	movl %ebp,(%edi)   /* write back bo */
	xorl %edi,%edi     /* restore %edi to 0; it's used later */
	movl 44(%esp),%ecx /* use buffs */
	jmp .L49
.L48: /* if(channel) use buffs+2176 */
	addl $2,%esi
	movl 44(%esp),%ecx /* *buffs */
	addl $2176,%ecx
.L49:
	testl $1,%ebp
	je .L50
	movl %ecx,%ebx
	movl %ebp,16(%esp)
	pushl %eax
	movl 20(%esp),%edx
	leal (%ebx,%edx,4),%eax
	pushl %eax
	movl 24(%esp),%eax
	incl %eax
	andl $15,%eax
	leal 1088(,%eax,4),%eax
	addl %ebx,%eax
	jmp .L74
.L50:
	leal 1088(%ecx),%ebx
	leal 1(%ebp),%edx
	movl %edx,16(%esp)
	pushl %eax
	leal 1092(%ecx,%ebp,4),%eax
	pushl %eax
	leal (%ecx,%ebp,4),%eax
.L74:
	pushl %eax
	call ASM_NAME(dct64_i386)
	addl $12,%esp
/* stack now back on track */
	movl 16(%esp),%edx
	leal 0(,%edx,4),%edx
	movl 52(%esp),%eax /* decwin */
	addl $64,%eax
	movl %eax,%ecx
	subl %edx,%ecx
	movl $16,%ebp
.L55:
	flds (%ecx)
	fmuls (%ebx)
	flds 4(%ecx)
	fmuls 4(%ebx)
	fxch %st(1)
	flds 8(%ecx)
	fmuls 8(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 12(%ecx)
	fmuls 12(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 16(%ecx)
	fmuls 16(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 20(%ecx)
	fmuls 20(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 24(%ecx)
	fmuls 24(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 28(%ecx)
	fmuls 28(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 32(%ecx)
	fmuls 32(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 36(%ecx)
	fmuls 36(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 40(%ecx)
	fmuls 40(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 44(%ecx)
	fmuls 44(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 48(%ecx)
	fmuls 48(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 52(%ecx)
	fmuls 52(%ebx)
	fxch %st(2)         
	faddp %st,%st(1)
	flds 56(%ecx)
	fmuls 56(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds 60(%ecx)
	fmuls 60(%ebx)
	fxch %st(2)
	subl $4,%esp
	faddp %st,%st(1)
	fxch %st(1)
	fsubrp %st,%st(1)
	fistpl (%esp)
	popl %eax
	cmpl $32767,%eax
	jg 1f
	cmpl $-32768,%eax
	jl 2f
	movw %ax,(%esi)
	jmp 4f
1:	movw $32767,(%esi)
	jmp 3f
2:	movw $-32768,(%esi)
3:	incl %edi
4:
.L54:
	addl $64,%ebx
	subl $-128,%ecx
	addl $4,%esi
	decl %ebp
	jnz .L55
	flds (%ecx)
	fmuls (%ebx)
	flds 8(%ecx)
	fmuls 8(%ebx)
	flds 16(%ecx)
	fmuls 16(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 24(%ecx)
	fmuls 24(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 32(%ecx)
	fmuls 32(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 40(%ecx)
	fmuls 40(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 48(%ecx)
	fmuls 48(%ebx)
	fxch %st(2)
	faddp %st,%st(1)
	flds 56(%ecx)
	fmuls 56(%ebx)
	fxch %st(2)
	subl $4,%esp
	faddp %st,%st(1)
	fxch %st(1)
	faddp %st,%st(1)
	fistpl (%esp)
	popl %eax
	cmpl $32767,%eax
	jg 1f
	cmpl $-32768,%eax
	jl 2f
	movw %ax,(%esi)
	jmp 4f
1:	movw $32767,(%esi)
	jmp 3f
2:	movw $-32768,(%esi)
3:	incl %edi
4:
.L62:
	addl $-64,%ebx
	addl $4,%esi
	movl 16(%esp),%edx
	leal -128(%ecx,%edx,8),%ecx
	movl $15,%ebp
.L68:
	flds -4(%ecx)
	fchs
	fmuls (%ebx)
	flds -8(%ecx)
	fmuls 4(%ebx)
	fxch %st(1)
	flds -12(%ecx)
	fmuls 8(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -16(%ecx)
	fmuls 12(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -20(%ecx)
	fmuls 16(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -24(%ecx)
	fmuls 20(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -28(%ecx)
	fmuls 24(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -32(%ecx)
	fmuls 28(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -36(%ecx)
	fmuls 32(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -40(%ecx)
	fmuls 36(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -44(%ecx)
	fmuls 40(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -48(%ecx)
	fmuls 44(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -52(%ecx)
	fmuls 48(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -56(%ecx)
	fmuls 52(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds -60(%ecx)
	fmuls 56(%ebx)
	fxch %st(2)
	fsubrp %st,%st(1)
	flds (%ecx)
	fmuls 60(%ebx)
	fxch %st(2)
	subl $4,%esp
	fsubrp %st,%st(1)
	fxch %st(1)
	fsubrp %st,%st(1)
	fistpl (%esp)
	popl %eax
	cmpl $32767,%eax
	jg 1f
	cmpl $-32768,%eax
	jl 2f
	movw %ax,(%esi)
	jmp 4f
1:	movw $32767,(%esi)
	jmp 3f
2:	movw $-32768,(%esi)
3:	incl %edi
4:
.L67:
	addl $-64,%ebx
	addl $-128,%ecx
	addl $4,%esi
	decl %ebp
	jnz .L68
	movl %edi,%eax
	popl %ebx
	popl %esi
	popl %edi
	popl %ebp
	addl $12,%esp
	ret

NONEXEC_STACK

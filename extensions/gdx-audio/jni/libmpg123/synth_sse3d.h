/*
	decode_sse3d: Synth for SSE and extended 3DNow (yeah, the name is a relic)

	copyright 2006-2007 by Zuxy Meng/the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by the mysterious higway for MMX (apparently)
	then developed into SSE opt by Zuxy Meng, also building on Romain Dolbeau's AltiVec
	Both have agreed to distribution under LGPL 2.1 .

	Transformed back into standalone asm, with help of
	gcc -S -DHAVE_CONFIG_H -I.  -march=pentium -O3 -Wall -pedantic -fno-strict-aliasing -DREAL_IS_FLOAT -c -o decode_mmxsse.{S,c}

	The difference between SSE and 3DNowExt is the dct64 function and the synth function name.
	This template here uses the SYNTH_NAME and MPL_DCT64 macros for this - see decode_sse.S and decode_3dnowext.S...
	That's not memory efficient since there's doubled code, but it's easier than giving another function pointer.
	Maybe I'll change it in future, but now I need something that works.

	Original comment from MPlayer source follows:
*/

/*
 * this code comes under GPL
 * This code was taken from http://www.mpg123.org
 * See ChangeLog of mpg123-0.59s-pre.1 for detail
 * Applied to mplayer by Nick Kurshev <nickols_k@mail.ru>
 *
 * Local ChangeLog:
 * - Partial loops unrolling and removing MOVW insn from loops
*/

#include "mangle.h"

	.data
	ALIGN8
one_null:
	.long	-65536
	.long	-65536
	ALIGN8
null_one:
	.long	65535
	.long	65535

	.text
	ALIGN16
	/* void SYNTH_NAME(real *bandPtr, int channel, short *samples, short *buffs, int *bo, float *decwins) */
.globl SYNTH_NAME
SYNTH_NAME:
	pushl	%ebp
/* stack:0=ebp 4=back 8=bandptr 12=channel 16=samples 20=buffs 24=bo 28=decwins */
	movl	%esp, %ebp
/* Now the old stack addresses are preserved via %epb. */
	subl  $4,%esp /* What has been called temp before. */
	pushl	%edi
	pushl	%esi
	pushl	%ebx
#define TEMP 12(%esp)
/* APP */
	movl 12(%ebp),%ecx
	movl 16(%ebp),%edi
	movl $15,%ebx
	movl 24(%ebp),%edx
	leal (%edi,%ecx,2),%edi
	decl %ecx
	movl 20(%ebp),%esi
	movl (%edx),%eax
	jecxz .L01
	decl %eax
	andl %ebx,%eax
	leal 1088(%esi),%esi
	movl %eax,(%edx)
	.L01:
	leal (%esi,%eax,2),%edx
	movl %eax,TEMP
	incl %eax
	andl %ebx,%eax
	leal 544(%esi,%eax,2),%ecx
	incl %ebx
	testl $1, %eax
	jnz .L02
	xchgl %edx,%ecx
	incl TEMP
	leal 544(%esi),%esi
	.L02:
	pushl 8(%ebp)
	pushl %edx
	pushl %ecx
	call MPL_DCT64
	addl $12, %esp
	leal 1(%ebx), %ecx
	subl TEMP,%ebx
	pushl %ecx
	/* leal ASM_NAME(decwins)(%ebx,%ebx,1), %edx */
	movl 28(%ebp),%ecx
	leal (%ecx,%ebx,2), %edx
	movl (%esp),%ecx /* restore, but leave value on stack */
	shrl $1, %ecx
	ALIGN16
	.L03:
	movq  (%edx),%mm0
	movq  64(%edx),%mm4
	pmaddwd (%esi),%mm0
	pmaddwd 32(%esi),%mm4
	movq  8(%edx),%mm1
	movq  72(%edx),%mm5
	pmaddwd 8(%esi),%mm1
	pmaddwd 40(%esi),%mm5
	movq  16(%edx),%mm2
	movq  80(%edx),%mm6
	pmaddwd 16(%esi),%mm2
	pmaddwd 48(%esi),%mm6
	movq  24(%edx),%mm3
	movq  88(%edx),%mm7
	pmaddwd 24(%esi),%mm3
	pmaddwd 56(%esi),%mm7
	paddd %mm1,%mm0
	paddd %mm5,%mm4
	paddd %mm2,%mm0
	paddd %mm6,%mm4
	paddd %mm3,%mm0
	paddd %mm7,%mm4
	movq  %mm0,%mm1
	movq  %mm4,%mm5
	psrlq $32,%mm1
	psrlq $32,%mm5
	paddd %mm1,%mm0
	paddd %mm5,%mm4
	psrad $13,%mm0
	psrad $13,%mm4
	packssdw %mm0,%mm0
	packssdw %mm4,%mm4
	movq	(%edi), %mm1
	punpckldq %mm4, %mm0
	pand   one_null, %mm1
	pand   null_one, %mm0
	por    %mm0, %mm1
	movq   %mm1,(%edi)
	leal 64(%esi),%esi
	leal 128(%edx),%edx
	leal 8(%edi),%edi
	decl %ecx
	jnz  .L03
	popl %ecx
	andl $1, %ecx
	jecxz .next_loop
	movq  (%edx),%mm0
	pmaddwd (%esi),%mm0
	movq  8(%edx),%mm1
	pmaddwd 8(%esi),%mm1
	movq  16(%edx),%mm2
	pmaddwd 16(%esi),%mm2
	movq  24(%edx),%mm3
	pmaddwd 24(%esi),%mm3
	paddd %mm1,%mm0
	paddd %mm2,%mm0
	paddd %mm3,%mm0
	movq  %mm0,%mm1
	psrlq $32,%mm1
	paddd %mm1,%mm0
	psrad $13,%mm0
	packssdw %mm0,%mm0
	movd %mm0,%eax
	movw %ax, (%edi)
	leal 32(%esi),%esi
	leal 64(%edx),%edx
	leal 4(%edi),%edi
	.next_loop:
	subl $64,%esi
	movl $7,%ecx
	ALIGN16
	.L04:
	movq  (%edx),%mm0
	movq  64(%edx),%mm4
	pmaddwd (%esi),%mm0
	pmaddwd -32(%esi),%mm4
	movq  8(%edx),%mm1
	movq  72(%edx),%mm5
	pmaddwd 8(%esi),%mm1
	pmaddwd -24(%esi),%mm5
	movq  16(%edx),%mm2
	movq  80(%edx),%mm6
	pmaddwd 16(%esi),%mm2
	pmaddwd -16(%esi),%mm6
	movq  24(%edx),%mm3
	movq  88(%edx),%mm7
	pmaddwd 24(%esi),%mm3
	pmaddwd -8(%esi),%mm7
	paddd %mm1,%mm0
	paddd %mm5,%mm4
	paddd %mm2,%mm0
	paddd %mm6,%mm4
	paddd %mm3,%mm0
	paddd %mm7,%mm4
	movq  %mm0,%mm1
	movq  %mm4,%mm5
	psrlq $32,%mm1
	psrlq $32,%mm5
	paddd %mm0,%mm1
	paddd %mm4,%mm5
	psrad $13,%mm1
	psrad $13,%mm5
	packssdw %mm1,%mm1
	packssdw %mm5,%mm5
	psubd %mm0,%mm0
	psubd %mm4,%mm4
	psubsw %mm1,%mm0
	psubsw %mm5,%mm4
	movq	(%edi), %mm1
	punpckldq %mm4, %mm0
	pand   one_null, %mm1
	pand   null_one, %mm0
	por    %mm0, %mm1
	movq   %mm1,(%edi)
	subl $64,%esi
	addl $128,%edx
	leal 8(%edi),%edi
	decl %ecx
	jnz  .L04
	movq  (%edx),%mm0
	pmaddwd (%esi),%mm0
	movq  8(%edx),%mm1
	pmaddwd 8(%esi),%mm1
	movq  16(%edx),%mm2
	pmaddwd 16(%esi),%mm2
	movq  24(%edx),%mm3
	pmaddwd 24(%esi),%mm3
	paddd %mm1,%mm0
	paddd %mm2,%mm0
	paddd %mm3,%mm0
	movq  %mm0,%mm1
	psrlq $32,%mm1
	paddd %mm0,%mm1
	psrad $13,%mm1
	packssdw %mm1,%mm1
	psubd %mm0,%mm0
	psubsw %mm1,%mm0
	movd %mm0,%eax
	movw %ax,(%edi)
	emms

/* NO_APP */
	popl	%ebx
	popl	%esi
	popl	%edi
	addl $4,%esp
	popl	%ebp
	ret

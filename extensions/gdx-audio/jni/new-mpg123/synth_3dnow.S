/*
	decode_3dnow.s - 3DNow! optimized synth_1to1()

	copyright ?-2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Syuuhei Kashiyama

	This code based 'decode_3dnow.s' by Syuuhei Kashiyama
	<squash@mb.kcom.ne.jp>,only two types of changes have been made:

	- remove PREFETCH instruction for speedup
	- change function name for support 3DNow! automatic detect
	- femms moved to before 'call dct64_3dnow'

	You can find Kashiyama's original 3dnow! support patch
	(for mpg123-0.59o) at
	http://user.ecc.u-tokyo.ac.jp/~g810370/linux-simd/ (Japanese).

	by KIMURA Takuhiro <kim@hannah.ipc.miyakyo-u.ac.jp> - until 31.Mar.1999
                  	<kim@comtec.co.jp>               - after  1.Apr.1999



	Replacement of synth_1to1() with AMD's 3DNow! SIMD operations support

	Syuuhei Kashiyama <squash@mb.kcom.ne.jp>

	The author of this program disclaim whole expressed or implied
	warranties with regard to this program, and in no event shall the
	author of this program liable to whatever resulted from the use of
	this program. Use it at your own risk.
*/

#include "mangle.h"

#ifdef ACCURATE_ROUNDING
#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN8
max_s16:
	.long   1191181824 /* 32767.0 */
	.long   1191181824
min_s16:
	.long   -956301312 /* -32768.0 */
	.long   -956301312
ftoi_magic:
	.long	1262485504 /* 2^23 + 2^22 */
	.long	1262485504
#endif
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_3dnow_asm)
/* int synth_1to1_3dnow_asm(real *bandPtr, int channel, unsigned char *out, unsigned char *buffs, int *bo, real *decwin); */
ASM_NAME(synth_1to1_3dnow_asm):
	subl $24,%esp
	pushl %ebp
	pushl %edi
	xorl %ebp,%ebp
	pushl %esi
	pushl %ebx
/* stack old: 0=ebx 4=esi 8=edi 12=ebp 16,20,24,28,32,36=local 40=back 44=bandptr 48=channel 52=out 56=pnt */
/* stack new: 0=ebx 4=esi 8=edi 12=ebp 16,20,24,28,32,36=local 40=back 44=bandptr 48=channel 52=out 56=buffs 60=bo 64=decwin */
#define OUT     52(%esp)
#define CHANNEL 48(%esp)
#define BANDPTR 44(%esp)
#define BUFFS   56(%esp)
#define BO      60(%esp)
#define DECWIN  64(%esp)
#define LOCAL0  16(%esp)
#define LOCAL1  20(%esp)
#define LOCAL5  36(%esp)
	movl OUT,%esi
	movl %esi,LOCAL0 /* save buffer start (samples pointer) to another local var */
	movl CHANNEL,%ebx
	movl BO,%esi     /* bo address */
	movl (%esi),%edx /* bo value */

	femms
	testl %ebx,%ebx
	jne .L26
/* if(!channel) */
	decl %edx   /* --bo */
	andl $15,%edx
	movl %edx,(%esi) /* save bo */
	movl BUFFS,%ecx
	jmp .L27
.L26: /* if(channel) */
	addl $2,LOCAL0   /* samples++ */
	movl BUFFS,%ecx
	addl $2176,%ecx
.L27:
/* edx (and it's lower end) still holds bo value */
	testb $1,%dl  /* bo & 0x1 */
	je .L28
	movl %edx,LOCAL5
	movl %ecx,%ebx
	movl BANDPTR,%esi
	movl %edx,%edi
	pushl %esi
	sall $2,%edi
	movl %ebx,%eax
	movl %edi,24(%esp) /* LOCAL1, actually */
	addl %edi,%eax
	pushl %eax
	movl %edx,%eax
	incl %eax
	andl $15,%eax
	leal 1088(,%eax,4),%eax
	addl %ebx,%eax
	pushl %eax
	call ASM_NAME(dct64_3dnow)
	addl $12,%esp
	jmp .L29
.L28:
	leal 1(%edx),%esi
	movl BANDPTR,%edi
	movl %esi,LOCAL5
	leal 1092(%ecx,%edx,4),%eax
	pushl %edi
	leal 1088(%ecx),%ebx
	pushl %eax
	sall $2,%esi
	leal (%ecx,%edx,4),%eax
	pushl %eax
	call ASM_NAME(dct64_3dnow)
	addl $12,%esp
	movl %esi,LOCAL1
.L29:
	movl DECWIN,%edx
	addl $64,%edx
	movl $16,%ecx
	subl LOCAL1,%edx
	movl LOCAL0,%edi

	pcmpeqb %mm7,%mm7
	pslld $31,%mm7
	movq (%edx),%mm0
	movq (%ebx),%mm1
	ALIGN32
.L33:
	movq 8(%edx),%mm3
	pfmul %mm1,%mm0
	movq 8(%ebx),%mm4
	movq 16(%edx),%mm5
	pfmul %mm4,%mm3
	movq 16(%ebx),%mm6
	pfadd %mm3,%mm0
	movq 24(%edx),%mm1
	pfmul %mm6,%mm5
	movq 24(%ebx),%mm2
	pfadd %mm5,%mm0
	movq 32(%edx),%mm3
	pfmul %mm2,%mm1
	movq 32(%ebx),%mm4
	pfadd %mm1,%mm0
	movq 40(%edx),%mm5
	pfmul %mm4,%mm3
	movq 40(%ebx),%mm6
	pfadd %mm3,%mm0
	movq 48(%edx),%mm1
	pfmul %mm6,%mm5
	movq 48(%ebx),%mm2
	pfadd %mm0,%mm5
	movq 56(%edx),%mm3
	pfmul %mm1,%mm2
	movq 56(%ebx),%mm4
	pfadd %mm5,%mm2
	addl $64,%ebx
	subl $-128,%edx
	movq (%edx),%mm0
	pfmul %mm4,%mm3
	movq (%ebx),%mm1
	pfadd %mm3,%mm2
	movq %mm2,%mm3
	psrlq $32,%mm3
	pfsub %mm3,%mm2
	incl %ebp
#ifdef ACCURATE_ROUNDING
	pfmin (max_s16),%mm2
	pfmax (min_s16),%mm2
	pfadd (ftoi_magic),%mm2
#else
	pf2id %mm2,%mm2
	packssdw %mm2,%mm2
#endif
	movd %mm2,%eax
	movw %ax,0(%edi)
	addl $4,%edi
	decl %ecx
	jnz .L33

	movd (%ebx),%mm0
	movd (%edx),%mm1
	punpckldq 8(%ebx),%mm0
	punpckldq 8(%edx),%mm1
	movd 16(%ebx),%mm3
	movd 16(%edx),%mm4
	pfmul %mm1,%mm0
	punpckldq 24(%ebx),%mm3
	punpckldq 24(%edx),%mm4
	movd 32(%ebx),%mm5
	movd 32(%edx),%mm6
	pfmul %mm4,%mm3
	punpckldq 40(%ebx),%mm5
	punpckldq 40(%edx),%mm6
	pfadd %mm3,%mm0
	movd 48(%ebx),%mm1
	movd 48(%edx),%mm2
	pfmul %mm6,%mm5
	punpckldq 56(%ebx),%mm1
	punpckldq 56(%edx),%mm2
	pfadd %mm5,%mm0
	pfmul %mm2,%mm1
	pfadd %mm1,%mm0
	pfacc %mm1,%mm0
#ifdef ACCURATE_ROUNDING
	pfmin (max_s16),%mm0
	pfmax (min_s16),%mm0
	pfadd (ftoi_magic),%mm0
#else
	pf2id %mm0,%mm0
	packssdw %mm0,%mm0
#endif
	movd %mm0,%eax
	movw %ax,0(%edi)
	incl %ebp
	movl LOCAL5,%esi
	addl $-64,%ebx
	movl $15,%ebp
	addl $4,%edi
	leal -128(%edx,%esi,8),%edx

	movl $15,%ecx
	movd (%ebx),%mm0
	movd -4(%edx),%mm1
	punpckldq 4(%ebx),%mm0
	punpckldq -8(%edx),%mm1
	ALIGN32
.L46:
	movd 8(%ebx),%mm3
	movd -12(%edx),%mm4
	pfmul %mm1,%mm0
	punpckldq 12(%ebx),%mm3
	punpckldq -16(%edx),%mm4
	movd 16(%ebx),%mm5
	movd -20(%edx),%mm6
	pfmul %mm4,%mm3
	punpckldq 20(%ebx),%mm5
	punpckldq -24(%edx),%mm6
	pfadd %mm3,%mm0
	movd 24(%ebx),%mm1
	movd -28(%edx),%mm2
	pfmul %mm6,%mm5
	punpckldq 28(%ebx),%mm1
	punpckldq -32(%edx),%mm2
	pfadd %mm5,%mm0
	movd 32(%ebx),%mm3
	movd -36(%edx),%mm4
	pfmul %mm2,%mm1
	punpckldq 36(%ebx),%mm3
	punpckldq -40(%edx),%mm4
	pfadd %mm1,%mm0
	movd 40(%ebx),%mm5
	movd -44(%edx),%mm6
	pfmul %mm4,%mm3
	punpckldq 44(%ebx),%mm5
	punpckldq -48(%edx),%mm6
	pfadd %mm3,%mm0
	movd 48(%ebx),%mm1
	movd -52(%edx),%mm2
	pfmul %mm6,%mm5
	punpckldq 52(%ebx),%mm1
	punpckldq -56(%edx),%mm2
	pfadd %mm0,%mm5
	movd 56(%ebx),%mm3
	movd -60(%edx),%mm4
	pfmul %mm2,%mm1
	punpckldq 60(%ebx),%mm3
	punpckldq (%edx),%mm4
	pfadd %mm1,%mm5
	addl $-128,%edx
	addl $-64,%ebx
	movd (%ebx),%mm0
	movd -4(%edx),%mm1
	pfmul %mm4,%mm3
	punpckldq 4(%ebx),%mm0
	punpckldq -8(%edx),%mm1
	pfadd %mm5,%mm3
	pfacc %mm3,%mm3
	incl %ebp
	pxor %mm7,%mm3
#ifdef ACCURATE_ROUNDING
	pfmin (max_s16),%mm3
	pfmax (min_s16),%mm3
	pfadd (ftoi_magic),%mm3
#else
	pf2id %mm3,%mm3
	packssdw %mm3,%mm3
#endif
	movd %mm3,%eax
	movw %ax,(%edi)
	addl $4,%edi
	decl %ecx
	jnz .L46

	femms
	movl %ebp,%eax
	popl %ebx
	popl %esi
	popl %edi
	popl %ebp
	addl $24,%esp
	ret

NONEXEC_STACK

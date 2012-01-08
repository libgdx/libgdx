/*
	dct64_sse: MMX/SSE optimized dct64

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define ARG(n) (8+n*4)(%ebp)
#define TEMP(n) (4+n*16)(%esp)
#define TEMP_BYTE(n) (4+n)(%esp)

/*
	void dct64_sse(short *out0, short *out1, real *samples);
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN16
pnpn:
	.long	0
	.long	-2147483648
	.long	0
	.long	-2147483648
	ALIGN16
mask:
	.long	-1
	.long	-1
	.long	-1
	.long	0
	
	.text
	ALIGN16
.globl ASM_NAME(dct64_sse)
ASM_NAME(dct64_sse):
	pushl		%ebp
	movl		%esp, %ebp
	
	andl		$-16, %esp /* align the stack at 16 bytes */
	subl		$128, %esp /* reserve space for temporal store */
	pushl		%ebx
	
	movl		ARG(0), %ecx
	movl		ARG(1), %ebx
	movl		ARG(2), %eax
	
	MOVUAPS 	(%eax), %xmm7
	MOVUAPS 	16(%eax), %xmm6
	MOVUAPS 	112(%eax), %xmm0
	MOVUAPS 	96(%eax), %xmm1
	shufps 		$0x1b, %xmm0, %xmm0
	shufps 		$0x1b, %xmm1, %xmm1
	movaps 		%xmm7, %xmm4
	movaps		%xmm6, %xmm5
	addps 		%xmm0, %xmm4
	addps 		%xmm1, %xmm5
	subps 		%xmm0, %xmm7
	subps 		%xmm1, %xmm6
	movaps		%xmm4, TEMP(0)
	movaps		%xmm5, TEMP(1)
	
	MOVUAPS 	32(%eax), %xmm2
	MOVUAPS 	48(%eax), %xmm3
	MOVUAPS 	80(%eax), %xmm0
	MOVUAPS 	64(%eax), %xmm1
	shufps 		$0x1b, %xmm0, %xmm0
	shufps 		$0x1b, %xmm1, %xmm1
	movaps 		%xmm2, %xmm5
	movaps		%xmm3, %xmm4
	addps 		%xmm0, %xmm2
	addps 		%xmm1, %xmm3
	subps 		%xmm0, %xmm5
	subps 		%xmm1, %xmm4
	
	mulps		ASM_NAME(costab_mmxsse), %xmm7
	mulps		ASM_NAME(costab_mmxsse)+16, %xmm6
	mulps		ASM_NAME(costab_mmxsse)+32, %xmm5
	mulps		ASM_NAME(costab_mmxsse)+48, %xmm4
	
	shufps		$0x1b, %xmm2, %xmm2
	shufps		$0x1b, %xmm3, %xmm3
	shufps		$0x1b, %xmm4, %xmm4
	shufps		$0x1b, %xmm5, %xmm5
	movaps		TEMP(0), %xmm0
	movaps		TEMP(1), %xmm1
	subps		%xmm3, %xmm0
	subps		%xmm2, %xmm1
	addps		TEMP(0), %xmm3
	addps		TEMP(1), %xmm2
	movaps		%xmm3, TEMP(0)
	movaps		%xmm2, TEMP(1)
	movaps		%xmm6, %xmm2
	movaps		%xmm7, %xmm3
	subps		%xmm5, %xmm6
	subps		%xmm4, %xmm7
	addps		%xmm3, %xmm4
	addps		%xmm2, %xmm5
	mulps		ASM_NAME(costab_mmxsse)+64, %xmm0
	mulps		ASM_NAME(costab_mmxsse)+80, %xmm1
	mulps		ASM_NAME(costab_mmxsse)+80, %xmm6
	mulps		ASM_NAME(costab_mmxsse)+64, %xmm7
	
	movaps		TEMP(0), %xmm2
	movaps		TEMP(1), %xmm3
	shufps		$0x1b, %xmm3, %xmm3
	shufps		$0x1b, %xmm5, %xmm5
	shufps		$0x1b, %xmm1, %xmm1
	shufps		$0x1b, %xmm6, %xmm6
	movaps		%xmm0, TEMP(1)
	subps		%xmm3, %xmm2
	subps		%xmm1, %xmm0
	addps		TEMP(0), %xmm3
	addps		TEMP(1), %xmm1
	movaps		%xmm3, TEMP(0)
	movaps		%xmm1, TEMP(2)
	movaps		%xmm5, %xmm1
	movaps		%xmm4, %xmm5
	movaps		%xmm7, %xmm3
	subps		%xmm1, %xmm5
	subps		%xmm6, %xmm7
	addps		%xmm1, %xmm4
	addps		%xmm3, %xmm6
	mulps		ASM_NAME(costab_mmxsse)+96, %xmm2
	mulps		ASM_NAME(costab_mmxsse)+96, %xmm0
	mulps		ASM_NAME(costab_mmxsse)+96, %xmm5
	mulps		ASM_NAME(costab_mmxsse)+96, %xmm7
	movaps		%xmm2, TEMP(1)
	movaps		%xmm0, TEMP(3)
	
	movaps		%xmm4, %xmm2
	movaps		%xmm5, %xmm3
	shufps		$0x44, %xmm6, %xmm2
	shufps		$0xbb, %xmm7, %xmm5
	shufps		$0xbb, %xmm6, %xmm4
	shufps		$0x44, %xmm7, %xmm3
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	subps		%xmm4, %xmm2
	subps		%xmm5, %xmm3
	addps		%xmm6, %xmm4
	addps		%xmm7, %xmm5
	movaps		ASM_NAME(costab_mmxsse)+112, %xmm0
	movlhps		%xmm0, %xmm0
	mulps		%xmm0, %xmm2
	mulps		%xmm0, %xmm3
	movaps		%xmm0, TEMP(4)
	movaps		%xmm4, %xmm6
	movaps		%xmm5, %xmm7
	shufps		$0x14, %xmm2, %xmm4
	shufps		$0xbe, %xmm2, %xmm6
	shufps		$0x14, %xmm3, %xmm5
	shufps		$0xbe, %xmm3, %xmm7
	movaps		%xmm5, TEMP(5)
	movaps		%xmm7, TEMP(7)
	
	movaps		TEMP(0), %xmm0
	movaps		TEMP(1), %xmm1
	movaps		%xmm0, %xmm2
	movaps		%xmm1, %xmm3
	shufps		$0x44, TEMP(2), %xmm2
	shufps		$0xbb, TEMP(3), %xmm1
	shufps		$0xbb, TEMP(2), %xmm0
	shufps		$0x44, TEMP(3), %xmm3
	movaps		%xmm2, %xmm5
	movaps		%xmm3, %xmm7
	subps		%xmm0, %xmm2
	subps		%xmm1, %xmm3
	addps		%xmm5, %xmm0
	addps		%xmm7, %xmm1
	mulps		TEMP(4), %xmm2
	mulps		TEMP(4), %xmm3
	movaps		%xmm0, %xmm5
	movaps		%xmm1, %xmm7
	shufps		$0x14, %xmm2, %xmm0
	shufps		$0xbe, %xmm2, %xmm5
	shufps		$0x14, %xmm3, %xmm1
	shufps		$0xbe, %xmm3, %xmm7
	
	movaps		%xmm0, TEMP(0)
	movaps		%xmm1, TEMP(1)
	movaps		%xmm5, TEMP(2)
	movaps		%xmm7, TEMP(3)
	
	movss		ASM_NAME(costab_mmxsse)+120, %xmm5
	shufps		$0x00, %xmm5, %xmm5
	xorps		pnpn, %xmm5
	
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	unpcklps	TEMP(5), %xmm4
	unpckhps	TEMP(5), %xmm0
	unpcklps	TEMP(7), %xmm6
	unpckhps	TEMP(7), %xmm1
	movaps		%xmm4, %xmm2
	movaps		%xmm6, %xmm3
	unpcklps	%xmm0, %xmm4
	unpckhps	%xmm0, %xmm2
	unpcklps	%xmm1, %xmm6
	unpckhps	%xmm1, %xmm3
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	subps		%xmm2, %xmm0
	subps		%xmm3, %xmm1
	addps		%xmm2, %xmm4
	addps		%xmm3, %xmm6
	mulps		%xmm5, %xmm0
	mulps		%xmm5, %xmm1
	movaps		%xmm5, TEMP(5)
	movaps		%xmm4, %xmm5
	movaps		%xmm6, %xmm7
	unpcklps	%xmm0, %xmm4
	unpckhps	%xmm0, %xmm5
	unpcklps	%xmm1, %xmm6
	unpckhps	%xmm1, %xmm7
	
	movaps		TEMP(0), %xmm0
	movaps		TEMP(2), %xmm2
	movaps		%xmm4, TEMP(4)
	movaps		%xmm6, TEMP(6)
	
	movaps		%xmm0, %xmm4
	movaps		%xmm2, %xmm6
	unpcklps	TEMP(1), %xmm0
	unpckhps	TEMP(1), %xmm4
	unpcklps	TEMP(3), %xmm2
	unpckhps	TEMP(3), %xmm6
	movaps		%xmm0, %xmm1
	movaps		%xmm2, %xmm3
	unpcklps	%xmm4, %xmm0
	unpckhps	%xmm4, %xmm1
	unpcklps	%xmm6, %xmm2
	unpckhps	%xmm6, %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm2, %xmm6
	subps		%xmm1, %xmm4
	subps		%xmm3, %xmm6
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	mulps		TEMP(5), %xmm4
	mulps		TEMP(5), %xmm6
	movaps		%xmm0, %xmm1
	movaps		%xmm2, %xmm3
	unpcklps	%xmm4, %xmm0
	unpckhps	%xmm4, %xmm1
	unpcklps	%xmm6, %xmm2
	unpckhps	%xmm6, %xmm3
	
	movaps		%xmm0, TEMP(0)
	movaps		%xmm1, TEMP(1)
	movaps		%xmm2, TEMP(2)
	movaps		%xmm3, TEMP(3)
	movaps		%xmm5, TEMP(5)
	movaps		%xmm7, TEMP(7)
	
	movss		TEMP_BYTE(12), %xmm0
	movss		TEMP_BYTE(28), %xmm1
	movss		TEMP_BYTE(44), %xmm2
	movss		TEMP_BYTE(60), %xmm3
	addss		TEMP_BYTE(8), %xmm0
	addss		TEMP_BYTE(24), %xmm1
	addss		TEMP_BYTE(40), %xmm2
	addss		TEMP_BYTE(56), %xmm3
	movss		%xmm0, TEMP_BYTE(8)
	movss		%xmm1, TEMP_BYTE(24)
	movss		%xmm2, TEMP_BYTE(40)
	movss		%xmm3, TEMP_BYTE(56)
	movss		TEMP_BYTE(76), %xmm0
	movss		TEMP_BYTE(92), %xmm1
	movss		TEMP_BYTE(108), %xmm2
	movss		TEMP_BYTE(124), %xmm3
	addss		TEMP_BYTE(72), %xmm0
	addss		TEMP_BYTE(88), %xmm1
	addss		TEMP_BYTE(104), %xmm2
	addss		TEMP_BYTE(120), %xmm3
	movss		%xmm0, TEMP_BYTE(72)
	movss		%xmm1, TEMP_BYTE(88)
	movss		%xmm2, TEMP_BYTE(104)
	movss		%xmm3, TEMP_BYTE(120)
	
	movaps		TEMP_BYTE(16), %xmm1
	movaps		TEMP_BYTE(48), %xmm3
	movaps		TEMP_BYTE(80), %xmm5
	movaps		TEMP_BYTE(112), %xmm7
	movaps		%xmm1, %xmm0
	movaps		%xmm3, %xmm2
	movaps		%xmm5, %xmm4
	movaps		%xmm7, %xmm6
	shufps		$0x1e, %xmm0, %xmm0
	shufps		$0x1e, %xmm2, %xmm2
	shufps		$0x1e, %xmm4, %xmm4
	shufps		$0x1e, %xmm6, %xmm6
	andps		mask, %xmm0
	andps		mask, %xmm2
	andps		mask, %xmm4
	andps		mask, %xmm6
	addps		%xmm0, %xmm1
	addps		%xmm2, %xmm3
	addps		%xmm4, %xmm5
	addps		%xmm6, %xmm7
	
	movaps		TEMP_BYTE(32), %xmm2
	movaps		TEMP_BYTE(96), %xmm6
	movaps		%xmm2, %xmm0
	movaps		%xmm6, %xmm4
	shufps		$0x1e, %xmm0, %xmm0
	shufps		$0x1e, %xmm4, %xmm4
	andps		mask, %xmm0
	andps		mask, %xmm4
	addps		%xmm3, %xmm2
	addps		%xmm0, %xmm3
	addps		%xmm7, %xmm6
	addps		%xmm4, %xmm7
	
	movaps		TEMP_BYTE(0), %xmm0
	movaps		TEMP_BYTE(64), %xmm4
	
	cvtps2pi	%xmm0, %mm0
	cvtps2pi	%xmm1, %mm1
	movhlps		%xmm0, %xmm0
	movhlps		%xmm1, %xmm1
	cvtps2pi	%xmm0, %mm2
	cvtps2pi	%xmm1, %mm3
	packssdw	%mm2, %mm0
	packssdw	%mm3, %mm1
	
	cvtps2pi	%xmm2, %mm2
	cvtps2pi	%xmm3, %mm3
	movhlps		%xmm2, %xmm2
	movhlps		%xmm3, %xmm3
	cvtps2pi	%xmm2, %mm4
	cvtps2pi	%xmm3, %mm5
	packssdw	%mm4, %mm2
	packssdw	%mm5, %mm3
	
	movd		%mm0, %eax
	movd		%mm1, %edx
	movw		%ax, 512(%ecx)
	movw		%dx, 384(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, (%ecx)
	movw		%ax, (%ebx)
	movw		%dx, 128(%ebx)
	
	movd		%mm2, %eax
	movd		%mm3, %edx
	movw		%ax, 448(%ecx)
	movw		%dx, 320(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 64(%ebx)
	movw		%dx, 192(%ebx)
	
	psrlq		$32, %mm0
	psrlq		$32, %mm1
	movd		%mm0, %eax
	movd		%mm1, %edx
	movw		%ax, 256(%ecx)
	movw		%dx, 128(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 256(%ebx)
	movw		%dx, 384(%ebx)
	
	psrlq		$32, %mm2
	psrlq		$32, %mm3
	movd		%mm2, %eax
	movd		%mm3, %edx
	movw		%ax, 192(%ecx)
	movw		%dx, 64(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 320(%ebx)
	movw		%dx, 448(%ebx)
	
	movaps		%xmm4, %xmm0
	shufps		$0x1e, %xmm0, %xmm0
	movaps		%xmm5, %xmm1
	andps		mask, %xmm0
	
	addps		%xmm6, %xmm4
	addps		%xmm7, %xmm5
	addps		%xmm1, %xmm6
	addps		%xmm0, %xmm7
	
	cvtps2pi	%xmm4, %mm0
	cvtps2pi	%xmm5, %mm1
	movhlps		%xmm4, %xmm4
	movhlps		%xmm5, %xmm5
	cvtps2pi	%xmm4, %mm2
	cvtps2pi	%xmm5, %mm3
	packssdw	%mm2, %mm0
	packssdw	%mm3, %mm1
	
	cvtps2pi	%xmm6, %mm2
	cvtps2pi	%xmm7, %mm3
	movhlps		%xmm6, %xmm6
	movhlps		%xmm7, %xmm7
	cvtps2pi	%xmm6, %mm4
	cvtps2pi	%xmm7, %mm5
	packssdw	%mm4, %mm2
	packssdw	%mm5, %mm3
	
	movd		%mm0, %eax
	movd		%mm2, %edx
	movw		%ax, 480(%ecx)
	movw		%dx, 416(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 32(%ebx)
	movw		%dx, 96(%ebx)
	
	psrlq		$32, %mm0
	psrlq		$32, %mm2
	movd		%mm0, %eax
	movd		%mm2, %edx
	movw		%ax, 224(%ecx)
	movw		%dx, 160(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 288(%ebx)
	movw		%dx, 352(%ebx)
	
	movd		%mm1, %eax
	movd		%mm3, %edx
	movw		%ax, 352(%ecx)
	movw		%dx, 288(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 160(%ebx)
	movw		%dx, 224(%ebx)
	
	psrlq		$32, %mm1
	psrlq		$32, %mm3
	movd		%mm1, %eax
	movd		%mm3, %edx
	movw		%ax, 96(%ecx)
	movw		%dx, 32(%ecx)
	shrl		$16, %eax
	shrl		$16, %edx
	movw		%ax, 416(%ebx)
	movw		%dx, 480(%ebx)
	
	popl		%ebx
	movl		%ebp, %esp
	popl		%ebp
	ret

NONEXEC_STACK

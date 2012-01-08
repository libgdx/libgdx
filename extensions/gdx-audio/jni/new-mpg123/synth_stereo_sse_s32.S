/*
	synth_stereo_sse_s32: SSE optimized synth (stereo specific, s32 output version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

/* real *window; */
#define WINDOW %ebx
/* real *b0l; */
#define B0L %edx
/* real *b0r; */
#define B0R %esi
/* real *samples; */
#define SAMPLES %edi

#define TEMP(n) (12+16*n)(%esp)
#define MMREG_CLIP %mm7

/*
	int synth_1to1_s32_s_sse_asm(real *window, real *b0l, real *b0r, int32_t *samples, int bo1);
	return value: number of clipped samples
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(scale_s32):
	.long   1199570944 /* 65536.0 */
	.long   1199570944
	.long   1199570944
	.long   1199570944
	ALIGN16
ASM_NAME(maxmin_s32):
	.long   1191182335 /* 32767.999 */
	.long   1191182335
	.long   1191182335
	.long   1191182335
	.long   -956301312 /* -32768.0 */
	.long   -956301312
	.long   -956301312
	.long   -956301312
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_s32_s_sse_asm)
ASM_NAME(synth_1to1_s32_s_sse_asm):
	pushl		%ebp
	movl		%esp, %ebp
	andl		$-16, %esp
	subl		$128, %esp
	pushl		%ebx
	pushl		%esi
	pushl		%edi
	
	pxor		MMREG_CLIP, MMREG_CLIP
	
	movl		8(%ebp), WINDOW
	movl		12(%ebp), B0L
	movl		16(%ebp), B0R
	movl		20(%ebp), SAMPLES
	movl		24(%ebp), %eax
	shll		$2, %eax
	
	leal		64(WINDOW), WINDOW
	subl		%eax, WINDOW

	movl		$4, %ecx
	
	ALIGN16
Loop_start_1:
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(0)
	movaps		%xmm4, TEMP(4)
	
	leal		128(WINDOW), WINDOW
	leal		64(B0L), B0L
	leal		64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(1)
	movaps		%xmm4, TEMP(5)
	
	leal		128(WINDOW), WINDOW
	leal		64(B0L), B0L
	leal		64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(2)
	movaps		%xmm4, TEMP(6)
	
	leal		128(WINDOW), WINDOW
	leal		64(B0L), B0L
	leal		64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, %xmm7
	movaps		%xmm4, TEMP(7)
	
	leal		128(WINDOW), WINDOW
	leal		64(B0L), B0L
	leal		64(B0R), B0R
	
	movaps		TEMP(0), %xmm4
	movaps		TEMP(1), %xmm5
	movaps		TEMP(2), %xmm6
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	unpcklps	%xmm5, %xmm4
	unpcklps	%xmm7, %xmm6
	unpckhps	%xmm5, %xmm0
	unpckhps	%xmm7, %xmm1
	movaps		%xmm4, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm6, %xmm4
	movhlps		%xmm2, %xmm6
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	subps		%xmm6, %xmm4
	subps		%xmm1, %xmm0
	addps		%xmm4, %xmm0
	movaps		%xmm0, %xmm2
	
	movaps		TEMP(4), %xmm4
	movaps		TEMP(5), %xmm5
	movaps		TEMP(6), %xmm6
	movaps		TEMP(7), %xmm7
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	unpcklps	%xmm5, %xmm4
	unpcklps	%xmm7, %xmm6
	unpckhps	%xmm5, %xmm0
	unpckhps	%xmm7, %xmm1
	movaps		%xmm2, %xmm5
	movaps		%xmm4, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm6, %xmm4
	movhlps		%xmm2, %xmm6
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	subps		%xmm6, %xmm4
	subps		%xmm1, %xmm0
	addps		%xmm4, %xmm0
	
	movaps		%xmm5, %xmm1
	movaps		%xmm5, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm0, %xmm4
	mulps		ASM_NAME(scale_s32), %xmm5
	mulps		ASM_NAME(scale_s32), %xmm0
	cmpnleps	ASM_NAME(maxmin_s32), %xmm1
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm2
	cmpnleps	ASM_NAME(maxmin_s32), %xmm3
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm4
	cvtps2pi	%xmm5, %mm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm2
	cvtps2pi	%xmm3, %mm3
	psrad		$31, %mm2
	psrad		$31, %mm3
	pxor		%mm2, %mm0
	pxor		%mm3, %mm1
	movq		%mm0, %mm4
	punpckldq	%mm1, %mm0
	punpckhdq	%mm1, %mm4
	movq		%mm0, (SAMPLES)
	movq		%mm4, 8(SAMPLES)
	movhlps		%xmm5, %xmm5
	movhlps		%xmm0, %xmm0
	movhlps		%xmm1, %xmm1
	movhlps		%xmm3, %xmm3
	cvtps2pi	%xmm5, %mm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm4
	cvtps2pi	%xmm3, %mm5
	psrad		$31, %mm4
	psrad		$31, %mm5
	pxor		%mm4, %mm0
	pxor		%mm5, %mm1
	movq		%mm0, %mm6
	punpckldq	%mm1, %mm0
	punpckhdq	%mm1, %mm6
	movq		%mm0, 16(SAMPLES)
	movq		%mm6, 24(SAMPLES)
	
	packssdw	%mm4, %mm2
	packssdw	%mm5, %mm3
	psrlw		$15, %mm2
	psrlw		$15, %mm3
	cvtps2pi	%xmm2, %mm0
	cvtps2pi	%xmm4, %mm1
	movhlps		%xmm2, %xmm2
	movhlps		%xmm4, %xmm4
	cvtps2pi	%xmm2, %mm4
	cvtps2pi	%xmm4, %mm5
	packssdw	%mm4, %mm0
	packssdw	%mm5, %mm1
	psrlw		$15, %mm0
	psrlw		$15, %mm1
	paddw		%mm3, %mm2
	paddw		%mm1, %mm0
	paddw		%mm2, %mm0
	paddw		%mm0, MMREG_CLIP
	
	leal		32(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_1
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_2:
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(0)
	movaps		%xmm4, TEMP(4)
	
	leal		128(WINDOW), WINDOW
	leal		-64(B0L), B0L
	leal		-64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(1)
	movaps		%xmm4, TEMP(5)
	
	leal		128(WINDOW), WINDOW
	leal		-64(B0L), B0L
	leal		-64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, TEMP(2)
	movaps		%xmm4, TEMP(6)
	
	leal		128(WINDOW), WINDOW
	leal		-64(B0L), B0L
	leal		-64(B0R), B0R
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movaps		%xmm0, %xmm4
	movaps		%xmm1, %xmm5
	movaps		%xmm2, %xmm6
	movaps		%xmm3, %xmm7
	mulps		0(B0L), %xmm0
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		0(B0R), %xmm4
	mulps		16(B0R), %xmm5
	mulps		32(B0R), %xmm6
	mulps		48(B0R), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm0, %xmm7
	movaps		%xmm4, TEMP(7)
	
	leal		128(WINDOW), WINDOW
	leal		-64(B0L), B0L
	leal		-64(B0R), B0R
	
	movaps		TEMP(0), %xmm4
	movaps		TEMP(1), %xmm5
	movaps		TEMP(2), %xmm6
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	unpcklps	%xmm5, %xmm4
	unpcklps	%xmm7, %xmm6
	unpckhps	%xmm5, %xmm0
	unpckhps	%xmm7, %xmm1
	movaps		%xmm4, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm6, %xmm4
	movhlps		%xmm2, %xmm6
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	addps		%xmm6, %xmm4
	addps		%xmm1, %xmm0
	addps		%xmm4, %xmm0
	movaps		%xmm0, %xmm2
	
	movaps		TEMP(4), %xmm4
	movaps		TEMP(5), %xmm5
	movaps		TEMP(6), %xmm6
	movaps		TEMP(7), %xmm7
	movaps		%xmm4, %xmm0
	movaps		%xmm6, %xmm1
	unpcklps	%xmm5, %xmm4
	unpcklps	%xmm7, %xmm6
	unpckhps	%xmm5, %xmm0
	unpckhps	%xmm7, %xmm1
	movaps		%xmm2, %xmm5
	movaps		%xmm4, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm6, %xmm4
	movhlps		%xmm2, %xmm6
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	addps		%xmm6, %xmm4
	addps		%xmm1, %xmm0
	addps		%xmm4, %xmm0
	
	movaps		%xmm5, %xmm1
	movaps		%xmm5, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm0, %xmm4
	mulps		ASM_NAME(scale_s32), %xmm5
	mulps		ASM_NAME(scale_s32), %xmm0
	cmpnleps	ASM_NAME(maxmin_s32), %xmm1
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm2
	cmpnleps	ASM_NAME(maxmin_s32), %xmm3
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm4
	cvtps2pi	%xmm5, %mm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm2
	cvtps2pi	%xmm3, %mm3
	psrad		$31, %mm2
	psrad		$31, %mm3
	pxor		%mm2, %mm0
	pxor		%mm3, %mm1
	movq		%mm0, %mm4
	punpckldq	%mm1, %mm0
	punpckhdq	%mm1, %mm4
	movq		%mm0, (SAMPLES)
	movq		%mm4, 8(SAMPLES)
	movhlps		%xmm5, %xmm5
	movhlps		%xmm0, %xmm0
	movhlps		%xmm1, %xmm1
	movhlps		%xmm3, %xmm3
	cvtps2pi	%xmm5, %mm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm4
	cvtps2pi	%xmm3, %mm5
	psrad		$31, %mm4
	psrad		$31, %mm5
	pxor		%mm4, %mm0
	pxor		%mm5, %mm1
	movq		%mm0, %mm6
	punpckldq	%mm1, %mm0
	punpckhdq	%mm1, %mm6
	movq		%mm0, 16(SAMPLES)
	movq		%mm6, 24(SAMPLES)
	
	packssdw	%mm4, %mm2
	packssdw	%mm5, %mm3
	psrlw		$15, %mm2
	psrlw		$15, %mm3
	cvtps2pi	%xmm2, %mm0
	cvtps2pi	%xmm4, %mm1
	movhlps		%xmm2, %xmm2
	movhlps		%xmm4, %xmm4
	cvtps2pi	%xmm2, %mm4
	cvtps2pi	%xmm4, %mm5
	packssdw	%mm4, %mm0
	packssdw	%mm5, %mm1
	psrlw		$15, %mm0
	psrlw		$15, %mm1
	paddw		%mm3, %mm2
	paddw		%mm1, %mm0
	paddw		%mm2, %mm0
	paddw		%mm0, MMREG_CLIP
	
	leal		32(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_2
	
	pshufw		$0xee, MMREG_CLIP, %mm0
	paddw		MMREG_CLIP, %mm0
	pshufw		$0x55, %mm0, %mm1
	paddw		%mm1, %mm0
	movd		%mm0, %eax
	andl		$0xffff, %eax
	
	popl		%edi
	popl		%esi
	popl		%ebx
	movl		%ebp, %esp
	popl		%ebp
	
	emms
	
	ret

NONEXEC_STACK

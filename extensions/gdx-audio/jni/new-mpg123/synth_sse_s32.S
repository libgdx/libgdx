/*
	synth_sse_s32: SSE optimized synth (s32 output version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

/* real *window; */
#define WINDOW %ebx
/* real *b0; */
#define B0 %edx
/* real *samples; */
#define SAMPLES %esi

#define MMREG_CLIP %mm7

/*
	int synth_1to1_s32_sse_asm(real *window, real *b0, int32_t *samples, int bo1);
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
.globl ASM_NAME(synth_1to1_s32_sse_asm)
ASM_NAME(synth_1to1_s32_sse_asm):
	pushl		%ebp
	movl		%esp, %ebp
	pushl		%ebx
	pushl		%esi
	
	pxor		MMREG_CLIP, MMREG_CLIP
	
	movl		8(%ebp), WINDOW
	movl		12(%ebp), B0
	movl		16(%ebp), SAMPLES
	movl		20(%ebp), %eax
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
	movups		128(WINDOW), %xmm4
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	mulps		0(B0), %xmm0
	mulps		16(B0), %xmm1
	mulps		32(B0), %xmm2
	mulps		48(B0), %xmm3
	mulps		64(B0), %xmm4
	mulps		80(B0), %xmm5
	mulps		96(B0), %xmm6
	mulps		112(B0), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm4, %xmm5
	movaps		%xmm0, %xmm4
	
	leal		256(WINDOW), WINDOW
	leal		128(B0), B0
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm6
	movups		144(WINDOW), %xmm7
	mulps		(B0), %xmm0
	mulps		16(B0), %xmm1
	mulps		32(B0), %xmm2
	mulps		48(B0), %xmm3
	mulps		64(B0), %xmm6
	mulps		80(B0), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm7, %xmm6
	movups		160(WINDOW), %xmm1
	movups		176(WINDOW), %xmm3
	mulps		96(B0), %xmm1
	mulps		112(B0), %xmm3
	addps		%xmm2, %xmm0
	addps		%xmm3, %xmm1
	addps		%xmm1, %xmm6
	movaps		%xmm6, %xmm7
	movaps		%xmm0, %xmm6
	
	leal		256(WINDOW), WINDOW
	leal		128(B0), B0
	
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
	
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	mulps		ASM_NAME(scale_s32), %xmm0
	cmpnleps	ASM_NAME(maxmin_s32), %xmm1
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm2
	cvtps2pi	%xmm0, %mm0
	movhlps		%xmm0, %xmm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm2
	movhlps		%xmm1, %xmm1
	cvtps2pi	%xmm1, %mm3
	psrad		$31, %mm2
	psrad		$31, %mm3
	pxor		%mm2, %mm0
	pxor		%mm3, %mm1
	movd		%mm0, (SAMPLES)
	psrlq		$32, %mm0
	movd		%mm0, 8(SAMPLES)
	movd		%mm1, 16(SAMPLES)
	psrlq		$32, %mm1
	movd		%mm1, 24(SAMPLES)
	
	cvtps2pi	%xmm2, %mm0
	movhlps		%xmm2, %xmm2
	cvtps2pi	%xmm2, %mm1
	packssdw	%mm3, %mm2
	packssdw	%mm1, %mm0
	psrlw		$15, %mm2
	psrlw		$15, %mm0
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
	movups		128(WINDOW), %xmm4
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	mulps		0(B0), %xmm0
	mulps		16(B0), %xmm1
	mulps		32(B0), %xmm2
	mulps		48(B0), %xmm3
	mulps		-64(B0), %xmm4
	mulps		-48(B0), %xmm5
	mulps		-32(B0), %xmm6
	mulps		-16(B0), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm5, %xmm4
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm0
	addps		%xmm6, %xmm4
	movaps		%xmm4, %xmm5
	movaps		%xmm0, %xmm4
	
	leal		256(WINDOW), WINDOW
	leal		-128(B0), B0
	
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm6
	movups		144(WINDOW), %xmm7
	mulps		(B0), %xmm0
	mulps		16(B0), %xmm1
	mulps		32(B0), %xmm2
	mulps		48(B0), %xmm3
	mulps		-64(B0), %xmm6
	mulps		-48(B0), %xmm7
	addps		%xmm1, %xmm0
	addps		%xmm3, %xmm2
	addps		%xmm7, %xmm6
	movups		160(WINDOW), %xmm1
	movups		176(WINDOW), %xmm3
	mulps		-32(B0), %xmm1
	mulps		-16(B0), %xmm3
	addps		%xmm2, %xmm0
	addps		%xmm3, %xmm1
	addps		%xmm1, %xmm6
	movaps		%xmm6, %xmm7
	movaps		%xmm0, %xmm6
	
	leal		256(WINDOW), WINDOW
	leal		-128(B0), B0
	
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
	
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	mulps		ASM_NAME(scale_s32), %xmm0
	cmpnleps	ASM_NAME(maxmin_s32), %xmm1
	cmpltps		ASM_NAME(maxmin_s32)+16, %xmm2
	cvtps2pi	%xmm0, %mm0
	movhlps		%xmm0, %xmm0
	cvtps2pi	%xmm0, %mm1
	cvtps2pi	%xmm1, %mm2
	movhlps		%xmm1, %xmm1
	cvtps2pi	%xmm1, %mm3
	psrad		$31, %mm2
	psrad		$31, %mm3
	pxor		%mm2, %mm0
	pxor		%mm3, %mm1
	movd		%mm0, (SAMPLES)
	psrlq		$32, %mm0
	movd		%mm0, 8(SAMPLES)
	movd		%mm1, 16(SAMPLES)
	psrlq		$32, %mm1
	movd		%mm1, 24(SAMPLES)
	
	cvtps2pi	%xmm2, %mm0
	movhlps		%xmm2, %xmm2
	cvtps2pi	%xmm2, %mm1
	packssdw	%mm3, %mm2
	packssdw	%mm1, %mm0
	psrlw		$15, %mm2
	psrlw		$15, %mm0
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
	
	popl		%esi
	popl		%ebx
	movl		%ebp, %esp
	popl		%ebp
	
	emms
	
	ret

NONEXEC_STACK

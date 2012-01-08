/*
	synth_sse_float: SSE optimized synth (stereo specific, float output version)

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

/*
	int synth_1to1_real_s_sse_asm(real *window, real *b0l, real *b0r, real *samples, int bo1);
	return value: number of clipped samples (0)
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(scale_sse):
	.long   939524096
	.long   939524096
	.long   939524096
	.long   939524096
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_real_s_sse_asm)
ASM_NAME(synth_1to1_real_s_sse_asm):
	pushl		%ebp
	movl		%esp, %ebp
	andl		$-16, %esp
	subl		$128, %esp
	pushl		%ebx
	pushl		%esi
	pushl		%edi
	
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
	
	mulps		ASM_NAME(scale_sse), %xmm5
	mulps		ASM_NAME(scale_sse), %xmm0
	movaps		%xmm5, %xmm1
	unpcklps	%xmm0, %xmm5
	unpckhps	%xmm0, %xmm1
	movups		%xmm5, (SAMPLES)
	movups		%xmm1, 16(SAMPLES)
	
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
	
	mulps		ASM_NAME(scale_sse), %xmm5
	mulps		ASM_NAME(scale_sse), %xmm0
	movaps		%xmm5, %xmm1
	unpcklps	%xmm0, %xmm5
	unpckhps	%xmm0, %xmm1
	movups		%xmm5, (SAMPLES)
	movups		%xmm1, 16(SAMPLES)
	
	leal		32(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_2
	
	xorl		%eax, %eax
	
	popl		%edi
	popl		%esi
	popl		%ebx
	movl		%ebp, %esp
	popl		%ebp
	
	ret

NONEXEC_STACK

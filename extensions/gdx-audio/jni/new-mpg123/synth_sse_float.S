/*
	synth_sse_float: SSE optimized synth (float output version)

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

/*
	int synth_1to1_real_sse_asm(real *window, real *b0, real *samples, int bo1);
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
.globl ASM_NAME(synth_1to1_real_sse_asm)
ASM_NAME(synth_1to1_real_sse_asm):
	pushl		%ebp
	movl		%esp, %ebp
	pushl		%ebx
	pushl		%esi
	
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
	
	movups		(SAMPLES), %xmm1
	movups		16(SAMPLES), %xmm2
	mulps		ASM_NAME(scale_sse), %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (SAMPLES)
	movups		%xmm2, 16(SAMPLES)
	
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
	
	movups		(SAMPLES), %xmm1
	movups		16(SAMPLES), %xmm2
	mulps		ASM_NAME(scale_sse), %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (SAMPLES)
	movups		%xmm2, 16(SAMPLES)
	
	leal		32(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_2
	
	xorl		%eax, %eax
	
	popl		%esi
	popl		%ebx
	movl		%ebp, %esp
	popl		%ebp
	
	ret

NONEXEC_STACK

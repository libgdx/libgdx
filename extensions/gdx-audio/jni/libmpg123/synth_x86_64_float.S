/*
	synth_x86_64_float: SSE optimized synth for x86-64 (float output version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#ifdef _WIN64
/* short *window; */
#define ARG0 %r10
/* short *b0; */
#define ARG1 %rdx
/* short *samples; */
#define ARG2 %r8
/* int bo1; */
#define ARG3 %r9
#else
/* real *window; */
#define ARG0 %rdi
/* real *b0; */
#define ARG1 %rsi
/* real *samples; */
#define ARG2 %rdx
/* int bo1; */
#define ARG3 %rcx
#endif

#define XMMREG_SCALE %xmm15  /* {1/32768.0, 1/32768.0, 1/32768.0, 1/32768.0} */

/*
	int synth_1to1_real_x86_64_asm(real *window, real *b0, real *samples, int bo1);
	return value: number of clipped samples (0)
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(scale_x86_64):
	.long   939524096
	.long   939524096
	.long   939524096
	.long   939524096
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_real_x86_64_asm)
ASM_NAME(synth_1to1_real_x86_64_asm):
#ifdef _WIN64 /* should save xmm6-15 */
	movq		%rcx, ARG0
	subq		$120, %rsp /* stack alignment + 7 xmm registers */
	movaps		%xmm6, (%rsp)
	movaps		%xmm7, 16(%rsp)
	movaps		%xmm8, 32(%rsp)
	movaps		%xmm9, 48(%rsp)
	movaps		%xmm10, 64(%rsp)
	movaps		%xmm11, 80(%rsp)
	movaps		%xmm15, 96(%rsp)
#endif

	leaq		ASM_NAME(scale_x86_64)(%rip), %rax
	movaps		(%rax), XMMREG_SCALE
	
	andq		$0xf, ARG3
	shlq		$2, ARG3
	leaq		64(ARG0), ARG0
	subq		ARG3, ARG0

	movl		$4, %ecx
	
	ALIGN16
Loop_start_1:
	movups		(ARG0), %xmm8
	movups		16(ARG0), %xmm1
	movups		32(ARG0), %xmm2
	movups		48(ARG0), %xmm3
	movups		128(ARG0), %xmm9
	movups		144(ARG0), %xmm5
	movups		160(ARG0), %xmm6
	movups		176(ARG0), %xmm7
	mulps		(ARG1), %xmm8
	mulps		16(ARG1), %xmm1
	mulps		32(ARG1), %xmm2
	mulps		48(ARG1), %xmm3
	mulps		64(ARG1), %xmm9
	mulps		80(ARG1), %xmm5
	mulps		96(ARG1), %xmm6
	mulps		112(ARG1), %xmm7
	
	addps		%xmm1, %xmm8
	addps		%xmm2, %xmm3
	addps		%xmm5, %xmm9
	addps		%xmm7, %xmm6
	addps		%xmm3, %xmm8
	addps		%xmm6, %xmm9
	leaq		256(ARG0), ARG0
	leaq		128(ARG1), ARG1
	
	movups		(ARG0), %xmm10
	movups		16(ARG0), %xmm1
	movups		32(ARG0), %xmm2
	movups		48(ARG0), %xmm3
	movups		128(ARG0), %xmm11
	movups		144(ARG0), %xmm5
	movups		160(ARG0), %xmm6
	movups		176(ARG0), %xmm7
	mulps		(ARG1), %xmm10
	mulps		16(ARG1), %xmm1
	mulps		32(ARG1), %xmm2
	mulps		48(ARG1), %xmm3
	mulps		64(ARG1), %xmm11
	mulps		80(ARG1), %xmm5
	mulps		96(ARG1), %xmm6
	mulps		112(ARG1), %xmm7
	
	addps		%xmm1, %xmm10
	addps		%xmm2, %xmm3
	addps		%xmm5, %xmm11
	addps		%xmm7, %xmm6
	addps		%xmm3, %xmm10
	addps		%xmm6, %xmm11
	leaq		256(ARG0), ARG0
	leaq		128(ARG1), ARG1
	
	movaps		%xmm8, %xmm0
	movaps		%xmm10, %xmm1
	unpcklps	%xmm9, %xmm8
	unpcklps	%xmm11, %xmm10
	unpckhps	%xmm9, %xmm0
	unpckhps	%xmm11, %xmm1
	movaps		%xmm8, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm10, %xmm8
	movhlps		%xmm2, %xmm10
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	subps		%xmm10, %xmm8
	subps		%xmm1, %xmm0
	addps		%xmm8, %xmm0
	
	movups		(ARG2), %xmm1
	movups		16(ARG2), %xmm2
	mulps		XMMREG_SCALE, %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (ARG2)
	movups		%xmm2, 16(ARG2)
	
	leaq		32(ARG2), ARG2
	decl		%ecx
	jnz			Loop_start_1
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_2:
	movups		(ARG0), %xmm8
	movups		16(ARG0), %xmm1
	movups		32(ARG0), %xmm2
	movups		48(ARG0), %xmm3
	movups		128(ARG0), %xmm9
	movups		144(ARG0), %xmm5
	movups		160(ARG0), %xmm6
	movups		176(ARG0), %xmm7
	mulps		(ARG1), %xmm8
	mulps		16(ARG1), %xmm1
	mulps		32(ARG1), %xmm2
	mulps		48(ARG1), %xmm3
	mulps		-64(ARG1), %xmm9
	mulps		-48(ARG1), %xmm5
	mulps		-32(ARG1), %xmm6
	mulps		-16(ARG1), %xmm7
	
	addps		%xmm1, %xmm8
	addps		%xmm2, %xmm3
	addps		%xmm5, %xmm9
	addps		%xmm7, %xmm6
	addps		%xmm3, %xmm8
	addps		%xmm6, %xmm9
	leaq		256(ARG0), ARG0
	leaq		-128(ARG1), ARG1
	
	movups		(ARG0), %xmm10
	movups		16(ARG0), %xmm1
	movups		32(ARG0), %xmm2
	movups		48(ARG0), %xmm3
	movups		128(ARG0), %xmm11
	movups		144(ARG0), %xmm5
	movups		160(ARG0), %xmm6
	movups		176(ARG0), %xmm7
	mulps		(ARG1), %xmm10
	mulps		16(ARG1), %xmm1
	mulps		32(ARG1), %xmm2
	mulps		48(ARG1), %xmm3
	mulps		-64(ARG1), %xmm11
	mulps		-48(ARG1), %xmm5
	mulps		-32(ARG1), %xmm6
	mulps		-16(ARG1), %xmm7
	
	addps		%xmm1, %xmm10
	addps		%xmm2, %xmm3
	addps		%xmm5, %xmm11
	addps		%xmm7, %xmm6
	addps		%xmm3, %xmm10
	addps		%xmm6, %xmm11
	leaq		256(ARG0), ARG0
	leaq		-128(ARG1), ARG1
	
	movaps		%xmm8, %xmm0
	movaps		%xmm10, %xmm1
	unpcklps	%xmm9, %xmm8
	unpcklps	%xmm11, %xmm10
	unpckhps	%xmm9, %xmm0
	unpckhps	%xmm11, %xmm1
	movaps		%xmm8, %xmm2
	movaps		%xmm0, %xmm3
	movlhps		%xmm10, %xmm8
	movhlps		%xmm2, %xmm10
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	addps		%xmm10, %xmm8
	addps		%xmm1, %xmm0
	addps		%xmm8, %xmm0
	
	movups		(ARG2), %xmm1
	movups		16(ARG2), %xmm2
	mulps		XMMREG_SCALE, %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (ARG2)
	movups		%xmm2, 16(ARG2)
	
	leaq		32(ARG2), ARG2
	decl		%ecx
	jnz			Loop_start_2
	
	xorl		%eax, %eax
	
#ifdef _WIN64
	movaps		(%rsp), %xmm6
	movaps		16(%rsp), %xmm7
	movaps		32(%rsp), %xmm8
	movaps		48(%rsp), %xmm9
	movaps		64(%rsp), %xmm10
	movaps		80(%rsp), %xmm11
	movaps		96(%rsp), %xmm15
	addq		$120, %rsp
#endif
	ret

NONEXEC_STACK

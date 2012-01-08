/*
	synth_x86_64_s32: SSE optimized synth for x86-64 (s32 output version)

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

#define XMMREG_SCALE %xmm15  /* {65536.0, 65536.0, 65536.0, 65536.0} */
#define XMMREG_MAX %xmm14  /* {32767.999, 32767.999, 32767.999, 32767.999} */
#define XMMREG_MIN %xmm13  /* {-32768.0, -32768.0, -32768.0, -32768.0} */
#define XMMREG_CLIP %xmm12

/*
	int synth_1to1_s32_x86_64_asm(real *window, real *b0, int32_t *samples, int bo1);
	return value: number of clipped samples
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(scale_s32):
	.long   1199570944
	.long   1199570944
	.long   1199570944
	.long   1199570944
	ALIGN16
ASM_NAME(maxmin_s32):
	.long   1191182335
	.long   1191182335
	.long   1191182335
	.long   1191182335
	.long   -956301312
	.long   -956301312
	.long   -956301312
	.long   -956301312
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_s32_x86_64_asm)
ASM_NAME(synth_1to1_s32_x86_64_asm):
#ifdef _WIN64 /* should save xmm6-15 */
	movq		%rcx, ARG0
	subq		$168, %rsp /* stack alignment + 10 xmm registers */
	movaps		%xmm6, (%rsp)
	movaps		%xmm7, 16(%rsp)
	movaps		%xmm8, 32(%rsp)
	movaps		%xmm9, 48(%rsp)
	movaps		%xmm10, 64(%rsp)
	movaps		%xmm11, 80(%rsp)
	movaps		%xmm12, 96(%rsp)
	movaps		%xmm13, 112(%rsp)
	movaps		%xmm14, 128(%rsp)
	movaps		%xmm15, 144(%rsp)
#endif
	
	leaq		ASM_NAME(scale_s32)(%rip), %rax
	movaps		(%rax), XMMREG_SCALE
	leaq		ASM_NAME(maxmin_s32)(%rip), %rax
	movaps		(%rax), XMMREG_MAX
	movaps		16(%rax), XMMREG_MIN
	
	xorps		XMMREG_CLIP, XMMREG_CLIP
	
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
	movaps		%xmm0, %xmm3
	movaps		%xmm0, %xmm4
	mulps		XMMREG_SCALE, %xmm0
	cmpnleps	XMMREG_MAX, %xmm3
	cmpltps		XMMREG_MIN, %xmm4
	cvtps2dq	%xmm0, %xmm0
	xorps		%xmm3, %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (ARG2)
	movups		%xmm2, 16(ARG2)
	
	psrld		$31, %xmm3
	psrld		$31, %xmm4
	paddd		%xmm4, %xmm3
	paddd		%xmm3, XMMREG_CLIP
	
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
	movaps		%xmm0, %xmm3
	movaps		%xmm0, %xmm4
	mulps		XMMREG_SCALE, %xmm0
	cmpnleps	XMMREG_MAX, %xmm3
	cmpltps		XMMREG_MIN, %xmm4
	cvtps2dq	%xmm0, %xmm0
	xorps		%xmm3, %xmm0
	shufps		$0xdd, %xmm2, %xmm1
	movaps		%xmm0, %xmm2
	unpcklps	%xmm1, %xmm0
	unpckhps	%xmm1, %xmm2
	movups		%xmm0, (ARG2)
	movups		%xmm2, 16(ARG2)
	
	psrld		$31, %xmm3
	psrld		$31, %xmm4
	paddd		%xmm4, %xmm3
	paddd		%xmm3, XMMREG_CLIP
	
	leaq		32(ARG2), ARG2
	decl		%ecx
	jnz			Loop_start_2
	
	pshuflw		$0xee, XMMREG_CLIP, %xmm0
	movhlps		XMMREG_CLIP, %xmm1
	pshuflw		$0xee, %xmm1, %xmm2
	paddd		%xmm0, XMMREG_CLIP
	paddd		%xmm1, XMMREG_CLIP
	paddd		%xmm2, XMMREG_CLIP
	
	movd		XMMREG_CLIP, %eax
	
#ifdef _WIN64
	movaps		(%rsp), %xmm6
	movaps		16(%rsp), %xmm7
	movaps		32(%rsp), %xmm8
	movaps		48(%rsp), %xmm9
	movaps		64(%rsp), %xmm10
	movaps		80(%rsp), %xmm11
	movaps		96(%rsp), %xmm12
	movaps		112(%rsp), %xmm13
	movaps		128(%rsp), %xmm14
	movaps		144(%rsp), %xmm15
	addq		$168, %rsp
#endif
	ret

NONEXEC_STACK

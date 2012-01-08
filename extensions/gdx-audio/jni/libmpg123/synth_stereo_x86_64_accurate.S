/*
	synth_stereo_x86_64_accurate: SSE optimized synth for x86-64 (stereo specific, MPEG-compliant 16bit output version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#ifdef _WIN64
/* short *window; */
#define WINDOW %rsi
/* short *b0l; */
#define B0L %rdx
/* short *b0r; */
#define B0R %r8
/* short *samples; */
#define SAMPLES %r9
#else
/* real *window; */
#define WINDOW %rdi
/* real *b0l; */
#define B0L %rsi
/* real *b0r; */
#define B0R %rdx
/* real *samples; */
#define SAMPLES %r8
#endif

#define XMMREG_MAX (%r10)  /* {32767.0, 32767.0, 32767.0, 32767.0} */
#define XMMREG_MIN (%r11)  /* {-32768.0, -32768.0, -32768.0, -32768.0} */
#define TEMP_CLIP (%rsp)

/*
	int synth_1to1_s_x86_64_accurate_asm(real *window, real *b0l, real *b0r, short *samples, int bo1);
	return value: number of clipped samples
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(maxmin_s16):
	.long   1191181824
	.long   1191181824
	.long   1191181824
	.long   1191181824
	.long   -956301312
	.long   -956301312
	.long   -956301312
	.long   -956301312
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_s_x86_64_accurate_asm)
ASM_NAME(synth_1to1_s_x86_64_accurate_asm):
#ifdef _WIN64 /* should save xmm6-15 */
	movl		40(%rsp), %eax /* 5th argument; placed after 32-byte shadow space */
	pushq		%rsi
	subq		$176, %rsp /* 10 xmm registers + temp */
	movaps		%xmm6, 16(%rsp)
	movaps		%xmm7, 32(%rsp)
	movaps		%xmm8, 48(%rsp)
	movaps		%xmm9, 64(%rsp)
	movaps		%xmm10, 80(%rsp)
	movaps		%xmm11, 96(%rsp)
	movaps		%xmm12, 112(%rsp)
	movaps		%xmm13, 128(%rsp)
	movaps		%xmm14, 144(%rsp)
	movaps		%xmm15, 160(%rsp)
#else
	subq		$24, %rsp /* stack alignment + temp */
#endif
	
	leaq		ASM_NAME(maxmin_s16)(%rip), %r10
	leaq		16(%r10), %r11
	xorps		%xmm0, %xmm0
	movaps		%xmm0, TEMP_CLIP
	
#ifdef _WIN64
	shlq		$32, %rax
	shrq		$30, %rax
	movq		%rcx, %rbx
#else
	movq		%r8, %rax
	shlq		$32, %rax
	shrq		$30, %rax
	movq		%rcx, %r8
#endif
	leaq		64(WINDOW), WINDOW
	subq		%rax, WINDOW

	movl		$4, %ecx
	
	ALIGN16
Loop_start_1:
	movups		(WINDOW), %xmm8
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm9
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	movaps		%xmm8, %xmm0
	movaps		%xmm1, %xmm4
	movaps		%xmm2, %xmm10
	movaps		%xmm3, %xmm11
	movaps		%xmm9, %xmm12
	movaps		%xmm5, %xmm13
	movaps		%xmm6, %xmm14
	movaps		%xmm7, %xmm15
	mulps		(B0L), %xmm8
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		64(B0L), %xmm9
	mulps		80(B0L), %xmm5
	mulps		96(B0L), %xmm6
	mulps		112(B0L), %xmm7
	mulps		(B0R), %xmm0
	mulps		16(B0R), %xmm4
	mulps		32(B0R), %xmm10
	mulps		48(B0R), %xmm11
	mulps		64(B0R), %xmm12
	mulps		80(B0R), %xmm13
	mulps		96(B0R), %xmm14
	mulps		112(B0R), %xmm15
	
	addps		%xmm1, %xmm8
	addps		%xmm2, %xmm3
	addps		%xmm4, %xmm0
	addps		%xmm11, %xmm10
	addps		%xmm5, %xmm9
	addps		%xmm7, %xmm6
	addps		%xmm13, %xmm12
	addps		%xmm15, %xmm14
	addps		%xmm3, %xmm8
	addps		%xmm6, %xmm9
	addps		%xmm10, %xmm0
	addps		%xmm12, %xmm14
	movaps		%xmm0, %xmm12
	movaps		%xmm14, %xmm13
	leaq		256(WINDOW), WINDOW
	leaq		128(B0L), B0L
	leaq		128(B0R), B0R
	
	movups		(WINDOW), %xmm10
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm11
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	movaps		%xmm10, %xmm0
	movaps		%xmm1, %xmm4
	movaps		%xmm2, %xmm14
	movaps		%xmm3, %xmm15
	mulps		(B0L), %xmm10
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		(B0R), %xmm0
	mulps		16(B0R), %xmm4
	mulps		32(B0R), %xmm14
	mulps		48(B0R), %xmm15
	addps		%xmm1, %xmm10
	addps		%xmm2, %xmm3
	addps		%xmm4, %xmm0
	addps		%xmm15, %xmm14
	movaps		%xmm11, %xmm1
	movaps		%xmm5, %xmm2
	movaps		%xmm6, %xmm4
	movaps		%xmm7, %xmm15
	mulps		64(B0L), %xmm11
	mulps		80(B0L), %xmm5
	mulps		96(B0L), %xmm6
	mulps		112(B0L), %xmm7
	mulps		64(B0R), %xmm1
	mulps		80(B0R), %xmm2
	mulps		96(B0R), %xmm4
	mulps		112(B0R), %xmm15
	addps		%xmm5, %xmm11
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm1
	addps		%xmm15, %xmm4
	
	addps		%xmm3, %xmm10
	addps		%xmm6, %xmm11
	addps		%xmm0, %xmm14
	addps		%xmm4, %xmm1
	movaps		%xmm1, %xmm15
	leaq		256(WINDOW), WINDOW
	leaq		128(B0L), B0L
	leaq		128(B0R), B0R
	
	movaps		%xmm8, %xmm0
	movaps		%xmm10, %xmm1
	movaps		%xmm12, %xmm4
	movaps		%xmm14, %xmm5
	unpcklps	%xmm9, %xmm8
	unpcklps	%xmm11, %xmm10
	unpckhps	%xmm9, %xmm0
	unpckhps	%xmm11, %xmm1
	unpcklps	%xmm13, %xmm12
	unpcklps	%xmm15, %xmm14
	unpckhps	%xmm13, %xmm4
	unpckhps	%xmm15, %xmm5
	movaps		%xmm8, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm12, %xmm6
	movaps		%xmm4, %xmm7
	movlhps		%xmm10, %xmm8
	movhlps		%xmm2, %xmm10
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	movlhps		%xmm14, %xmm12
	movhlps		%xmm6, %xmm14
	movlhps		%xmm5, %xmm4
	movhlps		%xmm7, %xmm5
	subps		%xmm10, %xmm8
	subps		%xmm1, %xmm0
	subps		%xmm14, %xmm12
	subps		%xmm5, %xmm4
	addps		%xmm8, %xmm0
	addps		%xmm12, %xmm4
	
	movaps		%xmm0, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm4, %xmm5
	movaps		%xmm4, %xmm6
	cmpnleps	XMMREG_MAX, %xmm2
	cmpltps		XMMREG_MIN, %xmm3
	cmpnleps	XMMREG_MAX, %xmm5
	cmpltps		XMMREG_MIN, %xmm6
	cvtps2dq	%xmm0, %xmm0
	cvtps2dq	%xmm4, %xmm4
	movaps		%xmm0, %xmm1
	unpcklps	%xmm4, %xmm0
	unpckhps	%xmm4, %xmm1
	packssdw	%xmm1, %xmm0
	movups		%xmm0, (SAMPLES)
	
	packssdw	%xmm5, %xmm2
	packssdw	%xmm6, %xmm3
	psrlw		$15, %xmm2
	psrlw		$15, %xmm3
	paddw		%xmm3, %xmm2
	paddw		TEMP_CLIP, %xmm2
	movaps		%xmm2, TEMP_CLIP
	
	leaq		16(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_1
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_2:
	movups		(WINDOW), %xmm8
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm9
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	movaps		%xmm8, %xmm0
	movaps		%xmm1, %xmm4
	movaps		%xmm2, %xmm10
	movaps		%xmm3, %xmm11
	movaps		%xmm9, %xmm12
	movaps		%xmm5, %xmm13
	movaps		%xmm6, %xmm14
	movaps		%xmm7, %xmm15
	mulps		(B0L), %xmm8
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		-64(B0L), %xmm9
	mulps		-48(B0L), %xmm5
	mulps		-32(B0L), %xmm6
	mulps		-16(B0L), %xmm7
	mulps		(B0R), %xmm0
	mulps		16(B0R), %xmm4
	mulps		32(B0R), %xmm10
	mulps		48(B0R), %xmm11
	mulps		-64(B0R), %xmm12
	mulps		-48(B0R), %xmm13
	mulps		-32(B0R), %xmm14
	mulps		-16(B0R), %xmm15
	
	addps		%xmm1, %xmm8
	addps		%xmm2, %xmm3
	addps		%xmm4, %xmm0
	addps		%xmm11, %xmm10
	addps		%xmm5, %xmm9
	addps		%xmm7, %xmm6
	addps		%xmm13, %xmm12
	addps		%xmm15, %xmm14
	addps		%xmm3, %xmm8
	addps		%xmm6, %xmm9
	addps		%xmm10, %xmm0
	addps		%xmm12, %xmm14
	movaps		%xmm0, %xmm12
	movaps		%xmm14, %xmm13
	leaq		256(WINDOW), WINDOW
	leaq		-128(B0L), B0L
	leaq		-128(B0R), B0R
	
	movups		(WINDOW), %xmm10
	movups		16(WINDOW), %xmm1
	movups		32(WINDOW), %xmm2
	movups		48(WINDOW), %xmm3
	movups		128(WINDOW), %xmm11
	movups		144(WINDOW), %xmm5
	movups		160(WINDOW), %xmm6
	movups		176(WINDOW), %xmm7
	movaps		%xmm10, %xmm0
	movaps		%xmm1, %xmm4
	movaps		%xmm2, %xmm14
	movaps		%xmm3, %xmm15
	mulps		(B0L), %xmm10
	mulps		16(B0L), %xmm1
	mulps		32(B0L), %xmm2
	mulps		48(B0L), %xmm3
	mulps		(B0R), %xmm0
	mulps		16(B0R), %xmm4
	mulps		32(B0R), %xmm14
	mulps		48(B0R), %xmm15
	addps		%xmm1, %xmm10
	addps		%xmm2, %xmm3
	addps		%xmm4, %xmm0
	addps		%xmm15, %xmm14
	movaps		%xmm11, %xmm1
	movaps		%xmm5, %xmm2
	movaps		%xmm6, %xmm4
	movaps		%xmm7, %xmm15
	mulps		-64(B0L), %xmm11
	mulps		-48(B0L), %xmm5
	mulps		-32(B0L), %xmm6
	mulps		-16(B0L), %xmm7
	mulps		-64(B0R), %xmm1
	mulps		-48(B0R), %xmm2
	mulps		-32(B0R), %xmm4
	mulps		-16(B0R), %xmm15
	addps		%xmm5, %xmm11
	addps		%xmm7, %xmm6
	addps		%xmm2, %xmm1
	addps		%xmm15, %xmm4
	
	addps		%xmm3, %xmm10
	addps		%xmm6, %xmm11
	addps		%xmm0, %xmm14
	addps		%xmm4, %xmm1
	movaps		%xmm1, %xmm15
	leaq		256(WINDOW), WINDOW
	leaq		-128(B0L), B0L
	leaq		-128(B0R), B0R
	
	movaps		%xmm8, %xmm0
	movaps		%xmm10, %xmm1
	movaps		%xmm12, %xmm4
	movaps		%xmm14, %xmm5
	unpcklps	%xmm9, %xmm8
	unpcklps	%xmm11, %xmm10
	unpckhps	%xmm9, %xmm0
	unpckhps	%xmm11, %xmm1
	unpcklps	%xmm13, %xmm12
	unpcklps	%xmm15, %xmm14
	unpckhps	%xmm13, %xmm4
	unpckhps	%xmm15, %xmm5
	movaps		%xmm8, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm12, %xmm6
	movaps		%xmm4, %xmm7
	movlhps		%xmm10, %xmm8
	movhlps		%xmm2, %xmm10
	movlhps		%xmm1, %xmm0
	movhlps		%xmm3, %xmm1
	movlhps		%xmm14, %xmm12
	movhlps		%xmm6, %xmm14
	movlhps		%xmm5, %xmm4
	movhlps		%xmm7, %xmm5
	addps		%xmm10, %xmm8
	addps		%xmm1, %xmm0
	addps		%xmm14, %xmm12
	addps		%xmm5, %xmm4
	addps		%xmm8, %xmm0
	addps		%xmm12, %xmm4
	
	movaps		%xmm0, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm4, %xmm5
	movaps		%xmm4, %xmm6
	cmpnleps	XMMREG_MAX, %xmm2
	cmpltps		XMMREG_MIN, %xmm3
	cmpnleps	XMMREG_MAX, %xmm5
	cmpltps		XMMREG_MIN, %xmm6
	cvtps2dq	%xmm0, %xmm0
	cvtps2dq	%xmm4, %xmm4
	movaps		%xmm0, %xmm1
	unpcklps	%xmm4, %xmm0
	unpckhps	%xmm4, %xmm1
	packssdw	%xmm1, %xmm0
	movups		%xmm0, (SAMPLES)
	
	packssdw	%xmm5, %xmm2
	packssdw	%xmm6, %xmm3
	psrlw		$15, %xmm2
	psrlw		$15, %xmm3
	paddw		%xmm3, %xmm2
	paddw		TEMP_CLIP, %xmm2
	movaps		%xmm2, TEMP_CLIP
	
	leaq		16(SAMPLES), SAMPLES
	decl		%ecx
	jnz			Loop_start_2
	
	movaps		TEMP_CLIP, %xmm4
	movhlps		%xmm4, %xmm0
	paddw		%xmm4, %xmm0
	pshuflw		$0x55, %xmm0, %xmm1
	pshuflw		$0xaa, %xmm0, %xmm2
	pshuflw		$0xff, %xmm0, %xmm3
	paddw		%xmm1, %xmm0
	paddw		%xmm2, %xmm0
	paddw		%xmm3, %xmm0
	
	movd		%xmm0, %eax
	andl		$0xffff, %eax
	
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
	addq		$176, %rsp
	popq		%rsi
#else
	addq		$24, %rsp
#endif
	ret

NONEXEC_STACK

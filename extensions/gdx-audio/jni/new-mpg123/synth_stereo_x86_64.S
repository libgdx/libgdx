/*
	synth_stereo_x86_64: SSE optimized synth for x86-64 (stereo specific version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#ifdef _WIN64
/* short *window; */
#define WINDOW %r10
/* short *b0l; */
#define B0L %rdx
/* short *b0r; */
#define B0R %r8
/* short *samples; */
#define SAMPLES %r9
#else
/* short *window; */
#define WINDOW %rdi
/* short *b0l; */
#define B0L %rsi
/* short *b0r; */
#define B0R %rdx
/* short *samples; */
#define SAMPLES %r9
#endif

#define XMMREG_CLIP %xmm15
#define XMMREG_MAX %xmm14 /* {32767, 32767, 32767, 32767} */
#define XMMREG_MIN %xmm13 /* {-32769, -32769, -32769, -32769} : not -32768 because SSE doesn't have "less than" comparison... */
#define XMMREG_FULL %xmm12 /* {0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF} */

/*
	int synth_1to1_s_x86_64_asm(short *window, short *b0l, short *b0r, short *samples, int bo1);
	return value: number of clipped samples
*/

#ifndef __APPLE__
	.section	.rodata
#else
	.data
#endif
	ALIGN32
ASM_NAME(maxmin_x86_64):
	.long   32767
	.long   32767
	.long   32767
	.long   32767
	.long   -32769
	.long   -32769
	.long   -32769
	.long   -32769
	.text
	ALIGN16
.globl ASM_NAME(synth_1to1_s_x86_64_asm)
ASM_NAME(synth_1to1_s_x86_64_asm):
#ifdef _WIN64 /* should save xmm6-15 */
	movl		40(%rsp), %eax /* 5th argument; placed after 32-byte shadow space */
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
	
#ifdef _WIN64
	shlq		$32, %rax
	shrq		$31, %rax
	movq		%rcx, %r10
#else
	movq		%r8, %rax
	shlq		$32, %rax
	shrq		$31, %rax
	movq		%rcx, %r9
#endif
	leaq		32(WINDOW), WINDOW
	subq		%rax, WINDOW
	
	leaq		ASM_NAME(maxmin_x86_64)(%rip), %rax
	movaps		(%rax), XMMREG_MAX
	movaps		16(%rax), XMMREG_MIN
	pxor		XMMREG_CLIP, XMMREG_CLIP
	pcmpeqd		XMMREG_FULL, XMMREG_FULL
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_1:
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		64(WINDOW), %xmm2
	movups		80(WINDOW), %xmm3
	movups		128(WINDOW), %xmm4
	movups		144(WINDOW), %xmm5
	movups		192(WINDOW), %xmm6
	movups		208(WINDOW), %xmm7
	movaps		%xmm0, %xmm8
	movaps		%xmm1, %xmm9
	movaps		%xmm2, %xmm10
	movaps		%xmm3, %xmm11
	pmaddwd		(B0L), %xmm0
	pmaddwd		16(B0L), %xmm1
	pmaddwd		32(B0L), %xmm2
	pmaddwd		48(B0L), %xmm3
	pmaddwd		(B0R), %xmm8
	pmaddwd		16(B0R), %xmm9
	pmaddwd		32(B0R), %xmm10
	pmaddwd		48(B0R), %xmm11
	paddd		%xmm1, %xmm0
	paddd		%xmm3, %xmm2
	paddd		%xmm9, %xmm8
	paddd		%xmm11, %xmm10
	movaps		%xmm4, %xmm1
	movaps		%xmm5, %xmm9
	movaps		%xmm6, %xmm3
	movaps		%xmm7, %xmm11
	pmaddwd		64(B0L), %xmm4
	pmaddwd		80(B0L), %xmm5
	pmaddwd		96(B0L), %xmm6
	pmaddwd		112(B0L), %xmm7
	pmaddwd		64(B0R), %xmm1
	pmaddwd		80(B0R), %xmm9
	pmaddwd		96(B0R), %xmm3
	pmaddwd		112(B0R), %xmm11
	paddd		%xmm5, %xmm4
	paddd		%xmm7, %xmm6
	paddd		%xmm1, %xmm9
	paddd		%xmm3, %xmm11
	
	movaps		%xmm0, %xmm1
	movaps		%xmm4, %xmm3
	movaps		%xmm8, %xmm5
	movaps		%xmm9, %xmm7
	punpckldq	%xmm2, %xmm0
	punpckldq	%xmm6, %xmm4
	punpckhdq	%xmm2, %xmm1
	punpckhdq	%xmm6, %xmm3
	punpckldq	%xmm10, %xmm8
	punpckldq	%xmm11, %xmm9
	punpckhdq	%xmm10, %xmm5
	punpckhdq	%xmm11, %xmm7
	movaps		%xmm0, %xmm2
	movaps		%xmm1, %xmm6
	movaps		%xmm8, %xmm10
	movaps		%xmm5, %xmm11
	movlhps		%xmm4, %xmm0
	movhlps		%xmm2, %xmm4
	movlhps		%xmm3, %xmm1
	movhlps		%xmm6, %xmm3
	movlhps		%xmm9, %xmm8
	movhlps		%xmm10, %xmm9
	movlhps		%xmm7, %xmm5
	movhlps		%xmm11, %xmm7
	paddd		%xmm4, %xmm0
	paddd		%xmm3, %xmm1
	paddd		%xmm9, %xmm8
	paddd		%xmm7, %xmm5
	paddd		%xmm1, %xmm0
	paddd		%xmm5, %xmm8
	psrad		$13, %xmm0
	psrad		$13, %xmm8
	
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm8, %xmm4
	punpckldq	%xmm8, %xmm0
	punpckhdq	%xmm8, %xmm1
	packssdw	%xmm1, %xmm0
	movups		%xmm0, (SAMPLES)
	
	pcmpgtd		XMMREG_MAX, %xmm2
	pcmpgtd		XMMREG_MIN, %xmm3
	pcmpgtd		XMMREG_MAX, %xmm4
	pcmpgtd		XMMREG_MIN, %xmm8
	packssdw	%xmm4, %xmm2
	packssdw	%xmm8, %xmm3
	pxor		XMMREG_FULL, %xmm3
	psrlw		$15, %xmm2
	psrlw		$15, %xmm3
	paddw		%xmm3, %xmm2
	paddw		%xmm2, XMMREG_CLIP
	
	leaq		256(WINDOW), WINDOW
	leaq		128(B0L), B0L
	leaq		128(B0R), B0R
	leaq		16(SAMPLES), SAMPLES
	
	decl		%ecx
	jnz			Loop_start_1
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_2:
	movups		(WINDOW), %xmm0
	movups		16(WINDOW), %xmm1
	movups		64(WINDOW), %xmm2
	movups		80(WINDOW), %xmm3
	movups		128(WINDOW), %xmm4
	movups		144(WINDOW), %xmm5
	movups		192(WINDOW), %xmm6
	movups		208(WINDOW), %xmm7
	movaps		%xmm0, %xmm8
	movaps		%xmm1, %xmm9
	movaps		%xmm2, %xmm10
	movaps		%xmm3, %xmm11
	pmaddwd		(B0L), %xmm0
	pmaddwd		16(B0L), %xmm1
	pmaddwd		-32(B0L), %xmm2
	pmaddwd		-16(B0L), %xmm3
	pmaddwd		(B0R), %xmm8
	pmaddwd		16(B0R), %xmm9
	pmaddwd		-32(B0R), %xmm10
	pmaddwd		-16(B0R), %xmm11
	paddd		%xmm1, %xmm0
	paddd		%xmm3, %xmm2
	paddd		%xmm9, %xmm8
	paddd		%xmm11, %xmm10
	movaps		%xmm4, %xmm1
	movaps		%xmm5, %xmm9
	movaps		%xmm6, %xmm3
	movaps		%xmm7, %xmm11
	pmaddwd		-64(B0L), %xmm4
	pmaddwd		-48(B0L), %xmm5
	pmaddwd		-96(B0L), %xmm6
	pmaddwd		-80(B0L), %xmm7
	pmaddwd		-64(B0R), %xmm1
	pmaddwd		-48(B0R), %xmm9
	pmaddwd		-96(B0R), %xmm3
	pmaddwd		-80(B0R), %xmm11
	paddd		%xmm5, %xmm4
	paddd		%xmm7, %xmm6
	paddd		%xmm1, %xmm9
	paddd		%xmm3, %xmm11
	
	movaps		%xmm0, %xmm1
	movaps		%xmm4, %xmm3
	movaps		%xmm8, %xmm5
	movaps		%xmm9, %xmm7
	punpckldq	%xmm2, %xmm0
	punpckldq	%xmm6, %xmm4
	punpckhdq	%xmm2, %xmm1
	punpckhdq	%xmm6, %xmm3
	punpckldq	%xmm10, %xmm8
	punpckldq	%xmm11, %xmm9
	punpckhdq	%xmm10, %xmm5
	punpckhdq	%xmm11, %xmm7
	movaps		%xmm0, %xmm2
	movaps		%xmm1, %xmm6
	movaps		%xmm8, %xmm10
	movaps		%xmm5, %xmm11
	movlhps		%xmm4, %xmm0
	movhlps		%xmm2, %xmm4
	movlhps		%xmm3, %xmm1
	movhlps		%xmm6, %xmm3
	movlhps		%xmm9, %xmm8
	movhlps		%xmm10, %xmm9
	movlhps		%xmm7, %xmm5
	movhlps		%xmm11, %xmm7
	paddd		%xmm4, %xmm0
	paddd		%xmm3, %xmm1
	paddd		%xmm9, %xmm8
	paddd		%xmm7, %xmm5
	paddd		%xmm1, %xmm0
	paddd		%xmm5, %xmm8
	psrad		$13, %xmm0
	psrad		$13, %xmm8
	
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	movaps		%xmm0, %xmm3
	movaps		%xmm8, %xmm4
	punpckldq	%xmm8, %xmm0
	punpckhdq	%xmm8, %xmm1
	packssdw	%xmm1, %xmm0
	movups		%xmm0, (SAMPLES)
	
	pcmpgtd		XMMREG_MAX, %xmm2
	pcmpgtd		XMMREG_MIN, %xmm3
	pcmpgtd		XMMREG_MAX, %xmm4
	pcmpgtd		XMMREG_MIN, %xmm8
	packssdw	%xmm4, %xmm2
	packssdw	%xmm8, %xmm3
	pxor		XMMREG_FULL, %xmm3
	psrlw		$15, %xmm2
	psrlw		$15, %xmm3
	paddw		%xmm3, %xmm2
	paddw		%xmm2, XMMREG_CLIP
	
	leaq		256(WINDOW), WINDOW
	leaq		-128(B0L), B0L
	leaq		-128(B0R), B0R
	leaq		16(SAMPLES), SAMPLES
	
	decl		%ecx
	jnz			Loop_start_2
	
	movhlps		XMMREG_CLIP, %xmm0
	paddw		XMMREG_CLIP, %xmm0
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
	addq		$168, %rsp
#endif
	ret

NONEXEC_STACK

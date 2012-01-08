/*
	synth_x86_64: SSE optimized synth for x86-64

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
/* short *window; */
#define ARG0 %rdi
/* short *b0; */
#define ARG1 %rsi
/* short *samples; */
#define ARG2 %rdx
/* int bo1; */
#define ARG3 %rcx
#endif

#define XMMREG_CLIP %xmm15
#define XMMREG_MAX %xmm14 /* {32767, 32767, 32767, 32767} */
#define XMMREG_MIN %xmm13 /* {-32769, -32769, -32769, -32769} : not -32768 because SSE doesn't have "less than" comparison... */
#define XMMREG_FULL %xmm12 /* {0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF} */

/*
	int synth_1to1_x86_64_asm(short *window, short *b0, short *samples, int bo1);
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
.globl ASM_NAME(synth_1to1_x86_64_asm)
ASM_NAME(synth_1to1_x86_64_asm):
#ifdef _WIN64 /* should save xmm6-15 */
	movq		%rcx, ARG0
	subq		$104, %rsp /* stack alignment + 6 xmm registers */
	movaps		%xmm6, (%rsp)
	movaps		%xmm7, 16(%rsp)
	movaps		%xmm12, 32(%rsp)
	movaps		%xmm13, 48(%rsp)
	movaps		%xmm14, 64(%rsp)
	movaps		%xmm15, 80(%rsp)
#endif

	leaq		ASM_NAME(maxmin_x86_64)(%rip), %rax
	movaps		(%rax), XMMREG_MAX
	movaps		16(%rax), XMMREG_MIN
	pxor		XMMREG_CLIP, XMMREG_CLIP
	pcmpeqd		XMMREG_FULL, XMMREG_FULL
	
	andq		$0xf, ARG3
	shlq		$1, ARG3
	leaq		32(ARG0), ARG0
	subq		ARG3, ARG0
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_1:
	movups		(ARG0), %xmm0
	movups		16(ARG0), %xmm1
	movups		64(ARG0), %xmm2
	movups		80(ARG0), %xmm3
	movups		128(ARG0), %xmm4
	movups		144(ARG0), %xmm5
	movups		192(ARG0), %xmm6
	movups		208(ARG0), %xmm7
	pmaddwd		(ARG1), %xmm0
	pmaddwd		16(ARG1), %xmm1
	pmaddwd		32(ARG1), %xmm2
	pmaddwd		48(ARG1), %xmm3
	pmaddwd		64(ARG1), %xmm4
	pmaddwd		80(ARG1), %xmm5
	pmaddwd		96(ARG1), %xmm6
	pmaddwd		112(ARG1), %xmm7
	
	paddd		%xmm1, %xmm0
	paddd		%xmm3, %xmm2
	paddd		%xmm5, %xmm4
	paddd		%xmm7, %xmm6
	
	movaps		%xmm0, %xmm1
	movaps		%xmm4, %xmm3
	punpckldq	%xmm2, %xmm0
	punpckldq	%xmm6, %xmm4
	punpckhdq	%xmm2, %xmm1
	punpckhdq	%xmm6, %xmm3
	movaps		%xmm0, %xmm5
	movaps		%xmm1, %xmm7
	movlhps		%xmm4, %xmm0
	movhlps		%xmm5, %xmm4
	movlhps		%xmm3, %xmm1
	movhlps		%xmm7, %xmm3
	paddd		%xmm4, %xmm0
	paddd		%xmm3, %xmm1
	paddd		%xmm1, %xmm0
	psrad		$13, %xmm0
	
	movups		(ARG2), %xmm3
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	packssdw	%xmm0, %xmm0
	pcmpgtd		XMMREG_MAX, %xmm1
	pcmpgtd		XMMREG_MIN, %xmm2
	movhlps		%xmm3, %xmm4
	pshuflw		$0xdd, %xmm3, %xmm3
	pshuflw		$0xdd, %xmm4, %xmm4
	psrlq		$32, %xmm3
	psllq		$32, %xmm4
	por			%xmm4, %xmm3
	punpcklwd	%xmm3, %xmm0
	movups		%xmm0, (ARG2)
	
	pxor		XMMREG_FULL, %xmm2
	psrld		$31, %xmm1
	psrld		$31, %xmm2
	paddd		%xmm2, %xmm1
	paddd		%xmm1, XMMREG_CLIP
	
	leaq		256(ARG0), ARG0
	leaq		128(ARG1), ARG1
	leaq		16(ARG2), ARG2
	
	decl		%ecx
	jnz			Loop_start_1
	
	movl		$4, %ecx
	
	ALIGN16
Loop_start_2:
	movups		(ARG0), %xmm0
	movups		16(ARG0), %xmm1
	movups		64(ARG0), %xmm2
	movups		80(ARG0), %xmm3
	movups		128(ARG0), %xmm4
	movups		144(ARG0), %xmm5
	movups		192(ARG0), %xmm6
	movups		208(ARG0), %xmm7
	pmaddwd		(ARG1), %xmm0
	pmaddwd		16(ARG1), %xmm1
	pmaddwd		-32(ARG1), %xmm2
	pmaddwd		-16(ARG1), %xmm3
	pmaddwd		-64(ARG1), %xmm4
	pmaddwd		-48(ARG1), %xmm5
	pmaddwd		-96(ARG1), %xmm6
	pmaddwd		-80(ARG1), %xmm7
	
	paddd		%xmm1, %xmm0
	paddd		%xmm3, %xmm2
	paddd		%xmm5, %xmm4
	paddd		%xmm7, %xmm6
	
	movaps		%xmm0, %xmm1
	movaps		%xmm4, %xmm3
	punpckldq	%xmm2, %xmm0
	punpckldq	%xmm6, %xmm4
	punpckhdq	%xmm2, %xmm1
	punpckhdq	%xmm6, %xmm3
	movaps		%xmm0, %xmm5
	movaps		%xmm1, %xmm7
	movlhps		%xmm4, %xmm0
	movhlps		%xmm5, %xmm4
	movlhps		%xmm3, %xmm1
	movhlps		%xmm7, %xmm3
	paddd		%xmm4, %xmm0
	paddd		%xmm3, %xmm1
	paddd		%xmm1, %xmm0
	psrad		$13, %xmm0
	
	movups		(ARG2), %xmm3
	movaps		%xmm0, %xmm1
	movaps		%xmm0, %xmm2
	packssdw	%xmm0, %xmm0
	pcmpgtd		XMMREG_MAX, %xmm1
	pcmpgtd		XMMREG_MIN, %xmm2
	movhlps		%xmm3, %xmm4
	pshuflw		$0xdd, %xmm3, %xmm3
	pshuflw		$0xdd, %xmm4, %xmm4
	psrlq		$32, %xmm3
	psllq		$32, %xmm4
	por			%xmm4, %xmm3
	punpcklwd	%xmm3, %xmm0
	movups		%xmm0, (ARG2)
	
	pxor		XMMREG_FULL, %xmm2
	psrld		$31, %xmm1
	psrld		$31, %xmm2
	paddd		%xmm2, %xmm1
	paddd		%xmm1, XMMREG_CLIP
	
	leaq		256(ARG0), ARG0
	leaq		-128(ARG1), ARG1
	leaq		16(ARG2), ARG2
	
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
	movaps		32(%rsp), %xmm12
	movaps		48(%rsp), %xmm13
	movaps		64(%rsp), %xmm14
	movaps		80(%rsp), %xmm15
	addq		$104, %rsp
#endif
	ret

NONEXEC_STACK

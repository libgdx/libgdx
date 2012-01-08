/*
	synth_arm: ARM optimized synth

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define WINDOW r0
#define B0 r1
#define SAMPLES r2
#define REG_CLIP r4
#define REG_MAX r12

/*
	int synth_1to1_arm_asm(real *window, real *b0, short *samples, int bo1);
	return value: number of clipped samples
*/

	.text
	ALIGN4
	.globl ASM_NAME(synth_1to1_arm_asm)
ASM_NAME(synth_1to1_arm_asm):
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, lr}
	
	add		WINDOW, WINDOW, #64
	sub		WINDOW, WINDOW, r3, lsl #2
	eor		REG_CLIP, REG_CLIP, REG_CLIP
	mov		REG_MAX, #1073741824
	sub		REG_MAX, REG_MAX, #32768
	
	mov		r3, #16
	
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
.Loop_start_1:
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mul		r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mul		r10, r8, r9
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #68
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	
	sub		r7, r7, r10
	
	cmp		r7, REG_MAX
	movgt	r7, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r7, #-1073741824
	movlt	r7, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r7, r7, asr #15
	adc		r7, r7, #0
	strh	r7, [SAMPLES], #4
	
	subs	r3, r3, #1
	bne		.Loop_start_1
	
	add		WINDOW, WINDOW, #4
	add		B0, B0, #4
	
	ldr		r8, [WINDOW], #8
	ldr		r9, [B0], #8
	mul		r7, r5, r6
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	mul		r10, r8, r9
	ldr		r8, [WINDOW], #8
	ldr		r9, [B0], #8
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #8
	ldr		r9, [B0], #8
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #72
	ldr		r9, [B0], #-120
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	
	add		r7, r7, r10
	
	cmp		r7, REG_MAX
	movgt	r7, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r7, #-1073741824
	movlt	r7, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r7, r7, asr #15
	adc		r7, r7, #0
	strh	r7, [SAMPLES], #4
	
	mov		r3, #14
	
.Loop_start_2:
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mul		r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mul		r10, r8, r9
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #68
	ldr		r9, [B0], #-124
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	
	add		r7, r7, r10
	
	cmp		r7, REG_MAX
	movgt	r7, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r7, #-1073741824
	movlt	r7, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r7, r7, asr #15
	adc		r7, r7, #0
	strh	r7, [SAMPLES], #4
	
	subs	r3, r3, #1
	bne		.Loop_start_2
	
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mul		r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mul		r10, r8, r9
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW], #4
	ldr		r9, [B0], #4
	mla		r7, r5, r6, r7
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	mla		r10, r8, r9, r10
	ldr		r8, [WINDOW]
	ldr		r9, [B0]
	mla		r7, r5, r6, r7
	mla		r10, r8, r9, r10
	
	add		r7, r7, r10
	
	cmp		r7, REG_MAX
	movgt	r7, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r7, #-1073741824
	movlt	r7, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r7, r7, asr #15
	adc		r7, r7, #0
	strh	r7, [SAMPLES]
	
	mov		r0, REG_CLIP
	
	ldmfd   sp!, {r4, r5, r6, r7, r8, r9, r10, pc}

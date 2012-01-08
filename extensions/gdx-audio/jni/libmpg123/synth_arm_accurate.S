/*
	synth_arm_accurate: ARM optimized synth (ISO compliant 16bit output version)

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define WINDOW r0
#define B0 r1
#define SAMPLES r2
#define REG_CLIP r4
#define REG_MAX r14

/*
	int synth_1to1_arm_accurate_asm(real *window, real *b0, short *samples, int bo1);
	return value: number of clipped samples
*/

	.text
	ALIGN4
	.globl ASM_NAME(synth_1to1_arm_accurate_asm)
ASM_NAME(synth_1to1_arm_accurate_asm):
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}
	
	add		WINDOW, WINDOW, #64
	sub		WINDOW, WINDOW, r3, lsl #2
	eor		REG_CLIP, REG_CLIP, REG_CLIP
	mov		REG_MAX, #1073741824
	sub		REG_MAX, REG_MAX, #32768
	
	mov		r3, #16
	
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
.Loop_start_1:
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smull	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smull	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #68
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	
	mov		r8, r8, lsr #24
	mov		r12, r12, lsr #24
	orr		r8, r8, r7, lsl #8
	orr		r12, r12, r11, lsl #8
	sub		r8, r8, r12
	
	cmp		r8, REG_MAX
	movgt	r8, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r8, #-1073741824
	movlt	r8, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r8, r8, asr #15
	adc		r8, r8, #0
	strh	r8, [SAMPLES], #4
	
	subs	r3, r3, #1
	bne		.Loop_start_1
	
	add		WINDOW, WINDOW, #4
	add		B0, B0, #4
	
	ldr		r9, [WINDOW], #8
	ldr		r10, [B0], #8
	smull	r8, r7, r5, r6
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	smull	r12, r11, r9, r10
	ldr		r9, [WINDOW], #8
	ldr		r10, [B0], #8
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #8
	ldr		r10, [B0], #8
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #8
	ldr		r6, [B0], #8
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #72
	ldr		r10, [B0], #-120
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	
	mov		r8, r8, lsr #24
	mov		r12, r12, lsr #24
	orr		r8, r8, r7, lsl #8
	orr		r12, r12, r11, lsl #8
	add		r8, r8, r12
	
	cmp		r8, REG_MAX
	movgt	r8, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r8, #-1073741824
	movlt	r8, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r8, r8, asr #15
	adc		r8, r8, #0
	strh	r8, [SAMPLES], #4
	
	mov		r3, #14
	
.Loop_start_2:
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smull	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smull	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #68
	ldr		r10, [B0], #-124
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	
	mov		r8, r8, lsr #24
	mov		r12, r12, lsr #24
	orr		r8, r8, r7, lsl #8
	orr		r12, r12, r11, lsl #8
	add		r8, r8, r12
	
	cmp		r8, REG_MAX
	movgt	r8, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r8, #-1073741824
	movlt	r8, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r8, r8, asr #15
	adc		r8, r8, #0
	strh	r8, [SAMPLES], #4
	
	subs	r3, r3, #1
	bne		.Loop_start_2
	
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smull	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smull	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #4
	ldr		r10, [B0], #4
	smlal	r8, r7, r5, r6
	ldr		r5, [WINDOW], #4
	ldr		r6, [B0], #4
	smlal	r12, r11, r9, r10
	ldr		r9, [WINDOW], #68
	ldr		r10, [B0], #-124
	smlal	r8, r7, r5, r6
	smlal	r12, r11, r9, r10
	
	mov		r8, r8, lsr #24
	mov		r12, r12, lsr #24
	orr		r8, r8, r7, lsl #8
	orr		r12, r12, r11, lsl #8
	add		r8, r8, r12
	
	cmp		r8, REG_MAX
	movgt	r8, REG_MAX
	addgt	REG_CLIP, REG_CLIP, #1
	cmp		r8, #-1073741824
	movlt	r8, #-1073741824
	addlt	REG_CLIP, REG_CLIP, #1
	movs	r8, r8, asr #15
	adc		r8, r8, #0
	strh	r8, [SAMPLES]
	
	mov		r0, REG_CLIP
	
	ldmfd   sp!, {r4, r5, r6, r7, r8, r9, r10, r11, pc}

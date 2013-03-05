/*
	synth_stereo_neon: ARM NEON optimized synth (stereo specific version)

	copyright 1995-2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define WINDOW r0
#define B0L r1
#define B0R r2
#define SAMPLES r3

/*
	int synth_1to1_s_neon_asm(short *window, short *b0l, short *b0r, short *samples, int bo1);
	return value: number of clipped samples
*/

	.text
	.globl ASM_NAME(synth_1to1_s_neon_asm)
	ALIGN4
ASM_NAME(synth_1to1_s_neon_asm):
	push		{r4-r6, lr}
	vpush		{q4-q7}

	ldr			r4, [sp, #80]
	add			WINDOW, WINDOW, #32
	sub			WINDOW, WINDOW, r4, lsl #1

	mov			r4, #4
	mov			r5, #64
.Loop_start_1:
	vld1.16		{d0-d3}, [WINDOW], r5
	vld1.16		{d4-d7}, [WINDOW], r5
	vld1.16		{d8-d11}, [B0L, :128]!
	vld1.16		{d12-d15}, [B0R, :128]!
	vld1.16		{d16-d19}, [B0L, :128]!
	vld1.16		{d20-d23}, [B0R, :128]!
	
	vmull.s16	q12, d0, d8
	vmull.s16	q13, d0, d12
	vmull.s16	q14, d4, d16
	vmull.s16	q15, d4, d20
	vmlal.s16	q12, d1, d9
	vmlal.s16	q13, d1, d13
	vmlal.s16	q14, d5, d17
	vmlal.s16	q15, d5, d21
	vmlal.s16	q12, d2, d10
	vmlal.s16	q13, d2, d14
	vmlal.s16	q14, d6, d18
	vmlal.s16	q15, d6, d22
	vmlal.s16	q12, d3, d11
	vmlal.s16	q13, d3, d15
	vmlal.s16	q14, d7, d19
	vmlal.s16	q15, d7, d23
	vpadd.i32	d24, d24, d25
	vpadd.i32	d26, d26, d27
	vpadd.i32	d28, d28, d29
	vpadd.i32	d30, d30, d31
	vpadd.i32	d24, d24, d26
	vpadd.i32	d25, d28, d30
	vqshrn.s32	d0, q12, #13
	vst1.16		{d0}, [SAMPLES]!
	
	vld1.16		{d0-d3}, [WINDOW], r5
	vld1.16		{d4-d7}, [WINDOW], r5
	vld1.16		{d8-d11}, [B0L, :128]!
	vld1.16		{d12-d15}, [B0R, :128]!
	vld1.16		{d16-d19}, [B0L, :128]!
	vld1.16		{d20-d23}, [B0R, :128]!
	
	vmull.s16	q12, d0, d8
	vmull.s16	q13, d0, d12
	vmull.s16	q14, d4, d16
	vmull.s16	q15, d4, d20
	vmlal.s16	q12, d1, d9
	vmlal.s16	q13, d1, d13
	vmlal.s16	q14, d5, d17
	vmlal.s16	q15, d5, d21
	vmlal.s16	q12, d2, d10
	vmlal.s16	q13, d2, d14
	vmlal.s16	q14, d6, d18
	vmlal.s16	q15, d6, d22
	vmlal.s16	q12, d3, d11
	vmlal.s16	q13, d3, d15
	vmlal.s16	q14, d7, d19
	vmlal.s16	q15, d7, d23
	vpadd.i32	d24, d24, d25
	vpadd.i32	d26, d26, d27
	vpadd.i32	d28, d28, d29
	vpadd.i32	d30, d30, d31
	vpadd.i32	d24, d24, d26
	vpadd.i32	d25, d28, d30
	vqshrn.s32	d0, q12, #13
	vst1.16		{d0}, [SAMPLES]!

	subs		r4, r4, #1
	bne			.Loop_start_1

	mov			r4, #4
	mov			r6, #-32
.Loop_start_2:
	vld1.16		{d0-d3}, [WINDOW], r5
	vld1.16		{d4-d7}, [WINDOW], r5
	vld1.16		{d8-d11}, [B0L, :128], r6
	vld1.16		{d12-d15}, [B0R, :128], r6
	vld1.16		{d16-d19}, [B0L, :128], r6
	vld1.16		{d20-d23}, [B0R, :128], r6
	
	vmull.s16	q12, d0, d8
	vmull.s16	q13, d0, d12
	vmull.s16	q14, d4, d16
	vmull.s16	q15, d4, d20
	vmlal.s16	q12, d1, d9
	vmlal.s16	q13, d1, d13
	vmlal.s16	q14, d5, d17
	vmlal.s16	q15, d5, d21
	vmlal.s16	q12, d2, d10
	vmlal.s16	q13, d2, d14
	vmlal.s16	q14, d6, d18
	vmlal.s16	q15, d6, d22
	vmlal.s16	q12, d3, d11
	vmlal.s16	q13, d3, d15
	vmlal.s16	q14, d7, d19
	vmlal.s16	q15, d7, d23
	vpadd.i32	d24, d24, d25
	vpadd.i32	d26, d26, d27
	vpadd.i32	d28, d28, d29
	vpadd.i32	d30, d30, d31
	vpadd.i32	d24, d24, d26
	vpadd.i32	d25, d28, d30
	vqshrn.s32	d0, q12, #13
	vst1.16		{d0}, [SAMPLES]!
	
	vld1.16		{d0-d3}, [WINDOW], r5
	vld1.16		{d4-d7}, [WINDOW], r5
	vld1.16		{d8-d11}, [B0L, :128], r6
	vld1.16		{d12-d15}, [B0R, :128], r6
	vld1.16		{d16-d19}, [B0L, :128], r6
	vld1.16		{d20-d23}, [B0R, :128], r6
	
	vmull.s16	q12, d0, d8
	vmull.s16	q13, d0, d12
	vmull.s16	q14, d4, d16
	vmull.s16	q15, d4, d20
	vmlal.s16	q12, d1, d9
	vmlal.s16	q13, d1, d13
	vmlal.s16	q14, d5, d17
	vmlal.s16	q15, d5, d21
	vmlal.s16	q12, d2, d10
	vmlal.s16	q13, d2, d14
	vmlal.s16	q14, d6, d18
	vmlal.s16	q15, d6, d22
	vmlal.s16	q12, d3, d11
	vmlal.s16	q13, d3, d15
	vmlal.s16	q14, d7, d19
	vmlal.s16	q15, d7, d23
	vpadd.i32	d24, d24, d25
	vpadd.i32	d26, d26, d27
	vpadd.i32	d28, d28, d29
	vpadd.i32	d30, d30, d31
	vpadd.i32	d24, d24, d26
	vpadd.i32	d25, d28, d30
	vqshrn.s32	d0, q12, #13
	vst1.16		{d0}, [SAMPLES]!

	subs		r4, r4, #1
	bne			.Loop_start_2

	mov			r0, #0

	vpop		{q4-q7}
	pop			{r4-r6, pc}

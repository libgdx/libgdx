/*
	synth_neon: ARM NEON optimized synth

	copyright 1995-2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define WINDOW r0
#define B0 r1
#define SAMPLES r2

/*
	int synth_1to1_neon_asm(short *window, short *b0, short *samples, int bo1);
	return value: number of clipped samples
*/

	.text
	.globl ASM_NAME(synth_1to1_neon_asm)
ASM_NAME(synth_1to1_neon_asm):
	push		{r4-r5, lr}
	vpush		{q4-q7}

	add			WINDOW, WINDOW, #32
	sub			WINDOW, WINDOW, r3, lsl #1

	mov			r3, #4
	mov			r4, #64
.Loop_start_1:
	vld1.16		{d0-d3}, [WINDOW], r4
	vld1.16		{d4-d7}, [B0, :128]!
	vld1.16		{d8-d11}, [WINDOW], r4
	vswp		d1, d4
	vld1.16		{d12-d15}, [B0, :128]!
	vld1.16		{d16-d19}, [WINDOW], r4
	vld1.16		{d20-d23}, [B0, :128]!
	vswp		d9, d12
	vld1.16		{d24-d27}, [WINDOW], r4
	vld1.16		{d28-d31}, [B0, :128]!
	vswp		d17, d20
	vswp		d25, d28
	vmull.s16	q0, d0, d1
	vmull.s16	q4, d8, d9
	vmull.s16	q8, d16, d17
	vmull.s16	q12, d24, d25
	vmlal.s16	q0, d4, d5
	vmlal.s16	q4, d12, d13
	vmlal.s16	q8, d20, d21
	vmlal.s16	q12, d28, d29
	vmlal.s16	q0, d2, d6
	vmlal.s16	q4, d10, d14
	vmlal.s16	q8, d18, d22
	vmlal.s16	q12, d26, d30
	vmlal.s16	q0, d3, d7
	vmlal.s16	q4, d11, d15
	vmlal.s16	q8, d19, d23
	vmlal.s16	q12, d27, d31
	vpadd.i32	d0, d0, d1
	vpadd.i32	d8, d8, d9
	vpadd.i32	d16, d16, d17
	vpadd.i32	d24, d24, d25
	vpadd.i32	d0, d0, d8
	vpadd.i32	d1, d16, d24

	vld2.16		{d2,d3}, [SAMPLES]
	vqshrn.s32	d1, q0, #13
	vst2.16		{d1,d3}, [SAMPLES]!

	subs		r3, r3, #1
	bne			.Loop_start_1

	mov			r3, #4
	mov			r5, #-32
.Loop_start_2:
	vld1.16		{d0-d3}, [WINDOW], r4
	vld1.16		{d4-d7}, [B0, :128], r5
	vld1.16		{d8-d11}, [WINDOW], r4
	vswp		d1, d4
	vld1.16		{d12-d15}, [B0, :128], r5
	vld1.16		{d16-d19}, [WINDOW], r4
	vld1.16		{d20-d23}, [B0, :128], r5
	vswp		d9, d12
	vld1.16		{d24-d27}, [WINDOW], r4
	vld1.16		{d28-d31}, [B0, :128], r5
	vswp		d17, d20
	vswp		d25, d28
	vmull.s16	q0, d0, d1
	vmull.s16	q4, d8, d9
	vmull.s16	q8, d16, d17
	vmull.s16	q12, d24, d25
	vmlal.s16	q0, d4, d5
	vmlal.s16	q4, d12, d13
	vmlal.s16	q8, d20, d21
	vmlal.s16	q12, d28, d29
	vmlal.s16	q0, d2, d6
	vmlal.s16	q4, d10, d14
	vmlal.s16	q8, d18, d22
	vmlal.s16	q12, d26, d30
	vmlal.s16	q0, d3, d7
	vmlal.s16	q4, d11, d15
	vmlal.s16	q8, d19, d23
	vmlal.s16	q12, d27, d31
	vpadd.i32	d0, d0, d1
	vpadd.i32	d8, d8, d9
	vpadd.i32	d16, d16, d17
	vpadd.i32	d24, d24, d25
	vpadd.i32	d0, d0, d8
	vpadd.i32	d1, d16, d24

	vld2.16		{d2,d3}, [SAMPLES]
	vqshrn.s32	d1, q0, #13
	vst2.16		{d1,d3}, [SAMPLES]!

	subs		r3, r3, #1
	bne			.Loop_start_2

	mov			r0, #0

	vpop		{q4-q7}
	pop			{r4-r5, pc}

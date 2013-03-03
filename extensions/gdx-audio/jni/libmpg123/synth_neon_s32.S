/*
	synth_neon_s32: ARM NEON optimized synth (32-bit output version)

	copyright 1995-2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

#define WINDOW r0
#define B0 r1
#define SAMPLES r2

/*
	int synth_1to1_s32_neon_asm(real *window, real *b0, int *samples, int bo1);
	return value: number of clipped samples (0)
*/

	.text
	.globl ASM_NAME(synth_1to1_s32_neon_asm)
	ALIGN4
ASM_NAME(synth_1to1_s32_neon_asm):
	push		{r4-r6, lr}
	vpush		{q4-q7}
	mov			r6, sp
	sub			sp, sp, #16
	bic			sp, #0xff
	
	add			WINDOW, WINDOW, #64
	sub			WINDOW, WINDOW, r3, lsl #2

	mov			r3, #4
	mov			r4, #128
	mov			r5, #64
.Loop_start_1:
	vld1.32		{q0,q1}, [WINDOW], r4
	vld1.32		{q2,q3}, [WINDOW], r4
	vld1.32		{q4,q5}, [WINDOW], r4
	vld1.32		{q6,q7}, [WINDOW]
	sub			WINDOW, WINDOW, #352
	vld1.32		{q8,q9}, [B0, :128], r5
	vld1.32		{q10,q11}, [B0, :128], r5
	vld1.32		{q12,q13}, [B0, :128], r5
	vld1.32		{q14,q15}, [B0, :128]
	vswp		q1, q4
	vswp		q3, q6
	sub			B0, B0, #160
	vmul.f32	q0, q0, q8
	vmul.f32	q2, q2, q10
	vmul.f32	q1, q1, q12
	vmul.f32	q3, q3, q14
	vmla.f32	q0, q4, q9
	vmla.f32	q2, q6, q11
	vmla.f32	q1, q5, q13
	vmla.f32	q3, q7, q15
	vld1.32		{q4,q5}, [WINDOW], r4
	vld1.32		{q6,q7}, [WINDOW], r4
	vld1.32		{q8,q9}, [WINDOW], r4
	vld1.32		{q10,q11}, [B0, :128], r5
	vld1.32		{q12,q13}, [B0, :128], r5
	vld1.32		{q14,q15}, [B0, :128], r5
	vswp		q5, q6
	vswp		q11, q12
	vmla.f32	q0, q4, q10
	vmla.f32	q2, q5, q11
	vmla.f32	q1, q8, q14
	vld1.32		{q4,q5}, [WINDOW]
	vld1.32		{q10,q11}, [B0, :128]!
	add			WINDOW, WINDOW, #96
	vmla.f32	q3, q4, q10
	vmla.f32	q0, q6, q12
	vmla.f32	q2, q7, q13
	vmla.f32	q1, q9, q15
	vmla.f32	q3, q5, q11
	vmvn.i32	q5, #0xb9000000
	vpadd.f32	d0, d0, d1
	vpadd.f32	d4, d4, d5
	vpadd.f32	d2, d2, d3
	vpadd.f32	d6, d6, d7
	vld1.32		{q6}, [sp, :128]
	vpadd.f32	d0, d0, d4
	vpadd.f32	d1, d2, d6

	vcvt.s32.f32	q3, q0, #16
	vacgt.f32	q5, q0, q5
	vld2.32		{q1,q2}, [SAMPLES]
	vshr.u32	q5, q5, #31
	vmov		q1, q3
	vst2.32		{q1,q2}, [SAMPLES]!
	vadd.i32	q5, q5, q6
	vst1.32		{q5}, [sp, :128]

	subs		r3, r3, #1
	bne			.Loop_start_1

	mov			r3, #4
	mov			r5, #-64
.Loop_start_2:
	vld1.32		{q0,q1}, [WINDOW], r4
	vld1.32		{q2,q3}, [WINDOW], r4
	vld1.32		{q4,q5}, [WINDOW], r4
	vld1.32		{q6,q7}, [WINDOW]
	sub			WINDOW, WINDOW, #352
	vld1.32		{q8,q9}, [B0, :128], r5
	vld1.32		{q10,q11}, [B0, :128], r5
	vld1.32		{q12,q13}, [B0, :128], r5
	vld1.32		{q14,q15}, [B0, :128]
	vswp		q1, q4
	vswp		q3, q6
	add			B0, B0, #224
	vmul.f32	q0, q0, q8
	vmul.f32	q2, q2, q10
	vmul.f32	q1, q1, q12
	vmul.f32	q3, q3, q14
	vmla.f32	q0, q4, q9
	vmla.f32	q2, q6, q11
	vmla.f32	q1, q5, q13
	vmla.f32	q3, q7, q15
	vld1.32		{q4,q5}, [WINDOW], r4
	vld1.32		{q6,q7}, [WINDOW], r4
	vld1.32		{q8,q9}, [WINDOW], r4
	vld1.32		{q10,q11}, [B0, :128], r5
	vld1.32		{q12,q13}, [B0, :128], r5
	vld1.32		{q14,q15}, [B0, :128], r5
	vswp		q5, q6
	vswp		q11, q12
	vmla.f32	q0, q4, q10
	vmla.f32	q2, q5, q11
	vmla.f32	q1, q8, q14
	vld1.32		{q4,q5}, [WINDOW]
	vld1.32		{q10,q11}, [B0, :128]
	add			WINDOW, WINDOW, #96
	sub			B0, B0, #96
	vmla.f32	q3, q4, q10
	vmla.f32	q0, q6, q12
	vmla.f32	q2, q7, q13
	vmla.f32	q1, q9, q15
	vmla.f32	q3, q5, q11
	vmvn.i32	q5, #0xb9000000
	vpadd.f32	d0, d0, d1
	vpadd.f32	d4, d4, d5
	vpadd.f32	d2, d2, d3
	vpadd.f32	d6, d6, d7
	vld1.32		{q6}, [sp, :128]
	vpadd.f32	d0, d0, d4
	vpadd.f32	d1, d2, d6

	vcvt.s32.f32	q3, q0, #16
	vacgt.f32	q5, q0, q5
	vld2.32		{q1,q2}, [SAMPLES]
	vshr.u32	q5, q5, #31
	vmov		q1, q3
	vst2.32		{q1,q2}, [SAMPLES]!
	vadd.i32	q5, q5, q6
	vst1.32		{q5}, [sp, :128]

	subs		r3, r3, #1
	bne			.Loop_start_2

	vld1.32		{q0}, [sp, :128]
	vpadd.i32	d0, d0, d1
	vpadd.i32	d0, d0, d0
	vmov.32		r0, d0[0]

	mov			sp, r6
	vpop		{q4-q7}
	pop			{r4-r6, pc}

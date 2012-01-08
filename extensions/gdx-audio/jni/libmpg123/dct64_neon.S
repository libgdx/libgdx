/*
	dct64_neon: ARM NEON optimized dct64

	copyright 1995-2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "mangle.h"

	.text
	ALIGN16
costab_arm:
	.word 1056974725
	.word 1057056395
	.word 1057223771
	.word 1057485416
	.word 1057855544
	.word 1058356026
	.word 1059019886
	.word 1059897405
	.word 1061067246
	.word 1062657950
	.word 1064892987
	.word 1066774581
	.word 1069414683
	.word 1073984175
	.word 1079645762
	.word 1092815430
	.word 1057005197
	.word 1057342072
	.word 1058087743
	.word 1059427869
	.word 1061799040
	.word 1065862217
	.word 1071413542
	.word 1084439708
	.word 1057128951
	.word 1058664893
	.word 1063675095
	.word 1076102863
	.word 1057655764
	.word 1067924853
	.word 1060439283
	.word 1060439283
	.globl ASM_NAME(dct64_neon)
ASM_NAME(dct64_neon):
	vpush		{q4-q7}

	adr			r3, costab_arm
	vld1.32		{q0, q1}, [r2]!
	vld1.32		{q2, q3}, [r2]!
	vld1.32		{q4, q5}, [r2]!
	vld1.32		{q6, q7}, [r2]
	vld1.32		{q12, q13}, [r3, :128]!
	vld1.32		{q14, q15}, [r3, :128]!

	vrev64.32	q4, q4
	vrev64.32	q5, q5
	vrev64.32	q6, q6
	vrev64.32	q7, q7
	vswp		d8, d9
	vswp		d10, d11
	vswp		d12, d13
	vswp		d14, d15

	vsub.f32	q8, q0, q7
	vsub.f32	q9, q1, q6
	vsub.f32	q10, q2, q5
	vsub.f32	q11, q3, q4
	vadd.f32	q0, q0, q7
	vadd.f32	q1, q1, q6
	vadd.f32	q2, q2, q5
	vadd.f32	q3, q3, q4
	vmul.f32	q4, q8, q12
	vmul.f32	q5, q9, q13
	vmul.f32	q6, q10, q14
	vmul.f32	q7, q11, q15

	vld1.32		{q12, q13}, [r3, :128]!
	vld1.32		{q14, q15}, [r3, :128]

	vrev64.32	q2, q2
	vrev64.32	q3, q3
	vrev64.32	q6, q6
	vrev64.32	q7, q7
	vswp		d4, d5
	vswp		d6, d7
	vswp		d12, d13
	vswp		d14, d15

	vsub.f32	q8, q0, q3
	vsub.f32	q9, q1, q2
	vsub.f32	q10, q4, q7
	vsub.f32	q11, q5, q6
	vadd.f32	q0, q0, q3
	vadd.f32	q1, q1, q2
	vadd.f32	q4, q4, q7
	vadd.f32	q5, q5, q6
	vmul.f32	q2, q8, q12
	vmul.f32	q3, q9, q13
	vmul.f32	q6, q10, q12
	vmul.f32	q7, q11, q13

	vrev64.32	q1, q1
	vrev64.32	q3, q3
	vrev64.32	q5, q5
	vrev64.32	q7, q7
	vswp		d2, d3
	vswp		d6, d7
	vswp		d10, d11
	vswp		d14, d15

	vsub.f32	q8, q0, q1
	vsub.f32	q9, q2, q3
	vsub.f32	q10, q4, q5
	vsub.f32	q11, q6, q7
	vadd.f32	q0, q0, q1
	vadd.f32	q2, q2, q3
	vadd.f32	q4, q4, q5
	vadd.f32	q6, q6, q7
	vmul.f32	q1, q8, q14
	vmul.f32	q3, q9, q14
	vmul.f32	q5, q10, q14
	vmul.f32	q7, q11, q14

	vdup.32		q12, d31[0]
	vmov		d31, d30

	vswp		d1, d2
	vswp		d5, d6
	vswp		d9, d10
	vswp		d13, d14
	vrev64.32	q1, q1
	vrev64.32	q3, q3
	vrev64.32	q5, q5
	vrev64.32	q7, q7

	vsub.f32	q8, q0, q1
	vsub.f32	q9, q2, q3
	vsub.f32	q10, q4, q5
	vsub.f32	q11, q6, q7
	vadd.f32	q0, q0, q1
	vadd.f32	q2, q2, q3
	vadd.f32	q4, q4, q5
	vadd.f32	q6, q6, q7
	vmul.f32	q1, q8, q15
	vmul.f32	q3, q9, q15
	vmul.f32	q5, q10, q15
	vmul.f32	q7, q11, q15

	vtrn.32		q0, q1
	vtrn.32		q2, q3
	vtrn.32		q4, q5
	vtrn.32		q6, q7

	vsub.f32	q8, q0, q1
	vsub.f32	q9, q2, q3
	vsub.f32	q10, q4, q5
	vsub.f32	q11, q6, q7
	vadd.f32	q0, q0, q1
	vadd.f32	q2, q2, q3
	vadd.f32	q4, q4, q5
	vadd.f32	q6, q6, q7
	vmul.f32	q1, q8, q12
	vmul.f32	q3, q9, q12
	vmul.f32	q5, q10, q12
	vmul.f32	q7, q11, q12

	vtrn.32		q0, q1
	vtrn.32		q2, q3
	vtrn.32		q4, q5
	vtrn.32		q6, q7
	vswp		d1, d2
	vswp		d5, d6
	vswp		d9, d10
	vswp		d13, d14

	vshr.u64	d16, d1, #32
	vshr.u64	d17, d3, #32
	vshr.u64	d18, d5, #32
	vshr.u64	d19, d7, #32
	vadd.f32	d1, d1, d16
	vadd.f32	d3, d3, d17
	vadd.f32	d5, d5, d18
	vadd.f32	d7, d7, d19
	vshr.u64	d20, d9, #32
	vshr.u64	d21, d11, #32
	vshr.u64	d22, d13, #32
	vshr.u64	d23, d15, #32
	vadd.f32	d9, d9, d20
	vadd.f32	d11, d11, d21
	vadd.f32	d13, d13, d22
	vadd.f32	d15, d15, d23

	vshr.u64	d16, d2, #32
	vshr.u64	d18, d6, #32
	vshr.u64	d20, d10, #32
	vshr.u64	d22, d14, #32
	vext.8		q8, q1, q8, #8
	vext.8		q9, q3, q9, #8
	vext.8		q10, q5, q10, #8
	vext.8		q11, q7, q11, #8
	vadd.f32	q1, q1, q8
	vadd.f32	q3, q3, q9
	vadd.f32	q5, q5, q10
	vadd.f32	q7, q7, q11

	vshr.u64	d16, d4, #32
	vshr.u64	d18, d12, #32
	vext.8		q8, q2, q8, #8
	vext.8		q9, q6, q9, #8
	vadd.f32	q2, q2, q3
	vadd.f32	q6, q6, q7
	vadd.f32	q3, q3, q8
	vadd.f32	q7, q7, q9

	vrev64.32	q8, q4
	vshr.u64	d19, d9, #32
	vext.8		d17, d17, d16, #4
	vswp		d9, d10
	vswp		d13, d14
	vtrn.32		q4, q5
	vtrn.32		q6, q7
	vmov		d16, d9
	vmov		d18, d11

	vadd.f32	q4, q6
	vadd.f32	q5, q7
	vadd.f32	q6, q8
	vadd.f32	q7, q9

	vmov.i32	q8, #0x4b000000
	vorr.i32	q8, #0x00400000
	vadd.f32	q0, q0, q8
	vadd.f32	q1, q1, q8
	vadd.f32	q2, q2, q8
	vadd.f32	q3, q3, q8
	vadd.f32	q4, q4, q8
	vadd.f32	q5, q5, q8
	vadd.f32	q6, q6, q8
	vadd.f32	q7, q7, q8
	vshl.i32	q0, q0, #10
	vshl.i32	q1, q1, #10
	vshl.i32	q2, q2, #10
	vshl.i32	q3, q3, #10
	vshl.i32	q4, q4, #10
	vshl.i32	q5, q5, #10
	vshl.i32	q6, q6, #10
	vshl.i32	q7, q7, #10
	vqshrn.s32	d0, q0, #10
	vqshrn.s32	d2, q1, #10
	vqshrn.s32	d4, q2, #10
	vqshrn.s32	d6, q3, #10
	vqshrn.s32	d8, q4, #10
	vqshrn.s32	d10, q5, #10
	vqshrn.s32	d12, q6, #10
	vqshrn.s32	d14, q7, #10

	mov			r3, #32
	vst1.16		{d0[1]}, [r0, :16], r3
	vst1.16		{d12[3]}, [r0, :16], r3
	vst1.16		{d6[2]}, [r0, :16], r3
	vst1.16		{d8[3]}, [r0, :16], r3
	vst1.16		{d2[2]}, [r0, :16], r3
	vst1.16		{d12[1]}, [r0, :16], r3
	vst1.16		{d4[2]}, [r0, :16], r3
	vst1.16		{d8[1]}, [r0, :16], r3
	vst1.16		{d0[2]}, [r0, :16], r3
	vst1.16		{d12[2]}, [r0, :16], r3
	vst1.16		{d6[0]}, [r0, :16], r3
	vst1.16		{d8[2]}, [r0, :16], r3
	vst1.16		{d2[0]}, [r0, :16], r3
	vst1.16		{d12[0]}, [r0, :16], r3
	vst1.16		{d4[0]}, [r0, :16], r3
	vst1.16		{d8[0]}, [r0, :16], r3
	vst1.16		{d0[0]}, [r0, :16]

	vst1.16		{d0[1]}, [r1, :16], r3
	vst1.16		{d10[0]}, [r1, :16], r3
	vst1.16		{d4[1]}, [r1, :16], r3
	vst1.16		{d14[0]}, [r1, :16], r3
	vst1.16		{d2[1]}, [r1, :16], r3
	vst1.16		{d10[2]}, [r1, :16], r3
	vst1.16		{d6[1]}, [r1, :16], r3
	vst1.16		{d14[2]}, [r1, :16], r3
	vst1.16		{d0[3]}, [r1, :16], r3
	vst1.16		{d10[1]}, [r1, :16], r3
	vst1.16		{d4[3]}, [r1, :16], r3
	vst1.16		{d14[1]}, [r1, :16], r3
	vst1.16		{d2[3]}, [r1, :16], r3
	vst1.16		{d10[3]}, [r1, :16], r3
	vst1.16		{d6[3]}, [r1, :16], r3
	vst1.16		{d14[3]}, [r1, :16]

	vpop		{q4-q7}
	bx			lr

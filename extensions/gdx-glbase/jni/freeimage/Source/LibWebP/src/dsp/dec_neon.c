// Copyright 2012 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// ARM NEON version of dsp functions and loop filtering.
//
// Authors: Somnath Banerjee (somnath@google.com)
//          Johann Koenig (johannkoenig@google.com)

#include "./dsp.h"

#if defined(WEBP_USE_NEON)

#include <arm_neon.h>

#include "../dec/vp8i.h"

#define QRegs "q0", "q1", "q2", "q3",                                          \
              "q8", "q9", "q10", "q11", "q12", "q13", "q14", "q15"

#define FLIP_SIGN_BIT2(a, b, s)                                                \
  "veor     " #a "," #a "," #s "               \n"                             \
  "veor     " #b "," #b "," #s "               \n"                             \

#define FLIP_SIGN_BIT4(a, b, c, d, s)                                          \
  FLIP_SIGN_BIT2(a, b, s)                                                      \
  FLIP_SIGN_BIT2(c, d, s)                                                      \

#define NEEDS_FILTER(p1, p0, q0, q1, thresh, mask)                             \
  "vabd.u8    q15," #p0 "," #q0 "         \n"  /* abs(p0 - q0) */              \
  "vabd.u8    q14," #p1 "," #q1 "         \n"  /* abs(p1 - q1) */              \
  "vqadd.u8   q15, q15, q15               \n"  /* abs(p0 - q0) * 2 */          \
  "vshr.u8    q14, q14, #1                \n"  /* abs(p1 - q1) / 2 */          \
  "vqadd.u8   q15, q15, q14     \n"  /* abs(p0 - q0) * 2 + abs(p1 - q1) / 2 */ \
  "vdup.8     q14, " #thresh "            \n"                                  \
  "vcge.u8   " #mask ", q14, q15          \n"  /* mask <= thresh */

#define GET_BASE_DELTA(p1, p0, q0, q1, o)                                      \
  "vqsub.s8   q15," #q0 "," #p0 "         \n"  /* (q0 - p0) */                 \
  "vqsub.s8  " #o "," #p1 "," #q1 "       \n"  /* (p1 - q1) */                 \
  "vqadd.s8  " #o "," #o ", q15           \n"  /* (p1 - q1) + 1 * (p0 - q0) */ \
  "vqadd.s8  " #o "," #o ", q15           \n"  /* (p1 - q1) + 2 * (p0 - q0) */ \
  "vqadd.s8  " #o "," #o ", q15           \n"  /* (p1 - q1) + 3 * (p0 - q0) */

#define DO_SIMPLE_FILTER(p0, q0, fl)                                           \
  "vmov.i8    q15, #0x03                  \n"                                  \
  "vqadd.s8   q15, q15, " #fl "           \n"  /* filter1 = filter + 3 */      \
  "vshr.s8    q15, q15, #3                \n"  /* filter1 >> 3 */              \
  "vqadd.s8  " #p0 "," #p0 ", q15         \n"  /* p0 += filter1 */             \
                                                                               \
  "vmov.i8    q15, #0x04                  \n"                                  \
  "vqadd.s8   q15, q15, " #fl "           \n"  /* filter1 = filter + 4 */      \
  "vshr.s8    q15, q15, #3                \n"  /* filter2 >> 3 */              \
  "vqsub.s8  " #q0 "," #q0 ", q15         \n"  /* q0 -= filter2 */

// Applies filter on 2 pixels (p0 and q0)
#define DO_FILTER2(p1, p0, q0, q1, thresh)                                     \
  NEEDS_FILTER(p1, p0, q0, q1, thresh, q9)     /* filter mask in q9 */         \
  "vmov.i8    q10, #0x80                  \n"  /* sign bit */                  \
  FLIP_SIGN_BIT4(p1, p0, q0, q1, q10)          /* convert to signed value */   \
  GET_BASE_DELTA(p1, p0, q0, q1, q11)          /* get filter level  */         \
  "vand       q9, q9, q11                 \n"  /* apply filter mask */         \
  DO_SIMPLE_FILTER(p0, q0, q9)                 /* apply filter */              \
  FLIP_SIGN_BIT2(p0, q0, q10)

// Load/Store vertical edge
#define LOAD8x4(c1, c2, c3, c4, b1, b2, stride)                                \
  "vld4.8   {" #c1"[0], " #c2"[0], " #c3"[0], " #c4"[0]}," #b1 "," #stride"\n" \
  "vld4.8   {" #c1"[1], " #c2"[1], " #c3"[1], " #c4"[1]}," #b2 "," #stride"\n" \
  "vld4.8   {" #c1"[2], " #c2"[2], " #c3"[2], " #c4"[2]}," #b1 "," #stride"\n" \
  "vld4.8   {" #c1"[3], " #c2"[3], " #c3"[3], " #c4"[3]}," #b2 "," #stride"\n" \
  "vld4.8   {" #c1"[4], " #c2"[4], " #c3"[4], " #c4"[4]}," #b1 "," #stride"\n" \
  "vld4.8   {" #c1"[5], " #c2"[5], " #c3"[5], " #c4"[5]}," #b2 "," #stride"\n" \
  "vld4.8   {" #c1"[6], " #c2"[6], " #c3"[6], " #c4"[6]}," #b1 "," #stride"\n" \
  "vld4.8   {" #c1"[7], " #c2"[7], " #c3"[7], " #c4"[7]}," #b2 "," #stride"\n"

#define STORE8x2(c1, c2, p, stride)                                            \
  "vst2.8   {" #c1"[0], " #c2"[0]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[1], " #c2"[1]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[2], " #c2"[2]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[3], " #c2"[3]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[4], " #c2"[4]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[5], " #c2"[5]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[6], " #c2"[6]}," #p "," #stride " \n"                      \
  "vst2.8   {" #c1"[7], " #c2"[7]}," #p "," #stride " \n"

// Treats 'v' as an uint8x8_t and zero extends to an int16x8_t.
static WEBP_INLINE int16x8_t ConvertU8ToS16(uint32x2_t v) {
  return vreinterpretq_s16_u16(vmovl_u8(vreinterpret_u8_u32(v)));
}

// Performs unsigned 8b saturation on 'dst01' and 'dst23' storing the result
// to the corresponding rows of 'dst'.
static WEBP_INLINE void SaturateAndStore4x4(uint8_t* const dst,
                                            int16x8_t dst01, int16x8_t dst23) {
  // Unsigned saturate to 8b.
  const uint8x8_t dst01_u8 = vqmovun_s16(dst01);
  const uint8x8_t dst23_u8 = vqmovun_s16(dst23);

  // Store the results.
  *(int*)(dst + 0 * BPS) = vget_lane_s32(vreinterpret_s32_u8(dst01_u8), 0);
  *(int*)(dst + 1 * BPS) = vget_lane_s32(vreinterpret_s32_u8(dst01_u8), 1);
  *(int*)(dst + 2 * BPS) = vget_lane_s32(vreinterpret_s32_u8(dst23_u8), 0);
  *(int*)(dst + 3 * BPS) = vget_lane_s32(vreinterpret_s32_u8(dst23_u8), 1);
}

//-----------------------------------------------------------------------------
// Simple In-loop filtering (Paragraph 15.2)

static void SimpleVFilter16NEON(uint8_t* p, int stride, int thresh) {
  __asm__ volatile (
    "sub        %[p], %[p], %[stride], lsl #1  \n"  // p -= 2 * stride

    "vld1.u8    {q1}, [%[p]], %[stride]        \n"  // p1
    "vld1.u8    {q2}, [%[p]], %[stride]        \n"  // p0
    "vld1.u8    {q3}, [%[p]], %[stride]        \n"  // q0
    "vld1.u8    {q12}, [%[p]]                  \n"  // q1

    DO_FILTER2(q1, q2, q3, q12, %[thresh])

    "sub        %[p], %[p], %[stride], lsl #1  \n"  // p -= 2 * stride

    "vst1.u8    {q2}, [%[p]], %[stride]        \n"  // store op0
    "vst1.u8    {q3}, [%[p]]                   \n"  // store oq0
    : [p] "+r"(p)
    : [stride] "r"(stride), [thresh] "r"(thresh)
    : "memory", QRegs
  );
}

static void SimpleHFilter16NEON(uint8_t* p, int stride, int thresh) {
  __asm__ volatile (
    "sub        r4, %[p], #2                   \n"  // base1 = p - 2
    "lsl        r6, %[stride], #1              \n"  // r6 = 2 * stride
    "add        r5, r4, %[stride]              \n"  // base2 = base1 + stride

    LOAD8x4(d2, d3, d4, d5, [r4], [r5], r6)
    LOAD8x4(d24, d25, d26, d27, [r4], [r5], r6)
    "vswp       d3, d24                        \n"  // p1:q1 p0:q3
    "vswp       d5, d26                        \n"  // q0:q2 q1:q4
    "vswp       q2, q12                        \n"  // p1:q1 p0:q2 q0:q3 q1:q4

    DO_FILTER2(q1, q2, q12, q13, %[thresh])

    "sub        %[p], %[p], #1                 \n"  // p - 1

    "vswp        d5, d24                       \n"
    STORE8x2(d4, d5, [%[p]], %[stride])
    STORE8x2(d24, d25, [%[p]], %[stride])

    : [p] "+r"(p)
    : [stride] "r"(stride), [thresh] "r"(thresh)
    : "memory", "r4", "r5", "r6", QRegs
  );
}

static void SimpleVFilter16iNEON(uint8_t* p, int stride, int thresh) {
  int k;
  for (k = 3; k > 0; --k) {
    p += 4 * stride;
    SimpleVFilter16NEON(p, stride, thresh);
  }
}

static void SimpleHFilter16iNEON(uint8_t* p, int stride, int thresh) {
  int k;
  for (k = 3; k > 0; --k) {
    p += 4;
    SimpleHFilter16NEON(p, stride, thresh);
  }
}

//-----------------------------------------------------------------------------
// Inverse transforms (Paragraph 14.4)

static void TransformOne(const int16_t* in, uint8_t* dst) {
  const int kBPS = BPS;
  const int16_t constants[] = {20091, 17734, 0, 0};
  /* kC1, kC2. Padded because vld1.16 loads 8 bytes
   * Technically these are unsigned but vqdmulh is only available in signed.
   * vqdmulh returns high half (effectively >> 16) but also doubles the value,
   * changing the >> 16 to >> 15 and requiring an additional >> 1.
   * We use this to our advantage with kC2. The canonical value is 35468.
   * However, the high bit is set so treating it as signed will give incorrect
   * results. We avoid this by down shifting by 1 here to clear the highest bit.
   * Combined with the doubling effect of vqdmulh we get >> 16.
   * This can not be applied to kC1 because the lowest bit is set. Down shifting
   * the constant would reduce precision.
   */

  /* libwebp uses a trick to avoid some extra addition that libvpx does.
   * Instead of:
   * temp2 = ip[12] + ((ip[12] * cospi8sqrt2minus1) >> 16);
   * libwebp adds 1 << 16 to cospi8sqrt2minus1 (kC1). However, this causes the
   * same issue with kC1 and vqdmulh that we work around by down shifting kC2
   */

  /* Adapted from libvpx: vp8/common/arm/neon/shortidct4x4llm_neon.asm */
  __asm__ volatile (
    "vld1.16         {q1, q2}, [%[in]]           \n"
    "vld1.16         {d0}, [%[constants]]        \n"

    /* d2: in[0]
     * d3: in[8]
     * d4: in[4]
     * d5: in[12]
     */
    "vswp            d3, d4                      \n"

    /* q8 = {in[4], in[12]} * kC1 * 2 >> 16
     * q9 = {in[4], in[12]} * kC2 >> 16
     */
    "vqdmulh.s16     q8, q2, d0[0]               \n"
    "vqdmulh.s16     q9, q2, d0[1]               \n"

    /* d22 = a = in[0] + in[8]
     * d23 = b = in[0] - in[8]
     */
    "vqadd.s16       d22, d2, d3                 \n"
    "vqsub.s16       d23, d2, d3                 \n"

    /* The multiplication should be x * kC1 >> 16
     * However, with vqdmulh we get x * kC1 * 2 >> 16
     * (multiply, double, return high half)
     * We avoided this in kC2 by pre-shifting the constant.
     * q8 = in[4]/[12] * kC1 >> 16
     */
    "vshr.s16        q8, q8, #1                  \n"

    /* Add {in[4], in[12]} back after the multiplication. This is handled by
     * adding 1 << 16 to kC1 in the libwebp C code.
     */
    "vqadd.s16       q8, q2, q8                  \n"

    /* d20 = c = in[4]*kC2 - in[12]*kC1
     * d21 = d = in[4]*kC1 + in[12]*kC2
     */
    "vqsub.s16       d20, d18, d17               \n"
    "vqadd.s16       d21, d19, d16               \n"

    /* d2 = tmp[0] = a + d
     * d3 = tmp[1] = b + c
     * d4 = tmp[2] = b - c
     * d5 = tmp[3] = a - d
     */
    "vqadd.s16       d2, d22, d21                \n"
    "vqadd.s16       d3, d23, d20                \n"
    "vqsub.s16       d4, d23, d20                \n"
    "vqsub.s16       d5, d22, d21                \n"

    "vzip.16         q1, q2                      \n"
    "vzip.16         q1, q2                      \n"

    "vswp            d3, d4                      \n"

    /* q8 = {tmp[4], tmp[12]} * kC1 * 2 >> 16
     * q9 = {tmp[4], tmp[12]} * kC2 >> 16
     */
    "vqdmulh.s16     q8, q2, d0[0]               \n"
    "vqdmulh.s16     q9, q2, d0[1]               \n"

    /* d22 = a = tmp[0] + tmp[8]
     * d23 = b = tmp[0] - tmp[8]
     */
    "vqadd.s16       d22, d2, d3                 \n"
    "vqsub.s16       d23, d2, d3                 \n"

    /* See long winded explanations prior */
    "vshr.s16        q8, q8, #1                  \n"
    "vqadd.s16       q8, q2, q8                  \n"

    /* d20 = c = in[4]*kC2 - in[12]*kC1
     * d21 = d = in[4]*kC1 + in[12]*kC2
     */
    "vqsub.s16       d20, d18, d17               \n"
    "vqadd.s16       d21, d19, d16               \n"

    /* d2 = tmp[0] = a + d
     * d3 = tmp[1] = b + c
     * d4 = tmp[2] = b - c
     * d5 = tmp[3] = a - d
     */
    "vqadd.s16       d2, d22, d21                \n"
    "vqadd.s16       d3, d23, d20                \n"
    "vqsub.s16       d4, d23, d20                \n"
    "vqsub.s16       d5, d22, d21                \n"

    "vld1.32         d6[0], [%[dst]], %[kBPS]    \n"
    "vld1.32         d6[1], [%[dst]], %[kBPS]    \n"
    "vld1.32         d7[0], [%[dst]], %[kBPS]    \n"
    "vld1.32         d7[1], [%[dst]], %[kBPS]    \n"

    "sub         %[dst], %[dst], %[kBPS], lsl #2 \n"

    /* (val) + 4 >> 3 */
    "vrshr.s16       d2, d2, #3                  \n"
    "vrshr.s16       d3, d3, #3                  \n"
    "vrshr.s16       d4, d4, #3                  \n"
    "vrshr.s16       d5, d5, #3                  \n"

    "vzip.16         q1, q2                      \n"
    "vzip.16         q1, q2                      \n"

    /* Must accumulate before saturating */
    "vmovl.u8        q8, d6                      \n"
    "vmovl.u8        q9, d7                      \n"

    "vqadd.s16       q1, q1, q8                  \n"
    "vqadd.s16       q2, q2, q9                  \n"

    "vqmovun.s16     d0, q1                      \n"
    "vqmovun.s16     d1, q2                      \n"

    "vst1.32         d0[0], [%[dst]], %[kBPS]    \n"
    "vst1.32         d0[1], [%[dst]], %[kBPS]    \n"
    "vst1.32         d1[0], [%[dst]], %[kBPS]    \n"
    "vst1.32         d1[1], [%[dst]]             \n"

    : [in] "+r"(in), [dst] "+r"(dst)  /* modified registers */
    : [kBPS] "r"(kBPS), [constants] "r"(constants)  /* constants */
    : "memory", "q0", "q1", "q2", "q8", "q9", "q10", "q11"  /* clobbered */
  );
}

static void TransformTwo(const int16_t* in, uint8_t* dst, int do_two) {
  TransformOne(in, dst);
  if (do_two) {
    TransformOne(in + 16, dst + 4);
  }
}

static void TransformDC(const int16_t* in, uint8_t* dst) {
  const int16x8_t DC = vdupq_n_s16((in[0] + 4) >> 3);
  uint32x2_t dst01 = {0, 0};
  uint32x2_t dst23 = {0, 0};

  // Load the source pixels.
  dst01 = vset_lane_u32(*(uint32_t*)(dst + 0 * BPS), dst01, 0);
  dst23 = vset_lane_u32(*(uint32_t*)(dst + 2 * BPS), dst23, 0);
  dst01 = vset_lane_u32(*(uint32_t*)(dst + 1 * BPS), dst01, 1);
  dst23 = vset_lane_u32(*(uint32_t*)(dst + 3 * BPS), dst23, 1);

  {
    // Convert to 16b.
    int16x8_t dst01_s16 = ConvertU8ToS16(dst01);
    int16x8_t dst23_s16 = ConvertU8ToS16(dst23);

    // Add the inverse transform.
    dst01_s16 = vaddq_s16(dst01_s16, DC);
    dst23_s16 = vaddq_s16(dst23_s16, DC);

    SaturateAndStore4x4(dst, dst01_s16, dst23_s16);
  }
}

//------------------------------------------------------------------------------

#define STORE_WHT(dst, col, row01, row23) do {         \
  *dst = vgetq_lane_s32(row01.val[0], col); out += 16; \
  *dst = vgetq_lane_s32(row01.val[1], col); out += 16; \
  *dst = vgetq_lane_s32(row23.val[0], col); out += 16; \
  *dst = vgetq_lane_s32(row23.val[1], col); out += 16; \
} while (0)

static void TransformWHT(const int16_t* in, int16_t* out) {
  int32x4x2_t tmp0;  // tmp[0..7]
  int32x4x2_t tmp1;  // tmp[8..15]

  {
    // Load the source.
    const int16x4_t in00_03 = vld1_s16(in + 0);
    const int16x4_t in04_07 = vld1_s16(in + 4);
    const int16x4_t in08_11 = vld1_s16(in + 8);
    const int16x4_t in12_15 = vld1_s16(in + 12);
    const int32x4_t a0 = vaddl_s16(in00_03, in12_15);  // in[0..3] + in[12..15]
    const int32x4_t a1 = vaddl_s16(in04_07, in08_11);  // in[4..7] + in[8..11]
    const int32x4_t a2 = vsubl_s16(in04_07, in08_11);  // in[4..7] - in[8..11]
    const int32x4_t a3 = vsubl_s16(in00_03, in12_15);  // in[0..3] - in[12..15]
    tmp0.val[0] = vaddq_s32(a0, a1);
    tmp0.val[1] = vaddq_s32(a3, a2);
    tmp1.val[0] = vsubq_s32(a0, a1);
    tmp1.val[1] = vsubq_s32(a3, a2);
  }

  tmp0 = vzipq_s32(tmp0.val[0], tmp0.val[1]);  // 0,  4, 1,  5 |  2,  6,  3,  7
  tmp1 = vzipq_s32(tmp1.val[0], tmp1.val[1]);  // 8, 12, 9, 13 | 10, 14, 11, 15

  {
    // Arrange the temporary results column-wise.
    const int32x4_t tmp_0_4_8_12 =
        vcombine_s32(vget_low_s32(tmp0.val[0]), vget_low_s32(tmp1.val[0]));
    const int32x4_t tmp_2_6_10_14 =
        vcombine_s32(vget_low_s32(tmp0.val[1]), vget_low_s32(tmp1.val[1]));
    const int32x4_t tmp_1_5_9_13 =
        vcombine_s32(vget_high_s32(tmp0.val[0]), vget_high_s32(tmp1.val[0]));
    const int32x4_t tmp_3_7_11_15 =
        vcombine_s32(vget_high_s32(tmp0.val[1]), vget_high_s32(tmp1.val[1]));
    const int32x4_t three = vdupq_n_s32(3);
    const int32x4_t dc = vaddq_s32(tmp_0_4_8_12, three);  // add rounder
    const int32x4_t a0 = vaddq_s32(dc, tmp_3_7_11_15);
    const int32x4_t a1 = vaddq_s32(tmp_1_5_9_13, tmp_2_6_10_14);
    const int32x4_t a2 = vsubq_s32(tmp_1_5_9_13, tmp_2_6_10_14);
    const int32x4_t a3 = vsubq_s32(dc, tmp_3_7_11_15);

    tmp0.val[0] = vaddq_s32(a0, a1);
    tmp0.val[1] = vaddq_s32(a3, a2);
    tmp1.val[0] = vsubq_s32(a0, a1);
    tmp1.val[1] = vsubq_s32(a3, a2);

    // right shift the results by 3.
    tmp0.val[0] = vshrq_n_s32(tmp0.val[0], 3);
    tmp0.val[1] = vshrq_n_s32(tmp0.val[1], 3);
    tmp1.val[0] = vshrq_n_s32(tmp1.val[0], 3);
    tmp1.val[1] = vshrq_n_s32(tmp1.val[1], 3);

    STORE_WHT(out, 0, tmp0, tmp1);
    STORE_WHT(out, 1, tmp0, tmp1);
    STORE_WHT(out, 2, tmp0, tmp1);
    STORE_WHT(out, 3, tmp0, tmp1);
  }
}

#undef STORE_WHT

//------------------------------------------------------------------------------

#define MUL(a, b) (((a) * (b)) >> 16)
static void TransformAC3(const int16_t* in, uint8_t* dst) {
  static const int kC1 = 20091 + (1 << 16);
  static const int kC2 = 35468;
  const int16x4_t A = vdup_n_s16(in[0] + 4);
  const int16x4_t c4 = vdup_n_s16(MUL(in[4], kC2));
  const int16x4_t d4 = vdup_n_s16(MUL(in[4], kC1));
  const int c1 = MUL(in[1], kC2);
  const int d1 = MUL(in[1], kC1);
  const int16x4_t CD = {d1, c1, -c1, -d1};
  const int16x4_t B = vqadd_s16(A, CD);
  const int16x8_t m0_m1 = vcombine_s16(vqadd_s16(B, d4), vqadd_s16(B, c4));
  const int16x8_t m2_m3 = vcombine_s16(vqsub_s16(B, c4), vqsub_s16(B, d4));
  uint32x2_t dst01 = {0, 0};
  uint32x2_t dst23 = {0, 0};

  // Load the source pixels.
  dst01 = vset_lane_u32(*(uint32_t*)(dst + 0 * BPS), dst01, 0);
  dst23 = vset_lane_u32(*(uint32_t*)(dst + 2 * BPS), dst23, 0);
  dst01 = vset_lane_u32(*(uint32_t*)(dst + 1 * BPS), dst01, 1);
  dst23 = vset_lane_u32(*(uint32_t*)(dst + 3 * BPS), dst23, 1);

  {
    // Convert to 16b.
    int16x8_t dst01_s16 = ConvertU8ToS16(dst01);
    int16x8_t dst23_s16 = ConvertU8ToS16(dst23);

    // Add the inverse transform.
    dst01_s16 = vsraq_n_s16(dst01_s16, m0_m1, 3);
    dst23_s16 = vsraq_n_s16(dst23_s16, m2_m3, 3);

    SaturateAndStore4x4(dst, dst01_s16, dst23_s16);
  }
}
#undef MUL

#endif   // WEBP_USE_NEON

//------------------------------------------------------------------------------
// Entry point

extern void VP8DspInitNEON(void);

void VP8DspInitNEON(void) {
#if defined(WEBP_USE_NEON)
  VP8Transform = TransformTwo;
  VP8TransformAC3 = TransformAC3;
  VP8TransformDC = TransformDC;
  VP8TransformWHT = TransformWHT;

  VP8SimpleVFilter16 = SimpleVFilter16NEON;
  VP8SimpleHFilter16 = SimpleHFilter16NEON;
  VP8SimpleVFilter16i = SimpleVFilter16iNEON;
  VP8SimpleHFilter16i = SimpleHFilter16iNEON;
#endif   // WEBP_USE_NEON
}

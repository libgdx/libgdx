// Copyright 2011 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// SSE2 version of some decoding functions (idct, loop filtering).
//
// Author: somnath@google.com (Somnath Banerjee)
//         cduvivier@google.com (Christian Duvivier)

#include "./dsp.h"

#if defined(WEBP_USE_SSE2)

// The 3-coeff sparse transform in SSE2 is not really faster than the plain-C
// one it seems => disable it by default. Uncomment the following to enable:
// #define USE_TRANSFORM_AC3

#include <emmintrin.h>
#include "../dec/vp8i.h"

//------------------------------------------------------------------------------
// Transforms (Paragraph 14.4)

static void Transform(const int16_t* in, uint8_t* dst, int do_two) {
  // This implementation makes use of 16-bit fixed point versions of two
  // multiply constants:
  //    K1 = sqrt(2) * cos (pi/8) ~= 85627 / 2^16
  //    K2 = sqrt(2) * sin (pi/8) ~= 35468 / 2^16
  //
  // To be able to use signed 16-bit integers, we use the following trick to
  // have constants within range:
  // - Associated constants are obtained by subtracting the 16-bit fixed point
  //   version of one:
  //      k = K - (1 << 16)  =>  K = k + (1 << 16)
  //      K1 = 85267  =>  k1 =  20091
  //      K2 = 35468  =>  k2 = -30068
  // - The multiplication of a variable by a constant become the sum of the
  //   variable and the multiplication of that variable by the associated
  //   constant:
  //      (x * K) >> 16 = (x * (k + (1 << 16))) >> 16 = ((x * k ) >> 16) + x
  const __m128i k1 = _mm_set1_epi16(20091);
  const __m128i k2 = _mm_set1_epi16(-30068);
  __m128i T0, T1, T2, T3;

  // Load and concatenate the transform coefficients (we'll do two transforms
  // in parallel). In the case of only one transform, the second half of the
  // vectors will just contain random value we'll never use nor store.
  __m128i in0, in1, in2, in3;
  {
    in0 = _mm_loadl_epi64((const __m128i*)&in[0]);
    in1 = _mm_loadl_epi64((const __m128i*)&in[4]);
    in2 = _mm_loadl_epi64((const __m128i*)&in[8]);
    in3 = _mm_loadl_epi64((const __m128i*)&in[12]);
    // a00 a10 a20 a30   x x x x
    // a01 a11 a21 a31   x x x x
    // a02 a12 a22 a32   x x x x
    // a03 a13 a23 a33   x x x x
    if (do_two) {
      const __m128i inB0 = _mm_loadl_epi64((const __m128i*)&in[16]);
      const __m128i inB1 = _mm_loadl_epi64((const __m128i*)&in[20]);
      const __m128i inB2 = _mm_loadl_epi64((const __m128i*)&in[24]);
      const __m128i inB3 = _mm_loadl_epi64((const __m128i*)&in[28]);
      in0 = _mm_unpacklo_epi64(in0, inB0);
      in1 = _mm_unpacklo_epi64(in1, inB1);
      in2 = _mm_unpacklo_epi64(in2, inB2);
      in3 = _mm_unpacklo_epi64(in3, inB3);
      // a00 a10 a20 a30   b00 b10 b20 b30
      // a01 a11 a21 a31   b01 b11 b21 b31
      // a02 a12 a22 a32   b02 b12 b22 b32
      // a03 a13 a23 a33   b03 b13 b23 b33
    }
  }

  // Vertical pass and subsequent transpose.
  {
    // First pass, c and d calculations are longer because of the "trick"
    // multiplications.
    const __m128i a = _mm_add_epi16(in0, in2);
    const __m128i b = _mm_sub_epi16(in0, in2);
    // c = MUL(in1, K2) - MUL(in3, K1) = MUL(in1, k2) - MUL(in3, k1) + in1 - in3
    const __m128i c1 = _mm_mulhi_epi16(in1, k2);
    const __m128i c2 = _mm_mulhi_epi16(in3, k1);
    const __m128i c3 = _mm_sub_epi16(in1, in3);
    const __m128i c4 = _mm_sub_epi16(c1, c2);
    const __m128i c = _mm_add_epi16(c3, c4);
    // d = MUL(in1, K1) + MUL(in3, K2) = MUL(in1, k1) + MUL(in3, k2) + in1 + in3
    const __m128i d1 = _mm_mulhi_epi16(in1, k1);
    const __m128i d2 = _mm_mulhi_epi16(in3, k2);
    const __m128i d3 = _mm_add_epi16(in1, in3);
    const __m128i d4 = _mm_add_epi16(d1, d2);
    const __m128i d = _mm_add_epi16(d3, d4);

    // Second pass.
    const __m128i tmp0 = _mm_add_epi16(a, d);
    const __m128i tmp1 = _mm_add_epi16(b, c);
    const __m128i tmp2 = _mm_sub_epi16(b, c);
    const __m128i tmp3 = _mm_sub_epi16(a, d);

    // Transpose the two 4x4.
    // a00 a01 a02 a03   b00 b01 b02 b03
    // a10 a11 a12 a13   b10 b11 b12 b13
    // a20 a21 a22 a23   b20 b21 b22 b23
    // a30 a31 a32 a33   b30 b31 b32 b33
    const __m128i transpose0_0 = _mm_unpacklo_epi16(tmp0, tmp1);
    const __m128i transpose0_1 = _mm_unpacklo_epi16(tmp2, tmp3);
    const __m128i transpose0_2 = _mm_unpackhi_epi16(tmp0, tmp1);
    const __m128i transpose0_3 = _mm_unpackhi_epi16(tmp2, tmp3);
    // a00 a10 a01 a11   a02 a12 a03 a13
    // a20 a30 a21 a31   a22 a32 a23 a33
    // b00 b10 b01 b11   b02 b12 b03 b13
    // b20 b30 b21 b31   b22 b32 b23 b33
    const __m128i transpose1_0 = _mm_unpacklo_epi32(transpose0_0, transpose0_1);
    const __m128i transpose1_1 = _mm_unpacklo_epi32(transpose0_2, transpose0_3);
    const __m128i transpose1_2 = _mm_unpackhi_epi32(transpose0_0, transpose0_1);
    const __m128i transpose1_3 = _mm_unpackhi_epi32(transpose0_2, transpose0_3);
    // a00 a10 a20 a30 a01 a11 a21 a31
    // b00 b10 b20 b30 b01 b11 b21 b31
    // a02 a12 a22 a32 a03 a13 a23 a33
    // b02 b12 a22 b32 b03 b13 b23 b33
    T0 = _mm_unpacklo_epi64(transpose1_0, transpose1_1);
    T1 = _mm_unpackhi_epi64(transpose1_0, transpose1_1);
    T2 = _mm_unpacklo_epi64(transpose1_2, transpose1_3);
    T3 = _mm_unpackhi_epi64(transpose1_2, transpose1_3);
    // a00 a10 a20 a30   b00 b10 b20 b30
    // a01 a11 a21 a31   b01 b11 b21 b31
    // a02 a12 a22 a32   b02 b12 b22 b32
    // a03 a13 a23 a33   b03 b13 b23 b33
  }

  // Horizontal pass and subsequent transpose.
  {
    // First pass, c and d calculations are longer because of the "trick"
    // multiplications.
    const __m128i four = _mm_set1_epi16(4);
    const __m128i dc = _mm_add_epi16(T0, four);
    const __m128i a =  _mm_add_epi16(dc, T2);
    const __m128i b =  _mm_sub_epi16(dc, T2);
    // c = MUL(T1, K2) - MUL(T3, K1) = MUL(T1, k2) - MUL(T3, k1) + T1 - T3
    const __m128i c1 = _mm_mulhi_epi16(T1, k2);
    const __m128i c2 = _mm_mulhi_epi16(T3, k1);
    const __m128i c3 = _mm_sub_epi16(T1, T3);
    const __m128i c4 = _mm_sub_epi16(c1, c2);
    const __m128i c = _mm_add_epi16(c3, c4);
    // d = MUL(T1, K1) + MUL(T3, K2) = MUL(T1, k1) + MUL(T3, k2) + T1 + T3
    const __m128i d1 = _mm_mulhi_epi16(T1, k1);
    const __m128i d2 = _mm_mulhi_epi16(T3, k2);
    const __m128i d3 = _mm_add_epi16(T1, T3);
    const __m128i d4 = _mm_add_epi16(d1, d2);
    const __m128i d = _mm_add_epi16(d3, d4);

    // Second pass.
    const __m128i tmp0 = _mm_add_epi16(a, d);
    const __m128i tmp1 = _mm_add_epi16(b, c);
    const __m128i tmp2 = _mm_sub_epi16(b, c);
    const __m128i tmp3 = _mm_sub_epi16(a, d);
    const __m128i shifted0 = _mm_srai_epi16(tmp0, 3);
    const __m128i shifted1 = _mm_srai_epi16(tmp1, 3);
    const __m128i shifted2 = _mm_srai_epi16(tmp2, 3);
    const __m128i shifted3 = _mm_srai_epi16(tmp3, 3);

    // Transpose the two 4x4.
    // a00 a01 a02 a03   b00 b01 b02 b03
    // a10 a11 a12 a13   b10 b11 b12 b13
    // a20 a21 a22 a23   b20 b21 b22 b23
    // a30 a31 a32 a33   b30 b31 b32 b33
    const __m128i transpose0_0 = _mm_unpacklo_epi16(shifted0, shifted1);
    const __m128i transpose0_1 = _mm_unpacklo_epi16(shifted2, shifted3);
    const __m128i transpose0_2 = _mm_unpackhi_epi16(shifted0, shifted1);
    const __m128i transpose0_3 = _mm_unpackhi_epi16(shifted2, shifted3);
    // a00 a10 a01 a11   a02 a12 a03 a13
    // a20 a30 a21 a31   a22 a32 a23 a33
    // b00 b10 b01 b11   b02 b12 b03 b13
    // b20 b30 b21 b31   b22 b32 b23 b33
    const __m128i transpose1_0 = _mm_unpacklo_epi32(transpose0_0, transpose0_1);
    const __m128i transpose1_1 = _mm_unpacklo_epi32(transpose0_2, transpose0_3);
    const __m128i transpose1_2 = _mm_unpackhi_epi32(transpose0_0, transpose0_1);
    const __m128i transpose1_3 = _mm_unpackhi_epi32(transpose0_2, transpose0_3);
    // a00 a10 a20 a30 a01 a11 a21 a31
    // b00 b10 b20 b30 b01 b11 b21 b31
    // a02 a12 a22 a32 a03 a13 a23 a33
    // b02 b12 a22 b32 b03 b13 b23 b33
    T0 = _mm_unpacklo_epi64(transpose1_0, transpose1_1);
    T1 = _mm_unpackhi_epi64(transpose1_0, transpose1_1);
    T2 = _mm_unpacklo_epi64(transpose1_2, transpose1_3);
    T3 = _mm_unpackhi_epi64(transpose1_2, transpose1_3);
    // a00 a10 a20 a30   b00 b10 b20 b30
    // a01 a11 a21 a31   b01 b11 b21 b31
    // a02 a12 a22 a32   b02 b12 b22 b32
    // a03 a13 a23 a33   b03 b13 b23 b33
  }

  // Add inverse transform to 'dst' and store.
  {
    const __m128i zero = _mm_setzero_si128();
    // Load the reference(s).
    __m128i dst0, dst1, dst2, dst3;
    if (do_two) {
      // Load eight bytes/pixels per line.
      dst0 = _mm_loadl_epi64((__m128i*)(dst + 0 * BPS));
      dst1 = _mm_loadl_epi64((__m128i*)(dst + 1 * BPS));
      dst2 = _mm_loadl_epi64((__m128i*)(dst + 2 * BPS));
      dst3 = _mm_loadl_epi64((__m128i*)(dst + 3 * BPS));
    } else {
      // Load four bytes/pixels per line.
      dst0 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 0 * BPS));
      dst1 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 1 * BPS));
      dst2 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 2 * BPS));
      dst3 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 3 * BPS));
    }
    // Convert to 16b.
    dst0 = _mm_unpacklo_epi8(dst0, zero);
    dst1 = _mm_unpacklo_epi8(dst1, zero);
    dst2 = _mm_unpacklo_epi8(dst2, zero);
    dst3 = _mm_unpacklo_epi8(dst3, zero);
    // Add the inverse transform(s).
    dst0 = _mm_add_epi16(dst0, T0);
    dst1 = _mm_add_epi16(dst1, T1);
    dst2 = _mm_add_epi16(dst2, T2);
    dst3 = _mm_add_epi16(dst3, T3);
    // Unsigned saturate to 8b.
    dst0 = _mm_packus_epi16(dst0, dst0);
    dst1 = _mm_packus_epi16(dst1, dst1);
    dst2 = _mm_packus_epi16(dst2, dst2);
    dst3 = _mm_packus_epi16(dst3, dst3);
    // Store the results.
    if (do_two) {
      // Store eight bytes/pixels per line.
      _mm_storel_epi64((__m128i*)(dst + 0 * BPS), dst0);
      _mm_storel_epi64((__m128i*)(dst + 1 * BPS), dst1);
      _mm_storel_epi64((__m128i*)(dst + 2 * BPS), dst2);
      _mm_storel_epi64((__m128i*)(dst + 3 * BPS), dst3);
    } else {
      // Store four bytes/pixels per line.
      WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(dst0));
      WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(dst1));
      WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(dst2));
      WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(dst3));
    }
  }
}

#if defined(USE_TRANSFORM_AC3)
#define MUL(a, b) (((a) * (b)) >> 16)
static void TransformAC3(const int16_t* in, uint8_t* dst) {
  static const int kC1 = 20091 + (1 << 16);
  static const int kC2 = 35468;
  const __m128i A = _mm_set1_epi16(in[0] + 4);
  const __m128i c4 = _mm_set1_epi16(MUL(in[4], kC2));
  const __m128i d4 = _mm_set1_epi16(MUL(in[4], kC1));
  const int c1 = MUL(in[1], kC2);
  const int d1 = MUL(in[1], kC1);
  const __m128i CD = _mm_set_epi16(0, 0, 0, 0, -d1, -c1, c1, d1);
  const __m128i B = _mm_adds_epi16(A, CD);
  const __m128i m0 = _mm_adds_epi16(B, d4);
  const __m128i m1 = _mm_adds_epi16(B, c4);
  const __m128i m2 = _mm_subs_epi16(B, c4);
  const __m128i m3 = _mm_subs_epi16(B, d4);
  const __m128i zero = _mm_setzero_si128();
  // Load the source pixels.
  __m128i dst0 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 0 * BPS));
  __m128i dst1 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 1 * BPS));
  __m128i dst2 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 2 * BPS));
  __m128i dst3 = _mm_cvtsi32_si128(WebPMemToUint32(dst + 3 * BPS));
  // Convert to 16b.
  dst0 = _mm_unpacklo_epi8(dst0, zero);
  dst1 = _mm_unpacklo_epi8(dst1, zero);
  dst2 = _mm_unpacklo_epi8(dst2, zero);
  dst3 = _mm_unpacklo_epi8(dst3, zero);
  // Add the inverse transform.
  dst0 = _mm_adds_epi16(dst0, _mm_srai_epi16(m0, 3));
  dst1 = _mm_adds_epi16(dst1, _mm_srai_epi16(m1, 3));
  dst2 = _mm_adds_epi16(dst2, _mm_srai_epi16(m2, 3));
  dst3 = _mm_adds_epi16(dst3, _mm_srai_epi16(m3, 3));
  // Unsigned saturate to 8b.
  dst0 = _mm_packus_epi16(dst0, dst0);
  dst1 = _mm_packus_epi16(dst1, dst1);
  dst2 = _mm_packus_epi16(dst2, dst2);
  dst3 = _mm_packus_epi16(dst3, dst3);
  // Store the results.
  WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(dst0));
  WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(dst1));
  WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(dst2));
  WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(dst3));
}
#undef MUL
#endif   // USE_TRANSFORM_AC3

//------------------------------------------------------------------------------
// Loop Filter (Paragraph 15)

// Compute abs(p - q) = subs(p - q) OR subs(q - p)
#define MM_ABS(p, q)  _mm_or_si128(                                            \
    _mm_subs_epu8((q), (p)),                                                   \
    _mm_subs_epu8((p), (q)))

// Shift each byte of "x" by 3 bits while preserving by the sign bit.
static WEBP_INLINE void SignedShift8b(__m128i* const x) {
  const __m128i zero = _mm_setzero_si128();
  const __m128i lo_0 = _mm_unpacklo_epi8(zero, *x);
  const __m128i hi_0 = _mm_unpackhi_epi8(zero, *x);
  const __m128i lo_1 = _mm_srai_epi16(lo_0, 3 + 8);
  const __m128i hi_1 = _mm_srai_epi16(hi_0, 3 + 8);
  *x = _mm_packs_epi16(lo_1, hi_1);
}

#define FLIP_SIGN_BIT2(a, b) {                                                 \
  a = _mm_xor_si128(a, sign_bit);                                              \
  b = _mm_xor_si128(b, sign_bit);                                              \
}

#define FLIP_SIGN_BIT4(a, b, c, d) {                                           \
  FLIP_SIGN_BIT2(a, b);                                                        \
  FLIP_SIGN_BIT2(c, d);                                                        \
}

// input/output is uint8_t
static WEBP_INLINE void GetNotHEV(const __m128i* const p1,
                                  const __m128i* const p0,
                                  const __m128i* const q0,
                                  const __m128i* const q1,
                                  int hev_thresh, __m128i* const not_hev) {
  const __m128i zero = _mm_setzero_si128();
  const __m128i t_1 = MM_ABS(*p1, *p0);
  const __m128i t_2 = MM_ABS(*q1, *q0);

  const __m128i h = _mm_set1_epi8(hev_thresh);
  const __m128i t_max = _mm_max_epu8(t_1, t_2);

  const __m128i t_max_h = _mm_subs_epu8(t_max, h);
  *not_hev = _mm_cmpeq_epi8(t_max_h, zero);  // not_hev <= t1 && not_hev <= t2
}

// input pixels are int8_t
static WEBP_INLINE void GetBaseDelta(const __m128i* const p1,
                                     const __m128i* const p0,
                                     const __m128i* const q0,
                                     const __m128i* const q1,
                                     __m128i* const delta) {
  // beware of addition order, for saturation!
  const __m128i p1_q1 = _mm_subs_epi8(*p1, *q1);   // p1 - q1
  const __m128i q0_p0 = _mm_subs_epi8(*q0, *p0);   // q0 - p0
  const __m128i s1 = _mm_adds_epi8(p1_q1, q0_p0);  // p1 - q1 + 1 * (q0 - p0)
  const __m128i s2 = _mm_adds_epi8(q0_p0, s1);     // p1 - q1 + 2 * (q0 - p0)
  const __m128i s3 = _mm_adds_epi8(q0_p0, s2);     // p1 - q1 + 3 * (q0 - p0)
  *delta = s3;
}

// input and output are int8_t
static WEBP_INLINE void DoSimpleFilter(__m128i* const p0, __m128i* const q0,
                                       const __m128i* const fl) {
  const __m128i k3 = _mm_set1_epi8(3);
  const __m128i k4 = _mm_set1_epi8(4);
  __m128i v3 = _mm_adds_epi8(*fl, k3);
  __m128i v4 = _mm_adds_epi8(*fl, k4);

  SignedShift8b(&v4);                  // v4 >> 3
  SignedShift8b(&v3);                  // v3 >> 3
  *q0 = _mm_subs_epi8(*q0, v4);        // q0 -= v4
  *p0 = _mm_adds_epi8(*p0, v3);        // p0 += v3
}

// Updates values of 2 pixels at MB edge during complex filtering.
// Update operations:
// q = q - delta and p = p + delta; where delta = [(a_hi >> 7), (a_lo >> 7)]
// Pixels 'pi' and 'qi' are int8_t on input, uint8_t on output (sign flip).
static WEBP_INLINE void Update2Pixels(__m128i* const pi, __m128i* const qi,
                                      const __m128i* const a0_lo,
                                      const __m128i* const a0_hi) {
  const __m128i a1_lo = _mm_srai_epi16(*a0_lo, 7);
  const __m128i a1_hi = _mm_srai_epi16(*a0_hi, 7);
  const __m128i delta = _mm_packs_epi16(a1_lo, a1_hi);
  const __m128i sign_bit = _mm_set1_epi8(0x80);
  *pi = _mm_adds_epi8(*pi, delta);
  *qi = _mm_subs_epi8(*qi, delta);
  FLIP_SIGN_BIT2(*pi, *qi);
}

// input pixels are uint8_t
static WEBP_INLINE void NeedsFilter(const __m128i* const p1,
                                    const __m128i* const p0,
                                    const __m128i* const q0,
                                    const __m128i* const q1,
                                    int thresh, __m128i* const mask) {
  const __m128i m_thresh = _mm_set1_epi8(thresh);
  const __m128i t1 = MM_ABS(*p1, *q1);        // abs(p1 - q1)
  const __m128i kFE = _mm_set1_epi8(0xFE);
  const __m128i t2 = _mm_and_si128(t1, kFE);  // set lsb of each byte to zero
  const __m128i t3 = _mm_srli_epi16(t2, 1);   // abs(p1 - q1) / 2

  const __m128i t4 = MM_ABS(*p0, *q0);        // abs(p0 - q0)
  const __m128i t5 = _mm_adds_epu8(t4, t4);   // abs(p0 - q0) * 2
  const __m128i t6 = _mm_adds_epu8(t5, t3);   // abs(p0-q0)*2 + abs(p1-q1)/2

  const __m128i t7 = _mm_subs_epu8(t6, m_thresh);  // mask <= m_thresh
  *mask = _mm_cmpeq_epi8(t7, _mm_setzero_si128());
}

//------------------------------------------------------------------------------
// Edge filtering functions

// Applies filter on 2 pixels (p0 and q0)
static WEBP_INLINE void DoFilter2(__m128i* const p1, __m128i* const p0,
                                  __m128i* const q0, __m128i* const q1,
                                  int thresh) {
  __m128i a, mask;
  const __m128i sign_bit = _mm_set1_epi8(0x80);
  // convert p1/q1 to int8_t (for GetBaseDelta)
  const __m128i p1s = _mm_xor_si128(*p1, sign_bit);
  const __m128i q1s = _mm_xor_si128(*q1, sign_bit);

  NeedsFilter(p1, p0, q0, q1, thresh, &mask);

  FLIP_SIGN_BIT2(*p0, *q0);
  GetBaseDelta(&p1s, p0, q0, &q1s, &a);
  a = _mm_and_si128(a, mask);     // mask filter values we don't care about
  DoSimpleFilter(p0, q0, &a);
  FLIP_SIGN_BIT2(*p0, *q0);
}

// Applies filter on 4 pixels (p1, p0, q0 and q1)
static WEBP_INLINE void DoFilter4(__m128i* const p1, __m128i* const p0,
                                  __m128i* const q0, __m128i* const q1,
                                  const __m128i* const mask, int hev_thresh) {
  const __m128i zero = _mm_setzero_si128();
  const __m128i sign_bit = _mm_set1_epi8(0x80);
  const __m128i k64 = _mm_set1_epi8(64);
  const __m128i k3 = _mm_set1_epi8(3);
  const __m128i k4 = _mm_set1_epi8(4);
  __m128i not_hev;
  __m128i t1, t2, t3;

  // compute hev mask
  GetNotHEV(p1, p0, q0, q1, hev_thresh, &not_hev);

  // convert to signed values
  FLIP_SIGN_BIT4(*p1, *p0, *q0, *q1);

  t1 = _mm_subs_epi8(*p1, *q1);        // p1 - q1
  t1 = _mm_andnot_si128(not_hev, t1);  // hev(p1 - q1)
  t2 = _mm_subs_epi8(*q0, *p0);        // q0 - p0
  t1 = _mm_adds_epi8(t1, t2);          // hev(p1 - q1) + 1 * (q0 - p0)
  t1 = _mm_adds_epi8(t1, t2);          // hev(p1 - q1) + 2 * (q0 - p0)
  t1 = _mm_adds_epi8(t1, t2);          // hev(p1 - q1) + 3 * (q0 - p0)
  t1 = _mm_and_si128(t1, *mask);       // mask filter values we don't care about

  t2 = _mm_adds_epi8(t1, k3);        // 3 * (q0 - p0) + hev(p1 - q1) + 3
  t3 = _mm_adds_epi8(t1, k4);        // 3 * (q0 - p0) + hev(p1 - q1) + 4
  SignedShift8b(&t2);                // (3 * (q0 - p0) + hev(p1 - q1) + 3) >> 3
  SignedShift8b(&t3);                // (3 * (q0 - p0) + hev(p1 - q1) + 4) >> 3
  *p0 = _mm_adds_epi8(*p0, t2);      // p0 += t2
  *q0 = _mm_subs_epi8(*q0, t3);      // q0 -= t3
  FLIP_SIGN_BIT2(*p0, *q0);

  // this is equivalent to signed (a + 1) >> 1 calculation
  t2 = _mm_add_epi8(t3, sign_bit);
  t3 = _mm_avg_epu8(t2, zero);
  t3 = _mm_sub_epi8(t3, k64);

  t3 = _mm_and_si128(not_hev, t3);   // if !hev
  *q1 = _mm_subs_epi8(*q1, t3);      // q1 -= t3
  *p1 = _mm_adds_epi8(*p1, t3);      // p1 += t3
  FLIP_SIGN_BIT2(*p1, *q1);
}

// Applies filter on 6 pixels (p2, p1, p0, q0, q1 and q2)
static WEBP_INLINE void DoFilter6(__m128i* const p2, __m128i* const p1,
                                  __m128i* const p0, __m128i* const q0,
                                  __m128i* const q1, __m128i* const q2,
                                  const __m128i* const mask, int hev_thresh) {
  const __m128i zero = _mm_setzero_si128();
  const __m128i sign_bit = _mm_set1_epi8(0x80);
  __m128i a, not_hev;

  // compute hev mask
  GetNotHEV(p1, p0, q0, q1, hev_thresh, &not_hev);

  FLIP_SIGN_BIT4(*p1, *p0, *q0, *q1);
  FLIP_SIGN_BIT2(*p2, *q2);
  GetBaseDelta(p1, p0, q0, q1, &a);

  { // do simple filter on pixels with hev
    const __m128i m = _mm_andnot_si128(not_hev, *mask);
    const __m128i f = _mm_and_si128(a, m);
    DoSimpleFilter(p0, q0, &f);
  }

  { // do strong filter on pixels with not hev
    const __m128i k9 = _mm_set1_epi16(0x0900);
    const __m128i k63 = _mm_set1_epi16(63);

    const __m128i m = _mm_and_si128(not_hev, *mask);
    const __m128i f = _mm_and_si128(a, m);

    const __m128i f_lo = _mm_unpacklo_epi8(zero, f);
    const __m128i f_hi = _mm_unpackhi_epi8(zero, f);

    const __m128i f9_lo = _mm_mulhi_epi16(f_lo, k9);    // Filter (lo) * 9
    const __m128i f9_hi = _mm_mulhi_epi16(f_hi, k9);    // Filter (hi) * 9

    const __m128i a2_lo = _mm_add_epi16(f9_lo, k63);    // Filter * 9 + 63
    const __m128i a2_hi = _mm_add_epi16(f9_hi, k63);    // Filter * 9 + 63

    const __m128i a1_lo = _mm_add_epi16(a2_lo, f9_lo);  // Filter * 18 + 63
    const __m128i a1_hi = _mm_add_epi16(a2_hi, f9_hi);  // Filter * 18 + 63

    const __m128i a0_lo = _mm_add_epi16(a1_lo, f9_lo);  // Filter * 27 + 63
    const __m128i a0_hi = _mm_add_epi16(a1_hi, f9_hi);  // Filter * 27 + 63

    Update2Pixels(p2, q2, &a2_lo, &a2_hi);
    Update2Pixels(p1, q1, &a1_lo, &a1_hi);
    Update2Pixels(p0, q0, &a0_lo, &a0_hi);
  }
}

// reads 8 rows across a vertical edge.
static WEBP_INLINE void Load8x4(const uint8_t* const b, int stride,
                                __m128i* const p, __m128i* const q) {
  // A0 = 63 62 61 60 23 22 21 20 43 42 41 40 03 02 01 00
  // A1 = 73 72 71 70 33 32 31 30 53 52 51 50 13 12 11 10
  const __m128i A0 = _mm_set_epi32(
      WebPMemToUint32(&b[6 * stride]), WebPMemToUint32(&b[2 * stride]),
      WebPMemToUint32(&b[4 * stride]), WebPMemToUint32(&b[0 * stride]));
  const __m128i A1 = _mm_set_epi32(
      WebPMemToUint32(&b[7 * stride]), WebPMemToUint32(&b[3 * stride]),
      WebPMemToUint32(&b[5 * stride]), WebPMemToUint32(&b[1 * stride]));

  // B0 = 53 43 52 42 51 41 50 40 13 03 12 02 11 01 10 00
  // B1 = 73 63 72 62 71 61 70 60 33 23 32 22 31 21 30 20
  const __m128i B0 = _mm_unpacklo_epi8(A0, A1);
  const __m128i B1 = _mm_unpackhi_epi8(A0, A1);

  // C0 = 33 23 13 03 32 22 12 02 31 21 11 01 30 20 10 00
  // C1 = 73 63 53 43 72 62 52 42 71 61 51 41 70 60 50 40
  const __m128i C0 = _mm_unpacklo_epi16(B0, B1);
  const __m128i C1 = _mm_unpackhi_epi16(B0, B1);

  // *p = 71 61 51 41 31 21 11 01 70 60 50 40 30 20 10 00
  // *q = 73 63 53 43 33 23 13 03 72 62 52 42 32 22 12 02
  *p = _mm_unpacklo_epi32(C0, C1);
  *q = _mm_unpackhi_epi32(C0, C1);
}

static WEBP_INLINE void Load16x4(const uint8_t* const r0,
                                 const uint8_t* const r8,
                                 int stride,
                                 __m128i* const p1, __m128i* const p0,
                                 __m128i* const q0, __m128i* const q1) {
  // Assume the pixels around the edge (|) are numbered as follows
  //                00 01 | 02 03
  //                10 11 | 12 13
  //                 ...  |  ...
  //                e0 e1 | e2 e3
  //                f0 f1 | f2 f3
  //
  // r0 is pointing to the 0th row (00)
  // r8 is pointing to the 8th row (80)

  // Load
  // p1 = 71 61 51 41 31 21 11 01 70 60 50 40 30 20 10 00
  // q0 = 73 63 53 43 33 23 13 03 72 62 52 42 32 22 12 02
  // p0 = f1 e1 d1 c1 b1 a1 91 81 f0 e0 d0 c0 b0 a0 90 80
  // q1 = f3 e3 d3 c3 b3 a3 93 83 f2 e2 d2 c2 b2 a2 92 82
  Load8x4(r0, stride, p1, q0);
  Load8x4(r8, stride, p0, q1);

  {
    // p1 = f0 e0 d0 c0 b0 a0 90 80 70 60 50 40 30 20 10 00
    // p0 = f1 e1 d1 c1 b1 a1 91 81 71 61 51 41 31 21 11 01
    // q0 = f2 e2 d2 c2 b2 a2 92 82 72 62 52 42 32 22 12 02
    // q1 = f3 e3 d3 c3 b3 a3 93 83 73 63 53 43 33 23 13 03
    const __m128i t1 = *p1;
    const __m128i t2 = *q0;
    *p1 = _mm_unpacklo_epi64(t1, *p0);
    *p0 = _mm_unpackhi_epi64(t1, *p0);
    *q0 = _mm_unpacklo_epi64(t2, *q1);
    *q1 = _mm_unpackhi_epi64(t2, *q1);
  }
}

static WEBP_INLINE void Store4x4(__m128i* const x, uint8_t* dst, int stride) {
  int i;
  for (i = 0; i < 4; ++i, dst += stride) {
    WebPUint32ToMem(dst, _mm_cvtsi128_si32(*x));
    *x = _mm_srli_si128(*x, 4);
  }
}

// Transpose back and store
static WEBP_INLINE void Store16x4(const __m128i* const p1,
                                  const __m128i* const p0,
                                  const __m128i* const q0,
                                  const __m128i* const q1,
                                  uint8_t* r0, uint8_t* r8,
                                  int stride) {
  __m128i t1, p1_s, p0_s, q0_s, q1_s;

  // p0 = 71 70 61 60 51 50 41 40 31 30 21 20 11 10 01 00
  // p1 = f1 f0 e1 e0 d1 d0 c1 c0 b1 b0 a1 a0 91 90 81 80
  t1 = *p0;
  p0_s = _mm_unpacklo_epi8(*p1, t1);
  p1_s = _mm_unpackhi_epi8(*p1, t1);

  // q0 = 73 72 63 62 53 52 43 42 33 32 23 22 13 12 03 02
  // q1 = f3 f2 e3 e2 d3 d2 c3 c2 b3 b2 a3 a2 93 92 83 82
  t1 = *q0;
  q0_s = _mm_unpacklo_epi8(t1, *q1);
  q1_s = _mm_unpackhi_epi8(t1, *q1);

  // p0 = 33 32 31 30 23 22 21 20 13 12 11 10 03 02 01 00
  // q0 = 73 72 71 70 63 62 61 60 53 52 51 50 43 42 41 40
  t1 = p0_s;
  p0_s = _mm_unpacklo_epi16(t1, q0_s);
  q0_s = _mm_unpackhi_epi16(t1, q0_s);

  // p1 = b3 b2 b1 b0 a3 a2 a1 a0 93 92 91 90 83 82 81 80
  // q1 = f3 f2 f1 f0 e3 e2 e1 e0 d3 d2 d1 d0 c3 c2 c1 c0
  t1 = p1_s;
  p1_s = _mm_unpacklo_epi16(t1, q1_s);
  q1_s = _mm_unpackhi_epi16(t1, q1_s);

  Store4x4(&p0_s, r0, stride);
  r0 += 4 * stride;
  Store4x4(&q0_s, r0, stride);

  Store4x4(&p1_s, r8, stride);
  r8 += 4 * stride;
  Store4x4(&q1_s, r8, stride);
}

//------------------------------------------------------------------------------
// Simple In-loop filtering (Paragraph 15.2)

static void SimpleVFilter16(uint8_t* p, int stride, int thresh) {
  // Load
  __m128i p1 = _mm_loadu_si128((__m128i*)&p[-2 * stride]);
  __m128i p0 = _mm_loadu_si128((__m128i*)&p[-stride]);
  __m128i q0 = _mm_loadu_si128((__m128i*)&p[0]);
  __m128i q1 = _mm_loadu_si128((__m128i*)&p[stride]);

  DoFilter2(&p1, &p0, &q0, &q1, thresh);

  // Store
  _mm_storeu_si128((__m128i*)&p[-stride], p0);
  _mm_storeu_si128((__m128i*)&p[0], q0);
}

static void SimpleHFilter16(uint8_t* p, int stride, int thresh) {
  __m128i p1, p0, q0, q1;

  p -= 2;  // beginning of p1

  Load16x4(p, p + 8 * stride, stride, &p1, &p0, &q0, &q1);
  DoFilter2(&p1, &p0, &q0, &q1, thresh);
  Store16x4(&p1, &p0, &q0, &q1, p, p + 8 * stride, stride);
}

static void SimpleVFilter16i(uint8_t* p, int stride, int thresh) {
  int k;
  for (k = 3; k > 0; --k) {
    p += 4 * stride;
    SimpleVFilter16(p, stride, thresh);
  }
}

static void SimpleHFilter16i(uint8_t* p, int stride, int thresh) {
  int k;
  for (k = 3; k > 0; --k) {
    p += 4;
    SimpleHFilter16(p, stride, thresh);
  }
}

//------------------------------------------------------------------------------
// Complex In-loop filtering (Paragraph 15.3)

#define MAX_DIFF1(p3, p2, p1, p0, m) do {                                      \
  m = MM_ABS(p1, p0);                                                          \
  m = _mm_max_epu8(m, MM_ABS(p3, p2));                                         \
  m = _mm_max_epu8(m, MM_ABS(p2, p1));                                         \
} while (0)

#define MAX_DIFF2(p3, p2, p1, p0, m) do {                                      \
  m = _mm_max_epu8(m, MM_ABS(p1, p0));                                         \
  m = _mm_max_epu8(m, MM_ABS(p3, p2));                                         \
  m = _mm_max_epu8(m, MM_ABS(p2, p1));                                         \
} while (0)

#define LOAD_H_EDGES4(p, stride, e1, e2, e3, e4) {                             \
  e1 = _mm_loadu_si128((__m128i*)&(p)[0 * stride]);                            \
  e2 = _mm_loadu_si128((__m128i*)&(p)[1 * stride]);                            \
  e3 = _mm_loadu_si128((__m128i*)&(p)[2 * stride]);                            \
  e4 = _mm_loadu_si128((__m128i*)&(p)[3 * stride]);                            \
}

#define LOADUV_H_EDGE(p, u, v, stride) do {                                    \
  const __m128i U = _mm_loadl_epi64((__m128i*)&(u)[(stride)]);                 \
  const __m128i V = _mm_loadl_epi64((__m128i*)&(v)[(stride)]);                 \
  p = _mm_unpacklo_epi64(U, V);                                                \
} while (0)

#define LOADUV_H_EDGES4(u, v, stride, e1, e2, e3, e4) {                        \
  LOADUV_H_EDGE(e1, u, v, 0 * stride);                                         \
  LOADUV_H_EDGE(e2, u, v, 1 * stride);                                         \
  LOADUV_H_EDGE(e3, u, v, 2 * stride);                                         \
  LOADUV_H_EDGE(e4, u, v, 3 * stride);                                         \
}

#define STOREUV(p, u, v, stride) {                                             \
  _mm_storel_epi64((__m128i*)&u[(stride)], p);                                 \
  p = _mm_srli_si128(p, 8);                                                    \
  _mm_storel_epi64((__m128i*)&v[(stride)], p);                                 \
}

static WEBP_INLINE void ComplexMask(const __m128i* const p1,
                                    const __m128i* const p0,
                                    const __m128i* const q0,
                                    const __m128i* const q1,
                                    int thresh, int ithresh,
                                    __m128i* const mask) {
  const __m128i it = _mm_set1_epi8(ithresh);
  const __m128i diff = _mm_subs_epu8(*mask, it);
  const __m128i thresh_mask = _mm_cmpeq_epi8(diff, _mm_setzero_si128());
  __m128i filter_mask;
  NeedsFilter(p1, p0, q0, q1, thresh, &filter_mask);
  *mask = _mm_and_si128(thresh_mask, filter_mask);
}

// on macroblock edges
static void VFilter16(uint8_t* p, int stride,
                      int thresh, int ithresh, int hev_thresh) {
  __m128i t1;
  __m128i mask;
  __m128i p2, p1, p0, q0, q1, q2;

  // Load p3, p2, p1, p0
  LOAD_H_EDGES4(p - 4 * stride, stride, t1, p2, p1, p0);
  MAX_DIFF1(t1, p2, p1, p0, mask);

  // Load q0, q1, q2, q3
  LOAD_H_EDGES4(p, stride, q0, q1, q2, t1);
  MAX_DIFF2(t1, q2, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter6(&p2, &p1, &p0, &q0, &q1, &q2, &mask, hev_thresh);

  // Store
  _mm_storeu_si128((__m128i*)&p[-3 * stride], p2);
  _mm_storeu_si128((__m128i*)&p[-2 * stride], p1);
  _mm_storeu_si128((__m128i*)&p[-1 * stride], p0);
  _mm_storeu_si128((__m128i*)&p[+0 * stride], q0);
  _mm_storeu_si128((__m128i*)&p[+1 * stride], q1);
  _mm_storeu_si128((__m128i*)&p[+2 * stride], q2);
}

static void HFilter16(uint8_t* p, int stride,
                      int thresh, int ithresh, int hev_thresh) {
  __m128i mask;
  __m128i p3, p2, p1, p0, q0, q1, q2, q3;

  uint8_t* const b = p - 4;
  Load16x4(b, b + 8 * stride, stride, &p3, &p2, &p1, &p0);  // p3, p2, p1, p0
  MAX_DIFF1(p3, p2, p1, p0, mask);

  Load16x4(p, p + 8 * stride, stride, &q0, &q1, &q2, &q3);  // q0, q1, q2, q3
  MAX_DIFF2(q3, q2, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter6(&p2, &p1, &p0, &q0, &q1, &q2, &mask, hev_thresh);

  Store16x4(&p3, &p2, &p1, &p0, b, b + 8 * stride, stride);
  Store16x4(&q0, &q1, &q2, &q3, p, p + 8 * stride, stride);
}

// on three inner edges
static void VFilter16i(uint8_t* p, int stride,
                       int thresh, int ithresh, int hev_thresh) {
  int k;
  __m128i p3, p2, p1, p0;   // loop invariants

  LOAD_H_EDGES4(p, stride, p3, p2, p1, p0);  // prologue

  for (k = 3; k > 0; --k) {
    __m128i mask, tmp1, tmp2;
    uint8_t* const b = p + 2 * stride;   // beginning of p1
    p += 4 * stride;

    MAX_DIFF1(p3, p2, p1, p0, mask);   // compute partial mask
    LOAD_H_EDGES4(p, stride, p3, p2, tmp1, tmp2);
    MAX_DIFF2(p3, p2, tmp1, tmp2, mask);

    // p3 and p2 are not just temporary variables here: they will be
    // re-used for next span. And q2/q3 will become p1/p0 accordingly.
    ComplexMask(&p1, &p0, &p3, &p2, thresh, ithresh, &mask);
    DoFilter4(&p1, &p0, &p3, &p2, &mask, hev_thresh);

    // Store
    _mm_storeu_si128((__m128i*)&b[0 * stride], p1);
    _mm_storeu_si128((__m128i*)&b[1 * stride], p0);
    _mm_storeu_si128((__m128i*)&b[2 * stride], p3);
    _mm_storeu_si128((__m128i*)&b[3 * stride], p2);

    // rotate samples
    p1 = tmp1;
    p0 = tmp2;
  }
}

static void HFilter16i(uint8_t* p, int stride,
                       int thresh, int ithresh, int hev_thresh) {
  int k;
  __m128i p3, p2, p1, p0;   // loop invariants

  Load16x4(p, p + 8 * stride, stride, &p3, &p2, &p1, &p0);  // prologue

  for (k = 3; k > 0; --k) {
    __m128i mask, tmp1, tmp2;
    uint8_t* const b = p + 2;   // beginning of p1

    p += 4;  // beginning of q0 (and next span)

    MAX_DIFF1(p3, p2, p1, p0, mask);   // compute partial mask
    Load16x4(p, p + 8 * stride, stride, &p3, &p2, &tmp1, &tmp2);
    MAX_DIFF2(p3, p2, tmp1, tmp2, mask);

    ComplexMask(&p1, &p0, &p3, &p2, thresh, ithresh, &mask);
    DoFilter4(&p1, &p0, &p3, &p2, &mask, hev_thresh);

    Store16x4(&p1, &p0, &p3, &p2, b, b + 8 * stride, stride);

    // rotate samples
    p1 = tmp1;
    p0 = tmp2;
  }
}

// 8-pixels wide variant, for chroma filtering
static void VFilter8(uint8_t* u, uint8_t* v, int stride,
                     int thresh, int ithresh, int hev_thresh) {
  __m128i mask;
  __m128i t1, p2, p1, p0, q0, q1, q2;

  // Load p3, p2, p1, p0
  LOADUV_H_EDGES4(u - 4 * stride, v - 4 * stride, stride, t1, p2, p1, p0);
  MAX_DIFF1(t1, p2, p1, p0, mask);

  // Load q0, q1, q2, q3
  LOADUV_H_EDGES4(u, v, stride, q0, q1, q2, t1);
  MAX_DIFF2(t1, q2, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter6(&p2, &p1, &p0, &q0, &q1, &q2, &mask, hev_thresh);

  // Store
  STOREUV(p2, u, v, -3 * stride);
  STOREUV(p1, u, v, -2 * stride);
  STOREUV(p0, u, v, -1 * stride);
  STOREUV(q0, u, v, 0 * stride);
  STOREUV(q1, u, v, 1 * stride);
  STOREUV(q2, u, v, 2 * stride);
}

static void HFilter8(uint8_t* u, uint8_t* v, int stride,
                     int thresh, int ithresh, int hev_thresh) {
  __m128i mask;
  __m128i p3, p2, p1, p0, q0, q1, q2, q3;

  uint8_t* const tu = u - 4;
  uint8_t* const tv = v - 4;
  Load16x4(tu, tv, stride, &p3, &p2, &p1, &p0);  // p3, p2, p1, p0
  MAX_DIFF1(p3, p2, p1, p0, mask);

  Load16x4(u, v, stride, &q0, &q1, &q2, &q3);    // q0, q1, q2, q3
  MAX_DIFF2(q3, q2, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter6(&p2, &p1, &p0, &q0, &q1, &q2, &mask, hev_thresh);

  Store16x4(&p3, &p2, &p1, &p0, tu, tv, stride);
  Store16x4(&q0, &q1, &q2, &q3, u, v, stride);
}

static void VFilter8i(uint8_t* u, uint8_t* v, int stride,
                      int thresh, int ithresh, int hev_thresh) {
  __m128i mask;
  __m128i t1, t2, p1, p0, q0, q1;

  // Load p3, p2, p1, p0
  LOADUV_H_EDGES4(u, v, stride, t2, t1, p1, p0);
  MAX_DIFF1(t2, t1, p1, p0, mask);

  u += 4 * stride;
  v += 4 * stride;

  // Load q0, q1, q2, q3
  LOADUV_H_EDGES4(u, v, stride, q0, q1, t1, t2);
  MAX_DIFF2(t2, t1, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter4(&p1, &p0, &q0, &q1, &mask, hev_thresh);

  // Store
  STOREUV(p1, u, v, -2 * stride);
  STOREUV(p0, u, v, -1 * stride);
  STOREUV(q0, u, v, 0 * stride);
  STOREUV(q1, u, v, 1 * stride);
}

static void HFilter8i(uint8_t* u, uint8_t* v, int stride,
                      int thresh, int ithresh, int hev_thresh) {
  __m128i mask;
  __m128i t1, t2, p1, p0, q0, q1;
  Load16x4(u, v, stride, &t2, &t1, &p1, &p0);   // p3, p2, p1, p0
  MAX_DIFF1(t2, t1, p1, p0, mask);

  u += 4;  // beginning of q0
  v += 4;
  Load16x4(u, v, stride, &q0, &q1, &t1, &t2);  // q0, q1, q2, q3
  MAX_DIFF2(t2, t1, q1, q0, mask);

  ComplexMask(&p1, &p0, &q0, &q1, thresh, ithresh, &mask);
  DoFilter4(&p1, &p0, &q0, &q1, &mask, hev_thresh);

  u -= 2;  // beginning of p1
  v -= 2;
  Store16x4(&p1, &p0, &q0, &q1, u, v, stride);
}

//------------------------------------------------------------------------------
// 4x4 predictions

#define DST(x, y) dst[(x) + (y) * BPS]
#define AVG3(a, b, c) (((a) + 2 * (b) + (c) + 2) >> 2)

// We use the following 8b-arithmetic tricks:
//     (a + 2 * b + c + 2) >> 2 = (AC + b + 1) >> 1
//   where: AC = (a + c) >> 1 = [(a + c + 1) >> 1] - [(a^c) & 1]
// and:
//     (a + 2 * b + c + 2) >> 2 = (AB + BC + 1) >> 1 - (ab|bc)&lsb
//   where: AC = (a + b + 1) >> 1,   BC = (b + c + 1) >> 1
//   and ab = a ^ b, bc = b ^ c, lsb = (AC^BC)&1

static void VE4(uint8_t* dst) {    // vertical
  const __m128i one = _mm_set1_epi8(1);
  const __m128i ABCDEFGH = _mm_loadl_epi64((__m128i*)(dst - BPS - 1));
  const __m128i BCDEFGH0 = _mm_srli_si128(ABCDEFGH, 1);
  const __m128i CDEFGH00 = _mm_srli_si128(ABCDEFGH, 2);
  const __m128i a = _mm_avg_epu8(ABCDEFGH, CDEFGH00);
  const __m128i lsb = _mm_and_si128(_mm_xor_si128(ABCDEFGH, CDEFGH00), one);
  const __m128i b = _mm_subs_epu8(a, lsb);
  const __m128i avg = _mm_avg_epu8(b, BCDEFGH0);
  const uint32_t vals = _mm_cvtsi128_si32(avg);
  int i;
  for (i = 0; i < 4; ++i) {
    WebPUint32ToMem(dst + i * BPS, vals);
  }
}

static void LD4(uint8_t* dst) {   // Down-Left
  const __m128i one = _mm_set1_epi8(1);
  const __m128i ABCDEFGH = _mm_loadl_epi64((__m128i*)(dst - BPS));
  const __m128i BCDEFGH0 = _mm_srli_si128(ABCDEFGH, 1);
  const __m128i CDEFGH00 = _mm_srli_si128(ABCDEFGH, 2);
  const __m128i CDEFGHH0 = _mm_insert_epi16(CDEFGH00, dst[-BPS + 7], 3);
  const __m128i avg1 = _mm_avg_epu8(ABCDEFGH, CDEFGHH0);
  const __m128i lsb = _mm_and_si128(_mm_xor_si128(ABCDEFGH, CDEFGHH0), one);
  const __m128i avg2 = _mm_subs_epu8(avg1, lsb);
  const __m128i abcdefg = _mm_avg_epu8(avg2, BCDEFGH0);
  WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(               abcdefg    ));
  WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 1)));
  WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 2)));
  WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 3)));
}

static void VR4(uint8_t* dst) {   // Vertical-Right
  const __m128i one = _mm_set1_epi8(1);
  const int I = dst[-1 + 0 * BPS];
  const int J = dst[-1 + 1 * BPS];
  const int K = dst[-1 + 2 * BPS];
  const int X = dst[-1 - BPS];
  const __m128i XABCD = _mm_loadl_epi64((__m128i*)(dst - BPS - 1));
  const __m128i ABCD0 = _mm_srli_si128(XABCD, 1);
  const __m128i abcd = _mm_avg_epu8(XABCD, ABCD0);
  const __m128i _XABCD = _mm_slli_si128(XABCD, 1);
  const __m128i IXABCD = _mm_insert_epi16(_XABCD, I | (X << 8), 0);
  const __m128i avg1 = _mm_avg_epu8(IXABCD, ABCD0);
  const __m128i lsb = _mm_and_si128(_mm_xor_si128(IXABCD, ABCD0), one);
  const __m128i avg2 = _mm_subs_epu8(avg1, lsb);
  const __m128i efgh = _mm_avg_epu8(avg2, XABCD);
  WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(               abcd    ));
  WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(               efgh    ));
  WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(_mm_slli_si128(abcd, 1)));
  WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(_mm_slli_si128(efgh, 1)));

  // these two are hard to implement in SSE2, so we keep the C-version:
  DST(0, 2) = AVG3(J, I, X);
  DST(0, 3) = AVG3(K, J, I);
}

static void VL4(uint8_t* dst) {   // Vertical-Left
  const __m128i one = _mm_set1_epi8(1);
  const __m128i ABCDEFGH = _mm_loadl_epi64((__m128i*)(dst - BPS));
  const __m128i BCDEFGH_ = _mm_srli_si128(ABCDEFGH, 1);
  const __m128i CDEFGH__ = _mm_srli_si128(ABCDEFGH, 2);
  const __m128i avg1 = _mm_avg_epu8(ABCDEFGH, BCDEFGH_);
  const __m128i avg2 = _mm_avg_epu8(CDEFGH__, BCDEFGH_);
  const __m128i avg3 = _mm_avg_epu8(avg1, avg2);
  const __m128i lsb1 = _mm_and_si128(_mm_xor_si128(avg1, avg2), one);
  const __m128i ab = _mm_xor_si128(ABCDEFGH, BCDEFGH_);
  const __m128i bc = _mm_xor_si128(CDEFGH__, BCDEFGH_);
  const __m128i abbc = _mm_or_si128(ab, bc);
  const __m128i lsb2 = _mm_and_si128(abbc, lsb1);
  const __m128i avg4 = _mm_subs_epu8(avg3, lsb2);
  const uint32_t extra_out = _mm_cvtsi128_si32(_mm_srli_si128(avg4, 4));
  WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(               avg1    ));
  WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(               avg4    ));
  WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(avg1, 1)));
  WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(avg4, 1)));

  // these two are hard to get and irregular
  DST(3, 2) = (extra_out >> 0) & 0xff;
  DST(3, 3) = (extra_out >> 8) & 0xff;
}

static void RD4(uint8_t* dst) {   // Down-right
  const __m128i one = _mm_set1_epi8(1);
  const __m128i XABCD = _mm_loadl_epi64((__m128i*)(dst - BPS - 1));
  const __m128i ____XABCD = _mm_slli_si128(XABCD, 4);
  const uint32_t I = dst[-1 + 0 * BPS];
  const uint32_t J = dst[-1 + 1 * BPS];
  const uint32_t K = dst[-1 + 2 * BPS];
  const uint32_t L = dst[-1 + 3 * BPS];
  const __m128i LKJI_____ =
      _mm_cvtsi32_si128(L | (K << 8) | (J << 16) | (I << 24));
  const __m128i LKJIXABCD = _mm_or_si128(LKJI_____, ____XABCD);
  const __m128i KJIXABCD_ = _mm_srli_si128(LKJIXABCD, 1);
  const __m128i JIXABCD__ = _mm_srli_si128(LKJIXABCD, 2);
  const __m128i avg1 = _mm_avg_epu8(JIXABCD__, LKJIXABCD);
  const __m128i lsb = _mm_and_si128(_mm_xor_si128(JIXABCD__, LKJIXABCD), one);
  const __m128i avg2 = _mm_subs_epu8(avg1, lsb);
  const __m128i abcdefg = _mm_avg_epu8(avg2, KJIXABCD_);
  WebPUint32ToMem(dst + 3 * BPS, _mm_cvtsi128_si32(               abcdefg    ));
  WebPUint32ToMem(dst + 2 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 1)));
  WebPUint32ToMem(dst + 1 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 2)));
  WebPUint32ToMem(dst + 0 * BPS, _mm_cvtsi128_si32(_mm_srli_si128(abcdefg, 3)));
}

#undef DST
#undef AVG3

//------------------------------------------------------------------------------
// Luma 16x16

static WEBP_INLINE void TrueMotion(uint8_t* dst, int size) {
  const uint8_t* top = dst - BPS;
  const __m128i zero = _mm_setzero_si128();
  int y;
  if (size == 4) {
    const __m128i top_values = _mm_cvtsi32_si128(WebPMemToUint32(top));
    const __m128i top_base = _mm_unpacklo_epi8(top_values, zero);
    for (y = 0; y < 4; ++y, dst += BPS) {
      const int val = dst[-1] - top[-1];
      const __m128i base = _mm_set1_epi16(val);
      const __m128i out = _mm_packus_epi16(_mm_add_epi16(base, top_base), zero);
      WebPUint32ToMem(dst, _mm_cvtsi128_si32(out));
    }
  } else if (size == 8) {
    const __m128i top_values = _mm_loadl_epi64((const __m128i*)top);
    const __m128i top_base = _mm_unpacklo_epi8(top_values, zero);
    for (y = 0; y < 8; ++y, dst += BPS) {
      const int val = dst[-1] - top[-1];
      const __m128i base = _mm_set1_epi16(val);
      const __m128i out = _mm_packus_epi16(_mm_add_epi16(base, top_base), zero);
      _mm_storel_epi64((__m128i*)dst, out);
    }
  } else {
    const __m128i top_values = _mm_loadu_si128((const __m128i*)top);
    const __m128i top_base_0 = _mm_unpacklo_epi8(top_values, zero);
    const __m128i top_base_1 = _mm_unpackhi_epi8(top_values, zero);
    for (y = 0; y < 16; ++y, dst += BPS) {
      const int val = dst[-1] - top[-1];
      const __m128i base = _mm_set1_epi16(val);
      const __m128i out_0 = _mm_add_epi16(base, top_base_0);
      const __m128i out_1 = _mm_add_epi16(base, top_base_1);
      const __m128i out = _mm_packus_epi16(out_0, out_1);
      _mm_storeu_si128((__m128i*)dst, out);
    }
  }
}

static void TM4(uint8_t* dst)   { TrueMotion(dst, 4); }
static void TM8uv(uint8_t* dst) { TrueMotion(dst, 8); }
static void TM16(uint8_t* dst)  { TrueMotion(dst, 16); }

static void VE16(uint8_t* dst) {
  const __m128i top = _mm_loadu_si128((const __m128i*)(dst - BPS));
  int j;
  for (j = 0; j < 16; ++j) {
    _mm_storeu_si128((__m128i*)(dst + j * BPS), top);
  }
}

static void HE16(uint8_t* dst) {     // horizontal
  int j;
  for (j = 16; j > 0; --j) {
    const __m128i values = _mm_set1_epi8(dst[-1]);
    _mm_storeu_si128((__m128i*)dst, values);
    dst += BPS;
  }
}

static WEBP_INLINE void Put16(uint8_t v, uint8_t* dst) {
  int j;
  const __m128i values = _mm_set1_epi8(v);
  for (j = 0; j < 16; ++j) {
    _mm_storeu_si128((__m128i*)(dst + j * BPS), values);
  }
}

static void DC16(uint8_t* dst) {    // DC
  const __m128i zero = _mm_setzero_si128();
  const __m128i top = _mm_loadu_si128((const __m128i*)(dst - BPS));
  const __m128i sad8x2 = _mm_sad_epu8(top, zero);
  // sum the two sads: sad8x2[0:1] + sad8x2[8:9]
  const __m128i sum = _mm_add_epi16(sad8x2, _mm_shuffle_epi32(sad8x2, 2));
  int left = 0;
  int j;
  for (j = 0; j < 16; ++j) {
    left += dst[-1 + j * BPS];
  }
  {
    const int DC = _mm_cvtsi128_si32(sum) + left + 16;
    Put16(DC >> 5, dst);
  }
}

static void DC16NoTop(uint8_t* dst) {   // DC with top samples not available
  int DC = 8;
  int j;
  for (j = 0; j < 16; ++j) {
    DC += dst[-1 + j * BPS];
  }
  Put16(DC >> 4, dst);
}

static void DC16NoLeft(uint8_t* dst) {  // DC with left samples not available
  const __m128i zero = _mm_setzero_si128();
  const __m128i top = _mm_loadu_si128((const __m128i*)(dst - BPS));
  const __m128i sad8x2 = _mm_sad_epu8(top, zero);
  // sum the two sads: sad8x2[0:1] + sad8x2[8:9]
  const __m128i sum = _mm_add_epi16(sad8x2, _mm_shuffle_epi32(sad8x2, 2));
  const int DC = _mm_cvtsi128_si32(sum) + 8;
  Put16(DC >> 4, dst);
}

static void DC16NoTopLeft(uint8_t* dst) {  // DC with no top and left samples
  Put16(0x80, dst);
}

//------------------------------------------------------------------------------
// Chroma

static void VE8uv(uint8_t* dst) {    // vertical
  int j;
  const __m128i top = _mm_loadl_epi64((const __m128i*)(dst - BPS));
  for (j = 0; j < 8; ++j) {
    _mm_storel_epi64((__m128i*)(dst + j * BPS), top);
  }
}

static void HE8uv(uint8_t* dst) {    // horizontal
  int j;
  for (j = 0; j < 8; ++j) {
    const __m128i values = _mm_set1_epi8(dst[-1]);
    _mm_storel_epi64((__m128i*)dst, values);
    dst += BPS;
  }
}

// helper for chroma-DC predictions
static WEBP_INLINE void Put8x8uv(uint8_t v, uint8_t* dst) {
  int j;
  const __m128i values = _mm_set1_epi8(v);
  for (j = 0; j < 8; ++j) {
    _mm_storel_epi64((__m128i*)(dst + j * BPS), values);
  }
}

static void DC8uv(uint8_t* dst) {     // DC
  const __m128i zero = _mm_setzero_si128();
  const __m128i top = _mm_loadl_epi64((const __m128i*)(dst - BPS));
  const __m128i sum = _mm_sad_epu8(top, zero);
  int left = 0;
  int j;
  for (j = 0; j < 8; ++j) {
    left += dst[-1 + j * BPS];
  }
  {
    const int DC = _mm_cvtsi128_si32(sum) + left + 8;
    Put8x8uv(DC >> 4, dst);
  }
}

static void DC8uvNoLeft(uint8_t* dst) {   // DC with no left samples
  const __m128i zero = _mm_setzero_si128();
  const __m128i top = _mm_loadl_epi64((const __m128i*)(dst - BPS));
  const __m128i sum = _mm_sad_epu8(top, zero);
  const int DC = _mm_cvtsi128_si32(sum) + 4;
  Put8x8uv(DC >> 3, dst);
}

static void DC8uvNoTop(uint8_t* dst) {  // DC with no top samples
  int dc0 = 4;
  int i;
  for (i = 0; i < 8; ++i) {
    dc0 += dst[-1 + i * BPS];
  }
  Put8x8uv(dc0 >> 3, dst);
}

static void DC8uvNoTopLeft(uint8_t* dst) {    // DC with nothing
  Put8x8uv(0x80, dst);
}

//------------------------------------------------------------------------------
// Entry point

extern void VP8DspInitSSE2(void);

WEBP_TSAN_IGNORE_FUNCTION void VP8DspInitSSE2(void) {
  VP8Transform = Transform;
#if defined(USE_TRANSFORM_AC3)
  VP8TransformAC3 = TransformAC3;
#endif

  VP8VFilter16 = VFilter16;
  VP8HFilter16 = HFilter16;
  VP8VFilter8 = VFilter8;
  VP8HFilter8 = HFilter8;
  VP8VFilter16i = VFilter16i;
  VP8HFilter16i = HFilter16i;
  VP8VFilter8i = VFilter8i;
  VP8HFilter8i = HFilter8i;

  VP8SimpleVFilter16 = SimpleVFilter16;
  VP8SimpleHFilter16 = SimpleHFilter16;
  VP8SimpleVFilter16i = SimpleVFilter16i;
  VP8SimpleHFilter16i = SimpleHFilter16i;

  VP8PredLuma4[1] = TM4;
  VP8PredLuma4[2] = VE4;
  VP8PredLuma4[4] = RD4;
  VP8PredLuma4[5] = VR4;
  VP8PredLuma4[6] = LD4;
  VP8PredLuma4[7] = VL4;

  VP8PredLuma16[0] = DC16;
  VP8PredLuma16[1] = TM16;
  VP8PredLuma16[2] = VE16;
  VP8PredLuma16[3] = HE16;
  VP8PredLuma16[4] = DC16NoTop;
  VP8PredLuma16[5] = DC16NoLeft;
  VP8PredLuma16[6] = DC16NoTopLeft;

  VP8PredChroma8[0] = DC8uv;
  VP8PredChroma8[1] = TM8uv;
  VP8PredChroma8[2] = VE8uv;
  VP8PredChroma8[3] = HE8uv;
  VP8PredChroma8[4] = DC8uvNoTop;
  VP8PredChroma8[5] = DC8uvNoLeft;
  VP8PredChroma8[6] = DC8uvNoTopLeft;
}

#else  // !WEBP_USE_SSE2

WEBP_DSP_INIT_STUB(VP8DspInitSSE2)

#endif  // WEBP_USE_SSE2

// Copyright 2014 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// MIPS version of YUV to RGB upsampling functions.
//
// Author(s):  Djordje Pesut    (djordje.pesut@imgtec.com)
//             Jovan Zelincevic (jovan.zelincevic@imgtec.com)

#include "./dsp.h"

#if defined(WEBP_USE_MIPS32)

#include "./yuv.h"

//------------------------------------------------------------------------------
// simple point-sampling

#define SAMPLE_FUNC_MIPS(FUNC_NAME, XSTEP, R, G, B, A)                         \
static void FUNC_NAME(const uint8_t* top_y, const uint8_t* bottom_y,           \
                      const uint8_t* u, const uint8_t* v,                      \
                      uint8_t* top_dst, uint8_t* bottom_dst, int len) {        \
  int i, r, g, b;                                                              \
  int temp0, temp1, temp2, temp3, temp4;                                       \
  for (i = 0; i < (len >> 1); i++) {                                           \
    temp1 = kVToR * v[0];                                                      \
    temp3 = kVToG * v[0];                                                      \
    temp2 = kUToG * u[0];                                                      \
    temp4 = kUToB * u[0];                                                      \
    temp0 = kYScale * top_y[0];                                                \
    temp1 += kRCst;                                                            \
    temp3 -= kGCst;                                                            \
    temp2 += temp3;                                                            \
    temp4 += kBCst;                                                            \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    temp0 = kYScale * top_y[1];                                                \
    top_dst[R] = r;                                                            \
    top_dst[G] = g;                                                            \
    top_dst[B] = b;                                                            \
    if (A) top_dst[A] = 0xff;                                                  \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    temp0 = kYScale * bottom_y[0];                                             \
    top_dst[R + XSTEP] = r;                                                    \
    top_dst[G + XSTEP] = g;                                                    \
    top_dst[B + XSTEP] = b;                                                    \
    if (A) top_dst[A + XSTEP] = 0xff;                                          \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    temp0 = kYScale * bottom_y[1];                                             \
    bottom_dst[R] = r;                                                         \
    bottom_dst[G] = g;                                                         \
    bottom_dst[B] = b;                                                         \
    if (A) bottom_dst[A] = 0xff;                                               \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    bottom_dst[R + XSTEP] = r;                                                 \
    bottom_dst[G + XSTEP] = g;                                                 \
    bottom_dst[B + XSTEP] = b;                                                 \
    if (A) bottom_dst[A + XSTEP] = 0xff;                                       \
    top_y += 2;                                                                \
    bottom_y += 2;                                                             \
    u++;                                                                       \
    v++;                                                                       \
    top_dst += 2 * XSTEP;                                                      \
    bottom_dst += 2 * XSTEP;                                                   \
  }                                                                            \
  if (len & 1) {                                                               \
    temp1 = kVToR * v[0];                                                      \
    temp3 = kVToG * v[0];                                                      \
    temp2 = kUToG * u[0];                                                      \
    temp4 = kUToB * u[0];                                                      \
    temp0 = kYScale * top_y[0];                                                \
    temp1 += kRCst;                                                            \
    temp3 -= kGCst;                                                            \
    temp2 += temp3;                                                            \
    temp4 += kBCst;                                                            \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    temp0 = kYScale * bottom_y[0];                                             \
    top_dst[R] = r;                                                            \
    top_dst[G] = g;                                                            \
    top_dst[B] = b;                                                            \
    if (A) top_dst[A] = 0xff;                                                  \
    r = VP8Clip8(temp0 + temp1);                                               \
    g = VP8Clip8(temp0 - temp2);                                               \
    b = VP8Clip8(temp0 + temp4);                                               \
    bottom_dst[R] = r;                                                         \
    bottom_dst[G] = g;                                                         \
    bottom_dst[B] = b;                                                         \
    if (A) bottom_dst[A] = 0xff;                                               \
  }                                                                            \
}

SAMPLE_FUNC_MIPS(SampleRgbLinePairMIPS,      3, 0, 1, 2, 0)
SAMPLE_FUNC_MIPS(SampleRgbaLinePairMIPS,     4, 0, 1, 2, 3)
SAMPLE_FUNC_MIPS(SampleBgrLinePairMIPS,      3, 2, 1, 0, 0)
SAMPLE_FUNC_MIPS(SampleBgraLinePairMIPS,     4, 2, 1, 0, 3)

#endif   // WEBP_USE_MIPS32

//------------------------------------------------------------------------------

void WebPInitSamplersMIPS32(void) {
#if defined(WEBP_USE_MIPS32)
  WebPSamplers[MODE_RGB]  = SampleRgbLinePairMIPS;
  WebPSamplers[MODE_RGBA] = SampleRgbaLinePairMIPS;
  WebPSamplers[MODE_BGR]  = SampleBgrLinePairMIPS;
  WebPSamplers[MODE_BGRA] = SampleBgraLinePairMIPS;
#endif  // WEBP_USE_MIPS32
}

// Copyright 2011 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// YUV to RGB upsampling functions.
//
// Author: somnath@google.com (Somnath Banerjee)

#include "./dsp.h"
#include "./yuv.h"

#include <assert.h>

//------------------------------------------------------------------------------
// Fancy upsampler

#ifdef FANCY_UPSAMPLING

// Fancy upsampling functions to convert YUV to RGB
WebPUpsampleLinePairFunc WebPUpsamplers[MODE_LAST];

// Given samples laid out in a square as:
//  [a b]
//  [c d]
// we interpolate u/v as:
//  ([9*a + 3*b + 3*c +   d    3*a + 9*b + 3*c +   d] + [8 8]) / 16
//  ([3*a +   b + 9*c + 3*d      a + 3*b + 3*c + 9*d]   [8 8]) / 16

// We process u and v together stashed into 32bit (16bit each).
#define LOAD_UV(u, v) ((u) | ((v) << 16))

#define UPSAMPLE_FUNC(FUNC_NAME, FUNC, XSTEP)                                  \
static void FUNC_NAME(const uint8_t* top_y, const uint8_t* bottom_y,           \
                      const uint8_t* top_u, const uint8_t* top_v,              \
                      const uint8_t* cur_u, const uint8_t* cur_v,              \
                      uint8_t* top_dst, uint8_t* bottom_dst, int len) {        \
  int x;                                                                       \
  const int last_pixel_pair = (len - 1) >> 1;                                  \
  uint32_t tl_uv = LOAD_UV(top_u[0], top_v[0]);   /* top-left sample */        \
  uint32_t l_uv  = LOAD_UV(cur_u[0], cur_v[0]);   /* left-sample */            \
  assert(top_y != NULL);                                                       \
  {                                                                            \
    const uint32_t uv0 = (3 * tl_uv + l_uv + 0x00020002u) >> 2;                \
    FUNC(top_y[0], uv0 & 0xff, (uv0 >> 16), top_dst);                          \
  }                                                                            \
  if (bottom_y != NULL) {                                                      \
    const uint32_t uv0 = (3 * l_uv + tl_uv + 0x00020002u) >> 2;                \
    FUNC(bottom_y[0], uv0 & 0xff, (uv0 >> 16), bottom_dst);                    \
  }                                                                            \
  for (x = 1; x <= last_pixel_pair; ++x) {                                     \
    const uint32_t t_uv = LOAD_UV(top_u[x], top_v[x]);  /* top sample */       \
    const uint32_t uv   = LOAD_UV(cur_u[x], cur_v[x]);  /* sample */           \
    /* precompute invariant values associated with first and second diagonals*/\
    const uint32_t avg = tl_uv + t_uv + l_uv + uv + 0x00080008u;               \
    const uint32_t diag_12 = (avg + 2 * (t_uv + l_uv)) >> 3;                   \
    const uint32_t diag_03 = (avg + 2 * (tl_uv + uv)) >> 3;                    \
    {                                                                          \
      const uint32_t uv0 = (diag_12 + tl_uv) >> 1;                             \
      const uint32_t uv1 = (diag_03 + t_uv) >> 1;                              \
      FUNC(top_y[2 * x - 1], uv0 & 0xff, (uv0 >> 16),                          \
           top_dst + (2 * x - 1) * XSTEP);                                     \
      FUNC(top_y[2 * x - 0], uv1 & 0xff, (uv1 >> 16),                          \
           top_dst + (2 * x - 0) * XSTEP);                                     \
    }                                                                          \
    if (bottom_y != NULL) {                                                    \
      const uint32_t uv0 = (diag_03 + l_uv) >> 1;                              \
      const uint32_t uv1 = (diag_12 + uv) >> 1;                                \
      FUNC(bottom_y[2 * x - 1], uv0 & 0xff, (uv0 >> 16),                       \
           bottom_dst + (2 * x - 1) * XSTEP);                                  \
      FUNC(bottom_y[2 * x + 0], uv1 & 0xff, (uv1 >> 16),                       \
           bottom_dst + (2 * x + 0) * XSTEP);                                  \
    }                                                                          \
    tl_uv = t_uv;                                                              \
    l_uv = uv;                                                                 \
  }                                                                            \
  if (!(len & 1)) {                                                            \
    {                                                                          \
      const uint32_t uv0 = (3 * tl_uv + l_uv + 0x00020002u) >> 2;              \
      FUNC(top_y[len - 1], uv0 & 0xff, (uv0 >> 16),                            \
           top_dst + (len - 1) * XSTEP);                                       \
    }                                                                          \
    if (bottom_y != NULL) {                                                    \
      const uint32_t uv0 = (3 * l_uv + tl_uv + 0x00020002u) >> 2;              \
      FUNC(bottom_y[len - 1], uv0 & 0xff, (uv0 >> 16),                         \
           bottom_dst + (len - 1) * XSTEP);                                    \
    }                                                                          \
  }                                                                            \
}

// All variants implemented.
UPSAMPLE_FUNC(UpsampleRgbLinePair,  VP8YuvToRgb,  3)
UPSAMPLE_FUNC(UpsampleBgrLinePair,  VP8YuvToBgr,  3)
UPSAMPLE_FUNC(UpsampleRgbaLinePair, VP8YuvToRgba, 4)
UPSAMPLE_FUNC(UpsampleBgraLinePair, VP8YuvToBgra, 4)
UPSAMPLE_FUNC(UpsampleArgbLinePair, VP8YuvToArgb, 4)
UPSAMPLE_FUNC(UpsampleRgba4444LinePair, VP8YuvToRgba4444, 2)
UPSAMPLE_FUNC(UpsampleRgb565LinePair,  VP8YuvToRgb565,  2)

#undef LOAD_UV
#undef UPSAMPLE_FUNC

#endif  // FANCY_UPSAMPLING

//------------------------------------------------------------------------------
// simple point-sampling


WebPSampleLinePairFunc WebPSamplers[MODE_LAST];

#define SAMPLE_FUNC(FUNC_NAME, FUNC, XSTEP)                                    \
static void FUNC_NAME(const uint8_t* top_y, const uint8_t* bottom_y,           \
                      const uint8_t* u, const uint8_t* v,                      \
                      uint8_t* top_dst, uint8_t* bottom_dst, int len) {        \
  int i;                                                                       \
  for (i = 0; i < len - 1; i += 2) {                                           \
    FUNC(top_y[0], u[0], v[0], top_dst);                                       \
    FUNC(top_y[1], u[0], v[0], top_dst + XSTEP);                               \
    FUNC(bottom_y[0], u[0], v[0], bottom_dst);                                 \
    FUNC(bottom_y[1], u[0], v[0], bottom_dst + XSTEP);                         \
    top_y += 2;                                                                \
    bottom_y += 2;                                                             \
    u++;                                                                       \
    v++;                                                                       \
    top_dst += 2 * XSTEP;                                                      \
    bottom_dst += 2 * XSTEP;                                                   \
  }                                                                            \
  if (i == len - 1) {    /* last one */                                        \
    FUNC(top_y[0], u[0], v[0], top_dst);                                       \
    FUNC(bottom_y[0], u[0], v[0], bottom_dst);                                 \
  }                                                                            \
}

// All variants implemented.
SAMPLE_FUNC(SampleRgbLinePair,      VP8YuvToRgb,  3)
SAMPLE_FUNC(SampleBgrLinePair,      VP8YuvToBgr,  3)
SAMPLE_FUNC(SampleRgbaLinePair,     VP8YuvToRgba, 4)
SAMPLE_FUNC(SampleBgraLinePair,     VP8YuvToBgra, 4)
SAMPLE_FUNC(SampleArgbLinePair,     VP8YuvToArgb, 4)
SAMPLE_FUNC(SampleRgba4444LinePair, VP8YuvToRgba4444, 2)
SAMPLE_FUNC(SampleRgb565LinePair,   VP8YuvToRgb565, 2)

#undef SAMPLE_FUNC

//------------------------------------------------------------------------------

#if !defined(FANCY_UPSAMPLING)
#define DUAL_SAMPLE_FUNC(FUNC_NAME, FUNC)                                      \
static void FUNC_NAME(const uint8_t* top_y, const uint8_t* bot_y,              \
                      const uint8_t* top_u, const uint8_t* top_v,              \
                      const uint8_t* bot_u, const uint8_t* bot_v,              \
                      uint8_t* top_dst, uint8_t* bot_dst, int len) {           \
  const int half_len = len >> 1;                                               \
  int x;                                                                       \
  assert(top_dst != NULL);                                                     \
  {                                                                            \
    for (x = 0; x < half_len; ++x) {                                           \
      FUNC(top_y[2 * x + 0], top_u[x], top_v[x], top_dst + 8 * x + 0);         \
      FUNC(top_y[2 * x + 1], top_u[x], top_v[x], top_dst + 8 * x + 4);         \
    }                                                                          \
    if (len & 1) FUNC(top_y[2 * x + 0], top_u[x], top_v[x], top_dst + 8 * x);  \
  }                                                                            \
  if (bot_dst != NULL) {                                                       \
    for (x = 0; x < half_len; ++x) {                                           \
      FUNC(bot_y[2 * x + 0], bot_u[x], bot_v[x], bot_dst + 8 * x + 0);         \
      FUNC(bot_y[2 * x + 1], bot_u[x], bot_v[x], bot_dst + 8 * x + 4);         \
    }                                                                          \
    if (len & 1) FUNC(bot_y[2 * x + 0], bot_u[x], bot_v[x], bot_dst + 8 * x);  \
  }                                                                            \
}

DUAL_SAMPLE_FUNC(DualLineSamplerBGRA, VP8YuvToBgra)
DUAL_SAMPLE_FUNC(DualLineSamplerARGB, VP8YuvToArgb)
#undef DUAL_SAMPLE_FUNC

#endif  // !FANCY_UPSAMPLING

WebPUpsampleLinePairFunc WebPGetLinePairConverter(int alpha_is_last) {
  WebPInitUpsamplers();
  VP8YUVInit();
#ifdef FANCY_UPSAMPLING
  return WebPUpsamplers[alpha_is_last ? MODE_BGRA : MODE_ARGB];
#else
  return (alpha_is_last ? DualLineSamplerBGRA : DualLineSamplerARGB);
#endif
}

//------------------------------------------------------------------------------
// YUV444 converter

#define YUV444_FUNC(FUNC_NAME, FUNC, XSTEP)                                    \
static void FUNC_NAME(const uint8_t* y, const uint8_t* u, const uint8_t* v,    \
                      uint8_t* dst, int len) {                                 \
  int i;                                                                       \
  for (i = 0; i < len; ++i) FUNC(y[i], u[i], v[i], &dst[i * XSTEP]);           \
}

YUV444_FUNC(Yuv444ToRgb,      VP8YuvToRgb,  3)
YUV444_FUNC(Yuv444ToBgr,      VP8YuvToBgr,  3)
YUV444_FUNC(Yuv444ToRgba,     VP8YuvToRgba, 4)
YUV444_FUNC(Yuv444ToBgra,     VP8YuvToBgra, 4)
YUV444_FUNC(Yuv444ToArgb,     VP8YuvToArgb, 4)
YUV444_FUNC(Yuv444ToRgba4444, VP8YuvToRgba4444, 2)
YUV444_FUNC(Yuv444ToRgb565,   VP8YuvToRgb565, 2)

#undef YUV444_FUNC

const WebPYUV444Converter WebPYUV444Converters[MODE_LAST] = {
  Yuv444ToRgb,       // MODE_RGB
  Yuv444ToRgba,      // MODE_RGBA
  Yuv444ToBgr,       // MODE_BGR
  Yuv444ToBgra,      // MODE_BGRA
  Yuv444ToArgb,      // MODE_ARGB
  Yuv444ToRgba4444,  // MODE_RGBA_4444
  Yuv444ToRgb565,    // MODE_RGB_565
  Yuv444ToRgba,      // MODE_rgbA
  Yuv444ToBgra,      // MODE_bgrA
  Yuv444ToArgb,      // MODE_Argb
  Yuv444ToRgba4444   // MODE_rgbA_4444
};

//------------------------------------------------------------------------------
// Premultiplied modes

// non dithered-modes

// (x * a * 32897) >> 23 is bit-wise equivalent to (int)(x * a / 255.)
// for all 8bit x or a. For bit-wise equivalence to (int)(x * a / 255. + .5),
// one can use instead: (x * a * 65793 + (1 << 23)) >> 24
#if 1     // (int)(x * a / 255.)
#define MULTIPLIER(a)   ((a) * 32897UL)
#define PREMULTIPLY(x, m) (((x) * (m)) >> 23)
#else     // (int)(x * a / 255. + .5)
#define MULTIPLIER(a) ((a) * 65793UL)
#define PREMULTIPLY(x, m) (((x) * (m) + (1UL << 23)) >> 24)
#endif

static void ApplyAlphaMultiply(uint8_t* rgba, int alpha_first,
                               int w, int h, int stride) {
  while (h-- > 0) {
    uint8_t* const rgb = rgba + (alpha_first ? 1 : 0);
    const uint8_t* const alpha = rgba + (alpha_first ? 0 : 3);
    int i;
    for (i = 0; i < w; ++i) {
      const uint32_t a = alpha[4 * i];
      if (a != 0xff) {
        const uint32_t mult = MULTIPLIER(a);
        rgb[4 * i + 0] = PREMULTIPLY(rgb[4 * i + 0], mult);
        rgb[4 * i + 1] = PREMULTIPLY(rgb[4 * i + 1], mult);
        rgb[4 * i + 2] = PREMULTIPLY(rgb[4 * i + 2], mult);
      }
    }
    rgba += stride;
  }
}
#undef MULTIPLIER
#undef PREMULTIPLY

// rgbA4444

#define MULTIPLIER(a)  ((a) * 0x1111)    // 0x1111 ~= (1 << 16) / 15

static WEBP_INLINE uint8_t dither_hi(uint8_t x) {
  return (x & 0xf0) | (x >> 4);
}

static WEBP_INLINE uint8_t dither_lo(uint8_t x) {
  return (x & 0x0f) | (x << 4);
}

static WEBP_INLINE uint8_t multiply(uint8_t x, uint32_t m) {
  return (x * m) >> 16;
}

static WEBP_INLINE void ApplyAlphaMultiply4444(uint8_t* rgba4444,
                                               int w, int h, int stride,
                                               int rg_byte_pos /* 0 or 1 */) {
  while (h-- > 0) {
    int i;
    for (i = 0; i < w; ++i) {
      const uint32_t rg = rgba4444[2 * i + rg_byte_pos];
      const uint32_t ba = rgba4444[2 * i + (rg_byte_pos ^ 1)];
      const uint8_t a = ba & 0x0f;
      const uint32_t mult = MULTIPLIER(a);
      const uint8_t r = multiply(dither_hi(rg), mult);
      const uint8_t g = multiply(dither_lo(rg), mult);
      const uint8_t b = multiply(dither_hi(ba), mult);
      rgba4444[2 * i + rg_byte_pos] = (r & 0xf0) | ((g >> 4) & 0x0f);
      rgba4444[2 * i + (rg_byte_pos ^ 1)] = (b & 0xf0) | a;
    }
    rgba4444 += stride;
  }
}
#undef MULTIPLIER

static void ApplyAlphaMultiply_16b(uint8_t* rgba4444,
                                   int w, int h, int stride) {
#ifdef WEBP_SWAP_16BIT_CSP
  ApplyAlphaMultiply4444(rgba4444, w, h, stride, 1);
#else
  ApplyAlphaMultiply4444(rgba4444, w, h, stride, 0);
#endif
}

void (*WebPApplyAlphaMultiply)(uint8_t*, int, int, int, int)
    = ApplyAlphaMultiply;
void (*WebPApplyAlphaMultiply4444)(uint8_t*, int, int, int)
    = ApplyAlphaMultiply_16b;

//------------------------------------------------------------------------------
// Main call

void WebPInitUpsamplers(void) {
#ifdef FANCY_UPSAMPLING
  WebPUpsamplers[MODE_RGB]       = UpsampleRgbLinePair;
  WebPUpsamplers[MODE_RGBA]      = UpsampleRgbaLinePair;
  WebPUpsamplers[MODE_BGR]       = UpsampleBgrLinePair;
  WebPUpsamplers[MODE_BGRA]      = UpsampleBgraLinePair;
  WebPUpsamplers[MODE_ARGB]      = UpsampleArgbLinePair;
  WebPUpsamplers[MODE_RGBA_4444] = UpsampleRgba4444LinePair;
  WebPUpsamplers[MODE_RGB_565]   = UpsampleRgb565LinePair;

  // If defined, use CPUInfo() to overwrite some pointers with faster versions.
  if (VP8GetCPUInfo != NULL) {
#if defined(WEBP_USE_SSE2)
    if (VP8GetCPUInfo(kSSE2)) {
      WebPInitUpsamplersSSE2();
    }
#endif
#if defined(WEBP_USE_NEON)
    if (VP8GetCPUInfo(kNEON)) {
      WebPInitUpsamplersNEON();
    }
#endif
  }
#endif  // FANCY_UPSAMPLING
}

void WebPInitSamplers(void) {
  WebPSamplers[MODE_RGB]       = SampleRgbLinePair;
  WebPSamplers[MODE_RGBA]      = SampleRgbaLinePair;
  WebPSamplers[MODE_BGR]       = SampleBgrLinePair;
  WebPSamplers[MODE_BGRA]      = SampleBgraLinePair;
  WebPSamplers[MODE_ARGB]      = SampleArgbLinePair;
  WebPSamplers[MODE_RGBA_4444] = SampleRgba4444LinePair;
  WebPSamplers[MODE_RGB_565]   = SampleRgb565LinePair;
  WebPSamplers[MODE_rgbA]      = SampleRgbaLinePair;
  WebPSamplers[MODE_bgrA]      = SampleBgraLinePair;
  WebPSamplers[MODE_Argb]      = SampleArgbLinePair;
  WebPSamplers[MODE_rgbA_4444] = SampleRgba4444LinePair;

  // If defined, use CPUInfo() to overwrite some pointers with faster versions.
  if (VP8GetCPUInfo != NULL) {
#if defined(WEBP_USE_MIPS32)
    if (VP8GetCPUInfo(kMIPS32)) {
      WebPInitSamplersMIPS32();
    }
#endif  // WEBP_USE_MIPS32
  }
}

void WebPInitPremultiply(void) {
  WebPApplyAlphaMultiply = ApplyAlphaMultiply;
  WebPApplyAlphaMultiply4444 = ApplyAlphaMultiply_16b;

#ifdef FANCY_UPSAMPLING
  WebPUpsamplers[MODE_rgbA]      = UpsampleRgbaLinePair;
  WebPUpsamplers[MODE_bgrA]      = UpsampleBgraLinePair;
  WebPUpsamplers[MODE_Argb]      = UpsampleArgbLinePair;
  WebPUpsamplers[MODE_rgbA_4444] = UpsampleRgba4444LinePair;

  if (VP8GetCPUInfo != NULL) {
#if defined(WEBP_USE_SSE2)
    if (VP8GetCPUInfo(kSSE2)) {
      WebPInitPremultiplySSE2();
    }
#endif
#if defined(WEBP_USE_NEON)
    if (VP8GetCPUInfo(kNEON)) {
      WebPInitPremultiplyNEON();
    }
#endif
  }
#endif  // FANCY_UPSAMPLING
}


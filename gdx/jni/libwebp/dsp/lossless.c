// Copyright 2012 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Image transforms and color space conversion methods for lossless decoder.
//
// Authors: Vikas Arora (vikaas.arora@gmail.com)
//          Jyrki Alakuijala (jyrki@google.com)
//          Urvang Joshi (urvang@google.com)

#include "./dsp.h"

#include <math.h>
#include <stdlib.h>
#include "../dec/vp8li.h"
#include "../utils/endian_inl.h"
#include "./lossless.h"

#define MAX_DIFF_COST (1e30f)

//------------------------------------------------------------------------------
// Image transforms.

// In-place sum of each component with mod 256.
static WEBP_INLINE void AddPixelsEq(uint32_t* a, uint32_t b) {
  const uint32_t alpha_and_green = (*a & 0xff00ff00u) + (b & 0xff00ff00u);
  const uint32_t red_and_blue = (*a & 0x00ff00ffu) + (b & 0x00ff00ffu);
  *a = (alpha_and_green & 0xff00ff00u) | (red_and_blue & 0x00ff00ffu);
}

static WEBP_INLINE uint32_t Average2(uint32_t a0, uint32_t a1) {
  return (((a0 ^ a1) & 0xfefefefeu) >> 1) + (a0 & a1);
}

static WEBP_INLINE uint32_t Average3(uint32_t a0, uint32_t a1, uint32_t a2) {
  return Average2(Average2(a0, a2), a1);
}

static WEBP_INLINE uint32_t Average4(uint32_t a0, uint32_t a1,
                                     uint32_t a2, uint32_t a3) {
  return Average2(Average2(a0, a1), Average2(a2, a3));
}

static WEBP_INLINE uint32_t Clip255(uint32_t a) {
  if (a < 256) {
    return a;
  }
  // return 0, when a is a negative integer.
  // return 255, when a is positive.
  return ~a >> 24;
}

static WEBP_INLINE int AddSubtractComponentFull(int a, int b, int c) {
  return Clip255(a + b - c);
}

static WEBP_INLINE uint32_t ClampedAddSubtractFull(uint32_t c0, uint32_t c1,
                                                   uint32_t c2) {
  const int a = AddSubtractComponentFull(c0 >> 24, c1 >> 24, c2 >> 24);
  const int r = AddSubtractComponentFull((c0 >> 16) & 0xff,
                                         (c1 >> 16) & 0xff,
                                         (c2 >> 16) & 0xff);
  const int g = AddSubtractComponentFull((c0 >> 8) & 0xff,
                                         (c1 >> 8) & 0xff,
                                         (c2 >> 8) & 0xff);
  const int b = AddSubtractComponentFull(c0 & 0xff, c1 & 0xff, c2 & 0xff);
  return ((uint32_t)a << 24) | (r << 16) | (g << 8) | b;
}

static WEBP_INLINE int AddSubtractComponentHalf(int a, int b) {
  return Clip255(a + (a - b) / 2);
}

static WEBP_INLINE uint32_t ClampedAddSubtractHalf(uint32_t c0, uint32_t c1,
                                                   uint32_t c2) {
  const uint32_t ave = Average2(c0, c1);
  const int a = AddSubtractComponentHalf(ave >> 24, c2 >> 24);
  const int r = AddSubtractComponentHalf((ave >> 16) & 0xff, (c2 >> 16) & 0xff);
  const int g = AddSubtractComponentHalf((ave >> 8) & 0xff, (c2 >> 8) & 0xff);
  const int b = AddSubtractComponentHalf((ave >> 0) & 0xff, (c2 >> 0) & 0xff);
  return ((uint32_t)a << 24) | (r << 16) | (g << 8) | b;
}

// gcc-4.9 on ARM generates incorrect code in Select() when Sub3() is inlined.
#if defined(__arm__) && LOCAL_GCC_VERSION == 0x409
# define LOCAL_INLINE __attribute__ ((noinline))
#else
# define LOCAL_INLINE WEBP_INLINE
#endif

static LOCAL_INLINE int Sub3(int a, int b, int c) {
  const int pb = b - c;
  const int pa = a - c;
  return abs(pb) - abs(pa);
}

#undef LOCAL_INLINE

static WEBP_INLINE uint32_t Select(uint32_t a, uint32_t b, uint32_t c) {
  const int pa_minus_pb =
      Sub3((a >> 24)       , (b >> 24)       , (c >> 24)       ) +
      Sub3((a >> 16) & 0xff, (b >> 16) & 0xff, (c >> 16) & 0xff) +
      Sub3((a >>  8) & 0xff, (b >>  8) & 0xff, (c >>  8) & 0xff) +
      Sub3((a      ) & 0xff, (b      ) & 0xff, (c      ) & 0xff);
  return (pa_minus_pb <= 0) ? a : b;
}

//------------------------------------------------------------------------------
// Predictors

static uint32_t Predictor0(uint32_t left, const uint32_t* const top) {
  (void)top;
  (void)left;
  return ARGB_BLACK;
}
static uint32_t Predictor1(uint32_t left, const uint32_t* const top) {
  (void)top;
  return left;
}
static uint32_t Predictor2(uint32_t left, const uint32_t* const top) {
  (void)left;
  return top[0];
}
static uint32_t Predictor3(uint32_t left, const uint32_t* const top) {
  (void)left;
  return top[1];
}
static uint32_t Predictor4(uint32_t left, const uint32_t* const top) {
  (void)left;
  return top[-1];
}
static uint32_t Predictor5(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average3(left, top[0], top[1]);
  return pred;
}
static uint32_t Predictor6(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average2(left, top[-1]);
  return pred;
}
static uint32_t Predictor7(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average2(left, top[0]);
  return pred;
}
static uint32_t Predictor8(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average2(top[-1], top[0]);
  (void)left;
  return pred;
}
static uint32_t Predictor9(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average2(top[0], top[1]);
  (void)left;
  return pred;
}
static uint32_t Predictor10(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Average4(left, top[-1], top[0], top[1]);
  return pred;
}
static uint32_t Predictor11(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = Select(top[0], left, top[-1]);
  return pred;
}
static uint32_t Predictor12(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = ClampedAddSubtractFull(left, top[0], top[-1]);
  return pred;
}
static uint32_t Predictor13(uint32_t left, const uint32_t* const top) {
  const uint32_t pred = ClampedAddSubtractHalf(left, top[0], top[-1]);
  return pred;
}

//------------------------------------------------------------------------------

// Inverse prediction.
static void PredictorInverseTransform(const VP8LTransform* const transform,
                                      int y_start, int y_end, uint32_t* data) {
  const int width = transform->xsize_;
  if (y_start == 0) {  // First Row follows the L (mode=1) mode.
    int x;
    const uint32_t pred0 = Predictor0(data[-1], NULL);
    AddPixelsEq(data, pred0);
    for (x = 1; x < width; ++x) {
      const uint32_t pred1 = Predictor1(data[x - 1], NULL);
      AddPixelsEq(data + x, pred1);
    }
    data += width;
    ++y_start;
  }

  {
    int y = y_start;
    const int tile_width = 1 << transform->bits_;
    const int mask = tile_width - 1;
    const int safe_width = width & ~mask;
    const int tiles_per_row = VP8LSubSampleSize(width, transform->bits_);
    const uint32_t* pred_mode_base =
        transform->data_ + (y >> transform->bits_) * tiles_per_row;

    while (y < y_end) {
      const uint32_t pred2 = Predictor2(data[-1], data - width);
      const uint32_t* pred_mode_src = pred_mode_base;
      VP8LPredictorFunc pred_func;
      int x = 1;
      int t = 1;
      // First pixel follows the T (mode=2) mode.
      AddPixelsEq(data, pred2);
      // .. the rest:
      while (x < safe_width) {
        pred_func = VP8LPredictors[((*pred_mode_src++) >> 8) & 0xf];
        for (; t < tile_width; ++t, ++x) {
          const uint32_t pred = pred_func(data[x - 1], data + x - width);
          AddPixelsEq(data + x, pred);
        }
        t = 0;
      }
      if (x < width) {
        pred_func = VP8LPredictors[((*pred_mode_src++) >> 8) & 0xf];
        for (; x < width; ++x) {
          const uint32_t pred = pred_func(data[x - 1], data + x - width);
          AddPixelsEq(data + x, pred);
        }
      }
      data += width;
      ++y;
      if ((y & mask) == 0) {   // Use the same mask, since tiles are squares.
        pred_mode_base += tiles_per_row;
      }
    }
  }
}

// Add green to blue and red channels (i.e. perform the inverse transform of
// 'subtract green').
void VP8LAddGreenToBlueAndRed_C(uint32_t* data, int num_pixels) {
  int i;
  for (i = 0; i < num_pixels; ++i) {
    const uint32_t argb = data[i];
    const uint32_t green = ((argb >> 8) & 0xff);
    uint32_t red_blue = (argb & 0x00ff00ffu);
    red_blue += (green << 16) | green;
    red_blue &= 0x00ff00ffu;
    data[i] = (argb & 0xff00ff00u) | red_blue;
  }
}

static WEBP_INLINE uint32_t ColorTransformDelta(int8_t color_pred,
                                                int8_t color) {
  return (uint32_t)((int)(color_pred) * color) >> 5;
}

static WEBP_INLINE void ColorCodeToMultipliers(uint32_t color_code,
                                               VP8LMultipliers* const m) {
  m->green_to_red_  = (color_code >>  0) & 0xff;
  m->green_to_blue_ = (color_code >>  8) & 0xff;
  m->red_to_blue_   = (color_code >> 16) & 0xff;
}

void VP8LTransformColorInverse_C(const VP8LMultipliers* const m, uint32_t* data,
                                 int num_pixels) {
  int i;
  for (i = 0; i < num_pixels; ++i) {
    const uint32_t argb = data[i];
    const uint32_t green = argb >> 8;
    const uint32_t red = argb >> 16;
    uint32_t new_red = red;
    uint32_t new_blue = argb;
    new_red += ColorTransformDelta(m->green_to_red_, green);
    new_red &= 0xff;
    new_blue += ColorTransformDelta(m->green_to_blue_, green);
    new_blue += ColorTransformDelta(m->red_to_blue_, new_red);
    new_blue &= 0xff;
    data[i] = (argb & 0xff00ff00u) | (new_red << 16) | (new_blue);
  }
}

// Color space inverse transform.
static void ColorSpaceInverseTransform(const VP8LTransform* const transform,
                                       int y_start, int y_end, uint32_t* data) {
  const int width = transform->xsize_;
  const int tile_width = 1 << transform->bits_;
  const int mask = tile_width - 1;
  const int safe_width = width & ~mask;
  const int remaining_width = width - safe_width;
  const int tiles_per_row = VP8LSubSampleSize(width, transform->bits_);
  int y = y_start;
  const uint32_t* pred_row =
      transform->data_ + (y >> transform->bits_) * tiles_per_row;

  while (y < y_end) {
    const uint32_t* pred = pred_row;
    VP8LMultipliers m = { 0, 0, 0 };
    const uint32_t* const data_safe_end = data + safe_width;
    const uint32_t* const data_end = data + width;
    while (data < data_safe_end) {
      ColorCodeToMultipliers(*pred++, &m);
      VP8LTransformColorInverse(&m, data, tile_width);
      data += tile_width;
    }
    if (data < data_end) {  // Left-overs using C-version.
      ColorCodeToMultipliers(*pred++, &m);
      VP8LTransformColorInverse(&m, data, remaining_width);
      data += remaining_width;
    }
    ++y;
    if ((y & mask) == 0) pred_row += tiles_per_row;
  }
}

// Separate out pixels packed together using pixel-bundling.
// We define two methods for ARGB data (uint32_t) and alpha-only data (uint8_t).
#define COLOR_INDEX_INVERSE(FUNC_NAME, F_NAME, STATIC_DECL, TYPE, BIT_SUFFIX,  \
                            GET_INDEX, GET_VALUE)                              \
static void F_NAME(const TYPE* src, const uint32_t* const color_map,           \
                   TYPE* dst, int y_start, int y_end, int width) {             \
  int y;                                                                       \
  for (y = y_start; y < y_end; ++y) {                                          \
    int x;                                                                     \
    for (x = 0; x < width; ++x) {                                              \
      *dst++ = GET_VALUE(color_map[GET_INDEX(*src++)]);                        \
    }                                                                          \
  }                                                                            \
}                                                                              \
STATIC_DECL void FUNC_NAME(const VP8LTransform* const transform,               \
                           int y_start, int y_end, const TYPE* src,            \
                           TYPE* dst) {                                        \
  int y;                                                                       \
  const int bits_per_pixel = 8 >> transform->bits_;                            \
  const int width = transform->xsize_;                                         \
  const uint32_t* const color_map = transform->data_;                          \
  if (bits_per_pixel < 8) {                                                    \
    const int pixels_per_byte = 1 << transform->bits_;                         \
    const int count_mask = pixels_per_byte - 1;                                \
    const uint32_t bit_mask = (1 << bits_per_pixel) - 1;                       \
    for (y = y_start; y < y_end; ++y) {                                        \
      uint32_t packed_pixels = 0;                                              \
      int x;                                                                   \
      for (x = 0; x < width; ++x) {                                            \
        /* We need to load fresh 'packed_pixels' once every                */  \
        /* 'pixels_per_byte' increments of x. Fortunately, pixels_per_byte */  \
        /* is a power of 2, so can just use a mask for that, instead of    */  \
        /* decrementing a counter.                                         */  \
        if ((x & count_mask) == 0) packed_pixels = GET_INDEX(*src++);          \
        *dst++ = GET_VALUE(color_map[packed_pixels & bit_mask]);               \
        packed_pixels >>= bits_per_pixel;                                      \
      }                                                                        \
    }                                                                          \
  } else {                                                                     \
    VP8LMapColor##BIT_SUFFIX(src, color_map, dst, y_start, y_end, width);      \
  }                                                                            \
}

COLOR_INDEX_INVERSE(ColorIndexInverseTransform, MapARGB, static, uint32_t, 32b,
                    VP8GetARGBIndex, VP8GetARGBValue)
COLOR_INDEX_INVERSE(VP8LColorIndexInverseTransformAlpha, MapAlpha, , uint8_t,
                    8b, VP8GetAlphaIndex, VP8GetAlphaValue)

#undef COLOR_INDEX_INVERSE

void VP8LInverseTransform(const VP8LTransform* const transform,
                          int row_start, int row_end,
                          const uint32_t* const in, uint32_t* const out) {
  const int width = transform->xsize_;
  assert(row_start < row_end);
  assert(row_end <= transform->ysize_);
  switch (transform->type_) {
    case SUBTRACT_GREEN:
      VP8LAddGreenToBlueAndRed(out, (row_end - row_start) * width);
      break;
    case PREDICTOR_TRANSFORM:
      PredictorInverseTransform(transform, row_start, row_end, out);
      if (row_end != transform->ysize_) {
        // The last predicted row in this iteration will be the top-pred row
        // for the first row in next iteration.
        memcpy(out - width, out + (row_end - row_start - 1) * width,
               width * sizeof(*out));
      }
      break;
    case CROSS_COLOR_TRANSFORM:
      ColorSpaceInverseTransform(transform, row_start, row_end, out);
      break;
    case COLOR_INDEXING_TRANSFORM:
      if (in == out && transform->bits_ > 0) {
        // Move packed pixels to the end of unpacked region, so that unpacking
        // can occur seamlessly.
        // Also, note that this is the only transform that applies on
        // the effective width of VP8LSubSampleSize(xsize_, bits_). All other
        // transforms work on effective width of xsize_.
        const int out_stride = (row_end - row_start) * width;
        const int in_stride = (row_end - row_start) *
            VP8LSubSampleSize(transform->xsize_, transform->bits_);
        uint32_t* const src = out + out_stride - in_stride;
        memmove(src, out, in_stride * sizeof(*src));
        ColorIndexInverseTransform(transform, row_start, row_end, src, out);
      } else {
        ColorIndexInverseTransform(transform, row_start, row_end, in, out);
      }
      break;
  }
}

//------------------------------------------------------------------------------
// Color space conversion.

static int is_big_endian(void) {
  static const union {
    uint16_t w;
    uint8_t b[2];
  } tmp = { 1 };
  return (tmp.b[0] != 1);
}

void VP8LConvertBGRAToRGB_C(const uint32_t* src,
                            int num_pixels, uint8_t* dst) {
  const uint32_t* const src_end = src + num_pixels;
  while (src < src_end) {
    const uint32_t argb = *src++;
    *dst++ = (argb >> 16) & 0xff;
    *dst++ = (argb >>  8) & 0xff;
    *dst++ = (argb >>  0) & 0xff;
  }
}

void VP8LConvertBGRAToRGBA_C(const uint32_t* src,
                             int num_pixels, uint8_t* dst) {
  const uint32_t* const src_end = src + num_pixels;
  while (src < src_end) {
    const uint32_t argb = *src++;
    *dst++ = (argb >> 16) & 0xff;
    *dst++ = (argb >>  8) & 0xff;
    *dst++ = (argb >>  0) & 0xff;
    *dst++ = (argb >> 24) & 0xff;
  }
}

void VP8LConvertBGRAToRGBA4444_C(const uint32_t* src,
                                 int num_pixels, uint8_t* dst) {
  const uint32_t* const src_end = src + num_pixels;
  while (src < src_end) {
    const uint32_t argb = *src++;
    const uint8_t rg = ((argb >> 16) & 0xf0) | ((argb >> 12) & 0xf);
    const uint8_t ba = ((argb >>  0) & 0xf0) | ((argb >> 28) & 0xf);
#ifdef WEBP_SWAP_16BIT_CSP
    *dst++ = ba;
    *dst++ = rg;
#else
    *dst++ = rg;
    *dst++ = ba;
#endif
  }
}

void VP8LConvertBGRAToRGB565_C(const uint32_t* src,
                               int num_pixels, uint8_t* dst) {
  const uint32_t* const src_end = src + num_pixels;
  while (src < src_end) {
    const uint32_t argb = *src++;
    const uint8_t rg = ((argb >> 16) & 0xf8) | ((argb >> 13) & 0x7);
    const uint8_t gb = ((argb >>  5) & 0xe0) | ((argb >>  3) & 0x1f);
#ifdef WEBP_SWAP_16BIT_CSP
    *dst++ = gb;
    *dst++ = rg;
#else
    *dst++ = rg;
    *dst++ = gb;
#endif
  }
}

void VP8LConvertBGRAToBGR_C(const uint32_t* src,
                            int num_pixels, uint8_t* dst) {
  const uint32_t* const src_end = src + num_pixels;
  while (src < src_end) {
    const uint32_t argb = *src++;
    *dst++ = (argb >>  0) & 0xff;
    *dst++ = (argb >>  8) & 0xff;
    *dst++ = (argb >> 16) & 0xff;
  }
}

static void CopyOrSwap(const uint32_t* src, int num_pixels, uint8_t* dst,
                       int swap_on_big_endian) {
  if (is_big_endian() == swap_on_big_endian) {
    const uint32_t* const src_end = src + num_pixels;
    while (src < src_end) {
      const uint32_t argb = *src++;

#if !defined(WORDS_BIGENDIAN)
#if !defined(WEBP_REFERENCE_IMPLEMENTATION)
      WebPUint32ToMem(dst, BSwap32(argb));
#else  // WEBP_REFERENCE_IMPLEMENTATION
      dst[0] = (argb >> 24) & 0xff;
      dst[1] = (argb >> 16) & 0xff;
      dst[2] = (argb >>  8) & 0xff;
      dst[3] = (argb >>  0) & 0xff;
#endif
#else  // WORDS_BIGENDIAN
      dst[0] = (argb >>  0) & 0xff;
      dst[1] = (argb >>  8) & 0xff;
      dst[2] = (argb >> 16) & 0xff;
      dst[3] = (argb >> 24) & 0xff;
#endif
      dst += sizeof(argb);
    }
  } else {
    memcpy(dst, src, num_pixels * sizeof(*src));
  }
}

void VP8LConvertFromBGRA(const uint32_t* const in_data, int num_pixels,
                         WEBP_CSP_MODE out_colorspace, uint8_t* const rgba) {
  switch (out_colorspace) {
    case MODE_RGB:
      VP8LConvertBGRAToRGB(in_data, num_pixels, rgba);
      break;
    case MODE_RGBA:
      VP8LConvertBGRAToRGBA(in_data, num_pixels, rgba);
      break;
    case MODE_rgbA:
      VP8LConvertBGRAToRGBA(in_data, num_pixels, rgba);
      WebPApplyAlphaMultiply(rgba, 0, num_pixels, 1, 0);
      break;
    case MODE_BGR:
      VP8LConvertBGRAToBGR(in_data, num_pixels, rgba);
      break;
    case MODE_BGRA:
      CopyOrSwap(in_data, num_pixels, rgba, 1);
      break;
    case MODE_bgrA:
      CopyOrSwap(in_data, num_pixels, rgba, 1);
      WebPApplyAlphaMultiply(rgba, 0, num_pixels, 1, 0);
      break;
    case MODE_ARGB:
      CopyOrSwap(in_data, num_pixels, rgba, 0);
      break;
    case MODE_Argb:
      CopyOrSwap(in_data, num_pixels, rgba, 0);
      WebPApplyAlphaMultiply(rgba, 1, num_pixels, 1, 0);
      break;
    case MODE_RGBA_4444:
      VP8LConvertBGRAToRGBA4444(in_data, num_pixels, rgba);
      break;
    case MODE_rgbA_4444:
      VP8LConvertBGRAToRGBA4444(in_data, num_pixels, rgba);
      WebPApplyAlphaMultiply4444(rgba, num_pixels, 1, 0);
      break;
    case MODE_RGB_565:
      VP8LConvertBGRAToRGB565(in_data, num_pixels, rgba);
      break;
    default:
      assert(0);          // Code flow should not reach here.
  }
}

//------------------------------------------------------------------------------

VP8LProcessBlueAndRedFunc VP8LAddGreenToBlueAndRed;
VP8LPredictorFunc VP8LPredictors[16];

VP8LTransformColorFunc VP8LTransformColorInverse;

VP8LConvertFunc VP8LConvertBGRAToRGB;
VP8LConvertFunc VP8LConvertBGRAToRGBA;
VP8LConvertFunc VP8LConvertBGRAToRGBA4444;
VP8LConvertFunc VP8LConvertBGRAToRGB565;
VP8LConvertFunc VP8LConvertBGRAToBGR;

VP8LMapARGBFunc VP8LMapColor32b;
VP8LMapAlphaFunc VP8LMapColor8b;

extern void VP8LDspInitSSE2(void);
extern void VP8LDspInitNEON(void);
extern void VP8LDspInitMIPSdspR2(void);

static volatile VP8CPUInfo lossless_last_cpuinfo_used =
    (VP8CPUInfo)&lossless_last_cpuinfo_used;

WEBP_TSAN_IGNORE_FUNCTION void VP8LDspInit(void) {
  if (lossless_last_cpuinfo_used == VP8GetCPUInfo) return;

  VP8LPredictors[0] = Predictor0;
  VP8LPredictors[1] = Predictor1;
  VP8LPredictors[2] = Predictor2;
  VP8LPredictors[3] = Predictor3;
  VP8LPredictors[4] = Predictor4;
  VP8LPredictors[5] = Predictor5;
  VP8LPredictors[6] = Predictor6;
  VP8LPredictors[7] = Predictor7;
  VP8LPredictors[8] = Predictor8;
  VP8LPredictors[9] = Predictor9;
  VP8LPredictors[10] = Predictor10;
  VP8LPredictors[11] = Predictor11;
  VP8LPredictors[12] = Predictor12;
  VP8LPredictors[13] = Predictor13;
  VP8LPredictors[14] = Predictor0;     // <- padding security sentinels
  VP8LPredictors[15] = Predictor0;

  VP8LAddGreenToBlueAndRed = VP8LAddGreenToBlueAndRed_C;

  VP8LTransformColorInverse = VP8LTransformColorInverse_C;

  VP8LConvertBGRAToRGB = VP8LConvertBGRAToRGB_C;
  VP8LConvertBGRAToRGBA = VP8LConvertBGRAToRGBA_C;
  VP8LConvertBGRAToRGBA4444 = VP8LConvertBGRAToRGBA4444_C;
  VP8LConvertBGRAToRGB565 = VP8LConvertBGRAToRGB565_C;
  VP8LConvertBGRAToBGR = VP8LConvertBGRAToBGR_C;

  VP8LMapColor32b = MapARGB;
  VP8LMapColor8b = MapAlpha;

  // If defined, use CPUInfo() to overwrite some pointers with faster versions.
  if (VP8GetCPUInfo != NULL) {
#if defined(WEBP_USE_SSE2)
    if (VP8GetCPUInfo(kSSE2)) {
      VP8LDspInitSSE2();
    }
#endif
#if defined(WEBP_USE_NEON)
    if (VP8GetCPUInfo(kNEON)) {
      VP8LDspInitNEON();
    }
#endif
#if defined(WEBP_USE_MIPS_DSP_R2)
    if (VP8GetCPUInfo(kMIPSdspR2)) {
      VP8LDspInitMIPSdspR2();
    }
#endif
  }
  lossless_last_cpuinfo_used = VP8GetCPUInfo;
}

//------------------------------------------------------------------------------

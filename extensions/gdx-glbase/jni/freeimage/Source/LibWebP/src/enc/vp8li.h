// Copyright 2012 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Lossless encoder: internal header.
//
// Author: Vikas Arora (vikaas.arora@gmail.com)

#ifndef WEBP_ENC_VP8LI_H_
#define WEBP_ENC_VP8LI_H_

#include "./histogram.h"
#include "../utils/bit_writer.h"
#include "../webp/encode.h"
#include "../webp/format_constants.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
  const WebPConfig* config_;    // user configuration and parameters
  const WebPPicture* pic_;      // input picture.

  uint32_t* argb_;              // Transformed argb image data.
  uint32_t* argb_scratch_;      // Scratch memory for argb rows
                                // (used for prediction).
  uint32_t* transform_data_;    // Scratch memory for transform data.
  int       current_width_;     // Corresponds to packed image width.

  // Encoding parameters derived from quality parameter.
  int histo_bits_;
  int transform_bits_;
  int cache_bits_;        // If equal to 0, don't use color cache.

  // Encoding parameters derived from image characteristics.
  int use_cross_color_;
  int use_subtract_green_;
  int use_predict_;
  int use_palette_;
  int palette_size_;
  uint32_t palette_[MAX_PALETTE_SIZE];
} VP8LEncoder;

//------------------------------------------------------------------------------
// internal functions. Not public.

// Encodes the picture.
// Returns 0 if config or picture is NULL or picture doesn't have valid argb
// input.
int VP8LEncodeImage(const WebPConfig* const config,
                    const WebPPicture* const picture);

// Encodes the main image stream using the supplied bit writer.
WebPEncodingError VP8LEncodeStream(const WebPConfig* const config,
                                   const WebPPicture* const picture,
                                   VP8LBitWriter* const bw);

//------------------------------------------------------------------------------

#ifdef __cplusplus
}    // extern "C"
#endif

#endif  /* WEBP_ENC_VP8LI_H_ */

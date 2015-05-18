// Copyright 2011 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Bit writing and boolean coder
//
// Author: Skal (pascal.massimino@gmail.com)

#ifndef WEBP_UTILS_BIT_WRITER_H_
#define WEBP_UTILS_BIT_WRITER_H_

#include "../webp/types.h"

#ifdef __cplusplus
extern "C" {
#endif

//------------------------------------------------------------------------------
// Bit-writing

typedef struct VP8BitWriter VP8BitWriter;
struct VP8BitWriter {
  int32_t  range_;      // range-1
  int32_t  value_;
  int      run_;        // number of outstanding bits
  int      nb_bits_;    // number of pending bits
  uint8_t* buf_;        // internal buffer. Re-allocated regularly. Not owned.
  size_t   pos_;
  size_t   max_pos_;
  int      error_;      // true in case of error
};

// Initialize the object. Allocates some initial memory based on expected_size.
int VP8BitWriterInit(VP8BitWriter* const bw, size_t expected_size);
// Finalize the bitstream coding. Returns a pointer to the internal buffer.
uint8_t* VP8BitWriterFinish(VP8BitWriter* const bw);
// Release any pending memory and zeroes the object. Not a mandatory call.
// Only useful in case of error, when the internal buffer hasn't been grabbed!
void VP8BitWriterWipeOut(VP8BitWriter* const bw);

int VP8PutBit(VP8BitWriter* const bw, int bit, int prob);
int VP8PutBitUniform(VP8BitWriter* const bw, int bit);
void VP8PutValue(VP8BitWriter* const bw, int value, int nb_bits);
void VP8PutSignedValue(VP8BitWriter* const bw, int value, int nb_bits);

// Appends some bytes to the internal buffer. Data is copied.
int VP8BitWriterAppend(VP8BitWriter* const bw,
                       const uint8_t* data, size_t size);

// return approximate write position (in bits)
static WEBP_INLINE uint64_t VP8BitWriterPos(const VP8BitWriter* const bw) {
  return (uint64_t)(bw->pos_ + bw->run_) * 8 + 8 + bw->nb_bits_;
}

// Returns a pointer to the internal buffer.
static WEBP_INLINE uint8_t* VP8BitWriterBuf(const VP8BitWriter* const bw) {
  return bw->buf_;
}
// Returns the size of the internal buffer.
static WEBP_INLINE size_t VP8BitWriterSize(const VP8BitWriter* const bw) {
  return bw->pos_;
}

//------------------------------------------------------------------------------
// VP8LBitWriter

#if defined(__x86_64__) || defined(_M_X64)   // 64bit
typedef uint64_t vp8l_atype_t;   // accumulator type
typedef uint32_t vp8l_wtype_t;   // writing type
#define WSWAP htole32
#else
typedef uint32_t vp8l_atype_t;
typedef uint16_t vp8l_wtype_t;
#define WSWAP htole16
#endif

typedef struct {
  vp8l_atype_t bits_;   // bit accumulator
  int          used_;   // number of bits used in accumulator
  uint8_t*     buf_;    // start of buffer
  uint8_t*     cur_;    // current write position
  uint8_t*     end_;    // end of buffer

  // After all bits are written (VP8LBitWriterFinish()), the caller must observe
  // the state of error_. A value of 1 indicates that a memory allocation
  // failure has happened during bit writing. A value of 0 indicates successful
  // writing of bits.
  int error_;
} VP8LBitWriter;

static WEBP_INLINE size_t VP8LBitWriterNumBytes(VP8LBitWriter* const bw) {
  return (bw->cur_ - bw->buf_) + ((bw->used_ + 7) >> 3);
}

uint8_t* VP8LBitWriterFinish(VP8LBitWriter* const bw);

// Returns 0 in case of memory allocation error.
int VP8LBitWriterInit(VP8LBitWriter* const bw, size_t expected_size);

void VP8LBitWriterDestroy(VP8LBitWriter* const bw);

// This function writes bits into bytes in increasing addresses (little endian),
// and within a byte least-significant-bit first.
// The function can write up to 8*sizeof(vp8l_wtype_t) bits in one go.
// VP8LBitWriter's error_ flag is set in case of  memory allocation error.
void VP8LWriteBits(VP8LBitWriter* const bw, int n_bits, uint32_t bits);

//------------------------------------------------------------------------------

#ifdef __cplusplus
}    // extern "C"
#endif

#endif  /* WEBP_UTILS_BIT_WRITER_H_ */

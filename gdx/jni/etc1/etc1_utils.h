// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef __etc1_h__
#define __etc1_h__

#define ETC1_ENCODED_BLOCK_SIZE 8
#define ETC1_DECODED_BLOCK_SIZE 48

#ifndef ETC1_RGB8_OES
#define ETC1_RGB8_OES 0x8D64
#endif

typedef unsigned char etc1_byte;
typedef int etc1_bool;
typedef unsigned int etc1_uint32;

#ifdef __cplusplus
extern "C" {
#endif

// Encode a block of pixels.
//
// pIn is a pointer to a ETC_DECODED_BLOCK_SIZE array of bytes that represent a
// 4 x 4 square of 3-byte pixels in form R, G, B. Byte (3 * (x + 4 * y) is the R
// value of pixel (x, y).
//
// validPixelMask is a 16-bit mask where bit (1 << (x + y * 4)) indicates whether
// the corresponding (x,y) pixel is valid. Invalid pixel color values are ignored when compressing.
//
// pOut is an ETC1 compressed version of the data.

void etc1_encode_block(const etc1_byte* pIn, etc1_uint32 validPixelMask, etc1_byte* pOut);

// Decode a block of pixels.
//
// pIn is an ETC1 compressed version of the data.
//
// pOut is a pointer to a ETC_DECODED_BLOCK_SIZE array of bytes that represent a
// 4 x 4 square of 3-byte pixels in form R, G, B. Byte (3 * (x + 4 * y) is the R
// value of pixel (x, y).

void etc1_decode_block(const etc1_byte* pIn, etc1_byte* pOut);

// Return the size of the encoded image data (does not include size of PKM header).

etc1_uint32 etc1_get_encoded_data_size(etc1_uint32 width, etc1_uint32 height);

// Encode an entire image.
// pIn - pointer to the image data. Formatted such that
//       pixel (x,y) is at pIn + pixelSize * x + stride * y;
// pOut - pointer to encoded data. Must be large enough to store entire encoded image.
// pixelSize can be 2 or 3. 2 is an GL_UNSIGNED_SHORT_5_6_5 image, 3 is a GL_BYTE RGB image.
// returns non-zero if there is an error.

int etc1_encode_image(const etc1_byte* pIn, etc1_uint32 width, etc1_uint32 height,
        etc1_uint32 pixelSize, etc1_uint32 stride, etc1_byte* pOut);

// Decode an entire image.
// pIn - pointer to encoded data.
// pOut - pointer to the image data. Will be written such that
//        pixel (x,y) is at pIn + pixelSize * x + stride * y. Must be
//        large enough to store entire image.
// pixelSize can be 2 or 3. 2 is an GL_UNSIGNED_SHORT_5_6_5 image, 3 is a GL_BYTE RGB image.
// returns non-zero if there is an error.

int etc1_decode_image(const etc1_byte* pIn, etc1_byte* pOut,
        etc1_uint32 width, etc1_uint32 height,
        etc1_uint32 pixelSize, etc1_uint32 stride);

// Size of a PKM header, in bytes.

#define ETC_PKM_HEADER_SIZE 16

// Format a PKM header

void etc1_pkm_format_header(etc1_byte* pHeader, etc1_uint32 width, etc1_uint32 height);

// Check if a PKM header is correctly formatted.

etc1_bool etc1_pkm_is_valid(const etc1_byte* pHeader);

// Read the image width from a PKM header

etc1_uint32 etc1_pkm_get_width(const etc1_byte* pHeader);

// Read the image height from a PKM header

etc1_uint32 etc1_pkm_get_height(const etc1_byte* pHeader);

#ifdef __cplusplus
}
#endif

#endif
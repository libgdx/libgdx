// Copyright 2011 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Read APIs for mux.
//
// Authors: Urvang (urvang@google.com)
//          Vikas (vikasa@google.com)

#include <assert.h>
#include "./muxi.h"
#include "../utils/utils.h"

//------------------------------------------------------------------------------
// Helper method(s).

// Handy MACRO.
#define SWITCH_ID_LIST(INDEX, LIST)                                           \
  if (idx == (INDEX)) {                                                       \
    const WebPChunk* const chunk = ChunkSearchList((LIST), nth,               \
                                                   kChunks[(INDEX)].tag);     \
    if (chunk) {                                                              \
      *data = chunk->data_;                                                   \
      return WEBP_MUX_OK;                                                     \
    } else {                                                                  \
      return WEBP_MUX_NOT_FOUND;                                              \
    }                                                                         \
  }

static WebPMuxError MuxGet(const WebPMux* const mux, CHUNK_INDEX idx,
                           uint32_t nth, WebPData* const data) {
  assert(mux != NULL);
  assert(!IsWPI(kChunks[idx].id));
  WebPDataInit(data);

  SWITCH_ID_LIST(IDX_VP8X, mux->vp8x_);
  SWITCH_ID_LIST(IDX_ICCP, mux->iccp_);
  SWITCH_ID_LIST(IDX_ANIM, mux->anim_);
  SWITCH_ID_LIST(IDX_EXIF, mux->exif_);
  SWITCH_ID_LIST(IDX_XMP, mux->xmp_);
  SWITCH_ID_LIST(IDX_UNKNOWN, mux->unknown_);
  return WEBP_MUX_NOT_FOUND;
}
#undef SWITCH_ID_LIST

// Fill the chunk with the given data (includes chunk header bytes), after some
// verifications.
static WebPMuxError ChunkVerifyAndAssign(WebPChunk* chunk,
                                         const uint8_t* data, size_t data_size,
                                         size_t riff_size, int copy_data) {
  uint32_t chunk_size;
  WebPData chunk_data;

  // Sanity checks.
  if (data_size < TAG_SIZE) return WEBP_MUX_NOT_ENOUGH_DATA;
  chunk_size = GetLE32(data + TAG_SIZE);

  {
    const size_t chunk_disk_size = SizeWithPadding(chunk_size);
    if (chunk_disk_size > riff_size) return WEBP_MUX_BAD_DATA;
    if (chunk_disk_size > data_size) return WEBP_MUX_NOT_ENOUGH_DATA;
  }

  // Data assignment.
  chunk_data.bytes = data + CHUNK_HEADER_SIZE;
  chunk_data.size = chunk_size;
  return ChunkAssignData(chunk, &chunk_data, copy_data, GetLE32(data + 0));
}

int MuxImageFinalize(WebPMuxImage* const wpi) {
  const WebPChunk* const img = wpi->img_;
  const WebPData* const image = &img->data_;
  const int is_lossless = (img->tag_ == kChunks[IDX_VP8L].tag);
  int w, h;
  int vp8l_has_alpha = 0;
  const int ok = is_lossless ?
      VP8LGetInfo(image->bytes, image->size, &w, &h, &vp8l_has_alpha) :
      VP8GetInfo(image->bytes, image->size, image->size, &w, &h);
  assert(img != NULL);
  if (ok) {
    // Ignore ALPH chunk accompanying VP8L.
    if (is_lossless && (wpi->alpha_ != NULL)) {
      ChunkDelete(wpi->alpha_);
      wpi->alpha_ = NULL;
    }
    wpi->width_ = w;
    wpi->height_ = h;
    wpi->has_alpha_ = vp8l_has_alpha || (wpi->alpha_ != NULL);
  }
  return ok;
}

static int MuxImageParse(const WebPChunk* const chunk, int copy_data,
                         WebPMuxImage* const wpi) {
  const uint8_t* bytes = chunk->data_.bytes;
  size_t size = chunk->data_.size;
  const uint8_t* const last = bytes + size;
  WebPChunk subchunk;
  size_t subchunk_size;
  ChunkInit(&subchunk);

  assert(chunk->tag_ == kChunks[IDX_ANMF].tag ||
         chunk->tag_ == kChunks[IDX_FRGM].tag);
  assert(!wpi->is_partial_);

  // ANMF/FRGM.
  {
    const size_t hdr_size = (chunk->tag_ == kChunks[IDX_ANMF].tag) ?
        ANMF_CHUNK_SIZE : FRGM_CHUNK_SIZE;
    const WebPData temp = { bytes, hdr_size };
    // Each of ANMF and FRGM chunk contain a header at the beginning. So, its
    // size should at least be 'hdr_size'.
    if (size < hdr_size) goto Fail;
    ChunkAssignData(&subchunk, &temp, copy_data, chunk->tag_);
  }
  ChunkSetNth(&subchunk, &wpi->header_, 1);
  wpi->is_partial_ = 1;  // Waiting for ALPH and/or VP8/VP8L chunks.

  // Rest of the chunks.
  subchunk_size = ChunkDiskSize(&subchunk) - CHUNK_HEADER_SIZE;
  bytes += subchunk_size;
  size -= subchunk_size;

  while (bytes != last) {
    ChunkInit(&subchunk);
    if (ChunkVerifyAndAssign(&subchunk, bytes, size, size,
                             copy_data) != WEBP_MUX_OK) {
      goto Fail;
    }
    switch (ChunkGetIdFromTag(subchunk.tag_)) {
      case WEBP_CHUNK_ALPHA:
        if (wpi->alpha_ != NULL) goto Fail;  // Consecutive ALPH chunks.
        if (ChunkSetNth(&subchunk, &wpi->alpha_, 1) != WEBP_MUX_OK) goto Fail;
        wpi->is_partial_ = 1;  // Waiting for a VP8 chunk.
        break;
      case WEBP_CHUNK_IMAGE:
        if (ChunkSetNth(&subchunk, &wpi->img_, 1) != WEBP_MUX_OK) goto Fail;
        if (!MuxImageFinalize(wpi)) goto Fail;
        wpi->is_partial_ = 0;  // wpi is completely filled.
        break;
      case WEBP_CHUNK_UNKNOWN:
        if (wpi->is_partial_) goto Fail;  // Encountered an unknown chunk
                                          // before some image chunks.
        if (ChunkSetNth(&subchunk, &wpi->unknown_, 0) != WEBP_MUX_OK) goto Fail;
        break;
      default:
        goto Fail;
        break;
    }
    subchunk_size = ChunkDiskSize(&subchunk);
    bytes += subchunk_size;
    size -= subchunk_size;
  }
  if (wpi->is_partial_) goto Fail;
  return 1;

 Fail:
  ChunkRelease(&subchunk);
  return 0;
}

//------------------------------------------------------------------------------
// Create a mux object from WebP-RIFF data.

WebPMux* WebPMuxCreateInternal(const WebPData* bitstream, int copy_data,
                               int version) {
  size_t riff_size;
  uint32_t tag;
  const uint8_t* end;
  WebPMux* mux = NULL;
  WebPMuxImage* wpi = NULL;
  const uint8_t* data;
  size_t size;
  WebPChunk chunk;
  ChunkInit(&chunk);

  // Sanity checks.
  if (WEBP_ABI_IS_INCOMPATIBLE(version, WEBP_MUX_ABI_VERSION)) {
    return NULL;  // version mismatch
  }
  if (bitstream == NULL) return NULL;

  data = bitstream->bytes;
  size = bitstream->size;

  if (data == NULL) return NULL;
  if (size < RIFF_HEADER_SIZE) return NULL;
  if (GetLE32(data + 0) != MKFOURCC('R', 'I', 'F', 'F') ||
      GetLE32(data + CHUNK_HEADER_SIZE) != MKFOURCC('W', 'E', 'B', 'P')) {
    return NULL;
  }

  mux = WebPMuxNew();
  if (mux == NULL) return NULL;

  if (size < RIFF_HEADER_SIZE + TAG_SIZE) goto Err;

  tag = GetLE32(data + RIFF_HEADER_SIZE);
  if (tag != kChunks[IDX_VP8].tag &&
      tag != kChunks[IDX_VP8L].tag &&
      tag != kChunks[IDX_VP8X].tag) {
    goto Err;  // First chunk should be VP8, VP8L or VP8X.
  }

  riff_size = SizeWithPadding(GetLE32(data + TAG_SIZE));
  if (riff_size > MAX_CHUNK_PAYLOAD || riff_size > size) {
    goto Err;
  } else {
    if (riff_size < size) {  // Redundant data after last chunk.
      size = riff_size;  // To make sure we don't read any data beyond mux_size.
    }
  }

  end = data + size;
  data += RIFF_HEADER_SIZE;
  size -= RIFF_HEADER_SIZE;

  wpi = (WebPMuxImage*)malloc(sizeof(*wpi));
  if (wpi == NULL) goto Err;
  MuxImageInit(wpi);

  // Loop over chunks.
  while (data != end) {
    size_t data_size;
    WebPChunkId id;
    WebPChunk** chunk_list;
    if (ChunkVerifyAndAssign(&chunk, data, size, riff_size,
                             copy_data) != WEBP_MUX_OK) {
      goto Err;
    }
    data_size = ChunkDiskSize(&chunk);
    id = ChunkGetIdFromTag(chunk.tag_);
    switch (id) {
      case WEBP_CHUNK_ALPHA:
        if (wpi->alpha_ != NULL) goto Err;  // Consecutive ALPH chunks.
        if (ChunkSetNth(&chunk, &wpi->alpha_, 1) != WEBP_MUX_OK) goto Err;
        wpi->is_partial_ = 1;  // Waiting for a VP8 chunk.
        break;
      case WEBP_CHUNK_IMAGE:
        if (ChunkSetNth(&chunk, &wpi->img_, 1) != WEBP_MUX_OK) goto Err;
        if (!MuxImageFinalize(wpi)) goto Err;
        wpi->is_partial_ = 0;  // wpi is completely filled.
 PushImage:
        // Add this to mux->images_ list.
        if (MuxImagePush(wpi, &mux->images_) != WEBP_MUX_OK) goto Err;
        MuxImageInit(wpi);  // Reset for reading next image.
        break;
      case WEBP_CHUNK_ANMF:
#ifdef WEBP_EXPERIMENTAL_FEATURES
      case WEBP_CHUNK_FRGM:
#endif
        if (wpi->is_partial_) goto Err;  // Previous wpi is still incomplete.
        if (!MuxImageParse(&chunk, copy_data, wpi)) goto Err;
        ChunkRelease(&chunk);
        goto PushImage;
        break;
      default:  // A non-image chunk.
        if (wpi->is_partial_) goto Err;  // Encountered a non-image chunk before
                                         // getting all chunks of an image.
        chunk_list = MuxGetChunkListFromId(mux, id);  // List to add this chunk.
        if (ChunkSetNth(&chunk, chunk_list, 0) != WEBP_MUX_OK) goto Err;
        break;
    }
    data += data_size;
    size -= data_size;
    ChunkInit(&chunk);
  }

  // Validate mux if complete.
  if (MuxValidate(mux) != WEBP_MUX_OK) goto Err;

  MuxImageDelete(wpi);
  return mux;  // All OK;

 Err:  // Something bad happened.
  ChunkRelease(&chunk);
  MuxImageDelete(wpi);
  WebPMuxDelete(mux);
  return NULL;
}

//------------------------------------------------------------------------------
// Get API(s).

// Validates that the given mux has a single image.
static WebPMuxError ValidateForSingleImage(const WebPMux* const mux) {
  const int num_images = MuxImageCount(mux->images_, WEBP_CHUNK_IMAGE);
  const int num_frames = MuxImageCount(mux->images_, WEBP_CHUNK_ANMF);
  const int num_fragments = MuxImageCount(mux->images_, WEBP_CHUNK_FRGM);

  if (num_images == 0) {
    // No images in mux.
    return WEBP_MUX_NOT_FOUND;
  } else if (num_images == 1 && num_frames == 0 && num_fragments == 0) {
    // Valid case (single image).
    return WEBP_MUX_OK;
  } else {
    // Frame/Fragment case OR an invalid mux.
    return WEBP_MUX_INVALID_ARGUMENT;
  }
}

// Get the canvas width, height and flags after validating that VP8X/VP8/VP8L
// chunk and canvas size are valid.
static WebPMuxError MuxGetCanvasInfo(const WebPMux* const mux,
                                     int* width, int* height, uint32_t* flags) {
  int w, h;
  uint32_t f = 0;
  WebPData data;
  assert(mux != NULL);

  // Check if VP8X chunk is present.
  if (MuxGet(mux, IDX_VP8X, 1, &data) == WEBP_MUX_OK) {
    if (data.size < VP8X_CHUNK_SIZE) return WEBP_MUX_BAD_DATA;
    f = GetLE32(data.bytes + 0);
    w = GetLE24(data.bytes + 4) + 1;
    h = GetLE24(data.bytes + 7) + 1;
  } else {  // Single image case.
    const WebPMuxImage* const wpi = mux->images_;
    WebPMuxError err = ValidateForSingleImage(mux);
    if (err != WEBP_MUX_OK) return err;
    assert(wpi != NULL);
    w = wpi->width_;
    h = wpi->height_;
    if (wpi->has_alpha_) f |= ALPHA_FLAG;
  }
  if (w * (uint64_t)h >= MAX_IMAGE_AREA) return WEBP_MUX_BAD_DATA;

  if (width != NULL) *width = w;
  if (height != NULL) *height = h;
  if (flags != NULL) *flags = f;
  return WEBP_MUX_OK;
}

WebPMuxError WebPMuxGetCanvasSize(const WebPMux* mux, int* width, int* height) {
  if (mux == NULL || width == NULL || height == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }
  return MuxGetCanvasInfo(mux, width, height, NULL);
}

WebPMuxError WebPMuxGetFeatures(const WebPMux* mux, uint32_t* flags) {
  if (mux == NULL || flags == NULL) return WEBP_MUX_INVALID_ARGUMENT;
  return MuxGetCanvasInfo(mux, NULL, NULL, flags);
}

static uint8_t* EmitVP8XChunk(uint8_t* const dst, int width,
                              int height, uint32_t flags) {
  const size_t vp8x_size = CHUNK_HEADER_SIZE + VP8X_CHUNK_SIZE;
  assert(width >= 1 && height >= 1);
  assert(width <= MAX_CANVAS_SIZE && height <= MAX_CANVAS_SIZE);
  assert(width * (uint64_t)height < MAX_IMAGE_AREA);
  PutLE32(dst, MKFOURCC('V', 'P', '8', 'X'));
  PutLE32(dst + TAG_SIZE, VP8X_CHUNK_SIZE);
  PutLE32(dst + CHUNK_HEADER_SIZE, flags);
  PutLE24(dst + CHUNK_HEADER_SIZE + 4, width - 1);
  PutLE24(dst + CHUNK_HEADER_SIZE + 7, height - 1);
  return dst + vp8x_size;
}

// Assemble a single image WebP bitstream from 'wpi'.
static WebPMuxError SynthesizeBitstream(const WebPMuxImage* const wpi,
                                        WebPData* const bitstream) {
  uint8_t* dst;

  // Allocate data.
  const int need_vp8x = (wpi->alpha_ != NULL);
  const size_t vp8x_size = need_vp8x ? CHUNK_HEADER_SIZE + VP8X_CHUNK_SIZE : 0;
  const size_t alpha_size = need_vp8x ? ChunkDiskSize(wpi->alpha_) : 0;
  // Note: No need to output ANMF/FRGM chunk for a single image.
  const size_t size = RIFF_HEADER_SIZE + vp8x_size + alpha_size +
                      ChunkDiskSize(wpi->img_);
  uint8_t* const data = (uint8_t*)malloc(size);
  if (data == NULL) return WEBP_MUX_MEMORY_ERROR;

  // Main RIFF header.
  dst = MuxEmitRiffHeader(data, size);

  if (need_vp8x) {
    dst = EmitVP8XChunk(dst, wpi->width_, wpi->height_, ALPHA_FLAG);  // VP8X.
    dst = ChunkListEmit(wpi->alpha_, dst);       // ALPH.
  }

  // Bitstream.
  dst = ChunkListEmit(wpi->img_, dst);
  assert(dst == data + size);

  // Output.
  bitstream->bytes = data;
  bitstream->size = size;
  return WEBP_MUX_OK;
}

WebPMuxError WebPMuxGetChunk(const WebPMux* mux, const char fourcc[4],
                             WebPData* chunk_data) {
  CHUNK_INDEX idx;
  if (mux == NULL || fourcc == NULL || chunk_data == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }
  idx = ChunkGetIndexFromFourCC(fourcc);
  if (IsWPI(kChunks[idx].id)) {     // An image chunk.
    return WEBP_MUX_INVALID_ARGUMENT;
  } else if (idx != IDX_UNKNOWN) {  // A known chunk type.
    return MuxGet(mux, idx, 1, chunk_data);
  } else {                          // An unknown chunk type.
    const WebPChunk* const chunk =
        ChunkSearchList(mux->unknown_, 1, ChunkGetTagFromFourCC(fourcc));
    if (chunk == NULL) return WEBP_MUX_NOT_FOUND;
    *chunk_data = chunk->data_;
    return WEBP_MUX_OK;
  }
}

static WebPMuxError MuxGetImageInternal(const WebPMuxImage* const wpi,
                                        WebPMuxFrameInfo* const info) {
  // Set some defaults for unrelated fields.
  info->x_offset = 0;
  info->y_offset = 0;
  info->duration = 1;
  info->dispose_method = WEBP_MUX_DISPOSE_NONE;
  info->blend_method = WEBP_MUX_BLEND;
  // Extract data for related fields.
  info->id = ChunkGetIdFromTag(wpi->img_->tag_);
  return SynthesizeBitstream(wpi, &info->bitstream);
}

static WebPMuxError MuxGetFrameFragmentInternal(const WebPMuxImage* const wpi,
                                                WebPMuxFrameInfo* const frame) {
  const int is_frame = (wpi->header_->tag_ == kChunks[IDX_ANMF].tag);
  const CHUNK_INDEX idx = is_frame ? IDX_ANMF : IDX_FRGM;
  const WebPData* frame_frgm_data;
#ifndef WEBP_EXPERIMENTAL_FEATURES
  if (!is_frame) return WEBP_MUX_INVALID_ARGUMENT;
#endif
  assert(wpi->header_ != NULL);  // Already checked by WebPMuxGetFrame().
  // Get frame/fragment chunk.
  frame_frgm_data = &wpi->header_->data_;
  if (frame_frgm_data->size < kChunks[idx].size) return WEBP_MUX_BAD_DATA;
  // Extract info.
  frame->x_offset = 2 * GetLE24(frame_frgm_data->bytes + 0);
  frame->y_offset = 2 * GetLE24(frame_frgm_data->bytes + 3);
  if (is_frame) {
    const uint8_t bits = frame_frgm_data->bytes[15];
    frame->duration = GetLE24(frame_frgm_data->bytes + 12);
    frame->dispose_method =
        (bits & 1) ? WEBP_MUX_DISPOSE_BACKGROUND : WEBP_MUX_DISPOSE_NONE;
    frame->blend_method = (bits & 2) ? WEBP_MUX_NO_BLEND : WEBP_MUX_BLEND;
  } else {  // Defaults for unused values.
    frame->duration = 1;
    frame->dispose_method = WEBP_MUX_DISPOSE_NONE;
    frame->blend_method = WEBP_MUX_BLEND;
  }
  frame->id = ChunkGetIdFromTag(wpi->header_->tag_);
  return SynthesizeBitstream(wpi, &frame->bitstream);
}

WebPMuxError WebPMuxGetFrame(
    const WebPMux* mux, uint32_t nth, WebPMuxFrameInfo* frame) {
  WebPMuxError err;
  WebPMuxImage* wpi;

  // Sanity checks.
  if (mux == NULL || frame == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  // Get the nth WebPMuxImage.
  err = MuxImageGetNth((const WebPMuxImage**)&mux->images_, nth, &wpi);
  if (err != WEBP_MUX_OK) return err;

  // Get frame info.
  if (wpi->header_ == NULL) {
    return MuxGetImageInternal(wpi, frame);
  } else {
    return MuxGetFrameFragmentInternal(wpi, frame);
  }
}

WebPMuxError WebPMuxGetAnimationParams(const WebPMux* mux,
                                       WebPMuxAnimParams* params) {
  WebPData anim;
  WebPMuxError err;

  if (mux == NULL || params == NULL) return WEBP_MUX_INVALID_ARGUMENT;

  err = MuxGet(mux, IDX_ANIM, 1, &anim);
  if (err != WEBP_MUX_OK) return err;
  if (anim.size < kChunks[WEBP_CHUNK_ANIM].size) return WEBP_MUX_BAD_DATA;
  params->bgcolor = GetLE32(anim.bytes);
  params->loop_count = GetLE16(anim.bytes + 4);

  return WEBP_MUX_OK;
}

// Get chunk index from chunk id. Returns IDX_NIL if not found.
static CHUNK_INDEX ChunkGetIndexFromId(WebPChunkId id) {
  int i;
  for (i = 0; kChunks[i].id != WEBP_CHUNK_NIL; ++i) {
    if (id == kChunks[i].id) return (CHUNK_INDEX)i;
  }
  return IDX_NIL;
}

// Count number of chunks matching 'tag' in the 'chunk_list'.
// If tag == NIL_TAG, any tag will be matched.
static int CountChunks(const WebPChunk* const chunk_list, uint32_t tag) {
  int count = 0;
  const WebPChunk* current;
  for (current = chunk_list; current != NULL; current = current->next_) {
    if (tag == NIL_TAG || current->tag_ == tag) {
      count++;  // Count chunks whose tags match.
    }
  }
  return count;
}

WebPMuxError WebPMuxNumChunks(const WebPMux* mux,
                              WebPChunkId id, int* num_elements) {
  if (mux == NULL || num_elements == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  if (IsWPI(id)) {
    *num_elements = MuxImageCount(mux->images_, id);
  } else {
    WebPChunk* const* chunk_list = MuxGetChunkListFromId(mux, id);
    const CHUNK_INDEX idx = ChunkGetIndexFromId(id);
    *num_elements = CountChunks(*chunk_list, kChunks[idx].tag);
  }

  return WEBP_MUX_OK;
}

//------------------------------------------------------------------------------


// Copyright 2011 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Set and delete APIs for mux.
//
// Authors: Urvang (urvang@google.com)
//          Vikas (vikasa@google.com)

#include <assert.h>
#include "./muxi.h"
#include "../utils/utils.h"

//------------------------------------------------------------------------------
// Life of a mux object.

static void MuxInit(WebPMux* const mux) {
  if (mux == NULL) return;
  memset(mux, 0, sizeof(*mux));
}

WebPMux* WebPNewInternal(int version) {
  if (WEBP_ABI_IS_INCOMPATIBLE(version, WEBP_MUX_ABI_VERSION)) {
    return NULL;
  } else {
    WebPMux* const mux = (WebPMux*)malloc(sizeof(WebPMux));
    // If mux is NULL MuxInit is a noop.
    MuxInit(mux);
    return mux;
  }
}

// Delete all images in 'wpi_list'.
static void DeleteAllImages(WebPMuxImage** const wpi_list) {
  while (*wpi_list != NULL) {
    *wpi_list = MuxImageDelete(*wpi_list);
  }
}

static void MuxRelease(WebPMux* const mux) {
  if (mux == NULL) return;
  DeleteAllImages(&mux->images_);
  ChunkListDelete(&mux->vp8x_);
  ChunkListDelete(&mux->iccp_);
  ChunkListDelete(&mux->anim_);
  ChunkListDelete(&mux->exif_);
  ChunkListDelete(&mux->xmp_);
  ChunkListDelete(&mux->unknown_);
}

void WebPMuxDelete(WebPMux* mux) {
  // If mux is NULL MuxRelease is a noop.
  MuxRelease(mux);
  free(mux);
}

//------------------------------------------------------------------------------
// Helper method(s).

// Handy MACRO, makes MuxSet() very symmetric to MuxGet().
#define SWITCH_ID_LIST(INDEX, LIST)                                            \
  if (idx == (INDEX)) {                                                        \
    err = ChunkAssignData(&chunk, data, copy_data, tag);                       \
    if (err == WEBP_MUX_OK) {                                                  \
      err = ChunkSetNth(&chunk, (LIST), nth);                                  \
    }                                                                          \
    return err;                                                                \
  }

static WebPMuxError MuxSet(WebPMux* const mux, uint32_t tag, uint32_t nth,
                           const WebPData* const data, int copy_data) {
  WebPChunk chunk;
  WebPMuxError err = WEBP_MUX_NOT_FOUND;
  const CHUNK_INDEX idx = ChunkGetIndexFromTag(tag);
  assert(mux != NULL);
  assert(!IsWPI(kChunks[idx].id));

  ChunkInit(&chunk);
  SWITCH_ID_LIST(IDX_VP8X,    &mux->vp8x_);
  SWITCH_ID_LIST(IDX_ICCP,    &mux->iccp_);
  SWITCH_ID_LIST(IDX_ANIM,    &mux->anim_);
  SWITCH_ID_LIST(IDX_EXIF,    &mux->exif_);
  SWITCH_ID_LIST(IDX_XMP,     &mux->xmp_);
  SWITCH_ID_LIST(IDX_UNKNOWN, &mux->unknown_);
  return err;
}
#undef SWITCH_ID_LIST

// Create data for frame/fragment given image data, offsets and duration.
static WebPMuxError CreateFrameFragmentData(
    int width, int height, const WebPMuxFrameInfo* const info, int is_frame,
    WebPData* const frame_frgm) {
  uint8_t* frame_frgm_bytes;
  const size_t frame_frgm_size = kChunks[is_frame ? IDX_ANMF : IDX_FRGM].size;

  assert(width > 0 && height > 0 && info->duration >= 0);
  assert(info->dispose_method == (info->dispose_method & 1));
  // Note: assertion on upper bounds is done in PutLE24().

  frame_frgm_bytes = (uint8_t*)malloc(frame_frgm_size);
  if (frame_frgm_bytes == NULL) return WEBP_MUX_MEMORY_ERROR;

  PutLE24(frame_frgm_bytes + 0, info->x_offset / 2);
  PutLE24(frame_frgm_bytes + 3, info->y_offset / 2);

  if (is_frame) {
    PutLE24(frame_frgm_bytes + 6, width - 1);
    PutLE24(frame_frgm_bytes + 9, height - 1);
    PutLE24(frame_frgm_bytes + 12, info->duration);
    frame_frgm_bytes[15] =
        (info->blend_method == WEBP_MUX_NO_BLEND ? 2 : 0) |
        (info->dispose_method == WEBP_MUX_DISPOSE_BACKGROUND ? 1 : 0);
  }

  frame_frgm->bytes = frame_frgm_bytes;
  frame_frgm->size = frame_frgm_size;
  return WEBP_MUX_OK;
}

// Outputs image data given a bitstream. The bitstream can either be a
// single-image WebP file or raw VP8/VP8L data.
// Also outputs 'is_lossless' to be true if the given bitstream is lossless.
static WebPMuxError GetImageData(const WebPData* const bitstream,
                                 WebPData* const image, WebPData* const alpha,
                                 int* const is_lossless) {
  WebPDataInit(alpha);  // Default: no alpha.
  if (bitstream->size < TAG_SIZE ||
      memcmp(bitstream->bytes, "RIFF", TAG_SIZE)) {
    // It is NOT webp file data. Return input data as is.
    *image = *bitstream;
  } else {
    // It is webp file data. Extract image data from it.
    const WebPMuxImage* wpi;
    WebPMux* const mux = WebPMuxCreate(bitstream, 0);
    if (mux == NULL) return WEBP_MUX_BAD_DATA;
    wpi = mux->images_;
    assert(wpi != NULL && wpi->img_ != NULL);
    *image = wpi->img_->data_;
    if (wpi->alpha_ != NULL) {
      *alpha = wpi->alpha_->data_;
    }
    WebPMuxDelete(mux);
  }
  *is_lossless = VP8LCheckSignature(image->bytes, image->size);
  return WEBP_MUX_OK;
}

static WebPMuxError DeleteChunks(WebPChunk** chunk_list, uint32_t tag) {
  WebPMuxError err = WEBP_MUX_NOT_FOUND;
  assert(chunk_list);
  while (*chunk_list) {
    WebPChunk* const chunk = *chunk_list;
    if (chunk->tag_ == tag) {
      *chunk_list = ChunkDelete(chunk);
      err = WEBP_MUX_OK;
    } else {
      chunk_list = &chunk->next_;
    }
  }
  return err;
}

static WebPMuxError MuxDeleteAllNamedData(WebPMux* const mux, uint32_t tag) {
  const WebPChunkId id = ChunkGetIdFromTag(tag);
  assert(mux != NULL);
  if (IsWPI(id)) return WEBP_MUX_INVALID_ARGUMENT;
  return DeleteChunks(MuxGetChunkListFromId(mux, id), tag);
}

//------------------------------------------------------------------------------
// Set API(s).

WebPMuxError WebPMuxSetChunk(WebPMux* mux, const char fourcc[4],
                             const WebPData* chunk_data, int copy_data) {
  uint32_t tag;
  WebPMuxError err;
  if (mux == NULL || fourcc == NULL || chunk_data == NULL ||
      chunk_data->bytes == NULL || chunk_data->size > MAX_CHUNK_PAYLOAD) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }
  tag = ChunkGetTagFromFourCC(fourcc);

  // Delete existing chunk(s) with the same 'fourcc'.
  err = MuxDeleteAllNamedData(mux, tag);
  if (err != WEBP_MUX_OK && err != WEBP_MUX_NOT_FOUND) return err;

  // Add the given chunk.
  return MuxSet(mux, tag, 1, chunk_data, copy_data);
}

// Creates a chunk from given 'data' and sets it as 1st chunk in 'chunk_list'.
static WebPMuxError AddDataToChunkList(
    const WebPData* const data, int copy_data, uint32_t tag,
    WebPChunk** chunk_list) {
  WebPChunk chunk;
  WebPMuxError err;
  ChunkInit(&chunk);
  err = ChunkAssignData(&chunk, data, copy_data, tag);
  if (err != WEBP_MUX_OK) goto Err;
  err = ChunkSetNth(&chunk, chunk_list, 1);
  if (err != WEBP_MUX_OK) goto Err;
  return WEBP_MUX_OK;
 Err:
  ChunkRelease(&chunk);
  return err;
}

// Extracts image & alpha data from the given bitstream and then sets wpi.alpha_
// and wpi.img_ appropriately.
static WebPMuxError SetAlphaAndImageChunks(
    const WebPData* const bitstream, int copy_data, WebPMuxImage* const wpi) {
  int is_lossless = 0;
  WebPData image, alpha;
  WebPMuxError err = GetImageData(bitstream, &image, &alpha, &is_lossless);
  const int image_tag =
      is_lossless ? kChunks[IDX_VP8L].tag : kChunks[IDX_VP8].tag;
  if (err != WEBP_MUX_OK) return err;
  if (alpha.bytes != NULL) {
    err = AddDataToChunkList(&alpha, copy_data, kChunks[IDX_ALPHA].tag,
                             &wpi->alpha_);
    if (err != WEBP_MUX_OK) return err;
  }
  err = AddDataToChunkList(&image, copy_data, image_tag, &wpi->img_);
  if (err != WEBP_MUX_OK) return err;
  return MuxImageFinalize(wpi) ? WEBP_MUX_OK : WEBP_MUX_INVALID_ARGUMENT;
}

WebPMuxError WebPMuxSetImage(WebPMux* mux, const WebPData* bitstream,
                             int copy_data) {
  WebPMuxImage wpi;
  WebPMuxError err;

  // Sanity checks.
  if (mux == NULL || bitstream == NULL || bitstream->bytes == NULL ||
      bitstream->size > MAX_CHUNK_PAYLOAD) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  if (mux->images_ != NULL) {
    // Only one 'simple image' can be added in mux. So, remove present images.
    DeleteAllImages(&mux->images_);
  }

  MuxImageInit(&wpi);
  err = SetAlphaAndImageChunks(bitstream, copy_data, &wpi);
  if (err != WEBP_MUX_OK) goto Err;

  // Add this WebPMuxImage to mux.
  err = MuxImagePush(&wpi, &mux->images_);
  if (err != WEBP_MUX_OK) goto Err;

  // All is well.
  return WEBP_MUX_OK;

 Err:  // Something bad happened.
  MuxImageRelease(&wpi);
  return err;
}

WebPMuxError WebPMuxPushFrame(WebPMux* mux, const WebPMuxFrameInfo* frame,
                              int copy_data) {
  WebPMuxImage wpi;
  WebPMuxError err;
  int is_frame;
  const WebPData* const bitstream = &frame->bitstream;

  // Sanity checks.
  if (mux == NULL || frame == NULL) return WEBP_MUX_INVALID_ARGUMENT;

  is_frame = (frame->id == WEBP_CHUNK_ANMF);
  if (!(is_frame || (frame->id == WEBP_CHUNK_FRGM))) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }
#ifndef WEBP_EXPERIMENTAL_FEATURES
  if (frame->id == WEBP_CHUNK_FRGM) {     // disabled for now.
    return WEBP_MUX_INVALID_ARGUMENT;
  }
#endif

  if (bitstream->bytes == NULL || bitstream->size > MAX_CHUNK_PAYLOAD) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  if (mux->images_ != NULL) {
    const WebPMuxImage* const image = mux->images_;
    const uint32_t image_id = (image->header_ != NULL) ?
        ChunkGetIdFromTag(image->header_->tag_) : WEBP_CHUNK_IMAGE;
    if (image_id != frame->id) {
      return WEBP_MUX_INVALID_ARGUMENT;  // Conflicting frame types.
    }
  }

  MuxImageInit(&wpi);
  err = SetAlphaAndImageChunks(bitstream, copy_data, &wpi);
  if (err != WEBP_MUX_OK) goto Err;
  assert(wpi.img_ != NULL);  // As SetAlphaAndImageChunks() was successful.

  {
    WebPData frame_frgm;
    const uint32_t tag = kChunks[is_frame ? IDX_ANMF : IDX_FRGM].tag;
    WebPMuxFrameInfo tmp = *frame;
    tmp.x_offset &= ~1;  // Snap offsets to even.
    tmp.y_offset &= ~1;
    if (!is_frame) {  // Reset unused values.
      tmp.duration = 1;
      tmp.dispose_method = WEBP_MUX_DISPOSE_NONE;
      tmp.blend_method = WEBP_MUX_BLEND;
    }
    if (tmp.x_offset < 0 || tmp.x_offset >= MAX_POSITION_OFFSET ||
        tmp.y_offset < 0 || tmp.y_offset >= MAX_POSITION_OFFSET ||
        (tmp.duration < 0 || tmp.duration >= MAX_DURATION) ||
        tmp.dispose_method != (tmp.dispose_method & 1)) {
      err = WEBP_MUX_INVALID_ARGUMENT;
      goto Err;
    }
    err = CreateFrameFragmentData(wpi.width_, wpi.height_, &tmp, is_frame,
                                  &frame_frgm);
    if (err != WEBP_MUX_OK) goto Err;
    // Add frame/fragment chunk (with copy_data = 1).
    err = AddDataToChunkList(&frame_frgm, 1, tag, &wpi.header_);
    WebPDataClear(&frame_frgm);  // frame_frgm owned by wpi.header_ now.
    if (err != WEBP_MUX_OK) goto Err;
  }

  // Add this WebPMuxImage to mux.
  err = MuxImagePush(&wpi, &mux->images_);
  if (err != WEBP_MUX_OK) goto Err;

  // All is well.
  return WEBP_MUX_OK;

 Err:  // Something bad happened.
  MuxImageRelease(&wpi);
  return err;
}

WebPMuxError WebPMuxSetAnimationParams(WebPMux* mux,
                                       const WebPMuxAnimParams* params) {
  WebPMuxError err;
  uint8_t data[ANIM_CHUNK_SIZE];
  const WebPData anim = { data, ANIM_CHUNK_SIZE };

  if (mux == NULL || params == NULL) return WEBP_MUX_INVALID_ARGUMENT;
  if (params->loop_count < 0 || params->loop_count >= MAX_LOOP_COUNT) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  // Delete any existing ANIM chunk(s).
  err = MuxDeleteAllNamedData(mux, kChunks[IDX_ANIM].tag);
  if (err != WEBP_MUX_OK && err != WEBP_MUX_NOT_FOUND) return err;

  // Set the animation parameters.
  PutLE32(data, params->bgcolor);
  PutLE16(data + 4, params->loop_count);
  return MuxSet(mux, kChunks[IDX_ANIM].tag, 1, &anim, 1);
}

//------------------------------------------------------------------------------
// Delete API(s).

WebPMuxError WebPMuxDeleteChunk(WebPMux* mux, const char fourcc[4]) {
  if (mux == NULL || fourcc == NULL) return WEBP_MUX_INVALID_ARGUMENT;
  return MuxDeleteAllNamedData(mux, ChunkGetTagFromFourCC(fourcc));
}

WebPMuxError WebPMuxDeleteFrame(WebPMux* mux, uint32_t nth) {
  if (mux == NULL) return WEBP_MUX_INVALID_ARGUMENT;
  return MuxImageDeleteNth(&mux->images_, nth);
}

//------------------------------------------------------------------------------
// Assembly of the WebP RIFF file.

static WebPMuxError GetFrameFragmentInfo(
    const WebPChunk* const frame_frgm_chunk,
    int* const x_offset, int* const y_offset, int* const duration) {
  const uint32_t tag = frame_frgm_chunk->tag_;
  const int is_frame = (tag == kChunks[IDX_ANMF].tag);
  const WebPData* const data = &frame_frgm_chunk->data_;
  const size_t expected_data_size =
      is_frame ? ANMF_CHUNK_SIZE : FRGM_CHUNK_SIZE;
  assert(frame_frgm_chunk != NULL);
  assert(tag == kChunks[IDX_ANMF].tag || tag ==  kChunks[IDX_FRGM].tag);
  if (data->size != expected_data_size) return WEBP_MUX_INVALID_ARGUMENT;

  *x_offset = 2 * GetLE24(data->bytes + 0);
  *y_offset = 2 * GetLE24(data->bytes + 3);
  if (is_frame) *duration = GetLE24(data->bytes + 12);
  return WEBP_MUX_OK;
}

static WebPMuxError GetImageInfo(const WebPMuxImage* const wpi,
                                 int* const x_offset, int* const y_offset,
                                 int* const duration,
                                 int* const width, int* const height) {
  const WebPChunk* const frame_frgm_chunk = wpi->header_;
  WebPMuxError err;
  assert(wpi != NULL);
  assert(frame_frgm_chunk != NULL);

  // Get offsets and duration from ANMF/FRGM chunk.
  err = GetFrameFragmentInfo(frame_frgm_chunk, x_offset, y_offset, duration);
  if (err != WEBP_MUX_OK) return err;

  // Get width and height from VP8/VP8L chunk.
  if (width != NULL) *width = wpi->width_;
  if (height != NULL) *height = wpi->height_;
  return WEBP_MUX_OK;
}

static WebPMuxError GetImageCanvasWidthHeight(
    const WebPMux* const mux, uint32_t flags,
    int* const width, int* const height) {
  WebPMuxImage* wpi = NULL;
  assert(mux != NULL);
  assert(width != NULL && height != NULL);

  wpi = mux->images_;
  assert(wpi != NULL);
  assert(wpi->img_ != NULL);

  if (wpi->next_ != NULL) {
    int max_x = 0;
    int max_y = 0;
    int64_t image_area = 0;
    // if we have a chain of wpi's, header_ is necessarily set
    assert(wpi->header_ != NULL);
    // Aggregate the bounding box for animation frames & fragmented images.
    for (; wpi != NULL; wpi = wpi->next_) {
      int x_offset = 0, y_offset = 0, duration = 0, w = 0, h = 0;
      const WebPMuxError err = GetImageInfo(wpi, &x_offset, &y_offset,
                                            &duration, &w, &h);
      const int max_x_pos = x_offset + w;
      const int max_y_pos = y_offset + h;
      if (err != WEBP_MUX_OK) return err;
      assert(x_offset < MAX_POSITION_OFFSET);
      assert(y_offset < MAX_POSITION_OFFSET);

      if (max_x_pos > max_x) max_x = max_x_pos;
      if (max_y_pos > max_y) max_y = max_y_pos;
      image_area += w * h;
    }
    *width = max_x;
    *height = max_y;
    // Crude check to validate that there are no image overlaps/holes for
    // fragmented images. Check that the aggregated image area for individual
    // fragments exactly matches the image area of the constructed canvas.
    // However, the area-match is necessary but not sufficient condition.
    if ((flags & FRAGMENTS_FLAG) && (image_area != (max_x * max_y))) {
      *width = 0;
      *height = 0;
      return WEBP_MUX_INVALID_ARGUMENT;
    }
  } else {
    // For a single image, canvas dimensions are same as image dimensions.
    *width = wpi->width_;
    *height = wpi->height_;
  }
  return WEBP_MUX_OK;
}

// VP8X format:
// Total Size : 10,
// Flags  : 4 bytes,
// Width  : 3 bytes,
// Height : 3 bytes.
static WebPMuxError CreateVP8XChunk(WebPMux* const mux) {
  WebPMuxError err = WEBP_MUX_OK;
  uint32_t flags = 0;
  int width = 0;
  int height = 0;
  uint8_t data[VP8X_CHUNK_SIZE];
  const WebPData vp8x = { data, VP8X_CHUNK_SIZE };
  const WebPMuxImage* images = NULL;

  assert(mux != NULL);
  images = mux->images_;  // First image.
  if (images == NULL || images->img_ == NULL ||
      images->img_->data_.bytes == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  // If VP8X chunk(s) is(are) already present, remove them (and later add new
  // VP8X chunk with updated flags).
  err = MuxDeleteAllNamedData(mux, kChunks[IDX_VP8X].tag);
  if (err != WEBP_MUX_OK && err != WEBP_MUX_NOT_FOUND) return err;

  // Set flags.
  if (mux->iccp_ != NULL && mux->iccp_->data_.bytes != NULL) {
    flags |= ICCP_FLAG;
  }
  if (mux->exif_ != NULL && mux->exif_->data_.bytes != NULL) {
    flags |= EXIF_FLAG;
  }
  if (mux->xmp_ != NULL && mux->xmp_->data_.bytes != NULL) {
    flags |= XMP_FLAG;
  }
  if (images->header_ != NULL) {
    if (images->header_->tag_ == kChunks[IDX_FRGM].tag) {
      // This is a fragmented image.
      flags |= FRAGMENTS_FLAG;
    } else if (images->header_->tag_ == kChunks[IDX_ANMF].tag) {
      // This is an image with animation.
      flags |= ANIMATION_FLAG;
    }
  }
  if (MuxImageCount(images, WEBP_CHUNK_ALPHA) > 0) {
    flags |= ALPHA_FLAG;  // Some images have an alpha channel.
  }

  if (flags == 0) {
    // For Simple Image, VP8X chunk should not be added.
    return WEBP_MUX_OK;
  }

  err = GetImageCanvasWidthHeight(mux, flags, &width, &height);
  if (err != WEBP_MUX_OK) return err;

  if (width <= 0 || height <= 0) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }
  if (width > MAX_CANVAS_SIZE || height > MAX_CANVAS_SIZE) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  if (MuxHasAlpha(images)) {
    // This means some frames explicitly/implicitly contain alpha.
    // Note: This 'flags' update must NOT be done for a lossless image
    // without a VP8X chunk!
    flags |= ALPHA_FLAG;
  }

  PutLE32(data + 0, flags);   // VP8X chunk flags.
  PutLE24(data + 4, width - 1);   // canvas width.
  PutLE24(data + 7, height - 1);  // canvas height.

  return MuxSet(mux, kChunks[IDX_VP8X].tag, 1, &vp8x, 1);
}

// Cleans up 'mux' by removing any unnecessary chunks.
static WebPMuxError MuxCleanup(WebPMux* const mux) {
  int num_frames;
  int num_fragments;
  int num_anim_chunks;

  // If we have an image with single fragment or frame, convert it to a
  // non-animated non-fragmented image (to avoid writing FRGM/ANMF chunk
  // unnecessarily).
  WebPMuxError err = WebPMuxNumChunks(mux, kChunks[IDX_ANMF].id, &num_frames);
  if (err != WEBP_MUX_OK) return err;
  err = WebPMuxNumChunks(mux, kChunks[IDX_FRGM].id, &num_fragments);
  if (err != WEBP_MUX_OK) return err;
  if (num_frames == 1 || num_fragments == 1) {
    WebPMuxImage* frame_frag;
    err = MuxImageGetNth((const WebPMuxImage**)&mux->images_, 1, &frame_frag);
    assert(err == WEBP_MUX_OK);  // We know that one frame/fragment does exist.
    if (frame_frag->header_ != NULL) {
      assert(frame_frag->header_->tag_ == kChunks[IDX_ANMF].tag ||
             frame_frag->header_->tag_ == kChunks[IDX_FRGM].tag);
      ChunkDelete(frame_frag->header_);  // Removes ANMF/FRGM chunk.
      frame_frag->header_ = NULL;
    }
    num_frames = 0;
    num_fragments = 0;
  }
  // Remove ANIM chunk if this is a non-animated image.
  err = WebPMuxNumChunks(mux, kChunks[IDX_ANIM].id, &num_anim_chunks);
  if (err != WEBP_MUX_OK) return err;
  if (num_anim_chunks >= 1 && num_frames == 0) {
    err = MuxDeleteAllNamedData(mux, kChunks[IDX_ANIM].tag);
    if (err != WEBP_MUX_OK) return err;
  }
  return WEBP_MUX_OK;
}

// Total size of a list of images.
static size_t ImageListDiskSize(const WebPMuxImage* wpi_list) {
  size_t size = 0;
  while (wpi_list != NULL) {
    size += MuxImageDiskSize(wpi_list);
    wpi_list = wpi_list->next_;
  }
  return size;
}

// Write out the given list of images into 'dst'.
static uint8_t* ImageListEmit(const WebPMuxImage* wpi_list, uint8_t* dst) {
  while (wpi_list != NULL) {
    dst = MuxImageEmit(wpi_list, dst);
    wpi_list = wpi_list->next_;
  }
  return dst;
}

WebPMuxError WebPMuxAssemble(WebPMux* mux, WebPData* assembled_data) {
  size_t size = 0;
  uint8_t* data = NULL;
  uint8_t* dst = NULL;
  WebPMuxError err;

  if (mux == NULL || assembled_data == NULL) {
    return WEBP_MUX_INVALID_ARGUMENT;
  }

  // Finalize mux.
  err = MuxCleanup(mux);
  if (err != WEBP_MUX_OK) return err;
  err = CreateVP8XChunk(mux);
  if (err != WEBP_MUX_OK) return err;

  // Allocate data.
  size = ChunkListDiskSize(mux->vp8x_) + ChunkListDiskSize(mux->iccp_)
       + ChunkListDiskSize(mux->anim_) + ImageListDiskSize(mux->images_)
       + ChunkListDiskSize(mux->exif_) + ChunkListDiskSize(mux->xmp_)
       + ChunkListDiskSize(mux->unknown_) + RIFF_HEADER_SIZE;

  data = (uint8_t*)malloc(size);
  if (data == NULL) return WEBP_MUX_MEMORY_ERROR;

  // Emit header & chunks.
  dst = MuxEmitRiffHeader(data, size);
  dst = ChunkListEmit(mux->vp8x_, dst);
  dst = ChunkListEmit(mux->iccp_, dst);
  dst = ChunkListEmit(mux->anim_, dst);
  dst = ImageListEmit(mux->images_, dst);
  dst = ChunkListEmit(mux->exif_, dst);
  dst = ChunkListEmit(mux->xmp_, dst);
  dst = ChunkListEmit(mux->unknown_, dst);
  assert(dst == data + size);

  // Validate mux.
  err = MuxValidate(mux);
  if (err != WEBP_MUX_OK) {
    free(data);
    data = NULL;
    size = 0;
  }

  // Finalize data.
  assembled_data->bytes = data;
  assembled_data->size = size;

  return err;
}

//------------------------------------------------------------------------------


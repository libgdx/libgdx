/*
 * libmad - MPEG audio decoder library
 * Copyright (C) 2000-2004 Underbit Technologies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: frame.c,v 1.29 2004/02/04 22:59:19 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# include <stdlib.h>

# include "bit.h"
# include "stream.h"
# include "frame.h"
# include "timer.h"
# include "layer12.h"
# include "layer3.h"

static
unsigned long const bitrate_table[5][15] = {
  /* MPEG-1 */
  { 0,  32000,  64000,  96000, 128000, 160000, 192000, 224000,  /* Layer I   */
       256000, 288000, 320000, 352000, 384000, 416000, 448000 },
  { 0,  32000,  48000,  56000,  64000,  80000,  96000, 112000,  /* Layer II  */
       128000, 160000, 192000, 224000, 256000, 320000, 384000 },
  { 0,  32000,  40000,  48000,  56000,  64000,  80000,  96000,  /* Layer III */
       112000, 128000, 160000, 192000, 224000, 256000, 320000 },

  /* MPEG-2 LSF */
  { 0,  32000,  48000,  56000,  64000,  80000,  96000, 112000,  /* Layer I   */
       128000, 144000, 160000, 176000, 192000, 224000, 256000 },
  { 0,   8000,  16000,  24000,  32000,  40000,  48000,  56000,  /* Layers    */
        64000,  80000,  96000, 112000, 128000, 144000, 160000 } /* II & III  */
};

static
unsigned int const samplerate_table[3] = { 44100, 48000, 32000 };

static
int (*const decoder_table[3])(struct mad_stream *, struct mad_frame *) = {
  mad_layer_I,
  mad_layer_II,
  mad_layer_III
};

/*
 * NAME:	header->init()
 * DESCRIPTION:	initialize header struct
 */
void mad_header_init(struct mad_header *header)
{
  header->layer          = 0;
  header->mode           = 0;
  header->mode_extension = 0;
  header->emphasis       = 0;

  header->bitrate        = 0;
  header->samplerate     = 0;

  header->crc_check      = 0;
  header->crc_target     = 0;

  header->flags          = 0;
  header->private_bits   = 0;

  header->duration       = mad_timer_zero;
}

/*
 * NAME:	frame->init()
 * DESCRIPTION:	initialize frame struct
 */
void mad_frame_init(struct mad_frame *frame)
{
  mad_header_init(&frame->header);

  frame->options = 0;

  frame->overlap = 0;
  mad_frame_mute(frame);
}

/*
 * NAME:	frame->finish()
 * DESCRIPTION:	deallocate any dynamic memory associated with frame
 */
void mad_frame_finish(struct mad_frame *frame)
{
  mad_header_finish(&frame->header);

  if (frame->overlap) {
    free(frame->overlap);
    frame->overlap = 0;
  }
}

/*
 * NAME:	decode_header()
 * DESCRIPTION:	read header data and following CRC word
 */
static
int decode_header(struct mad_header *header, struct mad_stream *stream)
{
  unsigned int index;

  header->flags        = 0;
  header->private_bits = 0;

  /* header() */

  /* syncword */
  mad_bit_skip(&stream->ptr, 11);

  /* MPEG 2.5 indicator (really part of syncword) */
  if (mad_bit_read(&stream->ptr, 1) == 0)
    header->flags |= MAD_FLAG_MPEG_2_5_EXT;

  /* ID */
  if (mad_bit_read(&stream->ptr, 1) == 0)
    header->flags |= MAD_FLAG_LSF_EXT;
  else if (header->flags & MAD_FLAG_MPEG_2_5_EXT) {
    stream->error = MAD_ERROR_LOSTSYNC;
    return -1;
  }

  /* layer */
  header->layer = 4 - mad_bit_read(&stream->ptr, 2);

  if (header->layer == 4) {
    stream->error = MAD_ERROR_BADLAYER;
    return -1;
  }

  /* protection_bit */
  if (mad_bit_read(&stream->ptr, 1) == 0) {
    header->flags    |= MAD_FLAG_PROTECTION;
    header->crc_check = mad_bit_crc(stream->ptr, 16, 0xffff);
  }

  /* bitrate_index */
  index = mad_bit_read(&stream->ptr, 4);

  if (index == 15) {
    stream->error = MAD_ERROR_BADBITRATE;
    return -1;
  }

  if (header->flags & MAD_FLAG_LSF_EXT)
    header->bitrate = bitrate_table[3 + (header->layer >> 1)][index];
  else
    header->bitrate = bitrate_table[header->layer - 1][index];

  /* sampling_frequency */
  index = mad_bit_read(&stream->ptr, 2);

  if (index == 3) {
    stream->error = MAD_ERROR_BADSAMPLERATE;
    return -1;
  }

  header->samplerate = samplerate_table[index];

  if (header->flags & MAD_FLAG_LSF_EXT) {
    header->samplerate /= 2;

    if (header->flags & MAD_FLAG_MPEG_2_5_EXT)
      header->samplerate /= 2;
  }

  /* padding_bit */
  if (mad_bit_read(&stream->ptr, 1))
    header->flags |= MAD_FLAG_PADDING;

  /* private_bit */
  if (mad_bit_read(&stream->ptr, 1))
    header->private_bits |= MAD_PRIVATE_HEADER;

  /* mode */
  header->mode = 3 - mad_bit_read(&stream->ptr, 2);

  /* mode_extension */
  header->mode_extension = mad_bit_read(&stream->ptr, 2);

  /* copyright */
  if (mad_bit_read(&stream->ptr, 1))
    header->flags |= MAD_FLAG_COPYRIGHT;

  /* original/copy */
  if (mad_bit_read(&stream->ptr, 1))
    header->flags |= MAD_FLAG_ORIGINAL;

  /* emphasis */
  header->emphasis = mad_bit_read(&stream->ptr, 2);

# if defined(OPT_STRICT)
  /*
   * ISO/IEC 11172-3 says this is a reserved emphasis value, but
   * streams exist which use it anyway. Since the value is not important
   * to the decoder proper, we allow it unless OPT_STRICT is defined.
   */
  if (header->emphasis == MAD_EMPHASIS_RESERVED) {
    stream->error = MAD_ERROR_BADEMPHASIS;
    return -1;
  }
# endif

  /* error_check() */

  /* crc_check */
  if (header->flags & MAD_FLAG_PROTECTION)
    header->crc_target = mad_bit_read(&stream->ptr, 16);

  return 0;
}

/*
 * NAME:	free_bitrate()
 * DESCRIPTION:	attempt to discover the bitstream's free bitrate
 */
static
int free_bitrate(struct mad_stream *stream, struct mad_header const *header)
{
  struct mad_bitptr keep_ptr;
  unsigned long rate = 0;
  unsigned int pad_slot, slots_per_frame;
  unsigned char const *ptr = 0;

  keep_ptr = stream->ptr;

  pad_slot = (header->flags & MAD_FLAG_PADDING) ? 1 : 0;
  slots_per_frame = (header->layer == MAD_LAYER_III &&
		     (header->flags & MAD_FLAG_LSF_EXT)) ? 72 : 144;

  while (mad_stream_sync(stream) == 0) {
    struct mad_stream peek_stream;
    struct mad_header peek_header;

    peek_stream = *stream;
    peek_header = *header;

    if (decode_header(&peek_header, &peek_stream) == 0 &&
	peek_header.layer == header->layer &&
	peek_header.samplerate == header->samplerate) {
      unsigned int N;

      ptr = mad_bit_nextbyte(&stream->ptr);

      N = ptr - stream->this_frame;

      if (header->layer == MAD_LAYER_I) {
	rate = (unsigned long) header->samplerate *
	  (N - 4 * pad_slot + 4) / 48 / 1000;
      }
      else {
	rate = (unsigned long) header->samplerate *
	  (N - pad_slot + 1) / slots_per_frame / 1000;
      }

      if (rate >= 8)
	break;
    }

    mad_bit_skip(&stream->ptr, 8);
  }

  stream->ptr = keep_ptr;

  if (rate < 8 || (header->layer == MAD_LAYER_III && rate > 640)) {
    stream->error = MAD_ERROR_LOSTSYNC;
    return -1;
  }

  stream->freerate = rate * 1000;

  return 0;
}

/*
 * NAME:	header->decode()
 * DESCRIPTION:	read the next frame header from the stream
 */
int mad_header_decode(struct mad_header *header, struct mad_stream *stream)
{
  register unsigned char const *ptr, *end;
  unsigned int pad_slot, N;

  ptr = stream->next_frame;
  end = stream->bufend;

  if (ptr == 0) {
    stream->error = MAD_ERROR_BUFPTR;
    goto fail;
  }

  /* stream skip */
  if (stream->skiplen) {
    if (!stream->sync)
      ptr = stream->this_frame;

    if (end - ptr < stream->skiplen) {
      stream->skiplen   -= end - ptr;
      stream->next_frame = end;

      stream->error = MAD_ERROR_BUFLEN;
      goto fail;
    }

    ptr += stream->skiplen;
    stream->skiplen = 0;

    stream->sync = 1;
  }

 sync:
  /* synchronize */
  if (stream->sync) {
    if (end - ptr < MAD_BUFFER_GUARD) {
      stream->next_frame = ptr;

      stream->error = MAD_ERROR_BUFLEN;
      goto fail;
    }
    else if (!(ptr[0] == 0xff && (ptr[1] & 0xe0) == 0xe0)) {
      /* mark point where frame sync word was expected */
      stream->this_frame = ptr;
      stream->next_frame = ptr + 1;

      stream->error = MAD_ERROR_LOSTSYNC;
      goto fail;
    }
  }
  else {
    mad_bit_init(&stream->ptr, ptr);

    if (mad_stream_sync(stream) == -1) {
      if (end - stream->next_frame >= MAD_BUFFER_GUARD)
	stream->next_frame = end - MAD_BUFFER_GUARD;

      stream->error = MAD_ERROR_BUFLEN;
      goto fail;
    }

    ptr = mad_bit_nextbyte(&stream->ptr);
  }

  /* begin processing */
  stream->this_frame = ptr;
  stream->next_frame = ptr + 1;  /* possibly bogus sync word */

  mad_bit_init(&stream->ptr, stream->this_frame);

  if (decode_header(header, stream) == -1)
    goto fail;

  /* calculate frame duration */
  mad_timer_set(&header->duration, 0,
		32 * MAD_NSBSAMPLES(header), header->samplerate);

  /* calculate free bit rate */
  if (header->bitrate == 0) {
    if ((stream->freerate == 0 || !stream->sync ||
	 (header->layer == MAD_LAYER_III && stream->freerate > 640000)) &&
	free_bitrate(stream, header) == -1)
      goto fail;

    header->bitrate = stream->freerate;
    header->flags  |= MAD_FLAG_FREEFORMAT;
  }

  /* calculate beginning of next frame */
  pad_slot = (header->flags & MAD_FLAG_PADDING) ? 1 : 0;

  if (header->layer == MAD_LAYER_I)
    N = ((12 * header->bitrate / header->samplerate) + pad_slot) * 4;
  else {
    unsigned int slots_per_frame;

    slots_per_frame = (header->layer == MAD_LAYER_III &&
		       (header->flags & MAD_FLAG_LSF_EXT)) ? 72 : 144;

    N = (slots_per_frame * header->bitrate / header->samplerate) + pad_slot;
  }

  /* verify there is enough data left in buffer to decode this frame */
  if (N + MAD_BUFFER_GUARD > end - stream->this_frame) {
    stream->next_frame = stream->this_frame;

    stream->error = MAD_ERROR_BUFLEN;
    goto fail;
  }

  stream->next_frame = stream->this_frame + N;

  if (!stream->sync) {
    /* check that a valid frame header follows this frame */

    ptr = stream->next_frame;
    if (!(ptr[0] == 0xff && (ptr[1] & 0xe0) == 0xe0)) {
      ptr = stream->next_frame = stream->this_frame + 1;
      goto sync;
    }

    stream->sync = 1;
  }

  header->flags |= MAD_FLAG_INCOMPLETE;

  return 0;

 fail:
  stream->sync = 0;

  return -1;
}

/*
 * NAME:	frame->decode()
 * DESCRIPTION:	decode a single frame from a bitstream
 */
int mad_frame_decode(struct mad_frame *frame, struct mad_stream *stream)
{
  frame->options = stream->options;

  /* header() */
  /* error_check() */

  if (!(frame->header.flags & MAD_FLAG_INCOMPLETE) &&
      mad_header_decode(&frame->header, stream) == -1)
    goto fail;

  /* audio_data() */

  frame->header.flags &= ~MAD_FLAG_INCOMPLETE;

  if (decoder_table[frame->header.layer - 1](stream, frame) == -1) {
    if (!MAD_RECOVERABLE(stream->error))
      stream->next_frame = stream->this_frame;

    goto fail;
  }

  /* ancillary_data() */

  if (frame->header.layer != MAD_LAYER_III) {
    struct mad_bitptr next_frame;

    mad_bit_init(&next_frame, stream->next_frame);

    stream->anc_ptr    = stream->ptr;
    stream->anc_bitlen = mad_bit_length(&stream->ptr, &next_frame);

    mad_bit_finish(&next_frame);
  }

  return 0;

 fail:
  stream->anc_bitlen = 0;
  return -1;
}

/*
 * NAME:	frame->mute()
 * DESCRIPTION:	zero all subband values so the frame becomes silent
 */
void mad_frame_mute(struct mad_frame *frame)
{
  unsigned int s, sb;

  for (s = 0; s < 36; ++s) {
    for (sb = 0; sb < 32; ++sb) {
      frame->sbsample[0][s][sb] =
      frame->sbsample[1][s][sb] = 0;
    }
  }

  if (frame->overlap) {
    for (s = 0; s < 18; ++s) {
      for (sb = 0; sb < 32; ++sb) {
	(*frame->overlap)[0][sb][s] =
	(*frame->overlap)[1][sb][s] = 0;
      }
    }
  }
}

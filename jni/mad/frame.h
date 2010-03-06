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
 * $Id: frame.h,v 1.20 2004/01/23 09:41:32 rob Exp $
 */

# ifndef LIBMAD_FRAME_H
# define LIBMAD_FRAME_H

# include "fixed.h"
# include "timer.h"
# include "stream.h"

enum mad_layer {
  MAD_LAYER_I   = 1,			/* Layer I */
  MAD_LAYER_II  = 2,			/* Layer II */
  MAD_LAYER_III = 3			/* Layer III */
};

enum mad_mode {
  MAD_MODE_SINGLE_CHANNEL = 0,		/* single channel */
  MAD_MODE_DUAL_CHANNEL	  = 1,		/* dual channel */
  MAD_MODE_JOINT_STEREO	  = 2,		/* joint (MS/intensity) stereo */
  MAD_MODE_STEREO	  = 3		/* normal LR stereo */
};

enum mad_emphasis {
  MAD_EMPHASIS_NONE	  = 0,		/* no emphasis */
  MAD_EMPHASIS_50_15_US	  = 1,		/* 50/15 microseconds emphasis */
  MAD_EMPHASIS_CCITT_J_17 = 3,		/* CCITT J.17 emphasis */
  MAD_EMPHASIS_RESERVED   = 2		/* unknown emphasis */
};

struct mad_header {
  enum mad_layer layer;			/* audio layer (1, 2, or 3) */
  enum mad_mode mode;			/* channel mode (see above) */
  int mode_extension;			/* additional mode info */
  enum mad_emphasis emphasis;		/* de-emphasis to use (see above) */

  unsigned long bitrate;		/* stream bitrate (bps) */
  unsigned int samplerate;		/* sampling frequency (Hz) */

  unsigned short crc_check;		/* frame CRC accumulator */
  unsigned short crc_target;		/* final target CRC checksum */

  int flags;				/* flags (see below) */
  int private_bits;			/* private bits (see below) */

  mad_timer_t duration;			/* audio playing time of frame */
};

struct mad_frame {
  struct mad_header header;		/* MPEG audio header */

  int options;				/* decoding options (from stream) */

  mad_fixed_t sbsample[2][36][32];	/* synthesis subband filter samples */
  mad_fixed_t (*overlap)[2][32][18];	/* Layer III block overlap data */
};

# define MAD_NCHANNELS(header)		((header)->mode ? 2 : 1)
# define MAD_NSBSAMPLES(header)  \
  ((header)->layer == MAD_LAYER_I ? 12 :  \
   (((header)->layer == MAD_LAYER_III &&  \
     ((header)->flags & MAD_FLAG_LSF_EXT)) ? 18 : 36))

enum {
  MAD_FLAG_NPRIVATE_III	= 0x0007,	/* number of Layer III private bits */
  MAD_FLAG_INCOMPLETE	= 0x0008,	/* header but not data is decoded */

  MAD_FLAG_PROTECTION	= 0x0010,	/* frame has CRC protection */
  MAD_FLAG_COPYRIGHT	= 0x0020,	/* frame is copyright */
  MAD_FLAG_ORIGINAL	= 0x0040,	/* frame is original (else copy) */
  MAD_FLAG_PADDING	= 0x0080,	/* frame has additional slot */

  MAD_FLAG_I_STEREO	= 0x0100,	/* uses intensity joint stereo */
  MAD_FLAG_MS_STEREO	= 0x0200,	/* uses middle/side joint stereo */
  MAD_FLAG_FREEFORMAT	= 0x0400,	/* uses free format bitrate */

  MAD_FLAG_LSF_EXT	= 0x1000,	/* lower sampling freq. extension */
  MAD_FLAG_MC_EXT	= 0x2000,	/* multichannel audio extension */
  MAD_FLAG_MPEG_2_5_EXT	= 0x4000	/* MPEG 2.5 (unofficial) extension */
};

enum {
  MAD_PRIVATE_HEADER	= 0x0100,	/* header private bit */
  MAD_PRIVATE_III	= 0x001f	/* Layer III private bits (up to 5) */
};

void mad_header_init(struct mad_header *);

# define mad_header_finish(header)  /* nothing */

int mad_header_decode(struct mad_header *, struct mad_stream *);

void mad_frame_init(struct mad_frame *);
void mad_frame_finish(struct mad_frame *);

int mad_frame_decode(struct mad_frame *, struct mad_stream *);

void mad_frame_mute(struct mad_frame *);

# endif

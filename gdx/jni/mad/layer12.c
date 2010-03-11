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
 * $Id: layer12.c,v 1.17 2004/02/05 09:02:39 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# ifdef HAVE_LIMITS_H
#  include <limits.h>
# else
#  define CHAR_BIT  8
# endif

# include "fixed.h"
# include "bit.h"
# include "stream.h"
# include "frame.h"
# include "layer12.h"

/*
 * scalefactor table
 * used in both Layer I and Layer II decoding
 */
static
mad_fixed_t const sf_table[64] = {
# include "sf_table.dat"
};

/* --- Layer I ------------------------------------------------------------- */

/* linear scaling table */
static
mad_fixed_t const linear_table[14] = {
  MAD_F(0x15555555),  /* 2^2  / (2^2  - 1) == 1.33333333333333 */
  MAD_F(0x12492492),  /* 2^3  / (2^3  - 1) == 1.14285714285714 */
  MAD_F(0x11111111),  /* 2^4  / (2^4  - 1) == 1.06666666666667 */
  MAD_F(0x10842108),  /* 2^5  / (2^5  - 1) == 1.03225806451613 */
  MAD_F(0x10410410),  /* 2^6  / (2^6  - 1) == 1.01587301587302 */
  MAD_F(0x10204081),  /* 2^7  / (2^7  - 1) == 1.00787401574803 */
  MAD_F(0x10101010),  /* 2^8  / (2^8  - 1) == 1.00392156862745 */
  MAD_F(0x10080402),  /* 2^9  / (2^9  - 1) == 1.00195694716243 */
  MAD_F(0x10040100),  /* 2^10 / (2^10 - 1) == 1.00097751710655 */
  MAD_F(0x10020040),  /* 2^11 / (2^11 - 1) == 1.00048851978505 */
  MAD_F(0x10010010),  /* 2^12 / (2^12 - 1) == 1.00024420024420 */
  MAD_F(0x10008004),  /* 2^13 / (2^13 - 1) == 1.00012208521548 */
  MAD_F(0x10004001),  /* 2^14 / (2^14 - 1) == 1.00006103888177 */
  MAD_F(0x10002000)   /* 2^15 / (2^15 - 1) == 1.00003051850948 */
};

/*
 * NAME:	I_sample()
 * DESCRIPTION:	decode one requantized Layer I sample from a bitstream
 */
static
mad_fixed_t I_sample(struct mad_bitptr *ptr, unsigned int nb)
{
  mad_fixed_t sample;

  sample = mad_bit_read(ptr, nb);

  /* invert most significant bit, extend sign, then scale to fixed format */

  sample ^= 1 << (nb - 1);
  sample |= -(sample & (1 << (nb - 1)));

  sample <<= MAD_F_FRACBITS - (nb - 1);

  /* requantize the sample */

  /* s'' = (2^nb / (2^nb - 1)) * (s''' + 2^(-nb + 1)) */

  sample += MAD_F_ONE >> (nb - 1);

  return mad_f_mul(sample, linear_table[nb - 2]);

  /* s' = factor * s'' */
  /* (to be performed by caller) */
}

/*
 * NAME:	layer->I()
 * DESCRIPTION:	decode a single Layer I frame
 */
int mad_layer_I(struct mad_stream *stream, struct mad_frame *frame)
{
  struct mad_header *header = &frame->header;
  unsigned int nch, bound, ch, s, sb, nb;
  unsigned char allocation[2][32], scalefactor[2][32];

  nch = MAD_NCHANNELS(header);

  bound = 32;
  if (header->mode == MAD_MODE_JOINT_STEREO) {
    header->flags |= MAD_FLAG_I_STEREO;
    bound = 4 + header->mode_extension * 4;
  }

  /* check CRC word */

  if (header->flags & MAD_FLAG_PROTECTION) {
    header->crc_check =
      mad_bit_crc(stream->ptr, 4 * (bound * nch + (32 - bound)),
		  header->crc_check);

    if (header->crc_check != header->crc_target &&
	!(frame->options & MAD_OPTION_IGNORECRC)) {
      stream->error = MAD_ERROR_BADCRC;
      return -1;
    }
  }

  /* decode bit allocations */

  for (sb = 0; sb < bound; ++sb) {
    for (ch = 0; ch < nch; ++ch) {
      nb = mad_bit_read(&stream->ptr, 4);

      if (nb == 15) {
	stream->error = MAD_ERROR_BADBITALLOC;
	return -1;
      }

      allocation[ch][sb] = nb ? nb + 1 : 0;
    }
  }

  for (sb = bound; sb < 32; ++sb) {
    nb = mad_bit_read(&stream->ptr, 4);

    if (nb == 15) {
      stream->error = MAD_ERROR_BADBITALLOC;
      return -1;
    }

    allocation[0][sb] =
    allocation[1][sb] = nb ? nb + 1 : 0;
  }

  /* decode scalefactors */

  for (sb = 0; sb < 32; ++sb) {
    for (ch = 0; ch < nch; ++ch) {
      if (allocation[ch][sb]) {
	scalefactor[ch][sb] = mad_bit_read(&stream->ptr, 6);

# if defined(OPT_STRICT)
	/*
	 * Scalefactor index 63 does not appear in Table B.1 of
	 * ISO/IEC 11172-3. Nonetheless, other implementations accept it,
	 * so we only reject it if OPT_STRICT is defined.
	 */
	if (scalefactor[ch][sb] == 63) {
	  stream->error = MAD_ERROR_BADSCALEFACTOR;
	  return -1;
	}
# endif
      }
    }
  }

  /* decode samples */

  for (s = 0; s < 12; ++s) {
    for (sb = 0; sb < bound; ++sb) {
      for (ch = 0; ch < nch; ++ch) {
	nb = allocation[ch][sb];
	frame->sbsample[ch][s][sb] = nb ?
	  mad_f_mul(I_sample(&stream->ptr, nb),
		    sf_table[scalefactor[ch][sb]]) : 0;
      }
    }

    for (sb = bound; sb < 32; ++sb) {
      if ((nb = allocation[0][sb])) {
	mad_fixed_t sample;

	sample = I_sample(&stream->ptr, nb);

	for (ch = 0; ch < nch; ++ch) {
	  frame->sbsample[ch][s][sb] =
	    mad_f_mul(sample, sf_table[scalefactor[ch][sb]]);
	}
      }
      else {
	for (ch = 0; ch < nch; ++ch)
	  frame->sbsample[ch][s][sb] = 0;
      }
    }
  }

  return 0;
}

/* --- Layer II ------------------------------------------------------------ */

/* possible quantization per subband table */
static
struct {
  unsigned int sblimit;
  unsigned char const offsets[30];
} const sbquant_table[5] = {
  /* ISO/IEC 11172-3 Table B.2a */
  { 27, { 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 3, 3, 3, 3, 3,	/* 0 */
	  3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0 } },
  /* ISO/IEC 11172-3 Table B.2b */
  { 30, { 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 3, 3, 3, 3, 3,	/* 1 */
	  3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 } },
  /* ISO/IEC 11172-3 Table B.2c */
  {  8, { 5, 5, 2, 2, 2, 2, 2, 2 } },				/* 2 */
  /* ISO/IEC 11172-3 Table B.2d */
  { 12, { 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 } },		/* 3 */
  /* ISO/IEC 13818-3 Table B.1 */
  { 30, { 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1,	/* 4 */
	  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } }
};

/* bit allocation table */
static
struct {
  unsigned short nbal;
  unsigned short offset;
} const bitalloc_table[8] = {
  { 2, 0 },  /* 0 */
  { 2, 3 },  /* 1 */
  { 3, 3 },  /* 2 */
  { 3, 1 },  /* 3 */
  { 4, 2 },  /* 4 */
  { 4, 3 },  /* 5 */
  { 4, 4 },  /* 6 */
  { 4, 5 }   /* 7 */
};

/* offsets into quantization class table */
static
unsigned char const offset_table[6][15] = {
  { 0, 1, 16                                             },  /* 0 */
  { 0, 1,  2, 3, 4, 5, 16                                },  /* 1 */
  { 0, 1,  2, 3, 4, 5,  6, 7,  8,  9, 10, 11, 12, 13, 14 },  /* 2 */
  { 0, 1,  3, 4, 5, 6,  7, 8,  9, 10, 11, 12, 13, 14, 15 },  /* 3 */
  { 0, 1,  2, 3, 4, 5,  6, 7,  8,  9, 10, 11, 12, 13, 16 },  /* 4 */
  { 0, 2,  4, 5, 6, 7,  8, 9, 10, 11, 12, 13, 14, 15, 16 }   /* 5 */
};

/* quantization class table */
static
struct quantclass {
  unsigned short nlevels;
  unsigned char group;
  unsigned char bits;
  mad_fixed_t C;
  mad_fixed_t D;
} const qc_table[17] = {
# include "qc_table.dat"
};

/*
 * NAME:	II_samples()
 * DESCRIPTION:	decode three requantized Layer II samples from a bitstream
 */
static
void II_samples(struct mad_bitptr *ptr,
		struct quantclass const *quantclass,
		mad_fixed_t output[3])
{
  unsigned int nb, s, sample[3];

  if ((nb = quantclass->group)) {
    unsigned int c, nlevels;

    /* degrouping */
    c = mad_bit_read(ptr, quantclass->bits);
    nlevels = quantclass->nlevels;

    for (s = 0; s < 3; ++s) {
      sample[s] = c % nlevels;
      c /= nlevels;
    }
  }
  else {
    nb = quantclass->bits;

    for (s = 0; s < 3; ++s)
      sample[s] = mad_bit_read(ptr, nb);
  }

  for (s = 0; s < 3; ++s) {
    mad_fixed_t requantized;

    /* invert most significant bit, extend sign, then scale to fixed format */

    requantized  = sample[s] ^ (1 << (nb - 1));
    requantized |= -(requantized & (1 << (nb - 1)));

    requantized <<= MAD_F_FRACBITS - (nb - 1);

    /* requantize the sample */

    /* s'' = C * (s''' + D) */

    output[s] = mad_f_mul(requantized + quantclass->D, quantclass->C);

    /* s' = factor * s'' */
    /* (to be performed by caller) */
  }
}

/*
 * NAME:	layer->II()
 * DESCRIPTION:	decode a single Layer II frame
 */
int mad_layer_II(struct mad_stream *stream, struct mad_frame *frame)
{
  struct mad_header *header = &frame->header;
  struct mad_bitptr start;
  unsigned int index, sblimit, nbal, nch, bound, gr, ch, s, sb;
  unsigned char const *offsets;
  unsigned char allocation[2][32], scfsi[2][32], scalefactor[2][32][3];
  mad_fixed_t samples[3];

  nch = MAD_NCHANNELS(header);

  if (header->flags & MAD_FLAG_LSF_EXT)
    index = 4;
  else if (header->flags & MAD_FLAG_FREEFORMAT)
    goto freeformat;
  else {
    unsigned long bitrate_per_channel;

    bitrate_per_channel = header->bitrate;
    if (nch == 2) {
      bitrate_per_channel /= 2;

# if defined(OPT_STRICT)
      /*
       * ISO/IEC 11172-3 allows only single channel mode for 32, 48, 56, and
       * 80 kbps bitrates in Layer II, but some encoders ignore this
       * restriction. We enforce it if OPT_STRICT is defined.
       */
      if (bitrate_per_channel <= 28000 || bitrate_per_channel == 40000) {
	stream->error = MAD_ERROR_BADMODE;
	return -1;
      }
# endif
    }
    else {  /* nch == 1 */
      if (bitrate_per_channel > 192000) {
	/*
	 * ISO/IEC 11172-3 does not allow single channel mode for 224, 256,
	 * 320, or 384 kbps bitrates in Layer II.
	 */
	stream->error = MAD_ERROR_BADMODE;
	return -1;
      }
    }

    if (bitrate_per_channel <= 48000)
      index = (header->samplerate == 32000) ? 3 : 2;
    else if (bitrate_per_channel <= 80000)
      index = 0;
    else {
    freeformat:
      index = (header->samplerate == 48000) ? 0 : 1;
    }
  }

  sblimit = sbquant_table[index].sblimit;
  offsets = sbquant_table[index].offsets;

  bound = 32;
  if (header->mode == MAD_MODE_JOINT_STEREO) {
    header->flags |= MAD_FLAG_I_STEREO;
    bound = 4 + header->mode_extension * 4;
  }

  if (bound > sblimit)
    bound = sblimit;

  start = stream->ptr;

  /* decode bit allocations */

  for (sb = 0; sb < bound; ++sb) {
    nbal = bitalloc_table[offsets[sb]].nbal;

    for (ch = 0; ch < nch; ++ch)
      allocation[ch][sb] = mad_bit_read(&stream->ptr, nbal);
  }

  for (sb = bound; sb < sblimit; ++sb) {
    nbal = bitalloc_table[offsets[sb]].nbal;

    allocation[0][sb] =
    allocation[1][sb] = mad_bit_read(&stream->ptr, nbal);
  }

  /* decode scalefactor selection info */

  for (sb = 0; sb < sblimit; ++sb) {
    for (ch = 0; ch < nch; ++ch) {
      if (allocation[ch][sb])
	scfsi[ch][sb] = mad_bit_read(&stream->ptr, 2);
    }
  }

  /* check CRC word */

  if (header->flags & MAD_FLAG_PROTECTION) {
    header->crc_check =
      mad_bit_crc(start, mad_bit_length(&start, &stream->ptr),
		  header->crc_check);

    if (header->crc_check != header->crc_target &&
	!(frame->options & MAD_OPTION_IGNORECRC)) {
      stream->error = MAD_ERROR_BADCRC;
      return -1;
    }
  }

  /* decode scalefactors */

  for (sb = 0; sb < sblimit; ++sb) {
    for (ch = 0; ch < nch; ++ch) {
      if (allocation[ch][sb]) {
	scalefactor[ch][sb][0] = mad_bit_read(&stream->ptr, 6);

	switch (scfsi[ch][sb]) {
	case 2:
	  scalefactor[ch][sb][2] =
	  scalefactor[ch][sb][1] =
	  scalefactor[ch][sb][0];
	  break;

	case 0:
	  scalefactor[ch][sb][1] = mad_bit_read(&stream->ptr, 6);
	  /* fall through */

	case 1:
	case 3:
	  scalefactor[ch][sb][2] = mad_bit_read(&stream->ptr, 6);
	}

	if (scfsi[ch][sb] & 1)
	  scalefactor[ch][sb][1] = scalefactor[ch][sb][scfsi[ch][sb] - 1];

# if defined(OPT_STRICT)
	/*
	 * Scalefactor index 63 does not appear in Table B.1 of
	 * ISO/IEC 11172-3. Nonetheless, other implementations accept it,
	 * so we only reject it if OPT_STRICT is defined.
	 */
	if (scalefactor[ch][sb][0] == 63 ||
	    scalefactor[ch][sb][1] == 63 ||
	    scalefactor[ch][sb][2] == 63) {
	  stream->error = MAD_ERROR_BADSCALEFACTOR;
	  return -1;
	}
# endif
      }
    }
  }

  /* decode samples */

  for (gr = 0; gr < 12; ++gr) {
    for (sb = 0; sb < bound; ++sb) {
      for (ch = 0; ch < nch; ++ch) {
	if ((index = allocation[ch][sb])) {
	  index = offset_table[bitalloc_table[offsets[sb]].offset][index - 1];

	  II_samples(&stream->ptr, &qc_table[index], samples);

	  for (s = 0; s < 3; ++s) {
	    frame->sbsample[ch][3 * gr + s][sb] =
	      mad_f_mul(samples[s], sf_table[scalefactor[ch][sb][gr / 4]]);
	  }
	}
	else {
	  for (s = 0; s < 3; ++s)
	    frame->sbsample[ch][3 * gr + s][sb] = 0;
	}
      }
    }

    for (sb = bound; sb < sblimit; ++sb) {
      if ((index = allocation[0][sb])) {
	index = offset_table[bitalloc_table[offsets[sb]].offset][index - 1];

	II_samples(&stream->ptr, &qc_table[index], samples);

	for (ch = 0; ch < nch; ++ch) {
	  for (s = 0; s < 3; ++s) {
	    frame->sbsample[ch][3 * gr + s][sb] =
	      mad_f_mul(samples[s], sf_table[scalefactor[ch][sb][gr / 4]]);
	  }
	}
      }
      else {
	for (ch = 0; ch < nch; ++ch) {
	  for (s = 0; s < 3; ++s)
	    frame->sbsample[ch][3 * gr + s][sb] = 0;
	}
      }
    }

    for (ch = 0; ch < nch; ++ch) {
      for (s = 0; s < 3; ++s) {
	for (sb = sblimit; sb < 32; ++sb)
	  frame->sbsample[ch][3 * gr + s][sb] = 0;
      }
    }
  }

  return 0;
}

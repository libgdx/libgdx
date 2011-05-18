/*
	sample.h: The conversion from internal data to output samples of differing formats.

	copyright 2007-9 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis, taking WRITE_SAMPLE from decode.c
	Later added the end-conversion specific macros here, too.
*/

#ifndef SAMPLE_H
#define SAMPLE_H

/* mpg123lib_intern.h is included already, right? */

/* Special case is fixed point math... which does work, but not that nice yet.  */
static inline short idiv_signed_rounded(long x, int shift)
{
	x >>= (shift - 1);
	x += (x & 1);
	return (short)(x >> 1);
}
#define REAL_PLUS_32767       ( 32767 << 15 )
#define REAL_MINUS_32768      ( -32768 << 15 )
#define REAL_TO_SHORT(x)      (idiv_signed_rounded(x, 15))
/* This is just here for completeness, it is not used! */
#define REAL_TO_S32(x)        (x)

#ifndef REAL_PLUS_S32
# define REAL_PLUS_S32 2147483647.0
#endif
#ifndef REAL_MINUS_S32
# define REAL_MINUS_S32 -2147483648.0
#endif


/* The actual storage of a decoded sample is separated in the following macros.
   We can handle different types, we could also handle dithering here. */

/* Macro to produce a short (signed 16bit) output sample from internal representation,
   which may be float, double or indeed some integer for fixed point handling. */
#define WRITE_SHORT_SAMPLE(samples,sum,clip) \
  if( (sum) > REAL_PLUS_32767) { *(samples) = 0x7fff; (clip)++; } \
  else if( (sum) < REAL_MINUS_32768) { *(samples) = -0x8000; (clip)++; } \
  else { *(samples) = REAL_TO_SHORT(sum); }

/*
	32bit signed 
	We do clipping with the same old borders... but different conversion.
	We see here that we need extra work for non-16bit output... we optimized for 16bit.
	-0x7fffffff-1 is the minimum 32 bit signed integer value expressed so that MSVC 
	does not give a compile time warning.
*/
#define WRITE_S32_SAMPLE(samples,sum,clip) \
	{ \
		real tmpsum = REAL_MUL((sum),S32_RESCALE); \
		if( tmpsum > REAL_PLUS_S32 ){ *(samples) = 0x7fffffff; (clip)++; } \
		else if( tmpsum < REAL_MINUS_S32 ) { *(samples) = -0x7fffffff-1; (clip)++; } \
		else { *(samples) = REAL_TO_S32(tmpsum); } \
	}

/* Produce an 8bit sample, via 16bit intermediate. */
#define WRITE_8BIT_SAMPLE(samples,sum,clip) \
{ \
	short write_8bit_tmp; \
	if( (sum) > REAL_PLUS_32767) { write_8bit_tmp = 0x7fff; (clip)++; } \
	else if( (sum) < REAL_MINUS_32768) { write_8bit_tmp = -0x8000; (clip)++; } \
	else { write_8bit_tmp = REAL_TO_SHORT(sum); } \
	*(samples) = fr->conv16to8[write_8bit_tmp>>AUSHIFT]; \
}

#endif

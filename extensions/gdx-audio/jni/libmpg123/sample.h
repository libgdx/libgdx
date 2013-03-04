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
#ifdef REAL_IS_FIXED
static inline short idiv_signed_rounded(long x, int shift)
{
	x >>= (shift - 1);
	x += (x & 1);
	return (short)(x >> 1);
}
#  define REAL_PLUS_32767       ( 32767 << 15 )
#  define REAL_MINUS_32768      ( -32768 << 15 )
#  define REAL_TO_SHORT(x)      (idiv_signed_rounded(x, 15))
/* No better code (yet).  */
#  define REAL_TO_SHORT_ACCURATE(x) REAL_TO_SHORT(x)
/* This is just here for completeness, it is not used! */
# define REAL_TO_S32(x)        (x)
#endif

/* From now on for single precision float... double precision is a possible option once we added some bits. But, it would be rather insane. */
#ifndef REAL_TO_SHORT

/* Define the accurate rounding function. */
# if (defined REAL_IS_FLOAT) && (defined IEEE_FLOAT)
/* This function is only available for IEEE754 single-precision values
   This is nearly identical to proper rounding, just -+0.5 is rounded to 0 */
static inline short ftoi16(float x)
{
	union
	{
		float f;
		int32_t i;
	} u_fi;
	u_fi.f = x + 12582912.0f; /* Magic Number: 2^23 + 2^22 */
	return (short)u_fi.i;
}
#  define REAL_TO_SHORT_ACCURATE(x)      ftoi16(x)
# else
/* The "proper" rounding, plain C, a bit slow. */
#  define REAL_TO_SHORT_ACCURATE(x)      (short)((x)>0.0?(x)+0.5:(x)-0.5)
# endif

/* Now define the normal rounding. */
# ifdef ACCURATE_ROUNDING
#  define REAL_TO_SHORT(x)      REAL_TO_SHORT_ACCURATE(x)
# else
/* Non-accurate rounding... simple truncation. Fastest, most LSB errors. */
#  define REAL_TO_SHORT(x)      (short)(x)
# endif

#endif /* REAL_TO_SHORT */

/* We should add dithering for S32, too? */
#ifndef REAL_TO_S32
# ifdef ACCURATE_ROUNDING
#  define REAL_TO_S32(x) (int32_t)((x)>0.0?(x)+0.5:(x)-0.5)
# else
#  define REAL_TO_S32(x) (int32_t)(x)
# endif
#endif

#ifndef REAL_PLUS_32767
# define REAL_PLUS_32767 32767.0
#endif
#ifndef REAL_MINUS_32768
# define REAL_MINUS_32768 -32768.0
#endif
#ifndef REAL_PLUS_S32
# define REAL_PLUS_S32 2147483647.0
#endif
#ifndef REAL_MINUS_S32
# define REAL_MINUS_S32 -2147483648.0
#endif


/* The actual storage of a decoded sample is separated in the following macros.
   We can handle different types, we could also handle dithering here. */

#ifdef NEWOLD_WRITE_SAMPLE

/* This is the old new mpg123 WRITE_SAMPLE, fixed for newer GCC by MPlayer folks.
   Makes a huge difference on old machines. */
#if WORDS_BIGENDIAN 
#define MANTISSA_OFFSET 1
#else
#define MANTISSA_OFFSET 0
#endif
#define WRITE_SHORT_SAMPLE(samples,sum,clip) { \
  union { double dtemp; int itemp[2]; } u; int v; \
  u.dtemp = ((((65536.0 * 65536.0 * 16)+(65536.0 * 0.5))* 65536.0)) + (sum);\
  v = u.itemp[MANTISSA_OFFSET] - 0x80000000; \
  if( v > 32767) { *(samples) = 0x7fff; (clip)++; } \
  else if( v < -32768) { *(samples) = -0x8000; (clip)++; } \
  else { *(samples) = v; }  \
}

#else
/* Macro to produce a short (signed 16bit) output sample from internal representation,
   which may be float, double or indeed some integer for fixed point handling. */
#define WRITE_SHORT_SAMPLE(samples,sum,clip) \
  if( (sum) > REAL_PLUS_32767) { *(samples) = 0x7fff; (clip)++; } \
  else if( (sum) < REAL_MINUS_32768) { *(samples) = -0x8000; (clip)++; } \
  else { *(samples) = REAL_TO_SHORT(sum); }
#endif

/* Same as above, but always using accurate rounding. Would we want softer clipping here, too? */
#define WRITE_SHORT_SAMPLE_ACCURATE(samples,sum,clip) \
  if( (sum) > REAL_PLUS_32767) { *(samples) = 0x7fff; (clip)++; } \
  else if( (sum) < REAL_MINUS_32768) { *(samples) = -0x8000; (clip)++; } \
  else { *(samples) = REAL_TO_SHORT_ACCURATE(sum); }

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
#ifndef REAL_IS_FIXED
#define WRITE_REAL_SAMPLE(samples,sum,clip) *(samples) = ((real)1./SHORT_SCALE)*(sum)
#endif

#endif

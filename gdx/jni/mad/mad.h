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
 * If you would like to negotiate alternate licensing terms, you may do
 * so by contacting: Underbit Technologies, Inc. <info@underbit.com>
 */

# ifdef __cplusplus
extern "C" {
# endif

# define FPM_INTEL



# define SIZEOF_INT 4
# define SIZEOF_LONG 4
# define SIZEOF_LONG_LONG 8


/* Id: version.h,v 1.26 2004/01/23 09:41:33 rob Exp */

# ifndef LIBMAD_VERSION_H
# define LIBMAD_VERSION_H

# define MAD_VERSION_MAJOR	0
# define MAD_VERSION_MINOR	15
# define MAD_VERSION_PATCH	1
# define MAD_VERSION_EXTRA	" (beta)"

# define MAD_VERSION_STRINGIZE(str)	#str
# define MAD_VERSION_STRING(num)	MAD_VERSION_STRINGIZE(num)

# define MAD_VERSION		MAD_VERSION_STRING(MAD_VERSION_MAJOR) "."  \
				MAD_VERSION_STRING(MAD_VERSION_MINOR) "."  \
				MAD_VERSION_STRING(MAD_VERSION_PATCH)  \
				MAD_VERSION_EXTRA

# define MAD_PUBLISHYEAR	"2000-2004"
# define MAD_AUTHOR		"Underbit Technologies, Inc."
# define MAD_EMAIL		"info@underbit.com"

extern char const mad_version[];
extern char const mad_copyright[];
extern char const mad_author[];
extern char const mad_build[];

# endif

/* Id: fixed.h,v 1.38 2004/02/17 02:02:03 rob Exp */

# ifndef LIBMAD_FIXED_H
# define LIBMAD_FIXED_H

# if SIZEOF_INT >= 4
typedef   signed int mad_fixed_t;

typedef   signed int mad_fixed64hi_t;
typedef unsigned int mad_fixed64lo_t;
# else
typedef   signed long mad_fixed_t;

typedef   signed long mad_fixed64hi_t;
typedef unsigned long mad_fixed64lo_t;
# endif

# if defined(_MSC_VER)
#  define mad_fixed64_t  signed __int64
# elif 1 || defined(__GNUC__)
#  define mad_fixed64_t  signed long long
# endif

# if defined(FPM_FLOAT)
typedef double mad_sample_t;
# else
typedef mad_fixed_t mad_sample_t;
# endif

/*
 * Fixed-point format: 0xABBBBBBB
 * A == whole part      (sign + 3 bits)
 * B == fractional part (28 bits)
 *
 * Values are signed two's complement, so the effective range is:
 * 0x80000000 to 0x7fffffff
 *       -8.0 to +7.9999999962747097015380859375
 *
 * The smallest representable value is:
 * 0x00000001 == 0.0000000037252902984619140625 (i.e. about 3.725e-9)
 *
 * 28 bits of fractional accuracy represent about
 * 8.6 digits of decimal accuracy.
 *
 * Fixed-point numbers can be added or subtracted as normal
 * integers, but multiplication requires shifting the 64-bit result
 * from 56 fractional bits back to 28 (and rounding.)
 *
 * Changing the definition of MAD_F_FRACBITS is only partially
 * supported, and must be done with care.
 */

# define MAD_F_FRACBITS		28

# if MAD_F_FRACBITS == 28
#  define MAD_F(x)		((mad_fixed_t) (x##L))
# else
#  if MAD_F_FRACBITS < 28
#   warning "MAD_F_FRACBITS < 28"
#   define MAD_F(x)		((mad_fixed_t)  \
				 (((x##L) +  \
				   (1L << (28 - MAD_F_FRACBITS - 1))) >>  \
				  (28 - MAD_F_FRACBITS)))
#  elif MAD_F_FRACBITS > 28
#   error "MAD_F_FRACBITS > 28 not currently supported"
#   define MAD_F(x)		((mad_fixed_t)  \
				 ((x##L) << (MAD_F_FRACBITS - 28)))
#  endif
# endif

# define MAD_F_MIN		((mad_fixed_t) -0x80000000L)
# define MAD_F_MAX		((mad_fixed_t) +0x7fffffffL)

# define MAD_F_ONE		MAD_F(0x10000000)

# define mad_f_tofixed(x)	((mad_fixed_t)  \
				 ((x) * (double) (1L << MAD_F_FRACBITS) + 0.5))
# define mad_f_todouble(x)	((double)  \
				 ((x) / (double) (1L << MAD_F_FRACBITS)))

# define mad_f_intpart(x)	((x) >> MAD_F_FRACBITS)
# define mad_f_fracpart(x)	((x) & ((1L << MAD_F_FRACBITS) - 1))
				/* (x should be positive) */

# define mad_f_fromint(x)	((x) << MAD_F_FRACBITS)

# define mad_f_add(x, y)	((x) + (y))
# define mad_f_sub(x, y)	((x) - (y))

# if defined(FPM_FLOAT)
#  error "FPM_FLOAT not yet supported"

#  undef MAD_F
#  define MAD_F(x)		mad_f_todouble(x)

#  define mad_f_mul(x, y)	((x) * (y))
#  define mad_f_scale64

#  undef ASO_ZEROCHECK

# elif defined(FPM_64BIT)

/*
 * This version should be the most accurate if 64-bit types are supported by
 * the compiler, although it may not be the most efficient.
 */
#  if defined(OPT_ACCURACY)
#   define mad_f_mul(x, y)  \
    ((mad_fixed_t)  \
     ((((mad_fixed64_t) (x) * (y)) +  \
       (1L << (MAD_F_SCALEBITS - 1))) >> MAD_F_SCALEBITS))
#  else
#   define mad_f_mul(x, y)  \
    ((mad_fixed_t) (((mad_fixed64_t) (x) * (y)) >> MAD_F_SCALEBITS))
#  endif

#  define MAD_F_SCALEBITS  MAD_F_FRACBITS

/* --- Intel --------------------------------------------------------------- */

# elif defined(FPM_INTEL)

#  if defined(_MSC_VER)
#   pragma warning(push)
#   pragma warning(disable: 4035)  /* no return value */
static __forceinline
mad_fixed_t mad_f_mul_inline(mad_fixed_t x, mad_fixed_t y)
{
  enum {
    fracbits = MAD_F_FRACBITS
  };

  __asm {
    mov eax, x
    imul y
    shrd eax, edx, fracbits
  }

  /* implicit return of eax */
}
#   pragma warning(pop)

#   define mad_f_mul		mad_f_mul_inline
#   define mad_f_scale64
#  else
/*
 * This Intel version is fast and accurate; the disposition of the least
 * significant bit depends on OPT_ACCURACY via mad_f_scale64().
 */
#   define MAD_F_MLX(hi, lo, x, y)  \
    asm ("imull %3"  \
	 : "=a" (lo), "=d" (hi)  \
	 : "%a" (x), "rm" (y)  \
	 : "cc")

#   if defined(OPT_ACCURACY)
/*
 * This gives best accuracy but is not very fast.
 */
#    define MAD_F_MLA(hi, lo, x, y)  \
    ({ mad_fixed64hi_t __hi;  \
       mad_fixed64lo_t __lo;  \
       MAD_F_MLX(__hi, __lo, (x), (y));  \
       asm ("addl %2,%0\n\t"  \
	    "adcl %3,%1"  \
	    : "=rm" (lo), "=rm" (hi)  \
	    : "r" (__lo), "r" (__hi), "0" (lo), "1" (hi)  \
	    : "cc");  \
    })
#   endif  /* OPT_ACCURACY */

#   if defined(OPT_ACCURACY)
/*
 * Surprisingly, this is faster than SHRD followed by ADC.
 */
#    define mad_f_scale64(hi, lo)  \
    ({ mad_fixed64hi_t __hi_;  \
       mad_fixed64lo_t __lo_;  \
       mad_fixed_t __result;  \
       asm ("addl %4,%2\n\t"  \
	    "adcl %5,%3"  \
	    : "=rm" (__lo_), "=rm" (__hi_)  \
	    : "0" (lo), "1" (hi),  \
	      "ir" (1L << (MAD_F_SCALEBITS - 1)), "ir" (0)  \
	    : "cc");  \
       asm ("shrdl %3,%2,%1"  \
	    : "=rm" (__result)  \
	    : "0" (__lo_), "r" (__hi_), "I" (MAD_F_SCALEBITS)  \
	    : "cc");  \
       __result;  \
    })
#   elif defined(OPT_INTEL)
/*
 * Alternate Intel scaling that may or may not perform better.
 */
#    define mad_f_scale64(hi, lo)  \
    ({ mad_fixed_t __result;  \
       asm ("shrl %3,%1\n\t"  \
	    "shll %4,%2\n\t"  \
	    "orl %2,%1"  \
	    : "=rm" (__result)  \
	    : "0" (lo), "r" (hi),  \
	      "I" (MAD_F_SCALEBITS), "I" (32 - MAD_F_SCALEBITS)  \
	    : "cc");  \
       __result;  \
    })
#   else
#    define mad_f_scale64(hi, lo)  \
    ({ mad_fixed_t __result;  \
       asm ("shrdl %3,%2,%1"  \
	    : "=rm" (__result)  \
	    : "0" (lo), "r" (hi), "I" (MAD_F_SCALEBITS)  \
	    : "cc");  \
       __result;  \
    })
#   endif  /* OPT_ACCURACY */

#   define MAD_F_SCALEBITS  MAD_F_FRACBITS
#  endif

/* --- ARM ----------------------------------------------------------------- */

# elif defined(FPM_ARM)

/* 
 * This ARM V4 version is as accurate as FPM_64BIT but much faster. The
 * least significant bit is properly rounded at no CPU cycle cost!
 */
# if 1
/*
 * This is faster than the default implementation via MAD_F_MLX() and
 * mad_f_scale64().
 */
#  define mad_f_mul(x, y)  \
    ({ mad_fixed64hi_t __hi;  \
       mad_fixed64lo_t __lo;  \
       mad_fixed_t __result;  \
       asm ("smull	%0, %1, %3, %4\n\t"  \
	    "movs	%0, %0, lsr %5\n\t"  \
	    "adc	%2, %0, %1, lsl %6"  \
	    : "=&r" (__lo), "=&r" (__hi), "=r" (__result)  \
	    : "%r" (x), "r" (y),  \
	      "M" (MAD_F_SCALEBITS), "M" (32 - MAD_F_SCALEBITS)  \
	    : "cc");  \
       __result;  \
    })
# endif

#  define MAD_F_MLX(hi, lo, x, y)  \
    asm ("smull	%0, %1, %2, %3"  \
	 : "=&r" (lo), "=&r" (hi)  \
	 : "%r" (x), "r" (y))

#  define MAD_F_MLA(hi, lo, x, y)  \
    asm ("smlal	%0, %1, %2, %3"  \
	 : "+r" (lo), "+r" (hi)  \
	 : "%r" (x), "r" (y))

#  define MAD_F_MLN(hi, lo)  \
    asm ("rsbs	%0, %2, #0\n\t"  \
	 "rsc	%1, %3, #0"  \
	 : "=r" (lo), "=r" (hi)  \
	 : "0" (lo), "1" (hi)  \
	 : "cc")

#  define mad_f_scale64(hi, lo)  \
    ({ mad_fixed_t __result;  \
       asm ("movs	%0, %1, lsr %3\n\t"  \
	    "adc	%0, %0, %2, lsl %4"  \
	    : "=&r" (__result)  \
	    : "r" (lo), "r" (hi),  \
	      "M" (MAD_F_SCALEBITS), "M" (32 - MAD_F_SCALEBITS)  \
	    : "cc");  \
       __result;  \
    })

#  define MAD_F_SCALEBITS  MAD_F_FRACBITS

/* --- MIPS ---------------------------------------------------------------- */

# elif defined(FPM_MIPS)

/*
 * This MIPS version is fast and accurate; the disposition of the least
 * significant bit depends on OPT_ACCURACY via mad_f_scale64().
 */
#  define MAD_F_MLX(hi, lo, x, y)  \
    asm ("mult	%2,%3"  \
	 : "=l" (lo), "=h" (hi)  \
	 : "%r" (x), "r" (y))

# if defined(HAVE_MADD_ASM)
#  define MAD_F_MLA(hi, lo, x, y)  \
    asm ("madd	%2,%3"  \
	 : "+l" (lo), "+h" (hi)  \
	 : "%r" (x), "r" (y))
# elif defined(HAVE_MADD16_ASM)
/*
 * This loses significant accuracy due to the 16-bit integer limit in the
 * multiply/accumulate instruction.
 */
#  define MAD_F_ML0(hi, lo, x, y)  \
    asm ("mult	%2,%3"  \
	 : "=l" (lo), "=h" (hi)  \
	 : "%r" ((x) >> 12), "r" ((y) >> 16))
#  define MAD_F_MLA(hi, lo, x, y)  \
    asm ("madd16	%2,%3"  \
	 : "+l" (lo), "+h" (hi)  \
	 : "%r" ((x) >> 12), "r" ((y) >> 16))
#  define MAD_F_MLZ(hi, lo)  ((mad_fixed_t) (lo))
# endif

# if defined(OPT_SPEED)
#  define mad_f_scale64(hi, lo)  \
    ((mad_fixed_t) ((hi) << (32 - MAD_F_SCALEBITS)))
#  define MAD_F_SCALEBITS  MAD_F_FRACBITS
# endif

/* --- SPARC --------------------------------------------------------------- */

# elif defined(FPM_SPARC)

/*
 * This SPARC V8 version is fast and accurate; the disposition of the least
 * significant bit depends on OPT_ACCURACY via mad_f_scale64().
 */
#  define MAD_F_MLX(hi, lo, x, y)  \
    asm ("smul %2, %3, %0\n\t"  \
	 "rd %%y, %1"  \
	 : "=r" (lo), "=r" (hi)  \
	 : "%r" (x), "rI" (y))

/* --- PowerPC ------------------------------------------------------------- */

# elif defined(FPM_PPC)

/*
 * This PowerPC version is fast and accurate; the disposition of the least
 * significant bit depends on OPT_ACCURACY via mad_f_scale64().
 */
#  define MAD_F_MLX(hi, lo, x, y)  \
    do {  \
      asm ("mullw %0,%1,%2"  \
	   : "=r" (lo)  \
	   : "%r" (x), "r" (y));  \
      asm ("mulhw %0,%1,%2"  \
	   : "=r" (hi)  \
	   : "%r" (x), "r" (y));  \
    }  \
    while (0)

#  if defined(OPT_ACCURACY)
/*
 * This gives best accuracy but is not very fast.
 */
#   define MAD_F_MLA(hi, lo, x, y)  \
    ({ mad_fixed64hi_t __hi;  \
       mad_fixed64lo_t __lo;  \
       MAD_F_MLX(__hi, __lo, (x), (y));  \
       asm ("addc %0,%2,%3\n\t"  \
	    "adde %1,%4,%5"  \
	    : "=r" (lo), "=r" (hi)  \
	    : "%r" (lo), "r" (__lo),  \
	      "%r" (hi), "r" (__hi)  \
	    : "xer");  \
    })
#  endif

#  if defined(OPT_ACCURACY)
/*
 * This is slower than the truncating version below it.
 */
#   define mad_f_scale64(hi, lo)  \
    ({ mad_fixed_t __result, __round;  \
       asm ("rotrwi %0,%1,%2"  \
	    : "=r" (__result)  \
	    : "r" (lo), "i" (MAD_F_SCALEBITS));  \
       asm ("extrwi %0,%1,1,0"  \
	    : "=r" (__round)  \
	    : "r" (__result));  \
       asm ("insrwi %0,%1,%2,0"  \
	    : "+r" (__result)  \
	    : "r" (hi), "i" (MAD_F_SCALEBITS));  \
       asm ("add %0,%1,%2"  \
	    : "=r" (__result)  \
	    : "%r" (__result), "r" (__round));  \
       __result;  \
    })
#  else
#   define mad_f_scale64(hi, lo)  \
    ({ mad_fixed_t __result;  \
       asm ("rotrwi %0,%1,%2"  \
	    : "=r" (__result)  \
	    : "r" (lo), "i" (MAD_F_SCALEBITS));  \
       asm ("insrwi %0,%1,%2,0"  \
	    : "+r" (__result)  \
	    : "r" (hi), "i" (MAD_F_SCALEBITS));  \
       __result;  \
    })
#  endif

#  define MAD_F_SCALEBITS  MAD_F_FRACBITS

/* --- Default ------------------------------------------------------------- */

# elif defined(FPM_DEFAULT)

/*
 * This version is the most portable but it loses significant accuracy.
 * Furthermore, accuracy is biased against the second argument, so care
 * should be taken when ordering operands.
 *
 * The scale factors are constant as this is not used with SSO.
 *
 * Pre-rounding is required to stay within the limits of compliance.
 */
#  if defined(OPT_SPEED)
#   define mad_f_mul(x, y)	(((x) >> 12) * ((y) >> 16))
#  else
#   define mad_f_mul(x, y)	((((x) + (1L << 11)) >> 12) *  \
				 (((y) + (1L << 15)) >> 16))
#  endif

/* ------------------------------------------------------------------------- */

# else
#  error "no FPM selected"
# endif

/* default implementations */

# if !defined(mad_f_mul)
#  define mad_f_mul(x, y)  \
    ({ register mad_fixed64hi_t __hi;  \
       register mad_fixed64lo_t __lo;  \
       MAD_F_MLX(__hi, __lo, (x), (y));  \
       mad_f_scale64(__hi, __lo);  \
    })
# endif

# if !defined(MAD_F_MLA)
#  define MAD_F_ML0(hi, lo, x, y)	((lo)  = mad_f_mul((x), (y)))
#  define MAD_F_MLA(hi, lo, x, y)	((lo) += mad_f_mul((x), (y)))
#  define MAD_F_MLN(hi, lo)		((lo)  = -(lo))
#  define MAD_F_MLZ(hi, lo)		((void) (hi), (mad_fixed_t) (lo))
# endif

# if !defined(MAD_F_ML0)
#  define MAD_F_ML0(hi, lo, x, y)	MAD_F_MLX((hi), (lo), (x), (y))
# endif

# if !defined(MAD_F_MLN)
#  define MAD_F_MLN(hi, lo)		((hi) = ((lo) = -(lo)) ? ~(hi) : -(hi))
# endif

# if !defined(MAD_F_MLZ)
#  define MAD_F_MLZ(hi, lo)		mad_f_scale64((hi), (lo))
# endif

# if !defined(mad_f_scale64)
#  if defined(OPT_ACCURACY)
#   define mad_f_scale64(hi, lo)  \
    ((((mad_fixed_t)  \
       (((hi) << (32 - (MAD_F_SCALEBITS - 1))) |  \
	((lo) >> (MAD_F_SCALEBITS - 1)))) + 1) >> 1)
#  else
#   define mad_f_scale64(hi, lo)  \
    ((mad_fixed_t)  \
     (((hi) << (32 - MAD_F_SCALEBITS)) |  \
      ((lo) >> MAD_F_SCALEBITS)))
#  endif
#  define MAD_F_SCALEBITS  MAD_F_FRACBITS
# endif

/* C routines */

mad_fixed_t mad_f_abs(mad_fixed_t);
mad_fixed_t mad_f_div(mad_fixed_t, mad_fixed_t);

# endif

/* Id: bit.h,v 1.12 2004/01/23 09:41:32 rob Exp */

# ifndef LIBMAD_BIT_H
# define LIBMAD_BIT_H

struct mad_bitptr {
  unsigned char const *byte;
  unsigned short cache;
  unsigned short left;
};

void mad_bit_init(struct mad_bitptr *, unsigned char const *);

# define mad_bit_finish(bitptr)		/* nothing */

unsigned int mad_bit_length(struct mad_bitptr const *,
			    struct mad_bitptr const *);

# define mad_bit_bitsleft(bitptr)  ((bitptr)->left)
unsigned char const *mad_bit_nextbyte(struct mad_bitptr const *);

void mad_bit_skip(struct mad_bitptr *, unsigned int);
unsigned long mad_bit_read(struct mad_bitptr *, unsigned int);
void mad_bit_write(struct mad_bitptr *, unsigned int, unsigned long);

unsigned short mad_bit_crc(struct mad_bitptr, unsigned int, unsigned short);

# endif

/* Id: timer.h,v 1.16 2004/01/23 09:41:33 rob Exp */

# ifndef LIBMAD_TIMER_H
# define LIBMAD_TIMER_H

typedef struct {
  signed long seconds;		/* whole seconds */
  unsigned long fraction;	/* 1/MAD_TIMER_RESOLUTION seconds */
} mad_timer_t;

extern mad_timer_t const mad_timer_zero;

# define MAD_TIMER_RESOLUTION	352800000UL

enum mad_units {
  MAD_UNITS_HOURS	 =    -2,
  MAD_UNITS_MINUTES	 =    -1,
  MAD_UNITS_SECONDS	 =     0,

  /* metric units */

  MAD_UNITS_DECISECONDS	 =    10,
  MAD_UNITS_CENTISECONDS =   100,
  MAD_UNITS_MILLISECONDS =  1000,

  /* audio sample units */

  MAD_UNITS_8000_HZ	 =  8000,
  MAD_UNITS_11025_HZ	 = 11025,
  MAD_UNITS_12000_HZ	 = 12000,

  MAD_UNITS_16000_HZ	 = 16000,
  MAD_UNITS_22050_HZ	 = 22050,
  MAD_UNITS_24000_HZ	 = 24000,

  MAD_UNITS_32000_HZ	 = 32000,
  MAD_UNITS_44100_HZ	 = 44100,
  MAD_UNITS_48000_HZ	 = 48000,

  /* video frame/field units */

  MAD_UNITS_24_FPS	 =    24,
  MAD_UNITS_25_FPS	 =    25,
  MAD_UNITS_30_FPS	 =    30,
  MAD_UNITS_48_FPS	 =    48,
  MAD_UNITS_50_FPS	 =    50,
  MAD_UNITS_60_FPS	 =    60,

  /* CD audio frames */

  MAD_UNITS_75_FPS	 =    75,

  /* video drop-frame units */

  MAD_UNITS_23_976_FPS	 =   -24,
  MAD_UNITS_24_975_FPS	 =   -25,
  MAD_UNITS_29_97_FPS	 =   -30,
  MAD_UNITS_47_952_FPS	 =   -48,
  MAD_UNITS_49_95_FPS	 =   -50,
  MAD_UNITS_59_94_FPS	 =   -60
};

# define mad_timer_reset(timer)	((void) (*(timer) = mad_timer_zero))

int mad_timer_compare(mad_timer_t, mad_timer_t);

# define mad_timer_sign(timer)	mad_timer_compare((timer), mad_timer_zero)

void mad_timer_negate(mad_timer_t *);
mad_timer_t mad_timer_abs(mad_timer_t);

void mad_timer_set(mad_timer_t *, unsigned long, unsigned long, unsigned long);
void mad_timer_add(mad_timer_t *, mad_timer_t);
void mad_timer_multiply(mad_timer_t *, signed long);

signed long mad_timer_count(mad_timer_t, enum mad_units);
unsigned long mad_timer_fraction(mad_timer_t, unsigned long);
void mad_timer_string(mad_timer_t, char *, char const *,
		      enum mad_units, enum mad_units, unsigned long);

# endif

/* Id: stream.h,v 1.20 2004/02/05 09:02:39 rob Exp */

# ifndef LIBMAD_STREAM_H
# define LIBMAD_STREAM_H


# define MAD_BUFFER_GUARD	8
# define MAD_BUFFER_MDLEN	(511 + 2048 + MAD_BUFFER_GUARD)

enum mad_error {
  MAD_ERROR_NONE	   = 0x0000,	/* no error */

  MAD_ERROR_BUFLEN	   = 0x0001,	/* input buffer too small (or EOF) */
  MAD_ERROR_BUFPTR	   = 0x0002,	/* invalid (null) buffer pointer */

  MAD_ERROR_NOMEM	   = 0x0031,	/* not enough memory */

  MAD_ERROR_LOSTSYNC	   = 0x0101,	/* lost synchronization */
  MAD_ERROR_BADLAYER	   = 0x0102,	/* reserved header layer value */
  MAD_ERROR_BADBITRATE	   = 0x0103,	/* forbidden bitrate value */
  MAD_ERROR_BADSAMPLERATE  = 0x0104,	/* reserved sample frequency value */
  MAD_ERROR_BADEMPHASIS	   = 0x0105,	/* reserved emphasis value */

  MAD_ERROR_BADCRC	   = 0x0201,	/* CRC check failed */
  MAD_ERROR_BADBITALLOC	   = 0x0211,	/* forbidden bit allocation value */
  MAD_ERROR_BADSCALEFACTOR = 0x0221,	/* bad scalefactor index */
  MAD_ERROR_BADMODE        = 0x0222,	/* bad bitrate/mode combination */
  MAD_ERROR_BADFRAMELEN	   = 0x0231,	/* bad frame length */
  MAD_ERROR_BADBIGVALUES   = 0x0232,	/* bad big_values count */
  MAD_ERROR_BADBLOCKTYPE   = 0x0233,	/* reserved block_type */
  MAD_ERROR_BADSCFSI	   = 0x0234,	/* bad scalefactor selection info */
  MAD_ERROR_BADDATAPTR	   = 0x0235,	/* bad main_data_begin pointer */
  MAD_ERROR_BADPART3LEN	   = 0x0236,	/* bad audio data length */
  MAD_ERROR_BADHUFFTABLE   = 0x0237,	/* bad Huffman table select */
  MAD_ERROR_BADHUFFDATA	   = 0x0238,	/* Huffman data overrun */
  MAD_ERROR_BADSTEREO	   = 0x0239	/* incompatible block_type for JS */
};

# define MAD_RECOVERABLE(error)	((error) & 0xff00)

struct mad_stream {
  unsigned char const *buffer;		/* input bitstream buffer */
  unsigned char const *bufend;		/* end of buffer */
  unsigned long skiplen;		/* bytes to skip before next frame */

  int sync;				/* stream sync found */
  unsigned long freerate;		/* free bitrate (fixed) */

  unsigned char const *this_frame;	/* start of current frame */
  unsigned char const *next_frame;	/* start of next frame */
  struct mad_bitptr ptr;		/* current processing bit pointer */

  struct mad_bitptr anc_ptr;		/* ancillary bits pointer */
  unsigned int anc_bitlen;		/* number of ancillary bits */

  unsigned char (*main_data)[MAD_BUFFER_MDLEN];
					/* Layer III main_data() */
  unsigned int md_len;			/* bytes in main_data */

  int options;				/* decoding options (see below) */
  enum mad_error error;			/* error code (see above) */
};

enum {
  MAD_OPTION_IGNORECRC      = 0x0001,	/* ignore CRC errors */
  MAD_OPTION_HALFSAMPLERATE = 0x0002	/* generate PCM at 1/2 sample rate */
# if 0  /* not yet implemented */
  MAD_OPTION_LEFTCHANNEL    = 0x0010,	/* decode left channel only */
  MAD_OPTION_RIGHTCHANNEL   = 0x0020,	/* decode right channel only */
  MAD_OPTION_SINGLECHANNEL  = 0x0030	/* combine channels */
# endif
};

void mad_stream_init(struct mad_stream *);
void mad_stream_finish(struct mad_stream *);

# define mad_stream_options(stream, opts)  \
    ((void) ((stream)->options = (opts)))

void mad_stream_buffer(struct mad_stream *,
		       unsigned char const *, unsigned long);
void mad_stream_skip(struct mad_stream *, unsigned long);

int mad_stream_sync(struct mad_stream *);

char const *mad_stream_errorstr(struct mad_stream const *);

# endif

/* Id: frame.h,v 1.20 2004/01/23 09:41:32 rob Exp */

# ifndef LIBMAD_FRAME_H
# define LIBMAD_FRAME_H


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

/* Id: synth.h,v 1.15 2004/01/23 09:41:33 rob Exp */

# ifndef LIBMAD_SYNTH_H
# define LIBMAD_SYNTH_H


struct mad_pcm {
  unsigned int samplerate;		/* sampling frequency (Hz) */
  unsigned short channels;		/* number of channels */
  unsigned short length;		/* number of samples per channel */
  mad_fixed_t samples[2][1152];		/* PCM output samples [ch][sample] */
};

struct mad_synth {
  mad_fixed_t filter[2][2][2][16][8];	/* polyphase filterbank outputs */
  					/* [ch][eo][peo][s][v] */

  unsigned int phase;			/* current processing phase */

  struct mad_pcm pcm;			/* PCM output */
};

/* single channel PCM selector */
enum {
  MAD_PCM_CHANNEL_SINGLE = 0
};

/* dual channel PCM selector */
enum {
  MAD_PCM_CHANNEL_DUAL_1 = 0,
  MAD_PCM_CHANNEL_DUAL_2 = 1
};

/* stereo PCM selector */
enum {
  MAD_PCM_CHANNEL_STEREO_LEFT  = 0,
  MAD_PCM_CHANNEL_STEREO_RIGHT = 1
};

void mad_synth_init(struct mad_synth *);

# define mad_synth_finish(synth)  /* nothing */

void mad_synth_mute(struct mad_synth *);

void mad_synth_frame(struct mad_synth *, struct mad_frame const *);

# endif

/* Id: decoder.h,v 1.17 2004/01/23 09:41:32 rob Exp */

# ifndef LIBMAD_DECODER_H
# define LIBMAD_DECODER_H


enum mad_decoder_mode {
  MAD_DECODER_MODE_SYNC  = 0,
  MAD_DECODER_MODE_ASYNC
};

enum mad_flow {
  MAD_FLOW_CONTINUE = 0x0000,	/* continue normally */
  MAD_FLOW_STOP     = 0x0010,	/* stop decoding normally */
  MAD_FLOW_BREAK    = 0x0011,	/* stop decoding and signal an error */
  MAD_FLOW_IGNORE   = 0x0020	/* ignore the current frame */
};

struct mad_decoder {
  enum mad_decoder_mode mode;

  int options;

  struct {
    long pid;
    int in;
    int out;
  } async;

  struct {
    struct mad_stream stream;
    struct mad_frame frame;
    struct mad_synth synth;
  } *sync;

  void *cb_data;

  enum mad_flow (*input_func)(void *, struct mad_stream *);
  enum mad_flow (*header_func)(void *, struct mad_header const *);
  enum mad_flow (*filter_func)(void *,
			       struct mad_stream const *, struct mad_frame *);
  enum mad_flow (*output_func)(void *,
			       struct mad_header const *, struct mad_pcm *);
  enum mad_flow (*error_func)(void *, struct mad_stream *, struct mad_frame *);
  enum mad_flow (*message_func)(void *, void *, unsigned int *);
};

void mad_decoder_init(struct mad_decoder *, void *,
		      enum mad_flow (*)(void *, struct mad_stream *),
		      enum mad_flow (*)(void *, struct mad_header const *),
		      enum mad_flow (*)(void *,
					struct mad_stream const *,
					struct mad_frame *),
		      enum mad_flow (*)(void *,
					struct mad_header const *,
					struct mad_pcm *),
		      enum mad_flow (*)(void *,
					struct mad_stream *,
					struct mad_frame *),
		      enum mad_flow (*)(void *, void *, unsigned int *));
int mad_decoder_finish(struct mad_decoder *);

# define mad_decoder_options(decoder, opts)  \
    ((void) ((decoder)->options = (opts)))

int mad_decoder_run(struct mad_decoder *, enum mad_decoder_mode);
int mad_decoder_message(struct mad_decoder *, void *, unsigned int *);

# endif

# ifdef __cplusplus
}
# endif

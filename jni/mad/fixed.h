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
 * $Id: fixed.h,v 1.38 2004/02/17 02:02:03 rob Exp $
 */

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

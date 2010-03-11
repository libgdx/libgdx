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
 * $Id: timer.c,v 1.18 2004/01/23 09:41:33 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# include <stdio.h>

# ifdef HAVE_ASSERT_H
#  include <assert.h>
# endif

# include "timer.h"

mad_timer_t const mad_timer_zero = { 0, 0 };

/*
 * NAME:	timer->compare()
 * DESCRIPTION:	indicate relative order of two timers
 */
int mad_timer_compare(mad_timer_t timer1, mad_timer_t timer2)
{
  signed long diff;

  diff = timer1.seconds - timer2.seconds;
  if (diff < 0)
    return -1;
  else if (diff > 0)
    return +1;

  diff = timer1.fraction - timer2.fraction;
  if (diff < 0)
    return -1;
  else if (diff > 0)
    return +1;

  return 0;
}

/*
 * NAME:	timer->negate()
 * DESCRIPTION:	invert the sign of a timer
 */
void mad_timer_negate(mad_timer_t *timer)
{
  timer->seconds = -timer->seconds;

  if (timer->fraction) {
    timer->seconds -= 1;
    timer->fraction = MAD_TIMER_RESOLUTION - timer->fraction;
  }
}

/*
 * NAME:	timer->abs()
 * DESCRIPTION:	return the absolute value of a timer
 */
mad_timer_t mad_timer_abs(mad_timer_t timer)
{
  if (timer.seconds < 0)
    mad_timer_negate(&timer);

  return timer;
}

/*
 * NAME:	reduce_timer()
 * DESCRIPTION:	carry timer fraction into seconds
 */
static
void reduce_timer(mad_timer_t *timer)
{
  timer->seconds  += timer->fraction / MAD_TIMER_RESOLUTION;
  timer->fraction %= MAD_TIMER_RESOLUTION;
}

/*
 * NAME:	gcd()
 * DESCRIPTION:	compute greatest common denominator
 */
static
unsigned long gcd(unsigned long num1, unsigned long num2)
{
  unsigned long tmp;

  while (num2) {
    tmp  = num2;
    num2 = num1 % num2;
    num1 = tmp;
  }

  return num1;
}

/*
 * NAME:	reduce_rational()
 * DESCRIPTION:	convert rational expression to lowest terms
 */
static
void reduce_rational(unsigned long *numer, unsigned long *denom)
{
  unsigned long factor;

  factor = gcd(*numer, *denom);

  assert(factor != 0);

  *numer /= factor;
  *denom /= factor;
}

/*
 * NAME:	scale_rational()
 * DESCRIPTION:	solve numer/denom == ?/scale avoiding overflowing
 */
static
unsigned long scale_rational(unsigned long numer, unsigned long denom,
			     unsigned long scale)
{
  reduce_rational(&numer, &denom);
  reduce_rational(&scale, &denom);

  assert(denom != 0);

  if (denom < scale)
    return numer * (scale / denom) + numer * (scale % denom) / denom;
  if (denom < numer)
    return scale * (numer / denom) + scale * (numer % denom) / denom;

  return numer * scale / denom;
}

/*
 * NAME:	timer->set()
 * DESCRIPTION:	set timer to specific (positive) value
 */
void mad_timer_set(mad_timer_t *timer, unsigned long seconds,
		   unsigned long numer, unsigned long denom)
{
  timer->seconds = seconds;
  if (numer >= denom && denom > 0) {
    timer->seconds += numer / denom;
    numer %= denom;
  }

  switch (denom) {
  case 0:
  case 1:
    timer->fraction = 0;
    break;

  case MAD_TIMER_RESOLUTION:
    timer->fraction = numer;
    break;

  case 1000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION /  1000);
    break;

  case 8000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION /  8000);
    break;

  case 11025:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 11025);
    break;

  case 12000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 12000);
    break;

  case 16000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 16000);
    break;

  case 22050:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 22050);
    break;

  case 24000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 24000);
    break;

  case 32000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 32000);
    break;

  case 44100:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 44100);
    break;

  case 48000:
    timer->fraction = numer * (MAD_TIMER_RESOLUTION / 48000);
    break;

  default:
    timer->fraction = scale_rational(numer, denom, MAD_TIMER_RESOLUTION);
    break;
  }

  if (timer->fraction >= MAD_TIMER_RESOLUTION)
    reduce_timer(timer);
}

/*
 * NAME:	timer->add()
 * DESCRIPTION:	add one timer to another
 */
void mad_timer_add(mad_timer_t *timer, mad_timer_t incr)
{
  timer->seconds  += incr.seconds;
  timer->fraction += incr.fraction;

  if (timer->fraction >= MAD_TIMER_RESOLUTION)
    reduce_timer(timer);
}

/*
 * NAME:	timer->multiply()
 * DESCRIPTION:	multiply a timer by a scalar value
 */
void mad_timer_multiply(mad_timer_t *timer, signed long scalar)
{
  mad_timer_t addend;
  unsigned long factor;

  factor = scalar;
  if (scalar < 0) {
    factor = -scalar;
    mad_timer_negate(timer);
  }

  addend = *timer;
  *timer = mad_timer_zero;

  while (factor) {
    if (factor & 1)
      mad_timer_add(timer, addend);

    mad_timer_add(&addend, addend);
    factor >>= 1;
  }
}

/*
 * NAME:	timer->count()
 * DESCRIPTION:	return timer value in selected units
 */
signed long mad_timer_count(mad_timer_t timer, enum mad_units units)
{
  switch (units) {
  case MAD_UNITS_HOURS:
    return timer.seconds / 60 / 60;

  case MAD_UNITS_MINUTES:
    return timer.seconds / 60;

  case MAD_UNITS_SECONDS:
    return timer.seconds;

  case MAD_UNITS_DECISECONDS:
  case MAD_UNITS_CENTISECONDS:
  case MAD_UNITS_MILLISECONDS:

  case MAD_UNITS_8000_HZ:
  case MAD_UNITS_11025_HZ:
  case MAD_UNITS_12000_HZ:
  case MAD_UNITS_16000_HZ:
  case MAD_UNITS_22050_HZ:
  case MAD_UNITS_24000_HZ:
  case MAD_UNITS_32000_HZ:
  case MAD_UNITS_44100_HZ:
  case MAD_UNITS_48000_HZ:

  case MAD_UNITS_24_FPS:
  case MAD_UNITS_25_FPS:
  case MAD_UNITS_30_FPS:
  case MAD_UNITS_48_FPS:
  case MAD_UNITS_50_FPS:
  case MAD_UNITS_60_FPS:
  case MAD_UNITS_75_FPS:
    return timer.seconds * (signed long) units +
      (signed long) scale_rational(timer.fraction, MAD_TIMER_RESOLUTION,
				   units);

  case MAD_UNITS_23_976_FPS:
  case MAD_UNITS_24_975_FPS:
  case MAD_UNITS_29_97_FPS:
  case MAD_UNITS_47_952_FPS:
  case MAD_UNITS_49_95_FPS:
  case MAD_UNITS_59_94_FPS:
    return (mad_timer_count(timer, -units) + 1) * 1000 / 1001;
  }

  /* unsupported units */
  return 0;
}

/*
 * NAME:	timer->fraction()
 * DESCRIPTION:	return fractional part of timer in arbitrary terms
 */
unsigned long mad_timer_fraction(mad_timer_t timer, unsigned long denom)
{
  timer = mad_timer_abs(timer);

  switch (denom) {
  case 0:
    return timer.fraction ?
      MAD_TIMER_RESOLUTION / timer.fraction : MAD_TIMER_RESOLUTION + 1;

  case MAD_TIMER_RESOLUTION:
    return timer.fraction;

  default:
    return scale_rational(timer.fraction, MAD_TIMER_RESOLUTION, denom);
  }
}

/*
 * NAME:	timer->string()
 * DESCRIPTION:	write a string representation of a timer using a template
 */
void mad_timer_string(mad_timer_t timer,
		      char *dest, char const *format, enum mad_units units,
		      enum mad_units fracunits, unsigned long subparts)
{
  unsigned long hours, minutes, seconds, sub;
  unsigned int frac;

  timer = mad_timer_abs(timer);

  seconds = timer.seconds;
  frac = sub = 0;

  switch (fracunits) {
  case MAD_UNITS_HOURS:
  case MAD_UNITS_MINUTES:
  case MAD_UNITS_SECONDS:
    break;

  case MAD_UNITS_DECISECONDS:
  case MAD_UNITS_CENTISECONDS:
  case MAD_UNITS_MILLISECONDS:

  case MAD_UNITS_8000_HZ:
  case MAD_UNITS_11025_HZ:
  case MAD_UNITS_12000_HZ:
  case MAD_UNITS_16000_HZ:
  case MAD_UNITS_22050_HZ:
  case MAD_UNITS_24000_HZ:
  case MAD_UNITS_32000_HZ:
  case MAD_UNITS_44100_HZ:
  case MAD_UNITS_48000_HZ:

  case MAD_UNITS_24_FPS:
  case MAD_UNITS_25_FPS:
  case MAD_UNITS_30_FPS:
  case MAD_UNITS_48_FPS:
  case MAD_UNITS_50_FPS:
  case MAD_UNITS_60_FPS:
  case MAD_UNITS_75_FPS:
    {
      unsigned long denom;

      denom = MAD_TIMER_RESOLUTION / fracunits;

      frac = timer.fraction / denom;
      sub  = scale_rational(timer.fraction % denom, denom, subparts);
    }
    break;

  case MAD_UNITS_23_976_FPS:
  case MAD_UNITS_24_975_FPS:
  case MAD_UNITS_29_97_FPS:
  case MAD_UNITS_47_952_FPS:
  case MAD_UNITS_49_95_FPS:
  case MAD_UNITS_59_94_FPS:
    /* drop-frame encoding */
    /* N.B. this is only well-defined for MAD_UNITS_29_97_FPS */
    {
      unsigned long frame, cycle, d, m;

      frame = mad_timer_count(timer, fracunits);

      cycle = -fracunits * 60 * 10 - (10 - 1) * 2;

      d = frame / cycle;
      m = frame % cycle;
      frame += (10 - 1) * 2 * d;
      if (m > 2)
	frame += 2 * ((m - 2) / (cycle / 10));

      frac    = frame % -fracunits;
      seconds = frame / -fracunits;
    }
    break;
  }

  switch (units) {
  case MAD_UNITS_HOURS:
    minutes = seconds / 60;
    hours   = minutes / 60;

    sprintf(dest, format,
	    hours,
	    (unsigned int) (minutes % 60),
	    (unsigned int) (seconds % 60),
	    frac, sub);
    break;

  case MAD_UNITS_MINUTES:
    minutes = seconds / 60;

    sprintf(dest, format,
	    minutes,
	    (unsigned int) (seconds % 60),
	    frac, sub);
    break;

  case MAD_UNITS_SECONDS:
    sprintf(dest, format,
	    seconds,
	    frac, sub);
    break;

  case MAD_UNITS_23_976_FPS:
  case MAD_UNITS_24_975_FPS:
  case MAD_UNITS_29_97_FPS:
  case MAD_UNITS_47_952_FPS:
  case MAD_UNITS_49_95_FPS:
  case MAD_UNITS_59_94_FPS:
    if (fracunits < 0) {
      /* not yet implemented */
      sub = 0;
    }

    /* fall through */

  case MAD_UNITS_DECISECONDS:
  case MAD_UNITS_CENTISECONDS:
  case MAD_UNITS_MILLISECONDS:

  case MAD_UNITS_8000_HZ:
  case MAD_UNITS_11025_HZ:
  case MAD_UNITS_12000_HZ:
  case MAD_UNITS_16000_HZ:
  case MAD_UNITS_22050_HZ:
  case MAD_UNITS_24000_HZ:
  case MAD_UNITS_32000_HZ:
  case MAD_UNITS_44100_HZ:
  case MAD_UNITS_48000_HZ:

  case MAD_UNITS_24_FPS:
  case MAD_UNITS_25_FPS:
  case MAD_UNITS_30_FPS:
  case MAD_UNITS_48_FPS:
  case MAD_UNITS_50_FPS:
  case MAD_UNITS_60_FPS:
  case MAD_UNITS_75_FPS:
    sprintf(dest, format, mad_timer_count(timer, units), sub);
    break;
  }
}

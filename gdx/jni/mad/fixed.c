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
 * $Id: fixed.c,v 1.13 2004/01/23 09:41:32 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# include "fixed.h"

/*
 * NAME:	fixed->abs()
 * DESCRIPTION:	return absolute value of a fixed-point number
 */
mad_fixed_t mad_f_abs(mad_fixed_t x)
{
  return x < 0 ? -x : x;
}

/*
 * NAME:	fixed->div()
 * DESCRIPTION:	perform division using fixed-point math
 */
mad_fixed_t mad_f_div(mad_fixed_t x, mad_fixed_t y)
{
  mad_fixed_t q, r;
  unsigned int bits;

  q = mad_f_abs(x / y);

  if (x < 0) {
    x = -x;
    y = -y;
  }

  r = x % y;

  if (y < 0) {
    x = -x;
    y = -y;
  }

  if (q > mad_f_intpart(MAD_F_MAX) &&
      !(q == -mad_f_intpart(MAD_F_MIN) && r == 0 && (x < 0) != (y < 0)))
    return 0;

  for (bits = MAD_F_FRACBITS; bits && r; --bits) {
    q <<= 1, r <<= 1;
    if (r >= y)
      r -= y, ++q;
  }

  /* round */
  if (2 * r >= y)
    ++q;

  /* fix sign */
  if ((x < 0) != (y < 0))
    q = -q;

  return q << bits;
}

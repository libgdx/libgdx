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
 * $Id: version.c,v 1.15 2004/01/23 09:41:33 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# include "version.h"

char const mad_version[]   = "MPEG Audio Decoder " MAD_VERSION;
char const mad_copyright[] = "Copyright (C) " MAD_PUBLISHYEAR " " MAD_AUTHOR;
char const mad_author[]    = MAD_AUTHOR " <" MAD_EMAIL ">";

char const mad_build[] = ""
# if defined(DEBUG)
  "DEBUG "
# elif defined(NDEBUG)
  "NDEBUG "
# endif

# if defined(EXPERIMENTAL)
  "EXPERIMENTAL "
# endif

# if defined(FPM_64BIT)
  "FPM_64BIT "
# elif defined(FPM_INTEL)
  "FPM_INTEL "
# elif defined(FPM_ARM)
  "FPM_ARM "
# elif defined(FPM_MIPS)
  "FPM_MIPS "
# elif defined(FPM_SPARC)
  "FPM_SPARC "
# elif defined(FPM_PPC)
  "FPM_PPC "
# elif defined(FPM_DEFAULT)
  "FPM_DEFAULT "
# endif

# if defined(ASO_IMDCT)
  "ASO_IMDCT "
# endif
# if defined(ASO_INTERLEAVE1)
  "ASO_INTERLEAVE1 "
# endif
# if defined(ASO_INTERLEAVE2)
  "ASO_INTERLEAVE2 "
# endif
# if defined(ASO_ZEROCHECK)
  "ASO_ZEROCHECK "
# endif

# if defined(OPT_SPEED)
  "OPT_SPEED "
# elif defined(OPT_ACCURACY)
  "OPT_ACCURACY "
# endif

# if defined(OPT_SSO)
  "OPT_SSO "
# endif

# if defined(OPT_DCTO)  /* never defined here */
  "OPT_DCTO "
# endif

# if defined(OPT_STRICT)
  "OPT_STRICT "
# endif
;

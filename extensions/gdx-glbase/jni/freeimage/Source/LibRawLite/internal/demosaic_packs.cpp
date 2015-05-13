/* 
  Copyright 2008-2013 LibRaw LLC (info@libraw.org)

LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

   This file is generated from Dave Coffin's dcraw.c
   dcraw.c -- Dave Coffin's raw photo decoder
   Copyright 1997-2010 by Dave Coffin, dcoffin a cybercom o net

   Look into dcraw homepage (probably http://cybercom.net/~dcoffin/dcraw/)
   for more information
*/

#include <math.h>

#define CLASS LibRaw::

#include "libraw/libraw_types.h"
#define LIBRAW_LIBRARY_BUILD
#define LIBRAW_IO_REDEFINED
#include "libraw/libraw.h"
#include "internal/defines.h"
#define SRC_USES_SHRINK
#define SRC_USES_BLACK
#define SRC_USES_CURVE

/* WF filtering is allowed to triple libraw license */
#include "./wf_filtering.cpp"
/* DHT and AAHD are LGPL licensed, so include them */
#include "./dht_demosaic.cpp"
#include "./aahd_demosaic.cpp"


#include "internal/var_defines.h"


#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
/*AHD-Mod*/
#include <ahd_interpolate_mod.c>
/*LMMSE*/
#include <lmmse_interpolate.c>
/*AFD*/
#include <ahd_partial_interpolate.c>
#include <afd_interpolate_pl.c>
/* VCD*/
#include <refinement.c>
#include <vcd_interpolate.c>
#include <es_median_filter.c>
#include <median_filter_new.c>
#else
// fake implementations of all GPL2 demosaics

void CLASS ahd_interpolate_mod() {ahd_interpolate();}
void CLASS afd_interpolate_pl(int, int) {ahd_interpolate();}
void CLASS vcd_interpolate(int) {ahd_interpolate();}
void CLASS lmmse_interpolate(int) {ahd_interpolate();}
void CLASS es_median_filter() {}
void CLASS median_filter_new() {}
void CLASS refinement() {}

#endif

/* DCB is BSD licensed, so include it */
#include "./dcb_demosaicing.c"


#ifdef LIBRAW_DEMOSAIC_PACK_GPL3
/*AMaZE*/
#include <amaze_demosaic_RT.cc>
#include <CA_correct_RT.cc>
#include <cfa_linedn_new.c>
#include <cfa_impulse_gauss.c>
#include <green_equi.c>
#else
// fallback to AHD and no correction
void CLASS CA_correct_RT(float,float){}
void CLASS amaze_demosaic_RT() { ahd_interpolate();}
void CLASS green_equilibrate(float thresh) {} 
void CLASS cfa_linedn(float linenoise){}
void CLASS cfa_impulse_gauss(float lclean, float cclean){}
#endif

#define sget4(s) sget4((uchar *)s)
#ifndef M_PI
#define M_PI            3.14159265358979323846
#endif
#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
#include <dcraw_foveon.c>
#endif

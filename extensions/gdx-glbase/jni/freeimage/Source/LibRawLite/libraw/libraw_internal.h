/* -*- C++ -*-
 * File: libraw_internal.h
 * Copyright 2008-2013 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  8 , 2008
 *
 * LibRaw internal data structures (not visible outside)

LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

 */

#ifndef _LIBRAW_INTERNAL_TYPES_H
#define _LIBRAW_INTERNAL_TYPES_H

#include <stdio.h>
#ifdef __cplusplus

#ifdef LIBRAW_LIBRARY_BUILD
#ifndef CLASS
#define CLASS LibRaw::
#endif
#endif

#else
#ifndef CLASS
#define CLASS
#endif
#endif


#ifdef __cplusplus

#include "libraw_datastream.h"
#include "libraw_types.h"

class LibRaw_TLS
{
public:
    struct
    {
         unsigned bitbuf;
         int vbits, reset;
    }getbits;
    struct 
    {
         UINT64 bitbuf;
         int vbits;

    }ph1_bits;
    struct
    {
         unsigned pad[128], p;
    }sony_decrypt;
    struct
    {
        uchar buf[0x4000];
        int vbits, padding;
    }pana_bits;
    uchar jpeg_buffer[4096];
    struct 
    {
      float cbrt[0x10000], xyz_cam[3][4];
    }ahd_data;
    void init() 
        { 
            getbits.bitbuf = 0; getbits.vbits = getbits.reset = 0;
            ph1_bits.bitbuf = 0; ph1_bits.vbits = 0;
            pana_bits.vbits = 0;
            ahd_data.cbrt[0]=-2.0f;
        }
};


class LibRaw_constants
{
  public:
    static const float d65_white[3];
    static const double xyz_rgb[3][3];
};
#endif /* __cplusplus */


typedef struct
{
#ifndef __cplusplus
    struct
#endif
    LibRaw_abstract_datastream *input;
    FILE        *output;
    int         input_internal;
    char        *meta_data;
    INT64       profile_offset;
    INT64       toffset;

} internal_data_t;


#define LIBRAW_HISTOGRAM_SIZE 0x2000
typedef struct
{
    int         (*histogram)[LIBRAW_HISTOGRAM_SIZE];
    unsigned    *oprof;
} output_data_t;

typedef struct
{
    unsigned olympus_exif_cfa;
    unsigned     unique_id;
    unsigned tiff_nifds;
    int  tiff_flip;
}identify_data_t;

typedef struct
{
    short       order; 
    ushort      sraw_mul[4],cr2_slice[3];
    unsigned    kodak_cbpp;
    INT64       strip_offset, data_offset;
    INT64       meta_offset;
    unsigned    data_size;
    unsigned     meta_length;
    unsigned    thumb_misc;
    unsigned    fuji_layout;
    unsigned    tiff_samples;
    unsigned    tiff_bps;
    unsigned    tiff_compress;
    unsigned    zero_after_ff;
    unsigned    tile_width, tile_length,load_flags;
    unsigned    data_error;
}unpacker_data_t;



typedef struct
{
    internal_data_t internal_data;
    libraw_internal_output_params_t internal_output_params;    
    output_data_t output_data;
    identify_data_t identify_data;
    unpacker_data_t unpacker_data;
} libraw_internal_data_t;


struct decode 
{
    struct decode *branch[2];
    int leaf;
};

struct tiff_ifd_t 
{
    int t_width, t_height, bps, comp, phint, offset, t_flip, samples, bytes,tile_maxbytes;
    int t_tile_width, t_tile_length;
};


struct jhead {
  int bits, high, wide, clrs, sraw, psv, restart, vpred[6];
    ushort *huff[6], *free[4], *row;
};
struct tiff_tag {
  ushort tag, type;
  int count;
  union { char c[4]; short s[2]; int i; } val;
};

struct tiff_hdr {
  ushort t_order, magic;
  int ifd;
  ushort pad, ntag;
  struct tiff_tag tag[23];
  int nextifd;
  ushort pad2, nexif;
  struct tiff_tag exif[4];
  ushort pad3, ngps;
  struct tiff_tag gpst[10];
  short bps[4];
  int rat[10];
  unsigned gps[26];
  char t_desc[512], t_make[64], t_model[64], soft[32], date[20], t_artist[64];
};



#ifdef DEBUG_STAGE_CHECKS
#define CHECK_ORDER_HIGH(expected_stage) \
    do { if((imgdata.progress_flags & LIBRAW_PROGRESS_THUMB_MASK) >= expected_stage) {fprintf(stderr,"CHECK_HIGH: check %d >=  %d\n",imgdata.progress_flags & LIBRAW_PROGRESS_THUMB_MASK,expected_stage);return LIBRAW_OUT_OF_ORDER_CALL;} } while(0)

#define CHECK_ORDER_LOW(expected_stage) \
    do { printf("Checking LOW %d/%d : %d\n",imgdata.progress_flags,expected_stage,imgdata.progress_flags<expected_stage); if( (imgdata.progress_flags&LIBRAW_PROGRESS_THUMB_MASK) < expected_stage ) { printf("failed!\n"); return LIBRAW_OUT_OF_ORDER_CALL;} } while(0)
#define CHECK_ORDER_BIT(expected_stage) \
    do { if(imgdata.progress_flags & expected_stage) return LIBRAW_OUT_OF_ORDER_CALL; } while(0)

#define SET_PROC_FLAG(stage) do {imgdata.progress_flags |= stage; fprintf(stderr,"SET_FLAG: %d\n",stage); } while (0)

#else

#define CHECK_ORDER_HIGH(expected_stage) \
    do { if((imgdata.progress_flags & LIBRAW_PROGRESS_THUMB_MASK) >= expected_stage) \
            {return LIBRAW_OUT_OF_ORDER_CALL;} } while(0)

#define CHECK_ORDER_LOW(expected_stage) \
    do { if((imgdata.progress_flags&LIBRAW_PROGRESS_THUMB_MASK) < expected_stage) \
            return LIBRAW_OUT_OF_ORDER_CALL; } while(0)

#define CHECK_ORDER_BIT(expected_stage) \
    do { if(imgdata.progress_flags & expected_stage) return LIBRAW_OUT_OF_ORDER_CALL; } while(0)

#define SET_PROC_FLAG(stage) do {imgdata.progress_flags |= stage;} while (0)

#endif

#endif

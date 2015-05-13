/* -*- C++ -*-
 * File: libraw_internal_funcs.h
 * Copyright 2008-2013 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  14, 2008

LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

 */

#ifndef _LIBRAW_INTERNAL_FUNCS_H
#define _LIBRAW_INTERNAL_FUNCS_H

#ifndef LIBRAW_LIBRARY_BUILD
#error "This file should be used only for libraw library build"
#else
    /* WF */
    void        wf_bayer4_igauss_filter(int radius,void* src_image, int src_imgmode, void* dst_image, int dst_imgmode);
    void	wf_bayer4_green_blur   (int mode,void* src_image, int src_imgmode, void* dst_image, int dst_imgmode);
    void        wf_bayer4_block_filter (int* radius_list, void* src_image, int src_imgmode, void* dst_image, int dst_imgmode);
    double	wf_filter_energy       (int r1_greenmode, int r1, int r2_greenmode, int r2);


// inline functions
    ushort      sget2 (uchar *s);
    ushort      get2();
    unsigned    sget4 (uchar *s);
    unsigned    getint (int type);
    float       int_to_float (int i);
    double      getreal (int type);
    void        read_shorts (ushort *pixel, int count);

// Canon P&S cameras
    void        canon_600_fixed_wb (int temp);
    int         canon_600_color (int ratio[2], int mar);
    void        canon_600_auto_wb();
    void        canon_600_coeff();
    void        canon_600_load_raw();
    void        canon_600_correct();
    int         canon_s2is();
void        parse_ciff (int offset, int length, int);
    void        ciff_block_1030();

// LJPEG decoder
    unsigned    getbithuff (int nbits, ushort *huff);
    ushort*     make_decoder_ref (const uchar **source);
    ushort*     make_decoder (const uchar *source);
    int         ljpeg_start (struct jhead *jh, int info_only);
    void        ljpeg_end(struct jhead *jh);
    int         ljpeg_diff (ushort *huff); 
    ushort *    ljpeg_row (int jrow, struct jhead *jh);
    unsigned    ph1_bithuff (int nbits, ushort *huff);

// Canon DSLRs
void        crw_init_tables (unsigned table, ushort *huff[2]);
    int         canon_has_lowbits();
    void        canon_load_raw();
    void        lossless_jpeg_load_raw();
    void        canon_sraw_load_raw();
// Adobe DNG
    void        adobe_copy_pixel (unsigned int row, unsigned int col, ushort **rp);
    void        lossless_dng_load_raw();
    void        packed_dng_load_raw();
    void        lossy_dng_load_raw();
//void        adobe_dng_load_raw_nc();

// Pentax
    void        pentax_load_raw();
    void        pentax_tree();

// Nikon (and Minolta Z2)
    void        nikon_load_raw();
//void        nikon_load_raw();
    int         nikon_e995();
    int         nikon_e2100();
    void        nikon_3700();
    int         minolta_z2();
    void        nikon_e2100_load_raw();

// Fuji
//void        fuji_load_raw();
    void        parse_fuji (int offset);

// RedCine
    void        parse_redcine();
    void        redcine_load_raw();

// Rollei
    void        rollei_load_raw();
    void        parse_rollei();

// MF backs
//int         bayer (unsigned row, unsigned col);
    int         raw(unsigned,unsigned);
    void        phase_one_flat_field (int is_float, int nc);
    void        phase_one_load_raw();
    unsigned    ph1_bits (int nbits);
    void        phase_one_load_raw_c();
    void        hasselblad_load_raw();
    void        leaf_hdr_load_raw();
    void        sinar_4shot_load_raw();
    void        imacon_full_load_raw();
    void        hasselblad_full_load_raw();
    void        packed_load_raw();
    float	find_green(int,int,int,int);
    void        unpacked_load_raw();
    void        parse_sinar_ia();
    void        parse_phase_one (int base);

// Misc P&S cameras
    void        nokia_load_raw();
    void        canon_rmf_load_raw();
    unsigned    pana_bits (int nbits);
    void        panasonic_load_raw();
    void        olympus_load_raw();
    void        olympus_cseries_load_raw();
    void        minolta_rd175_load_raw();
    void        quicktake_100_load_raw();
    const int*  make_decoder_int (const int *source, int level);
    int         radc_token (int tree);
    void        kodak_radc_load_raw();
    void        kodak_jpeg_load_raw();
    void        kodak_dc120_load_raw();
    void        eight_bit_load_raw();
    void        smal_decode_segment (unsigned seg[2][2], int holes);
    void        smal_v6_load_raw();
    int         median4 (int *p);
    void        fill_holes (int holes);
    void        smal_v9_load_raw();
    void        parse_riff();
    void        parse_cine();
    void        parse_smal (int offset, int fsize);
    int         parse_jpeg (int offset);

// Kodak
    void        kodak_262_load_raw();
    int         kodak_65000_decode (short *out, int bsize);
    void        kodak_65000_load_raw();
    void        kodak_rgb_load_raw();
    void        kodak_yrgb_load_raw();
    void        kodak_ycbcr_load_raw();
    void        kodak_rgb_load_thumb();
    void        kodak_ycbcr_load_thumb();

// It's a Sony (and K&M)
    void        sony_decrypt (unsigned *data, int len, int start, int key);
    void        sony_load_raw();
    void        sony_arw_load_raw();
    void        sony_arw2_load_raw();
    void        samsung_load_raw();
    void        parse_minolta (int base);

// Foveon/Sigma
#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
    void        foveon_sd_load_raw();
    void        foveon_dp_load_raw();
    void        foveon_huff (ushort *huff);
    void        foveon_load_camf();

    const char* foveon_camf_param (const char *block, const char *param);
    void *      foveon_camf_matrix (unsigned dim[3], const char *name);
    int         foveon_fixed (void *ptr, int size, const char *name);
    float       foveon_avg (short *pix, int range[2], float cfilt);
    short *     foveon_make_curve (double max, double mul, double filt);
    void        foveon_make_curves(short **curvep, float dq[3], float div[3], float filt);
    int         foveon_apply_curve (short *curve, int i);
    void        foveon_interpolate();
    char *      foveon_gets (int offset, char *str, int len);
    void        parse_foveon();
#endif
// We always have x3f code compiled in!
    void        parse_x3f();
    void        x3f_load_raw();

// CAM/RGB
    void        pseudoinverse (double (*in)[3], double (*out)[3], int size);
    void        cam_xyz_coeff (double cam_xyz[4][3]);
    void        adobe_coeff (const char *, const char *);
    void        simple_coeff (int index);


// Tiff/Exif parsers
    void        tiff_get (unsigned base,unsigned *tag, unsigned *type, unsigned *len, unsigned *save);
    void        parse_thumb_note (int base, unsigned toff, unsigned tlen);
    void        parse_makernote (int base, int uptag);
    void        parse_exif (int base);
    void        linear_table (unsigned len);
    void        parse_kodak_ifd (int base);
    int         parse_tiff_ifd (int base);
    int         parse_tiff (int base);
    void        apply_tiff(void);
    void        parse_gps (int base);
    void        romm_coeff (float romm_cam[3][3]);
    void        parse_mos (int offset);
    void        get_timestamp (int reversed);

// External JPEGs, what cameras uses it ?
    void        parse_external_jpeg();

// The identify
    short       guess_byte_order (int words);

// Tiff writer
    void        tiff_set (ushort *ntag, ushort tag, ushort type, int count, int val);
    void        tiff_head (struct tiff_hdr *th, int full);

// splitted AHD code
#define TS 512
    void        ahd_interpolate_green_h_and_v(int top, int left, ushort (*out_rgb)[TS][TS][3]);
    void ahd_interpolate_r_and_b_in_rgb_and_convert_to_cielab(int top, int left, ushort (*inout_rgb)[TS][3], short (*out_lab)[TS][3]);
    void ahd_interpolate_r_and_b_and_convert_to_cielab(int top, int left, ushort (*inout_rgb)[TS][TS][3], short (*out_lab)[TS][TS][3]);
    void ahd_interpolate_build_homogeneity_map(int top, int left, short (*lab)[TS][TS][3], char (*out_homogeneity_map)[TS][2]);
    void ahd_interpolate_combine_homogeneous_pixels(int top, int left, ushort (*rgb)[TS][TS][3], char (*homogeneity_map)[TS][2]);

#undef TS

// LibRaw demosaic packs  functions
// AMaZe
    int         LinEqSolve(int,  float*, float*, float*);
// DCB
    void        dcb_pp();
    void        dcb_copy_to_buffer(float (*image2)[3]);
    void        dcb_restore_from_buffer(float (*image2)[3]);
    void        dcb_color();
    void        dcb_color_full();
    void        dcb_map();
    void        dcb_correction();
    void        dcb_correction2();
    void        dcb_refinement();
    void        rgb_to_lch(double (*image3)[3]);
    void        lch_to_rgb(double (*image3)[3]);
    void        fbdd_correction();
    void        fbdd_correction2(double (*image3)[3]);
    void        fbdd_green();
    void  	dcb_ver(float (*image3)[3]);
    void 	dcb_hor(float (*image2)[3]);
    void 	dcb_color2(float (*image2)[3]);
    void 	dcb_color3(float (*image3)[3]);
    void 	dcb_decide(float (*image2)[3], float (*image3)[3]);
    void 	dcb_nyquist();
// VCD/modified dcraw
    void        refinement();
    void        ahd_partial_interpolate(int threshold_value);
    void        es_median_filter();
    void        median_filter_new();
#endif

#endif

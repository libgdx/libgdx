/* -*- C++ -*-
 * File: libraw.h
 * Copyright 2008-2013 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  8, 2008 
 *
 * LibRaw C++ interface
 *

LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

*/

#ifndef _LIBRAW_CLASS_H
#define _LIBRAW_CLASS_H

#ifdef __linux__
#define _FILE_OFFSET_BITS 64
#endif

/* maximum file size to use LibRaw_file_datastream (fully buffered) I/O */
#define LIBRAW_USE_STREAMS_DATASTREAM_MAXSIZE (250*1024L*1024L)


#include <limits.h>
#include <memory.h>
#include <stdio.h>
#include <stdlib.h>


#include "libraw_datastream.h"
#include "libraw_types.h"
#include "libraw_const.h"
#include "libraw_internal.h"
#include "libraw_alloc.h"

#ifdef __cplusplus
extern "C" 
{
#endif
DllDef    const char          *libraw_strerror(int errorcode);
DllDef    const char          *libraw_strprogress(enum LibRaw_progress);
    /* LibRaw C API */
DllDef    libraw_data_t       *libraw_init(unsigned int flags);
DllDef    int                 libraw_open_file(libraw_data_t*, const char *);
DllDef    int                 libraw_open_file_ex(libraw_data_t*, const char *, INT64 max_buff_sz);
#if defined(_WIN32) && !defined(__MINGW32__) && defined(_MSC_VER) && (_MSC_VER > 1310)
DllDef    int                 libraw_open_wfile(libraw_data_t*, const wchar_t *);
DllDef    int                 libraw_open_wfile_ex(libraw_data_t*, const wchar_t *, INT64 max_buff_sz);
#endif
DllDef    int                 libraw_open_buffer(libraw_data_t*, void * buffer, size_t size);
DllDef    int                 libraw_unpack(libraw_data_t*);
DllDef    int                 libraw_unpack_thumb(libraw_data_t*);
DllDef    void                libraw_recycle_datastream(libraw_data_t*);
DllDef    void                libraw_recycle(libraw_data_t*);
DllDef    void                libraw_close(libraw_data_t*);
DllDef    void                libraw_subtract_black(libraw_data_t*);
DllDef    int                 libraw_raw2image(libraw_data_t*);
DllDef    void                libraw_free_image(libraw_data_t*);
    /* version helpers */
DllDef    const char*         libraw_version();
DllDef    int                 libraw_versionNumber();
    /* Camera list */
DllDef    const char**        libraw_cameraList();
DllDef    int                 libraw_cameraCount();

  /* helpers */
DllDef    void                libraw_set_memerror_handler(libraw_data_t*, memory_callback cb, void *datap);
DllDef    void                libraw_set_dataerror_handler(libraw_data_t*,data_callback func,void *datap);
DllDef    void                libraw_set_progress_handler(libraw_data_t*,progress_callback cb,void *datap);
DllDef    const char *        libraw_unpack_function_name(libraw_data_t* lr);
DllDef    int                 libraw_get_decoder_info(libraw_data_t* lr,libraw_decoder_info_t* d);
DllDef    int libraw_COLOR(libraw_data_t*,int row, int col);

    /* DCRAW compatibility */
DllDef    int                 libraw_adjust_sizes_info_only(libraw_data_t*);
DllDef    int                 libraw_dcraw_ppm_tiff_writer(libraw_data_t* lr,const char *filename);
DllDef    int                 libraw_dcraw_thumb_writer(libraw_data_t* lr,const char *fname);
DllDef    int                 libraw_dcraw_process(libraw_data_t* lr);
DllDef    libraw_processed_image_t* libraw_dcraw_make_mem_image(libraw_data_t* lr, int *errc);
DllDef    libraw_processed_image_t* libraw_dcraw_make_mem_thumb(libraw_data_t* lr, int *errc);
DllDef    void libraw_dcraw_clear_mem(libraw_processed_image_t*);
#ifdef __cplusplus
}
#endif


#ifdef __cplusplus

class DllDef LibRaw
{
  public:
    libraw_data_t imgdata;
    int verbose;

    LibRaw(unsigned int flags = LIBRAW_OPTIONS_NONE);
    libraw_output_params_t*     output_params_ptr() { return &imgdata.params;}
    int                         open_file(const char *fname, INT64 max_buffered_sz=LIBRAW_USE_STREAMS_DATASTREAM_MAXSIZE);
#if defined(_WIN32) && !defined(__MINGW32__) && defined(_MSC_VER) && (_MSC_VER > 1310)
	int                         open_file(const wchar_t *fname, INT64 max_buffered_sz=LIBRAW_USE_STREAMS_DATASTREAM_MAXSIZE);
#endif
    int                         open_buffer(void *buffer, size_t size);
    int                         open_datastream(LibRaw_abstract_datastream *);
	void							recycle_datastream();
    int                         unpack(void);
    int                         unpack_thumb(void);

    int                         adjust_sizes_info_only(void);
    int                         subtract_black();
    int                         subtract_black_internal();
    int                         raw2image();
    int                         raw2image_ex(int do_subtract_black);
    void                        raw2image_start();
    void                        free_image();
    int                         adjust_maximum();
    void                        set_memerror_handler( memory_callback cb,void *data) {callbacks.memcb_data = data; callbacks.mem_cb = cb; }
    void                        set_dataerror_handler(data_callback func, void *data) { callbacks.datacb_data = data; callbacks.data_cb = func;}
    void                        set_progress_handler(progress_callback pcb, void *data) { callbacks.progresscb_data = data; callbacks.progress_cb = pcb;}

    /* helpers */
    static const char*          version();
    static int                  versionNumber();
    static const char**         cameraList();
    static int                  cameraCount();
    static const char*          strprogress(enum LibRaw_progress);
    static const char*          strerror(int p);
    /* dcraw emulation */
    int                         dcraw_ppm_tiff_writer(const char *filename);
    int                         dcraw_thumb_writer(const char *fname);
    int                         dcraw_process(void);
    /* information calls */
    int is_fuji_rotated(){return libraw_internal_data.internal_output_params.fuji_width;}
    int is_sraw();
    /* memory writers */
    virtual libraw_processed_image_t*   dcraw_make_mem_image(int *errcode=NULL);  
    virtual libraw_processed_image_t*   dcraw_make_mem_thumb(int *errcode=NULL);
    static void                 dcraw_clear_mem(libraw_processed_image_t*);
    
    /* Additional calls for make_mem_image */
    void get_mem_image_format(int* width, int* height, int* colors, int* bps) const;
    int  copy_mem_image(void* scan0, int stride, int bgr);

    /* free all internal data structures */
    void         recycle(); 
    virtual ~LibRaw(void); 

    int COLOR(int row, int col) { return libraw_internal_data.internal_output_params.fuji_width? FCF(row,col):FC(row,col);}
 
    int FC(int row,int col) { return (imgdata.idata.filters >> (((row << 1 & 14) | (col & 1)) << 1) & 3);}
    int         fcol (int row, int col);
    
    const char *unpack_function_name();
    int get_decoder_info(libraw_decoder_info_t* d_info);
    libraw_internal_data_t * get_internal_data_pointer(){ return &libraw_internal_data; }

    /* Debanding filter */
    int                         wf_remove_banding();

  /* Phase one correction/subtractBL calls */
  void phase_one_subtract_black(ushort *src, ushort *dest);
  void        phase_one_correct();
  int set_rawspeed_camerafile(char *filename);
  void setCancelFlag();

protected:
    void checkCancel();
    void phase_one_allocate_tempbuffer();
    void phase_one_free_tempbuffer();
    virtual int  is_phaseone_compressed();
    /* Hotspots */
    virtual void copy_fuji_uncropped(unsigned short cblack[4], unsigned short *dmaxp);
    virtual void copy_bayer(unsigned short cblack[4], unsigned short *dmaxp);
    virtual void fuji_rotate();
    virtual void convert_to_rgb_loop(float out_cam[3][4]);
    virtual void lin_interpolate_loop(int code[16][16][32],int size);
    virtual void scale_colors_loop(float scale_mul[4]);

    int FCF(int row,int col) { 
        int rr,cc;
        if (libraw_internal_data.unpacker_data.fuji_layout) {
            rr = libraw_internal_data.internal_output_params.fuji_width - 1 - col + (row >> 1);
            cc = col + ((row+1) >> 1);
        } else {
            rr = libraw_internal_data.internal_output_params.fuji_width - 1 + row - (col >> 1);
            cc = row + ((col+1) >> 1);
        }
        return FC(rr,cc);
    }

    void adjust_bl();
    void*        malloc(size_t t);
    void*        calloc(size_t n,size_t t);
    void*        realloc(void *p, size_t s);
    void        free(void *p);
    void        merror (void *ptr, const char *where);
    void        derror();

    LibRaw_TLS  *tls;
    libraw_internal_data_t libraw_internal_data;
    decode      first_decode[2048], *second_decode, *free_decode;
    tiff_ifd_t  tiff_ifd[10];
    libraw_memmgr memmgr;
    libraw_callbacks_t callbacks;

    LibRaw_constants rgb_constants;

    void        (LibRaw:: *write_thumb)();
    void        (LibRaw:: *write_fun)();
    void        (LibRaw:: *load_raw)();
    void        (LibRaw:: *thumb_load_raw)();

    void        kodak_thumb_loader();
    void        write_thumb_ppm_tiff(FILE *); 
    void        x3f_thumb_loader();
#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
    void        foveon_thumb_loader (void);
#endif
    
    int         own_filtering_supported(){ return 0;}
    void        identify();
    void        write_ppm_tiff ();
    void        convert_to_rgb();
    void        remove_zeroes();
    void        crop_masked_pixels();
#ifndef NO_LCMS
    void	apply_profile(const char*,const char*);
#endif
    void        pre_interpolate();
    void        border_interpolate (int border);
    void        lin_interpolate();
    void        vng_interpolate();
    void        ppg_interpolate();
    void        cielab(ushort rgb[3], short lab[3]);
    void        xtrans_interpolate(int);
    void        ahd_interpolate();
    void        dht_interpolate();
    void        aahd_interpolate();

    /* from demosaic pack */
    void        ahd_interpolate_mod();
    void        afd_interpolate_pl(int afd_passes, int clip_on);
    void        afd_noise_filter_pl();
    void	lmmse_interpolate(int gamma_apply);
    void        dcb(int iterations, int dcb_enhance);
    void        fbdd(int noiserd);
    void        vcd_interpolate(int ahd_cutoff);
    void        amaze_demosaic_RT();
    void	exp_bef(float expos, float preser);
    void        CA_correct_RT(float cared, float cablue);
    void        cfa_linedn(float linenoise);
    void        cfa_impulse_gauss(float lclean, float cclean);
    void        green_equilibrate(float thresh);
	
    /* demosaic pack end */

    void        bad_pixels(const char*);
    void        subtract(const char*);
    void        hat_transform (float *temp, float *base, int st, int size, int sc);
    void        wavelet_denoise();
    void        scale_colors();
    void        median_filter ();
    void        blend_highlights();
    void        recover_highlights();
    void        green_matching();

    void        stretch();

#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
    void        foveon_thumb ();
#endif
    void        jpeg_thumb_writer (FILE *tfp,char *thumb,int thumb_length);
    void        jpeg_thumb ();
    void        ppm_thumb ();
    void        ppm16_thumb();
    void        layer_thumb ();
    void        rollei_thumb ();
    void        kodak_thumb_load_raw();

#ifdef LIBRAW_DEMOSAIC_PACK_GPL2
    void        foveon_decoder (unsigned size, unsigned code);
#endif
    unsigned    get4();

    int         flip_index (int row, int col);
    void        gamma_curve (double pwr, double ts, int mode, int imax);

  /* RawSpeed data */
  void		*_rawspeed_camerameta;
  void          *_rawspeed_decoder;
  void		fix_after_rawspeed(int bl);
  /* Fast cancel flag */
  long          _exitflag;

  /* X3F data */
  void          *_x3f_data;

#ifdef LIBRAW_LIBRARY_BUILD 
#include "internal/libraw_internal_funcs.h"
#endif

};

#ifdef LIBRAW_LIBRARY_BUILD 
#define RUN_CALLBACK(stage,iter,expect)  if(callbacks.progress_cb) { \
        int rr = (*callbacks.progress_cb)(callbacks.progresscb_data,stage,iter,expect); \
        if(rr!=0) throw LIBRAW_EXCEPTION_CANCELLED_BY_CALLBACK; \
    }
#endif


#endif /* __cplusplus */


#endif /* _LIBRAW_CLASS_H */

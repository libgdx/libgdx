/* -*- C++ -*-
 * File: libraw_c_api.cpp
 * Copyright 2008-2013 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  8 , 2008
 *
 * LibRaw C interface 


LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).
 */

#include <math.h>
#include <errno.h>
#include "libraw/libraw.h"

#ifdef __cplusplus
#include <new>
extern "C" 
{
#endif

    libraw_data_t *libraw_init(unsigned int flags)
    {
        LibRaw *ret;
        try {
            ret = new LibRaw(flags);
        }
        catch (std::bad_alloc)
            {
                return NULL;
            }
        return &(ret->imgdata);
    }

    const char*   libraw_version() { return LibRaw::version();}
    const char*   libraw_strprogress(enum LibRaw_progress p) { return LibRaw::strprogress(p);}
    int     libraw_versionNumber() { return LibRaw::versionNumber();}
    const char**  libraw_cameraList() { return LibRaw::cameraList();}
    int   libraw_cameraCount() { return LibRaw::cameraCount(); }
    const char* libraw_unpack_function_name(libraw_data_t* lr)
    {
        if(!lr) return "NULL parameter passed";
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->unpack_function_name();
    }

    void libraw_subtract_black(libraw_data_t* lr)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->subtract_black();
    }


    int libraw_open_file(libraw_data_t* lr, const char *file)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->open_file(file);
    }

    int libraw_open_file_ex(libraw_data_t* lr, const char *file,INT64 sz)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->open_file(file,sz);
    }
#if defined(_WIN32) && !defined(__MINGW32__) && defined(_MSC_VER) && (_MSC_VER > 1310)
    int libraw_open_wfile(libraw_data_t* lr, const wchar_t *file)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->open_file(file);
    }

    int libraw_open_wfile_ex(libraw_data_t* lr, const wchar_t *file,INT64 sz)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->open_file(file,sz);
    }
#endif
    int libraw_open_buffer(libraw_data_t* lr, void *buffer, size_t size)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->open_buffer(buffer,size);
    }
    int libraw_unpack(libraw_data_t* lr)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->unpack();
    }
    int libraw_unpack_thumb(libraw_data_t* lr)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->unpack_thumb();
    }
	void libraw_recycle_datastream(libraw_data_t* lr)
	{
		if(!lr) return;
		LibRaw *ip = (LibRaw*) lr->parent_class;
		ip->recycle_datastream();
	}
    void libraw_recycle(libraw_data_t* lr)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->recycle();
    }
    void libraw_close(libraw_data_t* lr)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        delete ip;
    }

    void  libraw_set_memerror_handler(libraw_data_t* lr, memory_callback cb,void *data)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->set_memerror_handler(cb,data);

    }
    void libraw_set_dataerror_handler(libraw_data_t* lr,data_callback func,void *data)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->set_dataerror_handler(func,data);

    }
    void  libraw_set_progress_handler(libraw_data_t* lr, progress_callback cb,void *data)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->set_progress_handler(cb,data);

    }

    // DCRAW
    int  libraw_adjust_sizes_info_only(libraw_data_t* lr)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->adjust_sizes_info_only();
    }
    int  libraw_dcraw_ppm_tiff_writer(libraw_data_t* lr,const char *filename)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->dcraw_ppm_tiff_writer(filename);
    }
    int  libraw_dcraw_thumb_writer(libraw_data_t* lr,const char *fname)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->dcraw_thumb_writer(fname);

    }
    int libraw_dcraw_process(libraw_data_t* lr)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->dcraw_process();
    }
    libraw_processed_image_t *libraw_dcraw_make_mem_image(libraw_data_t* lr,int *errc)
    {
        if(!lr) { if(errc) *errc=EINVAL; return NULL;}
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->dcraw_make_mem_image(errc);
    }
    libraw_processed_image_t *libraw_dcraw_make_mem_thumb(libraw_data_t* lr,int *errc)
    {
        if(!lr) { if(errc) *errc=EINVAL; return NULL;}
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->dcraw_make_mem_thumb(errc);
    }

    void libraw_dcraw_clear_mem(libraw_processed_image_t* p)
    {
        LibRaw::dcraw_clear_mem(p);
    }

    int  libraw_raw2image(libraw_data_t* lr)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->raw2image();
    }
    void  libraw_free_image(libraw_data_t* lr)
    {
        if(!lr) return;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        ip->free_image();
    }
    int  libraw_get_decoder_info(libraw_data_t* lr,libraw_decoder_info_t *d)
    {
        if(!lr || !d) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->get_decoder_info(d);
    }
    int libraw_COLOR(libraw_data_t *lr, int row, int col)
    {
        if(!lr) return EINVAL;
        LibRaw *ip = (LibRaw*) lr->parent_class;
        return ip->COLOR(row,col);
    }
#ifdef __cplusplus
}
#endif

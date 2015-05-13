#ifndef J2K_HELPER_H
#define J2K_HELPER_H

// ==========================================================
// Helper functions (see J2KHelper.cpp)
// ==========================================================

/** 
FreeImageIO wrapper
*/
typedef struct tagJ2KFIO_t {
	FreeImageIO *io;		//! FreeImage IO
    fi_handle handle;		//! FreeImage handle
	opj_stream_t *stream;	//! OpenJPEG stream
} J2KFIO_t;

/**
Stream constructor
*/
J2KFIO_t* opj_freeimage_stream_create(FreeImageIO *io, fi_handle handle, BOOL bRead);

/**
Stream destructor
*/
void opj_freeimage_stream_destroy(J2KFIO_t* fio);

/**
Conversion opj_image_t => FIBITMAP
*/
FIBITMAP* J2KImageToFIBITMAP(int format_id, const opj_image_t *image, BOOL header_only);
/**
Conversion FIBITMAP => opj_image_t
*/
opj_image_t* FIBITMAPToJ2KImage(int format_id, FIBITMAP *dib, const opj_cparameters_t *parameters);

#endif // J2K_HELPER_H
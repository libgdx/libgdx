// ==========================================================
// JPEG lossless transformations
//
// Design and implementation by
// - Petr Pytelka (pyta@lightcomp.com)
// - Hervé Drolon (drolon@infonie.fr)
// - Mihail Naydenov (mnaydenov@users.sourceforge.net)
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
// ==========================================================

extern "C" {
#define XMD_H
#undef FAR
#include <setjmp.h>

#include "../LibJPEG/jinclude.h"
#include "../LibJPEG/jpeglib.h"
#include "../LibJPEG/jerror.h"
#include "../LibJPEG/transupp.h"
}

#include "FreeImage.h"
#include "Utilities.h"
#include "FreeImageIO.h"

// ----------------------------------------------------------
//   Source manager & Destination manager setup
//   (see PluginJPEG.cpp)
// ----------------------------------------------------------

void jpeg_freeimage_src(j_decompress_ptr cinfo, fi_handle infile, FreeImageIO *io);
void jpeg_freeimage_dst(j_compress_ptr cinfo, fi_handle outfile, FreeImageIO *io);

// ----------------------------------------------------------
//   Error handling
//   (see also PluginJPEG.cpp)
// ----------------------------------------------------------

/**
	Receives control for a fatal error.  Information sufficient to
	generate the error message has been stored in cinfo->err; call
	output_message to display it.  Control must NOT return to the caller;
	generally this routine will exit() or longjmp() somewhere.
*/
METHODDEF(void)
ls_jpeg_error_exit (j_common_ptr cinfo) {
	// always display the message
	(*cinfo->err->output_message)(cinfo);

	// allow JPEG with a premature end of file
	if((cinfo)->err->msg_parm.i[0] != 13) {

		// let the memory manager delete any temp files before we die
		jpeg_destroy(cinfo);

		throw FIF_JPEG;
	}
}

/**
	Actual output of any JPEG message.  Note that this method does not know
	how to generate a message, only where to send it.
*/
METHODDEF(void)
ls_jpeg_output_message (j_common_ptr cinfo) {
	char buffer[JMSG_LENGTH_MAX];

	// create the message
	(*cinfo->err->format_message)(cinfo, buffer);
	// send it to user's message proc
	FreeImage_OutputMessageProc(FIF_JPEG, buffer);
}

// ----------------------------------------------------------
//   Main program
// ----------------------------------------------------------

/**
Build a crop string. 

@param crop Output crop string
@param left Specifies the left position of the cropped rectangle
@param top Specifies the top position of the cropped rectangle
@param right Specifies the right position of the cropped rectangle
@param bottom Specifies the bottom position of the cropped rectangle
@param width Image width
@param height Image height
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL
getCropString(char* crop, int* left, int* top, int* right, int* bottom, int width, int height) {
	if(!left || !top || !right || !bottom) {
		return FALSE;
	}

	*left = CLAMP(*left, 0, width);
	*top = CLAMP(*top, 0, height);

	// negative/zero right and bottom count from the edges inwards

	if(*right <= 0) {
		*right = width + *right;
	}
	if(*bottom <= 0) {
		*bottom = height + *bottom;
	}

	*right = CLAMP(*right, 0, width);
	*bottom = CLAMP(*bottom, 0, height);

	// test for empty rect

	if(((*left - *right) == 0) || ((*top - *bottom) == 0)) {
		return FALSE;
	}

	// normalize the rectangle

	if(*right < *left) {
		INPLACESWAP(*left, *right);
	}
	if(*bottom < *top) {
		INPLACESWAP(*top, *bottom);
	}

	// test for "noop" rect

	if(*left == 0 && *right == width && *top == 0 && *bottom == height) {
		return FALSE;
	}

	// build the crop option
	sprintf(crop, "%dx%d+%d+%d", *right - *left, *bottom - *top, *left, *top);

	return TRUE;
}

static BOOL
JPEGTransformFromHandle(FreeImageIO* src_io, fi_handle src_handle, FreeImageIO* dst_io, fi_handle dst_handle, FREE_IMAGE_JPEG_OPERATION operation, int* left, int* top, int* right, int* bottom, BOOL perfect) {
	const BOOL onlyReturnCropRect = (dst_io == NULL) || (dst_handle == NULL);
	const long stream_start = onlyReturnCropRect ? 0 : dst_io->tell_proc(dst_handle);
	BOOL swappedDim = FALSE;
	BOOL trimH = FALSE;
	BOOL trimV = FALSE;

	// Set up the jpeglib structures
	jpeg_decompress_struct srcinfo;
	jpeg_compress_struct dstinfo;
	jpeg_error_mgr jsrcerr, jdsterr;
	jvirt_barray_ptr *src_coef_arrays = NULL;
	jvirt_barray_ptr *dst_coef_arrays = NULL;
	// Support for copying optional markers from source to destination file
	JCOPY_OPTION copyoption;
	// Image transformation options
	jpeg_transform_info transfoptions;

	// Initialize structures
	memset(&srcinfo, 0, sizeof(srcinfo));
	memset(&jsrcerr, 0, sizeof(jsrcerr));
	memset(&jdsterr, 0, sizeof(jdsterr));
	memset(&dstinfo, 0, sizeof(dstinfo));
	memset(&transfoptions, 0, sizeof(transfoptions));

	// Copy all extra markers from source file
	copyoption = JCOPYOPT_ALL;

	// Set up default JPEG parameters
	transfoptions.force_grayscale = FALSE;
	transfoptions.crop = FALSE;

	// Select the transform option
	switch(operation) {
		case FIJPEG_OP_FLIP_H:		// horizontal flip
			transfoptions.transform = JXFORM_FLIP_H;
			trimH = TRUE;
			break;
		case FIJPEG_OP_FLIP_V:		// vertical flip
			transfoptions.transform = JXFORM_FLIP_V;
			trimV = TRUE;
			break;
		case FIJPEG_OP_TRANSPOSE:	// transpose across UL-to-LR axis
			transfoptions.transform = JXFORM_TRANSPOSE;
			swappedDim = TRUE;
			break;
		case FIJPEG_OP_TRANSVERSE:	// transpose across UR-to-LL axis
			transfoptions.transform = JXFORM_TRANSVERSE;
			trimH = TRUE;
			trimV = TRUE;
			swappedDim = TRUE;
			break;
		case FIJPEG_OP_ROTATE_90:	// 90-degree clockwise rotation
			transfoptions.transform = JXFORM_ROT_90;
			trimH = TRUE;
			swappedDim = TRUE;
			break;
		case FIJPEG_OP_ROTATE_180:	// 180-degree rotation
			trimH = TRUE;
			trimV = TRUE;
			transfoptions.transform = JXFORM_ROT_180;
			break;
		case FIJPEG_OP_ROTATE_270:	// 270-degree clockwise (or 90 ccw)
			transfoptions.transform = JXFORM_ROT_270;
			trimV = TRUE;
			swappedDim = TRUE;
			break;
		default:
		case FIJPEG_OP_NONE:		// no transformation
			transfoptions.transform = JXFORM_NONE;
			break;
	}
	// (perfect == TRUE) ==> fail if there is non-transformable edge blocks
	transfoptions.perfect = (perfect == TRUE) ? TRUE : FALSE;
	// Drop non-transformable edge blocks: trim off any partial edge MCUs that the transform can't handle.
	transfoptions.trim = TRUE;

	try {

		// Initialize the JPEG decompression object with default error handling
		srcinfo.err = jpeg_std_error(&jsrcerr);
		srcinfo.err->error_exit = ls_jpeg_error_exit;
		srcinfo.err->output_message = ls_jpeg_output_message;
		jpeg_create_decompress(&srcinfo);

		// Initialize the JPEG compression object with default error handling
		dstinfo.err = jpeg_std_error(&jdsterr);
		dstinfo.err->error_exit = ls_jpeg_error_exit;
		dstinfo.err->output_message = ls_jpeg_output_message;
		jpeg_create_compress(&dstinfo);

		// Specify data source for decompression
		jpeg_freeimage_src(&srcinfo, src_handle, src_io);

		// Enable saving of extra markers that we want to copy
		jcopy_markers_setup(&srcinfo, copyoption);

		// Read the file header
		jpeg_read_header(&srcinfo, TRUE);

		// crop option
		char crop[64];
		const BOOL hasCrop = getCropString(crop, left, top, right, bottom, swappedDim ? srcinfo.image_height : srcinfo.image_width, swappedDim ? srcinfo.image_width : srcinfo.image_height);

		if(hasCrop) {
			if(!jtransform_parse_crop_spec(&transfoptions, crop)) {
				FreeImage_OutputMessageProc(FIF_JPEG, "Bogus crop argument %s", crop);
				throw(1);
			}
		}

		// Any space needed by a transform option must be requested before
		// jpeg_read_coefficients so that memory allocation will be done right

		// Prepare transformation workspace
		// Fails right away if perfect flag is TRUE and transformation is not perfect
		if( !jtransform_request_workspace(&srcinfo, &transfoptions) ) {
			FreeImage_OutputMessageProc(FIF_JPEG, "Transformation is not perfect");
			throw(1);
		}

		if(left || top) {
			// compute left and top offsets, it's a bit tricky, taking into account both
			// transform, which might have trimed the image,
			// and crop itself, which is adjusted to lie on a iMCU boundary

			const int fullWidth = swappedDim ? srcinfo.image_height : srcinfo.image_width;
			const int fullHeight = swappedDim ? srcinfo.image_width : srcinfo.image_height;

			int transformedFullWidth = fullWidth;
			int transformedFullHeight = fullHeight;

			if(trimH && transformedFullWidth/transfoptions.iMCU_sample_width > 0) {
				transformedFullWidth = (transformedFullWidth/transfoptions.iMCU_sample_width) * transfoptions.iMCU_sample_width;
			}
			if(trimV && transformedFullHeight/transfoptions.iMCU_sample_height > 0) {
				transformedFullHeight = (transformedFullHeight/transfoptions.iMCU_sample_height) * transfoptions.iMCU_sample_height;
			}

			const int trimmedWidth = fullWidth - transformedFullWidth;
			const int trimmedHeight = fullHeight - transformedFullHeight;

			if(left) {
				*left = trimmedWidth + transfoptions.x_crop_offset * transfoptions.iMCU_sample_width;
			}
			if(top) {
				*top = trimmedHeight + transfoptions.y_crop_offset * transfoptions.iMCU_sample_height;
			}
		}

		if(right) {
			*right = (left ? *left : 0) + transfoptions.output_width;
		}
		if(bottom) {
			*bottom = (top ? *top : 0) + transfoptions.output_height;
		}

		// if only the crop rect is requested, we are done

		if(onlyReturnCropRect) {
			jpeg_destroy_compress(&dstinfo);
			jpeg_destroy_decompress(&srcinfo);
			return TRUE;
		}

		// Read source file as DCT coefficients
		src_coef_arrays = jpeg_read_coefficients(&srcinfo);

		// Initialize destination compression parameters from source values
		jpeg_copy_critical_parameters(&srcinfo, &dstinfo);

		// Adjust destination parameters if required by transform options;
		// also find out which set of coefficient arrays will hold the output
		dst_coef_arrays = jtransform_adjust_parameters(&srcinfo, &dstinfo, src_coef_arrays, &transfoptions);

		// Note: we assume that jpeg_read_coefficients consumed all input
		// until JPEG_REACHED_EOI, and that jpeg_finish_decompress will
		// only consume more while (! cinfo->inputctl->eoi_reached).
		// We cannot call jpeg_finish_decompress here since we still need the
		// virtual arrays allocated from the source object for processing.

		if(src_handle == dst_handle) {
			dst_io->seek_proc(dst_handle, stream_start, SEEK_SET);
		}

		// Specify data destination for compression
		jpeg_freeimage_dst(&dstinfo, dst_handle, dst_io);

		// Start compressor (note no image data is actually written here)
		jpeg_write_coefficients(&dstinfo, dst_coef_arrays);

		// Copy to the output file any extra markers that we want to preserve
		jcopy_markers_execute(&srcinfo, &dstinfo, copyoption);

		// Execute image transformation, if any
		jtransform_execute_transformation(&srcinfo, &dstinfo, src_coef_arrays, &transfoptions);

		// Finish compression and release memory
		jpeg_finish_compress(&dstinfo);
		jpeg_destroy_compress(&dstinfo);
		jpeg_finish_decompress(&srcinfo);
		jpeg_destroy_decompress(&srcinfo);

	}
	catch(...) {
		jpeg_destroy_compress(&dstinfo);
		jpeg_destroy_decompress(&srcinfo);
		return FALSE;
	}

	return TRUE;
}

// ----------------------------------------------------------
//   FreeImage interface
// ----------------------------------------------------------

BOOL DLL_CALLCONV
FreeImage_JPEGTransformFromHandle(FreeImageIO* src_io, fi_handle src_handle, FreeImageIO* dst_io, fi_handle dst_handle, FREE_IMAGE_JPEG_OPERATION operation, int* left, int* top, int* right, int* bottom, BOOL perfect) {
	return JPEGTransformFromHandle(src_io, src_handle, dst_io, dst_handle, operation, left, top, right, bottom, perfect);
}

static void
closeStdIO(fi_handle src_handle, fi_handle dst_handle) {
	if(src_handle) {
		fclose((FILE*)src_handle);
	}
	if(dst_handle) {
		fclose((FILE*)dst_handle);
	}
}

static BOOL
openStdIO(const char* src_file, const char* dst_file, FreeImageIO* dst_io, fi_handle* src_handle, fi_handle* dst_handle) {
	*src_handle = NULL;
	*dst_handle = NULL;
	
	FreeImageIO io;
	SetDefaultIO (&io);
	
	const BOOL isSameFile = (dst_file && (strcmp(src_file, dst_file) == 0)) ? TRUE : FALSE;
	
	FILE* srcp = NULL;
	FILE* dstp = NULL;
	
	if(isSameFile) {
		srcp = fopen(src_file, "r+b");
		dstp = srcp;
	}
	else {
		srcp = fopen(src_file, "rb");
		if(dst_file) {
			dstp = fopen(dst_file, "wb");
		}
	}
	
	if(!srcp || (dst_file && !dstp)) {
		if(!srcp) {
			FreeImage_OutputMessageProc(FIF_JPEG, "Cannot open \"%s\" for reading", src_file);
		} else {
			FreeImage_OutputMessageProc(FIF_JPEG, "Cannot open \"%s\" for writing", dst_file);
		}
		closeStdIO(srcp, dstp);
		return FALSE;
	}

	if(FreeImage_GetFileTypeFromHandle(&io, srcp) != FIF_JPEG) {
		FreeImage_OutputMessageProc(FIF_JPEG, " Source file \"%s\" is not jpeg", src_file);
		closeStdIO(srcp, dstp);
		return FALSE;
	}

	*dst_io = io;
	*src_handle = srcp;
	*dst_handle = dstp;

	return TRUE;
}

static BOOL
openStdIOU(const wchar_t* src_file, const wchar_t* dst_file, FreeImageIO* dst_io, fi_handle* src_handle, fi_handle* dst_handle) {
#ifdef _WIN32

	*src_handle = NULL;
	*dst_handle = NULL;

	FreeImageIO io;
	SetDefaultIO (&io);
	
	const BOOL isSameFile = (dst_file && (wcscmp(src_file, dst_file) == 0)) ? TRUE : FALSE;

	FILE* srcp = NULL;
	FILE* dstp = NULL;

	if(isSameFile) {
		srcp = _wfopen(src_file, L"r+b");
		dstp = srcp;
	} else {
		srcp = _wfopen(src_file, L"rb");
		if(dst_file) {
			dstp = _wfopen(dst_file, L"wb");
		}
	}

	if(!srcp || (dst_file && !dstp)) {
		if(!srcp) {
			FreeImage_OutputMessageProc(FIF_JPEG, "Cannot open source file for reading");
		} else {
			FreeImage_OutputMessageProc(FIF_JPEG, "Cannot open destination file for writing");
		}
		closeStdIO(srcp, dstp);
		return FALSE;
	}

	if(FreeImage_GetFileTypeFromHandle(&io, srcp) != FIF_JPEG) {
		FreeImage_OutputMessageProc(FIF_JPEG, " Source file is not jpeg");
		closeStdIO(srcp, dstp);
		return FALSE;
	}

	*dst_io = io;
	*src_handle = srcp;
	*dst_handle = dstp;

	return TRUE;

#else
	return FALSE;
#endif // _WIN32
}

BOOL DLL_CALLCONV
FreeImage_JPEGTransform(const char *src_file, const char *dst_file, FREE_IMAGE_JPEG_OPERATION operation, BOOL perfect) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIO(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = JPEGTransformFromHandle(&io, src, &io, dst, operation, NULL, NULL, NULL, NULL, perfect);

	closeStdIO(src, dst);

	return ret;
}

BOOL DLL_CALLCONV
FreeImage_JPEGCrop(const char *src_file, const char *dst_file, int left, int top, int right, int bottom) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIO(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = FreeImage_JPEGTransformFromHandle(&io, src, &io, dst, FIJPEG_OP_NONE, &left, &top, &right, &bottom, FALSE);
	
	closeStdIO(src, dst);
	
	return ret;
}

BOOL DLL_CALLCONV
FreeImage_JPEGTransformU(const wchar_t *src_file, const wchar_t *dst_file, FREE_IMAGE_JPEG_OPERATION operation, BOOL perfect) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIOU(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = JPEGTransformFromHandle(&io, src, &io, dst, operation, NULL, NULL, NULL, NULL, perfect);
	
	closeStdIO(src, dst);

	return ret;
}

BOOL DLL_CALLCONV
FreeImage_JPEGCropU(const wchar_t *src_file, const wchar_t *dst_file, int left, int top, int right, int bottom) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIOU(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = FreeImage_JPEGTransformFromHandle(&io, src, &io, dst, FIJPEG_OP_NONE, &left, &top, &right, &bottom, FALSE);

	closeStdIO(src, dst);

	return ret;
}

BOOL DLL_CALLCONV
FreeImage_JPEGTransformCombined(const char *src_file, const char *dst_file, FREE_IMAGE_JPEG_OPERATION operation, int* left, int* top, int* right, int* bottom, BOOL perfect) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIO(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = FreeImage_JPEGTransformFromHandle(&io, src, &io, dst, operation, left, top, right, bottom, perfect);

	closeStdIO(src, dst);

	return ret;
}

BOOL DLL_CALLCONV
FreeImage_JPEGTransformCombinedU(const wchar_t *src_file, const wchar_t *dst_file, FREE_IMAGE_JPEG_OPERATION operation, int* left, int* top, int* right, int* bottom, BOOL perfect) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!openStdIOU(src_file, dst_file, &io, &src, &dst)) {
		return FALSE;
	}
	
	BOOL ret = FreeImage_JPEGTransformFromHandle(&io, src, &io, dst, operation, left, top, right, bottom, perfect);

	closeStdIO(src, dst);

	return ret;
}

// --------------------------------------------------------------------------

static BOOL
getMemIO(FIMEMORY* src_stream, FIMEMORY* dst_stream, FreeImageIO* dst_io, fi_handle* src_handle, fi_handle* dst_handle) {
	*src_handle = NULL;
	*dst_handle = NULL;

	FreeImageIO io;
	SetMemoryIO (&io);

	if(dst_stream) {
		FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(dst_stream->data);
		if(mem_header->delete_me != TRUE) {
			// do not save in a user buffer
			FreeImage_OutputMessageProc(FIF_JPEG, "Destination memory buffer is read only");
			return FALSE;
		}
	}

	*dst_io = io;
	*src_handle = src_stream;
	*dst_handle = dst_stream;

	return TRUE;
}

BOOL DLL_CALLCONV
FreeImage_JPEGTransformCombinedFromMemory(FIMEMORY* src_stream, FIMEMORY* dst_stream, FREE_IMAGE_JPEG_OPERATION operation, int* left, int* top, int* right, int* bottom, BOOL perfect) {
	FreeImageIO io;
	fi_handle src;
	fi_handle dst;
	
	if(!getMemIO(src_stream, dst_stream, &io, &src, &dst)) {
		return FALSE;
	}
	
	return FreeImage_JPEGTransformFromHandle(&io, src, &io, dst, operation, left, top, right, bottom, perfect);
}


// ==========================================================
// RAW camera image loader
//
// Design and implementation by 
// - Hervé Drolon (drolon@infonie.fr)
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

#include "../LibRawLite/libraw/libraw.h"

#include "FreeImage.h"
#include "Utilities.h"
#include "../Metadata/FreeImageTag.h"

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Internal functions
// ==========================================================

// ----------------------------------------------------------
//   FreeImage datastream wrapper
// ----------------------------------------------------------

class LibRaw_freeimage_datastream : public LibRaw_abstract_datastream {
private: 
	FreeImageIO *_io;
	fi_handle _handle;
	long _eof;
	INT64 _fsize;

public:
	LibRaw_freeimage_datastream(FreeImageIO *io, fi_handle handle) : _io(io), _handle(handle) {
		long start_pos = io->tell_proc(handle);
		io->seek_proc(handle, 0, SEEK_END);
		_eof = io->tell_proc(handle);
		_fsize = _eof - start_pos;
		io->seek_proc(handle, start_pos, SEEK_SET);
	}

	~LibRaw_freeimage_datastream() {
	}

    int valid() { 
		return (_io && _handle);
	}

    int read(void *buffer, size_t size, size_t count) { 
		if(substream) return substream->read(buffer, size, count);
		return _io->read_proc(buffer, (unsigned)size, (unsigned)count, _handle);
	}

    int seek(INT64 offset, int origin) { 
        if(substream) return substream->seek(offset, origin);
		return _io->seek_proc(_handle, (long)offset, origin);
	}

    INT64 tell() { 
		if(substream) return substream->tell();
        return _io->tell_proc(_handle);
    }
	
	INT64 size() {
		return _fsize;
	}

    int get_char() { 
		int c = 0;
		if(substream) return substream->get_char();
		if(!_io->read_proc(&c, 1, 1, _handle)) return -1;
		return c;
   }
	
	char* gets(char *buffer, int length) { 
		if (substream) return substream->gets(buffer, length);
		memset(buffer, 0, length);
		for(int i = 0; i < length; i++) {
			if(!_io->read_proc(&buffer[i], 1, 1, _handle))
				return NULL;
			if(buffer[i] == 0x0A)
				break;
		}
		return buffer;
	}

	int scanf_one(const char *fmt, void* val) {
		std::string buffer;
		char element = 0;
		bool bDone = false;
		if(substream) return substream->scanf_one(fmt,val);				
		do {
			if(_io->read_proc(&element, 1, 1, _handle) == 1) {
				switch(element) {
					case '0':
					case '\n':
					case ' ':
					case '\t':
						bDone = true;
						break;
					default:
						break;
				}
				buffer.append(&element, 1);
			} else {
				return 0;
			}
		} while(!bDone);

		return sscanf(buffer.c_str(), fmt, val);
	}

	int eof() { 
		if(substream) return substream->eof();
        return (_io->tell_proc(_handle) >= _eof);
    }

	void * make_jas_stream() {
		return NULL;
	}
};

// ----------------------------------------------------------

/**
Convert a processed raw data array to a FIBITMAP
@param image Processed raw image
@return Returns the converted dib if successfull, returns NULL otherwise
*/
static FIBITMAP * 
libraw_ConvertToDib(libraw_processed_image_t *image) {
	FIBITMAP *dib = NULL;
	try {
		unsigned width = image->width;
		unsigned height = image->height;
		unsigned bpp = image->bits;
		if(bpp == 16) {
			// allocate output dib
			dib = FreeImage_AllocateT(FIT_RGB16, width, height);
			if(!dib) {
				throw FI_MSG_ERROR_DIB_MEMORY;
			}
			// write data
			WORD *raw_data = (WORD*)image->data;
			for(unsigned y = 0; y < height; y++) {
				FIRGB16 *output = (FIRGB16*)FreeImage_GetScanLine(dib, height - 1 - y);
				for(unsigned x = 0; x < width; x++) {
					output[x].red   = raw_data[0];
					output[x].green = raw_data[1];
					output[x].blue  = raw_data[2];
					raw_data += 3;
				}
			}
		} else if(bpp == 8) {
			// allocate output dib
			dib = FreeImage_AllocateT(FIT_BITMAP, width, height, 24);
			if(!dib) {
				throw FI_MSG_ERROR_DIB_MEMORY;
			}
			// write data
			BYTE *raw_data = (BYTE*)image->data;
			for(unsigned y = 0; y < height; y++) {
				RGBTRIPLE *output = (RGBTRIPLE*)FreeImage_GetScanLine(dib, height - 1 - y);
				for(unsigned x = 0; x < width; x++) {
					output[x].rgbtRed   = raw_data[0];
					output[x].rgbtGreen = raw_data[1];
					output[x].rgbtBlue  = raw_data[2];
					raw_data += 3;
				}
			}
		}

	} catch(const char *text) {
		FreeImage_OutputMessageProc(s_format_id, text);
	}

	return dib;
}

/** 
Get the embedded JPEG preview image from RAW picture with included Exif Data. 
@param RawProcessor Libraw handle
@param flags JPEG load flags
@return Returns the loaded dib if successfull, returns NULL otherwise
*/
static FIBITMAP * 
libraw_LoadEmbeddedPreview(LibRaw *RawProcessor, int flags) {
	FIBITMAP *dib = NULL;
	libraw_processed_image_t *thumb_image = NULL;
	
	try {
		// unpack data
		if(RawProcessor->unpack_thumb() != LIBRAW_SUCCESS) {
			// run silently "LibRaw : failed to run unpack_thumb"
			return NULL;
		}

		// retrieve thumb image
		int error_code = 0;
		thumb_image = RawProcessor->dcraw_make_mem_thumb(&error_code);
		if(thumb_image) {
			if(thumb_image->type != LIBRAW_IMAGE_BITMAP) {
				// attach the binary data to a memory stream
				FIMEMORY *hmem = FreeImage_OpenMemory((BYTE*)thumb_image->data, (DWORD)thumb_image->data_size);
				// get the file type
				FREE_IMAGE_FORMAT fif = FreeImage_GetFileTypeFromMemory(hmem, 0);
				if(fif == FIF_JPEG) {
					// rotate according to Exif orientation
					flags |= JPEG_EXIFROTATE;
				}
				// load an image from the memory stream
				dib = FreeImage_LoadFromMemory(fif, hmem, flags);
				// close the stream
				FreeImage_CloseMemory(hmem);
			} else {
				// convert processed data to output dib
				dib = libraw_ConvertToDib(thumb_image);
			}
		} else {
			throw "LibRaw : failed to run dcraw_make_mem_thumb";
		}

		// clean-up and return
		RawProcessor->dcraw_clear_mem(thumb_image);

		return dib;

	} catch(const char *text) {
		// clean-up and return
		if(thumb_image) {
			RawProcessor->dcraw_clear_mem(thumb_image);
		}
		if(text != NULL) {
			FreeImage_OutputMessageProc(s_format_id, text);
		}
	}

	return NULL;
}
/**
Load raw data and convert to FIBITMAP
@param RawProcessor Libraw handle
@param bitspersample Output bitdepth (8- or 16-bit)
@return Returns the loaded dib if successfull, returns NULL otherwise
*/
static FIBITMAP * 
libraw_LoadRawData(LibRaw *RawProcessor, int bitspersample) {
	FIBITMAP *dib = NULL;
	libraw_processed_image_t *processed_image = NULL;

	try {
		// set decoding parameters
		// -----------------------
		
		// (-6) 16-bit or 8-bit
		RawProcessor->imgdata.params.output_bps = bitspersample;
		// (-g power toe_slope)
		if(bitspersample == 16) {
			// set -g 1 1 for linear curve
			RawProcessor->imgdata.params.gamm[0] = 1;
			RawProcessor->imgdata.params.gamm[1] = 1;
		} else if(bitspersample == 8) {
			// by default settings for rec. BT.709 are used: power 2.222 (i.e. gamm[0]=1/2.222) and slope 4.5
			RawProcessor->imgdata.params.gamm[0] = 1/2.222;
			RawProcessor->imgdata.params.gamm[1] = 4.5;
		}
		// (-W) Don't use automatic increase of brightness by histogram
		RawProcessor->imgdata.params.no_auto_bright = 1;
		// (-a) Use automatic white balance obtained after averaging over the entire image
		RawProcessor->imgdata.params.use_auto_wb = 1;
		// (-q 3) Adaptive homogeneity-directed demosaicing algorithm (AHD)
		RawProcessor->imgdata.params.user_qual = 3;

		// -----------------------

		// unpack data
		if(RawProcessor->unpack() != LIBRAW_SUCCESS) {
			throw "LibRaw : failed to unpack data";
		}

		// process data (... most consuming task ...)
		if(RawProcessor->dcraw_process() != LIBRAW_SUCCESS) {
			throw "LibRaw : failed to process data";
		}

		// retrieve processed image
		int error_code = 0;
		processed_image = RawProcessor->dcraw_make_mem_image(&error_code);
		if(processed_image) {
			// type SHOULD be LIBRAW_IMAGE_BITMAP, but we'll check
			if(processed_image->type != LIBRAW_IMAGE_BITMAP) {
				throw "invalid image type";
			}
			// only 3-color images supported...
			if(processed_image->colors != 3) {
				throw "only 3-color images supported";
			}
		} else {
			throw "LibRaw : failed to run dcraw_make_mem_image";
		}

		// convert processed data to output dib
		dib = libraw_ConvertToDib(processed_image);
	
		// clean-up and return
		RawProcessor->dcraw_clear_mem(processed_image);

		return dib;

	} catch(const char *text) {
		// clean-up and return
		if(processed_image) {
			RawProcessor->dcraw_clear_mem(processed_image);
		}
		FreeImage_OutputMessageProc(s_format_id, text);
	}

	return NULL;
}

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "RAW";
}

static const char * DLL_CALLCONV
Description() {
	return "RAW camera image";
}

static const char * DLL_CALLCONV
Extension() {
	/**
	Below are known RAW file extensions that you can check using FreeImage_GetFIFFromFormat. 
	If a file extension is not listed, that doesn't mean that you cannot load it. 
	Using FreeImage_GetFileType is the best way to know if a RAW file format is supported. 
	*/
	static const char *raw_extensions = 
		"3fr,"   // Hasselblad Digital Camera Raw Image Format.
		"arw,"   // Sony Digital Camera Raw Image Format for Alpha devices.
		"bay,"   // Casio Digital Camera Raw File Format.
		"bmq,"   // NuCore Raw Image File.
		"cap,"   // Phase One Digital Camera Raw Image Format.
		"cine,"  // Phantom Software Raw Image File.
		"cr2,"   // Canon Digital Camera RAW Image Format version 2.0. These images are based on the TIFF image standard.
		"crw,"   // Canon Digital Camera RAW Image Format version 1.0. 
		"cs1,"   // Sinar Capture Shop Raw Image File.
		"dc2,"   // Kodak DC25 Digital Camera File.
		"dcr,"   // Kodak Digital Camera Raw Image Format for these models: Kodak DSC Pro SLR/c, Kodak DSC Pro SLR/n, Kodak DSC Pro 14N, Kodak DSC PRO 14nx.
		"drf,"   // Kodak Digital Camera Raw Image Format.
		"dsc,"   // Kodak Digital Camera Raw Image Format.
		"dng,"   // Adobe Digital Negative: DNG is publicly available archival format for the raw files generated by digital cameras. By addressing the lack of an open standard for the raw files created by individual camera models, DNG helps ensure that photographers will be able to access their files in the future. 
		"erf,"   // Epson Digital Camera Raw Image Format.
		"fff,"   // Imacon Digital Camera Raw Image Format.
		"ia,"    // Sinar Raw Image File.
		"iiq,"   // Phase One Digital Camera Raw Image Format.
		"k25,"   // Kodak DC25 Digital Camera Raw Image Format.
		"kc2,"   // Kodak DCS200 Digital Camera Raw Image Format.
		"kdc,"   // Kodak Digital Camera Raw Image Format.
		"mdc,"   // Minolta RD175 Digital Camera Raw Image Format.
		"mef,"   // Mamiya Digital Camera Raw Image Format.
		"mos,"   // Leaf Raw Image File.
		"mrw,"   // Minolta Dimage Digital Camera Raw Image Format.
		"nef,"   // Nikon Digital Camera Raw Image Format.
		"nrw,"   // Nikon Digital Camera Raw Image Format.
		"orf,"   // Olympus Digital Camera Raw Image Format.
		"pef,"   // Pentax Digital Camera Raw Image Format.
		"ptx,"   // Pentax Digital Camera Raw Image Format.
		"pxn,"   // Logitech Digital Camera Raw Image Format.
		"qtk,"   // Apple Quicktake 100/150 Digital Camera Raw Image Format.
		"raf,"   // Fuji Digital Camera Raw Image Format.
		"raw,"   // Panasonic Digital Camera Image Format.
		"rdc,"   // Digital Foto Maker Raw Image File.
		"rw2,"   // Panasonic LX3 Digital Camera Raw Image Format.
		"rwl,"	 // Leica Camera Raw Image Format.
		"rwz,"   // Rawzor Digital Camera Raw Image Format.
		"sr2,"   // Sony Digital Camera Raw Image Format.
		"srf,"   // Sony Digital Camera Raw Image Format for DSC-F828 8 megapixel digital camera or Sony DSC-R1.
		"srw,"   // Samsung Raw Image Format.
		"sti";   // Sinar Capture Shop Raw Image File.
//		"x3f"   // Sigma Digital Camera Raw Image Format for devices based on Foveon X3 direct image sensor.
	return raw_extensions;
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-dcraw";
}

static BOOL 
HasMagicHeader(FreeImageIO *io, fi_handle handle) {
	const unsigned signature_size = 32;
	BYTE signature[signature_size] = { 0 };

	// Canon (CR2), Intel byte order
	const BYTE CR2_II[] = { 0x49, 0x49, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52, 0x02, 0x00 };
	// Canon (CR2), Motorola byte order
	const BYTE CR2_MM[] = { 0x4D, 0x4D, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52, 0x02, 0x00 };
	// Canon (CRW), Intel byte order
	const BYTE CRW_II[] = { 0x49, 0x49, 0x1A, 0x00, 0x00, 0x00, 0x48, 0x45, 0x41, 0x50, 0x43, 0x43, 0x44, 0x52, 0x02, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	// Minolta (MRW)
	const BYTE MRW[] = { 0x00, 0x4D, 0x52, 0x4D, 0x00 };
	// Olympus (ORF), Intel byte order
	const BYTE ORF_IIRS[] = { 0x49, 0x49, 0x52, 0x53, 0x08, 0x00, 0x00, 0x00 }; 
	const BYTE ORF_IIRO[] = { 0x49, 0x49, 0x52, 0x4F, 0x08, 0x00, 0x00, 0x00 }; 
	// Olympus (ORF), Motorola byte order
	const BYTE ORF_MMOR[] = { 0x4D, 0x4D, 0x4F, 0x52, 0x00, 0x00, 0x00, 0x08 }; 
	// Fujifilm (RAF)
	const BYTE RAF[] = { 0x46, 0x55, 0x4A, 0x49, 0x46, 0x49, 0x4C, 0x4D, 0x43, 0x43, 0x44, 0x2D, 0x52, 0x41, 0x57, 0x20, 0x30, 0x32, 0x30, 0x31 };
	// Panasonic (RW2) or Leica (RWL)
	const BYTE RW2_II[] = { 0x49, 0x49, 0x55, 0x00, 0x18, 0x00, 0x00, 0x00, 0x88, 0xE7, 0x74, 0xD8, 0xF8, 0x25, 0x1D, 0x4D, 0x94, 0x7A, 0x6E, 0x77, 0x82, 0x2B, 0x5D, 0x6A };

	if(io->read_proc(signature, 1, signature_size, handle) != signature_size) {
		return FALSE;
	}
	if(memcmp(CR2_II, signature, 12) == 0)
		return TRUE;
	if(memcmp(CR2_MM, signature, 12) == 0)
		return TRUE;
	if(memcmp(CRW_II, signature, 26) == 0)
		return TRUE;
	if(memcmp(MRW, signature, 5) == 0)
		return TRUE;
	if(memcmp(ORF_IIRS, signature, 8) == 0)
		return TRUE;
	if(memcmp(ORF_IIRO, signature, 8) == 0)
		return TRUE;
	if(memcmp(ORF_MMOR, signature, 8) == 0)
		return TRUE;
	if(memcmp(RAF, signature, 20) == 0)
		return TRUE;
	if(memcmp(RW2_II, signature, 24) == 0)
		return TRUE;

	return FALSE;
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	// some RAW files have a magic signature (most of them have a TIFF signature)
	// try to check this in order to speed up the file identification
	{
		long tell = io->tell_proc(handle);
		if( HasMagicHeader(io, handle) ) {
			return TRUE;
		} else {
			io->seek_proc(handle, tell, SEEK_SET);
		}
	}

	// no magic signature : we need to open the file (it will take more time to identify it)
	// do not declare RawProcessor on the stack as it may be huge (300 KB)
	{
		LibRaw *RawProcessor = new(std::nothrow) LibRaw;

		if(RawProcessor) {
			BOOL bSuccess = TRUE;

			// wrap the input datastream
			LibRaw_freeimage_datastream datastream(io, handle);

			// open the datastream
			if(RawProcessor->open_datastream(&datastream) != LIBRAW_SUCCESS) {
				bSuccess = FALSE;	// LibRaw : failed to open input stream (unknown format)
			}

			// clean-up internal memory allocations
			RawProcessor->recycle();
			delete RawProcessor;

			return bSuccess;
		}
	}

	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return FALSE;
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsICCProfiles() {
	return TRUE;
}

static BOOL DLL_CALLCONV
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	FIBITMAP *dib = NULL;
	LibRaw *RawProcessor = NULL;

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	try {
		// do not declare RawProcessor on the stack as it may be huge (300 KB)
		RawProcessor = new(std::nothrow) LibRaw;
		if(!RawProcessor) {
			throw FI_MSG_ERROR_MEMORY;
		}

		// wrap the input datastream
		LibRaw_freeimage_datastream datastream(io, handle);

		// set decoding parameters
		// the following parameters affect data reading
		// --------------------------------------------

		// (-s [0..N-1]) Select one raw image from input file
		RawProcessor->imgdata.params.shot_select = 0;
		// (-w) Use camera white balance, if possible (otherwise, fallback to auto_wb)
		RawProcessor->imgdata.params.use_camera_wb = 1;
		// (-h) outputs the image in 50% size
		RawProcessor->imgdata.params.half_size = ((flags & RAW_HALFSIZE) == RAW_HALFSIZE) ? 1 : 0;

		// open the datastream
		if(RawProcessor->open_datastream(&datastream) != LIBRAW_SUCCESS) {
			throw "LibRaw : failed to open input stream (unknown format)";
		}

		if(header_only) {
			// header only mode
			dib = FreeImage_AllocateHeaderT(header_only, FIT_RGB16, RawProcessor->imgdata.sizes.width, RawProcessor->imgdata.sizes.height);
		}
		else if((flags & RAW_PREVIEW) == RAW_PREVIEW) {
			// try to get the embedded JPEG
			dib = libraw_LoadEmbeddedPreview(RawProcessor, 0);
			if(!dib) {
				// no JPEG preview: try to load as 8-bit/sample (i.e. RGB 24-bit)
				dib = libraw_LoadRawData(RawProcessor, 8);
			}
		} 
		else if((flags & RAW_DISPLAY) == RAW_DISPLAY) {
			// load raw data as 8-bit/sample (i.e. RGB 24-bit)
			dib = libraw_LoadRawData(RawProcessor, 8);
		} 
		else {
			// default: load raw data as linear 16-bit/sample (i.e. RGB 48-bit)
			dib = libraw_LoadRawData(RawProcessor, 16);
		}

		// save ICC profile if present
		if(dib && (NULL != RawProcessor->imgdata.color.profile)) {
			FreeImage_CreateICCProfile(dib, RawProcessor->imgdata.color.profile, RawProcessor->imgdata.color.profile_length);
		}

		// try to get JPEG embedded Exif metadata
		if(dib && !((flags & RAW_PREVIEW) == RAW_PREVIEW)) {
			FIBITMAP *metadata_dib = libraw_LoadEmbeddedPreview(RawProcessor, FIF_LOAD_NOPIXELS);
			if(metadata_dib) {
				FreeImage_CloneMetadata(dib, metadata_dib);
				FreeImage_Unload(metadata_dib);
			}
		}

		// clean-up internal memory allocations
		RawProcessor->recycle();
		delete RawProcessor;

		return dib;

	} catch(const char *text) {
		if(RawProcessor) {
			RawProcessor->recycle();
			delete RawProcessor;
		}
		if(dib) {
			FreeImage_Unload(dib);
		}
		FreeImage_OutputMessageProc(s_format_id, text);
	}

	return NULL;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitRAW(Plugin *plugin, int format_id) {
	s_format_id = format_id;

	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = RegExpr;
	plugin->open_proc = NULL;
	plugin->close_proc = NULL;
	plugin->pagecount_proc = NULL;
	plugin->pagecapability_proc = NULL;
	plugin->load_proc = Load;
	plugin->save_proc = NULL;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = SupportsICCProfiles;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

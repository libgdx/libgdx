// ==========================================================
// CUT Loader
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
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

#include "FreeImage.h"
#include "Utilities.h"

// ----------------------------------------------------------
//   Constants + headers
// ----------------------------------------------------------

#ifdef _WIN32
#pragma pack(push, 1)
#else
#pragma pack(1)
#endif

typedef struct tagCUTHEADER {
	WORD width;
	WORD height;
	LONG dummy;
} CUTHEADER;

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "CUT";
}

static const char * DLL_CALLCONV
Description() {
	return "Dr. Halo";
}

static const char * DLL_CALLCONV
Extension() {
	return "cut";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-cut";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
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
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	FIBITMAP *dib = NULL;

	if(!handle) {
		return NULL;
	}

	try {
		CUTHEADER header;		

		BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

		// read the cut header

		if(io->read_proc(&header, 1, sizeof(CUTHEADER), handle) != sizeof(CUTHEADER)) {
			throw FI_MSG_ERROR_PARSING;
		}

#ifdef FREEIMAGE_BIGENDIAN
		SwapShort((WORD *)&header.width);
		SwapShort((WORD *)&header.height);
#endif

		if ((header.width == 0) || (header.height == 0)) {
			return NULL;
		}

		// allocate a new bitmap

		dib = FreeImage_AllocateHeader(header_only, header.width, header.height, 8);

		if (dib == NULL) {
			throw FI_MSG_ERROR_DIB_MEMORY;
		}

		// stuff it with a palette

		RGBQUAD *palette = FreeImage_GetPalette(dib);

		for (int j = 0; j < 256; ++j) {
			palette[j].rgbBlue = palette[j].rgbGreen = palette[j].rgbRed = (BYTE)j;
		}
		
		if(header_only) {
			// header only mode
			return dib;
		}

		// unpack the RLE bitmap bits

		BYTE *bits = FreeImage_GetScanLine(dib, header.height - 1);

		unsigned i = 0, k = 0;
		unsigned pitch = FreeImage_GetPitch(dib);
		unsigned size = header.width * header.height;
		BYTE count = 0, run = 0;

		while (i < size) {
			if(io->read_proc(&count, 1, sizeof(BYTE), handle) != 1) {
				throw FI_MSG_ERROR_PARSING;
			}

			if (count == 0) {
				k = 0;
				bits -= pitch;

				// paint shop pro adds two useless bytes here...

				io->read_proc(&count, 1, sizeof(BYTE), handle);
				io->read_proc(&count, 1, sizeof(BYTE), handle);

				continue;
			}

			if (count & 0x80) {
				count &= ~(0x80);

				if(io->read_proc(&run, 1, sizeof(BYTE), handle) != 1) {
					throw FI_MSG_ERROR_PARSING;
				}

				if(k + count <= header.width) {
					memset(bits + k, run, count);
				} else {
					throw FI_MSG_ERROR_PARSING;
				}
			} else {
				if(k + count <= header.width) {
					if(io->read_proc(&bits[k], count, sizeof(BYTE), handle) != 1) {
						throw FI_MSG_ERROR_PARSING;
					}
				} else {
					throw FI_MSG_ERROR_PARSING;
				}
			}

			k += count;
			i += count;
		}

		return dib;

	} catch(const char* text) {
		if(dib) {
			FreeImage_Unload(dib);
		}
		FreeImage_OutputMessageProc(s_format_id, text);
		return NULL;
	}
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitCUT(Plugin *plugin, int format_id) {
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
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

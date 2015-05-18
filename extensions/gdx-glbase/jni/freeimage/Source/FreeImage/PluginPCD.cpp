// ==========================================================
// Kodak PhotoCD Loader
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// 
// Based on pascal code developed by Alex Kwak
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

// ==========================================================
// Internal functions
// ==========================================================

static int 
clamp(double x) {
	int a = (int)floor(x + 0.5);
	return (a < 0) ? 0 : (a > 255) ? 255 : a;
}

static void
YUV2RGB(int y, int cb, int cr, int &r, int &g, int &b) {
	double c11 = 0.0054980  * 256.0;
	double c12 = 0.0000001  * 256.0;
	double c13 = 0.0051681  * 256.0;
	double c21 = 0.0054980  * 256.0;
	double c22 = -0.0015446 * 256.0;
	double c23 = -0.0026325 * 256.0;
	double c31 = 0.0054980  * 256.0;
	double c32 = 0.0079533  * 256.0;
	double c33 = 0.0000001  * 256.0;

	r = clamp(c11 * y + c12 * (cb - 156) + c13 * (cr - 137));
	g = clamp(c21 * y + c22 * (cb - 156) + c23 * (cr - 137));
	b = clamp(c31 * y + c32 * (cb - 156) + c33 * (cr - 137));
}

static BOOL
VerticalOrientation(FreeImageIO *io, fi_handle handle) {
	char buffer[128];

	io->read_proc(buffer, 128, 1, handle);

	return (buffer[72] & 63) == 8;
}

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "PCD";
}

static const char * DLL_CALLCONV
Description() {
	return "Kodak PhotoCD";
}

static const char * DLL_CALLCONV
Extension() {
	return "pcd";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-photo-cd";
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
	unsigned width;
	unsigned height;
	const unsigned bpp = 24;
	int scan_line_add   = 1;
	int start_scan_line = 0;
	
	BYTE *y1 = NULL, *y2 = NULL, *cbcr = NULL;

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	// to make absolute seeks possible we store the current position in the file
	
	long offset_in_file = io->tell_proc(handle);
	long seek = 0;

	// decide which bitmap in the cabinet to load

	switch (flags) {
		case PCD_BASEDIV4 :
			seek = 0x2000;
			width = 192;
			height = 128;
			break;

		case PCD_BASEDIV16 :
			seek = 0xB800;
			width = 384;
			height = 256;
			break;

		default :
			seek = 0x30000;
			width = 768;
			height = 512;
			break;
	}

	try {
		// allocate the dib and write out the header
		dib = FreeImage_AllocateHeader(header_only, width, height, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
		if(!dib) throw FI_MSG_ERROR_DIB_MEMORY;

		if(header_only) {
			return dib;
		}

		// check if the PCD is bottom-up

		if (VerticalOrientation(io, handle)) {
			scan_line_add = -1;
			start_scan_line = height - 1;		
		}

		// temporary stuff to load PCD

		BYTE *y1 = (BYTE*)malloc(width * sizeof(BYTE));
		BYTE *y2 = (BYTE*)malloc(width * sizeof(BYTE));
		BYTE *cbcr = (BYTE*)malloc(width * sizeof(BYTE));
		if(!y1 || !y2 || !cbcr) throw FI_MSG_ERROR_MEMORY;

		BYTE *yl[] = { y1, y2 };

		// seek to the part where the bitmap data begins

		io->seek_proc(handle, offset_in_file, SEEK_SET);
		io->seek_proc(handle, seek, SEEK_CUR);

		// read the data

		for (unsigned y = 0; y < height / 2; y++) {
			io->read_proc(y1, width, 1, handle);
			io->read_proc(y2, width, 1, handle);
			io->read_proc(cbcr, width, 1, handle);

			for (int i = 0; i < 2; i++) {
				BYTE *bits = FreeImage_GetScanLine(dib, start_scan_line);
				for (unsigned x = 0; x < width; x++) {
					int r, g, b;

					YUV2RGB(yl[i][x], cbcr[x / 2], cbcr[(width / 2) + (x / 2)], r, g, b);

					bits[FI_RGBA_BLUE]  = (BYTE)b;
					bits[FI_RGBA_GREEN] = (BYTE)g;
					bits[FI_RGBA_RED]   = (BYTE)r;
					bits += 3;
				}

				start_scan_line += scan_line_add;
			}
		}

		free(cbcr);
		free(y2);
		free(y1);

		return dib;

	} catch(const char *text) {
		if(dib) FreeImage_Unload(dib);
		if(cbcr) free(cbcr);
		if(y2) free(y2);
		if(y1) free(y1);

		FreeImage_OutputMessageProc(s_format_id, text);

		return NULL;
	}
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitPCD(Plugin *plugin, int format_id) {
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
	plugin->validate_proc = NULL;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

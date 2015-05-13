// ==========================================================
// KOALA Loader
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

typedef struct tagKOALA {
	BYTE image[8000];		// pixmap image
	BYTE colour1[1000];		// first colourmap (colour 1 and 2)
	BYTE colour2[1000];		// second colourmap (colour 3)
	BYTE background;		// background colour
} koala_t;

struct colour_t {
	int	r;
	int g;
	int b;	
};

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

// ----------------------------------------------------------

#define CBM_WIDTH  320
#define CBM_HEIGHT 200

// ----------------------------------------------------------

const colour_t c64colours[16] = {
	{   0,   0,   0 },	// Black
	{ 255, 255, 255 },	// White
	{ 170,  17,  17 },	// Red
	{  12, 204, 204 },	// Cyan
	{ 221,  51, 221 },	// Purple
	{  0,  187,  0 },	// Green
	{   0,   0, 204 },	// Blue
	{ 255, 255, 140 },	// Yellow
	{ 204, 119,  34 },	// Orange
	{ 136,  68,   0 },	// Brown
	{ 255, 153, 136 },	// Light red
	{  92,  92,  92 },	// Gray 1
	{ 170, 170, 170 },	// Gray 2
	{ 140, 255, 178 },	// Light green
	{  39, 148, 255 },	// Light blue
	{ 196, 196, 196 }	// Gray 3
};

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

const char * DLL_CALLCONV
Format() {
	return "KOALA";
}

const char * DLL_CALLCONV
Description() {
	return "C64 Koala Graphics";
}

const char * DLL_CALLCONV
Extension() {
	return "koa";
}

const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-koala";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE koala_signature[] = { 0x00, 0x60 };
	BYTE signature[2] = { 0, 0 };

	io->read_proc(signature, 1, sizeof(koala_signature), handle);

	return (memcmp(koala_signature, signature, sizeof(koala_signature)) == 0);
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return FALSE;
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return FALSE;
}

// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	if (handle) {
		koala_t image;

		// read the load address

		unsigned char load_address[2];  // highbit, lowbit

		io->read_proc(&load_address, 1, 2, handle);

		// if the load address is correct, skip it. otherwise ignore the load address

		if ((load_address[0] != 0x00) || (load_address[1] != 0x60)) {
			((BYTE *)&image)[0] = load_address[0];
			((BYTE *)&image)[1] = load_address[1];

			io->read_proc((BYTE *)&image + 2, 1, 10001 - 2, handle);
		} else {
			io->read_proc(&image, 1, 10001, handle);
		}		

		// build DIB in memory

		FIBITMAP *dib = FreeImage_Allocate(CBM_WIDTH, CBM_HEIGHT, 4);

		if (dib) {
			// write out the commodore 64 color palette

			RGBQUAD *palette = FreeImage_GetPalette(dib);

			for (int i = 0; i < 16; i++) {
				palette[i].rgbBlue  = (BYTE)c64colours[i].b;
				palette[i].rgbGreen = (BYTE)c64colours[i].g;
				palette[i].rgbRed   = (BYTE)c64colours[i].r;
			}

			// write out bitmap data

			BYTE pixel_mask[4]         = { 0xc0, 0x30, 0x0c, 0x03 };
			BYTE pixel_displacement[4] = { 6, 4, 2, 0 };
			int	pixel, index, colourindex;
			unsigned char found_color = 0;

			for (int y = 0; y < 200; y++) {
				for (int x = 0; x < 160; x++) {
					// Get value of pixel at (x,y)

					index = (x / 4) * 8 + (y % 8) + (y / 8) * CBM_WIDTH;
					colourindex = (x / 4) + (y / 8) * 40;
					pixel = (image.image[index] & pixel_mask[x % 4]) >> pixel_displacement[x % 4];

					// Retrieve RGB values

					switch (pixel) {
						case 0: // Background
							found_color = image.background;
							break;
							
						case 1: // Colour 1
							found_color = image.colour1[colourindex] >> 4;
							break;
							
						case 2: // Colour 2
							found_color = image.colour1[colourindex] & 0xf;
							break;
							
						case 3: // Colour 3
							found_color = image.colour2[colourindex] & 0xf;
							break;
					};

					*(FreeImage_GetScanLine(dib, CBM_HEIGHT - y - 1) + x) = (found_color << 4) | found_color;
				}
			}

			return dib;
		}
	}

	return NULL;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitKOALA(Plugin *plugin, int format_id) {
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
}

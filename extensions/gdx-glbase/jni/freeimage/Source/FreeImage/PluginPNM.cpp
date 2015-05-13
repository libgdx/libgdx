// ==========================================================
// PNM (PPM, PGM, PBM) Loader and Writer
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
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

#include "FreeImage.h"
#include "Utilities.h"

// ==========================================================
// Internal functions
// ==========================================================

/**
Get an integer value from the actual position pointed by handle
*/
static int
GetInt(FreeImageIO *io, fi_handle handle) {
    char c = 0;
	BOOL firstchar;

    // skip forward to start of next number

    if(!io->read_proc(&c, 1, 1, handle)) throw FI_MSG_ERROR_PARSING;

    while (1) {
        // eat comments

        if (c == '#') {
			// if we're at a comment, read to end of line

            firstchar = TRUE;

            while (1) {
				if(!io->read_proc(&c, 1, 1, handle)) throw FI_MSG_ERROR_PARSING;

				if (firstchar && c == ' ') {
					// loop off 1 sp after #

					firstchar = FALSE;
				} else if (c == '\n') {
					break;
				}
			}
		}

        if (c >= '0' && c <='9') {
			// we've found what we were looking for

            break;
		}

        if(!io->read_proc(&c, 1, 1, handle)) throw FI_MSG_ERROR_PARSING;
    }

    // we're at the start of a number, continue until we hit a non-number

    int i = 0;

    while (1) {
        i = (i * 10) + (c - '0');

        if(!io->read_proc(&c, 1, 1, handle)) throw FI_MSG_ERROR_PARSING;

        if (c < '0' || c > '9')
            break;
    }

    return i;
}

/**
Read a WORD value taking into account the endianess issue
*/
static inline WORD 
ReadWord(FreeImageIO *io, fi_handle handle) {
	WORD level = 0;
	io->read_proc(&level, 2, 1, handle); 
#ifndef FREEIMAGE_BIGENDIAN
	SwapShort(&level);	// PNM uses the big endian convention
#endif
	return level;
}

/**
Write a WORD value taking into account the endianess issue
*/
static inline void 
WriteWord(FreeImageIO *io, fi_handle handle, const WORD value) {
	WORD level = value;
#ifndef FREEIMAGE_BIGENDIAN
	SwapShort(&level);	// PNM uses the big endian convention
#endif
	io->write_proc(&level, 2, 1, handle);
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
	return "PNM";
}

static const char * DLL_CALLCONV
Description() {
	return "Portable Network Media";
}

static const char * DLL_CALLCONV
Extension() {
	return "pbm,pgm,ppm";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/freeimage-pnm";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE pbm_id1[] = { 0x50, 0x31 };
	BYTE pbm_id2[] = { 0x50, 0x34 };
	BYTE pgm_id1[] = { 0x50, 0x32 };
	BYTE pgm_id2[] = { 0x50, 0x35 };
	BYTE ppm_id1[] = { 0x50, 0x33 };
	BYTE ppm_id2[] = { 0x50, 0x36 };
	BYTE signature[2] = { 0, 0 };

	io->read_proc(signature, 1, sizeof(pbm_id1), handle);

	if (memcmp(pbm_id1, signature, sizeof(pbm_id1)) == 0)
		return TRUE;

	if (memcmp(pbm_id2, signature, sizeof(pbm_id2)) == 0)
		return TRUE;

	if (memcmp(pgm_id1, signature, sizeof(pgm_id1)) == 0)
		return TRUE;

	if (memcmp(pgm_id2, signature, sizeof(pgm_id2)) == 0)
		return TRUE;

	if (memcmp(ppm_id1, signature, sizeof(ppm_id1)) == 0)
		return TRUE;

	if (memcmp(ppm_id2, signature, sizeof(ppm_id2)) == 0)
		return TRUE;

	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
			(depth == 1) ||
			(depth == 8) ||
			(depth == 24)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (
		(type == FIT_BITMAP)  ||
		(type == FIT_UINT16)  ||
		(type == FIT_RGB16)
	);
}

static BOOL DLL_CALLCONV
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	char id_one = 0, id_two = 0;
	int x, y;
	FIBITMAP *dib = NULL;
	RGBQUAD *pal;	// pointer to dib palette
	int i;

	if (!handle) {
		return NULL;
	}

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	try {
		FREE_IMAGE_TYPE image_type = FIT_BITMAP;	// standard image: 1-, 8-, 24-bit

		// Read the first two bytes of the file to determine the file format
		// "P1" = ascii bitmap, "P2" = ascii greymap, "P3" = ascii pixmap,
		// "P4" = raw bitmap, "P5" = raw greymap, "P6" = raw pixmap

		io->read_proc(&id_one, 1, 1, handle);
		io->read_proc(&id_two, 1, 1, handle);

		if ((id_one != 'P') || (id_two < '1') || (id_two > '6')) {			
			// signature error
			throw FI_MSG_ERROR_MAGIC_NUMBER;
		}

		// Read the header information: width, height and the 'max' value if any

		int width  = GetInt(io, handle);
		int height = GetInt(io, handle);
		int maxval = 1;

		if((id_two == '2') || (id_two == '5') || (id_two == '3') || (id_two == '6')) {
			maxval = GetInt(io, handle);
			if((maxval <= 0) || (maxval > 65535)) {
				FreeImage_OutputMessageProc(s_format_id, "Invalid max value : %d", maxval);
				throw (const char*)NULL;
			}
		}

		// Create a new DIB

		switch (id_two) {
			case '1':
			case '4':
				// 1-bit
				dib = FreeImage_AllocateHeader(header_only, width, height, 1);
				break;

			case '2':
			case '5':
				if(maxval > 255) {
					// 16-bit greyscale
					image_type = FIT_UINT16;
					dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height);
				} else {
					// 8-bit greyscale
					dib = FreeImage_AllocateHeader(header_only, width, height, 8);
				}
				break;

			case '3':
			case '6':
				if(maxval > 255) {
					// 48-bit RGB
					image_type = FIT_RGB16;
					dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height);
				} else {
					// 24-bit RGB
					dib = FreeImage_AllocateHeader(header_only, width, height, 24, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
				}
				break;
		}

		if (dib == NULL) {
			throw FI_MSG_ERROR_DIB_MEMORY;
		}

		// Build a greyscale palette if needed

		if(image_type == FIT_BITMAP) {
			switch(id_two)  {
				case '1':
				case '4':
					pal = FreeImage_GetPalette(dib);
					pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
					pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;
					break;

				case '2':
				case '5':
					pal = FreeImage_GetPalette(dib);
					for (i = 0; i < 256; i++) {
						pal[i].rgbRed	=
						pal[i].rgbGreen =
						pal[i].rgbBlue	= (BYTE)i;
					}
					break;

				default:
					break;
			}
		}

		if(header_only) {
			// header only mode
			return dib;
		}

		// Read the image...

		switch(id_two)  {
			case '1':
			case '4':
				// write the bitmap data

				if (id_two == '1') {	// ASCII bitmap
					for (y = 0; y < height; y++) {		
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < width; x++) {
							if (GetInt(io, handle) == 0)
								bits[x >> 3] |= (0x80 >> (x & 0x7));
							else
								bits[x >> 3] &= (0xFF7F >> (x & 0x7));
						}
					}
				}  else {		// Raw bitmap
					int line = CalculateLine(width, 1);

					for (y = 0; y < height; y++) {	
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < line; x++) {
							io->read_proc(&bits[x], 1, 1, handle);

							bits[x] = ~bits[x];
						}
					}
				}

				return dib;

			case '2':
			case '5':
				if(image_type == FIT_BITMAP) {
					// write the bitmap data

					if(id_two == '2') {		// ASCII greymap
						int level = 0;

						for (y = 0; y < height; y++) {	
							BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = GetInt(io, handle);
								bits[x] = (BYTE)((255 * level) / maxval);
							}
						}
					} else {		// Raw greymap
						BYTE level = 0;

						for (y = 0; y < height; y++) {		
							BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								io->read_proc(&level, 1, 1, handle);
								bits[x] = (BYTE)((255 * (int)level) / maxval);
							}
						}
					}
				}
				else if(image_type == FIT_UINT16) {
					// write the bitmap data

					if(id_two == '2') {		// ASCII greymap
						int level = 0;

						for (y = 0; y < height; y++) {	
							WORD *bits = (WORD*)FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = GetInt(io, handle);
								bits[x] = (WORD)((65535 * (double)level) / maxval);
							}
						}
					} else {		// Raw greymap
						WORD level = 0;

						for (y = 0; y < height; y++) {		
							WORD *bits = (WORD*)FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = ReadWord(io, handle);
								bits[x] = (WORD)((65535 * (double)level) / maxval);
							}
						}
					}
				}

				return dib;

			case '3':
			case '6':
				if(image_type == FIT_BITMAP) {
					// write the bitmap data

					if (id_two == '3') {		// ASCII pixmap
						int level = 0;

						for (y = 0; y < height; y++) {	
							BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = GetInt(io, handle);
								bits[FI_RGBA_RED] = (BYTE)((255 * level) / maxval);		// R
								level = GetInt(io, handle);
								bits[FI_RGBA_GREEN] = (BYTE)((255 * level) / maxval);	// G
								level = GetInt(io, handle);
								bits[FI_RGBA_BLUE] = (BYTE)((255 * level) / maxval);	// B

								bits += 3;
							}
						}
					}  else {			// Raw pixmap
						BYTE level = 0;

						for (y = 0; y < height; y++) {	
							BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								io->read_proc(&level, 1, 1, handle); 
								bits[FI_RGBA_RED] = (BYTE)((255 * (int)level) / maxval);	// R

								io->read_proc(&level, 1, 1, handle);
								bits[FI_RGBA_GREEN] = (BYTE)((255 * (int)level) / maxval);	// G

								io->read_proc(&level, 1, 1, handle);
								bits[FI_RGBA_BLUE] = (BYTE)((255 * (int)level) / maxval);	// B

								bits += 3;
							}
						}
					}
				}
				else if(image_type == FIT_RGB16) {
					// write the bitmap data

					if (id_two == '3') {		// ASCII pixmap
						int level = 0;

						for (y = 0; y < height; y++) {	
							FIRGB16 *bits = (FIRGB16*)FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = GetInt(io, handle);
								bits[x].red = (WORD)((65535 * (double)level) / maxval);		// R
								level = GetInt(io, handle);
								bits[x].green = (WORD)((65535 * (double)level) / maxval);	// G
								level = GetInt(io, handle);
								bits[x].blue = (WORD)((65535 * (double)level) / maxval);	// B
							}
						}
					}  else {			// Raw pixmap
						WORD level = 0;

						for (y = 0; y < height; y++) {	
							FIRGB16 *bits = (FIRGB16*)FreeImage_GetScanLine(dib, height - 1 - y);

							for (x = 0; x < width; x++) {
								level = ReadWord(io, handle);
								bits[x].red = (WORD)((65535 * (double)level) / maxval);		// R
								level = ReadWord(io, handle);
								bits[x].green = (WORD)((65535 * (double)level) / maxval);	// G
								level = ReadWord(io, handle);
								bits[x].blue = (WORD)((65535 * (double)level) / maxval);	// B
							}
						}
					}
				}

				return dib;
		}

	} catch (const char *text)  {
		if(dib) FreeImage_Unload(dib);

		if(NULL != text) {
			switch(id_two)  {
				case '1':
				case '4':
					FreeImage_OutputMessageProc(s_format_id, text);
					break;

				case '2':
				case '5':
					FreeImage_OutputMessageProc(s_format_id, text);
					break;

				case '3':
				case '6':
					FreeImage_OutputMessageProc(s_format_id, text);
					break;
			}
		}
	}
		
	return NULL;
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	// ----------------------------------------------------------
	//   PNM Saving
	// ----------------------------------------------------------
	//
	// Output format :
	//
	// Bit depth		flags			file format
	// -------------    --------------  -----------
	// 1-bit / pixel	PNM_SAVE_ASCII	PBM (P1)
	// 1-bit / pixel	PNM_SAVE_RAW	PBM (P4)
	// 8-bit / pixel	PNM_SAVE_ASCII	PGM (P2)
	// 8-bit / pixel	PNM_SAVE_RAW	PGM (P5)
	// 24-bit / pixel	PNM_SAVE_ASCII	PPM (P3)
	// 24-bit / pixel	PNM_SAVE_RAW	PPM (P6)
	// ----------------------------------------------------------

	int x, y;

	char buffer[256];	// temporary buffer whose size should be enough for what we need

	if(!dib || !handle) return FALSE;
	
	FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);

	int bpp		= FreeImage_GetBPP(dib);
	int width	= FreeImage_GetWidth(dib);
	int height	= FreeImage_GetHeight(dib);

	// Find the appropriate magic number for this file type

	int magic = 0;
	int maxval = 255;

	switch(image_type) {
		case FIT_BITMAP:
			switch (bpp) {
				case 1 :
					magic = 1;	// PBM file (B & W)
					break;
				case 8 : 			
					magic = 2;	// PGM file	(Greyscale)
					break;

				case 24 :
					magic = 3;	// PPM file (RGB)
					break;

				default:
					return FALSE;	// Invalid bit depth
			}
			break;
		
		case FIT_UINT16:
			magic = 2;	// PGM file	(Greyscale)
			maxval = 65535;
			break;

		case FIT_RGB16:
			magic = 3;	// PPM file (RGB)
			maxval = 65535;
			break;

		default:
			return FALSE;
	}


	if (flags == PNM_SAVE_RAW)
		magic += 3;

	// Write the header info

	sprintf(buffer, "P%d\n%d %d\n", magic, width, height);
	io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

	if (bpp != 1) {
		sprintf(buffer, "%d\n", maxval);
		io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
	}

	// Write the image data
	///////////////////////

	if(image_type == FIT_BITMAP) {
		switch(bpp)  {
			case 24 :            // 24-bit RGB, 3 bytes per pixel
			{
				if (flags == PNM_SAVE_RAW)  {
					for (y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < width; x++) {
							io->write_proc(&bits[FI_RGBA_RED], 1, 1, handle);	// R
							io->write_proc(&bits[FI_RGBA_GREEN], 1, 1, handle);	// G
							io->write_proc(&bits[FI_RGBA_BLUE], 1, 1, handle);	// B

							bits += 3;
						}
					}
				} else {
					int length = 0;

					for (y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);
						
						for (x = 0; x < width; x++) {
							sprintf(buffer, "%3d %3d %3d ", bits[FI_RGBA_RED], bits[FI_RGBA_GREEN], bits[FI_RGBA_BLUE]);

							io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

							length += 12;

							if(length > 58) {
								// No line should be longer than 70 characters
								sprintf(buffer, "\n");
								io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
								length = 0;
							}

							bits += 3;
						}					
					}

				}
			}
			break;

			case 8:		// 8-bit greyscale
			{
				if (flags == PNM_SAVE_RAW)  {
					for (y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < width; x++) {
							io->write_proc(&bits[x], 1, 1, handle);
						}
					}
				} else {
					int length = 0;

					for (y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < width; x++) {
							sprintf(buffer, "%3d ", bits[x]);

							io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

							length += 4;

							if (length > 66) {
								// No line should be longer than 70 characters
								sprintf(buffer, "\n");
								io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
								length = 0;
							}
						}
					}
				}
			}
			break;

			case 1:		// 1-bit B & W
			{
				int color;

				if (flags == PNM_SAVE_RAW)  {
					for(y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for(x = 0; x < (int)FreeImage_GetLine(dib); x++)
							io->write_proc(&bits[x], 1, 1, handle);
					}
				} else  {
					int length = 0;

					for (y = 0; y < height; y++) {
						// write the scanline to disc
						BYTE *bits = FreeImage_GetScanLine(dib, height - 1 - y);

						for (x = 0; x < (int)FreeImage_GetLine(dib) * 8; x++)	{
							color = (bits[x>>3] & (0x80 >> (x & 0x07))) != 0;

							sprintf(buffer, "%c ", color ? '1':'0');

							io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

							length += 2;

							if (length > 68) {
								// No line should be longer than 70 characters
								sprintf(buffer, "\n");
								io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
								length = 0;
							}
						}
					}
				}
			}
			
			break;
		}
	} // if(FIT_BITMAP)

	else if(image_type == FIT_UINT16) {		// 16-bit greyscale
		if (flags == PNM_SAVE_RAW)  {
			for (y = 0; y < height; y++) {
				// write the scanline to disc
				WORD *bits = (WORD*)FreeImage_GetScanLine(dib, height - 1 - y);

				for (x = 0; x < width; x++) {
					WriteWord(io, handle, bits[x]);
				}
			}
		} else {
			int length = 0;

			for (y = 0; y < height; y++) {
				// write the scanline to disc
				WORD *bits = (WORD*)FreeImage_GetScanLine(dib, height - 1 - y);

				for (x = 0; x < width; x++) {
					sprintf(buffer, "%5d ", bits[x]);

					io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

					length += 6;

					if (length > 64) {
						// No line should be longer than 70 characters
						sprintf(buffer, "\n");
						io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
						length = 0;
					}
				}
			}
		}
	}

	else if(image_type == FIT_RGB16) {		// 48-bit RGB
		if (flags == PNM_SAVE_RAW)  {
			for (y = 0; y < height; y++) {
				// write the scanline to disc
				FIRGB16 *bits = (FIRGB16*)FreeImage_GetScanLine(dib, height - 1 - y);

				for (x = 0; x < width; x++) {
					WriteWord(io, handle, bits[x].red);		// R
					WriteWord(io, handle, bits[x].green);	// G
					WriteWord(io, handle, bits[x].blue);	// B
				}
			}
		} else {
			int length = 0;

			for (y = 0; y < height; y++) {
				// write the scanline to disc
				FIRGB16 *bits = (FIRGB16*)FreeImage_GetScanLine(dib, height - 1 - y);
				
				for (x = 0; x < width; x++) {
					sprintf(buffer, "%5d %5d %5d ", bits[x].red, bits[x].green, bits[x].blue);

					io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);

					length += 18;

					if(length > 52) {
						// No line should be longer than 70 characters
						sprintf(buffer, "\n");
						io->write_proc(&buffer, (unsigned int)strlen(buffer), 1, handle);
						length = 0;
					}
				}					
			}

		}
	}

	return TRUE;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitPNM(Plugin *plugin, int format_id) {
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
	plugin->save_proc = Save;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

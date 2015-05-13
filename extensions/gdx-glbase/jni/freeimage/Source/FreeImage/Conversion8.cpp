// ==========================================================
// Bitmap conversion routines
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Hervé Drolon (drolon@infonie.fr)
// - Jani Kajala (janik@remedy.fi)
// - Karl-Heinz Bussian (khbussian@moss.de)
// - Carsten Klein (cklein05@users.sourceforge.net)
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
//  internal conversions X to 8 bits
// ----------------------------------------------------------

void DLL_CALLCONV
FreeImage_ConvertLine1To8(BYTE *target, BYTE *source, int width_in_pixels) {
	for (unsigned cols = 0; cols < (unsigned)width_in_pixels; cols++)
		target[cols] = (source[cols >> 3] & (0x80 >> (cols & 0x07))) != 0 ? 255 : 0;	
}

void DLL_CALLCONV
FreeImage_ConvertLine4To8(BYTE *target, BYTE *source, int width_in_pixels) {
	unsigned count_new = 0;
	unsigned count_org = 0;
	BOOL hinibble = TRUE;

	while (count_new < (unsigned)width_in_pixels) {
		if (hinibble) {
			target[count_new] = (source[count_org] >> 4);
		} else {
			target[count_new] = (source[count_org] & 0x0F);
			count_org++;
		}
		hinibble = !hinibble;
		count_new++;
	}
}

void DLL_CALLCONV
FreeImage_ConvertLine16To8_555(BYTE *target, BYTE *source, int width_in_pixels) {
	const WORD *const bits = (WORD *)source;
	for (unsigned cols = 0; cols < (unsigned)width_in_pixels; cols++) {
		target[cols] = GREY((((bits[cols] & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT) * 0xFF) / 0x1F,
			                (((bits[cols] & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT) * 0xFF) / 0x1F,
							(((bits[cols] & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT) * 0xFF) / 0x1F);
	}
}

void DLL_CALLCONV
FreeImage_ConvertLine16To8_565(BYTE *target, BYTE *source, int width_in_pixels) {
	const WORD *const bits = (WORD *)source;
	for (unsigned cols = 0; cols < (unsigned)width_in_pixels; cols++) {
		target[cols] = GREY((((bits[cols] & FI16_565_RED_MASK) >> FI16_565_RED_SHIFT) * 0xFF) / 0x1F,
			        (((bits[cols] & FI16_565_GREEN_MASK) >> FI16_565_GREEN_SHIFT) * 0xFF) / 0x3F,
					(((bits[cols] & FI16_565_BLUE_MASK) >> FI16_565_BLUE_SHIFT) * 0xFF) / 0x1F);
	}
}

void DLL_CALLCONV
FreeImage_ConvertLine24To8(BYTE *target, BYTE *source, int width_in_pixels) {
	for (unsigned cols = 0; cols < (unsigned)width_in_pixels; cols++) {
		target[cols] = GREY(source[FI_RGBA_RED], source[FI_RGBA_GREEN], source[FI_RGBA_BLUE]);
		source += 3;
	}
}

void DLL_CALLCONV
FreeImage_ConvertLine32To8(BYTE *target, BYTE *source, int width_in_pixels) {
	for (unsigned cols = 0; cols < (unsigned)width_in_pixels; cols++) {
		target[cols] = GREY(source[FI_RGBA_RED], source[FI_RGBA_GREEN], source[FI_RGBA_BLUE]);
		source += 4;
	}
}

// ----------------------------------------------------------
//   smart convert X to 8 bits
// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertTo8Bits(FIBITMAP *dib) {
	if (!FreeImage_HasPixels(dib)) {
		return NULL;
	}

	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	if (image_type != FIT_BITMAP && image_type != FIT_UINT16) {
		return NULL;
	}

	const unsigned bpp = FreeImage_GetBPP(dib);

	if (bpp != 8) {

		const unsigned width = FreeImage_GetWidth(dib);
		const unsigned height = FreeImage_GetHeight(dib);

		// Allocate a destination image
		FIBITMAP *new_dib = FreeImage_Allocate(width, height, 8);
		if (new_dib == NULL) {
			return NULL;
		}

		// Copy metadata from src to dst
		FreeImage_CloneMetadata(new_dib, dib);

		// Palette of destination image has already been initialized
		RGBQUAD *new_pal = FreeImage_GetPalette(new_dib);

		const FREE_IMAGE_COLOR_TYPE color_type = FreeImage_GetColorType(dib);

		if (image_type == FIT_BITMAP) {

			switch(bpp) {
				case 1:
				{
					if (color_type == FIC_PALETTE) {
						// Copy the palette
						RGBQUAD *old_pal = FreeImage_GetPalette(dib);
						new_pal[0] = old_pal[0];
						new_pal[255] = old_pal[1];

					} else if (color_type == FIC_MINISWHITE) {
						// Create a reverse grayscale palette
						CREATE_GREYSCALE_PALETTE_REVERSE(new_pal, 256);
					}

					// Expand and copy the bitmap data
					for (unsigned rows = 0; rows < height; rows++) {
						FreeImage_ConvertLine1To8(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);
					}
					return new_dib;
				}

				case 4 :
				{
					if (color_type == FIC_PALETTE) {
						// Copy the palette
						memcpy(new_pal, FreeImage_GetPalette(dib), 16 * sizeof(RGBQUAD));
					}

					// Expand and copy the bitmap data
					for (unsigned rows = 0; rows < height; rows++) {
						FreeImage_ConvertLine4To8(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);					
					}
					return new_dib;
				}

				case 16 :
				{
					// Expand and copy the bitmap data
					if (IS_FORMAT_RGB565(dib)) {
						for (unsigned rows = 0; rows < height; rows++) {
							FreeImage_ConvertLine16To8_565(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);
						}
					} else {
						for (unsigned rows = 0; rows < height; rows++) {
							FreeImage_ConvertLine16To8_555(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);
						}
					}
					return new_dib;
				}

				case 24 :
				{
					// Expand and copy the bitmap data
					for (unsigned rows = 0; rows < height; rows++) {
						FreeImage_ConvertLine24To8(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);					
					}
					return new_dib;
				}

				case 32 :
				{
					// Expand and copy the bitmap data
					for (unsigned rows = 0; rows < height; rows++) {
						FreeImage_ConvertLine32To8(FreeImage_GetScanLine(new_dib, rows), FreeImage_GetScanLine(dib, rows), width);
					}
					return new_dib;
				}
			}

		} else if (image_type == FIT_UINT16) {

			const unsigned src_pitch = FreeImage_GetPitch(dib);
			const unsigned dst_pitch = FreeImage_GetPitch(new_dib);
			const BYTE *src_bits = FreeImage_GetBits(dib);
			BYTE *dst_bits = FreeImage_GetBits(new_dib);

			for (unsigned rows = 0; rows < height; rows++) {
				const WORD *const src_pixel = (WORD*)src_bits;
				BYTE *dst_pixel = (BYTE*)dst_bits;
				for(unsigned cols = 0; cols < width; cols++) {
					dst_pixel[cols] = (BYTE)(src_pixel[cols] >> 8);
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
			return new_dib;
		} 

	} // bpp != 8

	return FreeImage_Clone(dib);
}

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertToGreyscale(FIBITMAP *dib) {
	if (!FreeImage_HasPixels(dib)) {
		return NULL;
	}

	const FREE_IMAGE_COLOR_TYPE color_type = FreeImage_GetColorType(dib);

	if (color_type == FIC_PALETTE || color_type == FIC_MINISWHITE) {

		const unsigned bpp = FreeImage_GetBPP(dib);
		const unsigned width  = FreeImage_GetWidth(dib);
		const unsigned height = FreeImage_GetHeight(dib);

		FIBITMAP *new_dib = FreeImage_Allocate(width, height, 8);
		if (new_dib == NULL) {
			return NULL;
		}

		// Copy metadata from src to dst
		FreeImage_CloneMetadata(new_dib, dib);

		// Create a greyscale palette
		BYTE grey_pal[256];
		const RGBQUAD *pal = FreeImage_GetPalette(dib);
		const unsigned size = CalculateUsedPaletteEntries(bpp);
		for (unsigned i = 0; i < size; i++) {
			grey_pal[i] = GREY(pal->rgbRed, pal->rgbGreen, pal->rgbBlue);
			pal++;
		}

		const BYTE *src_bits = FreeImage_GetBits(dib);
		BYTE *dst_bits = FreeImage_GetBits(new_dib);

		const unsigned src_pitch = FreeImage_GetPitch(dib);
		const unsigned dst_pitch = FreeImage_GetPitch(new_dib);

		switch(bpp) {
			case 1:
			{
				for (unsigned y = 0; y < height; y++) {
					for (unsigned x = 0; x < width; x++) {
						const unsigned pixel = (src_bits[x >> 3] & (0x80 >> (x & 0x07))) != 0;
						dst_bits[x] = grey_pal[pixel];
					}
					src_bits += src_pitch;
					dst_bits += dst_pitch;
				}
			}
			break;

			case 4:
			{
				for (unsigned y = 0; y < height; y++) {
					for (unsigned x = 0; x < width; x++) {
						const unsigned pixel = x & 0x01 ? src_bits[x >> 1] & 0x0F : src_bits[x >> 1] >> 4;
						dst_bits[x] = grey_pal[pixel];
					}
					src_bits += src_pitch;
					dst_bits += dst_pitch;
				}
			}
			break;

			case 8:
			{
				for (unsigned y = 0; y < height; y++) {
					for (unsigned x = 0; x < width; x++) {
						dst_bits[x] = grey_pal[src_bits[x]];
					}
					src_bits += src_pitch;
					dst_bits += dst_pitch;
				}
			}
			break;
		}
		return new_dib;
	} 
	
	// Convert the bitmap to 8-bit greyscale
	return FreeImage_ConvertTo8Bits(dib);
}

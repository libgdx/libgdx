// ==========================================================
// Bitmap conversion routines
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

#include "FreeImage.h"
#include "Utilities.h"

// ----------------------------------------------------------
//   smart convert X to RGB16
// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertToRGB16(FIBITMAP *dib) {
	FIBITMAP *src = NULL;
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	const FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(dib);

	// check for allowed conversions 
	switch(src_type) {
		case FIT_BITMAP:
		{
			// convert to 24-bit if needed
			if((FreeImage_GetBPP(dib) == 24) || (FreeImage_GetBPP(dib) == 32)) {
				src = dib;
			} else {
				src = FreeImage_ConvertTo24Bits(dib);
				if(!src) return NULL;
			}
			break;
		}
		case FIT_UINT16:
			// allow conversion from unsigned 16-bit
			src = dib;
			break;
		case FIT_RGB16:
			// RGB16 type : clone the src
			return FreeImage_Clone(dib);
			break;
		case FIT_RGBA16:
			// allow conversion from 64-bit RGBA (ignore the alpha channel)
			src = dib;
			break;
		default:
			return NULL;
	}

	// allocate dst image

	const unsigned width = FreeImage_GetWidth(src);
	const unsigned height = FreeImage_GetHeight(src);

	dst = FreeImage_AllocateT(FIT_RGB16, width, height);
	if(!dst) {
		if(src != dib) {
			FreeImage_Unload(src);
		}
		return NULL;
	}

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);

	// convert from src type to RGB16

	switch(src_type) {
		case FIT_BITMAP:
		{
			// Calculate the number of bytes per pixel (1 for 8-bit, 3 for 24-bit or 4 for 32-bit)
			const unsigned bytespp = FreeImage_GetLine(src) / FreeImage_GetWidth(src);

			for(unsigned y = 0; y < height; y++) {
				const BYTE *src_bits = (BYTE*)FreeImage_GetScanLine(src, y);
				FIRGB16 *dst_bits = (FIRGB16*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					dst_bits[x].red   = src_bits[FI_RGBA_RED] << 8;
					dst_bits[x].green = src_bits[FI_RGBA_GREEN] << 8;
					dst_bits[x].blue  = src_bits[FI_RGBA_BLUE] << 8;
					src_bits += bytespp;
				}
			}
		}
		break;

		case FIT_UINT16:
		{
			for(unsigned y = 0; y < height; y++) {
				const WORD *src_bits = (WORD*)FreeImage_GetScanLine(src, y);
				FIRGB16 *dst_bits = (FIRGB16*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					// convert by copying greyscale channel to each R, G, B channels
					dst_bits[x].red   = src_bits[x];
					dst_bits[x].green = src_bits[x];
					dst_bits[x].blue  = src_bits[x];
				}
			}
		}
		break;

		case FIT_RGBA16:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGBA16 *src_bits = (FIRGBA16*)FreeImage_GetScanLine(src, y);
				FIRGB16 *dst_bits = (FIRGB16*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					// convert and skip alpha channel
					dst_bits[x].red   = src_bits[x].red;
					dst_bits[x].green = src_bits[x].green;
					dst_bits[x].blue  = src_bits[x].blue;
				}
			}
		}
		break;

		default:
			break;
	}

	if(src != dib) {
		FreeImage_Unload(src);
	}

	return dst;
}


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
//   smart convert X to UINT16
// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertToUINT16(FIBITMAP *dib) {
	FIBITMAP *src = NULL;
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	const FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(dib);

	// check for allowed conversions 
	switch(src_type) {
		case FIT_BITMAP:
		{
			// convert to greyscale if needed
			if((FreeImage_GetBPP(dib) == 8) && (FreeImage_GetColorType(dib) == FIC_MINISBLACK)) {
				src = dib;
			} else {
				src = FreeImage_ConvertToGreyscale(dib);
				if(!src) return NULL;
			}
			break;
		}
		case FIT_UINT16:
			// UINT16 type : clone the src
			return FreeImage_Clone(dib);
			break;
		case FIT_RGB16:
			// allow conversion from 48-bit RGB
			src = dib;
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

	dst = FreeImage_AllocateT(FIT_UINT16, width, height);
	if(!dst) {
		if(src != dib) {
			FreeImage_Unload(src);
		}
		return NULL;
	}

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);

	// convert from src type to UINT16

	switch(src_type) {
		case FIT_BITMAP:
		{
			for(unsigned y = 0; y < height; y++) {
				const BYTE *src_bits = (BYTE*)FreeImage_GetScanLine(src, y);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					dst_bits[x] = src_bits[x] << 8;
				}
			}
		}
		break;

		case FIT_RGB16:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGB16 *src_bits = (FIRGB16*)FreeImage_GetScanLine(src, y);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					// convert to grey
					dst_bits[x] = (WORD)LUMA_REC709(src_bits[x].red, src_bits[x].green, src_bits[x].blue);
				}
			}
		}
		break;

		case FIT_RGBA16:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGBA16 *src_bits = (FIRGBA16*)FreeImage_GetScanLine(src, y);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);
				for(unsigned x = 0; x < width; x++) {
					// convert to grey
					dst_bits[x] = (WORD)LUMA_REC709(src_bits[x].red, src_bits[x].green, src_bits[x].blue);
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


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
//   smart convert X to Float
// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertToFloat(FIBITMAP *dib) {
	FIBITMAP *src = NULL;
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(dib);

	// check for allowed conversions 
	switch(src_type) {
		case FIT_BITMAP:
		{
			// allow conversion from 8-bit
			if((FreeImage_GetBPP(dib) == 8) && (FreeImage_GetColorType(dib) == FIC_MINISBLACK)) {
				src = dib;
			} else {
				src = FreeImage_ConvertToGreyscale(dib);
				if(!src) return NULL;
			}
			break;
		}
		case FIT_UINT16:
		case FIT_RGB16:
		case FIT_RGBA16:
		case FIT_RGBF:
		case FIT_RGBAF:
			src = dib;
			break;
		case FIT_FLOAT:
			// float type : clone the src
			return FreeImage_Clone(dib);
		default:
			return NULL;
	}

	// allocate dst image

	const unsigned width = FreeImage_GetWidth(src);
	const unsigned height = FreeImage_GetHeight(src);

	dst = FreeImage_AllocateT(FIT_FLOAT, width, height);
	if(!dst) {
		if(src != dib) {
			FreeImage_Unload(src);
		}
		return NULL;
	}

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);

	// convert from src type to float

	const unsigned src_pitch = FreeImage_GetPitch(src);
	const unsigned dst_pitch = FreeImage_GetPitch(dst);

	const BYTE *src_bits = (BYTE*)FreeImage_GetBits(src);
	BYTE *dst_bits = (BYTE*)FreeImage_GetBits(dst);

	switch(src_type) {
		case FIT_BITMAP:
		{
			for(unsigned y = 0; y < height; y++) {
				const BYTE *src_pixel = (BYTE*)src_bits;
				float *dst_pixel = (float*)dst_bits;
				for(unsigned x = 0; x < width; x++) {
					// convert and scale to the range [0..1]
					dst_pixel[x] = (float)(src_pixel[x]) / 255;
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;

		case FIT_UINT16:
		{
			for(unsigned y = 0; y < height; y++) {
				const WORD *src_pixel = (WORD*)src_bits;
				float *dst_pixel = (float*)dst_bits;

				for(unsigned x = 0; x < width; x++) {
					// convert and scale to the range [0..1]
					dst_pixel[x] = (float)(src_pixel[x]) / 65535;
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;

		case FIT_RGB16:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGB16 *src_pixel = (FIRGB16*)src_bits;
				float *dst_pixel = (float*)dst_bits;

				for(unsigned x = 0; x < width; x++) {
					// convert and scale to the range [0..1]
					dst_pixel[x] = LUMA_REC709(src_pixel[x].red, src_pixel[x].green, src_pixel[x].blue) / 65535.0F;
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;

		case FIT_RGBA16:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGBA16 *src_pixel = (FIRGBA16*)src_bits;
				float *dst_pixel = (float*)dst_bits;

				for(unsigned x = 0; x < width; x++) {
					// convert and scale to the range [0..1]
					dst_pixel[x] = LUMA_REC709(src_pixel[x].red, src_pixel[x].green, src_pixel[x].blue) / 65535.0F;
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;

		case FIT_RGBF:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGBF *src_pixel = (FIRGBF*)src_bits;
				float *dst_pixel = (float*)dst_bits;

				for(unsigned x = 0; x < width; x++) {
					// convert (assume pixel values are in the range [0..1])
					dst_pixel[x] = LUMA_REC709(src_pixel[x].red, src_pixel[x].green, src_pixel[x].blue);
					dst_pixel[x] = CLAMP(dst_pixel[x], 0.0F, 1.0F);
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;

		case FIT_RGBAF:
		{
			for(unsigned y = 0; y < height; y++) {
				const FIRGBAF *src_pixel = (FIRGBAF*)src_bits;
				float *dst_pixel = (float*)dst_bits;

				for(unsigned x = 0; x < width; x++) {
					// convert (assume pixel values are in the range [0..1])
					dst_pixel[x] = LUMA_REC709(src_pixel[x].red, src_pixel[x].green, src_pixel[x].blue);
					dst_pixel[x] = CLAMP(dst_pixel[x], 0.0F, 1.0F);
				}
				src_bits += src_pitch;
				dst_bits += dst_pitch;
			}
		}
		break;
	}

	if(src != dib) {
		FreeImage_Unload(src);
	}

	return dst;
}


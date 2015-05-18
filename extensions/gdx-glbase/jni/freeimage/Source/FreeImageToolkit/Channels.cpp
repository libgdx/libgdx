// ==========================================================
// Channel processing support
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


/** @brief Retrieves the red, green, blue or alpha channel of a BGR[A] image. 
@param src Input image to be processed.
@param channel Color channel to extract
@return Returns the extracted channel if successful, returns NULL otherwise.
*/
FIBITMAP * DLL_CALLCONV 
FreeImage_GetChannel(FIBITMAP *src, FREE_IMAGE_COLOR_CHANNEL channel) {

	if(!FreeImage_HasPixels(src)) return NULL;

	FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(src);
	unsigned bpp = FreeImage_GetBPP(src);

	// 24- or 32-bit 
	if(image_type == FIT_BITMAP && ((bpp == 24) || (bpp == 32))) {
		int c;

		// select the channel to extract
		switch(channel) {
			case FICC_BLUE:
				c = FI_RGBA_BLUE;
				break;
			case FICC_GREEN:
				c = FI_RGBA_GREEN;
				break;
			case FICC_RED: 
				c = FI_RGBA_RED;
				break;
			case FICC_ALPHA:
				if(bpp != 32) return NULL;
				c = FI_RGBA_ALPHA;
				break;
			default:
				return NULL;
		}

		// allocate a 8-bit dib
		unsigned width  = FreeImage_GetWidth(src);
		unsigned height = FreeImage_GetHeight(src);
		FIBITMAP *dst = FreeImage_Allocate(width, height, 8) ;
		if(!dst) return NULL;
		// build a greyscale palette
		RGBQUAD *pal = FreeImage_GetPalette(dst);
		for(int i = 0; i < 256; i++) {
			pal[i].rgbBlue = pal[i].rgbGreen = pal[i].rgbRed = (BYTE)i;
		}

		// perform extraction

		int bytespp = bpp / 8;	// bytes / pixel

		for(unsigned y = 0; y < height; y++) {
			BYTE *src_bits = FreeImage_GetScanLine(src, y);
			BYTE *dst_bits = FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < width; x++) {
				dst_bits[x] = src_bits[c];
				src_bits += bytespp;
			}
		}

		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, src);
		
		return dst;
	}

	// 48-bit RGB or 64-bit RGBA images
	if((image_type == FIT_RGB16) ||  (image_type == FIT_RGBA16)) {
		int c;

		// select the channel to extract (always RGB[A])
		switch(channel) {
			case FICC_BLUE:
				c = 2;
				break;
			case FICC_GREEN:
				c = 1;
				break;
			case FICC_RED: 
				c = 0;
				break;
			case FICC_ALPHA:
				if(bpp != 64) return NULL;
				c = 3;
				break;
			default:
				return NULL;
		}

		// allocate a greyscale dib
		unsigned width  = FreeImage_GetWidth(src);
		unsigned height = FreeImage_GetHeight(src);
		FIBITMAP *dst = FreeImage_AllocateT(FIT_UINT16, width, height) ;
		if(!dst) return NULL;

		// perform extraction

		int bytespp = bpp / 16;	// words / pixel

		for(unsigned y = 0; y < height; y++) {
			unsigned short *src_bits = (unsigned short*)FreeImage_GetScanLine(src, y);
			unsigned short *dst_bits = (unsigned short*)FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < width; x++) {
				dst_bits[x] = src_bits[c];
				src_bits += bytespp;
			}
		}

		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, src);
		
		return dst;
	}

	// 96-bit RGBF or 128-bit RGBAF images
	if((image_type == FIT_RGBF) ||  (image_type == FIT_RGBAF)) {
		int c;

		// select the channel to extract (always RGB[A])
		switch(channel) {
			case FICC_BLUE:
				c = 2;
				break;
			case FICC_GREEN:
				c = 1;
				break;
			case FICC_RED: 
				c = 0;
				break;
			case FICC_ALPHA:
				if(bpp != 128) return NULL;
				c = 3;
				break;
			default:
				return NULL;
		}

		// allocate a greyscale dib
		unsigned width  = FreeImage_GetWidth(src);
		unsigned height = FreeImage_GetHeight(src);
		FIBITMAP *dst = FreeImage_AllocateT(FIT_FLOAT, width, height) ;
		if(!dst) return NULL;

		// perform extraction

		int bytespp = bpp / 32;	// floats / pixel

		for(unsigned y = 0; y < height; y++) {
			float *src_bits = (float*)FreeImage_GetScanLine(src, y);
			float *dst_bits = (float*)FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < width; x++) {
				dst_bits[x] = src_bits[c];
				src_bits += bytespp;
			}
		}

		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, src);
		
		return dst;
	}

	return NULL;
}

/** @brief Insert a greyscale dib into a RGB[A] image. 
Both src and dst must have the same width and height.
@param dst Image to modify (RGB or RGBA)
@param src Input greyscale image to insert
@param channel Color channel to modify
@return Returns TRUE if successful, FALSE otherwise.
*/
BOOL DLL_CALLCONV 
FreeImage_SetChannel(FIBITMAP *dst, FIBITMAP *src, FREE_IMAGE_COLOR_CHANNEL channel) {
	int c;

	if(!FreeImage_HasPixels(src) || !FreeImage_HasPixels(dst)) return FALSE;
	
	// src and dst images should have the same width and height
	unsigned src_width  = FreeImage_GetWidth(src);
	unsigned src_height = FreeImage_GetHeight(src);
	unsigned dst_width  = FreeImage_GetWidth(dst);
	unsigned dst_height = FreeImage_GetHeight(dst);
	if((src_width != dst_width) || (src_height != dst_height))
		return FALSE;

	// src image should be grayscale, dst image should be RGB or RGBA
	FREE_IMAGE_COLOR_TYPE src_type = FreeImage_GetColorType(src);
	FREE_IMAGE_COLOR_TYPE dst_type = FreeImage_GetColorType(dst);
	if((dst_type != FIC_RGB) && (dst_type != FIC_RGBALPHA) || (src_type != FIC_MINISBLACK)) {
		return FALSE;
	}

	FREE_IMAGE_TYPE src_image_type = FreeImage_GetImageType(src);
	FREE_IMAGE_TYPE dst_image_type = FreeImage_GetImageType(dst);

	if((dst_image_type == FIT_BITMAP) && (src_image_type == FIT_BITMAP)) {

		// src image should be grayscale, dst image should be 24- or 32-bit
		unsigned src_bpp = FreeImage_GetBPP(src);
		unsigned dst_bpp = FreeImage_GetBPP(dst);
		if((src_bpp != 8) || (dst_bpp != 24) && (dst_bpp != 32))
			return FALSE;


		// select the channel to modify
		switch(channel) {
			case FICC_BLUE:
				c = FI_RGBA_BLUE;
				break;
			case FICC_GREEN:
				c = FI_RGBA_GREEN;
				break;
			case FICC_RED: 
				c = FI_RGBA_RED;
				break;
			case FICC_ALPHA:
				if(dst_bpp != 32) return FALSE;
				c = FI_RGBA_ALPHA;
				break;
			default:
				return FALSE;
		}

		// perform insertion

		int bytespp = dst_bpp / 8;	// bytes / pixel

		for(unsigned y = 0; y < dst_height; y++) {
			BYTE *src_bits = FreeImage_GetScanLine(src, y);
			BYTE *dst_bits = FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < dst_width; x++) {
				dst_bits[c] = src_bits[x];
				dst_bits += bytespp;
			}
		}

		return TRUE;
	}

	if(((dst_image_type == FIT_RGB16) || (dst_image_type == FIT_RGBA16)) && (src_image_type == FIT_UINT16)) {

		// src image should be grayscale, dst image should be 48- or 64-bit
		unsigned src_bpp = FreeImage_GetBPP(src);
		unsigned dst_bpp = FreeImage_GetBPP(dst);
		if((src_bpp != 16) || (dst_bpp != 48) && (dst_bpp != 64))
			return FALSE;


		// select the channel to modify (always RGB[A])
		switch(channel) {
			case FICC_BLUE:
				c = 2;
				break;
			case FICC_GREEN:
				c = 1;
				break;
			case FICC_RED: 
				c = 0;
				break;
			case FICC_ALPHA:
				if(dst_bpp != 64) return FALSE;
				c = 3;
				break;
			default:
				return FALSE;
		}

		// perform insertion

		int bytespp = dst_bpp / 16;	// words / pixel

		for(unsigned y = 0; y < dst_height; y++) {
			unsigned short *src_bits = (unsigned short*)FreeImage_GetScanLine(src, y);
			unsigned short *dst_bits = (unsigned short*)FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < dst_width; x++) {
				dst_bits[c] = src_bits[x];
				dst_bits += bytespp;
			}
		}

		return TRUE;
	}
	
	if(((dst_image_type == FIT_RGBF) || (dst_image_type == FIT_RGBAF)) && (src_image_type == FIT_FLOAT)) {

		// src image should be grayscale, dst image should be 96- or 128-bit
		unsigned src_bpp = FreeImage_GetBPP(src);
		unsigned dst_bpp = FreeImage_GetBPP(dst);
		if((src_bpp != 32) || (dst_bpp != 96) && (dst_bpp != 128))
			return FALSE;


		// select the channel to modify (always RGB[A])
		switch(channel) {
			case FICC_BLUE:
				c = 2;
				break;
			case FICC_GREEN:
				c = 1;
				break;
			case FICC_RED: 
				c = 0;
				break;
			case FICC_ALPHA:
				if(dst_bpp != 128) return FALSE;
				c = 3;
				break;
			default:
				return FALSE;
		}

		// perform insertion

		int bytespp = dst_bpp / 32;	// floats / pixel

		for(unsigned y = 0; y < dst_height; y++) {
			float *src_bits = (float*)FreeImage_GetScanLine(src, y);
			float *dst_bits = (float*)FreeImage_GetScanLine(dst, y);
			for(unsigned x = 0; x < dst_width; x++) {
				dst_bits[c] = src_bits[x];
				dst_bits += bytespp;
			}
		}

		return TRUE;
	}

	return FALSE;
}

/** @brief Retrieves the real part, imaginary part, magnitude or phase of a complex image.
@param src Input image to be processed.
@param channel Channel to extract
@return Returns the extracted channel if successful, returns NULL otherwise.
*/
FIBITMAP * DLL_CALLCONV 
FreeImage_GetComplexChannel(FIBITMAP *src, FREE_IMAGE_COLOR_CHANNEL channel) {
	unsigned x, y;
	double mag, phase;
	FICOMPLEX *src_bits = NULL;
	double *dst_bits = NULL;
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(src)) return NULL;

	if(FreeImage_GetImageType(src) == FIT_COMPLEX) {
		// allocate a dib of type FIT_DOUBLE
		unsigned width  = FreeImage_GetWidth(src);
		unsigned height = FreeImage_GetHeight(src);
		dst = FreeImage_AllocateT(FIT_DOUBLE, width, height) ;
		if(!dst) return NULL;

		// perform extraction

		switch(channel) {
			case FICC_REAL: // real part
				for(y = 0; y < height; y++) {
					src_bits = (FICOMPLEX *)FreeImage_GetScanLine(src, y);
					dst_bits = (double *)FreeImage_GetScanLine(dst, y);
					for(x = 0; x < width; x++) {
						dst_bits[x] = src_bits[x].r;
					}
				}
				break;

			case FICC_IMAG: // imaginary part
				for(y = 0; y < height; y++) {
					src_bits = (FICOMPLEX *)FreeImage_GetScanLine(src, y);
					dst_bits = (double *)FreeImage_GetScanLine(dst, y);
					for(x = 0; x < width; x++) {
						dst_bits[x] = src_bits[x].i;
					}
				}
				break;

			case FICC_MAG: // magnitude
				for(y = 0; y < height; y++) {
					src_bits = (FICOMPLEX *)FreeImage_GetScanLine(src, y);
					dst_bits = (double *)FreeImage_GetScanLine(dst, y);
					for(x = 0; x < width; x++) {
						mag = src_bits[x].r * src_bits[x].r + src_bits[x].i * src_bits[x].i;
						dst_bits[x] = sqrt(mag);
					}
				}
				break;

			case FICC_PHASE: // phase
				for(y = 0; y < height; y++) {
					src_bits = (FICOMPLEX *)FreeImage_GetScanLine(src, y);
					dst_bits = (double *)FreeImage_GetScanLine(dst, y);
					for(x = 0; x < width; x++) {
						if((src_bits[x].r == 0) && (src_bits[x].i == 0)) {
							phase = 0;
						} else {
							phase = atan2(src_bits[x].i, src_bits[x].r);
						}
						dst_bits[x] = phase;
					}
				}
				break;
		}
	}

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);
	
	return dst;
}

/** @brief Set the real or imaginary part of a complex image.
Both src and dst must have the same width and height.
@param dst Image to modify (image of type FIT_COMPLEX)
@param src Input image of type FIT_DOUBLE
@param channel Channel to modify
@return Returns TRUE if successful, FALSE otherwise.
*/
BOOL DLL_CALLCONV 
FreeImage_SetComplexChannel(FIBITMAP *dst, FIBITMAP *src, FREE_IMAGE_COLOR_CHANNEL channel) {
	unsigned x, y;
	double *src_bits = NULL;
	FICOMPLEX *dst_bits = NULL;

	if(!FreeImage_HasPixels(src) || !FreeImage_HasPixels(dst)) return FALSE;

	// src image should be of type FIT_DOUBLE, dst image should be of type FIT_COMPLEX
	const FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(src);
	const FREE_IMAGE_TYPE dst_type = FreeImage_GetImageType(dst);
	if((src_type != FIT_DOUBLE) || (dst_type != FIT_COMPLEX))
		return FALSE;

	// src and dst images should have the same width and height
	unsigned src_width  = FreeImage_GetWidth(src);
	unsigned src_height = FreeImage_GetHeight(src);
	unsigned dst_width  = FreeImage_GetWidth(dst);
	unsigned dst_height = FreeImage_GetHeight(dst);
	if((src_width != dst_width) || (src_height != dst_height))
		return FALSE;

	// select the channel to modify
	switch(channel) {
		case FICC_REAL: // real part
			for(y = 0; y < dst_height; y++) {
				src_bits = (double *)FreeImage_GetScanLine(src, y);
				dst_bits = (FICOMPLEX *)FreeImage_GetScanLine(dst, y);
				for(x = 0; x < dst_width; x++) {
					dst_bits[x].r = src_bits[x];
				}
			}
			break;
		case FICC_IMAG: // imaginary part
			for(y = 0; y < dst_height; y++) {
				src_bits = (double *)FreeImage_GetScanLine(src, y);
				dst_bits = (FICOMPLEX *)FreeImage_GetScanLine(dst, y);
				for(x = 0; x < dst_width; x++) {
					dst_bits[x].i = src_bits[x];
				}
			}
			break;
	}

	return TRUE;
}

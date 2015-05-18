// ==========================================================
// Pixel access functions
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Hervé Drolon (drolon@infonie.fr)
// - Ryan Rubley (ryan@lostreality.org)
// - Riley McNiff (rmcniff@marexgroup.com)
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

BYTE * DLL_CALLCONV
FreeImage_GetBits(FIBITMAP *dib) {
	if(!FreeImage_HasPixels(dib)) {
		return NULL;
	}
	// returns the pixels aligned on a FIBITMAP_ALIGNMENT bytes alignment boundary
	size_t lp = (size_t)FreeImage_GetInfoHeader(dib);
	lp += sizeof(BITMAPINFOHEADER) + sizeof(RGBQUAD) * FreeImage_GetColorsUsed(dib);
	lp += FreeImage_HasRGBMasks(dib) ? sizeof(DWORD) * 3 : 0;
	lp += (lp % FIBITMAP_ALIGNMENT ? FIBITMAP_ALIGNMENT - lp % FIBITMAP_ALIGNMENT : 0);
	return (BYTE *)lp;
}

BYTE * DLL_CALLCONV
FreeImage_GetScanLine(FIBITMAP *dib, int scanline) {
	if(!FreeImage_HasPixels(dib)) {
		return NULL;
	}
	return CalculateScanLine(FreeImage_GetBits(dib), FreeImage_GetPitch(dib), scanline);
}

BOOL DLL_CALLCONV
FreeImage_GetPixelIndex(FIBITMAP *dib, unsigned x, unsigned y, BYTE *value) {
	BYTE shift;

	if(!FreeImage_HasPixels(dib) || (FreeImage_GetImageType(dib) != FIT_BITMAP))
		return FALSE;

	if((x < FreeImage_GetWidth(dib)) && (y < FreeImage_GetHeight(dib))) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);

		switch(FreeImage_GetBPP(dib)) {
			case 1:
				*value = (bits[x >> 3] & (0x80 >> (x & 0x07))) != 0;
				break;
			case 4:
				shift = (BYTE)((1 - x % 2) << 2);
				*value = (bits[x >> 1] & (0x0F << shift)) >> shift;
				break;
			case 8:
				*value = bits[x];
				break;
			default:
				return FALSE;
		}

		return TRUE;
	}

	return FALSE;
}

BOOL DLL_CALLCONV
FreeImage_GetPixelColor(FIBITMAP *dib, unsigned x, unsigned y, RGBQUAD *value) {
	if(!FreeImage_HasPixels(dib) || (FreeImage_GetImageType(dib) != FIT_BITMAP))
		return FALSE;

	if((x < FreeImage_GetWidth(dib)) && (y < FreeImage_GetHeight(dib))) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);

		switch(FreeImage_GetBPP(dib)) {
			case 16:
			{
				bits += 2*x;
				WORD *pixel = (WORD *)bits;
				if((FreeImage_GetRedMask(dib) == FI16_565_RED_MASK) && (FreeImage_GetGreenMask(dib) == FI16_565_GREEN_MASK) && (FreeImage_GetBlueMask(dib) == FI16_565_BLUE_MASK)) {
					value->rgbBlue		= (BYTE)((((*pixel & FI16_565_BLUE_MASK) >> FI16_565_BLUE_SHIFT) * 0xFF) / 0x1F);
					value->rgbGreen		= (BYTE)((((*pixel & FI16_565_GREEN_MASK) >> FI16_565_GREEN_SHIFT) * 0xFF) / 0x3F);
					value->rgbRed		= (BYTE)((((*pixel & FI16_565_RED_MASK) >> FI16_565_RED_SHIFT) * 0xFF) / 0x1F);
					value->rgbReserved	= 0;
				} else {
					value->rgbBlue		= (BYTE)((((*pixel & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT) * 0xFF) / 0x1F);
					value->rgbGreen		= (BYTE)((((*pixel & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT) * 0xFF) / 0x1F);
					value->rgbRed		= (BYTE)((((*pixel & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT) * 0xFF) / 0x1F);
					value->rgbReserved	= 0;
				}
				break;
			}
			case 24:
				bits += 3*x;
				value->rgbBlue		= bits[FI_RGBA_BLUE];	// B
				value->rgbGreen		= bits[FI_RGBA_GREEN];	// G
				value->rgbRed		= bits[FI_RGBA_RED];	// R
				value->rgbReserved	= 0;
				break;
			case 32:
				bits += 4*x;
				value->rgbBlue		= bits[FI_RGBA_BLUE];	// B
				value->rgbGreen		= bits[FI_RGBA_GREEN];	// G
				value->rgbRed		= bits[FI_RGBA_RED];	// R
				value->rgbReserved	= bits[FI_RGBA_ALPHA];	// A
				break;
			default:
				return FALSE;
		}

		return TRUE;
	}

	return FALSE;
}

BOOL DLL_CALLCONV
FreeImage_SetPixelIndex(FIBITMAP *dib, unsigned x, unsigned y, BYTE *value) {
	BYTE shift;

	if(!FreeImage_HasPixels(dib) || (FreeImage_GetImageType(dib) != FIT_BITMAP))
		return FALSE;

	if((x < FreeImage_GetWidth(dib)) && (y < FreeImage_GetHeight(dib))) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);

		switch(FreeImage_GetBPP(dib)) {
			case 1:
				*value ? bits[x >> 3] |= (0x80 >> (x & 0x7)) : bits[x >> 3] &= (0xFF7F >> (x & 0x7));
				break;
			case 4:
				shift = (BYTE)((1 - x % 2) << 2);
				bits[x >> 1] &= ~(0x0F << shift);
				bits[x >> 1] |= ((*value & 0x0F) << shift);
				break;
			case 8:
				bits[x] = *value;
				break;
			default:
				return FALSE;
		}

		return TRUE;
	}

	return FALSE;
}

BOOL DLL_CALLCONV
FreeImage_SetPixelColor(FIBITMAP *dib, unsigned x, unsigned y, RGBQUAD *value) {
	if(!FreeImage_HasPixels(dib) || (FreeImage_GetImageType(dib) != FIT_BITMAP))
		return FALSE;

	if((x < FreeImage_GetWidth(dib)) && (y < FreeImage_GetHeight(dib))) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);

		switch(FreeImage_GetBPP(dib)) {
			case 16:
			{
				bits += 2*x;
				WORD *pixel = (WORD *)bits;
				if((FreeImage_GetRedMask(dib) == FI16_565_RED_MASK) && (FreeImage_GetGreenMask(dib) == FI16_565_GREEN_MASK) && (FreeImage_GetBlueMask(dib) == FI16_565_BLUE_MASK)) {
					*pixel = ((value->rgbBlue >> 3) << FI16_565_BLUE_SHIFT) |
						((value->rgbGreen >> 2) << FI16_565_GREEN_SHIFT) |
						((value->rgbRed >> 3) << FI16_565_RED_SHIFT);
				} else {
					*pixel = ((value->rgbBlue >> 3) << FI16_555_BLUE_SHIFT) |
						((value->rgbGreen >> 3) << FI16_555_GREEN_SHIFT) |
						((value->rgbRed >> 3) << FI16_555_RED_SHIFT);
				}
				break;
			}
			case 24:
				bits += 3*x;
				bits[FI_RGBA_BLUE]	= value->rgbBlue;	// B
				bits[FI_RGBA_GREEN] = value->rgbGreen;	// G
				bits[FI_RGBA_RED]	= value->rgbRed;	// R
				break;
			case 32:
				bits += 4*x;
				bits[FI_RGBA_BLUE]	= value->rgbBlue;		// B
				bits[FI_RGBA_GREEN] = value->rgbGreen;		// G
				bits[FI_RGBA_RED]	= value->rgbRed;		// R
				bits[FI_RGBA_ALPHA] = value->rgbReserved;	// A
				break;
			default:
				return FALSE;
		}

		return TRUE;
	}

	return FALSE;
}


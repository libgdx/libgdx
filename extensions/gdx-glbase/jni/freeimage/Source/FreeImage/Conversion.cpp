// ==========================================================
// Bitmap conversion routines
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Hervé Drolon (drolon@infonie.fr)
// - Jani Kajala (janik@remedy.fi)
// - Mihail Naydenov (mnaydenov@users.sourceforge.net)
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
#include "Quantizers.h"

// ----------------------------------------------------------

#define CONVERT(from, to) case to : FreeImage_ConvertLine##from##To##to(bits, scanline, FreeImage_GetWidth(dib)); break;
#define CONVERTWITHPALETTE(from, to) case to : FreeImage_ConvertLine##from##To##to(bits, scanline, FreeImage_GetWidth(dib), FreeImage_GetPalette(dib)); break;

#define CONVERTTO16(from) \
	case 16 : \
		if ((red_mask == FI16_555_RED_MASK) && (green_mask == FI16_555_GREEN_MASK) && (blue_mask == FI16_555_BLUE_MASK)) { \
			FreeImage_ConvertLine##from##To16_555(bits, scanline, FreeImage_GetWidth(dib)); \
		} else { \
			FreeImage_ConvertLine##from##To16_565(bits, scanline, FreeImage_GetWidth(dib)); \
		} \
		break;

#define CONVERTTO16WITHPALETTE(from) \
	case 16 : \
		if ((red_mask == FI16_555_RED_MASK) && (green_mask == FI16_555_GREEN_MASK) && (blue_mask == FI16_555_BLUE_MASK)) { \
			FreeImage_ConvertLine##from##To16_555(bits, scanline, FreeImage_GetWidth(dib), FreeImage_GetPalette(dib)); \
		} else { \
			FreeImage_ConvertLine##from##To16_565(bits, scanline, FreeImage_GetWidth(dib), FreeImage_GetPalette(dib)); \
		} \
		break;

// ==========================================================
// Utility functions declared in Utilities.h

BOOL SwapRedBlue32(FIBITMAP* dib) {
	if(FreeImage_GetImageType(dib) != FIT_BITMAP) {
		return FALSE;
	}
		
	const unsigned bytesperpixel = FreeImage_GetBPP(dib) / 8;
	if(bytesperpixel > 4 || bytesperpixel < 3) {
		return FALSE;
	}
		
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch = FreeImage_GetPitch(dib);
	const unsigned lineSize = FreeImage_GetLine(dib);
	
	BYTE* line = FreeImage_GetBits(dib);
	for(unsigned y = 0; y < height; ++y, line += pitch) {
		for(BYTE* pixel = line; pixel < line + lineSize ; pixel += bytesperpixel) {
			INPLACESWAP(pixel[0], pixel[2]);
		}
	}
	
	return TRUE;
}

// ----------------------------------------------------------

static inline void 
assignRGB(WORD r, WORD g, WORD b, WORD* out) {
	out[0] = r;
	out[1] = g;
	out[2] = b;
}

static inline void 
assignRGB(BYTE r, BYTE g, BYTE b, BYTE* out) {
	out[FI_RGBA_RED]	= r;
	out[FI_RGBA_GREEN]	= g;
	out[FI_RGBA_BLUE]	= b;
}

/**
CMYK -> CMY -> RGB conversion from http://www.easyrgb.com/

CMYK to CMY [0-1]: C,M,Y * (1 - K) + K
CMY to RGB [0-1]: (1 - C,M,Y)

=> R,G,B = (1 - C,M,Y) * (1 - K)
mapped to [0-MAX_VAL]: 
(MAX_VAL - C,M,Y) * (MAX_VAL - K) / MAX_VAL
*/
template <class T>
static inline void 
CMYKToRGB(T C, T M, T Y, T K, T* out) {
	unsigned max_val = std::numeric_limits<T>::max();
	
	unsigned r = (max_val - C) * (max_val - K) / max_val;
	unsigned g = (max_val - M) * (max_val - K) / max_val;
	unsigned b = (max_val - Y) * (max_val - K) / max_val;

	// clamp values to [0..max_val]
	T red	= (T)CLAMP(r, (unsigned)0, max_val);
	T green	= (T)CLAMP(g, (unsigned)0, max_val);
	T blue	= (T)CLAMP(b, (unsigned)0, max_val);

	assignRGB(red, green, blue, out);
}

template <class T>
static void 
_convertCMYKtoRGBA(unsigned width, unsigned height, BYTE* line_start, unsigned pitch, unsigned samplesperpixel) {
	const BOOL hasBlack = (samplesperpixel > 3) ? TRUE : FALSE;
	const T MAX_VAL = std::numeric_limits<T>::max();
		
	T K = 0;
	for(unsigned y = 0; y < height; y++) {
		T *line = (T*)line_start;

		for(unsigned x = 0; x < width; x++) {
			if(hasBlack) {
				K = line[FI_RGBA_ALPHA];			
				line[FI_RGBA_ALPHA] = MAX_VAL; // TODO write the first extra channel as alpha!
			}			
			
			CMYKToRGB<T>(line[0], line[1], line[2], K, line);
			
			line += samplesperpixel;
		}
		line_start += pitch;
	}
}

BOOL 
ConvertCMYKtoRGBA(FIBITMAP* dib) {
	if(!FreeImage_HasPixels(dib)) {
		return FALSE;
	}
		
	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	const unsigned bytesperpixel = FreeImage_GetBPP(dib)/8;
	
	unsigned channelSize = 1;
	if (image_type == FIT_RGBA16 || image_type == FIT_RGB16) {
		channelSize = sizeof(WORD);
	} else if (!(image_type == FIT_BITMAP && (bytesperpixel > 2))) {
		return FALSE;
	}
				
	const unsigned width = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	BYTE *line_start = FreeImage_GetScanLine(dib, 0);
	const unsigned pitch = FreeImage_GetPitch(dib);
	
	unsigned samplesperpixel = FreeImage_GetLine(dib) / width / channelSize;

	if(channelSize == sizeof(WORD)) {
		_convertCMYKtoRGBA<WORD>(width, height, line_start, pitch, samplesperpixel);
	} else {
		_convertCMYKtoRGBA<BYTE>(width, height, line_start, pitch, samplesperpixel);
	}

	return TRUE;	
}

// ----------------------------------------------------------

/**
CIELab -> XYZ conversion from http://www.easyrgb.com/
*/
static void 
CIELabToXYZ(float L, float a, float b, float *X, float *Y, float *Z) {
	float pow_3;
	
	// CIELab -> XYZ conversion 
	// ------------------------
	float var_Y = (L + 16.F ) / 116.F;
	float var_X = a / 500.F + var_Y;
	float var_Z = var_Y - b / 200.F;

	pow_3 = powf(var_Y, 3);
	if(pow_3 > 0.008856F) {
		var_Y = pow_3;
	} else {
		var_Y = ( var_Y - 16.F / 116.F ) / 7.787F;
	}
	pow_3 = powf(var_X, 3);
	if(pow_3 > 0.008856F) {
		var_X = pow_3;
	} else {
		var_X = ( var_X - 16.F / 116.F ) / 7.787F;
	}
	pow_3 = powf(var_Z, 3);
	if(pow_3 > 0.008856F) {
		var_Z = pow_3;
	} else {
		var_Z = ( var_Z - 16.F / 116.F ) / 7.787F;
	}

	static const float ref_X =  95.047F;
	static const float ref_Y = 100.000F;
	static const float ref_Z = 108.883F;

	*X = ref_X * var_X;	// ref_X = 95.047 (Observer= 2°, Illuminant= D65)
	*Y = ref_Y * var_Y;	// ref_Y = 100.000
	*Z = ref_Z * var_Z;	// ref_Z = 108.883
}

/**
XYZ -> RGB conversion from http://www.easyrgb.com/
*/
static void 
XYZToRGB(float X, float Y, float Z, float *R, float *G, float *B) {
	float var_X = X / 100; // X from 0 to  95.047 (Observer = 2°, Illuminant = D65)
	float var_Y = Y / 100; // Y from 0 to 100.000
	float var_Z = Z / 100; // Z from 0 to 108.883

	float var_R = var_X *  3.2406F + var_Y * -1.5372F + var_Z * -0.4986F;
	float var_G = var_X * -0.9689F + var_Y *  1.8758F + var_Z *  0.0415F;
	float var_B = var_X *  0.0557F + var_Y * -0.2040F + var_Z *  1.0570F;

	float exponent = 1.F / 2.4F;

	if(var_R > 0.0031308F) {
		var_R = 1.055F * powf(var_R, exponent) - 0.055F;
	} else {
		var_R = 12.92F * var_R;
	}
	if(var_G > 0.0031308F) {
		var_G = 1.055F * powf(var_G, exponent) - 0.055F;
	} else {
		var_G = 12.92F * var_G;
	}
	if(var_B > 0.0031308F) {
		var_B = 1.055F * powf(var_B, exponent) - 0.055F;
	} else {
		var_B = 12.92F * var_B;
	}

	*R = var_R;
	*G = var_G;
	*B = var_B;
}

template<class T>
static void 
CIELabToRGB(float L, float a, float b, T *rgb) {
	float X, Y, Z;
	float R, G, B;
	const float max_val = std::numeric_limits<T>::max();

	CIELabToXYZ(L, a, b, &X, &Y, &Z);
	XYZToRGB(X, Y, Z, &R, &G, &B);
	
	// clamp values to [0..max_val]
	T red	= (T)CLAMP(R * max_val, 0.0F, max_val);
	T green	= (T)CLAMP(G * max_val, 0.0F, max_val);
	T blue	= (T)CLAMP(B * max_val, 0.0F, max_val);

	assignRGB(red, green, blue, rgb);
}

template<class T>
static void 
_convertLABtoRGB(unsigned width, unsigned height, BYTE* line_start, unsigned pitch, unsigned samplesperpixel) {
	const unsigned max_val = std::numeric_limits<T>::max();
	const float sL = 100.F / max_val;
	const float sa = 256.F / max_val;
	const float sb = 256.F / max_val;
	
	for(unsigned y = 0; y < height; y++) {
		T *line = (T*)line_start;

		for(unsigned x = 0; x < width; x++) {
			CIELabToRGB(line[0]* sL, line[1]* sa - 128.F, line[2]* sb - 128.F, line);
			
			line += samplesperpixel;
		}
		line_start += pitch;
	}
}

BOOL
ConvertLABtoRGB(FIBITMAP* dib) {
	if(!FreeImage_HasPixels(dib)) {
		return FALSE;
	}
		
	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	const unsigned bytesperpixel = FreeImage_GetBPP(dib) / 8;
	
	unsigned channelSize = 1;
	if (image_type == FIT_RGBA16 || image_type == FIT_RGB16) {
		channelSize = sizeof(WORD);
	} else if (!(image_type == FIT_BITMAP && (bytesperpixel > 2))) {
		return FALSE;
	}
				
	const unsigned width = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	BYTE *line_start = FreeImage_GetScanLine(dib, 0);
	const unsigned pitch = FreeImage_GetPitch(dib);
	
	unsigned samplesperpixel = FreeImage_GetLine(dib) / width / channelSize;
			
	if(channelSize == 1) {
		_convertLABtoRGB<BYTE>(width, height, line_start, pitch, samplesperpixel);
	}
	else {
		_convertLABtoRGB<WORD>(width, height, line_start, pitch, samplesperpixel);
	}

	return TRUE;	
}

// ----------------------------------------------------------

FIBITMAP* 
RemoveAlphaChannel(FIBITMAP* src) { 

	if(!FreeImage_HasPixels(src)) {
		return NULL;
	}

	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(src);
		
	switch(image_type) {
		case FIT_BITMAP:
			if(FreeImage_GetBPP(src) == 32) {
				// convert to 24-bit
				return FreeImage_ConvertTo24Bits(src);
			}
			break;
		case FIT_RGBA16:
			// convert to RGB16
			return FreeImage_ConvertToRGB16(src);
		case FIT_RGBAF:
			// convert to RGBF
			return FreeImage_ConvertToRGBF(src);
		default:
			// unsupported image type
			return NULL;
	}

	return NULL;
}


// ==========================================================

FIBITMAP * DLL_CALLCONV
FreeImage_ColorQuantize(FIBITMAP *dib, FREE_IMAGE_QUANTIZE quantize) {
	return FreeImage_ColorQuantizeEx(dib, quantize);
}

FIBITMAP * DLL_CALLCONV
FreeImage_ColorQuantizeEx(FIBITMAP *dib, FREE_IMAGE_QUANTIZE quantize, int PaletteSize, int ReserveSize, RGBQUAD *ReservePalette) {
	if( PaletteSize < 2 ) PaletteSize = 2;
	if( PaletteSize > 256 ) PaletteSize = 256;
	if( ReserveSize < 0 ) ReserveSize = 0;
	if( ReserveSize > PaletteSize ) ReserveSize = PaletteSize;
	if (FreeImage_HasPixels(dib)) {
		if (FreeImage_GetBPP(dib) == 24) {
			switch(quantize) {
				case FIQ_WUQUANT :
				{
					try {
						WuQuantizer Q (dib);
						FIBITMAP *dst = Q.Quantize(PaletteSize, ReserveSize, ReservePalette);
						if(dst) {
							// copy metadata from src to dst
							FreeImage_CloneMetadata(dst, dib);
						}
						return dst;
					} catch (const char *) {
						return NULL;
					}
				}
				case FIQ_NNQUANT :
				{
					// sampling factor in range 1..30. 
					// 1 => slower (but better), 30 => faster. Default value is 1
					const int sampling = 1;

					NNQuantizer Q(PaletteSize);
					FIBITMAP *dst = Q.Quantize(dib, ReserveSize, ReservePalette, sampling);
					if(dst) {
						// copy metadata from src to dst
						FreeImage_CloneMetadata(dst, dib);
					}
					return dst;
				}
			}
		}
	}

	return NULL;
}

// ==========================================================

FIBITMAP * DLL_CALLCONV
FreeImage_ConvertFromRawBits(BYTE *bits, int width, int height, int pitch, unsigned bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask, BOOL topdown) {
	FIBITMAP *dib = FreeImage_Allocate(width, height, bpp, red_mask, green_mask, blue_mask);

	if (dib != NULL) {
		if (topdown) {
			for (int i = height - 1; i >= 0; --i) {
				memcpy(FreeImage_GetScanLine(dib, i), bits, FreeImage_GetLine(dib));
				bits += pitch;
			}
		} else {
			for (int i = 0; i < height; ++i) {			
				memcpy(FreeImage_GetScanLine(dib, i), bits, FreeImage_GetLine(dib));
				bits += pitch;
			}
		}
	}

	return dib;
}

void DLL_CALLCONV
FreeImage_ConvertToRawBits(BYTE *bits, FIBITMAP *dib, int pitch, unsigned bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask, BOOL topdown) {
	if (FreeImage_HasPixels(dib) && (bits != NULL)) {
		for (unsigned i = 0; i < FreeImage_GetHeight(dib); ++i) {
			BYTE *scanline = FreeImage_GetScanLine(dib, topdown ? (FreeImage_GetHeight(dib) - i - 1) : i);

			if ((bpp == 16) && (FreeImage_GetBPP(dib) == 16)) {
				// convert 555 to 565 or vice versa

				if ((red_mask == FI16_555_RED_MASK) && (green_mask == FI16_555_GREEN_MASK) && (blue_mask == FI16_555_BLUE_MASK)) {
					if ((FreeImage_GetRedMask(dib) == FI16_565_RED_MASK) && (FreeImage_GetGreenMask(dib) == FI16_565_GREEN_MASK) && (FreeImage_GetBlueMask(dib) == FI16_565_BLUE_MASK)) {
						FreeImage_ConvertLine16_565_To16_555(bits, scanline, FreeImage_GetWidth(dib));
					} else {
						memcpy(bits, scanline, FreeImage_GetLine(dib));
					}
				} else {
					if ((FreeImage_GetRedMask(dib) == FI16_555_RED_MASK) && (FreeImage_GetGreenMask(dib) == FI16_555_GREEN_MASK) && (FreeImage_GetBlueMask(dib) == FI16_555_BLUE_MASK)) {
						FreeImage_ConvertLine16_555_To16_565(bits, scanline, FreeImage_GetWidth(dib));
					} else {
						memcpy(bits, scanline, FreeImage_GetLine(dib));
					}
				}
			} else if (FreeImage_GetBPP(dib) != bpp) {
				switch(FreeImage_GetBPP(dib)) {
					case 1 :
						switch(bpp) {
							CONVERT(1, 8)
							CONVERTTO16WITHPALETTE(1)
							CONVERTWITHPALETTE(1, 24)
							CONVERTWITHPALETTE(1, 32)
						}

						break;

					case 4 :
						switch(bpp) {
							CONVERT(4, 8)
							CONVERTTO16WITHPALETTE(4)
							CONVERTWITHPALETTE(4, 24)
							CONVERTWITHPALETTE(4, 32)
						}

						break;

					case 8 :
						switch(bpp) {
							CONVERTTO16WITHPALETTE(8)
							CONVERTWITHPALETTE(8, 24)
							CONVERTWITHPALETTE(8, 32)
						}

						break;

					case 24 :
						switch(bpp) {
							CONVERT(24, 8)
							CONVERTTO16(24)
							CONVERT(24, 32)
						}

						break;

					case 32 :
						switch(bpp) {
							CONVERT(32, 8)
							CONVERTTO16(32)
							CONVERT(32, 24)
						}

						break;
				}
			} else {
				memcpy(bits, scanline, FreeImage_GetLine(dib));
			}

			bits += pitch;
		}
	}
}

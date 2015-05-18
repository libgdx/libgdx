// ==========================================================
// Tone mapping operator (Reinhard, 2005)
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
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
#include "ToneMapping.h"

// ----------------------------------------------------------
// Global and/or local tone mapping operator
// References: 
// [1] Erik Reinhard and Kate Devlin, 'Dynamic Range Reduction Inspired by Photoreceptor Physiology', 
//     IEEE Transactions on Visualization and Computer Graphics, 11(1), Jan/Feb 2005. 
// [2] Erik Reinhard, 'Parameter estimation for photographic tone reproduction',
//     Journal of Graphics Tools, vol. 7, no. 1, pp. 45–51, 2003.
// ----------------------------------------------------------

/**
Tone mapping operator
@param dib Input / Output RGBF image
@param Y Input luminance image version of dib
@param f Overall intensity in range [-8:8] : default to 0
@param m Contrast in range [0.3:1) : default to 0
@param a Adaptation in range [0:1] : default to 1
@param c Color correction in range [0:1] : default to 0
@return Returns TRUE if successful, returns FALSE otherwise
@see LuminanceFromY
*/
static BOOL 
ToneMappingReinhard05(FIBITMAP *dib, FIBITMAP *Y, float f, float m, float a, float c) {
	float Cav[3];		// channel average
	float Lav = 0;		// average luminance
	float Llav = 0;		// log average luminance
	float minLum = 1;	// min luminance
	float maxLum = 1;	// max luminance

	float L;		// pixel luminance
	float I_g, I_l; // global and local light adaptation
	float I_a;		// interpolated pixel light adaptation
	float k;		// key (low-key means overall dark image, high-key means overall light image)

	// check input parameters 

	if((FreeImage_GetImageType(dib) != FIT_RGBF) || (FreeImage_GetImageType(Y) != FIT_FLOAT)) {
		return FALSE;
	}

	if(f < -8) f = -8; if(f > 8) f = 8;
    if(m < 0)  m = 0;  if(m > 1) m = 1;
    if(a < 0)  a = 0;  if(a > 1) a = 1;
    if(c < 0)  c = 0;  if(c > 1) c = 1;

	const unsigned width  = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);

	const unsigned dib_pitch  = FreeImage_GetPitch(dib);
	const unsigned y_pitch    = FreeImage_GetPitch(Y);

	int i;
	unsigned x, y;
	BYTE *bits = NULL, *Ybits = NULL;

	// get statistics about the data (but only if its really needed)

	f = exp(-f);
	if((m == 0) || (a != 1) && (c != 1)) {
		// avoid these calculations if its not needed after ...
		LuminanceFromY(Y, &maxLum, &minLum, &Lav, &Llav);
		k = (log(maxLum) - Llav) / (log(maxLum) - log(minLum));
		if(k < 0) {
			// pow(k, 1.4F) is undefined ...
			// there's an ambiguity about the calculation of Llav between Reinhard papers and the various implementations  ...
			// try another world adaptation luminance formula using instead 'worldLum = log(Llav)'
			k = (log(maxLum) - log(Llav)) / (log(maxLum) - log(minLum));
			if(k < 0) m = 0.3F;
		}
	}
	m = (m > 0) ? m : (float)(0.3 + 0.7 * pow(k, 1.4F));

	float max_color = -1e6F;
	float min_color = +1e6F;

	// tone map image

	bits  = (BYTE*)FreeImage_GetBits(dib);
	Ybits = (BYTE*)FreeImage_GetBits(Y);

	if((a == 1) && (c == 0)) {
		// when using default values, use a fastest code

		for(y = 0; y < height; y++) {
			float *Y     = (float*)Ybits;
			float *color = (float*)bits;

			for(x = 0; x < width; x++) {
				I_a = Y[x];	// luminance(x, y)
				for (i = 0; i < 3; i++) {
					*color /= ( *color + pow(f * I_a, m) );
					
					max_color = (*color > max_color) ? *color : max_color;
					min_color = (*color < min_color) ? *color : min_color;

					color++;
				}
			}
			// next line
			bits  += dib_pitch;
			Ybits += y_pitch;
		}
	} else {
		// complete algorithm

		// channel averages

		Cav[0] = Cav[1] = Cav[2] = 0;
		if((a != 1) && (c != 0)) {
			// channel averages are not needed when (a == 1) or (c == 0)
			bits = (BYTE*)FreeImage_GetBits(dib);
			for(y = 0; y < height; y++) {
				float *color = (float*)bits;
				for(x = 0; x < width; x++) {
					for(i = 0; i < 3; i++) {
						Cav[i] += *color;
						color++;
					}
				}
				// next line
				bits += dib_pitch;
			}
			const float image_size = (float)width * height;
			for(i = 0; i < 3; i++) {
				Cav[i] /= image_size;
			}
		}

		// perform tone mapping

		bits = (BYTE*)FreeImage_GetBits(dib);
		for(y = 0; y < height; y++) {
			const float *Y     = (float*)Ybits;
			float *color = (float*)bits;

			for(x = 0; x < width; x++) {
				L = Y[x];	// luminance(x, y)
				for (i = 0; i < 3; i++) {
					I_l = c * *color + (1-c) * L;
					I_g = c * Cav[i] + (1-c) * Lav;
					I_a = a * I_l + (1-a) * I_g;
					*color /= ( *color + pow(f * I_a, m) );
					
					max_color = (*color > max_color) ? *color : max_color;
					min_color = (*color < min_color) ? *color : min_color;

					color++;
				}
			}
			// next line
			bits  += dib_pitch;
			Ybits += y_pitch;
		}
	}

	// normalize intensities

	if(max_color != min_color) {
		bits = (BYTE*)FreeImage_GetBits(dib);
		const float range = max_color - min_color;
		for(y = 0; y < height; y++) {
			float *color = (float*)bits;
			for(x = 0; x < width; x++) {
				for(i = 0; i < 3; i++) {
					*color = (*color - min_color) / range;
					color++;
				}
			}
			// next line
			bits += dib_pitch;
		}
	}

	return TRUE;
}

// ----------------------------------------------------------
//  Main algorithm
// ----------------------------------------------------------

/**
Apply the global/local tone mapping operator to a RGBF image and convert to 24-bit RGB<br>
User parameters control intensity, contrast, and level of adaptation
@param src Input RGBF image
@param intensity Overall intensity in range [-8:8] : default to 0
@param contrast Contrast in range [0.3:1) : default to 0
@param adaptation Adaptation in range [0:1] : default to 1
@param color_correction Color correction in range [0:1] : default to 0
@return Returns a 24-bit RGB image if successful, returns NULL otherwise
*/
FIBITMAP* DLL_CALLCONV 
FreeImage_TmoReinhard05Ex(FIBITMAP *src, double intensity, double contrast, double adaptation, double color_correction) {
	if(!FreeImage_HasPixels(src)) return NULL;

	// working RGBF variable
	FIBITMAP *dib = NULL, *Y = NULL;

	dib = FreeImage_ConvertToRGBF(src);
	if(!dib) return NULL;

	// get the Luminance channel
	Y = ConvertRGBFToY(dib);
	if(!Y) {
		FreeImage_Unload(dib);
		return NULL;
	}

	// perform the tone mapping
	ToneMappingReinhard05(dib, Y, (float)intensity, (float)contrast, (float)adaptation, (float)color_correction);
	// not needed anymore
	FreeImage_Unload(Y);
	// clamp image highest values to display white, then convert to 24-bit RGB
	FIBITMAP *dst = ClampConvertRGBFTo24(dib);

	// clean-up and return
	FreeImage_Unload(dib);

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);

	return dst;
}

/**
Apply the global tone mapping operator to a RGBF image and convert to 24-bit RGB<br>
User parameters control intensity and contrast
@param src Input RGBF image
@param intensity Overall intensity in range [-8:8] : default to 0
@param contrast Contrast in range [0.3:1) : default to 0
@return Returns a 24-bit RGB image if successful, returns NULL otherwise
*/
FIBITMAP* DLL_CALLCONV 
FreeImage_TmoReinhard05(FIBITMAP *src, double intensity, double contrast) {
	return FreeImage_TmoReinhard05Ex(src, intensity, contrast, 1, 0);
}

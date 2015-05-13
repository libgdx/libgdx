// ==========================================================
// Tone mapping operator (Drago, 2003)
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
#include "ToneMapping.h"

// ----------------------------------------------------------
// Logarithmic mapping operator
// Reference: 
// [1] F. Drago, K. Myszkowski, T. Annen, and N. Chiba, 
// Adaptive Logarithmic Mapping for Displaying High Contrast Scenes, 
// Eurographics 2003.
// ----------------------------------------------------------

/**
Bias function
*/
static inline double 
biasFunction(const double b, const double x) {
	return pow (x, b);		// pow(x, log(bias)/log(0.5)
}

/**
Padé approximation of log(x + 1)
x(6+x)/(6+4x) good if x < 1
x*(6 + 0.7662x)/(5.9897 + 3.7658x) between 1 and 2
See http://www.nezumi.demon.co.uk/consult/logx.htm
*/
static inline double 
pade_log(const double x) {
	if(x < 1) {
		return (x * (6 + x) / (6 + 4 * x));
	} else if(x < 2) {
		return (x * (6 + 0.7662 * x) / (5.9897 + 3.7658 * x));
	}
	return log(x + 1);
}

/**
Log mapping operator
@param dib Input / Output Yxy image
@param maxLum Maximum luminance
@param avgLum Average luminance (world adaptation luminance)
@param biasParam Bias parameter (a zero value default to 0.85)
@param exposure Exposure parameter (default to 0)
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
ToneMappingDrago03(FIBITMAP *dib, const float maxLum, const float avgLum, float biasParam, const float exposure) {
	const float LOG05 = -0.693147F;	// log(0.5) 

	double Lmax, divider, interpol, biasP;
	unsigned x, y;
	double L;

	if(FreeImage_GetImageType(dib) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch  = FreeImage_GetPitch(dib);


	// arbitrary Bias Parameter 
	if(biasParam == 0) 
		biasParam = 0.85F;

	// normalize maximum luminance by average luminance
	Lmax = maxLum / avgLum;
	
	divider = log10(Lmax+1);
	biasP = log(biasParam)/LOG05;

#if !defined(DRAGO03_FAST)

	/**
	Normal tone mapping of every pixel
	further acceleration is obtained by a Padé approximation of log(x + 1)
	*/
	BYTE *bits = (BYTE*)FreeImage_GetBits(dib);
	for(y = 0; y < height; y++) {
		FIRGBF *pixel = (FIRGBF*)bits;
		for(x = 0; x < width; x++) {
			double Yw = pixel[x].red / avgLum;
			Yw *= exposure;
			interpol = log(2 + biasFunction(biasP, Yw / Lmax) * 8);
			L = pade_log(Yw);// log(Yw + 1)
			pixel[x].red = (float)((L / interpol) / divider);
		}
		// next line
		bits += pitch;
	}

#else
	unsigned index;
	int i, j;

	unsigned max_width  = width - (width % 3);
	unsigned max_height = height - (height % 3); 
	unsigned fpitch = pitch / sizeof(FIRGBF);

	/**
	fast tone mapping
	split the image into 3x3 pixel tiles and perform the computation for each group of 9 pixels
	further acceleration is obtained by a Padé approximation of log(x + 1)
	=> produce artifacts and not so faster, so the code has been disabled
	*/
#define PIXEL(x, y)	image[y*fpitch + x].red

	FIRGBF *image = (FIRGBF*)FreeImage_GetBits(dib);
	for(y = 0; y < max_height; y += 3) {
		for(x = 0; x < max_width; x += 3) {
			double average = 0;
			for(i = 0; i < 3; i++) {
				for(j = 0; j < 3; j++) {
					index = (y + i)*fpitch + (x + j);
					image[index].red /= (float)avgLum;
					image[index].red *= exposure; 
					average += image[index].red;
				}
			}
			average = average / 9 - PIXEL(x, y);
			if(average > -1 && average < 1) {
				interpol = log(2 + pow(PIXEL(x + 1, y + 1) / Lmax, biasP) * 8);
				for(i = 0; i < 3; i++) {
					for(j = 0; j < 3; j++) {
						index = (y + i)*fpitch + (x + j);
						L = pade_log(image[index].red);// log(image[index].red + 1)
						image[index].red = (float)((L / interpol) / divider);
					}
				}
			}
			else {
				for(i = 0; i < 3; i++) {
					for(j = 0; j < 3; j++) {
						index = (y + i)*fpitch + (x + j);
						interpol = log(2 + pow(image[index].red / Lmax, biasP) * 8);
						L = pade_log(image[index].red);// log(image[index].red + 1)
						image[index].red = (float)((L / interpol) / divider);
					}
				}
			}
		} //x
	} // y

	/**
	Normal tone mapping of every pixel for the remaining right and bottom bands
	*/
	BYTE *bits;

	// right band
	bits = (BYTE*)FreeImage_GetBits(dib);
	for(y = 0; y < height; y++) {
		FIRGBF *pixel = (FIRGBF*)bits;
		for(x = max_width; x < width; x++) {
			double Yw = pixel[x].red / avgLum;
			Yw *= exposure;
			interpol = log(2 + biasFunction(biasP, Yw / Lmax) * 8);
			L = pade_log(Yw);// log(Yw + 1)
			pixel[x].red = (float)((L / interpol) / divider);
		}
		// next line
		bits += pitch;
	}
	// bottom band
	bits = (BYTE*)FreeImage_GetBits(dib);
	for(y = max_height; y < height; y++) {
		FIRGBF *pixel = (FIRGBF*)bits;
		for(x = 0; x < max_width; x++) {
			double Yw = pixel[x].red / avgLum;
			Yw *= exposure;
			interpol = log(2 + biasFunction(biasP, Yw / Lmax) * 8);
			L = pade_log(Yw);// log(Yw + 1)
			pixel[x].red = (float)((L / interpol) / divider);
		}
		// next line
		bits += pitch;
	}

#endif	// DRAGO03_FAST

	return TRUE;
}

/**
Custom gamma correction based on the ITU-R BT.709 standard
@param dib RGBF image to be corrected
@param gammaval Gamma value (2.2 is a good default value)
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
REC709GammaCorrection(FIBITMAP *dib, const float gammaval) {
	if(FreeImage_GetImageType(dib) != FIT_RGBF)
		return FALSE;

	float slope = 4.5F;
	float start = 0.018F;
	
	const float fgamma = (float)((0.45 / gammaval) * 2);
	if(gammaval >= 2.1F) {
		start = (float)(0.018 / ((gammaval - 2) * 7.5));
		slope = (float)(4.5 * ((gammaval - 2) * 7.5));
	} else if (gammaval <= 1.9F) {
		start = (float)(0.018 * ((2 - gammaval) * 7.5));
		slope = (float)(4.5 / ((2 - gammaval) * 7.5));
	}

	const unsigned width  = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch  = FreeImage_GetPitch(dib);

	BYTE *bits = (BYTE*)FreeImage_GetBits(dib);
	for(unsigned y = 0; y < height; y++) {
		float *pixel = (float*)bits;
		for(unsigned x = 0; x < width; x++) {
			for(int i = 0; i < 3; i++) {
				*pixel = (*pixel <= start) ? *pixel * slope : (1.099F * pow(*pixel, fgamma) - 0.099F);
				pixel++;
			}
		}
		bits += pitch;
	}

	return TRUE;
}

// ----------------------------------------------------------
//  Main algorithm
// ----------------------------------------------------------

/**
Apply the Adaptive Logarithmic Mapping operator to a HDR image and convert to 24-bit RGB
@param src Input RGB16 or RGB[A]F image
@param gamma Gamma correction (gamma > 0). 1 means no correction, 2.2 in the original paper.
@param exposure Exposure parameter (0 means no correction, 0 in the original paper)
@return Returns a 24-bit RGB image if successful, returns NULL otherwise
*/
FIBITMAP* DLL_CALLCONV 
FreeImage_TmoDrago03(FIBITMAP *src, double gamma, double exposure) {
	float maxLum, minLum, avgLum;

	if(!FreeImage_HasPixels(src)) return NULL;

	// working RGBF variable
	FIBITMAP *dib = NULL;

	dib = FreeImage_ConvertToRGBF(src);
	if(!dib) return NULL;

	// default algorithm parameters
	const float biasParam = 0.85F;
	const float expoParam = (float)pow(2.0, exposure); //default exposure is 1, 2^0

	// convert to Yxy
	ConvertInPlaceRGBFToYxy(dib);
	// get the luminance
	LuminanceFromYxy(dib, &maxLum, &minLum, &avgLum);
	// perform the tone mapping
	ToneMappingDrago03(dib, maxLum, avgLum, biasParam, expoParam);
	// convert back to RGBF
	ConvertInPlaceYxyToRGBF(dib);
	if(gamma != 1) {
		// perform gamma correction
		REC709GammaCorrection(dib, (float)gamma);
	}
	// clamp image highest values to display white, then convert to 24-bit RGB
	FIBITMAP *dst = ClampConvertRGBFTo24(dib);

	// clean-up and return
	FreeImage_Unload(dib);

	// copy metadata from src to dst
	FreeImage_CloneMetadata(dst, src);
	
	return dst;
}

// ==========================================================
// High Dynamic Range bitmap conversion routines
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
// Convert RGB to and from Yxy, same as in Reinhard et al. SIGGRAPH 2002
// References : 
// [1] Radiance Home Page [Online] http://radsite.lbl.gov/radiance/HOME.html
// [2] E. Reinhard, M. Stark, P. Shirley, and J. Ferwerda,  
//     Photographic Tone Reproduction for Digital Images, ACM Transactions on Graphics, 
//     21(3):267-276, 2002 (Proceedings of SIGGRAPH 2002). 
// [3] J. Tumblin and H.E. Rushmeier, 
//     Tone Reproduction for Realistic Images. IEEE Computer Graphics and Applications, 
//     13(6):42-48, 1993.
// ----------------------------------------------------------

/**
nominal CRT primaries 
*/
/*
static const float CIE_x_r = 0.640F;
static const float CIE_y_r = 0.330F;
static const float CIE_x_g = 0.290F;
static const float CIE_y_g = 0.600F;
static const float CIE_x_b = 0.150F;
static const float CIE_y_b = 0.060F;
static const float CIE_x_w = 0.3333F;	// use true white
static const float CIE_y_w = 0.3333F;
*/
/**
sRGB primaries
*/
static const float CIE_x_r = 0.640F;
static const float CIE_y_r = 0.330F;
static const float CIE_x_g = 0.300F;
static const float CIE_y_g = 0.600F;
static const float CIE_x_b = 0.150F;
static const float CIE_y_b = 0.060F;
static const float CIE_x_w = 0.3127F;	// Illuminant D65
static const float CIE_y_w = 0.3290F;

static const float CIE_D = ( CIE_x_r*(CIE_y_g - CIE_y_b) + CIE_x_g*(CIE_y_b - CIE_y_r) + CIE_x_b*(CIE_y_r - CIE_y_g) );
static const float CIE_C_rD = ( (1/CIE_y_w) * ( CIE_x_w*(CIE_y_g - CIE_y_b) - CIE_y_w*(CIE_x_g - CIE_x_b) + CIE_x_g*CIE_y_b - CIE_x_b*CIE_y_g) );
static const float CIE_C_gD = ( (1/CIE_y_w) * ( CIE_x_w*(CIE_y_b - CIE_y_r) - CIE_y_w*(CIE_x_b - CIE_x_r) - CIE_x_r*CIE_y_b + CIE_x_b*CIE_y_r) );
static const float CIE_C_bD = ( (1/CIE_y_w) * ( CIE_x_w*(CIE_y_r - CIE_y_g) - CIE_y_w*(CIE_x_r - CIE_x_g) + CIE_x_r*CIE_y_g - CIE_x_g*CIE_y_r) );

/**
RGB to XYZ (no white balance)
*/
static const float  RGB2XYZ[3][3] = {
	{ CIE_x_r*CIE_C_rD / CIE_D, 
	  CIE_x_g*CIE_C_gD / CIE_D, 
	  CIE_x_b*CIE_C_bD / CIE_D 
	},
	{ CIE_y_r*CIE_C_rD / CIE_D, 
	  CIE_y_g*CIE_C_gD / CIE_D, 
	  CIE_y_b*CIE_C_bD / CIE_D 
	},
	{ (1 - CIE_x_r-CIE_y_r)*CIE_C_rD / CIE_D,
	  (1 - CIE_x_g-CIE_y_g)*CIE_C_gD / CIE_D,
	  (1 - CIE_x_b-CIE_y_b)*CIE_C_bD / CIE_D
	}
};

/**
XYZ to RGB (no white balance)
*/
static const float  XYZ2RGB[3][3] = {
	{(CIE_y_g - CIE_y_b - CIE_x_b*CIE_y_g + CIE_y_b*CIE_x_g) / CIE_C_rD,
	 (CIE_x_b - CIE_x_g - CIE_x_b*CIE_y_g + CIE_x_g*CIE_y_b) / CIE_C_rD,
	 (CIE_x_g*CIE_y_b - CIE_x_b*CIE_y_g) / CIE_C_rD
	},
	{(CIE_y_b - CIE_y_r - CIE_y_b*CIE_x_r + CIE_y_r*CIE_x_b) / CIE_C_gD,
	 (CIE_x_r - CIE_x_b - CIE_x_r*CIE_y_b + CIE_x_b*CIE_y_r) / CIE_C_gD,
	 (CIE_x_b*CIE_y_r - CIE_x_r*CIE_y_b) / CIE_C_gD
	},
	{(CIE_y_r - CIE_y_g - CIE_y_r*CIE_x_g + CIE_y_g*CIE_x_r) / CIE_C_bD,
	 (CIE_x_g - CIE_x_r - CIE_x_g*CIE_y_r + CIE_x_r*CIE_y_g) / CIE_C_bD,
	 (CIE_x_r*CIE_y_g - CIE_x_g*CIE_y_r) / CIE_C_bD
	}
};

/**
This gives approximately the following matrices : 

static const float RGB2XYZ[3][3] = { 
	{ 0.41239083F, 0.35758433F, 0.18048081F },
	{ 0.21263903F, 0.71516865F, 0.072192319F },
	{ 0.019330820F, 0.11919473F, 0.95053220F }
};
static const float XYZ2RGB[3][3] = { 
	{ 3.2409699F, -1.5373832F, -0.49861079F },
	{ -0.96924376F, 1.8759676F, 0.041555084F },
	{ 0.055630036F, -0.20397687F, 1.0569715F }
};
*/

// ----------------------------------------------------------

static const float EPSILON = 1e-06F;
static const float INF = 1e+10F;

/**
Convert in-place floating point RGB data to Yxy.<br>
On output, pixel->red == Y, pixel->green == x, pixel->blue == y
@param dib Input RGBF / Output Yxy image
@return Returns TRUE if successful, returns FALSE otherwise
*/
BOOL 
ConvertInPlaceRGBFToYxy(FIBITMAP *dib) {
	float result[3];

	if(FreeImage_GetImageType(dib) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch  = FreeImage_GetPitch(dib);
	
	BYTE *bits = (BYTE*)FreeImage_GetBits(dib);
	for(unsigned y = 0; y < height; y++) {
		FIRGBF *pixel = (FIRGBF*)bits;
		for(unsigned x = 0; x < width; x++) {
			result[0] = result[1] = result[2] = 0;
			for (int i = 0; i < 3; i++) {
				result[i] += RGB2XYZ[i][0] * pixel[x].red;
				result[i] += RGB2XYZ[i][1] * pixel[x].green;
				result[i] += RGB2XYZ[i][2] * pixel[x].blue;
			}
			const float W = result[0] + result[1] + result[2];
			const float Y = result[1];
			if(W > 0) { 
				pixel[x].red   = Y;			    // Y 
				pixel[x].green = result[0] / W;	// x 
				pixel[x].blue  = result[1] / W;	// y 	
			} else {
				pixel[x].red = pixel[x].green = pixel[x].blue = 0;
			}
		}
		// next line
		bits += pitch;
	}

	return TRUE;
}

/**
Convert in-place Yxy image to floating point RGB data.<br>
On input, pixel->red == Y, pixel->green == x, pixel->blue == y
@param dib Input Yxy / Output RGBF image
@return Returns TRUE if successful, returns FALSE otherwise
*/
BOOL 
ConvertInPlaceYxyToRGBF(FIBITMAP *dib) {
	float result[3];
	float X, Y, Z;

	if(FreeImage_GetImageType(dib) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch  = FreeImage_GetPitch(dib);

	BYTE *bits = (BYTE*)FreeImage_GetBits(dib);
	for(unsigned y = 0; y < height; y++) {
		FIRGBF *pixel = (FIRGBF*)bits;
		for(unsigned x = 0; x < width; x++) {
			Y = pixel[x].red;	        // Y 
			result[1] = pixel[x].green;	// x 
			result[2] = pixel[x].blue;	// y 
			if ((Y > EPSILON) && (result[1] > EPSILON) && (result[2] > EPSILON)) {
				X = (result[1] * Y) / result[2];
				Z = (X / result[1]) - X - Y;
			} else {
				X = Z = EPSILON;
			}
			pixel[x].red   = X;
			pixel[x].green = Y;
			pixel[x].blue  = Z;
			result[0] = result[1] = result[2] = 0;
			for (int i = 0; i < 3; i++) {
				result[i] += XYZ2RGB[i][0] * pixel[x].red;
				result[i] += XYZ2RGB[i][1] * pixel[x].green;
				result[i] += XYZ2RGB[i][2] * pixel[x].blue;
			}
			pixel[x].red   = result[0];	// R
			pixel[x].green = result[1];	// G
			pixel[x].blue  = result[2];	// B
		}
		// next line
		bits += pitch;
	}

	return TRUE;
}

/**
Get the maximum, minimum and average luminance.<br>
On input, pixel->red == Y, pixel->green == x, pixel->blue == y
@param Yxy Source Yxy image to analyze
@param maxLum Maximum luminance
@param minLum Minimum luminance
@param worldLum Average luminance (world adaptation luminance)
@return Returns TRUE if successful, returns FALSE otherwise
*/
BOOL 
LuminanceFromYxy(FIBITMAP *Yxy, float *maxLum, float *minLum, float *worldLum) {
	if(FreeImage_GetImageType(Yxy) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(Yxy);
	const unsigned height = FreeImage_GetHeight(Yxy);
	const unsigned pitch  = FreeImage_GetPitch(Yxy);

	float max_lum = 0, min_lum = 0;
	double sum = 0;

	BYTE *bits = (BYTE*)FreeImage_GetBits(Yxy);
	for(unsigned y = 0; y < height; y++) {
		const FIRGBF *pixel = (FIRGBF*)bits;
		for(unsigned x = 0; x < width; x++) {
			const float Y = MAX(0.0F, pixel[x].red);// avoid negative values
			max_lum = (max_lum < Y) ? Y : max_lum;	// max Luminance in the scene
			min_lum = (min_lum < Y) ? min_lum : Y;	// min Luminance in the scene
			sum += log(2.3e-5F + Y);				// contrast constant in Tumblin paper
		}
		// next line
		bits += pitch;
	}
	// maximum luminance
	*maxLum = max_lum;
	// minimum luminance
	*minLum = min_lum;
	// average log luminance
	double avgLogLum = (sum / (width * height));
	// world adaptation luminance
	*worldLum = (float)exp(avgLogLum);

	return TRUE;
}

/**
Clamp RGBF image highest values to display white, 
then convert to 24-bit RGB
*/
FIBITMAP* 
ClampConvertRGBFTo24(FIBITMAP *src) {
	if(FreeImage_GetImageType(src) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(src);
	const unsigned height = FreeImage_GetHeight(src);

	FIBITMAP *dst = FreeImage_Allocate(width, height, 24, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
	if(!dst) return NULL;

	const unsigned src_pitch  = FreeImage_GetPitch(src);
	const unsigned dst_pitch  = FreeImage_GetPitch(dst);

	BYTE *src_bits = (BYTE*)FreeImage_GetBits(src);
	BYTE *dst_bits = (BYTE*)FreeImage_GetBits(dst);

	for(unsigned y = 0; y < height; y++) {
		const FIRGBF *src_pixel = (FIRGBF*)src_bits;
		BYTE *dst_pixel = (BYTE*)dst_bits;
		for(unsigned x = 0; x < width; x++) {
			const float red   = (src_pixel[x].red > 1)   ? 1 : src_pixel[x].red;
			const float green = (src_pixel[x].green > 1) ? 1 : src_pixel[x].green;
			const float blue  = (src_pixel[x].blue > 1)  ? 1 : src_pixel[x].blue;
			
			dst_pixel[FI_RGBA_RED]   = (BYTE)(255.0F * red   + 0.5F);
			dst_pixel[FI_RGBA_GREEN] = (BYTE)(255.0F * green + 0.5F);
			dst_pixel[FI_RGBA_BLUE]  = (BYTE)(255.0F * blue  + 0.5F);
			dst_pixel += 3;
		}
		src_bits += src_pitch;
		dst_bits += dst_pitch;
	}

	return dst;
}

/**
Extract the luminance channel L from a RGBF image. 
Luminance is calculated from the sRGB model (RGB2XYZ matrix) 
using a D65 white point : 
L = ( 0.2126 * r ) + ( 0.7152 * g ) + ( 0.0722 * b )
Reference : 
A Standard Default Color Space for the Internet - sRGB. 
[online] http://www.w3.org/Graphics/Color/sRGB
*/
FIBITMAP*  
ConvertRGBFToY(FIBITMAP *src) {
	if(FreeImage_GetImageType(src) != FIT_RGBF)
		return FALSE;

	const unsigned width  = FreeImage_GetWidth(src);
	const unsigned height = FreeImage_GetHeight(src);

	FIBITMAP *dst = FreeImage_AllocateT(FIT_FLOAT, width, height);
	if(!dst) return NULL;

	const unsigned src_pitch  = FreeImage_GetPitch(src);
	const unsigned dst_pitch  = FreeImage_GetPitch(dst);

	
	BYTE *src_bits = (BYTE*)FreeImage_GetBits(src);
	BYTE *dst_bits = (BYTE*)FreeImage_GetBits(dst);

	for(unsigned y = 0; y < height; y++) {
		const FIRGBF *src_pixel = (FIRGBF*)src_bits;
		float  *dst_pixel = (float*)dst_bits;
		for(unsigned x = 0; x < width; x++) {
			const float L = LUMA_REC709(src_pixel[x].red, src_pixel[x].green, src_pixel[x].blue);
			dst_pixel[x] = (L > 0) ? L : 0;
		}
		// next line
		src_bits += src_pitch;
		dst_bits += dst_pitch;
	}

	return dst;
}

/**
Get the maximum, minimum, average luminance and log average luminance from a Y image
@param dib Source Y image to analyze
@param maxLum Maximum luminance
@param minLum Minimum luminance
@param Lav Average luminance
@param Llav Log average luminance (also known as 'world adaptation luminance')
@return Returns TRUE if successful, returns FALSE otherwise
@see ConvertRGBFToY, FreeImage_TmoReinhard05Ex
*/
BOOL 
LuminanceFromY(FIBITMAP *dib, float *maxLum, float *minLum, float *Lav, float *Llav) {
	if(FreeImage_GetImageType(dib) != FIT_FLOAT)
		return FALSE;

	unsigned width  = FreeImage_GetWidth(dib);
	unsigned height = FreeImage_GetHeight(dib);
	unsigned pitch  = FreeImage_GetPitch(dib);

	float max_lum = -1e20F, min_lum = 1e20F;
	double sumLum = 0, sumLogLum = 0;

	BYTE *bits = (BYTE*)FreeImage_GetBits(dib);
	for(unsigned y = 0; y < height; y++) {
		const float *pixel = (float*)bits;
		for(unsigned x = 0; x < width; x++) {
			const float Y = pixel[x];
			max_lum = (max_lum < Y) ? Y : max_lum;				// max Luminance in the scene
			min_lum = ((Y > 0) && (min_lum < Y)) ? min_lum : Y;	// min Luminance in the scene
			sumLum += Y;										// average luminance
			sumLogLum += log(2.3e-5F + Y);						// contrast constant in Tumblin paper
		}
		// next line
		bits += pitch;
	}

	// maximum luminance
	*maxLum = max_lum;
	// minimum luminance
	*minLum = min_lum;
	// average luminance
	*Lav = (float)(sumLum / (width * height));
	// average log luminance, a.k.a. world adaptation luminance
	*Llav = (float)exp(sumLogLum / (width * height));

	return TRUE;
}
// --------------------------------------------------------------------------

static void findMaxMinPercentile(FIBITMAP *Y, float minPrct, float *minLum, float maxPrct, float *maxLum) {
	int x, y;
	int width = FreeImage_GetWidth(Y);
	int height = FreeImage_GetHeight(Y);
	int pitch = FreeImage_GetPitch(Y);

	std::vector<float> vY(width * height);

	BYTE *bits = (BYTE*)FreeImage_GetBits(Y);
	for(y = 0; y < height; y++) {
		float *pixel = (float*)bits;
		for(x = 0; x < width; x++) {
			if(pixel[x] != 0) {
				vY.push_back(pixel[x]);
			}
		}
		bits += pitch;
	}

	std::sort(vY.begin(), vY.end());
	
	*minLum = vY.at( int(minPrct * vY.size()) );
	*maxLum = vY.at( int(maxPrct * vY.size()) );
}

/**
Clipping function<br>
Remove any extremely bright and/or extremely dark pixels 
and normalize between 0 and 1. 
@param Y Input/Output image
@param minPrct Minimum percentile
@param maxPrct Maximum percentile
*/
void 
NormalizeY(FIBITMAP *Y, float minPrct, float maxPrct) {
	int x, y;
	float maxLum, minLum;

	if(minPrct > maxPrct) {
		// swap values
		float t = minPrct; minPrct = maxPrct; maxPrct = t;
	}
	if(minPrct < 0) minPrct = 0;
	if(maxPrct > 1) maxPrct = 1;

	int width = FreeImage_GetWidth(Y);
	int height = FreeImage_GetHeight(Y);
	int pitch = FreeImage_GetPitch(Y);

	// find max & min luminance values
	if((minPrct > 0) || (maxPrct < 1)) {
		maxLum = 0, minLum = 0;
		findMaxMinPercentile(Y, minPrct, &minLum, maxPrct, &maxLum);
	} else {
		maxLum = -1e20F, minLum = 1e20F;
		BYTE *bits = (BYTE*)FreeImage_GetBits(Y);
		for(y = 0; y < height; y++) {
			const float *pixel = (float*)bits;
			for(x = 0; x < width; x++) {
				const float value = pixel[x];
				maxLum = (maxLum < value) ? value : maxLum;	// max Luminance in the scene
				minLum = (minLum < value) ? minLum : value;	// min Luminance in the scene
			}
			// next line
			bits += pitch;
		}
	}
	if(maxLum == minLum) return;

	// normalize to range 0..1 
	const float divider = maxLum - minLum;
	BYTE *bits = (BYTE*)FreeImage_GetBits(Y);
	for(y = 0; y < height; y++) {
		float *pixel = (float*)bits;
		for(x = 0; x < width; x++) {
			pixel[x] = (pixel[x] - minLum) / divider;
			if(pixel[x] <= 0) pixel[x] = EPSILON;
			if(pixel[x] > 1) pixel[x] = 1;
		}
		// next line
		bits += pitch;
	}
}

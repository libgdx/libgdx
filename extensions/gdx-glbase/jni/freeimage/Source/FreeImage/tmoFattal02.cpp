// ==========================================================
// Tone mapping operator (Fattal, 2002)
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
// Gradient domain HDR compression
// Reference:
// [1] R. Fattal, D. Lischinski, and M.Werman, 
// Gradient domain high dynamic range compression,
// ACM Transactions on Graphics, special issue on Proc. of ACM SIGGRAPH 2002, 
// San Antonio, Texas, vol. 21(3), pp. 257-266, 2002.
// ----------------------------------------------------------

static const float EPSILON = 1e-4F;

/**
Performs a 5 by 5 gaussian filtering using two 1D convolutions, 
followed by a subsampling by 2. 
@param dib Input image
@return Returns a blurred image of size SIZE(dib)/2
@see GaussianPyramid
*/
static FIBITMAP* GaussianLevel5x5(FIBITMAP *dib) {
	FIBITMAP *h_dib = NULL, *v_dib = NULL, *dst = NULL;
	float *src_pixel, *dst_pixel;

	try {
		const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
		if(image_type != FIT_FLOAT) throw(1);

		const unsigned width = FreeImage_GetWidth(dib);
		const unsigned height = FreeImage_GetHeight(dib);

		h_dib = FreeImage_AllocateT(image_type, width, height);
		v_dib = FreeImage_AllocateT(image_type, width, height);
		if(!h_dib || !v_dib) throw(1);

		const unsigned pitch = FreeImage_GetPitch(dib) / sizeof(float);

		// horizontal convolution dib -> h_dib

		src_pixel = (float*)FreeImage_GetBits(dib);
		dst_pixel = (float*)FreeImage_GetBits(h_dib);

		for(unsigned y = 0; y < height; y++) {
			// work on line y
			for(unsigned x = 2; x < width - 2; x++) {
				dst_pixel[x] = src_pixel[x-2] + src_pixel[x+2] + 4 * (src_pixel[x-1] + src_pixel[x+1]) + 6 * src_pixel[x];
				dst_pixel[x] /= 16;
			}
			// boundary mirroring
			dst_pixel[0] = (2 * src_pixel[2] + 8 * src_pixel[1] + 6 * src_pixel[0]) / 16;
			dst_pixel[1] = (src_pixel[3] + 4 * (src_pixel[0] + src_pixel[2]) + 7 * src_pixel[1]) / 16;
			dst_pixel[width-2] = (src_pixel[width-4] + 5 * src_pixel[width-1] + 4 * src_pixel[width-3] + 6 * src_pixel[width-2]) / 16;
			dst_pixel[width-1] = (src_pixel[width-3] + 5 * src_pixel[width-2] + 10 * src_pixel[width-1]) / 16;

			// next line
			src_pixel += pitch;
			dst_pixel += pitch;
		}

		// vertical convolution h_dib -> v_dib

		src_pixel = (float*)FreeImage_GetBits(h_dib);
		dst_pixel = (float*)FreeImage_GetBits(v_dib);

		for(unsigned x = 0; x < width; x++) {		
			// work on column x
			for(unsigned y = 2; y < height - 2; y++) {
				const unsigned index = y*pitch + x;
				dst_pixel[index] = src_pixel[index-2*pitch] + src_pixel[index+2*pitch] + 4 * (src_pixel[index-pitch] + src_pixel[index+pitch]) + 6 * src_pixel[index];
				dst_pixel[index] /= 16;
			}
			// boundary mirroring
			dst_pixel[x] = (2 * src_pixel[x+2*pitch] + 8 * src_pixel[x+pitch] + 6 * src_pixel[x]) / 16;
			dst_pixel[x+pitch] = (src_pixel[x+3*pitch] + 4 * (src_pixel[x] + src_pixel[x+2*pitch]) + 7 * src_pixel[x+pitch]) / 16;
			dst_pixel[(height-2)*pitch+x] = (src_pixel[(height-4)*pitch+x] + 5 * src_pixel[(height-1)*pitch+x] + 4 * src_pixel[(height-3)*pitch+x] + 6 * src_pixel[(height-2)*pitch+x]) / 16;
			dst_pixel[(height-1)*pitch+x] = (src_pixel[(height-3)*pitch+x] + 5 * src_pixel[(height-2)*pitch+x] + 10 * src_pixel[(height-1)*pitch+x]) / 16;
		}

		FreeImage_Unload(h_dib); h_dib = NULL;

		// perform downsampling

		dst = FreeImage_Rescale(v_dib, width/2, height/2, FILTER_BILINEAR);

		FreeImage_Unload(v_dib);

		return dst;

	} catch(int) {
		if(h_dib) FreeImage_Unload(h_dib);
		if(v_dib) FreeImage_Unload(v_dib);
		if(dst) FreeImage_Unload(dst);
		return NULL;
	}
}

/**
Compute a Gaussian pyramid using the specified number of levels. 
@param H Original bitmap
@param pyramid Resulting pyramid array 
@param nlevels Number of resolution levels
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL GaussianPyramid(FIBITMAP *H, FIBITMAP **pyramid, int nlevels) {
	try {
		// first level is the original image
		pyramid[0] = FreeImage_Clone(H);
		if(pyramid[0] == NULL) throw(1);
		// compute next levels
		for(int k = 1; k < nlevels; k++) {
			pyramid[k] = GaussianLevel5x5(pyramid[k-1]);
			if(pyramid[k] == NULL) throw(1);
		}
		return TRUE;
	} catch(int) {
		for(int k = 0; k < nlevels; k++) {
			if(pyramid[k] != NULL) {
				FreeImage_Unload(pyramid[k]);
				pyramid[k] = NULL;
			}
		}
		return FALSE;
	}
}

/**
Compute the gradient magnitude of an input image H using central differences, 
and returns the average gradient. 
@param H Input image
@param avgGrad [out] Average gradient
@param k Level number
@return Returns the gradient magnitude if successful, returns NULL otherwise
@see GradientPyramid
*/
static FIBITMAP* GradientLevel(FIBITMAP *H, float *avgGrad, int k) {
	FIBITMAP *G = NULL;

	try {
		const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(H);
		if(image_type != FIT_FLOAT) throw(1);

		const unsigned width = FreeImage_GetWidth(H);
		const unsigned height = FreeImage_GetHeight(H);

		G = FreeImage_AllocateT(image_type, width, height);
		if(!G) throw(1);
		
		const unsigned pitch = FreeImage_GetPitch(H) / sizeof(float);
		
		const float divider = (float)(1 << (k + 1));
		float average = 0;
		
		float *src_pixel = (float*)FreeImage_GetBits(H);
		float *dst_pixel = (float*)FreeImage_GetBits(G);

		for(unsigned y = 0; y < height; y++) {
			const unsigned n = (y == 0 ? 0 : y-1);
			const unsigned s = (y+1 == height ? y : y+1);
			for(unsigned x = 0; x < width; x++) {
				const unsigned w = (x == 0 ? 0 : x-1);
				const unsigned e = (x+1 == width ? x : x+1);		
				// central difference
				const float gx = (src_pixel[y*pitch+e] - src_pixel[y*pitch+w]) / divider; // [Hk(x+1, y) - Hk(x-1, y)] / 2**(k+1)
				const float gy = (src_pixel[s*pitch+x] - src_pixel[n*pitch+x]) / divider; // [Hk(x, y+1) - Hk(x, y-1)] / 2**(k+1)
				// gradient
				dst_pixel[x] = sqrt(gx*gx + gy*gy);
				// average gradient
				average += dst_pixel[x];
			}
			// next line
			dst_pixel += pitch;
		}
		
		*avgGrad = average / (width * height);

		return G;

	} catch(int) {
		if(G) FreeImage_Unload(G);
		return NULL;
	}
}

/**
Calculate gradient magnitude and its average value on each pyramid level
@param pyramid Gaussian pyramid (nlevels levels)
@param nlevels Number of levels
@param gradients [out] Gradient pyramid (nlevels levels)
@param avgGrad [out] Average gradient on each level (array of size nlevels)
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL GradientPyramid(FIBITMAP **pyramid, int nlevels, FIBITMAP **gradients, float *avgGrad) {
	try {
		for(int k = 0; k < nlevels; k++) {
			FIBITMAP *Hk = pyramid[k];
			gradients[k] = GradientLevel(Hk, &avgGrad[k], k);
			if(gradients[k] == NULL) throw(1);
		}
		return TRUE;
	} catch(int) {
		for(int k = 0; k < nlevels; k++) {
			if(gradients[k] != NULL) {
				FreeImage_Unload(gradients[k]);
				gradients[k] = NULL;
			}
		}
		return FALSE;
	}
}

/**
Compute the gradient attenuation function PHI(x, y)
@param gradients Gradient pyramid (nlevels levels)
@param avgGrad Average gradient on each level (array of size nlevels)
@param nlevels Number of levels
@param alpha Parameter alpha in the paper
@param beta Parameter beta in the paper
@return Returns the attenuation matrix Phi if successful, returns NULL otherwise
*/
static FIBITMAP* PhiMatrix(FIBITMAP **gradients, float *avgGrad, int nlevels, float alpha, float beta) {
	float *src_pixel, *dst_pixel;
	FIBITMAP **phi = NULL;

	try {
		phi = (FIBITMAP**)malloc(nlevels * sizeof(FIBITMAP*));
		if(!phi) throw(1);
		memset(phi, 0, nlevels * sizeof(FIBITMAP*));

		for(int k = nlevels-1; k >= 0; k--) {
			// compute phi(k)

			FIBITMAP *Gk = gradients[k];

			const unsigned width = FreeImage_GetWidth(Gk);
			const unsigned height = FreeImage_GetHeight(Gk);
			const unsigned pitch = FreeImage_GetPitch(Gk) / sizeof(float);

			// parameter alpha is 0.1 times the average gradient magnitude
			// also, note the factor of 2**k in the denominator; 
			// that is there to correct for the fact that an average gradient avgGrad(H) over 2**k pixels 
			// in the original image will appear as a gradient grad(Hk) = 2**k*avgGrad(H) over a single pixel in Hk. 
			float ALPHA =  alpha * avgGrad[k] * (float)((int)1 << k);
			if(ALPHA == 0) ALPHA = EPSILON;

			phi[k] = FreeImage_AllocateT(FIT_FLOAT, width, height);
			if(!phi[k]) throw(1);
			
			src_pixel = (float*)FreeImage_GetBits(Gk);
			dst_pixel = (float*)FreeImage_GetBits(phi[k]);
			for(unsigned y = 0; y < height; y++) {
				for(unsigned x = 0; x < width; x++) {
					// compute (alpha / grad) * (grad / alpha) ** beta
					const float v = src_pixel[x] / ALPHA;
					const float value = (float)pow((float)v, (float)(beta-1));
					dst_pixel[x] = (value > 1) ? 1 : value;
				}
				// next line
				src_pixel += pitch;
				dst_pixel += pitch;
			}

			if(k < nlevels-1) {
				// compute PHI(k) = L( PHI(k+1) ) * phi(k)
				FIBITMAP *L = FreeImage_Rescale(phi[k+1], width, height, FILTER_BILINEAR);
				if(!L) throw(1);

				src_pixel = (float*)FreeImage_GetBits(L);
				dst_pixel = (float*)FreeImage_GetBits(phi[k]);
				for(unsigned y = 0; y < height; y++) {
					for(unsigned x = 0; x < width; x++) {
						dst_pixel[x] *= src_pixel[x];
					}
					// next line
					src_pixel += pitch;
					dst_pixel += pitch;
				}

				FreeImage_Unload(L);

				// PHI(k+1) is no longer needed
				FreeImage_Unload(phi[k+1]);
				phi[k+1] = NULL;
			}

			// next level
		}

		// get the final result and return
		FIBITMAP *dst = phi[0];

		free(phi);

		return dst;

	} catch(int) {
		if(phi) {
			for(int k = nlevels-1; k >= 0; k--) {
				if(phi[k]) FreeImage_Unload(phi[k]);
			}
			free(phi);
		}
		return NULL;
	}
}

/**
Compute gradients in x and y directions, attenuate them with the attenuation matrix, 
then compute the divergence div G from the attenuated gradient. 
@param H Normalized luminance
@param PHI Attenuation matrix
@return Returns the divergence matrix if successful, returns NULL otherwise
*/
static FIBITMAP* Divergence(FIBITMAP *H, FIBITMAP *PHI) {
	FIBITMAP *Gx = NULL, *Gy = NULL, *divG = NULL;
	float *phi, *h, *gx, *gy, *divg;

	try {
		const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(H);
		if(image_type != FIT_FLOAT) throw(1);

		const unsigned width = FreeImage_GetWidth(H);
		const unsigned height = FreeImage_GetHeight(H);

		Gx = FreeImage_AllocateT(image_type, width, height);
		if(!Gx) throw(1);
		Gy = FreeImage_AllocateT(image_type, width, height);
		if(!Gy) throw(1);
		
		const unsigned pitch = FreeImage_GetPitch(H) / sizeof(float);
		
		// perform gradient attenuation

		phi = (float*)FreeImage_GetBits(PHI);
		h   = (float*)FreeImage_GetBits(H);
		gx  = (float*)FreeImage_GetBits(Gx);
		gy  = (float*)FreeImage_GetBits(Gy);

		for(unsigned y = 0; y < height; y++) {
			const unsigned s = (y+1 == height ? y : y+1);
			for(unsigned x = 0; x < width; x++) {				
				const unsigned e = (x+1 == width ? x : x+1);
				// forward difference
				const unsigned index = y*pitch + x;
				const float phi_xy = phi[index];
				const float h_xy   = h[index];
				gx[x] = (h[y*pitch+e] - h_xy) * phi_xy; // [H(x+1, y) - H(x, y)] * PHI(x, y)
				gy[x] = (h[s*pitch+x] - h_xy) * phi_xy; // [H(x, y+1) - H(x, y)] * PHI(x, y)
			}
			// next line
			gx += pitch;
			gy += pitch;
		}

		// calculate the divergence

		divG = FreeImage_AllocateT(image_type, width, height);
		if(!divG) throw(1);
		
		gx  = (float*)FreeImage_GetBits(Gx);
		gy  = (float*)FreeImage_GetBits(Gy);
		divg = (float*)FreeImage_GetBits(divG);

		for(unsigned y = 0; y < height; y++) {
			for(unsigned x = 0; x < width; x++) {				
				// backward difference approximation
				// divG = Gx(x, y) - Gx(x-1, y) + Gy(x, y) - Gy(x, y-1)
				const unsigned index = y*pitch + x;
				divg[index] = gx[index] + gy[index];
				if(x > 0) divg[index] -= gx[index-1];
				if(y > 0) divg[index] -= gy[index-pitch];
			}
		}

		// no longer needed ... 
		FreeImage_Unload(Gx);
		FreeImage_Unload(Gy);

		// return the divergence
		return divG;

	} catch(int) {
		if(Gx) FreeImage_Unload(Gx);
		if(Gy) FreeImage_Unload(Gy);
		if(divG) FreeImage_Unload(divG);
		return NULL;
	}
}

/**
Given the luminance channel, find max & min luminance values, 
normalize to range 0..100 and take the logarithm. 
@param Y Image luminance
@return Returns the normalized luminance H if successful, returns NULL otherwise
*/
static FIBITMAP* LogLuminance(FIBITMAP *Y) {
	FIBITMAP *H = NULL;

	try {
		// get the luminance channel
		FIBITMAP *H = FreeImage_Clone(Y);
		if(!H) throw(1);

		const unsigned width  = FreeImage_GetWidth(H);
		const unsigned height = FreeImage_GetHeight(H);
		const unsigned pitch  = FreeImage_GetPitch(H);

		// find max & min luminance values
		float maxLum = -1e20F, minLum = 1e20F;

		BYTE *bits = (BYTE*)FreeImage_GetBits(H);
		for(unsigned y = 0; y < height; y++) {
			const float *pixel = (float*)bits;
			for(unsigned x = 0; x < width; x++) {
				const float value = pixel[x];
				maxLum = (maxLum < value) ? value : maxLum;	// max Luminance in the scene
				minLum = (minLum < value) ? minLum : value;	// min Luminance in the scene
			}
			// next line
			bits += pitch;
		}
		if(maxLum == minLum) throw(1);

		// normalize to range 0..100 and take the logarithm
		const float scale = 100.F / (maxLum - minLum);
		bits = (BYTE*)FreeImage_GetBits(H);
		for(unsigned y = 0; y < height; y++) {
			float *pixel = (float*)bits;
			for(unsigned x = 0; x < width; x++) {
				const float value = (pixel[x] - minLum) * scale;
				pixel[x] = log(value + EPSILON);
			}
			// next line
			bits += pitch;
		}

		return H;

	} catch(int) {
		if(H) FreeImage_Unload(H);
		return NULL;
	}
}

/**
Given a normalized luminance, perform exponentiation and recover the log compressed image 
@param Y Input/Output luminance image
*/
static void ExpLuminance(FIBITMAP *Y) {
	const unsigned width = FreeImage_GetWidth(Y);
	const unsigned height = FreeImage_GetHeight(Y);
	const unsigned pitch = FreeImage_GetPitch(Y);

	BYTE *bits = (BYTE*)FreeImage_GetBits(Y);
	for(unsigned y = 0; y < height; y++) {
		float *pixel = (float*)bits;
		for(unsigned x = 0; x < width; x++) {
			pixel[x] = exp(pixel[x]) - EPSILON;
		}
		bits += pitch;
	}
}

// --------------------------------------------------------------------------

/**
Gradient Domain HDR tone mapping operator
@param Y Image luminance values
@param alpha Parameter alpha of the paper (suggested value is 0.1)
@param beta Parameter beta of the paper (suggested value is between 0.8 and 0.9)
@return returns the tone mapped luminance
*/
static FIBITMAP* tmoFattal02(FIBITMAP *Y, float alpha, float beta) {
	const unsigned MIN_PYRAMID_SIZE = 32;	// minimun size (width or height) of the coarsest level of the pyramid

	FIBITMAP *H = NULL;
	FIBITMAP **pyramid = NULL;
	FIBITMAP **gradients = NULL;
	FIBITMAP *phy = NULL;
	FIBITMAP *divG = NULL;
	FIBITMAP *U = NULL;
	float *avgGrad = NULL;

	int k;
	int nlevels = 0;

	try {
		// get the normalized luminance
		FIBITMAP *H = LogLuminance(Y);
		if(!H) throw(1);
		
		// get the number of levels for the pyramid
		const unsigned width = FreeImage_GetWidth(H);
		const unsigned height = FreeImage_GetHeight(H);
		unsigned minsize = MIN(width, height);
		while(minsize >= MIN_PYRAMID_SIZE) {
			nlevels++;
			minsize /= 2;
		}

		// create the Gaussian pyramid
		pyramid = (FIBITMAP**)malloc(nlevels * sizeof(FIBITMAP*));
		if(!pyramid) throw(1);
		memset(pyramid, 0, nlevels * sizeof(FIBITMAP*));

		if(!GaussianPyramid(H, pyramid, nlevels)) throw(1);

		// calculate gradient magnitude and its average value on each pyramid level
		gradients = (FIBITMAP**)malloc(nlevels * sizeof(FIBITMAP*));
		if(!gradients) throw(1);
		memset(gradients, 0, nlevels * sizeof(FIBITMAP*));
		avgGrad = (float*)malloc(nlevels * sizeof(float));
		if(!avgGrad) throw(1);

		if(!GradientPyramid(pyramid, nlevels, gradients, avgGrad)) throw(1);

		// free the Gaussian pyramid
		for(k = 0; k < nlevels; k++) {
			if(pyramid[k]) FreeImage_Unload(pyramid[k]);
		}
		free(pyramid); pyramid = NULL;

		// compute the gradient attenuation function PHI(x, y)
		phy = PhiMatrix(gradients, avgGrad, nlevels, alpha, beta);
		if(!phy) throw(1);

		// free the gradient pyramid
		for(k = 0; k < nlevels; k++) {
			if(gradients[k]) FreeImage_Unload(gradients[k]);
		}
		free(gradients); gradients = NULL;
		free(avgGrad); avgGrad = NULL;

		// compute gradients in x and y directions, attenuate them with the attenuation matrix, 
		// then compute the divergence div G from the attenuated gradient. 
		divG = Divergence(H, phy);
		if(!divG) throw(1);

		// H & phy no longer needed
		FreeImage_Unload(H); H = NULL;
		FreeImage_Unload(phy); phy = NULL;

		// solve the PDE (Poisson equation) using a multigrid solver and 3 cycles
		FIBITMAP *U = FreeImage_MultigridPoissonSolver(divG, 3);
		if(!U) throw(1);

		FreeImage_Unload(divG);

		// perform exponentiation and recover the log compressed image
		ExpLuminance(U);

		return U;

	} catch(int) {
		if(H) FreeImage_Unload(H);
		if(pyramid) {
			for(int i = 0; i < nlevels; i++) {
				if(pyramid[i]) FreeImage_Unload(pyramid[i]);
			}
			free(pyramid);
		}
		if(gradients) {
			for(int i = 0; i < nlevels; i++) {
				if(gradients[i]) FreeImage_Unload(gradients[i]);
			}
			free(gradients);
		}
		if(avgGrad) free(avgGrad);
		if(phy) FreeImage_Unload(phy);
		if(divG) FreeImage_Unload(divG);
		if(U) FreeImage_Unload(U);

		return NULL;
	}
}

// ----------------------------------------------------------
//  Main algorithm
// ----------------------------------------------------------

/**
Apply the Gradient Domain High Dynamic Range Compression to a RGBF image and convert to 24-bit RGB
@param dib Input RGBF / RGB16 image
@param color_saturation Color saturation (s parameter in the paper) in [0.4..0.6]
@param attenuation Atenuation factor (beta parameter in the paper) in [0.8..0.9]
@return Returns a 24-bit RGB image if successful, returns NULL otherwise
*/
FIBITMAP* DLL_CALLCONV 
FreeImage_TmoFattal02(FIBITMAP *dib, double color_saturation, double attenuation) {	
	const float alpha = 0.1F;									// parameter alpha = 0.1
	const float beta = (float)MAX(0.8, MIN(0.9, attenuation));	// parameter beta = [0.8..0.9]
	const float s = (float)MAX(0.4, MIN(0.6, color_saturation));// exponent s controls color saturation = [0.4..0.6]

	FIBITMAP *src = NULL;
	FIBITMAP *Yin = NULL;
	FIBITMAP *Yout = NULL;
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	try {

		// convert to RGBF
		src = FreeImage_ConvertToRGBF(dib);
		if(!src) throw(1);

		// get the luminance channel
		Yin = ConvertRGBFToY(src);
		if(!Yin) throw(1);

		// perform the tone mapping
		Yout = tmoFattal02(Yin, alpha, beta);
		if(!Yout) throw(1);

		// clip low and high values and normalize to [0..1]
		//NormalizeY(Yout, 0.001F, 0.995F);
		NormalizeY(Yout, 0, 1);

		// compress the dynamic range

		const unsigned width = FreeImage_GetWidth(src);
		const unsigned height = FreeImage_GetHeight(src);

		const unsigned rgb_pitch = FreeImage_GetPitch(src);
		const unsigned y_pitch = FreeImage_GetPitch(Yin);

		BYTE *bits      = (BYTE*)FreeImage_GetBits(src);
		BYTE *bits_yin  = (BYTE*)FreeImage_GetBits(Yin);
		BYTE *bits_yout = (BYTE*)FreeImage_GetBits(Yout);

		for(unsigned y = 0; y < height; y++) {
			float *Lin = (float*)bits_yin;
			float *Lout = (float*)bits_yout;
			float *color = (float*)bits;
			for(unsigned x = 0; x < width; x++) {
				for(unsigned c = 0; c < 3; c++) {
					*color = (Lin[x] > 0) ? pow(*color/Lin[x], s) * Lout[x] : 0;
					color++;
				}
			}
			bits += rgb_pitch;
			bits_yin += y_pitch;
			bits_yout += y_pitch;
		}

		// not needed anymore
		FreeImage_Unload(Yin);  Yin  = NULL;
		FreeImage_Unload(Yout); Yout = NULL;

		// clamp image highest values to display white, then convert to 24-bit RGB
		dst = ClampConvertRGBFTo24(src);

		// clean-up and return
		FreeImage_Unload(src); src = NULL;

		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, dib);
		
		return dst;

	} catch(int) {
		if(src) FreeImage_Unload(src);
		if(Yin) FreeImage_Unload(Yin);
		if(Yout) FreeImage_Unload(Yout);
		return NULL;
	}
}

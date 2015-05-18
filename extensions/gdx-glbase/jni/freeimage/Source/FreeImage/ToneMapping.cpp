// ==========================================================
// Tone mapping operators
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

/**
Performs a tone mapping on a 48-bit RGB or a 96-bit RGBF image and returns a 24-bit image. 
The meaning of the parameters depends on the choosen algorithm. 
When both parameters are set to zero, a default set of parameters is used. 
@param dib Input RGB/RGBF image
@param tmo Tone mapping operator
@param first_param First parameter of the algorithm
@param second_param Second parameter of the algorithm
return Returns a 24-bit tone mapped image if successful, returns NULL otherwise
*/ 
FIBITMAP * DLL_CALLCONV
FreeImage_ToneMapping(FIBITMAP *dib, FREE_IMAGE_TMO tmo, double first_param, double second_param) {
	if(FreeImage_HasPixels(dib)) {
		switch(tmo) {
			// Adaptive logarithmic mapping (F. Drago, 2003)
			case FITMO_DRAGO03:
				if((first_param == 0) && (second_param == 0)) {
					// use default values (gamma = 2.2, exposure = 0)
					return FreeImage_TmoDrago03(dib, 2.2, 0);
				} else {
					// use user's value
					return FreeImage_TmoDrago03(dib, first_param, second_param);
				}
				break;
			// Dynamic range reduction inspired by photoreceptor phhysiology (E. Reinhard, 2005)
			case FITMO_REINHARD05:
				if((first_param == 0) && (second_param == 0)) {
					// use default values by setting intensity to 0 and contrast to 0
					return FreeImage_TmoReinhard05(dib, 0, 0);
				} else {
					// use user's value
					return FreeImage_TmoReinhard05(dib, first_param, second_param);
				}
				break;
			// Gradient Domain HDR Compression (R. Fattal, 2002)
			case FITMO_FATTAL02:
				if((first_param == 0) && (second_param == 0)) {
					// use default values by setting color saturation to 0.5 and attenuation to 0.85
					return FreeImage_TmoFattal02(dib, 0.5, 0.85);
				} else {
					// use user's value
					return FreeImage_TmoFattal02(dib, first_param, second_param);
				}
				break;
		}
	}

	return NULL;
}



// ==========================================================
// XYZ to RGB TIFF conversion routines
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

void tiff_ConvertLineXYZToRGB(BYTE *target, BYTE *source, double stonits, int width_in_pixels) {
	FIRGBF *rgbf = (FIRGBF*)target;
	float *xyz = (float*)source;
	
	for (int cols = 0; cols < width_in_pixels; cols++) {
		// assume CCIR-709 primaries (matrix from tif_luv.c)
		// LOG Luv XYZ (D65) -> sRGB (CIE Illuminant E)
		rgbf->red	= (float)( 2.690*xyz[0] + -1.276*xyz[1] + -0.414*xyz[2]);
		rgbf->green	= (float)(-1.022*xyz[0] +  1.978*xyz[1] +  0.044*xyz[2]);
		rgbf->blue	= (float)( 0.061*xyz[0] + -0.224*xyz[1] +  1.163*xyz[2]);
		
		/*
		if (stonits != 0.0) {
			rgbf->red	= (float)(rgbf->red   * stonits);
			rgbf->green	= (float)(rgbf->green * stonits);
			rgbf->blue	= (float)(rgbf->blue  * stonits);
		} 
		*/

		rgbf++;
		xyz += 3;
	}
}

void tiff_ConvertLineRGBToXYZ(BYTE *target, BYTE *source, int width_in_pixels) {
	FIRGBF *rgbf = (FIRGBF*)source;
	float *xyz = (float*)target;
	
	for (int cols = 0; cols < width_in_pixels; cols++) {
		// assume CCIR-709 primaries, whitepoint x = 1/3 y = 1/3 (D_E)
		// "The LogLuv Encoding for Full Gamut, High Dynamic Range Images" <G.Ward>
		// sRGB ( CIE Illuminant E ) -> LOG Luv XYZ (D65)
		xyz[0] =  (float)(0.497*rgbf->red +  0.339*rgbf->green +  0.164*rgbf->blue);
		xyz[1] =  (float)(0.256*rgbf->red +  0.678*rgbf->green +  0.066*rgbf->blue);
		xyz[2] =  (float)(0.023*rgbf->red +  0.113*rgbf->green +  0.864*rgbf->blue);

		rgbf++;
		xyz += 3;
	}
}


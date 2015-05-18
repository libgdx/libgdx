// ==========================================================
// Bitmap rotation using B-Splines
//
// Design and implementation by
// - Philippe Thévenaz (philippe.thevenaz@epfl.ch)
// Adaptation for FreeImage by
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

/* 
==========================================================
This code was taken and adapted from the following reference : 

[1] Philippe Thévenaz, Spline interpolation, a C source code 
implementation. http://bigwww.epfl.ch/thevenaz/

It implements ideas described in the following papers : 

[2] Unser M., Splines: A Perfect Fit for Signal and Image Processing. 
IEEE Signal Processing Magazine, vol. 16, no. 6, pp. 22-38, November 1999. 

[3] Unser M., Aldroubi A., Eden M., B-Spline Signal Processing: Part I--Theory.
IEEE Transactions on Signal Processing, vol. 41, no. 2, pp. 821-832, February 1993. 

[4] Unser M., Aldroubi A., Eden M., B-Spline Signal Processing: Part II--Efficient Design and Applications.
IEEE Transactions on Signal Processing, vol. 41, no. 2, pp. 834-848, February 1993.

========================================================== 
*/


#include <float.h>
#include "FreeImage.h"
#include "Utilities.h"

#define PI	((double)3.14159265358979323846264338327950288419716939937510)

#define ROTATE_QUADRATIC 2L	// Use B-splines of degree 2 (quadratic interpolation)
#define ROTATE_CUBIC     3L	// Use B-splines of degree 3 (cubic interpolation)
#define ROTATE_QUARTIC   4L	// Use B-splines of degree 4 (quartic interpolation)
#define ROTATE_QUINTIC   5L	// Use B-splines of degree 5 (quintic interpolation)


/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Prototypes definition

static void ConvertToInterpolationCoefficients(double *c, long DataLength, double *z, long NbPoles,	double Tolerance);
static double InitialCausalCoefficient(double *c, long DataLength, double z, double Tolerance);
static void GetColumn(double *Image, long Width, long x, double *Line, long Height);
static void	GetRow(double *Image, long y, double *Line, long Width);
static double InitialAntiCausalCoefficient(double *c, long DataLength, double z);
static void	PutColumn(double *Image, long Width, long x, double *Line, long Height);
static void	PutRow(double *Image, long y, double *Line, long Width);
static bool SamplesToCoefficients(double *Image, long Width, long Height, long spline_degree);
static double InterpolatedValue(double *Bcoeff, long Width, long Height, double x, double y, long spline_degree);

static FIBITMAP * Rotate8Bit(FIBITMAP *dib, double angle, double x_shift, double y_shift, double x_origin, double y_origin, long spline_degree, BOOL use_mask);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Coefficients routines

/**
 ConvertToInterpolationCoefficients

 @param c Input samples --> output coefficients
 @param DataLength Number of samples or coefficients
 @param z Poles
 @param NbPoles Number of poles
 @param Tolerance Admissible relative error
*/
static void 
ConvertToInterpolationCoefficients(double *c, long DataLength, double *z, long NbPoles,	double Tolerance) {
	double	Lambda = 1;
	long	n, k;

	// special case required by mirror boundaries
	if(DataLength == 1L) {
		return;
	}
	// compute the overall gain
	for(k = 0L; k < NbPoles; k++) {
		Lambda = Lambda * (1.0 - z[k]) * (1.0 - 1.0 / z[k]);
	}
	// apply the gain 
	for (n = 0L; n < DataLength; n++) {
		c[n] *= Lambda;
	}
	// loop over all poles 
	for (k = 0L; k < NbPoles; k++) {
		// causal initialization 
		c[0] = InitialCausalCoefficient(c, DataLength, z[k], Tolerance);
		// causal recursion 
		for (n = 1L; n < DataLength; n++) {
			c[n] += z[k] * c[n - 1L];
		}
		// anticausal initialization 
		c[DataLength - 1L] = InitialAntiCausalCoefficient(c, DataLength, z[k]);
		// anticausal recursion 
		for (n = DataLength - 2L; 0 <= n; n--) {
			c[n] = z[k] * (c[n + 1L] - c[n]);
		}
	}
} 

/**
 InitialCausalCoefficient

 @param c Coefficients
 @param DataLength Number of coefficients
 @param z Actual pole
 @param Tolerance Admissible relative error
 @return
*/
static double 
InitialCausalCoefficient(double	*c, long DataLength, double	z, double Tolerance) {
	double	Sum, zn, z2n, iz;
	long	n, Horizon;

	// this initialization corresponds to mirror boundaries 
	Horizon = DataLength;
	if(Tolerance > 0) {
		Horizon = (long)ceil(log(Tolerance) / log(fabs(z)));
	}
	if(Horizon < DataLength) {
		// accelerated loop
		zn = z;
		Sum = c[0];
		for (n = 1L; n < Horizon; n++) {
			Sum += zn * c[n];
			zn *= z;
		}
		return(Sum);
	}
	else {
		// full loop 
		zn = z;
		iz = 1.0 / z;
		z2n = pow(z, (double)(DataLength - 1L));
		Sum = c[0] + z2n * c[DataLength - 1L];
		z2n *= z2n * iz;
		for (n = 1L; n <= DataLength - 2L; n++) {
			Sum += (zn + z2n) * c[n];
			zn *= z;
			z2n *= iz;
		}
		return(Sum / (1.0 - zn * zn));
	}
}

/**
 GetColumn

 @param Image Input image array
 @param Width Width of the image
 @param x x coordinate of the selected line
 @param Line Output linear array
 @param Height Length of the line
*/
static void 
GetColumn(double *Image, long Width, long x, double *Line, long Height) {
	long y;

	Image = Image + x;
	for(y = 0L; y < Height; y++) {
		Line[y] = (double)*Image;
		Image += Width;
	}
}

/**
 GetRow

 @param Image Input image array
 @param y y coordinate of the selected line
 @param Line Output linear array
 @param Width Length of the line
*/
static void	
GetRow(double *Image, long y, double *Line, long Width) {
	long	x;

	Image = Image + (y * Width);
	for(x = 0L; x < Width; x++) {
		Line[x] = (double)*Image++;
	}
}

/**
 InitialAntiCausalCoefficient

 @param c Coefficients
 @param DataLength Number of samples or coefficients
 @param z Actual pole
 @return
*/
static double 
InitialAntiCausalCoefficient(double	*c, long DataLength, double	z) {
	// this initialization corresponds to mirror boundaries
	return((z / (z * z - 1.0)) * (z * c[DataLength - 2L] + c[DataLength - 1L]));
}

/**
 PutColumn

 @param Image Output image array
 @param Width Width of the image
 @param x x coordinate of the selected line
 @param Line Input linear array
 @param Height Length of the line and height of the image
*/
static void	
PutColumn(double *Image, long Width, long x, double *Line, long Height) {
	long	y;

	Image = Image + x;
	for(y = 0L; y < Height; y++) {
		*Image = (double)Line[y];
		Image += Width;
	}
}

/**
 PutRow

 @param Image Output image array
 @param y y coordinate of the selected line
 @param Line Input linear array
 @param Width length of the line and width of the image
*/
static void	
PutRow(double *Image, long y, double *Line, long Width) {
	long	x;

	Image = Image + (y * Width);
	for(x = 0L; x < Width; x++) {
		*Image++ = (double)Line[x];
	}
}

/**
 SamplesToCoefficients.<br>
 Implement the algorithm that converts the image samples into B-spline coefficients. 
 This efficient procedure essentially relies on the three papers cited above; 
 data are processed in-place. 
 Even though this algorithm is robust with respect to quantization, 
 we advocate the use of a floating-point format for the data. 

 @param Image Input / Output image (in-place processing)
 @param Width Width of the image
 @param Height Height of the image
 @param spline_degree Degree of the spline model
 @return Returns true if success, false otherwise
*/
static bool	
SamplesToCoefficients(double *Image, long Width, long Height, long spline_degree) {
	double	*Line;
	double	Pole[2];
	long	NbPoles;
	long	x, y;

	// recover the poles from a lookup table
	switch (spline_degree) {
		case 2L:
			NbPoles = 1L;
			Pole[0] = sqrt(8.0) - 3.0;
			break;
		case 3L:
			NbPoles = 1L;
			Pole[0] = sqrt(3.0) - 2.0;
			break;
		case 4L:
			NbPoles = 2L;
			Pole[0] = sqrt(664.0 - sqrt(438976.0)) + sqrt(304.0) - 19.0;
			Pole[1] = sqrt(664.0 + sqrt(438976.0)) - sqrt(304.0) - 19.0;
			break;
		case 5L:
			NbPoles = 2L;
			Pole[0] = sqrt(135.0 / 2.0 - sqrt(17745.0 / 4.0)) + sqrt(105.0 / 4.0)
				- 13.0 / 2.0;
			Pole[1] = sqrt(135.0 / 2.0 + sqrt(17745.0 / 4.0)) - sqrt(105.0 / 4.0)
				- 13.0 / 2.0;
			break;
		default:
			// Invalid spline degree
			return false;
	}

	// convert the image samples into interpolation coefficients 

	// in-place separable process, along x 
	Line = (double *)malloc(Width * sizeof(double));
	if (Line == NULL) {
		// Row allocation failed
		return false;
	}
	for (y = 0L; y < Height; y++) {
		GetRow(Image, y, Line, Width);
		ConvertToInterpolationCoefficients(Line, Width, Pole, NbPoles, DBL_EPSILON);
		PutRow(Image, y, Line, Width);
	}
	free(Line);

	// in-place separable process, along y 
	Line = (double *)malloc(Height * sizeof(double));
	if (Line == NULL) {
		// Column allocation failed
		return false;
	}
	for (x = 0L; x < Width; x++) {
		GetColumn(Image, Width, x, Line, Height);
		ConvertToInterpolationCoefficients(Line, Height, Pole, NbPoles, DBL_EPSILON);
		PutColumn(Image, Width, x, Line, Height);
	}
	free(Line);

	return true;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Interpolation routines

/**
Perform the bidimensional interpolation of an image.
Given an array of spline coefficients, return the value of 
the underlying continuous spline model, sampled at the location (x, y). 
The model degree can be 2 (quadratic), 3 (cubic), 4 (quartic), or 5 (quintic).

@param Bcoeff Input B-spline array of coefficients
@param Width Width of the image
@param Height Height of the image
@param x x coordinate where to interpolate
@param y y coordinate where to interpolate
@param spline_degree Degree of the spline model
@return Returns the value of the underlying continuous spline model, 
sampled at the location (x, y)
*/
static double 
InterpolatedValue(double *Bcoeff, long Width, long Height, double x, double y, long spline_degree) {
	double	*p;
	double	xWeight[6], yWeight[6];
	double	interpolated;
	double	w, w2, w4, t, t0, t1;
	long	xIndex[6], yIndex[6];
	long	Width2 = 2L * Width - 2L, Height2 = 2L * Height - 2L;
	long	i, j, k;

	// compute the interpolation indexes
	if (spline_degree & 1L) {
		i = (long)floor(x) - spline_degree / 2L;
		j = (long)floor(y) - spline_degree / 2L;
		for(k = 0; k <= spline_degree; k++) {
			xIndex[k] = i++;
			yIndex[k] = j++;
		}
	}
	else {
		i = (long)floor(x + 0.5) - spline_degree / 2L;
		j = (long)floor(y + 0.5) - spline_degree / 2L;
		for (k = 0; k <= spline_degree; k++) {
			xIndex[k] = i++;
			yIndex[k] = j++;
		}
	}

	// compute the interpolation weights
	switch (spline_degree) {
		case 2L:
			/* x */
			w = x - (double)xIndex[1];
			xWeight[1] = 3.0 / 4.0 - w * w;
			xWeight[2] = (1.0 / 2.0) * (w - xWeight[1] + 1.0);
			xWeight[0] = 1.0 - xWeight[1] - xWeight[2];
			/* y */
			w = y - (double)yIndex[1];
			yWeight[1] = 3.0 / 4.0 - w * w;
			yWeight[2] = (1.0 / 2.0) * (w - yWeight[1] + 1.0);
			yWeight[0] = 1.0 - yWeight[1] - yWeight[2];
			break;
		case 3L:
			/* x */
			w = x - (double)xIndex[1];
			xWeight[3] = (1.0 / 6.0) * w * w * w;
			xWeight[0] = (1.0 / 6.0) + (1.0 / 2.0) * w * (w - 1.0) - xWeight[3];
			xWeight[2] = w + xWeight[0] - 2.0 * xWeight[3];
			xWeight[1] = 1.0 - xWeight[0] - xWeight[2] - xWeight[3];
			/* y */
			w = y - (double)yIndex[1];
			yWeight[3] = (1.0 / 6.0) * w * w * w;
			yWeight[0] = (1.0 / 6.0) + (1.0 / 2.0) * w * (w - 1.0) - yWeight[3];
			yWeight[2] = w + yWeight[0] - 2.0 * yWeight[3];
			yWeight[1] = 1.0 - yWeight[0] - yWeight[2] - yWeight[3];
			break;
		case 4L:
			/* x */
			w = x - (double)xIndex[2];
			w2 = w * w;
			t = (1.0 / 6.0) * w2;
			xWeight[0] = 1.0 / 2.0 - w;
			xWeight[0] *= xWeight[0];
			xWeight[0] *= (1.0 / 24.0) * xWeight[0];
			t0 = w * (t - 11.0 / 24.0);
			t1 = 19.0 / 96.0 + w2 * (1.0 / 4.0 - t);
			xWeight[1] = t1 + t0;
			xWeight[3] = t1 - t0;
			xWeight[4] = xWeight[0] + t0 + (1.0 / 2.0) * w;
			xWeight[2] = 1.0 - xWeight[0] - xWeight[1] - xWeight[3] - xWeight[4];
			/* y */
			w = y - (double)yIndex[2];
			w2 = w * w;
			t = (1.0 / 6.0) * w2;
			yWeight[0] = 1.0 / 2.0 - w;
			yWeight[0] *= yWeight[0];
			yWeight[0] *= (1.0 / 24.0) * yWeight[0];
			t0 = w * (t - 11.0 / 24.0);
			t1 = 19.0 / 96.0 + w2 * (1.0 / 4.0 - t);
			yWeight[1] = t1 + t0;
			yWeight[3] = t1 - t0;
			yWeight[4] = yWeight[0] + t0 + (1.0 / 2.0) * w;
			yWeight[2] = 1.0 - yWeight[0] - yWeight[1] - yWeight[3] - yWeight[4];
			break;
		case 5L:
			/* x */
			w = x - (double)xIndex[2];
			w2 = w * w;
			xWeight[5] = (1.0 / 120.0) * w * w2 * w2;
			w2 -= w;
			w4 = w2 * w2;
			w -= 1.0 / 2.0;
			t = w2 * (w2 - 3.0);
			xWeight[0] = (1.0 / 24.0) * (1.0 / 5.0 + w2 + w4) - xWeight[5];
			t0 = (1.0 / 24.0) * (w2 * (w2 - 5.0) + 46.0 / 5.0);
			t1 = (-1.0 / 12.0) * w * (t + 4.0);
			xWeight[2] = t0 + t1;
			xWeight[3] = t0 - t1;
			t0 = (1.0 / 16.0) * (9.0 / 5.0 - t);
			t1 = (1.0 / 24.0) * w * (w4 - w2 - 5.0);
			xWeight[1] = t0 + t1;
			xWeight[4] = t0 - t1;
			/* y */
			w = y - (double)yIndex[2];
			w2 = w * w;
			yWeight[5] = (1.0 / 120.0) * w * w2 * w2;
			w2 -= w;
			w4 = w2 * w2;
			w -= 1.0 / 2.0;
			t = w2 * (w2 - 3.0);
			yWeight[0] = (1.0 / 24.0) * (1.0 / 5.0 + w2 + w4) - yWeight[5];
			t0 = (1.0 / 24.0) * (w2 * (w2 - 5.0) + 46.0 / 5.0);
			t1 = (-1.0 / 12.0) * w * (t + 4.0);
			yWeight[2] = t0 + t1;
			yWeight[3] = t0 - t1;
			t0 = (1.0 / 16.0) * (9.0 / 5.0 - t);
			t1 = (1.0 / 24.0) * w * (w4 - w2 - 5.0);
			yWeight[1] = t0 + t1;
			yWeight[4] = t0 - t1;
			break;
		default:
			// Invalid spline degree
			return 0;
	}

	// apply the mirror boundary conditions
	for(k = 0; k <= spline_degree; k++) {
		xIndex[k] = (Width == 1L) ? (0L) : ((xIndex[k] < 0L) ?
			(-xIndex[k] - Width2 * ((-xIndex[k]) / Width2))
			: (xIndex[k] - Width2 * (xIndex[k] / Width2)));
		if (Width <= xIndex[k]) {
			xIndex[k] = Width2 - xIndex[k];
		}
		yIndex[k] = (Height == 1L) ? (0L) : ((yIndex[k] < 0L) ?
			(-yIndex[k] - Height2 * ((-yIndex[k]) / Height2))
			: (yIndex[k] - Height2 * (yIndex[k] / Height2)));
		if (Height <= yIndex[k]) {
			yIndex[k] = Height2 - yIndex[k];
		}
	}

	// perform interpolation
	interpolated = 0.0;
	for(j = 0; j <= spline_degree; j++) {
		p = Bcoeff + (yIndex[j] * Width);
		w = 0.0;
		for(i = 0; i <= spline_degree; i++) {
			w += xWeight[i] * p[xIndex[i]];
		}
		interpolated += yWeight[j] * w;
	}

	return interpolated;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// FreeImage implementation


/** 
 Image translation and rotation using B-Splines.

 @param dib Input 8-bit greyscale image
 @param angle Output image rotation in degree
 @param x_shift Output image horizontal shift
 @param y_shift Output image vertical shift
 @param x_origin Output origin of the x-axis
 @param y_origin Output origin of the y-axis
 @param spline_degree Output degree of the B-spline model
 @param use_mask Whether or not to mask the image
 @return Returns the translated & rotated dib if successful, returns NULL otherwise
*/
static FIBITMAP * 
Rotate8Bit(FIBITMAP *dib, double angle, double x_shift, double y_shift, double x_origin, double y_origin, long spline_degree, BOOL use_mask) {
	double	*ImageRasterArray;
	double	p;
	double	a11, a12, a21, a22;
	double	x0, y0, x1, y1;
	long	x, y;
	long	spline;
	bool	bResult;

	int bpp = FreeImage_GetBPP(dib);
	if(bpp != 8) {
		return NULL;
	}
	
	int width = FreeImage_GetWidth(dib);
	int height = FreeImage_GetHeight(dib);
	switch(spline_degree) {
		case ROTATE_QUADRATIC:
			spline = 2L;	// Use splines of degree 2 (quadratic interpolation)
			break;
		case ROTATE_CUBIC:
			spline = 3L;	// Use splines of degree 3 (cubic interpolation)
			break;
		case ROTATE_QUARTIC:
			spline = 4L;	// Use splines of degree 4 (quartic interpolation)
			break;
		case ROTATE_QUINTIC:
			spline = 5L;	// Use splines of degree 5 (quintic interpolation)
			break;
		default:
			spline = 3L;
	}

	// allocate output image
	FIBITMAP *dst = FreeImage_Allocate(width, height, bpp);
	if(!dst)
		return NULL;
	// buid a grey scale palette
	RGBQUAD *pal = FreeImage_GetPalette(dst);
	for(int i = 0; i < 256; i++) {
		pal[i].rgbRed = pal[i].rgbGreen = pal[i].rgbBlue = (BYTE)i;
	}

	// allocate a temporary array
	ImageRasterArray = (double*)malloc(width * height * sizeof(double));
	if(!ImageRasterArray) {
		FreeImage_Unload(dst);
		return NULL;
	}
	// copy data samples
	for(y = 0; y < height; y++) {
		double *pImage = &ImageRasterArray[y*width];
		BYTE *src_bits = FreeImage_GetScanLine(dib, height-1-y);

		for(x = 0; x < width; x++) {
			pImage[x] = (double)src_bits[x];
		}
	}

	// convert between a representation based on image samples
	// and a representation based on image B-spline coefficients
	bResult = SamplesToCoefficients(ImageRasterArray, width, height, spline);
	if(!bResult) {
		FreeImage_Unload(dst);
		free(ImageRasterArray);
		return NULL;
	}

	// prepare the geometry
	angle *= PI / 180.0;
	a11 = cos(angle);
	a12 = -sin(angle);
	a21 = sin(angle);
	a22 = cos(angle);
	x0 = a11 * (x_shift + x_origin) + a12 * (y_shift + y_origin);
	y0 = a21 * (x_shift + x_origin) + a22 * (y_shift + y_origin);
	x_shift = x_origin - x0;
	y_shift = y_origin - y0;

	// visit all pixels of the output image and assign their value
	for(y = 0; y < height; y++) {
		BYTE *dst_bits = FreeImage_GetScanLine(dst, height-1-y);
		
		x0 = a12 * (double)y + x_shift;
		y0 = a22 * (double)y + y_shift;

		for(x = 0; x < width; x++) {
			x1 = x0 + a11 * (double)x;
			y1 = y0 + a21 * (double)x;
			if(use_mask) {
				if((x1 <= -0.5) || (((double)width - 0.5) <= x1) || (y1 <= -0.5) || (((double)height - 0.5) <= y1)) {
					p = 0;
				}
				else {
					p = (double)InterpolatedValue(ImageRasterArray, width, height, x1, y1, spline);
				}
			}
			else {
				p = (double)InterpolatedValue(ImageRasterArray, width, height, x1, y1, spline);
			}
			// clamp and convert to BYTE
			dst_bits[x] = (BYTE)MIN(MAX((int)0, (int)(p + 0.5)), (int)255);
		}
	}

	// free working array and return
	free(ImageRasterArray);

	return dst;
}

/** 
 Image rotation using a 3rd order (cubic) B-Splines.

 @param dib Input dib (8, 24 or 32-bit)
 @param angle Output image rotation
 @param x_shift Output image horizontal shift
 @param y_shift Output image vertical shift
 @param x_origin Output origin of the x-axis
 @param y_origin Output origin of the y-axis
 @param use_mask Whether or not to mask the image
 @return Returns the translated & rotated dib if successful, returns NULL otherwise
*/
FIBITMAP * DLL_CALLCONV 
FreeImage_RotateEx(FIBITMAP *dib, double angle, double x_shift, double y_shift, double x_origin, double y_origin, BOOL use_mask) {

	int x, y, bpp;
	int channel, nb_channels;
	BYTE *src_bits, *dst_bits;
	FIBITMAP *src8 = NULL, *dst8 = NULL, *dst = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	try {

		bpp = FreeImage_GetBPP(dib);

		if(bpp == 8) {
			FIBITMAP *dst_8 = Rotate8Bit(dib, angle, x_shift, y_shift, x_origin, y_origin, ROTATE_CUBIC, use_mask);
			if(dst_8) {
				// copy metadata from src to dst
				FreeImage_CloneMetadata(dst_8, dib);
			}
			return dst_8;
		}
		if((bpp == 24) || (bpp == 32)) {
			// allocate dst image
			int width  = FreeImage_GetWidth(dib);
			int height = FreeImage_GetHeight(dib);
			if( bpp == 24 ) {
				dst = FreeImage_Allocate(width, height, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
			} else {
				dst = FreeImage_Allocate(width, height, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
			}
			if(!dst) throw(1);

			// allocate a temporary 8-bit dib (no need to build a palette)
			src8 = FreeImage_Allocate(width, height, 8);
			if(!src8) throw(1);

			// process each channel separately
			// -------------------------------
			nb_channels = (bpp / 8);

			for(channel = 0; channel < nb_channels; channel++) {
				// extract channel from source dib
				for(y = 0; y < height; y++) {
					src_bits = FreeImage_GetScanLine(dib, y);
					dst_bits = FreeImage_GetScanLine(src8, y);
					for(x = 0; x < width; x++) {
						dst_bits[x] = src_bits[channel];
						src_bits += nb_channels;
					}
				}

				// process channel
				dst8 = Rotate8Bit(src8, angle, x_shift, y_shift, x_origin, y_origin, ROTATE_CUBIC, use_mask);
				if(!dst8) throw(1);

				// insert channel to destination dib
				for(y = 0; y < height; y++) {
					src_bits = FreeImage_GetScanLine(dst8, y);
					dst_bits = FreeImage_GetScanLine(dst, y);
					for(x = 0; x < width; x++) {
						dst_bits[channel] = src_bits[x];
						dst_bits += nb_channels;
					}
				}

				FreeImage_Unload(dst8);
			}

			FreeImage_Unload(src8);

			// copy metadata from src to dst
			FreeImage_CloneMetadata(dst, dib);
			
			return dst;
		}
	} catch(int) {
		if(src8) FreeImage_Unload(src8);
		if(dst8) FreeImage_Unload(dst8);
		if(dst)  FreeImage_Unload(dst);
	}

	return NULL;
}

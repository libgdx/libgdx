// ==========================================================
// Bitmap conversion routines
// Thresholding and halftoning functions
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
// - Dennis Lim (dlkj@users.sourceforge.net)
// - Thomas Chmielewski (Chmielewski.Thomas@oce.de)
//
// Main reference : Ulichney, R., Digital Halftoning, The MIT Press, Cambridge, MA, 1987
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

static const int WHITE = 255;
static const int BLACK = 0;

// Floyd & Steinberg error diffusion dithering
// This algorithm use the following filter
//          *   7
//      3   5   1     (1/16)
static FIBITMAP* FloydSteinberg(FIBITMAP *dib) {

#define RAND(RN) (((seed = 1103515245 * seed + 12345) >> 12) % (RN))
#define INITERR(X, Y) (((int) X) - (((int) Y) ? WHITE : BLACK) + ((WHITE/2)-((int)X)) / 2)

	int seed = 0;
	int x, y, p, pixel, threshold, error;
	int width, height, pitch;
	BYTE *bits, *new_bits;
	FIBITMAP *new_dib = NULL;

	// allocate a 8-bit DIB
	width = FreeImage_GetWidth(dib);
	height = FreeImage_GetHeight(dib);
	pitch = FreeImage_GetPitch(dib);
	new_dib = FreeImage_Allocate(width, height, 8);
	if(NULL == new_dib) return NULL;

	// allocate space for error arrays
	int *lerr = (int*)malloc (width * sizeof(int));
	int *cerr = (int*)malloc (width * sizeof(int));
	memset(lerr, 0, width * sizeof(int));
	memset(cerr, 0, width * sizeof(int));

	// left border
	error = 0;
	for(y = 0; y < height; y++) {
		bits = FreeImage_GetScanLine(dib, y);
		new_bits = FreeImage_GetScanLine(new_dib, y);

		threshold = (WHITE / 2 + RAND(129) - 64);
		pixel = bits[0] + error;
		p = (pixel > threshold) ? WHITE : BLACK;
		error = pixel - p;
		new_bits[0] = (BYTE)p;
	}
	// right border
	error = 0;
	for(y = 0; y < height; y++) {
		bits = FreeImage_GetScanLine(dib, y);
		new_bits = FreeImage_GetScanLine(new_dib, y);

		threshold = (WHITE / 2 + RAND(129) - 64);
		pixel = bits[width-1] + error;
		p = (pixel > threshold) ? WHITE : BLACK;
		error = pixel - p;
		new_bits[width-1] = (BYTE)p;
	}
	// top border
	bits = FreeImage_GetBits(dib);
	new_bits = FreeImage_GetBits(new_dib);
	error = 0;
	for(x = 0; x < width; x++) {
		threshold = (WHITE / 2 + RAND(129) - 64);
		pixel = bits[x] + error;
		p = (pixel > threshold) ? WHITE : BLACK;
		error = pixel - p;
		new_bits[x] = (BYTE)p;
		lerr[x] = INITERR(bits[x], p);
	}

	// interior bits
	for(y = 1; y < height; y++) {
		// scan left to right
		bits = FreeImage_GetScanLine(dib, y);
		new_bits = FreeImage_GetScanLine(new_dib, y);

	    cerr[0] = INITERR(bits[0], new_bits[0]);
		for(x = 1; x < width - 1; x++) {
			error = (lerr[x-1] + 5 * lerr[x] + 3 * lerr[x+1] + 7 * cerr[x-1]) / 16;
			pixel = bits[x] + error;
			if(pixel > (WHITE / 2)) {		
				new_bits[x] = WHITE;
				cerr[x] = pixel - WHITE; 
			} else {
				new_bits[x] = BLACK;
				cerr[x] = pixel - BLACK; 
			}
		}
		// set errors for ends of the row
		cerr[0] = INITERR (bits[0], new_bits[0]);
		cerr[width - 1] = INITERR (bits[width - 1], new_bits[width - 1]);

		// swap error buffers
		int *terr = lerr; lerr = cerr; cerr = terr;
	}

	free(lerr);
	free(cerr);

	return new_dib;
}

// ==========================================================
// Bayer ordered dispersed dot dithering
//

// Function taken from "Ordered Dithering, Stephen Hawley, Graphics Gems, Academic Press, 1990"
// This function is used to generate a Bayer dithering matrice whose dimension are 2^size by 2^size
//
static int dithervalue(int x, int y, int size) {
	int d = 0;
	/*
	 * calculate the dither value at a particular
	 * (x, y) over the size of the matrix.
	 */
	while (size-->0)	{
		/* Think of d as the density. At every iteration,
		 * d is shifted left one and a new bit is put in the
		 * low bit based on x and y. If x is odd and y is even,
		 * or x is even and y is odd, a bit is put in. This
		 * generates the checkerboard seen in dithering.
		 * This quantity is shifted left again and the low bit of
		 * y is added in.
		 * This whole thing interleaves a checkerboard bit pattern
		 * and y's bits, which is the value you want.
		 */
		d = (d <<1 | (x&1 ^ y&1))<<1 | y&1;
		x >>= 1;
		y >>= 1;
	}
	return d;
}

// Ordered dithering with a Bayer matrix of size 2^order by 2^order
//
static FIBITMAP* OrderedDispersedDot(FIBITMAP *dib, int order) {
	int x, y;
	int width, height;
	BYTE *bits, *new_bits;
	FIBITMAP *new_dib = NULL;

	// allocate a 8-bit DIB
	width = FreeImage_GetWidth(dib);
	height = FreeImage_GetHeight(dib);
	new_dib = FreeImage_Allocate(width, height, 8);
	if(NULL == new_dib) return NULL;

	// build the dithering matrix
	int l = (1 << order);	// square of dither matrix order; the dimensions of the matrix
	BYTE *matrix = (BYTE*)malloc(l*l * sizeof(BYTE));
	for(int i = 0; i < l*l; i++) {
		// according to "Purdue University: Digital Image Processing Laboratory: Image Halftoning, April 30th, 2006
		matrix[i] = (BYTE)( 255 * (((double)dithervalue(i / l, i % l, order) + 0.5) / (l*l)) );
	}

	// perform the dithering
	for(y = 0; y < height; y++) {
		// scan left to right
		bits = FreeImage_GetScanLine(dib, y);
		new_bits = FreeImage_GetScanLine(new_dib, y);
		for(x = 0; x < width; x++) {
			if(bits[x] > matrix[(x % l) + l * (y % l)]) {
				new_bits[x] = WHITE;
			} else {
				new_bits[x] = BLACK;
			}
		}
	}

	free(matrix);

	return new_dib;
}

// ==========================================================
// Ordered clustered dot dithering
//

// NB : The predefined dither matrices are the same as matrices used in 
// the Netpbm package (http://netpbm.sourceforge.net) and are defined in Ulichney's book.
// See also : The newsprint web site at http://www.cl.cam.ac.uk/~and1000/newsprint/
// for more technical info on this dithering technique
//
static FIBITMAP* OrderedClusteredDot(FIBITMAP *dib, int order) {
	// Order-3 clustered dithering matrix.
	int cluster3[] = {
	  9,11,10, 8, 6, 7,
	  12,17,16, 5, 0, 1,
	  13,14,15, 4, 3, 2,
	  8, 6, 7, 9,11,10,
	  5, 0, 1,12,17,16,
	  4, 3, 2,13,14,15
	};

	// Order-4 clustered dithering matrix. 
	int cluster4[] = {
	  18,20,19,16,13,11,12,15,
	  27,28,29,22, 4, 3, 2, 9,
	  26,31,30,21, 5, 0, 1,10,
	  23,25,24,17, 8, 6, 7,14,
	  13,11,12,15,18,20,19,16,
	  4, 3, 2, 9,27,28,29,22,
	  5, 0, 1,10,26,31,30,21,
	  8, 6, 7,14,23,25,24,17
	};

	// Order-8 clustered dithering matrix. 
	int cluster8[] = {
	   64, 69, 77, 87, 86, 76, 68, 67, 63, 58, 50, 40, 41, 51, 59, 60,
	   70, 94,100,109,108, 99, 93, 75, 57, 33, 27, 18, 19, 28, 34, 52,
	   78,101,114,116,115,112, 98, 83, 49, 26, 13, 11, 12, 15, 29, 44,
	   88,110,123,124,125,118,107, 85, 39, 17,  4,  3,  2,  9, 20, 42,
	   89,111,122,127,126,117,106, 84, 38, 16,  5,  0,  1, 10, 21, 43,
	   79,102,119,121,120,113, 97, 82, 48, 25,  8,  6,  7, 14, 30, 45,
	   71, 95,103,104,105, 96, 92, 74, 56, 32, 24, 23, 22, 31, 35, 53,
	   65, 72, 80, 90, 91, 81, 73, 66, 62, 55, 47, 37, 36, 46, 54, 61,
	   63, 58, 50, 40, 41, 51, 59, 60, 64, 69, 77, 87, 86, 76, 68, 67,
	   57, 33, 27, 18, 19, 28, 34, 52, 70, 94,100,109,108, 99, 93, 75,
	   49, 26, 13, 11, 12, 15, 29, 44, 78,101,114,116,115,112, 98, 83,
	   39, 17,  4,  3,  2,  9, 20, 42, 88,110,123,124,125,118,107, 85,
	   38, 16,  5,  0,  1, 10, 21, 43, 89,111,122,127,126,117,106, 84,
	   48, 25,  8,  6,  7, 14, 30, 45, 79,102,119,121,120,113, 97, 82,
	   56, 32, 24, 23, 22, 31, 35, 53, 71, 95,103,104,105, 96, 92, 74,
	   62, 55, 47, 37, 36, 46, 54, 61, 65, 72, 80, 90, 91, 81, 73, 66
	};

	int x, y, pixel;
	int width, height;
	BYTE *bits, *new_bits;
	FIBITMAP *new_dib = NULL;

	// allocate a 8-bit DIB
	width = FreeImage_GetWidth(dib);
	height = FreeImage_GetHeight(dib);
	new_dib = FreeImage_Allocate(width, height, 8);
	if(NULL == new_dib) return NULL;

	// select the dithering matrix
	int *matrix = NULL;
	switch(order) {
		case 3:
			matrix = &cluster3[0];
			break;
		case 4:
			matrix = &cluster4[0];
			break;
		case 8:
			matrix = &cluster8[0];
			break;
		default:
			return NULL;
	}

	// scale the dithering matrix
	int l = 2 * order;
	int scale = 256 / (l * order);
	for(y = 0; y < l; y++) {
		for(x = 0; x < l; x++) {
			matrix[y*l + x] *= scale;
		}
	}

	// perform the dithering
	for(y = 0; y < height; y++) {
		// scan left to right
		bits = FreeImage_GetScanLine(dib, y);
		new_bits = FreeImage_GetScanLine(new_dib, y);
		for(x = 0; x < width; x++) {
			pixel = bits[x];
			if(pixel >= matrix[(y % l) + l * (x % l)]) {
				new_bits[x] = WHITE;
			} else {
				new_bits[x] = BLACK;
			}
		}
	}

	return new_dib;
}


// ==========================================================
// Halftoning function
//
FIBITMAP * DLL_CALLCONV
FreeImage_Dither(FIBITMAP *dib, FREE_IMAGE_DITHER algorithm) {
	FIBITMAP *input = NULL, *dib8 = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	const unsigned bpp = FreeImage_GetBPP(dib);

	if(bpp == 1) {
		// Just clone the dib and adjust the palette if needed
		FIBITMAP *new_dib = FreeImage_Clone(dib);
		if(NULL == new_dib) return NULL;
		if(FreeImage_GetColorType(new_dib) == FIC_PALETTE) {
			// Build a monochrome palette
			RGBQUAD *pal = FreeImage_GetPalette(new_dib);
			pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
			pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;
		}
		return new_dib;
	}

	// Convert the input dib to a 8-bit greyscale dib
	//
	switch(bpp) {
		case 8:
			if(FreeImage_GetColorType(dib) == FIC_MINISBLACK) {
				input = dib;
			} else {
				input = FreeImage_ConvertToGreyscale(dib);
			} 
			break;
		case 4:
		case 16:
		case 24:
		case 32:
			input = FreeImage_ConvertToGreyscale(dib);
			break;			
	}
	if(NULL == input) return NULL;

	// Apply the dithering algorithm
	switch(algorithm) {
		case FID_FS:
			dib8 = FloydSteinberg(input);
			break;
		case FID_BAYER4x4:
			dib8 = OrderedDispersedDot(input, 2);
			break;
		case FID_BAYER8x8:
			dib8 = OrderedDispersedDot(input, 3);
			break;
		case FID_BAYER16x16:
			dib8 = OrderedDispersedDot(input, 4);
			break;
		case FID_CLUSTER6x6:
			dib8 = OrderedClusteredDot(input, 3);
			break;
		case FID_CLUSTER8x8:
			dib8 = OrderedClusteredDot(input, 4);
			break;
		case FID_CLUSTER16x16:
			dib8 = OrderedClusteredDot(input, 8);
			break;
	}
	if(input != dib) {
		FreeImage_Unload(input);
	}

	// Build a greyscale palette (needed by threshold)
	RGBQUAD *grey_pal = FreeImage_GetPalette(dib8);
	for(int i = 0; i < 256; i++) {
		grey_pal[i].rgbRed	= (BYTE)i;
		grey_pal[i].rgbGreen = (BYTE)i;
		grey_pal[i].rgbBlue	= (BYTE)i;
	}

	// Convert to 1-bit
	FIBITMAP *new_dib = FreeImage_Threshold(dib8, 128);
	FreeImage_Unload(dib8);

	// copy metadata from src to dst
	FreeImage_CloneMetadata(new_dib, dib);

	return new_dib;
}

// ==========================================================
// Thresholding function
//
FIBITMAP * DLL_CALLCONV
FreeImage_Threshold(FIBITMAP *dib, BYTE T) {
	FIBITMAP *dib8 = NULL;

	if(!FreeImage_HasPixels(dib)) return NULL;

	const unsigned bpp = FreeImage_GetBPP(dib);

	if(bpp == 1) {
		// Just clone the dib and adjust the palette if needed
		FIBITMAP *new_dib = FreeImage_Clone(dib);
		if(NULL == new_dib) return NULL;
		if(FreeImage_GetColorType(new_dib) == FIC_PALETTE) {
			// Build a monochrome palette
			RGBQUAD *pal = FreeImage_GetPalette(new_dib);
			pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
			pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;
		}
		return new_dib;
	}

	// Convert the input dib to a 8-bit greyscale dib
	//
	switch(bpp) {
		case 8:
			if(FreeImage_GetColorType(dib) == FIC_MINISBLACK) {
				dib8 = dib;
			} else {
				dib8 = FreeImage_ConvertToGreyscale(dib);
			} 
			break;
		case 4:
		case 16:
		case 24:
		case 32:
			dib8 = FreeImage_ConvertToGreyscale(dib);
			break;			
	}
	if(NULL == dib8) return NULL;

	// Allocate a new 1-bit DIB
	int width = FreeImage_GetWidth(dib);
	int height = FreeImage_GetHeight(dib);
	FIBITMAP *new_dib = FreeImage_Allocate(width, height, 1);
	if(NULL == new_dib) return NULL;
	// Build a monochrome palette
	RGBQUAD *pal = FreeImage_GetPalette(new_dib);
	pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
	pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;

	// Perform the thresholding
	//
	for(int y = 0; y < height; y++) {
		BYTE *bits8 = FreeImage_GetScanLine(dib8, y);
		BYTE *bits1 = FreeImage_GetScanLine(new_dib, y);
		for(int x = 0; x < width; x++) {
			if(bits8[x] < T) {
				// Set bit(x, y) to 0
				bits1[x >> 3] &= (0xFF7F >> (x & 0x7));
			} else {
				// Set bit(x, y) to 1
				bits1[x >> 3] |= (0x80 >> (x & 0x7));
			}
		}
	}
	if(dib8 != dib) {
		FreeImage_Unload(dib8);
	}

	// copy metadata from src to dst
	FreeImage_CloneMetadata(new_dib, dib);

	return new_dib;
}


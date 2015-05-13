// ==========================================================
// Bitmap conversion routines
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

// ----------------------------------------------------------

/** Convert a greyscale image of type Tsrc to type Tdst.
	Conversion is done using standard C language casting convention.
*/
template<class Tdst, class Tsrc>
class CONVERT_TYPE
{
public:
	FIBITMAP* convert(FIBITMAP *src, FREE_IMAGE_TYPE dst_type);
};

template<class Tdst, class Tsrc> FIBITMAP* 
CONVERT_TYPE<Tdst, Tsrc>::convert(FIBITMAP *src, FREE_IMAGE_TYPE dst_type) {

	FIBITMAP *dst = NULL;

	unsigned width	= FreeImage_GetWidth(src);
	unsigned height = FreeImage_GetHeight(src);
	unsigned bpp	= FreeImage_GetBPP(src);

	// allocate dst image

	dst = FreeImage_AllocateT(dst_type, width, height, bpp, 
			FreeImage_GetRedMask(src), FreeImage_GetGreenMask(src), FreeImage_GetBlueMask(src));
	if(!dst) return NULL;

	// convert from src_type to dst_type
	
	for(unsigned y = 0; y < height; y++) {
		const Tsrc *src_bits = reinterpret_cast<Tsrc*>(FreeImage_GetScanLine(src, y));
		Tdst *dst_bits = reinterpret_cast<Tdst*>(FreeImage_GetScanLine(dst, y));

		for(unsigned x = 0; x < width; x++) {
			*dst_bits++ = static_cast<Tdst>(*src_bits++);
		}
	}

	return dst;
}


/** Convert a greyscale image of type Tsrc to a 8-bit grayscale dib.
	Conversion is done using either a linear scaling from [min, max] to [0, 255]
	or a rounding from src_pixel to (BYTE) MIN(255, MAX(0, q)) where int q = int(src_pixel + 0.5); 
*/
template<class Tsrc>
class CONVERT_TO_BYTE
{
public:
	FIBITMAP* convert(FIBITMAP *src, BOOL scale_linear);
};

template<class Tsrc> FIBITMAP* 
CONVERT_TO_BYTE<Tsrc>::convert(FIBITMAP *src, BOOL scale_linear) {
	FIBITMAP *dst = NULL;
	unsigned x, y;

	unsigned width	= FreeImage_GetWidth(src);
	unsigned height = FreeImage_GetHeight(src);

	// allocate a 8-bit dib

	dst = FreeImage_AllocateT(FIT_BITMAP, width, height, 8, 0, 0, 0);
	if(!dst) return NULL;

	// build a greyscale palette
	RGBQUAD *pal = FreeImage_GetPalette(dst);
	for(int i = 0; i < 256; i++) {
		pal[i].rgbRed = (BYTE)i;
		pal[i].rgbGreen = (BYTE)i;
		pal[i].rgbBlue = (BYTE)i;
	}

	// convert the src image to dst
	// (FIBITMAP are stored upside down)
	if(scale_linear) {
		Tsrc max, min;
		double scale;

		// find the min and max value of the image
		Tsrc l_min, l_max;
		min = 255, max = 0;
		for(y = 0; y < height; y++) {
			Tsrc *bits = reinterpret_cast<Tsrc*>(FreeImage_GetScanLine(src, y));
			MAXMIN(bits, width, l_max, l_min);
			if(l_max > max) max = l_max;
			if(l_min < min) min = l_min;
		}
		if(max == min) {
			max = 255; min = 0;
		}

		// compute the scaling factor
		scale = 255 / (double)(max - min);

		// scale to 8-bit
		for(y = 0; y < height; y++) {
			Tsrc *src_bits = reinterpret_cast<Tsrc*>(FreeImage_GetScanLine(src, y));
			BYTE *dst_bits = FreeImage_GetScanLine(dst, y);
			for(x = 0; x < width; x++) {
				dst_bits[x] = (BYTE)( scale * (src_bits[x] - min) + 0.5);
			}
		}
	} else {
		for(y = 0; y < height; y++) {
			Tsrc *src_bits = reinterpret_cast<Tsrc*>(FreeImage_GetScanLine(src, y));
			BYTE *dst_bits = FreeImage_GetScanLine(dst, y);
			for(x = 0; x < width; x++) {
				// rounding
				int q = int(src_bits[x] + 0.5);
				dst_bits[x] = (BYTE) MIN(255, MAX(0, q));
			}
		}
	}

	return dst;
}

/** Convert a greyscale image of type Tsrc to a FICOMPLEX dib.
*/
template<class Tsrc>
class CONVERT_TO_COMPLEX
{
public:
	FIBITMAP* convert(FIBITMAP *src);
};

template<class Tsrc> FIBITMAP* 
CONVERT_TO_COMPLEX<Tsrc>::convert(FIBITMAP *src) {
	FIBITMAP *dst = NULL;

	unsigned width	= FreeImage_GetWidth(src);
	unsigned height = FreeImage_GetHeight(src);

	// allocate dst image

	dst = FreeImage_AllocateT(FIT_COMPLEX, width, height);
	if(!dst) return NULL;

	// convert from src_type to FIT_COMPLEX
	
	for(unsigned y = 0; y < height; y++) {
		const Tsrc *src_bits = reinterpret_cast<Tsrc*>(FreeImage_GetScanLine(src, y));
		FICOMPLEX *dst_bits = (FICOMPLEX *)FreeImage_GetScanLine(dst, y);

		for(unsigned x = 0; x < width; x++) {
			dst_bits[x].r = (double)src_bits[x];
			dst_bits[x].i = 0;
		}
	}

	return dst;
}

// ----------------------------------------------------------

// Convert from type BYTE to type X
CONVERT_TYPE<unsigned short, BYTE>	convertByteToUShort;
CONVERT_TYPE<short, BYTE>			convertByteToShort;
CONVERT_TYPE<DWORD, BYTE>			convertByteToULong;
CONVERT_TYPE<LONG, BYTE>			convertByteToLong;
CONVERT_TYPE<float, BYTE>			convertByteToFloat;
CONVERT_TYPE<double, BYTE>			convertByteToDouble;

// Convert from type X to type BYTE
CONVERT_TO_BYTE<unsigned short>	convertUShortToByte;
CONVERT_TO_BYTE<short>			convertShortToByte;
CONVERT_TO_BYTE<DWORD>			convertULongToByte;
CONVERT_TO_BYTE<LONG>			convertLongToByte;
CONVERT_TO_BYTE<float>			convertFloatToByte;
CONVERT_TO_BYTE<double>			convertDoubleToByte;

// Convert from type X to type float
CONVERT_TYPE<float, unsigned short>	convertUShortToFloat;
CONVERT_TYPE<float, short>			convertShortToFloat;
CONVERT_TYPE<float, DWORD>			convertULongToFloat;
CONVERT_TYPE<float, LONG>			convertLongToFloat;

// Convert from type X to type double
CONVERT_TYPE<double, unsigned short>	convertUShortToDouble;
CONVERT_TYPE<double, short>				convertShortToDouble;
CONVERT_TYPE<double, DWORD>				convertULongToDouble;
CONVERT_TYPE<double, LONG>				convertLongToDouble;
CONVERT_TYPE<double, float>				convertFloatToDouble;

// Convert from type X to type FICOMPLEX
CONVERT_TO_COMPLEX<BYTE>			convertByteToComplex;
CONVERT_TO_COMPLEX<unsigned short>	convertUShortToComplex;
CONVERT_TO_COMPLEX<short>			convertShortToComplex;
CONVERT_TO_COMPLEX<DWORD>			convertULongToComplex;
CONVERT_TO_COMPLEX<LONG>			convertLongToComplex;
CONVERT_TO_COMPLEX<float>			convertFloatToComplex;
CONVERT_TO_COMPLEX<double>			convertDoubleToComplex;

// ----------------------------------------------------------

// ----------------------------------------------------------
//   smart convert X to standard FIBITMAP
// ----------------------------------------------------------

/** Convert image of any type to a standard 8-bit greyscale image.
For standard images, a clone of the input image is returned.
When the scale_linear parameter is TRUE, conversion is done by scaling linearly 
each pixel to an integer value between [0..255]. When it is FALSE, conversion is done 
by rounding each float pixel to an integer between [0..255]. 
For complex images, the magnitude is extracted as a double image, then converted according to the scale parameter. 
@param image Image to convert
@param scale_linear Linear scaling / rounding switch
*/
FIBITMAP* DLL_CALLCONV
FreeImage_ConvertToStandardType(FIBITMAP *src, BOOL scale_linear) {
	FIBITMAP *dst = NULL;

	if(!src) return NULL;

	// convert from src_type to FIT_BITMAP

	const FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(src);

	switch(src_type) {
		case FIT_BITMAP:	// standard image: 1-, 4-, 8-, 16-, 24-, 32-bit
			dst = FreeImage_Clone(src);
			break;
		case FIT_UINT16:	// array of unsigned short: unsigned 16-bit
			dst = convertUShortToByte.convert(src, scale_linear);
			break;
		case FIT_INT16:		// array of short: signed 16-bit
			dst = convertShortToByte.convert(src, scale_linear);
			break;
		case FIT_UINT32:	// array of unsigned long: unsigned 32-bit
			dst = convertULongToByte.convert(src, scale_linear);
			break;
		case FIT_INT32:		// array of long: signed 32-bit
			dst = convertLongToByte.convert(src, scale_linear);
			break;
		case FIT_FLOAT:		// array of float: 32-bit
			dst = convertFloatToByte.convert(src, scale_linear);
			break;
		case FIT_DOUBLE:	// array of double: 64-bit
			dst = convertDoubleToByte.convert(src, scale_linear);
			break;
		case FIT_COMPLEX:	// array of FICOMPLEX: 2 x 64-bit
			{
				// Convert to type FIT_DOUBLE
				FIBITMAP *dib_double = FreeImage_GetComplexChannel(src, FICC_MAG);
				if(dib_double) {
					// Convert to a standard bitmap (linear scaling)
					dst = convertDoubleToByte.convert(dib_double, scale_linear);
					// Free image of type FIT_DOUBLE
					FreeImage_Unload(dib_double);
				}
			}
			break;
		case FIT_RGB16:		// 48-bit RGB image: 3 x 16-bit
			break;
		case FIT_RGBA16:	// 64-bit RGBA image: 4 x 16-bit
			break;
		case FIT_RGBF:		// 96-bit RGB float image: 3 x 32-bit IEEE floating point
			break;
		case FIT_RGBAF:		// 128-bit RGBA float image: 4 x 32-bit IEEE floating point
			break;
	}

	if(NULL == dst) {
		FreeImage_OutputMessageProc(FIF_UNKNOWN, "FREE_IMAGE_TYPE: Unable to convert from type %d to type %d.\n No such conversion exists.", src_type, FIT_BITMAP);
	} else {
		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, src);
	}
	
	return dst;
}



// ----------------------------------------------------------
//   smart convert X to Y
// ----------------------------------------------------------

FIBITMAP* DLL_CALLCONV
FreeImage_ConvertToType(FIBITMAP *src, FREE_IMAGE_TYPE dst_type, BOOL scale_linear) {
	FIBITMAP *dst = NULL;

	if(!FreeImage_HasPixels(src)) return NULL;

	// convert from src_type to dst_type

	const FREE_IMAGE_TYPE src_type = FreeImage_GetImageType(src);

	if(src_type == dst_type) {
		return FreeImage_Clone(src);
	}

	const unsigned src_bpp = FreeImage_GetBPP(src);

	switch(src_type) {
		case FIT_BITMAP:
			switch(dst_type) {
				case FIT_UINT16:
					dst = FreeImage_ConvertToUINT16(src);
					break;
				case FIT_INT16:
					dst = (src_bpp == 8) ? convertByteToShort.convert(src, dst_type) : NULL;
					break;
				case FIT_UINT32:
					dst = (src_bpp == 8) ? convertByteToULong.convert(src, dst_type) : NULL;
					break;
				case FIT_INT32:
					dst = (src_bpp == 8) ? convertByteToLong.convert(src, dst_type) : NULL;
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					dst = (src_bpp == 8) ? convertByteToDouble.convert(src, dst_type) : NULL;
					break;
				case FIT_COMPLEX:
					dst = (src_bpp == 8) ? convertByteToComplex.convert(src) : NULL;
					break;
				case FIT_RGB16:
					dst = FreeImage_ConvertToRGB16(src);
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_UINT16:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					dst = convertUShortToDouble.convert(src, dst_type);
					break;
				case FIT_COMPLEX:
					dst = convertUShortToComplex.convert(src);
					break;
				case FIT_RGB16:
					dst = FreeImage_ConvertToRGB16(src);
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_INT16:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_UINT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = convertShortToFloat.convert(src, dst_type);
					break;
				case FIT_DOUBLE:
					dst = convertShortToDouble.convert(src, dst_type);
					break;
				case FIT_COMPLEX:
					dst = convertShortToComplex.convert(src);
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_UINT32:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = convertULongToFloat.convert(src, dst_type);
					break;
				case FIT_DOUBLE:
					dst = convertULongToDouble.convert(src, dst_type);
					break;
				case FIT_COMPLEX:
					dst = convertULongToComplex.convert(src);
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_INT32:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_FLOAT:
					dst = convertLongToFloat.convert(src, dst_type);
					break;
				case FIT_DOUBLE:
					dst = convertLongToDouble.convert(src, dst_type);
					break;
				case FIT_COMPLEX:
					dst = convertLongToComplex.convert(src);
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_FLOAT:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_DOUBLE:
					dst = convertFloatToDouble.convert(src, dst_type);
					break;
				case FIT_COMPLEX:
					dst = convertFloatToComplex.convert(src);
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_DOUBLE:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertToStandardType(src, scale_linear);
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					break;
				case FIT_COMPLEX:
					dst = convertDoubleToComplex.convert(src);
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_COMPLEX:
			switch(dst_type) {
				case FIT_BITMAP:
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					break;
				case FIT_DOUBLE:
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_RGB16:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertTo24Bits(src);
					break;
				case FIT_UINT16:
					dst = FreeImage_ConvertToUINT16(src);
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					break;
				case FIT_COMPLEX:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_RGBA16:
			switch(dst_type) {
				case FIT_BITMAP:
					dst = FreeImage_ConvertTo32Bits(src);
					break;
				case FIT_UINT16:
					dst = FreeImage_ConvertToUINT16(src);
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					break;
				case FIT_COMPLEX:
					break;
				case FIT_RGB16:
					dst = FreeImage_ConvertToRGB16(src);
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_RGBF:
			switch(dst_type) {
				case FIT_BITMAP:
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					break;
				case FIT_COMPLEX:
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBAF:
					break;
			}
			break;
		case FIT_RGBAF:
			switch(dst_type) {
				case FIT_BITMAP:
					break;
				case FIT_UINT16:
					break;
				case FIT_INT16:
					break;
				case FIT_UINT32:
					break;
				case FIT_INT32:
					break;
				case FIT_FLOAT:
					dst = FreeImage_ConvertToFloat(src);
					break;
				case FIT_DOUBLE:
					break;
				case FIT_COMPLEX:
					break;
				case FIT_RGB16:
					break;
				case FIT_RGBA16:
					break;
				case FIT_RGBF:
					dst = FreeImage_ConvertToRGBF(src);
					break;
			}
			break;
	}

	if(NULL == dst) {
		FreeImage_OutputMessageProc(FIF_UNKNOWN, "FREE_IMAGE_TYPE: Unable to convert from type %d to type %d.\n No such conversion exists.", src_type, dst_type);
	} else {
		// copy metadata from src to dst
		FreeImage_CloneMetadata(dst, src);
	}

	return dst;
}

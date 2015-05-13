// ==========================================================
// Upsampling / downsampling classes
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
// - Detlev Vendt (detlev.vendt@brillit.de)
// - Carsten Klein (cklein05@users.sourceforge.net)
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

#include "Resize.h"

/**
Returns the color type of a bitmap. In contrast to FreeImage_GetColorType,
this function optionally supports a boolean OUT parameter, that receives TRUE,
if the specified bitmap is greyscale, that is, it consists of grey colors only.
Although it returns the same value as returned by FreeImage_GetColorType for all
image types, this extended function primarily is intended for palletized images,
since the boolean pointed to by 'bIsGreyscale' remains unchanged for RGB(A/F)
images. However, the outgoing boolean is properly maintained for palletized images,
as well as for any non-RGB image type, like FIT_UINTxx and FIT_DOUBLE, for example.
@param dib A pointer to a FreeImage bitmap to calculate the extended color type for
@param bIsGreyscale A pointer to a boolean, that receives TRUE, if the specified bitmap
is greyscale, that is, it consists of grey colors only. This parameter can be NULL.
@return the color type of the specified bitmap
*/
static FREE_IMAGE_COLOR_TYPE
GetExtendedColorType(FIBITMAP *dib, BOOL *bIsGreyscale) {
	const unsigned bpp = FreeImage_GetBPP(dib);
	const unsigned size = CalculateUsedPaletteEntries(bpp);
	const RGBQUAD * const pal = FreeImage_GetPalette(dib);
	FREE_IMAGE_COLOR_TYPE color_type = FIC_MINISBLACK;
	BOOL bIsGrey = TRUE;

	switch (bpp) {
		case 1:
		{
			for (unsigned i = 0; i < size; i++) {
				if ((pal[i].rgbRed != pal[i].rgbGreen) || (pal[i].rgbRed != pal[i].rgbBlue)) {
					color_type = FIC_PALETTE;
					bIsGrey = FALSE;
					break;
				}
			}
			if (bIsGrey) {
				if (pal[0].rgbBlue == 255 && pal[1].rgbBlue == 0) {
					color_type = FIC_MINISWHITE;
				} else if (pal[0].rgbBlue != 0 || pal[1].rgbBlue != 255) {
					color_type = FIC_PALETTE;
				}
			}
			break;
		}

		case 4:
		case 8:
		{
			for (unsigned i = 0; i < size; i++) {
				if ((pal[i].rgbRed != pal[i].rgbGreen) || (pal[i].rgbRed != pal[i].rgbBlue)) {
					color_type = FIC_PALETTE;
					bIsGrey = FALSE;
					break;
				}
				if (color_type != FIC_PALETTE && pal[i].rgbBlue != i) {
					if ((size - i - 1) != pal[i].rgbBlue) {
						color_type = FIC_PALETTE;
						if (!bIsGreyscale) {
							// exit loop if we're not setting
							// bIsGreyscale parameter
							break;
						}
					} else {
						color_type = FIC_MINISWHITE;
					}
				}
			}
			break;
		}

		default:
		{
			color_type = FreeImage_GetColorType(dib);
			bIsGrey = (color_type == FIC_MINISBLACK) ? TRUE : FALSE;
			break;
		}

	}
	if (bIsGreyscale) {
		*bIsGreyscale = bIsGrey;
	}

	return color_type;
}

/**
Returns a pointer to an RGBA palette, created from the specified bitmap.
The RGBA palette is a copy of the specified bitmap's palette, that, additionally
contains the bitmap's transparency information in the rgbReserved member
of the palette's RGBQUAD elements.
@param dib A pointer to a FreeImage bitmap to create the RGBA palette from.
@param buffer A pointer to the buffer to store the RGBA palette.
@return A pointer to the newly created RGBA palette or NULL, if the specified
bitmap is no palletized standard bitmap. If non-NULL, the returned value is
actually the pointer passed in parameter 'buffer'.
*/
static inline RGBQUAD *
GetRGBAPalette(FIBITMAP *dib, RGBQUAD * const buffer) {
	// clone the palette
	const unsigned ncolors = FreeImage_GetColorsUsed(dib);
	if (ncolors == 0) {
		return NULL;
	}
	memcpy(buffer, FreeImage_GetPalette(dib), ncolors * sizeof(RGBQUAD));
	// merge the transparency table
	const unsigned ntransp = MIN(ncolors, FreeImage_GetTransparencyCount(dib));
	const BYTE * const tt = FreeImage_GetTransparencyTable(dib);
	for (unsigned i = 0; i < ntransp; i++) {
		buffer[i].rgbReserved = tt[i];
	}
	for (unsigned i = ntransp; i < ncolors; i++) {
		buffer[i].rgbReserved = 255;
	}
	return buffer;
}

// --------------------------------------------------------------------------

CWeightsTable::CWeightsTable(CGenericFilter *pFilter, unsigned uDstSize, unsigned uSrcSize) {
	double dWidth;
	double dFScale;
	const double dFilterWidth = pFilter->GetWidth();

	// scale factor
	const double dScale = double(uDstSize) / double(uSrcSize);

	if(dScale < 1.0) {
		// minification
		dWidth = dFilterWidth / dScale; 
		dFScale = dScale; 
	} else {
		// magnification
		dWidth = dFilterWidth; 
		dFScale = 1.0; 
	}

	// allocate a new line contributions structure
	//
	// window size is the number of sampled pixels
	m_WindowSize = 2 * (int)ceil(dWidth) + 1; 
	// length of dst line (no. of rows / cols) 
	m_LineLength = uDstSize; 

	 // allocate list of contributions 
	m_WeightTable = (Contribution*)malloc(m_LineLength * sizeof(Contribution));
	for(unsigned u = 0; u < m_LineLength; u++) {
		// allocate contributions for every pixel
		m_WeightTable[u].Weights = (double*)malloc(m_WindowSize * sizeof(double));
	}

	// offset for discrete to continuous coordinate conversion
	const double dOffset = (0.5 / dScale);

	for(unsigned u = 0; u < m_LineLength; u++) {
		// scan through line of contributions

		// inverse mapping (discrete dst 'u' to continous src 'dCenter')
		const double dCenter = (double)u / dScale + dOffset;

		// find the significant edge points that affect the pixel
		const int iLeft = MAX(0, (int)(dCenter - dWidth + 0.5));
		const int iRight = MIN((int)(dCenter + dWidth + 0.5), int(uSrcSize));

		m_WeightTable[u].Left = iLeft; 
		m_WeightTable[u].Right = iRight;

		double dTotalWeight = 0;  // sum of weights (initialized to zero)
		for(int iSrc = iLeft; iSrc < iRight; iSrc++) {
			// calculate weights
			const double weight = dFScale * pFilter->Filter(dFScale * ((double)iSrc + 0.5 - dCenter));
			// assert((iSrc-iLeft) < m_WindowSize);
			m_WeightTable[u].Weights[iSrc-iLeft] = weight;
			dTotalWeight += weight;
		}
		if((dTotalWeight > 0) && (dTotalWeight != 1)) {
			// normalize weight of neighbouring points
			for(int iSrc = iLeft; iSrc < iRight; iSrc++) {
				// normalize point
				m_WeightTable[u].Weights[iSrc-iLeft] /= dTotalWeight; 
			}
		}

		// simplify the filter, discarding null weights at the right
		{			
			int iTrailing = iRight - iLeft - 1;
			while(m_WeightTable[u].Weights[iTrailing] == 0) {
				m_WeightTable[u].Right--;
				iTrailing--;
				if(m_WeightTable[u].Right == m_WeightTable[u].Left) {
					break;
				}
			}
			
		}

	} // next dst pixel
}

CWeightsTable::~CWeightsTable() {
	for(unsigned u = 0; u < m_LineLength; u++) {
		// free contributions for every pixel
		free(m_WeightTable[u].Weights);
	}
	// free list of pixels contributions
	free(m_WeightTable);
}

// --------------------------------------------------------------------------

FIBITMAP* CResizeEngine::scale(FIBITMAP *src, unsigned dst_width, unsigned dst_height, unsigned src_left, unsigned src_top, unsigned src_width, unsigned src_height) {

	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(src);
	const unsigned src_bpp = FreeImage_GetBPP(src);

	// determine the image's color type
	BOOL bIsGreyscale = FALSE;
	FREE_IMAGE_COLOR_TYPE color_type;
	if (src_bpp <= 8) {
		color_type = GetExtendedColorType(src, &bIsGreyscale);
	} else {
		color_type = FIC_RGB;
	}

	// determine the required bit depth of the destination image
	unsigned dst_bpp;
	if (color_type == FIC_PALETTE && !bIsGreyscale) {
		// non greyscale FIC_PALETTE images require a high-color destination
		// image (24- or 32-bits depending on the image's transparent state)
		dst_bpp = FreeImage_IsTransparent(src) ? 32 : 24;
	} else if (src_bpp <= 8) {
		// greyscale images require an 8-bit destination image
		// (or a 32-bit image if the image is transparent)
		dst_bpp = FreeImage_IsTransparent(src) ? 32 : 8;
		if (dst_bpp == 32) {
			// additionally, for transparent images we always need a
			// palette including transparency information (an RGBA palette)
			// so, set color_type accordingly.
			color_type = FIC_PALETTE;
		}
	} else if (src_bpp == 16 && image_type == FIT_BITMAP) {
		// 16-bit 555 and 565 RGB images require a high-color destination image
		// (fixed to 24 bits, since 16-bit RGBs don't support transparency in FreeImage)
		dst_bpp = 24;
	} else {
		// bit depth remains unchanged for all other images
		dst_bpp = src_bpp;
	}

	// early exit if destination size is equal to source size
	if ((src_width == dst_width) && (src_height == dst_height)) {
		FIBITMAP *out = src;
		FIBITMAP *tmp = src;
		if ((src_width != FreeImage_GetWidth(src)) || (src_height != FreeImage_GetHeight(src))) {
			out = FreeImage_Copy(tmp, src_left, src_top, src_left + src_width, src_top + src_height);
			tmp = out;
		}
		if (src_bpp != dst_bpp) {
			switch (dst_bpp) {
				case 8:
					out = FreeImage_ConvertToGreyscale(tmp);
					if (tmp != src) {
						FreeImage_Unload(tmp);
					}
					break;

				case 24:
					out = FreeImage_ConvertTo24Bits(tmp);
					if (tmp != src) {
						FreeImage_Unload(tmp);
					}
					break;

				case 32:
					out = FreeImage_ConvertTo32Bits(tmp);
					if (tmp != src) {
						FreeImage_Unload(tmp);
					}
					break;
			}
		}

		return (out != src) ? out : FreeImage_Clone(src);
	}

	RGBQUAD pal_buffer[256];
	RGBQUAD *src_pal = NULL;

	// provide the source image's palette to the rescaler for
	// FIC_PALETTE type images (this includes palletized greyscale
	// images with an unordered palette as well as transparent images)
	if (color_type == FIC_PALETTE) {
		if (dst_bpp == 32) {
			// a 32 bit destination image signals transparency, so
			// create an RGBA palette from the source palette
			src_pal = GetRGBAPalette(src, pal_buffer);
		} else {
			src_pal = FreeImage_GetPalette(src);
		}
	}

	// allocate the dst image
	FIBITMAP *dst = FreeImage_AllocateT(image_type, dst_width, dst_height, dst_bpp, 0, 0, 0);
	if (!dst) {
		return NULL;
	}
	
	if (dst_bpp == 8) {
		RGBQUAD * const dst_pal = FreeImage_GetPalette(dst);
		if (color_type == FIC_MINISWHITE) {
			// build an inverted greyscale palette
			CREATE_GREYSCALE_PALETTE_REVERSE(dst_pal, 256);
		} 
		/*
		else {
			// build a default greyscale palette
			// Currently, FreeImage_AllocateT already creates a default
			// greyscale palette for 8 bpp images, so we can skip this here.
			CREATE_GREYSCALE_PALETTE(dst_pal, 256);
		}
		*/
	}

	// calculate x and y offsets; since FreeImage uses bottom-up bitmaps, the
	// value of src_offset_y is measured from the bottom of the image
	unsigned src_offset_x = src_left;
	unsigned src_offset_y;
	if (src_top > 0) {
		src_offset_y = FreeImage_GetHeight(src) - src_height - src_top;
	} else {
		src_offset_y = 0;
	}

	/*
	Decide which filtering order (xy or yx) is faster for this mapping. 
	--- The theory ---
	Try to minimize calculations by counting the number of convolution multiplies
	if(dst_width*src_height <= src_width*dst_height) {
		// xy filtering
	} else {
		// yx filtering
	}
	--- The practice ---
	Try to minimize calculations by counting the number of vertical convolutions (the most time consuming task)
	if(dst_width*dst_height <= src_width*dst_height) {
		// xy filtering
	} else {
		// yx filtering
	}
	*/

	if (dst_width <= src_width) {
		// xy filtering
		// -------------

		FIBITMAP *tmp = NULL;

		if (src_width != dst_width) {
			// source and destination widths are different so, we must
			// filter horizontally
			if (src_height != dst_height) {
				// source and destination heights are also different so, we need
				// a temporary image
				tmp = FreeImage_AllocateT(image_type, dst_width, src_height, dst_bpp, 0, 0, 0);
				if (!tmp) {
					FreeImage_Unload(dst);
					return NULL;
				}
			} else {
				// source and destination heights are equal so, we can directly
				// scale into destination image (second filter method will not
				// be invoked)
				tmp = dst;
			}

			// scale source image horizontally into temporary (or destination) image
			horizontalFilter(src, src_height, src_width, src_offset_x, src_offset_y, src_pal, tmp, dst_width);

			// set x and y offsets to zero for the second filter method
			// invocation (the temporary image only contains the portion of
			// the image to be rescaled with no offsets)
			src_offset_x = 0;
			src_offset_y = 0;

			// also ensure, that the second filter method gets no source
			// palette (the temporary image is palletized only, if it is
			// greyscale; in that case, it is an 8-bit image with a linear
			// palette so, the source palette is not needed or will even be
			// mismatching, if the source palette is unordered)
			src_pal = NULL;
		} else {
			// source and destination widths are equal so, just copy the
			// image pointer
			tmp = src;
		}

		if (src_height != dst_height) {
			// source and destination heights are different so, scale
			// temporary (or source) image vertically into destination image
			verticalFilter(tmp, dst_width, src_height, src_offset_x, src_offset_y, src_pal, dst, dst_height);
		}

		// free temporary image, if not pointing to either src or dst
		if (tmp != src && tmp != dst) {
			FreeImage_Unload(tmp);
		}

	} else {
		// yx filtering
		// -------------

		// Remark:
		// The yx filtering branch could be more optimized by taking into,
		// account that (src_width != dst_width) is always true, which
		// follows from the above condition, which selects filtering order.
		// Since (dst_width <= src_width) == TRUE selects xy filtering,
		// both widths must be different when performing yx filtering.
		// However, to make the code more robust, not depending on that
		// condition and more symmetric to the xy filtering case, these
		// (src_width != dst_width) conditions are still in place.

		FIBITMAP *tmp = NULL;

		if (src_height != dst_height) {
			// source and destination heights are different so, we must
			// filter vertically
			if (src_width != dst_width) {
				// source and destination widths are also different so, we need
				// a temporary image
				tmp = FreeImage_AllocateT(image_type, src_width, dst_height, dst_bpp, 0, 0, 0);
				if (!tmp) {
					FreeImage_Unload(dst);
					return NULL;
				}
			} else {
				// source and destination widths are equal so, we can directly
				// scale into destination image (second filter method will not
				// be invoked)
				tmp = dst;
			}

			// scale source image vertically into temporary (or destination) image
			verticalFilter(src, src_width, src_height, src_offset_x, src_offset_y, src_pal, tmp, dst_height);

			// set x and y offsets to zero for the second filter method
			// invocation (the temporary image only contains the portion of
			// the image to be rescaled with no offsets)
			src_offset_x = 0;
			src_offset_y = 0;

			// also ensure, that the second filter method gets no source
			// palette (the temporary image is palletized only, if it is
			// greyscale; in that case, it is an 8-bit image with a linear
			// palette so, the source palette is not needed or will even be
			// mismatching, if the source palette is unordered)
			src_pal = NULL;

		} else {
			// source and destination heights are equal so, just copy the
			// image pointer
			tmp = src;
		}

		if (src_width != dst_width) {
			// source and destination heights are different so, scale
			// temporary (or source) image horizontally into destination image
			horizontalFilter(tmp, dst_height, src_width, src_offset_x, src_offset_y, src_pal, dst, dst_width);
		}

		// free temporary image, if not pointing to either src or dst
		if (tmp != src && tmp != dst) {
			FreeImage_Unload(tmp);
		}
	}

	return dst;
} 

void CResizeEngine::horizontalFilter(FIBITMAP *const src, unsigned height, unsigned src_width, unsigned src_offset_x, unsigned src_offset_y, const RGBQUAD *const src_pal, FIBITMAP *const dst, unsigned dst_width) {

	// allocate and calculate the contributions
	CWeightsTable weightsTable(m_pFilter, dst_width, src_width);

	// step through rows
	switch(FreeImage_GetImageType(src)) {
		case FIT_BITMAP:
		{
			switch(FreeImage_GetBPP(src)) {
				case 1:
				{
					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// transparently convert the 1-bit non-transparent greyscale
							// image to 8 bpp
							src_offset_x >>= 3;
							if (src_pal) {
								// we have got a palette
								for (unsigned y = 0; y < height; y++) {
									// scale each row
									const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
									BYTE * const dst_bits = FreeImage_GetScanLine(dst, y);

									for (unsigned x = 0; x < dst_width; x++) {
										// loop through row
										const unsigned iLeft = weightsTable.getLeftBoundary(x);		// retrieve left boundary
										const unsigned iRight = weightsTable.getRightBoundary(x);	// retrieve right boundary
										double value = 0;

										for (unsigned i = iLeft; i < iRight; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											const unsigned pixel = (src_bits[i >> 3] & (0x80 >> (i & 0x07))) != 0;
											value += (weightsTable.getWeight(x, i - iLeft) * (double)*(BYTE *)&src_pal[pixel]);
										}

										// clamp and place result in destination pixel
										dst_bits[x] = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
									}
								}
							} else {
								// we do not have a palette
								for (unsigned y = 0; y < height; y++) {
									// scale each row
									const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
									BYTE * const dst_bits = FreeImage_GetScanLine(dst, y);

									for (unsigned x = 0; x < dst_width; x++) {
										// loop through row
										const unsigned iLeft = weightsTable.getLeftBoundary(x);		// retrieve left boundary
										const unsigned iRight = weightsTable.getRightBoundary(x);	// retrieve right boundary
										double value = 0;

										for (unsigned i = iLeft; i < iRight; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											const unsigned pixel = (src_bits[i >> 3] & (0x80 >> (i & 0x07))) != 0;
											value += (weightsTable.getWeight(x, i - iLeft) * (double)pixel);
										}
										value *= 0xFF;

										// clamp and place result in destination pixel
										dst_bits[x] = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
									}
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 1-bit image
							// to 24 bpp; we always have got a palette here
							src_offset_x >>= 3;

							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
									const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
									double r = 0, g = 0, b = 0;

									for (unsigned i = iLeft; i < iRight; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i - iLeft);
										const unsigned pixel = (src_bits[i >> 3] & (0x80 >> (i & 0x07))) != 0;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += 3;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 1-bit image
							// to 32 bpp; we always have got a palette here
							src_offset_x >>= 3;

							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
									const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
									double r = 0, g = 0, b = 0, a = 0;

									for (unsigned i = iLeft; i < iRight; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i - iLeft);
										const unsigned pixel = (src_bits[i >> 3] & (0x80 >> (i & 0x07))) != 0;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += 4;
								}
							}
						}
						break;
					}
				}
				break;

				case 4:
				{
					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// transparently convert the non-transparent 4-bit greyscale image
							// to 8 bpp; we always have got a palette for 4-bit images
							src_offset_x >>= 1;

							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE * const dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
									const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
									double value = 0;

									for (unsigned i = iLeft; i < iRight; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const unsigned pixel = i & 0x01 ? src_bits[i >> 1] & 0x0F : src_bits[i >> 1] >> 4;
										value += (weightsTable.getWeight(x, i - iLeft)
												* (double)*(BYTE *)&src_pal[pixel]);
									}

									// clamp and place result in destination pixel
									dst_bits[x] = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 4-bit image
							// to 24 bpp; we always have got a palette for 4-bit images
							src_offset_x >>= 1;

							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
									const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
									double r = 0, g = 0, b = 0;

									for (unsigned i = iLeft; i < iRight; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i - iLeft);
										const unsigned pixel = i & 0x01 ? src_bits[i >> 1] & 0x0F : src_bits[i >> 1] >> 4;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += 3;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 4-bit image
							// to 32 bpp; we always have got a palette for 4-bit images
							src_offset_x >>= 1;

							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
									const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
									double r = 0, g = 0, b = 0, a = 0;

									for (unsigned i = iLeft; i < iRight; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i - iLeft);
										const unsigned pixel = i & 0x01 ? src_bits[i >> 1] & 0x0F : src_bits[i >> 1] >> 4;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += 4;
								}
							}
						}
						break;
					}
				}
				break;

				case 8:
				{
					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// scale the 8-bit non-transparent greyscale image
							// into an 8 bpp destination image
							if (src_pal) {
								// we have got a palette
								for (unsigned y = 0; y < height; y++) {
									// scale each row
									const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
									BYTE * const dst_bits = FreeImage_GetScanLine(dst, y);

									for (unsigned x = 0; x < dst_width; x++) {
										// loop through row
										const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
										const BYTE * const pixel = src_bits + iLeft;
										double value = 0;

										// for(i = iLeft to iRight)
										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											value += (weightsTable.getWeight(x, i)
													* (double)*(BYTE *)&src_pal[pixel[i]]);
										}

										// clamp and place result in destination pixel
										dst_bits[x] = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
									}
								}
							} else {
								// we do not have a palette
								for (unsigned y = 0; y < height; y++) {
									// scale each row
									const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
									BYTE * const dst_bits = FreeImage_GetScanLine(dst, y);

									for (unsigned x = 0; x < dst_width; x++) {
										// loop through row
										const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
										const BYTE * const pixel = src_bits + iLeft;
										double value = 0;

										// for(i = iLeft to iRight)
										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											value += (weightsTable.getWeight(x, i) * (double)pixel[i]);
										}

										// clamp and place result in destination pixel
										dst_bits[x] = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
									}
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 8-bit image
							// to 24 bpp; we always have got a palette here
							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
									const BYTE * const pixel = src_bits + iLeft;
									double r = 0, g = 0, b = 0;

									// for(i = iLeft to iRight)
									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i);
										const BYTE *const entry = (BYTE *)&src_pal[pixel[i]];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += 3;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 8-bit image
							// to 32 bpp; we always have got a palette here
							for (unsigned y = 0; y < height; y++) {
								// scale each row
								const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
								BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

								for (unsigned x = 0; x < dst_width; x++) {
									// loop through row
									const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
									const BYTE * const pixel = src_bits + iLeft;
									double r = 0, g = 0, b = 0, a = 0;

									// for(i = iLeft to iRight)
									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(x, i);
										const BYTE * const entry = (BYTE *)&src_pal[pixel[i]];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += 4;
								}
							}
						}
						break;
					}
				}
				break;

				case 16:
				{
					// transparently convert the 16-bit non-transparent image
					// to 24 bpp
					if (IS_FORMAT_RGB565(src)) {
						// image has 565 format
						for (unsigned y = 0; y < height; y++) {
							// scale each row
							const WORD * const src_bits = (WORD *)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x / sizeof(WORD);
							BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

							for (unsigned x = 0; x < dst_width; x++) {
								// loop through row
								const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
								const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
								const WORD *pixel = src_bits + iLeft;
								double r = 0, g = 0, b = 0;

								// for(i = iLeft to iRight)
								for (unsigned i = 0; i < iLimit; i++) {
									// scan between boundaries
									// accumulate weighted effect of each neighboring pixel
									const double weight = weightsTable.getWeight(x, i);
									r += (weight * (double)((*pixel & FI16_565_RED_MASK) >> FI16_565_RED_SHIFT));
									g += (weight * (double)((*pixel & FI16_565_GREEN_MASK) >> FI16_565_GREEN_SHIFT));
									b += (weight * (double)((*pixel & FI16_565_BLUE_MASK) >> FI16_565_BLUE_SHIFT));
									pixel++;
								}

								// clamp and place result in destination pixel
								dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(((r * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(((g * 0xFF) / 0x3F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(((b * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits += 3;
							}
						}
					} else {
						// image has 555 format
						for (unsigned y = 0; y < height; y++) {
							// scale each row
							const WORD * const src_bits = (WORD *)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x;
							BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

							for (unsigned x = 0; x < dst_width; x++) {
								// loop through row
								const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
								const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
								const WORD *pixel = src_bits + iLeft;
								double r = 0, g = 0, b = 0;

								// for(i = iLeft to iRight)
								for (unsigned i = 0; i < iLimit; i++) {
									// scan between boundaries
									// accumulate weighted effect of each neighboring pixel
									const double weight = weightsTable.getWeight(x, i);
									r += (weight * (double)((*pixel & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT));
									g += (weight * (double)((*pixel & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT));
									b += (weight * (double)((*pixel & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT));
									pixel++;
								}

								// clamp and place result in destination pixel
								dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(((r * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(((g * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(((b * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits += 3;
							}
						}
					}
				}
				break;

				case 24:
				{
					// scale the 24-bit non-transparent image
					// into a 24 bpp destination image
					for (unsigned y = 0; y < height; y++) {
						// scale each row
						const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x * 3;
						BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

						for (unsigned x = 0; x < dst_width; x++) {
							// loop through row
							const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
							const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
							const BYTE * pixel = src_bits + iLeft * 3;
							double r = 0, g = 0, b = 0;

							// for(i = iLeft to iRight)
							for (unsigned i = 0; i < iLimit; i++) {
								// scan between boundaries
								// accumulate weighted effect of each neighboring pixel
								const double weight = weightsTable.getWeight(x, i);
								r += (weight * (double)pixel[FI_RGBA_RED]);
								g += (weight * (double)pixel[FI_RGBA_GREEN]);
								b += (weight * (double)pixel[FI_RGBA_BLUE]);
								pixel += 3;
							}

							// clamp and place result in destination pixel
							dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
							dst_bits += 3;
						}
					}
				}
				break;

				case 32:
				{
					// scale the 32-bit transparent image
					// into a 32 bpp destination image
					for (unsigned y = 0; y < height; y++) {
						// scale each row
						const BYTE * const src_bits = FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x * 4;
						BYTE *dst_bits = FreeImage_GetScanLine(dst, y);

						for (unsigned x = 0; x < dst_width; x++) {
							// loop through row
							const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
							const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
							const BYTE *pixel = src_bits + iLeft * 4;
							double r = 0, g = 0, b = 0, a = 0;

							// for(i = iLeft to iRight)
							for (unsigned i = 0; i < iLimit; i++) {
								// scan between boundaries
								// accumulate weighted effect of each neighboring pixel
								const double weight = weightsTable.getWeight(x, i);
								r += (weight * (double)pixel[FI_RGBA_RED]);
								g += (weight * (double)pixel[FI_RGBA_GREEN]);
								b += (weight * (double)pixel[FI_RGBA_BLUE]);
								a += (weight * (double)pixel[FI_RGBA_ALPHA]);
								pixel += 4;
							}

							// clamp and place result in destination pixel
							dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
							dst_bits += 4;
						}
					}
				}
				break;
			}
		}
		break;

		case FIT_UINT16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / src_width) / sizeof(WORD);

			for (unsigned y = 0; y < height; y++) {
				// scale each row
				const WORD *src_bits = (WORD*)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x / sizeof(WORD);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);

				for (unsigned x = 0; x < dst_width; x++) {
					// loop through row
					const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
					const WORD *pixel = src_bits + iLeft * wordspp;
					double value = 0;

					// for(i = iLeft to iRight)
					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(x, i);						
						value += (weight * (double)pixel[0]);
						pixel++;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(value + 0.5), 0, 0xFFFF);
					dst_bits += wordspp;
				}
			}
		}
		break;

		case FIT_RGB16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / src_width) / sizeof(WORD);

			for (unsigned y = 0; y < height; y++) {
				// scale each row
				const WORD *src_bits = (WORD*)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x / sizeof(WORD);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);

				for (unsigned x = 0; x < dst_width; x++) {
					// loop through row
					const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
					const WORD *pixel = src_bits + iLeft * wordspp;
					double r = 0, g = 0, b = 0;

					// for(i = iLeft to iRight)
					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(x, i);						
						r += (weight * (double)pixel[0]);
						g += (weight * (double)pixel[1]);
						b += (weight * (double)pixel[2]);
						pixel += wordspp;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(r + 0.5), 0, 0xFFFF);
					dst_bits[1] = (WORD)CLAMP<int>((int)(g + 0.5), 0, 0xFFFF);
					dst_bits[2] = (WORD)CLAMP<int>((int)(b + 0.5), 0, 0xFFFF);
					dst_bits += wordspp;
				}
			}
		}
		break;

		case FIT_RGBA16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / src_width) / sizeof(WORD);

			for (unsigned y = 0; y < height; y++) {
				// scale each row
				const WORD *src_bits = (WORD*)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x / sizeof(WORD);
				WORD *dst_bits = (WORD*)FreeImage_GetScanLine(dst, y);

				for (unsigned x = 0; x < dst_width; x++) {
					// loop through row
					const unsigned iLeft = weightsTable.getLeftBoundary(x);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(x) - iLeft;	// retrieve right boundary
					const WORD *pixel = src_bits + iLeft * wordspp;
					double r = 0, g = 0, b = 0, a = 0;

					// for(i = iLeft to iRight)
					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(x, i);						
						r += (weight * (double)pixel[0]);
						g += (weight * (double)pixel[1]);
						b += (weight * (double)pixel[2]);
						a += (weight * (double)pixel[3]);
						pixel += wordspp;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(r + 0.5), 0, 0xFFFF);
					dst_bits[1] = (WORD)CLAMP<int>((int)(g + 0.5), 0, 0xFFFF);
					dst_bits[2] = (WORD)CLAMP<int>((int)(b + 0.5), 0, 0xFFFF);
					dst_bits[3] = (WORD)CLAMP<int>((int)(a + 0.5), 0, 0xFFFF);
					dst_bits += wordspp;
				}
			}
		}
		break;

		case FIT_FLOAT:
		case FIT_RGBF:
		case FIT_RGBAF:
		{
			// Calculate the number of floats per pixel (1 for 32-bit, 3 for 96-bit or 4 for 128-bit)
			const unsigned floatspp = (FreeImage_GetLine(src) / src_width) / sizeof(float);

			for(unsigned y = 0; y < height; y++) {
				// scale each row
				const float *src_bits = (float*)FreeImage_GetScanLine(src, y + src_offset_y) + src_offset_x / sizeof(float);
				float *dst_bits = (float*)FreeImage_GetScanLine(dst, y);

				for(unsigned x = 0; x < dst_width; x++) {
					// loop through row
					const unsigned iLeft = weightsTable.getLeftBoundary(x);    // retrieve left boundary
					const unsigned iRight = weightsTable.getRightBoundary(x);  // retrieve right boundary
					double value[4] = {0, 0, 0, 0};                            // 4 = 128 bpp max

					for(unsigned i = iLeft; i < iRight; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(x, i-iLeft);

						unsigned index = i * floatspp;	// pixel index
						for (unsigned j = 0; j < floatspp; j++) {
							value[j] += (weight * (double)src_bits[index++]);
						}
					}

					// place result in destination pixel
					for (unsigned j = 0; j < floatspp; j++) {
						dst_bits[j] = (float)value[j];
					}

					dst_bits += floatspp;
				}
			}
		}
		break;
	}
}

/// Performs vertical image filtering
void CResizeEngine::verticalFilter(FIBITMAP *const src, unsigned width, unsigned src_height, unsigned src_offset_x, unsigned src_offset_y, const RGBQUAD *const src_pal, FIBITMAP *const dst, unsigned dst_height) {

	// allocate and calculate the contributions
	CWeightsTable weightsTable(m_pFilter, dst_height, src_height);

	// step through columns
	switch(FreeImage_GetImageType(src)) {
		case FIT_BITMAP:
		{
			const unsigned dst_pitch = FreeImage_GetPitch(dst);
			BYTE * const dst_base = FreeImage_GetBits(dst);

			switch(FreeImage_GetBPP(src)) {
				case 1:
				{
					const unsigned src_pitch = FreeImage_GetPitch(src);
					const BYTE * const src_base = FreeImage_GetBits(src)
							+ src_offset_y * src_pitch + (src_offset_x >> 3);

					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// transparently convert the 1-bit non-transparent greyscale
							// image to 8 bpp
							if (src_pal) {
								// we have got a palette
								for (unsigned x = 0; x < width; x++) {
									// work on column x in dst
									BYTE *dst_bits = dst_base + x;
									const unsigned index = x >> 3;
									const unsigned mask = 0x80 >> (x & 0x07);

									// scale each column
									for (unsigned y = 0; y < dst_height; y++) {
										// loop through column
										const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
										const BYTE *src_bits = src_base + iLeft * src_pitch + index;
										double value = 0;

										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											const unsigned pixel = (*src_bits & mask) != 0;
											value += (weightsTable.getWeight(y, i)
													* (double)*(BYTE *)&src_pal[pixel]);
											src_bits += src_pitch;
										}
										value *= 0xFF;

										// clamp and place result in destination pixel
										*dst_bits = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
										dst_bits += dst_pitch;
									}
								}
							} else {
								// we do not have a palette
								for (unsigned x = 0; x < width; x++) {
									// work on column x in dst
									BYTE *dst_bits = dst_base + x;
									const unsigned index = x >> 3;
									const unsigned mask = 0x80 >> (x & 0x07);

									// scale each column
									for (unsigned y = 0; y < dst_height; y++) {
										// loop through column
										const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
										const BYTE *src_bits = src_base + iLeft * src_pitch + index;
										double value = 0;

										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											value += (weightsTable.getWeight(y, i)
													* (double)((*src_bits & mask) != 0));
											src_bits += src_pitch;
										}
										value *= 0xFF;

										// clamp and place result in destination pixel
										*dst_bits = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
										dst_bits += dst_pitch;
									}
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 1-bit image
							// to 24 bpp; we always have got a palette here
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 3;
								const unsigned index = x >> 3;
								const unsigned mask = 0x80 >> (x & 0x07);

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + index;
									double r = 0, g = 0, b = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const unsigned pixel = (*src_bits & mask) != 0;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 1-bit image
							// to 32 bpp; we always have got a palette here
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 4;
								const unsigned index = x >> 3;
								const unsigned mask = 0x80 >> (x & 0x07);

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + index;
									double r = 0, g = 0, b = 0, a = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const unsigned pixel = (*src_bits & mask) != 0;
										const BYTE * const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;
					}
				}
				break;

				case 4:
				{
					const unsigned src_pitch = FreeImage_GetPitch(src);
					const BYTE *const src_base = FreeImage_GetBits(src) + src_offset_y * src_pitch + (src_offset_x >> 1);

					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// transparently convert the non-transparent 4-bit greyscale image
							// to 8 bpp; we always have got a palette for 4-bit images
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x;
								const unsigned index = x >> 1;

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + index;
									double value = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const unsigned pixel = x & 0x01 ? *src_bits & 0x0F : *src_bits >> 4;
										value += (weightsTable.getWeight(y, i)
												* (double)*(BYTE *)&src_pal[pixel]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									*dst_bits = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 4-bit image
							// to 24 bpp; we always have got a palette for 4-bit images
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 3;
								const unsigned index = x >> 1;

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + index;
									double r = 0, g = 0, b = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const unsigned pixel = x & 0x01 ? *src_bits & 0x0F : *src_bits >> 4;
										const BYTE *const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 4-bit image
							// to 32 bpp; we always have got a palette for 4-bit images
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 4;
								const unsigned index = x >> 1;

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + index;
									double r = 0, g = 0, b = 0, a = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const unsigned pixel = x & 0x01 ? *src_bits & 0x0F : *src_bits >> 4;
										const BYTE *const entry = (BYTE *)&src_pal[pixel];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;
					}
				}
				break;

				case 8:
				{
					const unsigned src_pitch = FreeImage_GetPitch(src);
					const BYTE *const src_base = FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x;

					switch(FreeImage_GetBPP(dst)) {
						case 8:
						{
							// scale the 8-bit non-transparent greyscale image
							// into an 8 bpp destination image
							if (src_pal) {
								// we have got a palette
								for (unsigned x = 0; x < width; x++) {
									// work on column x in dst
									BYTE *dst_bits = dst_base + x;

									// scale each column
									for (unsigned y = 0; y < dst_height; y++) {
										// loop through column
										const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
										const BYTE *src_bits = src_base + iLeft * src_pitch + x;
										double value = 0;

										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											value += (weightsTable.getWeight(y, i)
													* (double)*(BYTE *)&src_pal[*src_bits]);
											src_bits += src_pitch;
										}

										// clamp and place result in destination pixel
										*dst_bits = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
										dst_bits += dst_pitch;
									}
								}
							} else {
								// we do not have a palette
								for (unsigned x = 0; x < width; x++) {
									// work on column x in dst
									BYTE *dst_bits = dst_base + x;

									// scale each column
									for (unsigned y = 0; y < dst_height; y++) {
										// loop through column
										const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
										const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
										const BYTE *src_bits = src_base + iLeft * src_pitch + x;
										double value = 0;

										for (unsigned i = 0; i < iLimit; i++) {
											// scan between boundaries
											// accumulate weighted effect of each neighboring pixel
											value += (weightsTable.getWeight(y, i)
													* (double)*src_bits);
											src_bits += src_pitch;
										}

										// clamp and place result in destination pixel
										*dst_bits = (BYTE)CLAMP<int>((int)(value + 0.5), 0, 0xFF);
										dst_bits += dst_pitch;
									}
								}
							}
						}
						break;

						case 24:
						{
							// transparently convert the non-transparent 8-bit image
							// to 24 bpp; we always have got a palette here
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 3;

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + x;
									double r = 0, g = 0, b = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const BYTE * const entry = (BYTE *)&src_pal[*src_bits];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;

						case 32:
						{
							// transparently convert the transparent 8-bit image
							// to 32 bpp; we always have got a palette here
							for (unsigned x = 0; x < width; x++) {
								// work on column x in dst
								BYTE *dst_bits = dst_base + x * 4;

								// scale each column
								for (unsigned y = 0; y < dst_height; y++) {
									// loop through column
									const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
									const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
									const BYTE *src_bits = src_base + iLeft * src_pitch + x;
									double r = 0, g = 0, b = 0, a = 0;

									for (unsigned i = 0; i < iLimit; i++) {
										// scan between boundaries
										// accumulate weighted effect of each neighboring pixel
										const double weight = weightsTable.getWeight(y, i);
										const BYTE * const entry = (BYTE *)&src_pal[*src_bits];
										r += (weight * (double)entry[FI_RGBA_RED]);
										g += (weight * (double)entry[FI_RGBA_GREEN]);
										b += (weight * (double)entry[FI_RGBA_BLUE]);
										a += (weight * (double)entry[FI_RGBA_ALPHA]);
										src_bits += src_pitch;
									}

									// clamp and place result in destination pixel
									dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(r + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(g + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(b + 0.5), 0, 0xFF);
									dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int)(a + 0.5), 0, 0xFF);
									dst_bits += dst_pitch;
								}
							}
						}
						break;
					}
				}
				break;

				case 16:
				{
					// transparently convert the 16-bit non-transparent image
					// to 24 bpp
					const unsigned src_pitch = FreeImage_GetPitch(src) / sizeof(WORD);
					const WORD *const src_base = (WORD *)FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x;

					if (IS_FORMAT_RGB565(src)) {
						// image has 565 format
						for (unsigned x = 0; x < width; x++) {
							// work on column x in dst
							BYTE *dst_bits = dst_base + x * 3;

							// scale each column
							for (unsigned y = 0; y < dst_height; y++) {
								// loop through column
								const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
								const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
								const WORD *src_bits = src_base + iLeft * src_pitch + x;
								double r = 0, g = 0, b = 0;

								for (unsigned i = 0; i < iLimit; i++) {
									// scan between boundaries
									// accumulate weighted effect of each neighboring pixel
									const double weight = weightsTable.getWeight(y, i);
									r += (weight * (double)((*src_bits & FI16_565_RED_MASK) >> FI16_565_RED_SHIFT));
									g += (weight * (double)((*src_bits & FI16_565_GREEN_MASK) >> FI16_565_GREEN_SHIFT));
									b += (weight * (double)((*src_bits & FI16_565_BLUE_MASK) >> FI16_565_BLUE_SHIFT));
									src_bits += src_pitch;
								}

								// clamp and place result in destination pixel
								dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(((r * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(((g * 0xFF) / 0x3F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(((b * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits += dst_pitch;
							}
						}
					} else {
						// image has 555 format
						for (unsigned x = 0; x < width; x++) {
							// work on column x in dst
							BYTE *dst_bits = dst_base + x * 3;

							// scale each column
							for (unsigned y = 0; y < dst_height; y++) {
								// loop through column
								const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
								const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
								const WORD *src_bits = src_base + iLeft * src_pitch + x;
								double r = 0, g = 0, b = 0;

								for (unsigned i = 0; i < iLimit; i++) {
									// scan between boundaries
									// accumulate weighted effect of each neighboring pixel
									const double weight = weightsTable.getWeight(y, i);
									r += (weight * (double)((*src_bits & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT));
									g += (weight * (double)((*src_bits & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT));
									b += (weight * (double)((*src_bits & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT));
									src_bits += src_pitch;
								}

								// clamp and place result in destination pixel
								dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int)(((r * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int)(((g * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int)(((b * 0xFF) / 0x1F) + 0.5), 0, 0xFF);
								dst_bits += dst_pitch;
							}
						}
					}
				}
				break;

				case 24:
				{
					// scale the 24-bit transparent image
					// into a 24 bpp destination image
					const unsigned src_pitch = FreeImage_GetPitch(src);
					const BYTE *const src_base = FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x * 3;

					for (unsigned x = 0; x < width; x++) {
						// work on column x in dst
						const unsigned index = x * 3;
						BYTE *dst_bits = dst_base + index;

						// scale each column
						for (unsigned y = 0; y < dst_height; y++) {
							// loop through column
							const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
							const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
							const BYTE *src_bits = src_base + iLeft * src_pitch + index;
							double r = 0, g = 0, b = 0;

							for (unsigned i = 0; i < iLimit; i++) {
								// scan between boundaries
								// accumulate weighted effect of each neighboring pixel
								const double weight = weightsTable.getWeight(y, i);
								r += (weight * (double)src_bits[FI_RGBA_RED]);
								g += (weight * (double)src_bits[FI_RGBA_GREEN]);
								b += (weight * (double)src_bits[FI_RGBA_BLUE]);
								src_bits += src_pitch;
							}

							// clamp and place result in destination pixel
							dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int) (r + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int) (g + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int) (b + 0.5), 0, 0xFF);
							dst_bits += dst_pitch;
						}
					}
				}
				break;

				case 32:
				{
					// scale the 32-bit transparent image
					// into a 32 bpp destination image
					const unsigned src_pitch = FreeImage_GetPitch(src);
					const BYTE *const src_base = FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x * 4;

					for (unsigned x = 0; x < width; x++) {
						// work on column x in dst
						const unsigned index = x * 4;
						BYTE *dst_bits = dst_base + index;

						// scale each column
						for (unsigned y = 0; y < dst_height; y++) {
							// loop through column
							const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
							const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
							const BYTE *src_bits = src_base + iLeft * src_pitch + index;
							double r = 0, g = 0, b = 0, a = 0;

							for (unsigned i = 0; i < iLimit; i++) {
								// scan between boundaries
								// accumulate weighted effect of each neighboring pixel
								const double weight = weightsTable.getWeight(y, i);
								r += (weight * (double)src_bits[FI_RGBA_RED]);
								g += (weight * (double)src_bits[FI_RGBA_GREEN]);
								b += (weight * (double)src_bits[FI_RGBA_BLUE]);
								a += (weight * (double)src_bits[FI_RGBA_ALPHA]);
								src_bits += src_pitch;
							}

							// clamp and place result in destination pixel
							dst_bits[FI_RGBA_RED]	= (BYTE)CLAMP<int>((int) (r + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_GREEN]	= (BYTE)CLAMP<int>((int) (g + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_BLUE]	= (BYTE)CLAMP<int>((int) (b + 0.5), 0, 0xFF);
							dst_bits[FI_RGBA_ALPHA]	= (BYTE)CLAMP<int>((int) (a + 0.5), 0, 0xFF);
							dst_bits += dst_pitch;
						}
					}
				}
				break;
			}
		}
		break;

		case FIT_UINT16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / width) / sizeof(WORD);

			const unsigned dst_pitch = FreeImage_GetPitch(dst) / sizeof(WORD);
			WORD *const dst_base = (WORD *)FreeImage_GetBits(dst);

			const unsigned src_pitch = FreeImage_GetPitch(src) / sizeof(WORD);
			const WORD *const src_base = (WORD *)FreeImage_GetBits(src)	+ src_offset_y * src_pitch + src_offset_x * wordspp;

			for (unsigned x = 0; x < width; x++) {
				// work on column x in dst
				const unsigned index = x * wordspp;	// pixel index
				WORD *dst_bits = dst_base + index;

				// scale each column
				for (unsigned y = 0; y < dst_height; y++) {
					// loop through column
					const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
					const WORD *src_bits = src_base + iLeft * src_pitch + index;
					double value = 0;

					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(y, i);
						value += (weight * (double)src_bits[0]);
						src_bits += src_pitch;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(value + 0.5), 0, 0xFFFF);

					dst_bits += dst_pitch;
				}
			}
		}
		break;

		case FIT_RGB16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / width) / sizeof(WORD);

			const unsigned dst_pitch = FreeImage_GetPitch(dst) / sizeof(WORD);
			WORD *const dst_base = (WORD *)FreeImage_GetBits(dst);

			const unsigned src_pitch = FreeImage_GetPitch(src) / sizeof(WORD);
			const WORD *const src_base = (WORD *)FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x * wordspp;

			for (unsigned x = 0; x < width; x++) {
				// work on column x in dst
				const unsigned index = x * wordspp;	// pixel index
				WORD *dst_bits = dst_base + index;

				// scale each column
				for (unsigned y = 0; y < dst_height; y++) {
					// loop through column
					const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
					const WORD *src_bits = src_base + iLeft * src_pitch + index;
					double r = 0, g = 0, b = 0;

					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(y, i);					
						r += (weight * (double)src_bits[0]);
						g += (weight * (double)src_bits[1]);
						b += (weight * (double)src_bits[2]);

						src_bits += src_pitch;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(r + 0.5), 0, 0xFFFF);
					dst_bits[1] = (WORD)CLAMP<int>((int)(g + 0.5), 0, 0xFFFF);
					dst_bits[2] = (WORD)CLAMP<int>((int)(b + 0.5), 0, 0xFFFF);

					dst_bits += dst_pitch;
				}
			}
		}
		break;

		case FIT_RGBA16:
		{
			// Calculate the number of words per pixel (1 for 16-bit, 3 for 48-bit or 4 for 64-bit)
			const unsigned wordspp = (FreeImage_GetLine(src) / width) / sizeof(WORD);

			const unsigned dst_pitch = FreeImage_GetPitch(dst) / sizeof(WORD);
			WORD *const dst_base = (WORD *)FreeImage_GetBits(dst);

			const unsigned src_pitch = FreeImage_GetPitch(src) / sizeof(WORD);
			const WORD *const src_base = (WORD *)FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x * wordspp;

			for (unsigned x = 0; x < width; x++) {
				// work on column x in dst
				const unsigned index = x * wordspp;	// pixel index
				WORD *dst_bits = dst_base + index;

				// scale each column
				for (unsigned y = 0; y < dst_height; y++) {
					// loop through column
					const unsigned iLeft = weightsTable.getLeftBoundary(y);				// retrieve left boundary
					const unsigned iLimit = weightsTable.getRightBoundary(y) - iLeft;	// retrieve right boundary
					const WORD *src_bits = src_base + iLeft * src_pitch + index;
					double r = 0, g = 0, b = 0, a = 0;

					for (unsigned i = 0; i < iLimit; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(y, i);					
						r += (weight * (double)src_bits[0]);
						g += (weight * (double)src_bits[1]);
						b += (weight * (double)src_bits[2]);
						a += (weight * (double)src_bits[3]);

						src_bits += src_pitch;
					}

					// clamp and place result in destination pixel
					dst_bits[0] = (WORD)CLAMP<int>((int)(r + 0.5), 0, 0xFFFF);
					dst_bits[1] = (WORD)CLAMP<int>((int)(g + 0.5), 0, 0xFFFF);
					dst_bits[2] = (WORD)CLAMP<int>((int)(b + 0.5), 0, 0xFFFF);
					dst_bits[3] = (WORD)CLAMP<int>((int)(a + 0.5), 0, 0xFFFF);

					dst_bits += dst_pitch;
				}
			}
		}
		break;

		case FIT_FLOAT:
		case FIT_RGBF:
		case FIT_RGBAF:
		{
			// Calculate the number of floats per pixel (1 for 32-bit, 3 for 96-bit or 4 for 128-bit)
			const unsigned floatspp = (FreeImage_GetLine(src) / width) / sizeof(float);

			const unsigned dst_pitch = FreeImage_GetPitch(dst) / sizeof(float);
			float *const dst_base = (float *)FreeImage_GetBits(dst);

			const unsigned src_pitch = FreeImage_GetPitch(src) / sizeof(float);
			const float *const src_base = (float *)FreeImage_GetBits(src) + src_offset_y * src_pitch + src_offset_x * floatspp;

			for (unsigned x = 0; x < width; x++) {
				// work on column x in dst
				const unsigned index = x * floatspp;	// pixel index
				float *dst_bits = (float *)dst_base + index;

				// scale each column
				for (unsigned y = 0; y < dst_height; y++) {
					// loop through column
					const unsigned iLeft = weightsTable.getLeftBoundary(y);    // retrieve left boundary
					const unsigned iRight = weightsTable.getRightBoundary(y);  // retrieve right boundary
					const float *src_bits = src_base + iLeft * src_pitch + index;
					double value[4] = {0, 0, 0, 0};                            // 4 = 128 bpp max

					for (unsigned i = iLeft; i < iRight; i++) {
						// scan between boundaries
						// accumulate weighted effect of each neighboring pixel
						const double weight = weightsTable.getWeight(y, i - iLeft);
						for (unsigned j = 0; j < floatspp; j++) {
							value[j] += (weight * (double)src_bits[j]);
						}
						src_bits += src_pitch;
					}

					// place result in destination pixel
					for (unsigned j = 0; j < floatspp; j++) {
						dst_bits[j] = (float)value[j];
					}
					dst_bits += dst_pitch;
				}
			}
		}
		break;
	}
}

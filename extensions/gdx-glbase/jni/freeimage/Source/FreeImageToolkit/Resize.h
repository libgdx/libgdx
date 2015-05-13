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

#ifndef _RESIZE_H_
#define _RESIZE_H_

#include "FreeImage.h"
#include "Utilities.h"
#include "Filters.h" 

/**
  Filter weights table.<br>
  This class stores contribution information for an entire line (row or column).
*/
class CWeightsTable
{
/**
  Sampled filter weight table.<br>
  Contribution information for a single pixel
*/
typedef struct {
	/// Normalized weights of neighboring pixels
	double *Weights;
	/// Bounds of source pixels window
	unsigned Left, Right;
} Contribution;

private:
	/// Row (or column) of contribution weights 
	Contribution *m_WeightTable;
	/// Filter window size (of affecting source pixels) 
	unsigned m_WindowSize;
	/// Length of line (no. of rows / cols) 
	unsigned m_LineLength;

public:
	/** 
	Constructor<br>
	Allocate and compute the weights table
	@param pFilter Filter used for upsampling or downsampling
	@param uDstSize Length (in pixels) of the destination line buffer
	@param uSrcSize Length (in pixels) of the source line buffer
	*/
	CWeightsTable(CGenericFilter *pFilter, unsigned uDstSize, unsigned uSrcSize);

	/**
	Destructor<br>
	Destroy the weights table
	*/
	~CWeightsTable();

	/** Retrieve a filter weight, given source and destination positions
	@param dst_pos Pixel position in destination line buffer
	@param src_pos Pixel position in source line buffer
	@return Returns the filter weight
	*/
	double getWeight(unsigned dst_pos, unsigned src_pos) {
		return m_WeightTable[dst_pos].Weights[src_pos];
	}

	/** Retrieve left boundary of source line buffer
	@param dst_pos Pixel position in destination line buffer
	@return Returns the left boundary of source line buffer
	*/
	unsigned getLeftBoundary(unsigned dst_pos) {
		return m_WeightTable[dst_pos].Left;
	}

	/** Retrieve right boundary of source line buffer
	@param dst_pos Pixel position in destination line buffer
	@return Returns the right boundary of source line buffer
	*/
	unsigned getRightBoundary(unsigned dst_pos) {
		return m_WeightTable[dst_pos].Right;
	}
};

// ---------------------------------------------

/**
 CResizeEngine<br>
 This class performs filtered zoom. It scales an image to the desired dimensions with 
 any of the CGenericFilter derived filter class.<br>
 It works with FIT_BITMAP buffers, WORD buffers (FIT_UINT16, FIT_RGB16, FIT_RGBA16) 
 and float buffers (FIT_FLOAT, FIT_RGBF, FIT_RGBAF).<br><br>

 <b>References</b> : <br>
 [1] Paul Heckbert, C code to zoom raster images up or down, with nice filtering. 
 UC Berkeley, August 1989. [online] http://www-2.cs.cmu.edu/afs/cs.cmu.edu/Web/People/ph/heckbert.html
 [2] Eran Yariv, Two Pass Scaling using Filters. The Code Project, December 1999. 
 [online] http://www.codeproject.com/bitmap/2_pass_scaling.asp

*/
class CResizeEngine
{
private:
	/// Pointer to the FIR / IIR filter
	CGenericFilter* m_pFilter;

public:

	/**
	Constructor
	@param filter FIR /IIR filter to be used
	*/
	CResizeEngine(CGenericFilter* filter):m_pFilter(filter) {}

	/// Destructor
	virtual ~CResizeEngine() {}

	/** Scale an image to the desired dimensions.

	Method CResizeEngine::scale, as well as the two filtering methods
	CResizeEngine::horizontalFilter and CResizeEngine::verticalFilter take
	four additional parameters, that define a rectangle in the source
	image to be rescaled.

	These are src_left, src_top, src_width and src_height and should work
	like these of function FreeImage_Copy. However, src_left and src_top are
	actually named src_offset_x and src_offset_y in the filtering methods.

	Additionally, since src_height and dst_height are always the same for
	method horizontalFilter as src_width and dst_width are always the same
	for verticalFilter, these have been stripped down to a single parameter
	height and width for horizontalFilter and verticalFilter respectively.

	Currently, method scale is called with the actual size of the source
	image. However, in a future version, we could provide a new function
	called FreeImage_RescaleRect that rescales only part of an image. 

	@param src Pointer to the source image
	@param dst_width Destination image width
	@param dst_height Destination image height
	@param src_left Left boundary of the source rectangle to be scaled
	@param src_top Top boundary of the source rectangle to be scaled
	@param src_width Width of the source rectangle to be scaled
	@param src_height Height of the source rectangle to be scaled
	@return Returns the scaled image if successful, returns NULL otherwise
	*/
	FIBITMAP* scale(FIBITMAP *src, unsigned dst_width, unsigned dst_height, unsigned src_left, unsigned src_top, unsigned src_width, unsigned src_height);

private:

	/**
	Performs horizontal image filtering

	@param src Source image
	@param height Source / Destination image height
	@param src_width Source image width
	@param src_offset_x
	@param src_offset_y
	@param src_pal
	@param dst Destination image
	@param dst_width Destination image width
	*/
	void horizontalFilter(FIBITMAP * const src, const unsigned height, const unsigned src_width,
			const unsigned src_offset_x, const unsigned src_offset_y, const RGBQUAD * const src_pal,
			FIBITMAP * const dst, const unsigned dst_width);

	/**
	Performs vertical image filtering
	@param src Source image
	@param width Source / Destination image width
	@param src_height Source image height
	@param src_offset_x
	@param src_offset_y
	@param src_pal
	@param dst Destination image
	@param dst_height Destination image height
	*/
	void verticalFilter(FIBITMAP * const src, const unsigned width, const unsigned src_height,
			const unsigned src_offset_x, const unsigned src_offset_y, const RGBQUAD * const src_pal,
			FIBITMAP * const dst, const unsigned dst_height);
};

#endif //   _RESIZE_H_

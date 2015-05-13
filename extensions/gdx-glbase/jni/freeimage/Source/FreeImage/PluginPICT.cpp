// ==========================================================
// Apple Macintosh QuickDraw/PICT Loader
//
// Design and implementation by
// - Amir Ebrahimi (amir@unity3d.com)
//
// Based on PICT loading code from paintlib (http://www.paintlib.de/paintlib/).
//
// Paintlib License:
// The paintlib source code and all documentation are copyright (c) 1996-2002 
// Ulrich von Zadow and other contributors.
//
// The paintlib source code is supplied "AS IS". Ulrich von Zadow and other 
// contributors disclaim all warranties, expressed or implied, including, without
// limitation, the warranties of merchantability and of fitness for any purpose. 
// The authors assume no liability for direct, indirect, incidental, special, 
// exemplary, or consequential damages, which may result from the use of paintlib, 
// even if advised of the possibility of such damage.
//
// Permission is hereby granted to use, copy, modify, and distribute this source 
// code, or portions hereof, for any purpose, without fee, subject to the following 
// restrictions:
//
// 1. The origin of this source code must not be misrepresented.
// 2. Altered versions must be plainly marked as such and must not be misrepresented
//    as being the original source.
// 3. This Copyright notice may not be removed or altered from any source or altered 
//    source distribution.
// 4. Executables containing paintlib or parts of it must state that the software 
//    "contains paintlib code. paintlib is copyright (c) 1996-2002 Ulrich von Zadow
//    and other contributors.". This notice must be displayed in at least one place
//    where the copyright for the software itself is displayed. The documentation must 
//    also contain this notice.
//
// Bug fixes were made to the original code to support version 2 PICT files
// properly.
// 
// Additional resources:
// http://developer.apple.com/documentation/mac/QuickDraw/QuickDraw-458.html
// http://www.fileformat.info/format/macpict/egff.htm
// 
// Notes (http://lists.apple.com/archives/java-dev/2006/Apr/msg00588.html):
// There are three main types of PICT files:
//  - Version 1
//  - Version 2
//  - Extended Version 2
//
// Some things to look out for:
//  - The bounds and target DPI are stored in a different place in all three.
//  - Some of the values are fixed-point shorts ( short / 65536f )
//  - Values are big endian 
//  - All of this may be *preceded* by a 512 byte header--sometimes it is
//    there, and sometimes it isn't. You just have to check for the magic
//	  values in both places.
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

// ==========================================================
// Plugin Interface
// ==========================================================
static int s_format_id;

static const int outputMessageSize = 256;

// ==========================================================
// Internal functions
// ==========================================================

static unsigned
Read8(FreeImageIO *io, fi_handle handle) {
	unsigned char i = 0;
	io->read_proc(&i, 1, 1, handle);
	return i;
}

static unsigned
Read16(FreeImageIO *io, fi_handle handle) {
	// reads a two-byte big-endian integer from the given file and returns its value.
	// assumes unsigned.
	
	unsigned hi = Read8(io, handle);
	unsigned lo = Read8(io, handle);
	return lo + (hi << 8);
}

static unsigned
Read32(FreeImageIO *io, fi_handle handle) {
	// reads a four-byte big-endian integer from the given file and returns its value.
	// assumes unsigned.
	
	unsigned b3 = Read8(io, handle);
	unsigned b2 = Read8(io, handle);
	unsigned b1 = Read8(io, handle);
	unsigned b0 = Read8(io, handle);
	return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
}

// ----------------------------------------------------------

struct OpDef
{
	const char * name;
	int    len;
	const char * description;
};

// for reserved opcodes
#define res(length) { "reserved", (length), "reserved for Apple use" }
#define RGB_LEN 6
#define WORD_LEN -1
#define NA 0

static OpDef optable[] =
{
/* 0x00 */  { "NOP",               0, "nop" },
/* 0x01 */  { "Clip",             NA, "clip" },
/* 0x02 */  { "BkPat",             8, "background pattern" },
/* 0x03 */  { "TxFont",            2, "text font (word)" },
/* 0x04 */  { "TxFace",            1, "text face (byte)" },
/* 0x05 */  { "TxMode",            2, "text mode (word)" },
/* 0x06 */  { "SpExtra",           4, "space extra (fixed point)" },
/* 0x07 */  { "PnSize",            4, "pen size (point)" },
/* 0x08 */  { "PnMode",            2, "pen mode (word)" },
/* 0x09 */  { "PnPat",             8, "pen pattern" },
/* 0x0a */  { "FillPat",           8, "fill pattern" },
/* 0x0b */  { "OvSize",            4, "oval size (point)" },
/* 0x0c */  { "Origin",            4, "dh, dv (word)" },
/* 0x0d */  { "TxSize",            2, "text size (word)" },
/* 0x0e */  { "FgColor",           4, "foreground color (longword)" },
/* 0x0f */  { "BkColor",           4, "background color (longword)" },
/* 0x10 */  { "TxRatio",           8, "numerator (point), denominator (point)" },
/* 0x11 */  { "Version",           1, "version (byte)" },
/* 0x12 */  { "BkPixPat",         NA, "color background pattern" },
/* 0x13 */  { "PnPixPat",         NA, "color pen pattern" },
/* 0x14 */  { "FillPixPat",       NA, "color fill pattern" },
/* 0x15 */  { "PnLocHFrac",        2, "fractional pen position" },
/* 0x16 */  { "ChExtra",           2, "extra for each character" },
/* 0x17 */  res(0),
/* 0x18 */  res(0),
/* 0x19 */  res(0),
/* 0x1a */  { "RGBFgCol",    RGB_LEN, "RGB foreColor" },
/* 0x1b */  { "RGBBkCol",    RGB_LEN, "RGB backColor" },
/* 0x1c */  { "HiliteMode",        0, "hilite mode flag" },
/* 0x1d */  { "HiliteColor", RGB_LEN, "RGB hilite color" },
/* 0x1e */  { "DefHilite",         0, "Use default hilite color" },
/* 0x1f */  { "OpColor",           6, "RGB OpColor for arithmetic modes" },
/* 0x20 */  { "Line",              8, "pnLoc (point), newPt (point)" },
/* 0x21 */  { "LineFrom",          4, "newPt (point)" },
/* 0x22 */  { "ShortLine",         6, "pnLoc (point, dh, dv (-128 .. 127))" },
/* 0x23 */  { "ShortLineFrom",     2, "dh, dv (-128 .. 127)" },
/* 0x24 */  res(WORD_LEN),
/* 0x25 */  res(WORD_LEN),
/* 0x26 */  res(WORD_LEN),
/* 0x27 */  res(WORD_LEN),
/* 0x28 */  { "LongText",         NA, "txLoc (point), count (0..255), text" },
/* 0x29 */  { "DHText",           NA, "dh (0..255), count (0..255), text" },
/* 0x2a */  { "DVText",           NA, "dv (0..255), count (0..255), text" },
/* 0x2b */  { "DHDVText",         NA, "dh, dv (0..255), count (0..255), text" },
/* 0x2c */  res(WORD_LEN),
/* 0x2d */  res(WORD_LEN),
/* 0x2e */  res(WORD_LEN),
/* 0x2f */  res(WORD_LEN),
/* 0x30 */  { "frameRect",         8, "rect" },
/* 0x31 */  { "paintRect",         8, "rect" },
/* 0x32 */  { "eraseRect",         8, "rect" },
/* 0x33 */  { "invertRect",        8, "rect" },
/* 0x34 */  { "fillRect",          8, "rect" },
/* 0x35 */  res(8),
/* 0x36 */  res(8),
/* 0x37 */  res(8),
/* 0x38 */  { "frameSameRect",     0, "rect" },
/* 0x39 */  { "paintSameRect",     0, "rect" },
/* 0x3a */  { "eraseSameRect",     0, "rect" },
/* 0x3b */  { "invertSameRect",    0, "rect" },
/* 0x3c */  { "fillSameRect",      0, "rect" },
/* 0x3d */  res(0),
/* 0x3e */  res(0),
/* 0x3f */  res(0),
/* 0x40 */  { "frameRRect",        8, "rect" },
/* 0x41 */  { "paintRRect",        8, "rect" },
/* 0x42 */  { "eraseRRect",        8, "rect" },
/* 0x43 */  { "invertRRect",       8, "rect" },
/* 0x44 */  { "fillRRrect",        8, "rect" },
/* 0x45 */  res(8),
/* 0x46 */  res(8),
/* 0x47 */  res(8),
/* 0x48 */  { "frameSameRRect",    0, "rect" },
/* 0x49 */  { "paintSameRRect",    0, "rect" },
/* 0x4a */  { "eraseSameRRect",    0, "rect" },
/* 0x4b */  { "invertSameRRect",   0, "rect" },
/* 0x4c */  { "fillSameRRect",     0, "rect" },
/* 0x4d */  res(0),
/* 0x4e */  res(0),
/* 0x4f */  res(0),
/* 0x50 */  { "frameOval",         8, "rect" },
/* 0x51 */  { "paintOval",         8, "rect" },
/* 0x52 */  { "eraseOval",         8, "rect" },
/* 0x53 */  { "invertOval",        8, "rect" },
/* 0x54 */  { "fillOval",          8, "rect" },
/* 0x55 */  res(8),
/* 0x56 */  res(8),
/* 0x57 */  res(8),
/* 0x58 */  { "frameSameOval",     0, "rect" },
/* 0x59 */  { "paintSameOval",     0, "rect" },
/* 0x5a */  { "eraseSameOval",     0, "rect" },
/* 0x5b */  { "invertSameOval",    0, "rect" },
/* 0x5c */  { "fillSameOval",      0, "rect" },
/* 0x5d */  res(0),
/* 0x5e */  res(0),
/* 0x5f */  res(0),
/* 0x60 */  { "frameArc",         12, "rect, startAngle, arcAngle" },
/* 0x61 */  { "paintArc",         12, "rect, startAngle, arcAngle" },
/* 0x62 */  { "eraseArc",         12, "rect, startAngle, arcAngle" },
/* 0x63 */  { "invertArc",        12, "rect, startAngle, arcAngle" },
/* 0x64 */  { "fillArc",          12, "rect, startAngle, arcAngle" },
/* 0x65 */  res(12),
/* 0x66 */  res(12),
/* 0x67 */  res(12),
/* 0x68 */  { "frameSameArc",      4, "rect, startAngle, arcAngle" },
/* 0x69 */  { "paintSameArc",      4, "rect, startAngle, arcAngle" },
/* 0x6a */  { "eraseSameArc",      4, "rect, startAngle, arcAngle" },
/* 0x6b */  { "invertSameArc",     4, "rect, startAngle, arcAngle" },
/* 0x6c */  { "fillSameArc",       4, "rect, startAngle, arcAngle" },
/* 0x6d */  res(4),
/* 0x6e */  res(4),
/* 0x6f */  res(4),
/* 0x70 */  { "framePoly",        NA, "poly" },
/* 0x71 */  { "paintPoly",        NA, "poly" },
/* 0x72 */  { "erasePoly",        NA, "poly" },
/* 0x73 */  { "invertPoly",       NA, "poly" },
/* 0x74 */  { "fillPoly",         NA, "poly" },
/* 0x75 */  res(NA),
/* 0x76 */  res(NA),
/* 0x77 */  res(NA),
/* 0x78 */  { "frameSamePoly",     0, "poly (NYI)" },
/* 0x79 */  { "paintSamePoly",     0, "poly (NYI)" },
/* 0x7a */  { "eraseSamePoly",     0, "poly (NYI)" },
/* 0x7b */  { "invertSamePoly",    0, "poly (NYI)" },
/* 0x7c */  { "fillSamePoly",      0, "poly (NYI)" },
/* 0x7d */  res(0),
/* 0x7e */  res(0),
/* 0x7f */  res(0),
/* 0x80 */  { "frameRgn",         NA, "region" },
/* 0x81 */  { "paintRgn",         NA, "region" },
/* 0x82 */  { "eraseRgn",         NA, "region" },
/* 0x83 */  { "invertRgn",        NA, "region" },
/* 0x84 */  { "fillRgn",          NA, "region" },
/* 0x85 */  res(NA),
/* 0x86 */  res(NA),
/* 0x87 */  res(NA),
/* 0x88 */  { "frameSameRgn",      0, "region (NYI)" },
/* 0x89 */  { "paintSameRgn",      0, "region (NYI)" },
/* 0x8a */  { "eraseSameRgn",      0, "region (NYI)" },
/* 0x8b */  { "invertSameRgn",     0, "region (NYI)" },
/* 0x8c */  { "fillSameRgn",       0, "region (NYI)" },
/* 0x8d */  res(0),
/* 0x8e */  res(0),
/* 0x8f */  res(0),
/* 0x90 */  { "BitsRect",         NA, "copybits, rect clipped" },
/* 0x91 */  { "BitsRgn",          NA, "copybits, rgn clipped" },
/* 0x92 */  res(WORD_LEN),
/* 0x93 */  res(WORD_LEN),
/* 0x94 */  res(WORD_LEN),
/* 0x95 */  res(WORD_LEN),
/* 0x96 */  res(WORD_LEN),
/* 0x97 */  res(WORD_LEN),
/* 0x98 */  { "PackBitsRect",     NA, "packed copybits, rect clipped" },
/* 0x99 */  { "PackBitsRgn",      NA, "packed copybits, rgn clipped" },
/* 0x9a */  { "Opcode_9A",        NA, "the mysterious opcode 9A" },
/* 0x9b */  res(WORD_LEN),
/* 0x9c */  res(WORD_LEN),
/* 0x9d */  res(WORD_LEN),
/* 0x9e */  res(WORD_LEN),
/* 0x9f */  res(WORD_LEN),
/* 0xa0 */  { "ShortComment",      2, "kind (word)" },
/* 0xa1 */  { "LongComment",      NA, "kind (word), size (word), data" }
};

// ----------------------------------------------------------

struct MacRect
{
	WORD top;
	WORD left;
	WORD bottom;
	WORD right;
};

struct MacpixMap
{
	// Ptr baseAddr              // Not used in file.
	// short rowBytes            // read in seperatly.
	struct MacRect Bounds;
	WORD version;
	WORD packType;
	LONG packSize;
	LONG hRes;
	LONG vRes;
	WORD pixelType;
	WORD pixelSize;
	WORD cmpCount;
	WORD cmpSize;
	LONG planeBytes;
	LONG pmTable;
	LONG pmReserved;
};

struct MacRGBColour
{
	WORD red;
	WORD green;
	WORD blue;
};

struct MacPoint
{
	WORD x;
	WORD y;
};

struct MacPattern // Klaube
{
	BYTE pix[64];
};

// ----------------------------------------------------------

static void 
ReadRect( FreeImageIO *io, fi_handle handle, MacRect* rect ) {
	rect->top = Read16( io, handle );
	rect->left = Read16( io, handle );
	rect->bottom = Read16( io, handle );
	rect->right = Read16( io, handle );
}

static void 
ReadPixmap( FreeImageIO *io, fi_handle handle, MacpixMap* pPixMap ) {
	pPixMap->version = Read16( io, handle );
	pPixMap->packType = Read16( io, handle );
	pPixMap->packSize = Read32( io, handle );
	pPixMap->hRes = Read16( io, handle );
	Read16( io, handle );
	pPixMap->vRes = Read16( io, handle );
	Read16( io, handle );
	pPixMap->pixelType = Read16( io, handle );
	pPixMap->pixelSize = Read16( io, handle );
	pPixMap->cmpCount = Read16( io, handle );
	pPixMap->cmpSize = Read16( io, handle );
	pPixMap->planeBytes = Read32( io, handle );
	pPixMap->pmTable = Read32( io, handle );
	pPixMap->pmReserved = Read32( io, handle );
}

/**
Reads a mac color table into a bitmap palette.
*/
static void 
ReadColorTable( FreeImageIO *io, fi_handle handle, WORD* pNumColors, RGBQUAD* pPal ) {
	LONG        ctSeed;
	WORD        ctFlags;
	WORD        val;
	int         i;
	
	ctSeed = Read32( io, handle );
	ctFlags = Read16( io, handle );
	WORD numColors = Read16( io, handle )+1;
	*pNumColors = numColors;
	
	for (i = 0; i < numColors; i++) {
		val = Read16( io, handle );
		if (ctFlags & 0x8000) {
			// The indicies in a device colour table are bogus and
			// usually == 0, so I assume we allocate up the list of
			// colours in order.
			val = i;
		}
		if (val >= numColors) {
			throw "pixel value greater than color table size.";
		}
		// Mac colour tables contain 16-bit values for R, G, and B...
		pPal[val].rgbRed = ((BYTE) (((WORD) (Read16( io, handle )) >> 8) & 0xFF));
		pPal[val].rgbGreen = ((BYTE) (((WORD) (Read16( io, handle )) >> 8) & 0xFF));
		pPal[val].rgbBlue = ((BYTE) (((WORD) (Read16( io, handle )) >> 8) & 0xFF));
	}
}

/**
skips unneeded packbits.
pixelSize == Source bits per pixel.
*/
static void 
SkipBits( FreeImageIO *io, fi_handle handle, MacRect* bounds, WORD rowBytes, int pixelSize ) {
	int    i;
	WORD   pixwidth;           // bytes per row when uncompressed.
	
	int height = bounds->bottom - bounds->top;
	int width = bounds->right - bounds->left;
	
	// High bit of rowBytes is flag.
	if (pixelSize <= 8) {
		rowBytes &= 0x7fff;
	}
	pixwidth = width;
	
	if (pixelSize == 16) {
		pixwidth *= 2;
	}
	if (rowBytes == 0) {
		rowBytes = pixwidth;
	}
	if (rowBytes < 8) {
		io->seek_proc( handle, rowBytes*height, SEEK_CUR );
	}
	else {
		for (i = 0; i < height; i++) {
			int lineLen;            // length of source line in bytes.
			if (rowBytes > 250) {
				lineLen = Read16( io, handle );
			} else {
				lineLen = Read8( io, handle );
			}
			io->seek_proc( handle, lineLen, SEEK_CUR );
		}
	}
}

/**
Skip polygon or region
*/
static void 
SkipPolyOrRegion( FreeImageIO *io, fi_handle handle ) {
	WORD len = Read16( io, handle ) - 2;
	io->seek_proc(handle, len, SEEK_CUR);	
}

/**
Width in bytes for 8 bpp or less.
Width in pixels for 16 bpp.
Expands Width units to 32-bit pixel data.
*/
static void 
expandBuf( FreeImageIO *io, fi_handle handle, int width, int bpp, BYTE* dst ) { 
	switch (bpp) {
		case 16:
			for ( int i=0; i<width; i++) {
				WORD src = Read16( io, handle );
				dst[ FI_RGBA_BLUE ] = (src & 31)*8;				// Blue
				dst[ FI_RGBA_GREEN ] = ((src >> 5) & 31)*8;		// Green
				dst[ FI_RGBA_RED ] = ((src >> 10) & 31)*8;		// Red
				dst[ FI_RGBA_ALPHA ] = 0xFF;					// Alpha
				dst += 4;
			}
			break;
		default:
			throw "Bad bits per pixel in expandBuf.";
	}
}

/**
Expands Width units to 8-bit pixel data.
Max. 8 bpp source format.
*/
static void 
expandBuf8( FreeImageIO *io, fi_handle handle, int width, int bpp, BYTE* dst )
{
	switch (bpp) {
		case 8:
			io->read_proc( dst, width, 1, handle );
			break;
		case 4:
			for (int i = 0; i < width; i++) {
				WORD src = Read8( io, handle );
				*dst = (src >> 4) & 15;
				*(dst+1) = (src & 15);
				dst += 2;
			}
			if (width & 1) { // Odd Width?
				WORD src = Read8( io, handle );
				*dst = (src >> 4) & 15;
				dst++;
			}
			break;
		case 2:
			for (int i = 0; i < width; i++) {
				WORD src = Read8( io, handle );
				*dst = (src >> 6) & 3;
				*(dst+1) = (src >> 4) & 3;
				*(dst+2) = (src >> 2) & 3;
				*(dst+3) = (src & 3);
				dst += 4;
			}
			if (width & 3)  { // Check for leftover pixels
				for (int i = 6; i > 8 - (width & 3) * 2; i -= 2) {
					WORD src = Read8( io, handle );
					*dst = (src >> i) & 3;
					dst++;
				}
			}
			break;
		case 1:
			for (int i = 0; i < width; i++) {
				WORD src = Read8( io, handle );
				*dst = (src >> 7) & 1;
				*(dst+1) = (src >> 6) & 1;
				*(dst+2) = (src >> 5) & 1;
				*(dst+3) = (src >> 4) & 1;
				*(dst+4) = (src >> 3) & 1;
				*(dst+5) = (src >> 2) & 1;
				*(dst+6) = (src >> 1) & 1;
				*(dst+7) = (src  & 1);
				dst += 8;
			}
			if (width & 7) {  // Check for leftover pixels
				for (int i = 7; i > (8-width & 7); i--) {
					WORD src = Read8( io, handle );
					*dst = (src >> i) & 1;
					dst++;
				}
			}
			break;
		default:
			throw "Bad bits per pixel in expandBuf8.";
	}
}

static BYTE* 
UnpackPictRow( FreeImageIO *io, fi_handle handle, BYTE* pLineBuf, int width, int rowBytes, int srcBytes ) {	
	if (rowBytes < 8) { // Ah-ha!  The bits aren't actually packed.  This will be easy.
		io->read_proc( pLineBuf, rowBytes, 1, handle );
	}
	else {
		BYTE* pCurPixel = pLineBuf;
		
		// Unpack RLE. The data is packed bytewise.
		for (int j = 0; j < srcBytes; )	{
			BYTE FlagCounter = Read8( io, handle );
			if (FlagCounter & 0x80) {
				if (FlagCounter == 0x80) {
					// Special case: repeat value of 0.
					// Apple says ignore.
					j++;
				} else { 
					// Packed data.
					int len = ((FlagCounter ^ 255) & 255) + 2;					
					BYTE p = Read8( io, handle );
					memset( pCurPixel, p, len);
					pCurPixel += len;
					j += 2;
				}
			}
			else { 
				// Unpacked data
				int len = (FlagCounter & 255) + 1;
				io->read_proc( pCurPixel, len, 1, handle );
				pCurPixel += len;
				j += len + 1;
			}
		}
	}
	
	return pLineBuf;
}

/**
This routine decompresses BitsRects with a packType of 4 (and 32 bits per pixel). 
In this format, each line is separated into 8-bit-bitplanes and then compressed via RLE. 
To decode, the routine decompresses each line & then juggles the bytes around to get pixel-oriented data.
NumBitPlanes == 3 if RGB, 4 if RGBA
*/
static void 
Unpack32Bits( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, MacRect* bounds, WORD rowBytes, int numPlanes ) {
	int height = bounds->bottom - bounds->top;
	int width = bounds->right - bounds->left;
	
	if (rowBytes == 0) {
		rowBytes = width*4;
	}
	
	BYTE* pLineBuf = (BYTE*)malloc( rowBytes ); // Let's allocate enough for 4 bit planes
	if ( pLineBuf )	{
		try	{
			for ( int i = 0; i < height; i++ ) { 
				// for each line do...
				int linelen;            // length of source line in bytes.
				if (rowBytes > 250) {
					linelen = Read16( io, handle );
				} else {
					linelen = Read8( io, handle);
				}
				
				BYTE* pBuf = UnpackPictRow( io, handle, pLineBuf, width, rowBytes, linelen );
				
				// Convert plane-oriented data into pixel-oriented data &
				// copy into destination bitmap.
				BYTE* dst = (BYTE*)FreeImage_GetScanLine( dib, height - 1 - i);
				
				if ( numPlanes == 3 ) {
					for ( int j = 0; j < width; j++ ) { 
						// For each pixel in line...
						dst[ FI_RGBA_BLUE ] = (*(pBuf+width*2));     // Blue
						dst[ FI_RGBA_GREEN ] = (*(pBuf+width));       // Green
						dst[ FI_RGBA_RED ] = (*pBuf);             // Red
						dst[ FI_RGBA_ALPHA ] = (0xFF);
						dst += 4;
						pBuf++;
					}
				} else {
					for ( int j = 0; j < width; j++ ) { 
						// For each pixel in line...
						dst[ FI_RGBA_BLUE ] = (*(pBuf+width*3));     // Blue
						dst[ FI_RGBA_GREEN ] = (*(pBuf+width*2));     // Green
						dst[ FI_RGBA_RED ] = (*(pBuf+width));       // Red
						dst[ FI_RGBA_ALPHA ] = (*pBuf);
						dst += 4;
						pBuf++;
					}
				}
			}
		}
		catch( ... ) {
			free( pLineBuf );
			throw;
		}
	}
	free( pLineBuf );
}

/**
Decompression routine for 8 bpp. 
rowBytes is the number of bytes each source row would take if it were uncompressed.
This _isn't_ equal to the number of pixels in the row - it seems apple pads the data to a word boundary and then compresses it. 
Of course, we have to decompress the excess data and then throw it away.
*/
static void 
Unpack8Bits( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, MacRect* bounds, WORD rowBytes ) {	
	int height = bounds->bottom - bounds->top;
	int width = bounds->right - bounds->left;
	
	// High bit of rowBytes is flag.
	rowBytes &= 0x7fff;
	
	if (rowBytes == 0) {
		rowBytes = width;
	}
	
	for ( int i = 0; i < height; i++ ) {
		int linelen;            // length of source line in bytes.
		if (rowBytes > 250) {
			linelen = Read16( io, handle );
		} else {
			linelen = Read8( io, handle );
		}
		BYTE* dst = (BYTE*)FreeImage_GetScanLine( dib, height - 1 - i);				
		dst = UnpackPictRow( io, handle, dst, width, rowBytes, linelen );
	}
}

/**
Decompression routine for everything but 8 & 32 bpp. 
This routine is slower than the two routines above since it has to deal with a lot of special cases :-(.
It's also a bit chaotic because of these special cases...
unpack8bits is basically a dumber version of unpackbits.
pixelSize == Source bits per pixel.
*/
static void 
UnpackBits( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, MacRect* bounds, WORD rowBytes, int pixelSize ) {
	WORD   pixwidth;           // bytes per row when uncompressed.
	int    pkpixsize;
	int    PixelPerRLEUnit;

	char outputMessage[ outputMessageSize ] = "";
	
	int height = bounds->bottom - bounds->top;
	int width = bounds->right - bounds->left;
	
	// High bit of rowBytes is flag.
	if (pixelSize <= 8) {
		rowBytes &= 0x7fff;
	}
	
	pixwidth = width;
	pkpixsize = 1;          // RLE unit: one byte for everything...
	if (pixelSize == 16) {    // ...except 16 bpp.
		pkpixsize = 2;
		pixwidth *= 2;
	}
	
	if (rowBytes == 0) {
		rowBytes = pixwidth;
	}
	
	{
		// I allocate the temporary line buffer here. I allocate too
		// much memory to compensate for sloppy (& hence fast) decompression.
		switch (pixelSize) {
			case 1:
				PixelPerRLEUnit = 8;
				break;
			case 2:
				PixelPerRLEUnit = 4;
				break;
			case 4:
				PixelPerRLEUnit = 2;
				break;
			case 8:
				PixelPerRLEUnit = 1;
				break;
			case 16:
				PixelPerRLEUnit = 1;
				break;
			default:
				sprintf( outputMessage, "Illegal bpp value in unpackbits: %d\n", pixelSize );
				throw outputMessage;
		}
		
		if (rowBytes < 8) { 
			// ah-ha!  The bits aren't actually packed.  This will be easy.
			for ( int i = 0; i < height; i++ ) {
				BYTE* dst = (BYTE*)FreeImage_GetScanLine( dib, height - 1 - i);
				if (pixelSize == 16) {
					expandBuf( io, handle, width, pixelSize, dst );
				} else {
					expandBuf8( io, handle, width, pixelSize, dst );
				}
			}
		}
		else {
			for ( int i = 0; i < height; i++ ) { 
				// For each line do...
				int    linelen;            // length of source line in bytes.
				if (rowBytes > 250) {
					linelen = Read16( io, handle );
				} else {
					linelen = Read8( io, handle );
				}
				
				BYTE* dst = (BYTE*)FreeImage_GetScanLine( dib, height - 1 - i);
				BYTE FlagCounter;
				
				// Unpack RLE. The data is packed bytewise - except for
				// 16 bpp data, which is packed per pixel :-(.
				for ( int j = 0; j < linelen; ) {
					FlagCounter = Read8( io, handle );
					if (FlagCounter & 0x80) {
						if (FlagCounter == 0x80) {
							// Special case: repeat value of 0.
							// Apple says ignore.
							j++;
						}
						else { 
							// Packed data.
							int len = ((FlagCounter ^ 255) & 255) + 2;
							
							// This is slow for some formats...
							if (pixelSize == 16) {
								expandBuf( io, handle, 1, pixelSize, dst );
								for ( int k = 1; k < len; k++ ) { 
									// Repeat the pixel len times.
									memcpy( dst+(k*4*PixelPerRLEUnit), dst,	4*PixelPerRLEUnit);
								}
								dst += len*4*PixelPerRLEUnit;
							}
							else {
								expandBuf8( io, handle, 1, pixelSize, dst );
								for ( int k = 1; k < len; k++ ) { 
									// Repeat the expanded byte len times.
									memcpy( dst+(k*PixelPerRLEUnit), dst, PixelPerRLEUnit);
								}
								dst += len*PixelPerRLEUnit;
							}
							j += pkpixsize + 1;
						}
					}
					else { 
						// Unpacked data
						int len = (FlagCounter & 255) + 1;
						if (pixelSize == 16) {
							expandBuf( io, handle, len, pixelSize, dst );
							dst += len*4*PixelPerRLEUnit;
						}
						else {
							expandBuf8( io, handle, len, pixelSize, dst );
							dst += len*PixelPerRLEUnit;
						}
						j += ( len * pkpixsize ) + 1;
					}
				}
			}
		}
	}
}

static void 
DecodeOp9a( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, MacpixMap* pixMap ) {
	// Do the actual unpacking.
	switch ( pixMap->pixelSize ) {
		case 32:
			Unpack32Bits( io, handle, dib, &pixMap->Bounds, 0, pixMap->cmpCount );
			break;
		case 8:
			Unpack8Bits( io, handle, dib, &pixMap->Bounds, 0 );
			break;
		default:
			UnpackBits( io, handle, dib, &pixMap->Bounds, 0, pixMap->pixelSize );
	}
}

static void 
DecodeBitmap( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, BOOL isRegion, MacRect* bounds, WORD rowBytes ) {
	WORD mode = Read16( io, handle );
	
	if ( isRegion ) {
		SkipPolyOrRegion( io, handle );
	}
	
	RGBQUAD* pal = FreeImage_GetPalette( dib );
	if ( !pal ) {
		throw "No palette for bitmap!";
	}
	
	for (int i = 0; i < 2; i++) {
		unsigned char val = i ? 0xFF : 0x0;
		pal[i].rgbRed = val;
		pal[i].rgbGreen = val;
		pal[i].rgbBlue = val;
	}
	
	UnpackBits( io, handle, dib, bounds, rowBytes, 1 );
}

static void 
DecodePixmap( FreeImageIO *io, fi_handle handle, FIBITMAP* dib, BOOL isRegion, MacpixMap* pixMap, WORD rowBytes ) {
	// Read mac colour table into windows palette.
	WORD numColors;    // Palette size.
	RGBQUAD ct[256];
	
	ReadColorTable( io, handle, &numColors, ct );
	if ( FreeImage_GetBPP( dib ) == 8 ) {
		RGBQUAD* pal = FreeImage_GetPalette( dib );
		if ( !pal ) {
			throw "No palette for bitmap!";
		}
		
		for (int i = 0; i < numColors; i++) {
			pal[i].rgbRed = ct[ i ].rgbRed;
			pal[i].rgbGreen = ct[ i ].rgbGreen;
			pal[i].rgbBlue = ct[ i ].rgbBlue;
		}
	}
	
	// Ignore source & destination rectangle as well as transfer mode.
	MacRect tempRect;
	ReadRect( io, handle, &tempRect );
	ReadRect( io, handle, &tempRect );
	WORD mode = Read16( io, handle );
	
	if ( isRegion) {
		SkipPolyOrRegion( io, handle );
	}
	
	switch ( pixMap->pixelSize ) {
		case 32:
			Unpack32Bits( io, handle, dib, &pixMap->Bounds, rowBytes, pixMap->cmpCount );
			break;
		case 8:
			Unpack8Bits( io, handle, dib, &pixMap->Bounds, rowBytes );
			break;
		default:
			UnpackBits( io, handle, dib, &pixMap->Bounds, rowBytes, pixMap->pixelSize );
	}
}

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "PICT";
}

static const char * DLL_CALLCONV
Description() {
	return "Macintosh PICT";
}

static const char * DLL_CALLCONV
Extension() {
	return "pct,pict,pic";
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-pict";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	if(io->seek_proc(handle, 522, SEEK_SET) == 0) {
		BYTE pict_signature[] = { 0x00, 0x11, 0x02, 0xFF, 0x0C, 0X00 };
		BYTE signature[6];

		if(io->read_proc(signature, 1, sizeof(pict_signature), handle)) {
			// v1.0 files have 0x11 (version operator) followed by 0x01 (version number)
			// v2.0 files have 0x0011 (version operator) followed by 0x02ff (version number)
			//   and additionally 0x0c00 as a header opcode
			// Currently, we are only supporting v2.0
			return (memcmp(pict_signature, signature, sizeof(pict_signature)) == 0);
		} else {
			return FALSE;
		}
	}

	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return FALSE;
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsICCProfiles() {
	return FALSE;
}

/**
This plugin decodes macintosh PICT files with 1,2,4,8,16 and 32 bits per pixel as well as PICT/JPEG. 
If an alpha channel is present in a 32-bit-PICT, it is decoded as well. 
The PICT format is a general picture file format and can contain a lot of other elements besides bitmaps. 
These elements are ignored.	
*/
static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	char outputMessage[ outputMessageSize ] = "";
	FIBITMAP* dib = NULL;
	try {		
		// Skip empty 512 byte header.
		if ( !io->seek_proc(handle, 512, SEEK_CUR) == 0 )
			return NULL;
		
		// Read PICT header
		Read16( io, handle ); // Skip version 1 picture size
		
		MacRect frame;
		ReadRect( io, handle, &frame );

		BYTE b = 0;
		while ((b = Read8(io, handle)) == 0);
		if ( b != 0x11 ) {
			throw "invalid header: version number missing.";
		}
		
		int version = Read8( io, handle );
		if ( version == 2 && Read8( io, handle ) != 0xff ) {
			throw "invalid header: illegal version number.";
		}
		
		enum PICTType {none, op9a, jpeg, pixmap, bitmap};
		PICTType pictType = none;
		
		MacRect bounds;
		MacpixMap pixMap;
		int hRes = 0x480000; // in pixels/inch (72 by default == 0x480000 in fixed point)
		int vRes = 0x480000; // in pixels/inch (72 by default == 0x480000 in fixed point)
		WORD rowBytes = 0;
		BOOL isRegion = FALSE;
		BOOL done = FALSE;
		long currentPos = 0;

		while ( !done ) {
			WORD opcode = 0;

			// get the current stream position (used to avoid infinite loops)
			currentPos = io->tell_proc(handle);
			
			if ((version == 1) || ((io->tell_proc( handle ) % 2) != 0)) {
				// align to word for version 2
				opcode = Read8( io, handle );
			}
			if (version == 2) {
				opcode = Read16( io, handle );
			}
			
			if (opcode == 0xFF || opcode == 0xFFFF) {
				done = TRUE;
				throw "PICT contained only vector data!";
			}
			else if (opcode < 0xa2)	{				
				switch (opcode)	{
					case 0x01:
					{
						// skip clipping rectangle
						MacRect clipRect;
						WORD len = Read16( io, handle );

						if (len == 0x000a) { 
							/* null rgn */
							ReadRect( io, handle, &clipRect );
						} else {
							io->seek_proc(handle, len - 2, SEEK_CUR);
						}
						break;
					}						
					case 0x12:
					case 0x13:
					case 0x14:
					{
						// skip pattern definition
						WORD       patType;
						WORD       rowBytes;
						MacpixMap  p;
						WORD       numColors;
						
						patType = Read16( io, handle );
						
						switch( patType ) {
							case 2:
								io->seek_proc(handle, 8, SEEK_CUR);
								io->seek_proc(handle, 5, SEEK_CUR);
								break;
							case 1:
							{
								io->seek_proc(handle, 8, SEEK_CUR);
								rowBytes = Read16( io, handle );
								ReadRect( io, handle, &p.Bounds );
								ReadPixmap( io, handle, &p);
								
								RGBQUAD ct[256];
								ReadColorTable(io, handle, &numColors, ct );
								SkipBits( io, handle, &p.Bounds, rowBytes, p.pixelSize );
								break;
							}
							default:
								throw "Unknown pattern type.";
						}
						
						break;
					}
					case 0x70:
					case 0x71:
					case 0x72:
					case 0x73:
					case 0x74:
					case 0x75:
					case 0x76:
					case 0x77:
					{
						SkipPolyOrRegion( io, handle );
						break;
					}
					case 0x90:
					case 0x98:
					{
						// Bitmap/pixmap data clipped by a rectangle.
						rowBytes = Read16( io, handle );    // Bytes per row in source when uncompressed.						
						isRegion = FALSE;
						
						if ( rowBytes & 0x8000) {
							pictType = pixmap;
						} else {
							pictType = bitmap;
						}
						done = TRUE;
						break;
					}
					case 0x91:
					case 0x99:
					{
						// Bitmap/pixmap data clipped by a region.
						rowBytes = Read16( io, handle );    // Bytes per row in source when uncompressed.						
						isRegion = TRUE;
						
						if ( rowBytes & 0x8000) {
							pictType = pixmap;
						} else {
							pictType = bitmap;						
						}
						done = TRUE;
						break;
					}
					case 0x9a:
					{
						// DirectBitsRect.
						Read32( io, handle );           // Skip fake len and fake EOF.
						Read16( io, handle );			// bogus row bytes.
						
						// Read in the PixMap fields.
						ReadRect( io, handle, &pixMap.Bounds );
						ReadPixmap( io, handle, &pixMap );
						
						// Ignore source & destination rectangle as well as transfer mode.
						MacRect dummy;
						ReadRect( io, handle, &dummy );
						ReadRect( io, handle, &dummy );
						WORD mode = Read16( io, handle );
						
						pictType=op9a;
						done = TRUE;
						break;
					}
					case 0xa1:
					{
						// long comment
						WORD type;
						WORD len;
						
						type = Read16( io, handle );
						len = Read16( io, handle);
						if (len > 0) {
							io->seek_proc(handle, len, SEEK_CUR);
						}
						break;
					}
					default:
						// No function => skip to next opcode
						if (optable[opcode].len == WORD_LEN) {
							WORD len = Read16( io, handle );
							io->seek_proc(handle, len, SEEK_CUR);
						} else {
							io->seek_proc(handle, optable[opcode].len, SEEK_CUR);
						}
						break;
				}
			}
			else if (opcode == 0xc00) {
				// version 2 header (26 bytes)
				WORD minorVersion = Read16( io, handle );	// always FFFE (-2) for extended version 2
				Read16( io, handle );						// reserved
				hRes = Read32( io, handle );				// original horizontal resolution in pixels/inch
				vRes = Read32( io, handle );				// original horizontal resolution in pixels/inch
				MacRect dummy;
				ReadRect( io, handle, &dummy );				// frame bounds at original resolution
				Read32( io, handle );						// reserved
			}
			else if (opcode == 0x8200) {
				// jpeg
				long opLen = Read32( io, handle );
				BOOL found = FALSE;
				int i = 0;
				
				// skip to JPEG header.
				while ( !found && i < opLen ) {
//					io->seek_proc( handle, 24, SEEK_CUR );
//					MacRect dummy;
//					ReadRect( io, handle, &dummy );
//					io->seek_proc( handle, 122, SEEK_CUR );
//					found = TRUE;
					BYTE data[ 2 ];
					if( io->read_proc( data, 2, 1, handle ) ) {
						io->seek_proc( handle, -2, SEEK_CUR );
						
						if ( data[0] == 0xFF && data[1] == 0xD8 ) {
							found = TRUE;
						} else {
							Read8( io, handle );
							i++;
						}
					}
				}
				
				if ( found ) {
					// Pass the data to the JPEG decoder.
					pictType = jpeg;
				} else {
					throw "PICT file contains unrecognized quicktime data.";
				}
				done = TRUE;
			}
			else if (opcode >= 0xa2 && opcode <= 0xaf) {
				// reserved
				WORD len = Read16( io, handle );
				io->seek_proc(handle, len, SEEK_CUR);
			}
			else if ((opcode >= 0xb0 && opcode <= 0xcf) || (opcode >= 0x8000 && opcode <= 0x80ff)) {
				// just a reserved opcode, no data
			}
			else if ((opcode >= 0xd0 && opcode <= 0xfe) || opcode >= 8100) {
				// reserved
				LONG len = Read32( io, handle );
				io->seek_proc(handle, len, SEEK_CUR);
			}
			else if (opcode >= 0x100 && opcode <= 0x7fff) {
				// reserved
				io->seek_proc(handle, ((opcode >> 7) & 255), SEEK_CUR);				
			}
			else {
				sprintf( outputMessage, "Can't handle opcode %x.\n", opcode );
				throw outputMessage;
			}

			if(currentPos == io->tell_proc(handle)) {
				// we probaly reached the end of file as we can no longer move forward ... 
				throw "Invalid PICT file";
			}
		}
				
		switch ( pictType )	{
			case op9a:
			{
				bounds = pixMap.Bounds;
				int width = bounds.right - bounds.left;
				int height = bounds.bottom - bounds.top;
				
				if ( pixMap.pixelSize > 8 ) {
					dib = FreeImage_Allocate( width, height, 32, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
				} else {
					dib = FreeImage_Allocate( width, height, 8);
				}
				hRes = pixMap.hRes << 16;
				vRes = pixMap.vRes << 16;				
				break;
			}	
				
			case jpeg:
			{
				dib = FreeImage_LoadFromHandle( FIF_JPEG, io, handle );					
				break;
			}

			case pixmap:
			{
				// Decode version 2 pixmap
				ReadRect( io, handle, &pixMap.Bounds );
				ReadPixmap( io, handle, &pixMap );
				
				bounds = pixMap.Bounds;
				int width = bounds.right - bounds.left;
				int height = bounds.bottom - bounds.top;

				if ( pixMap.pixelSize > 8 ) {
					dib = FreeImage_Allocate( width, height, 32, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
				} else {
					dib = FreeImage_Allocate( width, height, 8);
				}
				hRes = pixMap.hRes << 16;
				vRes = pixMap.vRes << 16;				
				break;
			}
				
			case bitmap:
			{
				// Decode version 1 bitmap: 1 bpp.
				MacRect srcRect;
				MacRect dstRect;
				WORD width;        // Width in pixels
				WORD height;       // Height in pixels
				
				ReadRect( io, handle, &bounds );
				ReadRect( io, handle, &srcRect );
				ReadRect( io, handle, &dstRect );
				
				width = bounds.right - bounds.left;
				height = bounds.bottom - bounds.top;
				
				dib = FreeImage_Allocate(width, height, 8);
				break;
			}			
		}		
		
		if ( dib ) {
			// need to convert resolution figures from fixed point, pixels/inch
			// to floating point, pixels/meter.			
			float hres_ppm = hRes * ((float)39.4 / (float)65536.0);
			float vres_ppm = vRes * ((float)39.4 / (float)65536.0);		
			
			FreeImage_SetDotsPerMeterX( dib, (LONG)hres_ppm );
			FreeImage_SetDotsPerMeterY( dib, (LONG)vres_ppm );			
			
			switch( pictType ) {
				case op9a:
					DecodeOp9a( io, handle, dib, &pixMap );
					break;
				case jpeg:
					// Already decoded if the embedded format was valid.
					break;
				case pixmap:
					DecodePixmap( io, handle, dib, isRegion, &pixMap, rowBytes );
					break;
				case bitmap:
					DecodeBitmap( io, handle, dib, isRegion, &bounds, rowBytes );
					break;
				default:
					throw "invalid pict type";
			}			
		}
		
		return dib;
	} 
	catch(const char *message) {
		FreeImage_Unload( dib );
		FreeImage_OutputMessageProc(s_format_id, message);
	}

	return NULL;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitPICT(Plugin *plugin, int format_id) {
	s_format_id = format_id;

	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = NULL;
	plugin->open_proc = NULL;
	plugin->close_proc = NULL;
	plugin->pagecount_proc = NULL;
	plugin->pagecapability_proc = NULL;
	plugin->load_proc = Load;
	plugin->save_proc = NULL;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = SupportsICCProfiles;
}

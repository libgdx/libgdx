/* $Id: tif_luv.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * Copyright (c) 1997 Greg Ward Larson
 * Copyright (c) 1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler, Greg Larson and Silicon Graphics may not be used in any
 * advertising or publicity relating to the software without the specific,
 * prior written permission of Sam Leffler, Greg Larson and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER, GREG LARSON OR SILICON GRAPHICS BE LIABLE
 * FOR ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */

#include "tiffiop.h"
#ifdef LOGLUV_SUPPORT

/*
 * TIFF Library.
 * LogLuv compression support for high dynamic range images.
 *
 * Contributed by Greg Larson.
 *
 * LogLuv image support uses the TIFF library to store 16 or 10-bit
 * log luminance values with 8 bits each of u and v or a 14-bit index.
 *
 * The codec can take as input and produce as output 32-bit IEEE float values 
 * as well as 16-bit integer values.  A 16-bit luminance is interpreted
 * as a sign bit followed by a 15-bit integer that is converted
 * to and from a linear magnitude using the transformation:
 *
 *	L = 2^( (Le+.5)/256 - 64 )		# real from 15-bit
 *
 *	Le = floor( 256*(log2(L) + 64) )	# 15-bit from real
 *
 * The actual conversion to world luminance units in candelas per sq. meter
 * requires an additional multiplier, which is stored in the TIFFTAG_STONITS.
 * This value is usually set such that a reasonable exposure comes from
 * clamping decoded luminances above 1 to 1 in the displayed image.
 *
 * The 16-bit values for u and v may be converted to real values by dividing
 * each by 32768.  (This allows for negative values, which aren't useful as
 * far as we know, but are left in case of future improvements in human
 * color vision.)
 *
 * Conversion from (u,v), which is actually the CIE (u',v') system for
 * you color scientists, is accomplished by the following transformation:
 *
 *	u = 4*x / (-2*x + 12*y + 3)
 *	v = 9*y / (-2*x + 12*y + 3)
 *
 *	x = 9*u / (6*u - 16*v + 12)
 *	y = 4*v / (6*u - 16*v + 12)
 *
 * This process is greatly simplified by passing 32-bit IEEE floats
 * for each of three CIE XYZ coordinates.  The codec then takes care
 * of conversion to and from LogLuv, though the application is still
 * responsible for interpreting the TIFFTAG_STONITS calibration factor.
 *
 * By definition, a CIE XYZ vector of [1 1 1] corresponds to a neutral white
 * point of (x,y)=(1/3,1/3).  However, most color systems assume some other
 * white point, such as D65, and an absolute color conversion to XYZ then
 * to another color space with a different white point may introduce an
 * unwanted color cast to the image.  It is often desirable, therefore, to
 * perform a white point conversion that maps the input white to [1 1 1]
 * in XYZ, then record the original white point using the TIFFTAG_WHITEPOINT
 * tag value.  A decoder that demands absolute color calibration may use
 * this white point tag to get back the original colors, but usually it
 * will be ignored and the new white point will be used instead that
 * matches the output color space.
 *
 * Pixel information is compressed into one of two basic encodings, depending
 * on the setting of the compression tag, which is one of COMPRESSION_SGILOG
 * or COMPRESSION_SGILOG24.  For COMPRESSION_SGILOG, greyscale data is
 * stored as:
 *
 *	 1       15
 *	|-+---------------|
 *
 * COMPRESSION_SGILOG color data is stored as:
 *
 *	 1       15           8        8
 *	|-+---------------|--------+--------|
 *	 S       Le           ue       ve
 *
 * For the 24-bit COMPRESSION_SGILOG24 color format, the data is stored as:
 *
 *	     10           14
 *	|----------|--------------|
 *	     Le'          Ce
 *
 * There is no sign bit in the 24-bit case, and the (u,v) chromaticity is
 * encoded as an index for optimal color resolution.  The 10 log bits are
 * defined by the following conversions:
 *
 *	L = 2^((Le'+.5)/64 - 12)		# real from 10-bit
 *
 *	Le' = floor( 64*(log2(L) + 12) )	# 10-bit from real
 *
 * The 10 bits of the smaller format may be converted into the 15 bits of
 * the larger format by multiplying by 4 and adding 13314.  Obviously,
 * a smaller range of magnitudes is covered (about 5 orders of magnitude
 * instead of 38), and the lack of a sign bit means that negative luminances
 * are not allowed.  (Well, they aren't allowed in the real world, either,
 * but they are useful for certain types of image processing.)
 *
 * The desired user format is controlled by the setting the internal
 * pseudo tag TIFFTAG_SGILOGDATAFMT to one of:
 *  SGILOGDATAFMT_FLOAT       = IEEE 32-bit float XYZ values
 *  SGILOGDATAFMT_16BIT	      = 16-bit integer encodings of logL, u and v
 * Raw data i/o is also possible using:
 *  SGILOGDATAFMT_RAW         = 32-bit unsigned integer with encoded pixel
 * In addition, the following decoding is provided for ease of display:
 *  SGILOGDATAFMT_8BIT        = 8-bit default RGB gamma-corrected values
 *
 * For grayscale images, we provide the following data formats:
 *  SGILOGDATAFMT_FLOAT       = IEEE 32-bit float Y values
 *  SGILOGDATAFMT_16BIT       = 16-bit integer w/ encoded luminance
 *  SGILOGDATAFMT_8BIT        = 8-bit gray monitor values
 *
 * Note that the COMPRESSION_SGILOG applies a simple run-length encoding
 * scheme by separating the logL, u and v bytes for each row and applying
 * a PackBits type of compression.  Since the 24-bit encoding is not
 * adaptive, the 32-bit color format takes less space in many cases.
 *
 * Further control is provided over the conversion from higher-resolution
 * formats to final encoded values through the pseudo tag
 * TIFFTAG_SGILOGENCODE:
 *  SGILOGENCODE_NODITHER     = do not dither encoded values
 *  SGILOGENCODE_RANDITHER    = apply random dithering during encoding
 *
 * The default value of this tag is SGILOGENCODE_NODITHER for
 * COMPRESSION_SGILOG to maximize run-length encoding and
 * SGILOGENCODE_RANDITHER for COMPRESSION_SGILOG24 to turn
 * quantization errors into noise.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

/*
 * State block for each open TIFF
 * file using LogLuv compression/decompression.
 */
typedef struct logLuvState LogLuvState;

struct logLuvState {
	int                     user_datafmt;   /* user data format */
	int                     encode_meth;    /* encoding method */
	int                     pixel_size;     /* bytes per pixel */

	uint8*                  tbuf;           /* translation buffer */
	tmsize_t                tbuflen;        /* buffer length */
	void (*tfunc)(LogLuvState*, uint8*, tmsize_t);

	TIFFVSetMethod          vgetparent;     /* super-class method */
	TIFFVSetMethod          vsetparent;     /* super-class method */
};

#define DecoderState(tif)	((LogLuvState*) (tif)->tif_data)
#define EncoderState(tif)	((LogLuvState*) (tif)->tif_data)

#define SGILOGDATAFMT_UNKNOWN -1

#define MINRUN 4 /* minimum run length */

/*
 * Decode a string of 16-bit gray pixels.
 */
static int
LogL16Decode(TIFF* tif, uint8* op, tmsize_t occ, uint16 s)
{
	static const char module[] = "LogL16Decode";
	LogLuvState* sp = DecoderState(tif);
	int shft;
	tmsize_t i;
	tmsize_t npixels;
	unsigned char* bp;
	int16* tp;
	int16 b;
	tmsize_t cc;
	int rc;

	assert(s == 0);
	assert(sp != NULL);

	npixels = occ / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_16BIT)
		tp = (int16*) op;
	else {
		assert(sp->tbuflen >= npixels);
		tp = (int16*) sp->tbuf;
	}
	_TIFFmemset((void*) tp, 0, npixels*sizeof (tp[0]));

	bp = (unsigned char*) tif->tif_rawcp;
	cc = tif->tif_rawcc;
	/* get each byte string */
	for (shft = 2*8; (shft -= 8) >= 0; ) {
		for (i = 0; i < npixels && cc > 0; )
			if (*bp >= 128) {		/* run */
				rc = *bp++ + (2-128);   /* TODO: potential input buffer overrun when decoding corrupt or truncated data */
				b = (int16)(*bp++ << shft);
				cc -= 2;
				while (rc-- && i < npixels)
					tp[i++] |= b;
			} else {			/* non-run */
				rc = *bp++;		/* nul is noop */
				while (--cc && rc-- && i < npixels)
					tp[i++] |= (int16)*bp++ << shft;
			}
		if (i != npixels) {
#if defined(__WIN32__) && (defined(_MSC_VER) || defined(__MINGW32__))
			TIFFErrorExt(tif->tif_clientdata, module,
			    "Not enough data at row %lu (short %I64d pixels)",
				     (unsigned long) tif->tif_row,
				     (unsigned __int64) (npixels - i));
#else
			TIFFErrorExt(tif->tif_clientdata, module,
			    "Not enough data at row %lu (short %llu pixels)",
				     (unsigned long) tif->tif_row,
				     (unsigned long long) (npixels - i));
#endif
			tif->tif_rawcp = (uint8*) bp;
			tif->tif_rawcc = cc;
			return (0);
		}
	}
	(*sp->tfunc)(sp, op, npixels);
	tif->tif_rawcp = (uint8*) bp;
	tif->tif_rawcc = cc;
	return (1);
}

/*
 * Decode a string of 24-bit pixels.
 */
static int
LogLuvDecode24(TIFF* tif, uint8* op, tmsize_t occ, uint16 s)
{
	static const char module[] = "LogLuvDecode24";
	LogLuvState* sp = DecoderState(tif);
	tmsize_t cc;
	tmsize_t i;
	tmsize_t npixels;
	unsigned char* bp;
	uint32* tp;

	assert(s == 0);
	assert(sp != NULL);

	npixels = occ / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_RAW)
		tp = (uint32 *)op;
	else {
		assert(sp->tbuflen >= npixels);
		tp = (uint32 *) sp->tbuf;
	}
	/* copy to array of uint32 */
	bp = (unsigned char*) tif->tif_rawcp;
	cc = tif->tif_rawcc;
	for (i = 0; i < npixels && cc > 0; i++) {
		tp[i] = bp[0] << 16 | bp[1] << 8 | bp[2];
		bp += 3;
		cc -= 3;
	}
	tif->tif_rawcp = (uint8*) bp;
	tif->tif_rawcc = cc;
	if (i != npixels) {
#if defined(__WIN32__) && (defined(_MSC_VER) || defined(__MINGW32__))
		TIFFErrorExt(tif->tif_clientdata, module,
			"Not enough data at row %lu (short %I64d pixels)",
			     (unsigned long) tif->tif_row,
			     (unsigned __int64) (npixels - i));
#else
		TIFFErrorExt(tif->tif_clientdata, module,
			"Not enough data at row %lu (short %llu pixels)",
			     (unsigned long) tif->tif_row,
			     (unsigned long long) (npixels - i));
#endif
		return (0);
	}
	(*sp->tfunc)(sp, op, npixels);
	return (1);
}

/*
 * Decode a string of 32-bit pixels.
 */
static int
LogLuvDecode32(TIFF* tif, uint8* op, tmsize_t occ, uint16 s)
{
	static const char module[] = "LogLuvDecode32";
	LogLuvState* sp;
	int shft;
	tmsize_t i;
	tmsize_t npixels;
	unsigned char* bp;
	uint32* tp;
	uint32 b;
	tmsize_t cc;
	int rc;

	assert(s == 0);
	sp = DecoderState(tif);
	assert(sp != NULL);

	npixels = occ / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_RAW)
		tp = (uint32*) op;
	else {
		assert(sp->tbuflen >= npixels);
		tp = (uint32*) sp->tbuf;
	}
	_TIFFmemset((void*) tp, 0, npixels*sizeof (tp[0]));

	bp = (unsigned char*) tif->tif_rawcp;
	cc = tif->tif_rawcc;
	/* get each byte string */
	for (shft = 4*8; (shft -= 8) >= 0; ) {
		for (i = 0; i < npixels && cc > 0; )
			if (*bp >= 128) {		/* run */
				rc = *bp++ + (2-128);
				b = (uint32)*bp++ << shft;
				cc -= 2;                /* TODO: potential input buffer overrun when decoding corrupt or truncated data */
				while (rc-- && i < npixels)
					tp[i++] |= b;
			} else {			/* non-run */
				rc = *bp++;		/* nul is noop */
				while (--cc && rc-- && i < npixels)
					tp[i++] |= (uint32)*bp++ << shft;
			}
		if (i != npixels) {
#if defined(__WIN32__) && (defined(_MSC_VER) || defined(__MINGW32__))
			TIFFErrorExt(tif->tif_clientdata, module,
			"Not enough data at row %lu (short %I64d pixels)",
				     (unsigned long) tif->tif_row,
				     (unsigned __int64) (npixels - i));
#else
			TIFFErrorExt(tif->tif_clientdata, module,
			"Not enough data at row %lu (short %llu pixels)",
				     (unsigned long) tif->tif_row,
				     (unsigned long long) (npixels - i));
#endif
			tif->tif_rawcp = (uint8*) bp;
			tif->tif_rawcc = cc;
			return (0);
		}
	}
	(*sp->tfunc)(sp, op, npixels);
	tif->tif_rawcp = (uint8*) bp;
	tif->tif_rawcc = cc;
	return (1);
}

/*
 * Decode a strip of pixels.  We break it into rows to
 * maintain synchrony with the encode algorithm, which
 * is row by row.
 */
static int
LogLuvDecodeStrip(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	tmsize_t rowlen = TIFFScanlineSize(tif);

	assert(cc%rowlen == 0);
	while (cc && (*tif->tif_decoderow)(tif, bp, rowlen, s))
		bp += rowlen, cc -= rowlen;
	return (cc == 0);
}

/*
 * Decode a tile of pixels.  We break it into rows to
 * maintain synchrony with the encode algorithm, which
 * is row by row.
 */
static int
LogLuvDecodeTile(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	tmsize_t rowlen = TIFFTileRowSize(tif);

	assert(cc%rowlen == 0);
	while (cc && (*tif->tif_decoderow)(tif, bp, rowlen, s))
		bp += rowlen, cc -= rowlen;
	return (cc == 0);
}

/*
 * Encode a row of 16-bit pixels.
 */
static int
LogL16Encode(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	LogLuvState* sp = EncoderState(tif);
	int shft;
	tmsize_t i;
	tmsize_t j;
	tmsize_t npixels;
	uint8* op;
	int16* tp;
	int16 b;
	tmsize_t occ;
	int rc=0, mask;
	tmsize_t beg;

	assert(s == 0);
	assert(sp != NULL);
	npixels = cc / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_16BIT)
		tp = (int16*) bp;
	else {
		tp = (int16*) sp->tbuf;
		assert(sp->tbuflen >= npixels);
		(*sp->tfunc)(sp, bp, npixels);
	}
	/* compress each byte string */
	op = tif->tif_rawcp;
	occ = tif->tif_rawdatasize - tif->tif_rawcc;
	for (shft = 2*8; (shft -= 8) >= 0; )
		for (i = 0; i < npixels; i += rc) {
			if (occ < 4) {
				tif->tif_rawcp = op;
				tif->tif_rawcc = tif->tif_rawdatasize - occ;
				if (!TIFFFlushData1(tif))
					return (-1);
				op = tif->tif_rawcp;
				occ = tif->tif_rawdatasize - tif->tif_rawcc;
			}
			mask = 0xff << shft;		/* find next run */
			for (beg = i; beg < npixels; beg += rc) {
				b = (int16) (tp[beg] & mask);
				rc = 1;
				while (rc < 127+2 && beg+rc < npixels &&
				    (tp[beg+rc] & mask) == b)
					rc++;
				if (rc >= MINRUN)
					break;		/* long enough */
			}
			if (beg-i > 1 && beg-i < MINRUN) {
				b = (int16) (tp[i] & mask);/*check short run */
				j = i+1;
				while ((tp[j++] & mask) == b)
					if (j == beg) {
						*op++ = (uint8)(128-2+j-i);
						*op++ = (uint8)(b >> shft);
						occ -= 2;
						i = beg;
						break;
					}
			}
			while (i < beg) {		/* write out non-run */
				if ((j = beg-i) > 127) j = 127;
				if (occ < j+3) {
					tif->tif_rawcp = op;
					tif->tif_rawcc = tif->tif_rawdatasize - occ;
					if (!TIFFFlushData1(tif))
						return (-1);
					op = tif->tif_rawcp;
					occ = tif->tif_rawdatasize - tif->tif_rawcc;
				}
				*op++ = (uint8) j; occ--;
				while (j--) {
					*op++ = (uint8) (tp[i++] >> shft & 0xff);
					occ--;
				}
			}
			if (rc >= MINRUN) {		/* write out run */
				*op++ = (uint8) (128-2+rc);
				*op++ = (uint8) (tp[beg] >> shft & 0xff);
				occ -= 2;
			} else
				rc = 0;
		}
	tif->tif_rawcp = op;
	tif->tif_rawcc = tif->tif_rawdatasize - occ;

	return (1);
}

/*
 * Encode a row of 24-bit pixels.
 */
static int
LogLuvEncode24(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	LogLuvState* sp = EncoderState(tif);
	tmsize_t i;
	tmsize_t npixels;
	tmsize_t occ;
	uint8* op;
	uint32* tp;

	assert(s == 0);
	assert(sp != NULL);
	npixels = cc / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_RAW)
		tp = (uint32*) bp;
	else {
		tp = (uint32*) sp->tbuf;
		assert(sp->tbuflen >= npixels);
		(*sp->tfunc)(sp, bp, npixels);
	}
	/* write out encoded pixels */
	op = tif->tif_rawcp;
	occ = tif->tif_rawdatasize - tif->tif_rawcc;
	for (i = npixels; i--; ) {
		if (occ < 3) {
			tif->tif_rawcp = op;
			tif->tif_rawcc = tif->tif_rawdatasize - occ;
			if (!TIFFFlushData1(tif))
				return (-1);
			op = tif->tif_rawcp;
			occ = tif->tif_rawdatasize - tif->tif_rawcc;
		}
		*op++ = (uint8)(*tp >> 16);
		*op++ = (uint8)(*tp >> 8 & 0xff);
		*op++ = (uint8)(*tp++ & 0xff);
		occ -= 3;
	}
	tif->tif_rawcp = op;
	tif->tif_rawcc = tif->tif_rawdatasize - occ;

	return (1);
}

/*
 * Encode a row of 32-bit pixels.
 */
static int
LogLuvEncode32(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	LogLuvState* sp = EncoderState(tif);
	int shft;
	tmsize_t i;
	tmsize_t j;
	tmsize_t npixels;
	uint8* op;
	uint32* tp;
	uint32 b;
	tmsize_t occ;
	int rc=0, mask;
	tmsize_t beg;

	assert(s == 0);
	assert(sp != NULL);

	npixels = cc / sp->pixel_size;

	if (sp->user_datafmt == SGILOGDATAFMT_RAW)
		tp = (uint32*) bp;
	else {
		tp = (uint32*) sp->tbuf;
		assert(sp->tbuflen >= npixels);
		(*sp->tfunc)(sp, bp, npixels);
	}
	/* compress each byte string */
	op = tif->tif_rawcp;
	occ = tif->tif_rawdatasize - tif->tif_rawcc;
	for (shft = 4*8; (shft -= 8) >= 0; )
		for (i = 0; i < npixels; i += rc) {
			if (occ < 4) {
				tif->tif_rawcp = op;
				tif->tif_rawcc = tif->tif_rawdatasize - occ;
				if (!TIFFFlushData1(tif))
					return (-1);
				op = tif->tif_rawcp;
				occ = tif->tif_rawdatasize - tif->tif_rawcc;
			}
			mask = 0xff << shft;		/* find next run */
			for (beg = i; beg < npixels; beg += rc) {
				b = tp[beg] & mask;
				rc = 1;
				while (rc < 127+2 && beg+rc < npixels &&
						(tp[beg+rc] & mask) == b)
					rc++;
				if (rc >= MINRUN)
					break;		/* long enough */
			}
			if (beg-i > 1 && beg-i < MINRUN) {
				b = tp[i] & mask;	/* check short run */
				j = i+1;
				while ((tp[j++] & mask) == b)
					if (j == beg) {
						*op++ = (uint8)(128-2+j-i);
						*op++ = (uint8)(b >> shft);
						occ -= 2;
						i = beg;
						break;
					}
			}
			while (i < beg) {		/* write out non-run */
				if ((j = beg-i) > 127) j = 127;
				if (occ < j+3) {
					tif->tif_rawcp = op;
					tif->tif_rawcc = tif->tif_rawdatasize - occ;
					if (!TIFFFlushData1(tif))
						return (-1);
					op = tif->tif_rawcp;
					occ = tif->tif_rawdatasize - tif->tif_rawcc;
				}
				*op++ = (uint8) j; occ--;
				while (j--) {
					*op++ = (uint8)(tp[i++] >> shft & 0xff);
					occ--;
				}
			}
			if (rc >= MINRUN) {		/* write out run */
				*op++ = (uint8) (128-2+rc);
				*op++ = (uint8)(tp[beg] >> shft & 0xff);
				occ -= 2;
			} else
				rc = 0;
		}
	tif->tif_rawcp = op;
	tif->tif_rawcc = tif->tif_rawdatasize - occ;

	return (1);
}

/*
 * Encode a strip of pixels.  We break it into rows to
 * avoid encoding runs across row boundaries.
 */
static int
LogLuvEncodeStrip(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	tmsize_t rowlen = TIFFScanlineSize(tif);

	assert(cc%rowlen == 0);
	while (cc && (*tif->tif_encoderow)(tif, bp, rowlen, s) == 1)
		bp += rowlen, cc -= rowlen;
	return (cc == 0);
}

/*
 * Encode a tile of pixels.  We break it into rows to
 * avoid encoding runs across row boundaries.
 */
static int
LogLuvEncodeTile(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	tmsize_t rowlen = TIFFTileRowSize(tif);

	assert(cc%rowlen == 0);
	while (cc && (*tif->tif_encoderow)(tif, bp, rowlen, s) == 1)
		bp += rowlen, cc -= rowlen;
	return (cc == 0);
}

/*
 * Encode/Decode functions for converting to and from user formats.
 */

#include "uvcode.h"

#ifndef UVSCALE
#define U_NEU		0.210526316
#define V_NEU		0.473684211
#define UVSCALE		410.
#endif

#ifndef	M_LN2
#define M_LN2		0.69314718055994530942
#endif
#ifndef M_PI
#define M_PI		3.14159265358979323846
#endif
#define log2(x)		((1./M_LN2)*log(x))
#define exp2(x)		exp(M_LN2*(x))

#define itrunc(x,m)	((m)==SGILOGENCODE_NODITHER ? \
				(int)(x) : \
				(int)((x) + rand()*(1./RAND_MAX) - .5))

#if !LOGLUV_PUBLIC
static
#endif
double
LogL16toY(int p16)		/* compute luminance from 16-bit LogL */
{
	int	Le = p16 & 0x7fff;
	double	Y;

	if (!Le)
		return (0.);
	Y = exp(M_LN2/256.*(Le+.5) - M_LN2*64.);
	return (!(p16 & 0x8000) ? Y : -Y);
}

#if !LOGLUV_PUBLIC
static
#endif
int
LogL16fromY(double Y, int em)	/* get 16-bit LogL from Y */
{
	if (Y >= 1.8371976e19)
		return (0x7fff);
	if (Y <= -1.8371976e19)
		return (0xffff);
	if (Y > 5.4136769e-20)
		return itrunc(256.*(log2(Y) + 64.), em);
	if (Y < -5.4136769e-20)
		return (~0x7fff | itrunc(256.*(log2(-Y) + 64.), em));
	return (0);
}

static void
L16toY(LogLuvState* sp, uint8* op, tmsize_t n)
{
	int16* l16 = (int16*) sp->tbuf;
	float* yp = (float*) op;

	while (n-- > 0)
		*yp++ = (float)LogL16toY(*l16++);
}

static void
L16toGry(LogLuvState* sp, uint8* op, tmsize_t n)
{
	int16* l16 = (int16*) sp->tbuf;
	uint8* gp = (uint8*) op;

	while (n-- > 0) {
		double Y = LogL16toY(*l16++);
		*gp++ = (uint8) ((Y <= 0.) ? 0 : (Y >= 1.) ? 255 : (int)(256.*sqrt(Y)));
	}
}

static void
L16fromY(LogLuvState* sp, uint8* op, tmsize_t n)
{
	int16* l16 = (int16*) sp->tbuf;
	float* yp = (float*) op;

	while (n-- > 0)
		*l16++ = (int16) (LogL16fromY(*yp++, sp->encode_meth));
}

#if !LOGLUV_PUBLIC
static
#endif
void
XYZtoRGB24(float xyz[3], uint8 rgb[3])
{
	double	r, g, b;
					/* assume CCIR-709 primaries */
	r =  2.690*xyz[0] + -1.276*xyz[1] + -0.414*xyz[2];
	g = -1.022*xyz[0] +  1.978*xyz[1] +  0.044*xyz[2];
	b =  0.061*xyz[0] + -0.224*xyz[1] +  1.163*xyz[2];
					/* assume 2.0 gamma for speed */
	/* could use integer sqrt approx., but this is probably faster */
	rgb[0] = (uint8)((r<=0.) ? 0 : (r >= 1.) ? 255 : (int)(256.*sqrt(r)));
	rgb[1] = (uint8)((g<=0.) ? 0 : (g >= 1.) ? 255 : (int)(256.*sqrt(g)));
	rgb[2] = (uint8)((b<=0.) ? 0 : (b >= 1.) ? 255 : (int)(256.*sqrt(b)));
}

#if !LOGLUV_PUBLIC
static
#endif
double
LogL10toY(int p10)		/* compute luminance from 10-bit LogL */
{
	if (p10 == 0)
		return (0.);
	return (exp(M_LN2/64.*(p10+.5) - M_LN2*12.));
}

#if !LOGLUV_PUBLIC
static
#endif
int
LogL10fromY(double Y, int em)	/* get 10-bit LogL from Y */
{
	if (Y >= 15.742)
		return (0x3ff);
	else if (Y <= .00024283)
		return (0);
	else
		return itrunc(64.*(log2(Y) + 12.), em);
}

#define NANGLES		100
#define uv2ang(u, v)	( (NANGLES*.499999999/M_PI) \
				* atan2((v)-V_NEU,(u)-U_NEU) + .5*NANGLES )

static int
oog_encode(double u, double v)		/* encode out-of-gamut chroma */
{
	static int	oog_table[NANGLES];
	static int	initialized = 0;
	register int	i;

	if (!initialized) {		/* set up perimeter table */
		double	eps[NANGLES], ua, va, ang, epsa;
		int	ui, vi, ustep;
		for (i = NANGLES; i--; )
			eps[i] = 2.;
		for (vi = UV_NVS; vi--; ) {
			va = UV_VSTART + (vi+.5)*UV_SQSIZ;
			ustep = uv_row[vi].nus-1;
			if (vi == UV_NVS-1 || vi == 0 || ustep <= 0)
				ustep = 1;
			for (ui = uv_row[vi].nus-1; ui >= 0; ui -= ustep) {
				ua = uv_row[vi].ustart + (ui+.5)*UV_SQSIZ;
				ang = uv2ang(ua, va);
				i = (int) ang;
				epsa = fabs(ang - (i+.5));
				if (epsa < eps[i]) {
					oog_table[i] = uv_row[vi].ncum + ui;
					eps[i] = epsa;
				}
			}
		}
		for (i = NANGLES; i--; )	/* fill any holes */
			if (eps[i] > 1.5) {
				int	i1, i2;
				for (i1 = 1; i1 < NANGLES/2; i1++)
					if (eps[(i+i1)%NANGLES] < 1.5)
						break;
				for (i2 = 1; i2 < NANGLES/2; i2++)
					if (eps[(i+NANGLES-i2)%NANGLES] < 1.5)
						break;
				if (i1 < i2)
					oog_table[i] =
						oog_table[(i+i1)%NANGLES];
				else
					oog_table[i] =
						oog_table[(i+NANGLES-i2)%NANGLES];
			}
		initialized = 1;
	}
	i = (int) uv2ang(u, v);		/* look up hue angle */
	return (oog_table[i]);
}

#undef uv2ang
#undef NANGLES

#if !LOGLUV_PUBLIC
static
#endif
int
uv_encode(double u, double v, int em)	/* encode (u',v') coordinates */
{
	register int	vi, ui;

	if (v < UV_VSTART)
		return oog_encode(u, v);
	vi = itrunc((v - UV_VSTART)*(1./UV_SQSIZ), em);
	if (vi >= UV_NVS)
		return oog_encode(u, v);
	if (u < uv_row[vi].ustart)
		return oog_encode(u, v);
	ui = itrunc((u - uv_row[vi].ustart)*(1./UV_SQSIZ), em);
	if (ui >= uv_row[vi].nus)
		return oog_encode(u, v);

	return (uv_row[vi].ncum + ui);
}

#if !LOGLUV_PUBLIC
static
#endif
int
uv_decode(double *up, double *vp, int c)	/* decode (u',v') index */
{
	int	upper, lower;
	register int	ui, vi;

	if (c < 0 || c >= UV_NDIVS)
		return (-1);
	lower = 0;				/* binary search */
	upper = UV_NVS;
	while (upper - lower > 1) {
		vi = (lower + upper) >> 1;
		ui = c - uv_row[vi].ncum;
		if (ui > 0)
			lower = vi;
		else if (ui < 0)
			upper = vi;
		else {
			lower = vi;
			break;
		}
	}
	vi = lower;
	ui = c - uv_row[vi].ncum;
	*up = uv_row[vi].ustart + (ui+.5)*UV_SQSIZ;
	*vp = UV_VSTART + (vi+.5)*UV_SQSIZ;
	return (0);
}

#if !LOGLUV_PUBLIC
static
#endif
void
LogLuv24toXYZ(uint32 p, float XYZ[3])
{
	int	Ce;
	double	L, u, v, s, x, y;
					/* decode luminance */
	L = LogL10toY(p>>14 & 0x3ff);
	if (L <= 0.) {
		XYZ[0] = XYZ[1] = XYZ[2] = 0.;
		return;
	}
					/* decode color */
	Ce = p & 0x3fff;
	if (uv_decode(&u, &v, Ce) < 0) {
		u = U_NEU; v = V_NEU;
	}
	s = 1./(6.*u - 16.*v + 12.);
	x = 9.*u * s;
	y = 4.*v * s;
					/* convert to XYZ */
	XYZ[0] = (float)(x/y * L);
	XYZ[1] = (float)L;
	XYZ[2] = (float)((1.-x-y)/y * L);
}

#if !LOGLUV_PUBLIC
static
#endif
uint32
LogLuv24fromXYZ(float XYZ[3], int em)
{
	int	Le, Ce;
	double	u, v, s;
					/* encode luminance */
	Le = LogL10fromY(XYZ[1], em);
					/* encode color */
	s = XYZ[0] + 15.*XYZ[1] + 3.*XYZ[2];
	if (!Le || s <= 0.) {
		u = U_NEU;
		v = V_NEU;
	} else {
		u = 4.*XYZ[0] / s;
		v = 9.*XYZ[1] / s;
	}
	Ce = uv_encode(u, v, em);
	if (Ce < 0)			/* never happens */
		Ce = uv_encode(U_NEU, V_NEU, SGILOGENCODE_NODITHER);
					/* combine encodings */
	return (Le << 14 | Ce);
}

static void
Luv24toXYZ(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	float* xyz = (float*) op;

	while (n-- > 0) {
		LogLuv24toXYZ(*luv, xyz);
		xyz += 3;
		luv++;
	}
}

static void
Luv24toLuv48(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	int16* luv3 = (int16*) op;

	while (n-- > 0) {
		double u, v;

		*luv3++ = (int16)((*luv >> 12 & 0xffd) + 13314);
		if (uv_decode(&u, &v, *luv&0x3fff) < 0) {
			u = U_NEU;
			v = V_NEU;
		}
		*luv3++ = (int16)(u * (1L<<15));
		*luv3++ = (int16)(v * (1L<<15));
		luv++;
	}
}

static void
Luv24toRGB(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	uint8* rgb = (uint8*) op;

	while (n-- > 0) {
		float xyz[3];

		LogLuv24toXYZ(*luv++, xyz);
		XYZtoRGB24(xyz, rgb);
		rgb += 3;
	}
}

static void
Luv24fromXYZ(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	float* xyz = (float*) op;

	while (n-- > 0) {
		*luv++ = LogLuv24fromXYZ(xyz, sp->encode_meth);
		xyz += 3;
	}
}

static void
Luv24fromLuv48(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	int16* luv3 = (int16*) op;

	while (n-- > 0) {
		int Le, Ce;

		if (luv3[0] <= 0)
			Le = 0;
		else if (luv3[0] >= (1<<12)+3314)
			Le = (1<<10) - 1;
		else if (sp->encode_meth == SGILOGENCODE_NODITHER)
			Le = (luv3[0]-3314) >> 2;
		else
			Le = itrunc(.25*(luv3[0]-3314.), sp->encode_meth);

		Ce = uv_encode((luv3[1]+.5)/(1<<15), (luv3[2]+.5)/(1<<15),
					sp->encode_meth);
		if (Ce < 0)	/* never happens */
			Ce = uv_encode(U_NEU, V_NEU, SGILOGENCODE_NODITHER);
		*luv++ = (uint32)Le << 14 | Ce;
		luv3 += 3;
	}
}

#if !LOGLUV_PUBLIC
static
#endif
void
LogLuv32toXYZ(uint32 p, float XYZ[3])
{
	double	L, u, v, s, x, y;
					/* decode luminance */
	L = LogL16toY((int)p >> 16);
	if (L <= 0.) {
		XYZ[0] = XYZ[1] = XYZ[2] = 0.;
		return;
	}
					/* decode color */
	u = 1./UVSCALE * ((p>>8 & 0xff) + .5);
	v = 1./UVSCALE * ((p & 0xff) + .5);
	s = 1./(6.*u - 16.*v + 12.);
	x = 9.*u * s;
	y = 4.*v * s;
					/* convert to XYZ */
	XYZ[0] = (float)(x/y * L);
	XYZ[1] = (float)L;
	XYZ[2] = (float)((1.-x-y)/y * L);
}

#if !LOGLUV_PUBLIC
static
#endif
uint32
LogLuv32fromXYZ(float XYZ[3], int em)
{
	unsigned int	Le, ue, ve;
	double	u, v, s;
					/* encode luminance */
	Le = (unsigned int)LogL16fromY(XYZ[1], em);
					/* encode color */
	s = XYZ[0] + 15.*XYZ[1] + 3.*XYZ[2];
	if (!Le || s <= 0.) {
		u = U_NEU;
		v = V_NEU;
	} else {
		u = 4.*XYZ[0] / s;
		v = 9.*XYZ[1] / s;
	}
	if (u <= 0.) ue = 0;
	else ue = itrunc(UVSCALE*u, em);
	if (ue > 255) ue = 255;
	if (v <= 0.) ve = 0;
	else ve = itrunc(UVSCALE*v, em);
	if (ve > 255) ve = 255;
					/* combine encodings */
	return (Le << 16 | ue << 8 | ve);
}

static void
Luv32toXYZ(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	float* xyz = (float*) op;

	while (n-- > 0) {
		LogLuv32toXYZ(*luv++, xyz);
		xyz += 3;
	}
}

static void
Luv32toLuv48(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	int16* luv3 = (int16*) op;

	while (n-- > 0) {
		double u, v;

		*luv3++ = (int16)(*luv >> 16);
		u = 1./UVSCALE * ((*luv>>8 & 0xff) + .5);
		v = 1./UVSCALE * ((*luv & 0xff) + .5);
		*luv3++ = (int16)(u * (1L<<15));
		*luv3++ = (int16)(v * (1L<<15));
		luv++;
	}
}

static void
Luv32toRGB(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	uint8* rgb = (uint8*) op;

	while (n-- > 0) {
		float xyz[3];

		LogLuv32toXYZ(*luv++, xyz);
		XYZtoRGB24(xyz, rgb);
		rgb += 3;
	}
}

static void
Luv32fromXYZ(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;  
	float* xyz = (float*) op;

	while (n-- > 0) {
		*luv++ = LogLuv32fromXYZ(xyz, sp->encode_meth);
		xyz += 3;
	}
}

static void
Luv32fromLuv48(LogLuvState* sp, uint8* op, tmsize_t n)
{
	uint32* luv = (uint32*) sp->tbuf;
	int16* luv3 = (int16*) op;

	if (sp->encode_meth == SGILOGENCODE_NODITHER) {
		while (n-- > 0) {
			*luv++ = (uint32)luv3[0] << 16 |
				(luv3[1]*(uint32)(UVSCALE+.5) >> 7 & 0xff00) |
				(luv3[2]*(uint32)(UVSCALE+.5) >> 15 & 0xff);
			luv3 += 3;
		}
		return;
	}
	while (n-- > 0) {
		*luv++ = (uint32)luv3[0] << 16 |
	(itrunc(luv3[1]*(UVSCALE/(1<<15)), sp->encode_meth) << 8 & 0xff00) |
		(itrunc(luv3[2]*(UVSCALE/(1<<15)), sp->encode_meth) & 0xff);
		luv3 += 3;
	}
}

static void
_logLuvNop(LogLuvState* sp, uint8* op, tmsize_t n)
{
	(void) sp; (void) op; (void) n;
}

static int
LogL16GuessDataFmt(TIFFDirectory *td)
{
#define	PACK(s,b,f)	(((b)<<6)|((s)<<3)|(f))
	switch (PACK(td->td_samplesperpixel, td->td_bitspersample, td->td_sampleformat)) {
	case PACK(1, 32, SAMPLEFORMAT_IEEEFP):
		return (SGILOGDATAFMT_FLOAT);
	case PACK(1, 16, SAMPLEFORMAT_VOID):
	case PACK(1, 16, SAMPLEFORMAT_INT):
	case PACK(1, 16, SAMPLEFORMAT_UINT):
		return (SGILOGDATAFMT_16BIT);
	case PACK(1,  8, SAMPLEFORMAT_VOID):
	case PACK(1,  8, SAMPLEFORMAT_UINT):
		return (SGILOGDATAFMT_8BIT);
	}
#undef PACK
	return (SGILOGDATAFMT_UNKNOWN);
}

static tmsize_t
multiply_ms(tmsize_t m1, tmsize_t m2)
{
	tmsize_t bytes = m1 * m2;

	if (m1 && bytes / m1 != m2)
		bytes = 0;

	return bytes;
}

static int
LogL16InitState(TIFF* tif)
{
	static const char module[] = "LogL16InitState";
	TIFFDirectory *td = &tif->tif_dir;
	LogLuvState* sp = DecoderState(tif);

	assert(sp != NULL);
	assert(td->td_photometric == PHOTOMETRIC_LOGL);

	/* for some reason, we can't do this in TIFFInitLogL16 */
	if (sp->user_datafmt == SGILOGDATAFMT_UNKNOWN)
		sp->user_datafmt = LogL16GuessDataFmt(td);
	switch (sp->user_datafmt) {
	case SGILOGDATAFMT_FLOAT:
		sp->pixel_size = sizeof (float);
		break;
	case SGILOGDATAFMT_16BIT:
		sp->pixel_size = sizeof (int16);
		break;
	case SGILOGDATAFMT_8BIT:
		sp->pixel_size = sizeof (uint8);
		break;
	default:
		TIFFErrorExt(tif->tif_clientdata, module,
		    "No support for converting user data format to LogL");
		return (0);
	}
        if( isTiled(tif) )
            sp->tbuflen = multiply_ms(td->td_tilewidth, td->td_tilelength);
        else
            sp->tbuflen = multiply_ms(td->td_imagewidth, td->td_rowsperstrip);
	if (multiply_ms(sp->tbuflen, sizeof (int16)) == 0 ||
	    (sp->tbuf = (uint8*) _TIFFmalloc(sp->tbuflen * sizeof (int16))) == NULL) {
		TIFFErrorExt(tif->tif_clientdata, module, "No space for SGILog translation buffer");
		return (0);
	}
	return (1);
}

static int
LogLuvGuessDataFmt(TIFFDirectory *td)
{
	int guess;

	/*
	 * If the user didn't tell us their datafmt,
	 * take our best guess from the bitspersample.
	 */
#define	PACK(a,b)	(((a)<<3)|(b))
	switch (PACK(td->td_bitspersample, td->td_sampleformat)) {
	case PACK(32, SAMPLEFORMAT_IEEEFP):
		guess = SGILOGDATAFMT_FLOAT;
		break;
	case PACK(32, SAMPLEFORMAT_VOID):
	case PACK(32, SAMPLEFORMAT_UINT):
	case PACK(32, SAMPLEFORMAT_INT):
		guess = SGILOGDATAFMT_RAW;
		break;
	case PACK(16, SAMPLEFORMAT_VOID):
	case PACK(16, SAMPLEFORMAT_INT):
	case PACK(16, SAMPLEFORMAT_UINT):
		guess = SGILOGDATAFMT_16BIT;
		break;
	case PACK( 8, SAMPLEFORMAT_VOID):
	case PACK( 8, SAMPLEFORMAT_UINT):
		guess = SGILOGDATAFMT_8BIT;
		break;
	default:
		guess = SGILOGDATAFMT_UNKNOWN;
		break;
#undef PACK
	}
	/*
	 * Double-check samples per pixel.
	 */
	switch (td->td_samplesperpixel) {
	case 1:
		if (guess != SGILOGDATAFMT_RAW)
			guess = SGILOGDATAFMT_UNKNOWN;
		break;
	case 3:
		if (guess == SGILOGDATAFMT_RAW)
			guess = SGILOGDATAFMT_UNKNOWN;
		break;
	default:
		guess = SGILOGDATAFMT_UNKNOWN;
		break;
	}
	return (guess);
}

static int
LogLuvInitState(TIFF* tif)
{
	static const char module[] = "LogLuvInitState";
	TIFFDirectory* td = &tif->tif_dir;
	LogLuvState* sp = DecoderState(tif);

	assert(sp != NULL);
	assert(td->td_photometric == PHOTOMETRIC_LOGLUV);

	/* for some reason, we can't do this in TIFFInitLogLuv */
	if (td->td_planarconfig != PLANARCONFIG_CONTIG) {
		TIFFErrorExt(tif->tif_clientdata, module,
		    "SGILog compression cannot handle non-contiguous data");
		return (0);
	}
	if (sp->user_datafmt == SGILOGDATAFMT_UNKNOWN)
		sp->user_datafmt = LogLuvGuessDataFmt(td);
	switch (sp->user_datafmt) {
	case SGILOGDATAFMT_FLOAT:
		sp->pixel_size = 3*sizeof (float);
		break;
	case SGILOGDATAFMT_16BIT:
		sp->pixel_size = 3*sizeof (int16);
		break;
	case SGILOGDATAFMT_RAW:
		sp->pixel_size = sizeof (uint32);
		break;
	case SGILOGDATAFMT_8BIT:
		sp->pixel_size = 3*sizeof (uint8);
		break;
	default:
		TIFFErrorExt(tif->tif_clientdata, module,
		    "No support for converting user data format to LogLuv");
		return (0);
	}
        if( isTiled(tif) )
            sp->tbuflen = multiply_ms(td->td_tilewidth, td->td_tilelength);
        else
            sp->tbuflen = multiply_ms(td->td_imagewidth, td->td_rowsperstrip);
	if (multiply_ms(sp->tbuflen, sizeof (uint32)) == 0 ||
	    (sp->tbuf = (uint8*) _TIFFmalloc(sp->tbuflen * sizeof (uint32))) == NULL) {
		TIFFErrorExt(tif->tif_clientdata, module, "No space for SGILog translation buffer");
		return (0);
	}
	return (1);
}

static int
LogLuvFixupTags(TIFF* tif)
{
	(void) tif;
	return (1);
}

static int
LogLuvSetupDecode(TIFF* tif)
{
	static const char module[] = "LogLuvSetupDecode";
	LogLuvState* sp = DecoderState(tif);
	TIFFDirectory* td = &tif->tif_dir;

	tif->tif_postdecode = _TIFFNoPostDecode;
	switch (td->td_photometric) {
	case PHOTOMETRIC_LOGLUV:
		if (!LogLuvInitState(tif))
			break;
		if (td->td_compression == COMPRESSION_SGILOG24) {
			tif->tif_decoderow = LogLuvDecode24;
			switch (sp->user_datafmt) {
			case SGILOGDATAFMT_FLOAT:
				sp->tfunc = Luv24toXYZ;  
				break;
			case SGILOGDATAFMT_16BIT:
				sp->tfunc = Luv24toLuv48;  
				break;
			case SGILOGDATAFMT_8BIT:
				sp->tfunc = Luv24toRGB;
				break;
			}
		} else {
			tif->tif_decoderow = LogLuvDecode32;
			switch (sp->user_datafmt) {
			case SGILOGDATAFMT_FLOAT:
				sp->tfunc = Luv32toXYZ;
				break;
			case SGILOGDATAFMT_16BIT:
				sp->tfunc = Luv32toLuv48;
				break;
			case SGILOGDATAFMT_8BIT:
				sp->tfunc = Luv32toRGB;
				break;
			}
		}
		return (1);
	case PHOTOMETRIC_LOGL:
		if (!LogL16InitState(tif))
			break;
		tif->tif_decoderow = LogL16Decode;
		switch (sp->user_datafmt) {
		case SGILOGDATAFMT_FLOAT:
			sp->tfunc = L16toY;
			break;
		case SGILOGDATAFMT_8BIT:
			sp->tfunc = L16toGry;
			break;
		}
		return (1);
	default:
		TIFFErrorExt(tif->tif_clientdata, module,
		    "Inappropriate photometric interpretation %d for SGILog compression; %s",
		    td->td_photometric, "must be either LogLUV or LogL");
		break;
	}
	return (0);
}

static int
LogLuvSetupEncode(TIFF* tif)
{
	static const char module[] = "LogLuvSetupEncode";
	LogLuvState* sp = EncoderState(tif);
	TIFFDirectory* td = &tif->tif_dir;

	switch (td->td_photometric) {
	case PHOTOMETRIC_LOGLUV:
		if (!LogLuvInitState(tif))
			break;
		if (td->td_compression == COMPRESSION_SGILOG24) {
			tif->tif_encoderow = LogLuvEncode24;
			switch (sp->user_datafmt) {
			case SGILOGDATAFMT_FLOAT:
				sp->tfunc = Luv24fromXYZ;
				break;
			case SGILOGDATAFMT_16BIT:
				sp->tfunc = Luv24fromLuv48;  
				break;
			case SGILOGDATAFMT_RAW:
				break;
			default:
				goto notsupported;
			}
		} else {
			tif->tif_encoderow = LogLuvEncode32;  
			switch (sp->user_datafmt) {
			case SGILOGDATAFMT_FLOAT:
				sp->tfunc = Luv32fromXYZ;  
				break;
			case SGILOGDATAFMT_16BIT:
				sp->tfunc = Luv32fromLuv48;  
				break;
			case SGILOGDATAFMT_RAW:
				break;
			default:
				goto notsupported;
			}
		}
		break;
	case PHOTOMETRIC_LOGL:
		if (!LogL16InitState(tif))
			break;
		tif->tif_encoderow = LogL16Encode;  
		switch (sp->user_datafmt) {
		case SGILOGDATAFMT_FLOAT:
			sp->tfunc = L16fromY;
			break;
		case SGILOGDATAFMT_16BIT:
			break;
		default:
			goto notsupported;
		}
		break;
	default:
		TIFFErrorExt(tif->tif_clientdata, module,
		    "Inappropriate photometric interpretation %d for SGILog compression; %s",
		    td->td_photometric, "must be either LogLUV or LogL");
		break;
	}
	return (1);
notsupported:
	TIFFErrorExt(tif->tif_clientdata, module,
	    "SGILog compression supported only for %s, or raw data",
	    td->td_photometric == PHOTOMETRIC_LOGL ? "Y, L" : "XYZ, Luv");
	return (0);
}

static void
LogLuvClose(TIFF* tif)
{
	TIFFDirectory *td = &tif->tif_dir;

	/*
	 * For consistency, we always want to write out the same
	 * bitspersample and sampleformat for our TIFF file,
	 * regardless of the data format being used by the application.
	 * Since this routine is called after tags have been set but
	 * before they have been recorded in the file, we reset them here.
	 */
	td->td_samplesperpixel =
	    (td->td_photometric == PHOTOMETRIC_LOGL) ? 1 : 3;
	td->td_bitspersample = 16;
	td->td_sampleformat = SAMPLEFORMAT_INT;
}

static void
LogLuvCleanup(TIFF* tif)
{
	LogLuvState* sp = (LogLuvState *)tif->tif_data;

	assert(sp != 0);

	tif->tif_tagmethods.vgetfield = sp->vgetparent;
	tif->tif_tagmethods.vsetfield = sp->vsetparent;

	if (sp->tbuf)
		_TIFFfree(sp->tbuf);
	_TIFFfree(sp);
	tif->tif_data = NULL;

	_TIFFSetDefaultCompressionState(tif);
}

static int
LogLuvVSetField(TIFF* tif, uint32 tag, va_list ap)
{
	static const char module[] = "LogLuvVSetField";
	LogLuvState* sp = DecoderState(tif);
	int bps, fmt;

	switch (tag) {
	case TIFFTAG_SGILOGDATAFMT:
		sp->user_datafmt = (int) va_arg(ap, int);
		/*
		 * Tweak the TIFF header so that the rest of libtiff knows what
		 * size of data will be passed between app and library, and
		 * assume that the app knows what it is doing and is not
		 * confused by these header manipulations...
		 */
		switch (sp->user_datafmt) {
		case SGILOGDATAFMT_FLOAT:
			bps = 32, fmt = SAMPLEFORMAT_IEEEFP;
			break;
		case SGILOGDATAFMT_16BIT:
			bps = 16, fmt = SAMPLEFORMAT_INT;
			break;
		case SGILOGDATAFMT_RAW:
			bps = 32, fmt = SAMPLEFORMAT_UINT;
			TIFFSetField(tif, TIFFTAG_SAMPLESPERPIXEL, 1);
			break;
		case SGILOGDATAFMT_8BIT:
			bps = 8, fmt = SAMPLEFORMAT_UINT;
			break;
		default:
			TIFFErrorExt(tif->tif_clientdata, tif->tif_name,
			    "Unknown data format %d for LogLuv compression",
			    sp->user_datafmt);
			return (0);
		}
		TIFFSetField(tif, TIFFTAG_BITSPERSAMPLE, bps);
		TIFFSetField(tif, TIFFTAG_SAMPLEFORMAT, fmt);
		/*
		 * Must recalculate sizes should bits/sample change.
		 */
		tif->tif_tilesize = isTiled(tif) ? TIFFTileSize(tif) : (tmsize_t) -1;
		tif->tif_scanlinesize = TIFFScanlineSize(tif);
		return (1);
	case TIFFTAG_SGILOGENCODE:
		sp->encode_meth = (int) va_arg(ap, int);
		if (sp->encode_meth != SGILOGENCODE_NODITHER &&
		    sp->encode_meth != SGILOGENCODE_RANDITHER) {
			TIFFErrorExt(tif->tif_clientdata, module,
			    "Unknown encoding %d for LogLuv compression",
			    sp->encode_meth);
			return (0);
		}
		return (1);
	default:
		return (*sp->vsetparent)(tif, tag, ap);
	}
}

static int
LogLuvVGetField(TIFF* tif, uint32 tag, va_list ap)
{
	LogLuvState *sp = (LogLuvState *)tif->tif_data;

	switch (tag) {
	case TIFFTAG_SGILOGDATAFMT:
		*va_arg(ap, int*) = sp->user_datafmt;
		return (1);
	default:
		return (*sp->vgetparent)(tif, tag, ap);
	}
}

static const TIFFField LogLuvFields[] = {
    { TIFFTAG_SGILOGDATAFMT, 0, 0, TIFF_SHORT, 0, TIFF_SETGET_INT, TIFF_SETGET_UNDEFINED, FIELD_PSEUDO, TRUE, FALSE, "SGILogDataFmt", NULL},
    { TIFFTAG_SGILOGENCODE, 0, 0, TIFF_SHORT, 0, TIFF_SETGET_INT, TIFF_SETGET_UNDEFINED, FIELD_PSEUDO, TRUE, FALSE, "SGILogEncode", NULL}
};

int
TIFFInitSGILog(TIFF* tif, int scheme)
{
	static const char module[] = "TIFFInitSGILog";
	LogLuvState* sp;

	assert(scheme == COMPRESSION_SGILOG24 || scheme == COMPRESSION_SGILOG);

	/*
	 * Merge codec-specific tag information.
	 */
	if (!_TIFFMergeFields(tif, LogLuvFields,
			      TIFFArrayCount(LogLuvFields))) {
		TIFFErrorExt(tif->tif_clientdata, module,
		    "Merging SGILog codec-specific tags failed");
		return 0;
	}

	/*
	 * Allocate state block so tag methods have storage to record values.
	 */
	tif->tif_data = (uint8*) _TIFFmalloc(sizeof (LogLuvState));
	if (tif->tif_data == NULL)
		goto bad;
	sp = (LogLuvState*) tif->tif_data;
	_TIFFmemset((void*)sp, 0, sizeof (*sp));
	sp->user_datafmt = SGILOGDATAFMT_UNKNOWN;
	sp->encode_meth = (scheme == COMPRESSION_SGILOG24) ?
	    SGILOGENCODE_RANDITHER : SGILOGENCODE_NODITHER;
	sp->tfunc = _logLuvNop;

	/*
	 * Install codec methods.
	 * NB: tif_decoderow & tif_encoderow are filled
	 *     in at setup time.
	 */
	tif->tif_fixuptags = LogLuvFixupTags;  
	tif->tif_setupdecode = LogLuvSetupDecode;
	tif->tif_decodestrip = LogLuvDecodeStrip;
	tif->tif_decodetile = LogLuvDecodeTile;
	tif->tif_setupencode = LogLuvSetupEncode;
	tif->tif_encodestrip = LogLuvEncodeStrip;  
	tif->tif_encodetile = LogLuvEncodeTile;
	tif->tif_close = LogLuvClose;
	tif->tif_cleanup = LogLuvCleanup;

	/*
	 * Override parent get/set field methods.
	 */
	sp->vgetparent = tif->tif_tagmethods.vgetfield;
	tif->tif_tagmethods.vgetfield = LogLuvVGetField;   /* hook for codec tags */
	sp->vsetparent = tif->tif_tagmethods.vsetfield;
	tif->tif_tagmethods.vsetfield = LogLuvVSetField;   /* hook for codec tags */

	return (1);
bad:
	TIFFErrorExt(tif->tif_clientdata, module,
		     "%s: No space for LogLuv state block", tif->tif_name);
	return (0);
}
#endif /* LOGLUV_SUPPORT */

/* vim: set ts=8 sts=8 sw=8 noet: */
/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */

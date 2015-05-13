/* $Id: tif_zip.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * Copyright (c) 1995-1997 Sam Leffler
 * Copyright (c) 1995-1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler and Silicon Graphics may not be used in any advertising or
 * publicity relating to the software without the specific, prior written
 * permission of Sam Leffler and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
 * ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */

#include "tiffiop.h"
#ifdef ZIP_SUPPORT
/*
 * TIFF Library.
 *
 * ZIP (aka Deflate) Compression Support
 *
 * This file is simply an interface to the zlib library written by
 * Jean-loup Gailly and Mark Adler.  You must use version 1.0 or later
 * of the library: this code assumes the 1.0 API and also depends on
 * the ability to write the zlib header multiple times (one per strip)
 * which was not possible with versions prior to 0.95.  Note also that
 * older versions of this codec avoided this bug by supressing the header
 * entirely.  This means that files written with the old library cannot
 * be read; they should be converted to a different compression scheme
 * and then reconverted.
 *
 * The data format used by the zlib library is described in the files
 * zlib-3.1.doc, deflate-1.1.doc and gzip-4.1.doc, available in the
 * directory ftp://ftp.uu.net/pub/archiving/zip/doc.  The library was
 * last found at ftp://ftp.uu.net/pub/archiving/zip/zlib/zlib-0.99.tar.gz.
 */
#include "tif_predict.h"
#include "../ZLib/zlib.h"

#include <stdio.h>

/*
 * Sigh, ZLIB_VERSION is defined as a string so there's no
 * way to do a proper check here.  Instead we guess based
 * on the presence of #defines that were added between the
 * 0.95 and 1.0 distributions.
 */
#if !defined(Z_NO_COMPRESSION) || !defined(Z_DEFLATED)
#error "Antiquated ZLIB software; you must use version 1.0 or later"
#endif

#define SAFE_MSG(sp)   ((sp)->stream.msg == NULL ? "" : (sp)->stream.msg)

/*
 * State block for each open TIFF
 * file using ZIP compression/decompression.
 */
typedef struct {
	TIFFPredictorState predict;
        z_stream        stream;
	int             zipquality;            /* compression level */
	int             state;                 /* state flags */
#define ZSTATE_INIT_DECODE 0x01
#define ZSTATE_INIT_ENCODE 0x02

	TIFFVGetMethod  vgetparent;            /* super-class method */
	TIFFVSetMethod  vsetparent;            /* super-class method */
} ZIPState;

#define ZState(tif)             ((ZIPState*) (tif)->tif_data)
#define DecoderState(tif)       ZState(tif)
#define EncoderState(tif)       ZState(tif)

static int ZIPEncode(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s);
static int ZIPDecode(TIFF* tif, uint8* op, tmsize_t occ, uint16 s);

static int
ZIPFixupTags(TIFF* tif)
{
	(void) tif;
	return (1);
}

static int
ZIPSetupDecode(TIFF* tif)
{
	static const char module[] = "ZIPSetupDecode";
	ZIPState* sp = DecoderState(tif);

	assert(sp != NULL);
        
        /* if we were last encoding, terminate this mode */
	if (sp->state & ZSTATE_INIT_ENCODE) {
	    deflateEnd(&sp->stream);
	    sp->state = 0;
	}

	if (inflateInit(&sp->stream) != Z_OK) {
		TIFFErrorExt(tif->tif_clientdata, module, "%s", SAFE_MSG(sp));
		return (0);
	} else {
		sp->state |= ZSTATE_INIT_DECODE;
		return (1);
	}
}

/*
 * Setup state for decoding a strip.
 */
static int
ZIPPreDecode(TIFF* tif, uint16 s)
{
	static const char module[] = "ZIPPreDecode";
	ZIPState* sp = DecoderState(tif);

	(void) s;
	assert(sp != NULL);

	if( (sp->state & ZSTATE_INIT_DECODE) == 0 )
            tif->tif_setupdecode( tif );

	sp->stream.next_in = tif->tif_rawdata;
	assert(sizeof(sp->stream.avail_in)==4);  /* if this assert gets raised,
	    we need to simplify this code to reflect a ZLib that is likely updated
	    to deal with 8byte memory sizes, though this code will respond
	    apropriately even before we simplify it */
	sp->stream.avail_in = (uInt) tif->tif_rawcc;
	if ((tmsize_t)sp->stream.avail_in != tif->tif_rawcc)
	{
		TIFFErrorExt(tif->tif_clientdata, module, "ZLib cannot deal with buffers this size");
		return (0);
	}
	return (inflateReset(&sp->stream) == Z_OK);
}

static int
ZIPDecode(TIFF* tif, uint8* op, tmsize_t occ, uint16 s)
{
	static const char module[] = "ZIPDecode";
	ZIPState* sp = DecoderState(tif);

	(void) s;
	assert(sp != NULL);
	assert(sp->state == ZSTATE_INIT_DECODE);

        sp->stream.next_in = tif->tif_rawcp;
	sp->stream.avail_in = (uInt) tif->tif_rawcc;
        
	sp->stream.next_out = op;
	assert(sizeof(sp->stream.avail_out)==4);  /* if this assert gets raised,
	    we need to simplify this code to reflect a ZLib that is likely updated
	    to deal with 8byte memory sizes, though this code will respond
	    apropriately even before we simplify it */
	sp->stream.avail_out = (uInt) occ;
	if ((tmsize_t)sp->stream.avail_out != occ)
	{
		TIFFErrorExt(tif->tif_clientdata, module, "ZLib cannot deal with buffers this size");
		return (0);
	}
	do {
		int state = inflate(&sp->stream, Z_PARTIAL_FLUSH);
		if (state == Z_STREAM_END)
			break;
		if (state == Z_DATA_ERROR) {
			TIFFErrorExt(tif->tif_clientdata, module,
			    "Decoding error at scanline %lu, %s",
			     (unsigned long) tif->tif_row, SAFE_MSG(sp));
			if (inflateSync(&sp->stream) != Z_OK)
				return (0);
			continue;
		}
		if (state != Z_OK) {
			TIFFErrorExt(tif->tif_clientdata, module, 
				     "ZLib error: %s", SAFE_MSG(sp));
			return (0);
		}
	} while (sp->stream.avail_out > 0);
	if (sp->stream.avail_out != 0) {
		TIFFErrorExt(tif->tif_clientdata, module,
		    "Not enough data at scanline %lu (short " TIFF_UINT64_FORMAT " bytes)",
		    (unsigned long) tif->tif_row, (TIFF_UINT64_T) sp->stream.avail_out);
		return (0);
	}

        tif->tif_rawcp = sp->stream.next_in;
        tif->tif_rawcc = sp->stream.avail_in;

	return (1);
}

static int
ZIPSetupEncode(TIFF* tif)
{
	static const char module[] = "ZIPSetupEncode";
	ZIPState* sp = EncoderState(tif);

	assert(sp != NULL);
	if (sp->state & ZSTATE_INIT_DECODE) {
		inflateEnd(&sp->stream);
		sp->state = 0;
	}

	if (deflateInit(&sp->stream, sp->zipquality) != Z_OK) {
		TIFFErrorExt(tif->tif_clientdata, module, "%s", SAFE_MSG(sp));
		return (0);
	} else {
		sp->state |= ZSTATE_INIT_ENCODE;
		return (1);
	}
}

/*
 * Reset encoding state at the start of a strip.
 */
static int
ZIPPreEncode(TIFF* tif, uint16 s)
{
	static const char module[] = "ZIPPreEncode";
	ZIPState *sp = EncoderState(tif);

	(void) s;
	assert(sp != NULL);
	if( sp->state != ZSTATE_INIT_ENCODE )
            tif->tif_setupencode( tif );

	sp->stream.next_out = tif->tif_rawdata;
	assert(sizeof(sp->stream.avail_out)==4);  /* if this assert gets raised,
	    we need to simplify this code to reflect a ZLib that is likely updated
	    to deal with 8byte memory sizes, though this code will respond
	    apropriately even before we simplify it */
	sp->stream.avail_out = tif->tif_rawdatasize;
	if ((tmsize_t)sp->stream.avail_out != tif->tif_rawdatasize)
	{
		TIFFErrorExt(tif->tif_clientdata, module, "ZLib cannot deal with buffers this size");
		return (0);
	}
	return (deflateReset(&sp->stream) == Z_OK);
}

/*
 * Encode a chunk of pixels.
 */
static int
ZIPEncode(TIFF* tif, uint8* bp, tmsize_t cc, uint16 s)
{
	static const char module[] = "ZIPEncode";
	ZIPState *sp = EncoderState(tif);

	assert(sp != NULL);
	assert(sp->state == ZSTATE_INIT_ENCODE);

	(void) s;
	sp->stream.next_in = bp;
	assert(sizeof(sp->stream.avail_in)==4);  /* if this assert gets raised,
	    we need to simplify this code to reflect a ZLib that is likely updated
	    to deal with 8byte memory sizes, though this code will respond
	    apropriately even before we simplify it */
	sp->stream.avail_in = (uInt) cc;
	if ((tmsize_t)sp->stream.avail_in != cc)
	{
		TIFFErrorExt(tif->tif_clientdata, module, "ZLib cannot deal with buffers this size");
		return (0);
	}
	do {
		if (deflate(&sp->stream, Z_NO_FLUSH) != Z_OK) {
			TIFFErrorExt(tif->tif_clientdata, module, 
				     "Encoder error: %s",
				     SAFE_MSG(sp));
			return (0);
		}
		if (sp->stream.avail_out == 0) {
			tif->tif_rawcc = tif->tif_rawdatasize;
			TIFFFlushData1(tif);
			sp->stream.next_out = tif->tif_rawdata;
			sp->stream.avail_out = (uInt) tif->tif_rawdatasize;  /* this is a safe typecast, as check is made already in ZIPPreEncode */
		}
	} while (sp->stream.avail_in > 0);
	return (1);
}

/*
 * Finish off an encoded strip by flushing the last
 * string and tacking on an End Of Information code.
 */
static int
ZIPPostEncode(TIFF* tif)
{
	static const char module[] = "ZIPPostEncode";
	ZIPState *sp = EncoderState(tif);
	int state;

	sp->stream.avail_in = 0;
	do {
		state = deflate(&sp->stream, Z_FINISH);
		switch (state) {
		case Z_STREAM_END:
		case Z_OK:
			if ((tmsize_t)sp->stream.avail_out != tif->tif_rawdatasize)
			{
				tif->tif_rawcc =  tif->tif_rawdatasize - sp->stream.avail_out;
				TIFFFlushData1(tif);
				sp->stream.next_out = tif->tif_rawdata;
				sp->stream.avail_out = (uInt) tif->tif_rawdatasize;  /* this is a safe typecast, as check is made already in ZIPPreEncode */
			}
			break;
		default:
			TIFFErrorExt(tif->tif_clientdata, module, 
				     "ZLib error: %s", SAFE_MSG(sp));
			return (0);
		}
	} while (state != Z_STREAM_END);
	return (1);
}

static void
ZIPCleanup(TIFF* tif)
{
	ZIPState* sp = ZState(tif);

	assert(sp != 0);

	(void)TIFFPredictorCleanup(tif);

	tif->tif_tagmethods.vgetfield = sp->vgetparent;
	tif->tif_tagmethods.vsetfield = sp->vsetparent;

	if (sp->state & ZSTATE_INIT_ENCODE) {
		deflateEnd(&sp->stream);
		sp->state = 0;
	} else if( sp->state & ZSTATE_INIT_DECODE) {
		inflateEnd(&sp->stream);
		sp->state = 0;
	}
	_TIFFfree(sp);
	tif->tif_data = NULL;

	_TIFFSetDefaultCompressionState(tif);
}

static int
ZIPVSetField(TIFF* tif, uint32 tag, va_list ap)
{
	static const char module[] = "ZIPVSetField";
	ZIPState* sp = ZState(tif);

	switch (tag) {
	case TIFFTAG_ZIPQUALITY:
		sp->zipquality = (int) va_arg(ap, int);
		if ( sp->state&ZSTATE_INIT_ENCODE ) {
			if (deflateParams(&sp->stream,
			    sp->zipquality, Z_DEFAULT_STRATEGY) != Z_OK) {
				TIFFErrorExt(tif->tif_clientdata, module, "ZLib error: %s",
					     SAFE_MSG(sp));
				return (0);
			}
		}
		return (1);
	default:
		return (*sp->vsetparent)(tif, tag, ap);
	}
	/*NOTREACHED*/
}

static int
ZIPVGetField(TIFF* tif, uint32 tag, va_list ap)
{
	ZIPState* sp = ZState(tif);

	switch (tag) {
	case TIFFTAG_ZIPQUALITY:
		*va_arg(ap, int*) = sp->zipquality;
		break;
	default:
		return (*sp->vgetparent)(tif, tag, ap);
	}
	return (1);
}

static const TIFFField zipFields[] = {
    { TIFFTAG_ZIPQUALITY, 0, 0, TIFF_ANY, 0, TIFF_SETGET_INT, TIFF_SETGET_UNDEFINED, FIELD_PSEUDO, TRUE, FALSE, "", NULL },
};

int
TIFFInitZIP(TIFF* tif, int scheme)
{
	static const char module[] = "TIFFInitZIP";
	ZIPState* sp;

	assert( (scheme == COMPRESSION_DEFLATE)
		|| (scheme == COMPRESSION_ADOBE_DEFLATE));

	/*
	 * Merge codec-specific tag information.
	 */
	if (!_TIFFMergeFields(tif, zipFields, TIFFArrayCount(zipFields))) {
		TIFFErrorExt(tif->tif_clientdata, module,
			     "Merging Deflate codec-specific tags failed");
		return 0;
	}

	/*
	 * Allocate state block so tag methods have storage to record values.
	 */
	tif->tif_data = (uint8*) _TIFFmalloc(sizeof (ZIPState));
	if (tif->tif_data == NULL)
		goto bad;
	sp = ZState(tif);
	sp->stream.zalloc = NULL;
	sp->stream.zfree = NULL;
	sp->stream.opaque = NULL;
	sp->stream.data_type = Z_BINARY;

	/*
	 * Override parent get/set field methods.
	 */
	sp->vgetparent = tif->tif_tagmethods.vgetfield;
	tif->tif_tagmethods.vgetfield = ZIPVGetField; /* hook for codec tags */
	sp->vsetparent = tif->tif_tagmethods.vsetfield;
	tif->tif_tagmethods.vsetfield = ZIPVSetField; /* hook for codec tags */

	/* Default values for codec-specific fields */
	sp->zipquality = Z_DEFAULT_COMPRESSION;	/* default comp. level */
	sp->state = 0;

	/*
	 * Install codec methods.
	 */
	tif->tif_fixuptags = ZIPFixupTags; 
	tif->tif_setupdecode = ZIPSetupDecode;
	tif->tif_predecode = ZIPPreDecode;
	tif->tif_decoderow = ZIPDecode;
	tif->tif_decodestrip = ZIPDecode;
	tif->tif_decodetile = ZIPDecode;  
	tif->tif_setupencode = ZIPSetupEncode;
	tif->tif_preencode = ZIPPreEncode;
	tif->tif_postencode = ZIPPostEncode;
	tif->tif_encoderow = ZIPEncode;
	tif->tif_encodestrip = ZIPEncode;
	tif->tif_encodetile = ZIPEncode;
	tif->tif_cleanup = ZIPCleanup;
	/*
	 * Setup predictor setup.
	 */
	(void) TIFFPredictorInit(tif);
	return (1);
bad:
	TIFFErrorExt(tif->tif_clientdata, module,
		     "No space for ZIP state block");
	return (0);
}
#endif /* ZIP_SUPORT */

/* vim: set ts=8 sts=8 sw=8 noet: */
/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */

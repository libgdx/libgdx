/* $Header: /cvsroot/freeimage/FreeImage/Source/LibTIFF4/tif_error.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * Copyright (c) 1988-1997 Sam Leffler
 * Copyright (c) 1991-1997 Silicon Graphics, Inc.
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

/*
 * TIFF Library.
 */
#include "tiffiop.h"

TIFFErrorHandlerExt _TIFFerrorHandlerExt = NULL;

TIFFErrorHandler
TIFFSetErrorHandler(TIFFErrorHandler handler)
{
	TIFFErrorHandler prev = _TIFFerrorHandler;
	_TIFFerrorHandler = handler;
	return (prev);
}

TIFFErrorHandlerExt
TIFFSetErrorHandlerExt(TIFFErrorHandlerExt handler)
{
	TIFFErrorHandlerExt prev = _TIFFerrorHandlerExt;
	_TIFFerrorHandlerExt = handler;
	return (prev);
}

void
TIFFError(const char* module, const char* fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);
	if (_TIFFerrorHandler)
		(*_TIFFerrorHandler)(module, fmt, ap);
	if (_TIFFerrorHandlerExt)
		(*_TIFFerrorHandlerExt)(0, module, fmt, ap);
	va_end(ap);
}

void
TIFFErrorExt(thandle_t fd, const char* module, const char* fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);
	if (_TIFFerrorHandler)
		(*_TIFFerrorHandler)(module, fmt, ap);
	if (_TIFFerrorHandlerExt)
		(*_TIFFerrorHandlerExt)(fd, module, fmt, ap);
	va_end(ap);
}

/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */

// ==========================================================
// ZLib library interface
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
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

#include "../ZLib/zlib.h"
#include "FreeImage.h"
#include "Utilities.h"
#include "../ZLib/zutil.h"	/* must be the last header because of error C3163 in VS2008 (_vsnprintf defined in stdio.h) */

/**
Compresses a source buffer into a target buffer, using the ZLib library. 
Upon entry, target_size is the total size of the destination buffer, 
which must be at least 0.1% larger than source_size plus 12 bytes. 

@param target Destination buffer
@param target_size Size of the destination buffer, in bytes
@param source Source buffer
@param source_size Size of the source buffer, in bytes
@return Returns the actual size of the compressed buffer, returns 0 if an error occured
@see FreeImage_ZLibUncompress
*/
DWORD DLL_CALLCONV 
FreeImage_ZLibCompress(BYTE *target, DWORD target_size, BYTE *source, DWORD source_size) {
	uLongf dest_len = (uLongf)target_size;

	int zerr = compress(target, &dest_len, source, source_size);
	switch(zerr) {
		case Z_MEM_ERROR:	// not enough memory
		case Z_BUF_ERROR:	// not enough room in the output buffer
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Zlib error : %s", zError(zerr));
			return 0;
		case Z_OK:
			return dest_len;
	}

	return 0;
}

/**
Decompresses a source buffer into a target buffer, using the ZLib library. 
Upon entry, target_size is the total size of the destination buffer, 
which must be large enough to hold the entire uncompressed data. 
The size of the uncompressed data must have been saved previously by the compressor 
and transmitted to the decompressor by some mechanism outside the scope of this 
compression library.

@param target Destination buffer
@param target_size Size of the destination buffer, in bytes
@param source Source buffer
@param source_size Size of the source buffer, in bytes
@return Returns the actual size of the uncompressed buffer, returns 0 if an error occured
@see FreeImage_ZLibCompress
*/
DWORD DLL_CALLCONV 
FreeImage_ZLibUncompress(BYTE *target, DWORD target_size, BYTE *source, DWORD source_size) {
	uLongf dest_len = (uLongf)target_size;

	int zerr = uncompress(target, &dest_len, source, source_size);
	switch(zerr) {
		case Z_MEM_ERROR:	// not enough memory
		case Z_BUF_ERROR:	// not enough room in the output buffer
		case Z_DATA_ERROR:	// input data was corrupted
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Zlib error : %s", zError(zerr));
			return 0;
		case Z_OK:
			return dest_len;
	}

	return 0;
}

/**
Compresses a source buffer into a target buffer, using the ZLib library. 
On success, the target buffer contains a GZIP compatible layout.
Upon entry, target_size is the total size of the destination buffer, 
which must be at least 0.1% larger than source_size plus 24 bytes. 

@param target Destination buffer
@param target_size Size of the destination buffer, in bytes
@param source Source buffer
@param source_size Size of the source buffer, in bytes
@return Returns the actual size of the compressed buffer, returns 0 if an error occured
@see FreeImage_ZLibCompress
*/
DWORD DLL_CALLCONV 
FreeImage_ZLibGZip(BYTE *target, DWORD target_size, BYTE *source, DWORD source_size) {
	uLongf dest_len = (uLongf)target_size - 12;
	DWORD crc = crc32(0L, NULL, 0);

    // set up header (stolen from zlib/gzio.c)
    sprintf((char *)target, "%c%c%c%c%c%c%c%c", 0x1f, 0x8b,
         Z_DEFLATED, 0 /*flags*/, 0,0,0,0 /*time*/);
    int zerr = compress2(target + 8, &dest_len, source, source_size, Z_BEST_COMPRESSION);
	switch(zerr) {
		case Z_MEM_ERROR:	// not enough memory
		case Z_BUF_ERROR:	// not enough room in the output buffer
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Zlib error : %s", zError(zerr));
			return 0;
        case Z_OK: {
            // patch header, setup crc and length (stolen from mod_trace_output)
            BYTE *p = target + 8; *p++ = 2; *p = OS_CODE; // xflags, os_code
 	        crc = crc32(crc, source, source_size);
	        memcpy(target + 4 + dest_len, &crc, 4);
	        memcpy(target + 8 + dest_len, &source_size, 4);
            return dest_len + 12;
        }
	}
	return 0;
}

/**
Decompresses a gzipped source buffer into a target buffer, using the ZLib library. 
Upon entry, target_size is the total size of the destination buffer, 
which must be large enough to hold the entire uncompressed data. 
The size of the uncompressed data must have been saved previously by the compressor 
and transmitted to the decompressor by some mechanism outside the scope of this 
compression library.

@param target Destination buffer
@param target_size Size of the destination buffer, in bytes
@param source Source buffer
@param source_size Size of the source buffer, in bytes
@return Returns the actual size of the uncompressed buffer, returns 0 if an error occured
@see FreeImage_ZLibGZip
*/

static int get_byte(z_stream *stream) {
    if (stream->avail_in <= 0) return EOF;
    stream->avail_in--;
    return *(stream->next_in)++;
}

static int checkheader(z_stream *stream) {
    int flags, c;
    DWORD len;

    if (get_byte(stream) != 0x1f || get_byte(stream) != 0x8b)
        return Z_DATA_ERROR;
    if (get_byte(stream) != Z_DEFLATED || ((flags = get_byte(stream)) & 0xE0) != 0)
        return Z_DATA_ERROR;
    for (len = 0; len < 6; len++) (void)get_byte(stream);

    if ((flags & 0x04) != 0) { /* skip the extra field */
        len  =  (DWORD)get_byte(stream);
        len += ((DWORD)get_byte(stream)) << 8;
        /* len is garbage if EOF but the loop below will quit anyway */
        while (len-- != 0 && get_byte(stream) != EOF) ;
    }
    if ((flags & 0x08) != 0) { /* skip the original file name */
        while ((c = get_byte(stream)) != 0 && c != EOF) ;
    }
    if ((flags & 0x10) != 0) {   /* skip the .gz file comment */
        while ((c = get_byte(stream)) != 0 && c != EOF) ;
    }
    if ((flags & 0x02) != 0) {  /* skip the header crc */
        for (len = 0; len < 2; len++) (void)get_byte(stream);
    }
    return Z_OK;
}

DWORD DLL_CALLCONV 
FreeImage_ZLibGUnzip(BYTE *target, DWORD target_size, BYTE *source, DWORD source_size) {
    DWORD src_len  = source_size;
    DWORD dest_len = target_size;
    int   zerr     = Z_DATA_ERROR;

    if (src_len > 0) {
        z_stream stream;
        memset(&stream, 0, sizeof (stream));
        if ((zerr = inflateInit2(&stream, -MAX_WBITS)) == Z_OK) {
            stream.next_in  = source;
            stream.avail_in = source_size;

            stream.next_out  = target;
            stream.avail_out = target_size;

            if ((zerr = checkheader(&stream)) == Z_OK) {
                zerr = inflate (&stream, Z_NO_FLUSH);
                dest_len = target_size - stream.avail_out;

                if (zerr == Z_OK || zerr == Z_STREAM_END)
                    inflateEnd(&stream);
            } 
        }
    }
    if (zerr != Z_OK && zerr != Z_STREAM_END) {
        FreeImage_OutputMessageProc(FIF_UNKNOWN, "Zlib error : %s", zError(zerr));
        return 0;
    }
    return dest_len;
}

/**
Update a running crc from source and return the updated crc, using the ZLib library.
If source is NULL, this function returns the required initial value for the crc.

@param crc Running crc value
@param source Source buffer
@param source_size Size of the source buffer, in bytes
@return Returns the new crc value
*/
DWORD DLL_CALLCONV 
FreeImage_ZLibCRC32(DWORD crc, BYTE *source, DWORD source_size) {

    return crc32(crc, source, source_size);
}

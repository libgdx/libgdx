/* $Id: tif_dirwrite.c,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

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
 *
 * Directory Write Support Routines.
 */
#include "tiffiop.h"

#ifdef HAVE_IEEEFP
#define TIFFCvtNativeToIEEEFloat(tif, n, fp)
#define TIFFCvtNativeToIEEEDouble(tif, n, dp)
#else
extern void TIFFCvtNativeToIEEEFloat(TIFF* tif, uint32 n, float* fp);
extern void TIFFCvtNativeToIEEEDouble(TIFF* tif, uint32 n, double* dp);
#endif

static int TIFFWriteDirectorySec(TIFF* tif, int isimage, int imagedone, uint64* pdiroff);

static int TIFFWriteDirectoryTagSampleformatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value);
#if 0
static int TIFFWriteDirectoryTagSampleformatPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
#endif

static int TIFFWriteDirectoryTagAscii(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, char* value);
static int TIFFWriteDirectoryTagUndefinedArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value);
#ifdef notdef
static int TIFFWriteDirectoryTagByte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value);
#endif
static int TIFFWriteDirectoryTagByteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value);
#if 0
static int TIFFWriteDirectoryTagBytePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value);
#endif
#ifdef notdef
static int TIFFWriteDirectoryTagSbyte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value);
#endif
static int TIFFWriteDirectoryTagSbyteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int8* value);
#if 0
static int TIFFWriteDirectoryTagSbytePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value);
#endif
static int TIFFWriteDirectoryTagShort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value);
static int TIFFWriteDirectoryTagShortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint16* value);
static int TIFFWriteDirectoryTagShortPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value);
#ifdef notdef
static int TIFFWriteDirectoryTagSshort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value);
#endif
static int TIFFWriteDirectoryTagSshortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int16* value);
#if 0
static int TIFFWriteDirectoryTagSshortPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value);
#endif
static int TIFFWriteDirectoryTagLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value);
static int TIFFWriteDirectoryTagLongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value);
#if 0
static int TIFFWriteDirectoryTagLongPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value);
#endif
#ifdef notdef
static int TIFFWriteDirectoryTagSlong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value);
#endif
static int TIFFWriteDirectoryTagSlongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int32* value);
#if 0
static int TIFFWriteDirectoryTagSlongPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value);
#endif
#ifdef notdef
static int TIFFWriteDirectoryTagLong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint64 value);
#endif
static int TIFFWriteDirectoryTagLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
#ifdef notdef
static int TIFFWriteDirectoryTagSlong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int64 value);
#endif
static int TIFFWriteDirectoryTagSlong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int64* value);
static int TIFFWriteDirectoryTagRational(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
static int TIFFWriteDirectoryTagRationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
static int TIFFWriteDirectoryTagSrationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
#ifdef notdef
static int TIFFWriteDirectoryTagFloat(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value);
#endif
static int TIFFWriteDirectoryTagFloatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
#if 0
static int TIFFWriteDirectoryTagFloatPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value);
#endif
#ifdef notdef
static int TIFFWriteDirectoryTagDouble(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
#endif
static int TIFFWriteDirectoryTagDoubleArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value);
#if 0
static int TIFFWriteDirectoryTagDoublePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
#endif
static int TIFFWriteDirectoryTagIfdArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value);
#ifdef notdef
static int TIFFWriteDirectoryTagIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
#endif
static int TIFFWriteDirectoryTagShortLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value);
static int TIFFWriteDirectoryTagLongLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
static int TIFFWriteDirectoryTagIfdIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
#ifdef notdef
static int TIFFWriteDirectoryTagShortLongLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
#endif
static int TIFFWriteDirectoryTagColormap(TIFF* tif, uint32* ndir, TIFFDirEntry* dir);
static int TIFFWriteDirectoryTagTransferfunction(TIFF* tif, uint32* ndir, TIFFDirEntry* dir);
static int TIFFWriteDirectoryTagSubifd(TIFF* tif, uint32* ndir, TIFFDirEntry* dir);

static int TIFFWriteDirectoryTagCheckedAscii(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, char* value);
static int TIFFWriteDirectoryTagCheckedUndefinedArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedByte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value);
#endif
static int TIFFWriteDirectoryTagCheckedByteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedSbyte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value);
#endif
static int TIFFWriteDirectoryTagCheckedSbyteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int8* value);
static int TIFFWriteDirectoryTagCheckedShort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value);
static int TIFFWriteDirectoryTagCheckedShortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint16* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedSshort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value);
#endif
static int TIFFWriteDirectoryTagCheckedSshortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int16* value);
static int TIFFWriteDirectoryTagCheckedLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value);
static int TIFFWriteDirectoryTagCheckedLongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedSlong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value);
#endif
static int TIFFWriteDirectoryTagCheckedSlongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int32* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedLong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint64 value);
#endif
static int TIFFWriteDirectoryTagCheckedLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedSlong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int64 value);
#endif
static int TIFFWriteDirectoryTagCheckedSlong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int64* value);
static int TIFFWriteDirectoryTagCheckedRational(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
static int TIFFWriteDirectoryTagCheckedRationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
static int TIFFWriteDirectoryTagCheckedSrationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedFloat(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value);
#endif
static int TIFFWriteDirectoryTagCheckedFloatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value);
#ifdef notdef
static int TIFFWriteDirectoryTagCheckedDouble(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value);
#endif
static int TIFFWriteDirectoryTagCheckedDoubleArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value);
static int TIFFWriteDirectoryTagCheckedIfdArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value);
static int TIFFWriteDirectoryTagCheckedIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value);

static int TIFFWriteDirectoryTagData(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 datatype, uint32 count, uint32 datalength, void* data);

static int TIFFLinkDirectory(TIFF*);

/*
 * Write the contents of the current directory
 * to the specified file.  This routine doesn't
 * handle overwriting a directory with auxiliary
 * storage that's been changed.
 */
int
TIFFWriteDirectory(TIFF* tif)
{
	return TIFFWriteDirectorySec(tif,TRUE,TRUE,NULL);
}

/*
 * Similar to TIFFWriteDirectory(), writes the directory out
 * but leaves all data structures in memory so that it can be
 * written again.  This will make a partially written TIFF file
 * readable before it is successfully completed/closed.
 */
int
TIFFCheckpointDirectory(TIFF* tif)
{
	int rc;
	/* Setup the strips arrays, if they haven't already been. */
	if (tif->tif_dir.td_stripoffset == NULL)
	    (void) TIFFSetupStrips(tif);
	rc = TIFFWriteDirectorySec(tif,TRUE,FALSE,NULL);
	(void) TIFFSetWriteOffset(tif, TIFFSeekFile(tif, 0, SEEK_END));
	return rc;
}

int
TIFFWriteCustomDirectory(TIFF* tif, uint64* pdiroff)
{
	return TIFFWriteDirectorySec(tif,FALSE,FALSE,pdiroff);
}

/*
 * Similar to TIFFWriteDirectory(), but if the directory has already
 * been written once, it is relocated to the end of the file, in case it
 * has changed in size.  Note that this will result in the loss of the
 * previously used directory space. 
 */ 
int
TIFFRewriteDirectory( TIFF *tif )
{
	static const char module[] = "TIFFRewriteDirectory";

	/* We don't need to do anything special if it hasn't been written. */
	if( tif->tif_diroff == 0 )
		return TIFFWriteDirectory( tif );

	/*
	 * Find and zero the pointer to this directory, so that TIFFLinkDirectory
	 * will cause it to be added after this directories current pre-link.
	 */

	if (!(tif->tif_flags&TIFF_BIGTIFF))
	{
		if (tif->tif_header.classic.tiff_diroff == tif->tif_diroff)
		{
			tif->tif_header.classic.tiff_diroff = 0;
			tif->tif_diroff = 0;

			TIFFSeekFile(tif,4,SEEK_SET);
			if (!WriteOK(tif, &(tif->tif_header.classic.tiff_diroff),4))
			{
				TIFFErrorExt(tif->tif_clientdata, tif->tif_name,
				    "Error updating TIFF header");
				return (0);
			}
		}
		else
		{
			uint32 nextdir;
			nextdir = tif->tif_header.classic.tiff_diroff;
			while(1) {
				uint16 dircount;
				uint32 nextnextdir;

				if (!SeekOK(tif, nextdir) ||
				    !ReadOK(tif, &dircount, 2)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory count");
					return (0);
				}
				if (tif->tif_flags & TIFF_SWAB)
					TIFFSwabShort(&dircount);
				(void) TIFFSeekFile(tif,
				    nextdir+2+dircount*12, SEEK_SET);
				if (!ReadOK(tif, &nextnextdir, 4)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory link");
					return (0);
				}
				if (tif->tif_flags & TIFF_SWAB)
					TIFFSwabLong(&nextnextdir);
				if (nextnextdir==tif->tif_diroff)
				{
					uint32 m;
					m=0;
					(void) TIFFSeekFile(tif,
					    nextdir+2+dircount*12, SEEK_SET);
					if (!WriteOK(tif, &m, 4)) {
						TIFFErrorExt(tif->tif_clientdata, module,
						     "Error writing directory link");
						return (0);
					}
					tif->tif_diroff=0;
					break;
				}
				nextdir=nextnextdir;
			}
		}
	}
	else
	{
		if (tif->tif_header.big.tiff_diroff == tif->tif_diroff)
		{
			tif->tif_header.big.tiff_diroff = 0;
			tif->tif_diroff = 0;

			TIFFSeekFile(tif,8,SEEK_SET);
			if (!WriteOK(tif, &(tif->tif_header.big.tiff_diroff),8))
			{
				TIFFErrorExt(tif->tif_clientdata, tif->tif_name,
				    "Error updating TIFF header");
				return (0);
			}
		}
		else
		{
			uint64 nextdir;
			nextdir = tif->tif_header.big.tiff_diroff;
			while(1) {
				uint64 dircount64;
				uint16 dircount;
				uint64 nextnextdir;

				if (!SeekOK(tif, nextdir) ||
				    !ReadOK(tif, &dircount64, 8)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory count");
					return (0);
				}
				if (tif->tif_flags & TIFF_SWAB)
					TIFFSwabLong8(&dircount64);
				if (dircount64>0xFFFF)
				{
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Sanity check on tag count failed, likely corrupt TIFF");
					return (0);
				}
				dircount=(uint16)dircount64;
				(void) TIFFSeekFile(tif,
				    nextdir+8+dircount*20, SEEK_SET);
				if (!ReadOK(tif, &nextnextdir, 8)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory link");
					return (0);
				}
				if (tif->tif_flags & TIFF_SWAB)
					TIFFSwabLong8(&nextnextdir);
				if (nextnextdir==tif->tif_diroff)
				{
					uint64 m;
					m=0;
					(void) TIFFSeekFile(tif,
					    nextdir+8+dircount*20, SEEK_SET);
					if (!WriteOK(tif, &m, 8)) {
						TIFFErrorExt(tif->tif_clientdata, module,
						     "Error writing directory link");
						return (0);
					}
					tif->tif_diroff=0;
					break;
				}
				nextdir=nextnextdir;
			}
		}
	}

	/*
	 * Now use TIFFWriteDirectory() normally.
	 */

	return TIFFWriteDirectory( tif );
}

static int
TIFFWriteDirectorySec(TIFF* tif, int isimage, int imagedone, uint64* pdiroff)
{
	static const char module[] = "TIFFWriteDirectorySec";
	uint32 ndir;
	TIFFDirEntry* dir;
	uint32 dirsize;
	void* dirmem;
	uint32 m;
	if (tif->tif_mode == O_RDONLY)
		return (1);

        _TIFFFillStriles( tif );
        
	/*
	 * Clear write state so that subsequent images with
	 * different characteristics get the right buffers
	 * setup for them.
	 */
	if (imagedone)
	{
		if (tif->tif_flags & TIFF_POSTENCODE)
		{
			tif->tif_flags &= ~TIFF_POSTENCODE;
			if (!(*tif->tif_postencode)(tif))
			{
				TIFFErrorExt(tif->tif_clientdata,module,
				    "Error post-encoding before directory write");
				return (0);
			}
		}
		(*tif->tif_close)(tif);       /* shutdown encoder */
		/*
		 * Flush any data that might have been written
		 * by the compression close+cleanup routines.  But
                 * be careful not to write stuff if we didn't add data
                 * in the previous steps as the "rawcc" data may well be
                 * a previously read tile/strip in mixed read/write mode.
		 */
		if (tif->tif_rawcc > 0 
		    && (tif->tif_flags & TIFF_BEENWRITING) != 0 )
		{
		    if( !TIFFFlushData1(tif) )
                    {
			TIFFErrorExt(tif->tif_clientdata, module,
			    "Error flushing data before directory write");
			return (0);
                    }
		}
		if ((tif->tif_flags & TIFF_MYBUFFER) && tif->tif_rawdata)
		{
			_TIFFfree(tif->tif_rawdata);
			tif->tif_rawdata = NULL;
			tif->tif_rawcc = 0;
			tif->tif_rawdatasize = 0;
                        tif->tif_rawdataoff = 0;
                        tif->tif_rawdataloaded = 0;
		}
		tif->tif_flags &= ~(TIFF_BEENWRITING|TIFF_BUFFERSETUP);
	}
	dir=NULL;
	dirmem=NULL;
	dirsize=0;
	while (1)
	{
		ndir=0;
		if (isimage)
		{
			if (TIFFFieldSet(tif,FIELD_IMAGEDIMENSIONS))
			{
				if (!TIFFWriteDirectoryTagShortLong(tif,&ndir,dir,TIFFTAG_IMAGEWIDTH,tif->tif_dir.td_imagewidth))
					goto bad;
				if (!TIFFWriteDirectoryTagShortLong(tif,&ndir,dir,TIFFTAG_IMAGELENGTH,tif->tif_dir.td_imagelength))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_TILEDIMENSIONS))
			{
				if (!TIFFWriteDirectoryTagShortLong(tif,&ndir,dir,TIFFTAG_TILEWIDTH,tif->tif_dir.td_tilewidth))
					goto bad;
				if (!TIFFWriteDirectoryTagShortLong(tif,&ndir,dir,TIFFTAG_TILELENGTH,tif->tif_dir.td_tilelength))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_RESOLUTION))
			{
				if (!TIFFWriteDirectoryTagRational(tif,&ndir,dir,TIFFTAG_XRESOLUTION,tif->tif_dir.td_xresolution))
					goto bad;
				if (!TIFFWriteDirectoryTagRational(tif,&ndir,dir,TIFFTAG_YRESOLUTION,tif->tif_dir.td_yresolution))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_POSITION))
			{
				if (!TIFFWriteDirectoryTagRational(tif,&ndir,dir,TIFFTAG_XPOSITION,tif->tif_dir.td_xposition))
					goto bad;
				if (!TIFFWriteDirectoryTagRational(tif,&ndir,dir,TIFFTAG_YPOSITION,tif->tif_dir.td_yposition))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_SUBFILETYPE))
			{
				if (!TIFFWriteDirectoryTagLong(tif,&ndir,dir,TIFFTAG_SUBFILETYPE,tif->tif_dir.td_subfiletype))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_BITSPERSAMPLE))
			{
				if (!TIFFWriteDirectoryTagShortPerSample(tif,&ndir,dir,TIFFTAG_BITSPERSAMPLE,tif->tif_dir.td_bitspersample))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_COMPRESSION))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_COMPRESSION,tif->tif_dir.td_compression))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_PHOTOMETRIC))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_PHOTOMETRIC,tif->tif_dir.td_photometric))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_THRESHHOLDING))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_THRESHHOLDING,tif->tif_dir.td_threshholding))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_FILLORDER))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_FILLORDER,tif->tif_dir.td_fillorder))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_ORIENTATION))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_ORIENTATION,tif->tif_dir.td_orientation))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_SAMPLESPERPIXEL))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_SAMPLESPERPIXEL,tif->tif_dir.td_samplesperpixel))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_ROWSPERSTRIP))
			{
				if (!TIFFWriteDirectoryTagShortLong(tif,&ndir,dir,TIFFTAG_ROWSPERSTRIP,tif->tif_dir.td_rowsperstrip))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_MINSAMPLEVALUE))
			{
				if (!TIFFWriteDirectoryTagShortPerSample(tif,&ndir,dir,TIFFTAG_MINSAMPLEVALUE,tif->tif_dir.td_minsamplevalue))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_MAXSAMPLEVALUE))
			{
				if (!TIFFWriteDirectoryTagShortPerSample(tif,&ndir,dir,TIFFTAG_MAXSAMPLEVALUE,tif->tif_dir.td_maxsamplevalue))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_PLANARCONFIG))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_PLANARCONFIG,tif->tif_dir.td_planarconfig))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_RESOLUTIONUNIT))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_RESOLUTIONUNIT,tif->tif_dir.td_resolutionunit))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_PAGENUMBER))
			{
				if (!TIFFWriteDirectoryTagShortArray(tif,&ndir,dir,TIFFTAG_PAGENUMBER,2,&tif->tif_dir.td_pagenumber[0]))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_STRIPBYTECOUNTS))
			{
				if (!isTiled(tif))
				{
					if (!TIFFWriteDirectoryTagLongLong8Array(tif,&ndir,dir,TIFFTAG_STRIPBYTECOUNTS,tif->tif_dir.td_nstrips,tif->tif_dir.td_stripbytecount))
						goto bad;
				}
				else
				{
					if (!TIFFWriteDirectoryTagLongLong8Array(tif,&ndir,dir,TIFFTAG_TILEBYTECOUNTS,tif->tif_dir.td_nstrips,tif->tif_dir.td_stripbytecount))
						goto bad;
				}
			}
			if (TIFFFieldSet(tif,FIELD_STRIPOFFSETS))
			{
				if (!isTiled(tif))
				{
					if (!TIFFWriteDirectoryTagLongLong8Array(tif,&ndir,dir,TIFFTAG_STRIPOFFSETS,tif->tif_dir.td_nstrips,tif->tif_dir.td_stripoffset))
						goto bad;
				}
				else
				{
					if (!TIFFWriteDirectoryTagLongLong8Array(tif,&ndir,dir,TIFFTAG_TILEOFFSETS,tif->tif_dir.td_nstrips,tif->tif_dir.td_stripoffset))
						goto bad;
				}
			}
			if (TIFFFieldSet(tif,FIELD_COLORMAP))
			{
				if (!TIFFWriteDirectoryTagColormap(tif,&ndir,dir))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_EXTRASAMPLES))
			{
				if (tif->tif_dir.td_extrasamples)
				{
					uint16 na;
					uint16* nb;
					TIFFGetFieldDefaulted(tif,TIFFTAG_EXTRASAMPLES,&na,&nb);
					if (!TIFFWriteDirectoryTagShortArray(tif,&ndir,dir,TIFFTAG_EXTRASAMPLES,na,nb))
						goto bad;
				}
			}
			if (TIFFFieldSet(tif,FIELD_SAMPLEFORMAT))
			{
				if (!TIFFWriteDirectoryTagShortPerSample(tif,&ndir,dir,TIFFTAG_SAMPLEFORMAT,tif->tif_dir.td_sampleformat))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_SMINSAMPLEVALUE))
			{
				if (!TIFFWriteDirectoryTagSampleformatArray(tif,&ndir,dir,TIFFTAG_SMINSAMPLEVALUE,tif->tif_dir.td_samplesperpixel,tif->tif_dir.td_sminsamplevalue))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_SMAXSAMPLEVALUE))
			{
				if (!TIFFWriteDirectoryTagSampleformatArray(tif,&ndir,dir,TIFFTAG_SMAXSAMPLEVALUE,tif->tif_dir.td_samplesperpixel,tif->tif_dir.td_smaxsamplevalue))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_IMAGEDEPTH))
			{
				if (!TIFFWriteDirectoryTagLong(tif,&ndir,dir,TIFFTAG_IMAGEDEPTH,tif->tif_dir.td_imagedepth))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_TILEDEPTH))
			{
				if (!TIFFWriteDirectoryTagLong(tif,&ndir,dir,TIFFTAG_TILEDEPTH,tif->tif_dir.td_tiledepth))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_HALFTONEHINTS))
			{
				if (!TIFFWriteDirectoryTagShortArray(tif,&ndir,dir,TIFFTAG_HALFTONEHINTS,2,&tif->tif_dir.td_halftonehints[0]))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_YCBCRSUBSAMPLING))
			{
				if (!TIFFWriteDirectoryTagShortArray(tif,&ndir,dir,TIFFTAG_YCBCRSUBSAMPLING,2,&tif->tif_dir.td_ycbcrsubsampling[0]))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_YCBCRPOSITIONING))
			{
				if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,TIFFTAG_YCBCRPOSITIONING,tif->tif_dir.td_ycbcrpositioning))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_REFBLACKWHITE))
			{
				if (!TIFFWriteDirectoryTagRationalArray(tif,&ndir,dir,TIFFTAG_REFERENCEBLACKWHITE,6,tif->tif_dir.td_refblackwhite))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_TRANSFERFUNCTION))
			{
				if (!TIFFWriteDirectoryTagTransferfunction(tif,&ndir,dir))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_INKNAMES))
			{
				if (!TIFFWriteDirectoryTagAscii(tif,&ndir,dir,TIFFTAG_INKNAMES,tif->tif_dir.td_inknameslen,tif->tif_dir.td_inknames))
					goto bad;
			}
			if (TIFFFieldSet(tif,FIELD_SUBIFD))
			{
				if (!TIFFWriteDirectoryTagSubifd(tif,&ndir,dir))
					goto bad;
			}
			{
				uint32 n;
				for (n=0; n<tif->tif_nfields; n++) {
					const TIFFField* o;
					o = tif->tif_fields[n];
					if ((o->field_bit>=FIELD_CODEC)&&(TIFFFieldSet(tif,o->field_bit)))
					{
						switch (o->get_field_type)
						{
							case TIFF_SETGET_ASCII:
								{
									uint32 pa;
									char* pb;
									assert(o->field_type==TIFF_ASCII);
									assert(o->field_readcount==TIFF_VARIABLE);
									assert(o->field_passcount==0);
									TIFFGetField(tif,o->field_tag,&pb);
									pa=(uint32)(strlen(pb));
									if (!TIFFWriteDirectoryTagAscii(tif,&ndir,dir,o->field_tag,pa,pb))
										goto bad;
								}
								break;
							case TIFF_SETGET_UINT16:
								{
									uint16 p;
									assert(o->field_type==TIFF_SHORT);
									assert(o->field_readcount==1);
									assert(o->field_passcount==0);
									TIFFGetField(tif,o->field_tag,&p);
									if (!TIFFWriteDirectoryTagShort(tif,&ndir,dir,o->field_tag,p))
										goto bad;
								}
								break;
							case TIFF_SETGET_UINT32:
								{
									uint32 p;
									assert(o->field_type==TIFF_LONG);
									assert(o->field_readcount==1);
									assert(o->field_passcount==0);
									TIFFGetField(tif,o->field_tag,&p);
									if (!TIFFWriteDirectoryTagLong(tif,&ndir,dir,o->field_tag,p))
										goto bad;
								}
								break;
							case TIFF_SETGET_C32_UINT8:
								{
									uint32 pa;
									void* pb;
									assert(o->field_type==TIFF_UNDEFINED);
									assert(o->field_readcount==TIFF_VARIABLE2);
									assert(o->field_passcount==1);
									TIFFGetField(tif,o->field_tag,&pa,&pb);
									if (!TIFFWriteDirectoryTagUndefinedArray(tif,&ndir,dir,o->field_tag,pa,pb))
										goto bad;
								}
								break;
							default:
								assert(0);   /* we should never get here */
								break;
						}
					}
				}
			}
		}
		for (m=0; m<(uint32)(tif->tif_dir.td_customValueCount); m++)
		{
			switch (tif->tif_dir.td_customValues[m].info->field_type)
			{
				case TIFF_ASCII:
					if (!TIFFWriteDirectoryTagAscii(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_UNDEFINED:
					if (!TIFFWriteDirectoryTagUndefinedArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_BYTE:
					if (!TIFFWriteDirectoryTagByteArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SBYTE:
					if (!TIFFWriteDirectoryTagSbyteArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SHORT:
					if (!TIFFWriteDirectoryTagShortArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SSHORT:
					if (!TIFFWriteDirectoryTagSshortArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_LONG:
					if (!TIFFWriteDirectoryTagLongArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SLONG:
					if (!TIFFWriteDirectoryTagSlongArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_LONG8:
					if (!TIFFWriteDirectoryTagLong8Array(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SLONG8:
					if (!TIFFWriteDirectoryTagSlong8Array(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_RATIONAL:
					if (!TIFFWriteDirectoryTagRationalArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_SRATIONAL:
					if (!TIFFWriteDirectoryTagSrationalArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_FLOAT:
					if (!TIFFWriteDirectoryTagFloatArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_DOUBLE:
					if (!TIFFWriteDirectoryTagDoubleArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_IFD:
					if (!TIFFWriteDirectoryTagIfdArray(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				case TIFF_IFD8:
					if (!TIFFWriteDirectoryTagIfdIfd8Array(tif,&ndir,dir,tif->tif_dir.td_customValues[m].info->field_tag,tif->tif_dir.td_customValues[m].count,tif->tif_dir.td_customValues[m].value))
						goto bad;
					break;
				default:
					assert(0);   /* we should never get here */
					break;
			}
		}
		if (dir!=NULL)
			break;
		dir=_TIFFmalloc(ndir*sizeof(TIFFDirEntry));
		if (dir==NULL)
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
			goto bad;
		}
		if (isimage)
		{
			if ((tif->tif_diroff==0)&&(!TIFFLinkDirectory(tif)))
				goto bad;
		}
		else
			tif->tif_diroff=(TIFFSeekFile(tif,0,SEEK_END)+1)&(~1);
		if (pdiroff!=NULL)
			*pdiroff=tif->tif_diroff;
		if (!(tif->tif_flags&TIFF_BIGTIFF))
			dirsize=2+ndir*12+4;
		else
			dirsize=8+ndir*20+8;
		tif->tif_dataoff=tif->tif_diroff+dirsize;
		if (!(tif->tif_flags&TIFF_BIGTIFF))
			tif->tif_dataoff=(uint32)tif->tif_dataoff;
		if ((tif->tif_dataoff<tif->tif_diroff)||(tif->tif_dataoff<(uint64)dirsize))
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Maximum TIFF file size exceeded");
			goto bad;
		}
		if (tif->tif_dataoff&1)
			tif->tif_dataoff++;
		if (isimage)
			tif->tif_curdir++;
	}
	if (isimage)
	{
		if (TIFFFieldSet(tif,FIELD_SUBIFD)&&(tif->tif_subifdoff==0))
		{
			uint32 na;
			TIFFDirEntry* nb;
			for (na=0, nb=dir; ; na++, nb++)
			{
				assert(na<ndir);
				if (nb->tdir_tag==TIFFTAG_SUBIFD)
					break;
			}
			if (!(tif->tif_flags&TIFF_BIGTIFF))
				tif->tif_subifdoff=tif->tif_diroff+2+na*12+8;
			else
				tif->tif_subifdoff=tif->tif_diroff+8+na*20+12;
		}
	}
	dirmem=_TIFFmalloc(dirsize);
	if (dirmem==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		goto bad;
	}
	if (!(tif->tif_flags&TIFF_BIGTIFF))
	{
		uint8* n;
		uint32 nTmp;
		TIFFDirEntry* o;
		n=dirmem;
		*(uint16*)n=ndir;
		if (tif->tif_flags&TIFF_SWAB)
			TIFFSwabShort((uint16*)n);
		n+=2;
		o=dir;
		for (m=0; m<ndir; m++)
		{
			*(uint16*)n=o->tdir_tag;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabShort((uint16*)n);
			n+=2;
			*(uint16*)n=o->tdir_type;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabShort((uint16*)n);
			n+=2;
			nTmp = (uint32)o->tdir_count;
			_TIFFmemcpy(n,&nTmp,4);
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabLong((uint32*)n);
			n+=4;
			/* This is correct. The data has been */
			/* swabbed previously in TIFFWriteDirectoryTagData */
			_TIFFmemcpy(n,&o->tdir_offset,4);
			n+=4;
			o++;
		}
		nTmp = (uint32)tif->tif_nextdiroff;
		if (tif->tif_flags&TIFF_SWAB)
			TIFFSwabLong(&nTmp);
		_TIFFmemcpy(n,&nTmp,4);
	}
	else
	{
		uint8* n;
		TIFFDirEntry* o;
		n=dirmem;
		*(uint64*)n=ndir;
		if (tif->tif_flags&TIFF_SWAB)
			TIFFSwabLong8((uint64*)n);
		n+=8;
		o=dir;
		for (m=0; m<ndir; m++)
		{
			*(uint16*)n=o->tdir_tag;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabShort((uint16*)n);
			n+=2;
			*(uint16*)n=o->tdir_type;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabShort((uint16*)n);
			n+=2;
			_TIFFmemcpy(n,&o->tdir_count,8);
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabLong8((uint64*)n);
			n+=8;
			_TIFFmemcpy(n,&o->tdir_offset,8);
			n+=8;
			o++;
		}
		_TIFFmemcpy(n,&tif->tif_nextdiroff,8);
		if (tif->tif_flags&TIFF_SWAB)
			TIFFSwabLong8((uint64*)n);
	}
	_TIFFfree(dir);
	dir=NULL;
	if (!SeekOK(tif,tif->tif_diroff))
	{
		TIFFErrorExt(tif->tif_clientdata,module,"IO error writing directory");
		goto bad;
	}
	if (!WriteOK(tif,dirmem,(tmsize_t)dirsize))
	{
		TIFFErrorExt(tif->tif_clientdata,module,"IO error writing directory");
		goto bad;
	}
	_TIFFfree(dirmem);
	if (imagedone)
	{
		TIFFFreeDirectory(tif);
		tif->tif_flags &= ~TIFF_DIRTYDIRECT;
		tif->tif_flags &= ~TIFF_DIRTYSTRIP;
		(*tif->tif_cleanup)(tif);
		/*
		* Reset directory-related state for subsequent
		* directories.
		*/
		TIFFCreateDirectory(tif);
	}
	return(1);
bad:
	if (dir!=NULL)
		_TIFFfree(dir);
	if (dirmem!=NULL)
		_TIFFfree(dirmem);
	return(0);
}

static int
TIFFWriteDirectoryTagSampleformatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value)
{
	static const char module[] = "TIFFWriteDirectoryTagSampleformatArray";
	void* conv;
	uint32 i;
	int ok;
	conv = _TIFFmalloc(count*sizeof(double));
	if (conv == NULL)
	{
		TIFFErrorExt(tif->tif_clientdata, module, "Out of memory");
		return (0);
	}

	switch (tif->tif_dir.td_sampleformat)
	{
		case SAMPLEFORMAT_IEEEFP:
			if (tif->tif_dir.td_bitspersample<=32)
			{
				for (i = 0; i < count; ++i)
					((float*)conv)[i] = (float)value[i];
				ok = TIFFWriteDirectoryTagFloatArray(tif,ndir,dir,tag,count,(float*)conv);
			}
			else
			{
				ok = TIFFWriteDirectoryTagDoubleArray(tif,ndir,dir,tag,count,value);
			}
			break;
		case SAMPLEFORMAT_INT:
			if (tif->tif_dir.td_bitspersample<=8)
			{
				for (i = 0; i < count; ++i)
					((int8*)conv)[i] = (int8)value[i];
				ok = TIFFWriteDirectoryTagSbyteArray(tif,ndir,dir,tag,count,(int8*)conv);
			}
			else if (tif->tif_dir.td_bitspersample<=16)
			{
				for (i = 0; i < count; ++i)
					((int16*)conv)[i] = (int16)value[i];
				ok = TIFFWriteDirectoryTagSshortArray(tif,ndir,dir,tag,count,(int16*)conv);
			}
			else
			{
				for (i = 0; i < count; ++i)
					((int32*)conv)[i] = (int32)value[i];
				ok = TIFFWriteDirectoryTagSlongArray(tif,ndir,dir,tag,count,(int32*)conv);
			}
			break;
		case SAMPLEFORMAT_UINT:
			if (tif->tif_dir.td_bitspersample<=8)
			{
				for (i = 0; i < count; ++i)
					((uint8*)conv)[i] = (uint8)value[i];
				ok = TIFFWriteDirectoryTagByteArray(tif,ndir,dir,tag,count,(uint8*)conv);
			}
			else if (tif->tif_dir.td_bitspersample<=16)
			{
				for (i = 0; i < count; ++i)
					((uint16*)conv)[i] = (uint16)value[i];
				ok = TIFFWriteDirectoryTagShortArray(tif,ndir,dir,tag,count,(uint16*)conv);
			}
			else
			{
				for (i = 0; i < count; ++i)
					((uint32*)conv)[i] = (uint32)value[i];
				ok = TIFFWriteDirectoryTagLongArray(tif,ndir,dir,tag,count,(uint32*)conv);
			}
			break;
		default:
			ok = 0;
	}

	_TIFFfree(conv);
	return (ok);
}

#if 0
static int
TIFFWriteDirectoryTagSampleformatPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	switch (tif->tif_dir.td_sampleformat)
	{
		case SAMPLEFORMAT_IEEEFP:
			if (tif->tif_dir.td_bitspersample<=32)
				return(TIFFWriteDirectoryTagFloatPerSample(tif,ndir,dir,tag,(float)value));
			else
				return(TIFFWriteDirectoryTagDoublePerSample(tif,ndir,dir,tag,value));
		case SAMPLEFORMAT_INT:
			if (tif->tif_dir.td_bitspersample<=8)
				return(TIFFWriteDirectoryTagSbytePerSample(tif,ndir,dir,tag,(int8)value));
			else if (tif->tif_dir.td_bitspersample<=16)
				return(TIFFWriteDirectoryTagSshortPerSample(tif,ndir,dir,tag,(int16)value));
			else
				return(TIFFWriteDirectoryTagSlongPerSample(tif,ndir,dir,tag,(int32)value));
		case SAMPLEFORMAT_UINT:
			if (tif->tif_dir.td_bitspersample<=8)
				return(TIFFWriteDirectoryTagBytePerSample(tif,ndir,dir,tag,(uint8)value));
			else if (tif->tif_dir.td_bitspersample<=16)
				return(TIFFWriteDirectoryTagShortPerSample(tif,ndir,dir,tag,(uint16)value));
			else
				return(TIFFWriteDirectoryTagLongPerSample(tif,ndir,dir,tag,(uint32)value));
		default:
			return(1);
	}
}
#endif

static int
TIFFWriteDirectoryTagAscii(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, char* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedAscii(tif,ndir,dir,tag,count,value));
}

static int
TIFFWriteDirectoryTagUndefinedArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedUndefinedArray(tif,ndir,dir,tag,count,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagByte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedByte(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagByteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedByteArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int
TIFFWriteDirectoryTagBytePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value)
{
	static const char module[] = "TIFFWriteDirectoryTagBytePerSample";
	uint8* m;
	uint8* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(uint8));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedByteArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

#ifdef notdef
static int
TIFFWriteDirectoryTagSbyte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSbyte(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagSbyteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int8* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSbyteArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int
TIFFWriteDirectoryTagSbytePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value)
{
	static const char module[] = "TIFFWriteDirectoryTagSbytePerSample";
	int8* m;
	int8* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(int8));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedSbyteArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

static int
TIFFWriteDirectoryTagShort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedShort(tif,ndir,dir,tag,value));
}

static int
TIFFWriteDirectoryTagShortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint16* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedShortArray(tif,ndir,dir,tag,count,value));
}

static int
TIFFWriteDirectoryTagShortPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value)
{
	static const char module[] = "TIFFWriteDirectoryTagShortPerSample";
	uint16* m;
	uint16* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(uint16));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedShortArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}

#ifdef notdef
static int
TIFFWriteDirectoryTagSshort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSshort(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagSshortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int16* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSshortArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int
TIFFWriteDirectoryTagSshortPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value)
{
	static const char module[] = "TIFFWriteDirectoryTagSshortPerSample";
	int16* m;
	int16* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(int16));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedSshortArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

static int
TIFFWriteDirectoryTagLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedLong(tif,ndir,dir,tag,value));
}

static int
TIFFWriteDirectoryTagLongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedLongArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int
TIFFWriteDirectoryTagLongPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value)
{
	static const char module[] = "TIFFWriteDirectoryTagLongPerSample";
	uint32* m;
	uint32* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(uint32));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedLongArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

#ifdef notdef
static int
TIFFWriteDirectoryTagSlong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSlong(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagSlongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int32* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSlongArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int
TIFFWriteDirectoryTagSlongPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value)
{
	static const char module[] = "TIFFWriteDirectoryTagSlongPerSample";
	int32* m;
	int32* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(int32));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedSlongArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

#ifdef notdef
static int
TIFFWriteDirectoryTagLong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint64 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedLong8(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedLong8Array(tif,ndir,dir,tag,count,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagSlong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int64 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSlong8(tif,ndir,dir,tag,value));
}
#endif

static int
TIFFWriteDirectoryTagSlong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int64* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSlong8Array(tif,ndir,dir,tag,count,value));
}

static int
TIFFWriteDirectoryTagRational(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedRational(tif,ndir,dir,tag,value));
}

static int
TIFFWriteDirectoryTagRationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedRationalArray(tif,ndir,dir,tag,count,value));
}

static int
TIFFWriteDirectoryTagSrationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedSrationalArray(tif,ndir,dir,tag,count,value));
}

#ifdef notdef
static int TIFFWriteDirectoryTagFloat(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedFloat(tif,ndir,dir,tag,value));
}
#endif

static int TIFFWriteDirectoryTagFloatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedFloatArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int TIFFWriteDirectoryTagFloatPerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value)
{
	static const char module[] = "TIFFWriteDirectoryTagFloatPerSample";
	float* m;
	float* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(float));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedFloatArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

#ifdef notdef
static int TIFFWriteDirectoryTagDouble(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedDouble(tif,ndir,dir,tag,value));
}
#endif

static int TIFFWriteDirectoryTagDoubleArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedDoubleArray(tif,ndir,dir,tag,count,value));
}

#if 0
static int TIFFWriteDirectoryTagDoublePerSample(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	static const char module[] = "TIFFWriteDirectoryTagDoublePerSample";
	double* m;
	double* na;
	uint16 nb;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=_TIFFmalloc(tif->tif_dir.td_samplesperpixel*sizeof(double));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=m, nb=0; nb<tif->tif_dir.td_samplesperpixel; na++, nb++)
		*na=value;
	o=TIFFWriteDirectoryTagCheckedDoubleArray(tif,ndir,dir,tag,tif->tif_dir.td_samplesperpixel,m);
	_TIFFfree(m);
	return(o);
}
#endif

static int
TIFFWriteDirectoryTagIfdArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedIfdArray(tif,ndir,dir,tag,count,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	return(TIFFWriteDirectoryTagCheckedIfd8Array(tif,ndir,dir,tag,count,value));
}
#endif

static int
TIFFWriteDirectoryTagShortLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value)
{
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	if (value<=0xFFFF)
		return(TIFFWriteDirectoryTagCheckedShort(tif,ndir,dir,tag,(uint16)value));
	else
		return(TIFFWriteDirectoryTagCheckedLong(tif,ndir,dir,tag,value));
}

/************************************************************************/
/*                TIFFWriteDirectoryTagLongLong8Array()                 */
/*                                                                      */
/*      Write out LONG8 array as LONG8 for BigTIFF or LONG for          */
/*      Classic TIFF with some checking.                                */
/************************************************************************/

static int
TIFFWriteDirectoryTagLongLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
    static const char module[] = "TIFFWriteDirectoryTagLongLong8Array";
    uint64* ma;
    uint32 mb;
    uint32* p;
    uint32* q;
    int o;

    /* is this just a counting pass? */
    if (dir==NULL)
    {
        (*ndir)++;
        return(1);
    }

    /* We always write LONG8 for BigTIFF, no checking needed. */
    if( tif->tif_flags&TIFF_BIGTIFF )
        return TIFFWriteDirectoryTagCheckedLong8Array(tif,ndir,dir,
                                                      tag,count,value);

    /*
    ** For classic tiff we want to verify everything is in range for LONG
    ** and convert to long format.
    */

    p = _TIFFmalloc(count*sizeof(uint32));
    if (p==NULL)
    {
        TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
        return(0);
    }

    for (q=p, ma=value, mb=0; mb<count; ma++, mb++, q++)
    {
        if (*ma>0xFFFFFFFF)
        {
            TIFFErrorExt(tif->tif_clientdata,module,
                         "Attempt to write value larger than 0xFFFFFFFF in Classic TIFF file.");
            _TIFFfree(p);
            return(0);
        }
        *q= (uint32)(*ma);
    }

    o=TIFFWriteDirectoryTagCheckedLongArray(tif,ndir,dir,tag,count,p);
    _TIFFfree(p);

    return(o);
}

/************************************************************************/
/*                 TIFFWriteDirectoryTagIfdIfd8Array()                  */
/*                                                                      */
/*      Write either IFD8 or IFD array depending on file type.          */
/************************************************************************/

static int
TIFFWriteDirectoryTagIfdIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
    static const char module[] = "TIFFWriteDirectoryTagIfdIfd8Array";
    uint64* ma;
    uint32 mb;
    uint32* p;
    uint32* q;
    int o;

    /* is this just a counting pass? */
    if (dir==NULL)
    {
        (*ndir)++;
        return(1);
    }

    /* We always write IFD8 for BigTIFF, no checking needed. */
    if( tif->tif_flags&TIFF_BIGTIFF )
        return TIFFWriteDirectoryTagCheckedIfd8Array(tif,ndir,dir,
                                                     tag,count,value);

    /*
    ** For classic tiff we want to verify everything is in range for IFD
    ** and convert to long format.
    */

    p = _TIFFmalloc(count*sizeof(uint32));
    if (p==NULL)
    {
        TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
        return(0);
    }

    for (q=p, ma=value, mb=0; mb<count; ma++, mb++, q++)
    {
        if (*ma>0xFFFFFFFF)
        {
            TIFFErrorExt(tif->tif_clientdata,module,
                         "Attempt to write value larger than 0xFFFFFFFF in Classic TIFF file.");
            _TIFFfree(p);
            return(0);
        }
        *q= (uint32)(*ma);
    }

    o=TIFFWriteDirectoryTagCheckedIfdArray(tif,ndir,dir,tag,count,p);
    _TIFFfree(p);

    return(o);
}

#ifdef notdef
static int
TIFFWriteDirectoryTagShortLongLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
	static const char module[] = "TIFFWriteDirectoryTagShortLongLong8Array";
	uint64* ma;
	uint32 mb;
	uint8 n;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	n=0;
	for (ma=value, mb=0; mb<count; ma++, mb++)
	{
		if ((n==0)&&(*ma>0xFFFF))
			n=1;
		if ((n==1)&&(*ma>0xFFFFFFFF))
		{
			n=2;
			break;
		}
	}
	if (n==0)
	{
		uint16* p;
		uint16* q;
		p=_TIFFmalloc(count*sizeof(uint16));
		if (p==NULL)
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
			return(0);
		}
		for (ma=value, mb=0, q=p; mb<count; ma++, mb++, q++)
			*q=(uint16)(*ma);
		o=TIFFWriteDirectoryTagCheckedShortArray(tif,ndir,dir,tag,count,p);
		_TIFFfree(p);
	}
	else if (n==1)
	{
		uint32* p;
		uint32* q;
		p=_TIFFmalloc(count*sizeof(uint32));
		if (p==NULL)
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
			return(0);
		}
		for (ma=value, mb=0, q=p; mb<count; ma++, mb++, q++)
			*q=(uint32)(*ma);
		o=TIFFWriteDirectoryTagCheckedLongArray(tif,ndir,dir,tag,count,p);
		_TIFFfree(p);
	}
	else
	{
		assert(n==2);
		o=TIFFWriteDirectoryTagCheckedLong8Array(tif,ndir,dir,tag,count,value);
	}
	return(o);
}
#endif
static int
TIFFWriteDirectoryTagColormap(TIFF* tif, uint32* ndir, TIFFDirEntry* dir)
{
	static const char module[] = "TIFFWriteDirectoryTagColormap";
	uint32 m;
	uint16* n;
	int o;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=(1<<tif->tif_dir.td_bitspersample);
	n=_TIFFmalloc(3*m*sizeof(uint16));
	if (n==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	_TIFFmemcpy(&n[0],tif->tif_dir.td_colormap[0],m*sizeof(uint16));
	_TIFFmemcpy(&n[m],tif->tif_dir.td_colormap[1],m*sizeof(uint16));
	_TIFFmemcpy(&n[2*m],tif->tif_dir.td_colormap[2],m*sizeof(uint16));
	o=TIFFWriteDirectoryTagCheckedShortArray(tif,ndir,dir,TIFFTAG_COLORMAP,3*m,n);
	_TIFFfree(n);
	return(o);
}

static int
TIFFWriteDirectoryTagTransferfunction(TIFF* tif, uint32* ndir, TIFFDirEntry* dir)
{
	static const char module[] = "TIFFWriteDirectoryTagTransferfunction";
	uint32 m;
	uint16 n;
	uint16* o;
	int p;
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=(1<<tif->tif_dir.td_bitspersample);
	n=tif->tif_dir.td_samplesperpixel-tif->tif_dir.td_extrasamples;
	/*
	 * Check if the table can be written as a single column,
	 * or if it must be written as 3 columns.  Note that we
	 * write a 3-column tag if there are 2 samples/pixel and
	 * a single column of data won't suffice--hmm.
	 */
	if (n>3)
		n=3;
	if (n==3)
	{
		if (!_TIFFmemcmp(tif->tif_dir.td_transferfunction[0],tif->tif_dir.td_transferfunction[2],m*sizeof(uint16)))
			n=2;
	}
	if (n==2)
	{
		if (!_TIFFmemcmp(tif->tif_dir.td_transferfunction[0],tif->tif_dir.td_transferfunction[1],m*sizeof(uint16)))
			n=1;
	}
	if (n==0)
		n=1;
	o=_TIFFmalloc(n*m*sizeof(uint16));
	if (o==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	_TIFFmemcpy(&o[0],tif->tif_dir.td_transferfunction[0],m*sizeof(uint16));
	if (n>1)
		_TIFFmemcpy(&o[m],tif->tif_dir.td_transferfunction[1],m*sizeof(uint16));
	if (n>2)
		_TIFFmemcpy(&o[2*m],tif->tif_dir.td_transferfunction[2],m*sizeof(uint16));
	p=TIFFWriteDirectoryTagCheckedShortArray(tif,ndir,dir,TIFFTAG_TRANSFERFUNCTION,n*m,o);
	_TIFFfree(o);
	return(p);
}

static int
TIFFWriteDirectoryTagSubifd(TIFF* tif, uint32* ndir, TIFFDirEntry* dir)
{
	static const char module[] = "TIFFWriteDirectoryTagSubifd";
	uint64 m;
	int n;
	if (tif->tif_dir.td_nsubifd==0)
		return(1);
	if (dir==NULL)
	{
		(*ndir)++;
		return(1);
	}
	m=tif->tif_dataoff;
	if (!(tif->tif_flags&TIFF_BIGTIFF))
	{
		uint32* o;
		uint64* pa;
		uint32* pb;
		uint16 p;
		o=_TIFFmalloc(tif->tif_dir.td_nsubifd*sizeof(uint32));
		if (o==NULL)
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
			return(0);
		}
		pa=tif->tif_dir.td_subifd;
		pb=o;
		for (p=0; p < tif->tif_dir.td_nsubifd; p++)
		{
                        assert(pa != 0);
			assert(*pa <= 0xFFFFFFFFUL);
			*pb++=(uint32)(*pa++);
		}
		n=TIFFWriteDirectoryTagCheckedIfdArray(tif,ndir,dir,TIFFTAG_SUBIFD,tif->tif_dir.td_nsubifd,o);
		_TIFFfree(o);
	}
	else
		n=TIFFWriteDirectoryTagCheckedIfd8Array(tif,ndir,dir,TIFFTAG_SUBIFD,tif->tif_dir.td_nsubifd,tif->tif_dir.td_subifd);
	if (!n)
		return(0);
	/*
	 * Total hack: if this directory includes a SubIFD
	 * tag then force the next <n> directories to be
	 * written as ``sub directories'' of this one.  This
	 * is used to write things like thumbnails and
	 * image masks that one wants to keep out of the
	 * normal directory linkage access mechanism.
	 */
	tif->tif_flags|=TIFF_INSUBIFD;
	tif->tif_nsubifd=tif->tif_dir.td_nsubifd;
	if (tif->tif_dir.td_nsubifd==1)
		tif->tif_subifdoff=0;
	else
		tif->tif_subifdoff=m;
	return(1);
}

static int
TIFFWriteDirectoryTagCheckedAscii(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, char* value)
{
	assert(sizeof(char)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_ASCII,count,count,value));
}

static int
TIFFWriteDirectoryTagCheckedUndefinedArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value)
{
	assert(sizeof(uint8)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_UNDEFINED,count,count,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedByte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint8 value)
{
	assert(sizeof(uint8)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_BYTE,1,1,&value));
}
#endif

static int
TIFFWriteDirectoryTagCheckedByteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint8* value)
{
	assert(sizeof(uint8)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_BYTE,count,count,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedSbyte(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int8 value)
{
	assert(sizeof(int8)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SBYTE,1,1,&value));
}
#endif

static int
TIFFWriteDirectoryTagCheckedSbyteArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int8* value)
{
	assert(sizeof(int8)==1);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SBYTE,count,count,value));
}

static int
TIFFWriteDirectoryTagCheckedShort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 value)
{
	uint16 m;
	assert(sizeof(uint16)==2);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabShort(&m);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SHORT,1,2,&m));
}

static int
TIFFWriteDirectoryTagCheckedShortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint16* value)
{
	assert(count<0x80000000);
	assert(sizeof(uint16)==2);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfShort(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SHORT,count,count*2,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedSshort(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int16 value)
{
	int16 m;
	assert(sizeof(int16)==2);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabShort((uint16*)(&m));
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SSHORT,1,2,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedSshortArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int16* value)
{
	assert(count<0x80000000);
	assert(sizeof(int16)==2);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfShort((uint16*)value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SSHORT,count,count*2,value));
}

static int
TIFFWriteDirectoryTagCheckedLong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 value)
{
	uint32 m;
	assert(sizeof(uint32)==4);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabLong(&m);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_LONG,1,4,&m));
}

static int
TIFFWriteDirectoryTagCheckedLongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value)
{
	assert(count<0x40000000);
	assert(sizeof(uint32)==4);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_LONG,count,count*4,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedSlong(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int32 value)
{
	int32 m;
	assert(sizeof(int32)==4);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabLong((uint32*)(&m));
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SLONG,1,4,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedSlongArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int32* value)
{
	assert(count<0x40000000);
	assert(sizeof(int32)==4);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong((uint32*)value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SLONG,count,count*4,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedLong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint64 value)
{
	uint64 m;
	assert(sizeof(uint64)==8);
	assert(tif->tif_flags&TIFF_BIGTIFF);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabLong8(&m);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_LONG8,1,8,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedLong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
	assert(count<0x20000000);
	assert(sizeof(uint64)==8);
	assert(tif->tif_flags&TIFF_BIGTIFF);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong8(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_LONG8,count,count*8,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedSlong8(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, int64 value)
{
	int64 m;
	assert(sizeof(int64)==8);
	assert(tif->tif_flags&TIFF_BIGTIFF);
	m=value;
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabLong8((uint64*)(&m));
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SLONG8,1,8,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedSlong8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, int64* value)
{
	assert(count<0x20000000);
	assert(sizeof(int64)==8);
	assert(tif->tif_flags&TIFF_BIGTIFF);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong8((uint64*)value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SLONG8,count,count*8,value));
}

static int
TIFFWriteDirectoryTagCheckedRational(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	uint32 m[2];
	assert(value>=0.0);
	assert(sizeof(uint32)==4);
	if (value<=0.0)
	{
		m[0]=0;
		m[1]=1;
	}
	else if (value==(double)(uint32)value)
	{
		m[0]=(uint32)value;
		m[1]=1;
	}
	else if (value<1.0)
	{
		m[0]=(uint32)(value*0xFFFFFFFF);
		m[1]=0xFFFFFFFF;
	}
	else
	{
		m[0]=0xFFFFFFFF;
		m[1]=(uint32)(0xFFFFFFFF/value);
	}
	if (tif->tif_flags&TIFF_SWAB)
	{
		TIFFSwabLong(&m[0]);
		TIFFSwabLong(&m[1]);
	}
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_RATIONAL,1,8,&m[0]));
}

static int
TIFFWriteDirectoryTagCheckedRationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	static const char module[] = "TIFFWriteDirectoryTagCheckedRationalArray";
	uint32* m;
	float* na;
	uint32* nb;
	uint32 nc;
	int o;
	assert(sizeof(uint32)==4);
	m=_TIFFmalloc(count*2*sizeof(uint32));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=value, nb=m, nc=0; nc<count; na++, nb+=2, nc++)
	{
		if (*na<=0.0)
		{
			nb[0]=0;
			nb[1]=1;
		}
		else if (*na==(float)(uint32)(*na))
		{
			nb[0]=(uint32)(*na);
			nb[1]=1;
		}
		else if (*na<1.0)
		{
			nb[0]=(uint32)((*na)*0xFFFFFFFF);
			nb[1]=0xFFFFFFFF;
		}
		else
		{
			nb[0]=0xFFFFFFFF;
			nb[1]=(uint32)(0xFFFFFFFF/(*na));
		}
	}
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong(m,count*2);
	o=TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_RATIONAL,count,count*8,&m[0]);
	_TIFFfree(m);
	return(o);
}

static int
TIFFWriteDirectoryTagCheckedSrationalArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	static const char module[] = "TIFFWriteDirectoryTagCheckedSrationalArray";
	int32* m;
	float* na;
	int32* nb;
	uint32 nc;
	int o;
	assert(sizeof(int32)==4);
	m=_TIFFmalloc(count*2*sizeof(int32));
	if (m==NULL)
	{
		TIFFErrorExt(tif->tif_clientdata,module,"Out of memory");
		return(0);
	}
	for (na=value, nb=m, nc=0; nc<count; na++, nb+=2, nc++)
	{
		if (*na<0.0)
		{
			if (*na==(int32)(*na))
			{
				nb[0]=(int32)(*na);
				nb[1]=1;
			}
			else if (*na>-1.0)
			{
				nb[0]=-(int32)((-*na)*0x7FFFFFFF);
				nb[1]=0x7FFFFFFF;
			}
			else
			{
				nb[0]=-0x7FFFFFFF;
				nb[1]=(int32)(0x7FFFFFFF/(-*na));
			}
		}
		else
		{
			if (*na==(int32)(*na))
			{
				nb[0]=(int32)(*na);
				nb[1]=1;
			}
			else if (*na<1.0)
			{
				nb[0]=(int32)((*na)*0x7FFFFFFF);
				nb[1]=0x7FFFFFFF;
			}
			else
			{
				nb[0]=0x7FFFFFFF;
				nb[1]=(int32)(0x7FFFFFFF/(*na));
			}
		}
	}
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong((uint32*)m,count*2);
	o=TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_SRATIONAL,count,count*8,&m[0]);
	_TIFFfree(m);
	return(o);
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedFloat(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, float value)
{
	float m;
	assert(sizeof(float)==4);
	m=value;
	TIFFCvtNativeToIEEEFloat(tif,1,&m);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabFloat(&m);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_FLOAT,1,4,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedFloatArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, float* value)
{
	assert(count<0x40000000);
	assert(sizeof(float)==4);
	TIFFCvtNativeToIEEEFloat(tif,count,&value);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfFloat(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_FLOAT,count,count*4,value));
}

#ifdef notdef
static int
TIFFWriteDirectoryTagCheckedDouble(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, double value)
{
	double m;
	assert(sizeof(double)==8);
	m=value;
	TIFFCvtNativeToIEEEDouble(tif,1,&m);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabDouble(&m);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_DOUBLE,1,8,&m));
}
#endif

static int
TIFFWriteDirectoryTagCheckedDoubleArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, double* value)
{
	assert(count<0x20000000);
	assert(sizeof(double)==8);
	TIFFCvtNativeToIEEEDouble(tif,count,&value);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfDouble(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_DOUBLE,count,count*8,value));
}

static int
TIFFWriteDirectoryTagCheckedIfdArray(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint32* value)
{
	assert(count<0x40000000);
	assert(sizeof(uint32)==4);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_IFD,count,count*4,value));
}

static int
TIFFWriteDirectoryTagCheckedIfd8Array(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint32 count, uint64* value)
{
	assert(count<0x20000000);
	assert(sizeof(uint64)==8);
	assert(tif->tif_flags&TIFF_BIGTIFF);
	if (tif->tif_flags&TIFF_SWAB)
		TIFFSwabArrayOfLong8(value,count);
	return(TIFFWriteDirectoryTagData(tif,ndir,dir,tag,TIFF_IFD8,count,count*8,value));
}

static int
TIFFWriteDirectoryTagData(TIFF* tif, uint32* ndir, TIFFDirEntry* dir, uint16 tag, uint16 datatype, uint32 count, uint32 datalength, void* data)
{
	static const char module[] = "TIFFWriteDirectoryTagData";
	uint32 m;
	m=0;
	while (m<(*ndir))
	{
		assert(dir[m].tdir_tag!=tag);
		if (dir[m].tdir_tag>tag)
			break;
		m++;
	}
	if (m<(*ndir))
	{
		uint32 n;
		for (n=*ndir; n>m; n--)
			dir[n]=dir[n-1];
	}
	dir[m].tdir_tag=tag;
	dir[m].tdir_type=datatype;
	dir[m].tdir_count=count;
	dir[m].tdir_offset.toff_long8 = 0;
	if (datalength<=((tif->tif_flags&TIFF_BIGTIFF)?0x8U:0x4U))
		_TIFFmemcpy(&dir[m].tdir_offset,data,datalength);
	else
	{
		uint64 na,nb;
		na=tif->tif_dataoff;
		nb=na+datalength;
		if (!(tif->tif_flags&TIFF_BIGTIFF))
			nb=(uint32)nb;
		if ((nb<na)||(nb<datalength))
		{
			TIFFErrorExt(tif->tif_clientdata,module,"Maximum TIFF file size exceeded");
			return(0);
		}
		if (!SeekOK(tif,na))
		{
			TIFFErrorExt(tif->tif_clientdata,module,"IO error writing tag data");
			return(0);
		}
		assert(datalength<0x80000000UL);
		if (!WriteOK(tif,data,(tmsize_t)datalength))
		{
			TIFFErrorExt(tif->tif_clientdata,module,"IO error writing tag data");
			return(0);
		}
		tif->tif_dataoff=nb;
		if (tif->tif_dataoff&1)
			tif->tif_dataoff++;
		if (!(tif->tif_flags&TIFF_BIGTIFF))
		{
			uint32 o;
			o=(uint32)na;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabLong(&o);
			_TIFFmemcpy(&dir[m].tdir_offset,&o,4);
		}
		else
		{
			dir[m].tdir_offset.toff_long8 = na;
			if (tif->tif_flags&TIFF_SWAB)
				TIFFSwabLong8(&dir[m].tdir_offset.toff_long8);
		}
	}
	(*ndir)++;
	return(1);
}

/*
 * Link the current directory into the directory chain for the file.
 */
static int
TIFFLinkDirectory(TIFF* tif)
{
	static const char module[] = "TIFFLinkDirectory";

	tif->tif_diroff = (TIFFSeekFile(tif,0,SEEK_END)+1) &~ 1;

	/*
	 * Handle SubIFDs
	 */
	if (tif->tif_flags & TIFF_INSUBIFD)
	{
		if (!(tif->tif_flags&TIFF_BIGTIFF))
		{
			uint32 m;
			m = (uint32)tif->tif_diroff;
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabLong(&m);
			(void) TIFFSeekFile(tif, tif->tif_subifdoff, SEEK_SET);
			if (!WriteOK(tif, &m, 4)) {
				TIFFErrorExt(tif->tif_clientdata, module,
				     "Error writing SubIFD directory link");
				return (0);
			}
			/*
			 * Advance to the next SubIFD or, if this is
			 * the last one configured, revert back to the
			 * normal directory linkage.
			 */
			if (--tif->tif_nsubifd)
				tif->tif_subifdoff += 4;
			else
				tif->tif_flags &= ~TIFF_INSUBIFD;
			return (1);
		}
		else
		{
			uint64 m;
			m = tif->tif_diroff;
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabLong8(&m);
			(void) TIFFSeekFile(tif, tif->tif_subifdoff, SEEK_SET);
			if (!WriteOK(tif, &m, 8)) {
				TIFFErrorExt(tif->tif_clientdata, module,
				     "Error writing SubIFD directory link");
				return (0);
			}
			/*
			 * Advance to the next SubIFD or, if this is
			 * the last one configured, revert back to the
			 * normal directory linkage.
			 */
			if (--tif->tif_nsubifd)
				tif->tif_subifdoff += 8;
			else
				tif->tif_flags &= ~TIFF_INSUBIFD;
			return (1);
		}
	}

	if (!(tif->tif_flags&TIFF_BIGTIFF))
	{
		uint32 m;
		uint32 nextdir;
		m = (uint32)(tif->tif_diroff);
		if (tif->tif_flags & TIFF_SWAB)
			TIFFSwabLong(&m);
		if (tif->tif_header.classic.tiff_diroff == 0) {
			/*
			 * First directory, overwrite offset in header.
			 */
			tif->tif_header.classic.tiff_diroff = (uint32) tif->tif_diroff;
			(void) TIFFSeekFile(tif,4, SEEK_SET);
			if (!WriteOK(tif, &m, 4)) {
				TIFFErrorExt(tif->tif_clientdata, tif->tif_name,
					     "Error writing TIFF header");
				return (0);
			}
			return (1);
		}
		/*
		 * Not the first directory, search to the last and append.
		 */
		nextdir = tif->tif_header.classic.tiff_diroff;
		while(1) {
			uint16 dircount;
			uint32 nextnextdir;

			if (!SeekOK(tif, nextdir) ||
			    !ReadOK(tif, &dircount, 2)) {
				TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory count");
				return (0);
			}
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabShort(&dircount);
			(void) TIFFSeekFile(tif,
			    nextdir+2+dircount*12, SEEK_SET);
			if (!ReadOK(tif, &nextnextdir, 4)) {
				TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory link");
				return (0);
			}
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabLong(&nextnextdir);
			if (nextnextdir==0)
			{
				(void) TIFFSeekFile(tif,
				    nextdir+2+dircount*12, SEEK_SET);
				if (!WriteOK(tif, &m, 4)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error writing directory link");
					return (0);
				}
				break;
			}
			nextdir=nextnextdir;
		}
	}
	else
	{
		uint64 m;
		uint64 nextdir;
		m = tif->tif_diroff;
		if (tif->tif_flags & TIFF_SWAB)
			TIFFSwabLong8(&m);
		if (tif->tif_header.big.tiff_diroff == 0) {
			/*
			 * First directory, overwrite offset in header.
			 */
			tif->tif_header.big.tiff_diroff = tif->tif_diroff;
			(void) TIFFSeekFile(tif,8, SEEK_SET);
			if (!WriteOK(tif, &m, 8)) {
				TIFFErrorExt(tif->tif_clientdata, tif->tif_name,
					     "Error writing TIFF header");
				return (0);
			}
			return (1);
		}
		/*
		 * Not the first directory, search to the last and append.
		 */
		nextdir = tif->tif_header.big.tiff_diroff;
		while(1) {
			uint64 dircount64;
			uint16 dircount;
			uint64 nextnextdir;

			if (!SeekOK(tif, nextdir) ||
			    !ReadOK(tif, &dircount64, 8)) {
				TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory count");
				return (0);
			}
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabLong8(&dircount64);
			if (dircount64>0xFFFF)
			{
				TIFFErrorExt(tif->tif_clientdata, module,
					     "Sanity check on tag count failed, likely corrupt TIFF");
				return (0);
			}
			dircount=(uint16)dircount64;
			(void) TIFFSeekFile(tif,
			    nextdir+8+dircount*20, SEEK_SET);
			if (!ReadOK(tif, &nextnextdir, 8)) {
				TIFFErrorExt(tif->tif_clientdata, module,
					     "Error fetching directory link");
				return (0);
			}
			if (tif->tif_flags & TIFF_SWAB)
				TIFFSwabLong8(&nextnextdir);
			if (nextnextdir==0)
			{
				(void) TIFFSeekFile(tif,
				    nextdir+8+dircount*20, SEEK_SET);
				if (!WriteOK(tif, &m, 8)) {
					TIFFErrorExt(tif->tif_clientdata, module,
					     "Error writing directory link");
					return (0);
				}
				break;
			}
			nextdir=nextnextdir;
		}
	}
	return (1);
}

/************************************************************************/
/*                          TIFFRewriteField()                          */
/*                                                                      */
/*      Rewrite a field in the directory on disk without regard to      */
/*      updating the TIFF directory structure in memory.  Currently     */
/*      only supported for field that already exist in the on-disk      */
/*      directory.  Mainly used for updating stripoffset /              */
/*      stripbytecount values after the directory is already on         */
/*      disk.                                                           */
/*                                                                      */
/*      Returns zero on failure, and one on success.                    */
/************************************************************************/

int
_TIFFRewriteField(TIFF* tif, uint16 tag, TIFFDataType in_datatype, 
                  tmsize_t count, void* data)
{
    static const char module[] = "TIFFResetField";
    /* const TIFFField* fip = NULL; */
    uint16 dircount;
    tmsize_t dirsize;
    uint8 direntry_raw[20];
    uint16 entry_tag = 0;
    uint16 entry_type = 0;
    uint64 entry_count = 0;
    uint64 entry_offset = 0;
    int    value_in_entry = 0;
    uint64 read_offset;
    uint8 *buf_to_write = NULL;
    TIFFDataType datatype;

/* -------------------------------------------------------------------- */
/*      Find field definition.                                          */
/* -------------------------------------------------------------------- */
    /*fip =*/ TIFFFindField(tif, tag, TIFF_ANY);

/* -------------------------------------------------------------------- */
/*      Do some checking this is a straight forward case.               */
/* -------------------------------------------------------------------- */
    if( isMapped(tif) )
    {
        TIFFErrorExt( tif->tif_clientdata, module, 
                      "Memory mapped files not currently supported for this operation." );
        return 0;
    }

    if( tif->tif_diroff == 0 )
    {
        TIFFErrorExt( tif->tif_clientdata, module, 
                      "Attempt to reset field on directory not already on disk." );
        return 0;
    }

/* -------------------------------------------------------------------- */
/*      Read the directory entry count.                                 */
/* -------------------------------------------------------------------- */
    if (!SeekOK(tif, tif->tif_diroff)) {
        TIFFErrorExt(tif->tif_clientdata, module,
                     "%s: Seek error accessing TIFF directory",
                     tif->tif_name);
        return 0;
    }

    read_offset = tif->tif_diroff;

    if (!(tif->tif_flags&TIFF_BIGTIFF))
    {
        if (!ReadOK(tif, &dircount, sizeof (uint16))) {
            TIFFErrorExt(tif->tif_clientdata, module,
                         "%s: Can not read TIFF directory count",
                         tif->tif_name);
            return 0;
        }
        if (tif->tif_flags & TIFF_SWAB)
            TIFFSwabShort(&dircount);
        dirsize = 12;
        read_offset += 2;
    } else {
        uint64 dircount64;
        if (!ReadOK(tif, &dircount64, sizeof (uint64))) {
            TIFFErrorExt(tif->tif_clientdata, module,
                         "%s: Can not read TIFF directory count",
                         tif->tif_name);
            return 0;
        }
        if (tif->tif_flags & TIFF_SWAB)
            TIFFSwabLong8(&dircount64);
        dircount = (uint16)dircount64;
        dirsize = 20;
        read_offset += 8;
    }

/* -------------------------------------------------------------------- */
/*      Read through directory to find target tag.                      */
/* -------------------------------------------------------------------- */
    while( dircount > 0 )
    {
        if (!ReadOK(tif, direntry_raw, dirsize)) {
            TIFFErrorExt(tif->tif_clientdata, module,
                         "%s: Can not read TIFF directory entry.",
                         tif->tif_name);
            return 0;
        }

        memcpy( &entry_tag, direntry_raw + 0, sizeof(uint16) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabShort( &entry_tag );

        if( entry_tag == tag )
            break;

        read_offset += dirsize;
    }

    if( entry_tag != tag )
    {
        TIFFErrorExt(tif->tif_clientdata, module,
                     "%s: Could not find tag %d.",
                     tif->tif_name, tag );
        return 0;
    }

/* -------------------------------------------------------------------- */
/*      Extract the type, count and offset for this entry.              */
/* -------------------------------------------------------------------- */
    memcpy( &entry_type, direntry_raw + 2, sizeof(uint16) );
    if (tif->tif_flags&TIFF_SWAB)
        TIFFSwabShort( &entry_type );

    if (!(tif->tif_flags&TIFF_BIGTIFF))
    {
        uint32 value;
        
        memcpy( &value, direntry_raw + 4, sizeof(uint32) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong( &value );
        entry_count = value;

        memcpy( &value, direntry_raw + 8, sizeof(uint32) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong( &value );
        entry_offset = value;
    }
    else
    {
        memcpy( &entry_count, direntry_raw + 4, sizeof(uint64) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong8( &entry_count );

        memcpy( &entry_offset, direntry_raw + 12, sizeof(uint64) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong8( &entry_offset );
    }

/* -------------------------------------------------------------------- */
/*      What data type do we want to write this as?                     */
/* -------------------------------------------------------------------- */
    if( TIFFDataWidth(in_datatype) == 8 && !(tif->tif_flags&TIFF_BIGTIFF) )
    {
        if( in_datatype == TIFF_LONG8 )
            datatype = TIFF_LONG;
        else if( in_datatype == TIFF_SLONG8 )
            datatype = TIFF_SLONG;
        else if( in_datatype == TIFF_IFD8 )
            datatype = TIFF_IFD;
        else
            datatype = in_datatype;
    }
    else 
        datatype = in_datatype;

/* -------------------------------------------------------------------- */
/*      Prepare buffer of actual data to write.  This includes          */
/*      swabbing as needed.                                             */
/* -------------------------------------------------------------------- */
    buf_to_write =
	    (uint8 *)_TIFFCheckMalloc(tif, count, TIFFDataWidth(datatype),
				      "for field buffer.");
    if (!buf_to_write)
        return 0;

    if( datatype == in_datatype )
        memcpy( buf_to_write, data, count * TIFFDataWidth(datatype) );
    else if( datatype == TIFF_SLONG && in_datatype == TIFF_SLONG8 )
    {
	tmsize_t i;

        for( i = 0; i < count; i++ )
        {
            ((int32 *) buf_to_write)[i] = 
                (int32) ((int64 *) data)[i];
            if( (int64) ((int32 *) buf_to_write)[i] != ((int64 *) data)[i] )
            {
                _TIFFfree( buf_to_write );
                TIFFErrorExt( tif->tif_clientdata, module, 
                              "Value exceeds 32bit range of output type." );
                return 0;
            }
        }
    }
    else if( (datatype == TIFF_LONG && in_datatype == TIFF_LONG8)
             || (datatype == TIFF_IFD && in_datatype == TIFF_IFD8) )
    {
	tmsize_t i;

        for( i = 0; i < count; i++ )
        {
            ((uint32 *) buf_to_write)[i] = 
                (uint32) ((uint64 *) data)[i];
            if( (uint64) ((uint32 *) buf_to_write)[i] != ((uint64 *) data)[i] )
            {
                _TIFFfree( buf_to_write );
                TIFFErrorExt( tif->tif_clientdata, module, 
                              "Value exceeds 32bit range of output type." );
                return 0;
            }
        }
    }

    if( TIFFDataWidth(datatype) > 1 && (tif->tif_flags&TIFF_SWAB) )
    {
        if( TIFFDataWidth(datatype) == 2 )
            TIFFSwabArrayOfShort( (uint16 *) buf_to_write, count );
        else if( TIFFDataWidth(datatype) == 4 )
            TIFFSwabArrayOfLong( (uint32 *) buf_to_write, count );
        else if( TIFFDataWidth(datatype) == 8 )
            TIFFSwabArrayOfLong8( (uint64 *) buf_to_write, count );
    }

/* -------------------------------------------------------------------- */
/*      Is this a value that fits into the directory entry?             */
/* -------------------------------------------------------------------- */
    if (!(tif->tif_flags&TIFF_BIGTIFF))
    {
        if( TIFFDataWidth(datatype) * count <= 4 )
        {
            entry_offset = read_offset + 8;
            value_in_entry = 1;
        }
    }
    else
    {
        if( TIFFDataWidth(datatype) * count <= 8 )
        {
            entry_offset = read_offset + 12;
            value_in_entry = 1;
        }
    }

/* -------------------------------------------------------------------- */
/*      If the tag type, and count match, then we just write it out     */
/*      over the old values without altering the directory entry at     */
/*      all.                                                            */
/* -------------------------------------------------------------------- */
    if( entry_count == (uint64)count && entry_type == (uint16) datatype )
    {
        if (!SeekOK(tif, entry_offset)) {
            _TIFFfree( buf_to_write );
            TIFFErrorExt(tif->tif_clientdata, module,
                         "%s: Seek error accessing TIFF directory",
                         tif->tif_name);
            return 0;
        }
        if (!WriteOK(tif, buf_to_write, count*TIFFDataWidth(datatype))) {
            _TIFFfree( buf_to_write );
            TIFFErrorExt(tif->tif_clientdata, module,
                         "Error writing directory link");
            return (0);
        }

        _TIFFfree( buf_to_write );
        return 1;
    }

/* -------------------------------------------------------------------- */
/*      Otherwise, we write the new tag data at the end of the file.    */
/* -------------------------------------------------------------------- */
    if( !value_in_entry )
    {
        entry_offset = TIFFSeekFile(tif,0,SEEK_END);
        
        if (!WriteOK(tif, buf_to_write, count*TIFFDataWidth(datatype))) {
            _TIFFfree( buf_to_write );
            TIFFErrorExt(tif->tif_clientdata, module,
                         "Error writing directory link");
            return (0);
        }
        
        _TIFFfree( buf_to_write );
    }
    else
    {
        memcpy( &entry_offset, buf_to_write, count*TIFFDataWidth(datatype));
    }

/* -------------------------------------------------------------------- */
/*      Adjust the directory entry.                                     */
/* -------------------------------------------------------------------- */
    entry_type = datatype;
    memcpy( direntry_raw + 2, &entry_type, sizeof(uint16) );
    if (tif->tif_flags&TIFF_SWAB)
        TIFFSwabShort( (uint16 *) (direntry_raw + 2) );

    if (!(tif->tif_flags&TIFF_BIGTIFF))
    {
        uint32 value;

        value = (uint32) entry_count;
        memcpy( direntry_raw + 4, &value, sizeof(uint32) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong( (uint32 *) (direntry_raw + 4) );

        value = (uint32) entry_offset;
        memcpy( direntry_raw + 8, &value, sizeof(uint32) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong( (uint32 *) (direntry_raw + 8) );
    }
    else
    {
        memcpy( direntry_raw + 4, &entry_count, sizeof(uint64) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong8( (uint64 *) (direntry_raw + 4) );

        memcpy( direntry_raw + 12, &entry_offset, sizeof(uint64) );
        if (tif->tif_flags&TIFF_SWAB)
            TIFFSwabLong8( (uint64 *) (direntry_raw + 12) );
    }

/* -------------------------------------------------------------------- */
/*      Write the directory entry out to disk.                          */
/* -------------------------------------------------------------------- */
    if (!SeekOK(tif, read_offset )) {
        TIFFErrorExt(tif->tif_clientdata, module,
                     "%s: Seek error accessing TIFF directory",
                     tif->tif_name);
        return 0;
    }

    if (!WriteOK(tif, direntry_raw,dirsize))
    {
        TIFFErrorExt(tif->tif_clientdata, module,
                     "%s: Can not write TIFF directory entry.",
                     tif->tif_name);
        return 0;
    }
    
    return 1;
}
/* vim: set ts=8 sts=8 sw=8 noet: */
/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */

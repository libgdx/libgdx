// ==========================================================
// MNG / JNG helpers
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

/**
References
http://www.libpng.org/pub/mng/spec/jng.html
http://www.w3.org/TR/PNG/
http://libpng.org/pub/mng/spec/
*/

// --------------------------------------------------------------------------

#define MNG_INCLUDE_JNG

#ifdef MNG_INCLUDE_JNG
#define MNG_COLORTYPE_JPEGGRAY           8       /* JHDR */
#define MNG_COLORTYPE_JPEGCOLOR         10
#define MNG_COLORTYPE_JPEGGRAYA         12
#define MNG_COLORTYPE_JPEGCOLORA        14

#define MNG_BITDEPTH_JPEG8               8       /* JHDR */
#define MNG_BITDEPTH_JPEG12             12
#define MNG_BITDEPTH_JPEG8AND12         20

#define MNG_COMPRESSION_BASELINEJPEG     8       /* JHDR */

#define MNG_INTERLACE_SEQUENTIAL         0       /* JHDR */
#define MNG_INTERLACE_PROGRESSIVE        8
#endif /* MNG_INCLUDE_JNG */

// --------------------------------------------------------------------------

#define JNG_SUPPORTED

/** Size of a JDAT chunk on writing */
const DWORD JPEG_CHUNK_SIZE	= 8192;

/** PNG signature */
static const BYTE g_png_signature[8] = { 137, 80, 78, 71, 13, 10, 26, 10 };
/** JNG signature */
static const BYTE g_jng_signature[8] = { 139, 74, 78, 71, 13, 10, 26, 10 };

// --------------------------------------------------------------------------

/** Chunk type converted to enum */
enum eChunckType {
	UNKNOWN_CHUNCK,
	MHDR,
	BACK,
	BASI,
	CLIP,
	CLON,
	DEFI,
	DHDR,
	DISC,
	ENDL,
	FRAM,
	IEND,
	IHDR,
	JHDR,
	LOOP,
	MAGN,
	MEND,
	MOVE,
	PAST,
	PLTE,
	SAVE,
	SEEK,
	SHOW,
	TERM,
	bKGD,
	cHRM,
	gAMA,
	iCCP,
	nEED,
	pHYg,
	vpAg,
	pHYs,
	sBIT,
	sRGB,
	tRNS,
	IDAT,
	JDAT,
	JDAA,
	JdAA,
	JSEP,
	oFFs,
	hIST,
	iTXt,
	sPLT,
	sTER,
	tEXt,
	tIME,
	zTXt
};

/**
Helper for map<key, value> where value is a pointer to a string. 
Used to store tEXt metadata. 
*/
typedef std::map<std::string, std::string> tEXtMAP;

// --------------------------------------------------------------------------

/*
  Constant strings for known chunk types.  If you need to add a chunk,
  add a string holding the name here.   To make the code more
  portable, we use ASCII numbers like this, not characters.
*/

static BYTE mng_MHDR[5]={ 77,  72,  68,  82, (BYTE) '\0'};
static BYTE mng_BACK[5]={ 66,  65,  67,  75, (BYTE) '\0'};
static BYTE mng_BASI[5]={ 66,  65,  83,  73, (BYTE) '\0'};
static BYTE mng_CLIP[5]={ 67,  76,  73,  80, (BYTE) '\0'};
static BYTE mng_CLON[5]={ 67,  76,  79,  78, (BYTE) '\0'};
static BYTE mng_DEFI[5]={ 68,  69,  70,  73, (BYTE) '\0'};
static BYTE mng_DHDR[5]={ 68,  72,  68,  82, (BYTE) '\0'};
static BYTE mng_DISC[5]={ 68,  73,  83,  67, (BYTE) '\0'};
static BYTE mng_ENDL[5]={ 69,  78,  68,  76, (BYTE) '\0'};
static BYTE mng_FRAM[5]={ 70,  82,  65,  77, (BYTE) '\0'};
static BYTE mng_IEND[5]={ 73,  69,  78,  68, (BYTE) '\0'};
static BYTE mng_IHDR[5]={ 73,  72,  68,  82, (BYTE) '\0'};
static BYTE mng_JHDR[5]={ 74,  72,  68,  82, (BYTE) '\0'};
static BYTE mng_LOOP[5]={ 76,  79,  79,  80, (BYTE) '\0'};
static BYTE mng_MAGN[5]={ 77,  65,  71,  78, (BYTE) '\0'};
static BYTE mng_MEND[5]={ 77,  69,  78,  68, (BYTE) '\0'};
static BYTE mng_MOVE[5]={ 77,  79,  86,  69, (BYTE) '\0'};
static BYTE mng_PAST[5]={ 80,  65,  83,  84, (BYTE) '\0'};
static BYTE mng_PLTE[5]={ 80,  76,  84,  69, (BYTE) '\0'};
static BYTE mng_SAVE[5]={ 83,  65,  86,  69, (BYTE) '\0'};
static BYTE mng_SEEK[5]={ 83,  69,  69,  75, (BYTE) '\0'};
static BYTE mng_SHOW[5]={ 83,  72,  79,  87, (BYTE) '\0'};
static BYTE mng_TERM[5]={ 84,  69,  82,  77, (BYTE) '\0'};
static BYTE mng_bKGD[5]={ 98,  75,  71,  68, (BYTE) '\0'};
static BYTE mng_cHRM[5]={ 99,  72,  82,  77, (BYTE) '\0'};
static BYTE mng_gAMA[5]={103,  65,  77,  65, (BYTE) '\0'};
static BYTE mng_iCCP[5]={105,  67,  67,  80, (BYTE) '\0'};
static BYTE mng_nEED[5]={110,  69,  69,  68, (BYTE) '\0'};
static BYTE mng_pHYg[5]={112,  72,  89, 103, (BYTE) '\0'};
static BYTE mng_vpAg[5]={118, 112,  65, 103, (BYTE) '\0'};
static BYTE mng_pHYs[5]={112,  72,  89, 115, (BYTE) '\0'};
static BYTE mng_sBIT[5]={115,  66,  73,  84, (BYTE) '\0'};
static BYTE mng_sRGB[5]={115,  82,  71,  66, (BYTE) '\0'};
static BYTE mng_tRNS[5]={116,  82,  78,  83, (BYTE) '\0'};

#if defined(JNG_SUPPORTED)
static BYTE mng_IDAT[5]={ 73,  68,  65,  84, (BYTE) '\0'};
static BYTE mng_JDAT[5]={ 74,  68,  65,  84, (BYTE) '\0'};
static BYTE mng_JDAA[5]={ 74,  68,  65,  65, (BYTE) '\0'};
static BYTE mng_JdAA[5]={ 74, 100,  65,  65, (BYTE) '\0'};
static BYTE mng_JSEP[5]={ 74,  83,  69,  80, (BYTE) '\0'};
static BYTE mng_oFFs[5]={111,  70,  70, 115, (BYTE) '\0'};
#endif

static BYTE mng_hIST[5]={104,  73,  83,  84, (BYTE) '\0'};
static BYTE mng_iTXt[5]={105,  84,  88, 116, (BYTE) '\0'};
static BYTE mng_sPLT[5]={115,  80,  76,  84, (BYTE) '\0'};
static BYTE mng_sTER[5]={115,  84,  69,  82, (BYTE) '\0'};
static BYTE mng_tEXt[5]={116,  69,  88, 116, (BYTE) '\0'};
static BYTE mng_tIME[5]={116,  73,  77,  69, (BYTE) '\0'};
static BYTE mng_zTXt[5]={122,  84,  88, 116, (BYTE) '\0'};


// --------------------------------------------------------------------------

/**
Convert a chunk name to a unique ID
*/
static eChunckType 
mng_GetChunckType(const BYTE *mChunkName) {
	if(memcmp(mChunkName, mng_MHDR, 4) == 0) {
		return MHDR;
	}
	if(memcmp(mChunkName, mng_LOOP, 4) == 0) {
		return LOOP;
	}
	if(memcmp(mChunkName, mng_DEFI, 4) == 0) {
		return DEFI;
	}
	if(memcmp(mChunkName, mng_PLTE, 4) == 0) {
		return PLTE;
	}
	if(memcmp(mChunkName, mng_tRNS, 4) == 0) {
		return tRNS;
	}
	if(memcmp(mChunkName, mng_IHDR, 4) == 0) {
		return IHDR;
	}
	if(memcmp(mChunkName, mng_JHDR, 4) == 0) {
		return JHDR;
	}
	if(memcmp(mChunkName, mng_MEND, 4) == 0) {
		return MEND;
	}
	if(memcmp(mChunkName, mng_IEND, 4) == 0) {
		return IEND;
	}
	if(memcmp(mChunkName, mng_JDAT, 4) == 0) {
		return JDAT;
	}
	if(memcmp(mChunkName, mng_IDAT, 4) == 0) {
		return IDAT;
	}
	if(memcmp(mChunkName, mng_JDAA, 4) == 0) {
		return JDAA;
	}
	if(memcmp(mChunkName, mng_gAMA, 4) == 0) {
		return gAMA;
	}
	if(memcmp(mChunkName, mng_pHYs, 4) == 0) {
		return pHYs;
	}
	if(memcmp(mChunkName, mng_bKGD, 4) == 0) {
		return bKGD;
	}
	if(memcmp(mChunkName, mng_tEXt, 4) == 0) {
		return tEXt;
	}

	return UNKNOWN_CHUNCK;
}

inline void
mng_SwapShort(WORD *sp) {
#ifndef FREEIMAGE_BIGENDIAN
	SwapShort(sp);
#endif
}

inline void
mng_SwapLong(DWORD *lp) {
#ifndef FREEIMAGE_BIGENDIAN
	SwapLong(lp);
#endif
}

/**
Returns the size, in bytes, of a FreeImageIO stream, from the current position. 
*/
static long
mng_LOF(FreeImageIO *io, fi_handle handle) {
	long start_pos = io->tell_proc(handle);
	io->seek_proc(handle, 0, SEEK_END);
	long file_length = io->tell_proc(handle);
	io->seek_proc(handle, start_pos, SEEK_SET);
	return file_length;
}

/**
Count the number of bytes in a PNG stream, from IHDR to IEND. 
If successful, the stream position, as given by io->tell_proc(handle), 
should be the end of the PNG stream at the return of the function. 
@param io
@param handle
@param inPos
@param m_TotalBytesOfChunks
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
mng_CountPNGChunks(FreeImageIO *io, fi_handle handle, long inPos, unsigned *m_TotalBytesOfChunks) {
	long mLOF;
	long mPos;
	BOOL mEnd = FALSE;
	DWORD mLength = 0;
	BYTE mChunkName[5];

	*m_TotalBytesOfChunks = 0;

	// get the length of the file
	mLOF = mng_LOF(io, handle);

	// go to the start of the file
	io->seek_proc(handle, inPos, SEEK_SET);

	try {
		// parse chunks
		while(mEnd == FALSE) {
			// chunk length
			mPos = io->tell_proc(handle);
			if(mPos + 4 > mLOF) {
				throw(1);
			}
			io->read_proc(&mLength, 1, 4, handle);
			mng_SwapLong(&mLength);
			// chunk name
			mPos = io->tell_proc(handle);
			if(mPos + 4 > mLOF) {
				throw(1);
			}
			io->read_proc(&mChunkName[0], 1, 4, handle);
			mChunkName[4] = '\0';

			// go to next chunk
			mPos = io->tell_proc(handle);
			// 4 = size of the CRC
			if(mPos + (long)mLength + 4 > mLOF) {
				throw(1);
			}
			io->seek_proc(handle, mLength + 4, SEEK_CUR);

			switch( mng_GetChunckType(mChunkName) ) {
				case IHDR:
					if(mLength != 13) {
						throw(1);
					}
					break;
				
				case IEND:
					mEnd = TRUE;		
					// the length below includes 4 bytes CRC, but no bytes for Length
					*m_TotalBytesOfChunks = io->tell_proc(handle) - inPos;
					break;		
				
				case UNKNOWN_CHUNCK:
				default:
					break;
			}

		} // while(!mEnd)

		return TRUE;
		
	} catch(int) {
		return FALSE;
	}
}

/**
Retrieve the position of a chunk in a PNG stream
@param hPngMemory PNG stream handle
@param chunk_name Name of the chunk to be found
@param offset Start of the search in the stream
@param start_pos [returned value] Start position of the chunk
@param next_pos [returned value] Start position of the next chunk
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
mng_FindChunk(FIMEMORY *hPngMemory, BYTE *chunk_name, long offset, DWORD *start_pos, DWORD *next_pos) {
	BOOL mEnd = FALSE;
	DWORD mLength = 0;

	BYTE *data = NULL;
	DWORD size_in_bytes = 0;

	*start_pos = 0;
	*next_pos = 0;

	// get a pointer to the stream buffer
	FreeImage_AcquireMemory(hPngMemory, &data, &size_in_bytes);
	if(!(data && size_in_bytes) || (size_in_bytes < 20) || (size_in_bytes - offset < 20)) {
		// not enough space to read a signature(8 bytes) + a chunk(at least 12 bytes)
		return FALSE;
	}

	try {
	
		// skip the signature and/or any following chunk(s)
		DWORD chunk_pos = offset;

		while(1) {
			// get chunk length
			if(chunk_pos + 4 > size_in_bytes) {
				break;
			}

			memcpy(&mLength, &data[chunk_pos], 4);
			mng_SwapLong(&mLength);
			chunk_pos += 4;

			const DWORD next_chunk_pos = chunk_pos + 4 + mLength + 4;
			if(next_chunk_pos > size_in_bytes) {
				break;
			}

			// get chunk name
			if(memcmp(&data[chunk_pos], chunk_name, 4) == 0) {
				chunk_pos -= 4;	// found chunk
				*start_pos = chunk_pos;
				*next_pos = next_chunk_pos;
				return TRUE;
			}
			
			chunk_pos = next_chunk_pos;
		}

		return FALSE;

	} catch(int) {
		return FALSE;
	}
}

/**
Remove a chunk located at (start_pos, next_pos) in the PNG stream
@param hPngMemory PNG stream handle
@param start_pos Start position of the chunk
@param next_pos Start position of the next chunk
@return Returns TRUE if successfull, returns FALSE otherwise
*/
static BOOL 
mng_CopyRemoveChunks(FIMEMORY *hPngMemory, DWORD start_pos, DWORD next_pos) {
	BYTE *data = NULL;
	DWORD size_in_bytes = 0;

	// length of the chunk to remove
	DWORD chunk_length = next_pos - start_pos;
	if(chunk_length == 0) {
		return TRUE;
	}

	// get a pointer to the stream buffer
	FreeImage_AcquireMemory(hPngMemory, &data, &size_in_bytes);
	if(!(data && size_in_bytes) || (size_in_bytes < 20) || (chunk_length >= size_in_bytes)) {
		// not enough space to read a signature(8 bytes) + a chunk(at least 12 bytes)
		return FALSE;
	}
	
	// new file length
	unsigned buffer_size = size_in_bytes + chunk_length;

	BYTE *buffer = (BYTE*)malloc(buffer_size * sizeof(BYTE));
	if(!buffer) {
		return FALSE;
	}
	memcpy(&buffer[0], &data[0], start_pos);
	memcpy(&buffer[start_pos], &data[next_pos], size_in_bytes - next_pos);

	// seek to the start of the stream
	FreeImage_SeekMemory(hPngMemory, 0, SEEK_SET);
	// re-write the stream
	FreeImage_WriteMemory(buffer, 1, buffer_size, hPngMemory);

	free(buffer);

	return TRUE;
}

/**
Insert a chunk just before the inNextChunkName chunk
@param hPngMemory PNG stream handle
@param start_pos Start position of the inNextChunkName chunk
@param next_pos Start position of the next chunk
@return Returns TRUE if successfull, returns FALSE otherwise
*/
static BOOL 
mng_CopyInsertChunks(FIMEMORY *hPngMemory, BYTE *inNextChunkName, BYTE *inInsertChunk, DWORD inChunkLength, DWORD start_pos, DWORD next_pos) {
	BYTE *data = NULL;
	DWORD size_in_bytes = 0;

	// length of the chunk to check
	DWORD chunk_length = next_pos - start_pos;
	if(chunk_length == 0) {
		return TRUE;
	}

	// get a pointer to the stream buffer
	FreeImage_AcquireMemory(hPngMemory, &data, &size_in_bytes);
	if(!(data && size_in_bytes) || (size_in_bytes < 20) || (chunk_length >= size_in_bytes)) {
		// not enough space to read a signature(8 bytes) + a chunk(at least 12 bytes)
		return FALSE;
	}
	
	// new file length
	unsigned buffer_size = inChunkLength + size_in_bytes;

	BYTE *buffer = (BYTE*)malloc(buffer_size * sizeof(BYTE));
	if(!buffer) {
		return FALSE;
	}
	unsigned p = 0;
	memcpy(&buffer[p], &data[0], start_pos);
	p += start_pos;
	memcpy(&buffer[p], inInsertChunk, inChunkLength);
	p += inChunkLength;
	memcpy(&buffer[p], &data[start_pos], size_in_bytes - start_pos);

	// seek to the start of the stream
	FreeImage_SeekMemory(hPngMemory, 0, SEEK_SET);
	// re-write the stream
	FreeImage_WriteMemory(buffer, 1, buffer_size, hPngMemory);

	free(buffer);

	return TRUE;
}

static BOOL 
mng_RemoveChunk(FIMEMORY *hPngMemory, BYTE *chunk_name) {
	BOOL bResult = FALSE;

	DWORD start_pos = 0;
	DWORD next_pos = 0;
	
	bResult = mng_FindChunk(hPngMemory, chunk_name, 8, &start_pos, &next_pos);
	if(!bResult) return FALSE;

	bResult = mng_CopyRemoveChunks(hPngMemory, start_pos, next_pos);
	if(!bResult) return FALSE;

	return TRUE;
}

static BOOL 
mng_InsertChunk(FIMEMORY *hPngMemory, BYTE *inNextChunkName, BYTE *inInsertChunk, unsigned chunk_length) {
	BOOL bResult = FALSE;

	DWORD start_pos = 0;
	DWORD next_pos = 0;
	
	bResult = mng_FindChunk(hPngMemory, inNextChunkName, 8, &start_pos, &next_pos);
	if(!bResult) return FALSE;

	bResult = mng_CopyInsertChunks(hPngMemory, inNextChunkName, inInsertChunk, chunk_length, start_pos, next_pos);
	if(!bResult) return FALSE;

	return TRUE;
}

static FIBITMAP* 
mng_LoadFromMemoryHandle(FIMEMORY *hmem, int flags = 0) {
	long offset = 0;
	FIBITMAP *dib = NULL;

	if(hmem) {
		// seek to the start of the stream
		FreeImage_SeekMemory(hmem, offset, SEEK_SET);

		// check the file signature and deduce its format
		// (the second argument is currently not used by FreeImage)
		FREE_IMAGE_FORMAT fif = FreeImage_GetFileTypeFromMemory(hmem, 0);
		if(fif != FIF_UNKNOWN) {
			dib = FreeImage_LoadFromMemory(fif, hmem, flags);
		}
	}
	
	return dib;
}

/**
Write a chunk in a PNG stream from the current position. 
@param chunk_name Name of the chunk
@param chunk_data Chunk array
@param length Chunk length
@param hPngMemory PNG stream handle
*/
static void
mng_WriteChunk(BYTE *chunk_name, BYTE *chunk_data, DWORD length, FIMEMORY *hPngMemory) {
	DWORD crc_file = 0;
	// write a PNG chunk ...
	// - length
	mng_SwapLong(&length);
	FreeImage_WriteMemory(&length, 1, 4, hPngMemory);
	mng_SwapLong(&length);
	// - chunk name
	FreeImage_WriteMemory(chunk_name, 1, 4, hPngMemory);
	if(chunk_data && length) {
		// - chunk data
		FreeImage_WriteMemory(chunk_data, 1, length, hPngMemory);
		// - crc
		crc_file = FreeImage_ZLibCRC32(0, chunk_name, 4);
		crc_file = FreeImage_ZLibCRC32(crc_file, chunk_data, length);
		mng_SwapLong(&crc_file);
		FreeImage_WriteMemory(&crc_file, 1, 4, hPngMemory);
	} else {
		// - crc
		crc_file = FreeImage_ZLibCRC32(0, chunk_name, 4);
		mng_SwapLong(&crc_file);
		FreeImage_WriteMemory(&crc_file, 1, 4, hPngMemory);
	}

}

/**
Wrap a IDAT chunk as a PNG stream. 
The stream has the structure { g_png_signature, IHDR, IDAT, IEND }
The image is assumed to be a greyscale image. 

@param jng_width Image width
@param jng_height Image height
@param jng_alpha_sample_depth Bits per pixel
@param mChunk PNG grayscale IDAT format
@param mLength IDAT chunk length
@param hPngMemory Output memory stream
*/
static void 
mng_WritePNGStream(DWORD jng_width, DWORD jng_height, BYTE jng_alpha_sample_depth, BYTE *mChunk, DWORD mLength, FIMEMORY *hPngMemory) {
	// PNG grayscale IDAT format

	BYTE data[14];

	// wrap the IDAT chunk as a PNG stream

	// write PNG file signature
	FreeImage_WriteMemory(g_png_signature, 1, 8, hPngMemory);

	// write a IHDR chunk ...
	/*
	The IHDR chunk must appear FIRST. It contains:
	Width:              4 bytes
	Height:             4 bytes
	Bit depth:          1 byte
	Color type:         1 byte
	Compression method: 1 byte
	Filter method:      1 byte
	Interlace method:   1 byte
	*/
	// - chunk data
	mng_SwapLong(&jng_width);
	mng_SwapLong(&jng_height);
	memcpy(&data[0], &jng_width, 4);
	memcpy(&data[4], &jng_height, 4);
	mng_SwapLong(&jng_width);
	mng_SwapLong(&jng_height);
	data[8] = jng_alpha_sample_depth;
	data[9] = 0;	// color_type gray (jng_color_type)
	data[10] = 0;	// compression method 0 (jng_alpha_compression_method)
	data[11] = 0;	// filter_method 0 (jng_alpha_filter_method)
	data[12] = 0;	// interlace_method 0 (jng_alpha_interlace_method)

	mng_WriteChunk(mng_IHDR, &data[0], 13, hPngMemory);

	// write a IDAT chunk ...
	mng_WriteChunk(mng_IDAT, mChunk, mLength, hPngMemory);

	// write a IEND chunk ...
	mng_WriteChunk(mng_IEND, NULL, 0, hPngMemory);

}

// --------------------------------------------------------------------------

/**
Build and set a FITAG whose type is FIDT_ASCII. 
The tag must be destroyed by the caller using FreeImage_DeleteTag.
@param model Metadata model to be filled
@param dib Image to be filled
@param key Tag key
@param value Tag value
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
mng_SetKeyValue(FREE_IMAGE_MDMODEL model, FIBITMAP *dib, const char *key, const char *value) {
	if(!dib || !key || !value) {
		return FALSE;
	}
	// create a tag
	FITAG *tag = FreeImage_CreateTag();
	if(tag) {
		BOOL bSuccess = TRUE;
		// fill the tag
		DWORD tag_length = (DWORD)(strlen(value) + 1);
		bSuccess &= FreeImage_SetTagKey(tag, key);
		bSuccess &= FreeImage_SetTagLength(tag, tag_length);
		bSuccess &= FreeImage_SetTagCount(tag, tag_length);
		bSuccess &= FreeImage_SetTagType(tag, FIDT_ASCII);
		bSuccess &= FreeImage_SetTagValue(tag, value);
		if(bSuccess) {
			// set the tag
			FreeImage_SetMetadata(model, dib, FreeImage_GetTagKey(tag), tag);
		}
		FreeImage_DeleteTag(tag);
		return bSuccess;
	}

	return FALSE;
}

/**
Read a tEXt chunk and extract the key/value pair. 
@param key_value_pair [returned value] Array of key/value pairs
@param mChunk Chunk data
@param mLength Chunk length
@return Returns TRUE if successful, returns FALSE otherwise
*/
static BOOL 
mng_SetMetadata_tEXt(tEXtMAP &key_value_pair, const BYTE *mChunk, DWORD mLength) {
	std::string key;
	std::string value;
	BYTE *buffer = (BYTE*)malloc(mLength * sizeof(BYTE));
	if(!buffer) {
		return FALSE;
	}
	DWORD pos = 0;

	memset(buffer, 0, mLength * sizeof(BYTE));

	for(DWORD i = 0; i < mLength; i++) {
		buffer[pos++] = mChunk[i];
		if(mChunk[i] == '\0') {
			if(key.size() == 0) {
				key = (char*)buffer;
				pos = 0;
				memset(buffer, 0, mLength * sizeof(BYTE));
			} else {
				break;
			}
		}
	}
	value = (char*)buffer;
	free(buffer);

	key_value_pair[key] = value;

	return TRUE;
}

// --------------------------------------------------------------------------

/**
Load a FIBITMAP from a MNG or a JNG stream
@param format_id ID of the caller
@param io Stream i/o functions
@param handle Stream handle
@param Offset Start of the first chunk
@param flags Loading flags
@return Returns a dib if successful, returns NULL otherwise
*/
FIBITMAP* 
mng_ReadChunks(int format_id, FreeImageIO *io, fi_handle handle, long Offset, int flags = 0) {
	DWORD mLength = 0;
	BYTE mChunkName[5];
	BYTE *mChunk = NULL;
	DWORD crc_file;
	long LastOffset;
	long mOrigPos;
	BYTE *PLTE_file_chunk = NULL;	// whole PLTE chunk (lentgh, name, array, crc)
	DWORD PLTE_file_size = 0;		// size of PLTE chunk

	BOOL m_HasGlobalPalette = FALSE; // may turn to TRUE in PLTE chunk
	unsigned m_TotalBytesOfChunks = 0;
	FIBITMAP *dib = NULL;
	FIBITMAP *dib_alpha = NULL;

	FIMEMORY *hJpegMemory = NULL;
	FIMEMORY *hPngMemory = NULL;
	FIMEMORY *hIDATMemory = NULL;

	// ---
	DWORD jng_width = 0;
	DWORD jng_height = 0;
	BYTE jng_color_type = 0;
	BYTE jng_image_sample_depth = 0;
	BYTE jng_image_compression_method = 0;

	BYTE jng_alpha_sample_depth = 0;
	BYTE jng_alpha_compression_method = 0;
	BYTE jng_alpha_filter_method = 0;
	BYTE jng_alpha_interlace_method = 0;

	DWORD mng_frame_width = 0;
	DWORD mng_frame_height = 0;
	DWORD mng_ticks_per_second = 0;
	DWORD mng_nominal_layer_count = 0;
	DWORD mng_nominal_frame_count = 0;
	DWORD mng_nominal_play_time = 0;
	DWORD mng_simplicity_profile = 0;


	DWORD res_x = 2835;	// 72 dpi
	DWORD res_y = 2835;	// 72 dpi
	RGBQUAD rgbBkColor = {0, 0, 0, 0};
	WORD bk_red, bk_green, bk_blue;
	BOOL hasBkColor = FALSE;
	BOOL mHasIDAT = FALSE;

	tEXtMAP key_value_pair;

	// ---

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;
	
	// get the file size
	const long mLOF = mng_LOF(io, handle);
	// go to the first chunk
	io->seek_proc(handle, Offset, SEEK_SET);

	try {
		BOOL mEnd = FALSE;

		while(mEnd == FALSE) {
			// start of the chunk
			LastOffset = io->tell_proc(handle);
			// read length
			mLength = 0;			
			io->read_proc(&mLength, 1, sizeof(mLength), handle);
			mng_SwapLong(&mLength);
			// read name			
			io->read_proc(&mChunkName[0], 1, 4, handle);
			mChunkName[4] = '\0';

			if(mLength > 0) {
				mChunk = (BYTE*)realloc(mChunk, mLength);
				if(!mChunk) {
					FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: out of memory", mChunkName);
					throw (const char*)NULL;
				}				
				Offset = io->tell_proc(handle);
				if(Offset + (long)mLength > mLOF) {
					FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: unexpected end of file", mChunkName);
					throw (const char*)NULL;
				}
				// read chunk
				io->read_proc(mChunk, 1, mLength, handle);
			}
			// read crc
			io->read_proc(&crc_file, 1, sizeof(crc_file), handle);
			mng_SwapLong(&crc_file);
			// check crc
			DWORD crc_check = FreeImage_ZLibCRC32(0, &mChunkName[0], 4);
			crc_check = FreeImage_ZLibCRC32(crc_check, mChunk, mLength);
			if(crc_check != crc_file) {
				FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: bad CRC", mChunkName);
				throw (const char*)NULL;
			}		

			switch( mng_GetChunckType(mChunkName) ) {
				case MHDR:
					// The MHDR chunk is always first in all MNG datastreams except for those 
					// that consist of a single PNG or JNG datastream with a PNG or JNG signature. 
					if(mLength == 28) {
						memcpy(&mng_frame_width, &mChunk[0], 4);
						memcpy(&mng_frame_height, &mChunk[4], 4);
						memcpy(&mng_ticks_per_second, &mChunk[8], 4);
						memcpy(&mng_nominal_layer_count, &mChunk[12], 4);
						memcpy(&mng_nominal_frame_count, &mChunk[16], 4);
						memcpy(&mng_nominal_play_time, &mChunk[20], 4);
						memcpy(&mng_simplicity_profile, &mChunk[24], 4);

						mng_SwapLong(&mng_frame_width);
						mng_SwapLong(&mng_frame_height);
						mng_SwapLong(&mng_ticks_per_second);
						mng_SwapLong(&mng_nominal_layer_count);
						mng_SwapLong(&mng_nominal_frame_count);
						mng_SwapLong(&mng_nominal_play_time);
						mng_SwapLong(&mng_simplicity_profile);

					} else {
						FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: size is %d instead of 28", mChunkName, mLength);
					}
					break;

				case MEND:
					mEnd = TRUE;
					break;

				case LOOP:
				case ENDL:
					break;
				case DEFI:
					break;
				case SAVE:
				case SEEK:
				case TERM:
					break;
				case BACK:
					break;

					// Global "PLTE" and "tRNS" (if any).  PNG "PLTE" will be of 0 byte, as it uses global data.
				case PLTE:	// Global
					m_HasGlobalPalette = TRUE;
					PLTE_file_size = mLength + 12; // (lentgh, name, array, crc) = (4, 4, mLength, 4)
					PLTE_file_chunk = (BYTE*)realloc(PLTE_file_chunk, PLTE_file_size);
					if(!PLTE_file_chunk) {
						FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: out of memory", mChunkName);
						throw (const char*)NULL;
					} else {
						mOrigPos = io->tell_proc(handle);
						// seek to the start of the chunk
						io->seek_proc(handle, LastOffset, SEEK_SET);
						// load the whole chunk
						io->read_proc(PLTE_file_chunk, 1, PLTE_file_size, handle);
						// go to the start of the next chunk
						io->seek_proc(handle, mOrigPos, SEEK_SET);
					}
					break;

				case tRNS:	// Global
					break;
					
				case IHDR:
					Offset = LastOffset;
					// parse the PNG file and get its file size
					if(mng_CountPNGChunks(io, handle, Offset, &m_TotalBytesOfChunks) == FALSE) {
						// reach an unexpected end of file
						mEnd = TRUE;
						FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: unexpected end of PNG file", mChunkName);
						break;
					}
					
					// wrap the { IHDR, ..., IEND } chunks as a PNG stream
					if(hPngMemory == NULL) {
						hPngMemory = FreeImage_OpenMemory();
					}

					mOrigPos = io->tell_proc(handle);

					// write PNG file signature
					FreeImage_SeekMemory(hPngMemory, 0, SEEK_SET);
					FreeImage_WriteMemory(g_png_signature, 1, 8, hPngMemory);

					mChunk = (BYTE*)realloc(mChunk, m_TotalBytesOfChunks);
					if(!mChunk) {
						FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: out of memory", mChunkName);
						throw (const char*)NULL;
					}
					
					// on calling CountPNGChunks earlier, we were in Offset pos,
					// go back there
					io->seek_proc(handle, Offset, SEEK_SET);
					io->read_proc(mChunk, 1, m_TotalBytesOfChunks, handle);
					// Put back to original pos
					io->seek_proc(handle, mOrigPos, SEEK_SET);
					// write the PNG chunks
					FreeImage_WriteMemory(mChunk, 1, m_TotalBytesOfChunks, hPngMemory);

					// plug in global PLTE if local PLTE exists
					if(m_HasGlobalPalette) {
						// ensure we remove some local chunks, so that global
						// "PLTE" can be inserted right before "IDAT".
						mng_RemoveChunk(hPngMemory, mng_PLTE);
						mng_RemoveChunk(hPngMemory, mng_tRNS);
						mng_RemoveChunk(hPngMemory, mng_bKGD);
						// insert global "PLTE" chunk in its entirety before "IDAT"
						mng_InsertChunk(hPngMemory, mng_IDAT, PLTE_file_chunk, PLTE_file_size);
					}

					if(dib) FreeImage_Unload(dib);
					dib = mng_LoadFromMemoryHandle(hPngMemory, flags);

					// stop after the first image
					mEnd = TRUE;
					break;

				case JHDR:
					if(mLength == 16) {
						memcpy(&jng_width, &mChunk[0], 4);
						memcpy(&jng_height, &mChunk[4], 4);
						mng_SwapLong(&jng_width);
						mng_SwapLong(&jng_height);

						jng_color_type = mChunk[8];
						jng_image_sample_depth = mChunk[9];
						jng_image_compression_method = mChunk[10];
						BYTE jng_image_interlace_method = mChunk[11];

						jng_alpha_sample_depth = mChunk[12];
						jng_alpha_compression_method = mChunk[13];
						jng_alpha_filter_method = mChunk[14];
						jng_alpha_interlace_method = mChunk[15];
					} else {
						FreeImage_OutputMessageProc(format_id, "Error while parsing %s chunk: invalid chunk length", mChunkName);
						throw (const char*)NULL;
					}
					break;

				case JDAT:
					if(hJpegMemory == NULL) {
						hJpegMemory = FreeImage_OpenMemory();
					}
					// as there may be several JDAT chunks, concatenate them
					FreeImage_WriteMemory(mChunk, 1, mLength, hJpegMemory);
					break;

				case IDAT:
					if(!header_only && (jng_alpha_compression_method == 0)) {
						// PNG grayscale IDAT format
						if(hIDATMemory == NULL) {
							hIDATMemory = FreeImage_OpenMemory();
							mHasIDAT = TRUE;
						}
						// as there may be several IDAT chunks, concatenate them
						FreeImage_WriteMemory(mChunk, 1, mLength, hIDATMemory);
					}
					break;

				case IEND:
					if(!hJpegMemory) {
						mEnd = TRUE;
						break;
					}
					// load the JPEG
					if(dib) FreeImage_Unload(dib);
					dib = mng_LoadFromMemoryHandle(hJpegMemory, flags);

					// load the PNG alpha layer
					if(mHasIDAT) {
						BYTE *data = NULL;
						DWORD size_in_bytes = 0;

						// get a pointer to the IDAT buffer
						FreeImage_AcquireMemory(hIDATMemory, &data, &size_in_bytes);
						if(data && size_in_bytes) {
							// wrap the IDAT chunk as a PNG stream
							if(hPngMemory == NULL) {
								hPngMemory = FreeImage_OpenMemory();
							}
							mng_WritePNGStream(jng_width, jng_height, jng_alpha_sample_depth, data, size_in_bytes, hPngMemory);
							// load the PNG
							if(dib_alpha) FreeImage_Unload(dib_alpha);
							dib_alpha = mng_LoadFromMemoryHandle(hPngMemory, flags);
						}
					}
					// stop the parsing
					mEnd = TRUE;
					break;

				case JDAA:
					break;

				case gAMA:
					break;

				case pHYs:
					// unit is pixels per meter
					memcpy(&res_x, &mChunk[0], 4);
					mng_SwapLong(&res_x);
					memcpy(&res_y, &mChunk[4], 4);
					mng_SwapLong(&res_y);
					break;

				case bKGD:
					memcpy(&bk_red, &mChunk[0], 2);
					mng_SwapShort(&bk_red);
					rgbBkColor.rgbRed = (BYTE)bk_red;
					memcpy(&bk_green, &mChunk[2], 2);
					mng_SwapShort(&bk_green);
					rgbBkColor.rgbGreen = (BYTE)bk_green;
					memcpy(&bk_blue, &mChunk[4], 2);
					mng_SwapShort(&bk_blue);
					rgbBkColor.rgbBlue = (BYTE)bk_blue;
					hasBkColor = TRUE;
					break;
				
				case tEXt:
					mng_SetMetadata_tEXt(key_value_pair, mChunk, mLength);
					break;

				case UNKNOWN_CHUNCK:
				default:
					break;


			} // switch( GetChunckType )
		} // while(!mEnd)

		FreeImage_CloseMemory(hJpegMemory);
		FreeImage_CloseMemory(hPngMemory);
		FreeImage_CloseMemory(hIDATMemory);
		free(mChunk);
		free(PLTE_file_chunk);

		// convert to 32-bit if a transparent layer is available
		if(!header_only && dib_alpha) {
			FIBITMAP *dst = FreeImage_ConvertTo32Bits(dib);
			if((FreeImage_GetBPP(dib_alpha) == 8) && (FreeImage_GetImageType(dib_alpha) == FIT_BITMAP)) {
				FreeImage_SetChannel(dst, dib_alpha, FICC_ALPHA);
			} else {
				FIBITMAP *dst_alpha = FreeImage_ConvertTo8Bits(dib_alpha);
				FreeImage_SetChannel(dst, dst_alpha, FICC_ALPHA);
				FreeImage_Unload(dst_alpha);
			}			
			FreeImage_Unload(dib);
			dib = dst;
		}
		FreeImage_Unload(dib_alpha);

		if(dib) {
			// set metadata
			FreeImage_SetDotsPerMeterX(dib, res_x);
			FreeImage_SetDotsPerMeterY(dib, res_y);
			if(hasBkColor) {
				FreeImage_SetBackgroundColor(dib, &rgbBkColor);
			}
			if(key_value_pair.size()) {
				for(tEXtMAP::iterator j = key_value_pair.begin(); j != key_value_pair.end(); j++) {
					std::string key = (*j).first;
					std::string value = (*j).second;
					mng_SetKeyValue(FIMD_COMMENTS, dib, key.c_str(), value.c_str());
				}
			}
		}
			
		return dib;

	} catch(const char *text) {
		FreeImage_CloseMemory(hJpegMemory);
		FreeImage_CloseMemory(hPngMemory);
		FreeImage_CloseMemory(hIDATMemory);
		free(mChunk);
		free(PLTE_file_chunk);
		FreeImage_Unload(dib);
		FreeImage_Unload(dib_alpha);
		if(text) {
			FreeImage_OutputMessageProc(format_id, text);
		}
		return NULL;
	}
}

// --------------------------------------------------------------------------

/**
Write a FIBITMAP to a JNG stream
@param format_id ID of the caller
@param io Stream i/o functions
@param dib Image to be saved
@param handle Stream handle
@param flags Saving flags
@return Returns TRUE if successful, returns FALSE otherwise
*/
BOOL 
mng_WriteJNG(int format_id, FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int flags) {
	DWORD jng_width = 0;
	DWORD jng_height = 0;
	BYTE jng_color_type = 0;
	BYTE jng_image_sample_depth = 8;
	BYTE jng_image_compression_method = 8;	//  8: ISO-10918-1 Huffman-coded baseline JPEG.
	BYTE jng_image_interlace_method = 0;

	BYTE jng_alpha_sample_depth = 0;
	BYTE jng_alpha_compression_method = 0;
	BYTE jng_alpha_filter_method = 0;
	BYTE jng_alpha_interlace_method = 0;

	BYTE buffer[16];

	FIMEMORY *hJngMemory = NULL;
	FIMEMORY *hJpegMemory = NULL;
	FIMEMORY *hPngMemory = NULL;

	FIBITMAP *dib_rgb = NULL;
	FIBITMAP *dib_alpha = NULL;

	if(!dib || (FreeImage_GetImageType(dib) != FIT_BITMAP)) {
		return FALSE;
	}

	unsigned bpp = FreeImage_GetBPP(dib);

	switch(bpp) {
		case 8:
			if(FreeImage_GetColorType(dib) == FIC_MINISBLACK) {
				dib_rgb = dib;
				jng_color_type = MNG_COLORTYPE_JPEGGRAY;
			} else {
				// JPEG plugin will convert other types (FIC_MINISWHITE, FIC_PALETTE) to 24-bit on the fly
				//dib_rgb = FreeImage_ConvertTo24Bits(dib);
				dib_rgb = dib;
				jng_color_type = MNG_COLORTYPE_JPEGCOLOR;

			}
			break;
		case 24:
			dib_rgb = dib;
			jng_color_type = MNG_COLORTYPE_JPEGCOLOR;
			break;
		case 32:
			dib_rgb = FreeImage_ConvertTo24Bits(dib);
			jng_color_type = MNG_COLORTYPE_JPEGCOLORA;
			jng_alpha_sample_depth = 8;
			break;
		default:
			return FALSE;
	}

	jng_width = (DWORD)FreeImage_GetWidth(dib);
	jng_height = (DWORD)FreeImage_GetHeight(dib);

	try {
		hJngMemory = FreeImage_OpenMemory();

		// --- write JNG file signature ---
		FreeImage_WriteMemory(g_jng_signature, 1, 8, hJngMemory);

		// --- write a JHDR chunk ---
		SwapLong(&jng_width);
		SwapLong(&jng_height);
		memcpy(&buffer[0], &jng_width, 4);
		memcpy(&buffer[4], &jng_height, 4);
		SwapLong(&jng_width);
		SwapLong(&jng_height);
		buffer[8] = jng_color_type;
		buffer[9] = jng_image_sample_depth;
		buffer[10] = jng_image_compression_method;
		buffer[11] = jng_image_interlace_method;
		buffer[12] = jng_alpha_sample_depth;
		buffer[13] = jng_alpha_compression_method;
		buffer[14] = jng_alpha_filter_method;
		buffer[15] = jng_alpha_interlace_method;
		mng_WriteChunk(mng_JHDR, &buffer[0], 16, hJngMemory);

		// --- write a sequence of JDAT chunks ---
		hJpegMemory = FreeImage_OpenMemory();
		flags |= JPEG_BASELINE;
		if(!FreeImage_SaveToMemory(FIF_JPEG, dib_rgb, hJpegMemory, flags)) {
			throw (const char*)NULL;
		}
		if(dib_rgb != dib) {
			FreeImage_Unload(dib_rgb);
			dib_rgb = NULL;
		}
		{
			BYTE *jpeg_data = NULL;
			DWORD size_in_bytes = 0;
			
			// get a pointer to the stream buffer
			FreeImage_AcquireMemory(hJpegMemory, &jpeg_data, &size_in_bytes);
			// write chunks
			for(DWORD k = 0; k < size_in_bytes;) {
				DWORD bytes_left = size_in_bytes - k;
				DWORD chunk_size = MIN(JPEG_CHUNK_SIZE, bytes_left);
				mng_WriteChunk(mng_JDAT, &jpeg_data[k], chunk_size, hJngMemory);
				k += chunk_size;
			}
		}
		FreeImage_CloseMemory(hJpegMemory);
		hJpegMemory = NULL;

		// --- write alpha layer as a sequence of IDAT chunk ---
		if((bpp == 32) && (jng_color_type == MNG_COLORTYPE_JPEGCOLORA)) {
			dib_alpha = FreeImage_GetChannel(dib, FICC_ALPHA);

			hPngMemory = FreeImage_OpenMemory();
			if(!FreeImage_SaveToMemory(FIF_PNG, dib_alpha, hPngMemory, PNG_DEFAULT)) {
				throw (const char*)NULL;
			}
			FreeImage_Unload(dib_alpha);
			dib_alpha = NULL;
			// get the IDAT chunk
			{		
				BOOL bResult = FALSE;
				DWORD start_pos = 0;
				DWORD next_pos = 0;
				long offset = 8;
				
				do {
					// find the next IDAT chunk from 'offset' position
					bResult = mng_FindChunk(hPngMemory, mng_IDAT, offset, &start_pos, &next_pos);
					if(!bResult) break;
					
					BYTE *png_data = NULL;
					DWORD size_in_bytes = 0;
					
					// get a pointer to the stream buffer
					FreeImage_AcquireMemory(hPngMemory, &png_data, &size_in_bytes);
					// write the IDAT chunk
					mng_WriteChunk(mng_IDAT, &png_data[start_pos+8], next_pos - start_pos - 12, hJngMemory);

					offset = next_pos;

				} while(bResult);
			}

			FreeImage_CloseMemory(hPngMemory);
			hPngMemory = NULL;
		}

		// --- write a IEND chunk ---
		mng_WriteChunk(mng_IEND, NULL, 0, hJngMemory);

		// write the JNG on output stream
		{
			BYTE *jng_data = NULL;
			DWORD size_in_bytes = 0;
			FreeImage_AcquireMemory(hJngMemory, &jng_data, &size_in_bytes);
			io->write_proc(jng_data, 1, size_in_bytes, handle);			
		}

		FreeImage_CloseMemory(hJngMemory);
		FreeImage_CloseMemory(hJpegMemory);
		FreeImage_CloseMemory(hPngMemory);

		return TRUE;

	} catch(const char *text) {
		FreeImage_CloseMemory(hJngMemory);
		FreeImage_CloseMemory(hJpegMemory);
		FreeImage_CloseMemory(hPngMemory);
		if(dib_rgb && (dib_rgb != dib)) {
			FreeImage_Unload(dib_rgb);
		}
		FreeImage_Unload(dib_alpha);
		if(text) {
			FreeImage_OutputMessageProc(format_id, text);
		}
		return FALSE;
	}
}

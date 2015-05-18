// ==========================================================
// SGI Loader
//
// Design and implementation by
// - Sherman Wilcox
// - Noam Gat
//
// References : 
// ------------
// - The SGI Image File Format, Version 1.0
// http://astronomy.swin.edu.au/~pbourke/dataformats/sgirgb/sgiversion.html
// - SGI RGB Image Format
// http://astronomy.swin.edu.au/~pbourke/dataformats/sgirgb/
//
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
//   Constants + headers
// ----------------------------------------------------------

#ifdef _WIN32
#pragma pack(push, 1)
#else
#pragma pack(1)
#endif

typedef struct tagSGIHeader {
	/** IRIS image file magic number. This should be decimal 474. */
	WORD magic;
	/** Storage format: 0 for uncompressed, 1 for RLE compression. */
	BYTE storage;
	/** Number of bytes per pixel channel. Legally 1 or 2. */
	BYTE bpc;
	/**
	Number of dimensions. Legally 1, 2, or 3. 
	1 means a single row, XSIZE long
	2 means a single 2D image
	3 means multiple 2D images
	*/
	WORD dimension;	
	/** X size in pixels */
	WORD xsize;
	/** Y size in pixels */
	WORD ysize;
	/**
	Number of channels. 
	1 indicates greyscale
	3 indicates RGB
	4 indicates RGB and Alpha
	*/
	WORD zsize;
	/** Minimum pixel value. This is the lowest pixel value in the image.*/
	LONG pixmin;
	/** Maximum pixel value. This is the highest pixel value in the image.*/
	LONG pixmax;
	/** Ignored. Normally set to 0. */
	char dummy[4];
	/** Image name. Must be null terminated, therefore at most 79 bytes. */
	char imagename[80];
	/** 
	Colormap ID. 
	0 - normal mode
	1 - dithered, 3 mits for red and green, 2 for blue, obsolete
	2 - index colour, obsolete
	3 - not an image but a colourmap
	*/
	LONG colormap;
	/** Ignored. Should be set to 0, makes the header 512 bytes. */
	char reserved[404];
} SGIHeader;

typedef struct tagRLEStatus {
  int cnt;
  int val;
} RLEStatus;

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

static const char *SGI_LESS_THAN_HEADER_LENGTH = "Incorrect header size";
static const char *SGI_16_BIT_COMPONENTS_NOT_SUPPORTED = "No 16 bit support";
static const char *SGI_COLORMAPS_NOT_SUPPORTED = "No colormap support";
static const char *SGI_EOF_IN_RLE_INDEX = "EOF in run length encoding";
static const char *SGI_EOF_IN_IMAGE_DATA = "EOF in image data";
static const char *SGI_INVALID_CHANNEL_COUNT = "Invalid channel count";

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

#ifndef FREEIMAGE_BIGENDIAN
static void 
SwapHeader(SGIHeader *header) {
	SwapShort(&header->magic);
	SwapShort(&header->dimension);
	SwapShort(&header->xsize);
	SwapShort(&header->ysize);
	SwapShort(&header->zsize);
	SwapLong((DWORD*)&header->pixmin);
	SwapLong((DWORD*)&header->pixmax);
	SwapLong((DWORD*)&header->colormap);
}
#endif

static int 
get_rlechar(FreeImageIO *io, fi_handle handle, RLEStatus *pstatus) {
	if (!pstatus->cnt) {
		int cnt = 0;
		while (0 == cnt) {
			BYTE packed = 0;
			if(io->read_proc(&packed, sizeof(BYTE), 1, handle) < 1) {
				return EOF;
			}
			cnt = packed;
		}
		if (cnt == EOF) {
			return EOF;
		}
		pstatus->cnt = cnt & 0x7F;
		if (cnt & 0x80) {
			pstatus->val = -1;
		} else {
			BYTE packed = 0;
			if(io->read_proc(&packed, sizeof(BYTE), 1, handle) < 1) {
				return EOF;
			}
			pstatus->val = packed;
		}
	}
	pstatus->cnt--;
	if (pstatus->val == -1) {
		BYTE packed = 0;
		if(io->read_proc(&packed, sizeof(BYTE), 1, handle) < 1) {
			return EOF;
		}
		return packed;
	}
	else {
		return pstatus->val;
	}
}

static const char * DLL_CALLCONV
Format() {
  return "SGI";
}

static const char * DLL_CALLCONV
Description() {
  return "SGI Image Format";
}

static const char * DLL_CALLCONV
Extension() {
  return "sgi,rgb,rgba,bw";
}

static const char * DLL_CALLCONV
RegExpr() {
  return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
  return "image/x-sgi";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE sgi_signature[2] = { 0x01, 0xDA };
	BYTE signature[2] = { 0, 0 };

	io->read_proc(signature, 1, sizeof(sgi_signature), handle);

	return (memcmp(sgi_signature, signature, sizeof(sgi_signature)) == 0);
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
  return FALSE;
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
  return FALSE;
}

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	int width = 0, height = 0, zsize = 0;
	int i, dim;
	int bitcount;
	SGIHeader sgiHeader;
	RLEStatus my_rle_status;
	FIBITMAP *dib = NULL;
	LONG *pRowIndex = NULL;

	try {
		// read the header
		memset(&sgiHeader, 0, sizeof(SGIHeader));
		if(io->read_proc(&sgiHeader, 1, sizeof(SGIHeader), handle) < sizeof(SGIHeader)) {
		   throw SGI_LESS_THAN_HEADER_LENGTH;
		}
#ifndef FREEIMAGE_BIGENDIAN
		SwapHeader(&sgiHeader);
#endif
		if(sgiHeader.magic != 474) {
			throw FI_MSG_ERROR_MAGIC_NUMBER;
		}
		
		BOOL bIsRLE = (sgiHeader.storage == 1) ? TRUE : FALSE;
	
		// check for unsupported image types
		if (sgiHeader.bpc != 1) {
			// Expected one byte per color component
			throw SGI_16_BIT_COMPONENTS_NOT_SUPPORTED; 
		}
		if (sgiHeader.colormap != 0) {
			// Indexed or dithered images not supported
			throw SGI_COLORMAPS_NOT_SUPPORTED; 
		}

		// get the width & height
		dim = sgiHeader.dimension;
		width = sgiHeader.xsize;
		if (dim < 3) {
			zsize = 1;
		} else {
			zsize = sgiHeader.zsize;
		}

		if (dim < 2) {
			height = 1;
		} else {
			height = sgiHeader.ysize;
		}
		
		if(bIsRLE) {
			// read the Offset Tables 
			int index_len = height * zsize;
			pRowIndex = (LONG*)malloc(index_len * sizeof(LONG));
			if(!pRowIndex) {
				throw FI_MSG_ERROR_MEMORY;
			}
			
			if ((unsigned)index_len != io->read_proc(pRowIndex, sizeof(LONG), index_len, handle)) {
				throw SGI_EOF_IN_RLE_INDEX;
			}
			
#ifndef FREEIMAGE_BIGENDIAN		
			// Fix byte order in index
			for (i = 0; i < index_len; i++) {
				SwapLong((DWORD*)&pRowIndex[i]);
			}
#endif
			// Discard row size index
			for (i = 0; i < (int)(index_len * sizeof(LONG)); i++) {
				BYTE packed = 0;
				if( io->read_proc(&packed, sizeof(BYTE), 1, handle) < 1 ) {
					throw SGI_EOF_IN_RLE_INDEX;
				}
			}
		}
		
		switch(zsize) {
			case 1:
				bitcount = 8;
				break;
			case 2:
				//Grayscale+Alpha. Need to fake RGBA
				bitcount = 32;
				break;
			case 3:
				bitcount = 24;
				break;
			case 4:
				bitcount = 32;
				break;
			default:
				throw SGI_INVALID_CHANNEL_COUNT;
		}
		
		dib = FreeImage_Allocate(width, height, bitcount);
		if(!dib) {
			throw FI_MSG_ERROR_DIB_MEMORY;
		}
		
		if (bitcount == 8) {
			// 8-bit SGI files are grayscale images, so we'll generate
			// a grayscale palette.
			RGBQUAD *pclrs = FreeImage_GetPalette(dib);
			for (i = 0; i < 256; i++) {
				pclrs[i].rgbRed = (BYTE)i;
				pclrs[i].rgbGreen = (BYTE)i;
				pclrs[i].rgbBlue = (BYTE)i;
				pclrs[i].rgbReserved = 0;
			}
		}

		// decode the image

		memset(&my_rle_status, 0, sizeof(RLEStatus));
		
		int ns = FreeImage_GetPitch(dib);                                                    
		BYTE *pStartRow = FreeImage_GetScanLine(dib, 0);
		int offset_table[] = { 2, 1, 0, 3 };
		int numChannels = zsize;
		if (zsize < 3) {
			offset_table[0] = 0;
		}
		if (zsize == 2)
		{
			//This is how faked grayscale+alpha works.
			//First channel goes into first 
			//second channel goes into alpha (4th channel)
			//Two channels are left empty and will be copied later
			offset_table[1] = 3;
			numChannels = 4;
		}
		
		LONG *pri = pRowIndex;
		for (i = 0; i < zsize; i++) {
			BYTE *pRow = pStartRow + offset_table[i];
			for (int j = 0; j < height; j++, pRow += ns, pri++) {
				BYTE *p = pRow;
				if (bIsRLE) {
					my_rle_status.cnt = 0;
					io->seek_proc(handle, *pri, SEEK_SET);
				}
				for (int k = 0; k < width; k++, p += numChannels) {
					int ch;
					BYTE packed = 0;
					if (bIsRLE) {
						ch = get_rlechar(io, handle, &my_rle_status);
						packed = (BYTE)ch;
					}
					else {
						ch = io->read_proc(&packed, sizeof(BYTE), 1, handle);
					}
					if (ch == EOF) {
						throw SGI_EOF_IN_IMAGE_DATA;
					}
					*p = packed;
				}
			}
		}
		
		if (zsize == 2)
		{
			BYTE *pRow = pStartRow;
			//If faking RGBA from grayscale + alpha, copy first channel to second and third
			for (int i=0; i<height; i++, pRow += ns)
			{
				BYTE *pPixel = pRow;
				for (int j=0; j<width; j++)
				{
					pPixel[2] = pPixel[1] = pPixel[0];
					pPixel += 4;
				}
			}
		}
		if(pRowIndex)
			free(pRowIndex);

		return dib;

	} catch(const char *text) {
		if(pRowIndex) free(pRowIndex);
		if(dib) FreeImage_Unload(dib);
		FreeImage_OutputMessageProc(s_format_id, text);
		return NULL;
	}
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV 
InitSGI(Plugin *plugin, int format_id) {
	s_format_id = format_id;
	
	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = RegExpr;
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
	plugin->supports_icc_profiles_proc = NULL;
}


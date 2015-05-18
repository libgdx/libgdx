// ==========================================================
// ICO Loader and Writer
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
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

// ----------------------------------------------------------
//   Constants + headers
// ----------------------------------------------------------

#ifdef _WIN32
#pragma pack(push, 1)
#else
#pragma pack(1)
#endif

// These next two structs represent how the icon information is stored
// in an ICO file.

typedef struct tagICONHEADER {
	WORD			idReserved;   // reserved
	WORD			idType;       // resource type (1 for icons)
	WORD			idCount;      // how many images?
} ICONHEADER;

typedef struct tagICONDIRECTORYENTRY {
	BYTE	bWidth;               // width of the image
	BYTE	bHeight;              // height of the image (times 2)
	BYTE	bColorCount;          // number of colors in image (0 if >=8bpp)
	BYTE	bReserved;            // reserved
	WORD	wPlanes;              // color Planes
	WORD	wBitCount;            // bits per pixel
	DWORD	dwBytesInRes;         // how many bytes in this resource?
	DWORD	dwImageOffset;        // where in the file is this image
} ICONDIRENTRY;

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

// ==========================================================
// Static helpers
// ==========================================================

/**  How wide, in bytes, would this many bits be, DWORD aligned ?
*/
static int 
WidthBytes(int bits) {
	return ((((bits) + 31)>>5)<<2);
}

/** Calculates the size of a single icon image
@return Returns the size for that image
*/
static DWORD 
CalculateImageSize(FIBITMAP* icon_dib) {
	DWORD dwNumBytes = 0;

	unsigned colors		= FreeImage_GetColorsUsed(icon_dib);
	unsigned width		= FreeImage_GetWidth(icon_dib);
	unsigned height		= FreeImage_GetHeight(icon_dib);
	unsigned pitch		= FreeImage_GetPitch(icon_dib);

	dwNumBytes = sizeof( BITMAPINFOHEADER );	// header
	dwNumBytes += colors * sizeof(RGBQUAD);		// palette
	dwNumBytes += height * pitch;				// XOR mask
	dwNumBytes += height * WidthBytes(width);	// AND mask

	return dwNumBytes;
}

/** Calculates the file offset for an icon image
@return Returns the file offset for that image
*/
static DWORD 
CalculateImageOffset(std::vector<FIBITMAP*>& vPages, int nIndex ) {
	DWORD	dwSize;

    // calculate the ICO header size
    dwSize = sizeof(ICONHEADER); 
    // add the ICONDIRENTRY's
    dwSize += (DWORD)( vPages.size() * sizeof(ICONDIRENTRY) );
    // add the sizes of the previous images
    for(int k = 0; k < nIndex; k++) {
		FIBITMAP *icon_dib = (FIBITMAP*)vPages[k];
		dwSize += CalculateImageSize(icon_dib);
	}

    return dwSize;
}

#ifdef FREEIMAGE_BIGENDIAN
static void
SwapInfoHeader(BITMAPINFOHEADER *header) {
	SwapLong(&header->biSize);
	SwapLong((DWORD *)&header->biWidth);
	SwapLong((DWORD *)&header->biHeight);
	SwapShort(&header->biPlanes);
	SwapShort(&header->biBitCount);
	SwapLong(&header->biCompression);
	SwapLong(&header->biSizeImage);
	SwapLong((DWORD *)&header->biXPelsPerMeter);
	SwapLong((DWORD *)&header->biYPelsPerMeter);
	SwapLong(&header->biClrUsed);
	SwapLong(&header->biClrImportant);
}

static void
SwapIconHeader(ICONHEADER *header) {
	SwapShort(&header->idReserved);
	SwapShort(&header->idType);
	SwapShort(&header->idCount);
}

static void
SwapIconDirEntries(ICONDIRENTRY *ent, int num) {
	while(num) {
		SwapShort(&ent->wPlanes);
		SwapShort(&ent->wBitCount);
		SwapLong(&ent->dwBytesInRes);
		SwapLong(&ent->dwImageOffset);
		num--;
		ent++;
	}
}
#endif

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "ICO";
}

static const char * DLL_CALLCONV
Description() {
	return "Windows Icon";
}

static const char * DLL_CALLCONV
Extension() {
	return "ico";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/vnd.microsoft.icon";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	ICONHEADER icon_header;

	io->read_proc(&icon_header, sizeof(ICONHEADER), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
	SwapIconHeader(&icon_header);
#endif

	return ((icon_header.idReserved == 0) && (icon_header.idType == 1) && (icon_header.idCount > 0));
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
			(depth == 1) ||
			(depth == 4) ||
			(depth == 8) ||
			(depth == 16) ||
			(depth == 24) ||
			(depth == 32)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (type == FIT_BITMAP) ? TRUE : FALSE;
}

static BOOL DLL_CALLCONV
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static void * DLL_CALLCONV
Open(FreeImageIO *io, fi_handle handle, BOOL read) {
	// Allocate memory for the header structure
	ICONHEADER *lpIH = (ICONHEADER*)malloc(sizeof(ICONHEADER));
	if(lpIH == NULL) {
		return NULL;
	}

	if (read) {
		// Read in the header
		io->read_proc(lpIH, 1, sizeof(ICONHEADER), handle);
#ifdef FREEIMAGE_BIGENDIAN
		SwapIconHeader(lpIH);
#endif

		if(!(lpIH->idReserved == 0) || !(lpIH->idType == 1)) {
			// Not an ICO file
			free(lpIH);
			return NULL;
		}
	}
	else {
		// Fill the header
		lpIH->idReserved = 0;
		lpIH->idType = 1;
		lpIH->idCount = 0;
	}

	return lpIH;
}

static void DLL_CALLCONV
Close(FreeImageIO *io, fi_handle handle, void *data) {
	// free the header structure
	ICONHEADER *lpIH = (ICONHEADER*)data;
	free(lpIH);
}

// ----------------------------------------------------------

static int DLL_CALLCONV
PageCount(FreeImageIO *io, fi_handle handle, void *data) {
	ICONHEADER *lpIH = (ICONHEADER*)data;

	if(lpIH) {
		return lpIH->idCount;
	}
	return 1;
}

// ----------------------------------------------------------

static FIBITMAP*
LoadStandardIcon(FreeImageIO *io, fi_handle handle, int flags, BOOL header_only) {
	FIBITMAP *dib = NULL;

	// load the BITMAPINFOHEADER
	BITMAPINFOHEADER bmih;
	io->read_proc(&bmih, sizeof(BITMAPINFOHEADER), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
	SwapInfoHeader(&bmih);
#endif

	// allocate the bitmap
	int width  = bmih.biWidth;
	int height = bmih.biHeight / 2; // height == xor + and mask
	unsigned bit_count = bmih.biBitCount;
	unsigned line   = CalculateLine(width, bit_count);
	unsigned pitch  = CalculatePitch(line);

	// allocate memory for one icon

	dib = FreeImage_AllocateHeader(header_only, width, height, bit_count);

	if (dib == NULL) {
		return NULL;
	}

	if( bmih.biBitCount <= 8 ) {
		// read the palette data
		io->read_proc(FreeImage_GetPalette(dib), CalculateUsedPaletteEntries(bit_count) * sizeof(RGBQUAD), 1, handle);
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
		RGBQUAD *pal = FreeImage_GetPalette(dib);
		for(unsigned i = 0; i < CalculateUsedPaletteEntries(bit_count); i++) {
			INPLACESWAP(pal[i].rgbRed, pal[i].rgbBlue);
		}
#endif
	}
	
	if(header_only) {
		// header only mode
		return dib;
	}

	// read the icon
	io->read_proc(FreeImage_GetBits(dib), height * pitch, 1, handle);

#ifdef FREEIMAGE_BIGENDIAN
	if (bit_count == 16) {
		for(int y = 0; y < height; y++) {
			WORD *pixel = (WORD *)FreeImage_GetScanLine(dib, y);
			for(int x = 0; x < width; x++) {
				SwapShort(pixel);
				pixel++;
			}
		}
	}
#endif
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
	if (bit_count == 24 || bit_count == 32) {
		for(int y = 0; y < height; y++) {
			BYTE *pixel = FreeImage_GetScanLine(dib, y);
			for(int x = 0; x < width; x++) {
				INPLACESWAP(pixel[0], pixel[2]);
				pixel += (bit_count>>3);
			}
		}
	}
#endif
	// bitmap has been loaded successfully!

	// convert to 32bpp and generate an alpha channel
	// apply the AND mask only if the image is not 32 bpp
	if(((flags & ICO_MAKEALPHA) == ICO_MAKEALPHA) && (bit_count < 32)) {
		FIBITMAP *dib32 = FreeImage_ConvertTo32Bits(dib);
		FreeImage_Unload(dib);

		if (dib32 == NULL) {
			return NULL;
		}

		int width_and	= WidthBytes(width);
		BYTE *line_and	= (BYTE *)malloc(width_and);

		if( line_and == NULL ) {
			FreeImage_Unload(dib32);
			return NULL;
		}

		//loop through each line of the AND-mask generating the alpha channel, invert XOR-mask
		for(int y = 0; y < height; y++) {
			RGBQUAD *quad = (RGBQUAD *)FreeImage_GetScanLine(dib32, y);
			io->read_proc(line_and, width_and, 1, handle);
			for(int x = 0; x < width; x++) {
				quad->rgbReserved = (line_and[x>>3] & (0x80 >> (x & 0x07))) != 0 ? 0 : 0xFF;
				if( quad->rgbReserved == 0 ) {
					quad->rgbBlue ^= 0xFF;
					quad->rgbGreen ^= 0xFF;
					quad->rgbRed ^= 0xFF;
				}
				quad++;
			}
		}
		free(line_and);

		return dib32;
	}

	return dib;
}

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	if (page == -1) {
		page = 0;
	}

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	if (handle != NULL) {
		FIBITMAP *dib = NULL;

		// get the icon header
		ICONHEADER *icon_header = (ICONHEADER*)data;

		if (icon_header) {
			// load the icon descriptions
			ICONDIRENTRY *icon_list = (ICONDIRENTRY*)malloc(icon_header->idCount * sizeof(ICONDIRENTRY));
			if(icon_list == NULL) {
				return NULL;
			}
			io->seek_proc(handle, sizeof(ICONHEADER), SEEK_SET);
			io->read_proc(icon_list, icon_header->idCount * sizeof(ICONDIRENTRY), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
			SwapIconDirEntries(icon_list, icon_header->idCount);
#endif

			// load the requested icon
			if (page < icon_header->idCount) {
				// seek to the start of the bitmap data for the icon
				io->seek_proc(handle, 0, SEEK_SET);
				io->seek_proc(handle, icon_list[page].dwImageOffset, SEEK_CUR);

				if((icon_list[page].bWidth == 0) && (icon_list[page].bHeight == 0)) {
					// Vista icon support
					dib = FreeImage_LoadFromHandle(FIF_PNG, io, handle, header_only ? FIF_LOAD_NOPIXELS : PNG_DEFAULT);
				}
				else {
					// standard icon support
					// see http://msdn.microsoft.com/en-us/library/ms997538.aspx
					dib = LoadStandardIcon(io, handle, flags, header_only);
				}

				free(icon_list);

				return dib;

			} else {
				free(icon_list);
				FreeImage_OutputMessageProc(s_format_id, "Page doesn't exist");
			}
		} else {
			FreeImage_OutputMessageProc(s_format_id, "File is not an ICO file");
		}
	}

	return NULL;
}

// ----------------------------------------------------------

static BOOL 
SaveStandardIcon(FreeImageIO *io, FIBITMAP *dib, fi_handle handle) {
	BITMAPINFOHEADER *bmih = NULL;

	// write the BITMAPINFOHEADER
	bmih = FreeImage_GetInfoHeader(dib);
	bmih->biHeight *= 2;	// height == xor + and mask
#ifdef FREEIMAGE_BIGENDIAN
	SwapInfoHeader(bmih);
#endif
	io->write_proc(bmih, sizeof(BITMAPINFOHEADER), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
	SwapInfoHeader(bmih);
#endif
	bmih->biHeight /= 2;

	// write the palette data
	if (FreeImage_GetPalette(dib) != NULL) {
		RGBQUAD *pal = FreeImage_GetPalette(dib);
		FILE_BGRA bgra;
		for(unsigned i = 0; i < FreeImage_GetColorsUsed(dib); i++) {
			bgra.b = pal[i].rgbBlue;
			bgra.g = pal[i].rgbGreen;
			bgra.r = pal[i].rgbRed;
			bgra.a = pal[i].rgbReserved;
			io->write_proc(&bgra, sizeof(FILE_BGRA), 1, handle);
		}
	}

	// write the bits
	int width			= bmih->biWidth;
	int height			= bmih->biHeight;
	unsigned bit_count	= bmih->biBitCount;
	unsigned line		= CalculateLine(width, bit_count);
	unsigned pitch		= CalculatePitch(line);
	int size_xor		= height * pitch;
	int size_and		= height * WidthBytes(width);

	// XOR mask
#ifdef FREEIMAGE_BIGENDIAN
	if (bit_count == 16) {
		WORD pixel;
		for(unsigned y = 0; y < FreeImage_GetHeight(dib); y++) {
			BYTE *line = FreeImage_GetScanLine(dib, y);
			for(unsigned x = 0; x < FreeImage_GetWidth(dib); x++) {
				pixel = ((WORD *)line)[x];
				SwapShort(&pixel);
				if (io->write_proc(&pixel, sizeof(WORD), 1, handle) != 1)
					return FALSE;
			}
		}
	} else
#endif
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
	if (bit_count == 24) {
		FILE_BGR bgr;
		for(unsigned y = 0; y < FreeImage_GetHeight(dib); y++) {
			BYTE *line = FreeImage_GetScanLine(dib, y);
			for(unsigned x = 0; x < FreeImage_GetWidth(dib); x++) {
				RGBTRIPLE *triple = ((RGBTRIPLE *)line)+x;
				bgr.b = triple->rgbtBlue;
				bgr.g = triple->rgbtGreen;
				bgr.r = triple->rgbtRed;
				if (io->write_proc(&bgr, sizeof(FILE_BGR), 1, handle) != 1)
					return FALSE;
			}
		}
	} else if (bit_count == 32) {
		FILE_BGRA bgra;
		for(unsigned y = 0; y < FreeImage_GetHeight(dib); y++) {
			BYTE *line = FreeImage_GetScanLine(dib, y);
			for(unsigned x = 0; x < FreeImage_GetWidth(dib); x++) {
				RGBQUAD *quad = ((RGBQUAD *)line)+x;
				bgra.b = quad->rgbBlue;
				bgra.g = quad->rgbGreen;
				bgra.r = quad->rgbRed;
				bgra.a = quad->rgbReserved;
				if (io->write_proc(&bgra, sizeof(FILE_BGRA), 1, handle) != 1)
					return FALSE;
			}
		}
	} else
#endif
#if defined(FREEIMAGE_BIGENDIAN) || FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
	{
#endif
		BYTE *xor_mask = FreeImage_GetBits(dib);
		io->write_proc(xor_mask, size_xor, 1, handle);
#if defined(FREEIMAGE_BIGENDIAN) || FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
	}
#endif
	// AND mask
	BYTE *and_mask = (BYTE*)malloc(size_and);
	if(!and_mask) {
		return FALSE;
	}

	if(FreeImage_IsTransparent(dib)) {

		if(bit_count == 32) {
			// create the AND mask from the alpha channel

			int width_and  = WidthBytes(width);
			BYTE *and_bits = and_mask;

			// clear the mask
			memset(and_mask, 0, size_and);

			for(int y = 0; y < height; y++) {
				RGBQUAD *bits = (RGBQUAD*)FreeImage_GetScanLine(dib, y);

				for(int x = 0; x < width; x++) {
					if(bits[x].rgbReserved != 0xFF) {
						// set any transparent color to full transparency
						and_bits[x >> 3] |= (0x80 >> (x & 0x7)); 
					}
				}

				and_bits += width_and;
			}
		} 
		else if(bit_count <= 8) {
			// create the AND mask from the transparency table

			BYTE *trns = FreeImage_GetTransparencyTable(dib);

			int width_and  = WidthBytes(width);
			BYTE *and_bits = and_mask;

			// clear the mask
			memset(and_mask, 0, size_and);

			switch(FreeImage_GetBPP(dib)) {
				case 1:
				{
					for(int y = 0; y < height; y++) {
						BYTE *bits = (BYTE*)FreeImage_GetScanLine(dib, y);
						for(int x = 0; x < width; x++) {
							// get pixel at (x, y)
							BYTE index = (bits[x >> 3] & (0x80 >> (x & 0x07))) != 0;
							if(trns[index] != 0xFF) {
								// set any transparent color to full transparency
								and_bits[x >> 3] |= (0x80 >> (x & 0x7)); 
							}
						}
						and_bits += width_and;
					}
				}
				break;

				case 4:
				{
					for(int y = 0; y < height; y++) {
						BYTE *bits = (BYTE*)FreeImage_GetScanLine(dib, y);
						for(int x = 0; x < width; x++) {
							// get pixel at (x, y)
							BYTE shift = (BYTE)((1 - x % 2) << 2);
							BYTE index = (bits[x >> 1] & (0x0F << shift)) >> shift;
							if(trns[index] != 0xFF) {
								// set any transparent color to full transparency
								and_bits[x >> 3] |= (0x80 >> (x & 0x7)); 
							}
						}
						and_bits += width_and;
					}
				}
				break;

				case 8:
				{
					for(int y = 0; y < height; y++) {
						BYTE *bits = (BYTE*)FreeImage_GetScanLine(dib, y);
						for(int x = 0; x < width; x++) {
							// get pixel at (x, y)
							BYTE index = bits[x];
							if(trns[index] != 0xFF) {
								// set any transparent color to full transparency
								and_bits[x >> 3] |= (0x80 >> (x & 0x7)); 
							}
						}
						and_bits += width_and;
					}
				}
				break;

			}
		}
	}
	else {
		// empty AND mask
		memset(and_mask, 0, size_and);
	}

	io->write_proc(and_mask, size_and, 1, handle);
	free(and_mask);

	return TRUE;
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	ICONHEADER *icon_header = NULL;
	std::vector<FIBITMAP*> vPages;
	int k;

	if(!dib || !handle || !data) {
		return FALSE;
	}

	// check format limits
	unsigned w = FreeImage_GetWidth(dib);
	unsigned h = FreeImage_GetHeight(dib);
	if((w < 16) || (w > 256) || (h < 16) || (h > 256) || (w != h)) {
		FreeImage_OutputMessageProc(s_format_id, "Unsupported icon size: width x height = %d x %d", w, h);
		return FALSE;
	}

	if (page == -1) {
		page = 0;
	}
	
	// get the icon header
	icon_header = (ICONHEADER*)data;

	try {
		FIBITMAP *icon_dib = NULL;

		// load all icons
		for(k = 0; k < icon_header->idCount; k++) {
			icon_dib = Load(io, handle, k, flags, data);
			if(!icon_dib) {
				throw FI_MSG_ERROR_DIB_MEMORY;
			}
			vPages.push_back(icon_dib);
		}

		// add the page
		icon_dib = FreeImage_Clone(dib);
		vPages.push_back(icon_dib);
		icon_header->idCount++;

		// write the header
		io->seek_proc(handle, 0, SEEK_SET);
#ifdef FREEIMAGE_BIGENDIAN
		SwapIconHeader(icon_header);
#endif
		io->write_proc(icon_header, sizeof(ICONHEADER), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
		SwapIconHeader(icon_header);
#endif

		// write all icons
		// ...
		
		// save the icon descriptions

		ICONDIRENTRY *icon_list = (ICONDIRENTRY *)malloc(icon_header->idCount * sizeof(ICONDIRENTRY));
		if(!icon_list) {
			throw FI_MSG_ERROR_MEMORY;
		}
		memset(icon_list, 0, icon_header->idCount * sizeof(ICONDIRENTRY));

		for(k = 0; k < icon_header->idCount; k++) {
			icon_dib = (FIBITMAP*)vPages[k];

			// convert internal format to ICONDIRENTRY
			// take into account Vista icons whose size is 256x256
			const BITMAPINFOHEADER *bmih = FreeImage_GetInfoHeader(icon_dib);
			icon_list[k].bWidth			= (bmih->biWidth > 255)  ? 0 : (BYTE)bmih->biWidth;
			icon_list[k].bHeight		= (bmih->biHeight > 255) ? 0 : (BYTE)bmih->biHeight;
			icon_list[k].bReserved		= 0;
			icon_list[k].wPlanes		= bmih->biPlanes;
			icon_list[k].wBitCount		= bmih->biBitCount;
			if( (icon_list[k].wPlanes * icon_list[k].wBitCount) >= 8 ) {
				icon_list[k].bColorCount = 0;
			} else {
				icon_list[k].bColorCount = (BYTE)(1 << (icon_list[k].wPlanes * icon_list[k].wBitCount));
			}
			// initial guess (correct only for standard icons)
			icon_list[k].dwBytesInRes	= CalculateImageSize(icon_dib);
			icon_list[k].dwImageOffset = CalculateImageOffset(vPages, k);
		}

		// make a room for icon dir entries, until later update
		const long directory_start = io->tell_proc(handle);
		io->write_proc(icon_list, sizeof(ICONDIRENTRY) * icon_header->idCount, 1, handle);

		// write the image bits for each image
		
		DWORD dwImageOffset = (DWORD)io->tell_proc(handle);

		for(k = 0; k < icon_header->idCount; k++) {
			icon_dib = (FIBITMAP*)vPages[k];
			
			if((icon_list[k].bWidth == 0) && (icon_list[k].bHeight == 0)) {
				// Vista icon support
				FreeImage_SaveToHandle(FIF_PNG, icon_dib, io, handle, PNG_DEFAULT);
			}
			else {
				// standard icon support
				// see http://msdn.microsoft.com/en-us/library/ms997538.aspx
				SaveStandardIcon(io, icon_dib, handle);
			}

			// update ICONDIRENTRY members			
			DWORD dwBytesInRes = (DWORD)io->tell_proc(handle) - dwImageOffset;
			icon_list[k].dwImageOffset = dwImageOffset;
			icon_list[k].dwBytesInRes  = dwBytesInRes;
			dwImageOffset += dwBytesInRes;
		}

		// update the icon descriptions
		const long current_pos = io->tell_proc(handle);
		io->seek_proc(handle, directory_start, SEEK_SET);
#ifdef FREEIMAGE_BIGENDIAN
		SwapIconDirEntries(icon_list, icon_header->idCount);
#endif
		io->write_proc(icon_list, sizeof(ICONDIRENTRY) * icon_header->idCount, 1, handle);
		io->seek_proc(handle, current_pos, SEEK_SET);

		free(icon_list);

		// free the vector class
		for(k = 0; k < icon_header->idCount; k++) {
			icon_dib = (FIBITMAP*)vPages[k];
			FreeImage_Unload(icon_dib);
		}

		return TRUE;

	} catch(const char *text) {
		// free the vector class
		for(size_t k = 0; k < vPages.size(); k++) {
			FIBITMAP *icon_dib = (FIBITMAP*)vPages[k];
			FreeImage_Unload(icon_dib);
		}
		FreeImage_OutputMessageProc(s_format_id, text);
		return FALSE;
	}
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitICO(Plugin *plugin, int format_id) {
	s_format_id = format_id;

	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = RegExpr;
	plugin->open_proc = Open;
	plugin->close_proc = Close;
	plugin->pagecount_proc = PageCount;
	plugin->pagecapability_proc = NULL;
	plugin->load_proc = Load;
	plugin->save_proc = Save;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

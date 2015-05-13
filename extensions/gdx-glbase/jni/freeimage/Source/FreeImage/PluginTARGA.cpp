// ==========================================================
// TARGA Loader and Writer
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Jani Kajala (janik@remedy.fi)
// - Martin Weber (martweb@gmx.net)
// - Machiel ten Brinke (brinkem@uni-one.nl)
// - Peter Lemmens (peter.lemmens@planetinternet.be)
// - Hervé Drolon (drolon@infonie.fr)
// - Mihail Naydenov (mnaydenov@users.sourceforge.net)
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

typedef struct tagTGAHEADER {
	BYTE id_length;				// ID length
	BYTE color_map_type;		// color map type
	BYTE image_type;			// image type

	WORD cm_first_entry;		// first entry index
	WORD cm_length;				// color map length
	BYTE cm_size;				// color map entry size, in bits

	WORD is_xorigin;			// X-origin of image
	WORD is_yorigin;			// Y-origin of image
	WORD is_width;				// image width
	WORD is_height;				// image height
	BYTE is_pixel_depth;		// pixel depth
	BYTE is_image_descriptor;	// image descriptor
} TGAHEADER;

typedef struct tagTGAEXTENSIONAREA {
	WORD extension_size;		// Size in bytes of the extension area, always 495
	char author_name[41];		// Name of the author. If not used, bytes should be set to NULL (\0) or spaces
	char author_comments[324];	// A comment, organized as four lines, each consisting of 80 characters plus a NULL
	WORD datetime_stamp[6];		// Date and time at which the image was created
	char job_name[41];			// Job ID
	WORD job_time[3];			// Hours, minutes and seconds spent creating the file (for billing, etc.)
	char software_id[41];		// The application that created the file
	BYTE software_version[3];
	DWORD key_color;
	WORD pixel_aspect_ratio[2];
	WORD gamma_value[2];
	DWORD color_correction_offset;	// Number of bytes from the beginning of the file to the color correction table if present
	DWORD postage_stamp_offset;		// Number of bytes from the beginning of the file to the postage stamp image if present
	DWORD scan_line_offset;			// Number of bytes from the beginning of the file to the scan lines table if present
	BYTE attributes_type;			// Specifies the alpha channel
} TGAEXTENSIONAREA;

typedef struct tagTGAFOOTER {
	DWORD extension_offset;	// extension area offset : offset in bytes from the beginning of the file
	DWORD developer_offset;	// developer directory offset : offset in bytes from the beginning of the file
	char signature[18];		// signature string : contains "TRUEVISION-XFILE.\0"
} TGAFOOTER;

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

static const char *FI_MSG_ERROR_CORRUPTED = "Image data corrupted";

// ----------------------------------------------------------
// Image type
//
#define TGA_NULL		0	// no image data included
#define TGA_CMAP		1	// uncompressed, color-mapped image
#define TGA_RGB			2	// uncompressed, true-color image
#define TGA_MONO		3	// uncompressed, black-and-white image
#define TGA_RLECMAP		9	// run-length encoded, color-mapped image
#define TGA_RLERGB		10	// run-length encoded, true-color image
#define TGA_RLEMONO		11	// run-length encoded, black-and-white image
#define TGA_CMPCMAP		32	// compressed (Huffman/Delta/RLE) color-mapped image (e.g., VDA/D) - Obsolete
#define TGA_CMPCMAP4	33	// compressed (Huffman/Delta/RLE) color-mapped four pass image (e.g., VDA/D) - Obsolete

// ==========================================================
// Thumbnail functions
// ==========================================================

class TargaThumbnail
{
public:
	TargaThumbnail() : _w(0), _h(0), _depth(0), _data(NULL) { 
	}
	~TargaThumbnail() { 
		if(_data) {
			free(_data); 
		}
	}

	BOOL isNull() const { 
		return (_data == NULL); 
	}
	
	BOOL read(FreeImageIO *io, fi_handle handle, size_t size) {
		io->read_proc(&_w, 1, 1, handle);
		io->read_proc(&_h, 1, 1, handle);
		
		const size_t sizeofData = size - 2;
		_data = (BYTE*)malloc(sizeofData);
		if(_data) {
			return (io->read_proc(_data, 1, (unsigned)sizeofData, handle) == sizeofData);
		}
		return FALSE;
	}
	
	void setDepth(BYTE dp) { 
		_depth = dp;
	}
	
	FIBITMAP* toFIBITMAP();
	
private:
	BYTE _w;
	BYTE _h;
	BYTE _depth;
	BYTE* _data;
};

#ifdef FREEIMAGE_BIGENDIAN
static void 
swapShortPixels(FIBITMAP* dib) {
	if(FreeImage_GetImageType(dib) != FIT_BITMAP) {
		return;
	}
		
	const unsigned Bpp = FreeImage_GetBPP(dib)/8;
	if(Bpp != 2) {
		return;
	}
		
	BYTE* bits = FreeImage_GetBits(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pitch = FreeImage_GetPitch(dib);
	
	BYTE* line = bits;
	for(unsigned y = 0; y < height; y++, line += pitch) {
		for(BYTE* pixel = line; pixel < line + pitch ; pixel += Bpp) {
			SwapShort((WORD*)pixel);
		}
	}
}
#endif // FREEIMAGE_BIGENDIAN

FIBITMAP* TargaThumbnail::toFIBITMAP() {
	if(isNull() || _depth == 0) {
		return NULL;
	}
		
	const unsigned line_size = _depth * _w / 8;
	FIBITMAP* dib = FreeImage_Allocate(_w, _h, _depth);
	if(!dib) {
		return NULL;
	}

	const BYTE* line = _data;
	const BYTE height = _h;
	for (BYTE h = 0; h < height; ++h, line += line_size) {
		BYTE* dst_line = FreeImage_GetScanLine(dib, height - 1 - h);
		memcpy(dst_line, line, line_size);
	}

#ifdef FREEIMAGE_BIGENDIAN
	swapShortPixels(dib);
#endif
	
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
	SwapRedBlue32(dib);
#endif

	return dib;
}
// ==========================================================
// Internal functions
// ==========================================================

/** This class is used when loading RLE compressed images, it implements io cache of fixed size.
	In general RLE compressed images *should* be compressed line by line with line sizes stored in Scan Line Table section.
	In reality, however there are images not obeying the specification, compressing image data continuously across lines,
	making it impossible to load the file cached at every line.
*/
class IOCache
{
public:
	IOCache(FreeImageIO *io, fi_handle handle, size_t size) :
		_ptr(NULL), _begin(NULL), _end(NULL), _size(size), _io(io), _handle(handle)	{
			_begin = (BYTE*)malloc(size);
			if (_begin) {
			_end = _begin + _size;
			_ptr = _end;	// will force refill on first access
		}
	}
	
	~IOCache() {
		if (_begin != NULL) {
			free(_begin);
		}	
	}
		
	BOOL isNull() { return _begin == NULL;}
	
	inline
	BYTE getByte() {
		if (_ptr >= _end) {
			// need refill
			_ptr = _begin;
			_io->read_proc(_ptr, sizeof(BYTE), (unsigned)_size, _handle);	//### EOF - no problem?
		}

		BYTE result = *_ptr;

		_ptr++;

		return result;
	}
	
	inline
	BYTE* getBytes(size_t count /*must be < _size!*/) {
		if (_ptr + count >= _end) {
			
			// need refill

			// 'count' bytes might span two cache bounds,
			// SEEK back to add the remains of the current cache again into the new one

			long read = long(_ptr - _begin);
			long remaining = long(_size - read);

			_io->seek_proc(_handle, -remaining, SEEK_CUR);

			_ptr = _begin;
			_io->read_proc(_ptr, sizeof(BYTE), (unsigned)_size, _handle);	//### EOF - no problem?
		}

		BYTE *result = _ptr;

		_ptr += count;

		return result;
	}

private:
	IOCache& operator=(const IOCache& src); // deleted
	IOCache(const IOCache& other); // deleted

private:
	BYTE *_ptr;
	BYTE *_begin;
	BYTE *_end;
	const size_t _size;
	const FreeImageIO *_io;	
	const fi_handle _handle;	
};

#ifdef FREEIMAGE_BIGENDIAN
static void
SwapHeader(TGAHEADER *header) {
	SwapShort(&header->cm_first_entry);
	SwapShort(&header->cm_length);
	SwapShort(&header->is_xorigin);
	SwapShort(&header->is_yorigin);
	SwapShort(&header->is_width);
	SwapShort(&header->is_height);
}

static void
SwapExtensionArea(TGAEXTENSIONAREA *ex) {
	SwapShort(&ex->extension_size);
	SwapShort(&ex->datetime_stamp[0]);
	SwapShort(&ex->datetime_stamp[1]);
	SwapShort(&ex->datetime_stamp[2]);
	SwapShort(&ex->datetime_stamp[3]);
	SwapShort(&ex->datetime_stamp[4]);
	SwapShort(&ex->datetime_stamp[5]);
	SwapShort(&ex->job_time[0]);
	SwapShort(&ex->job_time[1]);
	SwapShort(&ex->job_time[2]);
	SwapLong (&ex->key_color);
	SwapShort(&ex->pixel_aspect_ratio[0]);
	SwapShort(&ex->pixel_aspect_ratio[1]);
	SwapShort(&ex->gamma_value[0]);
	SwapShort(&ex->gamma_value[1]);
	SwapLong (&ex->color_correction_offset);
	SwapLong (&ex->postage_stamp_offset);
	SwapLong (&ex->scan_line_offset);
}

static void
SwapFooter(TGAFOOTER *footer) {
	SwapLong(&footer->extension_offset);
	SwapLong(&footer->developer_offset);
}

#endif // FREEIMAGE_BIGENDIAN

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "TARGA";
}

static const char * DLL_CALLCONV
Description() {
	return "Truevision Targa";
}

static const char * DLL_CALLCONV
Extension() {
	return "tga,targa";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/x-tga";
}

static BOOL 
isTARGA20(FreeImageIO *io, fi_handle handle) {
	const unsigned sizeofSig = 18;
	BYTE signature[sizeofSig];
	// tga_signature = "TRUEVISION-XFILE." (TGA 2.0 only)
	BYTE tga_signature[sizeofSig] = { 84, 82, 85, 69, 86, 73, 83, 73, 79, 78, 45, 88, 70, 73, 76, 69, 46, 0 };
	// get the start offset
	const long start_offset = io->tell_proc(handle);
	// get the end-of-file
	io->seek_proc(handle, 0, SEEK_END);
	const long eof = io->tell_proc(handle);
	// read the signature
	io->seek_proc(handle, start_offset + eof - sizeofSig, SEEK_SET);
	io->read_proc(&signature, 1, sizeofSig, handle);
	// rewind
	io->seek_proc(handle, start_offset, SEEK_SET);
		
	return (memcmp(tga_signature, signature, sizeofSig) == 0);
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) { 
	if(isTARGA20(io, handle)) {
		return TRUE;
	}
		
	// not a 2.0 image, try testing if it's a valid TGA anyway (not robust)
	{
		const long start_offset = io->tell_proc(handle);
		
		// get the header
		TGAHEADER header;
		io->read_proc(&header, sizeof(tagTGAHEADER), 1, handle);
#ifdef FREEIMAGE_BIGENDIAN
		SwapHeader(&header);
#endif
		// rewind
		io->seek_proc(handle, start_offset, SEEK_SET);

		// the color map type should be a 0 or a 1...
		if(header.color_map_type != 0 && header.color_map_type != 1) {
			return FALSE;
		}
		// if the color map type is 1 then we validate the map entry information...
		if(header.color_map_type > 0) {
			// It doesn't make any sense if the first entry is larger than the color map table
			if(header.cm_first_entry >= header.cm_length) {
				return FALSE;
			}
		}
		// check header.cm_size, don't allow 0 or anything bigger than 32
		if(header.cm_size == 0 || header.cm_size > 32) {
			return FALSE;
		}
		// the width/height shouldn't be 0, right ?
		if(header.is_width == 0 || header.is_height == 0) {
			return FALSE;
		}
		// let's now verify all the types that are supported by FreeImage (this is our final verification)
		switch(header.image_type) {
			case TGA_CMAP	:
			case TGA_RGB:
			case TGA_MONO	:
			case TGA_RLECMAP:
			case TGA_RLERGB:
			case TGA_RLEMONO:
				switch(header.is_pixel_depth) {
					case 8	:
					case 16:
					case 24:
					case 32:
						return TRUE;
					default:
						return FALSE;
				}
				break;
			default:
				return FALSE;
		}
	}

	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
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

/**
Used for all 32 and 24 bit loading of uncompressed images
*/
static void 
loadTrueColor(FIBITMAP* dib, int width, int height, int file_pixel_size, FreeImageIO* io, fi_handle handle, BOOL as24bit) {
	const int pixel_size = as24bit ? 3 : file_pixel_size;

	// input line cache
	BYTE* file_line = (BYTE*)malloc( width * file_pixel_size);

	if (!file_line) {
		throw FI_MSG_ERROR_MEMORY;
	}

	for (int y = 0; y < height; y++) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);
		io->read_proc(file_line, file_pixel_size, width, handle);
		BYTE *bgra = file_line;

		for (int x = 0; x < width; x++) {

			bits[FI_RGBA_BLUE]	= bgra[0];
			bits[FI_RGBA_GREEN] = bgra[1];
			bits[FI_RGBA_RED]	= bgra[2];

			if (!as24bit) {
				bits[FI_RGBA_ALPHA] = bgra[3];
			}

			bgra += file_pixel_size;

			bits += pixel_size;
		}
	}

	free(file_line);
}

/**
For the generic RLE loader we need to abstract away the pixel format.
We use a specific overload based on bits-per-pixel for each type of pixel
*/

template <int nBITS>
inline static void 
_assignPixel(BYTE* bits, BYTE* val, BOOL as24bit = FALSE) {
	// static assert should go here
	assert(FALSE);
}

template <>
inline void 
_assignPixel<8>(BYTE* bits, BYTE* val, BOOL as24bit) {
	*bits = *val;
}

template <>
inline void 
_assignPixel<16>(BYTE* bits, BYTE* val, BOOL as24bit) {
	WORD value(*reinterpret_cast<WORD*>(val));

#ifdef FREEIMAGE_BIGENDIAN
	SwapShort(&value);
#endif

	if (as24bit) {
		bits[FI_RGBA_BLUE]  = (BYTE)((((value & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT) * 0xFF) / 0x1F);
		bits[FI_RGBA_GREEN] = (BYTE)((((value & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT) * 0xFF) / 0x1F);
		bits[FI_RGBA_RED]   = (BYTE)((((value & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT) * 0xFF) / 0x1F);

	} else {
		*reinterpret_cast<WORD *>(bits) = 0x7FFF & value;
	}
}

template <>
inline void 
_assignPixel<24>(BYTE* bits, BYTE* val, BOOL as24bit) {
	bits[FI_RGBA_BLUE]	= val[0];
	bits[FI_RGBA_GREEN] = val[1];
	bits[FI_RGBA_RED]	= val[2];
}

template <>
inline void 
_assignPixel<32>(BYTE* bits, BYTE* val, BOOL as24bit) {
	if (as24bit) {
		_assignPixel<24>(bits, val, TRUE);

	} else {
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
		*(reinterpret_cast<unsigned*>(bits)) = *(reinterpret_cast<unsigned*> (val));
#else // NOTE This is faster then doing reinterpret_cast to int + INPLACESWAP !
		bits[FI_RGBA_BLUE]	= val[0];
		bits[FI_RGBA_GREEN] = val[1];
		bits[FI_RGBA_RED]	= val[2];
		bits[FI_RGBA_ALPHA]	= val[3];
#endif
	}
}

/**
Generic RLE loader
*/
template<int bPP>
static void 
loadRLE(FIBITMAP* dib, int width, int height, FreeImageIO* io, fi_handle handle, long eof, BOOL as24bit) {
	const int file_pixel_size = bPP/8;
	const int pixel_size = as24bit ? 3 : file_pixel_size;

	const BYTE bpp = as24bit ? 24 : bPP;
	const int line_size = CalculateLine(width, bpp);

	// Note, many of the params can be computed inside the function.
	// However, because this is a template function, it will lead to redundant code duplication.

	BYTE rle;
	BYTE *line_bits;

	// this is used to guard against writing beyond the end of the image (on corrupted rle block)
	const BYTE* dib_end = FreeImage_GetScanLine(dib, height);//< one-past-end row

	// Compute the rough size of a line...
	long pixels_offset = io->tell_proc(handle);
	long sz = ((eof - pixels_offset) / height);

	// ...and allocate cache of this size (yields good results)
	IOCache cache(io, handle, sz);
	if(cache.isNull()) {
		FreeImage_Unload(dib);
		dib = NULL;
		return;
	}
		
	int x = 0, y = 0;

	line_bits = FreeImage_GetScanLine(dib, y);

	while (y < height) {

		rle = cache.getByte();

		BOOL has_rle = rle & 0x80;
		rle &= ~0x80; // remove type-bit

		BYTE packet_count = rle + 1;

		//packet_count might be corrupt, test if we are not about to write beyond the last image bit

		if ((line_bits+x) + packet_count*pixel_size > dib_end) {
			FreeImage_OutputMessageProc(s_format_id, FI_MSG_ERROR_CORRUPTED);
			// return what is left from the bitmap
			return;
		}

		if (has_rle) {

			// read a pixel value from file...
			BYTE *val = cache.getBytes(file_pixel_size);

			//...and fill packet_count pixels with it

			for (int ix = 0; ix < packet_count; ix++) {
				_assignPixel<bPP>((line_bits+x), val, as24bit);
				x += pixel_size;

				if (x >= line_size) {
					x = 0;
					y++;
					line_bits = FreeImage_GetScanLine(dib, y);
				}
			}

		} else {
			// no rle commpresion

			// copy packet_count pixels from file to dib
			for (int ix = 0; ix < packet_count; ix++) {
				BYTE *val = cache.getBytes(file_pixel_size);
				_assignPixel<bPP>((line_bits+x), val, as24bit);
				x += pixel_size;

				if (x >= line_size) {
					x = 0;
					y++;
					line_bits = FreeImage_GetScanLine(dib, y);
				}
			} //< packet_count
		} //< has_rle

	} //< while height

}

// --------------------------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	FIBITMAP *dib = NULL;

	if (!handle) {
		return NULL;
	}

	try {
		
		const BOOL header_only =  (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;
				
		// remember the start offset
		long start_offset = io->tell_proc(handle);

		// remember end-of-file (used for RLE cache)
		io->seek_proc(handle, 0, SEEK_END);
		long eof = io->tell_proc(handle);
		io->seek_proc(handle, start_offset, SEEK_SET);

		// read and process the bitmap's footer

		TargaThumbnail thumbnail;
		if(isTARGA20(io, handle)) {
			TGAFOOTER footer;
			const long footer_offset = start_offset + eof - sizeof(footer);
			
			io->seek_proc(handle, footer_offset, SEEK_SET);
			io->read_proc(&footer, sizeof(tagTGAFOOTER), 1, handle);
			
#ifdef FREEIMAGE_BIGENDIAN
			SwapFooter(&footer);
#endif
			BOOL hasExtensionArea = footer.extension_offset > 0;
			if(hasExtensionArea) { 
				TGAEXTENSIONAREA extensionarea;
				io->seek_proc(handle, footer.extension_offset, SEEK_SET);
				io->read_proc(&extensionarea, sizeof(extensionarea), 1, handle);
				
#ifdef FREEIMAGE_BIGENDIAN
				SwapExtensionArea(&extensionarea);
#endif

				DWORD postage_stamp_offset = extensionarea.postage_stamp_offset;
				BOOL hasThumbnail = (postage_stamp_offset > 0) && (postage_stamp_offset < (DWORD)footer_offset);
				if(hasThumbnail) {
					io->seek_proc(handle, postage_stamp_offset, SEEK_SET);
					thumbnail.read(io, handle, footer_offset - postage_stamp_offset);
				}
			}
		}
		
		// read and process the bitmap's header

		TGAHEADER header;

		io->seek_proc(handle, start_offset, SEEK_SET);
		io->read_proc(&header, sizeof(tagTGAHEADER), 1, handle);

#ifdef FREEIMAGE_BIGENDIAN
		SwapHeader(&header);
#endif

		thumbnail.setDepth(header.is_pixel_depth);
			
		const int line = CalculateLine(header.is_width, header.is_pixel_depth);
		const int pixel_bits = header.is_pixel_depth;
		const int pixel_size = pixel_bits/8;

		int fliphoriz = (header.is_image_descriptor & 0x10) ? 1 : 0;
		int flipvert = (header.is_image_descriptor & 0x20) ? 1 : 0;

		// skip comment
		io->seek_proc(handle, header.id_length, SEEK_CUR);

		switch (header.is_pixel_depth) {
			case 8 : {
				dib = FreeImage_AllocateHeader(header_only, header.is_width, header.is_height, 8);
				
				if (dib == NULL) {
					throw FI_MSG_ERROR_DIB_MEMORY;
				}
					
				// read the palette (even if header only)

				RGBQUAD *palette = FreeImage_GetPalette(dib);

				if (header.color_map_type > 0) {
					unsigned count, csize;

					// calculate the color map size
					csize = header.cm_length * header.cm_size / 8;
					
					// read the color map
					BYTE *cmap = (BYTE*)malloc(csize * sizeof(BYTE));
					if (cmap == NULL) {
						throw FI_MSG_ERROR_DIB_MEMORY;
					}
					io->read_proc(cmap, sizeof(BYTE), csize, handle);

					// build the palette

					switch (header.cm_size) {
						case 16: {
							WORD *rgb555 = (WORD*)&cmap[0];
							unsigned start = (unsigned)header.cm_first_entry;
							unsigned stop = MIN((unsigned)256, (unsigned)header.cm_length);

							for (count = start; count < stop; count++) {
								palette[count].rgbRed   = (BYTE)((((*rgb555 & FI16_555_RED_MASK) >> FI16_555_RED_SHIFT) * 0xFF) / 0x1F);
								palette[count].rgbGreen = (BYTE)((((*rgb555 & FI16_555_GREEN_MASK) >> FI16_555_GREEN_SHIFT) * 0xFF) / 0x1F);
								palette[count].rgbBlue  = (BYTE)((((*rgb555 & FI16_555_BLUE_MASK) >> FI16_555_BLUE_SHIFT) * 0xFF) / 0x1F);
								rgb555++;
							}
						}
						break;

						case 24: {
							FILE_BGR *bgr = (FILE_BGR*)&cmap[0];
							unsigned start = (unsigned)header.cm_first_entry;
							unsigned stop = MIN((unsigned)256, (unsigned)header.cm_length);

							for (count = start; count < stop; count++) {
								palette[count].rgbBlue  = bgr->b;
								palette[count].rgbGreen = bgr->g;
								palette[count].rgbRed   = bgr->r;
								bgr++;
							}
						}
						break;

						case 32: {
							BYTE trns[256];

							// clear the transparency table
							memset(trns, 0xFF, 256);

							FILE_BGRA *bgra = (FILE_BGRA*)&cmap[0];
							unsigned start = (unsigned)header.cm_first_entry;
							unsigned stop = MIN((unsigned)256, (unsigned)header.cm_length);

							for (count = start; count < stop; count++) {
								palette[count].rgbBlue  = bgra->b;
								palette[count].rgbGreen = bgra->g;
								palette[count].rgbRed   = bgra->r;
								// alpha
								trns[count] = bgra->a;
								bgra++;
							}

							// set the tranparency table
							FreeImage_SetTransparencyTable(dib, trns, 256);
						}
						break;

					} // switch(header.cm_size)

					free(cmap);
				}
				
				// handle thumbnail
				
				FIBITMAP* th = thumbnail.toFIBITMAP();
				if(th) {
					RGBQUAD* pal = FreeImage_GetPalette(dib);
					RGBQUAD* dst_pal = FreeImage_GetPalette(th);
					if(dst_pal && pal) {
						for(unsigned i = 0; i < FreeImage_GetColorsUsed(dib); i++) {
							dst_pal[i] = pal[i];
						}
					}

					FreeImage_SetTransparencyTable(th, FreeImage_GetTransparencyTable(dib), FreeImage_GetTransparencyCount(dib));
					
					FreeImage_SetThumbnail(dib, th);
					FreeImage_Unload(th);				
				}

				if(header_only) {
					return dib;
				}
					
				// read in the bitmap bits

				switch (header.image_type) {
					case TGA_CMAP:
					case TGA_MONO: {
						BYTE *bits = NULL;

						for (unsigned count = 0; count < header.is_height; count++) {
							bits = FreeImage_GetScanLine(dib, count);
							io->read_proc(bits, sizeof(BYTE), line, handle);
						}
					}
					break;

					case TGA_RLECMAP:
					case TGA_RLEMONO: { //(8 bit)
						loadRLE<8>(dib, header.is_width, header.is_height, io, handle, eof, FALSE);
					}
					break;

					default :
						FreeImage_Unload(dib);
						return NULL;
				}
			}
			break; // header.is_pixel_depth == 8

			case 15 :

			case 16 : {
				int pixel_bits = 16;

				// allocate the dib

				if (TARGA_LOAD_RGB888 & flags) {
					pixel_bits = 24;
					dib = FreeImage_AllocateHeader(header_only, header.is_width, header.is_height, pixel_bits, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);

				} else {
					dib = FreeImage_AllocateHeader(header_only, header.is_width, header.is_height, pixel_bits, FI16_555_RED_MASK, FI16_555_GREEN_MASK, FI16_555_BLUE_MASK);
				}

				if (dib == NULL) {
					throw FI_MSG_ERROR_DIB_MEMORY;
				}
				
				// handle thumbnail
				
				FIBITMAP* th = thumbnail.toFIBITMAP();
				if(th) {
					if(TARGA_LOAD_RGB888 & flags) {
						FIBITMAP* t = FreeImage_ConvertTo24Bits(th);
						FreeImage_Unload(th);
						th = t;
					}

					FreeImage_SetThumbnail(dib, th);
					FreeImage_Unload(th);
				}
						
				if(header_only) {
					return dib;
				}

				int line = CalculateLine(header.is_width, pixel_bits);

				const unsigned pixel_size = unsigned(pixel_bits) / 8;
				const unsigned src_pixel_size = sizeof(WORD);

				// note header.cm_size is a misleading name, it should be seen as header.cm_bits
				// ignore current position in file and set filepointer explicitly from the beginning of the file

				int garblen = 0;

				if (header.color_map_type != 0) {
					garblen = (int)((header.cm_size + 7) / 8) * header.cm_length; /* should byte align */

				} else {
					garblen = 0;
				}

				io->seek_proc(handle, start_offset, SEEK_SET);
				io->seek_proc(handle, sizeof(tagTGAHEADER) + header.id_length + garblen, SEEK_SET);

				// read in the bitmap bits

				switch (header.image_type) {
					case TGA_RGB: { //(16 bit)
						// input line cache
						BYTE *in_line = (BYTE*)malloc(header.is_width * sizeof(WORD));

						if (!in_line)
							throw FI_MSG_ERROR_MEMORY;

						const int h = header.is_height;

						for (int y = 0; y < h; y++) {
							
							BYTE *bits = FreeImage_GetScanLine(dib, y);
							io->read_proc(in_line, src_pixel_size, header.is_width, handle);
							
							BYTE *val = in_line;
							for (int x = 0; x < line; x += pixel_size) {

								_assignPixel<16>(bits+x, val, TARGA_LOAD_RGB888 & flags);

								val += src_pixel_size;
							}
						}

						free(in_line);
					}
					break;

					case TGA_RLERGB: { //(16 bit)
						loadRLE<16>(dib, header.is_width, header.is_height, io, handle, eof, TARGA_LOAD_RGB888 & flags);
					}
					break;

					default :
						FreeImage_Unload(dib);
						return NULL;
				}
			}
			break; // header.is_pixel_depth == 15 or 16

			case 24 : {

				dib = FreeImage_AllocateHeader(header_only, header.is_width, header.is_height, pixel_bits, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);

				if (dib == NULL) {
					throw FI_MSG_ERROR_DIB_MEMORY;
				}
				
				FIBITMAP* th = thumbnail.toFIBITMAP();
				if(th) {
					FreeImage_SetThumbnail(dib, th);
					FreeImage_Unload(th);
				}
				
				if(header_only) {
					return dib;
				}
					
				// read in the bitmap bits

				switch (header.image_type) {
					case TGA_RGB: { //(24 bit)
						//uncompressed
						loadTrueColor(dib, header.is_width, header.is_height, pixel_size,io, handle, TRUE);
					}
					break;

					case TGA_RLERGB: { //(24 bit)
						loadRLE<24>(dib, header.is_width, header.is_height, io, handle, eof, TRUE);
					}
					break;

					default :
						FreeImage_Unload(dib);
						return NULL;
				}
			}
			break;	// header.is_pixel_depth == 24

			case 32 : {
				int pixel_bits = 32;

				if (TARGA_LOAD_RGB888 & flags) {
					pixel_bits = 24;
				}

				dib = FreeImage_AllocateHeader(header_only, header.is_width, header.is_height, pixel_bits, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);

				if (dib == NULL) {
					throw FI_MSG_ERROR_DIB_MEMORY;
				}
				
				// handle thumbnail
				
				FIBITMAP* th = thumbnail.toFIBITMAP();
				if(th) {
					if(TARGA_LOAD_RGB888 & flags) {
						FIBITMAP* t = FreeImage_ConvertTo24Bits(th);
						FreeImage_Unload(th);
						th = t;
					}
					FreeImage_SetThumbnail(dib, th);
					FreeImage_Unload(th);
				}

				if(header_only) {
					return dib;
				}
					
				// read in the bitmap bits

				switch (header.image_type) {
					case TGA_RGB: { //(32 bit)
						// uncompressed
						loadTrueColor(dib, header.is_width, header.is_height, 4 /*file_pixel_size*/, io, handle, TARGA_LOAD_RGB888 & flags);
					}
					break;

					case TGA_RLERGB: { //(32 bit)
						loadRLE<32>(dib, header.is_width, header.is_height, io, handle, eof, TARGA_LOAD_RGB888 & flags);
					}
					break;

					default :
						FreeImage_Unload(dib);
						return NULL;
				}
			}
			break; // header.is_pixel_depth == 32

		} // switch(header.is_pixel_depth)

		if (flipvert) {
			FreeImage_FlipVertical(dib);
		}

		if (fliphoriz) {
			FreeImage_FlipHorizontal(dib);
		}

		return dib;

	} catch (const char *message) {
		if (dib) {
			FreeImage_Unload(dib);
		}

		FreeImage_OutputMessageProc(s_format_id, message);

		return NULL;
	}
}

// --------------------------------------------------------------------------

static BOOL 
hasValidThumbnail(FIBITMAP* dib) {
	FIBITMAP* thumbnail = FreeImage_GetThumbnail(dib);
	
	return thumbnail 
		&& SupportsExportType(FreeImage_GetImageType(thumbnail)) 
		&& SupportsExportDepth(FreeImage_GetBPP(thumbnail))
		// Requirements according to the specification:
		&& FreeImage_GetBPP(thumbnail) == FreeImage_GetBPP(dib)
		&& FreeImage_GetImageType(thumbnail) == FreeImage_GetImageType(dib)
		&& FreeImage_GetWidth(thumbnail) <= 255
		&& FreeImage_GetHeight(thumbnail) <= 255;
}

/**
Writes the ready RLE packet to buffer
*/
static inline void 
flushPacket(BYTE*& dest, unsigned pixel_size, BYTE* packet_begin, BYTE*& packet, BYTE& packet_count, BOOL& has_rle) {
	if (packet_count) {
		const BYTE type_bit = has_rle ? 0x80 : 0x0;
		const BYTE write_count = has_rle ? 1 : packet_count;

		// build packet header: zero-based count + type bit
		assert(packet_count >= 1);
		BYTE rle = packet_count - 1;
		rle |= type_bit;

		// write packet header
		*dest = rle;
		++dest;

		// write packet data
		memcpy(dest, packet_begin, write_count * pixel_size);
		dest += write_count * pixel_size;

		// reset state
		packet_count = 0;
		packet = packet_begin;
		has_rle = FALSE;
	}
}


static inline void 
writeToPacket(BYTE* packet, BYTE* pixel, unsigned pixel_size) {
	// Take care of channel and byte order here, because packet will be flushed straight to the file
	switch (pixel_size) {
		case 1:
			*packet = *pixel;
			break;

		case 2: {
			WORD val(*(WORD*)pixel);
#ifdef FREEIMAGE_BIGENDIAN
			SwapShort(&val);
#endif
			*(WORD*)packet = val;
		}
		break;

		case 3: {
			packet[0] = pixel[FI_RGBA_BLUE];
			packet[1] = pixel[FI_RGBA_GREEN];
			packet[2] = pixel[FI_RGBA_RED];
		}
		break;

		case 4: {
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
			*(reinterpret_cast<unsigned*>(packet)) = *(reinterpret_cast<unsigned*> (pixel));
#else 
			packet[0] = pixel[FI_RGBA_BLUE];
			packet[1] = pixel[FI_RGBA_GREEN];
			packet[2] = pixel[FI_RGBA_RED];
			packet[3] = pixel[FI_RGBA_ALPHA];
#endif
		}
		break;

		default:
			assert(FALSE);
	}
}

static inline BOOL 
isEqualPixel(BYTE* lhs, BYTE* rhs, unsigned pixel_size) {
	switch (pixel_size) {
		case 1:
			return *lhs == *rhs;

		case 2:
			return *(WORD*)lhs == *(WORD*)rhs;

		case 3:
			return *(WORD*)lhs == *(WORD*)rhs && lhs[2] == rhs[2];

		case 4:
			return *(unsigned*)lhs == *(unsigned*)rhs;

		default:
			assert(FALSE);
			return FALSE;
	}
}

static void 
saveRLE(FIBITMAP* dib, FreeImageIO* io, fi_handle handle) {
	// Image is compressed line by line, packets don't span multiple lines (TGA2.0 recommendation)
		
	const unsigned width = FreeImage_GetWidth(dib);
	const unsigned height = FreeImage_GetHeight(dib);
	const unsigned pixel_size = FreeImage_GetBPP(dib)/8;
	const unsigned line_size = FreeImage_GetLine(dib);

	const BYTE max_packet_size = 128;
	BYTE packet_count = 0;
	BOOL has_rle = FALSE;

	// packet (compressed or not) to be written to line

	BYTE* const packet_begin = (BYTE*)malloc(max_packet_size * pixel_size);
	BYTE* packet = packet_begin;

	// line to be written to disk
	// Note: we need some extra bytes for anti-commpressed lines. The worst case is:
	// 8 bit images were every 3th pixel is different.
	// Rle packet becomes two pixels, but nothing is compressed: two byte pixels are transformed into byte header and byte pixel value
	// After every rle packet there is a non-rle packet of one pixel: an extra byte for the header will be added for it
	// In the end we gain no bytes from compression, but also must insert a byte at every 3th pixel

	// add extra space for anti-commpressed lines
	size_t extra_space = (size_t)ceil(width / 3.0);
	BYTE* const line_begin = (BYTE*)malloc(width * pixel_size + extra_space);
	BYTE* line = line_begin;

	BYTE *current = (BYTE*)malloc(pixel_size);
	BYTE *next    = (BYTE*)malloc(pixel_size);

	for(unsigned y = 0; y < height; y++) {
		BYTE *bits = FreeImage_GetScanLine(dib, y);

		// rewind line pointer
		line = line_begin;

		for(unsigned x = 0; x < line_size; x += pixel_size) {

			AssignPixel(current, (bits + x), pixel_size);

			// read next pixel from dib

			if( x + 1*pixel_size < line_size) {
				AssignPixel(next, (bits + x + 1*pixel_size), pixel_size);

			} else {
				// last pixel in line

				// include current pixel and flush
				if(!has_rle) {

					writeToPacket(packet, current, pixel_size);
					packet += pixel_size;

				}

				assert(packet_count < max_packet_size);

				++packet_count;

				flushPacket(line, pixel_size, packet_begin, packet, packet_count, has_rle);

				// start anew on next line
				break;
			}

			if(isEqualPixel(current, next, pixel_size)) {

				// has rle

				if(!has_rle) {
					// flush non rle packet

					flushPacket(line, pixel_size, packet_begin, packet, packet_count, has_rle);

					// start a rle packet

					has_rle = TRUE;

					writeToPacket(packet, current, pixel_size);
					packet += pixel_size;
				}

				// otherwise do nothing. We will just increase the count at the end

			} else {

				// no rle

				if(has_rle) {
					// flush rle packet

					// include current pixel first
					assert(packet_count < max_packet_size);
					++packet_count;

					flushPacket(line, pixel_size, packet_begin, packet, packet_count, has_rle);

					// start anew on the next pixel
					continue;

				} else {

					writeToPacket(packet, current, pixel_size);
					packet += pixel_size;
				}

			}

			// increase counter on every pixel

			++packet_count;

			if(packet_count == max_packet_size) {
				flushPacket(line, pixel_size, packet_begin, packet, packet_count, has_rle);
			}

		}//for width

		// write line to disk
		io->write_proc(line_begin, 1, (unsigned)(line - line_begin), handle);

	}//for height

	free(line_begin);
	free(packet_begin);
	free(current);
	free(next);
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	if ((dib == NULL) || (handle == NULL)) {
		return FALSE;
	}

	RGBQUAD *palette = FreeImage_GetPalette(dib);
	const unsigned bpp = FreeImage_GetBPP(dib);

	// write the file header

	TGAHEADER header;

	header.id_length = 0;
	header.cm_first_entry = 0;
	header.is_xorigin = 0;
	header.is_yorigin = 0;
	header.is_width = (WORD)FreeImage_GetWidth(dib);
	header.is_height = (WORD)FreeImage_GetHeight(dib);
	header.is_pixel_depth = (BYTE)bpp;
	header.is_image_descriptor = 0;

	if (palette) {
		header.color_map_type = 1;
		header.image_type = (TARGA_SAVE_RLE & flags) ? TGA_RLECMAP : TGA_CMAP;
		header.cm_length = (WORD)(1 << bpp);

		if (FreeImage_IsTransparent(dib)) {
			header.cm_size = 32;
		} else {
			header.cm_size = 24;
		}

	} else {
		header.color_map_type = 0;
		header.image_type = (TARGA_SAVE_RLE & flags) ? TGA_RLERGB : TGA_RGB;
		header.cm_length = 0;
		header.cm_size = 0;
	}

	// write the header

#ifdef FREEIMAGE_BIGENDIAN
	SwapHeader(&header);
#endif

	io->write_proc(&header, sizeof(header), 1, handle);

#ifdef FREEIMAGE_BIGENDIAN
	SwapHeader(&header);
#endif

	// write the palette

	if (palette) {
		if (FreeImage_IsTransparent(dib)) {
			FILE_BGRA *bgra_pal = (FILE_BGRA*)malloc(header.cm_length * sizeof(FILE_BGRA));

			// get the transparency table
			BYTE *trns = FreeImage_GetTransparencyTable(dib);

			for (unsigned i = 0; i < header.cm_length; i++) {
				bgra_pal[i].b = palette[i].rgbBlue;
				bgra_pal[i].g = palette[i].rgbGreen;
				bgra_pal[i].r = palette[i].rgbRed;
				bgra_pal[i].a = trns[i];
			}

			io->write_proc(bgra_pal, sizeof(FILE_BGRA), header.cm_length, handle);

			free(bgra_pal);

		} else {
			FILE_BGR *bgr_pal = (FILE_BGR*)malloc(header.cm_length * sizeof(FILE_BGR));

			for (unsigned i = 0; i < header.cm_length; i++) {
				bgr_pal[i].b = palette[i].rgbBlue;
				bgr_pal[i].g = palette[i].rgbGreen;
				bgr_pal[i].r = palette[i].rgbRed;
			}

			io->write_proc(bgr_pal, sizeof(FILE_BGR), header.cm_length, handle);

			free(bgr_pal);
		}
	}

	// write the data bits 


	if (TARGA_SAVE_RLE & flags) {
		
		saveRLE(dib, io, handle);

	} else {
		
		// -- no rle compression --
		
		const unsigned width = header.is_width;
		const unsigned height = header.is_height;
		const unsigned pixel_size = bpp/8;

		BYTE *line, *const line_begin = (BYTE*)malloc(width * pixel_size);
		BYTE *line_source = line_begin;

		for (unsigned y = 0; y < height; y++) {
			BYTE *scanline = FreeImage_GetScanLine(dib, y);

			// rewind the line pointer
			line = line_begin;

			switch (bpp) {
				case 8: {
					// don't copy line, read straight from dib
					line_source = scanline;
				}
				break;

				case 16: {
					for (unsigned x = 0; x < width; x++) {
						WORD pixel = *(((WORD *)scanline) + x);
						
#ifdef FREEIMAGE_BIGENDIAN
						SwapShort(&pixel);
#endif
						*(WORD*)line = pixel;

						line += pixel_size;
					}
				}
				break;

				case 24: {
					
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
						line_source = scanline;
#else 
					for (unsigned x = 0; x < width; ++x) {
						RGBTRIPLE* trip = ((RGBTRIPLE *)scanline) + x;
						line[0] = trip->rgbtBlue;
						line[1] = trip->rgbtGreen;
						line[2] = trip->rgbtRed;

						line += pixel_size;
					}
#endif
				}
				break;

				case 32: {

#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
					line_source = scanline;
#else 
					for (unsigned x = 0; x < width; ++x) {
						RGBQUAD* quad = ((RGBQUAD *)scanline) + x;
						line[0] = quad->rgbBlue;
						line[1] = quad->rgbGreen;
						line[2] = quad->rgbRed;
						line[3] = quad->rgbReserved;
						
						line += pixel_size;
					}
#endif
				}
				break;

			}//switch(bpp)

			// write line to disk

			io->write_proc(line_source, pixel_size, width, handle);

		}//for height

		free(line_begin);
	}

	
	long extension_offset = 0 ;
	if(hasValidThumbnail(dib)) {
		// write extension area
		
		extension_offset = io->tell_proc(handle);
		
		TGAEXTENSIONAREA ex;
		memset(&ex, 0, sizeof(ex));
		
		assert(sizeof(ex) == 495);
		ex.extension_size = sizeof(ex);
		ex.postage_stamp_offset = extension_offset + ex.extension_size + 0 /*< no Scan Line Table*/;
		ex.attributes_type = FreeImage_GetBPP(dib) == 32 ? 3 /*< useful Alpha channel data*/ : 0 /*< no Alpha data*/;
		
#ifdef FREEIMAGE_BIGENDIAN
		SwapExtensionArea(&ex);
#endif

		io->write_proc(&ex, sizeof(ex), 1, handle); 
		
		// (no Scan Line Table)
		
		// write thumbnail
		
		io->seek_proc(handle, ex.postage_stamp_offset, SEEK_SET);
		
		FIBITMAP* thumbnail = FreeImage_GetThumbnail(dib);
		BYTE width = (BYTE)FreeImage_GetWidth(thumbnail);
		BYTE height = (BYTE)FreeImage_GetHeight(thumbnail);
		
		io->write_proc(&width, 1, 1, handle); 
		io->write_proc(&height, 1, 1, handle); 
		
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
		SwapRedBlue32(dib); 
#endif
		
#ifdef FREEIMAGE_BIGENDIAN
		swapShortPixels(dib); 
#endif
		
		const unsigned line_size = FreeImage_GetLine(thumbnail);

		for (BYTE h = 0; h < height; ++h) {
			BYTE* src_line = FreeImage_GetScanLine(thumbnail, height - 1 - h);
			io->write_proc(src_line, 1, line_size, handle); 
		}
	}
	
	// (no Color Correction Table)
	
	// write the footer
	
	TGAFOOTER footer;
	footer.extension_offset = extension_offset;
	footer.developer_offset = 0;
	strcpy(footer.signature, "TRUEVISION-XFILE.");
	

#ifdef FREEIMAGE_BIGENDIAN
	SwapFooter(&footer);
#endif

	io->write_proc(&footer, sizeof(footer), 1, handle);

	return TRUE;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitTARGA(Plugin *plugin, int format_id) {
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
	plugin->save_proc = Save;
	plugin->validate_proc = Validate;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

// ==========================================================
// PNG Loader and Writer
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Herve Drolon (drolon@infonie.fr)
// - Detlev Vendt (detlev.vendt@brillit.de)
// - Aaron Shumate (trek@startreker.com)
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

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif

#include "FreeImage.h"
#include "Utilities.h"

#include "../Metadata/FreeImageTag.h"

// ----------------------------------------------------------

#define PNG_BYTES_TO_CHECK 8

#undef PNG_Z_DEFAULT_COMPRESSION	// already used in ../LibPNG/pnglibconf.h

// ----------------------------------------------------------

#include "../ZLib/zlib.h"
#include "../LibPNG/png.h"

// ----------------------------------------------------------

typedef struct {
    FreeImageIO *s_io;
    fi_handle    s_handle;
} fi_ioStructure, *pfi_ioStructure;

/////////////////////////////////////////////////////////////////////////////
// libpng interface 
// 

static void
_ReadProc(png_structp png_ptr, unsigned char *data, png_size_t size) {
    pfi_ioStructure pfio = (pfi_ioStructure)png_get_io_ptr(png_ptr);
	unsigned n = pfio->s_io->read_proc(data, (unsigned int)size, 1, pfio->s_handle);
	if(size && (n == 0)) {
		throw "Read error: invalid or corrupted PNG file";
	}
}

static void
_WriteProc(png_structp png_ptr, unsigned char *data, png_size_t size) {
    pfi_ioStructure pfio = (pfi_ioStructure)png_get_io_ptr(png_ptr);
    pfio->s_io->write_proc(data, (unsigned int)size, 1, pfio->s_handle);
}

static void
_FlushProc(png_structp png_ptr) {
	(png_structp)png_ptr;
	// empty flush implementation
}

static void
error_handler(png_structp png_ptr, const char *error) {
	(png_structp)png_ptr;
	throw error;
}

// in FreeImage warnings disabled

static void
warning_handler(png_structp png_ptr, const char *warning) {
	(png_structp)png_ptr;
	(char*)warning;
}

// ==========================================================
// Metadata routines
// ==========================================================

static BOOL 
ReadMetadata(png_structp png_ptr, png_infop info_ptr, FIBITMAP *dib) {
	// XMP keyword
	const char *g_png_xmp_keyword = "XML:com.adobe.xmp";

	FITAG *tag = NULL;
	png_textp text_ptr = NULL;
	int num_text = 0;

	// iTXt/tEXt/zTXt chuncks
	if(png_get_text(png_ptr, info_ptr, &text_ptr, &num_text) > 0) {
		for(int i = 0; i < num_text; i++) {
			// create a tag
			tag = FreeImage_CreateTag();
			if(!tag) return FALSE;

			DWORD tag_length = (DWORD) MAX(text_ptr[i].text_length, text_ptr[i].itxt_length);

			FreeImage_SetTagLength(tag, tag_length);
			FreeImage_SetTagCount(tag, tag_length);
			FreeImage_SetTagType(tag, FIDT_ASCII);
			FreeImage_SetTagValue(tag, text_ptr[i].text);

			if(strcmp(text_ptr[i].key, g_png_xmp_keyword) == 0) {
				// store the tag as XMP
				FreeImage_SetTagKey(tag, g_TagLib_XMPFieldName);
				FreeImage_SetMetadata(FIMD_XMP, dib, FreeImage_GetTagKey(tag), tag);
			} else {
				// store the tag as a comment
				FreeImage_SetTagKey(tag, text_ptr[i].key);
				FreeImage_SetMetadata(FIMD_COMMENTS, dib, FreeImage_GetTagKey(tag), tag);
			}
			
			// destroy the tag
			FreeImage_DeleteTag(tag);
		}
	}

	return TRUE;
}

static BOOL 
WriteMetadata(png_structp png_ptr, png_infop info_ptr, FIBITMAP *dib) {
	// XMP keyword
	const char *g_png_xmp_keyword = "XML:com.adobe.xmp";

	FITAG *tag = NULL;
	FIMETADATA *mdhandle = NULL;
	BOOL bResult = TRUE;

	png_text text_metadata;

	// set the 'Comments' metadata as iTXt chuncks

	mdhandle = FreeImage_FindFirstMetadata(FIMD_COMMENTS, dib, &tag);

	if(mdhandle) {
		do {
			memset(&text_metadata, 0, sizeof(png_text));
			text_metadata.compression = 1;							// iTXt, none
			text_metadata.key = (char*)FreeImage_GetTagKey(tag);	// keyword, 1-79 character description of "text"
			text_metadata.text = (char*)FreeImage_GetTagValue(tag);	// comment, may be an empty string (ie "")
			text_metadata.text_length = FreeImage_GetTagLength(tag);// length of the text string
			text_metadata.itxt_length = FreeImage_GetTagLength(tag);// length of the itxt string
			text_metadata.lang = 0;		 // language code, 0-79 characters or a NULL pointer
			text_metadata.lang_key = 0;	 // keyword translated UTF-8 string, 0 or more chars or a NULL pointer

			// set the tag 
			png_set_text(png_ptr, info_ptr, &text_metadata, 1);

		} while(FreeImage_FindNextMetadata(mdhandle, &tag));

		FreeImage_FindCloseMetadata(mdhandle);
		bResult &= TRUE;
	}

	// set the 'XMP' metadata as iTXt chuncks
	tag = NULL;
	FreeImage_GetMetadata(FIMD_XMP, dib, g_TagLib_XMPFieldName, &tag);
	if(tag && FreeImage_GetTagLength(tag)) {
		memset(&text_metadata, 0, sizeof(png_text));
		text_metadata.compression = 1;							// iTXt, none
		text_metadata.key = (char*)g_png_xmp_keyword;					// keyword, 1-79 character description of "text"
		text_metadata.text = (char*)FreeImage_GetTagValue(tag);	// comment, may be an empty string (ie "")
		text_metadata.text_length = FreeImage_GetTagLength(tag);// length of the text string
		text_metadata.itxt_length = FreeImage_GetTagLength(tag);// length of the itxt string
		text_metadata.lang = 0;		 // language code, 0-79 characters or a NULL pointer
		text_metadata.lang_key = 0;	 // keyword translated UTF-8 string, 0 or more chars or a NULL pointer

		// set the tag 
		png_set_text(png_ptr, info_ptr, &text_metadata, 1);
		bResult &= TRUE;
	}

	return bResult;
}

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "PNG";
}

static const char * DLL_CALLCONV
Description() {
	return "Portable Network Graphics";
}

static const char * DLL_CALLCONV
Extension() {
	return "png";
}

static const char * DLL_CALLCONV
RegExpr() {
	return "^.PNG\r";
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/png";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE png_signature[8] = { 137, 80, 78, 71, 13, 10, 26, 10 };
	BYTE signature[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };

	io->read_proc(&signature, 1, 8, handle);

	return (memcmp(png_signature, signature, 8) == 0);
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
			(depth == 1) ||
			(depth == 4) ||
			(depth == 8) ||
			(depth == 24) ||
			(depth == 32)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (
		(type == FIT_BITMAP) ||
		(type == FIT_UINT16) ||
		(type == FIT_RGB16) ||
		(type == FIT_RGBA16)
	);
}

static BOOL DLL_CALLCONV
SupportsICCProfiles() {
	return TRUE;
}

static BOOL DLL_CALLCONV
SupportsNoPixels() {
	return TRUE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	png_structp png_ptr = NULL;
	png_infop info_ptr;
	png_uint_32 width, height;
	png_colorp png_palette = NULL;
	int color_type, palette_entries = 0;
	int bit_depth, pixel_depth;		// pixel_depth = bit_depth * channels

	FIBITMAP *dib = NULL;
	RGBQUAD *palette = NULL;		// pointer to dib palette
	png_bytepp  row_pointers = NULL;
	int i;

    fi_ioStructure fio;
    fio.s_handle = handle;
	fio.s_io = io;
    
	if (handle) {
		BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

		try {		
			// check to see if the file is in fact a PNG file

			BYTE png_check[PNG_BYTES_TO_CHECK];

			io->read_proc(png_check, PNG_BYTES_TO_CHECK, 1, handle);

			if (png_sig_cmp(png_check, (png_size_t)0, PNG_BYTES_TO_CHECK) != 0) {
				return NULL;	// Bad signature
			}
			
			// create the chunk manage structure

			png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, (png_voidp)NULL, error_handler, warning_handler);

			if (!png_ptr) {
				return NULL;			
			}

			// create the info structure

		    info_ptr = png_create_info_struct(png_ptr);

			if (!info_ptr) {
				png_destroy_read_struct(&png_ptr, (png_infopp)NULL, (png_infopp)NULL);
				return NULL;
			}

			// init the IO

			png_set_read_fn(png_ptr, &fio, _ReadProc);

            if (setjmp(png_jmpbuf(png_ptr))) {
				png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
				return NULL;
			}

			// because we have already read the signature...

			png_set_sig_bytes(png_ptr, PNG_BYTES_TO_CHECK);

			// read the IHDR chunk

			png_read_info(png_ptr, info_ptr);
			png_get_IHDR(png_ptr, info_ptr, &width, &height, &bit_depth, &color_type, NULL, NULL, NULL);

			pixel_depth = png_get_bit_depth(png_ptr, info_ptr) * png_get_channels(png_ptr, info_ptr);

			// get image data type (assume standard image type)

			FREE_IMAGE_TYPE image_type = FIT_BITMAP;
			if (bit_depth == 16) {
				if ((pixel_depth == 16) && (color_type == PNG_COLOR_TYPE_GRAY)) {
					image_type = FIT_UINT16;
				} 
				else if ((pixel_depth == 48) && (color_type == PNG_COLOR_TYPE_RGB)) {
					image_type = FIT_RGB16;
				} 
				else if ((pixel_depth == 64) && (color_type == PNG_COLOR_TYPE_RGB_ALPHA)) {
					image_type = FIT_RGBA16;
				} else {
					// tell libpng to strip 16 bit/color files down to 8 bits/color
					png_set_strip_16(png_ptr);
					bit_depth = 8;
				}
			}

#ifndef FREEIMAGE_BIGENDIAN
			if((image_type == FIT_UINT16) || (image_type == FIT_RGB16) || (image_type == FIT_RGBA16)) {
				// turn on 16 bit byte swapping
				png_set_swap(png_ptr);
			}
#endif						

			// set some additional flags

			switch(color_type) {
				case PNG_COLOR_TYPE_RGB:
				case PNG_COLOR_TYPE_RGB_ALPHA:
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
					// flip the RGB pixels to BGR (or RGBA to BGRA)

					if(image_type == FIT_BITMAP) {
						png_set_bgr(png_ptr);
					}
#endif
					break;

				case PNG_COLOR_TYPE_PALETTE:
					// expand palette images to the full 8 bits from 2 bits/pixel

					if (pixel_depth == 2) {
						png_set_packing(png_ptr);
						pixel_depth = 8;
					}					

					break;

				case PNG_COLOR_TYPE_GRAY:
					// expand grayscale images to the full 8 bits from 2 bits/pixel
					// but *do not* expand fully transparent palette entries to a full alpha channel

					if (pixel_depth == 2) {
						png_set_expand_gray_1_2_4_to_8(png_ptr);
						pixel_depth = 8;
					}

					break;

				case PNG_COLOR_TYPE_GRAY_ALPHA:
					// expand 8-bit greyscale + 8-bit alpha to 32-bit

					png_set_gray_to_rgb(png_ptr);
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
					// flip the RGBA pixels to BGRA

					png_set_bgr(png_ptr);
#endif
					pixel_depth = 32;

					break;

				default:
					throw FI_MSG_ERROR_UNSUPPORTED_FORMAT;
			}

			// unlike the example in the libpng documentation, we have *no* idea where
			// this file may have come from--so if it doesn't have a file gamma, don't
			// do any correction ("do no harm")

			if (png_get_valid(png_ptr, info_ptr, PNG_INFO_gAMA)) {
				double gamma = 0;
				double screen_gamma = 2.2;

				if (png_get_gAMA(png_ptr, info_ptr, &gamma) && ( flags & PNG_IGNOREGAMMA ) != PNG_IGNOREGAMMA) {
					png_set_gamma(png_ptr, screen_gamma, gamma);
				}
			}

			// all transformations have been registered; now update info_ptr data

			png_read_update_info(png_ptr, info_ptr);

			// color type may have changed, due to our transformations

			color_type = png_get_color_type(png_ptr,info_ptr);

			// create a DIB and write the bitmap header
			// set up the DIB palette, if needed

			switch (color_type) {
				case PNG_COLOR_TYPE_RGB:
					png_set_invert_alpha(png_ptr);

					if(image_type == FIT_BITMAP) {
						dib = FreeImage_AllocateHeader(header_only, width, height, 24, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
					} else {
						dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height, pixel_depth);
					}
					break;

				case PNG_COLOR_TYPE_RGB_ALPHA:
					if(image_type == FIT_BITMAP) {
						dib = FreeImage_AllocateHeader(header_only, width, height, 32, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
					} else {
						dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height, pixel_depth);
					}
					break;

				case PNG_COLOR_TYPE_PALETTE:
					dib = FreeImage_AllocateHeader(header_only, width, height, pixel_depth);

					png_get_PLTE(png_ptr,info_ptr, &png_palette, &palette_entries);

					palette_entries = MIN((unsigned)palette_entries, FreeImage_GetColorsUsed(dib));
					palette = FreeImage_GetPalette(dib);

					// store the palette

					for (i = 0; i < palette_entries; i++) {
						palette[i].rgbRed   = png_palette[i].red;
						palette[i].rgbGreen = png_palette[i].green;
						palette[i].rgbBlue  = png_palette[i].blue;
					}
					break;

				case PNG_COLOR_TYPE_GRAY:
					dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height, pixel_depth);

					if(pixel_depth <= 8) {
						palette = FreeImage_GetPalette(dib);
						palette_entries = 1 << pixel_depth;

						for (i = 0; i < palette_entries; i++) {
							palette[i].rgbRed   =
							palette[i].rgbGreen =
							palette[i].rgbBlue  = (BYTE)((i * 255) / (palette_entries - 1));
						}
					}
					break;

				default:
					throw FI_MSG_ERROR_UNSUPPORTED_FORMAT;
			}

			// store the transparency table

			if (png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS)) {
				// array of alpha (transparency) entries for palette
				png_bytep trans_alpha = NULL;
				// number of transparent entries
				int num_trans = 0;						
				// graylevel or color sample values of the single transparent color for non-paletted images
				png_color_16p trans_color = NULL;

				png_get_tRNS(png_ptr, info_ptr, &trans_alpha, &num_trans, &trans_color);

				if((color_type == PNG_COLOR_TYPE_GRAY) && trans_color) {
					// single transparent color
					if (trans_color->gray < palette_entries) { 
						BYTE table[256]; 
						memset(table, 0xFF, palette_entries); 
						table[trans_color->gray] = 0; 
						FreeImage_SetTransparencyTable(dib, table, palette_entries); 
					}
				} else if((color_type == PNG_COLOR_TYPE_PALETTE) && trans_alpha) {
					// transparency table
					FreeImage_SetTransparencyTable(dib, (BYTE *)trans_alpha, num_trans);
				}
			}

			// store the background color 

			if (png_get_valid(png_ptr, info_ptr, PNG_INFO_bKGD)) {
				// Get the background color to draw transparent and alpha images over.
				// Note that even if the PNG file supplies a background, you are not required to
				// use it - you should use the (solid) application background if it has one.

				png_color_16p image_background = NULL;
				RGBQUAD rgbBkColor;

				if (png_get_bKGD(png_ptr, info_ptr, &image_background)) {
					rgbBkColor.rgbRed      = (BYTE)image_background->red;
					rgbBkColor.rgbGreen    = (BYTE)image_background->green;
					rgbBkColor.rgbBlue     = (BYTE)image_background->blue;
					rgbBkColor.rgbReserved = 0;

					FreeImage_SetBackgroundColor(dib, &rgbBkColor);
				}
			}

			// get physical resolution

			if (png_get_valid(png_ptr, info_ptr, PNG_INFO_pHYs)) {
				png_uint_32 res_x, res_y;
				
				// we'll overload this var and use 0 to mean no phys data,
				// since if it's not in meters we can't use it anyway

				int res_unit_type = PNG_RESOLUTION_UNKNOWN;

				png_get_pHYs(png_ptr,info_ptr, &res_x, &res_y, &res_unit_type);

				if (res_unit_type == PNG_RESOLUTION_METER) {
					FreeImage_SetDotsPerMeterX(dib, res_x);
					FreeImage_SetDotsPerMeterY(dib, res_y);
				}
			}

			// get possible ICC profile

			if (png_get_valid(png_ptr, info_ptr, PNG_INFO_iCCP)) {
				png_charp profile_name = NULL;
				png_bytep profile_data = NULL;
				png_uint_32 profile_length = 0;
				int  compression_type;

				png_get_iCCP(png_ptr, info_ptr, &profile_name, &compression_type, &profile_data, &profile_length);

				// copy ICC profile data (must be done after FreeImage_AllocateHeader)

				FreeImage_CreateICCProfile(dib, profile_data, profile_length);
			}

			// --- header only mode => clean-up and return

			if (header_only) {
				// get possible metadata (it can be located both before and after the image data)
				ReadMetadata(png_ptr, info_ptr, dib);
				if (png_ptr) {
					// clean up after the read, and free any memory allocated - REQUIRED
					png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp)NULL);
				}
				return dib;
			}

			// set the individual row_pointers to point at the correct offsets

			row_pointers = (png_bytepp)malloc(height * sizeof(png_bytep));

			if (!row_pointers) {
				png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
				FreeImage_Unload(dib);
				return NULL;
			}

			// read in the bitmap bits via the pointer table
			// allow loading of PNG with minor errors (such as images with several IDAT chunks)

			for (png_uint_32 k = 0; k < height; k++) {
				row_pointers[height - 1 - k] = FreeImage_GetScanLine(dib, k);			
			}

			png_set_benign_errors(png_ptr, 1);
			png_read_image(png_ptr, row_pointers);

			// check if the bitmap contains transparency, if so enable it in the header

			if (FreeImage_GetBPP(dib) == 32) {
				if (FreeImage_GetColorType(dib) == FIC_RGBALPHA) {
					FreeImage_SetTransparent(dib, TRUE);
				} else {
					FreeImage_SetTransparent(dib, FALSE);
				}
			}
				
			// cleanup

			if (row_pointers) {
				free(row_pointers);
				row_pointers = NULL;
			}

			// read the rest of the file, getting any additional chunks in info_ptr

			png_read_end(png_ptr, info_ptr);

			// get possible metadata (it can be located both before and after the image data)

			ReadMetadata(png_ptr, info_ptr, dib);

			if (png_ptr) {
				// clean up after the read, and free any memory allocated - REQUIRED
				png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp)NULL);
			}

			return dib;

		} catch (const char *text) {
			if (png_ptr) {
				png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp)NULL);
			}
			if (row_pointers) {
				free(row_pointers);
			}
			if (dib) {
				FreeImage_Unload(dib);			
			}
			FreeImage_OutputMessageProc(s_format_id, text);
			
			return NULL;
		}
	}			

	return NULL;
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	png_structp png_ptr;
	png_infop info_ptr;
	png_colorp palette = NULL;
	png_uint_32 width, height;
	BOOL has_alpha_channel = FALSE;

	RGBQUAD *pal;					// pointer to dib palette
	int bit_depth, pixel_depth;		// pixel_depth = bit_depth * channels
	int palette_entries;
	int	interlace_type;

	fi_ioStructure fio;
    fio.s_handle = handle;
	fio.s_io = io;

	if ((dib) && (handle)) {
		try {
			// create the chunk manage structure

			png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, (png_voidp)NULL, error_handler, warning_handler);

			if (!png_ptr)  {
				return FALSE;
			}

			// allocate/initialize the image information data.

			info_ptr = png_create_info_struct(png_ptr);

			if (!info_ptr)  {
				png_destroy_write_struct(&png_ptr,  (png_infopp)NULL);
				return FALSE;
			}

			// Set error handling.  REQUIRED if you aren't supplying your own
			// error handling functions in the png_create_write_struct() call.

			if (setjmp(png_jmpbuf(png_ptr)))  {
				// if we get here, we had a problem reading the file

				png_destroy_write_struct(&png_ptr, &info_ptr);

				return FALSE;
			}

			// init the IO
            
			png_set_write_fn(png_ptr, &fio, _WriteProc, _FlushProc);

			// set physical resolution

			png_uint_32 res_x = (png_uint_32)FreeImage_GetDotsPerMeterX(dib);
			png_uint_32 res_y = (png_uint_32)FreeImage_GetDotsPerMeterY(dib);

			if ((res_x > 0) && (res_y > 0))  {
				png_set_pHYs(png_ptr, info_ptr, res_x, res_y, PNG_RESOLUTION_METER);
			}
	
			// Set the image information here.  Width and height are up to 2^31,
			// bit_depth is one of 1, 2, 4, 8, or 16, but valid values also depend on
			// the color_type selected. color_type is one of PNG_COLOR_TYPE_GRAY,
			// PNG_COLOR_TYPE_GRAY_ALPHA, PNG_COLOR_TYPE_PALETTE, PNG_COLOR_TYPE_RGB,
			// or PNG_COLOR_TYPE_RGB_ALPHA.  interlace is either PNG_INTERLACE_NONE or
			// PNG_INTERLACE_ADAM7, and the compression_type and filter_type MUST
			// currently be PNG_COMPRESSION_TYPE_BASE and PNG_FILTER_TYPE_BASE. REQUIRED

			width = FreeImage_GetWidth(dib);
			height = FreeImage_GetHeight(dib);
			pixel_depth = FreeImage_GetBPP(dib);

			BOOL bInterlaced = FALSE;
			if( (flags & PNG_INTERLACED) == PNG_INTERLACED) {
				interlace_type = PNG_INTERLACE_ADAM7;
				bInterlaced = TRUE;
			} else {
				interlace_type = PNG_INTERLACE_NONE;
			}

			// set the ZLIB compression level or default to PNG default compression level (ZLIB level = 6)
			int zlib_level = flags & 0x0F;
			if((zlib_level >= 1) && (zlib_level <= 9)) {
				png_set_compression_level(png_ptr, zlib_level);
			} else if((flags & PNG_Z_NO_COMPRESSION) == PNG_Z_NO_COMPRESSION) {
				png_set_compression_level(png_ptr, Z_NO_COMPRESSION);
			}

			// filtered strategy works better for high color images
			if(pixel_depth >= 16){
				png_set_compression_strategy(png_ptr, Z_FILTERED);
				png_set_filter(png_ptr, 0, PNG_FILTER_NONE|PNG_FILTER_SUB|PNG_FILTER_PAETH);
			} else {
				png_set_compression_strategy(png_ptr, Z_DEFAULT_STRATEGY);
			}

			FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
			if(image_type == FIT_BITMAP) {
				// standard image type
				bit_depth = (pixel_depth > 8) ? 8 : pixel_depth;
			} else {
				// 16-bit greyscale or 16-bit RGB(A)
				bit_depth = 16;
			}

			// check for transparent images
			BOOL bIsTransparent = 
				(image_type == FIT_BITMAP) && FreeImage_IsTransparent(dib) && (FreeImage_GetTransparencyCount(dib) > 0) ? TRUE : FALSE;

			switch (FreeImage_GetColorType(dib)) {
				case FIC_MINISWHITE:
					if(!bIsTransparent) {
						// Invert monochrome files to have 0 as black and 1 as white (no break here)
						png_set_invert_mono(png_ptr);
					}
					// (fall through)

				case FIC_MINISBLACK:
					if(!bIsTransparent) {
						png_set_IHDR(png_ptr, info_ptr, width, height, bit_depth, 
							PNG_COLOR_TYPE_GRAY, interlace_type, 
							PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);
						break;
					}
					// If a monochrome image is transparent, save it with a palette
					// (fall through)

				case FIC_PALETTE:
				{
					png_set_IHDR(png_ptr, info_ptr, width, height, bit_depth, 
						PNG_COLOR_TYPE_PALETTE, interlace_type, 
						PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);

					// set the palette

					palette_entries = 1 << bit_depth;
					palette = (png_colorp)png_malloc(png_ptr, palette_entries * sizeof (png_color));
					pal = FreeImage_GetPalette(dib);

					for (int i = 0; i < palette_entries; i++) {
						palette[i].red   = pal[i].rgbRed;
						palette[i].green = pal[i].rgbGreen;
						palette[i].blue  = pal[i].rgbBlue;
					}
					
					png_set_PLTE(png_ptr, info_ptr, palette, palette_entries);

					// You must not free palette here, because png_set_PLTE only makes a link to
					// the palette that you malloced.  Wait until you are about to destroy
					// the png structure.

					break;
				}

				case FIC_RGBALPHA :
					has_alpha_channel = TRUE;

					png_set_IHDR(png_ptr, info_ptr, width, height, bit_depth, 
						PNG_COLOR_TYPE_RGBA, interlace_type, 
						PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);

#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
					// flip BGR pixels to RGB
					if(image_type == FIT_BITMAP) {
						png_set_bgr(png_ptr);
					}
#endif
					break;
	
				case FIC_RGB:
					png_set_IHDR(png_ptr, info_ptr, width, height, bit_depth, 
						PNG_COLOR_TYPE_RGB, interlace_type, 
						PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);

#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
					// flip BGR pixels to RGB
					if(image_type == FIT_BITMAP) {
						png_set_bgr(png_ptr);
					}
#endif
					break;
					
				case FIC_CMYK:
					break;
			}

			// write possible ICC profile

			FIICCPROFILE *iccProfile = FreeImage_GetICCProfile(dib);
			if (iccProfile->size && iccProfile->data) {
				png_set_iCCP(png_ptr, info_ptr, "Embedded Profile", 0, (png_const_bytep)iccProfile->data, iccProfile->size);
			}

			// write metadata

			WriteMetadata(png_ptr, info_ptr, dib);

			// Optional gamma chunk is strongly suggested if you have any guess
			// as to the correct gamma of the image.
			// png_set_gAMA(png_ptr, info_ptr, gamma);

			// set the transparency table

			if (bIsTransparent) {
				png_set_tRNS(png_ptr, info_ptr, FreeImage_GetTransparencyTable(dib), FreeImage_GetTransparencyCount(dib), NULL);
			}

			// set the background color

			if(FreeImage_HasBackgroundColor(dib)) {
				png_color_16 image_background;
				RGBQUAD rgbBkColor;

				FreeImage_GetBackgroundColor(dib, &rgbBkColor);
				memset(&image_background, 0, sizeof(png_color_16));
				image_background.blue  = rgbBkColor.rgbBlue;
				image_background.green = rgbBkColor.rgbGreen;
				image_background.red   = rgbBkColor.rgbRed;
				image_background.index = rgbBkColor.rgbReserved;

				png_set_bKGD(png_ptr, info_ptr, &image_background);
			}
			
			// Write the file header information.

			png_write_info(png_ptr, info_ptr);

			// write out the image data

#ifndef FREEIMAGE_BIGENDIAN
			if (bit_depth == 16) {
				// turn on 16 bit byte swapping
				png_set_swap(png_ptr);
			}
#endif

			int number_passes = 1;
			if (bInterlaced) {
				number_passes = png_set_interlace_handling(png_ptr);
			}

			if ((pixel_depth == 32) && (!has_alpha_channel)) {
				BYTE *buffer = (BYTE *)malloc(width * 3);

				// transparent conversion to 24-bit
				// the number of passes is either 1 for non-interlaced images, or 7 for interlaced images
				for (int pass = 0; pass < number_passes; pass++) {
					for (png_uint_32 k = 0; k < height; k++) {
						FreeImage_ConvertLine32To24(buffer, FreeImage_GetScanLine(dib, height - k - 1), width);			
						png_write_row(png_ptr, buffer);
					}
				}
				free(buffer);
			} else {
				// the number of passes is either 1 for non-interlaced images, or 7 for interlaced images
				for (int pass = 0; pass < number_passes; pass++) {
					for (png_uint_32 k = 0; k < height; k++) {			
						png_write_row(png_ptr, FreeImage_GetScanLine(dib, height - k - 1));					
					}
				}
			}

			// It is REQUIRED to call this to finish writing the rest of the file
			// Bug with png_flush

			png_write_end(png_ptr, info_ptr);

			// clean up after the write, and free any memory allocated
			if (palette) {
				png_free(png_ptr, palette);
			}

			png_destroy_write_struct(&png_ptr, &info_ptr);

			return TRUE;
		} catch (const char *text) {
			FreeImage_OutputMessageProc(s_format_id, text);
		}
	}

	return FALSE;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitPNG(Plugin *plugin, int format_id) {
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
	plugin->supports_icc_profiles_proc = SupportsICCProfiles;
	plugin->supports_no_pixels_proc = SupportsNoPixels;
}

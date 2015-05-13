// ==========================================================
// FreeImage implementation
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
// - Hervé Drolon (drolon@infonie.fr)
// - Detlev Vendt (detlev.vendt@brillit.de)
// - Petr Supina (psup@centrum.cz)
// - Carsten Klein (c.klein@datagis.com)
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

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif 

#include <stdlib.h>
#if defined(_WIN32) || defined(_WIN64) || defined(__MINGW32__)
#include <malloc.h>
#endif // _WIN32 || _WIN64 || __MINGW32__

#include "FreeImage.h"
#include "FreeImageIO.h"
#include "Utilities.h"

#include "../Metadata/FreeImageTag.h"

/** Constants for the BITMAPINFOHEADER::biCompression field */
#ifndef _WINGDI_
#define BI_RGB       0L
#define BI_BITFIELDS 3L
#endif // _WINGDI_

// ----------------------------------------------------------
//  Metadata definitions
// ----------------------------------------------------------

// helper for map<key, value> where value is a pointer to a FreeImage tag
typedef std::map<std::string, FITAG*> TAGMAP;

// helper for map<FREE_IMAGE_MDMODEL, TAGMAP*>
typedef std::map<int, TAGMAP*> METADATAMAP;

// helper for metadata iterator
FI_STRUCT (METADATAHEADER) { 
	long pos;		// current position when iterating the map
	TAGMAP *tagmap;	// pointer to the tag map
};

// ----------------------------------------------------------
//  FIBITMAP definition
// ----------------------------------------------------------

FI_STRUCT (FREEIMAGEHEADER) {
	FREE_IMAGE_TYPE type;		// data type - bitmap, array of long, double, complex, etc

	RGBQUAD bkgnd_color;		// background color used for RGB transparency

	BOOL transparent;			// why another table? for easy transparency table retrieval!
	int  transparency_count;	// transparency could be stored in the palette, which is better
	BYTE transparent_table[256];// overall, but it requires quite some changes and it will render
								// FreeImage_GetTransparencyTable obsolete in its current form;
	FIICCPROFILE iccProfile;	// space to hold ICC profile

	METADATAMAP *metadata;		// contains a list of metadata models attached to the bitmap

	BOOL has_pixels;			// FALSE if the FIBITMAP only contains the header and no pixel data

	FIBITMAP *thumbnail;		// optionally contains a thumbnail attached to the bitmap

	//BYTE filler[1];			 // fill to 32-bit alignment
};

// ----------------------------------------------------------
//  FREEIMAGERGBMASKS definition
// ----------------------------------------------------------

FI_STRUCT (FREEIMAGERGBMASKS) {
	unsigned red_mask;			// bit layout of the red components
	unsigned green_mask;		// bit layout of the green components
	unsigned blue_mask;			// bit layout of the blue components
};

// ----------------------------------------------------------
//  Memory allocation on a specified alignment boundary
// ----------------------------------------------------------

#if (defined(_WIN32) || defined(_WIN64)) && !defined(__MINGW32__)

void* FreeImage_Aligned_Malloc(size_t amount, size_t alignment) {
	assert(alignment == FIBITMAP_ALIGNMENT);
	return _aligned_malloc(amount, alignment);
}

void FreeImage_Aligned_Free(void* mem) {
	_aligned_free(mem);
}

#elif defined (__MINGW32__)

void* FreeImage_Aligned_Malloc(size_t amount, size_t alignment) {
	assert(alignment == FIBITMAP_ALIGNMENT);
	return __mingw_aligned_malloc (amount, alignment);
}

void FreeImage_Aligned_Free(void* mem) {
	__mingw_aligned_free (mem);
}

#else

void* FreeImage_Aligned_Malloc(size_t amount, size_t alignment) {
	assert(alignment == FIBITMAP_ALIGNMENT);
	/*
	In some rare situations, the malloc routines can return misaligned memory. 
	The routine FreeImage_Aligned_Malloc allocates a bit more memory to do
	aligned writes.  Normally, it *should* allocate "alignment" extra memory and then writes
	one dword back the true pointer.  But if the memory manager returns a
	misaligned block that is less than a dword from the next alignment, 
	then the writing back one dword will corrupt memory.

	For example, suppose that alignment is 16 and malloc returns the address 0xFFFF.

	16 - 0xFFFF % 16 + 0xFFFF = 16 - 15 + 0xFFFF = 0x10000.

	Now, you subtract one dword from that and write and that will corrupt memory.

	That's why the code below allocates *two* alignments instead of one. 
	*/
	void* mem_real = malloc(amount + 2 * alignment);
	if(!mem_real) return NULL;
	char* mem_align = (char*)((unsigned long)(2 * alignment - (unsigned long)mem_real % (unsigned long)alignment) + (unsigned long)mem_real);
	*((long*)mem_align - 1) = (long)mem_real;
	return mem_align;
}

void FreeImage_Aligned_Free(void* mem) {
	free((void*)*((long*)mem - 1));
}

#endif // _WIN32 || _WIN64

// ----------------------------------------------------------
//  DIB information functions
// ----------------------------------------------------------

/**
Calculate the size of a FreeImage image. 
Align the palette and the pixels on a FIBITMAP_ALIGNMENT bytes alignment boundary.

@param header_only If TRUE, calculate a 'header only' FIBITMAP size, otherwise calculate a full FIBITMAP size
@param width
@param height
@param bpp
@param need_masks
@see FreeImage_AllocateHeaderT
*/
static size_t 
FreeImage_GetImageSizeHeader(BOOL header_only, unsigned width, unsigned height, unsigned bpp, BOOL need_masks) {
	size_t dib_size = sizeof(FREEIMAGEHEADER); 
	dib_size += (dib_size % FIBITMAP_ALIGNMENT ? FIBITMAP_ALIGNMENT - dib_size % FIBITMAP_ALIGNMENT : 0);  
	dib_size += FIBITMAP_ALIGNMENT - sizeof(BITMAPINFOHEADER) % FIBITMAP_ALIGNMENT; 
	dib_size += sizeof(BITMAPINFOHEADER);  
	// palette is aligned on a 16 bytes boundary
	dib_size += sizeof(RGBQUAD) * CalculateUsedPaletteEntries(bpp);
	// we both add palette size and masks size if need_masks is true, since CalculateUsedPaletteEntries
	// always returns 0 if need_masks is true (which is only true for 16 bit images).
	dib_size += need_masks ? sizeof(DWORD) * 3 : 0;
	dib_size += (dib_size % FIBITMAP_ALIGNMENT ? FIBITMAP_ALIGNMENT - dib_size % FIBITMAP_ALIGNMENT : 0);  
	if(!header_only) {
		const size_t header_size = dib_size;

		// pixels are aligned on a 16 bytes boundary
		dib_size += (size_t)CalculatePitch(CalculateLine(width, bpp)) * (size_t)height; 

		// check for possible malloc overflow using a KISS integer overflow detection mechanism
		{
			/*
			The following constant take into account the additionnal memory used by 
			aligned malloc functions as well as debug malloc functions. 
			It is supposed here that using a (8 * FIBITMAP_ALIGNMENT) risk margin will be enough
			for the target compiler. 
			*/
			const double FIBITMAP_MAX_MEMORY = (double)((size_t)-1) - 8 * FIBITMAP_ALIGNMENT;
			const double dPitch = floor( ((double)bpp * width + 31.0) / 32.0 ) * 4.0;
			const double dImageSize = (double)header_size + dPitch * height;
			if(dImageSize != (double)dib_size) {
				// here, we are sure to encounter a malloc overflow: try to avoid it ...
				return 0;
			}
			if(dImageSize > FIBITMAP_MAX_MEMORY) {
				// avoid possible overflow inside C allocation functions
				return 0;
			}
		}
	}

	return dib_size;
}

/**
Helper for 16-bit FIT_BITMAP
Returns a pointer to the bitmap's red-, green- and blue masks.
@param dib The bitmap to obtain masks from.
@return Returns a pointer to the bitmap's red-, green- and blue masks
or NULL, if no masks are present (e.g. for 24 bit images).
*/
static FREEIMAGERGBMASKS *
FreeImage_GetRGBMasks(FIBITMAP *dib) {
	return FreeImage_HasRGBMasks(dib) ? (FREEIMAGERGBMASKS *)(((BYTE *)FreeImage_GetInfoHeader(dib)) + sizeof(BITMAPINFOHEADER)) : NULL;
}

FIBITMAP * DLL_CALLCONV
FreeImage_AllocateHeaderT(BOOL header_only, FREE_IMAGE_TYPE type, int width, int height, int bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask) {

	// check input variables
	width = abs(width);
	height = abs(height);
	if(!((width > 0) && (height > 0))) {
		return NULL;
	}

	// we only store the masks (and allocate memory for them) for 16-bit images of type FIT_BITMAP
	BOOL need_masks = FALSE;

	// check pixel bit depth
	switch(type) {
		case FIT_BITMAP:
			switch(bpp) {
				case 1:
				case 4:
				case 8:
					break;
				case 16:
					need_masks = TRUE;
                    break;
				case 24:
				case 32:
					break;
				default:
					bpp = 8;
					break;
			}
			break;
		case FIT_UINT16:
			bpp = 8 * sizeof(unsigned short);
			break;
		case FIT_INT16:
			bpp = 8 * sizeof(short);
			break;
		case FIT_UINT32:
			bpp = 8 * sizeof(DWORD);
			break;
		case FIT_INT32:
			bpp = 8 * sizeof(LONG);
			break;
		case FIT_FLOAT:
			bpp = 8 * sizeof(float);
			break;
		case FIT_DOUBLE:
			bpp = 8 * sizeof(double);
			break;
		case FIT_COMPLEX:
			bpp = 8 * sizeof(FICOMPLEX);
			break;
		case FIT_RGB16:
			bpp = 8 * sizeof(FIRGB16);
			break;
		case FIT_RGBA16:
			bpp = 8 * sizeof(FIRGBA16);
			break;
		case FIT_RGBF:
			bpp = 8 * sizeof(FIRGBF);
			break;
		case FIT_RGBAF:
			bpp = 8 * sizeof(FIRGBAF);
			break;
		default:
			return NULL;
	}

	FIBITMAP *bitmap = (FIBITMAP *)malloc(sizeof(FIBITMAP));

	if (bitmap != NULL) {

		// calculate the size of a FreeImage image
		// align the palette and the pixels on a FIBITMAP_ALIGNMENT bytes alignment boundary
		// palette is aligned on a 16 bytes boundary
		// pixels are aligned on a 16 bytes boundary

		size_t dib_size = FreeImage_GetImageSizeHeader(header_only, width, height, bpp, need_masks);

		if(dib_size == 0) {
			// memory allocation will fail (probably a malloc overflow)
			free(bitmap);
			return NULL;
		}

		bitmap->data = (BYTE *)FreeImage_Aligned_Malloc(dib_size * sizeof(BYTE), FIBITMAP_ALIGNMENT);

		if (bitmap->data != NULL) {
			memset(bitmap->data, 0, dib_size);

			// write out the FREEIMAGEHEADER

			FREEIMAGEHEADER *fih    = (FREEIMAGEHEADER *)bitmap->data;
			fih->type				= type;

			memset(&fih->bkgnd_color, 0, sizeof(RGBQUAD));

			fih->transparent        = FALSE;
			fih->transparency_count = 0;
			memset(fih->transparent_table, 0xff, 256);

			fih->has_pixels = header_only ? FALSE : TRUE;

			// initialize FIICCPROFILE link

			FIICCPROFILE *iccProfile = FreeImage_GetICCProfile(bitmap);
			iccProfile->size		= 0;
			iccProfile->data		= 0;
			iccProfile->flags		= 0;

			// initialize metadata models list

			fih->metadata = new(std::nothrow) METADATAMAP;

			// initialize attached thumbnail

			fih->thumbnail = NULL;

			// write out the BITMAPINFOHEADER

			BITMAPINFOHEADER *bih   = FreeImage_GetInfoHeader(bitmap);
			bih->biSize             = sizeof(BITMAPINFOHEADER);
			bih->biWidth            = width;
			bih->biHeight           = height;
			bih->biPlanes           = 1;
			bih->biCompression      = need_masks ? BI_BITFIELDS : BI_RGB;
			bih->biBitCount         = (WORD)bpp;
			bih->biClrUsed          = CalculateUsedPaletteEntries(bpp);
			bih->biClrImportant     = bih->biClrUsed;
			bih->biXPelsPerMeter	= 2835;	// 72 dpi
			bih->biYPelsPerMeter	= 2835;	// 72 dpi

			if(bpp == 8) {
				// build a default greyscale palette (very useful for image processing)
				RGBQUAD *pal = FreeImage_GetPalette(bitmap);
				for(int i = 0; i < 256; i++) {
					pal[i].rgbRed	= (BYTE)i;
					pal[i].rgbGreen = (BYTE)i;
					pal[i].rgbBlue	= (BYTE)i;
				}
			}

			// just setting the masks (only if needed) just like the palette.
			if (need_masks) {
				FREEIMAGERGBMASKS *masks = FreeImage_GetRGBMasks(bitmap);
				masks->red_mask = red_mask;
				masks->green_mask = green_mask;
				masks->blue_mask = blue_mask;
			}

			return bitmap;
		}

		free(bitmap);
	}

	return NULL;
}

FIBITMAP * DLL_CALLCONV
FreeImage_AllocateHeader(BOOL header_only, int width, int height, int bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask) {
	return FreeImage_AllocateHeaderT(header_only, FIT_BITMAP, width, height, bpp, red_mask, green_mask, blue_mask);
}

FIBITMAP * DLL_CALLCONV
FreeImage_Allocate(int width, int height, int bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask) {
	return FreeImage_AllocateHeaderT(FALSE, FIT_BITMAP, width, height, bpp, red_mask, green_mask, blue_mask);
}

FIBITMAP * DLL_CALLCONV
FreeImage_AllocateT(FREE_IMAGE_TYPE type, int width, int height, int bpp, unsigned red_mask, unsigned green_mask, unsigned blue_mask) {
	return FreeImage_AllocateHeaderT(FALSE, type, width, height, bpp, red_mask, green_mask, blue_mask);
}

void DLL_CALLCONV
FreeImage_Unload(FIBITMAP *dib) {
	if (NULL != dib) {	
		if (NULL != dib->data) {
			// delete possible icc profile ...
			if (FreeImage_GetICCProfile(dib)->data)
				free(FreeImage_GetICCProfile(dib)->data);

			// delete metadata models
			METADATAMAP *metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;

			for(METADATAMAP::iterator i = (*metadata).begin(); i != (*metadata).end(); i++) {
				TAGMAP *tagmap = (*i).second;

				if(tagmap) {
					for(TAGMAP::iterator j = tagmap->begin(); j != tagmap->end(); j++) {
						FITAG *tag = (*j).second;
						FreeImage_DeleteTag(tag);
					}

					delete tagmap;
				}
			}

			delete metadata;

			// delete embedded thumbnail
			FreeImage_Unload(FreeImage_GetThumbnail(dib));

			// delete bitmap ...
			FreeImage_Aligned_Free(dib->data);
		}
		free(dib);		// ... and the wrapper
	}
}

// ----------------------------------------------------------

FIBITMAP * DLL_CALLCONV
FreeImage_Clone(FIBITMAP *dib) {
	if(!dib) return NULL;

	FREE_IMAGE_TYPE type = FreeImage_GetImageType(dib);
	unsigned width       = FreeImage_GetWidth(dib);
	unsigned height      = FreeImage_GetHeight(dib);
	unsigned bpp         = FreeImage_GetBPP(dib);
	
	// check for pixel availability ...
	BOOL header_only = FreeImage_HasPixels(dib) ? FALSE : TRUE;
	// check whether this image has masks defined ...
	BOOL need_masks  = (bpp == 16 && type == FIT_BITMAP) ? TRUE : FALSE;

	// allocate a new dib
	FIBITMAP *new_dib = FreeImage_AllocateHeaderT(header_only, type, width, height, bpp,
			FreeImage_GetRedMask(dib), FreeImage_GetGreenMask(dib), FreeImage_GetBlueMask(dib));

	if (new_dib) {
		// save ICC profile links
		FIICCPROFILE *src_iccProfile = FreeImage_GetICCProfile(dib);
		FIICCPROFILE *dst_iccProfile = FreeImage_GetICCProfile(new_dib);

		// save metadata links
		METADATAMAP *src_metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;
		METADATAMAP *dst_metadata = ((FREEIMAGEHEADER *)new_dib->data)->metadata;

		// calculate the size of a FreeImage image
		// align the palette and the pixels on a FIBITMAP_ALIGNMENT bytes alignment boundary
		// palette is aligned on a 16 bytes boundary
		// pixels are aligned on a 16 bytes boundary

		size_t dib_size = FreeImage_GetImageSizeHeader(header_only, width, height, bpp, need_masks);

		// copy the bitmap + internal pointers (remember to restore new_dib internal pointers later)
		memcpy(new_dib->data, dib->data, dib_size);

		// reset ICC profile link for new_dib
		memset(dst_iccProfile, 0, sizeof(FIICCPROFILE));

		// restore metadata link for new_dib
		((FREEIMAGEHEADER *)new_dib->data)->metadata = dst_metadata;

		// reset thumbnail link for new_dib
		((FREEIMAGEHEADER *)new_dib->data)->thumbnail = NULL;

		// copy possible ICC profile
		FreeImage_CreateICCProfile(new_dib, src_iccProfile->data, src_iccProfile->size);
		dst_iccProfile->flags = src_iccProfile->flags;

		// copy metadata models
		for(METADATAMAP::iterator i = (*src_metadata).begin(); i != (*src_metadata).end(); i++) {
			int model = (*i).first;
			TAGMAP *src_tagmap = (*i).second;

			if(src_tagmap) {
				// create a metadata model
				TAGMAP *dst_tagmap = new(std::nothrow) TAGMAP();

				if(dst_tagmap) {
					// fill the model
					for(TAGMAP::iterator j = src_tagmap->begin(); j != src_tagmap->end(); j++) {
						std::string dst_key = (*j).first;
						FITAG *dst_tag = FreeImage_CloneTag( (*j).second );

						// assign key and tag value
						(*dst_tagmap)[dst_key] = dst_tag;
					}

					// assign model and tagmap
					(*dst_metadata)[model] = dst_tagmap;
				}
			}
		}

		// copy the thumbnail
		FreeImage_SetThumbnail(new_dib, FreeImage_GetThumbnail(dib));

		return new_dib;
	}

	return NULL;
}

// ----------------------------------------------------------

FIBITMAP* DLL_CALLCONV
FreeImage_GetThumbnail(FIBITMAP *dib) {
	return (dib != NULL) ? ((FREEIMAGEHEADER *)dib->data)->thumbnail : NULL;
}

BOOL DLL_CALLCONV
FreeImage_SetThumbnail(FIBITMAP *dib, FIBITMAP *thumbnail) {
	if(dib == NULL) {
		return FALSE;
	}
	FIBITMAP *currentThumbnail = ((FREEIMAGEHEADER *)dib->data)->thumbnail;
	if(currentThumbnail == thumbnail) {
		return TRUE;
	}
	FreeImage_Unload(currentThumbnail);

	((FREEIMAGEHEADER *)dib->data)->thumbnail = FreeImage_HasPixels(thumbnail) ? FreeImage_Clone(thumbnail) : NULL;

	return TRUE;
}

// ----------------------------------------------------------

FREE_IMAGE_COLOR_TYPE DLL_CALLCONV
FreeImage_GetColorType(FIBITMAP *dib) {
	RGBQUAD *rgb;

	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);

	// special bitmap type
	if(image_type != FIT_BITMAP) {
		switch(image_type) {
			case FIT_UINT16:
			{
				// 16-bit greyscale TIF can be either FIC_MINISBLACK (the most common case) or FIC_MINISWHITE
				// you can check this using EXIF_MAIN metadata
				FITAG *photometricTag = NULL;
				if(FreeImage_GetMetadata(FIMD_EXIF_MAIN, dib, "PhotometricInterpretation", &photometricTag)) {
					const short *value = (short*)FreeImage_GetTagValue(photometricTag);
					// PHOTOMETRIC_MINISWHITE = 0 => min value is white
					// PHOTOMETRIC_MINISBLACK = 1 => min value is black
					return (*value == 0) ? FIC_MINISWHITE : FIC_MINISBLACK;
				}
				return FIC_MINISBLACK;
			}
			break;
			case FIT_RGB16:
			case FIT_RGBF:
				return FIC_RGB;
			case FIT_RGBA16:
			case FIT_RGBAF:
				return FIC_RGBALPHA;
		}

		return FIC_MINISBLACK;
	}

	// standard image type
	switch (FreeImage_GetBPP(dib)) {
		case 1:
		{
			rgb = FreeImage_GetPalette(dib);

			if ((rgb->rgbRed == 0) && (rgb->rgbGreen == 0) && (rgb->rgbBlue == 0)) {
				rgb++;

				if ((rgb->rgbRed == 255) && (rgb->rgbGreen == 255) && (rgb->rgbBlue == 255))
					return FIC_MINISBLACK;				
			}

			if ((rgb->rgbRed == 255) && (rgb->rgbGreen == 255) && (rgb->rgbBlue == 255)) {
				rgb++;

				if ((rgb->rgbRed == 0) && (rgb->rgbGreen == 0) && (rgb->rgbBlue == 0))
					return FIC_MINISWHITE;				
			}

			return FIC_PALETTE;
		}

		case 4:
		case 8:	// Check if the DIB has a color or a greyscale palette
		{
			int ncolors = FreeImage_GetColorsUsed(dib);
		    int minisblack = 1;
			rgb = FreeImage_GetPalette(dib);

			for (int i = 0; i < ncolors; i++) {
				if ((rgb->rgbRed != rgb->rgbGreen) || (rgb->rgbRed != rgb->rgbBlue))
					return FIC_PALETTE;

				// The DIB has a color palette if the greyscale isn't a linear ramp
				// Take care of reversed grey images
				if (rgb->rgbRed != i) {
					if ((ncolors-i-1) != rgb->rgbRed)
						return FIC_PALETTE;
				    else
						minisblack = 0;
				}

				rgb++;
			}

			return minisblack ? FIC_MINISBLACK : FIC_MINISWHITE;
		}

		case 16:
		case 24:
			return FIC_RGB;

		case 32:
		{
			if (FreeImage_GetICCProfile(dib)->flags & FIICC_COLOR_IS_CMYK)
				return FIC_CMYK;

			if( FreeImage_HasPixels(dib) ) {
				// check for fully opaque alpha layer
				for (unsigned y = 0; y < FreeImage_GetHeight(dib); y++) {
					rgb = (RGBQUAD *)FreeImage_GetScanLine(dib, y);

					for (unsigned x = 0; x < FreeImage_GetWidth(dib); x++)
						if (rgb[x].rgbReserved != 0xFF)
							return FIC_RGBALPHA;			
				}
				return FIC_RGB;
			}

			return FIC_RGBALPHA;
		}
				
		default :
			return FIC_MINISBLACK;
	}
}

// ----------------------------------------------------------

FREE_IMAGE_TYPE DLL_CALLCONV 
FreeImage_GetImageType(FIBITMAP *dib) {
	return (dib != NULL) ? ((FREEIMAGEHEADER *)dib->data)->type : FIT_UNKNOWN;
}

// ----------------------------------------------------------

BOOL DLL_CALLCONV 
FreeImage_HasPixels(FIBITMAP *dib) {
	return (dib != NULL) ? ((FREEIMAGEHEADER *)dib->data)->has_pixels : FALSE;
}

// ----------------------------------------------------------

BOOL DLL_CALLCONV
FreeImage_HasRGBMasks(FIBITMAP *dib) {
	return dib && FreeImage_GetInfoHeader(dib)->biCompression == BI_BITFIELDS;
}

unsigned DLL_CALLCONV
FreeImage_GetRedMask(FIBITMAP *dib) {
	FREEIMAGERGBMASKS *masks = NULL;
	FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	switch(image_type) {
		case FIT_BITMAP:
			// check for 16-bit RGB (565 or 555)
			masks = FreeImage_GetRGBMasks(dib);
			return masks ? masks->red_mask : FI_RGBA_RED_MASK;
		default:
			return 0;
	}
}

unsigned DLL_CALLCONV
FreeImage_GetGreenMask(FIBITMAP *dib) {
	FREEIMAGERGBMASKS *masks = NULL;
	FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	switch(image_type) {
		case FIT_BITMAP:
			// check for 16-bit RGB (565 or 555)
			masks = FreeImage_GetRGBMasks(dib);
			return masks ? masks->green_mask : FI_RGBA_GREEN_MASK;
		default:
			return 0;
	}
}

unsigned DLL_CALLCONV
FreeImage_GetBlueMask(FIBITMAP *dib) {
	FREEIMAGERGBMASKS *masks = NULL;
	FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	switch(image_type) {
		case FIT_BITMAP:
			// check for 16-bit RGB (565 or 555)
			masks = FreeImage_GetRGBMasks(dib);
			return masks ? masks->blue_mask : FI_RGBA_BLUE_MASK;
		default:
			return 0;
	}
}

// ----------------------------------------------------------

BOOL DLL_CALLCONV
FreeImage_HasBackgroundColor(FIBITMAP *dib) {
	if(dib) {
		RGBQUAD *bkgnd_color = &((FREEIMAGEHEADER *)dib->data)->bkgnd_color;
		return (bkgnd_color->rgbReserved != 0) ? TRUE : FALSE;
	}
	return FALSE;
}

BOOL DLL_CALLCONV
FreeImage_GetBackgroundColor(FIBITMAP *dib, RGBQUAD *bkcolor) {
	if(dib && bkcolor) {
		if(FreeImage_HasBackgroundColor(dib)) {
			// get the background color
			RGBQUAD *bkgnd_color = &((FREEIMAGEHEADER *)dib->data)->bkgnd_color;
			memcpy(bkcolor, bkgnd_color, sizeof(RGBQUAD));
			// get the background index
			if(FreeImage_GetBPP(dib) == 8) {
				RGBQUAD *pal = FreeImage_GetPalette(dib);
				for(unsigned i = 0; i < FreeImage_GetColorsUsed(dib); i++) {
					if(bkgnd_color->rgbRed == pal[i].rgbRed) {
						if(bkgnd_color->rgbGreen == pal[i].rgbGreen) {
							if(bkgnd_color->rgbBlue == pal[i].rgbBlue) {
								bkcolor->rgbReserved = (BYTE)i;
								return TRUE;
							}
						}
					}
				}
			}

			bkcolor->rgbReserved = 0;

			return TRUE;
		}
	}

	return FALSE;
}

BOOL DLL_CALLCONV 
FreeImage_SetBackgroundColor(FIBITMAP *dib, RGBQUAD *bkcolor) {
	if(dib) {
		RGBQUAD *bkgnd_color = &((FREEIMAGEHEADER *)dib->data)->bkgnd_color;
		if(bkcolor) {
			// set the background color
			memcpy(bkgnd_color, bkcolor, sizeof(RGBQUAD));
			// enable the file background color
			bkgnd_color->rgbReserved = 1;
		} else {
			// clear and disable the file background color
			memset(bkgnd_color, 0, sizeof(RGBQUAD));
		}
		return TRUE;
	}

	return FALSE;
}

// ----------------------------------------------------------

BOOL DLL_CALLCONV
FreeImage_IsTransparent(FIBITMAP *dib) {
	if(dib) {
		FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
		switch(image_type) {
			case FIT_BITMAP:
				if(FreeImage_GetBPP(dib) == 32) {
					if(FreeImage_GetColorType(dib) == FIC_RGBALPHA) {
						return TRUE;
					}
				} else {
					return ((FREEIMAGEHEADER *)dib->data)->transparent ? TRUE : FALSE;
				}
				break;
			case FIT_RGBA16:
			case FIT_RGBAF:
				return TRUE;
			default:
				break;
		}
	}
	return FALSE;
}

BYTE * DLL_CALLCONV
FreeImage_GetTransparencyTable(FIBITMAP *dib) {
	return dib ? ((FREEIMAGEHEADER *)dib->data)->transparent_table : NULL;
}

void DLL_CALLCONV
FreeImage_SetTransparent(FIBITMAP *dib, BOOL enabled) {
	if (dib) {
		if ((FreeImage_GetBPP(dib) <= 8) || (FreeImage_GetBPP(dib) == 32)) {
			((FREEIMAGEHEADER *)dib->data)->transparent = enabled;
		} else {
			((FREEIMAGEHEADER *)dib->data)->transparent = FALSE;
		}
	}
}

unsigned DLL_CALLCONV
FreeImage_GetTransparencyCount(FIBITMAP *dib) {
	return dib ? ((FREEIMAGEHEADER *)dib->data)->transparency_count : 0;
}

void DLL_CALLCONV
FreeImage_SetTransparencyTable(FIBITMAP *dib, BYTE *table, int count) {
	if (dib) {
		count = MAX(0, MIN(count, 256));
		if (FreeImage_GetBPP(dib) <= 8) {
			((FREEIMAGEHEADER *)dib->data)->transparent = (count > 0) ? TRUE : FALSE;
			((FREEIMAGEHEADER *)dib->data)->transparency_count = count;

			if (table) {
				memcpy(((FREEIMAGEHEADER *)dib->data)->transparent_table, table, count);
			} else {
				memset(((FREEIMAGEHEADER *)dib->data)->transparent_table, 0xff, count);
			}
		} 
	}
}

/** @brief Sets the index of the palette entry to be used as transparent color
 for the image specified. Does nothing on high color images. 
 
 This method sets the index of the palette entry to be used as single transparent
 color for the image specified. This works on palletised images only and does
 nothing for high color images.
 
 Although it is possible for palletised images to have more than one transparent
 color, this method sets the palette entry specified as the single transparent
 color for the image. All other colors will be set to be non-transparent by this
 method.
 
 As with FreeImage_SetTransparencyTable(), this method also sets the image's
 transparency property to TRUE (as it is set and obtained by
 FreeImage_SetTransparent() and FreeImage_IsTransparent() respectively) for
 palletised images.
 
 @param dib Input image, whose transparent color is to be set.
 @param index The index of the palette entry to be set as transparent color.
 */
void DLL_CALLCONV
FreeImage_SetTransparentIndex(FIBITMAP *dib, int index) {
	if (dib) {
		int count = FreeImage_GetColorsUsed(dib);
		if (count) {
			BYTE *new_tt = (BYTE *)malloc(count * sizeof(BYTE));
			memset(new_tt, 0xFF, count);
			if ((index >= 0) && (index < count)) {
				new_tt[index] = 0x00;
			}
			FreeImage_SetTransparencyTable(dib, new_tt, count);
			free(new_tt);
		}
	}
}

/** @brief Returns the palette entry used as transparent color for the image
 specified. Works for palletised images only and returns -1 for high color
 images or if the image has no color set to be transparent. 
 
 Although it is possible for palletised images to have more than one transparent
 color, this function always returns the index of the first palette entry, set
 to be transparent. 
 
 @param dib Input image, whose transparent color is to be returned.
 @return Returns the index of the palette entry used as transparent color for
 the image specified or -1 if there is no transparent color found (e.g. the image
 is a high color image).
 */
int DLL_CALLCONV
FreeImage_GetTransparentIndex(FIBITMAP *dib) {
	int count = FreeImage_GetTransparencyCount(dib);
	BYTE *tt = FreeImage_GetTransparencyTable(dib);
	for (int i = 0; i < count; i++) {
		if (tt[i] == 0) {
			return i;
		}
	}
	return -1;
}

// ----------------------------------------------------------

FIICCPROFILE * DLL_CALLCONV
FreeImage_GetICCProfile(FIBITMAP *dib) {
	FIICCPROFILE *profile = (dib) ? (FIICCPROFILE *)&((FREEIMAGEHEADER *)dib->data)->iccProfile : NULL;
	return profile;
}

FIICCPROFILE * DLL_CALLCONV
FreeImage_CreateICCProfile(FIBITMAP *dib, void *data, long size) {
	// clear the profile but preserve profile->flags
	FreeImage_DestroyICCProfile(dib);
	// create the new profile
	FIICCPROFILE *profile = FreeImage_GetICCProfile(dib);
	if(size && profile) {
		profile->data = malloc(size);
		if(profile->data) {
			memcpy(profile->data, data, profile->size = size);
		}
	}
	return profile;
}

void DLL_CALLCONV
FreeImage_DestroyICCProfile(FIBITMAP *dib) {
	FIICCPROFILE *profile = FreeImage_GetICCProfile(dib);
	if(profile) {
		if (profile->data) {
			free (profile->data);
		}
		// clear the profile but preserve profile->flags
		profile->data = NULL;
		profile->size = 0;
	}
}

// ----------------------------------------------------------

unsigned DLL_CALLCONV
FreeImage_GetWidth(FIBITMAP *dib) {
	return dib ? FreeImage_GetInfoHeader(dib)->biWidth : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetHeight(FIBITMAP *dib) {
	return (dib) ? FreeImage_GetInfoHeader(dib)->biHeight : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetBPP(FIBITMAP *dib) {
	return dib ? FreeImage_GetInfoHeader(dib)->biBitCount : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetLine(FIBITMAP *dib) {
	return dib ? ((FreeImage_GetWidth(dib) * FreeImage_GetBPP(dib)) + 7) / 8 : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetPitch(FIBITMAP *dib) {
	return dib ? FreeImage_GetLine(dib) + 3 & ~3 : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetColorsUsed(FIBITMAP *dib) {
	return dib ? FreeImage_GetInfoHeader(dib)->biClrUsed : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetDIBSize(FIBITMAP *dib) {
	return (dib) ? sizeof(BITMAPINFOHEADER) + (FreeImage_GetColorsUsed(dib) * sizeof(RGBQUAD)) + (FreeImage_GetPitch(dib) * FreeImage_GetHeight(dib)) : 0;
}

RGBQUAD * DLL_CALLCONV
FreeImage_GetPalette(FIBITMAP *dib) {
	return (dib && FreeImage_GetBPP(dib) < 16) ? (RGBQUAD *)(((BYTE *)FreeImage_GetInfoHeader(dib)) + sizeof(BITMAPINFOHEADER)) : NULL;
}

unsigned DLL_CALLCONV
FreeImage_GetDotsPerMeterX(FIBITMAP *dib) {
	return (dib) ? FreeImage_GetInfoHeader(dib)->biXPelsPerMeter : 0;
}

unsigned DLL_CALLCONV
FreeImage_GetDotsPerMeterY(FIBITMAP *dib) {
	return (dib) ? FreeImage_GetInfoHeader(dib)->biYPelsPerMeter : 0;
}

void DLL_CALLCONV
FreeImage_SetDotsPerMeterX(FIBITMAP *dib, unsigned res) {
	if(dib) {
		FreeImage_GetInfoHeader(dib)->biXPelsPerMeter = res;
	}
}

void DLL_CALLCONV
FreeImage_SetDotsPerMeterY(FIBITMAP *dib, unsigned res) {
	if(dib) {
		FreeImage_GetInfoHeader(dib)->biYPelsPerMeter = res;
	}
}

BITMAPINFOHEADER * DLL_CALLCONV
FreeImage_GetInfoHeader(FIBITMAP *dib) {
	if(!dib) return NULL;
	size_t lp = (size_t)dib->data + sizeof(FREEIMAGEHEADER);
	lp += (lp % FIBITMAP_ALIGNMENT ? FIBITMAP_ALIGNMENT - lp % FIBITMAP_ALIGNMENT : 0);
	lp += FIBITMAP_ALIGNMENT - sizeof(BITMAPINFOHEADER) % FIBITMAP_ALIGNMENT;
	return (BITMAPINFOHEADER *)lp;
}

BITMAPINFO * DLL_CALLCONV
FreeImage_GetInfo(FIBITMAP *dib) {
	return (BITMAPINFO *)FreeImage_GetInfoHeader(dib);
}

// ----------------------------------------------------------
//  Metadata routines
// ----------------------------------------------------------

FIMETADATA * DLL_CALLCONV 
FreeImage_FindFirstMetadata(FREE_IMAGE_MDMODEL model, FIBITMAP *dib, FITAG **tag) {
	if(!dib)
		return NULL;

	// get the metadata model
	METADATAMAP *metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;
	TAGMAP *tagmap = NULL;
	if( (*metadata).find(model) != (*metadata).end() ) {
		tagmap = (*metadata)[model];
	}
	if(tagmap) {
		// allocate a handle
		FIMETADATA 	*handle = (FIMETADATA *)malloc(sizeof(FIMETADATA));
		if(handle) {
			// calculate the size of a METADATAHEADER
			int header_size = sizeof(METADATAHEADER);

			handle->data = (BYTE *)malloc(header_size * sizeof(BYTE));
			
			if(handle->data) {
				memset(handle->data, 0, header_size * sizeof(BYTE));

				// write out the METADATAHEADER
				METADATAHEADER *mdh = (METADATAHEADER *)handle->data;

				mdh->pos = 1;
				mdh->tagmap = tagmap;

				// get the first element
				TAGMAP::iterator i = tagmap->begin();
				*tag = (*i).second;

				return handle;
			}

			free(handle);
		}
	}

	return NULL;
}

BOOL DLL_CALLCONV 
FreeImage_FindNextMetadata(FIMETADATA *mdhandle, FITAG **tag) {
	if(!mdhandle)
		return FALSE;

	METADATAHEADER *mdh = (METADATAHEADER *)mdhandle->data;
	TAGMAP *tagmap = mdh->tagmap;

	int current_pos = mdh->pos;
	int mapsize     = (int)tagmap->size();

	if(current_pos < mapsize) {
		// get the tag element at position pos
		int count = 0;

		for(TAGMAP::iterator i = tagmap->begin(); i != tagmap->end(); i++) {
			if(count == current_pos) {
				*tag = (*i).second;
				mdh->pos++;
				break;
			}
			count++;
		}
		
		return TRUE;
	}

	return FALSE;
}

void DLL_CALLCONV 
FreeImage_FindCloseMetadata(FIMETADATA *mdhandle) {
	if (NULL != mdhandle) {	// delete the handle
		if (NULL != mdhandle->data) {
			free(mdhandle->data);
		}
		free(mdhandle);		// ... and the wrapper
	}
}


// ----------------------------------------------------------

BOOL DLL_CALLCONV
FreeImage_CloneMetadata(FIBITMAP *dst, FIBITMAP *src) {
	if(!src || !dst) return FALSE;

	// get metadata links
	METADATAMAP *src_metadata = ((FREEIMAGEHEADER *)src->data)->metadata;
	METADATAMAP *dst_metadata = ((FREEIMAGEHEADER *)dst->data)->metadata;

	// copy metadata models, *except* the FIMD_ANIMATION model
	for(METADATAMAP::iterator i = (*src_metadata).begin(); i != (*src_metadata).end(); i++) {
		int model = (*i).first;
		if(model == (int)FIMD_ANIMATION) {
			continue;
		}
		TAGMAP *src_tagmap = (*i).second;

		if(src_tagmap) {
			if( dst_metadata->find(model) != dst_metadata->end() ) {
				// destroy dst model
				FreeImage_SetMetadata((FREE_IMAGE_MDMODEL)model, dst, NULL, NULL);
			}

			// create a metadata model
			TAGMAP *dst_tagmap = new(std::nothrow) TAGMAP();

			if(dst_tagmap) {
				// fill the model
				for(TAGMAP::iterator j = src_tagmap->begin(); j != src_tagmap->end(); j++) {
					std::string dst_key = (*j).first;
					FITAG *dst_tag = FreeImage_CloneTag( (*j).second );

					// assign key and tag value
					(*dst_tagmap)[dst_key] = dst_tag;
				}

				// assign model and tagmap
				(*dst_metadata)[model] = dst_tagmap;
			}
		}
	}

	// clone resolution 
	FreeImage_SetDotsPerMeterX(dst, FreeImage_GetDotsPerMeterX(src)); 
	FreeImage_SetDotsPerMeterY(dst, FreeImage_GetDotsPerMeterY(src)); 

	return TRUE;
}

// ----------------------------------------------------------

BOOL DLL_CALLCONV 
FreeImage_SetMetadata(FREE_IMAGE_MDMODEL model, FIBITMAP *dib, const char *key, FITAG *tag) {
	if(!dib) 
		return FALSE;

	TAGMAP *tagmap = NULL;

	// get the metadata model
	METADATAMAP *metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;
	METADATAMAP::iterator model_iterator = metadata->find(model);
	if (model_iterator != metadata->end()) {
		tagmap = model_iterator->second;
	}

	if(key != NULL) {

		if(!tagmap) {
			// this model, doesn't exist: create it 
			tagmap = new(std::nothrow) TAGMAP();
			(*metadata)[model] = tagmap;
		}
		
		if(tag) {
			// first check the tag
			if(FreeImage_GetTagKey(tag) == NULL) {
				FreeImage_SetTagKey(tag, key);
			} else if(strcmp(key, FreeImage_GetTagKey(tag)) != 0) {
				// set the tag key
				FreeImage_SetTagKey(tag, key);
			}
			if(FreeImage_GetTagCount(tag) * FreeImage_TagDataWidth(FreeImage_GetTagType(tag)) != FreeImage_GetTagLength(tag)) {
				FreeImage_OutputMessageProc(FIF_UNKNOWN, "Invalid data count for tag '%s'", key);
				return FALSE;
			}

			// fill the tag ID if possible and if it's needed
			TagLib& tag_lib = TagLib::instance();
			switch(model) {
				case FIMD_IPTC:
				{
					int id = tag_lib.getTagID(TagLib::IPTC, key);
					/*
					if(id == -1) {
						FreeImage_OutputMessageProc(FIF_UNKNOWN, "IPTC: Invalid key '%s'", key);
					}
					*/
					FreeImage_SetTagID(tag, (WORD)id);
				}
				break;

				default:
					break;
			}

			// delete existing tag
			FITAG *old_tag = (*tagmap)[key];
			if(old_tag) {
				FreeImage_DeleteTag(old_tag);
			}

			// create a new tag
			(*tagmap)[key] = FreeImage_CloneTag(tag);
		}
		else {
			// delete existing tag
			TAGMAP::iterator i = tagmap->find(key);
			if(i != tagmap->end()) {
				FITAG *old_tag = (*i).second;
				FreeImage_DeleteTag(old_tag);
				tagmap->erase(key);
			}
		}
	}
	else {
		// destroy the metadata model
		if(tagmap) {
			for(TAGMAP::iterator i = tagmap->begin(); i != tagmap->end(); i++) {
				FITAG *tag = (*i).second;
				FreeImage_DeleteTag(tag);
			}

			delete tagmap;
			metadata->erase(model_iterator);
		}
	}

	return TRUE;
}

BOOL DLL_CALLCONV 
FreeImage_GetMetadata(FREE_IMAGE_MDMODEL model, FIBITMAP *dib, const char *key, FITAG **tag) {
	if(!dib || !key || !tag) 
		return FALSE;

	TAGMAP *tagmap = NULL;
	*tag = NULL;

	// get the metadata model
	METADATAMAP *metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;
	if(!(*metadata).empty()) {
		METADATAMAP::iterator model_iterator = metadata->find(model);
		if (model_iterator != metadata->end() ) {
			// this model exists : try to get the requested tag
			tagmap = model_iterator->second;
			TAGMAP::iterator tag_iterator = tagmap->find(key);
			if (tag_iterator != tagmap->end() ) {
				// get the requested tag
				*tag = tag_iterator->second;
			} 
		}
	}

	return (*tag != NULL) ? TRUE : FALSE;
}

// ----------------------------------------------------------

unsigned DLL_CALLCONV 
FreeImage_GetMetadataCount(FREE_IMAGE_MDMODEL model, FIBITMAP *dib) {
	if(!dib) 
		return FALSE;

	TAGMAP *tagmap = NULL;

	// get the metadata model
	METADATAMAP *metadata = ((FREEIMAGEHEADER *)dib->data)->metadata;
	if( (*metadata).find(model) != (*metadata).end() ) {
		tagmap = (*metadata)[model];
	}
	if(!tagmap) {
		// this model, doesn't exist: return
		return 0;
	}

	// get the tag count
	return (unsigned)tagmap->size();
}

// ----------------------------------------------------------



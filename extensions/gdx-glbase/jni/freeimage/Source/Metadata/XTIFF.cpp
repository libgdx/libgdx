// ==========================================================
// Metadata functions implementation
// Extended TIFF Directory GEO Tag Support
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
// - Thorsten Radde (support@IdealSoftware.com)
// - Berend Engelbrecht (softwarecave@users.sourceforge.net)
// - Mihail Naydenov (mnaydenov@users.sourceforge.net)
//
// Based on the LibTIFF xtiffio sample and on LibGeoTIFF
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

#include "../LibTIFF4/tiffiop.h"

#include "FreeImage.h"
#include "Utilities.h"
#include "FreeImageTag.h"
#include "FIRational.h"

// ----------------------------------------------------------
//   Extended TIFF Directory GEO Tag Support
// ----------------------------------------------------------

/**
  Tiff info structure.
  Entry format:
  { TAGNUMBER, ReadCount, WriteCount, DataType, FIELDNUM, OkToChange, PassDirCountOnSet, AsciiName }

  For ReadCount, WriteCount, -1 = unknown.
*/
static const TIFFFieldInfo xtiffFieldInfo[] = {
	{ TIFFTAG_GEOPIXELSCALE, -1, -1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "GeoPixelScale" },
	{ TIFFTAG_INTERGRAPH_MATRIX, -1, -1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "Intergraph TransformationMatrix" },
	{ TIFFTAG_GEOTRANSMATRIX, -1, -1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "GeoTransformationMatrix" },
	{ TIFFTAG_GEOTIEPOINTS,	-1, -1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "GeoTiePoints" },
	{ TIFFTAG_GEOKEYDIRECTORY,-1,-1, TIFF_SHORT, FIELD_CUSTOM, TRUE, TRUE, "GeoKeyDirectory" },
	{ TIFFTAG_GEODOUBLEPARAMS, -1, -1, TIFF_DOUBLE,	FIELD_CUSTOM, TRUE,	TRUE, "GeoDoubleParams" },
	{ TIFFTAG_GEOASCIIPARAMS, -1, -1, TIFF_ASCII, FIELD_CUSTOM, TRUE, FALSE, "GeoASCIIParams" },
	{ TIFFTAG_JPL_CARTO_IFD, 1, 1, TIFF_LONG, FIELD_CUSTOM, TRUE, TRUE,	"JPL Carto IFD offset" }  /** Don't use this! **/
};

static void
_XTIFFLocalDefaultDirectory(TIFF *tif) {
	int tag_size = sizeof(xtiffFieldInfo) / sizeof(xtiffFieldInfo[0]);
	// Install the extended Tag field info
	TIFFMergeFieldInfo(tif, xtiffFieldInfo, tag_size);
}

static TIFFExtendProc _ParentExtender;

/**
This is the callback procedure, and is
called by the DefaultDirectory method
every time a new TIFF directory is opened.
*/
static void
_XTIFFDefaultDirectory(TIFF *tif) {
	// set up our own defaults
	_XTIFFLocalDefaultDirectory(tif);

	/*
	Since an XTIFF client module may have overridden
	the default directory method, we call it now to
	allow it to set up the rest of its own methods.
	*/
	if (_ParentExtender)
		(*_ParentExtender)(tif);
}

/**
XTIFF Initializer -- sets up the callback procedure for the TIFF module
*/
void
XTIFFInitialize(void) {
	static int first_time = 1;

	if (! first_time)
		return; /* Been there. Done that. */
	first_time = 0;

	// Grab the inherited method and install
	_ParentExtender = TIFFSetTagExtender(_XTIFFDefaultDirectory);
}

// ----------------------------------------------------------
//   GeoTIFF tag reading / writing
// ----------------------------------------------------------

void
tiff_read_geotiff_profile(TIFF *tif, FIBITMAP *dib) {
	char defaultKey[16];

	size_t tag_size = sizeof(xtiffFieldInfo) / sizeof(xtiffFieldInfo[0]);

	TagLib& tag_lib = TagLib::instance();

	for(unsigned i = 0; i < tag_size; i++) {

		const TIFFFieldInfo *fieldInfo = &xtiffFieldInfo[i];

		if(fieldInfo->field_type == TIFF_ASCII) {
			char *params = NULL;

			if(TIFFGetField(tif, fieldInfo->field_tag, &params)) {
				// create a tag
				FITAG *tag = FreeImage_CreateTag();
				if(!tag)
					return;

				WORD tag_id = (WORD)fieldInfo->field_tag;

				FreeImage_SetTagType(tag, (FREE_IMAGE_MDTYPE)fieldInfo->field_type);
				FreeImage_SetTagID(tag, tag_id);
				FreeImage_SetTagKey(tag, tag_lib.getTagFieldName(TagLib::GEOTIFF, tag_id, defaultKey));
				FreeImage_SetTagDescription(tag, tag_lib.getTagDescription(TagLib::GEOTIFF, tag_id));
				FreeImage_SetTagLength(tag, (DWORD)strlen(params) + 1);
				FreeImage_SetTagCount(tag, FreeImage_GetTagLength(tag));
				FreeImage_SetTagValue(tag, params);
				FreeImage_SetMetadata(FIMD_GEOTIFF, dib, FreeImage_GetTagKey(tag), tag);

				// delete the tag
				FreeImage_DeleteTag(tag);
			}
		} else {
			short tag_count = 0;
			void* data = NULL;

			if(TIFFGetField(tif, fieldInfo->field_tag, &tag_count, &data)) {
				// create a tag
				FITAG *tag = FreeImage_CreateTag();
				if(!tag)
					return;

				WORD tag_id = (WORD)fieldInfo->field_tag;
				FREE_IMAGE_MDTYPE tag_type = (FREE_IMAGE_MDTYPE)fieldInfo->field_type;

				FreeImage_SetTagType(tag, tag_type);
				FreeImage_SetTagID(tag, tag_id);
				FreeImage_SetTagKey(tag, tag_lib.getTagFieldName(TagLib::GEOTIFF, tag_id, defaultKey));
				FreeImage_SetTagDescription(tag, tag_lib.getTagDescription(TagLib::GEOTIFF, tag_id));
				FreeImage_SetTagLength(tag, FreeImage_TagDataWidth(tag_type) * tag_count);
				FreeImage_SetTagCount(tag, tag_count);
				FreeImage_SetTagValue(tag, data);
				FreeImage_SetMetadata(FIMD_GEOTIFF, dib, FreeImage_GetTagKey(tag), tag);

				// delete the tag
				FreeImage_DeleteTag(tag);
			}
		}
	} // for(tag_size)
}

void
tiff_write_geotiff_profile(TIFF *tif, FIBITMAP *dib) {
	char defaultKey[16];

	if(FreeImage_GetMetadataCount(FIMD_GEOTIFF, dib) == 0) {
		return;
	}

	size_t tag_size = sizeof(xtiffFieldInfo) / sizeof(xtiffFieldInfo[0]);

	TagLib& tag_lib = TagLib::instance();

	for(unsigned i = 0; i < tag_size; i++) {
		const TIFFFieldInfo *fieldInfo = &xtiffFieldInfo[i];

		FITAG *tag = NULL;
		const char *key = tag_lib.getTagFieldName(TagLib::GEOTIFF, (WORD)fieldInfo->field_tag, defaultKey);

		if(FreeImage_GetMetadata(FIMD_GEOTIFF, dib, key, &tag)) {
			if(FreeImage_GetTagType(tag) == FIDT_ASCII) {
				TIFFSetField(tif, fieldInfo->field_tag, FreeImage_GetTagValue(tag));
			} else {
				TIFFSetField(tif, fieldInfo->field_tag, FreeImage_GetTagCount(tag), FreeImage_GetTagValue(tag));
			}
		}
	}
}

// ----------------------------------------------------------
//   EXIF tag reading & writing
// ----------------------------------------------------------

/**
Read a single exif tag
*/
static BOOL 
tiff_read_exif_tag(TIFF *tif, TagLib::MDMODEL md_model, FIBITMAP *dib, TagLib& tagLib, TIFFDirectory *td, uint32 tag) {
	const TIFFField *fip;
	uint32 value_count;
	int mem_alloc = 0;
	void *raw_data = NULL;

	if(tag == TIFFTAG_EXIFIFD) {
		return TRUE;
	}

	// get the tag key - use NULL to avoid reading GeoTIFF tags
	const char *key = tagLib.getTagFieldName(md_model, (WORD)tag, NULL);
	if(key == NULL) {
		return TRUE;
	}

	fip = TIFFFieldWithTag(tif, tag);
	if(fip == NULL) {
		return TRUE;
	}

	if(fip->field_passcount) { //<- "passcount" means "returns count"
		if (fip->field_readcount != TIFF_VARIABLE2) { //<- TIFF_VARIABLE2 means "uses LONG count"

			// assume TIFF_VARIABLE (uses SHORT count)
			uint16 value_count16;
			if(TIFFGetField(tif, tag, &value_count16, &raw_data) != 1) {
				return TRUE;
			}
			value_count = value_count16;
		} else {
			if(TIFFGetField(tif, tag, &value_count, &raw_data) != 1) {
				return TRUE;
			}
		}
	} else {

		// determine count

		if (fip->field_readcount == TIFF_VARIABLE || fip->field_readcount == TIFF_VARIABLE2) {
			value_count = 1;
		} else if (fip->field_readcount == TIFF_SPP) {
			value_count = td->td_samplesperpixel;
		} else {
			value_count = fip->field_readcount;
		}

		// access fields as pointers to data
		// (### determining this is NOT robust... and hardly can be. It is implemented looking the _TIFFVGetField code)

		if(fip->field_tag == TIFFTAG_TRANSFERFUNCTION) {
			// reading this tag cause a bug probably located somewhere inside libtiff
			return TRUE;
		}

		if ((fip->field_type == TIFF_ASCII
		     || fip->field_readcount == TIFF_VARIABLE
		     || fip->field_readcount == TIFF_VARIABLE2
		     || fip->field_readcount == TIFF_SPP
			 || value_count > 1)
			 
			 && fip->field_tag != TIFFTAG_PAGENUMBER
			 && fip->field_tag != TIFFTAG_HALFTONEHINTS
			 && fip->field_tag != TIFFTAG_YCBCRSUBSAMPLING
			 && fip->field_tag != TIFFTAG_DOTRANGE

			 && fip->field_tag != TIFFTAG_BITSPERSAMPLE	//<- these two are tricky - 
			 && fip->field_tag != TIFFTAG_COMPRESSION	//<- they are defined as TIFF_VARIABLE but in reality return a single value
			 ) {
				 if(TIFFGetField(tif, tag, &raw_data) != 1) {
					 return TRUE;
				 }
		} else {

			// access fields as values

			const int value_size = _TIFFDataSize(fip->field_type);
			raw_data = _TIFFmalloc(value_size * value_count);
			mem_alloc = 1;
			int ok = FALSE;
			
			// ### if value_count > 1, tag is PAGENUMBER or HALFTONEHINTS or YCBCRSUBSAMPLING or DOTRANGE, 
			// all off which are value_count == 2 (see tif_dirinfo.c)
			switch(value_count)
			{
				case 1:
					ok = TIFFGetField(tif, tag, raw_data);
					break;
				case 2:
					ok = TIFFGetField(tif, tag, raw_data, (BYTE*)(raw_data) + value_size*1);
					break;
/* # we might need more in the future:
				case 3:
					ok = TIFFGetField(tif, tag, raw_data, (BYTE*)(raw_data) + value_size*1, (BYTE*)(raw_data) + value_size*2);
					break;
*/
				default:
					FreeImage_OutputMessageProc(FIF_TIFF, "Unimplemented variable number of parameters for Tiff Tag %s", fip->field_name);
					break;
			}
			if(ok != 1) {
				_TIFFfree(raw_data);
				return TRUE;
			}
		}
	}

	// build FreeImage tag from Tiff Tag data we collected

	FITAG *fitag = FreeImage_CreateTag();
	if(!fitag) {
		if(mem_alloc) {
			_TIFFfree(raw_data);
		}
		return FALSE;
	}

	FreeImage_SetTagID(fitag, (WORD)tag);
	FreeImage_SetTagKey(fitag, key);

	switch(fip->field_type) {
		case TIFF_BYTE:
			FreeImage_SetTagType(fitag, FIDT_BYTE);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_UNDEFINED:
			FreeImage_SetTagType(fitag, FIDT_UNDEFINED);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_SBYTE:
			FreeImage_SetTagType(fitag, FIDT_SBYTE);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_SHORT:
			FreeImage_SetTagType(fitag, FIDT_SHORT);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_SSHORT:
			FreeImage_SetTagType(fitag, FIDT_SSHORT);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_LONG:
			FreeImage_SetTagType(fitag, FIDT_LONG);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_IFD:
			FreeImage_SetTagType(fitag, FIDT_IFD);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_SLONG:
			FreeImage_SetTagType(fitag, FIDT_SLONG);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_RATIONAL: {
			// LibTIFF converts rational to floats : reconvert floats to rationals
			DWORD *rvalue = (DWORD*)malloc(2 * value_count * sizeof(DWORD));
			for(uint32 i = 0; i < value_count; i++) {
				float *fv = (float*)raw_data;
				FIRational rational(fv[i]);
				rvalue[2*i] = rational.getNumerator();
				rvalue[2*i+1] = rational.getDenominator();
			}
			FreeImage_SetTagType(fitag, FIDT_RATIONAL);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, rvalue);
			free(rvalue);
		}
		break;

		case TIFF_SRATIONAL: {
			// LibTIFF converts rational to floats : reconvert floats to rationals
			LONG *rvalue = (LONG*)malloc(2 * value_count * sizeof(LONG));
			for(uint32 i = 0; i < value_count; i++) {
				float *fv = (float*)raw_data;
				FIRational rational(fv[i]);
				rvalue[2*i] = rational.getNumerator();
				rvalue[2*i+1] = rational.getDenominator();
			}
			FreeImage_SetTagType(fitag, FIDT_RATIONAL);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, rvalue);
			free(rvalue);
		}
		break;

		case TIFF_FLOAT:
			FreeImage_SetTagType(fitag, FIDT_FLOAT);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_DOUBLE:
			FreeImage_SetTagType(fitag, FIDT_DOUBLE);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_LONG8:	// BigTIFF 64-bit unsigned integer 
			FreeImage_SetTagType(fitag, FIDT_LONG8);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_IFD8:		// BigTIFF 64-bit unsigned integer (offset) 
			FreeImage_SetTagType(fitag, FIDT_IFD8);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_SLONG8:		// BigTIFF 64-bit signed integer 
			FreeImage_SetTagType(fitag, FIDT_SLONG8);
			FreeImage_SetTagLength(fitag, TIFFDataWidth(fip->field_type) * value_count);
			FreeImage_SetTagCount(fitag, value_count);
			FreeImage_SetTagValue(fitag, raw_data);
			break;

		case TIFF_ASCII:
		default: {
			size_t length = 0;
			if(!mem_alloc && (fip->field_type == TIFF_ASCII) && (fip->field_readcount == TIFF_VARIABLE)) {
				// when metadata tag is of type ASCII and it's value is of variable size (TIFF_VARIABLE),
				// tiff_read_exif_tag function gives length of 1 so all strings are truncated ...
				// ... try to avoid this by using an explicit calculation for 'length'
				length = strlen((char*)raw_data) + 1;
			}
			else {
				// remember that raw_data = _TIFFmalloc(value_size * value_count);
				const int value_size = _TIFFDataSize(fip->field_type);
				length = value_size * value_count;
			}
			FreeImage_SetTagType(fitag, FIDT_ASCII);
			FreeImage_SetTagLength(fitag, (DWORD)length);
			FreeImage_SetTagCount(fitag, (DWORD)length);
			FreeImage_SetTagValue(fitag, raw_data);
		}
		break;
	}

	const char *description = tagLib.getTagDescription(md_model, (WORD)tag);
	if(description) {
		FreeImage_SetTagDescription(fitag, description);
	}
	// store the tag
	FreeImage_SetMetadata(tagLib.getFreeImageModel(md_model), dib, FreeImage_GetTagKey(fitag), fitag);

	// destroy the tag
	FreeImage_DeleteTag(fitag);

	if(mem_alloc) {
		_TIFFfree(raw_data);
	}
	return TRUE;
}

/**
Read all known exif tags
*/
BOOL 
tiff_read_exif_tags(TIFF *tif, TagLib::MDMODEL md_model, FIBITMAP *dib) {
	int  i;
	short count;

	TagLib& tagLib = TagLib::instance();

	TIFFDirectory *td = &tif->tif_dir;

	count = (short) TIFFGetTagListCount(tif);
	for(i = 0; i < count; i++) {
		uint32 tag = TIFFGetTagListEntry(tif, i);
		// read the tag
		if (!tiff_read_exif_tag(tif, md_model, dib, tagLib, td, tag))
			return FALSE;
	}

	// we want to know values of standard tags too!!

	// loop over all Core Directory Tags
	// ### uses private data, but there is no other way
	if(md_model == TagLib::EXIF_MAIN) {

		uint32 lastTag = 0;	//<- used to prevent reading some tags twice (as stored in tif_fieldinfo)

		for (int fi = 0, nfi = (int)tif->tif_nfields; nfi > 0; nfi--, fi++) {
			const TIFFField *fld = tif->tif_fields[fi];

			if(fld->field_tag == lastTag)
				continue;

			// test if tag value is set
			// (lifted directly form LibTiff _TIFFWriteDirectory)

			if( fld->field_bit == FIELD_CUSTOM ) {
				int ci, is_set = FALSE;

				for( ci = 0; ci < td->td_customValueCount; ci++ ) {
					is_set |= (td->td_customValues[ci].info == fld);
				}

				if( !is_set ) {
					continue;
				}

			} else if(!TIFFFieldSet(tif, fld->field_bit)) {
				continue;
			}

			// process *all* other tags (some will be ignored)

			tiff_read_exif_tag(tif, md_model, dib, tagLib, td, fld->field_tag);


			lastTag = fld->field_tag;
		}

	}

	return TRUE;

}


/**
Skip tags that are already handled by the LibTIFF writing process
*/
static BOOL 
skip_write_field(TIFF* tif, uint32 tag) {
	switch (tag) {
		case TIFFTAG_SAMPLEFORMAT:
		case TIFFTAG_IMAGEWIDTH:
		case TIFFTAG_IMAGELENGTH:
		case TIFFTAG_SAMPLESPERPIXEL:
		case TIFFTAG_BITSPERSAMPLE:
		case TIFFTAG_PHOTOMETRIC:
		case TIFFTAG_PLANARCONFIG:
		case TIFFTAG_ROWSPERSTRIP:
		case TIFFTAG_STRIPBYTECOUNTS:
		case TIFFTAG_STRIPOFFSETS:
		case TIFFTAG_RESOLUTIONUNIT:
		case TIFFTAG_XRESOLUTION:
		case TIFFTAG_YRESOLUTION:
		case TIFFTAG_SUBFILETYPE:
		case TIFFTAG_PAGENUMBER:
		case TIFFTAG_COLORMAP:
		case TIFFTAG_ORIENTATION:
		case TIFFTAG_COMPRESSION:
		case TIFFTAG_PREDICTOR:
		case TIFFTAG_GROUP3OPTIONS:
		case TIFFTAG_FILLORDER:
			// skip always, values have been set in SaveOneTIFF()
			return TRUE;
			break;
		
		case TIFFTAG_RICHTIFFIPTC:
			// skip always, IPTC metadata model is set in tiff_write_iptc_profile()
			return TRUE;
			break;

		case TIFFTAG_YCBCRCOEFFICIENTS:
		case TIFFTAG_REFERENCEBLACKWHITE:
		case TIFFTAG_YCBCRSUBSAMPLING:
			// skip as they cannot be filled yet
			return TRUE;
			break;
			
		case TIFFTAG_PAGENAME:
		{
			char *value = NULL;
			TIFFGetField(tif, TIFFTAG_PAGENAME, &value);
			// only skip if no value has been set
			if(value == NULL) {
				return FALSE;
			} else {
				return TRUE;
			}
		}
		default:
			return FALSE;
			break;
	}
}

/**
Write all known exif tags
*/
BOOL 
tiff_write_exif_tags(TIFF *tif, TagLib::MDMODEL md_model, FIBITMAP *dib) {
	char defaultKey[16];
	
	// only EXIF_MAIN so far
	if(md_model != TagLib::EXIF_MAIN) {
		return FALSE;
	}
	
	if(FreeImage_GetMetadataCount(FIMD_EXIF_MAIN, dib) == 0) {
		return FALSE;
	}
	
	TagLib& tag_lib = TagLib::instance();
	
	for (int fi = 0, nfi = (int)tif->tif_nfields; nfi > 0; nfi--, fi++) {
		const TIFFField *fld = tif->tif_fields[fi];

		if(skip_write_field(tif, fld->field_tag)) {
			// skip tags that are already handled by the LibTIFF writing process
			continue;
		}

		FITAG *tag = NULL;
		// get the tag key
		const char *key = tag_lib.getTagFieldName(TagLib::EXIF_MAIN, (WORD)fld->field_tag, defaultKey);

		if(FreeImage_GetMetadata(FIMD_EXIF_MAIN, dib, key, &tag)) {
			FREE_IMAGE_MDTYPE tag_type = FreeImage_GetTagType(tag);
			TIFFDataType tif_tag_type = fld->field_type;
			
			// check for identical formats

			// (enum value are the sames between FREE_IMAGE_MDTYPE and TIFFDataType types)
			if((int)tif_tag_type != (int)tag_type) {
				// skip tag or _TIFFmemcpy will fail
				continue;
			}
			// type of storage may differ (e.g. rationnal array vs float array type)
			if(_TIFFDataSize(tif_tag_type) != FreeImage_TagDataWidth(tag_type)) {
				// skip tag or _TIFFmemcpy will fail
				continue;
			}

			if(tag_type == FIDT_ASCII) {
				TIFFSetField(tif, fld->field_tag, FreeImage_GetTagValue(tag));
			} else {
				TIFFSetField(tif, fld->field_tag, FreeImage_GetTagCount(tag), FreeImage_GetTagValue(tag));
			}
		}
	}

	return TRUE;
}

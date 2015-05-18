// ==========================================================
// Metadata functions implementation
// Exif metadata model
//
// Design and implementation by
// - Hervé Drolon (drolon@infonie.fr)
// - Mihail Naydenov (mnaydenov@users.sourceforge.net)
//
// Based on the following implementations:
// - metadata-extractor : http://www.drewnoakes.com/code/exif/
// - jhead : http://www.sentex.net/~mwandel/jhead/
// - ImageMagick : http://www.imagemagick.org/
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
#include "FreeImageTag.h"

// ==========================================================
// Exif JPEG routines
// ==========================================================

#define EXIF_NUM_FORMATS  12

#define TAG_EXIF_OFFSET			0x8769	// Exif IFD Pointer
#define TAG_GPS_OFFSET			0x8825	// GPS Info IFD Pointer
#define TAG_INTEROP_OFFSET		0xA005	// Interoperability IFD Pointer
#define TAG_MAKER_NOTE			0x927C	// Maker note

// CANON cameras have some funny bespoke fields that need further processing...
#define TAG_CANON_CAMERA_STATE_0x01	0x0001		// tags under tag 0x001 (CameraSettings)
#define TAG_CANON_CAMERA_STATE_0x02	0x0002		// tags under tag 0x002 (FocalLength)
#define TAG_CANON_CAMERA_STATE_0x04	0x0004		// tags under tag 0x004 (ShotInfo)
#define TAG_CANON_CAMERA_STATE_0x12	0x0012		// tags under tag 0x012 (AFInfo)
#define TAG_CANON_CAMERA_STATE_0xA0	0x00A0		// tags under tag 0x0A0 (ProcessingInfo)
#define TAG_CANON_CAMERA_STATE_0xE0	0x00E0		// tags under tag 0x0E0 (SensorInfo)


// =====================================================================
// Reimplementation of strnicmp (it is not supported on some systems)
// =====================================================================

/**
Compare characters of two strings without regard to case.
@param s1 Null-terminated string to compare.
@param s2 Null-terminated string to compare.
@param len Number of characters to compare
@return Returns 0 if s1 substring identical to s2 substring
*/
static int 
FreeImage_strnicmp(const char *s1, const char *s2, size_t len) {
	unsigned char c1, c2;

	if(!s1 || !s2) return -1;

	c1 = 0;	c2 = 0;
	if(len) {
		do {
			c1 = *s1; c2 = *s2;
			s1++; s2++;
			if (!c1)
				break;
			if (!c2)
				break;
			if (c1 == c2)
				continue;
			c1 = (BYTE)tolower(c1);
			c2 = (BYTE)tolower(c2);
			if (c1 != c2)
				break;
		} while (--len);
	}
	return (int)c1 - (int)c2;
}


// ----------------------------------------------------------
//   Little Endian / Big Endian io routines
// ----------------------------------------------------------

static short 
ReadInt16(BOOL msb_order, const void *buffer) {
	short value;

	if(msb_order) {
		value = (short)((((BYTE*) buffer)[0] << 8) | ((BYTE*) buffer)[1]);
		return value;
    }
	value = (short)((((BYTE*) buffer)[1] << 8) | ((BYTE*) buffer)[0]);
	return value;
}

static LONG 
ReadInt32(BOOL msb_order, const void *buffer) {
	LONG value;

	if(msb_order) {
		value = (LONG)((((BYTE*) buffer)[0] << 24) | (((BYTE*) buffer)[1] << 16) | (((BYTE*) buffer)[2] << 8) | (((BYTE*) buffer)[3]));
		return value;
    }
	value = (LONG)((((BYTE*) buffer)[3] << 24) | (((BYTE*) buffer)[2] << 16) | (((BYTE*) buffer)[1] << 8 ) | (((BYTE*) buffer)[0]));
	return value;
}

static unsigned short 
ReadUint16(BOOL msb_order, const void *buffer) {
	unsigned short value;
	
	if(msb_order) {
		value = (unsigned short) ((((BYTE*) buffer)[0] << 8) | ((BYTE*) buffer)[1]);
		return value;
    }
	value = (unsigned short) ((((BYTE*) buffer)[1] << 8) | ((BYTE*) buffer)[0]);
	return value;
}

static DWORD 
ReadUint32(BOOL msb_order, const void *buffer) {
	return ((DWORD) ReadInt32(msb_order, buffer) & 0xFFFFFFFF);
}

// ----------------------------------------------------------
//   Exif JPEG markers routines
// ----------------------------------------------------------

/**
Process a IFD offset
Returns the offset and the metadata model for this tag
*/
static void 
processIFDOffset(FITAG *tag, char *pval, BOOL msb_order, DWORD *subdirOffset, TagLib::MDMODEL *md_model) {
	// get the IFD offset
	*subdirOffset = (DWORD) ReadUint32(msb_order, pval);

	// select a tag info table
	switch(FreeImage_GetTagID(tag)) {
		case TAG_EXIF_OFFSET:
			*md_model = TagLib::EXIF_EXIF;
			break;
		case TAG_GPS_OFFSET:
			*md_model = TagLib::EXIF_GPS;
			break;
		case TAG_INTEROP_OFFSET:
			*md_model = TagLib::EXIF_INTEROP;
			break;
	}

}

/**
Process a maker note IFD offset
Returns the offset and the metadata model for this tag
*/
static void 
processMakerNote(FIBITMAP *dib, char *pval, BOOL msb_order, DWORD *subdirOffset, TagLib::MDMODEL *md_model) {
	FITAG *tagMake = NULL;

	*subdirOffset = 0;
	*md_model = TagLib::UNKNOWN;

	// Determine the camera model and makernote format
	// WARNING: note that Maker may be NULL sometimes so check its value before using it
	// (NULL pointer checking is done by FreeImage_strnicmp)
	FreeImage_GetMetadata(FIMD_EXIF_MAIN, dib, "Make", &tagMake);
	const char *Maker = (char*)FreeImage_GetTagValue(tagMake);

	if((memcmp("OLYMP\x00\x01", pval, 7) == 0) || (memcmp("OLYMP\x00\x02", pval, 7) == 0) || (memcmp("EPSON", pval, 5) == 0) || (memcmp("AGFA", pval, 4) == 0)) {
		// Olympus Type 1 Makernote
		// Epson and Agfa use Olympus maker note standard, 
		// see: http://www.ozhiker.com/electronics/pjmt/jpeg_info/
		*md_model = TagLib::EXIF_MAKERNOTE_OLYMPUSTYPE1;
		*subdirOffset = 8;
	} 
	else if(memcmp("OLYMPUS\x00\x49\x49\x03\x00", pval, 12) == 0) {
		// Olympus Type 2 Makernote
		// !!! NOT YET SUPPORTED !!!
		*subdirOffset = 0;
		*md_model = TagLib::UNKNOWN;
	}
	else if(memcmp("Nikon", pval, 5) == 0) {
		/* There are two scenarios here:
		 * Type 1:
		 * :0000: 4E 69 6B 6F 6E 00 01 00-05 00 02 00 02 00 06 00 Nikon...........
		 * :0010: 00 00 EC 02 00 00 03 00-03 00 01 00 00 00 06 00 ................
		 * Type 3:
		 * :0000: 4E 69 6B 6F 6E 00 02 00-00 00 4D 4D 00 2A 00 00 Nikon....MM.*...
		 * :0010: 00 08 00 1E 00 01 00 07-00 00 00 04 30 32 30 30 ............0200
		 */
		if (pval[6] == 1) {
			// Nikon type 1 Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_NIKONTYPE1;
			*subdirOffset = 8;
        } else if (pval[6] == 2) {
            // Nikon type 3 Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_NIKONTYPE3;
			*subdirOffset = 18;
        } else {
			// Unsupported makernote data ignored
			*subdirOffset = 0;
			*md_model = TagLib::UNKNOWN;
		}
	} else if(Maker && (FreeImage_strnicmp("NIKON", Maker, 5) == 0)) {
		// Nikon type 2 Makernote
		*md_model = TagLib::EXIF_MAKERNOTE_NIKONTYPE2;
		*subdirOffset = 0;
    } else if(Maker && (FreeImage_strnicmp("Canon", Maker, 5) == 0)) {
        // Canon Makernote
		*md_model = TagLib::EXIF_MAKERNOTE_CANON;
		*subdirOffset = 0;		
    } else if(Maker && (FreeImage_strnicmp("Casio", Maker, 5) == 0)) {
        // Casio Makernote
		if(memcmp("QVC\x00\x00\x00", pval, 6) == 0) {
			// Casio Type 2 Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_CASIOTYPE2;
			*subdirOffset = 6;
		} else {
			// Casio Type 1 Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_CASIOTYPE1;
			*subdirOffset = 0;
		}
	} else if ((memcmp("FUJIFILM", pval, 8) == 0) || (Maker && (FreeImage_strnicmp("Fujifilm", Maker, 8) == 0))) {
        // Fujifile Makernote
		// Fujifilm's Makernote always use Intel order altough the Exif section maybe in Intel order or in Motorola order. 
		// If msb_order == TRUE, the Makernote won't be read: 
		// the value of ifdStart will be 0x0c000000 instead of 0x0000000c and the MakerNote section will be discarded later
		// in jpeg_read_exif_dir because the IFD is too high
		*md_model = TagLib::EXIF_MAKERNOTE_FUJIFILM;
        DWORD ifdStart = (DWORD) ReadUint32(msb_order, pval + 8);
		*subdirOffset = ifdStart;
    }
	else if(memcmp("KYOCERA\x20\x20\x20\x20\x20\x20\x20\x20\x20\x20\x20\x20\x00\x00\x00", pval, 22) == 0) {
		*md_model = TagLib::EXIF_MAKERNOTE_KYOCERA;
		*subdirOffset = 22;
	}
	else if(Maker && (FreeImage_strnicmp("Minolta", Maker, 7) == 0)) {
		// Minolta maker note
		*md_model = TagLib::EXIF_MAKERNOTE_MINOLTA;
		*subdirOffset = 0;
	}
	else if(memcmp("Panasonic\x00\x00\x00", pval, 12) == 0) {
		// Panasonic maker note
		*md_model = TagLib::EXIF_MAKERNOTE_PANASONIC;
		*subdirOffset = 12;
	}
	else if(Maker && (FreeImage_strnicmp("LEICA", Maker, 5) == 0)) {
		// Leica maker note
		if(memcmp("LEICA\x00\x00\x00", pval, 8) == 0) {
			// not yet supported makernote data ignored
			*subdirOffset = 0;
			*md_model = TagLib::UNKNOWN;
		}
	}
	else if(Maker && ((FreeImage_strnicmp("Pentax", Maker, 6) == 0) || (FreeImage_strnicmp("Asahi", Maker, 5) == 0))) {
		// Pentax maker note
		if(memcmp("AOC\x00", pval, 4) == 0) {
			// Type 2 Pentax Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_PENTAX;
			*subdirOffset = 6;
		} else {
			// Type 1 Pentax Makernote
			*md_model = TagLib::EXIF_MAKERNOTE_ASAHI;
			*subdirOffset = 0;
		}
	}	
	else if((memcmp("SONY CAM\x20\x00\x00\x00", pval, 12) == 0) || (memcmp("SONY DSC\x20\x00\x00\x00", pval, 12) == 0)) {
		*md_model = TagLib::EXIF_MAKERNOTE_SONY;
		*subdirOffset = 12;
	}
	else if((memcmp("SIGMA\x00\x00\x00", pval, 8) == 0) || (memcmp("FOVEON\x00\x00", pval, 8) == 0)) {
		FITAG *tagModel = NULL;
		FreeImage_GetMetadata(FIMD_EXIF_MAIN, dib, "Model", &tagModel);
		const char *Model = (char*)FreeImage_GetTagValue(tagModel);
		if(Model && (memcmp("SIGMA SD1\x00", Model, 10) == 0)) {
			// Sigma SD1 maker note
			*subdirOffset = 10;
			*md_model = TagLib::EXIF_MAKERNOTE_SIGMA_SD1;
		} else {
			// Sigma / Foveon makernote
			*subdirOffset = 10;
			*md_model = TagLib::EXIF_MAKERNOTE_SIGMA_FOVEON;
		}
	}
}

/**
Process a Canon maker note tag. 
A single Canon tag may contain many other tags within.
*/
static BOOL 
processCanonMakerNoteTag(FIBITMAP *dib, FITAG *tag) {
	char defaultKey[16];
	DWORD startIndex = 0;
	TagLib& s = TagLib::instance();

	WORD tag_id = FreeImage_GetTagID(tag);

	int subTagTypeBase = 0;

	switch(tag_id) {
		case TAG_CANON_CAMERA_STATE_0x01:
			subTagTypeBase = 0xC100;
			startIndex = 1;
			break;
		case TAG_CANON_CAMERA_STATE_0x02:
			subTagTypeBase = 0xC200;
			startIndex = 0;
			break;
		case TAG_CANON_CAMERA_STATE_0x04:
			subTagTypeBase = 0xC400;
			startIndex = 1;
			break;
		case TAG_CANON_CAMERA_STATE_0x12:
			subTagTypeBase = 0x1200;
			startIndex = 0;
			break;
		case TAG_CANON_CAMERA_STATE_0xA0:
			subTagTypeBase = 0xCA00;
			startIndex = 1;
			break;
		case TAG_CANON_CAMERA_STATE_0xE0:
			subTagTypeBase = 0xCE00;
			startIndex = 1;
			break;

		default:
		{
			// process as a normal tag

			// get the tag key and description
			const char *key = s.getTagFieldName(TagLib::EXIF_MAKERNOTE_CANON, tag_id, defaultKey);
			FreeImage_SetTagKey(tag, key);
			const char *description = s.getTagDescription(TagLib::EXIF_MAKERNOTE_CANON, tag_id);
			FreeImage_SetTagDescription(tag, description);

			// store the tag
			if(key) {
				FreeImage_SetMetadata(FIMD_EXIF_MAKERNOTE, dib, key, tag);
			}

			return TRUE;
		}
		break;

	}

	WORD *pvalue = (WORD*)FreeImage_GetTagValue(tag);

	// create a tag
	FITAG *canonTag = FreeImage_CreateTag();
	if(!canonTag) return FALSE;

	// we intentionally skip the first array member (if needed)
    for (DWORD i = startIndex; i < FreeImage_GetTagCount(tag); i++) {

		tag_id = (WORD)(subTagTypeBase + i);

		FreeImage_SetTagID(canonTag, tag_id);
		FreeImage_SetTagType(canonTag, FIDT_SHORT);
		FreeImage_SetTagCount(canonTag, 1);
		FreeImage_SetTagLength(canonTag, 2);
		FreeImage_SetTagValue(canonTag, &pvalue[i]);

		// get the tag key and description
		const char *key = s.getTagFieldName(TagLib::EXIF_MAKERNOTE_CANON, tag_id, defaultKey);
		FreeImage_SetTagKey(canonTag, key);
		const char *description = s.getTagDescription(TagLib::EXIF_MAKERNOTE_CANON, tag_id);
		FreeImage_SetTagDescription(canonTag, description);

		// store the tag
		if(key) {
			FreeImage_SetMetadata(FIMD_EXIF_MAKERNOTE, dib, key, canonTag);
		}
	}

	// delete the tag
	FreeImage_DeleteTag(canonTag);

	return TRUE;
}

/**
Process a standard Exif tag
*/
static void 
processExifTag(FIBITMAP *dib, FITAG *tag, char *pval, BOOL msb_order, TagLib::MDMODEL md_model) {
	char defaultKey[16];
	int n;
	DWORD i;

	// allocate a buffer to store the tag value
	BYTE *exif_value = (BYTE*)malloc(FreeImage_GetTagLength(tag) * sizeof(BYTE));
	if(NULL == exif_value) {
		// out of memory ...
		return;
	}
	memset(exif_value, 0, FreeImage_GetTagLength(tag) * sizeof(BYTE));

	// get the tag value
	switch(FreeImage_GetTagType(tag)) {

		case FIDT_SHORT:
		{
			WORD *value = (WORD*)&exif_value[0];
			for(i = 0; i < FreeImage_GetTagCount(tag); i++) {
				value[i] = ReadUint16(msb_order, pval + i * sizeof(WORD));
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_SSHORT:
		{
			short *value = (short*)&exif_value[0];
			for(i = 0; i < FreeImage_GetTagCount(tag); i++) {
				value[i] = ReadInt16(msb_order, pval + i * sizeof(short));
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_LONG:
		{
			DWORD *value = (DWORD*)&exif_value[0];
			for(i = 0; i < FreeImage_GetTagCount(tag); i++) {
				value[i] = ReadUint32(msb_order, pval + i * sizeof(DWORD));
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_SLONG:
		{
			LONG *value = (LONG*)&exif_value[0];
			for(i = 0; i < FreeImage_GetTagCount(tag); i++) {
				value[i] = ReadInt32(msb_order, pval + i * sizeof(LONG));
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_RATIONAL:
		{
			n = sizeof(DWORD);

			DWORD *value = (DWORD*)&exif_value[0];						
			for(i = 0; i < 2 * FreeImage_GetTagCount(tag); i++) {
				// read a sequence of (numerator, denominator)
				value[i] = ReadUint32(msb_order, n*i + (char*)pval);
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_SRATIONAL:
		{
			n = sizeof(LONG);

			LONG *value = (LONG*)&exif_value[0];
			for(i = 0; i < 2 * FreeImage_GetTagCount(tag); i++) {
				// read a sequence of (numerator, denominator)
				value[i] = ReadInt32(msb_order, n*i + (char*)pval);
			}
			FreeImage_SetTagValue(tag, value);
			break;
		}
		case FIDT_BYTE:
		case FIDT_ASCII:
		case FIDT_SBYTE:
		case FIDT_UNDEFINED:
		case FIDT_FLOAT:
		case FIDT_DOUBLE:
		default:
			FreeImage_SetTagValue(tag, pval);
			break;
	}

	if(md_model == TagLib::EXIF_MAKERNOTE_CANON) {
		// A single Canon tag can have multiple values within
		processCanonMakerNoteTag(dib, tag);
	}
	else {
		TagLib& s = TagLib::instance();

		WORD tag_id = FreeImage_GetTagID(tag);

		// get the tag key and description
		const char *key = s.getTagFieldName(md_model, tag_id, defaultKey);
		FreeImage_SetTagKey(tag, key);
		const char *description = s.getTagDescription(md_model, tag_id);
		FreeImage_SetTagDescription(tag, description);

		// store the tag
		if(key) {
			FreeImage_SetMetadata(s.getFreeImageModel(md_model), dib, key, tag);
		}
	}
	

	// free the temporary buffer
	free(exif_value);

}

/**
Process Exif directory

@param dib Input FIBITMAP
@param tiffp Pointer to the TIFF header
@param offset 0th IFD offset
@param length Length of the datafile
@param msb_order Endianess order of the datafile
@param starting_md_model Metadata model of the IFD (should be TagLib::EXIF_MAIN for a jpeg)
@return Returns TRUE if sucessful, returns FALSE otherwise
*/
static BOOL 
jpeg_read_exif_dir(FIBITMAP *dib, const BYTE *tiffp, unsigned long offset, unsigned int length, BOOL msb_order, TagLib::MDMODEL starting_md_model) {
	WORD de, nde;

	std::stack<WORD>			destack;	// directory entries stack
	std::stack<const BYTE*>		ifdstack;	// IFD stack
	std::stack<TagLib::MDMODEL>	modelstack; // metadata model stack

	// Keep a list of already visited IFD to avoid stack overflows 
	// when recursive/cyclic directory structures exist. 
	// This kind of recursive Exif file was encountered with Kodak images coming from 
	// KODAK PROFESSIONAL DCS Photo Desk JPEG Export v3.2 W
	std::map<DWORD, int> visitedIFD;

	/*
	"An Image File Directory (IFD) consists of a 2-byte count of the number of directory
	entries (i.e. the number of fields), followed by a sequence of 12-byte field
	entries, followed by a 4-byte offset of the next IFD (or 0 if none)."
	The "next IFD" (1st IFD) is the thumbnail.
	*/
	#define DIR_ENTRY_ADDR(_start, _entry) (_start + 2 + (12 * _entry))

	// set the metadata model to Exif

	TagLib::MDMODEL md_model = starting_md_model;

	// set the pointer to the first IFD (0th IFD) and follow it were it leads.

	const BYTE *ifd0th = (BYTE*)tiffp + offset;

	const BYTE *ifdp = ifd0th;

	de = 0;

	do {
		// if there is anything on the stack then pop it off
		if(!destack.empty()) {
			ifdp		= ifdstack.top();	ifdstack.pop();
			de			= destack.top();	destack.pop();
			md_model	= modelstack.top();	modelstack.pop();
		}

		// remember that we've visited this directory and entry so that we don't visit it again later
		DWORD visited = (DWORD)( (((size_t)ifdp & 0xFFFF) << 16) | (size_t)de );
		if(visitedIFD.find(visited) != visitedIFD.end()) {
			continue;
		} else {
			visitedIFD[visited] = 1;	// processed
		}

		// determine how many entries there are in the current IFD
		nde = ReadUint16(msb_order, ifdp);

		for(; de < nde; de++) {
			char *pde = NULL;	// pointer to the directory entry
			char *pval = NULL;	// pointer to the tag value
			
			// create a tag
			FITAG *tag = FreeImage_CreateTag();
			if(!tag) return FALSE;

			// point to the directory entry
			pde = (char*) DIR_ENTRY_ADDR(ifdp, de);

			// get the tag ID
			FreeImage_SetTagID(tag, ReadUint16(msb_order, pde));
			// get the tag type
			WORD tag_type = (WORD)ReadUint16(msb_order, pde + 2);
            if((tag_type - 1) >= EXIF_NUM_FORMATS) {
                // a problem occured : delete the tag (not free'd after)
			    FreeImage_DeleteTag(tag);
				// break out of the for loop
				break;
            }
			FreeImage_SetTagType(tag, (FREE_IMAGE_MDTYPE)tag_type);

			// get number of components
			FreeImage_SetTagCount(tag, ReadUint32(msb_order, pde + 4));
            // check that tag length (size of the tag value in bytes) will fit in a DWORD
            unsigned tag_data_width = FreeImage_TagDataWidth(FreeImage_GetTagType(tag));
            if (tag_data_width != 0 && FreeImage_GetTagCount(tag) > ~(DWORD)0 / tag_data_width) {
                FreeImage_DeleteTag(tag);
                // jump to next entry
                continue;
            }
			FreeImage_SetTagLength(tag, FreeImage_GetTagCount(tag) * tag_data_width);

			if(FreeImage_GetTagLength(tag) <= 4) {
				// 4 bytes or less and value is in the dir entry itself
				pval = pde + 8;
			} else {
				// if its bigger than 4 bytes, the directory entry contains an offset
				// first check if offset exceeds buffer, at this stage FreeImage_GetTagLength may return invalid data
				DWORD offset_value = ReadUint32(msb_order, pde + 8);
				if(offset_value > length) {
					// a problem occured : delete the tag (not free'd after)
					FreeImage_DeleteTag(tag);
					// jump to next entry
					continue;
				}
				// now check that length does not exceed the buffer size
				if(FreeImage_GetTagLength(tag) > length - offset_value){
					// a problem occured : delete the tag (not free'd after)
					FreeImage_DeleteTag(tag);
					// jump to next entry
					continue;
				}
				pval = (char*)(tiffp + offset_value);
			}

			// check for a IFD offset
			BOOL isIFDOffset = FALSE;
			switch(FreeImage_GetTagID(tag)) {
				case TAG_EXIF_OFFSET:
				case TAG_GPS_OFFSET:
				case TAG_INTEROP_OFFSET:
				case TAG_MAKER_NOTE:
					isIFDOffset = TRUE;
					break;
			}
			if(isIFDOffset)	{
				DWORD sub_offset = 0;
				TagLib::MDMODEL next_mdmodel = md_model;
				const BYTE *next_ifd = ifdp;
				
				// get offset and metadata model
				if (FreeImage_GetTagID(tag) == TAG_MAKER_NOTE) {
					processMakerNote(dib, pval, msb_order, &sub_offset, &next_mdmodel);
					next_ifd = (BYTE*)pval + sub_offset;
				} else {
					processIFDOffset(tag, pval, msb_order, &sub_offset, &next_mdmodel);
					next_ifd = (BYTE*)tiffp + sub_offset;
				}

				if((sub_offset < (DWORD) length) && (next_mdmodel != TagLib::UNKNOWN)) {
					// push our current directory state onto the stack
					ifdstack.push(ifdp);
					// bump to the next entry
					de++;
					destack.push(de);

					// push our current metadata model
					modelstack.push(md_model);

					// push new state onto of stack to cause a jump
					ifdstack.push(next_ifd);
					destack.push(0);

					// select a new metadata model
					modelstack.push(next_mdmodel);
					
					// delete the tag as it won't be stored nor deleted in the for() loop
					FreeImage_DeleteTag(tag);
					
					break; // break out of the for loop
				}
				else {
					// unsupported camera model, canon maker tag or something unknown
					// process as a standard tag
					processExifTag(dib, tag, pval, msb_order, md_model);
				}			

			} else {
				// process as a standard tag
				processExifTag(dib, tag, pval, msb_order, md_model);
			}
			
			// delete the tag
			FreeImage_DeleteTag(tag);

        } // for(nde)

		// additional thumbnail data is skipped

    } while (!destack.empty()); 

	//
	// --- handle thumbnail data ---
	//

	const WORD entriesCount0th = ReadUint16(msb_order, ifd0th);
	
	DWORD next_offset = ReadUint32(msb_order, DIR_ENTRY_ADDR(ifd0th, entriesCount0th));
	if((next_offset == 0) || (next_offset >= length)) {
		return TRUE; //< no thumbnail
	}
	
	const BYTE* const ifd1st = (BYTE*)tiffp + next_offset;
	const WORD entriesCount1st = ReadUint16(msb_order, ifd1st);
	
	unsigned thCompression = 0;
	unsigned thOffset = 0; 
	unsigned thSize = 0; 
	
	for(int e = 0; e < entriesCount1st; e++) {

		// point to the directory entry
		const BYTE* base = DIR_ENTRY_ADDR(ifd1st, e);
		
		// check for buffer overflow
		const size_t remaining = (size_t)base + 12 - (size_t)tiffp;
		if(remaining >= length) {
			// bad IFD1 directory, ignore it
			return FALSE;
		}

		// get the tag ID
		WORD tag = ReadUint16(msb_order, base);
		// get the tag type
		WORD type = ReadUint16(msb_order, base + sizeof(WORD));
		// get number of components
		DWORD count = ReadUint32(msb_order, base + sizeof(WORD) + sizeof(WORD));
		// get the tag value
		DWORD offset = ReadUint32(msb_order, base + sizeof(WORD) + sizeof(WORD) + sizeof(DWORD));

		switch(tag) {
			case TAG_COMPRESSION:
				// Tiff Compression Tag (should be COMPRESSION_OJPEG (6), but is not always respected)
				thCompression = offset;
				break;
			case TAG_JPEG_INTERCHANGE_FORMAT:
				// Tiff JPEGInterchangeFormat Tag
				thOffset = offset;
				break;
			case TAG_JPEG_INTERCHANGE_FORMAT_LENGTH:
				// Tiff JPEGInterchangeFormatLength Tag
				thSize = offset;
				break;
			// ### X and Y Resolution ignored, orientation ignored
			case TAG_X_RESOLUTION:		// XResolution
			case TAG_Y_RESOLUTION:		// YResolution
			case TAG_RESOLUTION_UNIT:	// ResolutionUnit
			case TAG_ORIENTATION:		// Orientation
				break;
			default:
				break;
		}
	}
	
	if(/*thCompression != 6 ||*/ thOffset == 0 || thSize == 0) {
		return TRUE;
	}
	
	if(thOffset + thSize > length) {
		return TRUE;
	}
	
	// load the thumbnail

	const BYTE *thLocation = tiffp + thOffset;
	
	FIMEMORY* hmem = FreeImage_OpenMemory(const_cast<BYTE*>(thLocation), thSize);
	FIBITMAP* thumbnail = FreeImage_LoadFromMemory(FIF_JPEG, hmem);
	FreeImage_CloseMemory(hmem);
	
	// store the thumbnail
	FreeImage_SetThumbnail(dib, thumbnail);
	// then delete it
	FreeImage_Unload(thumbnail);

	return TRUE;
}

// --------------------------------------------------------------------------

/**
	Read and decode JPEG_APP1 marker (Exif profile)
	@param dib Input FIBITMAP
	@param dataptr Pointer to the APP1 marker
	@param datalen APP1 marker length
	@return Returns TRUE if successful, FALSE otherwise
*/
BOOL  
jpeg_read_exif_profile(FIBITMAP *dib, const BYTE *dataptr, unsigned datalen) {
    // marker identifying string for Exif = "Exif\0\0"
    BYTE exif_signature[6] = { 0x45, 0x78, 0x69, 0x66, 0x00, 0x00 };
	BYTE lsb_first[4] = { 0x49, 0x49, 0x2A, 0x00 };		// Intel order
	BYTE msb_first[4] = { 0x4D, 0x4D, 0x00, 0x2A };		// Motorola order

	unsigned int length = datalen;
	BYTE *profile = (BYTE*)dataptr;

	// verify the identifying string

	if(memcmp(exif_signature, profile, sizeof(exif_signature)) == 0) {
		// Exif profile - TIFF header with 2 IFDs. 0th - the image attributes, 1st - may be used for thumbnail

		profile += sizeof(exif_signature);
		length  -= sizeof(exif_signature);

		// read the TIFF header (8 bytes)

		// check the endianess order
		
		BOOL bMotorolaOrder = TRUE;

		if(memcmp(profile, lsb_first, sizeof(lsb_first)) == 0) {
			// Exif section in Intel order
			bMotorolaOrder = FALSE;
		} else {
			if(memcmp(profile, msb_first, sizeof(msb_first)) == 0) {
				// Exif section in Motorola order
				bMotorolaOrder = TRUE;
			} else {
				// Invalid Exif alignment marker
				return FALSE;
			}
		}

		// this is the offset to the first IFD (Image File Directory)
		unsigned long first_offset = ReadUint32(bMotorolaOrder, profile + 4);
		if (first_offset > length) {
			// bad Exif data
			return FALSE;
		}

		/*
		Note: as FreeImage 3.14.0, this test is no longer needed for images with similar suspicious offset
		=> tested with Pentax Optio 230, FujiFilm SP-2500 and Canon EOS 300D
		if (first_offset < 8 || first_offset > 16) {
			// This is usually set to 8
			// but PENTAX Optio 230 has it set differently, and uses it as offset. 
			FreeImage_OutputMessageProc(FIF_JPEG, "Exif: Suspicious offset of first IFD value");
			return FALSE;
		}
		*/

		// process Exif directories, starting with Exif-TIFF IFD
		return jpeg_read_exif_dir(dib, profile, first_offset, length, bMotorolaOrder, TagLib::EXIF_MAIN);
	}

	return FALSE;
}

/**
	Read JPEG_APP1 marker (Exif profile)
	@param dib Input FIBITMAP
	@param dataptr Pointer to the APP1 marker
	@param datalen APP1 marker length
	@return Returns TRUE if successful, FALSE otherwise
*/
BOOL  
jpeg_read_exif_profile_raw(FIBITMAP *dib, const BYTE *profile, unsigned length) {
    // marker identifying string for Exif = "Exif\0\0"
    BYTE exif_signature[6] = { 0x45, 0x78, 0x69, 0x66, 0x00, 0x00 };

	// verify the identifying string
	if(memcmp(exif_signature, profile, sizeof(exif_signature)) != 0) {
		// not an Exif profile
		return FALSE;
	}

	// create a tag
	FITAG *tag = FreeImage_CreateTag();
	if(tag) {
		FreeImage_SetTagKey(tag, g_TagLib_ExifRawFieldName);
		FreeImage_SetTagLength(tag, (DWORD)length);
		FreeImage_SetTagCount(tag, (DWORD)length);
		FreeImage_SetTagType(tag, FIDT_BYTE);
		FreeImage_SetTagValue(tag, profile);

		// store the tag
		FreeImage_SetMetadata(FIMD_EXIF_RAW, dib, FreeImage_GetTagKey(tag), tag);

		// destroy the tag
		FreeImage_DeleteTag(tag);

		return TRUE;
	}

	return FALSE;
}

/**
Read and decode JPEG-XR Exif IFD
@param dib Input FIBITMAP
@param profile Pointer to the Exif marker
@param length Exif marker length
@return Returns TRUE if successful, FALSE otherwise
*/
BOOL  
jpegxr_read_exif_profile(FIBITMAP *dib, const BYTE *profile, unsigned length) {
	// assume Little Endian order
	BOOL bMotorolaOrder = FALSE;
	
	// process Exif specific IFD
	return jpeg_read_exif_dir(dib, profile, 0, length, bMotorolaOrder, TagLib::EXIF_EXIF);
}

/**
Read and decode JPEG-XR Exif-GPS IFD
@param dib Input FIBITMAP
@param profile Pointer to the Exif-GPS marker
@param length Exif-GPS marker length
@return Returns TRUE if successful, FALSE otherwise
*/
BOOL  
jpegxr_read_exif_gps_profile(FIBITMAP *dib, const BYTE *profile, unsigned length) {
	// assume Little Endian order
	BOOL bMotorolaOrder = FALSE;
	
	// process Exif GPS IFD
	return jpeg_read_exif_dir(dib, profile, 0, length, bMotorolaOrder, TagLib::EXIF_GPS);
}

/**
Rotate a dib according to Exif info
@param dib Input / Output dib to rotate
@see PluginJPEG.cpp
*/
void 
RotateExif(FIBITMAP **dib) {
	// check for Exif rotation
	if(FreeImage_GetMetadataCount(FIMD_EXIF_MAIN, *dib)) {
		FIBITMAP *rotated = NULL;
		// process Exif rotation
		FITAG *tag = NULL;
		FreeImage_GetMetadata(FIMD_EXIF_MAIN, *dib, "Orientation", &tag);
		if(tag != NULL) {
			if(FreeImage_GetTagID(tag) == TAG_ORIENTATION) {
				unsigned short orientation = *((unsigned short *)FreeImage_GetTagValue(tag));
				switch (orientation) {
					case 1:		// "top, left side" => 0°
						break;
					case 2:		// "top, right side" => flip left-right
						FreeImage_FlipHorizontal(*dib);
						break;
					case 3:		// "bottom, right side"; => -180°
						rotated = FreeImage_Rotate(*dib, 180);
						FreeImage_Unload(*dib);
						*dib = rotated;
						break;
					case 4:		// "bottom, left side" => flip up-down
						FreeImage_FlipVertical(*dib);
						break;
					case 5:		// "left side, top" => +90° + flip up-down
						rotated = FreeImage_Rotate(*dib, 90);
						FreeImage_Unload(*dib);
						*dib = rotated;
						FreeImage_FlipVertical(*dib);
						break;
					case 6:		// "right side, top" => -90°
						rotated = FreeImage_Rotate(*dib, -90);
						FreeImage_Unload(*dib);
						*dib = rotated;
						break;
					case 7:		// "right side, bottom" => -90° + flip up-down
						rotated = FreeImage_Rotate(*dib, -90);
						FreeImage_Unload(*dib);
						*dib = rotated;
						FreeImage_FlipVertical(*dib);
						break;
					case 8:		// "left side, bottom" => +90°
						rotated = FreeImage_Rotate(*dib, 90);
						FreeImage_Unload(*dib);
						*dib = rotated;
						break;
					default:
						break;
				}
			}
		}
	}
}

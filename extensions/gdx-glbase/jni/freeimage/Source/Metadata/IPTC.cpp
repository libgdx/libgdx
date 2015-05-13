// ==========================================================
// Metadata functions implementation
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

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif

#include "FreeImage.h"
#include "Utilities.h"
#include "FreeImageTag.h"

// ----------------------------------------------------------
//   IPTC JPEG / TIFF markers routines
// ----------------------------------------------------------

static const char* IPTC_DELIMITER = ";";	// keywords/supplemental category delimiter
/**
	Read and decode IPTC binary data
*/
BOOL 
read_iptc_profile(FIBITMAP *dib, const BYTE *dataptr, unsigned int datalen) {
	char defaultKey[16];
	size_t length = datalen;
	BYTE *profile = (BYTE*)dataptr;

	const char *JPEG_AdobeCM_Tag = "Adobe_CM";

	std::string Keywords;
	std::string SupplementalCategory;

	WORD tag_id;

	if(!dataptr || (datalen == 0)) {
		return FALSE;
	}

	if(datalen > 8) {
		if(memcmp(JPEG_AdobeCM_Tag, dataptr, 8) == 0) {
			// the "Adobe_CM" APP13 segment presumably contains color management information, 
			// but the meaning of the data is currently unknown. 
			// If anyone has an idea about what this means, please let me know.
			return FALSE;
		}
	}


	// create a tag

	FITAG *tag = FreeImage_CreateTag();

	TagLib& tag_lib = TagLib::instance();

    // find start of the BIM portion of the binary data
    size_t offset = 0;
	while(offset < length - 1) {
		if((profile[offset] == 0x1C) && (profile[offset+1] == 0x02))
			break;
		offset++;
	}

    // for each tag
    while (offset < length) {

        // identifies start of a tag
        if (profile[offset] != 0x1c) {
            break;
        }
        // we need at least five bytes left to read a tag
        if ((offset + 5) >= length) {
            break;
        }

        offset++;

		int directoryType	= profile[offset++];
        int tagType			= profile[offset++];;
        int tagByteCount	= ((profile[offset] & 0xFF) << 8) | (profile[offset + 1] & 0xFF);
        offset += 2;

        if ((offset + tagByteCount) > length) {
            // data for tag extends beyond end of iptc segment
            break;
        }

		if(tagByteCount == 0) {
			// go to next tag
			continue;
		}

		// process the tag

		tag_id = (WORD)(tagType | (directoryType << 8));

		FreeImage_SetTagID(tag, tag_id);
		FreeImage_SetTagLength(tag, tagByteCount);

		// allocate a buffer to store the tag value
		BYTE *iptc_value = (BYTE*)malloc((tagByteCount + 1) * sizeof(BYTE));
		memset(iptc_value, 0, (tagByteCount + 1) * sizeof(BYTE));

		// get the tag value

		switch (tag_id) {
			case TAG_RECORD_VERSION:
			{
				// short
				FreeImage_SetTagType(tag, FIDT_SSHORT);
				FreeImage_SetTagCount(tag, 1);
				short *pvalue = (short*)&iptc_value[0];
				*pvalue = (short)((profile[offset] << 8) | profile[offset + 1]);
				FreeImage_SetTagValue(tag, pvalue);
				break;
			}

			case TAG_RELEASE_DATE:
			case TAG_DATE_CREATED:
				// Date object
			case TAG_RELEASE_TIME:
			case TAG_TIME_CREATED:
				// time
			default:
			{
				// string
				FreeImage_SetTagType(tag, FIDT_ASCII);
				FreeImage_SetTagCount(tag, tagByteCount);
				for(int i = 0; i < tagByteCount; i++) {
					iptc_value[i] = profile[offset + i];
				}
				iptc_value[tagByteCount] = '\0';
				FreeImage_SetTagValue(tag, (char*)&iptc_value[0]);
				break;
			}
		}

		if(tag_id == TAG_SUPPLEMENTAL_CATEGORIES) {
			// concatenate the categories
			if(SupplementalCategory.length() == 0) {
				SupplementalCategory.append((char*)iptc_value);
			} else {
				SupplementalCategory.append(IPTC_DELIMITER);
				SupplementalCategory.append((char*)iptc_value);
			}
		}
		else if(tag_id == TAG_KEYWORDS) {
			// concatenate the keywords
			if(Keywords.length() == 0) {
				Keywords.append((char*)iptc_value);
			} else {
				Keywords.append(IPTC_DELIMITER);
				Keywords.append((char*)iptc_value);
			}
		}
		else {
			// get the tag key and description
			const char *key = tag_lib.getTagFieldName(TagLib::IPTC, tag_id, defaultKey);
			FreeImage_SetTagKey(tag, key);
			const char *description = tag_lib.getTagDescription(TagLib::IPTC, tag_id);
			FreeImage_SetTagDescription(tag, description);

			// store the tag
			if(key) {
				FreeImage_SetMetadata(FIMD_IPTC, dib, key, tag);
			}
		}

		free(iptc_value);

        // next tag
		offset += tagByteCount;

    }

	// store the 'keywords' tag
	if(Keywords.length()) {
		FreeImage_SetTagType(tag, FIDT_ASCII);
		FreeImage_SetTagID(tag, TAG_KEYWORDS);
		FreeImage_SetTagKey(tag, tag_lib.getTagFieldName(TagLib::IPTC, TAG_KEYWORDS, defaultKey));
		FreeImage_SetTagDescription(tag, tag_lib.getTagDescription(TagLib::IPTC, TAG_KEYWORDS));
		FreeImage_SetTagLength(tag, (DWORD)Keywords.length());
		FreeImage_SetTagCount(tag, (DWORD)Keywords.length());
		FreeImage_SetTagValue(tag, (char*)Keywords.c_str());
		FreeImage_SetMetadata(FIMD_IPTC, dib, FreeImage_GetTagKey(tag), tag);
	}

	// store the 'supplemental category' tag
	if(SupplementalCategory.length()) {
		FreeImage_SetTagType(tag, FIDT_ASCII);
		FreeImage_SetTagID(tag, TAG_SUPPLEMENTAL_CATEGORIES);
		FreeImage_SetTagKey(tag, tag_lib.getTagFieldName(TagLib::IPTC, TAG_SUPPLEMENTAL_CATEGORIES, defaultKey));
		FreeImage_SetTagDescription(tag, tag_lib.getTagDescription(TagLib::IPTC, TAG_SUPPLEMENTAL_CATEGORIES));
		FreeImage_SetTagLength(tag, (DWORD)SupplementalCategory.length());
		FreeImage_SetTagCount(tag, (DWORD)SupplementalCategory.length());
		FreeImage_SetTagValue(tag, (char*)SupplementalCategory.c_str());
		FreeImage_SetMetadata(FIMD_IPTC, dib, FreeImage_GetTagKey(tag), tag);
	}

	// delete the tag

	FreeImage_DeleteTag(tag);

	return TRUE;
}

// --------------------------------------------------------------------------

static BYTE* 
append_iptc_tag(BYTE *profile, unsigned *profile_size, WORD id, DWORD length, const void *value) {
	BYTE *buffer = NULL;

	// calculate the new buffer size
	size_t buffer_size = (5 + *profile_size + length) * sizeof(BYTE);
	buffer = (BYTE*)malloc(buffer_size);
	if(!buffer)
		return NULL;

	// add the header
	buffer[0] = 0x1C;
	buffer[1] = 0x02;
	// add the tag type
	buffer[2] = (BYTE)(id & 0x00FF);
	// add the tag length
	buffer[3] = (BYTE)(length >> 8);
	buffer[4] = (BYTE)(length & 0xFF);
	// add the tag value
	memcpy(buffer + 5, (BYTE*)value, length);
	// append the previous profile
	if(NULL == profile)	{
		*profile_size = (5 + length);
	}
	else {
		memcpy(buffer + 5 + length, profile, *profile_size);
		*profile_size += (5 + length);
		free(profile);
	}
	
	return buffer;
}

/**
Encode IPTC metadata into a binary buffer. 
The buffer is allocated by the function and must be freed by the caller. 
*/
BOOL 
write_iptc_profile(FIBITMAP *dib, BYTE **profile, unsigned *profile_size) {
	FITAG *tag = NULL;
	FIMETADATA *mdhandle = NULL;

	BYTE *buffer = NULL;
	unsigned buffer_size = 0;

	// parse all IPTC tags and rebuild a IPTC profile
	mdhandle = FreeImage_FindFirstMetadata(FIMD_IPTC, dib, &tag);

	if(mdhandle) {
		do {
			WORD tag_id	= FreeImage_GetTagID(tag);

			// append the tag to the profile

			switch(tag_id) {
				case TAG_RECORD_VERSION:
					// ignore (already handled)
					break;

				case TAG_SUPPLEMENTAL_CATEGORIES:
				case TAG_KEYWORDS:
					if(FreeImage_GetTagType(tag) == FIDT_ASCII) {
						std::string value = (const char*)FreeImage_GetTagValue(tag);

						// split the tag value
						std::vector<std::string> output;
						std::string delimiter = IPTC_DELIMITER;		
						
						size_t offset = 0;
						size_t delimiterIndex = 0;

						delimiterIndex = value.find(delimiter, offset);
						while (delimiterIndex != std::string::npos) {
							output.push_back(value.substr(offset, delimiterIndex - offset));
							offset += delimiterIndex - offset + delimiter.length();
							delimiterIndex = value.find(delimiter, offset);
						}
						output.push_back(value.substr(offset));

						// add as many tags as there are comma separated strings
						for(int i = 0; i < (int)output.size(); i++) {
							std::string& tag_value = output[i];
							buffer = append_iptc_tag(buffer, &buffer_size, tag_id, (DWORD)tag_value.length(), tag_value.c_str());
						}

					}
					break;

				case TAG_URGENCY:
					if(FreeImage_GetTagType(tag) == FIDT_ASCII) {
						DWORD length = 1;	// keep the first octet only
						buffer = append_iptc_tag(buffer, &buffer_size, tag_id, length, FreeImage_GetTagValue(tag));
					}
					break;

				default:
					if(FreeImage_GetTagType(tag) == FIDT_ASCII) {
						DWORD length = FreeImage_GetTagLength(tag);	
						buffer = append_iptc_tag(buffer, &buffer_size, tag_id, length, FreeImage_GetTagValue(tag));
					}					
					break;
			}

		} while(FreeImage_FindNextMetadata(mdhandle, &tag));
		
		FreeImage_FindCloseMetadata(mdhandle);

		// add the DirectoryVersion tag
		const short version = 0x0200;
		buffer = append_iptc_tag(buffer, &buffer_size, TAG_RECORD_VERSION, sizeof(version), &version);
		
		*profile = buffer;
		*profile_size = buffer_size;

		return TRUE;
	}

	return FALSE;
}

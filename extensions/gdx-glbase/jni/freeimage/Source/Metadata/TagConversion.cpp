// ==========================================================
// Tag to string conversion functions
//
// Design and implementation by
// - Hervé Drolon <drolon@infonie.fr>
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
#include "FIRational.h"

#define MAX_TEXT_EXTENT	512

/**
Convert a tag to a C string
*/
static const char* 
ConvertAnyTag(FITAG *tag) {
	char format[MAX_TEXT_EXTENT];
	static std::string buffer;
	DWORD i;

	if(!tag)
		return NULL;

	buffer.erase();
	
	// convert the tag value to a string buffer

	FREE_IMAGE_MDTYPE tag_type = FreeImage_GetTagType(tag);
	DWORD tag_count = FreeImage_GetTagCount(tag);

	switch(tag_type) {
		case FIDT_BYTE:		// N x 8-bit unsigned integer 
		{
			BYTE *pvalue = (BYTE*)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld",	(LONG) pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %ld",	(LONG) pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_SHORT:	// N x 16-bit unsigned integer 
		{
			unsigned short *pvalue = (unsigned short *)FreeImage_GetTagValue(tag);

			sprintf(format, "%hu", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %hu",	pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_LONG:		// N x 32-bit unsigned integer 
		{
			DWORD *pvalue = (DWORD *)FreeImage_GetTagValue(tag);

			sprintf(format, "%lu", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %lu",	pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_RATIONAL: // N x 64-bit unsigned fraction 
		{
			DWORD *pvalue = (DWORD*)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld/%ld", pvalue[0], pvalue[1]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %ld/%ld", pvalue[2*i], pvalue[2*i+1]);
				buffer += format;
			}
			break;
		}
		case FIDT_SBYTE:	// N x 8-bit signed integer 
		{
			char *pvalue = (char*)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld",	(LONG) pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %ld",	(LONG) pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_SSHORT:	// N x 16-bit signed integer 
		{
			short *pvalue = (short *)FreeImage_GetTagValue(tag);

			sprintf(format, "%hd", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %hd",	pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_SLONG:	// N x 32-bit signed integer 
		{
			LONG *pvalue = (LONG *)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %ld",	pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_SRATIONAL:// N x 64-bit signed fraction 
		{
			LONG *pvalue = (LONG*)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld/%ld", pvalue[0], pvalue[1]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %ld/%ld", pvalue[2*i], pvalue[2*i+1]);
				buffer += format;
			}
			break;
		}
		case FIDT_FLOAT:	// N x 32-bit IEEE floating point 
		{
			float *pvalue = (float *)FreeImage_GetTagValue(tag);

			sprintf(format, "%f", (double) pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, "%f", (double) pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_DOUBLE:	// N x 64-bit IEEE floating point 
		{
			double *pvalue = (double *)FreeImage_GetTagValue(tag);

			sprintf(format, "%f", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, "%f", pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_IFD:		// N x 32-bit unsigned integer (offset) 
		{
			DWORD *pvalue = (DWORD *)FreeImage_GetTagValue(tag);

			sprintf(format, "%X", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " %X",	pvalue[i]);
				buffer += format;
			}
			break;
		}
		case FIDT_PALETTE:	// N x 32-bit RGBQUAD 
		{
			RGBQUAD *pvalue = (RGBQUAD *)FreeImage_GetTagValue(tag);

			sprintf(format, "(%d,%d,%d,%d)", pvalue[0].rgbRed, pvalue[0].rgbGreen, pvalue[0].rgbBlue, pvalue[0].rgbReserved);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, " (%d,%d,%d,%d)", pvalue[i].rgbRed, pvalue[i].rgbGreen, pvalue[i].rgbBlue, pvalue[i].rgbReserved);
				buffer += format;
			}
			break;
		}
		
		case FIDT_LONG8:	// N x 64-bit unsigned integer 
		{
			UINT64 *pvalue = (UINT64 *)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, "%ld", pvalue[i]);
				buffer += format;
			}
			break;
		}

		case FIDT_IFD8:		// N x 64-bit unsigned integer (offset)
		{
			UINT64 *pvalue = (UINT64 *)FreeImage_GetTagValue(tag);

			sprintf(format, "%X", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, "%X", pvalue[i]);
				buffer += format;
			}
			break;
		}

		case FIDT_SLONG8:	// N x 64-bit signed integer
		{
			INT64 *pvalue = (INT64 *)FreeImage_GetTagValue(tag);

			sprintf(format, "%ld", pvalue[0]);
			buffer += format;
			for(i = 1; i < tag_count; i++) {
				sprintf(format, "%ld", pvalue[i]);
				buffer += format;
			}
			break;
		}

		case FIDT_ASCII:	// 8-bit bytes w/ last byte null 
		case FIDT_UNDEFINED:// 8-bit untyped data 
		default:
		{
			int max_size = MIN((int)FreeImage_GetTagLength(tag), (int)MAX_TEXT_EXTENT);
			if(max_size == MAX_TEXT_EXTENT)
				max_size--;
			memcpy(format, (char*)FreeImage_GetTagValue(tag), max_size);
			format[max_size] = '\0';
			buffer += format;
			break;
		}
	}

	return buffer.c_str();
}

/**
Convert a Exif tag to a C string
*/
static const char* 
ConvertExifTag(FITAG *tag) {
	char format[MAX_TEXT_EXTENT];
	static std::string buffer;

	if(!tag)
		return NULL;

	buffer.erase();

	// convert the tag value to a string buffer

	switch(FreeImage_GetTagID(tag)) {
		case TAG_ORIENTATION:
		{
			unsigned short orientation = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch (orientation) {
				case 1:
					return "top, left side";
				case 2:
					return "top, right side";
				case 3:
					return "bottom, right side";
				case 4:
					return "bottom, left side";
				case 5:
					return "left side, top";
				case 6:
					return "right side, top";
				case 7:
					return "right side, bottom";
				case 8:
					return "left side, bottom";
				default:
					break;
			}
		}
		break;

		case TAG_REFERENCE_BLACK_WHITE:
		{
			DWORD *pvalue = (DWORD*)FreeImage_GetTagValue(tag);
			if(FreeImage_GetTagLength(tag) == 48) {
				// reference black point value and reference white point value (ReferenceBlackWhite)
				int blackR = 0, whiteR = 0, blackG = 0, whiteG = 0, blackB = 0, whiteB = 0;
				if(pvalue[1])
					blackR = (int)(pvalue[0] / pvalue[1]);
				if(pvalue[3])
					whiteR = (int)(pvalue[2] / pvalue[3]);
				if(pvalue[5])
					blackG = (int)(pvalue[4] / pvalue[5]);
				if(pvalue[7])
					whiteG = (int)(pvalue[6] / pvalue[7]);
				if(pvalue[9])
					blackB = (int)(pvalue[8] / pvalue[9]);
				if(pvalue[11])
					whiteB = (int)(pvalue[10] / pvalue[11]);

				sprintf(format, "[%d,%d,%d] [%d,%d,%d]", blackR, blackG, blackB, whiteR, whiteG, whiteB);
				buffer += format;
				return buffer.c_str();
			}

		}
		break;

		case TAG_COLOR_SPACE:
		{
			unsigned short colorSpace = *((unsigned short *)FreeImage_GetTagValue(tag));
			if (colorSpace == 1) {
				return "sRGB";
			} else if (colorSpace == 65535) {
				return "Undefined";
			} else {
				return "Unknown";
			}
		}
		break;

		case TAG_COMPONENTS_CONFIGURATION:
		{
			const char *componentStrings[7] = {"", "Y", "Cb", "Cr", "R", "G", "B"};
			BYTE *pvalue = (BYTE*)FreeImage_GetTagValue(tag);
			for(DWORD i = 0; i < MIN((DWORD)4, FreeImage_GetTagCount(tag)); i++) {
				int j = pvalue[i];
				if(j > 0 && j < 7)
					buffer += componentStrings[j];
			}
			return buffer.c_str();
		}
		break;

		case TAG_COMPRESSED_BITS_PER_PIXEL:
		{
			FIRational r(tag);
			buffer = r.toString();
			if(buffer == "1")
				buffer += " bit/pixel";
			else 
				buffer += " bits/pixel";
			return buffer.c_str();
		}
		break;

		case TAG_X_RESOLUTION:
		case TAG_Y_RESOLUTION:
		case TAG_FOCAL_PLANE_X_RES:
		case TAG_FOCAL_PLANE_Y_RES:
		case TAG_BRIGHTNESS_VALUE:
		case TAG_EXPOSURE_BIAS_VALUE:
		{
			FIRational r(tag);
			buffer = r.toString();
			return buffer.c_str();
		}
		break;

		case TAG_RESOLUTION_UNIT:
		case TAG_FOCAL_PLANE_UNIT:
		{
			unsigned short resolutionUnit = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch (resolutionUnit) {
				case 1:
					return "(No unit)";
				case 2:
					return "inches";
				case 3:
					return "cm";
				default:
					break;
			}
		}
		break;

		case TAG_YCBCR_POSITIONING:
		{
			unsigned short yCbCrPosition = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch (yCbCrPosition) {
				case 1:
					return "Center of pixel array";
				case 2:
					return "Datum point";
				default:
					break;
			}
		} 
		break;

		case TAG_EXPOSURE_TIME:
		{
			FIRational r(tag);
			buffer = r.toString();
			buffer += " sec";
			return buffer.c_str();
		}
		break;

		case TAG_SHUTTER_SPEED_VALUE:
		{
			FIRational r(tag);
			LONG apexValue = r.longValue();
			LONG apexPower = 1 << apexValue;
			sprintf(format, "1/%d sec", (int)apexPower);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_APERTURE_VALUE:
		case TAG_MAX_APERTURE_VALUE:
		{
			FIRational r(tag);
			double apertureApex = r.doubleValue();
	        double rootTwo = sqrt((double)2);
			double fStop = pow(rootTwo, apertureApex);
			sprintf(format, "F%.1f", fStop);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_FNUMBER:
		{
			FIRational r(tag);
			double fnumber = r.doubleValue();
			sprintf(format, "F%.1f", fnumber);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_FOCAL_LENGTH:
		{
			FIRational r(tag);
			double focalLength = r.doubleValue();
			sprintf(format, "%.1f mm", focalLength);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_FOCAL_LENGTH_IN_35MM_FILM:
		{
			unsigned short focalLength = *((unsigned short *)FreeImage_GetTagValue(tag));
			sprintf(format, "%hu mm", focalLength);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_FLASH:
		{
			unsigned short flash = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch(flash) {
				case 0x0000:
					return "Flash did not fire";
				case 0x0001:
					return "Flash fired";
				case 0x0005:
					return "Strobe return light not detected";
				case 0x0007:
					return "Strobe return light detected";
				case 0x0009:
					return "Flash fired, compulsory flash mode";
				case 0x000D:
					return "Flash fired, compulsory flash mode, return light not detected";
				case 0x000F:
					return "Flash fired, compulsory flash mode, return light detected";
				case 0x0010:
					return "Flash did not fire, compulsory flash mode";
				case 0x0018:
					return "Flash did not fire, auto mode";
				case 0x0019:
					return "Flash fired, auto mode";
				case 0x001D:
					return "Flash fired, auto mode, return light not detected";
				case 0x001F:
					return "Flash fired, auto mode, return light detected";
				case 0x0020:
					return "No flash function";
				case 0x0041:
					return "Flash fired, red-eye reduction mode";
				case 0x0045:
					return "Flash fired, red-eye reduction mode, return light not detected";
				case 0x0047:
					return "Flash fired, red-eye reduction mode, return light detected";
				case 0x0049:
					return "Flash fired, compulsory flash mode, red-eye reduction mode";
				case 0x004D:
					return "Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected";
				case 0x004F:
					return "Flash fired, compulsory flash mode, red-eye reduction mode, return light detected";
				case 0x0059:
					return "Flash fired, auto mode, red-eye reduction mode";
				case 0x005D:
					return "Flash fired, auto mode, return light not detected, red-eye reduction mode";
				case 0x005F:
					return "Flash fired, auto mode, return light detected, red-eye reduction mode";
				default:
					sprintf(format, "Unknown (%d)", flash);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_SCENE_TYPE:
		{
			BYTE sceneType = *((BYTE*)FreeImage_GetTagValue(tag));
			if (sceneType == 1) {
				return "Directly photographed image";
			} else {
				sprintf(format, "Unknown (%d)", sceneType);
				buffer += format;
				return buffer.c_str();
			}
		}
		break;

		case TAG_SUBJECT_DISTANCE:
		{
			FIRational r(tag);
			if(r.getNumerator() == 0xFFFFFFFF) {
				return "Infinity";
			} else if(r.getNumerator() == 0) {
				return "Distance unknown";
			} else {
				double distance = r.doubleValue();
				sprintf(format, "%.3f meters", distance);
				buffer += format;
				return buffer.c_str();
			}
		}
		break;
			
		case TAG_METERING_MODE:
		{
			unsigned short meteringMode = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch (meteringMode) {
				case 0:
					return "Unknown";
				case 1:
					return "Average";
				case 2:
					return "Center weighted average";
				case 3:
					return "Spot";
				case 4:
					return "Multi-spot";
				case 5:
					return "Multi-segment";
				case 6:
					return "Partial";
				case 255:
					return "(Other)";
				default:
					return "";
			}
		}
		break;

		case TAG_LIGHT_SOURCE:
		{
			unsigned short lightSource = *((unsigned short *)FreeImage_GetTagValue(tag));
			switch (lightSource) {
				case 0:
					return "Unknown";
				case 1:
					return "Daylight";
				case 2:
					return "Fluorescent";
				case 3:
					return "Tungsten (incandescent light)";
				case 4:
					return "Flash";
				case 9:
					return "Fine weather";
				case 10:
					return "Cloudy weather";
				case 11:
					return "Shade";
				case 12:
					return "Daylight fluorescent (D 5700 - 7100K)";
				case 13:
					return "Day white fluorescent (N 4600 - 5400K)";
				case 14:
					return "Cool white fluorescent (W 3900 - 4500K)";
				case 15:
					return "White fluorescent (WW 3200 - 3700K)";
				case 17:
					return "Standard light A";
				case 18:
					return "Standard light B";
				case 19:
					return "Standard light C";
				case 20:
					return "D55";
				case 21:
					return "D65";
				case 22:
					return "D75";
				case 23:
					return "D50";
				case 24:
					return "ISO studio tungsten";
				case 255:
					return "(Other)";
				default:
					return "";
			}
		}
		break;

		case TAG_SENSING_METHOD:
		{
			unsigned short sensingMethod = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (sensingMethod) {
				case 1:
					return "(Not defined)";
				case 2:
					return "One-chip color area sensor";
				case 3:
					return "Two-chip color area sensor";
				case 4:
					return "Three-chip color area sensor";
				case 5:
					return "Color sequential area sensor";
				case 7:
					return "Trilinear sensor";
				case 8:
					return "Color sequential linear sensor";
				default:
					return "";
			}
		}
		break;

		case TAG_FILE_SOURCE:
		{
			BYTE fileSource = *((BYTE*)FreeImage_GetTagValue(tag));
			if (fileSource == 3) {
				return "Digital Still Camera (DSC)";
			} else {
				sprintf(format, "Unknown (%d)", fileSource);
				buffer += format;
				return buffer.c_str();
			}
        }
		break;

		case TAG_EXPOSURE_PROGRAM:
		{
			unsigned short exposureProgram = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (exposureProgram) {
				case 1:
					return "Manual control";
				case 2:
					return "Program normal";
				case 3:
					return "Aperture priority";
				case 4:
					return "Shutter priority";
				case 5:
					return "Program creative (slow program)";
				case 6:
					return "Program action (high-speed program)";
				case 7:
					return "Portrait mode";
				case 8:
					return "Landscape mode";
				default:
					sprintf(format, "Unknown program (%d)", exposureProgram);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_CUSTOM_RENDERED:
		{
			unsigned short customRendered = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (customRendered) {
				case 0:
					return "Normal process";
				case 1:
					return "Custom process";
				default:
					sprintf(format, "Unknown rendering (%d)", customRendered);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_EXPOSURE_MODE:
		{
			unsigned short exposureMode = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (exposureMode) {
				case 0:
					return "Auto exposure";
				case 1:
					return "Manual exposure";
				case 2:
					return "Auto bracket";
				default:
					sprintf(format, "Unknown mode (%d)", exposureMode);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_WHITE_BALANCE:
		{
			unsigned short whiteBalance = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (whiteBalance) {
				case 0:
					return "Auto white balance";
				case 1:
					return "Manual white balance";
				default:
					sprintf(format, "Unknown (%d)", whiteBalance);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_SCENE_CAPTURE_TYPE:
		{
			unsigned short sceneType = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (sceneType) {
				case 0:
					return "Standard";
				case 1:
					return "Landscape";
				case 2:
					return "Portrait";
				case 3:
					return "Night scene";
				default:
					sprintf(format, "Unknown (%d)", sceneType);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_GAIN_CONTROL:
		{
			unsigned short gainControl = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (gainControl) {
				case 0:
					return "None";
				case 1:
					return "Low gain up";
				case 2:
					return "High gain up";
				case 3:
					return "Low gain down";
				case 4:
					return "High gain down";
				default:
					sprintf(format, "Unknown (%d)", gainControl);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_CONTRAST:
		{
			unsigned short contrast = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (contrast) {
				case 0:
					return "Normal";
				case 1:
					return "Soft";
				case 2:
					return "Hard";
				default:
					sprintf(format, "Unknown (%d)", contrast);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_SATURATION:
		{
			unsigned short saturation = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (saturation) {
				case 0:
					return "Normal";
				case 1:
					return "Low saturation";
				case 2:
					return "High saturation";
				default:
					sprintf(format, "Unknown (%d)", saturation);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_SHARPNESS:
		{
			unsigned short sharpness = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (sharpness) {
				case 0:
					return "Normal";
				case 1:
					return "Soft";
				case 2:
					return "Hard";
				default:
					sprintf(format, "Unknown (%d)", sharpness);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_SUBJECT_DISTANCE_RANGE:
		{
			unsigned short distanceRange = *((unsigned short *)FreeImage_GetTagValue(tag));

			switch (distanceRange) {
				case 0:
					return "unknown";
				case 1:
					return "Macro";
				case 2:
					return "Close view";
				case 3:
					return "Distant view";
				default:
					sprintf(format, "Unknown (%d)", distanceRange);
					buffer += format;
					return buffer.c_str();
			}
		}
		break;

		case TAG_ISO_SPEED_RATINGS:
		{
			unsigned short isoEquiv = *((unsigned short *)FreeImage_GetTagValue(tag));
			if (isoEquiv < 50) {
				isoEquiv *= 200;
			}
			sprintf(format, "%d", isoEquiv);
			buffer += format;
			return buffer.c_str();
		}
		break;

		case TAG_USER_COMMENT:
		{
			// first 8 bytes are used to define an ID code
			// we assume this is an ASCII string
			const BYTE *userComment = (BYTE*)FreeImage_GetTagValue(tag);
			for(DWORD i = 8; i < FreeImage_GetTagLength(tag); i++) {
				buffer += userComment[i];
			}
			buffer += '\0';
			return buffer.c_str();
		}
		break;

		case TAG_COMPRESSION:
		{
			WORD compression = *((WORD*)FreeImage_GetTagValue(tag));
			switch(compression) {
				case TAG_COMPRESSION_NONE:
					sprintf(format, "dump mode (%d)", compression);
					break;
				case TAG_COMPRESSION_CCITTRLE:
					sprintf(format, "CCITT modified Huffman RLE (%d)", compression);
					break;
				case TAG_COMPRESSION_CCITTFAX3:
					sprintf(format, "CCITT Group 3 fax encoding (%d)", compression);
					break;
				/*
				case TAG_COMPRESSION_CCITT_T4:
					sprintf(format, "CCITT T.4 (TIFF 6 name) (%d)", compression);
					break;
				*/
				case TAG_COMPRESSION_CCITTFAX4:
					sprintf(format, "CCITT Group 4 fax encoding (%d)", compression);
					break;
				/*
				case TAG_COMPRESSION_CCITT_T6:
					sprintf(format, "CCITT T.6 (TIFF 6 name) (%d)", compression);
					break;
				*/
				case TAG_COMPRESSION_LZW:
					sprintf(format, "LZW (%d)", compression);
					break;
				case TAG_COMPRESSION_OJPEG:
					sprintf(format, "!6.0 JPEG (%d)", compression);
					break;
				case TAG_COMPRESSION_JPEG:
					sprintf(format, "JPEG (%d)", compression);
					break;
				case TAG_COMPRESSION_NEXT:
					sprintf(format, "NeXT 2-bit RLE (%d)", compression);
					break;
				case TAG_COMPRESSION_CCITTRLEW:
					sprintf(format, "CCITTRLEW (%d)", compression);
					break;
				case TAG_COMPRESSION_PACKBITS:
					sprintf(format, "PackBits Macintosh RLE (%d)", compression);
					break;
				case TAG_COMPRESSION_THUNDERSCAN:
					sprintf(format, "ThunderScan RLE (%d)", compression);
					break;
				case TAG_COMPRESSION_PIXARFILM:
					sprintf(format, "Pixar companded 10bit LZW (%d)", compression);
					break;
				case TAG_COMPRESSION_PIXARLOG:
					sprintf(format, "Pixar companded 11bit ZIP (%d)", compression);
					break;
				case TAG_COMPRESSION_DEFLATE:
					sprintf(format, "Deflate compression (%d)", compression);
					break;
				case TAG_COMPRESSION_ADOBE_DEFLATE:
					sprintf(format, "Adobe Deflate compression (%d)", compression);
					break;
				case TAG_COMPRESSION_DCS:
					sprintf(format, "Kodak DCS encoding (%d)", compression);
					break;
				case TAG_COMPRESSION_JBIG:
					sprintf(format, "ISO JBIG (%d)", compression);
					break;
				case TAG_COMPRESSION_SGILOG:
					sprintf(format, "SGI Log Luminance RLE (%d)", compression);
					break;
				case TAG_COMPRESSION_SGILOG24:
					sprintf(format, "SGI Log 24-bit packed (%d)", compression);
					break;
				case TAG_COMPRESSION_JP2000:
					sprintf(format, "Leadtools JPEG2000 (%d)", compression);
					break;
				case TAG_COMPRESSION_LZMA:
					sprintf(format, "LZMA2 (%d)", compression);
					break;
				default:
					sprintf(format, "Unknown type (%d)", compression);
					break;
			}

			buffer += format;
			return buffer.c_str();
		}
		break;
	}

	return ConvertAnyTag(tag);
}

/**
Convert a Exif GPS tag to a C string
*/
static const char* 
ConvertExifGPSTag(FITAG *tag) {
	char format[MAX_TEXT_EXTENT];
	static std::string buffer;

	if(!tag)
		return NULL;

	buffer.erase();

	// convert the tag value to a string buffer

	switch(FreeImage_GetTagID(tag)) {
		case TAG_GPS_LATITUDE:
		case TAG_GPS_LONGITUDE:
		case TAG_GPS_TIME_STAMP:
		{
			DWORD *pvalue = (DWORD*)FreeImage_GetTagValue(tag);
			if(FreeImage_GetTagLength(tag) == 24) {
				// dd:mm:ss or hh:mm:ss
				int dd = 0, mm = 0;
				double ss = 0;

				// convert to seconds
				if(pvalue[1])
					ss += ((double)pvalue[0] / (double)pvalue[1]) * 3600;
				if(pvalue[3])
					ss += ((double)pvalue[2] / (double)pvalue[3]) * 60;
				if(pvalue[5])
					ss += ((double)pvalue[4] / (double)pvalue[5]);
				
				// convert to dd:mm:ss.ss
				dd = (int)(ss / 3600);
				mm = (int)(ss / 60) - dd * 60;
				ss = ss - dd * 3600 - mm * 60;

				sprintf(format, "%d:%d:%.2f", dd, mm, ss);
				buffer += format;
				return buffer.c_str();
			}
		}
		break;

		case TAG_GPS_VERSION_ID:
		case TAG_GPS_LATITUDE_REF:
		case TAG_GPS_LONGITUDE_REF:
		case TAG_GPS_ALTITUDE_REF:
		case TAG_GPS_ALTITUDE:
		case TAG_GPS_SATELLITES:
		case TAG_GPS_STATUS:
		case TAG_GPS_MEASURE_MODE:
		case TAG_GPS_DOP:
		case TAG_GPS_SPEED_REF:
		case TAG_GPS_SPEED:
		case TAG_GPS_TRACK_REF:
		case TAG_GPS_TRACK:
		case TAG_GPS_IMG_DIRECTION_REF:
		case TAG_GPS_IMG_DIRECTION:
		case TAG_GPS_MAP_DATUM:
		case TAG_GPS_DEST_LATITUDE_REF:
		case TAG_GPS_DEST_LATITUDE:
		case TAG_GPS_DEST_LONGITUDE_REF:
		case TAG_GPS_DEST_LONGITUDE:
		case TAG_GPS_DEST_BEARING_REF:
		case TAG_GPS_DEST_BEARING:
		case TAG_GPS_DEST_DISTANCE_REF:
		case TAG_GPS_DEST_DISTANCE:
		case TAG_GPS_PROCESSING_METHOD:
		case TAG_GPS_AREA_INFORMATION:
		case TAG_GPS_DATE_STAMP:
		case TAG_GPS_DIFFERENTIAL:
			break;
	}

	return ConvertAnyTag(tag);
}

// ==========================================================
// Tag to string conversion function
//

const char* DLL_CALLCONV 
FreeImage_TagToString(FREE_IMAGE_MDMODEL model, FITAG *tag, char *Make) {
	switch(model) {
		case FIMD_EXIF_MAIN:
		case FIMD_EXIF_EXIF:
			return ConvertExifTag(tag);

		case FIMD_EXIF_GPS:
			return ConvertExifGPSTag(tag);

		case FIMD_EXIF_MAKERNOTE:
			// We should use the Make string to select an appropriate conversion function
			// TO DO ...
			break;

		case FIMD_EXIF_INTEROP:
		default:
			break;
	}

	return ConvertAnyTag(tag);
}


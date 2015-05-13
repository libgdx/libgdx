// ==========================================================
// JPEG XR Loader & Writer
//
// Design and implementation by
// - Herve Drolon (drolon@infonie.fr)
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
#include "../Metadata/FreeImageTag.h"

#include "../LibJXR/jxrgluelib/JXRGlue.h"

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ==========================================================
// FreeImageIO interface (I/O streaming functions)
// ==========================================================

/**
JXR wrapper for FreeImage I/O handle
*/
typedef struct tagFreeImageJXRIO {
    FreeImageIO *io;
	fi_handle handle;
} FreeImageJXRIO;

static ERR 
_jxr_io_Read(WMPStream* pWS, void* pv, size_t cb) {
	FreeImageJXRIO *fio = (FreeImageJXRIO*)pWS->state.pvObj;
	return (fio->io->read_proc(pv, (unsigned)cb, 1, fio->handle) == 1) ? WMP_errSuccess : WMP_errFileIO;
}

static ERR 
_jxr_io_Write(WMPStream* pWS, const void* pv, size_t cb) {
	FreeImageJXRIO *fio = (FreeImageJXRIO*)pWS->state.pvObj;
	if(0 != cb) {
		return (fio->io->write_proc((void*)pv, (unsigned)cb, 1, fio->handle) == 1) ? WMP_errSuccess : WMP_errFileIO;
	}
	return WMP_errFileIO;
}

static ERR 
_jxr_io_SetPos(WMPStream* pWS, size_t offPos) {
	FreeImageJXRIO *fio = (FreeImageJXRIO*)pWS->state.pvObj;
    return (fio->io->seek_proc(fio->handle, (long)offPos, SEEK_SET) == 0) ? WMP_errSuccess : WMP_errFileIO;
}

static ERR 
_jxr_io_GetPos(WMPStream* pWS, size_t* poffPos) {
	FreeImageJXRIO *fio = (FreeImageJXRIO*)pWS->state.pvObj;
    long lOff = fio->io->tell_proc(fio->handle);
	if(lOff == -1) {
		return WMP_errFileIO;
	}
    *poffPos = (size_t)lOff;
	return WMP_errSuccess;
}

static Bool 
_jxr_io_EOS(WMPStream* pWS) {
	FreeImageJXRIO *fio = (FreeImageJXRIO*)pWS->state.pvObj;
    long currentPos = fio->io->tell_proc(fio->handle);
    fio->io->seek_proc(fio->handle, 0, SEEK_END);
    long fileRemaining = fio->io->tell_proc(fio->handle) - currentPos;
    fio->io->seek_proc(fio->handle, currentPos, SEEK_SET);
    return (fileRemaining > 0);
}

static ERR 
_jxr_io_Close(WMPStream** ppWS) {
	WMPStream *pWS = *ppWS;
	// HACK : we use fMem to avoid a stream destruction by the library
	// because FreeImage MUST HAVE the ownership of the stream
	// see _jxr_io_Create
	if(pWS && pWS->fMem) {
		free(pWS);
		*ppWS = NULL;
	}
	return WMP_errSuccess;
}

static ERR 
_jxr_io_Create(WMPStream **ppWS, FreeImageJXRIO *jxr_io) {
	*ppWS = (WMPStream*)calloc(1, sizeof(**ppWS));
	if(*ppWS) {
		WMPStream *pWS = *ppWS;

		pWS->state.pvObj = jxr_io;
		pWS->Close = _jxr_io_Close;
		pWS->EOS = _jxr_io_EOS;
		pWS->Read = _jxr_io_Read;
		pWS->Write = _jxr_io_Write;
		pWS->SetPos = _jxr_io_SetPos;
		pWS->GetPos = _jxr_io_GetPos;

		// HACK : we use fMem to avoid a stream destruction by the library
		// because FreeImage MUST HAVE the ownership of the stream
		// see _jxr_io_Close
		pWS->fMem = FALSE;

		return WMP_errSuccess;
	}
	return WMP_errOutOfMemory;
}

// ==========================================================
// JPEG XR Error handling
// ==========================================================

static const char* 
JXR_ErrorMessage(const int error) {
	switch(error) {
		case WMP_errNotYetImplemented:
		case WMP_errAbstractMethod:
			return "Not yet implemented";
		case WMP_errOutOfMemory:
			return "Out of memory";
		case WMP_errFileIO:
			return "File I/O error";
		case WMP_errBufferOverflow:
			return "Buffer overflow";
		case WMP_errInvalidParameter:
			return "Invalid parameter";
		case WMP_errInvalidArgument:
			return "Invalid argument";
		case WMP_errUnsupportedFormat:
			return "Unsupported format";
		case WMP_errIncorrectCodecVersion:
			return "Incorrect codec version";
		case WMP_errIndexNotFound:
			return "Format converter: Index not found";
		case WMP_errOutOfSequence:
			return "Metadata: Out of sequence";
		case WMP_errMustBeMultipleOf16LinesUntilLastCall:
			return "Must be multiple of 16 lines until last call";
		case WMP_errPlanarAlphaBandedEncRequiresTempFile:
			return "Planar alpha banded encoder requires temp files";
		case WMP_errAlphaModeCannotBeTranscoded:
			return "Alpha mode cannot be transcoded";
		case WMP_errIncorrectCodecSubVersion:
			return "Incorrect codec subversion";
		case WMP_errFail:
		case WMP_errNotInitialized:
		default:
			return "Invalid instruction - please contact the FreeImage team";
	}

	return NULL;
}

// ==========================================================
// Helper functions & macro
// ==========================================================

#define JXR_CHECK(error_code) \
	if(error_code < 0) { \
		const char *error_message = JXR_ErrorMessage(error_code); \
		throw error_message; \
	}

// --------------------------------------------------------------------------

/**
Input conversions natively understood by FreeImage
@see GetNativePixelFormat
*/
typedef struct tagJXRInputConversion {
	BITDEPTH_BITS bdBitDepth;
	U32 cbitUnit;
	FREE_IMAGE_TYPE image_type;
	unsigned red_mask;
	unsigned green_mask;
	unsigned blue_mask;
} JXRInputConversion;

/**
Conversion table for native FreeImage formats
@see GetNativePixelFormat
*/
static JXRInputConversion s_FreeImagePixelInfo[] = {
	// 1-bit bitmap
	{ BD_1, 1, FIT_BITMAP, 0, 0, 0 },
	// 8-, 24-, 32-bit bitmap
	{ BD_8, 8, FIT_BITMAP, 0, 0, 0 },
	{ BD_8, 24, FIT_BITMAP, 0, 0, 0 },
	{ BD_8, 32, FIT_BITMAP, 0, 0, 0 },
	// 16-bit RGB 565
	{ BD_565, 16, FIT_BITMAP, FI16_565_RED_MASK, FI16_565_GREEN_MASK, FI16_565_BLUE_MASK },
	// 16-bit RGB 555
	{ BD_5, 16, FIT_BITMAP, FI16_555_RED_MASK, FI16_555_GREEN_MASK, FI16_555_BLUE_MASK },
	// 16-bit greyscale, RGB16, RGBA16 bitmap
	{ BD_16, 16, FIT_UINT16, 0, 0, 0 },
	{ BD_16, 48, FIT_RGB16, 0, 0, 0 },
	{ BD_16, 64, FIT_RGBA16, 0, 0, 0 },
	// 32-bit float, RGBF, RGBAF bitmap
	{ BD_32F, 32, FIT_FLOAT, 0, 0, 0 },
	{ BD_32F, 96, FIT_RGBF, 0, 0, 0 },
	{ BD_32F, 128, FIT_RGBAF, 0, 0, 0 }
};

/**
Scan input pixelInfo specifications and return the equivalent FreeImage info for loading
@param pixelInfo Image specifications
@param out_guid_format (returned value) output pixel format
@param image_type (returned value) Image type
@param bpp (returned value) Image bit depth
@param red_mask (returned value) RGB mask
@param green_mask (returned value) RGB mask
@param blue_mask (returned value) RGB mask
@return Returns WMP_errSuccess if successful, returns WMP_errFail otherwise
@see GetInputPixelFormat
*/
static ERR
GetNativePixelFormat(const PKPixelInfo *pixelInfo, PKPixelFormatGUID *out_guid_format, FREE_IMAGE_TYPE *image_type, unsigned *bpp, unsigned *red_mask, unsigned *green_mask, unsigned *blue_mask) {
	const unsigned s_FreeImagePixelInfoSize = (unsigned)sizeof(s_FreeImagePixelInfo) / sizeof(*(s_FreeImagePixelInfo));

	for(unsigned i = 0; i < s_FreeImagePixelInfoSize; i++) {
		if(pixelInfo->bdBitDepth == s_FreeImagePixelInfo[i].bdBitDepth) {
			if(pixelInfo->cbitUnit == s_FreeImagePixelInfo[i].cbitUnit) {
				// found ! now get dst image format specifications
				memcpy(out_guid_format, pixelInfo->pGUIDPixFmt, sizeof(PKPixelFormatGUID));
				*image_type = s_FreeImagePixelInfo[i].image_type;
				*bpp = s_FreeImagePixelInfo[i].cbitUnit;
				*red_mask	= s_FreeImagePixelInfo[i].red_mask;
				*green_mask	= s_FreeImagePixelInfo[i].green_mask;
				*blue_mask	= s_FreeImagePixelInfo[i].blue_mask;
				return WMP_errSuccess;
			}
		}
	}

	// not found : need pixel format conversion
	return WMP_errFail;
}

/**
Scan input file guid format and return the equivalent FreeImage info & target guid format for loading
@param pDecoder Decoder handle
@param guid_format (returned value) Output pixel format
@param image_type (returned value) Image type
@param bpp (returned value) Image bit depth
@param red_mask (returned value) RGB mask
@param green_mask (returned value) RGB mask
@param blue_mask (returned value) RGB mask
@return Returns TRUE if successful, returns FALSE otherwise
*/
static ERR
GetInputPixelFormat(PKImageDecode *pDecoder, PKPixelFormatGUID *guid_format, FREE_IMAGE_TYPE *image_type, unsigned *bpp, unsigned *red_mask, unsigned *green_mask, unsigned *blue_mask) {
	ERR error_code = 0;		// error code as returned by the interface
	PKPixelInfo pixelInfo;	// image specifications

	try {		
		// get input file pixel format ...
		PKPixelFormatGUID pguidSourcePF;
		error_code = pDecoder->GetPixelFormat(pDecoder, &pguidSourcePF);
		JXR_CHECK(error_code);
		pixelInfo.pGUIDPixFmt = &pguidSourcePF;
		// ... check for a supported format and get the format specifications
		error_code = PixelFormatLookup(&pixelInfo, LOOKUP_FORWARD);
		JXR_CHECK(error_code);

		// search for an equivalent native FreeImage format
		error_code = GetNativePixelFormat(&pixelInfo, guid_format, image_type, bpp, red_mask, green_mask, blue_mask);

		if(error_code != WMP_errSuccess) {
			// try to find a suitable conversion function ...
			const PKPixelFormatGUID *ppguidTargetPF = NULL;	// target pixel format
			unsigned iIndex = 0;	// first available conversion function
			do {
				error_code = PKFormatConverter_EnumConversions(&pguidSourcePF, iIndex, &ppguidTargetPF);
				if(error_code == WMP_errSuccess) {
					// found a conversion function, is the converted format a native FreeImage format ?
					pixelInfo.pGUIDPixFmt = ppguidTargetPF;
					error_code = PixelFormatLookup(&pixelInfo, LOOKUP_FORWARD);
					JXR_CHECK(error_code);
					error_code = GetNativePixelFormat(&pixelInfo, guid_format, image_type, bpp, red_mask, green_mask, blue_mask);
					if(error_code == WMP_errSuccess) {
						break;
					}
				}
				// try next conversion function
				iIndex++;
			} while(error_code != WMP_errIndexNotFound);

		}

		return (error_code == WMP_errSuccess) ? WMP_errSuccess : WMP_errUnsupportedFormat;

	} catch(...) {
		return error_code;
	}
}

// --------------------------------------------------------------------------

/**
Scan input dib format and return the equivalent PKPixelFormatGUID format for saving
@param dib Image to be saved
@param guid_format (returned value) GUID format
@param bHasAlpha (returned value) TRUE if an alpha layer is present
@return Returns TRUE if successful, returns FALSE otherwise
*/
static ERR
GetOutputPixelFormat(FIBITMAP *dib, PKPixelFormatGUID *guid_format, BOOL *bHasAlpha) {
	const FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);
	const unsigned bpp = FreeImage_GetBPP(dib);
	const FREE_IMAGE_COLOR_TYPE color_type = FreeImage_GetColorType(dib);

	*guid_format = GUID_PKPixelFormatDontCare;
	*bHasAlpha = FALSE;

	switch(image_type) {
		case FIT_BITMAP:	// standard image	: 1-, 4-, 8-, 16-, 24-, 32-bit
			switch(bpp) {
				case 1:
					// assume FIC_MINISBLACK
					if(color_type == FIC_MINISBLACK) {
						*guid_format = GUID_PKPixelFormatBlackWhite;
					}
					break;
				case 8:
					// assume FIC_MINISBLACK
					if(color_type == FIC_MINISBLACK) {
						*guid_format = GUID_PKPixelFormat8bppGray;
					}
					break;
				case 16:
					if ((FreeImage_GetRedMask(dib) == FI16_565_RED_MASK) && (FreeImage_GetGreenMask(dib) == FI16_565_GREEN_MASK) && (FreeImage_GetBlueMask(dib) == FI16_565_BLUE_MASK)) {
						*guid_format = GUID_PKPixelFormat16bppRGB565;
					} else {
						// includes case where all the masks are 0
						*guid_format = GUID_PKPixelFormat16bppRGB555;
					}
					break;
#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
				case 24:
					*guid_format = GUID_PKPixelFormat24bppBGR;
					break;
				case 32:
					*guid_format = GUID_PKPixelFormat32bppBGRA;
					*bHasAlpha = TRUE;
					break;
#elif FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
				case 24:
					*guid_format = GUID_PKPixelFormat24bppRGB;
					break;
				case 32:
					*guid_format = GUID_PKPixelFormat32bppRGBA;
					*bHasAlpha = TRUE;
					break;
#endif
				case 4:
				default:
					// not supported
					break;
			}
			break;
		case FIT_UINT16:	// array of unsigned short	: unsigned 16-bit
			*guid_format = GUID_PKPixelFormat16bppGray;
			break;
		case FIT_FLOAT:		// array of float			: 32-bit IEEE floating point
			*guid_format = GUID_PKPixelFormat32bppGrayFloat;
			break;
		case FIT_RGB16:		// 48-bit RGB image			: 3 x 16-bit
			*guid_format = GUID_PKPixelFormat48bppRGB;
			break;
		case FIT_RGBA16:	// 64-bit RGBA image		: 4 x 16-bit
			*guid_format = GUID_PKPixelFormat64bppRGBA;
			*bHasAlpha = TRUE;
			break;
		case FIT_RGBF:		// 96-bit RGB float image	: 3 x 32-bit IEEE floating point
			*guid_format = GUID_PKPixelFormat96bppRGBFloat;
			break;
		case FIT_RGBAF:		// 128-bit RGBA float image	: 4 x 32-bit IEEE floating point
			*guid_format = GUID_PKPixelFormat128bppRGBAFloat;
			*bHasAlpha = TRUE;
			break;

		case FIT_INT16:		// array of short			: signed 16-bit
		case FIT_UINT32:	// array of unsigned long	: unsigned 32-bit
		case FIT_INT32:		// array of long			: signed 32-bit
		case FIT_DOUBLE:	// array of double			: 64-bit IEEE floating point
		case FIT_COMPLEX:	// array of FICOMPLEX		: 2 x 64-bit IEEE floating point

		default:
			// unsupported format
			break;
	}

	return (*guid_format != GUID_PKPixelFormatDontCare) ? WMP_errSuccess : WMP_errUnsupportedFormat;
}

// ==========================================================
// Metadata loading & saving
// ==========================================================

/**
Read a JPEG-XR IFD as a buffer
*/
static ERR
ReadProfile(WMPStream* pStream, unsigned cbByteCount, unsigned uOffset, BYTE **ppbProfile) {
	// (re-)allocate profile buffer
	BYTE *pbProfile = *ppbProfile;
	pbProfile = (BYTE*)realloc(pbProfile, cbByteCount);
	if(!pbProfile) {
		return WMP_errOutOfMemory;
	}
	// read the profile
	if(WMP_errSuccess == pStream->SetPos(pStream, uOffset)) {
		if(WMP_errSuccess == pStream->Read(pStream, pbProfile, cbByteCount)) {
			*ppbProfile = pbProfile;
			return WMP_errSuccess;
		}
	}
	return WMP_errFileIO;
}

/**
Convert a DPKPROPVARIANT to a FITAG, then store the tag as FIMD_EXIF_MAIN
*/
static BOOL
ReadPropVariant(WORD tag_id, const DPKPROPVARIANT & varSrc, FIBITMAP *dib) {
	DWORD dwSize;

	if(varSrc.vt == DPKVT_EMPTY) {
		return FALSE;
	}

	// get the tag key
	TagLib& s = TagLib::instance();
	const char *key = s.getTagFieldName(TagLib::EXIF_MAIN, tag_id, NULL);
	if(!key) {
		return FALSE;
	}

	// create a tag
	FITAG *tag = FreeImage_CreateTag();
	if(tag) {
		// set tag ID
		FreeImage_SetTagID(tag, tag_id);
		// set tag type, count, length and value
		switch (varSrc.vt) {
			case DPKVT_LPSTR:
				FreeImage_SetTagType(tag, FIDT_ASCII);
				dwSize = (DWORD)strlen(varSrc.VT.pszVal) + 1;
				FreeImage_SetTagCount(tag, dwSize);
				FreeImage_SetTagLength(tag, dwSize);
				FreeImage_SetTagValue(tag, varSrc.VT.pszVal);
				break;
			
			case DPKVT_LPWSTR:
				FreeImage_SetTagType(tag, FIDT_UNDEFINED);
				dwSize = (DWORD)(sizeof(U16) * (wcslen((wchar_t *) varSrc.VT.pwszVal) + 1)); // +1 for NULL term
				FreeImage_SetTagCount(tag, dwSize / 2);
				FreeImage_SetTagLength(tag, dwSize);
				FreeImage_SetTagValue(tag, varSrc.VT.pwszVal);
				break;
	            
			case DPKVT_UI2:
				FreeImage_SetTagType(tag, FIDT_SHORT);
				FreeImage_SetTagCount(tag, 1);
				FreeImage_SetTagLength(tag, 2);
				FreeImage_SetTagValue(tag, &varSrc.VT.uiVal);
				break;

			case DPKVT_UI4:
				FreeImage_SetTagType(tag, FIDT_LONG);
				FreeImage_SetTagCount(tag, 1);
				FreeImage_SetTagLength(tag, 4);
				FreeImage_SetTagValue(tag, &varSrc.VT.ulVal);
				break;

			default:
				assert(FALSE); // This case is not handled
				break;
		}
		// get the tag desctiption
		const char *description = s.getTagDescription(TagLib::EXIF_MAIN, tag_id);
		FreeImage_SetTagDescription(tag, description);

		// store the tag
		FreeImage_SetMetadata(FIMD_EXIF_MAIN, dib, key, tag);

		FreeImage_DeleteTag(tag);
	}
	return TRUE;
}

/**
Read JPEG-XR descriptive metadata and store as EXIF_MAIN metadata
@see ReadPropVariant
*/
static ERR
ReadDescriptiveMetadata(PKImageDecode *pID, FIBITMAP *dib) {
	// get Exif TIFF metadata
	const DESCRIPTIVEMETADATA *pDescMetadata = &pID->WMP.sDescMetadata;
	// convert metadata to FITAG and store into the EXIF_MAIN metadata model
	ReadPropVariant(WMP_tagImageDescription, pDescMetadata->pvarImageDescription, dib);
	ReadPropVariant(WMP_tagCameraMake, pDescMetadata->pvarCameraMake, dib);
	ReadPropVariant(WMP_tagCameraModel, pDescMetadata->pvarCameraModel, dib);
	ReadPropVariant(WMP_tagSoftware, pDescMetadata->pvarSoftware, dib);
	ReadPropVariant(WMP_tagDateTime, pDescMetadata->pvarDateTime, dib);
	ReadPropVariant(WMP_tagArtist, pDescMetadata->pvarArtist, dib);
	ReadPropVariant(WMP_tagCopyright, pDescMetadata->pvarCopyright, dib);
	ReadPropVariant(WMP_tagRatingStars, pDescMetadata->pvarRatingStars, dib);
	ReadPropVariant(WMP_tagRatingValue, pDescMetadata->pvarRatingValue, dib);
	ReadPropVariant(WMP_tagCaption, pDescMetadata->pvarCaption, dib);
	ReadPropVariant(WMP_tagDocumentName, pDescMetadata->pvarDocumentName, dib);
	ReadPropVariant(WMP_tagPageName, pDescMetadata->pvarPageName, dib);
	ReadPropVariant(WMP_tagPageNumber, pDescMetadata->pvarPageNumber, dib);
	ReadPropVariant(WMP_tagHostComputer, pDescMetadata->pvarHostComputer, dib);
	return WMP_errSuccess;
}

/**
Read ICC, XMP, Exif, Exif-GPS, IPTC, descriptive (i.e. Exif-TIFF) metadata
*/
static ERR
ReadMetadata(PKImageDecode *pID, FIBITMAP *dib) {
	ERR error_code = 0;		// error code as returned by the interface
	size_t currentPos = 0;	// current stream position
	
	WMPStream *pStream = pID->pStream;
	WmpDEMisc *wmiDEMisc = &pID->WMP.wmiDEMisc;
	BYTE *pbProfile = NULL;

	try {
		// save current position
		error_code = pStream->GetPos(pStream, &currentPos);
		JXR_CHECK(error_code);

		// ICC profile
		if(0 != wmiDEMisc->uColorProfileByteCount) {
			unsigned cbByteCount = wmiDEMisc->uColorProfileByteCount;
			unsigned uOffset = wmiDEMisc->uColorProfileOffset;
			error_code = ReadProfile(pStream, cbByteCount, uOffset, &pbProfile);
			JXR_CHECK(error_code);
			FreeImage_CreateICCProfile(dib, pbProfile, cbByteCount);
		}

		// XMP metadata
		if(0 != wmiDEMisc->uXMPMetadataByteCount) {
			unsigned cbByteCount = wmiDEMisc->uXMPMetadataByteCount;
			unsigned uOffset = wmiDEMisc->uXMPMetadataOffset;
			error_code = ReadProfile(pStream, cbByteCount, uOffset, &pbProfile);
			JXR_CHECK(error_code);
			// store the tag as XMP
			FITAG *tag = FreeImage_CreateTag();
			if(tag) {
				FreeImage_SetTagLength(tag, cbByteCount);
				FreeImage_SetTagCount(tag, cbByteCount);
				FreeImage_SetTagType(tag, FIDT_ASCII);
				FreeImage_SetTagValue(tag, pbProfile);
				FreeImage_SetTagKey(tag, g_TagLib_XMPFieldName);
				FreeImage_SetMetadata(FIMD_XMP, dib, FreeImage_GetTagKey(tag), tag);
				FreeImage_DeleteTag(tag);
			}
		}

		// IPTC metadata
		if(0 != wmiDEMisc->uIPTCNAAMetadataByteCount) {
			unsigned cbByteCount = wmiDEMisc->uIPTCNAAMetadataByteCount;
			unsigned uOffset = wmiDEMisc->uIPTCNAAMetadataOffset;
			error_code = ReadProfile(pStream, cbByteCount, uOffset, &pbProfile);
			JXR_CHECK(error_code);
			// decode the IPTC profile
			read_iptc_profile(dib, pbProfile, cbByteCount);
		}

		// Exif metadata
		if(0 != wmiDEMisc->uEXIFMetadataByteCount) {
			unsigned cbByteCount = wmiDEMisc->uEXIFMetadataByteCount;
			unsigned uOffset = wmiDEMisc->uEXIFMetadataOffset;
			error_code = ReadProfile(pStream, cbByteCount, uOffset, &pbProfile);
			JXR_CHECK(error_code);
			// decode the Exif profile
			jpegxr_read_exif_profile(dib, pbProfile, cbByteCount);
		}

		// Exif-GPS metadata
		if(0 != wmiDEMisc->uGPSInfoMetadataByteCount) {
			unsigned cbByteCount = wmiDEMisc->uGPSInfoMetadataByteCount;
			unsigned uOffset = wmiDEMisc->uGPSInfoMetadataOffset;
			error_code = ReadProfile(pStream, cbByteCount, uOffset, &pbProfile);
			JXR_CHECK(error_code);
			// decode the Exif-GPS profile
			jpegxr_read_exif_gps_profile(dib, pbProfile, cbByteCount);
		}

		// free profile buffer
		free(pbProfile);
		// restore initial position
		error_code = pID->pStream->SetPos(pID->pStream, currentPos);
		JXR_CHECK(error_code);

		// as a LAST STEP, read descriptive metadata
		// these metadata overwrite possible identical Exif-TIFF metadata 
		// that could have been read inside the Exif IFD
		
		return ReadDescriptiveMetadata(pID, dib);

	} catch(...) {
		// free profile buffer
		free(pbProfile);
		if(currentPos) {
			// restore initial position
			pStream->SetPos(pStream, currentPos);
		}
		return error_code;
	}
}

// ==========================================================
// Quantization tables (Y, U, V, YHP, UHP, VHP), 
// optimized for PSNR
// ==========================================================

static const int DPK_QPS_420[11][6] = {      // for 8 bit only
    { 66, 65, 70, 72, 72, 77 },
    { 59, 58, 63, 64, 63, 68 },
    { 52, 51, 57, 56, 56, 61 },
    { 48, 48, 54, 51, 50, 55 },
    { 43, 44, 48, 46, 46, 49 },
    { 37, 37, 42, 38, 38, 43 },
    { 26, 28, 31, 27, 28, 31 },
    { 16, 17, 22, 16, 17, 21 },
    { 10, 11, 13, 10, 10, 13 },
    {  5,  5,  6,  5,  5,  6 },
    {  2,  2,  3,  2,  2,  2 }
};

static const int DPK_QPS_8[12][6] = {
    { 67, 79, 86, 72, 90, 98 },
    { 59, 74, 80, 64, 83, 89 },
    { 53, 68, 75, 57, 76, 83 },
    { 49, 64, 71, 53, 70, 77 },
    { 45, 60, 67, 48, 67, 74 },
    { 40, 56, 62, 42, 59, 66 },
    { 33, 49, 55, 35, 51, 58 },
    { 27, 44, 49, 28, 45, 50 },
    { 20, 36, 42, 20, 38, 44 },
    { 13, 27, 34, 13, 28, 34 },
    {  7, 17, 21,  8, 17, 21 }, // Photoshop 100%
    {  2,  5,  6,  2,  5,  6 }
};

static const int DPK_QPS_16[11][6] = {
    { 197, 203, 210, 202, 207, 213 },
    { 174, 188, 193, 180, 189, 196 },
    { 152, 167, 173, 156, 169, 174 },
    { 135, 152, 157, 137, 153, 158 },
    { 119, 137, 141, 119, 138, 142 },
    { 102, 120, 125, 100, 120, 124 },
    {  82,  98, 104,  79,  98, 103 },
    {  60,  76,  81,  58,  76,  81 },
    {  39,  52,  58,  36,  52,  58 },
    {  16,  27,  33,  14,  27,  33 },
    {   5,   8,   9,   4,   7,   8 }
};

static const int DPK_QPS_16f[11][6] = {
    { 148, 177, 171, 165, 187, 191 },
    { 133, 155, 153, 147, 172, 181 },
    { 114, 133, 138, 130, 157, 167 },
    {  97, 118, 120, 109, 137, 144 },
    {  76,  98, 103,  85, 115, 121 },
    {  63,  86,  91,  62,  96,  99 },
    {  46,  68,  71,  43,  73,  75 },
    {  29,  48,  52,  27,  48,  51 },
    {  16,  30,  35,  14,  29,  34 },
    {   8,  14,  17,   7,  13,  17 },
    {   3,   5,   7,   3,   5,   6 }
};

static const int DPK_QPS_32f[11][6] = {
    { 194, 206, 209, 204, 211, 217 },
    { 175, 187, 196, 186, 193, 205 },
    { 157, 170, 177, 167, 180, 190 },
    { 133, 152, 156, 144, 163, 168 },
    { 116, 138, 142, 117, 143, 148 },
    {  98, 120, 123,  96, 123, 126 },
    {  80,  99, 102,  78,  99, 102 },
    {  65,  79,  84,  63,  79,  84 },
    {  48,  61,  67,  45,  60,  66 },
    {  27,  41,  46,  24,  40,  45 },
    {   3,  22,  24,   2,  21,  22 }
};

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "JPEG-XR";
}

static const char * DLL_CALLCONV
Description() {
	return "JPEG XR image format";
}

static const char * DLL_CALLCONV
Extension() {
	return "jxr,wdp,hdp";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/vnd.ms-photo";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE jxr_signature[3] = { 0x49, 0x49, 0xBC };
	BYTE signature[3] = { 0, 0, 0 };

	io->read_proc(&signature, 1, 3, handle);

	return (memcmp(jxr_signature, signature, 3) == 0);
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
		(depth == 1)  ||
		(depth == 8)  ||
		(depth == 16) ||
		(depth == 24) || 
		(depth == 32)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (
		(type == FIT_BITMAP) ||
		(type == FIT_UINT16) ||
		(type == FIT_RGB16)  ||
		(type == FIT_RGBA16) ||
		(type == FIT_FLOAT)  ||
		(type == FIT_RGBF)   ||
		(type == FIT_RGBAF)
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

// ==========================================================
//	Open & Close
// ==========================================================

static void * DLL_CALLCONV
Open(FreeImageIO *io, fi_handle handle, BOOL read) {
	WMPStream *pStream = NULL;	// stream interface
	if(io && handle) {
		// allocate the FreeImageIO stream wrapper
		FreeImageJXRIO *jxr_io = (FreeImageJXRIO*)malloc(sizeof(FreeImageJXRIO));
		if(jxr_io) {
			jxr_io->io = io;
			jxr_io->handle = handle;
			// create a JXR stream wrapper
			if(_jxr_io_Create(&pStream, jxr_io) != WMP_errSuccess) {
				free(jxr_io);
				return NULL;
			}
		}
	}
	return pStream;
}

static void DLL_CALLCONV
Close(FreeImageIO *io, fi_handle handle, void *data) {
	WMPStream *pStream = (WMPStream*)data;
	if(pStream) {
		// free the FreeImageIO stream wrapper
		FreeImageJXRIO *jxr_io = (FreeImageJXRIO*)pStream->state.pvObj;
		free(jxr_io);
		// free the JXR stream wrapper
		pStream->fMem = TRUE;
		_jxr_io_Close(&pStream);
	}
}

// ==========================================================
//	Load
// ==========================================================

/**
Set decoder parameters
@param pDecoder Decoder handle
@param flags FreeImage load flags
*/
static void 
SetDecoderParameters(PKImageDecode *pDecoder, int flags) {
	// load image & alpha for formats with alpha
	pDecoder->WMP.wmiSCP.uAlphaMode = 2;
	// more options to come ...
}

/**
Copy or convert & copy decoded pixels into the dib
@param pDecoder Decoder handle
@param out_guid_format Target guid format
@param dib Output dib
@param width Image width
@param height Image height
@return Returns 0 if successful, returns ERR otherwise
*/
static ERR
CopyPixels(PKImageDecode *pDecoder, PKPixelFormatGUID out_guid_format, FIBITMAP *dib, int width, int height) {
	PKFormatConverter *pConverter = NULL;	// pixel format converter
	ERR error_code = 0;	// error code as returned by the interface
	BYTE *pb = NULL;	// local buffer used for pixel format conversion
	
	// image dimensions
	const PKRect rect = {0, 0, width, height};

	try {
		// get input file pixel format ...
		PKPixelFormatGUID in_guid_format;
		error_code = pDecoder->GetPixelFormat(pDecoder, &in_guid_format);
		JXR_CHECK(error_code);
		
		// is a format conversion needed ?

		if(IsEqualGUID(out_guid_format, in_guid_format)) {
			// no conversion, load bytes "as is" ...

			// get a pointer to dst pixel data
			BYTE *dib_bits = FreeImage_GetBits(dib);

			// get dst pitch (count of BYTE for stride)
			const unsigned cbStride = FreeImage_GetPitch(dib);			

			// decode and copy bits to dst array
			error_code = pDecoder->Copy(pDecoder, &rect, dib_bits, cbStride);
			JXR_CHECK(error_code);		
		}
		else {
			// we need to use the conversion API ...
			
			// allocate the pixel format converter
			error_code = PKCodecFactory_CreateFormatConverter(&pConverter);
			JXR_CHECK(error_code);
			
			// set the conversion function
			error_code = pConverter->Initialize(pConverter, pDecoder, NULL, out_guid_format);
			JXR_CHECK(error_code);
			
			// get the maximum stride
			unsigned cbStride = 0;
			{
				PKPixelInfo pPIFrom;
				PKPixelInfo pPITo;
				
				pPIFrom.pGUIDPixFmt = &in_guid_format;
				error_code = PixelFormatLookup(&pPIFrom, LOOKUP_FORWARD);
				JXR_CHECK(error_code);

				pPITo.pGUIDPixFmt = &out_guid_format;
				error_code = PixelFormatLookup(&pPITo, LOOKUP_FORWARD);
				JXR_CHECK(error_code);

				unsigned cbStrideFrom = ((pPIFrom.cbitUnit + 7) >> 3) * width;
				unsigned cbStrideTo = ((pPITo.cbitUnit + 7) >> 3) * width;
				cbStride = MAX(cbStrideFrom, cbStrideTo);
			}

			// allocate a local decoder / encoder buffer
			error_code = PKAllocAligned((void **) &pb, cbStride * height, 128);
			JXR_CHECK(error_code);

			// copy / convert pixels
			error_code = pConverter->Copy(pConverter, &rect, pb, cbStride);
			JXR_CHECK(error_code);

			// now copy pixels into the dib
			const size_t line_size = FreeImage_GetLine(dib);
			for(int y = 0; y < height; y++) {
				BYTE *src_bits = (BYTE*)(pb + y * cbStride);
				BYTE *dst_bits = (BYTE*)FreeImage_GetScanLine(dib, y);
				memcpy(dst_bits, src_bits, line_size);
			}
			
			// free the local buffer
			PKFreeAligned((void **) &pb);

			// free the pixel format converter
			PKFormatConverter_Release(&pConverter);
		}

		// FreeImage DIB are upside-down relative to usual graphic conventions
		FreeImage_FlipVertical(dib);

		// post-processing ...
		// -------------------

		// swap RGB as needed

#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
		if(IsEqualGUID(out_guid_format, GUID_PKPixelFormat24bppRGB) || IsEqualGUID(out_guid_format, GUID_PKPixelFormat32bppRGB)) {
			SwapRedBlue32(dib);
		}
#elif FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_RGB
		if(IsEqualGUID(out_guid_format, GUID_PKPixelFormat24bppBGR) || IsEqualGUID(out_guid_format, GUID_PKPixelFormat32bppBGR)) {
			SwapRedBlue32(dib);
		}
#endif
		
		return WMP_errSuccess;

	} catch(...) {
		// free the local buffer
		PKFreeAligned((void **) &pb);
		// free the pixel format converter
		PKFormatConverter_Release(&pConverter);

		return error_code;
	}
}

// --------------------------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	PKImageDecode *pDecoder = NULL;	// decoder interface
	ERR error_code = 0;				// error code as returned by the interface
	PKPixelFormatGUID guid_format;	// loaded pixel format (== input file pixel format if no conversion needed)
	
	FREE_IMAGE_TYPE image_type = FIT_UNKNOWN;	// input image type
	unsigned bpp = 0;							// input image bit depth
	FIBITMAP *dib = NULL;
	
	// get the I/O stream wrapper
	WMPStream *pDecodeStream = (WMPStream*)data;

	if(!handle || !pDecodeStream) {
		return NULL;
	}

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	try {
		int width, height;	// image dimensions (in pixels)

		// create a JXR decoder interface and initialize function pointers with *_WMP functions
		error_code = PKImageDecode_Create_WMP(&pDecoder);
		JXR_CHECK(error_code);

		// attach the stream to the decoder ...
		// ... then read the image container and the metadata
		error_code = pDecoder->Initialize(pDecoder, pDecodeStream);
		JXR_CHECK(error_code);

		// set decoder parameters
		SetDecoderParameters(pDecoder, flags);

		// get dst image format specifications
		unsigned red_mask = 0, green_mask = 0, blue_mask = 0;
		error_code = GetInputPixelFormat(pDecoder, &guid_format, &image_type, &bpp, &red_mask, &green_mask, &blue_mask);
		JXR_CHECK(error_code);

		// get image dimensions
		pDecoder->GetSize(pDecoder, &width, &height);

		// allocate dst image
		{			
			dib = FreeImage_AllocateHeaderT(header_only, image_type, width, height, bpp, red_mask, green_mask, blue_mask);
			if(!dib) {
				throw FI_MSG_ERROR_DIB_MEMORY;
			}
			if(FreeImage_GetBPP(dib) == 1) {
				// BD_1 - build a FIC_MINISBLACK palette
				RGBQUAD *pal = FreeImage_GetPalette(dib);
				pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
				pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;
			}
		}

		// get image resolution
		{
			float resX, resY;	// image resolution (in dots per inch)
			// convert from English units, i.e. dots per inch to universal units, i.e. dots per meter
			pDecoder->GetResolution(pDecoder, &resX, &resY);
			FreeImage_SetDotsPerMeterX(dib, (unsigned)(resX / 0.0254F + 0.5F));
			FreeImage_SetDotsPerMeterY(dib, (unsigned)(resY / 0.0254F + 0.5F));
		}

		// get metadata & ICC profile
		error_code = ReadMetadata(pDecoder, dib);
		JXR_CHECK(error_code);

		if(header_only) {
			// header only mode ...
			
			// free the decoder
			pDecoder->Release(&pDecoder);
			assert(pDecoder == NULL);

			return dib;
		}
		
		// copy pixels into the dib, perform pixel conversion if needed
		error_code = CopyPixels(pDecoder, guid_format, dib, width, height);
		JXR_CHECK(error_code);

		// free the decoder
		pDecoder->Release(&pDecoder);
		assert(pDecoder == NULL);

		return dib;

	} catch (const char *message) {
		// unload the dib
		FreeImage_Unload(dib);
		// free the decoder
		pDecoder->Release(&pDecoder);

		if(NULL != message) {
			FreeImage_OutputMessageProc(s_format_id, message);
		}
	}

	return NULL;
}

// ==========================================================
//	Save
// ==========================================================

/**
Configure compression parameters

ImageQuality  Q (BD==1)  Q (BD==8)   Q (BD==16)  Q (BD==32F) Subsample   Overlap
[0.0, 0.4]    8-IQ*5     (see table) (see table) (see table) 4:4:4       2
(0.4, 0.8)    8-IQ*5     (see table) (see table) (see table) 4:4:4       1
[0.8, 1.0)    8-IQ*5     (see table) (see table) (see table) 4:4:4       1
[1.0, 1.0]    1          1           1           1           4:4:4       0

@param wmiSCP Encoder parameters
@param pixelInfo Image specifications
@param fltImageQuality Image output quality in [0..1), 1 means lossless
*/
static void 
SetCompression(CWMIStrCodecParam *wmiSCP, const PKPixelInfo *pixelInfo, float fltImageQuality) {
    if(fltImageQuality < 1.0F) {
        // overlap
		if(fltImageQuality >= 0.5F) {
			wmiSCP->olOverlap = OL_ONE;
		} else {
			wmiSCP->olOverlap = OL_TWO;
		}
		// chroma sub-sampling
		if(fltImageQuality >= 0.5F || pixelInfo->uBitsPerSample > 8) {
			wmiSCP->cfColorFormat = YUV_444;
		} else {
			wmiSCP->cfColorFormat = YUV_420;
		}

	    // bit depth
		if(pixelInfo->bdBitDepth == BD_1) {
			wmiSCP->uiDefaultQPIndex = (U8)(8 - 5.0F * fltImageQuality + 0.5F);
		}
		else {
			// remap [0.8, 0.866, 0.933, 1.0] to [0.8, 0.9, 1.0, 1.1]
            // to use 8-bit DPK QP table (0.933 == Photoshop JPEG 100)
            if(fltImageQuality > 0.8F && pixelInfo->bdBitDepth == BD_8 && wmiSCP->cfColorFormat != YUV_420 && wmiSCP->cfColorFormat != YUV_422) {
				fltImageQuality = 0.8F + (fltImageQuality - 0.8F) * 1.5F;
			}

            const int qi = (int) (10.0F * fltImageQuality);
            const float qf = 10.0F * fltImageQuality - (float)qi;
			
			const int *pQPs = 
				(wmiSCP->cfColorFormat == YUV_420 || wmiSCP->cfColorFormat == YUV_422) ?
				DPK_QPS_420[qi] :
				(pixelInfo->bdBitDepth == BD_8 ? DPK_QPS_8[qi] :
				(pixelInfo->bdBitDepth == BD_16 ? DPK_QPS_16[qi] :
				(pixelInfo->bdBitDepth == BD_16F ? DPK_QPS_16f[qi] :
				DPK_QPS_32f[qi])));
				
			wmiSCP->uiDefaultQPIndex = (U8) (0.5F + (float) pQPs[0] * (1.0F - qf) + (float) (pQPs + 6)[0] * qf);
			wmiSCP->uiDefaultQPIndexU = (U8) (0.5F + (float) pQPs[1] * (1.0F - qf) + (float) (pQPs + 6)[1] * qf);
			wmiSCP->uiDefaultQPIndexV = (U8) (0.5F + (float) pQPs[2] * (1.0F - qf) + (float) (pQPs + 6)[2] * qf);
            wmiSCP->uiDefaultQPIndexYHP = (U8) (0.5F + (float) pQPs[3] * (1.0F - qf) + (float) (pQPs + 6)[3] * qf);
			wmiSCP->uiDefaultQPIndexUHP = (U8) (0.5F + (float) pQPs[4] * (1.0F - qf) + (float) (pQPs + 6)[4] * qf);
			wmiSCP->uiDefaultQPIndexVHP = (U8) (0.5F + (float) pQPs[5] * (1.0F - qf) + (float) (pQPs + 6)[5] * qf);
		}
	} // fltImageQuality < 1.0F
    else {
		// lossless mode
		wmiSCP->uiDefaultQPIndex = 1;
	}
}

/**
Set encoder parameters
@param wmiSCP Encoder parameters
@param pixelInfo Image specifications
@param flags FreeImage save flags
@param bHasAlpha TRUE if an alpha layer is present
*/
static void 
SetEncoderParameters(CWMIStrCodecParam *wmiSCP, const PKPixelInfo *pixelInfo, int flags, BOOL bHasAlpha) {
	float fltImageQuality = 1.0F;

	// all values have been set to zero by the API
	// update default values for some attributes
    wmiSCP->cfColorFormat = YUV_444;		// color format
    wmiSCP->bdBitDepth = BD_LONG;			// internal bit depth
    wmiSCP->bfBitstreamFormat = SPATIAL;	// compressed image data in spatial order
    wmiSCP->bProgressiveMode = FALSE;		// sequential mode
    wmiSCP->olOverlap = OL_ONE;				// single level overlap processing 
	wmiSCP->cNumOfSliceMinus1H = 0;			// # of horizontal slices
	wmiSCP->cNumOfSliceMinus1V = 0;			// # of vertical slices
    wmiSCP->sbSubband = SB_ALL;				// keep all subbands
    wmiSCP->uAlphaMode = 0;					// 0:no alpha 1: alpha only else: something + alpha 
    wmiSCP->uiDefaultQPIndex = 1;			// quantization for grey or rgb layer(s), 1: lossless
    wmiSCP->uiDefaultQPIndexAlpha = 1;		// quantization for alpha layer, 1: lossless

	// process the flags
	// -----------------

	// progressive mode
	if((flags & JXR_PROGRESSIVE) == JXR_PROGRESSIVE) {
		// turn on progressive mode (instead of sequential mode)
		wmiSCP->bProgressiveMode = TRUE;
	}

	// quality in [0.01 - 1.0), 1.0 means lossless - default is 0.80
	int quality = flags & 0x7F;
	if(quality == 0) {
		// defaut to 0.80
		fltImageQuality = 0.8F;
	} else if((flags & JXR_LOSSLESS) == JXR_LOSSLESS) {
		fltImageQuality = 1.0F;
	} else {
		quality = (quality >= 100) ? 100 : quality;
		fltImageQuality = quality / 100.0F;
	}
	SetCompression(wmiSCP, pixelInfo, fltImageQuality);

	// alpha compression
	if(bHasAlpha) {
		wmiSCP->uAlphaMode = 2;	// encode with a planar alpha channel
	}
}

// --------------------------------------------------------------------------

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	BOOL bIsFlipped = FALSE;		// FreeImage DIB are upside-down relative to usual graphic conventions
	PKPixelFormatGUID guid_format;	// image format
	PKPixelInfo pixelInfo;			// image specifications
	BOOL bHasAlpha = FALSE;			// is alpha layer present ?

	PKImageEncode *pEncoder = NULL;		// encoder interface
	ERR error_code = 0;					// error code as returned by the interface

	// get the I/O stream wrapper
	WMPStream *pEncodeStream = (WMPStream*)data;

	if(!dib || !handle || !pEncodeStream) {
		return FALSE;
	}

	try {
		// get image dimensions
		unsigned width = FreeImage_GetWidth(dib);
		unsigned height = FreeImage_GetHeight(dib);

		// check JPEG-XR limits
		if((width < MB_WIDTH_PIXEL) || (height < MB_HEIGHT_PIXEL)) {
			FreeImage_OutputMessageProc(s_format_id, "Unsupported image size: width x height = %d x %d", width, height);
			throw (const char*)NULL;
		}

		// get output pixel format
		error_code = GetOutputPixelFormat(dib, &guid_format, &bHasAlpha);
		JXR_CHECK(error_code);
		pixelInfo.pGUIDPixFmt = &guid_format;
		error_code = PixelFormatLookup(&pixelInfo, LOOKUP_FORWARD);
		JXR_CHECK(error_code);

		// create a JXR encoder interface and initialize function pointers with *_WMP functions
		error_code = PKImageEncode_Create_WMP(&pEncoder);
		JXR_CHECK(error_code);

		// attach the stream to the encoder and set all encoder parameters to zero ...
		error_code = pEncoder->Initialize(pEncoder, pEncodeStream, &pEncoder->WMP.wmiSCP, sizeof(CWMIStrCodecParam));
		JXR_CHECK(error_code);

		// ... then configure the encoder
		SetEncoderParameters(&pEncoder->WMP.wmiSCP, &pixelInfo, flags, bHasAlpha);

		// set pixel format
		pEncoder->SetPixelFormat(pEncoder, guid_format);

		// set image size
		pEncoder->SetSize(pEncoder, width, height);
		
		// set resolution (convert from universal units to English units)
		float resX = (float)(unsigned)(0.5F + 0.0254F * FreeImage_GetDotsPerMeterX(dib));
		float resY = (float)(unsigned)(0.5F + 0.0254F * FreeImage_GetDotsPerMeterY(dib));
		pEncoder->SetResolution(pEncoder, resX, resY);

		// write pixels
		// --------------

		// dib coordinates are upside-down relative to usual conventions
		bIsFlipped = FreeImage_FlipVertical(dib);

		// get a pointer to dst pixel data
		BYTE *dib_bits = FreeImage_GetBits(dib);

		// get dst pitch (count of BYTE for stride)
		const unsigned cbStride = FreeImage_GetPitch(dib);

		// write pixels on output
		error_code = pEncoder->WritePixels(pEncoder, height, dib_bits, cbStride);
		JXR_CHECK(error_code);

		// recover dib coordinates
		FreeImage_FlipVertical(dib);

		// free the encoder
		pEncoder->Release(&pEncoder);
		assert(pEncoder == NULL);
		
		return TRUE;

	} catch (const char *message) {
		if(bIsFlipped) {
			// recover dib coordinates
			FreeImage_FlipVertical(dib);
		}
		if(pEncoder) {
			// free the encoder
			pEncoder->Release(&pEncoder);
			assert(pEncoder == NULL);
		}
		if(NULL != message) {
			FreeImage_OutputMessageProc(s_format_id, message);
		}
	}

	return FALSE;
}

// ==========================================================
//	 Init
// ==========================================================

void DLL_CALLCONV
InitJXR(Plugin *plugin, int format_id) {
	s_format_id = format_id;

	plugin->format_proc = Format;
	plugin->description_proc = Description;
	plugin->extension_proc = Extension;
	plugin->regexpr_proc = RegExpr;
	plugin->open_proc = Open;
	plugin->close_proc = Close;
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


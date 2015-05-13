// ==========================================================
// GetType
//
// Design and implementation by
// - Floris van den Berg (flvdberg@wxs.nl)
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
#include "FreeImageIO.h"
#include "Plugin.h"
#include "../DeprecationManager/DeprecationMgr.h"

// ----------------------------------------------------------

FREE_IMAGE_FORMAT DLL_CALLCONV
FreeImage_GetFileTypeFromHandle(FreeImageIO *io, fi_handle handle, int size) {
	if (handle != NULL) {
		int fif_count = FreeImage_GetFIFCount();

		for (int i = 0; i < fif_count; ++i) {
			FREE_IMAGE_FORMAT fif = (FREE_IMAGE_FORMAT)i;
			if (FreeImage_Validate(fif, io, handle)) {
				if(fif == FIF_TIFF) {
					// many camera raw files use a TIFF signature ...
					// ... try to revalidate against FIF_RAW (even if it breaks the code genericity)
					if (FreeImage_Validate(FIF_RAW, io, handle)) {
						return FIF_RAW;
					}
				}
				return fif;
			}
		}
	}

	return FIF_UNKNOWN;
}

FREE_IMAGE_FORMAT DLL_CALLCONV
FreeImage_GetFileType(const char *filename, int size) {
	FreeImageIO io;
	SetDefaultIO(&io);
	
	FILE *handle = fopen(filename, "rb");

	if (handle != NULL) {
		FREE_IMAGE_FORMAT format = FreeImage_GetFileTypeFromHandle(&io, (fi_handle)handle, size);

		fclose(handle);

		return format;
	}

	return FIF_UNKNOWN;
}

FREE_IMAGE_FORMAT DLL_CALLCONV 
FreeImage_GetFileTypeU(const wchar_t *filename, int size) {
#ifdef _WIN32	
	FreeImageIO io;
	SetDefaultIO(&io);
	FILE *handle = _wfopen(filename, L"rb");

	if (handle != NULL) {
		FREE_IMAGE_FORMAT format = FreeImage_GetFileTypeFromHandle(&io, (fi_handle)handle, size);

		fclose(handle);

		return format;
	}
#endif
	return FIF_UNKNOWN;
}


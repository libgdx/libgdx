// ==========================================================
// Memory Input/Output functions
//
// Design and implementation by
// - Ryan Rubley <ryan@lostreality.org> 
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
#include "FreeImageIO.h"

// =====================================================================


// =====================================================================
// Open and close a memory handle
// =====================================================================

FIMEMORY * DLL_CALLCONV 
FreeImage_OpenMemory(BYTE *data, DWORD size_in_bytes) {
	// allocate a memory handle
	FIMEMORY *stream = (FIMEMORY*)malloc(sizeof(FIMEMORY));
	if(stream) {
		stream->data = (BYTE*)malloc(sizeof(FIMEMORYHEADER));

		if(stream->data) {
			FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(stream->data);

			// initialize the memory header
			memset(mem_header, 0, sizeof(FIMEMORYHEADER));
			
			if(data && size_in_bytes) {
				// wrap a user buffer
				mem_header->delete_me = FALSE;
				mem_header->data = (BYTE*)data;
				mem_header->datalen = mem_header->filelen = size_in_bytes;
			} else {
				mem_header->delete_me = TRUE;
			}

			return stream;
		}
		free(stream);
	}

	return NULL;
}


void DLL_CALLCONV
FreeImage_CloseMemory(FIMEMORY *stream) {
	if(stream && stream->data) {
		FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(stream->data);
		if(mem_header->delete_me) {
			free(mem_header->data);
		}
		free(mem_header);
		free(stream);
	}
}

// =====================================================================
// Memory stream load/save functions
// =====================================================================

FIBITMAP * DLL_CALLCONV
FreeImage_LoadFromMemory(FREE_IMAGE_FORMAT fif, FIMEMORY *stream, int flags) {
	if (stream && stream->data) {
		FreeImageIO io;
		SetMemoryIO(&io);

		return FreeImage_LoadFromHandle(fif, &io, (fi_handle)stream, flags);
	}

	return NULL;
}


BOOL DLL_CALLCONV
FreeImage_SaveToMemory(FREE_IMAGE_FORMAT fif, FIBITMAP *dib, FIMEMORY *stream, int flags) {
	if (stream) {
		FreeImageIO io;
		SetMemoryIO(&io);

		FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(stream->data);

		if(mem_header->delete_me == TRUE) {
			return FreeImage_SaveToHandle(fif, dib, &io, (fi_handle)stream, flags);
		} else {
			// do not save in a user buffer
			FreeImage_OutputMessageProc(fif, "Memory buffer is read only");
		}
	}

	return FALSE;
}

// =====================================================================
// Memory stream buffer access
// =====================================================================

BOOL DLL_CALLCONV
FreeImage_AcquireMemory(FIMEMORY *stream, BYTE **data, DWORD *size_in_bytes) {
	if (stream) {
		FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(stream->data);

		*data = (BYTE*)mem_header->data;
		*size_in_bytes = mem_header->filelen;
		return TRUE;
	}

	return FALSE;
}

// =====================================================================
// Memory stream file type access
// =====================================================================

FREE_IMAGE_FORMAT DLL_CALLCONV
FreeImage_GetFileTypeFromMemory(FIMEMORY *stream, int size) {
	FreeImageIO io;
	SetMemoryIO(&io);

	if (stream != NULL) {
		return FreeImage_GetFileTypeFromHandle(&io, (fi_handle)stream, size);
	}

	return FIF_UNKNOWN;
}

// =====================================================================
// Seeking in Memory stream
// =====================================================================

/**
Moves the memory pointer to a specified location
@param stream Pointer to FIMEMORY structure
@param offset Number of bytes from origin
@param origin Initial position
@return Returns TRUE if successful, returns FALSE otherwise
*/
BOOL DLL_CALLCONV
FreeImage_SeekMemory(FIMEMORY *stream, long offset, int origin) {
	FreeImageIO io;
	SetMemoryIO(&io);

	if (stream != NULL) {
		int success = io.seek_proc((fi_handle)stream, offset, origin);
		return (success == 0) ? TRUE : FALSE;
	}

	return FALSE;
}

/**
Gets the current position of a memory pointer
@param stream Target FIMEMORY structure
@return Returns the current file position if successful, -1 otherwise
*/
long DLL_CALLCONV
FreeImage_TellMemory(FIMEMORY *stream) {
	FreeImageIO io;
	SetMemoryIO(&io);

	if (stream != NULL) {
		return io.tell_proc((fi_handle)stream);
	}

	return -1L;
}

// =====================================================================
// Reading or Writing in Memory stream
// =====================================================================

/**
Reads data from a memory stream
@param buffer Storage location for data
@param size Item size in bytes
@param count Maximum number of items to be read
@param stream Pointer to FIMEMORY structure
@return Returns the number of full items actually read, which may be less than count if an error occurs
*/
unsigned DLL_CALLCONV 
FreeImage_ReadMemory(void *buffer, unsigned size, unsigned count, FIMEMORY *stream) {
	FreeImageIO io;
	SetMemoryIO(&io);

	if (stream != NULL) {
		return io.read_proc(buffer, size, count, stream);
	}

	return 0;
}

/**
Writes data to a memory stream.
@param buffer Pointer to data to be written
@param size Item size in bytes
@param count Maximum number of items to be written
@param stream Pointer to FIMEMORY structure
@return Returns the number of full items actually written, which may be less than count if an error occurs
*/
unsigned DLL_CALLCONV 
FreeImage_WriteMemory(const void *buffer, unsigned size, unsigned count, FIMEMORY *stream) {
	if (stream != NULL) {
		FreeImageIO io;
		SetMemoryIO(&io);

		FIMEMORYHEADER *mem_header = (FIMEMORYHEADER*)(((FIMEMORY*)stream)->data);

		if(mem_header->delete_me == TRUE) {
			return io.write_proc((void *)buffer, size, count, stream);
		} else {
			// do not write in a user buffer
			FreeImage_OutputMessageProc(FIF_UNKNOWN, "Memory buffer is read only");
		}
	}

	return 0;
}


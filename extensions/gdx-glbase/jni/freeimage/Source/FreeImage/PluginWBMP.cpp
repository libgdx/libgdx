// ==========================================================
// Wireless Bitmap Format Loader and Writer
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

#include "FreeImage.h"
#include "Utilities.h"

// ----------------------------------------------------------
// Wireless Bitmap Format
// ----------------------
// The WBMP format enables graphical information to be sent to a variety of handsets.
// The WBMP format is terminal independent and describes only graphical information.

// IMPLEMENTATION NOTES:
// ------------------------
// The WBMP format is configured according to a type field value (TypeField below),
// which maps to all relevant image encoding information, such as:
// · Pixel organisation and encoding
// · Palette organisation and encoding
// · Compression characteristics
// · Animation encoding
// For each TypeField value, all relevant image characteristics are 
// fully specified as part of the WAP documentation.
// Currently, a simple compact, monochrome image format is defined
// within the WBMP type space :
//
// Image Type Identifier, multi-byte integer	0
// Image Format description						0 B/W, no compression
// -------------------------------------------------------------------------------

// WBMP Header

#ifdef _WIN32
#pragma pack(push, 1)
#else
#pragma pack(1)
#endif

typedef struct tagWBMPHEADER {
	WORD TypeField;			// Image type identifier of multi-byte length
	BYTE FixHeaderField;	// Octet of general header information
	BYTE ExtHeaderFields;	// Zero or more extension header fields
	WORD Width;				// Multi-byte width field
	WORD Height;			// Multi-byte height field
} WBMPHEADER;

#ifdef _WIN32
#pragma pack(pop)
#else
#pragma pack()
#endif

// The extension headers may be of type binary 00 through binary 11, defined as follows.

// - Type 00 indicates a multi-byte bitfield used to specify additional header information.
// The first bit is set if a type 00, extension header is set if more data follows.
//  The other bits are reserved for future use.
// - Type 01 - reserved for future use.
// - Type 10 - reserved for future use.
// - Type 11 indicates a sequence of parameter/value pairs. These can be used for 
// optimisations and special purpose extensions, eg, animation image formats.
// The parameter size tells the length (1-8 bytes) of the following parameter name.
// The value size gives the length (1-16 bytes) of the following parameter value.
// The concatenation flag indicates whether another parameter/value pair will follow
// after reading the specified bytes of data.

// ==========================================================
// Internal functions
// ==========================================================

static DWORD
multiByteRead(FreeImageIO *io, fi_handle handle) {
	// Multi-byte encoding / decoding
	// -------------------------------
	// A multi-byte integer consists of a series of octets, where the most significant bit
	// is the continuation flag, and the remaining seven bits are a scalar value.
	// The continuation flag is used to indicate that an octet is not the end of the multi-byte
	// sequence.

	DWORD Out = 0;
	BYTE In = 0;

	while (io->read_proc(&In, 1, 1, handle)) {
		Out += (In & 0x7F);

		if ((In & 0x80) == 0x00)
			break;

		Out <<= 7;
	}

	return Out;
}

static void
multiByteWrite(FreeImageIO *io, fi_handle handle, DWORD In) {
	BYTE Out, k = 1;
  
	while (In & (0x7F << 7*k))
		k++;
  
	while (k > 1) {
		k--;

		Out = (BYTE)(0x80 | (In >> 7*k) & 0xFF);

		io->write_proc(&Out, 1, 1, handle);
	}

	Out = (BYTE)(In & 0x7F);

	io->write_proc(&Out, 1, 1, handle);
}

static void
readExtHeader(FreeImageIO *io, fi_handle handle, BYTE b) {
    // Extension header fields
    // ------------------------
    // Read the extension header fields
    // (since we don't use them for the moment, we skip them).

	switch (b & 0x60) {
		// Type 00: read multi-byte bitfield

		case 0x00:
		{
			DWORD info = multiByteRead(io, handle);
			break;
		}		

		// Type 11: read a sequence of parameter/value pairs.

		case 0x60:
		{
			BYTE sizeParamIdent = (b & 0x70) >> 4;	// Size of Parameter Identifier (in bytes)
			BYTE sizeParamValue = (b & 0x0F);		// Size of Parameter Value (in bytes)
			
			BYTE *Ident = (BYTE*)malloc(sizeParamIdent * sizeof(BYTE));
			BYTE *Value = (BYTE*)malloc(sizeParamValue * sizeof(BYTE));
		
			io->read_proc(Ident, sizeParamIdent, 1, handle);
			io->read_proc(Value, sizeParamValue, 1, handle);
			
			free(Ident);
			free(Value);
			break;
		}		

		// reserved for future use

		case 0x20:	// Type 01
		case 0x40:	// Type 10
			break;
	}
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
	return "WBMP";
}

static const char * DLL_CALLCONV
Description() {
	return "Wireless Bitmap";
}

static const char * DLL_CALLCONV
Extension() {
	return "wap,wbmp,wbm";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/vnd.wap.wbmp";
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
		(depth == 1)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (type == FIT_BITMAP) ? TRUE : FALSE;
}

// ----------------------------------------------------------

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	WORD x, y, width, height;
	FIBITMAP *dib;
    BYTE *bits;		// pointer to dib data
	RGBQUAD *pal;	// pointer to dib palette

	WBMPHEADER header;

	if (handle) {
		try {
			// Read header information
			// -----------------------

			// Type

			header.TypeField = (WORD)multiByteRead(io, handle);

			if (header.TypeField != 0) {
				throw FI_MSG_ERROR_UNSUPPORTED_FORMAT;
			}

			// FixHeaderField

			io->read_proc(&header.FixHeaderField, 1, 1, handle);

			// ExtHeaderFields
			// 1 = more will follow, 0 = last octet

			if (header.FixHeaderField & 0x80) {
				header.ExtHeaderFields = 0x80;

				while(header.ExtHeaderFields & 0x80) {
					io->read_proc(&header.ExtHeaderFields, 1, 1, handle);

					readExtHeader(io, handle, header.ExtHeaderFields);
				}
			}

			// width & height

			width  = (WORD)multiByteRead(io, handle);
			height = (WORD)multiByteRead(io, handle);

			// Allocate a new dib

			dib = FreeImage_Allocate(width, height, 1);
			if (!dib) {
				throw FI_MSG_ERROR_DIB_MEMORY;
			}

			// write the palette data

			pal = FreeImage_GetPalette(dib);
			pal[0].rgbRed = pal[0].rgbGreen = pal[0].rgbBlue = 0;
			pal[1].rgbRed = pal[1].rgbGreen = pal[1].rgbBlue = 255;

			// read the bitmap data
			
			int line = FreeImage_GetLine(dib);

			for (y = 0; y < height; y++) {
				bits = FreeImage_GetScanLine(dib, height - 1 - y);

				for (x = 0; x < line; x++) {
					io->read_proc(&bits[x], 1, 1, handle);
				}
			}

			return dib;

		} catch(const char *text)  {
			FreeImage_OutputMessageProc(s_format_id, text);

			return NULL;
		}

	}

	return NULL;
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
    BYTE *bits;	// pointer to dib data

	if ((dib) && (handle)) {
		try {
			if (FreeImage_GetBPP(dib) != 1)
				throw "Only 1-bit depth bitmaps can be saved as WBMP";

			// write the header

			WBMPHEADER header;
			header.TypeField = 0;								// Type 0: B/W, no compression
			header.FixHeaderField = 0;							// No ExtHeaderField
			header.Width = (WORD)FreeImage_GetWidth(dib);		// Image width
			header.Height = (WORD)FreeImage_GetHeight(dib);		// Image height

			multiByteWrite(io, handle, header.TypeField);
			
			io->write_proc(&header.FixHeaderField, 1, 1, handle);

			multiByteWrite(io, handle, header.Width);
			multiByteWrite(io, handle, header.Height);

			// write the bitmap data

			WORD linelength = (WORD)FreeImage_GetLine(dib);

			for (WORD y = 0; y < header.Height; y++) {
				bits = FreeImage_GetScanLine(dib, header.Height - 1 - y);

				io->write_proc(&bits[0], linelength, 1, handle);
			}

			return TRUE;

		} catch (const char* text) {
			FreeImage_OutputMessageProc(s_format_id, text);
		}
	}

	return FALSE;
}

// ==========================================================
//   Init
// ==========================================================

void DLL_CALLCONV
InitWBMP(Plugin *plugin, int format_id) {
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
	plugin->validate_proc = NULL;
	plugin->mime_proc = MimeType;
	plugin->supports_export_bpp_proc = SupportsExportDepth;
	plugin->supports_export_type_proc = SupportsExportType;
	plugin->supports_icc_profiles_proc = NULL;
}

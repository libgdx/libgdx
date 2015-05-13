// ==========================================================
// Google WebP Loader & Writer
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

#include "../LibWebP/src/webp/decode.h"
#include "../LibWebP/src/webp/encode.h"
#include "../LibWebP/src/enc/vp8enci.h"
#include "../LibWebP/src/webp/mux.h"

// ==========================================================
// Plugin Interface
// ==========================================================

static int s_format_id;

// ----------------------------------------------------------
//   Helpers for the load function
// ----------------------------------------------------------

/**
Read the whole file into memory
*/
static BOOL
ReadFileToWebPData(FreeImageIO *io, fi_handle handle, WebPData * const bitstream) {
  uint8_t *raw_data = NULL;

  try {
	  // Read the input file and put it in memory
	  long start_pos = io->tell_proc(handle);
	  io->seek_proc(handle, 0, SEEK_END);
	  size_t file_length = (size_t)(io->tell_proc(handle) - start_pos);
	  io->seek_proc(handle, start_pos, SEEK_SET);
	  raw_data = (uint8_t*)malloc(file_length * sizeof(uint8_t));
	  if(!raw_data) {
		  throw FI_MSG_ERROR_MEMORY;
	  }
	  if(io->read_proc(raw_data, 1, (unsigned)file_length, handle) != file_length) {
		  throw "Error while reading input stream";
	  }
	  
	  // copy pointers (must be released later using free)
	  bitstream->bytes = raw_data;
	  bitstream->size = file_length;

	  return TRUE;

  } catch(const char *text) {
	  if(raw_data) {
		  free(raw_data);
	  }
	  memset(bitstream, 0, sizeof(WebPData));
	  if(NULL != text) {
		  FreeImage_OutputMessageProc(s_format_id, text);
	  }
	  return FALSE;
  }
}

// ----------------------------------------------------------
//   Helpers for the save function
// ----------------------------------------------------------

/**
Output function. Should return 1 if writing was successful.
data/data_size is the segment of data to write, and 'picture' is for
reference (and so one can make use of picture->custom_ptr).
*/
static int 
WebP_MemoryWriter(const BYTE *data, size_t data_size, const WebPPicture* const picture) {
	FIMEMORY *hmem = (FIMEMORY*)picture->custom_ptr;
	return data_size ? (FreeImage_WriteMemory(data, 1, (unsigned)data_size, hmem) == data_size) : 0;
}

// ==========================================================
// Plugin Implementation
// ==========================================================

static const char * DLL_CALLCONV
Format() {
	return "WebP";
}

static const char * DLL_CALLCONV
Description() {
	return "Google WebP image format";
}

static const char * DLL_CALLCONV
Extension() {
	return "webp";
}

static const char * DLL_CALLCONV
RegExpr() {
	return NULL;
}

static const char * DLL_CALLCONV
MimeType() {
	return "image/webp";
}

static BOOL DLL_CALLCONV
Validate(FreeImageIO *io, fi_handle handle) {
	BYTE riff_signature[4] = { 0x52, 0x49, 0x46, 0x46 };
	BYTE webp_signature[4] = { 0x57, 0x45, 0x42, 0x50 };
	BYTE signature[12] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	io->read_proc(signature, 1, 12, handle);

	if(memcmp(riff_signature, signature, 4) == 0) {
		if(memcmp(webp_signature, signature + 8, 4) == 0) {
			return TRUE;
		}
	}

	return FALSE;
}

static BOOL DLL_CALLCONV
SupportsExportDepth(int depth) {
	return (
		(depth == 24) || 
		(depth == 32)
		);
}

static BOOL DLL_CALLCONV 
SupportsExportType(FREE_IMAGE_TYPE type) {
	return (type == FIT_BITMAP) ? TRUE : FALSE;
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

static void * DLL_CALLCONV
Open(FreeImageIO *io, fi_handle handle, BOOL read) {
	WebPMux *mux = NULL;
	int copy_data = 1;	// 1 : copy data into the mux, 0 : keep a link to local data

	if(read) {
		// create the MUX object from the input stream
		WebPData bitstream;
		// read the input file and put it in memory
		if(!ReadFileToWebPData(io, handle, &bitstream)) {
			return NULL;
		}
		// create the MUX object
		mux = WebPMuxCreate(&bitstream, copy_data);
		// no longer needed since copy_data == 1
		free((void*)bitstream.bytes);
		if(mux == NULL) {
			FreeImage_OutputMessageProc(s_format_id, "Failed to create mux object from file");
			return NULL;
		}
	} else {
		// creates an empty mux object
		mux = WebPMuxNew();
		if(mux == NULL) {
			FreeImage_OutputMessageProc(s_format_id, "Failed to create empty mux object");
			return NULL;
		}
	}
	
	return mux;
}

static void DLL_CALLCONV
Close(FreeImageIO *io, fi_handle handle, void *data) {
	WebPMux *mux = (WebPMux*)data;
	if(mux != NULL) {
		// free the MUX object
		WebPMuxDelete(mux);
	}
}

// ----------------------------------------------------------

/**
Decode a WebP image and returns a FIBITMAP image
@param webp_image Raw WebP image
@param flags FreeImage load flags
@return Returns a dib if successfull, returns NULL otherwise
*/
static FIBITMAP *
DecodeImage(WebPData *webp_image, int flags) {
	FIBITMAP *dib = NULL;

	const uint8_t* data = webp_image->bytes;	// raw image data
	const size_t data_size = webp_image->size;	// raw image size

    VP8StatusCode webp_status = VP8_STATUS_OK;

	BOOL header_only = (flags & FIF_LOAD_NOPIXELS) == FIF_LOAD_NOPIXELS;

	// Main object storing the configuration for advanced decoding
	WebPDecoderConfig decoder_config;
	// Output buffer
	WebPDecBuffer* const output_buffer = &decoder_config.output;
	// Features gathered from the bitstream
	WebPBitstreamFeatures* const bitstream = &decoder_config.input;

	try {
		// Initialize the configuration as empty
		// This function must always be called first, unless WebPGetFeatures() is to be called
		if(!WebPInitDecoderConfig(&decoder_config)) {
			throw "Library version mismatch";
		}

		// Retrieve features from the bitstream
		// The bitstream structure is filled with information gathered from the bitstream
		webp_status = WebPGetFeatures(data, data_size, bitstream);
		if(webp_status != VP8_STATUS_OK) {
			throw FI_MSG_ERROR_PARSING;
		}

		// Allocate output dib

		unsigned bpp = bitstream->has_alpha ? 32 : 24;	
		unsigned width = (unsigned)bitstream->width;
		unsigned height = (unsigned)bitstream->height;

		dib = FreeImage_AllocateHeader(header_only, width, height, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK);
		if(!dib) {
			throw FI_MSG_ERROR_DIB_MEMORY;
		}

		if(header_only) {
			WebPFreeDecBuffer(output_buffer);
			return dib;
		}

		// --- Set decoding options ---

		// use multi-threaded decoding
		decoder_config.options.use_threads = 1;
		// set output color space
		output_buffer->colorspace = bitstream->has_alpha ? MODE_BGRA : MODE_BGR;

		// ---

		// decode the input stream, taking 'config' into account. 
		
		webp_status = WebPDecode(data, data_size, &decoder_config);
		if(webp_status != VP8_STATUS_OK) {
			throw FI_MSG_ERROR_PARSING;
		}

		// fill the dib with the decoded data

		const BYTE *src_bitmap = output_buffer->u.RGBA.rgba;
		const unsigned src_pitch = (unsigned)output_buffer->u.RGBA.stride;

		switch(bpp) {
			case 24:
				for(unsigned y = 0; y < height; y++) {
					const BYTE *src_bits = src_bitmap + y * src_pitch;						
					BYTE *dst_bits = (BYTE*)FreeImage_GetScanLine(dib, height-1-y);
					for(unsigned x = 0; x < width; x++) {
						dst_bits[FI_RGBA_BLUE]	= src_bits[0];	// B
						dst_bits[FI_RGBA_GREEN]	= src_bits[1];	// G
						dst_bits[FI_RGBA_RED]	= src_bits[2];	// R
						src_bits += 3;
						dst_bits += 3;
					}
				}
				break;
			case 32:
				for(unsigned y = 0; y < height; y++) {
					const BYTE *src_bits = src_bitmap + y * src_pitch;						
					BYTE *dst_bits = (BYTE*)FreeImage_GetScanLine(dib, height-1-y);
					for(unsigned x = 0; x < width; x++) {
						dst_bits[FI_RGBA_BLUE]	= src_bits[0];	// B
						dst_bits[FI_RGBA_GREEN]	= src_bits[1];	// G
						dst_bits[FI_RGBA_RED]	= src_bits[2];	// R
						dst_bits[FI_RGBA_ALPHA]	= src_bits[3];	// A
						src_bits += 4;
						dst_bits += 4;
					}
				}
				break;
		}

		// Free the decoder
		WebPFreeDecBuffer(output_buffer);

		return dib;

	} catch (const char *text) {
		if(dib) {
			FreeImage_Unload(dib);
		}
		WebPFreeDecBuffer(output_buffer);

		if(NULL != text) {
			FreeImage_OutputMessageProc(s_format_id, text);
		}

		return NULL;
	}
}

static FIBITMAP * DLL_CALLCONV
Load(FreeImageIO *io, fi_handle handle, int page, int flags, void *data) {
	WebPMux *mux = NULL;
	WebPMuxFrameInfo webp_frame = { 0 };	// raw image
	WebPData color_profile;	// ICC raw data
	WebPData xmp_metadata;	// XMP raw data
	WebPData exif_metadata;	// EXIF raw data
	FIBITMAP *dib = NULL;
	WebPMuxError error_status;

	if(!handle) {
		return NULL;
	}

	try {
		// get the MUX object
		mux = (WebPMux*)data;
		if(!mux) {
			throw (1);
		}
		
		// gets the feature flags from the mux object
		uint32_t webp_flags = 0;
		error_status = WebPMuxGetFeatures(mux, &webp_flags);
		if(error_status != WEBP_MUX_OK) {
			throw (1);
		}

		// get image data
		error_status = WebPMuxGetFrame(mux, 1, &webp_frame);

		if(error_status == WEBP_MUX_OK) {
			// decode the data (can be limited to the header if flags uses FIF_LOAD_NOPIXELS)
			dib = DecodeImage(&webp_frame.bitstream, flags);
			if(!dib) {
				throw (1);
			}
			
			// get ICC profile
			if(webp_flags & ICCP_FLAG) {
				error_status = WebPMuxGetChunk(mux, "ICCP", &color_profile);
				if(error_status == WEBP_MUX_OK) {
					FreeImage_CreateICCProfile(dib, (void*)color_profile.bytes, (long)color_profile.size);
				}
			}

			// get XMP metadata
			if(webp_flags & XMP_FLAG) {
				error_status = WebPMuxGetChunk(mux, "XMP ", &xmp_metadata);
				if(error_status == WEBP_MUX_OK) {
					// create a tag
					FITAG *tag = FreeImage_CreateTag();
					if(tag) {
						FreeImage_SetTagKey(tag, g_TagLib_XMPFieldName);
						FreeImage_SetTagLength(tag, (DWORD)xmp_metadata.size);
						FreeImage_SetTagCount(tag, (DWORD)xmp_metadata.size);
						FreeImage_SetTagType(tag, FIDT_ASCII);
						FreeImage_SetTagValue(tag, xmp_metadata.bytes);
						
						// store the tag
						FreeImage_SetMetadata(FIMD_XMP, dib, FreeImage_GetTagKey(tag), tag);

						// destroy the tag
						FreeImage_DeleteTag(tag);
					}
				}
			}

			// get Exif metadata
			if(webp_flags & EXIF_FLAG) {
				error_status = WebPMuxGetChunk(mux, "EXIF", &exif_metadata);
				if(error_status == WEBP_MUX_OK) {
					// read the Exif raw data as a blob
					jpeg_read_exif_profile_raw(dib, exif_metadata.bytes, (unsigned)exif_metadata.size);
					// read and decode the Exif data
					jpeg_read_exif_profile(dib, exif_metadata.bytes, (unsigned)exif_metadata.size);
				}
			}
		}

		WebPDataClear(&webp_frame.bitstream);

		return dib;

	} catch(int) {
		WebPDataClear(&webp_frame.bitstream);
		return NULL;
	}
}

// --------------------------------------------------------------------------

/**
Encode a FIBITMAP to a WebP image
@param hmem Memory output stream, containing on return the encoded image
@param dib The FIBITMAP to encode
@param flags FreeImage save flags
@return Returns TRUE if successfull, returns FALSE otherwise
*/
static BOOL
EncodeImage(FIMEMORY *hmem, FIBITMAP *dib, int flags) {
	WebPPicture picture;	// Input buffer
	WebPConfig config;		// Coding parameters

	BOOL bIsFlipped = FALSE;

	try {
		const unsigned width = FreeImage_GetWidth(dib);
		const unsigned height = FreeImage_GetHeight(dib);
		const unsigned bpp = FreeImage_GetBPP(dib);
		const unsigned pitch = FreeImage_GetPitch(dib);

		// check image type
		FREE_IMAGE_TYPE image_type = FreeImage_GetImageType(dib);

		if( !((image_type == FIT_BITMAP) && ((bpp == 24) || (bpp == 32))) )  {
			throw FI_MSG_ERROR_UNSUPPORTED_FORMAT;
		}

		// check format limits
		if(MAX(width, height) > WEBP_MAX_DIMENSION) {
			FreeImage_OutputMessageProc(s_format_id, "Unsupported image size: width x height = %d x %d", width, height);
			return FALSE;
		}

		// Initialize output I/O
		if(WebPPictureInit(&picture) == 1) {
			picture.writer = WebP_MemoryWriter;
			picture.custom_ptr = hmem;
			picture.width = (int)width;
			picture.height = (int)height;
		} else {
			throw "Couldn't initialize WebPPicture";
		}

		// --- Set encoding parameters ---

		// Initialize encoding parameters to default values
		WebPConfigInit(&config);

		// quality/speed trade-off (0=fast, 6=slower-better)
		config.method = 6;

		if((flags & WEBP_LOSSLESS) == WEBP_LOSSLESS) {
			// lossless encoding
			config.lossless = 1;
			picture.use_argb = 1;
		} else if((flags & 0x7F) > 0) {
			// lossy encoding
			config.lossless = 0;
			// quality is between 1 (smallest file) and 100 (biggest) - default to 75
			config.quality = (float)(flags & 0x7F);
			if(config.quality > 100) {
				config.quality = 100;
			}
		}

		// validate encoding parameters
		if(WebPValidateConfig(&config) == 0) {
			throw "Failed to initialize encoder";
		}

		// --- Perform encoding ---
		
		// Invert dib scanlines
		bIsFlipped = FreeImage_FlipVertical(dib);


		// convert dib buffer to output stream

		const BYTE *bits = FreeImage_GetBits(dib);

#if FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR
		switch(bpp) {
			case 24:
				WebPPictureImportBGR(&picture, bits, pitch);
				break;
			case 32:
				WebPPictureImportBGRA(&picture, bits, pitch);
				break;
		}
#else
		switch(bpp) {
			case 24:
				WebPPictureImportRGB(&picture, bits, pitch);
				break;
			case 32:
				WebPPictureImportRGBA(&picture, bits, pitch);
				break;
		}

#endif // FREEIMAGE_COLORORDER == FREEIMAGE_COLORORDER_BGR

		if(!WebPEncode(&config, &picture)) {
			throw "Failed to encode image";
		}

		WebPPictureFree(&picture);

		if(bIsFlipped) {
			// invert dib scanlines
			FreeImage_FlipVertical(dib);
		}

		return TRUE;

	} catch (const char* text) {

		WebPPictureFree(&picture);

		if(bIsFlipped) {
			// invert dib scanlines
			FreeImage_FlipVertical(dib);
		}

		if(NULL != text) {
			FreeImage_OutputMessageProc(s_format_id, text);
		}
	}

	return FALSE;
}

static BOOL DLL_CALLCONV
Save(FreeImageIO *io, FIBITMAP *dib, fi_handle handle, int page, int flags, void *data) {
	WebPMux *mux = NULL;
	FIMEMORY *hmem = NULL;
	WebPData webp_image;
	WebPData output_data = { 0 };
	WebPMuxError error_status;

	int copy_data = 1;	// 1 : copy data into the mux, 0 : keep a link to local data

	if(!dib || !handle || !data) {
		return FALSE;
	}

	try {

		// get the MUX object
		mux = (WebPMux*)data;
		if(!mux) {
			return FALSE;
		}

		// --- prepare image data ---

		// encode image as a WebP blob
		hmem = FreeImage_OpenMemory();
		if(!hmem || !EncodeImage(hmem, dib, flags)) {
			throw (1);
		}
		// store the blob into the mux
		BYTE *data = NULL;
		DWORD data_size = 0;
		FreeImage_AcquireMemory(hmem, &data, &data_size);
		webp_image.bytes = data;
		webp_image.size = data_size;
		error_status = WebPMuxSetImage(mux, &webp_image, copy_data);
		// no longer needed since copy_data == 1
		FreeImage_CloseMemory(hmem);
		hmem = NULL;
		if(error_status != WEBP_MUX_OK) {
			throw (1);
		}

		// --- set metadata ---
		
		// set ICC color profile
		{
			FIICCPROFILE *iccProfile = FreeImage_GetICCProfile(dib);
			if (iccProfile->size && iccProfile->data) {
				WebPData icc_profile;
				icc_profile.bytes = (uint8_t*)iccProfile->data;
				icc_profile.size = (size_t)iccProfile->size;
				error_status = WebPMuxSetChunk(mux, "ICCP", &icc_profile, copy_data);
				if(error_status != WEBP_MUX_OK) {
					throw (1);
				}
			}
		}

		// set XMP metadata
		{
			FITAG *tag = NULL;
			if(FreeImage_GetMetadata(FIMD_XMP, dib, g_TagLib_XMPFieldName, &tag)) {
				WebPData xmp_profile;
				xmp_profile.bytes = (uint8_t*)FreeImage_GetTagValue(tag);
				xmp_profile.size = (size_t)FreeImage_GetTagLength(tag);
				error_status = WebPMuxSetChunk(mux, "XMP ", &xmp_profile, copy_data);
				if(error_status != WEBP_MUX_OK) {
					throw (1);
				}
			}
		}

		// set Exif metadata
		{
			FITAG *tag = NULL;
			if(FreeImage_GetMetadata(FIMD_EXIF_RAW, dib, g_TagLib_ExifRawFieldName, &tag)) {
				WebPData exif_profile;
				exif_profile.bytes = (uint8_t*)FreeImage_GetTagValue(tag);
				exif_profile.size = (size_t)FreeImage_GetTagLength(tag);
				error_status = WebPMuxSetChunk(mux, "EXIF", &exif_profile, copy_data);
				if(error_status != WEBP_MUX_OK) {
					throw (1);
				}
			}
		}
		
		// get data from mux in WebP RIFF format
		error_status = WebPMuxAssemble(mux, &output_data);
		if(error_status != WEBP_MUX_OK) {
			FreeImage_OutputMessageProc(s_format_id, "Failed to create webp output file");
			throw (1);
		}

		// write the file to the output stream
		if(io->write_proc((void*)output_data.bytes, 1, (unsigned)output_data.size, handle) != output_data.size) {
			FreeImage_OutputMessageProc(s_format_id, "Failed to write webp output file");
			throw (1);
		}

		// free WebP output file
		WebPDataClear(&output_data);

		return TRUE;

	} catch(int) {
		if(hmem) {
			FreeImage_CloseMemory(hmem);
		}
		
		WebPDataClear(&output_data);

		return FALSE;
	}
}

// ==========================================================
//	 Init
// ==========================================================

void DLL_CALLCONV
InitWEBP(Plugin *plugin, int format_id) {
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


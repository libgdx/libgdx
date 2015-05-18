// ==========================================================
// Tag manipulation functions
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

#ifndef FREEIMAGETAG_H
#define FREEIMAGETAG_H

// ==========================================================
// Exif JPEG tags
// ==========================================================

// ----------------------------------------------------------
// TIFF Rev. 6.0 Attribute Information Used in Exif
// ----------------------------------------------------------

// Tags relating to image data structure

#define TAG_IMAGE_WIDTH					0x0100
#define TAG_IMAGE_HEIGHT				0x0101
#define TAG_BITS_PER_SAMPLE				0x0102
#define TAG_COMPRESSION					0x0103
#define TAG_PHOTOMETRIC_INTERPRETATION	0x0106
#define TAG_ORIENTATION					0x0112
#define TAG_SAMPLES_PER_PIXEL			0x0115
#define TAG_PLANAR_CONFIGURATION		0x011C
#define TAG_YCBCR_SUBSAMPLING			0x0212
#define TAG_YCBCR_POSITIONING			0x0213
#define TAG_X_RESOLUTION				0x011A
#define TAG_Y_RESOLUTION				0x011B
#define TAG_RESOLUTION_UNIT				0x0128

// LibTIF compression modes

#define	    TAG_COMPRESSION_NONE		1	/* dump mode */
#define	    TAG_COMPRESSION_CCITTRLE	2	/* CCITT modified Huffman RLE */
#define	    TAG_COMPRESSION_CCITTFAX3	3	/* CCITT Group 3 fax encoding */
#define     TAG_COMPRESSION_CCITT_T4        3       /* CCITT T.4 (TIFF 6 name) */
#define	    TAG_COMPRESSION_CCITTFAX4	4	/* CCITT Group 4 fax encoding */
#define     TAG_COMPRESSION_CCITT_T6        4       /* CCITT T.6 (TIFF 6 name) */
#define	    TAG_COMPRESSION_LZW		5       /* Lempel-Ziv  & Welch */
#define	    TAG_COMPRESSION_OJPEG		6	/* !6.0 JPEG */
#define	    TAG_COMPRESSION_JPEG		7	/* %JPEG DCT compression */
#define	    TAG_COMPRESSION_NEXT		32766	/* NeXT 2-bit RLE */
#define	    TAG_COMPRESSION_CCITTRLEW	32771	/* #1 w/ word alignment */
#define	    TAG_COMPRESSION_PACKBITS	32773	/* Macintosh RLE */
#define	    TAG_COMPRESSION_THUNDERSCAN	32809	/* ThunderScan RLE */
/* codes 32895-32898 are reserved for ANSI IT8 TIFF/IT <dkelly@apago.com) */
#define	    TAG_COMPRESSION_IT8CTPAD	32895   /* IT8 CT w/padding */
#define	    TAG_COMPRESSION_IT8LW		32896   /* IT8 Linework RLE */
#define	    TAG_COMPRESSION_IT8MP		32897   /* IT8 Monochrome picture */
#define	    TAG_COMPRESSION_IT8BL		32898   /* IT8 Binary line art */
/* compression codes 32908-32911 are reserved for Pixar */
#define     TAG_COMPRESSION_PIXARFILM	32908   /* Pixar companded 10bit LZW */
#define	    TAG_COMPRESSION_PIXARLOG	32909   /* Pixar companded 11bit ZIP */
#define	    TAG_COMPRESSION_DEFLATE		32946	/* Deflate compression */
#define     TAG_COMPRESSION_ADOBE_DEFLATE   8       /* Deflate compression,
						   as recognized by Adobe */
/* compression code 32947 is reserved for Oceana Matrix <dev@oceana.com> */
#define     TAG_COMPRESSION_DCS             32947   /* Kodak DCS encoding */
#define	    TAG_COMPRESSION_JBIG		34661	/* ISO JBIG */
#define     TAG_COMPRESSION_SGILOG		34676	/* SGI Log Luminance RLE */
#define     TAG_COMPRESSION_SGILOG24	34677	/* SGI Log 24-bit packed */
#define     TAG_COMPRESSION_JP2000          34712   /* Leadtools JPEG2000 */
#define	    TAG_COMPRESSION_LZMA		34925	/* LZMA2 */

// Tags relating to recording offset

#define TAG_STRIP_OFFSETS					0x0111
#define TAG_ROWS_PER_STRIP					0x0116
#define TAG_STRIP_BYTE_COUNTS				0x0117
#define TAG_JPEG_INTERCHANGE_FORMAT			0x0201
#define TAG_JPEG_INTERCHANGE_FORMAT_LENGTH	0x0202

// Tags relating to image data characteristics

#define TAG_TRANSFER_FUNCTION		0x012D
#define TAG_WHITE_POINT				0x013E
#define TAG_PRIMARY_CHROMATICITIES	0x013F
#define TAG_YCBCR_COEFFICIENTS		0x0211
#define TAG_REFERENCE_BLACK_WHITE	0x0214

// Other tags

#define TAG_DATETIME 				0x0132
#define TAG_IMAGE_DESCRIPTION 		0x010E
#define TAG_MAKE 					0x010F
#define TAG_MODEL 					0x0110
#define TAG_SOFTWARE 				0x0131
#define TAG_ARTIST 					0x013B
#define TAG_COPYRIGHT 				0x8298

// ----------------------------------------------------------
// Exif IFD Attribute Information
// ----------------------------------------------------------

// Tags relating to version

#define TAG_EXIF_VERSION 			0x9000
#define TAG_FLASHPIX_VERSION 		0xA000

// Tag relating to image data characteristics

#define TAG_COLOR_SPACE 			0xA001

// Tags relating to image configuration

#define TAG_COMPONENTS_CONFIGURATION	0x9101
#define TAG_COMPRESSED_BITS_PER_PIXEL	0x9102
#define TAG_PIXEL_X_DIMENSION			0xA002
#define TAG_PIXEL_Y_DIMENSION			0xA003

// Tags relating to user information

#define TAG_MARKER_NOTE		0x927C
#define TAG_USER_COMMENT	0x9286
    
// Tag relating to related file information

#define TAG_RELATED_SOUND_FILE			0xA004

// Tags relating to date and time

#define TAG_DATETIME_ORIGINAL			0x9003
#define TAG_DATETIME_DIGITIZED			0x9004
#define TAG_SUBSECOND_TIME				0x9290
#define TAG_SUBSECOND_TIME_ORIGINAL		0x9291
#define TAG_SUBSECOND_TIME_DIGITIZED	0x9292

// Tags relating to picture-taking conditions

#define TAG_EXPOSURE_TIME				0x829A
#define TAG_FNUMBER						0x829D
#define TAG_EXPOSURE_PROGRAM			0x8822
#define TAG_SPECTRAL_SENSITIVITY		0x8824
#define TAG_ISO_SPEED_RATINGS 			0x8827
#define TAG_OECF						0x8828
#define TAG_SHUTTER_SPEED_VALUE 		0x9201
#define TAG_APERTURE_VALUE 				0x9202
#define TAG_BRIGHTNESS_VALUE			0x9203
#define TAG_EXPOSURE_BIAS_VALUE 		0x9204
#define TAG_MAX_APERTURE_VALUE 			0x9205
#define TAG_SUBJECT_DISTANCE			0x9206
#define TAG_METERING_MODE				0x9207
#define TAG_LIGHT_SOURCE				0x9208
#define TAG_FLASH						0x9209
#define TAG_FOCAL_LENGTH				0x920A
#define TAG_SUBJECT_AREA				0x9214
#define TAG_FLASH_ENERGY				0xA20B
#define TAG_SPATIAL_FREQ_RESPONSE 		0xA20C
#define TAG_FOCAL_PLANE_X_RES			0xA20E
#define TAG_FOCAL_PLANE_Y_RES			0xA20F
#define TAG_FOCAL_PLANE_UNIT			0xA210
#define TAG_SUBJECT_LOCATION 			0xA214
#define TAG_EXPOSURE_INDEX				0xA215
#define TAG_SENSING_METHOD				0xA217
#define TAG_FILE_SOURCE					0xA300
#define TAG_SCENE_TYPE					0xA301
#define TAG_CFA_PATTERN					0xA302
#define TAG_CUSTOM_RENDERED				0xA401
#define TAG_EXPOSURE_MODE				0xA402
#define TAG_WHITE_BALANCE				0xA403
#define TAG_DIGITAL_ZOOM_RATIO			0xA404
#define TAG_FOCAL_LENGTH_IN_35MM_FILM	0xA405
#define TAG_SCENE_CAPTURE_TYPE			0xA406
#define TAG_GAIN_CONTROL				0xA407
#define TAG_CONTRAST					0xA408
#define TAG_SATURATION					0xA409
#define TAG_SHARPNESS					0xA40A
#define TAG_DEVICE_SETTING_DESCRIPTION	0xA40B
#define TAG_SUBJECT_DISTANCE_RANGE		0xA40C

// Other tags

#define TAG_IMAGE_UNIQUE_ID				0xA420

// ----------------------------------------------------------
// GPS Attribute Information
// ----------------------------------------------------------

#define TAG_GPS_VERSION_ID				0x0000
#define TAG_GPS_LATITUDE_REF			0x0001
#define TAG_GPS_LATITUDE				0x0002
#define TAG_GPS_LONGITUDE_REF			0x0003
#define TAG_GPS_LONGITUDE				0x0004
#define TAG_GPS_ALTITUDE_REF			0x0005
#define TAG_GPS_ALTITUDE				0x0006
#define TAG_GPS_TIME_STAMP				0x0007
#define TAG_GPS_SATELLITES				0x0008
#define TAG_GPS_STATUS					0x0009
#define TAG_GPS_MEASURE_MODE			0x000A
#define TAG_GPS_DOP						0x000B
#define TAG_GPS_SPEED_REF				0x000C
#define TAG_GPS_SPEED					0x000D
#define TAG_GPS_TRACK_REF				0x000E
#define TAG_GPS_TRACK					0x000F
#define TAG_GPS_IMG_DIRECTION_REF		0x0010
#define TAG_GPS_IMG_DIRECTION			0x0011
#define TAG_GPS_MAP_DATUM				0x0012
#define TAG_GPS_DEST_LATITUDE_REF		0x0013
#define TAG_GPS_DEST_LATITUDE			0x0014
#define TAG_GPS_DEST_LONGITUDE_REF		0x0015
#define TAG_GPS_DEST_LONGITUDE			0x0016
#define TAG_GPS_DEST_BEARING_REF		0x0017
#define TAG_GPS_DEST_BEARING			0x0018
#define TAG_GPS_DEST_DISTANCE_REF		0x0019
#define TAG_GPS_DEST_DISTANCE			0x001A
#define TAG_GPS_PROCESSING_METHOD		0x001B
#define TAG_GPS_AREA_INFORMATION		0x001C
#define TAG_GPS_DATE_STAMP				0x001D
#define TAG_GPS_DIFFERENTIAL			0x001E

// ==========================================================
// IPTC/NAA tags
// ==========================================================

#define TAG_RECORD_VERSION					0x0200
#define TAG_CAPTION							0x0278
#define TAG_WRITER							0x027A
#define TAG_HEADLINE						0x0269
#define TAG_SPECIAL_INSTRUCTIONS			0x0228
#define TAG_BY_LINE							0x0250
#define TAG_BY_LINE_TITLE					0x0255
#define TAG_CREDIT							0x026E
#define TAG_SOURCE							0x0273
#define TAG_OBJECT_NAME						0x0205
#define TAG_DATE_CREATED					0x0237
#define TAG_CITY							0x025A
#define TAG_PROVINCE_OR_STATE				0x025F
#define TAG_COUNTRY_OR_PRIMARY_LOCATION		0x0265
#define TAG_ORIGINAL_TRANSMISSION_REFERENCE 0x0267
#define TAG_CATEGORY						0x020F
#define TAG_SUPPLEMENTAL_CATEGORIES			0x0214
#define TAG_URGENCY							0x020A
#define TAG_KEYWORDS						0x0219
#define TAG_COPYRIGHT_NOTICE				0x0274
#define TAG_RELEASE_DATE					0x021E
#define TAG_RELEASE_TIME					0x0223
#define TAG_TIME_CREATED					0x023C
#define TAG_ORIGINATING_PROGRAM				0x0241

// ==========================================================
// GeoTIFF tags
// ==========================================================

// tags 33550 is a private tag registered to SoftDesk, Inc
#define TIFFTAG_GEOPIXELSCALE		33550
// tags 33920-33921 are private tags registered to Intergraph, Inc
#define TIFFTAG_INTERGRAPH_MATRIX	33920
#define TIFFTAG_GEOTIEPOINTS		33922
// tags 34263-34264 are private tags registered to NASA-JPL Carto Group
#define TIFFTAG_JPL_CARTO_IFD		34263
#define TIFFTAG_GEOTRANSMATRIX		34264    /* New Matrix Tag replaces 33920 */
// tags 34735-3438 are private tags registered to SPOT Image, Inc
#define TIFFTAG_GEOKEYDIRECTORY		34735
#define TIFFTAG_GEODOUBLEPARAMS		34736
#define TIFFTAG_GEOASCIIPARAMS		34737

// ==========================================================
// FreeImage Animation tags
// ==========================================================

#define ANIMTAG_LOGICALWIDTH	0x0001
#define ANIMTAG_LOGICALHEIGHT	0x0002
#define ANIMTAG_GLOBALPALETTE	0x0003
#define ANIMTAG_LOOP			0x0004
#define ANIMTAG_FRAMELEFT		0x1001
#define ANIMTAG_FRAMETOP		0x1002
#define ANIMTAG_NOLOCALPALETTE	0x1003
#define ANIMTAG_INTERLACED		0x1004
#define ANIMTAG_FRAMETIME		0x1005
#define ANIMTAG_DISPOSALMETHOD	0x1006

// --------------------------------------------------------------------------
// Helper functions to deal with the FITAG structure
// --------------------------------------------------------------------------

/** 
Describes the tag format descriptor
@param type Tag data type
@return Returns the width of a single element, in bytes
@see FREE_IMAGE_MDTYPE
*/
unsigned FreeImage_TagDataWidth(FREE_IMAGE_MDTYPE type);

// --------------------------------------------------------------------------

/**
	Structure to hold a tag information
*/
typedef struct tagTagInfo {
	WORD tag;			// Tag ID (required)
	char *fieldname;	// Field name (required)
	char *description;	// Field description (may be NULL)
} TagInfo;


/**
Class to hold tag information (based on Meyers’ Singleton).<br>

Sample usage :<br>
<code>
TagLib& s = TagLib::instance();
TagInfo *tag_info = s.getTagInfo(EXIF_MAIN, 0x0100);
</code>

Note on multi-threaded applications : 

The singleton pattern must be carefully constructed in multi-threaded applications. 
If two threads are to execute the creation method at the same time when a singleton 
does not yet exist, they both must check for an instance of the singleton and then 
only one should create the new one.
The classic solution to this problem is to use mutual exclusion on the class that 
indicates that the object is being instantiated.
The FreeImage solution is to instantiate the singleton before any other thread is launched, 
i.e. inside the FreeImage_Initialise function (see Plugin.cpp). 
*/

class TagLib {
public:

	/**
	internal tag info tables registered in TagLib
	*/
	enum MDMODEL {
		UNKNOWN,
		EXIF_MAIN, 
		EXIF_EXIF, 
		EXIF_GPS, 
		EXIF_INTEROP,
		EXIF_MAKERNOTE_CANON,
		EXIF_MAKERNOTE_CASIOTYPE1,
		EXIF_MAKERNOTE_CASIOTYPE2,
		EXIF_MAKERNOTE_FUJIFILM,
		EXIF_MAKERNOTE_KYOCERA,
		EXIF_MAKERNOTE_MINOLTA,
		EXIF_MAKERNOTE_NIKONTYPE1,
		EXIF_MAKERNOTE_NIKONTYPE2,
		EXIF_MAKERNOTE_NIKONTYPE3,
		EXIF_MAKERNOTE_OLYMPUSTYPE1,
		EXIF_MAKERNOTE_PANASONIC,
		EXIF_MAKERNOTE_ASAHI,
		EXIF_MAKERNOTE_PENTAX,
		EXIF_MAKERNOTE_SONY,
		EXIF_MAKERNOTE_SIGMA_SD1,
		EXIF_MAKERNOTE_SIGMA_FOVEON,
		IPTC,
		GEOTIFF,
		ANIMATION
	};

private:

	typedef std::map<WORD, TagInfo*> TAGINFO;
	typedef std::map<int, TAGINFO*>  TABLEMAP;

	/// store hash tables for all known tag info tables
	TABLEMAP _table_map;

private:
	/**
	Constructor (private)<br>
	This is where the tag info tables are initialized.
	@see addMetadataModel
	*/
	TagLib();

	/// Assignement operator (disabled)
	void operator=(TagLib&);

	/// Copy constructor (disabled)
	TagLib(const TagLib&);
	
	/** 
	Used in the constructor to initialize the tag tables
	@param md_model Internal metadata model
	@param tag_table Tag info table
	@return Returns TRUE if successful, returns FALSE otherwise
	*/
	BOOL addMetadataModel(MDMODEL md_model, TagInfo *tag_table);

public:
	/// Destructor
	~TagLib();

	/**
	@return Returns a reference to the TagLib instance
	*/
	static TagLib& instance();

	/**
	Given a tag ID, returns its TagInfo descriptor
	@param md_model Internal metadata model
	@param tagID tag ID
	@return Returns the TagInfo descriptor if successful, returns NULL otherwise
	*/
	const TagInfo* getTagInfo(MDMODEL md_model, WORD tagID);

	/**
	Given a tag ID, returns its tag field name. 
	When the tag is unknown and defaultKey is not NULL, a string such as "Tag 0x1234" is returned. 
	This string is contained in the provided defaultKey buffer (assumed to be an array of at least 16 chars). 
	@param md_model Internal metadata model
	@param tagID tag ID
	@param defaultKey Assumed to be an array of 16 chars. If not NULL, build a key for unknown tags
	@return Returns the tag field name if successful, returns an 'unknown tag' string contained in defaultKey otherwise
	*/
	const char* getTagFieldName(MDMODEL md_model, WORD tagID, char *defaultKey);

	/**
	Given a tag ID, returns its description. 
	When the tag has no description, a NULL value is returned.
	@param md_model Internal metadata model
	@param tagID tag ID
	@return Returns the tag description if successful, returns NULL otherwise
	*/
	const char* getTagDescription(MDMODEL md_model, WORD tagID);

	/**
	Given a tag field name, returns its tag ID. 
	When the tag doesn't exists, a value '-1' is returned.
	@param md_model Internal metadata model
	@param key tag field name
	@return Returns the tag ID if successful, returns -1 otherwise
	*/
	int getTagID(MDMODEL md_model, const char *key);

	/**
	Perform a conversion between internal metadata models and FreeImage public metadata models
	@param md_model Internal metadata model
	*/
	FREE_IMAGE_MDMODEL getFreeImageModel(MDMODEL model);

};

// --------------------------------------------------------------------------
// Constant strings
// --------------------------------------------------------------------------

/// Name of the XMP field
static const char *g_TagLib_XMPFieldName = "XMLPacket";

/// Name of the Exif raw field
static const char *g_TagLib_ExifRawFieldName = "ExifRaw";

// --------------------------------------------------------------------------
// Metadata routines
// --------------------------------------------------------------------------

#if defined(__cplusplus)
extern "C" {
#endif

// JPEG Exif profile
BOOL jpeg_read_exif_profile(FIBITMAP *dib, const BYTE *dataptr, unsigned datalen);
BOOL jpeg_read_exif_profile_raw(FIBITMAP *dib, const BYTE *profile, unsigned length);
BOOL jpegxr_read_exif_profile(FIBITMAP *dib, const BYTE *profile, unsigned length);
BOOL jpegxr_read_exif_gps_profile(FIBITMAP *dib, const BYTE *profile, unsigned length);

// JPEG / TIFF IPTC profile
BOOL read_iptc_profile(FIBITMAP *dib, const BYTE *dataptr, unsigned int datalen);
BOOL write_iptc_profile(FIBITMAP *dib, BYTE **profile, unsigned *profile_size);

#if defined(__cplusplus)
}
#endif


#endif // FREEIMAGETAG_H



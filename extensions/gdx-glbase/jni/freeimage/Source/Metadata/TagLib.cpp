// ==========================================================
// Tag library
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

// ==========================================================
// Implementation notes : 
// ----------------------
// The tag info tables declared in this file should probably 
// be loaded from an XML file. 
// This would allow internationalization features and also 
// more extensibility. 
// Maybe in a future release ? 
// ==========================================================

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif

#include "FreeImage.h"
#include "Utilities.h"
#include "FreeImageTag.h"

/**
 HOW-TO : add a new TagInfo table
 --------------------------------------------------------------------------
 1) add a table identifier in the TagLib class definition (see enum MDMODEL)
 2) declare the tag table as static and use a 0/NULL value as last entry
 3) initialize the table in TagLib::TagLib
 4) provide a conversion in TagLib::getFreeImageModel
*/

// --------------------------------------------------------------------------
// EXIF standard tags definition
// --------------------------------------------------------------------------

static TagInfo
  exif_exif_tag_table[] =
  {
    {  0x0100, (char *) "ImageWidth", (char *) "Image width"},
    {  0x0101, (char *) "ImageLength", (char *) "Image height"},
    {  0x0102, (char *) "BitsPerSample", (char *) "Number of bits per component"},
    {  0x0103, (char *) "Compression", (char *) "Compression scheme"},
    {  0x0106, (char *) "PhotometricInterpretation", (char *) "Pixel composition"},
    {  0x010A, (char *) "FillOrder", (char*) NULL},
    {  0x010D, (char *) "DocumentName", (char *) NULL},
    {  0x010E, (char *) "ImageDescription", (char *) "Image title"},
    {  0x010F, (char *) "Make", (char *) "Image input equipment manufacturer"},
    {  0x0110, (char *) "Model", (char *) "Image input equipment model"},
    {  0x0111, (char *) "StripOffsets", (char *) "Image data location"},
    {  0x0112, (char *) "Orientation", (char *) "Orientation of image"},
    {  0x0115, (char *) "SamplesPerPixel", (char *) "Number of components"},
    {  0x0116, (char *) "RowsPerStrip", (char *) "Number of rows per strip"},
    {  0x0117, (char *) "StripByteCounts", (char *) "Bytes per compressed strip"},
    {  0x011A, (char *) "XResolution", (char *) "Image resolution in width direction"},
    {  0x011B, (char *) "YResolution", (char *) "Image resolution in height direction"},
    {  0x011C, (char *) "PlanarConfiguration", (char *) "Image data arrangement"},
	{  0x011D, (char *) "PageName", (char *) "Name of the page"},
	{  0x011E, (char *) "XPosition", (char *) "X position of the image"},
	{  0x011F, (char *) "YPosition", (char *) "Y position of the image"},
    {  0x0128, (char *) "ResolutionUnit", (char *) "Unit of X and Y resolution"},
	{  0x0129, (char *) "PageNumber", (char *) "Page number"},
    {  0x012D, (char *) "TransferFunction", (char *) "Transfer function"},
    {  0x0131, (char *) "Software", (char *) "Software used"},
    {  0x0132, (char *) "DateTime", (char *) "File change date and time"},
    {  0x013B, (char *) "Artist", (char *) "Person who created the image"},
	{  0x013C, (char *) "HostComputer", (char *) "Host computer used to generate the image"},
    {  0x013E, (char *) "WhitePoint", (char *) "White point chromaticity"},
    {  0x013F, (char *) "PrimaryChromaticities", (char *) "Chromaticities of primaries"},
    {  0x0156, (char *) "TransferRange", (char *) NULL},
    {  0x0200, (char *) "JPEGProc", (char *) NULL},
    {  0x0201, (char *) "JPEGInterchangeFormat", (char *) "Offset to JPEG SOI"},
    {  0x0202, (char *) "JPEGInterchangeFormatLength", (char *) "Bytes of JPEG data"},
    {  0x0211, (char *) "YCbCrCoefficients", (char *) "Color space transformation matrix coefficients"},
    {  0x0212, (char *) "YCbCrSubSampling", (char *) "Subsampling ratio of Y to C"},
    {  0x0213, (char *) "YCbCrPositioning", (char *) "Y and C positioning"},
    {  0x0214, (char *) "ReferenceBlackWhite", (char *) "Pair of black and white reference values"},
    {  0x828D, (char *) "CFARepeatPatternDim", (char *) NULL},
    {  0x828E, (char *) "CFAPattern", (char *) NULL},
    {  0x828F, (char *) "BatteryLevel", (char *) NULL},
    {  0x8298, (char *) "Copyright", (char *) "Copyright holder"},
    {  0x829A, (char *) "ExposureTime", (char *) "Exposure time"},
    {  0x829D, (char *) "FNumber", (char *) "F number"},
    {  0x83BB, (char *) "IPTC/NAA", (char *) NULL},
    {  0x8773, (char *) "InterColorProfile", (char *) NULL},
    {  0x8822, (char *) "ExposureProgram", (char *) "Exposure program"},
    {  0x8824, (char *) "SpectralSensitivity", (char *) "Spectral sensitivity"},
    {  0x8825, (char *) "GPSInfo", (char *) NULL},
    {  0x8827, (char *) "ISOSpeedRatings", (char *) "ISO speed rating"},
    {  0x8828, (char *) "OECF", (char *) "Optoelectric conversion factor"},
    {  0x9000, (char *) "ExifVersion", (char *) "Exif version"},
    {  0x9003, (char *) "DateTimeOriginal", (char *) "Date and time of original data generation"},
    {  0x9004, (char *) "DateTimeDigitized", (char *) "Date and time of digital data generation"},
    {  0x9101, (char *) "ComponentsConfiguration", (char *) "Meaning of each component"},
    {  0x9102, (char *) "CompressedBitsPerPixel", (char *) "Image compression mode"},
    {  0x9201, (char *) "ShutterSpeedValue", (char *) "Shutter speed"},
    {  0x9202, (char *) "ApertureValue", (char *) "Aperture"},
    {  0x9203, (char *) "BrightnessValue", (char *) "Brightness"},
    {  0x9204, (char *) "ExposureBiasValue", (char *) "Exposure bias"},
    {  0x9205, (char *) "MaxApertureValue", (char *) "Maximum lens aperture"},
    {  0x9206, (char *) "SubjectDistance", (char *) "Subject distance"},
    {  0x9207, (char *) "MeteringMode", (char *) "Metering mode"},
    {  0x9208, (char *) "LightSource", (char *) "Light source"},
    {  0x9209, (char *) "Flash", (char *) "Flash"},
    {  0x920A, (char *) "FocalLength", (char *) "Lens focal length"},
	{  0x9214, (char *) "SubjectArea", (char *) "Subject area"},
    {  0x927C, (char *) "MakerNote", (char *) "Manufacturer notes"},
    {  0x9286, (char *) "UserComment", (char *) "User comments"},
    {  0x9290, (char *) "SubSecTime", (char *) "DateTime subseconds"},
    {  0x9291, (char *) "SubSecTimeOriginal", (char *) "DateTimeOriginal subseconds"},
    {  0x9292, (char *) "SubSecTimeDigitized", (char *) "DateTimeDigitized subseconds"},
    {  0xA000, (char *) "FlashPixVersion", (char *) "Supported Flashpix version"},
    {  0xA001, (char *) "ColorSpace", (char *) "Color space information"},
    {  0xA002, (char *) "PixelXDimension", (char *) "Valid image width"},
    {  0xA003, (char *) "PixelYDimension", (char *) "Valid image height"},
    {  0xA004, (char *) "RelatedSoundFile", (char *) "Related audio file"},
    {  0xA005, (char *) "InteroperabilityOffset", (char *) NULL},
    {  0xA20B, (char *) "FlashEnergy", (char *) "Flash energy"},
    {  0xA20C, (char *) "SpatialFrequencyResponse", (char *) "Spatial frequency response"},
    {  0xA20E, (char *) "FocalPlaneXResolution", (char *) "Focal plane X resolution"},
    {  0xA20F, (char *) "FocalPlaneYResolution", (char *) "Focal plane Y resolution"},
    {  0xA210, (char *) "FocalPlaneResolutionUnit", (char *) "Focal plane resolution unit"},
    {  0xA214, (char *) "SubjectLocation", (char *) "Subject location"},
    {  0xA215, (char *) "ExposureIndex", (char *) "Exposure index"},
    {  0xA217, (char *) "SensingMethod", (char *) "Sensing method"},
    {  0xA300, (char *) "FileSrc", (char *) "File source"},
    {  0xA301, (char *) "SceneType", (char *) "Scene type"},
    {  0xA302, (char *) "CFAPattern", (char *) "CFA pattern"},
    {  0xA401, (char *) "CustomRendered", (char *) "Custom image processing"},
    {  0xA402, (char *) "ExposureMode", (char *) "Exposure mode"},
    {  0xA403, (char *) "WhiteBalance", (char *) "White balance"},
    {  0xA404, (char *) "DigitalZoomRatio", (char *) "Digital zoom ratio"},
    {  0xA405, (char *) "FocalLengthIn35mmFilm", (char *) "Focal length in 35 mm film"},
    {  0xA406, (char *) "SceneCaptureType", (char *) "Scene capture type"},
    {  0xA407, (char *) "GainControl", (char *) "Gain control"},
    {  0xA408, (char *) "Contrast", (char *) "Contrast"},
    {  0xA409, (char *) "Saturation", (char *) "Saturation"},
    {  0xA40A, (char *) "Sharpness", (char *) "Sharpness"},
    {  0xA40B, (char *) "DeviceSettingDescription", (char *) "Device settings description"},
    {  0xA40C, (char *) "SubjectDistanceRange", (char *) "Subject distance range"},
    {  0xA420, (char *) "ImageUniqueID", (char *) "Unique image ID"},
    {  0xA430, (char *) "CameraOwnerName", (char *) "Camera owner name"},
    {  0xA431, (char *) "BodySerialNumber", (char *) "Body serial number"},
    {  0xA432, (char *) "LensSpecification", (char *) "Lens specification"},
    {  0xA433, (char *) "LensMake", (char *) "Lens make"},
    {  0xA434, (char *) "LensModel", (char *) "Lens model"},
    {  0xA435, (char *) "LensSerialNumber", (char *) "Lens serial number"},

	// These tags are not part of the Exiv v2.3 specifications but are often loaded by applications as Exif data
	{  0x4746, (char *) "Rating", (char *) "Rating tag used by Windows"},
	{  0x4749, (char *) "RatingPercent", (char *) "Rating tag used by Windows, value in percent"},
	{  0x9C9B, (char *) "XPTitle", (char *) "Title tag used by Windows, encoded in UCS2"},
	{  0x9C9C, (char *) "XPComment", (char *) "Comment tag used by Windows, encoded in UCS2"},
	{  0x9C9D, (char *) "XPAuthor", (char *) "Author tag used by Windows, encoded in UCS2"},
	{  0x9C9E, (char *) "XPKeywords", (char *) "Keywords tag used by Windows, encoded in UCS2"},
	{  0x9C9F, (char *) "XPSubject", (char *) "Subject tag used by Windows, encoded in UCS2"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// EXIF GPS tags definition
// --------------------------------------------------------------------------

static TagInfo
  exif_gps_tag_table[] =
  {
    {  0x0000, (char *) "GPSVersionID", (char *) "GPS tag version"},
	{  0x0001, (char *) "GPSLatitudeRef", (char *) "North or South Latitude"},
    {  0x0002, (char *) "GPSLatitude", (char *) "Latitude"},
    {  0x0003, (char *) "GPSLongitudeRef", (char *) "East or West Longitude"},
    {  0x0004, (char *) "GPSLongitude", (char *) "Longitude"},
    {  0x0005, (char *) "GPSAltitudeRef", (char *) "Altitude reference"},
    {  0x0006, (char *) "GPSAltitude", (char *) "Altitude"},
    {  0x0007, (char *) "GPSTimeStamp", (char *) "GPS time (atomic clock)"},
    {  0x0008, (char *) "GPSSatellites", (char *) "GPS satellites used for measurement"},
    {  0x0009, (char *) "GPSStatus", (char *) "GPS receiver status"},
    {  0x000A, (char *) "GPSMeasureMode", (char *) "GPS measurement mode"},
    {  0x000B, (char *) "GPSDOP", (char *) "Measurement precision"},
    {  0x000C, (char *) "GPSSpeedRef", (char *) "Speed unit"},
    {  0x000D, (char *) "GPSSpeed", (char *) "Speed of GPS receiver"},
    {  0x000E, (char *) "GPSTrackRef", (char *) "Reference for direction of movement"},
    {  0x000F, (char *) "GPSTrack", (char *) "Direction of movement"},
    {  0x0010, (char *) "GPSImgDirectionRef", (char *) "Reference for direction of image"},
    {  0x0011, (char *) "GPSImgDirection", (char *) "Direction of image"},
    {  0x0012, (char *) "GPSMapDatum", (char *) "Geodetic survey data used"},
    {  0x0013, (char *) "GPSDestLatitudeRef", (char *) "Reference for latitude of destination"},
    {  0x0014, (char *) "GPSDestLatitude", (char *) "Latitude of destination"},
    {  0x0015, (char *) "GPSDestLongitudeRef", (char *) "Reference for longitude of destination"},
    {  0x0016, (char *) "GPSDestLongitude", (char *) "Longitude of destination"},
    {  0x0017, (char *) "GPSDestBearingRef", (char *) "Reference for bearing of destination"},
    {  0x0018, (char *) "GPSDestBearing", (char *) "Bearing of destination"},
    {  0x0019, (char *) "GPSDestDistanceRef", (char *) "Reference for distance to destination"},
    {  0x001A, (char *) "GPSDestDistance", (char *) "Distance to destination"},
    {  0x001B, (char *) "GPSProcessingMethod", (char *) "Name of GPS processing method"},
    {  0x001C, (char *) "GPSAreaInformation", (char *) "Name of GPS area"},
    {  0x001D, (char *) "GPSDateStamp", (char *) "GPS date"},
    {  0x001E, (char *) "GPSDifferential", (char *) "GPS differential correction"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// EXIF interoperability tags definition
// --------------------------------------------------------------------------

static TagInfo
  exif_interop_tag_table[] =
  {
    {  0x0001, (char *) "InteroperabilityIndex", (char *) "Interoperability Identification"},
    {  0x0002, (char *) "InteroperabilityVersion", (char *) "Interoperability version"},
    {  0x1000, (char *) "RelatedImageFileFormat", (char *) "File format of image file"},
    {  0x1001, (char *) "RelatedImageWidth", (char *) "Image width"},
    {  0x1002, (char *) "RelatedImageLength", (char *) "Image height"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// EXIF maker note tags definition
// --------------------------------------------------------------------------

/**
Canon maker note
*/
static TagInfo
  exif_canon_tag_table[] =
  {
    {  0x0001, (char *) "CanonCameraSettings", (char *) "Canon CameraSettings Tags"},
    {  0x0002, (char *) "CanonFocalLength", (char *) "Canon FocalLength Tags"},
    {  0x0003, (char *) "CanonFlashInfo?", (char *) NULL},
    {  0x0004, (char *) "CanonShotInfo", (char *) "Canon ShotInfo Tags"},
    {  0x0005, (char *) "CanonPanorama", (char *) "Canon Panorama Tags"},
    {  0x0006, (char *) "CanonImageType", (char *) NULL},
    {  0x0007, (char *) "CanonFirmwareVersion", (char *) NULL},
    {  0x0008, (char *) "FileNumber", (char *) NULL},
    {  0x0009, (char *) "OwnerName", (char *) NULL},
    {  0x000A, (char *) "UnknownD30", (char *) "Canon UnknownD30 Tags"},
    {  0x000C, (char *) "SerialNumber", (char *) NULL},
    {  0x000D, (char *) "CanonCameraInfo", (char *) "Canon CameraInfo Tags"},
    {  0x000E, (char *) "CanonFileLength", (char *) NULL},
    {  0x000F, (char *) "CanonCustomFunctions", (char *) "Custom Functions"},
    {  0x0010, (char *) "CanonModelID", (char *) NULL},
    {  0x0012, (char *) "CanonAFInfo", (char *) "Canon AFInfo Tags"},
    {  0x0013, (char *) "ThumbnailImageValidArea", (char *) NULL},
    {  0x0015, (char *) "SerialNumberFormat", (char *) NULL},
    {  0x001A, (char *) "SuperMacro", (char *) NULL},
    {  0x001C, (char *) "DateStampMode", (char *) NULL},
    {  0x001D, (char *) "MyColors", (char *) NULL},
    {  0x001E, (char *) "FirmwareRevision", (char *) NULL},
    {  0x0023, (char *) "Categories", (char *) NULL},
    {  0x0024, (char *) "FaceDetect1", (char *) NULL},
    {  0x0025, (char *) "FaceDetect2", (char *) NULL},
    {  0x0026, (char *) "CanonAFInfo2", (char *) "Canon AFInfo2 Tags"},
    {  0x0028, (char *) "ImageUniqueID", (char *) NULL},
    {  0x0081, (char *) "RawDataOffset", (char *) NULL},
    {  0x0083, (char *) "OriginalDecisionDataOffset", (char *) NULL},
    {  0x0090, (char *) "CustomFunctions1D", (char *) "CanonCustom Functions1D Tags"},
    {  0x0091, (char *) "PersonalFunctions", (char *) "CanonCustom PersonalFuncs Tags"},
    {  0x0092, (char *) "PersonalFunctionValues", (char *) "CanonCustom PersonalFuncValues Tags"},
    {  0x0093, (char *) "CanonFileInfo", (char *) "Canon FileInfo Tags"},
    {  0x0094, (char *) "AFPointsInFocus1D", (char *) NULL},
    {  0x0095, (char *) "LensModel", (char *) NULL},
    {  0x0096, (char *) "SerialInfo", (char *) NULL},
    {  0x0097, (char *) "DustRemovalData", (char *) NULL},
	{  0x0098, (char *) "CropInfo", (char *) NULL},
    {  0x0099, (char *) "CustomFunctions2", (char *) NULL},
	{  0x009A, (char *) "AspectInfo", (char *) NULL},
    {  0x00A0, (char *) "ProcessingInfo", (char *) NULL},
    {  0x00A1, (char *) "ToneCurveTable", (char *) NULL},
    {  0x00A2, (char *) "SharpnessTable", (char *) NULL},
    {  0x00A3, (char *) "SharpnessFreqTable", (char *) NULL},
    {  0x00A4, (char *) "WhiteBalanceTable", (char *) NULL},
    {  0x00A9, (char *) "ColorBalance", (char *) NULL},
    {  0x00AA, (char *) "MeasuredColor", (char *) NULL},
    {  0x00AE, (char *) "ColorTemperature", (char *) NULL},
    {  0x00B0, (char *) "CanonFlags", (char *) NULL},
    {  0x00B1, (char *) "ModifiedInfo", (char *) NULL},
    {  0x00B2, (char *) "ToneCurveMatching", (char *) NULL},
    {  0x00B3, (char *) "WhiteBalanceMatching", (char *) NULL},
    {  0x00B4, (char *) "ColorSpace", (char *) NULL},
    {  0x00B6, (char *) "PreviewImageInfo", (char *) NULL},
    {  0x00D0, (char *) "VRDOffset", (char *) "Offset of VRD 'recipe data' if it exists"},
    {  0x00E0, (char *) "SensorInfo", (char *) NULL},
    {  0x4001, (char *) "ColorData", (char *) "Canon ColorData Tags"},
    {  0x4002, (char *) "CRWParam?", (char *) NULL},
    {  0x4003, (char *) "ColorInfo", (char *) NULL},
    {  0x4005, (char *) "Flavor?", (char *) NULL},
    {  0x4008, (char *) "BlackLevel?", (char *) NULL},
	{  0x4010, (char *) "CustomPictureStyleFileName", (char *) NULL},
    {  0x4013, (char *) "AFMicroAdj", (char *) NULL},
    {  0x4015, (char *) "VignettingCorr", (char *) NULL},
    {  0x4016, (char *) "VignettingCorr2", (char *) NULL},
    {  0x4018, (char *) "LightingOpt", (char *) NULL},
	{  0x4019, (char *) "LensInfo", (char *) NULL},
	{  0x4020, (char *) "AmbienceInfo", (char *) NULL},
	{  0x4024, (char *) "FilterInfo", (char *) NULL},

	// These 'sub'-tag values have been created for consistency -- they don't exist within the exif segment

	// Fields under tag 0x0001 (we add 0xC100 to make unique tag id)
    {  0xC100 + 1, (char *) "CameraSettings:MacroMode", (char *) NULL},
    {  0xC100 + 2, (char *) "CameraSettings:SelfTimer", (char *) NULL},
    {  0xC100 + 3, (char *) "CameraSettings:Quality", (char *) NULL},
    {  0xC100 + 4, (char *) "CameraSettings:CanonFlashMode", (char *) NULL},
    {  0xC100 + 5, (char *) "CameraSettings:ContinuousDrive", (char *) NULL},
	{  0xC100 + 6, (char *) "CameraSettings:0x0006", (char *) NULL},
    {  0xC100 + 7, (char *) "CameraSettings:FocusMode", (char *) NULL},
	{  0xC100 + 8, (char *) "CameraSettings:0x0008", (char *) NULL},
    {  0xC100 + 9, (char *) "CameraSettings:RecordMode", (char *) NULL},
    {  0xC100 + 10, (char *) "CameraSettings:CanonImageSize", (char *) NULL},
    {  0xC100 + 11, (char *) "CameraSettings:EasyMode", (char *) NULL},
    {  0xC100 + 12, (char *) "CameraSettings:DigitalZoom", (char *) NULL},
    {  0xC100 + 13, (char *) "CameraSettings:Contrast", (char *) NULL},
    {  0xC100 + 14, (char *) "CameraSettings:Saturation", (char *) NULL},
    {  0xC100 + 15, (char *) "CameraSettings:Sharpness", (char *) NULL},
    {  0xC100 + 16, (char *) "CameraSettings:CameraISO", (char *) NULL},
    {  0xC100 + 17, (char *) "CameraSettings:MeteringMode", (char *) NULL},
    {  0xC100 + 18, (char *) "CameraSettings:FocusRange", (char *) NULL},
    {  0xC100 + 19, (char *) "CameraSettings:AFPoint", (char *) NULL},
    {  0xC100 + 20, (char *) "CameraSettings:CanonExposureMode", (char *) NULL},
	{  0xC100 + 21, (char *) "CameraSettings:0x0015", (char *) NULL},
    {  0xC100 + 22, (char *) "CameraSettings:LensType", (char *) NULL},
    {  0xC100 + 23, (char *) "CameraSettings:LongFocal", (char *) NULL},
    {  0xC100 + 24, (char *) "CameraSettings:ShortFocal", (char *) NULL},
    {  0xC100 + 25, (char *) "CameraSettings:FocalUnits", (char *) "Focal Units per mm"},
    {  0xC100 + 26, (char *) "CameraSettings:MaxAperture", (char *) NULL},
    {  0xC100 + 27, (char *) "CameraSettings:MinAperture", (char *) NULL},
    {  0xC100 + 28, (char *) "CameraSettings:FlashActivity", (char *) NULL},
    {  0xC100 + 29, (char *) "CameraSettings:FlashBits", (char *) NULL},
	{  0xC100 + 30, (char *) "CameraSettings:0x001E", (char *) NULL},
	{  0xC100 + 31, (char *) "CameraSettings:0x001F", (char *) NULL},
    {  0xC100 + 32, (char *) "CameraSettings:FocusContinuous", (char *) NULL},
    {  0xC100 + 33, (char *) "CameraSettings:AESetting", (char *) NULL},
    {  0xC100 + 34, (char *) "CameraSettings:ImageStabilization", (char *) NULL},
    {  0xC100 + 35, (char *) "CameraSettings:DisplayAperture", (char *) NULL},
    {  0xC100 + 36, (char *) "CameraSettings:ZoomSourceWidth", (char *) NULL},
    {  0xC100 + 37, (char *) "CameraSettings:ZoomTargetWidth", (char *) NULL},
	{  0xC100 + 38, (char *) "CameraSettings:0x0026", (char *) NULL},
    {  0xC100 + 39, (char *) "CameraSettings:SpotMeteringMode", (char *) NULL},
    {  0xC100 + 40, (char *) "CameraSettings:PhotoEffect", (char *) NULL},
    {  0xC100 + 41, (char *) "CameraSettings:ManualFlashOutput", (char *) NULL},
    {  0xC100 + 42, (char *) "CameraSettings:ColorTone", (char *) NULL},
	{  0xC100 + 43, (char *) "CameraSettings:0x002B", (char *) NULL},
	{  0xC100 + 44, (char *) "CameraSettings:0x002C", (char *) NULL},
	{  0xC100 + 45, (char *) "CameraSettings:0x002D", (char *) NULL},
    {  0xC100 + 46, (char *) "CameraSettings:SRAWQuality", (char *) NULL},
	{  0xC100 + 47, (char *) "CameraSettings:0x002F", (char *) NULL},
	{  0xC100 + 48, (char *) "CameraSettings:0x0030", (char *) NULL},

	// Fields under tag 0x0002 (we add 0xC200 to make unique tag id)
    {  0xC200 + 0, (char *) "FocalLength:FocalType", (char *) NULL},
    {  0xC200 + 1, (char *) "FocalLength:FocalLength", (char *) NULL},
    {  0xC200 + 2, (char *) "FocalLength:FocalPlaneXSize", (char *) NULL},
    {  0xC200 + 3, (char *) "FocalLength:FocalPlaneYSize", (char *) NULL},
	
	// Fields under tag 0x0004 (we add 0xC400 to make unique tag id)
    {  0xC400 + 1, (char *) "ShotInfo:AutoISO", (char *) NULL},
	{  0xC400 + 2, (char *) "ShotInfo:BaseISO", (char *) NULL},
	{  0xC400 + 3, (char *) "ShotInfo:MeasuredEV", (char *) NULL},
    {  0xC400 + 4, (char *) "ShotInfo:TargetAperture", (char *) NULL},
    {  0xC400 + 5, (char *) "ShotInfo:TargetExposureTime", (char *) NULL},
    {  0xC400 + 6, (char *) "ShotInfo:ExposureCompensation", (char *) NULL},
    {  0xC400 + 7, (char *) "ShotInfo:WhiteBalance", (char *) NULL},
    {  0xC400 + 8, (char *) "ShotInfo:SlowShutter", (char *) NULL},
    {  0xC400 + 9, (char *) "ShotInfo:SequenceNumber", (char *) NULL},
    {  0xC400 + 10, (char *) "ShotInfo:OpticalZoomCode", (char *) NULL},
	{  0xC400 + 11, (char *) "ShotInfo:0x000B", (char *) NULL},
	{  0xC400 + 12, (char *) "ShotInfo:CameraTemperature", (char *) NULL},
    {  0xC400 + 13, (char *) "ShotInfo:FlashGuideNumber", (char *) NULL},
    {  0xC400 + 14, (char *) "ShotInfo:AFPointsInFocus", (char *) NULL},
    {  0xC400 + 15, (char *) "ShotInfo:FlashExposureComp", (char *) NULL},
    {  0xC400 + 16, (char *) "ShotInfo:AutoExposureBracketing", (char *) NULL},
    {  0xC400 + 17, (char *) "ShotInfo:AEBBracketValue", (char *) NULL},
    {  0xC400 + 18, (char *) "ShotInfo:ControlMode", (char *) NULL},
    {  0xC400 + 19, (char *) "ShotInfo:FocusDistanceUpper", (char *) NULL},
    {  0xC400 + 20, (char *) "ShotInfo:FocusDistanceLower", (char *) NULL},
    {  0xC400 + 21, (char *) "ShotInfo:FNumber", (char *) NULL},
    {  0xC400 + 22, (char *) "ShotInfo:ExposureTime", (char *) NULL},
    {  0xC400 + 23, (char *) "ShotInfo:MeasuredEV2", (char *) NULL},
    {  0xC400 + 24, (char *) "ShotInfo:BulbDuration", (char *) NULL},
	{  0xC400 + 25, (char *) "ShotInfo:0x0019", (char *) NULL},
    {  0xC400 + 26, (char *) "ShotInfo:CameraType", (char *) NULL},
    {  0xC400 + 27, (char *) "ShotInfo:AutoRotate", (char *) NULL},
    {  0xC400 + 28, (char *) "ShotInfo:NDFilter", (char *) NULL},
    {  0xC400 + 29, (char *) "ShotInfo:SelfTimer2", (char *) NULL},
	{  0xC400 + 30, (char *) "ShotInfo:0x001E", (char *) NULL},
	{  0xC400 + 31, (char *) "ShotInfo:0x001F", (char *) NULL},
	{  0xC400 + 32, (char *) "ShotInfo:0x0020", (char *) NULL},
    {  0xC400 + 33, (char *) "ShotInfo:FlashOutput", (char *) NULL},

	// Fields under tag 0x0012 (we add 0x1200 to make unique tag id)
    {  0x1200 + 0, (char *) "AFInfo:NumAFPoints", (char *) NULL},
    {  0x1200 + 1, (char *) "AFInfo:ValidAFPoints", (char *) NULL},
    {  0x1200 + 2, (char *) "AFInfo:CanonImageWidth", (char *) NULL},
    {  0x1200 + 3, (char *) "AFInfo:CanonImageHeight", (char *) NULL},
    {  0x1200 + 4, (char *) "AFInfo:AFImageWidth", (char *) NULL},
    {  0x1200 + 5, (char *) "AFInfo:AFImageHeight", (char *) NULL},
    {  0x1200 + 6, (char *) "AFInfo:AFAreaWidth", (char *) NULL},
    {  0x1200 + 7, (char *) "AFInfo:AFAreaHeight", (char *) NULL},
    {  0x1200 + 8, (char *) "AFInfo:AFAreaXPositions", (char *) NULL},
    {  0x1200 + 9, (char *) "AFInfo:AFAreaYPositions", (char *) NULL},
    {  0x1200 + 10, (char *) "AFInfo:AFPointsInFocus", (char *) NULL},
    {  0x1200 + 11, (char *) "AFInfo:PrimaryAFPoint?", (char *) NULL},
    {  0x1200 + 12, (char *) "AFInfo:PrimaryAFPoint", (char *) NULL},
	{  0x1200 + 13, (char *) "AFInfo:0x000D", (char *) NULL},
	{  0x1200 + 14, (char *) "AFInfo:0x000E", (char *) NULL},
	{  0x1200 + 15, (char *) "AFInfo:0x000F", (char *) NULL},
	{  0x1200 + 16, (char *) "AFInfo:0x0010", (char *) NULL},
	{  0x1200 + 17, (char *) "AFInfo:0x0011", (char *) NULL},
	{  0x1200 + 18, (char *) "AFInfo:0x0012", (char *) NULL},
	{  0x1200 + 19, (char *) "AFInfo:0x0013", (char *) NULL},
	{  0x1200 + 20, (char *) "AFInfo:0x0014", (char *) NULL},
	{  0x1200 + 21, (char *) "AFInfo:0x0015", (char *) NULL},
	{  0x1200 + 22, (char *) "AFInfo:0x0016", (char *) NULL},
	{  0x1200 + 23, (char *) "AFInfo:0x0017", (char *) NULL},
	{  0x1200 + 24, (char *) "AFInfo:0x0018", (char *) NULL},
	{  0x1200 + 25, (char *) "AFInfo:0x0019", (char *) NULL},
	{  0x1200 + 26, (char *) "AFInfo:0x001A", (char *) NULL},
	{  0x1200 + 27, (char *) "AFInfo:0x001B", (char *) NULL},

	// Fields under tag 0x00A0 (we add 0xCA00 to make unique tag id)
    {  0xCA00 + 1, (char *) "ProcessingInfo:ToneCurve", (char *) NULL},
    {  0xCA00 + 2, (char *) "ProcessingInfo:Sharpness", (char *) NULL},
    {  0xCA00 + 3, (char *) "ProcessingInfo:SharpnessFrequency", (char *) NULL},
    {  0xCA00 + 4, (char *) "ProcessingInfo:SensorRedLevel", (char *) NULL},
    {  0xCA00 + 5, (char *) "ProcessingInfo:SensorBlueLevel", (char *) NULL},
    {  0xCA00 + 6, (char *) "ProcessingInfo:WhiteBalanceRed", (char *) NULL},
    {  0xCA00 + 7, (char *) "ProcessingInfo:WhiteBalanceBlue", (char *) NULL},
    {  0xCA00 + 8, (char *) "ProcessingInfo:WhiteBalance", (char *) NULL},
    {  0xCA00 + 9, (char *) "ProcessingInfo:ColorTemperature", (char *) NULL},
    {  0xCA00 + 10, (char *) "ProcessingInfo:PictureStyle", (char *) NULL},
    {  0xCA00 + 11, (char *) "ProcessingInfo:DigitalGain", (char *) NULL},
    {  0xCA00 + 12, (char *) "ProcessingInfo:WBShiftAB", (char *) NULL},
    {  0xCA00 + 13, (char *) "ProcessingInfo:WBShiftGM", (char *) NULL},

	// Fields under tag 0x00E0 (we add 0xCE00 to make unique tag id)
    {  0xCE00 + 1, (char *) "SensorInfo:SensorWidth", (char *) NULL},
    {  0xCE00 + 2, (char *) "SensorInfo:SensorHeight", (char *) NULL},
	{  0xCE00 + 3, (char *) "SensorInfo:0x0003", (char *) NULL},
	{  0xCE00 + 4, (char *) "SensorInfo:0x0004", (char *) NULL},
    {  0xCE00 + 5, (char *) "SensorInfo:SensorLeftBorder", (char *) NULL},
    {  0xCE00 + 6, (char *) "SensorInfo:SensorTopBorder", (char *) NULL},
    {  0xCE00 + 7, (char *) "SensorInfo:SensorRightBorder", (char *) NULL},
    {  0xCE00 + 8, (char *) "SensorInfo:SensorBottomBorder", (char *) NULL},
    {  0xCE00 + 9, (char *) "SensorInfo:BlackMaskLeftBorder", (char *) NULL},
    {  0xCE00 + 10, (char *) "SensorInfo:BlackMaskTopBorder", (char *) NULL},
    {  0xCE00 + 11, (char *) "SensorInfo:BlackMaskRightBorder", (char *) NULL},
    {  0xCE00 + 12, (char *) "SensorInfo:BlackMaskBottomBorder", (char *) NULL},
	{  0xCE00 + 13, (char *) "SensorInfo:0x000D", (char *) NULL},
	{  0xCE00 + 14, (char *) "SensorInfo:0x000E", (char *) NULL},
	{  0xCE00 + 15, (char *) "SensorInfo:0x000F", (char *) NULL},
	{  0xCE00 + 16, (char *) "SensorInfo:0x0010", (char *) NULL},

    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Casio type 1 maker note
*/
static TagInfo
  exif_casio_type1_tag_table[] =
  {
    {  0x0001, (char *) "RecordingMode", (char *) NULL},
    {  0x0002, (char *) "Quality", (char *) NULL},
    {  0x0003, (char *) "FocusMode", (char *) NULL},
    {  0x0004, (char *) "FlashMode", (char *) NULL},
    {  0x0005, (char *) "FlashIntensity", (char *) NULL},
    {  0x0006, (char *) "ObjectDistance", (char *) NULL},
    {  0x0007, (char *) "WhiteBalance", (char *) NULL},
    {  0x000A, (char *) "DigitalZoom", (char *) NULL},
    {  0x000B, (char *) "Sharpness", (char *) NULL},
    {  0x000C, (char *) "Contrast", (char *) NULL},
    {  0x000D, (char *) "Saturation", (char *) NULL},
    {  0x0014, (char *) "ISO", (char *) NULL},
	{  0x0015, (char *) "FirmwareDate", (char *) NULL},
    {  0x0016, (char *) "Enhancement", (char *) NULL},
    {  0x0017, (char *) "ColorFilter", (char *) NULL},
    {  0x0018, (char *) "AFPoint", (char *) NULL},
    {  0x0019, (char *) "FlashIntensity", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Casio type 2 maker note
*/
static TagInfo
  exif_casio_type2_tag_table[] =
  {
    {  0x0002, (char *) "PreviewImageSize", (char *) NULL},
    {  0x0003, (char *) "PreviewImageLength", (char *) NULL},
    {  0x0004, (char *) "PreviewImageStart", (char *) NULL},
    {  0x0008, (char *) "QualityMode", (char *) NULL},
    {  0x0009, (char *) "CasioImageSize", (char *) NULL},
    {  0x000D, (char *) "FocusMode", (char *) NULL},
    {  0x0014, (char *) "ISO", (char *) NULL},
    {  0x0019, (char *) "WhiteBalance", (char *) NULL},
    {  0x001D, (char *) "FocalLength", (char *) NULL},
    {  0x001F, (char *) "Saturation", (char *) NULL},
    {  0x0020, (char *) "Contrast", (char *) NULL},
    {  0x0021, (char *) "Sharpness", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x2000, (char *) "PreviewImage", (char *) NULL},
    {  0x2001, (char *) "FirmwareDate", (char *) NULL},
    {  0x2011, (char *) "WhiteBalanceBias", (char *) NULL},
    {  0x2012, (char *) "WhiteBalance", (char *) NULL},
    {  0x2021, (char *) "AFPointPosition", (char *) NULL},
    {  0x2022, (char *) "ObjectDistance", (char *) NULL},
    {  0x2034, (char *) "FlashDistance", (char *) NULL},
	{  0x2076, (char *) "SpecialEffectMode", (char *) NULL},
    {  0x3000, (char *) "RecordMode", (char *) NULL},
    {  0x3001, (char *) "ReleaseMode", (char *) NULL},
    {  0x3002, (char *) "Quality", (char *) NULL},
    {  0x3003, (char *) "FocusMode", (char *) NULL},
    {  0x3006, (char *) "HometownCity", (char *) NULL},
    {  0x3007, (char *) "BestShotMode", (char *) NULL},
    {  0x3008, (char *) "AutoISO", (char *) NULL},
    {  0x3009, (char *) "AFMode", (char *) NULL},
    {  0x3011, (char *) "Sharpness", (char *) NULL},
    {  0x3012, (char *) "Contrast", (char *) NULL},
    {  0x3013, (char *) "Saturation", (char *) NULL},
    {  0x3014, (char *) "ISO", (char *) NULL},
    {  0x3015, (char *) "ColorMode", (char *) NULL},
    {  0x3016, (char *) "Enhancement", (char *) NULL},
    {  0x3017, (char *) "ColorFilter", (char *) NULL},
	{  0x301B, (char *) "ArtMode", (char *) NULL},
    {  0x301C, (char *) "SequenceNumber", (char *) NULL},
    {  0x301D, (char *) "BracketSequence", (char *) NULL},
    {  0x3020, (char *) "ImageStabilization", (char *) NULL},
    {  0x302A, (char *) "LightingMode", (char *) NULL},
    {  0x302B, (char *) "PortraitRefiner", (char *) NULL},
    {  0x3030, (char *) "SpecialEffectLevel", (char *) NULL},
    {  0x3031, (char *) "SpecialEffectSetting", (char *) NULL},
	{  0x3103, (char *) "DriveMode", (char *) NULL},
	{  0x4001, (char *) "CaptureFrameRate", (char *) NULL},
	{  0x4003, (char *) "VideoQuality", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
FujiFilm maker note
*/
static TagInfo
  exif_fujifilm_tag_table[] =
  {
    {  0x0000, (char *) "Version", (char *) NULL},
    {  0x0010, (char *) "InternalSerialNumber", (char *) NULL},
    {  0x1000, (char *) "Quality", (char *) NULL},
    {  0x1001, (char *) "Sharpness", (char *) NULL},
    {  0x1002, (char *) "WhiteBalance", (char *) NULL},
    {  0x1003, (char *) "Saturation", (char *) NULL},
    {  0x1004, (char *) "Contrast", (char *) NULL},
	{  0x1005, (char *) "ColorTemperature", (char *) NULL},
	{  0x100A, (char *) "WhiteBalanceFineTune", (char *) NULL},
	{  0x100B, (char *) "NoiseReduction", (char *) NULL},
    {  0x1010, (char *) "FujiFlashMode", (char *) NULL},
    {  0x1011, (char *) "FlashExposureComp", (char *) NULL},
    {  0x1020, (char *) "Macro", (char *) NULL},
    {  0x1021, (char *) "FocusMode", (char *) NULL},
    {  0x1023, (char *) "FocusPixel", (char *) NULL},
    {  0x1030, (char *) "SlowSync", (char *) NULL},
    {  0x1031, (char *) "PictureMode", (char *) NULL},
	{  0x1033, (char *) "EXRAuto", (char *) NULL},
	{  0x1034, (char *) "EXRMode", (char *) NULL},
    {  0x1100, (char *) "AutoBracketting", (char *) NULL},
    {  0x1101, (char *) "SequenceNumber", (char *) NULL},
    {  0x1210, (char *) "ColorMode", (char *) NULL},
    {  0x1300, (char *) "BlurWarning", (char *) NULL},
    {  0x1301, (char *) "FocusWarning", (char *) NULL},
    {  0x1302, (char *) "ExposureWarning", (char *) NULL},
    {  0x1400, (char *) "DynamicRange", (char *) NULL},
	{  0x1401, (char *) "FilmMode", (char *) NULL},
	{  0x1402, (char *) "DynamicRangeSetting", (char *) NULL},
	{  0x1403, (char *) "DevelopmentDynamicRange", (char *) NULL},
	{  0x1404, (char *) "MinFocalLength", (char *) NULL},
	{  0x1405, (char *) "MaxFocalLength", (char *) NULL},
	{  0x1406, (char *) "MaxApertureAtMinFocal", (char *) NULL},
	{  0x1407, (char *) "MaxApertureAtMaxFocal", (char *) NULL},
	{  0x4100, (char *) "FacesDetected", (char *) NULL},
	{  0x4103, (char *) "FacePositions", (char *) NULL},
	{  0x8000, (char *) "FileSource", (char *) NULL},
	{  0x8002, (char *) "OrderNumber", (char *) NULL},
	{  0x8003, (char *) "FrameNumber", (char *) NULL},
	{  0xB211, (char *) "Parallax", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Kyocera maker note
*/
static TagInfo
  exif_kyocera_tag_table[] =
  {
    {  0x0001, (char *) "ThumbnailImage", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) "Print Image Matching Info"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Olympus Type 1 / Epson / Agfa maker note
*/
static TagInfo
  exif_olympus_type1_tag_table[] =
  {
    {  0x0000, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x0001, (char *) "MinoltaCameraSettingsOld", (char *) NULL},
    {  0x0003, (char *) "MinoltaCameraSettings", (char *) NULL},
    {  0x0040, (char *) "CompressedImageSize", (char *) NULL},
    {  0x0081, (char *) "PreviewImageData", (char *) NULL},
    {  0x0088, (char *) "PreviewImageStart", (char *) NULL},
    {  0x0089, (char *) "PreviewImageLength", (char *) NULL},
    {  0x0100, (char *) "ThumbnailImage", (char *) NULL},
    {  0x0104, (char *) "BodyFirmwareVersion", (char *) NULL},
    {  0x0200, (char *) "SpecialMode", (char *) NULL},
    {  0x0201, (char *) "Quality", (char *) NULL},
    {  0x0202, (char *) "Macro", (char *) NULL},
    {  0x0203, (char *) "BWMode", (char *) NULL},
    {  0x0204, (char *) "DigitalZoom", (char *) NULL},
    {  0x0205, (char *) "FocalPlaneDiagonal", (char *) NULL},
	{  0x0206, (char *) "LensDistortionParams", (char *) NULL},
    {  0x0207, (char *) "CameraType", (char *) NULL},
    {  0x0208, (char *) "TextInfo", (char *) "Olympus TextInfo Tags"},
    {  0x0209, (char *) "CameraID", (char *) NULL},
    {  0x020B, (char *) "EpsonImageWidth", (char *) NULL},
    {  0x020C, (char *) "EpsonImageHeight", (char *) NULL},
    {  0x020D, (char *) "EpsonSoftware", (char *) NULL},
    {  0x0280, (char *) "PreviewImage", (char *) NULL},
    {  0x0300, (char *) "PreCaptureFrames", (char *) NULL},
	{  0x0301, (char *) "WhiteBoard", (char *) NULL},
    {  0x0302, (char *) "OneTouchWB", (char *) NULL}, 
	{  0x0303, (char *) "WhiteBalanceBracket", (char *) NULL}, 
	{  0x0304, (char *) "WhiteBalanceBias", (char *) NULL}, 
	{  0x0403, (char *) "SceneMode", (char *) NULL}, 
    {  0x0404, (char *) "SerialNumber", (char *) NULL}, 
	{  0x0405, (char *) "Firmware", (char *) NULL}, 
    {  0x0E00, (char *) "PrintIM", (char *) "PrintIM Tags"}, 
	{  0x0F00, (char *) "DataDump", (char *) NULL},
	{  0x0F01, (char *) "DataDump2", (char *) NULL},
	{  0x1000, (char *) "ShutterSpeedValue", (char *) NULL},
	{  0x1001, (char *) "ISOValue", (char *) NULL},
	{  0x1002, (char *) "ApertureValue", (char *) NULL},
	{  0x1003, (char *) "BrightnessValue", (char *) NULL},
	{  0x1004, (char *) "FlashMode", (char *) NULL},
	{  0x1005, (char *) "FlashDevice", (char *) NULL},
	{  0x1006, (char *) "ExposureCompensation", (char *) NULL},
	{  0x1007, (char *) "SensorTemperature", (char *) NULL},
	{  0x1008, (char *) "LensTemperature", (char *) NULL},
	{  0x1009, (char *) "LightCondition", (char *) NULL},
	{  0x100A, (char *) "FocusRange", (char *) NULL},
	{  0x100B, (char *) "FocusMode", (char *) NULL},
	{  0x100C, (char *) "ManualFocusDistance", (char *) NULL},
	{  0x100D, (char *) "ZoomStepCount", (char *) NULL},
	{  0x100E, (char *) "FocusStepCount", (char *) NULL},
	{  0x100F, (char *) "Sharpness", (char *) NULL},
	{  0x1010, (char *) "FlashChargeLevel", (char *) NULL},
	{  0x1011, (char *) "ColorMatrix", (char *) NULL},
	{  0x1012, (char *) "BlackLevel", (char *) NULL},
	{  0x1015, (char *) "WBMode", (char *) NULL},
	{  0x1017, (char *) "RedBalance", (char *) NULL},
	{  0x1018, (char *) "BlueBalance", (char *) NULL},
	{  0x1019, (char *) "ColorMatrixNumber", (char *) NULL},
	{  0x101A, (char *) "SerialNumber", (char *) NULL},
	{  0x1023, (char *) "FlashExposureComp", (char *) NULL},
	{  0x1024, (char *) "InternalFlashTable", (char *) NULL},
	{  0x1025, (char *) "ExternalFlashGValue", (char *) NULL},
	{  0x1026, (char *) "ExternalFlashBounce", (char *) NULL},
	{  0x1027, (char *) "ExternalFlashZoom", (char *) NULL},
	{  0x1028, (char *) "ExternalFlashMode", (char *) NULL},
	{  0x1029, (char *) "Contrast", (char *) NULL},
	{  0x102A, (char *) "SharpnessFactor", (char *) NULL},
	{  0x102B, (char *) "ColorControl", (char *) NULL},
	{  0x102C, (char *) "ValidBits", (char *) NULL},
	{  0x102D, (char *) "CoringFilter", (char *) NULL},
	{  0x102E, (char *) "OlympusImageWidth", (char *) NULL},
	{  0x102F, (char *) "OlympusImageHeight", (char *) NULL},
	{  0x1030, (char *) "SceneDetect", (char *) NULL},
	{  0x1031, (char *) "SceneArea?", (char *) NULL},
	{  0x1033, (char *) "SceneDetectData?", (char *) NULL},
	{  0x1034, (char *) "CompressionRatio", (char *) NULL},
	{  0x1035, (char *) "PreviewImageValid", (char *) NULL},
	{  0x1036, (char *) "PreviewImageStart", (char *) NULL},
	{  0x1037, (char *) "PreviewImageLength", (char *) NULL},
	{  0x1038, (char *) "AFResult", (char *) NULL},
	{  0x1039, (char *) "CCDScanMode", (char *) NULL},
	{  0x103A, (char *) "NoiseReduction", (char *) NULL},
	{  0x103B, (char *) "InfinityLensStep", (char *) NULL},
	{  0x103C, (char *) "NearLensStep", (char *) NULL},
	{  0x103D, (char *) "LightValueCenter", (char *) NULL},
	{  0x103E, (char *) "LightValuePeriphery", (char *) NULL},
	{  0x2010, (char *) "Equipment", (char *) "Olympus Equipment Tags"},
	{  0x2020, (char *) "CameraSettings", (char *) "Olympus CameraSettings Tags"},
	{  0x2030, (char *) "RawDevelopment", (char *) "Olympus RawDevelopment Tags"},
	{  0x2040, (char *) "ImageProcessing", (char *) "Olympus ImageProcessing Tags"},
	{  0x2050, (char *) "FocusInfo", (char *) "Olympus FocusInfo Tags"},
	{  0x2100, (char *) "Olympus2100", (char *) "Olympus FE Tags"},
	{  0x2200, (char *) "Olympus2200", (char *) "Olympus FE Tags"},
	{  0x2300, (char *) "Olympus2300", (char *) "Olympus FE Tags"},
	{  0x2400, (char *) "Olympus2400", (char *) "Olympus FE Tags"},
	{  0x2500, (char *) "Olympus2500", (char *) "Olympus FE Tags"},
	{  0x2600, (char *) "Olympus2600", (char *) "Olympus FE Tags"},
	{  0x2700, (char *) "Olympus2700", (char *) "Olympus FE Tags"},
	{  0x2800, (char *) "Olympus2800", (char *) "Olympus FE Tags"},
	{  0x2900, (char *) "Olympus2900", (char *) "Olympus FE Tags"},
	{  0x3000, (char *) "RawInfo", (char *) "Olympus RawInfo Tags"},
	{  0x4000, (char *) "MainInfo", (char *) "Olympus MainInfo Tags"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Minolta maker note
*/
static TagInfo
  exif_minolta_tag_table[] =
  {
    {  0x0000, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x0001, (char *) "MinoltaCameraSettingsOld", (char *) NULL},
    {  0x0003, (char *) "MinoltaCameraSettings", (char *) NULL},
    {  0x0004, (char *) "MinoltaCameraSettings7D", (char *) NULL},
    {  0x0018, (char *) "ImageStabilization", (char *) NULL},
	{  0x0040, (char *) "CompressedImageSize", (char *) NULL},
    {  0x0081, (char *) "PreviewImage", (char *) NULL},
    {  0x0088, (char *) "PreviewImageStart", (char *) NULL},
    {  0x0089, (char *) "PreviewImageLength", (char *) NULL},
    {  0x0100, (char *) "SceneMode", (char *) NULL},
    {  0x0101, (char *) "ColorMode", (char *) NULL},
    {  0x0102, (char *) "MinoltaQuality", (char *) NULL},
    {  0x0103, (char *) "MinoltaImageSize", (char *) NULL},
    {  0x0104, (char *) "FlashExposureComp", (char *) NULL},
    {  0x0105, (char *) "Teleconverter", (char *) NULL},
    {  0x0107, (char *) "ImageStabilization", (char *) NULL},
    {  0x0109, (char *) "RawAndJpgRecording", (char *) NULL},
    {  0x010A, (char *) "ZoneMatching", (char *) NULL},
    {  0x010B, (char *) "ColorTemperature", (char *) NULL},
    {  0x010C, (char *) "LensType", (char *) NULL},
    {  0x0111, (char *) "ColorCompensationFilter", (char *) NULL},
    {  0x0112, (char *) "WhiteBalanceFineTune", (char *) NULL},
    {  0x0113, (char *) "ImageStabilization", (char *) NULL},
    {  0x0114, (char *) "MinoltaCameraSettings5D", (char *) NULL},
    {  0x0115, (char *) "WhiteBalance", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x0F00, (char *) "MinoltaCameraSettings2", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
There are 3 formats of Nikon's MakerNote. MakerNote of E700/E800/E900/E900S/E910/E950
starts from ASCII string "Nikon". Data format is the same as IFD, but it starts from
offset 0x08. This is the same as Olympus except start string. 
*/

/**
TYPE 1 is for E-Series cameras prior to (not including) E990
*/
static TagInfo
  exif_nikon_type1_tag_table[] =
  {
    {  0x0002, (char *) "FamilyID", (char *) NULL},
    {  0x0003, (char *) "Quality", (char *) NULL},
    {  0x0004, (char *) "ColorMode", (char *) NULL},
    {  0x0005, (char *) "ImageAdjustment", (char *) NULL},
    {  0x0006, (char *) "CCDSensitivity", (char *) NULL},
    {  0x0007, (char *) "WhiteBalance", (char *) NULL},
    {  0x0008, (char *) "Focus", (char *) NULL},
    {  0x000A, (char *) "DigitalZoom", (char *) NULL},
    {  0x000B, (char *) "FisheyeConverter", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Nikon type 2 maker note
*/
static TagInfo
  exif_nikon_type2_tag_table[] =
  {
    {  0x0001, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x0002, (char *) "ISO", (char *) NULL},
    {  0x0003, (char *) "ColorMode", (char *) NULL},
    {  0x0004, (char *) "Quality", (char *) NULL},
    {  0x0005, (char *) "WhiteBalance", (char *) NULL},
    {  0x0006, (char *) "Sharpness", (char *) NULL},
    {  0x0007, (char *) "FocusMode", (char *) NULL},
    {  0x0008, (char *) "FlashSetting", (char *) NULL},
    {  0x0009, (char *) "FlashType", (char *) NULL},
    {  0x000B, (char *) "WhiteBalanceFineTune", (char *) NULL},
    {  0x000F, (char *) "ISOSelection", (char *) NULL},
    {  0x0010, (char *) "DataDump", (char *) NULL},
    {  0x0080, (char *) "ImageAdjustment", (char *) NULL},
    {  0x0082, (char *) "AuxiliaryLens", (char *) NULL},
    {  0x0085, (char *) "ManualFocusDistance", (char *) NULL},
    {  0x0086, (char *) "DigitalZoom", (char *) NULL},
    {  0x0088, (char *) "AFInfo", (char *) NULL},
    {  0x0089, (char *) "ShootingMode", (char *) NULL},
    {  0x008D, (char *) "ColorMode", (char *) NULL},
    {  0x008F, (char *) "SceneMode", (char *) NULL},
    {  0x0092, (char *) "HueAdjustment", (char *) NULL},
    {  0x0094, (char *) "Saturation", (char *) NULL},
    {  0x0095, (char *) "NoiseReduction", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
The type-3 directory is for D-Series cameras such as the D1 and D100.
see http://www.timelesswanderings.net/equipment/D100/NEF.html
*/
static TagInfo
  exif_nikon_type3_tag_table[] =
  {
    {  0x0001, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x0002, (char *) "ISO", (char *) NULL},
    {  0x0003, (char *) "ColorMode", (char *) NULL},
    {  0x0004, (char *) "Quality", (char *) NULL},
    {  0x0005, (char *) "WhiteBalance", (char *) NULL},
    {  0x0006, (char *) "Sharpness", (char *) NULL},
    {  0x0007, (char *) "FocusMode", (char *) NULL},
    {  0x0008, (char *) "FlashSetting", (char *) NULL},
    {  0x0009, (char *) "FlashType", (char *) NULL},
    {  0x000B, (char *) "WhiteBalanceFineTune", (char *) NULL},
    {  0x000C, (char *) "WB_RBLevels", (char *) NULL},
    {  0x000D, (char *) "ProgramShift", (char *) NULL},
    {  0x000E, (char *) "ExposureDifference", (char *) NULL},
    {  0x000F, (char *) "ISOSelection", (char *) NULL},
	{  0x0010, (char *) "DataDump", (char *) NULL},
    {  0x0011, (char *) "PreviewIFD", (char *) NULL},
    {  0x0012, (char *) "FlashExposureComp", (char *) NULL},
    {  0x0013, (char *) "ISOSetting", (char *) NULL},
	{  0x0014, (char *) "ColorBalanceA", (char *) NULL},
    {  0x0016, (char *) "ImageBoundary", (char *) NULL},
	{  0x0017, (char *) "FlashExposureComp", (char *) NULL},
    {  0x0018, (char *) "FlashExposureBracketValue", (char *) NULL},
    {  0x0019, (char *) "ExposureBracketValue", (char *) NULL},
    {  0x001A, (char *) "ImageProcessing", (char *) NULL},
    {  0x001B, (char *) "CropHiSpeed", (char *) NULL},
	{  0x001C, (char *) "ExposureTuning", (char *) NULL},
    {  0x001D, (char *) "SerialNumber", (char *) NULL},
    {  0x001E, (char *) "ColorSpace", (char *) NULL},
	{  0x001F, (char *) "VRInfo", (char *) NULL},
	{  0x0020, (char *) "ImageAuthentication", (char *) NULL},
	{  0x0022, (char *) "ActiveD-Lighting", (char *) NULL},
	{  0x0023, (char *) "PictureControl", (char *) NULL},
	{  0x0024, (char *) "WorldTime", (char *) NULL},
	{  0x0025, (char *) "ISOInfo", (char *) NULL},
	{  0x002A, (char *) "VignetteControl", (char *) NULL},
	{  0x002B, (char *) "DistortInfo", (char *) NULL},
	{  0x0080, (char *) "ImageAdjustment", (char *) NULL},
	{  0x0081, (char *) "ToneComp", (char *) NULL},
	{  0x0082, (char *) "AuxiliaryLens", (char *) NULL},
	{  0x0083, (char *) "LensType", (char *) NULL},
    {  0x0084, (char *) "Lens", (char *) NULL},
    {  0x0085, (char *) "ManualFocusDistance", (char *) NULL},
    {  0x0086, (char *) "DigitalZoom", (char *) NULL},
    {  0x0087, (char *) "FlashMode", (char *) NULL},
    {  0x0088, (char *) "AFInfo", (char *) NULL},
    {  0x0089, (char *) "ShootingMode", (char *) NULL},
    {  0x008B, (char *) "LensFStops", (char *) NULL},
    {  0x008C, (char *) "ContrastCurve", (char *) NULL},
    {  0x008D, (char *) "ColorHue", (char *) NULL},
    {  0x008F, (char *) "SceneMode", (char *) NULL},
    {  0x0090, (char *) "LightSource", (char *) NULL},
	{  0x0091, (char *) "ShotInfo", (char *) NULL},
    {  0x0092, (char *) "HueAdjustment", (char *) NULL},
	{  0x0093, (char *) "NEFCompression", (char *) NULL},
    {  0x0094, (char *) "Saturation", (char *) NULL},
    {  0x0095, (char *) "NoiseReduction", (char *) NULL},
    {  0x0096, (char *) "LinearizationTable", (char *) NULL},
	{  0x0097, (char *) "ColorBalance", (char *) NULL},
	{  0x0098, (char *) "LensData", (char *) NULL},
    {  0x0099, (char *) "RawImageCenter", (char *) NULL},
    {  0x009A, (char *) "SensorPixelSize", (char *) NULL},
    {  0x009C, (char *) "SceneAssist", (char *) NULL},
    {  0x009E, (char *) "RetouchHistory", (char *) NULL},
    {  0x00A0, (char *) "SerialNumber", (char *) NULL},
    {  0x00A2, (char *) "ImageDataSize", (char *) NULL},
    {  0x00A5, (char *) "ImageCount", (char *) NULL},
    {  0x00A6, (char *) "DeletedImageCount", (char *) NULL},
    {  0x00A7, (char *) "ShutterCount", (char *) NULL},
	{  0x00A8, (char *) "FlashInfo", (char *) NULL},
    {  0x00A9, (char *) "ImageOptimization", (char *) NULL},
    {  0x00AA, (char *) "Saturation", (char *) NULL},
    {  0x00AB, (char *) "VariProgram", (char *) NULL},
    {  0x00AC, (char *) "ImageStabilization", (char *) NULL},
    {  0x00AD, (char *) "AFResponse", (char *) NULL},
    {  0x00B0, (char *) "MultiExposure", (char *) NULL},
    {  0x00B1, (char *) "HighISONoiseReduction", (char *) NULL},
    {  0x00B3, (char *) "ToningEffect", (char *) NULL},
    {  0x00B6, (char *) "PowerUpTime", (char *) NULL},
    {  0x00B7, (char *) "AFInfo2", (char *) NULL},
    {  0x00B8, (char *) "FileInfo", (char *) NULL},
    {  0x00B9, (char *) "AFTune", (char *) NULL},
    {  0x00BD, (char *) "PictureControl", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x0E01, (char *) "NikonCaptureData", (char *) NULL},
    {  0x0E09, (char *) "NikonCaptureVersion", (char *) NULL},
    {  0x0E0E, (char *) "NikonCaptureOffsets", (char *) NULL},
    {  0x0E10, (char *) "NikonScanIFD", (char *) NULL},
    {  0x0E1D, (char *) "NikonICCProfile", (char *) NULL},
    {  0x0E1E, (char *) "NikonCaptureOutput", (char *) NULL},
	{  0x0E22, (char *) "NEFBitDepth", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Panasonic / Leica maker note
*/
static TagInfo
  exif_panasonic_tag_table[] =
  {
    {  0x0001, (char *) "ImageQuality", (char *) NULL},
    {  0x0002, (char *) "FirmwareVersion", (char *) NULL},
    {  0x0003, (char *) "WhiteBalance", (char *) NULL},
    {  0x0007, (char *) "FocusMode", (char *) NULL},
    {  0x000F, (char *) "AFAreaMode", (char *) NULL},
    {  0x001A, (char *) "ImageStabilization", (char *) NULL},
    {  0x001C, (char *) "MacroMode", (char *) NULL},
    {  0x001F, (char *) "ShootingMode", (char *) NULL},
    {  0x0020, (char *) "Audio", (char *) NULL},
    {  0x0021, (char *) "DataDump", (char *) NULL},
	{  0x0022, (char *) "EasyMode", (char *) NULL},
    {  0x0023, (char *) "WhiteBalanceBias", (char *) NULL},
    {  0x0024, (char *) "FlashBias", (char *) NULL},
    {  0x0025, (char *) "InternalSerialNumber", (char *) NULL},
	{  0x0026, (char *) "PanasonicExifVersion", (char *) NULL},
    {  0x0028, (char *) "ColorEffect", (char *) NULL},
	{  0x0029, (char *) "TimeSincePowerOn", (char *) NULL},
    {  0x002A, (char *) "BurstMode", (char *) NULL},
    {  0x002B, (char *) "SequenceNumber", (char *) NULL},
    {  0x002C, (char *) "ContrastMode", (char *) NULL},
    {  0x002D, (char *) "NoiseReduction", (char *) NULL},
    {  0x002E, (char *) "SelfTimer", (char *) NULL},
    {  0x0030, (char *) "Rotation", (char *) NULL},
	{  0x0031, (char *) "AFAssistLamp", (char *) NULL},
    {  0x0032, (char *) "ColorMode", (char *) NULL},
	{  0x0033, (char *) "BabyAge_0x0033", (char *) NULL},
	{  0x0034, (char *) "OpticalZoomMode", (char *) NULL},
	{  0x0035, (char *) "ConversionLens", (char *) NULL},
	{  0x0036, (char *) "TravelDay", (char *) NULL},
	{  0x0039, (char *) "Contrast", (char *) NULL},
	{  0x003A, (char *) "WorldTimeLocation", (char *) NULL},
	{  0x003B, (char *) "TextStamp_0x003B", (char *) NULL},
	{  0x003C, (char *) "ProgramISO", (char *) NULL},
	{  0x003D, (char *) "AdvancedSceneMode", (char *) NULL},
	{  0x003E, (char *) "TextStamp_0x003E", (char *) NULL},
	{  0x003F, (char *) "FacesDetected", (char *) NULL},
	{  0x0040, (char *) "Saturation", (char *) NULL},
	{  0x0041, (char *) "Sharpness", (char *) NULL},
	{  0x0042, (char *) "FilmMode", (char *) NULL},
	{  0x0046, (char *) "WBAdjustAB", (char *) NULL},
	{  0x0047, (char *) "WBAdjustGM", (char *) NULL},
	{  0x004B, (char *) "PanasonicImageWidth", (char *) NULL},
	{  0x004C, (char *) "PanasonicImageHeight", (char *) NULL},
	{  0x004D, (char *) "AFPointPosition", (char *) NULL},
	{  0x004E, (char *) "FaceDetInfo", (char *) "Panasonic FaceDetInfo Tags"},
	{  0x0051, (char *) "LensType", (char *) NULL},
	{  0x0052, (char *) "LensSerialNumber", (char *) NULL},
	{  0x0053, (char *) "AccessoryType", (char *) NULL},
	{  0x0059, (char *) "Transform", (char *) NULL},
	{  0x005D, (char *) "IntelligentExposure", (char *) NULL},
	{  0x0061, (char *) "FaceRecInfo", (char *) "Panasonic FaceRecInfo Tags"},
	{  0x0062, (char *) "FlashWarning", (char *) NULL},
	{  0x0063, (char *) "RecognizedFaceFlags?", (char *) NULL},
	{  0x0065, (char *) "Title", (char *) NULL},
	{  0x0066, (char *) "BabyName", (char *) NULL},
	{  0x0067, (char *) "Location", (char *) NULL},
	{  0x0069, (char *) "Country", (char *) NULL},
	{  0x006B, (char *) "State", (char *) NULL},
	{  0x006D, (char *) "City", (char *) NULL},
	{  0x006F, (char *) "Landmark", (char *) NULL},
	{  0x0070, (char *) "IntelligentResolution", (char *) NULL},
	{  0x0079, (char *) "IntelligentD-Range", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
	{  0x8000, (char *) "MakerNoteVersion", (char *) NULL},
	{  0x8001, (char *) "SceneMode", (char *) NULL},
	{  0x8004, (char *) "WBRedLevel", (char *) NULL},
	{  0x8005, (char *) "WBGreenLevel", (char *) NULL},
	{  0x8006, (char *) "WBBlueLevel", (char *) NULL},
	{  0x8007, (char *) "FlashFired", (char *) NULL},
	{  0x8008, (char *) "TextStamp_0x8008", (char *) NULL},
	{  0x8009, (char *) "TextStamp_0x8009", (char *) NULL},
	{  0x8010, (char *) "BabyAge_0x8010", (char *) NULL},
	{  0x8012, (char *) "Transform", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Pentax (Asahi) maker note type 1
*/
static TagInfo
  exif_asahi_tag_table[] =
  {
    {  0x0001, (char *) "Capture Mode", (char *) NULL},
    {  0x0002, (char *) "Quality Level", (char *) NULL},
    {  0x0003, (char *) "Focus Mode", (char *) NULL},
    {  0x0004, (char *) "Flash Mode", (char *) NULL},
    {  0x0007, (char *) "White Balance", (char *) NULL},
    {  0x000A, (char *) "Digital Zoom", (char *) NULL},
    {  0x000B, (char *) "Sharpness", (char *) NULL},
    {  0x000C, (char *) "Contrast", (char *) NULL},
    {  0x000D, (char *) "Saturation", (char *) NULL},
    {  0x0014, (char *) "ISO Speed", (char *) NULL},
    {  0x0017, (char *) "Color", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x1000, (char *) "Time Zone", (char *) NULL},
    {  0x1001, (char *) "Daylight Savings", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Pentax maker note type 2
*/
static TagInfo
  exif_pentax_tag_table[] =
  {
    {  0x0000, (char *) "PentaxVersion", (char *) NULL},
    {  0x0001, (char *) "PentaxMode", (char *) NULL},
    {  0x0002, (char *) "PreviewImageSize", (char *) NULL},
    {  0x0003, (char *) "PreviewImageLength", (char *) NULL},
    {  0x0004, (char *) "PreviewImageStart", (char *) NULL},
    {  0x0005, (char *) "PentaxModelID", (char *) "Pentax PentaxModelID Values"},
    {  0x0006, (char *) "Date", (char *) NULL},
    {  0x0007, (char *) "Time", (char *) NULL},
    {  0x0008, (char *) "Quality", (char *) NULL},
    {  0x0009, (char *) "PentaxImageSize", (char *) NULL},
    {  0x000B, (char *) "PictureMode", (char *) NULL},
    {  0x000C, (char *) "FlashMode", (char *) NULL},
    {  0x000D, (char *) "FocusMode", (char *) NULL},
    {  0x000E, (char *) "AFPointSelected", (char *) NULL},
    {  0x000F, (char *) "AFPointsInFocus", (char *) NULL},
    {  0x0010, (char *) "FocusPosition", (char *) NULL},
    {  0x0012, (char *) "ExposureTime", (char *) NULL},
    {  0x0013, (char *) "FNumber", (char *) NULL},
    {  0x0014, (char *) "ISO", (char *) NULL},
	{  0x0015, (char *) "LightReading", (char *) NULL},
    {  0x0016, (char *) "ExposureCompensation", (char *) NULL},
    {  0x0017, (char *) "MeteringMode", (char *) NULL},
    {  0x0018, (char *) "AutoBracketing", (char *) NULL},
    {  0x0019, (char *) "WhiteBalance", (char *) NULL},
    {  0x001A, (char *) "WhiteBalanceMode", (char *) NULL},
    {  0x001B, (char *) "BlueBalance", (char *) NULL},
    {  0x001C, (char *) "RedBalance", (char *) NULL},
    {  0x001D, (char *) "FocalLength", (char *) NULL},
    {  0x001E, (char *) "DigitalZoom", (char *) NULL},
    {  0x001F, (char *) "Saturation", (char *) NULL},
    {  0x0020, (char *) "Contrast", (char *) NULL},
    {  0x0021, (char *) "Sharpness", (char *) NULL},
    {  0x0022, (char *) "WorldTimeLocation", (char *) NULL},
    {  0x0023, (char *) "HometownCity", (char *) "Pentax City Values"},
    {  0x0024, (char *) "DestinationCity", (char *) "Pentax City Values"},
    {  0x0025, (char *) "HometownDST", (char *) NULL},
    {  0x0026, (char *) "DestinationDST", (char *) NULL},
    {  0x0027, (char *) "DSPFirmwareVersion", (char *) NULL},
    {  0x0028, (char *) "CPUFirmwareVersion", (char *) NULL},
    {  0x0029, (char *) "FrameNumber", (char *) NULL},
    {  0x002D, (char *) "EffectiveLV", (char *) NULL},
    {  0x0032, (char *) "ImageProcessing", (char *) NULL},
    {  0x0033, (char *) "PictureMode", (char *) NULL},
    {  0x0034, (char *) "DriveMode", (char *) NULL},
	{  0x0035, (char *) "SensorSize", (char *) NULL},
    {  0x0037, (char *) "ColorSpace", (char *) NULL},
    {  0x0039, (char *) "RawImageSize", (char *) NULL},
	{  0x003C, (char *) "AFPointsInFocus", (char *) NULL},
    {  0x003E, (char *) "PreviewImageBorders", (char *) NULL},
    {  0x003F, (char *) "LensType", (char *) "Pentax LensType Values"},
    {  0x0040, (char *) "SensitivityAdjust", (char *) NULL},
    {  0x0041, (char *) "ImageProcessingCount", (char *) NULL},
    {  0x0047, (char *) "CameraTemperature", (char *) NULL},
    {  0x0048, (char *) "AELock", (char *) NULL},
    {  0x0049, (char *) "NoiseReduction", (char *) NULL},
    {  0x004D, (char *) "FlashExposureComp", (char *) NULL},
    {  0x004F, (char *) "ImageTone", (char *) NULL},
	{  0x0050, (char *) "ColorTemperature", (char *) NULL},
    {  0x005C, (char *) "ShakeReductionInfo", (char *) "Pentax SRInfo Tags"},
    {  0x005D, (char *) "ShutterCount", (char *) NULL},
	{  0x0060, (char *) "FaceInfo", (char *) "Pentax FaceInfo Tags"},
	{  0x0067, (char *) "Hue", (char *) NULL},
	{  0x0068, (char *) "AWBInfo", (char *) "Pentax AWBInfo Tags"},
    {  0x0069, (char *) "DynamicRangeExpansion", (char *) NULL},
	{  0x006B, (char *) "TimeInfo", (char *) "Pentax TimeInfo Tags"},
	{  0x006C, (char *) "HighLowKeyAdj", (char *) NULL},
	{  0x006D, (char *) "ContrastHighlight", (char *) NULL},
	{  0x006E, (char *) "ContrastShadow", (char *) NULL},
	{  0x006F, (char *) "ContrastHighlightShadowAdj", (char *) NULL},
	{  0x0070, (char *) "FineSharpness", (char *) NULL},
    {  0x0071, (char *) "HighISONoiseReduction", (char *) NULL},
    {  0x0072, (char *) "AFAdjustment", (char *) NULL},
	{  0x0073, (char *) "MonochromeFilterEffect", (char *) NULL},
	{  0x0074, (char *) "MonochromeToning", (char *) NULL},
	{  0x0076, (char *) "FaceDetect", (char *) NULL},
	{  0x0077, (char *) "FaceDetectFrameSize", (char *) NULL},
	{  0x0079, (char *) "ShadowCompensation", (char *) NULL},
	{  0x007A, (char *) "ISOAutoParameters", (char *) NULL},
	{  0x007B, (char *) "CrossProcess", (char *) NULL},
	{  0x007D, (char *) "LensCorr", (char *) "Pentax LensCorr Tags"},
	{  0x007F, (char *) "BleachBypassToning", (char *) NULL},
    {  0x0200, (char *) "BlackPoint", (char *) NULL},
    {  0x0201, (char *) "WhitePoint", (char *) NULL},
    {  0x0203, (char *) "ColorMatrixA", (char *) NULL},
    {  0x0204, (char *) "ColorMatrixB", (char *) NULL},
    {  0x0205, (char *) "CameraSettings", (char *) "Pentax CameraSettings Tags"},
	{  0x0206, (char *) "AEInfo", (char *) "Pentax AEInfo Tags"},
    {  0x0207, (char *) "LensInfo", (char *) "Pentax LensInfo Tags"},
    {  0x0208, (char *) "FlashInfo", (char *) "Pentax FlashInfo Tags"},
    {  0x0209, (char *) "AEMeteringSegments", (char *) NULL},
    {  0x020A, (char *) "FlashMeteringSegments", (char *) NULL},
    {  0x020B, (char *) "SlaveFlashMeteringSegments", (char *) NULL},
    {  0x020D, (char *) "WB_RGGBLevelsDaylight", (char *) NULL},
    {  0x020E, (char *) "WB_RGGBLevelsShade", (char *) NULL},
    {  0x020F, (char *) "WB_RGGBLevelsCloudy", (char *) NULL},
    {  0x0210, (char *) "WB_RGGBLevelsTungsten", (char *) NULL},
    {  0x0211, (char *) "WB_RGGBLevelsFluorescentD", (char *) NULL},
    {  0x0212, (char *) "WB_RGGBLevelsFluorescentN", (char *) NULL},
    {  0x0213, (char *) "WB_RGGBLevelsFluorescentW", (char *) NULL},
    {  0x0214, (char *) "WB_RGGBLevelsFlash", (char *) NULL},
    {  0x0215, (char *) "CameraInfo", (char *) "Pentax CameraInfo Tags"},
    {  0x0216, (char *) "BatteryInfo", (char *) "Pentax BatteryInfo Tags"},
    {  0x021B, (char *) "SaturationInfo", (char *) NULL},
    {  0x021F, (char *) "AFInfo", (char *) "Pentax AFInfo Tags"},
    {  0x0222, (char *) "ColorInfo", (char *) "Pentax ColorInfo Tags"},
    {  0x0224, (char *) "EVStepInfo", (char *) "Pentax EVStepInfo Tags"},
	{  0x0226, (char *) "ShotInfo", (char *) "Pentax ShotInfo Tags"},
	{  0x0227, (char *) "FacePos", (char *) "Pentax FacePos Tags"},
	{  0x0228, (char *) "FaceSize", (char *) "Pentax FaceSize Tags"},
	{  0x0229, (char *) "SerialNumber", (char *) NULL},
	{  0x022A, (char *) "FilterInfo", (char *) "Pentax FilterInfo Tags"},
	{  0x022B, (char *) "LevelInfo", (char *) "Pentax LevelInfo Tags"},
	{  0x022E, (char *) "Artist", (char *) NULL},
	{  0x022F, (char *) "Copyright", (char *) NULL},
	{  0x0230, (char *) "FirmwareVersion", (char *) NULL},
	{  0x0231, (char *) "ContrastDetectAFArea", (char *) NULL},
	{  0x0235, (char *) "CrossProcessParams", (char *) NULL},
	{  0x03FE, (char *) "DataDump", (char *) NULL},
    {  0x0402, (char *) "ToneCurve", (char *) NULL},
    {  0x0403, (char *) "ToneCurves", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x1000, (char *) "HometownCityCode", (char *) NULL},
    {  0x1001, (char *) "DestinationCityCode", (char *) NULL},
    {  0x2000, (char *) "PreviewImageData", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Sony maker note
*/
static TagInfo
  exif_sony_tag_table[] =
  {
    {  0x0102, (char *) "Quality", (char *) NULL},
    {  0x0104, (char *) "FlashExposureComp", (char *) NULL},
    {  0x0105, (char *) "Teleconverter", (char *) NULL},
    {  0x0112, (char *) "WhiteBalanceFineTune", (char *) NULL},
    {  0x0114, (char *) "CameraSettings", (char *) NULL},
    {  0x0115, (char *) "WhiteBalance", (char *) NULL},
    {  0x0E00, (char *) "PrintIM", (char *) NULL},
    {  0x1000, (char *) "MultiBurstMode", (char *) NULL},
    {  0x1001, (char *) "MultiBurstImageWidth", (char *) NULL},
    {  0x1002, (char *) "MultiBurstImageHeight", (char *) NULL},
    {  0x1003, (char *) "Panorama", (char *) NULL},
    {  0x2001, (char *) "PreviewImage", (char *) NULL},
    {  0x2004, (char *) "Contrast", (char *) NULL},
    {  0x2005, (char *) "Saturation", (char *) NULL},
    {  0x2006, (char *) "Sharpness", (char *) NULL},
    {  0x2007, (char *) "Brightness", (char *) NULL},
    {  0x2008, (char *) "LongExposureNoiseReduction", (char *) NULL},
    {  0x2009, (char *) "HighISONoiseReduction", (char *) NULL},
    {  0x200A, (char *) "HDR", (char *) NULL},
    {  0x200B, (char *) "MultiFrameNoiseReduction", (char *) NULL},
    {  0x3000, (char *) "ShotInfo", (char *) NULL},
    {  0xB000, (char *) "FileFormat", (char *) NULL},
    {  0xB001, (char *) "SonyModelID", (char *) NULL},
    {  0xB020, (char *) "ColorReproduction", (char *) NULL},
    {  0xB021, (char *) "ColorTemperature", (char *) NULL},
    {  0xB022, (char *) "ColorCompensationFilter", (char *) NULL},
    {  0xB023, (char *) "SceneMode", (char *) NULL},
    {  0xB024, (char *) "ZoneMatching", (char *) NULL},
    {  0xB025, (char *) "DynamicRangeOptimizer", (char *) NULL},
    {  0xB026, (char *) "ImageStabilization", (char *) NULL},
    {  0xB027, (char *) "LensType", (char *) NULL},
    {  0xB028, (char *) "MinoltaMakerNote", (char *) NULL},
    {  0xB029, (char *) "ColorMode", (char *) NULL},
    {  0xB02B, (char *) "FullImageSize", (char *) NULL},
    {  0xB02C, (char *) "PreviewImageSize", (char *) NULL},
    {  0xB040, (char *) "Macro", (char *) NULL},
    {  0xB041, (char *) "ExposureMode", (char *) NULL},
    {  0xB042, (char *) "FocusMode", (char *) NULL},
    {  0xB043, (char *) "AFMode", (char *) NULL},
    {  0xB044, (char *) "AFIlluminator", (char *) NULL},
    {  0xB047, (char *) "Quality2", (char *) NULL},
    {  0xB048, (char *) "FlashLevel", (char *) NULL},
    {  0xB049, (char *) "ReleaseMode", (char *) NULL},
    {  0xB04A, (char *) "SequenceNumber", (char *) NULL},
    {  0xB04B, (char *) "Anti-Blur", (char *) NULL},
    {  0xB04E, (char *) "LongExposureNoiseReduction", (char *) NULL},
    {  0xB04F, (char *) "DynamicRangeOptimizer", (char *) NULL},
    {  0xB052, (char *) "IntelligentAuto", (char *) NULL},
    {  0xB054, (char *) "WhiteBalance2", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Sigma SD1 maker note
*/
static TagInfo
  exif_sigma_sd1_tag_table[] =
  {
    {  0x0002, (char *) "SerialNumber", (char *) NULL},
    {  0x0003, (char *) "DriveMode", (char *) NULL},
    {  0x0004, (char *) "ResolutionMode", (char *) NULL},
    {  0x0005, (char *) "AFMode", (char *) NULL},
    {  0x0006, (char *) "FocusSetting", (char *) NULL},
    {  0x0007, (char *) "WhiteBalance", (char *) NULL},
    {  0x0008, (char *) "ExposureMode", (char *) NULL},
    {  0x0009, (char *) "MeteringMode", (char *) NULL},
    {  0x000A, (char *) "LensFocalRange", (char *) NULL},
    {  0x000B, (char *) "ColorSpace", (char *) NULL},
    {  0x000C, (char *) "ExposureCompensation", (char *) NULL},
    {  0x000D, (char *) "Contrast", (char *) NULL},
    {  0x000E, (char *) "Shadow", (char *) NULL},
    {  0x000F, (char *) "Highlight", (char *) NULL},
    {  0x0010, (char *) "Saturation", (char *) NULL},
    {  0x0011, (char *) "Sharpness", (char *) NULL},
    {  0x0012, (char *) "X3FillLight", (char *) NULL},
    {  0x0014, (char *) "ColorAdjustment", (char *) NULL},
    {  0x0015, (char *) "AdjustmentMode", (char *) NULL},
    {  0x0016, (char *) "Quality", (char *) NULL},
    {  0x0017, (char *) "Firmware", (char *) NULL},
    {  0x0018, (char *) "Software", (char *) NULL},
    {  0x0019, (char *) "AutoBracket", (char *) NULL},
    {  0x001A, (char *) "ChrominanceNoiseReduction", (char *) NULL},
    {  0x001B, (char *) "LuminanceNoiseReduction", (char *) NULL},
    {  0x001C, (char *) "PreviewImageStart", (char *) NULL},
    {  0x001D, (char *) "PreviewImageLength", (char *) NULL},
    {  0x001F, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x0026, (char *) "FileFormat", (char *) NULL},
    {  0x002C, (char *) "ColorMode", (char *) NULL},
    {  0x0030, (char *) "Calibration", (char *) NULL},
    {  0x0048, (char *) "LensApertureRange", (char *) NULL},
    {  0x0049, (char *) "FNumber", (char *) NULL},
    {  0x004A, (char *) "ExposureTime", (char *) NULL},
    {  0x004B, (char *) "ExposureTime2", (char *) NULL},
    {  0x004D, (char *) "ExposureCompensation_SD1", (char *) NULL},
    {  0x0055, (char *) "SensorTemperature", (char *) NULL},
    {  0x0056, (char *) "FlashExposureComp", (char *) NULL},
    {  0x0057, (char *) "Firmware_SD1", (char *) NULL},
    {  0x0058, (char *) "WhiteBalance", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

/**
Sigma / Foveon maker note (others than SD1 models)
NB: many tags are not consistent between different models
*/
static TagInfo
  exif_sigma_foveon_tag_table[] =
  {
    {  0x0002, (char *) "SerialNumber", (char *) NULL},
    {  0x0003, (char *) "DriveMode", (char *) NULL},
    {  0x0004, (char *) "ResolutionMode", (char *) NULL},
    {  0x0005, (char *) "AFMode", (char *) NULL},
    {  0x0006, (char *) "FocusSetting", (char *) NULL},
    {  0x0007, (char *) "WhiteBalance", (char *) NULL},
    {  0x0008, (char *) "ExposureMode", (char *) NULL},
    {  0x0009, (char *) "MeteringMode", (char *) NULL},
    {  0x000A, (char *) "LensFocalRange", (char *) NULL},
    {  0x000B, (char *) "ColorSpace", (char *) NULL},
    {  0x000C, (char *) "ExposureCompensation", (char *) NULL},
    {  0x000D, (char *) "Contrast", (char *) NULL},
    {  0x000E, (char *) "Shadow", (char *) NULL},
    {  0x000F, (char *) "Highlight", (char *) NULL},
    {  0x0010, (char *) "Saturation", (char *) NULL},
    {  0x0011, (char *) "Sharpness", (char *) NULL},
    {  0x0012, (char *) "X3FillLight", (char *) NULL},
    {  0x0014, (char *) "ColorAdjustment", (char *) NULL},
    {  0x0015, (char *) "AdjustmentMode", (char *) NULL},
    {  0x0016, (char *) "Quality", (char *) NULL},
    {  0x0017, (char *) "Firmware", (char *) NULL},
    {  0x0018, (char *) "Software", (char *) NULL},
    {  0x0019, (char *) "AutoBracket", (char *) NULL},
    {  0x001A, (char *) "PreviewImageStart", (char *) NULL},
    {  0x001B, (char *) "PreviewImageLength", (char *) NULL},
    {  0x001C, (char *) "PreviewImageSize", (char *) NULL},
    {  0x001D, (char *) "MakerNoteVersion", (char *) NULL},
    {  0x001F, (char *) "AFPoint", (char *) NULL},
    {  0x0022, (char *) "FileFormat", (char *) NULL},
    {  0x0024, (char *) "Calibration", (char *) NULL},
    {  0x002C, (char *) "ColorMode", (char *) NULL},
    {  0x0030, (char *) "LensApertureRange", (char *) NULL},
    {  0x0031, (char *) "FNumber", (char *) NULL},
    {  0x0032, (char *) "ExposureTime", (char *) NULL},
    {  0x0033, (char *) "ExposureTime2", (char *) NULL},
    {  0x0034, (char *) "BurstShot", (char *) NULL},
    {  0x0035, (char *) "ExposureCompensation", (char *) NULL},
    {  0x0039, (char *) "SensorTemperature", (char *) NULL},
    {  0x003A, (char *) "FlashExposureComp", (char *) NULL},
    {  0x003B, (char *) "Firmware", (char *) NULL},
    {  0x003C, (char *) "WhiteBalance", (char *) NULL},
    {  0x003D, (char *) "PictureMode", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// IPTC tags definition
// --------------------------------------------------------------------------

static TagInfo
  iptc_tag_table[] =
  {
	  // IPTC-NAA IIM version 4
    {  0x0200 +   0, (char *) "ApplicationRecordVersion", (char *) "Application Record Version"},
    {  0x0200 +   3, (char *) "ObjectTypeReference", (char *) "Object Type Reference"},
    {  0x0200 +   4, (char *) "ObjectAttributeReference", (char *) "Object Attribute Reference"},
    {  0x0200 +   5, (char *) "ObjectName", (char *) "Title"},
    {  0x0200 +   7, (char *) "EditStatus", (char *) "Edit Status"},
    {  0x0200 +   8, (char *) "EditorialUpdate", (char *) "Editorial Update"},
    {  0x0200 +  10, (char *) "Urgency", (char *) "Urgency"},
    {  0x0200 +  12, (char *) "SubjectReference", (char *) "Subject Reference"},
    {  0x0200 +  15, (char *) "Category", (char *) "Category"},
    {  0x0200 +  20, (char *) "SupplementalCategories", (char *) "Supplemental Categories"},
    {  0x0200 +  22, (char *) "FixtureIdentifier", (char *) "Fixture Identifier"},
    {  0x0200 +  25, (char *) "Keywords", (char *) "Keywords"},
    {  0x0200 +  26, (char *) "ContentLocationCode", (char *) "Content Location Code"},
    {  0x0200 +  27, (char *) "ContentLocationName", (char *) "Content Location Name"},
    {  0x0200 +  30, (char *) "ReleaseDate", (char *) "Release Date"},
    {  0x0200 +  35, (char *) "ReleaseTime", (char *) "Release Time"},
    {  0x0200 +  37, (char *) "ExpirationDate", (char *) "Expiration Date"},
    {  0x0200 +  38, (char *) "ExpirationTime", (char *) "Expiration Time"},
    {  0x0200 +  40, (char *) "SpecialInstructions", (char *) "Instructions"},
    {  0x0200 +  42, (char *) "ActionAdvised", (char *) "Action Advised"},
    {  0x0200 +  45, (char *) "ReferenceService", (char *) "Reference Service"},
    {  0x0200 +  47, (char *) "ReferenceDate", (char *) "Reference Date"},
    {  0x0200 +  50, (char *) "ReferenceNumber", (char *) "Reference Number"},
    {  0x0200 +  55, (char *) "DateCreated", (char *) "Date Created"},
    {  0x0200 +  60, (char *) "TimeCreated", (char *) "Time Created"},
    {  0x0200 +  62, (char *) "DigitalCreationDate", (char *) "Digital Creation Date"},
    {  0x0200 +  63, (char *) "DigitalCreationTime", (char *) "Digital Creation Time"},
    {  0x0200 +  65, (char *) "OriginatingProgram", (char *) "Originating Program"},
    {  0x0200 +  70, (char *) "ProgramVersion", (char *) "Program Version"},
    {  0x0200 +  75, (char *) "ObjectCycle", (char *) "Object Cycle"},
    {  0x0200 +  80, (char *) "By-line", (char *) "Author"},
    {  0x0200 +  85, (char *) "By-lineTitle", (char *) "Author's Position"},
    {  0x0200 +  90, (char *) "City", (char *) "City"},
    {  0x0200 +  92, (char *) "SubLocation", (char *) "Sub-Location"},
    {  0x0200 +  95, (char *) "Province-State", (char *) "State/Province"},
    {  0x0200 + 100, (char *) "Country-PrimaryLocationCode", (char *) "Country Code"},
    {  0x0200 + 101, (char *) "Country-PrimaryLocationName", (char *) "Country Name"},
    {  0x0200 + 103, (char *) "OriginalTransmissionReference", (char *) "Transmission Reference"},
    {  0x0200 + 105, (char *) "Headline", (char *) "Headline"},
    {  0x0200 + 110, (char *) "Credit", (char *) "Credit"},
    {  0x0200 + 115, (char *) "Source", (char *) "Source"},
    {  0x0200 + 116, (char *) "CopyrightNotice", (char *) "Copyright Notice"},
    {  0x0200 + 118, (char *) "Contact", (char *) "Contact"},
    {  0x0200 + 120, (char *) "Caption-Abstract", (char *) "Caption"},
    {  0x0200 + 122, (char *) "Writer-Editor", (char *) "Caption Writer"},
    {  0x0200 + 125, (char *) "RasterizedCaption", (char *) "Rasterized Caption"},
    {  0x0200 + 130, (char *) "ImageType", (char *) "Image Type"},
    {  0x0200 + 131, (char *) "ImageOrientation", (char *) "Image Orientation"},
    {  0x0200 + 135, (char *) "LanguageIdentifier", (char *) "Language Identifier"},
    {  0x0200 + 150, (char *) "AudioType", (char *) "Audio Type"},
    {  0x0200 + 151, (char *) "AudioSamplingRate", (char *) "Audio Sampling Rate"},
    {  0x0200 + 152, (char *) "AudioSamplingResolution", (char *) "Audio Sampling Resolution"},
    {  0x0200 + 153, (char *) "AudioDuration", (char *) "Audio Duration"},
    {  0x0200 + 154, (char *) "AudioOutcue", (char *) "Audio Outcue"},
		// Metadata seen in other softwares (see also http://owl.phy.queensu.ca/~phil/exiftool/TagNames/IPTC.html#ApplicationRecord)
    {  0x0200 + 184, (char *) "JobID", (char *) "Job ID"},
    {  0x0200 + 185, (char *) "MasterDocumentID", (char *) "Master Document ID"},
    {  0x0200 + 186, (char *) "ShortDocumentID", (char *) "Short Document ID"},
    {  0x0200 + 187, (char *) "UniqueDocumentID", (char *) "Unique Document ID"},
    {  0x0200 + 188, (char *) "OwnerID", (char *) "Owner ID"},
		// IPTC-NAA IIM version 4
    {  0x0200 + 200, (char *) "ObjectPreviewFileFormat", (char *) "Object Preview File Format"},
    {  0x0200 + 201, (char *) "ObjectPreviewFileVersion", (char *) "Object Preview File Version"},
    {  0x0200 + 202, (char *) "ObjectPreviewData", (char *) "Audio Outcue"},
		// Metadata seen in other softwares (see also http://owl.phy.queensu.ca/~phil/exiftool/TagNames/IPTC.html#ApplicationRecord)
    {  0x0200 + 221, (char *) "Prefs", (char *) "PhotoMechanic preferences"},
    {  0x0200 + 225, (char *) "ClassifyState", (char *) "Classify State"},
    {  0x0200 + 228, (char *) "SimilarityIndex", (char *) "Similarity Index"},
    {  0x0200 + 230, (char *) "DocumentNotes", (char *) "Document Notes"},
    {  0x0200 + 231, (char *) "DocumentHistory", (char *) "Document History"},
    {  0x0200 + 232, (char *) "ExifCameraInfo", (char *) "Exif Camera Info"},

    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// GeoTIFF tags definition
// --------------------------------------------------------------------------

static TagInfo
  geotiff_tag_table[] =
  {
    {  0x830E, (char *) "GeoPixelScale", (char *) NULL},
    {  0x8480, (char *) "Intergraph TransformationMatrix", (char *) NULL},
    {  0x8482, (char *) "GeoTiePoints", (char *) NULL},
    {  0x85D7, (char *) "JPL Carto IFD offset", (char *) NULL},
    {  0x85D8, (char *) "GeoTransformationMatrix", (char *) NULL},
    {  0x87AF, (char *) "GeoKeyDirectory", (char *) NULL},
    {  0x87B0, (char *) "GeoDoubleParams", (char *) NULL},
    {  0x87B1, (char *) "GeoASCIIParams", (char *) NULL},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// Animation tags definition
// --------------------------------------------------------------------------

static TagInfo
  animation_tag_table[] =
  {
    {  0x0001, (char *) "LogicalWidth", (char *) "Logical width"},
    {  0x0002, (char *) "LogicalHeight", (char *) "Logical height"},
    {  0x0003, (char *) "GlobalPalette", (char *) "Global Palette"},
    {  0x0004, (char *) "Loop", (char *) "loop"},
    {  0x1001, (char *) "FrameLeft", (char *) "Frame left"},
    {  0x1002, (char *) "FrameTop", (char *) "Frame top"},
    {  0x1003, (char *) "NoLocalPalette", (char *) "No Local Palette"},
    {  0x1004, (char *) "Interlaced", (char *) "Interlaced"},
    {  0x1005, (char *) "FrameTime", (char *) "Frame display time"},
    {  0x1006, (char *) "DisposalMethod", (char *) "Frame disposal method"},
    {  0x0000, (char *) NULL, (char *) NULL}
  };

// --------------------------------------------------------------------------
// TagLib class definition
// --------------------------------------------------------------------------


/**
This is where the tag info tables are initialized
*/
TagLib::TagLib() {
	// initialize all known metadata models
	// ====================================

	// Exif
	addMetadataModel(TagLib::EXIF_MAIN, exif_exif_tag_table);
	addMetadataModel(TagLib::EXIF_EXIF, exif_exif_tag_table);
	addMetadataModel(TagLib::EXIF_GPS, exif_gps_tag_table);
	addMetadataModel(TagLib::EXIF_INTEROP, exif_interop_tag_table);

	// Exif maker note
	addMetadataModel(TagLib::EXIF_MAKERNOTE_CANON, exif_canon_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_CASIOTYPE1, exif_casio_type1_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_CASIOTYPE2, exif_casio_type2_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_FUJIFILM, exif_fujifilm_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_KYOCERA, exif_kyocera_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_MINOLTA, exif_minolta_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_NIKONTYPE1, exif_nikon_type1_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_NIKONTYPE2, exif_nikon_type2_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_NIKONTYPE3, exif_nikon_type3_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_OLYMPUSTYPE1, exif_olympus_type1_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_PANASONIC, exif_panasonic_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_ASAHI, exif_asahi_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_PENTAX, exif_pentax_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_SONY, exif_sony_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_SIGMA_SD1, exif_sigma_sd1_tag_table);
	addMetadataModel(TagLib::EXIF_MAKERNOTE_SIGMA_FOVEON, exif_sigma_foveon_tag_table);

	// IPTC/NAA
	addMetadataModel(TagLib::IPTC, iptc_tag_table);

	// GeoTIFF
	addMetadataModel(TagLib::GEOTIFF, geotiff_tag_table);

	// Animation
	addMetadataModel(TagLib::ANIMATION, animation_tag_table);
}

BOOL TagLib::addMetadataModel(MDMODEL md_model, TagInfo *tag_table) {
	// check that the model doesn't already exist
	if((_table_map.find(md_model) == _table_map.end()) && (tag_table != NULL)) {

		// add the tag description table
		TAGINFO *info_map = new(std::nothrow) TAGINFO();
		if(!info_map) return FALSE;

		for(int i = 0; ; i++) {
			if((tag_table[i].tag == 0) && (tag_table[i].fieldname == NULL))
				break;
			(*info_map)[tag_table[i].tag] = &tag_table[i];
		}

		// add the metadata model
		_table_map[md_model] = info_map;

		return TRUE;
	}

	return FALSE;
}

TagLib::~TagLib() {
	// delete metadata models
	for(TABLEMAP::iterator i = _table_map.begin(); i != _table_map.end(); i++) {
		TAGINFO *info_map = (*i).second;
		delete info_map;
	}
}


TagLib& 
TagLib::instance() {
	static TagLib s;
	return s;
}

const TagInfo* 
TagLib::getTagInfo(MDMODEL md_model, WORD tagID) {

	if(_table_map.find(md_model) != _table_map.end()) {

		TAGINFO *info_map = (TAGINFO*)_table_map[md_model];
		if(info_map->find(tagID) != info_map->end()) {
			return (*info_map)[tagID];
		}
	}
	return NULL;
}

const char* 
TagLib::getTagFieldName(MDMODEL md_model, WORD tagID, char *defaultKey) {

	const TagInfo *info = getTagInfo(md_model, tagID);
	if(NULL == info) {
		if(defaultKey != NULL) {
			sprintf(defaultKey, "Tag 0x%04X", tagID);
			return &defaultKey[0];
		} else {
			return NULL;
		}
	}

	return info->fieldname;
}

const char* 
TagLib::getTagDescription(MDMODEL md_model, WORD tagID) {

	const TagInfo *info = getTagInfo(md_model, tagID);
	if(info) {
		return info->description;
	}

	return NULL;
}

int TagLib::getTagID(MDMODEL md_model, const char *key) {

	if(_table_map.find(md_model) != _table_map.end()) {

		TAGINFO *info_map = (TAGINFO*)_table_map[md_model];
		for(TAGINFO::iterator i = info_map->begin(); i != info_map->end(); i++) {
			const TagInfo *info = (*i).second;
			if(info && (strcmp(info->fieldname, key) == 0)) {
				return (int)info->tag;
			}
		}
	}
	return -1;
}

FREE_IMAGE_MDMODEL 
TagLib::getFreeImageModel(MDMODEL model) {
	switch(model) {
		case EXIF_MAIN:
			return FIMD_EXIF_MAIN;

		case EXIF_EXIF:
			return FIMD_EXIF_EXIF;

		case EXIF_GPS: 
			return FIMD_EXIF_GPS;

		case EXIF_INTEROP:
			return FIMD_EXIF_INTEROP;

		case EXIF_MAKERNOTE_CANON:
		case EXIF_MAKERNOTE_CASIOTYPE1:
		case EXIF_MAKERNOTE_CASIOTYPE2:
		case EXIF_MAKERNOTE_FUJIFILM:
		case EXIF_MAKERNOTE_KYOCERA:
		case EXIF_MAKERNOTE_MINOLTA:
		case EXIF_MAKERNOTE_NIKONTYPE1:
		case EXIF_MAKERNOTE_NIKONTYPE2:
		case EXIF_MAKERNOTE_NIKONTYPE3:
		case EXIF_MAKERNOTE_OLYMPUSTYPE1:
		case EXIF_MAKERNOTE_PANASONIC:
		case EXIF_MAKERNOTE_ASAHI:
		case EXIF_MAKERNOTE_PENTAX:
		case EXIF_MAKERNOTE_SONY:
		case EXIF_MAKERNOTE_SIGMA_SD1:
		case EXIF_MAKERNOTE_SIGMA_FOVEON:
			return FIMD_EXIF_MAKERNOTE;

		case IPTC:
			return FIMD_IPTC;

		case GEOTIFF:
			return FIMD_GEOTIFF;

		case ANIMATION:
			return FIMD_ANIMATION;
	}

	return FIMD_NODATA;
}


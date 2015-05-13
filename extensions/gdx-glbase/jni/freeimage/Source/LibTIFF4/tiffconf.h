/*
  Configuration defines for installed libtiff.
  This file maintained for backward compatibility. Do not use definitions
  from this file in your programs.
*/

#ifndef _TIFFCONF_
#define _TIFFCONF_

/* The size of a `int', as computed by sizeof. */
#define SIZEOF_INT 4

/* The size of a `long', as computed by sizeof. */
#include <limits.h>
#if (LONG_MAX == +9223372036854775807L)
#define SIZEOF_LONG 8
#define SIZEOF_UNSIGNED_LONG 8
#elif (LONG_MAX == +2147483647)
#define SIZEOF_LONG 4
#define SIZEOF_UNSIGNED_LONG 4
#else
#error "Cannot detect SIZEOF_LONG"
#endif

/* Signed 8-bit type */
#define TIFF_INT8_T signed char

/* Unsigned 8-bit type */
#define TIFF_UINT8_T unsigned char

/* Signed 16-bit type */
#define TIFF_INT16_T signed short

/* Unsigned 16-bit type */
#define TIFF_UINT16_T unsigned short

/* Signed 32-bit type */
#define TIFF_INT32_T signed int

/* Unsigned 32-bit type */
#define TIFF_UINT32_T unsigned int

/* Signed / Unsigned 64-bit type */
#ifdef _MSC_VER
#define TIFF_INT64_T signed __int64
#define TIFF_UINT64_T unsigned __int64
#else
#include <inttypes.h>
#define TIFF_INT64_T int64_t
#define TIFF_UINT64_T uint64_t
#endif // _MSC_VER

/* Signed 64-bit type */
#if defined(_WIN64)
#define TIFF_SSIZE_T signed __int64
#else
#define TIFF_SSIZE_T signed long
#endif

/* Pointer difference type */
#define TIFF_PTRDIFF_T ptrdiff_t

/* Compatibility stuff. */

/* Define as 0 or 1 according to the floating point format suported by the
   machine */
#define HAVE_IEEEFP 1

/* 
-----------------------------------------------------------------------
Byte order
-----------------------------------------------------------------------
*/

/*
Define WORDS_BIGENDIAN to 1 if your processor stores words with the most
significant byte first (like Motorola and SPARC, unlike Intel).
Some versions of gcc may have BYTE_ORDER or __BYTE_ORDER defined
If your big endian system isn't being detected, add an OS specific check
*/
#if (defined(BYTE_ORDER) && BYTE_ORDER==BIG_ENDIAN) || \
	(defined(__BYTE_ORDER) && __BYTE_ORDER==__BIG_ENDIAN) || \
	defined(__BIG_ENDIAN__)
/* Set the native cpu bit order (FILLORDER_LSB2MSB or FILLORDER_MSB2LSB) */
#define HOST_FILLORDER FILLORDER_MSB2LSB
/* Native cpu byte order: 1 if big-endian (Motorola) or 0 if little-endian (Intel) */
#define WORDS_BIGENDIAN 1
/* Native cpu byte order: 1 if big-endian (Motorola) or 0 if little-endian (Intel) */
#define HOST_BIGENDIAN 1
#else
/* Set the native cpu bit order (FILLORDER_LSB2MSB or FILLORDER_MSB2LSB) */
#define HOST_FILLORDER FILLORDER_LSB2MSB
/* Native cpu byte order: 1 if big-endian (Motorola) or 0 if little-endian (Intel) */
#undef WORDS_BIGENDIAN
/* Native cpu byte order: 1 if big-endian (Motorola) or 0 if little-endian (Intel) */
#undef HOST_BIGENDIAN
#endif // BYTE_ORDER

/* Support CCITT Group 3 & 4 algorithms */
#define CCITT_SUPPORT 1

/* Support JPEG compression (requires IJG JPEG library) */
#define JPEG_SUPPORT 1

/* Support JBIG compression (requires JBIG-KIT library) */
/* #undef JBIG_SUPPORT */

/* Support LogLuv high dynamic range encoding */
#define LOGLUV_SUPPORT 1

/* Support LZW algorithm */
#define LZW_SUPPORT 1

/* Support NeXT 2-bit RLE algorithm */
#define NEXT_SUPPORT 1

/* Support Old JPEG compresson (read contrib/ojpeg/README first! Compilation
   fails with unpatched IJG JPEG library) */
#define OJPEG_SUPPORT 1

/* Support Macintosh PackBits algorithm */
#define PACKBITS_SUPPORT 1

/* Support Pixar log-format algorithm (requires Zlib) */
#define PIXARLOG_SUPPORT 1

/* Support ThunderScan 4-bit RLE algorithm */
#define THUNDER_SUPPORT 1

/* Support Deflate compression */
#define ZIP_SUPPORT 1

/* Support LZMA2 compression */
#undef LZMA_SUPPORT

/* Support Microsoft Document Imaging format */
#undef MDI_SUPPORT

/* Support strip chopping (whether or not to convert single-strip uncompressed
   images to mutiple strips of ~8Kb to reduce memory usage) */
#define STRIPCHOP_DEFAULT TIFF_STRIPCHOP

/* Enable SubIFD tag (330) support */
#define SUBIFD_SUPPORT 1

/* Treat extra sample as alpha (default enabled). The RGBA interface will
   treat a fourth sample with no EXTRASAMPLE_ value as being ASSOCALPHA. Many
   packages produce RGBA files but don't mark the alpha properly. */
#define DEFAULT_EXTRASAMPLE_AS_ALPHA 1

/* Pick up YCbCr subsampling info from the JPEG data stream to support files
   lacking the tag (default enabled). */
#define CHECK_JPEG_YCBCR_SUBSAMPLING 1

/* Support MS MDI magic number files as TIFF */
/* #undef MDI_SUPPORT */

/*
 * Feature support definitions.
 * XXX: These macros are obsoleted. Don't use them in your apps!
 * Macros stays here for backward compatibility and should be always defined.
 */
#define COLORIMETRY_SUPPORT
#define YCBCR_SUPPORT
#define CMYK_SUPPORT
#define ICC_SUPPORT
#define PHOTOSHOP_SUPPORT
#define IPTC_SUPPORT

#endif /* _TIFFCONF_ */

/* $Id: tif_config.wince.h,v 1.8 2013/11/29 22:22:01 drolon Exp $ */

/*
 * TIFF library configuration header for Windows CE platform.
 */
#ifndef _WIN32_WCE
# error This version of tif_config.h header is dedicated for Windows CE platform!
#endif

/* Define to 1 if you have the <assert.h> header file. */
#define HAVE_ASSERT_H 1

/* Define to 1 if you have the <fcntl.h> header file. */
#define  HAVE_FCNTL_H 1

/* Define as 0 or 1 according to the floating point format suported by the
   machine */
#define HAVE_IEEEFP 1

/* Define to 1 if you have the `jbg_newlen' function. */
#define HAVE_JBG_NEWLEN 1

/* Define to 1 if you have the <string.h> header file. */
#define HAVE_STRING_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#undef HAVE_SYS_TYPES_H

/* Define to 1 if you have the <io.h> header file. */
#define HAVE_IO_H 1

/* Define to 1 if you have the <search.h> header file. */
#define HAVE_SEARCH_H 1

/* Define to 1 if you have the `setmode' function. */
#define HAVE_SETMODE 1

/* Define to 1 if you have the `bsearch' function. */
#define HAVE_BSEARCH 1
#define bsearch wceex_bsearch

/* Define to 1 if you have the `lfind' function. */
#define HAVE_LFIND 1
#define lfind wceex_lfind

/* The size of a `int', as computed by sizeof. */
#define SIZEOF_INT 4

/* Set the native cpu bit order */
#define HOST_FILLORDER FILLORDER_LSB2MSB

/* Define to 1 if your processor stores words with the most significant byte
   first (like Motorola and SPARC, unlike Intel and VAX). */
/* #undef WORDS_BIGENDIAN */

/* Define to `__inline__' or `__inline' if that's what the C compiler
   calls it, or to nothing if 'inline' is not supported under any name.  */
#ifndef __cplusplus
# ifndef inline
#  define inline __inline
# endif
#endif


/*
 * Local Variables:
 * mode: c
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */

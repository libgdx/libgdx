/*****************************************************************************
 *   "Gif-Lib" - Yet another gif library.
 *
 * Written by:  Gershon Elber            IBM PC Ver 0.1,    Jun. 1989
 ******************************************************************************
 * Module to emulate a printf with a possible quiet (disable mode.)
 * A global variable GifQuietPrint controls the printing of this routine
 ******************************************************************************
 * History:
 * 12 May 91 - Version 1.0 by Gershon Elber.
 *****************************************************************************/

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <stdio.h>

#ifdef HAVE_STDARG_H
#include <stdarg.h>
#elif defined (HAVE_VARARGS_H)
#include <varargs.h>
#endif /* HAVE_STDARG_H */

#include "gif_lib.h"

#ifdef __MSDOS__
int GifQuietPrint = FALSE;
#else
int GifQuietPrint = TRUE;
#endif /* __MSDOS__ */


/***************************************************************************/
/*                                                                         */
/*  ft2build.h                                                             */
/*                                                                         */
/*    FreeType 2 build and setup macros (development version).             */
/*                                                                         */
/*  Copyright 1996-2001, 2003, 2006, 2013 by                               */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


 /*
  *  This is a development version of <ft2build.h> to build the library in
  *  debug mode.  Its only difference to the default version is that it
  *  includes a local `ftoption.h' header file with different settings for
  *  many configuration macros.
  *
  *  To use it, simply ensure that the directory containing this file is
  *  scanned by the compiler before the default FreeType header directory.
  *
  */

#ifndef __FT2BUILD_H__
#define __FT2BUILD_H__

#define FT_CONFIG_OPTIONS_H  <ftoption.h>

#include <config/ftheader.h>

#endif /* __FT2BUILD_H__ */


/* END */

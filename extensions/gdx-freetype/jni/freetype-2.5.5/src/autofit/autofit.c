/***************************************************************************/
/*                                                                         */
/*  autofit.c                                                              */
/*                                                                         */
/*    Auto-fitter module (body).                                           */
/*                                                                         */
/*  Copyright 2003-2007, 2011, 2013 by                                     */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#define FT_MAKE_OPTION_SINGLE_OBJECT
#include <ft2build.h>
#include "afpic.c"
#include "afangles.c"
#include "afblue.c"
#include "afglobal.c"
#include "afhints.c"

#include "afranges.c"

#include "afdummy.c"
#include "aflatin.c"
#ifdef FT_OPTION_AUTOFIT2
#include "aflatin2.c"
#endif
#include "afcjk.c"
#include "afindic.c"

#include "hbshim.c"

#include "afloader.c"
#include "afmodule.c"

#ifdef AF_CONFIG_OPTION_USE_WARPER
#include "afwarp.c"
#endif

/* END */

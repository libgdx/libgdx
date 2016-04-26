/***************************************************************************/
/*                                                                         */
/*  afranges.h                                                             */
/*                                                                         */
/*    Auto-fitter Unicode script ranges (specification).                   */
/*                                                                         */
/*  Copyright 2013-2015 by                                                 */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#ifndef __AFRANGES_H__
#define __AFRANGES_H__


#include "aftypes.h"


FT_BEGIN_HEADER

#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 )                             \
          extern const AF_Script_UniRangeRec  af_ ## s ## _uniranges[];

#include "afscript.h"

#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 )                                     \
          extern const AF_Script_UniRangeRec  af_ ## s ## _nonbase_uniranges[];

#include "afscript.h"

 /* */

FT_END_HEADER

#endif /* __AFRANGES_H__ */


/* END */

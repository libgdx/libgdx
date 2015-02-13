/***************************************************************************/
/*                                                                         */
/*  afglobal.h                                                             */
/*                                                                         */
/*    Auto-fitter routines to compute global hinting values                */
/*    (specification).                                                     */
/*                                                                         */
/*  Copyright 2003-2005, 2007, 2009, 2011-2014 by                          */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#ifndef __AFGLOBAL_H__
#define __AFGLOBAL_H__


#include "aftypes.h"
#include "afmodule.h"
#include "hbshim.h"


FT_BEGIN_HEADER


  FT_LOCAL_ARRAY( AF_WritingSystemClass )
  af_writing_system_classes[];


#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 )                    \
          AF_DECLARE_SCRIPT_CLASS( af_ ## s ## _script_class )

#include "afscript.h"

  FT_LOCAL_ARRAY( AF_ScriptClass )
  af_script_classes[];


#undef  STYLE
#define STYLE( s, S, d, ws, sc, ss, c )                      \
          AF_DECLARE_STYLE_CLASS( af_ ## s ## _style_class )

#include "afstyles.h"

  FT_LOCAL_ARRAY( AF_StyleClass )
  af_style_classes[];


#ifdef FT_DEBUG_LEVEL_TRACE
  FT_LOCAL_ARRAY( char* )
  af_style_names[];
#endif


  /*
   *  Default values and flags for both autofitter globals (found in
   *  AF_ModuleRec) and face globals (in AF_FaceGlobalsRec).
   */

  /* index of fallback style in `af_style_classes' */
#ifdef AF_CONFIG_OPTION_CJK
#define AF_STYLE_FALLBACK    AF_STYLE_HANI_DFLT
#else
#define AF_STYLE_FALLBACK    AF_STYLE_NONE_DFLT
#endif
  /* default script for OpenType; ignored if HarfBuzz isn't used */
#define AF_SCRIPT_DEFAULT    AF_SCRIPT_LATN
  /* a bit mask indicating an uncovered glyph        */
#define AF_STYLE_UNASSIGNED  0x7F
  /* if this flag is set, we have an ASCII digit     */
#define AF_DIGIT             0x80

  /* `increase-x-height' property */
#define AF_PROP_INCREASE_X_HEIGHT_MIN  6
#define AF_PROP_INCREASE_X_HEIGHT_MAX  0


  /************************************************************************/
  /************************************************************************/
  /*****                                                              *****/
  /*****                  F A C E   G L O B A L S                     *****/
  /*****                                                              *****/
  /************************************************************************/
  /************************************************************************/


  /*
   *  Note that glyph_styles[] maps each glyph to an index into the
   *  `af_style_classes' array.
   *
   */
  typedef struct  AF_FaceGlobalsRec_
  {
    FT_Face          face;
    FT_Long          glyph_count;    /* same as face->num_glyphs */
    FT_Byte*         glyph_styles;

#ifdef FT_CONFIG_OPTION_USE_HARFBUZZ
    hb_font_t*       hb_font;
#endif

    /* per-face auto-hinter properties */
    FT_UInt          increase_x_height;

    AF_StyleMetrics  metrics[AF_STYLE_MAX];

    AF_Module        module;         /* to access global properties */

  } AF_FaceGlobalsRec;


  /*
   *  model the global hints data for a given face, decomposed into
   *  style-specific items
   */

  FT_LOCAL( FT_Error )
  af_face_globals_new( FT_Face          face,
                       AF_FaceGlobals  *aglobals,
                       AF_Module        module );

  FT_LOCAL( FT_Error )
  af_face_globals_get_metrics( AF_FaceGlobals    globals,
                               FT_UInt           gindex,
                               FT_UInt           options,
                               AF_StyleMetrics  *ametrics );

  FT_LOCAL( void )
  af_face_globals_free( AF_FaceGlobals  globals );

  FT_LOCAL_DEF( FT_Bool )
  af_face_globals_is_digit( AF_FaceGlobals  globals,
                            FT_UInt         gindex );

  /* */


FT_END_HEADER

#endif /* __AFGLOBAL_H__ */


/* END */

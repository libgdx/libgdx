/***************************************************************************/
/*                                                                         */
/*  aftypes.h                                                              */
/*                                                                         */
/*    Auto-fitter types (specification only).                              */
/*                                                                         */
/*  Copyright 2003-2009, 2011-2014 by                                      */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


  /*************************************************************************
   *
   *  The auto-fitter is a complete rewrite of the old auto-hinter.
   *  Its main feature is the ability to differentiate between different
   *  writing systems and scripts in order to apply specific rules.
   *
   *  The code has also been compartmentized into several entities that
   *  should make algorithmic experimentation easier than with the old
   *  code.
   *
   *************************************************************************/


#ifndef __AFTYPES_H__
#define __AFTYPES_H__

#include <ft2build.h>

#include FT_FREETYPE_H
#include FT_OUTLINE_H
#include FT_INTERNAL_OBJECTS_H
#include FT_INTERNAL_DEBUG_H

#include "afblue.h"


FT_BEGIN_HEADER

  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                    D E B U G G I N G                          *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

#ifdef FT_DEBUG_AUTOFIT

#include FT_CONFIG_STANDARD_LIBRARY_H

extern int    _af_debug_disable_horz_hints;
extern int    _af_debug_disable_vert_hints;
extern int    _af_debug_disable_blue_hints;
extern void*  _af_debug_hints;

#endif /* FT_DEBUG_AUTOFIT */


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                 U T I L I T Y   S T U F F                     *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  typedef struct  AF_WidthRec_
  {
    FT_Pos  org;  /* original position/width in font units              */
    FT_Pos  cur;  /* current/scaled position/width in device sub-pixels */
    FT_Pos  fit;  /* current/fitted position/width in device sub-pixels */

  } AF_WidthRec, *AF_Width;


  FT_LOCAL( void )
  af_sort_pos( FT_UInt  count,
               FT_Pos*  table );

  FT_LOCAL( void )
  af_sort_and_quantize_widths( FT_UInt*  count,
                               AF_Width  widths,
                               FT_Pos    threshold );


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                   A N G L E   T Y P E S                       *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  The auto-fitter doesn't need a very high angular accuracy;
   *  this allows us to speed up some computations considerably with a
   *  light Cordic algorithm (see afangles.c).
   */

  typedef FT_Int  AF_Angle;


#define AF_ANGLE_PI   256
#define AF_ANGLE_2PI  ( AF_ANGLE_PI * 2 )
#define AF_ANGLE_PI2  ( AF_ANGLE_PI / 2 )
#define AF_ANGLE_PI4  ( AF_ANGLE_PI / 4 )


#if 0
  /*
   *  compute the angle of a given 2-D vector
   */
  FT_LOCAL( AF_Angle )
  af_angle_atan( FT_Pos  dx,
                 FT_Pos  dy );


  /*
   *  compute `angle2 - angle1'; the result is always within
   *  the range [-AF_ANGLE_PI .. AF_ANGLE_PI - 1]
   */
  FT_LOCAL( AF_Angle )
  af_angle_diff( AF_Angle  angle1,
                 AF_Angle  angle2 );
#endif /* 0 */


#define AF_ANGLE_DIFF( result, angle1, angle2 ) \
  FT_BEGIN_STMNT                                \
    AF_Angle  _delta = (angle2) - (angle1);     \
                                                \
                                                \
    _delta %= AF_ANGLE_2PI;                     \
    if ( _delta < 0 )                           \
      _delta += AF_ANGLE_2PI;                   \
                                                \
    if ( _delta > AF_ANGLE_PI )                 \
      _delta -= AF_ANGLE_2PI;                   \
                                                \
    result = _delta;                            \
  FT_END_STMNT


  /*  opaque handle to glyph-specific hints -- see `afhints.h' for more
   *  details
   */
  typedef struct AF_GlyphHintsRec_*  AF_GlyphHints;


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                       S C A L E R S                           *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  A scaler models the target pixel device that will receive the
   *  auto-hinted glyph image.
   */

  typedef enum  AF_ScalerFlags_
  {
    AF_SCALER_FLAG_NO_HORIZONTAL = 1,  /* disable horizontal hinting */
    AF_SCALER_FLAG_NO_VERTICAL   = 2,  /* disable vertical hinting   */
    AF_SCALER_FLAG_NO_ADVANCE    = 4   /* disable advance hinting    */

  } AF_ScalerFlags;


  typedef struct  AF_ScalerRec_
  {
    FT_Face         face;        /* source font face                        */
    FT_Fixed        x_scale;     /* from font units to 1/64th device pixels */
    FT_Fixed        y_scale;     /* from font units to 1/64th device pixels */
    FT_Pos          x_delta;     /* in 1/64th device pixels                 */
    FT_Pos          y_delta;     /* in 1/64th device pixels                 */
    FT_Render_Mode  render_mode; /* monochrome, anti-aliased, LCD, etc.     */
    FT_UInt32       flags;       /* additional control flags, see above     */

  } AF_ScalerRec, *AF_Scaler;


#define AF_SCALER_EQUAL_SCALES( a, b )      \
          ( (a)->x_scale == (b)->x_scale && \
            (a)->y_scale == (b)->y_scale && \
            (a)->x_delta == (b)->x_delta && \
            (a)->y_delta == (b)->y_delta )


  typedef struct AF_StyleMetricsRec_*  AF_StyleMetrics;

  /*  This function parses an FT_Face to compute global metrics for
   *  a specific style.
   */
  typedef FT_Error
  (*AF_WritingSystem_InitMetricsFunc)( AF_StyleMetrics  metrics,
                                       FT_Face          face );

  typedef void
  (*AF_WritingSystem_ScaleMetricsFunc)( AF_StyleMetrics  metrics,
                                        AF_Scaler        scaler );

  typedef void
  (*AF_WritingSystem_DoneMetricsFunc)( AF_StyleMetrics  metrics );


  typedef FT_Error
  (*AF_WritingSystem_InitHintsFunc)( AF_GlyphHints    hints,
                                     AF_StyleMetrics  metrics );

  typedef void
  (*AF_WritingSystem_ApplyHintsFunc)( AF_GlyphHints    hints,
                                      FT_Outline*      outline,
                                      AF_StyleMetrics  metrics );


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                W R I T I N G   S Y S T E M S                  *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  For the auto-hinter, a writing system consists of multiple scripts that
   *  can be handled similarly *in a typographical way*; the relationship is
   *  not based on history.  For example, both the Greek and the unrelated
   *  Armenian scripts share the same features like ascender, descender,
   *  x-height, etc.  Essentially, a writing system is covered by a
   *  submodule of the auto-fitter; it contains
   *
   *  - a specific global analyzer that computes global metrics specific to
   *    the script (based on script-specific characters to identify ascender
   *    height, x-height, etc.),
   *
   *  - a specific glyph analyzer that computes segments and edges for each
   *    glyph covered by the script,
   *
   *  - a specific grid-fitting algorithm that distorts the scaled glyph
   *    outline according to the results of the glyph analyzer.
   */

#define __AFWRTSYS_H__  /* don't load header files */
#undef  WRITING_SYSTEM
#define WRITING_SYSTEM( ws, WS )    \
          AF_WRITING_SYSTEM_ ## WS,

  /* The list of known writing systems. */
  typedef enum  AF_WritingSystem_
  {

#include "afwrtsys.h"

    AF_WRITING_SYSTEM_MAX   /* do not remove */

  } AF_WritingSystem;

#undef  __AFWRTSYS_H__


  typedef struct  AF_WritingSystemClassRec_
  {
    AF_WritingSystem  writing_system;

    FT_Offset                          style_metrics_size;
    AF_WritingSystem_InitMetricsFunc   style_metrics_init;
    AF_WritingSystem_ScaleMetricsFunc  style_metrics_scale;
    AF_WritingSystem_DoneMetricsFunc   style_metrics_done;

    AF_WritingSystem_InitHintsFunc     style_hints_init;
    AF_WritingSystem_ApplyHintsFunc    style_hints_apply;

  } AF_WritingSystemClassRec;

  typedef const AF_WritingSystemClassRec*  AF_WritingSystemClass;


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                        S C R I P T S                          *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  Each script is associated with a set of Unicode ranges that gets used
   *  to test whether the font face supports the script.
   *
   *  We use four-letter script tags from the OpenType specification,
   *  extended by `NONE', which indicates `no script'.
   */

#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 ) \
          AF_SCRIPT_ ## S,

  /* The list of known scripts. */
  typedef enum  AF_Script_
  {

#include "afscript.h"

    AF_SCRIPT_MAX   /* do not remove */

  } AF_Script;


  typedef struct  AF_Script_UniRangeRec_
  {
    FT_UInt32  first;
    FT_UInt32  last;

  } AF_Script_UniRangeRec;

#define AF_UNIRANGE_REC( a, b ) { (FT_UInt32)(a), (FT_UInt32)(b) }

  typedef const AF_Script_UniRangeRec*  AF_Script_UniRange;


  typedef struct  AF_ScriptClassRec_
  {
    AF_Script  script;

    AF_Script_UniRange  script_uni_ranges; /* last must be { 0, 0 }        */

    FT_UInt32  standard_char1;             /* for default width and height */
    FT_UInt32  standard_char2;             /* ditto                        */
    FT_UInt32  standard_char3;             /* ditto                        */

  } AF_ScriptClassRec;

  typedef const AF_ScriptClassRec*  AF_ScriptClass;


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                      C O V E R A G E S                        *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  Usually, a font contains more glyphs than can be addressed by its
   *  character map.
   *
   *  In the PostScript font world, encoding vectors specific to a given
   *  task are used to select such glyphs, and these glyphs can be often
   *  recognized by having a suffix in its glyph names.  For example, a
   *  superscript glyph `A' might be called `A.sup'.  Unfortunately, this
   *  naming scheme is not standardized and thus unusable for us.
   *
   *  In the OpenType world, a better solution was invented, namely
   *  `features', which cleanly separate a character's input encoding from
   *  the corresponding glyph's appearance, and which don't use glyph names
   *  at all.  For our purposes, and slightly generalized, an OpenType
   *  feature is a name of a mapping that maps character codes to
   *  non-standard glyph indices (features get used for other things also).
   *  For example, the `sups' feature provides superscript glyphs, thus
   *  mapping character codes like `A' or `B' to superscript glyph
   *  representation forms.  How this mapping happens is completely
   *  uninteresting to us.
   *
   *  For the auto-hinter, a `coverage' represents all glyphs of an OpenType
   *  feature collected in a set (as listed below) that can be hinted
   *  together.  To continue the above example, superscript glyphs must not
   *  be hinted together with normal glyphs because the blue zones
   *  completely differ.
   *
   *  Note that FreeType itself doesn't compute coverages; it only provides
   *  the glyphs addressable by the default Unicode character map.  Instead,
   *  we use the HarfBuzz library (if available), which has many functions
   *  exactly for this purpose.
   *
   *  AF_COVERAGE_DEFAULT is special: It should cover everything that isn't
   *  listed separately (including the glyphs addressable by the character
   *  map).  In case HarfBuzz isn't available, it exactly covers the glyphs
   *  addressable by the character map.
   *
   */

#undef  COVERAGE
#define COVERAGE( name, NAME, description, \
                  tag1, tag2, tag3, tag4 ) \
          AF_COVERAGE_ ## NAME,


  typedef enum  AF_Coverage_
  {
#include "afcover.h"

    AF_COVERAGE_DEFAULT

  } AF_Coverage;


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                         S T Y L E S                           *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  /*
   *  The topmost structure for modelling the auto-hinter glyph input data
   *  is a `style class', grouping everything together.
   */

#undef  STYLE
#define STYLE( s, S, d, ws, sc, ss, c ) \
          AF_STYLE_ ## S,

  /* The list of known styles. */
  typedef enum  AF_Style_
  {

#include "afstyles.h"

    AF_STYLE_MAX   /* do not remove */

  } AF_Style;


  typedef struct  AF_StyleClassRec_
  {
    AF_Style  style;

    AF_WritingSystem   writing_system;
    AF_Script          script;
    AF_Blue_Stringset  blue_stringset;
    AF_Coverage        coverage;

  } AF_StyleClassRec;

  typedef const AF_StyleClassRec*  AF_StyleClass;


  /*************************************************************************/
  /*************************************************************************/
  /*****                                                               *****/
  /*****                   S T Y L E   M E T R I C S                   *****/
  /*****                                                               *****/
  /*************************************************************************/
  /*************************************************************************/

  typedef struct AF_FaceGlobalsRec_*  AF_FaceGlobals;

  /* This is the main structure that combines everything.  Autofit modules */
  /* specific to writing systems derive their structures from it, for      */
  /* example `AF_LatinMetrics'.                                            */

  typedef struct  AF_StyleMetricsRec_
  {
    AF_StyleClass   style_class;
    AF_ScalerRec    scaler;
    FT_Bool         digits_have_same_width;

    AF_FaceGlobals  globals;    /* to access properties */

  } AF_StyleMetricsRec;


  /* Declare and define vtables for classes */
#ifndef FT_CONFIG_OPTION_PIC

#define AF_DECLARE_WRITING_SYSTEM_CLASS( writing_system_class ) \
  FT_CALLBACK_TABLE const AF_WritingSystemClassRec              \
  writing_system_class;

#define AF_DEFINE_WRITING_SYSTEM_CLASS(                  \
          writing_system_class,                          \
          system,                                        \
          m_size,                                        \
          m_init,                                        \
          m_scale,                                       \
          m_done,                                        \
          h_init,                                        \
          h_apply )                                      \
  FT_CALLBACK_TABLE_DEF                                  \
  const AF_WritingSystemClassRec  writing_system_class = \
  {                                                      \
    system,                                              \
                                                         \
    m_size,                                              \
                                                         \
    m_init,                                              \
    m_scale,                                             \
    m_done,                                              \
                                                         \
    h_init,                                              \
    h_apply                                              \
  };


#define AF_DECLARE_SCRIPT_CLASS( script_class ) \
  FT_CALLBACK_TABLE const AF_ScriptClassRec     \
  script_class;

#define AF_DEFINE_SCRIPT_CLASS(           \
          script_class,                   \
          script,                         \
          ranges,                         \
          std_char1,                      \
          std_char2,                      \
          std_char3 )                     \
  FT_CALLBACK_TABLE_DEF                   \
  const AF_ScriptClassRec  script_class = \
  {                                       \
    script,                               \
    ranges,                               \
    std_char1,                            \
    std_char2,                            \
    std_char3                             \
  };


#define AF_DECLARE_STYLE_CLASS( style_class ) \
  FT_CALLBACK_TABLE const AF_StyleClassRec    \
  style_class;

#define AF_DEFINE_STYLE_CLASS(          \
          style_class,                  \
          style,                        \
          writing_system,               \
          script,                       \
          blue_stringset,               \
          coverage )                    \
  FT_CALLBACK_TABLE_DEF                 \
  const AF_StyleClassRec  style_class = \
  {                                     \
    style,                              \
    writing_system,                     \
    script,                             \
    blue_stringset,                     \
    coverage                            \
  };

#else /* FT_CONFIG_OPTION_PIC */

#define AF_DECLARE_WRITING_SYSTEM_CLASS( writing_system_class )            \
  FT_LOCAL( void )                                                         \
  FT_Init_Class_ ## writing_system_class( AF_WritingSystemClassRec*  ac );

#define AF_DEFINE_WRITING_SYSTEM_CLASS(                                   \
          writing_system_class,                                           \
          system,                                                         \
          m_size,                                                         \
          m_init,                                                         \
          m_scale,                                                        \
          m_done,                                                         \
          h_init,                                                         \
          h_apply )                                                       \
  FT_LOCAL_DEF( void )                                                    \
  FT_Init_Class_ ## writing_system_class( AF_WritingSystemClassRec*  ac ) \
  {                                                                       \
    ac->writing_system      = system;                                     \
                                                                          \
    ac->style_metrics_size  = m_size;                                     \
                                                                          \
    ac->style_metrics_init  = m_init;                                     \
    ac->style_metrics_scale = m_scale;                                    \
    ac->style_metrics_done  = m_done;                                     \
                                                                          \
    ac->style_hints_init    = h_init;                                     \
    ac->style_hints_apply   = h_apply;                                    \
  }


#define AF_DECLARE_SCRIPT_CLASS( script_class )             \
  FT_LOCAL( void )                                          \
  FT_Init_Class_ ## script_class( AF_ScriptClassRec*  ac );

#define AF_DEFINE_SCRIPT_CLASS(                            \
          script_class,                                    \
          script_,                                         \
          ranges,                                          \
          std_char1,                                       \
          std_char2,                                       \
          std_char3 )                                      \
  FT_LOCAL_DEF( void )                                     \
  FT_Init_Class_ ## script_class( AF_ScriptClassRec*  ac ) \
  {                                                        \
    ac->script            = script_;                       \
    ac->script_uni_ranges = ranges;                        \
    ac->standard_char1    = std_char1;                     \
    ac->standard_char2    = std_char2;                     \
    ac->standard_char3    = std_char3;                     \
  }


#define AF_DECLARE_STYLE_CLASS( style_class )             \
  FT_LOCAL( void )                                        \
  FT_Init_Class_ ## style_class( AF_StyleClassRec*  ac );

#define AF_DEFINE_STYLE_CLASS(                           \
          style_class,                                   \
          style_,                                        \
          writing_system_,                               \
          script_,                                       \
          blue_stringset_,                               \
          coverage_ )                                    \
  FT_LOCAL_DEF( void )                                   \
  FT_Init_Class_ ## style_class( AF_StyleClassRec*  ac ) \
  {                                                      \
    ac->style          = style_;                         \
    ac->writing_system = writing_system_;                \
    ac->script         = script_;                        \
    ac->blue_stringset = blue_stringset_;                \
    ac->coverage       = coverage_;                      \
  }

#endif /* FT_CONFIG_OPTION_PIC */


/* */

FT_END_HEADER

#endif /* __AFTYPES_H__ */


/* END */

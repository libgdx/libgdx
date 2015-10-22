/***************************************************************************/
/*                                                                         */
/*  afglobal.c                                                             */
/*                                                                         */
/*    Auto-fitter routines to compute global hinting values (body).        */
/*                                                                         */
/*  Copyright 2003-2014 by                                                 */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#include "afglobal.h"
#include "afranges.h"
#include "hbshim.h"
#include FT_INTERNAL_DEBUG_H


  /*************************************************************************/
  /*                                                                       */
  /* The macro FT_COMPONENT is used in trace mode.  It is an implicit      */
  /* parameter of the FT_TRACE() and FT_ERROR() macros, used to print/log  */
  /* messages during execution.                                            */
  /*                                                                       */
#undef  FT_COMPONENT
#define FT_COMPONENT  trace_afglobal


  /* get writing system specific header files */
#undef  WRITING_SYSTEM
#define WRITING_SYSTEM( ws, WS )  /* empty */
#include "afwrtsys.h"

#include "aferrors.h"
#include "afpic.h"


#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 ) \
          AF_DEFINE_SCRIPT_CLASS(           \
            af_ ## s ## _script_class,      \
            AF_SCRIPT_ ## S,                \
            af_ ## s ## _uniranges,         \
            sc1, sc2, sc3 )

#include "afscript.h"


#undef  STYLE
#define STYLE( s, S, d, ws, sc, ss, c )  \
          AF_DEFINE_STYLE_CLASS(         \
            af_ ## s ## _style_class,    \
            AF_STYLE_ ## S,              \
            ws,                          \
            sc,                          \
            ss,                          \
            c )

#include "afstyles.h"


#ifndef FT_CONFIG_OPTION_PIC

#undef  WRITING_SYSTEM
#define WRITING_SYSTEM( ws, WS )               \
          &af_ ## ws ## _writing_system_class,

  FT_LOCAL_ARRAY_DEF( AF_WritingSystemClass )
  af_writing_system_classes[] =
  {

#include "afwrtsys.h"

    NULL  /* do not remove */
  };


#undef  SCRIPT
#define SCRIPT( s, S, d, h, sc1, sc2, sc3 ) \
          &af_ ## s ## _script_class,

  FT_LOCAL_ARRAY_DEF( AF_ScriptClass )
  af_script_classes[] =
  {

#include "afscript.h"

    NULL  /* do not remove */
  };


#undef  STYLE
#define STYLE( s, S, d, ws, sc, ss, c ) \
          &af_ ## s ## _style_class,

  FT_LOCAL_ARRAY_DEF( AF_StyleClass )
  af_style_classes[] =
  {

#include "afstyles.h"

    NULL  /* do not remove */
  };

#endif /* !FT_CONFIG_OPTION_PIC */


#ifdef FT_DEBUG_LEVEL_TRACE

#undef  STYLE
#define STYLE( s, S, d, ws, sc, ss, c )  #s,

  FT_LOCAL_ARRAY_DEF( char* )
  af_style_names[] =
  {

#include "afstyles.h"

  };

#endif /* FT_DEBUG_LEVEL_TRACE */


  /* Compute the style index of each glyph within a given face. */

  static FT_Error
  af_face_globals_compute_style_coverage( AF_FaceGlobals  globals )
  {
    FT_Error    error;
    FT_Face     face        = globals->face;
    FT_CharMap  old_charmap = face->charmap;
    FT_Byte*    gstyles     = globals->glyph_styles;
    FT_UInt     ss;
    FT_UInt     i;
    FT_UInt     dflt        = ~0U; /* a non-valid value */


    /* the value AF_STYLE_UNASSIGNED means `uncovered glyph' */
    FT_MEM_SET( globals->glyph_styles,
                AF_STYLE_UNASSIGNED,
                globals->glyph_count );

    error = FT_Select_Charmap( face, FT_ENCODING_UNICODE );
    if ( error )
    {
      /*
       * Ignore this error; we simply use the fallback style.
       * XXX: Shouldn't we rather disable hinting?
       */
      error = FT_Err_Ok;
      goto Exit;
    }

    /* scan each style in a Unicode charmap */
    for ( ss = 0; AF_STYLE_CLASSES_GET[ss]; ss++ )
    {
      AF_StyleClass       style_class =
                            AF_STYLE_CLASSES_GET[ss];
      AF_ScriptClass      script_class =
                            AF_SCRIPT_CLASSES_GET[style_class->script];
      AF_Script_UniRange  range;


      if ( script_class->script_uni_ranges == NULL )
        continue;

      /*
       *  Scan all Unicode points in the range and set the corresponding
       *  glyph style index.
       */
      if ( style_class->coverage == AF_COVERAGE_DEFAULT )
      {
        if ( (FT_UInt)style_class->script ==
             globals->module->default_script )
          dflt = ss;

        for ( range = script_class->script_uni_ranges;
              range->first != 0;
              range++ )
        {
          FT_ULong  charcode = range->first;
          FT_UInt   gindex;


          gindex = FT_Get_Char_Index( face, charcode );

          if ( gindex != 0                             &&
               gindex < (FT_ULong)globals->glyph_count &&
               gstyles[gindex] == AF_STYLE_UNASSIGNED  )
            gstyles[gindex] = (FT_Byte)ss;

          for (;;)
          {
            charcode = FT_Get_Next_Char( face, charcode, &gindex );

            if ( gindex == 0 || charcode > range->last )
              break;

            if ( gindex < (FT_ULong)globals->glyph_count &&
                 gstyles[gindex] == AF_STYLE_UNASSIGNED  )
              gstyles[gindex] = (FT_Byte)ss;
          }
        }
      }
      else
      {
        /* get glyphs not directly addressable by cmap */
        af_get_coverage( globals, style_class, gstyles );
      }
    }

    /* handle the default OpenType features of the default script ... */
    af_get_coverage( globals, AF_STYLE_CLASSES_GET[dflt], gstyles );

    /* ... and the remaining default OpenType features */
    for ( ss = 0; AF_STYLE_CLASSES_GET[ss]; ss++ )
    {
      AF_StyleClass  style_class = AF_STYLE_CLASSES_GET[ss];


      if ( ss != dflt && style_class->coverage == AF_COVERAGE_DEFAULT )
        af_get_coverage( globals, style_class, gstyles );
    }

    /* mark ASCII digits */
    for ( i = 0x30; i <= 0x39; i++ )
    {
      FT_UInt  gindex = FT_Get_Char_Index( face, i );


      if ( gindex != 0 && gindex < (FT_ULong)globals->glyph_count )
        gstyles[gindex] |= AF_DIGIT;
    }

  Exit:
    /*
     *  By default, all uncovered glyphs are set to the fallback style.
     *  XXX: Shouldn't we disable hinting or do something similar?
     */
    if ( globals->module->fallback_style != AF_STYLE_UNASSIGNED )
    {
      FT_Long  nn;


      for ( nn = 0; nn < globals->glyph_count; nn++ )
      {
        if ( ( gstyles[nn] & ~AF_DIGIT ) == AF_STYLE_UNASSIGNED )
        {
          gstyles[nn] &= ~AF_STYLE_UNASSIGNED;
          gstyles[nn] |= globals->module->fallback_style;
        }
      }
    }

#ifdef FT_DEBUG_LEVEL_TRACE

    FT_TRACE4(( "\n"
                "style coverage\n"
                "==============\n"
                "\n" ));

    for ( ss = 0; AF_STYLE_CLASSES_GET[ss]; ss++ )
    {
      AF_StyleClass  style_class = AF_STYLE_CLASSES_GET[ss];
      FT_UInt        count       = 0;
      FT_Long        idx;


      FT_TRACE4(( "%s:\n", af_style_names[style_class->style] ));

      for ( idx = 0; idx < globals->glyph_count; idx++ )
      {
        if ( ( gstyles[idx] & ~AF_DIGIT ) == style_class->style )
        {
          if ( !( count % 10 ) )
            FT_TRACE4(( " " ));

          FT_TRACE4(( " %d", idx ));
          count++;

          if ( !( count % 10 ) )
            FT_TRACE4(( "\n" ));
        }
      }

      if ( !count )
        FT_TRACE4(( "  (none)\n" ));
      if ( count % 10 )
        FT_TRACE4(( "\n" ));
    }

#endif /* FT_DEBUG_LEVEL_TRACE */

    FT_Set_Charmap( face, old_charmap );
    return error;
  }


  FT_LOCAL_DEF( FT_Error )
  af_face_globals_new( FT_Face          face,
                       AF_FaceGlobals  *aglobals,
                       AF_Module        module )
  {
    FT_Error        error;
    FT_Memory       memory;
    AF_FaceGlobals  globals = NULL;


    memory = face->memory;

    if ( FT_ALLOC( globals, sizeof ( *globals ) +
                            face->num_glyphs * sizeof ( FT_Byte ) ) )
      goto Exit;

    globals->face         = face;
    globals->glyph_count  = face->num_glyphs;
    globals->glyph_styles = (FT_Byte*)( globals + 1 );
    globals->module       = module;

#ifdef FT_CONFIG_OPTION_USE_HARFBUZZ
    globals->hb_font = hb_ft_font_create( face, NULL );
#endif

    error = af_face_globals_compute_style_coverage( globals );
    if ( error )
    {
      af_face_globals_free( globals );
      globals = NULL;
    }
    else
      globals->increase_x_height = AF_PROP_INCREASE_X_HEIGHT_MAX;

  Exit:
    *aglobals = globals;
    return error;
  }


  FT_LOCAL_DEF( void )
  af_face_globals_free( AF_FaceGlobals  globals )
  {
    if ( globals )
    {
      FT_Memory  memory = globals->face->memory;
      FT_UInt    nn;


      for ( nn = 0; nn < AF_STYLE_MAX; nn++ )
      {
        if ( globals->metrics[nn] )
        {
          AF_StyleClass          style_class =
            AF_STYLE_CLASSES_GET[nn];
          AF_WritingSystemClass  writing_system_class =
            AF_WRITING_SYSTEM_CLASSES_GET[style_class->writing_system];


          if ( writing_system_class->style_metrics_done )
            writing_system_class->style_metrics_done( globals->metrics[nn] );

          FT_FREE( globals->metrics[nn] );
        }
      }

#ifdef FT_CONFIG_OPTION_USE_HARFBUZZ
      hb_font_destroy( globals->hb_font );
      globals->hb_font = NULL;
#endif

      globals->glyph_count  = 0;
      globals->glyph_styles = NULL;  /* no need to free this one! */
      globals->face         = NULL;

      FT_FREE( globals );
    }
  }


  FT_LOCAL_DEF( FT_Error )
  af_face_globals_get_metrics( AF_FaceGlobals    globals,
                               FT_UInt           gindex,
                               FT_UInt           options,
                               AF_StyleMetrics  *ametrics )
  {
    AF_StyleMetrics  metrics = NULL;

    AF_Style               style = (AF_Style)options;
    AF_WritingSystemClass  writing_system_class;
    AF_StyleClass          style_class;

    FT_Error  error = FT_Err_Ok;


    if ( gindex >= (FT_ULong)globals->glyph_count )
    {
      error = FT_THROW( Invalid_Argument );
      goto Exit;
    }

    /* if we have a forced style (via `options'), use it, */
    /* otherwise look into `glyph_styles' array           */
    if ( style == AF_STYLE_NONE_DFLT || style + 1 >= AF_STYLE_MAX )
      style = (AF_Style)( globals->glyph_styles[gindex] &
                          AF_STYLE_UNASSIGNED           );

    style_class          = AF_STYLE_CLASSES_GET[style];
    writing_system_class = AF_WRITING_SYSTEM_CLASSES_GET
                             [style_class->writing_system];

    metrics = globals->metrics[style];
    if ( metrics == NULL )
    {
      /* create the global metrics object if necessary */
      FT_Memory  memory = globals->face->memory;


      if ( FT_ALLOC( metrics, writing_system_class->style_metrics_size ) )
        goto Exit;

      metrics->style_class = style_class;
      metrics->globals     = globals;

      if ( writing_system_class->style_metrics_init )
      {
        error = writing_system_class->style_metrics_init( metrics,
                                                          globals->face );
        if ( error )
        {
          if ( writing_system_class->style_metrics_done )
            writing_system_class->style_metrics_done( metrics );

          FT_FREE( metrics );
          goto Exit;
        }
      }

      globals->metrics[style] = metrics;
    }

  Exit:
    *ametrics = metrics;

    return error;
  }


  FT_LOCAL_DEF( FT_Bool )
  af_face_globals_is_digit( AF_FaceGlobals  globals,
                            FT_UInt         gindex )
  {
    if ( gindex < (FT_ULong)globals->glyph_count )
      return (FT_Bool)( globals->glyph_styles[gindex] & AF_DIGIT );

    return (FT_Bool)0;
  }


/* END */

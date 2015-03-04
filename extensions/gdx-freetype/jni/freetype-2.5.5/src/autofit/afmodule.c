/***************************************************************************/
/*                                                                         */
/*  afmodule.c                                                             */
/*                                                                         */
/*    Auto-fitter module implementation (body).                            */
/*                                                                         */
/*  Copyright 2003-2006, 2009, 2011-2014 by                                */
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
#include "afmodule.h"
#include "afloader.h"
#include "aferrors.h"
#include "afpic.h"

#ifdef FT_DEBUG_AUTOFIT
  int    _af_debug_disable_horz_hints;
  int    _af_debug_disable_vert_hints;
  int    _af_debug_disable_blue_hints;
  void*  _af_debug_hints;
#endif

#include FT_INTERNAL_OBJECTS_H
#include FT_INTERNAL_DEBUG_H
#include FT_AUTOHINTER_H
#include FT_SERVICE_PROPERTIES_H


  /*************************************************************************/
  /*                                                                       */
  /* The macro FT_COMPONENT is used in trace mode.  It is an implicit      */
  /* parameter of the FT_TRACE() and FT_ERROR() macros, used to print/log  */
  /* messages during execution.                                            */
  /*                                                                       */
#undef  FT_COMPONENT
#define FT_COMPONENT  trace_afmodule


  static FT_Error
  af_property_get_face_globals( FT_Face          face,
                                AF_FaceGlobals*  aglobals,
                                AF_Module        module )
  {
    FT_Error        error = FT_Err_Ok;
    AF_FaceGlobals  globals;


    if ( !face )
      return FT_THROW( Invalid_Face_Handle );

    globals = (AF_FaceGlobals)face->autohint.data;
    if ( !globals )
    {
      /* trigger computation of the global style data */
      /* in case it hasn't been done yet              */
      error = af_face_globals_new( face, &globals, module );
      if ( !error )
      {
        face->autohint.data =
          (FT_Pointer)globals;
        face->autohint.finalizer =
          (FT_Generic_Finalizer)af_face_globals_free;
      }
    }

    if ( !error )
      *aglobals = globals;

    return error;
  }


  static FT_Error
  af_property_set( FT_Module    ft_module,
                   const char*  property_name,
                   const void*  value )
  {
    FT_Error   error  = FT_Err_Ok;
    AF_Module  module = (AF_Module)ft_module;


    if ( !ft_strcmp( property_name, "fallback-script" ) )
    {
      FT_UInt*  fallback_script = (FT_UInt*)value;

      FT_UInt  ss;


      /* We translate the fallback script to a fallback style that uses */
      /* `fallback-script' as its script and `AF_COVERAGE_NONE' as its  */
      /* coverage value.                                                */
      for ( ss = 0; AF_STYLE_CLASSES_GET[ss]; ss++ )
      {
        AF_StyleClass  style_class = AF_STYLE_CLASSES_GET[ss];


        if ( (FT_UInt)style_class->script == *fallback_script &&
             style_class->coverage == AF_COVERAGE_DEFAULT     )
        {
          module->fallback_style = ss;
          break;
        }
      }

      if ( !AF_STYLE_CLASSES_GET[ss] )
      {
        FT_TRACE0(( "af_property_set: Invalid value %d for property `%s'\n",
                    fallback_script, property_name ));
        return FT_THROW( Invalid_Argument );
      }

      return error;
    }
    else if ( !ft_strcmp( property_name, "default-script" ) )
    {
      FT_UInt*  default_script = (FT_UInt*)value;


      module->default_script = *default_script;

      return error;
    }
    else if ( !ft_strcmp( property_name, "increase-x-height" ) )
    {
      FT_Prop_IncreaseXHeight*  prop = (FT_Prop_IncreaseXHeight*)value;
      AF_FaceGlobals            globals;


      error = af_property_get_face_globals( prop->face, &globals, module );
      if ( !error )
        globals->increase_x_height = prop->limit;

      return error;
    }

    FT_TRACE0(( "af_property_set: missing property `%s'\n",
                property_name ));
    return FT_THROW( Missing_Property );
  }


  static FT_Error
  af_property_get( FT_Module    ft_module,
                   const char*  property_name,
                   void*        value )
  {
    FT_Error   error          = FT_Err_Ok;
    AF_Module  module         = (AF_Module)ft_module;
    FT_UInt    fallback_style = module->fallback_style;
    FT_UInt    default_script = module->default_script;


    if ( !ft_strcmp( property_name, "glyph-to-script-map" ) )
    {
      FT_Prop_GlyphToScriptMap*  prop = (FT_Prop_GlyphToScriptMap*)value;
      AF_FaceGlobals             globals;


      error = af_property_get_face_globals( prop->face, &globals, module );
      if ( !error )
        prop->map = globals->glyph_styles;

      return error;
    }
    else if ( !ft_strcmp( property_name, "fallback-script" ) )
    {
      FT_UInt*  val = (FT_UInt*)value;

      AF_StyleClass  style_class = AF_STYLE_CLASSES_GET[fallback_style];


      *val = style_class->script;

      return error;
    }
    else if ( !ft_strcmp( property_name, "default-script" ) )
    {
      FT_UInt*  val = (FT_UInt*)value;


      *val = default_script;

      return error;
    }
    else if ( !ft_strcmp( property_name, "increase-x-height" ) )
    {
      FT_Prop_IncreaseXHeight*  prop = (FT_Prop_IncreaseXHeight*)value;
      AF_FaceGlobals            globals;


      error = af_property_get_face_globals( prop->face, &globals, module );
      if ( !error )
        prop->limit = globals->increase_x_height;

      return error;
    }


    FT_TRACE0(( "af_property_get: missing property `%s'\n",
                property_name ));
    return FT_THROW( Missing_Property );
  }


  FT_DEFINE_SERVICE_PROPERTIESREC(
    af_service_properties,
    (FT_Properties_SetFunc)af_property_set,
    (FT_Properties_GetFunc)af_property_get )


  FT_DEFINE_SERVICEDESCREC1(
    af_services,
    FT_SERVICE_ID_PROPERTIES, &AF_SERVICE_PROPERTIES_GET )


  FT_CALLBACK_DEF( FT_Module_Interface )
  af_get_interface( FT_Module    module,
                    const char*  module_interface )
  {
    /* AF_SERVICES_GET dereferences `library' in PIC mode */
#ifdef FT_CONFIG_OPTION_PIC
    FT_Library  library;


    if ( !module )
      return NULL;
    library = module->library;
    if ( !library )
      return NULL;
#else
    FT_UNUSED( module );
#endif

    return ft_service_list_lookup( AF_SERVICES_GET, module_interface );
  }


  FT_CALLBACK_DEF( FT_Error )
  af_autofitter_init( FT_Module  ft_module )      /* AF_Module */
  {
    AF_Module  module = (AF_Module)ft_module;


    module->fallback_style = AF_STYLE_FALLBACK;
    module->default_script = AF_SCRIPT_DEFAULT;

    return af_loader_init( module );
  }


  FT_CALLBACK_DEF( void )
  af_autofitter_done( FT_Module  ft_module )      /* AF_Module */
  {
    AF_Module  module = (AF_Module)ft_module;


    af_loader_done( module );
  }


  FT_CALLBACK_DEF( FT_Error )
  af_autofitter_load_glyph( AF_Module     module,
                            FT_GlyphSlot  slot,
                            FT_Size       size,
                            FT_UInt       glyph_index,
                            FT_Int32      load_flags )
  {
    FT_UNUSED( size );

    return af_loader_load_glyph( module, slot->face,
                                 glyph_index, load_flags );
  }


  FT_DEFINE_AUTOHINTER_INTERFACE(
    af_autofitter_interface,
    NULL,                                                    /* reset_face */
    NULL,                                              /* get_global_hints */
    NULL,                                             /* done_global_hints */
    (FT_AutoHinter_GlyphLoadFunc)af_autofitter_load_glyph )  /* load_glyph */


  FT_DEFINE_MODULE(
    autofit_module_class,

    FT_MODULE_HINTER,
    sizeof ( AF_ModuleRec ),

    "autofitter",
    0x10000L,   /* version 1.0 of the autofitter  */
    0x20000L,   /* requires FreeType 2.0 or above */

    (const void*)&AF_INTERFACE_GET,

    (FT_Module_Constructor)af_autofitter_init,
    (FT_Module_Destructor) af_autofitter_done,
    (FT_Module_Requester)  af_get_interface )


/* END */

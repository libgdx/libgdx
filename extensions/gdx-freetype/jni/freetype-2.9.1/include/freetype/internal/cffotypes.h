/***************************************************************************/
/*                                                                         */
/*  cffotypes.h                                                            */
/*                                                                         */
/*    Basic OpenType/CFF object type definitions (specification).          */
/*                                                                         */
/*  Copyright 2017-2018 by                                                 */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#ifndef CFFOTYPES_H_
#define CFFOTYPES_H_

#include <ft2build.h>
#include FT_INTERNAL_OBJECTS_H
#include FT_INTERNAL_CFF_TYPES_H
#include FT_INTERNAL_TRUETYPE_TYPES_H
#include FT_SERVICE_POSTSCRIPT_CMAPS_H
#include FT_INTERNAL_POSTSCRIPT_HINTS_H


FT_BEGIN_HEADER


  typedef TT_Face  CFF_Face;


  /*************************************************************************/
  /*                                                                       */
  /* <Type>                                                                */
  /*    CFF_Size                                                           */
  /*                                                                       */
  /* <Description>                                                         */
  /*    A handle to an OpenType size object.                               */
  /*                                                                       */
  typedef struct  CFF_SizeRec_
  {
    FT_SizeRec  root;
    FT_ULong    strike_index;    /* 0xFFFFFFFF to indicate invalid */

  } CFF_SizeRec, *CFF_Size;


  /*************************************************************************/
  /*                                                                       */
  /* <Type>                                                                */
  /*    CFF_GlyphSlot                                                      */
  /*                                                                       */
  /* <Description>                                                         */
  /*    A handle to an OpenType glyph slot object.                         */
  /*                                                                       */
  typedef struct  CFF_GlyphSlotRec_
  {
    FT_GlyphSlotRec  root;

    FT_Bool  hint;
    FT_Bool  scaled;

    FT_Fixed  x_scale;
    FT_Fixed  y_scale;

  } CFF_GlyphSlotRec, *CFF_GlyphSlot;


  /*************************************************************************/
  /*                                                                       */
  /* <Type>                                                                */
  /*    CFF_Internal                                                       */
  /*                                                                       */
  /* <Description>                                                         */
  /*    The interface to the `internal' field of `FT_Size'.                */
  /*                                                                       */
  typedef struct  CFF_InternalRec_
  {
    PSH_Globals  topfont;
    PSH_Globals  subfonts[CFF_MAX_CID_FONTS];

  } CFF_InternalRec, *CFF_Internal;


  /*************************************************************************/
  /*                                                                       */
  /* Subglyph transformation record.                                       */
  /*                                                                       */
  typedef struct  CFF_Transform_
  {
    FT_Fixed    xx, xy;     /* transformation matrix coefficients */
    FT_Fixed    yx, yy;
    FT_F26Dot6  ox, oy;     /* offsets                            */

  } CFF_Transform;


FT_END_HEADER


#endif /* CFFOTYPES_H_ */


/* END */

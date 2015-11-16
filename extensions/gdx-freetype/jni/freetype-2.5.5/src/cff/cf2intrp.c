/***************************************************************************/
/*                                                                         */
/*  cf2intrp.c                                                             */
/*                                                                         */
/*    Adobe's CFF Interpreter (body).                                      */
/*                                                                         */
/*  Copyright 2007-2014 Adobe Systems Incorporated.                        */
/*                                                                         */
/*  This software, and all works of authorship, whether in source or       */
/*  object code form as indicated by the copyright notice(s) included      */
/*  herein (collectively, the "Work") is made available, and may only be   */
/*  used, modified, and distributed under the FreeType Project License,    */
/*  LICENSE.TXT.  Additionally, subject to the terms and conditions of the */
/*  FreeType Project License, each contributor to the Work hereby grants   */
/*  to any individual or legal entity exercising permissions granted by    */
/*  the FreeType Project License and this section (hereafter, "You" or     */
/*  "Your") a perpetual, worldwide, non-exclusive, no-charge,              */
/*  royalty-free, irrevocable (except as stated in this section) patent    */
/*  license to make, have made, use, offer to sell, sell, import, and      */
/*  otherwise transfer the Work, where such license applies only to those  */
/*  patent claims licensable by such contributor that are necessarily      */
/*  infringed by their contribution(s) alone or by combination of their    */
/*  contribution(s) with the Work to which such contribution(s) was        */
/*  submitted.  If You institute patent litigation against any entity      */
/*  (including a cross-claim or counterclaim in a lawsuit) alleging that   */
/*  the Work or a contribution incorporated within the Work constitutes    */
/*  direct or contributory patent infringement, then any patent licenses   */
/*  granted to You under this License for that Work shall terminate as of  */
/*  the date such litigation is filed.                                     */
/*                                                                         */
/*  By using, modifying, or distributing the Work you indicate that you    */
/*  have read and understood the terms and conditions of the               */
/*  FreeType Project License as well as those provided in this section,    */
/*  and you accept them fully.                                             */
/*                                                                         */
/***************************************************************************/


#include "cf2ft.h"
#include FT_INTERNAL_DEBUG_H

#include "cf2glue.h"
#include "cf2font.h"
#include "cf2stack.h"
#include "cf2hints.h"

#include "cf2error.h"


  /*************************************************************************/
  /*                                                                       */
  /* The macro FT_COMPONENT is used in trace mode.  It is an implicit      */
  /* parameter of the FT_TRACE() and FT_ERROR() macros, used to print/log  */
  /* messages during execution.                                            */
  /*                                                                       */
#undef  FT_COMPONENT
#define FT_COMPONENT  trace_cf2interp


  /* some operators are not implemented yet */
#define CF2_FIXME  FT_TRACE4(( "cf2_interpT2CharString:"            \
                               " operator not implemented yet\n" ))



  FT_LOCAL_DEF( void )
  cf2_hintmask_init( CF2_HintMask  hintmask,
                     FT_Error*     error )
  {
    FT_ZERO( hintmask );

    hintmask->error = error;
  }


  FT_LOCAL_DEF( FT_Bool )
  cf2_hintmask_isValid( const CF2_HintMask  hintmask )
  {
    return hintmask->isValid;
  }


  FT_LOCAL_DEF( FT_Bool )
  cf2_hintmask_isNew( const CF2_HintMask  hintmask )
  {
    return hintmask->isNew;
  }


  FT_LOCAL_DEF( void )
  cf2_hintmask_setNew( CF2_HintMask  hintmask,
                       FT_Bool       val )
  {
    hintmask->isNew = val;
  }


  /* clients call `getMaskPtr' in order to iterate */
  /* through hint mask                             */

  FT_LOCAL_DEF( FT_Byte* )
  cf2_hintmask_getMaskPtr( CF2_HintMask  hintmask )
  {
    return hintmask->mask;
  }


  static size_t
  cf2_hintmask_setCounts( CF2_HintMask  hintmask,
                          size_t        bitCount )
  {
    if ( bitCount > CF2_MAX_HINTS )
    {
      /* total of h and v stems must be <= 96 */
      CF2_SET_ERROR( hintmask->error, Invalid_Glyph_Format );
      return 0;
    }

    hintmask->bitCount  = bitCount;
    hintmask->byteCount = ( hintmask->bitCount + 7 ) / 8;

    hintmask->isValid = TRUE;
    hintmask->isNew   = TRUE;

    return bitCount;
  }


  /* consume the hintmask bytes from the charstring, advancing the src */
  /* pointer                                                           */
  static void
  cf2_hintmask_read( CF2_HintMask  hintmask,
                     CF2_Buffer    charstring,
                     size_t        bitCount )
  {
    size_t  i;

#ifndef CF2_NDEBUG
    /* these are the bits in the final mask byte that should be zero  */
    /* Note: this variable is only used in an assert expression below */
    /* and then only if CF2_NDEBUG is not defined                     */
    CF2_UInt  mask = ( 1 << ( -(CF2_Int)bitCount & 7 ) ) - 1;
#endif


    /* initialize counts and isValid */
    if ( cf2_hintmask_setCounts( hintmask, bitCount ) == 0 )
      return;

    FT_ASSERT( hintmask->byteCount > 0 );

    FT_TRACE4(( " (maskbytes:" ));

    /* set mask and advance interpreter's charstring pointer */
    for ( i = 0; i < hintmask->byteCount; i++ )
    {
      hintmask->mask[i] = (FT_Byte)cf2_buf_readByte( charstring );
      FT_TRACE4(( " 0x%02X", hintmask->mask[i] ));
    }

    FT_TRACE4(( ")\n" ));

    /* assert any unused bits in last byte are zero unless there's a prior */
    /* error                                                               */
    /* bitCount -> mask, 0 -> 0, 1 -> 7f, 2 -> 3f, ... 6 -> 3, 7 -> 1      */
#ifndef CF2_NDEBUG
    FT_ASSERT( ( hintmask->mask[hintmask->byteCount - 1] & mask ) == 0 ||
               *hintmask->error                                        );
#endif
  }


  FT_LOCAL_DEF( void )
  cf2_hintmask_setAll( CF2_HintMask  hintmask,
                       size_t        bitCount )
  {
    size_t    i;
    CF2_UInt  mask = ( 1 << ( -(CF2_Int)bitCount & 7 ) ) - 1;


    /* initialize counts and isValid */
    if ( cf2_hintmask_setCounts( hintmask, bitCount ) == 0 )
      return;

    FT_ASSERT( hintmask->byteCount > 0 );
    FT_ASSERT( hintmask->byteCount <
                 sizeof ( hintmask->mask ) / sizeof ( hintmask->mask[0] ) );

    /* set mask to all ones */
    for ( i = 0; i < hintmask->byteCount; i++ )
      hintmask->mask[i] = 0xFF;

    /* clear unused bits                                              */
    /* bitCount -> mask, 0 -> 0, 1 -> 7f, 2 -> 3f, ... 6 -> 3, 7 -> 1 */
    hintmask->mask[hintmask->byteCount - 1] &= ~mask;
  }


  /* Type2 charstring opcodes */
  enum
  {
    cf2_cmdRESERVED_0,   /* 0 */
    cf2_cmdHSTEM,        /* 1 */
    cf2_cmdRESERVED_2,   /* 2 */
    cf2_cmdVSTEM,        /* 3 */
    cf2_cmdVMOVETO,      /* 4 */
    cf2_cmdRLINETO,      /* 5 */
    cf2_cmdHLINETO,      /* 6 */
    cf2_cmdVLINETO,      /* 7 */
    cf2_cmdRRCURVETO,    /* 8 */
    cf2_cmdRESERVED_9,   /* 9 */
    cf2_cmdCALLSUBR,     /* 10 */
    cf2_cmdRETURN,       /* 11 */
    cf2_cmdESC,          /* 12 */
    cf2_cmdRESERVED_13,  /* 13 */
    cf2_cmdENDCHAR,      /* 14 */
    cf2_cmdRESERVED_15,  /* 15 */
    cf2_cmdRESERVED_16,  /* 16 */
    cf2_cmdRESERVED_17,  /* 17 */
    cf2_cmdHSTEMHM,      /* 18 */
    cf2_cmdHINTMASK,     /* 19 */
    cf2_cmdCNTRMASK,     /* 20 */
    cf2_cmdRMOVETO,      /* 21 */
    cf2_cmdHMOVETO,      /* 22 */
    cf2_cmdVSTEMHM,      /* 23 */
    cf2_cmdRCURVELINE,   /* 24 */
    cf2_cmdRLINECURVE,   /* 25 */
    cf2_cmdVVCURVETO,    /* 26 */
    cf2_cmdHHCURVETO,    /* 27 */
    cf2_cmdEXTENDEDNMBR, /* 28 */
    cf2_cmdCALLGSUBR,    /* 29 */
    cf2_cmdVHCURVETO,    /* 30 */
    cf2_cmdHVCURVETO     /* 31 */
  };

  enum
  {
    cf2_escDOTSECTION,   /* 0 */
    cf2_escRESERVED_1,   /* 1 */
    cf2_escRESERVED_2,   /* 2 */
    cf2_escAND,          /* 3 */
    cf2_escOR,           /* 4 */
    cf2_escNOT,          /* 5 */
    cf2_escRESERVED_6,   /* 6 */
    cf2_escRESERVED_7,   /* 7 */
    cf2_escRESERVED_8,   /* 8 */
    cf2_escABS,          /* 9 */
    cf2_escADD,          /* 10     like otherADD */
    cf2_escSUB,          /* 11     like otherSUB */
    cf2_escDIV,          /* 12 */
    cf2_escRESERVED_13,  /* 13 */
    cf2_escNEG,          /* 14 */
    cf2_escEQ,           /* 15 */
    cf2_escRESERVED_16,  /* 16 */
    cf2_escRESERVED_17,  /* 17 */
    cf2_escDROP,         /* 18 */
    cf2_escRESERVED_19,  /* 19 */
    cf2_escPUT,          /* 20     like otherPUT    */
    cf2_escGET,          /* 21     like otherGET    */
    cf2_escIFELSE,       /* 22     like otherIFELSE */
    cf2_escRANDOM,       /* 23     like otherRANDOM */
    cf2_escMUL,          /* 24     like otherMUL    */
    cf2_escRESERVED_25,  /* 25 */
    cf2_escSQRT,         /* 26 */
    cf2_escDUP,          /* 27     like otherDUP    */
    cf2_escEXCH,         /* 28     like otherEXCH   */
    cf2_escINDEX,        /* 29 */
    cf2_escROLL,         /* 30 */
    cf2_escRESERVED_31,  /* 31 */
    cf2_escRESERVED_32,  /* 32 */
    cf2_escRESERVED_33,  /* 33 */
    cf2_escHFLEX,        /* 34 */
    cf2_escFLEX,         /* 35 */
    cf2_escHFLEX1,       /* 36 */
    cf2_escFLEX1         /* 37 */
  };


  /* `stemHintArray' does not change once we start drawing the outline. */
  static void
  cf2_doStems( const CF2_Font  font,
               CF2_Stack       opStack,
               CF2_ArrStack    stemHintArray,
               CF2_Fixed*      width,
               FT_Bool*        haveWidth,
               CF2_Fixed       hintOffset )
  {
    CF2_UInt  i;
    CF2_UInt  count       = cf2_stack_count( opStack );
    FT_Bool   hasWidthArg = (FT_Bool)( count & 1 );

    /* variable accumulates delta values from operand stack */
    CF2_Fixed  position = hintOffset;

    if ( hasWidthArg && ! *haveWidth )
      *width = cf2_stack_getReal( opStack, 0 ) +
                 cf2_getNominalWidthX( font->decoder );

    if ( font->decoder->width_only )
      goto exit;

    for ( i = hasWidthArg ? 1 : 0; i < count; i += 2 )
    {
      /* construct a CF2_StemHint and push it onto the list */
      CF2_StemHintRec  stemhint;


      stemhint.min  =
        position   += cf2_stack_getReal( opStack, i );
      stemhint.max  =
        position   += cf2_stack_getReal( opStack, i + 1 );

      stemhint.used  = FALSE;
      stemhint.maxDS =
      stemhint.minDS = 0;

      cf2_arrstack_push( stemHintArray, &stemhint ); /* defer error check */
    }

    cf2_stack_clear( opStack );

  exit:
    /* cf2_doStems must define a width (may be default) */
    *haveWidth = TRUE;
  }


  static void
  cf2_doFlex( CF2_Stack       opStack,
              CF2_Fixed*      curX,
              CF2_Fixed*      curY,
              CF2_GlyphPath   glyphPath,
              const FT_Bool*  readFromStack,
              FT_Bool         doConditionalLastRead )
  {
    CF2_Fixed  vals[14];
    CF2_UInt   index;
    FT_Bool    isHFlex;
    CF2_Int    top, i, j;


    vals[0] = *curX;
    vals[1] = *curY;
    index   = 0;
    isHFlex = readFromStack[9] == FALSE;
    top     = isHFlex ? 9 : 10;

    for ( i = 0; i < top; i++ )
    {
      vals[i + 2] = vals[i];
      if ( readFromStack[i] )
        vals[i + 2] += cf2_stack_getReal( opStack, index++ );
    }

    if ( isHFlex )
      vals[9 + 2] = *curY;

    if ( doConditionalLastRead )
    {
      FT_Bool    lastIsX = (FT_Bool)( cf2_fixedAbs( vals[10] - *curX ) >
                                        cf2_fixedAbs( vals[11] - *curY ) );
      CF2_Fixed  lastVal = cf2_stack_getReal( opStack, index );


      if ( lastIsX )
      {
        vals[12] = vals[10] + lastVal;
        vals[13] = *curY;
      }
      else
      {
        vals[12] = *curX;
        vals[13] = vals[11] + lastVal;
      }
    }
    else
    {
      if ( readFromStack[10] )
        vals[12] = vals[10] + cf2_stack_getReal( opStack, index++ );
      else
        vals[12] = *curX;

      if ( readFromStack[11] )
        vals[13] = vals[11] + cf2_stack_getReal( opStack, index );
      else
        vals[13] = *curY;
    }

    for ( j = 0; j < 2; j++ )
      cf2_glyphpath_curveTo( glyphPath, vals[j * 6 + 2],
                                        vals[j * 6 + 3],
                                        vals[j * 6 + 4],
                                        vals[j * 6 + 5],
                                        vals[j * 6 + 6],
                                        vals[j * 6 + 7] );

    cf2_stack_clear( opStack );

    *curX = vals[12];
    *curY = vals[13];
  }


  /*
   * `error' is a shared error code used by many objects in this
   * routine.  Before the code continues from an error, it must check and
   * record the error in `*error'.  The idea is that this shared
   * error code will record the first error encountered.  If testing
   * for an error anyway, the cost of `goto exit' is small, so we do it,
   * even if continuing would be safe.  In this case, `lastError' is
   * set, so the testing and storing can be done in one place, at `exit'.
   *
   * Continuing after an error is intended for objects which do their own
   * testing of `*error', e.g., array stack functions.  This allows us to
   * avoid an extra test after the call.
   *
   * Unimplemented opcodes are ignored.
   *
   */
  FT_LOCAL_DEF( void )
  cf2_interpT2CharString( CF2_Font              font,
                          CF2_Buffer            buf,
                          CF2_OutlineCallbacks  callbacks,
                          const FT_Vector*      translation,
                          FT_Bool               doingSeac,
                          CF2_Fixed             curX,
                          CF2_Fixed             curY,
                          CF2_Fixed*            width )
  {
    /* lastError is used for errors that are immediately tested */
    FT_Error  lastError = FT_Err_Ok;

    /* pointer to parsed font object */
    CFF_Decoder*  decoder = font->decoder;

    FT_Error*  error  = &font->error;
    FT_Memory  memory = font->memory;

    CF2_Fixed  scaleY        = font->innerTransform.d;
    CF2_Fixed  nominalWidthX = cf2_getNominalWidthX( decoder );

    /* save this for hinting seac accents */
    CF2_Fixed  hintOriginY = curY;

    CF2_Stack  opStack = NULL;
    FT_Byte    op1;                       /* first opcode byte */

    /* instruction limit; 20,000,000 matches Avalon */
    FT_UInt32  instructionLimit = 20000000UL;

    CF2_ArrStackRec  subrStack;

    FT_Bool     haveWidth;
    CF2_Buffer  charstring = NULL;

    CF2_Int  charstringIndex = -1;       /* initialize to empty */

    /* TODO: placeholders for hint structures */

    /* objects used for hinting */
    CF2_ArrStackRec  hStemHintArray;
    CF2_ArrStackRec  vStemHintArray;

    CF2_HintMaskRec   hintMask;
    CF2_GlyphPathRec  glyphPath;


    /* initialize the remaining objects */
    cf2_arrstack_init( &subrStack,
                       memory,
                       error,
                       sizeof ( CF2_BufferRec ) );
    cf2_arrstack_init( &hStemHintArray,
                       memory,
                       error,
                       sizeof ( CF2_StemHintRec ) );
    cf2_arrstack_init( &vStemHintArray,
                       memory,
                       error,
                       sizeof ( CF2_StemHintRec ) );

    /* initialize CF2_StemHint arrays */
    cf2_hintmask_init( &hintMask, error );

    /* initialize path map to manage drawing operations */

    /* Note: last 4 params are used to handle `MoveToPermissive', which */
    /*       may need to call `hintMap.Build'                           */
    /* TODO: MoveToPermissive is gone; are these still needed?          */
    cf2_glyphpath_init( &glyphPath,
                        font,
                        callbacks,
                        scaleY,
                        /* hShift, */
                        &hStemHintArray,
                        &vStemHintArray,
                        &hintMask,
                        hintOriginY,
                        &font->blues,
                        translation );

    /*
     * Initialize state for width parsing.  From the CFF Spec:
     *
     *   The first stack-clearing operator, which must be one of hstem,
     *   hstemhm, vstem, vstemhm, cntrmask, hintmask, hmoveto, vmoveto,
     *   rmoveto, or endchar, takes an additional argument - the width (as
     *   described earlier), which may be expressed as zero or one numeric
     *   argument.
     *
     * What we implement here uses the first validly specified width, but
     * does not detect errors for specifying more than one width.
     *
     * If one of the above operators occurs without explicitly specifying
     * a width, we assume the default width.
     *
     */
    haveWidth = FALSE;
    *width    = cf2_getDefaultWidthX( decoder );

    /*
     * Note: at this point, all pointers to resources must be NULL
     * and all local objects must be initialized.
     * There must be no branches to exit: above this point.
     *
     */

    /* allocate an operand stack */
    opStack = cf2_stack_init( memory, error );
    if ( !opStack )
    {
      lastError = FT_THROW( Out_Of_Memory );
      goto exit;
    }

    /* initialize subroutine stack by placing top level charstring as */
    /* first element (max depth plus one for the charstring)          */
    /* Note: Caller owns and must finalize the first charstring.      */
    /*       Our copy of it does not change that requirement.         */
    cf2_arrstack_setCount( &subrStack, CF2_MAX_SUBR + 1 );

    charstring  = (CF2_Buffer)cf2_arrstack_getBuffer( &subrStack );
    *charstring = *buf;    /* structure copy */

    charstringIndex = 0;       /* entry is valid now */

    /* catch errors so far */
    if ( *error )
      goto exit;

    /* main interpreter loop */
    while ( 1 )
    {
      if ( cf2_buf_isEnd( charstring ) )
      {
        /* If we've reached the end of the charstring, simulate a */
        /* cf2_cmdRETURN or cf2_cmdENDCHAR.                       */
        if ( charstringIndex )
          op1 = cf2_cmdRETURN;  /* end of buffer for subroutine */
        else
          op1 = cf2_cmdENDCHAR; /* end of buffer for top level charstring */
      }
      else
        op1 = (FT_Byte)cf2_buf_readByte( charstring );

      /* check for errors once per loop */
      if ( *error )
        goto exit;

      instructionLimit--;
      if ( instructionLimit == 0 )
      {
        lastError = FT_THROW( Invalid_Glyph_Format );
        goto exit;
      }

      switch( op1 )
      {
      case cf2_cmdRESERVED_0:
      case cf2_cmdRESERVED_2:
      case cf2_cmdRESERVED_9:
      case cf2_cmdRESERVED_13:
      case cf2_cmdRESERVED_15:
      case cf2_cmdRESERVED_16:
      case cf2_cmdRESERVED_17:
        /* we may get here if we have a prior error */
        FT_TRACE4(( " unknown op (%d)\n", op1 ));
        break;

      case cf2_cmdHSTEMHM:
      case cf2_cmdHSTEM:
        FT_TRACE4(( op1 == cf2_cmdHSTEMHM ? " hstemhm\n" : " hstem\n" ));

        /* never add hints after the mask is computed */
        if ( cf2_hintmask_isValid( &hintMask ) )
        {
          FT_TRACE4(( "cf2_interpT2CharString:"
                      " invalid horizontal hint mask\n" ));
          break;
        }

        cf2_doStems( font,
                     opStack,
                     &hStemHintArray,
                     width,
                     &haveWidth,
                     0 );

        if ( font->decoder->width_only )
            goto exit;

        break;

      case cf2_cmdVSTEMHM:
      case cf2_cmdVSTEM:
        FT_TRACE4(( op1 == cf2_cmdVSTEMHM ? " vstemhm\n" : " vstem\n" ));

        /* never add hints after the mask is computed */
        if ( cf2_hintmask_isValid( &hintMask ) )
        {
          FT_TRACE4(( "cf2_interpT2CharString:"
                      " invalid vertical hint mask\n" ));
          break;
        }

        cf2_doStems( font,
                     opStack,
                     &vStemHintArray,
                     width,
                     &haveWidth,
                     0 );

        if ( font->decoder->width_only )
            goto exit;

        break;

      case cf2_cmdVMOVETO:
        FT_TRACE4(( " vmoveto\n" ));

        if ( cf2_stack_count( opStack ) > 1 && !haveWidth )
          *width = cf2_stack_getReal( opStack, 0 ) + nominalWidthX;

        /* width is defined or default after this */
        haveWidth = TRUE;

        if ( font->decoder->width_only )
            goto exit;

        curY += cf2_stack_popFixed( opStack );

        cf2_glyphpath_moveTo( &glyphPath, curX, curY );

        break;

      case cf2_cmdRLINETO:
        {
          CF2_UInt  index;
          CF2_UInt  count = cf2_stack_count( opStack );


          FT_TRACE4(( " rlineto\n" ));

          for ( index = 0; index < count; index += 2 )
          {
            curX += cf2_stack_getReal( opStack, index + 0 );
            curY += cf2_stack_getReal( opStack, index + 1 );

            cf2_glyphpath_lineTo( &glyphPath, curX, curY );
          }

          cf2_stack_clear( opStack );
        }
        continue; /* no need to clear stack again */

      case cf2_cmdHLINETO:
      case cf2_cmdVLINETO:
        {
          CF2_UInt  index;
          CF2_UInt  count = cf2_stack_count( opStack );

          FT_Bool  isX = op1 == cf2_cmdHLINETO;


          FT_TRACE4(( isX ? " hlineto\n" : " vlineto\n" ));

          for ( index = 0; index < count; index++ )
          {
            CF2_Fixed  v = cf2_stack_getReal( opStack, index );


            if ( isX )
              curX += v;
            else
              curY += v;

            isX = !isX;

            cf2_glyphpath_lineTo( &glyphPath, curX, curY );
          }

          cf2_stack_clear( opStack );
        }
        continue;

      case cf2_cmdRCURVELINE:
      case cf2_cmdRRCURVETO:
        {
          CF2_UInt  count = cf2_stack_count( opStack );
          CF2_UInt  index = 0;


          FT_TRACE4(( op1 == cf2_cmdRCURVELINE ? " rcurveline\n"
                                               : " rrcurveto\n" ));

          while ( index + 6 <= count )
          {
            CF2_Fixed  x1 = cf2_stack_getReal( opStack, index + 0 ) + curX;
            CF2_Fixed  y1 = cf2_stack_getReal( opStack, index + 1 ) + curY;
            CF2_Fixed  x2 = cf2_stack_getReal( opStack, index + 2 ) + x1;
            CF2_Fixed  y2 = cf2_stack_getReal( opStack, index + 3 ) + y1;
            CF2_Fixed  x3 = cf2_stack_getReal( opStack, index + 4 ) + x2;
            CF2_Fixed  y3 = cf2_stack_getReal( opStack, index + 5 ) + y2;


            cf2_glyphpath_curveTo( &glyphPath, x1, y1, x2, y2, x3, y3 );

            curX   = x3;
            curY   = y3;
            index += 6;
          }

          if ( op1 == cf2_cmdRCURVELINE )
          {
            curX += cf2_stack_getReal( opStack, index + 0 );
            curY += cf2_stack_getReal( opStack, index + 1 );

            cf2_glyphpath_lineTo( &glyphPath, curX, curY );
          }

          cf2_stack_clear( opStack );
        }
        continue; /* no need to clear stack again */

      case cf2_cmdCALLGSUBR:
      case cf2_cmdCALLSUBR:
        {
          CF2_UInt  subrIndex;


          FT_TRACE4(( op1 == cf2_cmdCALLGSUBR ? " callgsubr"
                                              : " callsubr" ));

          if ( charstringIndex > CF2_MAX_SUBR )
          {
            /* max subr plus one for charstring */
            lastError = FT_THROW( Invalid_Glyph_Format );
            goto exit;                      /* overflow of stack */
          }

          /* push our current CFF charstring region on subrStack */
          charstring = (CF2_Buffer)
                         cf2_arrstack_getPointer( &subrStack,
                                                  charstringIndex + 1 );

          /* set up the new CFF region and pointer */
          subrIndex = cf2_stack_popInt( opStack );

          switch ( op1 )
          {
          case cf2_cmdCALLGSUBR:
            FT_TRACE4(( "(%d)\n", subrIndex + decoder->globals_bias ));

            if ( cf2_initGlobalRegionBuffer( decoder,
                                             subrIndex,
                                             charstring ) )
            {
              lastError = FT_THROW( Invalid_Glyph_Format );
              goto exit;  /* subroutine lookup or stream error */
            }
            break;

          default:
            /* cf2_cmdCALLSUBR */
            FT_TRACE4(( "(%d)\n", subrIndex + decoder->locals_bias ));

            if ( cf2_initLocalRegionBuffer( decoder,
                                            subrIndex,
                                            charstring ) )
            {
              lastError = FT_THROW( Invalid_Glyph_Format );
              goto exit;  /* subroutine lookup or stream error */
            }
          }

          charstringIndex += 1;       /* entry is valid now */
        }
        continue; /* do not clear the stack */

      case cf2_cmdRETURN:
        FT_TRACE4(( " return\n" ));

        if ( charstringIndex < 1 )
        {
          /* Note: cannot return from top charstring */
          lastError = FT_THROW( Invalid_Glyph_Format );
          goto exit;                      /* underflow of stack */
        }

        /* restore position in previous charstring */
        charstring = (CF2_Buffer)
                       cf2_arrstack_getPointer( &subrStack,
                                                --charstringIndex );
        continue;     /* do not clear the stack */

      case cf2_cmdESC:
        {
          FT_Byte  op2 = (FT_Byte)cf2_buf_readByte( charstring );


          switch ( op2 )
          {
          case cf2_escDOTSECTION:
            /* something about `flip type of locking' -- ignore it */
            FT_TRACE4(( " dotsection\n" ));

            break;

          /* TODO: should these operators be supported? */
          case cf2_escAND: /* in spec */
            FT_TRACE4(( " and\n" ));

            CF2_FIXME;
            break;

          case cf2_escOR: /* in spec */
            FT_TRACE4(( " or\n" ));

            CF2_FIXME;
            break;

          case cf2_escNOT: /* in spec */
            FT_TRACE4(( " not\n" ));

            CF2_FIXME;
            break;

          case cf2_escABS: /* in spec */
            FT_TRACE4(( " abs\n" ));

            CF2_FIXME;
            break;

          case cf2_escADD: /* in spec */
            FT_TRACE4(( " add\n" ));

            CF2_FIXME;
            break;

          case cf2_escSUB: /* in spec */
            FT_TRACE4(( " sub\n" ));

            CF2_FIXME;
            break;

          case cf2_escDIV: /* in spec */
            FT_TRACE4(( " div\n" ));

            CF2_FIXME;
            break;

          case cf2_escNEG: /* in spec */
            FT_TRACE4(( " neg\n" ));

            CF2_FIXME;
            break;

          case cf2_escEQ: /* in spec */
            FT_TRACE4(( " eq\n" ));

            CF2_FIXME;
            break;

          case cf2_escDROP: /* in spec */
            FT_TRACE4(( " drop\n" ));

            CF2_FIXME;
            break;

          case cf2_escPUT: /* in spec */
            FT_TRACE4(( " put\n" ));

            CF2_FIXME;
            break;

          case cf2_escGET: /* in spec */
            FT_TRACE4(( " get\n" ));

            CF2_FIXME;
            break;

          case cf2_escIFELSE: /* in spec */
            FT_TRACE4(( " ifelse\n" ));

            CF2_FIXME;
            break;

          case cf2_escRANDOM: /* in spec */
            FT_TRACE4(( " random\n" ));

            CF2_FIXME;
            break;

          case cf2_escMUL: /* in spec */
            FT_TRACE4(( " mul\n" ));

            CF2_FIXME;
            break;

          case cf2_escSQRT: /* in spec */
            FT_TRACE4(( " sqrt\n" ));

            CF2_FIXME;
            break;

          case cf2_escDUP: /* in spec */
            FT_TRACE4(( " dup\n" ));

            CF2_FIXME;
            break;

          case cf2_escEXCH: /* in spec */
            FT_TRACE4(( " exch\n" ));

            CF2_FIXME;
            break;

          case cf2_escINDEX: /* in spec */
            FT_TRACE4(( " index\n" ));

            CF2_FIXME;
            break;

          case cf2_escROLL: /* in spec */
            FT_TRACE4(( " roll\n" ));

            CF2_FIXME;
            break;

          case cf2_escHFLEX:
            {
              static const FT_Bool  readFromStack[12] =
              {
                TRUE /* dx1 */, FALSE /* dy1 */,
                TRUE /* dx2 */, TRUE  /* dy2 */,
                TRUE /* dx3 */, FALSE /* dy3 */,
                TRUE /* dx4 */, FALSE /* dy4 */,
                TRUE /* dx5 */, FALSE /* dy5 */,
                TRUE /* dx6 */, FALSE /* dy6 */
              };


              FT_TRACE4(( " hflex\n" ));

              cf2_doFlex( opStack,
                          &curX,
                          &curY,
                          &glyphPath,
                          readFromStack,
                          FALSE /* doConditionalLastRead */ );
            }
            continue;

          case cf2_escFLEX:
            {
              static const FT_Bool  readFromStack[12] =
              {
                TRUE /* dx1 */, TRUE /* dy1 */,
                TRUE /* dx2 */, TRUE /* dy2 */,
                TRUE /* dx3 */, TRUE /* dy3 */,
                TRUE /* dx4 */, TRUE /* dy4 */,
                TRUE /* dx5 */, TRUE /* dy5 */,
                TRUE /* dx6 */, TRUE /* dy6 */
              };


              FT_TRACE4(( " flex\n" ));

              cf2_doFlex( opStack,
                          &curX,
                          &curY,
                          &glyphPath,
                          readFromStack,
                          FALSE /* doConditionalLastRead */ );
            }
            break;      /* TODO: why is this not a continue? */

          case cf2_escHFLEX1:
            {
              static const FT_Bool  readFromStack[12] =
              {
                TRUE /* dx1 */, TRUE  /* dy1 */,
                TRUE /* dx2 */, TRUE  /* dy2 */,
                TRUE /* dx3 */, FALSE /* dy3 */,
                TRUE /* dx4 */, FALSE /* dy4 */,
                TRUE /* dx5 */, TRUE  /* dy5 */,
                TRUE /* dx6 */, FALSE /* dy6 */
              };


              FT_TRACE4(( " hflex1\n" ));

              cf2_doFlex( opStack,
                          &curX,
                          &curY,
                          &glyphPath,
                          readFromStack,
                          FALSE /* doConditionalLastRead */ );
            }
            continue;

          case cf2_escFLEX1:
            {
              static const FT_Bool  readFromStack[12] =
              {
                TRUE  /* dx1 */, TRUE  /* dy1 */,
                TRUE  /* dx2 */, TRUE  /* dy2 */,
                TRUE  /* dx3 */, TRUE  /* dy3 */,
                TRUE  /* dx4 */, TRUE  /* dy4 */,
                TRUE  /* dx5 */, TRUE  /* dy5 */,
                FALSE /* dx6 */, FALSE /* dy6 */
              };


              FT_TRACE4(( " flex1\n" ));

              cf2_doFlex( opStack,
                          &curX,
                          &curY,
                          &glyphPath,
                          readFromStack,
                          TRUE /* doConditionalLastRead */ );
            }
            continue;

          case cf2_escRESERVED_1:
          case cf2_escRESERVED_2:
          case cf2_escRESERVED_6:
          case cf2_escRESERVED_7:
          case cf2_escRESERVED_8:
          case cf2_escRESERVED_13:
          case cf2_escRESERVED_16:
          case cf2_escRESERVED_17:
          case cf2_escRESERVED_19:
          case cf2_escRESERVED_25:
          case cf2_escRESERVED_31:
          case cf2_escRESERVED_32:
          case cf2_escRESERVED_33:
          default:
            FT_TRACE4(( " unknown op (12, %d)\n", op2 ));

          }; /* end of switch statement checking `op2' */

        } /* case cf2_cmdESC */
        break;

      case cf2_cmdENDCHAR:
        FT_TRACE4(( " endchar\n" ));

        if ( cf2_stack_count( opStack ) == 1 ||
             cf2_stack_count( opStack ) == 5 )
        {
          if ( !haveWidth )
            *width = cf2_stack_getReal( opStack, 0 ) + nominalWidthX;
        }

        /* width is defined or default after this */
        haveWidth = TRUE;

        if ( font->decoder->width_only )
            goto exit;

        /* close path if still open */
        cf2_glyphpath_closeOpenPath( &glyphPath );

        if ( cf2_stack_count( opStack ) > 1 )
        {
          /* must be either 4 or 5 --                       */
          /* this is a (deprecated) implied `seac' operator */

          CF2_UInt       achar;
          CF2_UInt       bchar;
          CF2_BufferRec  component;
          CF2_Fixed      dummyWidth;   /* ignore component width */
          FT_Error       error2;


          if ( doingSeac )
          {
            lastError = FT_THROW( Invalid_Glyph_Format );
            goto exit;      /* nested seac */
          }

          achar = cf2_stack_popInt( opStack );
          bchar = cf2_stack_popInt( opStack );

          curY = cf2_stack_popFixed( opStack );
          curX = cf2_stack_popFixed( opStack );

          error2 = cf2_getSeacComponent( decoder, achar, &component );
          if ( error2 )
          {
             lastError = error2;      /* pass FreeType error through */
             goto exit;
          }
          cf2_interpT2CharString( font,
                                  &component,
                                  callbacks,
                                  translation,
                                  TRUE,
                                  curX,
                                  curY,
                                  &dummyWidth );
          cf2_freeSeacComponent( decoder, &component );

          error2 = cf2_getSeacComponent( decoder, bchar, &component );
          if ( error2 )
          {
            lastError = error2;      /* pass FreeType error through */
            goto exit;
          }
          cf2_interpT2CharString( font,
                                  &component,
                                  callbacks,
                                  translation,
                                  TRUE,
                                  0,
                                  0,
                                  &dummyWidth );
          cf2_freeSeacComponent( decoder, &component );
        }
        goto exit;

      case cf2_cmdCNTRMASK:
      case cf2_cmdHINTMASK:
        /* the final \n in the tracing message gets added in      */
        /* `cf2_hintmask_read' (which also traces the mask bytes) */
        FT_TRACE4(( op1 == cf2_cmdCNTRMASK ? " cntrmask" : " hintmask" ));

        /* never add hints after the mask is computed */
        if ( cf2_stack_count( opStack ) > 1    &&
             cf2_hintmask_isValid( &hintMask ) )
        {
          FT_TRACE4(( "cf2_interpT2CharString: invalid hint mask\n" ));
          break;
        }

        /* if there are arguments on the stack, there this is an */
        /* implied cf2_cmdVSTEMHM                                */
        cf2_doStems( font,
                     opStack,
                     &vStemHintArray,
                     width,
                     &haveWidth,
                     0 );

        if ( font->decoder->width_only )
            goto exit;

        if ( op1 == cf2_cmdHINTMASK )
        {
          /* consume the hint mask bytes which follow the operator */
          cf2_hintmask_read( &hintMask,
                             charstring,
                             cf2_arrstack_size( &hStemHintArray ) +
                               cf2_arrstack_size( &vStemHintArray ) );
        }
        else
        {
          /*
           * Consume the counter mask bytes which follow the operator:
           * Build a temporary hint map, just to place and lock those
           * stems participating in the counter mask.  These are most
           * likely the dominant hstems, and are grouped together in a
           * few counter groups, not necessarily in correspondence
           * with the hint groups.  This reduces the chances of
           * conflicts between hstems that are initially placed in
           * separate hint groups and then brought together.  The
           * positions are copied back to `hStemHintArray', so we can
           * discard `counterMask' and `counterHintMap'.
           *
           */
          CF2_HintMapRec   counterHintMap;
          CF2_HintMaskRec  counterMask;


          cf2_hintmap_init( &counterHintMap,
                            font,
                            &glyphPath.initialHintMap,
                            &glyphPath.hintMoves,
                            scaleY );
          cf2_hintmask_init( &counterMask, error );

          cf2_hintmask_read( &counterMask,
                             charstring,
                             cf2_arrstack_size( &hStemHintArray ) +
                               cf2_arrstack_size( &vStemHintArray ) );
          cf2_hintmap_build( &counterHintMap,
                             &hStemHintArray,
                             &vStemHintArray,
                             &counterMask,
                             0,
                             FALSE );
        }
        break;

      case cf2_cmdRMOVETO:
        FT_TRACE4(( " rmoveto\n" ));

        if ( cf2_stack_count( opStack ) > 2 && !haveWidth )
          *width = cf2_stack_getReal( opStack, 0 ) + nominalWidthX;

        /* width is defined or default after this */
        haveWidth = TRUE;

        if ( font->decoder->width_only )
            goto exit;

        curY += cf2_stack_popFixed( opStack );
        curX += cf2_stack_popFixed( opStack );

        cf2_glyphpath_moveTo( &glyphPath, curX, curY );

        break;

      case cf2_cmdHMOVETO:
        FT_TRACE4(( " hmoveto\n" ));

        if ( cf2_stack_count( opStack ) > 1 && !haveWidth )
          *width = cf2_stack_getReal( opStack, 0 ) + nominalWidthX;

        /* width is defined or default after this */
        haveWidth = TRUE;

        if ( font->decoder->width_only )
            goto exit;

        curX += cf2_stack_popFixed( opStack );

        cf2_glyphpath_moveTo( &glyphPath, curX, curY );

        break;

      case cf2_cmdRLINECURVE:
        {
          CF2_UInt  count = cf2_stack_count( opStack );
          CF2_UInt  index = 0;


          FT_TRACE4(( " rlinecurve\n" ));

          while ( index + 6 < count )
          {
            curX += cf2_stack_getReal( opStack, index + 0 );
            curY += cf2_stack_getReal( opStack, index + 1 );

            cf2_glyphpath_lineTo( &glyphPath, curX, curY );
            index += 2;
          }

          while ( index < count )
          {
            CF2_Fixed  x1 = cf2_stack_getReal( opStack, index + 0 ) + curX;
            CF2_Fixed  y1 = cf2_stack_getReal( opStack, index + 1 ) + curY;
            CF2_Fixed  x2 = cf2_stack_getReal( opStack, index + 2 ) + x1;
            CF2_Fixed  y2 = cf2_stack_getReal( opStack, index + 3 ) + y1;
            CF2_Fixed  x3 = cf2_stack_getReal( opStack, index + 4 ) + x2;
            CF2_Fixed  y3 = cf2_stack_getReal( opStack, index + 5 ) + y2;


            cf2_glyphpath_curveTo( &glyphPath, x1, y1, x2, y2, x3, y3 );

            curX   = x3;
            curY   = y3;
            index += 6;
          }

          cf2_stack_clear( opStack );
        }
        continue; /* no need to clear stack again */

      case cf2_cmdVVCURVETO:
        {
          CF2_UInt  count = cf2_stack_count( opStack );
          CF2_UInt  index = 0;


          FT_TRACE4(( " vvcurveto\n" ));

          while ( index < count )
          {
            CF2_Fixed  x1, y1, x2, y2, x3, y3;


            if ( ( count - index ) & 1 )
            {
              x1 = cf2_stack_getReal( opStack, index ) + curX;

              ++index;
            }
            else
              x1 = curX;

            y1 = cf2_stack_getReal( opStack, index + 0 ) + curY;
            x2 = cf2_stack_getReal( opStack, index + 1 ) + x1;
            y2 = cf2_stack_getReal( opStack, index + 2 ) + y1;
            x3 = x2;
            y3 = cf2_stack_getReal( opStack, index + 3 ) + y2;

            cf2_glyphpath_curveTo( &glyphPath, x1, y1, x2, y2, x3, y3 );

            curX   = x3;
            curY   = y3;
            index += 4;
          }

          cf2_stack_clear( opStack );
        }
        continue; /* no need to clear stack again */

      case cf2_cmdHHCURVETO:
        {
          CF2_UInt  count = cf2_stack_count( opStack );
          CF2_UInt  index = 0;


          FT_TRACE4(( " hhcurveto\n" ));

          while ( index < count )
          {
            CF2_Fixed  x1, y1, x2, y2, x3, y3;


            if ( ( count - index ) & 1 )
            {
              y1 = cf2_stack_getReal( opStack, index ) + curY;

              ++index;
            }
            else
              y1 = curY;

            x1 = cf2_stack_getReal( opStack, index + 0 ) + curX;
            x2 = cf2_stack_getReal( opStack, index + 1 ) + x1;
            y2 = cf2_stack_getReal( opStack, index + 2 ) + y1;
            x3 = cf2_stack_getReal( opStack, index + 3 ) + x2;
            y3 = y2;

            cf2_glyphpath_curveTo( &glyphPath, x1, y1, x2, y2, x3, y3 );

            curX   = x3;
            curY   = y3;
            index += 4;
          }

          cf2_stack_clear( opStack );
        }
        continue; /* no need to clear stack again */

      case cf2_cmdVHCURVETO:
      case cf2_cmdHVCURVETO:
        {
          CF2_UInt  count = cf2_stack_count( opStack );
          CF2_UInt  index = 0;

          FT_Bool  alternate = op1 == cf2_cmdHVCURVETO;


          FT_TRACE4(( alternate ? " hvcurveto\n" : " vhcurveto\n" ));

          while ( index < count )
          {
            CF2_Fixed x1, x2, x3, y1, y2, y3;


            if ( alternate )
            {
              x1 = cf2_stack_getReal( opStack, index + 0 ) + curX;
              y1 = curY;
              x2 = cf2_stack_getReal( opStack, index + 1 ) + x1;
              y2 = cf2_stack_getReal( opStack, index + 2 ) + y1;
              y3 = cf2_stack_getReal( opStack, index + 3 ) + y2;

              if ( count - index == 5 )
              {
                x3 = cf2_stack_getReal( opStack, index + 4 ) + x2;

                ++index;
              }
              else
                x3 = x2;

              alternate = FALSE;
            }
            else
            {
              x1 = curX;
              y1 = cf2_stack_getReal( opStack, index + 0 ) + curY;
              x2 = cf2_stack_getReal( opStack, index + 1 ) + x1;
              y2 = cf2_stack_getReal( opStack, index + 2 ) + y1;
              x3 = cf2_stack_getReal( opStack, index + 3 ) + x2;

              if ( count - index == 5 )
              {
                y3 = cf2_stack_getReal( opStack, index + 4 ) + y2;

                ++index;
              }
              else
                y3 = y2;

              alternate = TRUE;
            }

            cf2_glyphpath_curveTo( &glyphPath, x1, y1, x2, y2, x3, y3 );

            curX   = x3;
            curY   = y3;
            index += 4;
          }

          cf2_stack_clear( opStack );
        }
        continue;     /* no need to clear stack again */

      case cf2_cmdEXTENDEDNMBR:
        {
          CF2_Int  v;


          v = (FT_Short)( ( cf2_buf_readByte( charstring ) << 8 ) |
                            cf2_buf_readByte( charstring )        );

          FT_TRACE4(( " %d", v ));

          cf2_stack_pushInt( opStack, v );
        }
        continue;

      default:
        /* numbers */
        {
          if ( /* op1 >= 32 && */ op1 <= 246 )
          {
            CF2_Int  v;


            v = op1 - 139;

            FT_TRACE4(( " %d", v ));

            /* -107 .. 107 */
            cf2_stack_pushInt( opStack, v );
          }

          else if ( /* op1 >= 247 && */ op1 <= 250 )
          {
            CF2_Int  v;


            v  = op1;
            v -= 247;
            v *= 256;
            v += cf2_buf_readByte( charstring );
            v += 108;

            FT_TRACE4(( " %d", v ));

            /* 108 .. 1131 */
            cf2_stack_pushInt( opStack, v );
          }

          else if ( /* op1 >= 251 && */ op1 <= 254 )
          {
            CF2_Int  v;


            v  = op1;
            v -= 251;
            v *= 256;
            v += cf2_buf_readByte( charstring );
            v  = -v - 108;

            FT_TRACE4(( " %d", v ));

            /* -1131 .. -108 */
            cf2_stack_pushInt( opStack, v );
          }

          else /* op1 == 255 */
          {
            CF2_Fixed  v;


            v = (CF2_Fixed)
                  ( ( (FT_UInt32)cf2_buf_readByte( charstring ) << 24 ) |
                    ( (FT_UInt32)cf2_buf_readByte( charstring ) << 16 ) |
                    ( (FT_UInt32)cf2_buf_readByte( charstring ) <<  8 ) |
                      (FT_UInt32)cf2_buf_readByte( charstring )         );

            FT_TRACE4(( " %.2f", v / 65536.0 ));

            cf2_stack_pushFixed( opStack, v );
          }
        }
        continue;   /* don't clear stack */

      } /* end of switch statement checking `op1' */

      cf2_stack_clear( opStack );

    } /* end of main interpreter loop */

    /* we get here if the charstring ends without cf2_cmdENDCHAR */
    FT_TRACE4(( "cf2_interpT2CharString:"
                "  charstring ends without ENDCHAR\n" ));

  exit:
    /* check whether last error seen is also the first one */
    cf2_setError( error, lastError );

    /* free resources from objects we've used */
    cf2_glyphpath_finalize( &glyphPath );
    cf2_arrstack_finalize( &vStemHintArray );
    cf2_arrstack_finalize( &hStemHintArray );
    cf2_arrstack_finalize( &subrStack );
    cf2_stack_free( opStack );

    FT_TRACE4(( "\n" ));

    return;
  }


/* END */

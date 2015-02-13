/***************************************************************************/
/*                                                                         */
/*  afranges.c                                                             */
/*                                                                         */
/*    Auto-fitter Unicode script ranges (body).                            */
/*                                                                         */
/*  Copyright 2013, 2014 by                                                */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#include "afranges.h"


  const AF_Script_UniRangeRec  af_cyrl_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0400UL,  0x04FFUL ),  /* Cyrillic            */
    AF_UNIRANGE_REC(  0x0500UL,  0x052FUL ),  /* Cyrillic Supplement */
    AF_UNIRANGE_REC(  0x2DE0UL,  0x2DFFUL ),  /* Cyrillic Extended-A */
    AF_UNIRANGE_REC(  0xA640UL,  0xA69FUL ),  /* Cyrillic Extended-B */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  /* there are some characters in the Devanagari Unicode block that are    */
  /* generic to Indic scripts; we omit them so that their presence doesn't */
  /* trigger Devanagari                                                    */

  const AF_Script_UniRangeRec  af_deva_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0900UL,  0x093BUL ),  /* Devanagari       */
    /* omitting U+093C nukta */
    AF_UNIRANGE_REC(  0x093DUL,  0x0950UL ),
    /* omitting U+0951 udatta, U+0952 anudatta */
    AF_UNIRANGE_REC(  0x0953UL,  0x0963UL ),
    /* omitting U+0964 danda, U+0965 double danda */
    AF_UNIRANGE_REC(  0x0966UL,  0x097FUL ),
    AF_UNIRANGE_REC(  0x20B9UL,  0x20B9UL ),  /* (new) Rupee sign */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_grek_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0370UL,  0x03FFUL ),  /* Greek and Coptic */
    AF_UNIRANGE_REC(  0x1F00UL,  0x1FFFUL ),  /* Greek Extended   */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_hebr_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0590UL,  0x05FFUL ),  /* Hebrew                          */
    AF_UNIRANGE_REC(  0xFB1DUL,  0xFB4FUL ),  /* Alphab. Present. Forms (Hebrew) */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_latn_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0020UL,  0x007FUL ),  /* Basic Latin (no control chars)         */
    AF_UNIRANGE_REC(  0x00A0UL,  0x00FFUL ),  /* Latin-1 Supplement (no control chars)  */
    AF_UNIRANGE_REC(  0x0100UL,  0x017FUL ),  /* Latin Extended-A                       */
    AF_UNIRANGE_REC(  0x0180UL,  0x024FUL ),  /* Latin Extended-B                       */
    AF_UNIRANGE_REC(  0x0250UL,  0x02AFUL ),  /* IPA Extensions                         */
    AF_UNIRANGE_REC(  0x02B0UL,  0x02FFUL ),  /* Spacing Modifier Letters               */
    AF_UNIRANGE_REC(  0x0300UL,  0x036FUL ),  /* Combining Diacritical Marks            */
    AF_UNIRANGE_REC(  0x1D00UL,  0x1D7FUL ),  /* Phonetic Extensions                    */
    AF_UNIRANGE_REC(  0x1D80UL,  0x1DBFUL ),  /* Phonetic Extensions Supplement         */
    AF_UNIRANGE_REC(  0x1DC0UL,  0x1DFFUL ),  /* Combining Diacritical Marks Supplement */
    AF_UNIRANGE_REC(  0x1E00UL,  0x1EFFUL ),  /* Latin Extended Additional              */
    AF_UNIRANGE_REC(  0x2000UL,  0x206FUL ),  /* General Punctuation                    */
    AF_UNIRANGE_REC(  0x2070UL,  0x209FUL ),  /* Superscripts and Subscripts            */
    AF_UNIRANGE_REC(  0x20A0UL,  0x20B8UL ),  /* Currency Symbols ...                   */
    AF_UNIRANGE_REC(  0x20BAUL,  0x20CFUL ),  /* ... except new Rupee sign              */
    AF_UNIRANGE_REC(  0x2150UL,  0x218FUL ),  /* Number Forms                           */
    AF_UNIRANGE_REC(  0x2460UL,  0x24FFUL ),  /* Enclosed Alphanumerics                 */
    AF_UNIRANGE_REC(  0x2C60UL,  0x2C7FUL ),  /* Latin Extended-C                       */
    AF_UNIRANGE_REC(  0x2E00UL,  0x2E7FUL ),  /* Supplemental Punctuation               */
    AF_UNIRANGE_REC(  0xA720UL,  0xA7FFUL ),  /* Latin Extended-D                       */
    AF_UNIRANGE_REC(  0xFB00UL,  0xFB06UL ),  /* Alphab. Present. Forms (Latin Ligs)    */
    AF_UNIRANGE_REC( 0x1D400UL, 0x1D7FFUL ),  /* Mathematical Alphanumeric Symbols      */
    AF_UNIRANGE_REC( 0x1F100UL, 0x1F1FFUL ),  /* Enclosed Alphanumeric Supplement       */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_none_uniranges[] =
  {
    AF_UNIRANGE_REC( 0UL, 0UL )
  };

  const AF_Script_UniRangeRec  af_telu_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0C00UL,  0x0C7FUL ),  /* Telugu */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

#ifdef AF_CONFIG_OPTION_INDIC

  const AF_Script_UniRangeRec  af_beng_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0980UL,  0x09FFUL ),  /* Bengali */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_gujr_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0A80UL,  0x0AFFUL ),  /* Gujarati */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_guru_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0A00UL,  0x0A7FUL ),  /* Gurmukhi */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_knda_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0C80UL,  0x0CFFUL ),  /* Kannada */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_limb_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x1900UL,  0x194FUL ),  /* Limbu */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_mlym_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0D00UL,  0x0D7FUL ),  /* Malayalam */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_orya_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0B00UL,  0x0B7FUL ),  /* Oriya */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_sinh_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0D80UL,  0x0DFFUL ),  /* Sinhala */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_sund_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x1B80UL,  0x1BBFUL ),  /* Sundanese */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_sylo_uniranges[] =
  {
    AF_UNIRANGE_REC(  0xA800UL,  0xA82FUL ),  /* Syloti Nagri */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_taml_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0B80UL,  0x0BFFUL ),  /* Tamil */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

  const AF_Script_UniRangeRec  af_tibt_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x0F00UL,  0x0FFFUL ),  /* Tibetan */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

#endif /* !AF_CONFIG_OPTION_INDIC */

#ifdef AF_CONFIG_OPTION_CJK

  /* this corresponds to Unicode 6.0 */

  const AF_Script_UniRangeRec  af_hani_uniranges[] =
  {
    AF_UNIRANGE_REC(  0x1100UL,  0x11FFUL ),  /* Hangul Jamo                             */
    AF_UNIRANGE_REC(  0x2E80UL,  0x2EFFUL ),  /* CJK Radicals Supplement                 */
    AF_UNIRANGE_REC(  0x2F00UL,  0x2FDFUL ),  /* Kangxi Radicals                         */
    AF_UNIRANGE_REC(  0x2FF0UL,  0x2FFFUL ),  /* Ideographic Description Characters      */
    AF_UNIRANGE_REC(  0x3000UL,  0x303FUL ),  /* CJK Symbols and Punctuation             */
    AF_UNIRANGE_REC(  0x3040UL,  0x309FUL ),  /* Hiragana                                */
    AF_UNIRANGE_REC(  0x30A0UL,  0x30FFUL ),  /* Katakana                                */
    AF_UNIRANGE_REC(  0x3100UL,  0x312FUL ),  /* Bopomofo                                */
    AF_UNIRANGE_REC(  0x3130UL,  0x318FUL ),  /* Hangul Compatibility Jamo               */
    AF_UNIRANGE_REC(  0x3190UL,  0x319FUL ),  /* Kanbun                                  */
    AF_UNIRANGE_REC(  0x31A0UL,  0x31BFUL ),  /* Bopomofo Extended                       */
    AF_UNIRANGE_REC(  0x31C0UL,  0x31EFUL ),  /* CJK Strokes                             */
    AF_UNIRANGE_REC(  0x31F0UL,  0x31FFUL ),  /* Katakana Phonetic Extensions            */
    AF_UNIRANGE_REC(  0x3200UL,  0x32FFUL ),  /* Enclosed CJK Letters and Months         */
    AF_UNIRANGE_REC(  0x3300UL,  0x33FFUL ),  /* CJK Compatibility                       */
    AF_UNIRANGE_REC(  0x3400UL,  0x4DBFUL ),  /* CJK Unified Ideographs Extension A      */
    AF_UNIRANGE_REC(  0x4DC0UL,  0x4DFFUL ),  /* Yijing Hexagram Symbols                 */
    AF_UNIRANGE_REC(  0x4E00UL,  0x9FFFUL ),  /* CJK Unified Ideographs                  */
    AF_UNIRANGE_REC(  0xA960UL,  0xA97FUL ),  /* Hangul Jamo Extended-A                  */
    AF_UNIRANGE_REC(  0xAC00UL,  0xD7AFUL ),  /* Hangul Syllables                        */
    AF_UNIRANGE_REC(  0xD7B0UL,  0xD7FFUL ),  /* Hangul Jamo Extended-B                  */
    AF_UNIRANGE_REC(  0xF900UL,  0xFAFFUL ),  /* CJK Compatibility Ideographs            */
    AF_UNIRANGE_REC(  0xFE10UL,  0xFE1FUL ),  /* Vertical forms                          */
    AF_UNIRANGE_REC(  0xFE30UL,  0xFE4FUL ),  /* CJK Compatibility Forms                 */
    AF_UNIRANGE_REC(  0xFF00UL,  0xFFEFUL ),  /* Halfwidth and Fullwidth Forms           */
    AF_UNIRANGE_REC( 0x1B000UL, 0x1B0FFUL ),  /* Kana Supplement                         */
    AF_UNIRANGE_REC( 0x1D300UL, 0x1D35FUL ),  /* Tai Xuan Hing Symbols                   */
    AF_UNIRANGE_REC( 0x1F200UL, 0x1F2FFUL ),  /* Enclosed Ideographic Supplement         */
    AF_UNIRANGE_REC( 0x20000UL, 0x2A6DFUL ),  /* CJK Unified Ideographs Extension B      */
    AF_UNIRANGE_REC( 0x2A700UL, 0x2B73FUL ),  /* CJK Unified Ideographs Extension C      */
    AF_UNIRANGE_REC( 0x2B740UL, 0x2B81FUL ),  /* CJK Unified Ideographs Extension D      */
    AF_UNIRANGE_REC( 0x2F800UL, 0x2FA1FUL ),  /* CJK Compatibility Ideographs Supplement */
    AF_UNIRANGE_REC(       0UL,       0UL )
  };

#endif /* !AF_CONFIG_OPTION_CJK */

/* END */

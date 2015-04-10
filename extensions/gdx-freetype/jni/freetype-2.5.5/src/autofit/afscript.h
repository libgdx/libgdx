/***************************************************************************/
/*                                                                         */
/*  afscript.h                                                             */
/*                                                                         */
/*    Auto-fitter scripts (specification only).                            */
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


  /* The following part can be included multiple times. */
  /* Define `SCRIPT' as needed.                         */


  /* Add new scripts here.  The first and second arguments are the    */
  /* script name in lowercase and uppercase, respectively, followed   */
  /* by a description string.  Then comes the corresponding HarfBuzz  */
  /* script name tag, followed by a string of standard characters (to */
  /* derive the standard width and height of stems).                  */

  SCRIPT( cyrl, CYRL,
          "Cyrillic",
          HB_SCRIPT_CYRILLIC,
          0x43E, 0x41E, 0x0 ) /* оО */

  SCRIPT( deva, DEVA,
          "Devanagari",
          HB_SCRIPT_DEVANAGARI,
          0x920, 0x935, 0x91F ) /* ठ व ट */

  SCRIPT( grek, GREK,
          "Greek",
          HB_SCRIPT_GREEK,
          0x3BF, 0x39F, 0x0 ) /* οΟ */

  SCRIPT( hebr, HEBR,
          "Hebrew",
          HB_SCRIPT_HEBREW,
          0x5DD, 0x0, 0x0 ) /* ם */

  SCRIPT( latn, LATN,
          "Latin",
          HB_SCRIPT_LATIN,
          'o', 'O', '0' )

  SCRIPT( none, NONE,
          "no script",
          HB_SCRIPT_INVALID,
          0x0, 0x0, 0x0 )

  /* there are no simple forms for letters; we thus use two digit shapes */
  SCRIPT( telu, TELU,
          "Telugu",
          HB_SCRIPT_TELUGU,
          0xC66, 0xC67, 0x0 ) /* ౦ ౧ */

#ifdef AF_CONFIG_OPTION_INDIC

  SCRIPT( beng, BENG,
          "Bengali",
          HB_SCRIPT_BENGALI,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( gujr, GUJR,
          "Gujarati",
          HB_SCRIPT_GUJARATI,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( guru, GURU,
          "Gurmukhi",
          HB_SCRIPT_GURMUKHI,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( knda, KNDA,
          "Kannada",
          HB_SCRIPT_KANNADA,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( limb, LIMB,
          "Limbu",
          HB_SCRIPT_LIMBU,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( mlym, MLYM,
          "Malayalam",
          HB_SCRIPT_MALAYALAM,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( orya, ORYA,
          "Oriya",
          HB_SCRIPT_ORIYA,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( sinh, SINH,
          "Sinhala",
          HB_SCRIPT_SINHALA,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( sund, SUND,
          "Sundanese",
          HB_SCRIPT_SUNDANESE,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( sylo, SYLO,
          "Syloti Nagri",
          HB_SCRIPT_SYLOTI_NAGRI,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( taml, TAML,
          "Tamil",
          HB_SCRIPT_TAMIL,
          'o', 0x0, 0x0 ) /* XXX */

  SCRIPT( tibt, TIBT,
          "Tibetan",
          HB_SCRIPT_TIBETAN,
          'o', 0x0, 0x0 ) /* XXX */

#endif /* AF_CONFIG_OPTION_INDIC */

#ifdef AF_CONFIG_OPTION_CJK

  SCRIPT( hani, HANI,
          "CJKV ideographs",
          HB_SCRIPT_HAN,
          0x7530, 0x56D7, 0x0 ) /* 田囗 */

#endif /* AF_CONFIG_OPTION_CJK */


/* END */

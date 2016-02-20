/***************************************************************************/
/*                                                                         */
/*  ftlcdfil.h                                                             */
/*                                                                         */
/*    FreeType API for color filtering of subpixel bitmap glyphs           */
/*    (specification).                                                     */
/*                                                                         */
/*  Copyright 2006-2015 by                                                 */
/*  David Turner, Robert Wilhelm, and Werner Lemberg.                      */
/*                                                                         */
/*  This file is part of the FreeType project, and may only be used,       */
/*  modified, and distributed under the terms of the FreeType project      */
/*  license, LICENSE.TXT.  By continuing to use, modify, or distribute     */
/*  this file you indicate that you have read the license and              */
/*  understand and accept it fully.                                        */
/*                                                                         */
/***************************************************************************/


#ifndef __FT_LCD_FILTER_H__
#define __FT_LCD_FILTER_H__

#include <ft2build.h>
#include FT_FREETYPE_H

#ifdef FREETYPE_H
#error "freetype.h of FreeType 1 has been loaded!"
#error "Please fix the directory search order for header files"
#error "so that freetype.h of FreeType 2 is found first."
#endif


FT_BEGIN_HEADER

  /***************************************************************************
   *
   * @section:
   *   lcd_filtering
   *
   * @title:
   *   LCD Filtering
   *
   * @abstract:
   *   Reduce color fringes of subpixel-rendered bitmaps.
   *
   * @description:
   *   Subpixel rendering exploits the color-striped structure of LCD
   *   pixels, increasing the available resolution in the direction of the
   *   stripe (usually horizontal RGB) by a factor of~3.  Since these
   *   subpixels are color pixels, using them unfiltered creates severe
   *   color fringes.  Use the @FT_Library_SetLcdFilter API to specify a
   *   low-pass filter, which is then applied to subpixel-rendered bitmaps
   *   generated through @FT_Render_Glyph.  The filter sacrifices some of
   *   the higher resolution to reduce color fringes, making the glyph image
   *   slightly blurrier.  Positional improvements will remain.
   *
   *   Note that no filter is active by default, and that this function is
   *   *not* implemented in default builds of the library.  You need to
   *   #define FT_CONFIG_OPTION_SUBPIXEL_RENDERING in your `ftoption.h' file
   *   in order to activate it.
   *
   *   A filter should have two properties:
   *
   *   1) It should be normalized, meaning the sum of the 5~components
   *      should be 256 (0x100).  It is possible to go above or under this
   *      target sum, however: going under means tossing out contrast, going
   *      over means invoking clamping and thereby non-linearities that
   *      increase contrast somewhat at the expense of greater distortion
   *      and color-fringing.  Contrast is better enhanced through stem
   *      darkening.
   *
   *   2) It should be color-balanced, meaning a filter `{~a, b, c, b, a~}'
   *      where a~+ b~=~c.  It distributes the computed coverage for one
   *      subpixel to all subpixels equally, sacrificing some won resolution
   *      but drastically reducing color-fringing.  Positioning improvements
   *      remain!  Note that color-fringing can only really be minimized
   *      when using a color-balanced filter and alpha-blending the glyph
   *      onto a surface in linear space; see @FT_Render_Glyph.
   *
   *   Regarding the form, a filter can be a `boxy' filter or a `beveled'
   *   filter.  Boxy filters are sharper but are less forgiving of non-ideal
   *   gamma curves of a screen (viewing angles!), beveled filters are
   *   fuzzier but more tolerant.
   *
   *   Examples:
   *
   *   - [0x10 0x40 0x70 0x40 0x10] is beveled and neither balanced nor
   *     normalized.
   *
   *   - [0x1A 0x33 0x4D 0x33 0x1A] is beveled and balanced but not
   *     normalized.
   *
   *   - [0x19 0x33 0x66 0x4c 0x19] is beveled and normalized but not
   *     balanced.
   *
   *   - [0x00 0x4c 0x66 0x4c 0x00] is boxily beveled and normalized but not
   *     balanced.
   *
   *   - [0x00 0x55 0x56 0x55 0x00] is boxy, normalized, and almost
   *     balanced.
   *
   *   - [0x08 0x4D 0x56 0x4D 0x08] is beveled, normalized and, almost
   *     balanced.
   *
   *   It is important to understand that linear alpha blending and gamma
   *   correction is critical for correctly rendering glyphs onto surfaces
   *   without artifacts and even more critical when subpixel rendering is
   *   involved.
   *
   *   Each of the 3~alpha values (subpixels) is independently used to blend
   *   one color channel.  That is, red alpha blends the red channel of the
   *   text color with the red channel of the background pixel.  The
   *   distribution of density values by the color-balanced filter assumes
   *   alpha blending is done in linear space; only then color artifacts
   *   cancel out.
   */


  /****************************************************************************
   *
   * @enum:
   *   FT_LcdFilter
   *
   * @description:
   *   A list of values to identify various types of LCD filters.
   *
   * @values:
   *   FT_LCD_FILTER_NONE ::
   *     Do not perform filtering.  When used with subpixel rendering, this
   *     results in sometimes severe color fringes.
   *
   *   FT_LCD_FILTER_DEFAULT ::
   *     The default filter reduces color fringes considerably, at the cost
   *     of a slight blurriness in the output.
   *
   *     It is a beveled, normalized, and color-balanced five-tap filter
   *     that is more forgiving to screens with non-ideal gamma curves and
   *     viewing angles.  Note that while color-fringing is reduced, it can
   *     only be minimized by using linear alpha blending and gamma
   *     correction to render glyphs onto surfaces.
   *
   *   FT_LCD_FILTER_LIGHT ::
   *     The light filter is a variant that is sharper at the cost of
   *     slightly more color fringes than the default one.
   *
   *     It is a boxy, normalized, and color-balanced three-tap filter that
   *     is less forgiving to screens with non-ideal gamma curves and
   *     viewing angles.  This filter works best when the rendering system
   *     uses linear alpha blending and gamma correction to render glyphs
   *     onto surfaces.
   *
   *   FT_LCD_FILTER_LEGACY ::
   *     This filter corresponds to the original libXft color filter.  It
   *     provides high contrast output but can exhibit really bad color
   *     fringes if glyphs are not extremely well hinted to the pixel grid.
   *     In other words, it only works well if the TrueType bytecode
   *     interpreter is enabled *and* high-quality hinted fonts are used.
   *
   *     This filter is only provided for comparison purposes, and might be
   *     disabled or stay unsupported in the future.
   *
   *   FT_LCD_FILTER_LEGACY1 ::
   *     For historical reasons, the FontConfig library returns a different
   *     enumeration value for legacy LCD filtering.  To make code work that
   *     (incorrectly) forwards FontConfig's enumeration value to
   *     @FT_Library_SetLcdFilter without proper mapping, it is thus easiest
   *     to have another enumeration value, which is completely equal to
   *     `FT_LCD_FILTER_LEGACY'.
   *
   * @since:
   *   2.3.0 (`FT_LCD_FILTER_LEGACY1' since 2.6.2)
   */
  typedef enum  FT_LcdFilter_
  {
    FT_LCD_FILTER_NONE    = 0,
    FT_LCD_FILTER_DEFAULT = 1,
    FT_LCD_FILTER_LIGHT   = 2,
    FT_LCD_FILTER_LEGACY1 = 3,
    FT_LCD_FILTER_LEGACY  = 16,

    FT_LCD_FILTER_MAX   /* do not remove */

  } FT_LcdFilter;


  /**************************************************************************
   *
   * @func:
   *   FT_Library_SetLcdFilter
   *
   * @description:
   *   This function is used to apply color filtering to LCD decimated
   *   bitmaps, like the ones used when calling @FT_Render_Glyph with
   *   @FT_RENDER_MODE_LCD or @FT_RENDER_MODE_LCD_V.
   *
   * @input:
   *   library ::
   *     A handle to the target library instance.
   *
   *   filter ::
   *     The filter type.
   *
   *     You can use @FT_LCD_FILTER_NONE here to disable this feature, or
   *     @FT_LCD_FILTER_DEFAULT to use a default filter that should work
   *     well on most LCD screens.
   *
   * @return:
   *   FreeType error code.  0~means success.
   *
   * @note:
   *   This feature is always disabled by default.  Clients must make an
   *   explicit call to this function with a `filter' value other than
   *   @FT_LCD_FILTER_NONE in order to enable it.
   *
   *   Due to *PATENTS* covering subpixel rendering, this function doesn't
   *   do anything except returning `FT_Err_Unimplemented_Feature' if the
   *   configuration macro FT_CONFIG_OPTION_SUBPIXEL_RENDERING is not
   *   defined in your build of the library, which should correspond to all
   *   default builds of FreeType.
   *
   *   The filter affects glyph bitmaps rendered through @FT_Render_Glyph,
   *   @FT_Outline_Get_Bitmap, @FT_Load_Glyph, and @FT_Load_Char.
   *
   *   It does _not_ affect the output of @FT_Outline_Render and
   *   @FT_Outline_Get_Bitmap.
   *
   *   If this feature is activated, the dimensions of LCD glyph bitmaps are
   *   either larger or taller than the dimensions of the corresponding
   *   outline with regards to the pixel grid.  For example, for
   *   @FT_RENDER_MODE_LCD, the filter adds up to 3~pixels to the left, and
   *   up to 3~pixels to the right.
   *
   *   The bitmap offset values are adjusted correctly, so clients shouldn't
   *   need to modify their layout and glyph positioning code when enabling
   *   the filter.
   *
   * @since:
   *   2.3.0
   */
  FT_EXPORT( FT_Error )
  FT_Library_SetLcdFilter( FT_Library    library,
                           FT_LcdFilter  filter );


  /**************************************************************************
   *
   * @func:
   *   FT_Library_SetLcdFilterWeights
   *
   * @description:
   *   Use this function to override the filter weights selected by
   *   @FT_Library_SetLcdFilter.  By default, FreeType uses the quintuple
   *   (0x00, 0x55, 0x56, 0x55, 0x00) for FT_LCD_FILTER_LIGHT, and (0x10,
   *   0x40, 0x70, 0x40, 0x10) for FT_LCD_FILTER_DEFAULT and
   *   FT_LCD_FILTER_LEGACY.
   *
   * @input:
   *   library ::
   *     A handle to the target library instance.
   *
   *   weights ::
   *     A pointer to an array; the function copies the first five bytes and
   *     uses them to specify the filter weights.
   *
   * @return:
   *   FreeType error code.  0~means success.
   *
   * @note:
   *   Due to *PATENTS* covering subpixel rendering, this function doesn't
   *   do anything except returning `FT_Err_Unimplemented_Feature' if the
   *   configuration macro FT_CONFIG_OPTION_SUBPIXEL_RENDERING is not
   *   defined in your build of the library, which should correspond to all
   *   default builds of FreeType.
   *
   *   This function must be called after @FT_Library_SetLcdFilter to have
   *   any effect.
   *
   * @since:
   *   2.4.0
   */
  FT_EXPORT( FT_Error )
  FT_Library_SetLcdFilterWeights( FT_Library      library,
                                  unsigned char  *weights );

  /* */


FT_END_HEADER

#endif /* __FT_LCD_FILTER_H__ */


/* END */

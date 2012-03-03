package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class FreeType {
	/*JNI
	#include <ft2build.h>
	#include FT_FREETYPE_H
	 */	
	
	private static class Pointer {
		long address;
		
		Pointer(long address) {
			this.address = address;
		}
	}
	
	public static class Library extends Pointer {
		Library (long address) {
			super(address);
		}
	}
	
	public static class Face extends Pointer {
		public Face (long address) {
			super(address);
		}
		
		public static native int getFaceFlags(long face); /*
			return ((FT_Face)face)->face_flags;
		*/
		
		public static native int getStyleFlags(long face); /*
			return ((FT_Face)face)->style_flags;
		*/
		
		public static native int getNumGlyphs(long face); /*
			return ((FT_Face)face)->num_glyphs;
		*/
		
		public static native int getAscender(long face); /*
			return ((FT_Face)face)->ascender;
		*/
		
		public static native int getDescender(long face); /*
			return ((FT_Face)face)->descender;
		*/
		
		public static native int getHeight(long face); /*
			return ((FT_Face)face)->height;
		*/
		
		public static native int getMaxAdvanceWidth(long face); /*
			return ((FT_Face)face)->max_advance_width;
		*/
		
		public static native int getMaxAdvanceHeight(long face); /*
			return ((FT_Face)face)->max_advance_height;
		*/
		
		public static native int getUnderlinePosition(long face); /*
			return ((FT_Face)face)->underline_position;
		*/
		
		public static native int getUnderlineThickness(long face); /*
			return ((FT_Face)face)->underline_thickness;
		*/
		
		public static native long getGlyph(long face); /*
			return (jlong)((FT_Face)face)->glyph;
		*/
		
		public static native long getSize(long face); /*
			return (jlong)((FT_Face)face)->size;
		*/
	}
	
	public static class GlyphSlot extends Pointer {
		GlyphSlot (long address) {
			super(address);
		}
		
		public static native long getMetrics(long slot); /*
			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		*/
		
		public static native int getLinearHoriAdvance(long slot); /*
			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		*/
		
		public static native int getLinearVertAdvance(long slot); /*
			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		*/
		
		public static native int getAdvanceX(long slot); /*
			return ((FT_GlyphSlot)slot)->advance.x;
		*/
		
		public static native int getAdvanceY(long slot); /*
			return ((FT_GlyphSlot)slot)->advance.y;
		*/
		
		public static native int getFormat(long slot); /*
			return ((FT_GlyphSlot)slot)->format;
		*/
		
		public static native long getBitmap(long slot); /*
			return (jlong)&((FT_GlyphSlot)slot)->bitmap;
		*/
		
		public static native int getBitmapLeft(long slot); /*
			return ((FT_GlyphSlot)slot)->bitmap_left;
		*/
		
		public static native int getBitmapTop(long slot); /*
			return ((FT_GlyphSlot)slot)->bitmap_top;
		*/
	}
	
	public static class Bitmap extends Pointer {
		Bitmap (long address) {
			super(address);
		}
		
		public static native int getRows(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->rows;
		*/
		
		public static native int getWidth(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->width;
		*/
		
		public static native int getPitch(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->pitch;
		*/
		
		public static native long getBuffer(long bitmap); /*
			return (jlong)((FT_Bitmap*)bitmap)->buffer;
		*/
		
		public static native int getNumGray(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->num_grays;
		*/
		
		public static native int getPixelMode(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->pixel_mode;
		*/
	}
	
	public static class GlyphMetrics extends Pointer {
		GlyphMetrics (long address) {
			super(address);
		}
		
		public static native int getWidth(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->width;
		*/
		
		public static native int getHeight(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->height;
		*/
		
		public static native int getHoriBearingX(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiBearingX;
		*/
		
		public static native int getHoriBearingY(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiBearingY;
		*/
		
		public static native int getHoriAdvance(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiAdvance;
		*/
		
		public static native int getVertBearingX(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->vertBearingX;
		*/
	
		public static native int getVertBearingY(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->vertBearingY;
		 */
	
		public static native int getVertAdvance(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->vertAdvance;
		*/
	}
	
   public static int FT_PIXEL_MODE_NONE = 0;
   public static int FT_PIXEL_MODE_MONO = 1;
   public static int FT_PIXEL_MODE_GRAY = 2;
   public static int FT_PIXEL_MODE_GRAY2 = 3;
   public static int FT_PIXEL_MODE_GRAY4 = 4;
   public static int FT_PIXEL_MODE_LCD = 5;
   public static int FT_PIXEL_MODE_LCD_V = 6;
	
	private static int encode (char a, char b, char c, char d) {
		return (a << 24) | (b << 16) | (c << 8) | d;
	}
   
	public static int FT_ENCODING_NONE = 0;
	public static int FT_ENCODING_MS_SYMBOL = encode('s', 'y', 'm', 'b');
	public static int FT_ENCODING_UNICODE = encode('u', 'n', 'i', 'c');
	public static int FT_ENCODING_SJIS = encode('s', 'j', 'i', 's');
	public static int FT_ENCODING_GB2312 = encode('g', 'b', ' ', ' ');
	public static int FT_ENCODING_BIG5 = encode('b', 'i', 'g', '5');
	public static int FT_ENCODING_WANSUNG = encode('w', 'a', 'n', 's');
	public static int FT_ENCODING_JOHAB = encode('j', 'o', 'h', 'a');
	public static int FT_ENCODING_ADOBE_STANDARD = encode('A', 'D', 'O', 'B');
	public static int FT_ENCODING_ADOBE_EXPERT = encode('A', 'D', 'B', 'E');
	public static int FT_ENCODING_ADOBE_CUSTOM = encode('A', 'D', 'B', 'C');
	public static int FT_ENCODING_ADOBE_LATIN_1 = encode('l', 'a', 't', '1');
	public static int FT_ENCODING_OLD_LATIN_2 = encode('l', 'a', 't', '2');
	public static int FT_ENCODING_APPLE_ROMAN = encode('a', 'r', 'm', 'n');
	
	public static int FT_FACE_FLAG_SCALABLE          = ( 1 <<  0 );
	public static int FT_FACE_FLAG_FIXED_SIZES       = ( 1 <<  1 );
	public static int FT_FACE_FLAG_FIXED_WIDTH       = ( 1 <<  2 );
	public static int FT_FACE_FLAG_SFNT              = ( 1 <<  3 );
	public static int FT_FACE_FLAG_HORIZONTAL        = ( 1 <<  4 );
	public static int FT_FACE_FLAG_VERTICAL          = ( 1 <<  5 );
	public static int FT_FACE_FLAG_KERNING           = ( 1 <<  6 );
	public static int FT_FACE_FLAG_FAST_GLYPHS       = ( 1 <<  7 );
	public static int FT_FACE_FLAG_MULTIPLE_MASTERS  = ( 1 <<  8 );
	public static int FT_FACE_FLAG_GLYPH_NAMES       = ( 1 <<  9 );
	public static int FT_FACE_FLAG_EXTERNAL_STREAM   = ( 1 << 10 );
	public static int FT_FACE_FLAG_HINTER            = ( 1 << 11 );
	public static int FT_FACE_FLAG_CID_KEYED         = ( 1 << 12 );
	public static int FT_FACE_FLAG_TRICKY            = ( 1 << 13 );
	
	public static int FT_STYLE_FLAG_ITALIC = ( 1 << 0 );
	public static int FT_STYLE_FLAG_BOLD   = ( 1 << 1 );
	
	public static int FT_LOAD_DEFAULT                      = 0x0;
	public static int FT_LOAD_NO_SCALE                     = 0x1;
	public static int FT_LOAD_NO_HINTING                   = 0x2;
	public static int FT_LOAD_RENDER                       = 0x4;
	public static int FT_LOAD_NO_BITMAP                    = 0x8;
	public static int FT_LOAD_VERTICAL_LAYOUT              = 0x10;
	public static int FT_LOAD_FORCE_AUTOHINT               = 0x20;
	public static int FT_LOAD_CROP_BITMAP                  = 0x40;
	public static int FT_LOAD_PEDANTIC                     = 0x80;
	public static int FT_LOAD_IGNORE_GLOBAL_ADVANCE_WIDTH  = 0x200;
	public static int FT_LOAD_NO_RECURSE                   = 0x400;
	public static int FT_LOAD_IGNORE_TRANSFORM             = 0x800;
	public static int FT_LOAD_MONOCHROME                   = 0x1000;
	public static int FT_LOAD_LINEAR_DESIGN                = 0x2000;
	public static int FT_LOAD_NO_AUTOHINT                  = 0x8000;
	
   public static int FT_RENDER_MODE_NORMAL = 0;
   public static int FT_RENDER_MODE_LIGHT = 1;
   public static int FT_RENDER_MODE_MONO = 2;
   public static int FT_RENDER_MODE_LCD = 3;
   public static int FT_RENDER_MODE_LCD_V = 4;
   public static int FT_RENDER_MODE_MAX = 5;
   
   public static int FT_KERNING_DEFAULT = 0;
   public static int FT_KERNING_UNFITTED = 1;
   public static int FT_KERNING_UNSCALED = 2;
	
	public static native long initFreeType(); /*
		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) return 0;
		else return (jlong)library;
	*/
	
	public static native void doneFreeType(long library); /*
		FT_Done_FreeType((FT_Library)library);
	*/
	
	public static native long newMemoryFace(long library, byte[] data, int dataSize, int faceIndex); /*
		FT_Face face = 0;
		FT_Error error = FT_New_Memory_Face((FT_Library)library, (const FT_Byte*)data, dataSize, faceIndex, &face);
		if(error) return 0;
		else return (jlong)face; 
	*/
	
	public static native void doneFace(long face); /*
		FT_Done_Face((FT_Face)face);
	*/
	
	public static native boolean selectSize(long face, int strike_index); /*
		return !FT_Select_Size((FT_Face)face, strike_index);
	*/
	
	public static native boolean setCharSize(long face, int charWidth, int charHeight, int horzResolution, int vertResolution); /*
		return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
	*/
	
	public static native boolean setPixelSizes(long face, int pixelWidth, int pixelHeight); /*
		return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
	*/
	
	public static native boolean loadGlyph(long face, int glyphIndex, int loadFlags); /*
		return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
	*/

	public static native boolean loadChar(long face, int charCode, int loadFlags); /*
		return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
	*/
	
	public static native void setTransform(long face, int a, int b, int c, int d, int deltaX, int deltaY); /*
	
	*/
	
	public static native boolean renderGlyph(long slot, int renderMode); /*
		return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
	*/
   
   public static native boolean hasKerning(long face); /*
   	return FT_HAS_KERNING(((FT_Face)face));
   */
   
   public static native int getKerning(long face, int leftGlyph, int rightGlyph, int kernMode); /*
   	FT_Vector kerning;
   	FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
   	if(error) return 0;
   	return kerning.x;
   */
	
   public static native int getCharIndex(long face, int charCode); /*
   	return FT_Get_Char_Index((FT_Face)face, charCode);
   */
   
	public static void main (String[] args) throws Exception {
		FreetypeBuild.main(args);
		new SharedLibraryLoader("libs/gdx-freetype-natives.jar").load("gdx-freetype");
		long library = FreeType.initFreeType();
		
		byte[] font = new FileHandle("Roboto-Condensed.ttf").readBytes();
		long face = FreeType.newMemoryFace(library, font, font.length, 0);
		System.out.println(FreeType.setCharSize(face, 0, 16, 96, 96));
		char left = 40;
		char right = 74;
		int kerning = FreeType.getKerning(face, FreeType.getCharIndex(face,  left), FreeType.getCharIndex(face,  right), 0);
		System.out.println(FreeType.hasKerning(face) + ", " + left + ", " + right + ", " + kerning);
		System.out.println(FreeType.getCharIndex(face, '('));
		FreeType.doneFace(face);
		FreeType.doneFreeType(library);
	}
}

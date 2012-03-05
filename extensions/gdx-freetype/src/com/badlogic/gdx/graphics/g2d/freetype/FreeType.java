package com.badlogic.gdx.graphics.g2d.freetype;

import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
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
		
		public int getFaceFlags() {
			return getFaceFlags(address);
		}
		
		private static native int getFaceFlags(long face); /*
			return ((FT_Face)face)->face_flags;
		*/
		
		public int getStyleFlags() {
			return getStyleFlags(address);
		}
		
		private static native int getStyleFlags(long face); /*
			return ((FT_Face)face)->style_flags;
		*/
		
		public int getNumGlyphs() {
			return getNumGlyphs(address);
		}
		
		private static native int getNumGlyphs(long face); /*
			return ((FT_Face)face)->num_glyphs;
		*/
		
		public int getAscender() {
			return getAscender(address);
		}
		
		private static native int getAscender(long face); /*
			return ((FT_Face)face)->ascender;
		*/
		
		public int getDescender() {
			return getDescender(address);
		}
		
		private static native int getDescender(long face); /*
			return ((FT_Face)face)->descender;
		*/
		
		public int getHeight() {
			return getHeight(address);
		}
		
		private static native int getHeight(long face); /*
			return ((FT_Face)face)->height;
		*/
		
		public int getMaxAdvanceWidth() {
			return getMaxAdvanceWidth(address);
		}
		
		private static native int getMaxAdvanceWidth(long face); /*
			return ((FT_Face)face)->max_advance_width;
		*/
		
		public int getMaxAdvanceHeight() {
			return getMaxAdvanceHeight(address);
		}
		
		private static native int getMaxAdvanceHeight(long face); /*
			return ((FT_Face)face)->max_advance_height;
		*/
		
		public int getUnderlinePosition() {
			return getUnderlinePosition(address);
		}
		
		private static native int getUnderlinePosition(long face); /*
			return ((FT_Face)face)->underline_position;
		*/
		
		public int getUnderlineThickness() {
			return getUnderlineThickness(address);
		}
		
		private static native int getUnderlineThickness(long face); /*
			return ((FT_Face)face)->underline_thickness;
		*/
		
		public GlyphSlot getGlyph() {
			return new GlyphSlot(getGlyph(address));
		}
		
		private static native long getGlyph(long face); /*
			return (jlong)((FT_Face)face)->glyph;
		*/
		
		public Size getSize() {
			return new Size(getSize(address));
		}
		
		private static native long getSize(long face); /*
			return (jlong)((FT_Face)face)->size;
		*/
	}
	
	public static class Size extends Pointer {
		Size (long address) {
			super(address);
		}
		
		public SizeMetrics getMetrics() {
			return new SizeMetrics(getMetrics(address));
		}
		
		private static native long getMetrics(long address); /*
			return (jlong)&((FT_Size)address)->metrics;
		*/
	}
	
	public static class SizeMetrics extends Pointer {
		SizeMetrics (long address) {
			super(address);
		}
		
		public int getXppem() {
			return getXppem(address);
		}
		
		private static native int getXppem(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->x_ppem;
		*/
		
		public int getYppem() {
			return getYppem(address);
		}
		
		private static native int getYppem(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->y_ppem;
		*/
		
		public int getXScale() {
			return getXscale(address);
		}
		
		private static native int getXscale(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->x_scale;
		*/
		
		public int getYscale() {
			return getYscale(address);
		}
		
		private static native int getYscale(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->x_scale;
		*/
		
		public int getAscender() {
			return getAscender(address);
		}
		
		private static native int getAscender(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->ascender;
		*/
		
		public int getDescender() {
			return getDescender(address);
		}
		
		private static native int getDescender(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->descender;
		*/
		
		public int getHeight() {
			return getHeight(address);
		}
		
		private static native int getHeight(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->height;
		*/
		
		public int getMaxAdvance() {
			return getMaxAdvance(address);
		}
		
		private static native int getMaxAdvance(long metrics); /*
			return ((FT_Size_Metrics*)metrics)->max_advance;
		*/
	}
	
	public static class GlyphSlot extends Pointer {
		GlyphSlot (long address) {
			super(address);
		}
		
		public GlyphMetrics getMetrics() {
			return new GlyphMetrics(getMetrics(address));
		}		
		
		private static native long getMetrics(long slot); /*
			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		*/
		
		public int getLinearHoriAdvance() {
			return getLinearHoriAdvance(address);
		}
		
		private static native int getLinearHoriAdvance(long slot); /*
			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		*/
		
		public int getLinearVertAdvance() {
			return getLinearVertAdvance(address);
		}
		
		private static native int getLinearVertAdvance(long slot); /*
			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		*/
		
		public int getAdvanceX() {
			return getAdvanceX(address);
		}
		
		private static native int getAdvanceX(long slot); /*
			return ((FT_GlyphSlot)slot)->advance.x;
		*/
		
		public int getAdvanceY() {
			return getAdvanceY(address);
		}
		
		private static native int getAdvanceY(long slot); /*
			return ((FT_GlyphSlot)slot)->advance.y;
		*/
		
		public int getFormat() {
			return getFormat(address);
		}
		
		private static native int getFormat(long slot); /*
			return ((FT_GlyphSlot)slot)->format;
		*/
		
		public Bitmap getBitmap() {
			return new Bitmap(getBitmap(address));
		}
		
		private static native long getBitmap(long slot); /*
			FT_GlyphSlot glyph = ((FT_GlyphSlot)slot);
			return (jlong)&(glyph->bitmap);
		*/
		
		public int getBitmapLeft() {
			return getBitmapLeft(address);
		}
		
		private static native int getBitmapLeft(long slot); /*
			return ((FT_GlyphSlot)slot)->bitmap_left;
		*/
		
		public int getBitmapTop() {
			return getBitmapTop(address);
		}
		
		private static native int getBitmapTop(long slot); /*
			return ((FT_GlyphSlot)slot)->bitmap_top;
		*/
	}
	
	public static class Bitmap extends Pointer {
		Bitmap (long address) {
			super(address);
		}
		
		public int getRows() {
			return getRows(address);
		}
		
		private static native int getRows(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->rows;
		*/
		
		public int getWidth() {
			return getWidth(address);
		}
		
		private static native int getWidth(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->width;
		*/
		
		public int getPitch() {
			return getPitch(address);
		}
		
		private static native int getPitch(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->pitch;
		*/
		
		public ByteBuffer getBuffer() {
			return getBuffer(address);
		}
		
		/**
		 * @return Pixmap representing the glyph, needs to be disposed manually.
		 */
		public Pixmap getPixmap(Format format) {
			Pixmap pixmap = new Pixmap(getWidth(), getRows(), Format.Alpha);
			BufferUtils.copy(getBuffer(address), pixmap.getPixels(), pixmap.getPixels().capacity());
			Pixmap converted = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), format);
			converted.drawPixmap(pixmap, 0, 0);
			return converted;
		}
		
		private static native ByteBuffer getBuffer(long bitmap); /*
			FT_Bitmap* bmp = (FT_Bitmap*)bitmap;
			return env->NewDirectByteBuffer((void*)bmp->buffer, bmp->rows * abs(bmp->pitch));
		*/
		
		public int getNumGray() {
			return getNumGray(address);
		}
		
		private static native int getNumGray(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->num_grays;
		*/
		
		public int getPixelMode() {
			return getPixelMode(address);
		}
		
		private static native int getPixelMode(long bitmap); /*
			return ((FT_Bitmap*)bitmap)->pixel_mode;
		*/
	}
	
	public static class GlyphMetrics extends Pointer {
		GlyphMetrics (long address) {
			super(address);
		}
		
		public int getWidth() {
			return getWidth(address);
		}
		
		private static native int getWidth(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->width;
		*/
		
		public int getHeight() {
			return getHeight(address);
		}
		
		private static native int getHeight(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->height;
		*/
		
		public int getHoriBearingX() {
			return getHoriBearingX(address);
		}
		
		private static native int getHoriBearingX(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiBearingX;
		*/
		
		public int getHoriBearingY() {
			return getHoriBearingY(address);
		}
		
		private static native int getHoriBearingY(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiBearingY;
		*/
		
		public int getHoriAdvance() {
			return getHoriAdvance(address);
		}
		
		private static native int getHoriAdvance(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->horiAdvance;
		*/
	
		public int getVertBearingX() {
			return getVertBearingX(address);
		}
		
		private static native int getVertBearingX(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->vertBearingX;
		*/
		
		public int getVertBearingY() {
			return getVertBearingY(address);
		}
	
		private static native int getVertBearingY(long metrics); /*
			return ((FT_Glyph_Metrics*)metrics)->vertBearingY;
		 */
		
		public int getVertAdvance() {
			return getVertAdvance(address);
		}
	
		private static native int getVertAdvance(long metrics); /*
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
	
   public static Library initFreeType() {
   	new SharedLibraryLoader().load("gdx-freetype");
   	long address = initFreeTypeJni();
   	if(address == 0) throw new GdxRuntimeException("Couldn't initialize FreeType library");
   	else return new Library(address);
   }
   
	private static native long initFreeTypeJni(); /*
		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) return 0;
		else return (jlong)library;
	*/
	
	public static void doneFreeType(Library library) {
		doneFreeType(library.address);
	}
	
	private static native void doneFreeType(long library); /*
		FT_Done_FreeType((FT_Library)library);
	*/
	
	public static Face newFace(Library library, FileHandle font, int faceIndex) {
		byte[] data = font.readBytes();
		return newMemoryFace(library, data, data.length, faceIndex);
	}
	
	public static Face newMemoryFace(Library library, byte[] data, int dataSize, int faceIndex) {
		long address = newMemoryFace(library.address, data, dataSize, faceIndex);
		if(address == 0) throw new GdxRuntimeException("Couldn't load font");
		else return new Face(address);
	}
	
	private static native long newMemoryFace(long library, byte[] data, int dataSize, int faceIndex); /*
		FT_Face face = 0;
		FT_Error error = FT_New_Memory_Face((FT_Library)library, (const FT_Byte*)data, dataSize, faceIndex, &face);
		if(error) return 0;
		else return (jlong)face; 
	*/
	
	public static void doneFace(Face face) {
		doneFace(face.address);
	}
	
	private static native void doneFace(long face); /*
		FT_Done_Face((FT_Face)face);
	*/
	
	public static boolean selectSize(Face face, int strikeIndex) {
		return selectSize(face.address, strikeIndex);
	}
	
	private static native boolean selectSize(long face, int strike_index); /*
		return !FT_Select_Size((FT_Face)face, strike_index);
	*/
	
	public static boolean setCharSize(Face face, int charWidth, int charHeight, int horzResolution, int vertResolution) {
		return setCharSize(face.address, charWidth, charHeight, horzResolution, vertResolution);
	}
	
	private static native boolean setCharSize(long face, int charWidth, int charHeight, int horzResolution, int vertResolution); /*
		return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
	*/
	
	public static boolean setPixelSizes(Face face, int pixelWidth, int pixelHeight) {
		return setPixelSizes(face.address, pixelWidth, pixelHeight);
	}
	
	private static native boolean setPixelSizes(long face, int pixelWidth, int pixelHeight); /*
		return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
	*/
	
	public static boolean loadGlyph(Face face, int glyphIndex, int loadFlags) {
		return loadGlyph(face.address, glyphIndex, loadFlags);
	}
	
	private static native boolean loadGlyph(long face, int glyphIndex, int loadFlags); /*
		return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
	*/

	public static boolean loadChar(Face face, int charCode, int loadFlags) {
		return loadChar(face.address, charCode, loadFlags);
	}
	
	private static native boolean loadChar(long face, int charCode, int loadFlags); /*
		return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
	*/
	
	public static boolean renderGlyph(GlyphSlot slot, int renderMode) {
		return renderGlyph(slot.address, renderMode);
	}
	
	private static native boolean renderGlyph(long slot, int renderMode); /*
		return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
	*/
   
	public static boolean hasKerning(Face face) {
		return hasKerning(face.address);
	}
	
	private static native boolean hasKerning(long face); /*
   	return FT_HAS_KERNING(((FT_Face)face));
   */
   
	public static int getKerning(Face face, int leftGlyph, int rightGlyph, int kernMode) {
		return getKerning(face.address, leftGlyph, rightGlyph, kernMode);
	}
	
	private static native int getKerning(long face, int leftGlyph, int rightGlyph, int kernMode); /*
   	FT_Vector kerning;
   	FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
   	if(error) return 0;
   	return kerning.x;
   */
	
	public static int getCharIndex(Face face, int charCode) {
		return getCharIndex(face.address, charCode);
	}
	
	private static native int getCharIndex(long face, int charCode); /*
   	return FT_Get_Char_Index((FT_Face)face, charCode);
   */
	
	public static int round26_6 (int value) {
		if (value < 0) return (int)((value - 32) >> 6);
		else return (int)((value + 32) >> 6);
	}
   
	public static void main (String[] args) throws Exception {
//		FreetypeBuild.main(args);
		new SharedLibraryLoader("libs/gdx-freetype-natives.jar").load("gdx-freetype");
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~* ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
		
		Library library = FreeType.initFreeType();
		Face face = FreeType.newFace(library, new FileHandle("arial.ttf"), 0);
		FreeType.setPixelSizes(face, 0, 15);
		SizeMetrics faceMetrics = face.getSize().getMetrics();
		System.out.println(round26_6(faceMetrics.getAscender()) + ", " + round26_6(faceMetrics.getDescender()) + ", " + round26_6(faceMetrics.getHeight()));
		
		for(int i = 0; i < chars.length(); i++) {
			if(!FreeType.loadGlyph(face, FreeType.getCharIndex(face, chars.charAt(i)), 0)) continue;
			if(!FreeType.renderGlyph(face.getGlyph(), FT_RENDER_MODE_NORMAL)) continue;
			Bitmap bitmap = face.getGlyph().getBitmap();
			GlyphMetrics glyphMetrics = face.getGlyph().getMetrics();
			System.out.println(round26_6(glyphMetrics.getWidth()) + ", " + round26_6(glyphMetrics.getHeight()) + ", " + round26_6(glyphMetrics.getHoriAdvance()));
			System.out.println(bitmap.getWidth() + ", " + bitmap.getRows() + ", " + bitmap.getPitch() + ", " + bitmap.getNumGray());
			for(int y = 0; y < bitmap.getRows(); y++) {
				for(int x = 0; x < bitmap.getWidth(); x++) {
					System.out.print(bitmap.getBuffer().get(x + bitmap.getPitch() * y) != 0? "X": " ");
				}
				System.out.println();
			}
		}
	
		FreeType.doneFace(face);
		FreeType.doneFreeType(library);
	}
}

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d.freetype;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.StreamUtils;

public class FreeType {
	// @off
	/*JNI
	#include <ft2build.h>
	#include FT_FREETYPE_H
	#include FT_STROKER_H
	
	static jint lastError = 0;	
	 */
	
	/**
	 * 
	 * @return returns the last error code FreeType reported
	 */
	static native int getLastErrorCode(); /*
		return lastError;
	*/
	
	private static class Pointer {
		long address;
		
		Pointer(long address) {
			this.address = address;
		}
	}
	
	public static class Library extends Pointer implements Disposable {
		LongMap<ByteBuffer> fontData = new LongMap<ByteBuffer>();
		
		Library (long address) {
			super(address);
		}

		@Override
		public void dispose () {
			doneFreeType(address);
			for(ByteBuffer buffer: fontData.values()) {
				if (BufferUtils.isUnsafeByteBuffer(buffer)) 
					BufferUtils.disposeUnsafeByteBuffer(buffer);
			}
		}

		private static native void doneFreeType(long library); /*
			FT_Done_FreeType((FT_Library)library);
		*/

		public Face newFace(FileHandle fontFile, int faceIndex) {
			ByteBuffer buffer = null;
			try {
				buffer = fontFile.map();
			} catch (GdxRuntimeException ignored) {
				// OK to ignore, some platforms do not support file mapping.
			}
			if (buffer == null) {
				InputStream input = fontFile.read();
				try {
					int fileSize = (int)fontFile.length();
					if (fileSize == 0) {
						// Copy to a byte[] to get the size, then copy to the buffer.
						byte[] data = StreamUtils.copyStreamToByteArray(input, 1024 * 16);
						buffer = BufferUtils.newUnsafeByteBuffer(data.length);
						BufferUtils.copy(data, 0, buffer, data.length);
					} else {
						// Trust the specified file size.
						buffer = BufferUtils.newUnsafeByteBuffer(fileSize);
						StreamUtils.copyStream(input, buffer);
					}
				} catch (IOException ex) {
					throw new GdxRuntimeException(ex);
				} finally {
					StreamUtils.closeQuietly(input);
				}
			}
			return newMemoryFace(buffer, faceIndex);
		}

		public Face newMemoryFace(byte[] data, int dataSize, int faceIndex) {
			ByteBuffer buffer = BufferUtils.newUnsafeByteBuffer(data.length);
			BufferUtils.copy(data, 0, buffer, data.length);
			return newMemoryFace(buffer, faceIndex);
		}

		public Face newMemoryFace(ByteBuffer buffer, int faceIndex) {
			long face = newMemoryFace(address, buffer, buffer.remaining(), faceIndex);
			if(face == 0) {
				if (BufferUtils.isUnsafeByteBuffer(buffer)) 
					BufferUtils.disposeUnsafeByteBuffer(buffer);
				throw new GdxRuntimeException("Couldn't load font, FreeType error code: " + getLastErrorCode());
			}
			else {
				fontData.put(face, buffer);
				return new Face(face, this);
			}
		}

		private static native long newMemoryFace(long library, ByteBuffer data, int dataSize, int faceIndex); /*
			FT_Face face = 0;
			FT_Error error = FT_New_Memory_Face((FT_Library)library, (const FT_Byte*)data, dataSize, faceIndex, &face);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)face;
		*/

		public Stroker createStroker() {
			long stroker = strokerNew(address);
			if(stroker == 0) throw new GdxRuntimeException("Couldn't create FreeType stroker, FreeType error code: " + getLastErrorCode());
			return new Stroker(stroker);
		}

		private static native long strokerNew(long library); /*
			FT_Stroker stroker;
			FT_Error error = FT_Stroker_New((FT_Library)library, &stroker);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)stroker;
		*/
	}
	
	public static class Face extends Pointer implements Disposable {
		Library library;
		
		public Face (long address, Library library) {
			super(address);
			this.library = library;
		}
		
		@Override
		public void dispose() {
			doneFace(address);
			ByteBuffer buffer = library.fontData.get(address);
			if(buffer != null) {
				library.fontData.remove(address);
				if (BufferUtils.isUnsafeByteBuffer(buffer)) 
					BufferUtils.disposeUnsafeByteBuffer(buffer);
			}
		}

		private static native void doneFace(long face); /*
			FT_Done_Face((FT_Face)face);
		*/

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
		
		public boolean selectSize(int strikeIndex) {
			return selectSize(address, strikeIndex);
		}

		private static native boolean selectSize(long face, int strike_index); /*
			return !FT_Select_Size((FT_Face)face, strike_index);
		*/

		public boolean setCharSize(int charWidth, int charHeight, int horzResolution, int vertResolution) {
			return setCharSize(address, charWidth, charHeight, horzResolution, vertResolution);
		}

		private static native boolean setCharSize(long face, int charWidth, int charHeight, int horzResolution, int vertResolution); /*
			return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
		*/

		public boolean setPixelSizes(int pixelWidth, int pixelHeight) {
			return setPixelSizes(address, pixelWidth, pixelHeight);
		}

		private static native boolean setPixelSizes(long face, int pixelWidth, int pixelHeight); /*
			return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
		*/

		public boolean loadGlyph(int glyphIndex, int loadFlags) {
			return loadGlyph(address, glyphIndex, loadFlags);
		}

		private static native boolean loadGlyph(long face, int glyphIndex, int loadFlags); /*
			return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
		*/

		public boolean loadChar(int charCode, int loadFlags) {
			return loadChar(address, charCode, loadFlags);
		}

		private static native boolean loadChar(long face, int charCode, int loadFlags); /*
			return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
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

		public boolean hasKerning() {
			return hasKerning(address);
		}

		private static native boolean hasKerning(long face); /*
			return FT_HAS_KERNING(((FT_Face)face));
		*/

		public int getKerning(int leftGlyph, int rightGlyph, int kernMode) {
			return getKerning(address, leftGlyph, rightGlyph, kernMode);
		}

		private static native int getKerning(long face, int leftGlyph, int rightGlyph, int kernMode); /*
			FT_Vector kerning;
			FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
			if(error) return 0;
			return kerning.x;
		*/

		public int getCharIndex(int charCode) {
			return getCharIndex(address, charCode);
		}

		private static native int getCharIndex(long face, int charCode); /*
			return FT_Get_Char_Index((FT_Face)face, charCode);
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

		public boolean renderGlyph(int renderMode) {
			return renderGlyph(address, renderMode);
		}

		private static native boolean renderGlyph(long slot, int renderMode); /*
			return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
		*/

		public Glyph getGlyph() {
			long glyph = getGlyph(address);
			if(glyph == 0) throw new GdxRuntimeException("Couldn't get glyph, FreeType error code: " + getLastErrorCode());
			return new Glyph(glyph);
		}

		private static native long getGlyph(long glyphSlot); /*
			FT_Glyph glyph;
			FT_Error error = FT_Get_Glyph((FT_GlyphSlot)glyphSlot, &glyph);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)glyph;
		*/
	}
	
	public static class Glyph extends Pointer implements Disposable {
		private boolean rendered;

		Glyph (long address) {
			super(address);
		}

		@Override
		public void dispose () {
			done(address);
		}

		private static native void done(long glyph); /*
			FT_Done_Glyph((FT_Glyph)glyph);
		*/

		public void strokeBorder(Stroker stroker, boolean inside) {
			address = strokeBorder(address, stroker.address, inside);
		}

		private static native long strokeBorder(long glyph, long stroker, boolean inside); /*
			FT_Glyph border_glyph = (FT_Glyph)glyph;
			FT_Glyph_StrokeBorder(&border_glyph, (FT_Stroker)stroker, inside, 1);
			return (jlong)border_glyph;
		*/

		public void toBitmap(int renderMode) {
			long bitmap = toBitmap(address, renderMode);
			if (bitmap == 0) throw new GdxRuntimeException("Couldn't render glyph, FreeType error code: " + getLastErrorCode());
			address = bitmap;
			rendered = true;
		}

		private static native long toBitmap(long glyph, int renderMode); /*
			FT_Glyph bitmap = (FT_Glyph)glyph;
			FT_Error error = FT_Glyph_To_Bitmap(&bitmap, (FT_Render_Mode)renderMode, NULL, 1);
			if(error) {
				lastError = error;
				return 0;
			}
			return (jlong)bitmap;
		*/

		public Bitmap getBitmap() {
			if (!rendered) {
				throw new GdxRuntimeException("Glyph is not yet rendered");
			}
			return new Bitmap(getBitmap(address));
		}

		private static native long getBitmap(long glyph); /*
			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return (jlong)&(glyph_bitmap->bitmap);
		*/

		public int getLeft() {
			if (!rendered) {
				throw new GdxRuntimeException("Glyph is not yet rendered");
			}
			return getLeft(address);
		}

		private static native int getLeft(long glyph); /*
			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return glyph_bitmap->left;
		*/

		public int getTop() {
			if (!rendered) {
				throw new GdxRuntimeException("Glyph is not yet rendered");
			}
			return getTop(address);
		}

		private static native int getTop(long glyph); /*
			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return glyph_bitmap->top;
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
			if (getRows() == 0)
				// Issue #768 - CheckJNI frowns upon env->NewDirectByteBuffer with NULL buffer or capacity 0
				//                  "JNI WARNING: invalid values for address (0x0) or capacity (0)"
				//              FreeType sets FT_Bitmap::buffer to NULL when the bitmap is empty (e.g. for ' ')
				//              JNICheck is on by default on emulators and might have a point anyway...
				//              So let's avoid this and just return a dummy non-null non-zero buffer
				return BufferUtils.newByteBuffer(1);
			return getBuffer(address);
		}

		private static native ByteBuffer getBuffer(long bitmap); /*
			FT_Bitmap* bmp = (FT_Bitmap*)bitmap;
			return env->NewDirectByteBuffer((void*)bmp->buffer, bmp->rows * abs(bmp->pitch));
		*/

		// @on
		public Pixmap getPixmap (Format format, Color color, float gamma) {
			int width = getWidth(), rows = getRows();
			ByteBuffer src = getBuffer();
			Pixmap pixmap;
			int pixelMode = getPixelMode();
			int rowBytes = Math.abs(getPitch()); // We currently ignore negative pitch.
			if (color == Color.WHITE && pixelMode == FT_PIXEL_MODE_GRAY && rowBytes == width && gamma == 1) {
				pixmap = new Pixmap(width, rows, Format.Alpha);
				BufferUtils.copy(src, pixmap.getPixels(), pixmap.getPixels().capacity());
			} else {
				pixmap = new Pixmap(width, rows, Format.RGBA8888);
				int rgba = Color.rgba8888(color);
				byte[] srcRow = new byte[rowBytes];
				int[] dstRow = new int[width];
				IntBuffer dst = pixmap.getPixels().asIntBuffer();
				if (pixelMode == FT_PIXEL_MODE_MONO) {
					// Use the specified color for each set bit.
					for (int y = 0; y < rows; y++) {
						src.get(srcRow);
						for (int i = 0, x = 0; x < width; i++, x += 8) {
							byte b = srcRow[i];
							for (int ii = 0, n = Math.min(8, width - x); ii < n; ii++) {
								if ((b & (1 << (7 - ii))) != 0)
									dstRow[x + ii] = rgba;
								else
									dstRow[x + ii] = 0;
							}
						}
						dst.put(dstRow);
					}
				} else {
					// Use the specified color for RGB, blend the FreeType bitmap with alpha.
					int rgb = rgba & 0xffffff00;
					int a = rgba & 0xff;
					for (int y = 0; y < rows; y++) {
						src.get(srcRow);
						for (int x = 0; x < width; x++) {
							// Zero raised to any power is always zero.
							// 255 (=one) raised to any power is always one.
							// We only need Math.pow() when alpha is NOT zero and NOT one.
							int alpha = srcRow[x] & 0xff;
							if (alpha == 0)
								dstRow[x] = rgb;
							else if (alpha == 255)
								dstRow[x] = rgb | a;
							else
								dstRow[x] = rgb | (int)(a * (float)Math.pow(alpha / 255f, gamma)); // Inverse gamma.
						}
						dst.put(dstRow);
					}
				}
			}

			Pixmap converted = pixmap;
			if (format != pixmap.getFormat()) {
				converted = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), format);
				converted.setBlending(Blending.None);
				converted.drawPixmap(pixmap, 0, 0);
				converted.setBlending(Blending.SourceOver);
				pixmap.dispose();
			}
			return converted;
		}
		// @off

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

	public static class Stroker extends Pointer implements Disposable {
		Stroker(long address) {
			super(address);
		}

		public void set(int radius, int lineCap, int lineJoin, int miterLimit) {
			set(address, radius, lineCap, lineJoin, miterLimit);
		}

		private static native void set(long stroker, int radius, int lineCap, int lineJoin, int miterLimit); /*
			FT_Stroker_Set((FT_Stroker)stroker, radius, (FT_Stroker_LineCap)lineCap, (FT_Stroker_LineJoin)lineJoin, miterLimit);
		*/

		@Override
		public void dispose() {
			done(address);
		}

		private static native void done(long stroker); /*
			FT_Stroker_Done((FT_Stroker)stroker);
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
	
	public static int FT_LOAD_TARGET_NORMAL                = 0x0;
	public static int FT_LOAD_TARGET_LIGHT                 = 0x10000;
	public static int FT_LOAD_TARGET_MONO                  = 0x20000;
	public static int FT_LOAD_TARGET_LCD                   = 0x30000;
	public static int FT_LOAD_TARGET_LCD_V                 = 0x40000;

   public static int FT_RENDER_MODE_NORMAL = 0;
   public static int FT_RENDER_MODE_LIGHT = 1;
   public static int FT_RENDER_MODE_MONO = 2;
   public static int FT_RENDER_MODE_LCD = 3;
   public static int FT_RENDER_MODE_LCD_V = 4;
   public static int FT_RENDER_MODE_MAX = 5;
   
   public static int FT_KERNING_DEFAULT = 0;
   public static int FT_KERNING_UNFITTED = 1;
   public static int FT_KERNING_UNSCALED = 2;
	
	public static int FT_STROKER_LINECAP_BUTT = 0;
	public static int FT_STROKER_LINECAP_ROUND = 1;
	public static int FT_STROKER_LINECAP_SQUARE = 2;

	public static int FT_STROKER_LINEJOIN_ROUND          = 0;
	public static int FT_STROKER_LINEJOIN_BEVEL          = 1;
	public static int FT_STROKER_LINEJOIN_MITER_VARIABLE = 2;
	public static int FT_STROKER_LINEJOIN_MITER          = FT_STROKER_LINEJOIN_MITER_VARIABLE;
	public static int FT_STROKER_LINEJOIN_MITER_FIXED    = 3;

   public static Library initFreeType() {   	
   	new SharedLibraryLoader().load("gdx-freetype");
   	long address = initFreeTypeJni();
   	if(address == 0)
   		throw new GdxRuntimeException("Couldn't initialize FreeType library, FreeType error code: " + getLastErrorCode());
   	else
   		return new Library(address);
   }
   
	private static native long initFreeTypeJni(); /*
		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) {
			lastError = error;
			return 0;
		}
		else return (jlong)library;
	*/

	public static int toInt (int value) {
		return ((value + 63) & -64) >> 6;
	}
   
//	public static void main (String[] args) throws Exception {
//		FreetypeBuild.main(args);
//		new SharedLibraryLoader("libs/gdx-freetype-natives.jar").load("gdx-freetype");
//		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*�?�?�?�?�? ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿À�?ÂÃÄÅÆÇÈÉÊËÌ�?Î�?�?ÑÒÓÔÕÖ×ØÙÚÛÜ�?Þßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
//		
//		Library library = FreeType.initFreeType();
//		Face face = library.newFace(new FileHandle("arial.ttf"), 0);
//		face.setPixelSizes(0, 15);
//		SizeMetrics faceMetrics = face.getSize().getMetrics();
//		System.out.println(toInt(faceMetrics.getAscender()) + ", " + toInt(faceMetrics.getDescender()) + ", " + toInt(faceMetrics.getHeight()));
//		
//		for(int i = 0; i < chars.length(); i++) {
//			if(!FreeType.loadGlyph(face, FreeType.getCharIndex(face, chars.charAt(i)), 0)) continue;
//			if(!FreeType.renderGlyph(face.getGlyph(), FT_RENDER_MODE_NORMAL)) continue;
//			Bitmap bitmap = face.getGlyph().getBitmap();
//			GlyphMetrics glyphMetrics = face.getGlyph().getMetrics();
//			System.out.println(toInt(glyphMetrics.getHoriBearingX()) + ", " + toInt(glyphMetrics.getHoriBearingY()));
//			System.out.println(toInt(glyphMetrics.getWidth()) + ", " + toInt(glyphMetrics.getHeight()) + ", " + toInt(glyphMetrics.getHoriAdvance()));
//			System.out.println(bitmap.getWidth() + ", " + bitmap.getRows() + ", " + bitmap.getPitch() + ", " + bitmap.getNumGray());
//			for(int y = 0; y < bitmap.getRows(); y++) {
//				for(int x = 0; x < bitmap.getWidth(); x++) {
//					System.out.print(bitmap.getBuffer().get(x + bitmap.getPitch() * y) != 0? "X": " ");
//				}
//				System.out.println();
//			}
//		}
//	
//		face.dispose();
//		library.dispose();
//	}
}

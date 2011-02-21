/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics;

import java.io.InputStream;
import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * <p>A Pixmap represents an image in memory. It has a width and height expressed in pixels as well as a {@link Format}
 * specifying the number and order of color components per pixel. Coordinates of pixels are specified with respect
 * to the top left corner of the image, with the x-axis pointing to the right and the y-axis pointing downwards.</p>
 * 
 * <p>By default all methods use blending. You can disable blending with {@link Pixmap#setBlending(Blending)}. The {@link
 * Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)} method will scale and stretch the source image
 * to a target image. There either nearest neighbour or bilinear filtering can be used.</p>
 * 
 * <p>A Pixmap stores its data in native heap memory. It is mandatory to call {@link Pixmap#dispose()} when the pixmap 
 * is no longer needed, otherwise memory leaks will result</p> 
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class Pixmap implements Disposable {
	/**
	 * Different pixel formats.
	 * 
	 * @author mzechner
	 * 
	 */
	public enum Format {
		Alpha, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888;
		
		static int toGdx2DPixmapFormat(Format format) {
			if(format == Alpha) return Gdx2DPixmap.GDX2D_FORMAT_ALPHA;
			if(format == LuminanceAlpha) return Gdx2DPixmap.GDX2D_FORMAT_LUMINANCE_ALPHA;
			if(format == RGB565) return Gdx2DPixmap.GDX2D_FORMAT_RGB565;
			if(format == RGBA4444) return Gdx2DPixmap.GDX2D_FORMAT_RGBA4444;
			if(format == RGB888) return Gdx2DPixmap.GDX2D_FORMAT_RGB888;
			if(format == RGBA8888) return Gdx2DPixmap.GDX2D_FORMAT_RGBA8888;
			throw new GdxRuntimeException("Unknown Format: " + format);
		}
		
		static Format fromGdx2DPixmapFormat(int format) {
			if(format == Gdx2DPixmap.GDX2D_FORMAT_ALPHA) return Alpha;
			if(format == Gdx2DPixmap.GDX2D_FORMAT_LUMINANCE_ALPHA) return LuminanceAlpha;
			if(format == Gdx2DPixmap.GDX2D_FORMAT_RGB565) return RGB565;
			if(format == Gdx2DPixmap.GDX2D_FORMAT_RGBA4444) return RGBA4444;
			if(format == Gdx2DPixmap.GDX2D_FORMAT_RGB888) return RGB888;
			if(format == Gdx2DPixmap.GDX2D_FORMAT_RGBA8888) return RGBA8888;
			throw new GdxRuntimeException("Unknown Gdx2DPixmap Format: " + format);
		}
	}

	/**
	 * Blending functions to be set with {@link Pixmap#setBlending}.
	 * @author mzechner
	 *
	 */
	public enum Blending {
		None, SourceOver
	}
	
	/**
	 * Filters to be used with {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.
	 * 
	 * @author mzechner
	 *
	 */
	public enum Filter {
		NearestNeighbour, BiLinear
	}
	
	final Gdx2DPixmap pixmap;
	int color = 0;
	
	/**
	 * Sets the type of {@link Blending} to be used for all operations.
	 * @param blending the blending type
	 */
	public static void setBlending(Blending blending) {
		Gdx2DPixmap.setBlend(blending==Blending.None?0:1);
	}
	
	/**
	 * Sets the type of interpolation {@link Filter} to be used in conjunction
	 * with {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.	
	 * @param filter the filter.
	 */
	public static void setFilter(Filter filter) {
		Gdx2DPixmap.setScale(filter==Filter.NearestNeighbour?Gdx2DPixmap.GDX2D_SCALE_NEAREST:Gdx2DPixmap.GDX2D_SCALE_LINEAR);
	}
	
	/**
	 * Creates a new Pixmap instance with the given width, height 
	 * and format.
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param format the {@link Format}
	 */
	public Pixmap(int width, int height, Format format) {
		pixmap = new Gdx2DPixmap(width, height, Format.toGdx2DPixmapFormat(format));
		setColor(0, 0, 0, 0);
		fill();		
	}
	
	/**
	 * Creates a new Pixmap instance from the given file. The file must
	 * be a Png, Jpeg or Bitmap. Paletted formats are not supported.
	 * 
	 * @param file the {@link FileHandle}
	 */
	public Pixmap(FileHandle file) {
		InputStream in = null;
		try {
			in = file.read();
			pixmap = new Gdx2DPixmap(in, 0);
		} catch (Exception e) {
			throw new GdxRuntimeException("couldn't load file '" + file.name() + "'", e);
		} finally {
			if(in != null);
			try { in.close(); } catch(Exception e) { };
		}
	}
	
	/**
	 * Sets the color for the following drawing operations.
	 * 
	 * @param r The red component.
	 * @param g The green component.
	 * @param b The blue component.
	 * @param a The alpha component.
	 */
	public void setColor (float r, float g, float b, float a) {
		color = Color.rgba8888(r, g, b, a);
	}

	/**
	 * Fills the complete bitmap with the currently set color.
	 */
	public void fill () {
		pixmap.clear(color);
	}

//	/**
//	 * Sets the width in pixels of strokes.
//	 * 
//	 * @param width The stroke width in pixels.
//	 */
//	public void setStrokeWidth (int width);

	/**
	 * Draws a line between the given coordinates using the currently set color.
	 * 
	 * @param x The x-coodinate of the first point
	 * @param y The y-coordinate of the first point
	 * @param x2 The x-coordinate of the first point
	 * @param y2 The y-coordinate of the first point
	 */
	public void drawLine (int x, int y, int x2, int y2) {
		pixmap.drawLine(x, y, x2, y2, color);
	}

	/**
	 * Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards)
	 * using the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels
	 */
	public void drawRectangle (int x, int y, int width, int height) {
		pixmap.drawRect(x, y, width, height, color);
	}

	/**
	 * Draws an area form another Pixmap to this Pixmap.
	 * 
	 * @param pixmap The other Pixmap
	 * @param x The target x-coordinate (top left corner)
	 * @param y The target y-coordinate (top left corner)
	 * @param srcx The source x-coordinate (top left corner)
	 * @param srcy The source y-coordinate (top left corner);
	 * @param srcWidth The width of the area form the other Pixmap in pixels
	 * @param srcHeight The height of the area form the other Pixmap in pixles
	 */
	public void drawPixmap (Pixmap pixmap, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight) {
		this.pixmap.drawPixmap(pixmap.pixmap, srcx, srcy, x, y, srcWidth, srcHeight);
	}
	
	/**
	 * Draws an area form another Pixmap to this Pixmap. This will automatically scale and stretch
	 * the source image to the specified target rectangle. Use {@link Pixmap#setFilter(Filter)} to
	 * specify the type of filtering to be used (nearest neighbour or bilinear).
	 * 
	 * @param pixmap The other Pixmap 
	 * @param srcx The source x-coordinate (top left corner)
	 * @param srcy The source y-coordinate (top left corner);
	 * @param srcWidth The width of the area form the other Pixmap in pixels
	 * @param srcHeight The height of the area form the other Pixmap in pixles
	 * @param dstx The target x-coordinate (top left corner)
	 * @param dsty The target y-coordinate (top left corner)
	 * @param dstWidth The target width
	 * @param dstHeight the target height
	 * 
	 */
	public void drawPixmap (Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight) {
		this.pixmap.drawPixmap(pixmap.pixmap, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
	}

	/**
	 * Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
	 * the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels
	 */
	public void fillRectangle (int x, int y, int width, int height) {
		pixmap.fillRect(x, y, width, height, color);
	}

	/**
	 * Draws a circle outline with the center at x,y and a radius using the current color and stroke width.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels
	 */
	public void drawCircle (int x, int y, int radius) {
		pixmap.drawCircle(x, y, radius, color);
	}

	/**
	 * Fills a circle with the center at x,y and a radius using the current color.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels
	 */
	public void fillCircle (int x, int y, int radius) {
		pixmap.fillCircle(x, y, radius, color);
	}

	/**
	 * Returns the 32-bit RGBA8888 value of the pixel at x, y. For
	 * Alpha formats the RGB components will be one. 
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @return The pixel color in RGBA8888 format.
	 */
	public int getPixel (int x, int y) {
		return pixmap.getPixel(x, y);
	}

	/**
	 * @return The width of the Pixmap in pixels.
	 */
	public int getWidth () {
		return pixmap.getWidth();
	}

	/**
	 * @return The height of the Pixmap in pixels.
	 */
	public int getHeight () {
		return pixmap.getHeight();
	}

	/**
	 * Releases all resources associated with this Pixmap.
	 */
	public void dispose () {
		pixmap.dispose();
	}

	/**
	 * Draws a pixel at the given location with the current color.
	 * 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	public void drawPixel(int x, int y) {
		pixmap.setPixel(x, y, color);
	}
	
	/**
	 * Returns the OpenGL ES format of this Pixmap. Used as the seventh
	 * parameter to {@link GLCommon#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
	 * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
	 */
	public int getGLFormat() {
		return pixmap.getGLFormat();
	}
	
	/**
	 * Returns the OpenGL ES format of this Pixmap. Used as the third
	 * parameter to {@link GLCommon#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
	 * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
	 */
	public int getGLInternalFormat() {
		return pixmap.getGLInternalFormat();
	}
	
	/**
	 * Returns the OpenGL ES type of this Pixmap. Used as the eighth parameter to 
	 * {@link GLCommon#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
	 * @return one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_4_4_4_4
	 */
	public int getGLType() {
		return pixmap.getGLType();
	}
	
	/**
	 * Returns the direct ByteBuffer holding the pixel data. For the format Alpha each value
	 * is encoded as a byte. For the format LuminanceAlpha the luminance is the first byte and
	 * the alpha is the second byte of the pixel. For the formats RGB888 and RGBA8888 the color
	 * components are stored in a single byte each in the order red, green, blue (alpha). For
	 * the formats RGB565 and RGBA4444 the pixel colors are stored in shorts in machine dependent
	 * order.
	 * @return the direct {@link ByteBuffer} holding the pixel data.
	 */
	public ByteBuffer getPixels() {
		return pixmap.getPixels();
	}

	/**
	 * @return the {@link Format} of this Pixmap.
	 */
	public Format getFormat() {
		return Format.fromGdx2DPixmapFormat(pixmap.getFormat());
	}
}

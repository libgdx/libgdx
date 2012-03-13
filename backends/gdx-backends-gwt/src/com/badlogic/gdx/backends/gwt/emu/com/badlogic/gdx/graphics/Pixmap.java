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

package com.badlogic.gdx.graphics;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.backends.gwt.GwtFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;

public class Pixmap implements Disposable {
	public static Map<Integer, Pixmap> pixmaps = new HashMap<Integer, Pixmap>();
	static int nextId = 0;

	/** Different pixel formats.
	 * 
	 * @author mzechner */
	public enum Format {
		Alpha, Intensity, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888;
	}
	
	/** Blending functions to be set with {@link Pixmap#setBlending}.
	 * @author mzechner */
	public enum Blending {
		None, SourceOver
	}

	/** Filters to be used with {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.
	 * 
	 * @author mzechner */
	public enum Filter {
		NearestNeighbour, BiLinear
	}

	int width;
	int height;
	Format format;
	Canvas canvas;
	Context2d context;
	int id;
	IntBuffer buffer;
	int r = 255, g = 255, b = 255;
	float a;
	String color = make(r, g, b, a);
	static Blending blending;
	CanvasPixelArray pixels;
	
	public Pixmap (FileHandle file) {
		GwtFileHandle gwtFile = (GwtFileHandle)file;
		ImageElement img = gwtFile.preloader.images.get(file.path());
		if(img == null) throw new GdxRuntimeException("Couldn't load image '" + file.path() + "', file does not exist");
		create(img.getWidth(), img.getHeight(), Format.RGBA8888);
		context.drawImage(img, 0, 0);
	}
	
	public Pixmap(ImageElement img) {
		create(img.getWidth(), img.getHeight(), Format.RGBA8888);
		context.drawImage(img, 0, 0);
	}

	public Pixmap (int width, int height, Format format) {
		create(width, height, format);
	}

	private void create (int width, int height, Format format2) {
		this.width = width;
		this.height = height;
		this.format = Format.RGBA8888;
		canvas = Canvas.createIfSupported();
		canvas.getCanvasElement().setWidth(width);
		canvas.getCanvasElement().setHeight(height);
		context = canvas.getContext2d();
		context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		buffer = BufferUtils.newIntBuffer(1);
		id = nextId++;
		buffer.put(0, id);
		pixmaps.put(id, this);
	}

	public static String make (int r2, int g2, int b2, float a2) {
		return "rgba(" + r2 + "," + g2 + "," + b2 + "," + a2 + ")";
	}


	/** Sets the type of {@link Blending} to be used for all operations. Default is {@link Blending#SourceOver}.
	 * @param blending the blending type */
	public static void setBlending (Blending blending) {
		Pixmap.blending = blending;
	}
	
	/** @return the currently set {@link Blending} */
	public static Blending getBlending () {
		return blending;
	}

	/** Sets the type of interpolation {@link Filter} to be used in conjunction with
	 * {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.
	 * @param filter the filter. */
	public static void setFilter (Filter filter) {
	}
	
	public Format getFormat () {
		return format;
	}

	public int getWidth () {
		return width;
	}

	public int getHeight () {
		return height;
	}

	public Buffer getPixels () {
		return buffer;
	}

	@Override
	public void dispose () {
		pixmaps.remove(id);
	}

	public CanvasElement getCanvasElement () {
		return canvas.getCanvasElement();
	}

	/** Sets the color for the following drawing operations
	 * @param color the color, encoded as RGBA8888 */
	public void setColor (int color) {
		a = ((color >>> 24) & 0xff) / 255f;
		r = (color >>> 16) & 0xff;
		g = (color >>> 8) & 0xff;
		b = (color & 0xff);
		this.color = make(r, g, b, a);
		context.setFillStyle(this.color);
		context.setStrokeStyle(this.color);
	}

	/** Sets the color for the following drawing operations.
	 * 
	 * @param r The red component.
	 * @param g The green component.
	 * @param b The blue component.
	 * @param a The alpha component. */
	public void setColor (float r, float g, float b, float a) {
		this.r = (int)(r * 255);
		this.g = (int)(g * 255);
		this.b = (int)(b * 255);
		this.a = a;
		color = make(this.r, this.g, this.b, this.a);
		context.setFillStyle(color);
		context.setStrokeStyle(this.color);
	}

	/** Sets the color for the following drawing operations.
	 * @param color The color. */
	public void setColor (Color color) {
		setColor(color.r, color.g, color.b, color.a);
	}

	/** Fills the complete bitmap with the currently set color. */
	public void fill () {
		context.fillRect(0, 0, getWidth(), getHeight());
	}

// /**
// * Sets the width in pixels of strokes.
// *
// * @param width The stroke width in pixels.
// */
// public void setStrokeWidth (int width);

	/** Draws a line between the given coordinates using the currently set color.
	 * 
	 * @param x The x-coodinate of the first point
	 * @param y The y-coordinate of the first point
	 * @param x2 The x-coordinate of the first point
	 * @param y2 The y-coordinate of the first point */
	public void drawLine (int x, int y, int x2, int y2) {
		context.beginPath();
		context.moveTo(x, y);
		context.lineTo(x2, y2);
		context.stroke();
		context.closePath();
	}

	/** Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards)
	 * using the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels */
	public void drawRectangle (int x, int y, int width, int height) {
		context.beginPath();
		context.rect(x, y, width, height);
		context.stroke();
		context.closePath();
	}

	/** Draws an area form another Pixmap to this Pixmap.
	 * 
	 * @param pixmap The other Pixmap
	 * @param x The target x-coordinate (top left corner)
	 * @param y The target y-coordinate (top left corner) */
	public void drawPixmap (Pixmap pixmap, int x, int y) {
		context.drawImage(pixmap.getCanvasElement(), x, y);
	}

	/** Draws an area form another Pixmap to this Pixmap.
	 * 
	 * @param pixmap The other Pixmap
	 * @param x The target x-coordinate (top left corner)
	 * @param y The target y-coordinate (top left corner)
	 * @param srcx The source x-coordinate (top left corner)
	 * @param srcy The source y-coordinate (top left corner);
	 * @param srcWidth The width of the area form the other Pixmap in pixels
	 * @param srcHeight The height of the area form the other Pixmap in pixles */
	public void drawPixmap (Pixmap pixmap, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight) {
		context.drawImage(pixmap.getCanvasElement(), srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight);
	}

	/** Draws an area form another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
	 * specified target rectangle. Use {@link Pixmap#setFilter(Filter)} to specify the type of filtering to be used (nearest
	 * neighbour or bilinear).
	 * 
	 * @param pixmap The other Pixmap
	 * @param srcx The source x-coordinate (top left corner)
	 * @param srcy The source y-coordinate (top left corner);
	 * @param srcWidth The width of the area form the other Pixmap in pixels
	 * @param srcHeight The height of the area form the other Pixmap in pixles
	 * @param dstx The target x-coordinate (top left corner)
	 * @param dsty The target y-coordinate (top left corner)
	 * @param dstWidth The target width
	 * @param dstHeight the target height */
	public void drawPixmap (Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth,
		int dstHeight) {
		context.drawImage(pixmap.getCanvasElement(), srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
	}

	/** Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
	 * the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels */
	public void fillRectangle (int x, int y, int width, int height) {
		context.fillRect(x, y, width, height);
	}

	/** Draws a circle outline with the center at x,y and a radius using the current color and stroke width.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels */
	public void drawCircle (int x, int y, int radius) {
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI, false);
		context.stroke();
		context.closePath();
	}

	/** Fills a circle with the center at x,y and a radius using the current color.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels */
	public void fillCircle (int x, int y, int radius) {
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI, false);
		context.fill();
		context.closePath();
	}

	/** Returns the 32-bit RGBA8888 value of the pixel at x, y. For Alpha formats the RGB components will be one.
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @return The pixel color in RGBA8888 format. */
	public int getPixel (int x, int y) {
		if(pixels == null) pixels = context.getImageData(0, 0, width, height).getData();
		int i = x * 4 + y * width * 4;
		int r = pixels.get(i + 0);
		int g = pixels.get(i + 1);
		int b = pixels.get(i + 2);
		int a = pixels.get(i + 3);
		return (r << 24) | 
				 (g << 16) |
				 (b << 8) |
				 (a);
	}

	/** Draws a pixel at the given location with the current color.
	 * 
	 * @param x the x-coordinate
	 * @param y the y-coordinate */
	public void drawPixel (int x, int y) {
		context.fillRect(x, y, 1, 1);
	}

	/** Draws a pixel at the given location with the given color.
	 * 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param color the color in RGBA8888 format. */
	public void drawPixel (int x, int y, int color) {
		setColor(color);
		drawPixel(x, y);
	}

}

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
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
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
		
		public static int toGlFormat (Format format) {
			if (format == Alpha) return GL20.GL_ALPHA;
			if (format == Intensity) return GL20.GL_ALPHA;
			if (format == LuminanceAlpha) return GL20.GL_LUMINANCE_ALPHA;
			if (format == RGB565) return GL20.GL_RGB;
			if (format == RGB888) return GL20.GL_RGB;
			if (format == RGBA4444) return GL20.GL_RGBA;
			if (format == RGBA8888) return GL20.GL_RGBA;
			throw new GdxRuntimeException("unknown format: " + format);
		}
		
		public static int toGlType (Format format) {
			if (format == Alpha) return GL20.GL_UNSIGNED_BYTE;
			if (format == Intensity) return GL20.GL_UNSIGNED_BYTE;
			if (format == LuminanceAlpha) return GL20.GL_UNSIGNED_BYTE;
			if (format == RGB565) return GL20.GL_UNSIGNED_SHORT_5_6_5;
			if (format == RGB888) return GL20.GL_UNSIGNED_BYTE;
			if (format == RGBA4444) return GL20.GL_UNSIGNED_SHORT_4_4_4_4;
			if (format == RGBA8888) return GL20.GL_UNSIGNED_BYTE;
			throw new GdxRuntimeException("unknown format: " + format);
		}
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
	static String clearColor = make(255, 255, 255, 1.0f);
	static Blending blending;
	CanvasPixelArray pixels;

	public Pixmap (FileHandle file) {
		GwtFileHandle gwtFile = (GwtFileHandle)file;
		ImageElement img = gwtFile.preloader.images.get(file.path());
		if (img == null) throw new GdxRuntimeException("Couldn't load image '" + file.path() + "', file does not exist");
		create(img.getWidth(), img.getHeight(), Format.RGBA8888);
		context.setGlobalCompositeOperation(Composite.COPY);
		context.drawImage(img, 0, 0);
		context.setGlobalCompositeOperation(getComposite());
	}

	private static Composite getComposite () {
		return Composite.SOURCE_OVER;
	}

	public Pixmap (ImageElement img) {
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
		context.setGlobalCompositeOperation(getComposite());
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
		Composite composite = getComposite();
		for (Pixmap pixmap : pixmaps.values()) {
			pixmap.context.setGlobalCompositeOperation(composite);
		}
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

	public int getGLInternalFormat () {
		return GL20.GL_RGBA;
	}

	public int getGLFormat () {
		return GL20.GL_RGBA;
	}

	public int getGLType () {
		return GL20.GL_UNSIGNED_BYTE;
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
		r = (color >>> 24) & 0xff;
		g = (color >>> 16) & 0xff;
		b = (color >>> 8) & 0xff;
		a = (color & 0xff) / 255f;
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
		context.clearRect(0, 0, getWidth(), getHeight());
		rectangle(0, 0, getWidth(), getHeight(), DrawType.FILL);
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
		line(x, y, x2, y2, DrawType.STROKE);
	}

	/** Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards)
	 * using the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels */
	public void drawRectangle (int x, int y, int width, int height) {
		rectangle(x, y, width, height, DrawType.STROKE);
	}

	/** Draws an area form another Pixmap to this Pixmap.
	 * 
	 * @param pixmap The other Pixmap
	 * @param x The target x-coordinate (top left corner)
	 * @param y The target y-coordinate (top left corner) */
	public void drawPixmap (Pixmap pixmap, int x, int y) {
		CanvasElement image = pixmap.getCanvasElement();		
		image(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight());
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
		CanvasElement image = pixmap.getCanvasElement();		
		image(image, srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight);		
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
		image(pixmap.getCanvasElement(), srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
	}

	/** Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
	 * the current color.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param width The width in pixels
	 * @param height The height in pixels */
	public void fillRectangle (int x, int y, int width, int height) {
		rectangle(x, y, width, height, DrawType.FILL);
	}

	/** Draws a circle outline with the center at x,y and a radius using the current color and stroke width.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels */
	public void drawCircle (int x, int y, int radius) {
		circle(x, y, radius, DrawType.STROKE);
	}

	/** Fills a circle with the center at x,y and a radius using the current color.
	 * 
	 * @param x The x-coordinate of the center
	 * @param y The y-coordinate of the center
	 * @param radius The radius in pixels */
	public void fillCircle (int x, int y, int radius) {
		circle(x, y, radius, DrawType.FILL);
	}

	/** Fills a triangle with vertices at x1,y1 and x2,y2 and x3,y3 using the current color.
	 * 
	 * @param x1 The x-coordinate of vertex 1
	 * @param y1 The y-coordinate of vertex 1
	 * @param x2 The x-coordinate of vertex 2
	 * @param y2 The y-coordinate of vertex 2
	 * @param x3 The x-coordinate of vertex 3
	 * @param y3 The y-coordinate of vertex 3 */
	public void fillTriangle (int x1, int y1, int x2, int y2, int x3, int y3) {
		triangle(x1, y1, x2, y2, x3, y3, DrawType.FILL);
	}

	/** Returns the 32-bit RGBA8888 value of the pixel at x, y. For Alpha formats the RGB components will be one.
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @return The pixel color in RGBA8888 format. */
	public int getPixel (int x, int y) {
		if (pixels == null) pixels = context.getImageData(0, 0, width, height).getData();
		int i = x * 4 + y * width * 4;
		int r = pixels.get(i + 0) & 0xff;
		int g = pixels.get(i + 1) & 0xff;
		int b = pixels.get(i + 2) & 0xff;
		int a = pixels.get(i + 3) & 0xff;
		return (r << 24) | (g << 16) | (b << 8) | (a);
	}

	/** Draws a pixel at the given location with the current color.
	 * 
	 * @param x the x-coordinate
	 * @param y the y-coordinate */
	public void drawPixel (int x, int y) {
		rectangle(x, y, 1, 1, DrawType.FILL);
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

	private void circle (int x, int y, int radius, DrawType drawType) {
		if (blending == Blending.None) {
			context.setFillStyle(clearColor);
			context.setStrokeStyle(clearColor);
			context.setGlobalCompositeOperation("destination-out");
			context.beginPath();
			context.arc(x, y, radius, 0, 2 * Math.PI, false);
			fillOrStrokePath(drawType);
			context.closePath();
			context.setFillStyle(color);
			context.setStrokeStyle(color);
			context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		}
		context.beginPath();
		context.arc(x, y, radius, 0, 2 * Math.PI, false);
		fillOrStrokePath(drawType);
		context.closePath();
		pixels = null;		
	}
	
	private void line(int x, int y, int x2, int y2, DrawType drawType) {
		if (blending == Blending.None) {
			context.setFillStyle(clearColor);
			context.setStrokeStyle(clearColor);
			context.setGlobalCompositeOperation("destination-out");
			context.beginPath();
			context.moveTo(x, y);
			context.lineTo(x2, y2);
			fillOrStrokePath(drawType);
			context.closePath();
			context.setFillStyle(color);
			context.setStrokeStyle(color);
			context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		}		
		context.beginPath();
		context.moveTo(x, y);
		context.lineTo(x2, y2);
		fillOrStrokePath(drawType);
		context.closePath();
		pixels = null;
	}
	
	private void rectangle(int x, int y, int width, int height, DrawType drawType) {
		if (blending == Blending.None) {
			context.setFillStyle(clearColor);
			context.setStrokeStyle(clearColor);
			context.setGlobalCompositeOperation("destination-out");
			context.beginPath();
			context.rect(x, y, width, height);
			fillOrStrokePath(drawType);
			context.closePath();
			context.setFillStyle(color);
			context.setStrokeStyle(color);
			context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		}
		context.beginPath();
		context.rect(x, y, width, height);
		fillOrStrokePath(drawType);
		context.closePath();
		pixels = null;
	}
	
	private void triangle(int x1, int y1, int x2, int y2, int x3, int y3, DrawType drawType) {
		if (blending == Blending.None) {
			context.setFillStyle(clearColor);
			context.setStrokeStyle(clearColor);
			context.setGlobalCompositeOperation("destination-out");
			context.beginPath();
			context.moveTo(x1,y1);
			context.lineTo(x2,y2);
			context.lineTo(x3,y3);
			context.lineTo(x1,y1);
			fillOrStrokePath(drawType);
			context.closePath();
			context.setFillStyle(color);
			context.setStrokeStyle(color);
			context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		}		
		context.beginPath();
		context.moveTo(x1,y1);
		context.lineTo(x2,y2);
		context.lineTo(x3,y3);
		context.lineTo(x1,y1);
		fillOrStrokePath(drawType);
		context.closePath();
		pixels = null;
	}
	
	private void image (CanvasElement image, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight) {
		if (blending == Blending.None) {
			context.setFillStyle(clearColor);
			context.setStrokeStyle(clearColor);
			context.setGlobalCompositeOperation("destination-out");
			context.beginPath();
			context.rect(dstX, dstY, dstWidth, dstHeight);
			fillOrStrokePath(DrawType.FILL);
			context.closePath();
			context.setFillStyle(color);
			context.setStrokeStyle(color);
			context.setGlobalCompositeOperation(Composite.SOURCE_OVER);
		}
		context.drawImage(image, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
		pixels = null;
	}
	
	private void fillOrStrokePath(DrawType drawType) {
		switch (drawType) {
			case FILL:
				context.fill();
				break;
			case STROKE:
				context.stroke();
				break;
		}		
	}
	
	private enum DrawType {
		FILL, STROKE
	}
	
}

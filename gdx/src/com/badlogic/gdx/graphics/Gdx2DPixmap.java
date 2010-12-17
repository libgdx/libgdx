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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Experimental. Do not use!
 * @author mzechner
 *
 */
public class Gdx2DPixmap {
	static final int GDX2D_FORMAT_ALPHA = 1;
	static final int GDX2D_FORMAT_ALPHA_LUMINANCE = 2;
	static final int GDX2D_FORMAT_RGB = 3;
	static final int GDX2D_FORMAT_RGBA = 4;
	
	final long basePtr;
	final int width;
	final int height;
	final int format;	
	final ByteBuffer pixelPtr;
	static final long[] nativeData = new long[4];
	
	private Gdx2DPixmap(InputStream in, int requestedFormat) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int readBytes = 0;
		
		while((readBytes = in.read(buffer)) != -1) {
			bytes.write(buffer);
		}
		
		buffer = bytes.toByteArray();
		pixelPtr = load(nativeData, buffer, buffer.length, requestedFormat);
		if(pixelPtr == null)
			throw new IOException("couldn't load pixmap");
		
		basePtr = nativeData[0];
		width = (int)nativeData[1];
		height = (int)nativeData[2];
		format = (int)nativeData[3];		
	}
	
	private Gdx2DPixmap(int width, int height, int format) throws IllegalArgumentException {
		pixelPtr = newPixmap(nativeData, width, height, format);
		if(pixelPtr == null)
			throw new IllegalArgumentException("couldn't load pixmap");
		
		this.basePtr = nativeData[0];
		this.width = (int)nativeData[1];
		this.height = (int)nativeData[2];
		this.format = (int)nativeData[3];
	}
	
	public void dispose() {
		free(basePtr);
	}
	
	public void clear(int color) {
		clear(basePtr, color);
	}
	
	public void setPixel(int x, int y, int color) {
		setPixel(basePtr, x, y, color);
	}
	
	public void drawLine(int x, int y, int x2, int y2, int color) {
		drawLine(basePtr, x, y, x2, y2, color);
	}
	
	public void drawRect(int x, int y, int width, int height, int color) {
		drawRect(basePtr, x, y, width, height, color);
	}
	
	public void drawCircle(int x, int y, int radius, int color) {
		drawCircle(basePtr, x, y, radius, color);
	}
	
	public void fillRect(int x, int y, int width, int height, int color) {
		drawRect(basePtr, x, y, width, height, color);
	}
	
	public void fillCircle(int x, int y, int radius, int color) {
		drawCircle(basePtr, x, y, radius, color);
	}
	
	private void drawPixmap(Gdx2DPixmap src, 
							int srcX, int srcY, int srcWidth, int srcHeight, 
							int dstX, int dstY, int dstWidth, int dstHeight, 
							int blend, int scale) {
		drawPixmap(src.basePtr, basePtr, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight, blend, scale);
	}
	
	
	public static Gdx2DPixmap newPixmap(InputStream in, int requestedFormat) {
		try {
			return new Gdx2DPixmap(in, requestedFormat);
		} catch(IOException e) {
			return null;
		}
	}
	
	public static Gdx2DPixmap newPixmap(int width, int height, int format) {
		try {
			return new Gdx2DPixmap(width, height, format);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	private static native ByteBuffer load(long[] nativeData, byte[] buffer, int len, int requestedFormat);
	private static native ByteBuffer newPixmap(long[] nativeData, int width, int height, int format);
	private static native void free(long basePtr);
	private static native void clear(long pixmap, int color);
	private static native void setPixel(long pixmap, int x, int y, int color);
	private static native void drawLine(long pixmap, int x, int y, int x2, int y2, int color);
	private static native void drawRect(long pixmap, int x, int y, int width, int height, int color);
	private static native void drawCircle(long pixmap, int x, int y, int radius, int color);
	private static native void fillRect(long pixmap, int x, int y, int width, int height, int color);
	private static native void fillCircle(long pixmap, int x, int y, int radius, int color);
	private static native void drawPixmap(long src, long dst, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight, int blend, int scale);
}

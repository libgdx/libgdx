/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.desktop;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.Sys;

import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * An implementation of the {@link Graphics} interface based on Jogl.
 * @author mzechner
 */
public final class LwjglGraphics implements Graphics, RenderListener {
	private final LwjglApplication app;
	private GL10 gl10;
	private GL11 gl11;
	private GL20 gl20;
	private final boolean useGL2;
	private long lastTime;
	private float deltaTime = 0;
	private long frameStart = 0;
	private int frames = 0;
	private int fps;

	LwjglGraphics (final LwjglApplication application, String title, int width, int height, boolean useGL2IfAvailable) {
		this.app = application;
		useGL2 = useGL2IfAvailable;
	}

	public GL10 getGL10 () {
		return gl10;
	}

	public GL11 getGL11 () {
		return gl11;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public int getHeight () {
		return app.getHeight();
	}

	public int getWidth () {
		return app.getWidth();
	}

	public boolean isGL11Available () {
		return gl11 != null;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public Font newFont (String fontName, int size, FontStyle style) {
		return new LwjglFont(fontName, size, style);
	}

	public Font newFont (FileHandle file, int size, FontStyle style) {
		LwjglFileHandle jHandle = (LwjglFileHandle)file;
		InputStream in;
		try {
			in = new FileInputStream(jHandle.getFile());
			LwjglFont font = new LwjglFont(in, size, style);
			in.close();

			return font;
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't load font from file '" + file + "'", e);
		}
	}

	public Pixmap newPixmap (int width, int height, Format format) {
		return new LwjglPixmap(width, height, format);
	}

	public Pixmap newPixmap (InputStream in) {
		try {
			BufferedImage img = (BufferedImage)ImageIO.read(in);
			return new LwjglPixmap(img);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Couldn't load Pixmap from InputStream", ex);
		}
	}

	public Pixmap newPixmap (FileHandle file) {
		try {
			BufferedImage img = (BufferedImage)ImageIO.read(((LwjglFileHandle)file).getFile());
			return new LwjglPixmap(img);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Couldn't load Pixmap from file '" + file + "'", ex);
		}
	}

	public Pixmap newPixmap (Object nativePixmap) {
		return new LwjglPixmap((BufferedImage)nativePixmap);
	}

	private static boolean isPowerOfTwo (int value) {
		return ((value != 0) && (value & (value - 1)) == 0);
	}

	public Texture newUnmanagedTexture (int width, int height, Pixmap.Format format, TextureFilter minFilter,
		TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		if (!isPowerOfTwo(width) || !isPowerOfTwo(height))
			throw new GdxRuntimeException("Texture dimensions must be a power of two");

		if (format == Format.Alpha)
			return new LwjglTexture(width, height, BufferedImage.TYPE_BYTE_GRAY, minFilter, magFilter, uWrap, vWrap, false);
		else
			return new LwjglTexture(width, height, BufferedImage.TYPE_4BYTE_ABGR, minFilter, magFilter, uWrap, vWrap, false);
	}

	public Texture newUnmanagedTexture (Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap) {
		if (!isPowerOfTwo(pixmap.getHeight()) || !isPowerOfTwo(pixmap.getWidth()))
			throw new GdxRuntimeException("Texture dimensions must be a power of two");

		return new LwjglTexture((BufferedImage)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false);
	}

	public Texture newTexture (FileHandle file, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap) {
		Pixmap pixmap = newPixmap(file);
		if (!isPowerOfTwo(pixmap.getHeight()) || !isPowerOfTwo(pixmap.getWidth()))
			throw new GdxRuntimeException("Texture dimensions must be a power of two");

		return new LwjglTexture((BufferedImage)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false);
	}

	public void setRenderListener (RenderListener listener) {
		app.listeners.add(listener);
	}

	public void dispose () {

	}

	public void render () {
		long time = System.nanoTime();
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000 ) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	public void surfaceCreated () {
		String version = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		int major = Integer.parseInt("" + version.charAt(0));
		int minor = Integer.parseInt("" + version.charAt(2));

		if (useGL2 && major >= 2) {
			// FIXME add check whether gl 2.0 is supported
			gl20 = new LwjglGL20();
		} else {
			if (major == 1 && minor < 5) {
				gl10 = new LwjglGL10();
			} else {
				gl11 = new LwjglGL11();
				gl10 = gl11;
			}
		}

		lastTime = System.nanoTime();
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public void surfaceChanged (int width, int height) {
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public int getFramesPerSecond () {
		return fps;
	}
}

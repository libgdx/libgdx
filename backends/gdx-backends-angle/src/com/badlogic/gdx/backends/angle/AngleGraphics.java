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

package com.badlogic.gdx.backends.angle;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AngleGraphics implements Graphics {
	GL20 gl;
	int width;
	int height;
	long lastTime = System.nanoTime();
	long frameStart = System.nanoTime();
	int fps;
	int frames;
	float deltaTime = 0;

	AngleGraphics (int width, int height) {
		gl = new AngleGLES20();
	}

	@Override public boolean isGL11Available () {
		return false;
	}

	@Override public boolean isGL20Available () {
		return true;
	}

	@Override public GLCommon getGLCommon () {
		return gl;
	}

	@Override public GL10 getGL10 () {
		return null;
	}

	@Override public GL11 getGL11 () {
		return null;
	}

	@Override public GL20 getGL20 () {
		return gl;
	}

	@Override public int getWidth () {
		return width;
	}

	@Override public int getHeight () {
		return height;
	}

	@Override public float getDeltaTime () {
		return deltaTime;
	}

	@Override public int getFramesPerSecond () {
		return fps;
	}

	@Override public Pixmap newPixmap (int width, int height, Format format) {
		return new AnglePixmap(width, height, format);
	}

	@Override public Pixmap newPixmap (InputStream in) {
		try {
			BufferedImage img = (BufferedImage)ImageIO.read(in);
			return new AnglePixmap(img);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Couldn't load Pixmap from InputStream", ex);
		}
	}

	@Override public Pixmap newPixmap (FileHandle file) {
		return newPixmap(file.read());
	}

	@Override public Pixmap newPixmap (Object nativePixmap) {
		return new AnglePixmap((BufferedImage)nativePixmap);
	}

	@Override public Texture newUnmanagedTexture (int width, int height, Format format, TextureFilter minFilter,
		TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {

		if (format == Format.Alpha)
			return new AngleTexture(width, height, BufferedImage.TYPE_BYTE_GRAY, minFilter, magFilter, uWrap, vWrap, false);
		else
			return new AngleTexture(width, height, BufferedImage.TYPE_4BYTE_ABGR, minFilter, magFilter, uWrap, vWrap, false);
	}

	@Override public Texture newUnmanagedTexture (Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {

		return new AngleTexture((BufferedImage)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap, false);
	}

	@Override public Texture newTexture (FileHandle file, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap) {

		return new AngleTexture(file, minFilter, magFilter, uWrap, vWrap, false);
	}

	@Override public Texture newTexture (TextureData textureData, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {
		return new AngleTexture(textureData, minFilter, magFilter, uWrap, vWrap);
	}

	@Override public GraphicsType getType () {
		return GraphicsType.Angle;
	}

	@Override public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	void updateTime () {
		long time = System.nanoTime();
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}
}

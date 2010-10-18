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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * An implementation of {@link Texture} based on Jogl
 * 
 * @author badlogicgames@gmail.com
 * 
 */
final class LwjglTexture implements Texture {
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;
	/** whether this textures i managed or not **/
	private final boolean isManaged;
	private int textureID;

	/** global number of textures **/
	public static int textures = 0;

	static private IntBuffer buffer = BufferUtils.createIntBuffer(1);
	static private ByteBuffer imageBuffer;
	static private final BitmapDecoder bitmapDecoder = new BitmapDecoder();
	static private final PNGDecoder pngDecoder = new PNGDecoder();

	/**
	 * Create a new texture
	 */
	LwjglTexture (FileHandle file, InputStream in, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed) {
		this.isManaged = managed;
		load(file, in);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	LwjglTexture (BufferedImage image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap,
		boolean managed) {
		this.isManaged = managed;
		load(image);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	/**
	 * Create a new texture
	 */
	LwjglTexture (int width, int height, int format, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed) {
		this.isManaged = managed;
		BufferedImage image = new BufferedImage(width, height, format);
		load(image);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	private void load (FileHandle file, InputStream in) {
		try {
			if (file.toString().endsWith("png")) {
				pngDecoder.decodeHeader(in);
				texWidth = pngDecoder.getWidth();
				texHeight = pngDecoder.getHeight();
				int stride = pngDecoder.getWidth() * 4;
				int bufferSize = stride * pngDecoder.getHeight();
				if (imageBuffer == null || imageBuffer.capacity() < bufferSize)
					imageBuffer = ByteBuffer.allocateDirect(bufferSize);
				else
					imageBuffer.clear();
				pngDecoder.decode(imageBuffer, stride, PNGDecoder.Format.RGBA);
				imageBuffer.flip();
			} else {
				imageBuffer = bitmapDecoder.decode(in, imageBuffer);
				texWidth = bitmapDecoder.width;
				texHeight = bitmapDecoder.height;
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Couldn't load Texture: " + file, ex);
		}
		buffer.clear();
		GL11.glGenTextures(buffer);
		textureID = buffer.get(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
			imageBuffer);
	}

	private void load (BufferedImage image) {
		try {
			imageBuffer = bitmapDecoder.decode(image, imageBuffer);
			texWidth = bitmapDecoder.width;
			texHeight = bitmapDecoder.height;
		} catch (IOException ex) {
			throw new GdxRuntimeException("Couldn't load Texture", ex);
		}
		buffer.clear();
		GL11.glGenTextures(buffer);
		textureID = buffer.get(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
			imageBuffer);
	}

	private int getTextureFilter (TextureFilter filter) {
		if (filter == TextureFilter.Linear)
			return GL11.GL_LINEAR;
		else if (filter == TextureFilter.Nearest)
			return GL11.GL_NEAREST;
		else
			return GL11.GL_LINEAR_MIPMAP_LINEAR;
	}

	private int getTextureWrap (TextureWrap wrap) {
		if (wrap == TextureWrap.ClampToEdge)
			return GL11.GL_CLAMP;
		else
			return GL11.GL_REPEAT;
	}

	public boolean isManaged () {
		return isManaged;
	}

	public void bind () {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

	public void dispose () {
		GL11.glDeleteTextures(textureID);
		textures--;
	}

	public int getHeight () {
		return texHeight;
	}

	public int getWidth () {
		return texWidth;
	}

	public void draw (Pixmap pixmap, int x, int y) {
		if (isManaged) throw new GdxRuntimeException("Can't draw to a managed texture");
		try {
			imageBuffer = bitmapDecoder.decode((BufferedImage)pixmap.getNativePixmap(), imageBuffer);
		} catch (IOException ex) {
			throw new GdxRuntimeException(ex);
		}
		bind();
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, bitmapDecoder.width, bitmapDecoder.height, GL11.GL_RGBA,
			GL11.GL_UNSIGNED_BYTE, imageBuffer);
	}

	public int getTextureObjectHandle () {
		return textureID;
	}
}

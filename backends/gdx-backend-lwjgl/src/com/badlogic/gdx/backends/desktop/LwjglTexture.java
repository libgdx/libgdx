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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.desktop.PNGDecoder.Format;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * I apologize for this class. It is a big fucking mess which can be attributed to the late hour i created this piece of shit in.
 * Please take my apologize. It is slow. It is ugly. It is aids.
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
	private final boolean isMipMapped;

	/** global number of textures **/
	public static int textures = 0;

	static private ByteBuffer buffer;
	static private IntBuffer intBuffer;
	static private PNGDecoder pngDecoder = new PNGDecoder();

	/**
	 * Create a new texture
	 */
	LwjglTexture (FileHandle file, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap,
		boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		if (file.getPath().endsWith(".png"))
			loadPNG(file);
		else {
			BufferedImage image = (BufferedImage)Gdx.graphics.newPixmap(file).getNativePixmap();
			loadMipMap(image);
		}
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
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		loadMipMap(img);
		this.draw(Gdx.graphics.newPixmap(image), 0, 0);
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
		this.isMipMapped = minFilter == TextureFilter.MipMap;
		BufferedImage image = new BufferedImage(width, height, format);
		loadMipMap(image);
		bind();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	private void loadPNG (FileHandle file) {
		try {
			pngDecoder.decodeHeader(file.readFile());
			texWidth = pngDecoder.getWidth();
			texHeight = pngDecoder.getHeight();
			int stride = texWidth * 4;
			int bufferSize = stride * texHeight;
			if (buffer == null || buffer.capacity() < bufferSize)
				buffer = BufferUtils.createByteBuffer(bufferSize);
			else
				buffer.clear();

			Format pngFormat = pngDecoder.decideTextureFormat(PNGDecoder.Format.RGBA);
			int glFormat, glInternalFormat;
			switch (pngFormat) {
			case ALPHA:
				glFormat = GL11.GL_ALPHA;
				glInternalFormat = GL11.GL_ALPHA8;
				break;
			case LUMINANCE:
				glFormat = GL11.GL_LUMINANCE;
				glInternalFormat = GL11.GL_LUMINANCE8;
				break;
			case LUMINANCE_ALPHA:
				glFormat = GL11.GL_LUMINANCE_ALPHA;
				glInternalFormat = GL11.GL_LUMINANCE8_ALPHA8;
				break;
			case RGB:
				glFormat = GL11.GL_RGB;
				glInternalFormat = GL11.GL_RGB8;
				break;
			case RGBA:
				glFormat = GL11.GL_RGBA;
				glInternalFormat = GL11.GL_RGBA8;
				break;
			case BGRA:
				glFormat = GL12.GL_BGRA;
				glInternalFormat = GL12.GL_BGRA;
				break;
			default:
				throw new UnsupportedOperationException("PNG format not handled: " + pngFormat);
			}
			pngDecoder.decode(buffer, stride, pngFormat);
			buffer.flip();

			textureID = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, glInternalFormat, texWidth, texHeight, 0, glFormat, GL11.GL_UNSIGNED_BYTE,
				buffer);
			// FIXME - No mipmapping!
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading image file: " + file, ex);
		}
	}

	private ByteBuffer toByteBuffer (BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		int bufferSize = width * height * 4;
		if (buffer == null || buffer.capacity() < bufferSize) {
			buffer = ByteBuffer.allocateDirect(bufferSize);
			ByteBuffer temp = buffer.slice();
			temp.order(ByteOrder.LITTLE_ENDIAN);
			intBuffer = temp.asIntBuffer();
		} else {
			buffer.clear();
			intBuffer.clear();
		}

		Raster raster = image.getRaster();
		if (image.getType() == BufferedImage.TYPE_INT_ARGB)
			intBuffer.put(((DataBufferInt)raster.getDataBuffer()).getData(), 0, width * height);
		else {
			// Same as image.getRGB() without allocating a large int[].
			ColorModel colorModel = image.getColorModel();
			Object data = raster.getDataElements(0, 0, null);
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
					intBuffer.put(colorModel.getRGB(raster.getDataElements(x, y, data)));
		}

		buffer.limit(intBuffer.position() * 4);
		return buffer;
	}

	private BufferedImage scaleDown (BufferedImage image) {
		BufferedImage scaled = new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D g = scaled.createGraphics();
		g.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null); // FIXME replace with something that looks actually
// like a scaled image...
		g.dispose();
		return scaled;
	}

	private void loadMipMap (BufferedImage image) {
		int level = 0;
		int height = image.getHeight();
		int width = image.getWidth();
		texWidth = width;
		texHeight = height;
		textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

		while (height >= 1 || width >= 1 && level < 4) {
			ByteBuffer imageBuffer = toByteBuffer(image);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA8, width, height, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE,
				imageBuffer);
			if (height == 1 || width == 1 || isMipMapped == false) {
				break;
			}

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;

			image = scaleDown(image);
		}
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
		BufferedImage image = (BufferedImage)pixmap.getNativePixmap();

		int level = 0;
		int height = image.getHeight();
		int width = image.getWidth();
		bind();
		while (height >= 1 || width >= 1 && level < 4) {
			ByteBuffer imageBuffer = toByteBuffer(image);
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, x, y, width, height, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, imageBuffer);
			if (height == 1 || width == 1 || isMipMapped == false) {
				break;
			}

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;

			image = scaleDown(image);
		}

	}

	public int getTextureObjectHandle () {
		return textureID;
	}
}

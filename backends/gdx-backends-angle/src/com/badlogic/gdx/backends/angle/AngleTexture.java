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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.angle.PNGDecoder.Format;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author badlogicgames@gmail.com
 * @author Nathan Sweet <misc@n4te.com>
 */
final class AngleTexture implements Texture {
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
	static private final PNGDecoder pngDecoder = new PNGDecoder();

	AngleTexture (FileHandle file, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap,
		boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = TextureFilter.isMipMap(minFilter);

		BufferedImage image = (BufferedImage)Gdx.graphics.newPixmap(file).getNativePixmap();
		loadMipMap(image);

		bind();
		GL20 gl = Gdx.graphics.getGL20();
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	AngleTexture (BufferedImage image, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap, TextureWrap vWrap,
		boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = TextureFilter.isMipMap(minFilter);

		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		loadMipMap(img);
		this.draw(Gdx.graphics.newPixmap(image), 0, 0);
		bind();
		GL20 gl = Gdx.graphics.getGL20();
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	AngleTexture (int width, int height, int format, TextureFilter minFilter, TextureFilter maxFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed) {
		this.isManaged = managed;
		this.isMipMapped = TextureFilter.isMipMap(minFilter);

		BufferedImage image = new BufferedImage(width, height, format);
		loadMipMap(image);
		bind();
		GL20 gl = Gdx.graphics.getGL20();
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(maxFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
		textures++;
	}

	public AngleTexture (TextureData textureData, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap) {
		isManaged = false;
		this.isMipMapped = TextureFilter.isMipMap(minFilter);

		GL20 gl = Gdx.graphics.getGL20();
		IntBuffer buffer = BufferUtils.newIntBuffer(1);
		gl.glGenTextures(1, buffer);
		textureID = buffer.get(0);
		bind();
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(magFilter));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		gl.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));

		textureData.load();
		texWidth = textureData.getWidth();
		texHeight = textureData.getHeight();

		textures++;
	}

	private void loadPNG (FileHandle file) {
		try {
			pngDecoder.decodeHeader(file.read());
			texWidth = pngDecoder.getWidth();
			texHeight = pngDecoder.getHeight();
			int stride = texWidth * 4;
			ensureBufferSize(stride * texHeight);

			Format pngFormat = pngDecoder.decideTextureFormat(PNGDecoder.Format.BGRA);
			int glFormat, glInternalFormat;
			switch (pngFormat) {
			case ALPHA:
				glFormat = GL10.GL_ALPHA;
				glInternalFormat = GL10.GL_ALPHA;
				break;
			case LUMINANCE:
				glFormat = GL10.GL_LUMINANCE;
				glInternalFormat = GL10.GL_LUMINANCE;
				break;
			case LUMINANCE_ALPHA:
				glFormat = GL10.GL_LUMINANCE_ALPHA;
				glInternalFormat = GL10.GL_LUMINANCE_ALPHA;
				break;
			case RGB:
				glFormat = GL10.GL_RGB;
				glInternalFormat = GL10.GL_RGB;
				break;
			case RGBA:
				glFormat = GL10.GL_RGBA;
				glInternalFormat = GL10.GL_RGBA;
				break;
// case BGRA:
// glFormat = GL10.GL_BGRA;
// glInternalFormat = GL10.GL_BGRA;
// break;
			default:
				throw new UnsupportedOperationException("PNG format not handled: " + pngFormat);
			}
			pngDecoder.decode(buffer, stride, pngFormat);
			buffer.flip();

			GL20 gl = Gdx.graphics.getGL20();
			IntBuffer buffer = BufferUtils.newIntBuffer(1);
			gl.glGenTextures(1, buffer);
			textureID = buffer.get(0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, glInternalFormat, texWidth, texHeight, 0, glFormat, GL10.GL_UNSIGNED_BYTE, buffer);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading image file: " + file, ex);
		}
	}

	private ByteBuffer toByteBuffer (BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		ensureBufferSize(width * height * 4);

		Raster raster = image.getRaster();
		if (image.getType() == BufferedImage.TYPE_INT_ARGB ||
			image.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
			int[] pixels = ((DataBufferInt)raster.getDataBuffer()).getData();
			for(int i = 0; i < pixels.length; i++) {
				int col = pixels[i];
				col = ((col & 0xffffff) << 8) |
				      ((col & 0xff000000) >>> 24);
				pixels[i] = col;
			}
			intBuffer.put(pixels, 0, width * height);
		}
		else {
			// Same as image.getRGB() without allocating a large int[].
			ColorModel colorModel = image.getColorModel();
			Object data = raster.getDataElements(0, 0, null);
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++) {
					int col = colorModel.getRGB(raster.getDataElements(x, y, data));
					col = ((col & 0xffffff) << 8) |
				      	  ((col & 0xff000000) >>> 24);
					intBuffer.put(col);
				}
		}

		buffer.limit(intBuffer.position() * 4);
		return buffer;
	}

	private void ensureBufferSize (int size) {
		if (buffer == null || buffer.capacity() < size) {
			buffer = BufferUtils.newByteBuffer(size);
			ByteBuffer temp = buffer.slice();
			temp.order(ByteOrder.BIG_ENDIAN);
			intBuffer = temp.asIntBuffer();
		} else {
			buffer.clear();
			intBuffer.clear();
		}
	}

	private BufferedImage scaleDown (BufferedImage image) {
		BufferedImage scaled = new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
		g.dispose();
		return scaled;
	}

	private void loadMipMap (BufferedImage image) {		
		int level = 0;
		int height = image.getHeight();
		int width = image.getWidth();
		texWidth = width;
		texHeight = height;
		GL20 gl = Gdx.graphics.getGL20();
		IntBuffer buffer = BufferUtils.newIntBuffer(1);
		gl.glGenTextures(1, buffer);
		textureID = buffer.get(0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);

		while (height >= 1 || width >= 1 && level < 4) {
			ByteBuffer imageBuffer = toByteBuffer(image);
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, GL10.GL_RGBA, width, height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
				imageBuffer);
			if (height == 1 || width == 1 || isMipMapped == false) break;

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;

			image = scaleDown(image);
		}
	}

	private int getTextureFilter (TextureFilter filter) {
		if (filter == TextureFilter.Linear)
			return GL10.GL_LINEAR;
		else if (filter == TextureFilter.Nearest)
			return GL10.GL_NEAREST;
		else if (filter == TextureFilter.MipMap)
			return GL10.GL_LINEAR_MIPMAP_LINEAR;
		else if (filter == TextureFilter.MipMapNearestNearest)
			return GL10.GL_NEAREST_MIPMAP_NEAREST;
		else if (filter == TextureFilter.MipMapNearestLinear)
			return GL10.GL_NEAREST_MIPMAP_LINEAR;
		else if (filter == TextureFilter.MipMapLinearNearest)
			return GL10.GL_LINEAR_MIPMAP_NEAREST;
		else if (filter == TextureFilter.MipMapLinearLinear)
			return GL10.GL_LINEAR_MIPMAP_LINEAR;
		else
			return GL10.GL_LINEAR_MIPMAP_LINEAR;
	}

	private int getTextureWrap (TextureWrap wrap) {
		if (wrap == TextureWrap.ClampToEdge)
			return GL10.GL_CLAMP_TO_EDGE;
		else
			return GL10.GL_REPEAT;
	}

	public boolean isManaged () {
		return isManaged;
	}

	public void bind () {
		GL20 gl = Gdx.graphics.getGL20();
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
	}

	public void dispose () {
		GL20 gl = Gdx.graphics.getGL20();
		IntBuffer buffer = BufferUtils.newIntBuffer(1);
		buffer.put(textureID);
		buffer.flip();
		gl.glDeleteTextures(1, buffer);
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
		GL20 gl = Gdx.graphics.getGL20();
		bind();
		while (height >= 1 || width >= 1 && level < 4) {
			ByteBuffer imageBuffer = toByteBuffer(image);
			gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, level, x, y, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, imageBuffer);
			if (height == 1 || width == 1 || isMipMapped == false) break;

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;

			image = scaleDown(image);
		}

	}

	public int getTextureObjectHandle () {
		return textureID;
	}

	public void setWrap (TextureWrap x, TextureWrap y) {
		bind();
		GL20 gl = Gdx.graphics.getGL20();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, x == TextureWrap.Repeat ? GL10.GL_REPEAT
			: GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, y == TextureWrap.Repeat ? GL10.GL_REPEAT
			: GL10.GL_CLAMP_TO_EDGE);
	}
}

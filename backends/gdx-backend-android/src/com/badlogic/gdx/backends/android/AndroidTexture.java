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

package com.badlogic.gdx.backends.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.MathUtils;

/**
 * An implementation of {@link Texture} for Android
 * 
 * @author badlogicgames@gmail.com
 * 
 */
final class AndroidTexture implements Texture {
	/** list of currently active textures used to invalidate them in case the surface was lost **/
	private static final ArrayList<AndroidTexture> textures = new ArrayList<AndroidTexture>();
	/** the texture handle **/
	private int textureHandle;
	/** height in pixels of texture **/
	private int texHeight;
	/** width in pixels of texture **/
	private int texWidth;
	/** whether this texture is managed **/
	private final boolean isManaged;
	/** the managed pixmap **/
	private Bitmap bitmap;
	/** whether this texture is mip mapped **/
	private final boolean isMipMap;
	/** the min filter **/
	private final TextureFilter minFilter;
	/** the mag filter **/
	private final TextureFilter magFilter;
	/** the u wrap **/
	private final TextureWrap uWrap;
	/** the v wrap **/
	private final TextureWrap vWrap;
	/** invalidate flag **/
	private boolean invalidated = false;
	/** file handle **/
	private FileHandle file;
	private TextureData textureData;

	/**
	 * Creates a new texture based on the given image
	 */
	AndroidTexture (AndroidGraphics graphics, Bitmap image, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap,
		TextureWrap vWrap, boolean managed, FileHandle file) {
		this.file = file;
		this.isManaged = managed;
		this.bitmap = image;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		isMipMap = TextureFilter.isMipMap(minFilter);

		if (image != null) {
			this.texWidth = image.getWidth();
			this.texHeight = image.getHeight();

			if (Gdx.gl != Gdx.gl20 && (!MathUtils.isPowerOfTwo(image.getWidth()) || !MathUtils.isPowerOfTwo(image.getHeight())))
				throw new GdxRuntimeException("texture must have power of two size");
		}

		createTexture();
		buildMipmap();
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		bitmap = null;

		if (isManaged) textures.add(this);
	}

	/**
	 * Creates a managed texture that loads using the specified TextureData.
	 */
	AndroidTexture (AndroidGraphics graphics, TextureData textureData, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {
		this.textureData = textureData;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		isMipMap = TextureFilter.isMipMap(minFilter);

		createTexture();
		textureData.load();
		texWidth = textureData.getWidth();
		texHeight = textureData.getHeight();

		isManaged = true;
		textures.add(this);
	}

	private void rebuild () {
		createTexture();
		if (textureData != null)
			textureData.load();
		else
			buildMipmap();
		invalidated = false;
	}

	private void createTexture () {
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = buffer.asIntBuffer();
		Gdx.gl.glGenTextures(1, intBuffer);
		textureHandle = intBuffer.get(0);

		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(magFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, getTextureWrap(uWrap));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, getTextureWrap(vWrap));
	}

	private int getTextureFilter (TextureFilter filter) {

		if (filter == TextureFilter.Linear)
			return GL10.GL_LINEAR;
		else if (filter == TextureFilter.Nearest)
			return GL10.GL_NEAREST;
		else if (filter == TextureFilter.MipMap)
			return GL10.GL_NEAREST_MIPMAP_LINEAR;
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

	private Bitmap loadBitmap (FileHandle file) {
		Pixmap pixmap = Gdx.graphics.newPixmap(file);
		Bitmap image = (Bitmap)pixmap.getNativePixmap();
		this.texWidth = image.getWidth();
		this.texHeight = image.getHeight();
		return (Bitmap)pixmap.getNativePixmap();
	}

	private void buildMipmap () {
		Bitmap obitmap = null;
		if (file != null)
			obitmap = loadBitmap(file);
		else
			obitmap = this.bitmap;
		Bitmap bitmap = obitmap;

		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();

		if (!MathUtils.isPowerOfTwo(bitmap.getWidth()) || !MathUtils.isPowerOfTwo(bitmap.getHeight()))
			throw new GdxRuntimeException("Dimensions have to be a power of two");

		while (height >= 1 || width >= 1 && level < 4) {
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);
			if (height == 1 || width == 1 || isMipMap == false) {
				break;
			}

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;
			Log.d("GDX", "Creating texture mipmap: " + width + ", " + height);

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			if (level > 1) bitmap.recycle();
			bitmap = bitmap2;
		}

		if (file != null) obitmap.recycle();
	}

	public boolean isManaged () {
		return isManaged;
	}

	/**
	 * {@inheritDoc}
	 */
	public void draw (Pixmap bmp, int x, int y) {
		if (isManaged) throw new GdxRuntimeException("Can't draw to a managed texture!");

		if (isManaged && invalidated) rebuild();

		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle);
		Bitmap bitmap = (Bitmap)bmp.getNativePixmap();

		int level = 0;
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();

		while (height >= 1 || width >= 1 && level < 4) {
			GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, level, x, y, bitmap);

			if (height == 1 || width == 1 || isMipMap == false) {
				break;
			}

			level++;
			if (height > 1) height /= 2;
			if (width > 1) width /= 2;

			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			if (level > 1) bitmap.recycle();
			bitmap = bitmap2;
		}
	}

	static Texture lastTexture = null;

	/**
	 * {@inheritDoc}
	 */
	public void bind () {
		if (isManaged && invalidated) {
			rebuild();
			lastTexture = null;
		}

		if (lastTexture != this) {
			lastTexture = this;
			Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void dispose () {
		ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		buffer.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = buffer.asIntBuffer();
		intBuffer.put(textureHandle);
		intBuffer.position(0);
		Gdx.gl.glDeleteTextures(1, intBuffer);
		textureHandle = 0;
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		textures.remove(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getHeight () {
		return texHeight;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getWidth () {
		return texWidth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getTextureObjectHandle () {
		return textureHandle;
	}

	public static void invalidateAllTextures () {
		for (int i = 0; i < textures.size(); i++) {
			if (textures.get(i).isManaged) {
				AndroidTexture texture = textures.get(i);
				texture.invalidated = true;
				texture.rebuild();
			}
		}
		lastTexture = null;
	}

	public static void clearAllTextures () {
		textures.clear();
		lastTexture = null;
	}

	@Override public void setWrap (TextureWrap x, TextureWrap y) {
		bind();
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, x == TextureWrap.Repeat ? GL10.GL_REPEAT
			: GL10.GL_CLAMP_TO_EDGE);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, y == TextureWrap.Repeat ? GL10.GL_REPEAT
			: GL10.GL_CLAMP_TO_EDGE);
	}
}

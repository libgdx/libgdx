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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.MathUtils;

/**
 * <p>
 * A Texture wraps a standard OpenGL ES texture.
 * </p>
 * 
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all managed textures get invalidated. This happens when a user switches
 * to another application or receives an incoming call. Managed textures get reloaded automatically.
 * </p>
 * 
 * <p>
 * A Texture has to be bound via the {@link Texture#bind()} method in order for it to be applied to geometry. The texture will be
 * bound to the currently active texture unit specified via {@link GLCommon#glActiveTexture(int)}.
 * </p>
 * 
 * <p>
 * You can draw {@link Pixmap}s to a texture at any time. The changes will be automatically uploaded to texture memory. This is of
 * course not extremely fast so use it with care. It also only works with unmanaged textures.
 * </p>
 * 
 * <p>
 * A Texture must be disposed when it is no longer used
 * </p>
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class Texture implements Disposable {
	 static private boolean enforcePotImages = true;

	/**
	 * Texture filter enum
	 * 
	 * @author badlogicgames@gmail.com
	 * 
	 */
	public enum TextureFilter {
		Nearest, Linear, MipMap, MipMapNearestNearest, MipMapLinearNearest, MipMapNearestLinear, MipMapLinearLinear;

		public static boolean isMipMap (TextureFilter filter) {
			return filter != Nearest && filter != Linear;
		}
	}

	/**
	 * Texture wrap enum
	 * 
	 * @author badlogicgames@gmail.com
	 * 
	 */
	public enum TextureWrap {
		ClampToEdge, Repeat
	}
	
	final static IntBuffer buffer = BufferUtils.newIntBuffer(1);
	final static ArrayList<Texture> managedTextures = new ArrayList<Texture>();	
	
	int width;
	int height;	
	final boolean isMipMap;
	final boolean isManaged;
	int glHandle;
	final FileHandle file;
	final TextureData textureData;
	TextureFilter minFilter = TextureFilter.Nearest;
	TextureFilter magFilter = TextureFilter.Nearest;
	TextureWrap uWrap = TextureWrap.ClampToEdge;
	TextureWrap vWrap = TextureWrap.ClampToEdge;
	final Format format;
	
	/**
	 * Creates a new texture from the given {@link FileHandle}. The 
	 * FileHandle must point to a Jpeg, Png or Bmp file. This constructor
	 * will throw an runtime exception in case the dimensions are not powers of two. 
	 * The minification and magnification filters will be set to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture will be managed and reloaded in case of a context loss.
	 * 
	 * @param file the FileHandle to the image file.
	 */
	public Texture(FileHandle file) {
		this(file, false);
	}
	
	/**
	 * Creates a new texture from the given {@link FileHandle}. The 
	 * FileHandle must point to a Jpeg, Png or Bmp file. This constructor
	 * will throw an runtime exception in case the dimensions are not powers of two. 
	 * It will also throw an exception in case mipmapping is requested but the texture is not square
	 * when using OpenGL ES 1.x. The minification and magnification filters will be set 
	 * to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture will be managed and reloaded in case of a context loss. In case mipmapping is
	 * set to true then the minification filter will be set to TextureFilter.MipMap automatically.
	 * 
	 * @param file the FileHandle to the image file.
	 * @param mipmap whether to build a mipmap chain.
	 */
	public Texture(FileHandle file, boolean mipmap) {
		this.isManaged = true;
		this.isMipMap = mipmap;
		this.file = file;		
		this.textureData = null;
		glHandle = createGLHandle();
		Pixmap pixmap = new Pixmap(file);
		format = pixmap.getFormat();
		uploadImageData(pixmap);
		pixmap.dispose();		
		if(mipmap)
			minFilter = TextureFilter.MipMap;
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		managedTextures.add(this);
	}
	
	/**
	 * Creates a new texture from the given {@link FileHandle}. The 
	 * FileHandle must point to a Jpeg, Png or Bmp file. This constructor
	 * will throw an runtime exception in case the dimensions are not powers of two. 
	 * It will also throw an exception in case mipmapping is requested but the texture is not square
	 * when using OpenGL ES 1.x. The minification and magnification filters will be set 
	 * to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture will be managed and reloaded in case of a context loss. In case mipmapping is
	 * set to true then the minification filter will be set to TextureFilter.MipMap automatically.
	 * 
	 * @param file the FileHandle to the image file.
	 * @param format the prefered format for the texture. Conversion happens on the fly.
	 * @param mipmap whether to build a mipmap chain.
	 */
	public Texture(FileHandle file, Format format, boolean mipmap) {
		this.isManaged = true;
		this.isMipMap = mipmap;
		this.file = file;		
		this.textureData = null;
		glHandle = createGLHandle();
		Pixmap pixmap = new Pixmap(file);
		this.format = format;
		uploadImageData(pixmap);
		pixmap.dispose();		
		if(mipmap)
			minFilter = TextureFilter.MipMap;
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		managedTextures.add(this);
	}
	
	/**
	 * Creates a new texture from the given {@link Pixmap}. The Pixmap must 
	 * have power of two dimensions. The minification and magnification filters will be set 
	 * to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture is not managed and has to be reloaded manually on a context loss. 
	 * 
	 * @param pixmap the {@link Pixmap}
	 */
	public Texture(Pixmap pixmap) {
		this(pixmap, false);
	}
	
	/**
	 * Creates a new texture from the given {@link Pixmap}. The Pixmap must 
	 * have power of two dimensions. In case mipmapping is requested the
	 * pixmap must be square.  The minification and magnification filters will be set 
	 * to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture is not managed and has to be reloaded manually on a context loss. In case mipmapping is
	 * set to true then the minification filter will be set to TextureFilter.MipMap automatically.
	 * 
	 * @param pixmap the {@link Pixmap}
	 * @param mipmap whether to generate mipmaps
	 */
	public Texture(Pixmap pixmap, boolean mipmap) {
		this.isManaged = false;
		this.isMipMap = mipmap;
		this.file = null;
		this.textureData = null;
		glHandle = createGLHandle();
		format = pixmap.getFormat();		
		uploadImageData(pixmap);	
		if(mipmap)
			minFilter = TextureFilter.MipMap;
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
	}
	
	/**
	 * Creates a new texture with the given width, height and format. The dimensions
	 * must be powers of two. he minification and magnification filters will be set 
	 * to GL_NEAREST by default. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture is not managed and has to be reloaded manually on a context loss.
	 * 
	 * @param width the width of the texture
	 * @param height the height of the texture
	 * @param format the {@link Format} of the texture
	 */
	public Texture(int width, int height, Format format) {
		this.isManaged = false;
		this.isMipMap = false;
		this.file = null;
		this.textureData = null;
		glHandle = createGLHandle();
		Pixmap pixmap = new Pixmap(width, height, format);
		pixmap.setColor(0, 0, 0, 0);
		pixmap.fill();
		this.format = pixmap.getFormat();
		uploadImageData(pixmap);	
		pixmap.dispose();
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
	}
	
	/**
	 * Creates a new texture from the given {@link TextureData}. The
	 * texture is managed and the TextureData instance is invoked
	 * in case of a context loss to recreate the texture. The texture wrap for u and v is set to GL_CLAMP_TO_EDGE by default.
	 * The texture is managed.
	 * @param data the TextureData
	 */
	public Texture(TextureData data) {
		this.isManaged = true;
		this.isMipMap = false;
		this.textureData = data;
		this.file = null;
		glHandle = createGLHandle();
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		textureData.load();			
		format = Format.RGBA8888; // FIXME, let TextureData return the format upon load.
		this.width = textureData.getWidth();
		this.height = textureData.getHeight();
		managedTextures.add(this);
	}
	
	private void reload() {
		glHandle = createGLHandle();
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		
		if(file != null) {
			Pixmap pixmap = new Pixmap(file);
			uploadImageData(pixmap);
			pixmap.dispose();
		} 		
		if(textureData != null) {
			Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
			textureData.load();			
		}
	}
	
	private int createGLHandle() {
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl.glGenTextures(1, buffer);
		return buffer.get(0);		
	}
	
	private void uploadImageData(Pixmap pixmap) {
		boolean disposePixmap = false;
		if(this.format != pixmap.getFormat()) {
			Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), this.format);			
			Blending blend = Pixmap.getBlending();
			Pixmap.setBlending(Blending.None);
			tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
			Pixmap.setBlending(blend);
			pixmap = tmp;
			disposePixmap = true;
		}
		
		this.width = pixmap.getWidth();
		this.height = pixmap.getHeight();
		if(enforcePotImages && Gdx.gl20 == null && (!MathUtils.isPowerOfTwo(width) || !MathUtils.isPowerOfTwo(height)))
			throw new GdxRuntimeException("texture width and height must be powers of two");
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		if(isMipMap) {		
			if(!(Gdx.gl20==null) && width != height)
				throw new GdxRuntimeException("texture width and height must be square when using mipmapping in OpenGL ES 1.x");
			int width = pixmap.getWidth() / 2;
			int height = pixmap.getHeight() / 2;
			int level = 1;
			while(width > 0  && height > 0) {
				Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());		
				tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
				if(level > 1 || disposePixmap)
					pixmap.dispose();				
				pixmap = tmp;
				
				Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());							
								
				width = pixmap.getWidth() / 2;
				height = pixmap.getHeight() / 2;
				level++;
			}
			pixmap.dispose();
		} else {
			if(disposePixmap) pixmap.dispose();
		}
	}

	/**
	 * Binds this texture. The texture will be bound to the currently active texture unit specified via
	 * {@link GLCommon#glActiveTexture(int)}.
	 */
	public void bind () {
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
	}

	/**
	 * Binds the texture to the given texture unit. Sets the currently active texture unit via
	 * {@link GLCommon#glActiveTexture(int)}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS).
	 */
	public void bind(int unit) {
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0 + unit);
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);
	}
	
	/**
	 * Draws the given {@link Pixmap} to the texture at position x, y. No clipping is performed so you have to make sure that you
	 * draw only inside the texture region.
	 * 
	 * @param pixmap The Pixmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels
	 */
	public void draw (Pixmap pixmap, int x, int y) {
		if(isManaged) 
			throw new GdxRuntimeException("can't draw to a managed texture");
		
		Gdx.gl.glBindTexture(GL10.GL_TEXTURE_2D, glHandle);		
		Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, x, y, pixmap.getWidth(), pixmap.getHeight(), pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());

		if(isMipMap) {
			int level = 1;
			int height = pixmap.getHeight();
			int width = pixmap.getWidth();
	
			while (height > 0 && width > 0) {					
				Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());		
				tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
				if(level > 1)
					pixmap.dispose();				
				pixmap = tmp;
				
				Gdx.gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, level, x, y, pixmap.getWidth(), pixmap.getHeight(), pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());							
								
				width = pixmap.getWidth() / 2;
				height = pixmap.getHeight() / 2;
				level++;					
			}
		}
	}

	/**
	 * 
	 * @return the width of the texture in pixels
	 */
	public int getWidth () {
		return width;
	}

	/**
	 * 
	 * @return the height of the texture in pixels
	 */
	public int getHeight () {
		return height;
	}

	/**
	 * @return whether this texture is managed or not.
	 */
	public boolean isManaged () {
		return isManaged;
	}

	/**
	 * Disposes all resources associated with the texture
	 */
	public void dispose () {
		buffer.put(0, glHandle);
		Gdx.gl.glDeleteTextures(1, buffer);
		if(isManaged)
			managedTextures.remove(this);
	}

	/**
	 * @return the OpenGL texture object handle so you can change texture parameters.
	 */
	public int getTextureObjectHandle () {
		return glHandle;
	}

	/**
	 * Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind
	 * this texture!
	 * 
	 * @param u the u wrap
	 * @param v the v wrap
	 */
	public void setWrap (TextureWrap u, TextureWrap v) {
		this.uWrap = u;
		this.vWrap = v;
		bind();
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, u == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, v == TextureWrap.ClampToEdge?GL10.GL_CLAMP_TO_EDGE:GL10.GL_REPEAT);
	}
	
	public void setFilter(TextureFilter minFilter, TextureFilter magFilter) {
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		bind();
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, getTextureFilter(minFilter));
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, getTextureFilter(magFilter));
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
	
	/**
	 * Clears all managed textures. This is an internal method. Do not use it!
	 */
	public static void clearAllTextures () {
		managedTextures.clear();
	}
	
	/**
	 * Invalidate all managed textures. This is an internal method. Do not use it!
	 */
	public static void invalidateAllTextures () {
		for (int i = 0; i < managedTextures.size(); i++) {			
			Texture texture = managedTextures.get(i);			
			texture.reload();			
		}		
	}
	
	/**
	 * @return the {@link Format} of the Texture. For {@link TextureData} based textures this will always be RGBA8888.
	 */
	public Format getFormat() {
		return format;
	}

	static public void setEnforcePotImages (boolean enforcePotImages) {
		Texture.enforcePotImages = enforcePotImages;
	}

//	public static Texture getFrameBufferTexture () {
//		ByteBuffer pixels = BufferUtils.newByteBuffer(Gdx.graphics.getWidth() * Gdx.graphics.getHeight() * 4);
//		Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
//		Pixmap pixmap = new Pixmap(MathUtils.nextPowerOfTwo(Gdx.graphics.getWidth()), MathUtils.nextPowerOfTwo(Gdx.graphics.getHeight()), Format.RGBA8888);
//		
//		pixels.position(0);		
//		int scanSrc = Gdx.graphics.getWidth() * 4;
//		int scanDst = pixmap.getWidth() * 4;
//		int posSrc = 0;
//		int posDst = 0;
//		ByteBuffer dst = pixmap.getPixels();
//		ByteBuffer src = pixels;
//		for(int i = 0; i < Gdx.graphics.getHeight(); i++) {
//			src.position(posSrc);
//			src.limit(posSrc + scanSrc);
//			dst.position(posDst);
//			dst.limit(posDst + scanDst);
//			dst.put(src);
//			
//			posSrc += scanSrc;
//			posDst += scanDst;			
//		}
//		
//		dst.position(0);
//		dst.limit(pixmap.getWidth() * pixmap.getHeight() * 4);
//		return new Texture(pixmap);
//		
//		Texture texture = new Texture(MathUtils.nextPowerOfTwo(Gdx.graphics.getWidth()),
//												MathUtils.nextPowerOfTwo(Gdx.graphics.getHeight()), 
//												Format.RGBA8888);
//		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//		Gdx.gl.glFlush();
//		texture.bind();
//		Gdx.gl.glCopyTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);	
//		return texture;
//	}
}

package com.badlogic.gdx.graphics;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Class representing an OpenGL texture by its target and handle. 
 * Keeps track of its state like the TextureFilter and TextureWrap.
 * Also provides some (protected) static methods to create TextureData and upload image data. 
 * @author badlogic, Xoppa
 */
public abstract class GLTexture implements Disposable {
	/** The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D */
	public final int glTarget;
	protected int glHandle;
	protected TextureFilter minFilter = TextureFilter.Nearest;
	protected TextureFilter magFilter = TextureFilter.Nearest;
	protected TextureWrap uWrap = TextureWrap.ClampToEdge;
	protected TextureWrap vWrap = TextureWrap.ClampToEdge;
	
	/** @return the width of the texture in pixels */
	public abstract int getWidth();
	
	/** @return the height of the texture in pixels */
	public abstract int getHeight();
	
	/** @return the depth of the texture in pixels */
	public abstract int getDepth();
	
	/** Generates a new OpenGL texture with the specified target. */ 
	public GLTexture(int glTarget) {
		this(glTarget, createGLHandle());
	}
	
	public GLTexture(int glTarget, int glHandle) {
		this.glTarget = glTarget;
		this.glHandle = glHandle;
	}
	
	/** @return whether this texture is managed or not. */
	public abstract boolean isManaged ();
	
	protected abstract void reload();
	
	/** Binds this texture. The texture will be bound to the currently active texture unit specified via
	 * {@link GLCommon#glActiveTexture(int)}. */
	public void bind () {
		Gdx.gl.glBindTexture(glTarget, glHandle);
	}

	/** Binds the texture to the given texture unit. Sets the currently active texture unit via
	 * {@link GLCommon#glActiveTexture(int)}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS). */
	public void bind (int unit) {
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0 + unit);
		Gdx.gl.glBindTexture(glTarget, glHandle);
	}
	
	/** @return The {@link Texture.TextureFilter} used for minification. */
	public TextureFilter getMinFilter () {
		return minFilter;
	}

	/** @return The {@link Texture.TextureFilter} used for magnification. */
	public TextureFilter getMagFilter () {
		return magFilter;
	}

	/** @return The {@link Texture.TextureWrap} used for horizontal (U) texture coordinates. */
	public TextureWrap getUWrap () {
		return uWrap;
	}

	/** @return The {@link Texture.TextureWrap} used for vertical (V) texture coordinates. */
	public TextureWrap getVWrap () {
		return vWrap;
	}
	
	/** @return The OpenGL handle for this texture. */
	public int getTextureObjectHandle () {
		return glHandle;
	}

	/** Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
	 * @param u the u wrap
	 * @param v the v wrap */
	public void unsafeSetWrap(TextureWrap u, TextureWrap v) {
		unsafeSetWrap(u, v, false);
	}
	
	/** Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
	 * @param u the u wrap
	 * @param v the v wrap 
	 * @param force True to always set the values, even if they are the same as the current values. */
	public void unsafeSetWrap(TextureWrap u, TextureWrap v, boolean force) {
		if (u != null && (force || uWrap != u)) {
			Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_WRAP_S, u.getGLEnum());
			uWrap = u;
		}
		if (v != null && (force || vWrap != v)) {
			Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_WRAP_T, v.getGLEnum());
			vWrap = v;
		}
	}
	
	/** Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
	 * @param u the u wrap
	 * @param v the v wrap */
	public void setWrap (TextureWrap u, TextureWrap v) {
		this.uWrap = u;
		this.vWrap = v;
		bind();
		Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_WRAP_S, u.getGLEnum());
		Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_WRAP_T, v.getGLEnum());
	}
	
	/** Sets the {@link TextureFilter} for this texture for minification and magnification.
	 * Assumes the texture is bound and active!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter) {
		unsafeSetFilter(minFilter, magFilter, false);
	}
	
	
	/** Sets the {@link TextureFilter} for this texture for minification and magnification.
	 * Assumes the texture is bound and active!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter 
	 * @param force True to always set the values, even if they are the same as the current values. */
	public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter, boolean force) {
		if (minFilter != null && (force || this.minFilter != minFilter)) {
			Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
			this.minFilter = minFilter;
		}
		if (magFilter != null && (force || this.magFilter != magFilter)) {
			Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
			this.magFilter = magFilter;
		}
	}

	/** Sets the {@link TextureFilter} for this texture for minification and magnification.
	 * This will bind this texture!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		bind();
		Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
		Gdx.gl.glTexParameterf(glTarget, GL10.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
	}
	
	/** Destroys the OpenGL Texture as specified by the glHandle. */
	protected void delete() {
		if (glHandle != 0) {
			buffer.put(0, glHandle);
			Gdx.gl.glDeleteTextures(1, buffer);
			glHandle = 0;
		}
	}
	
	@Override
	public void dispose () {
		delete();
	}
	
	
	private static boolean enforcePotImages = true;
	private static final IntBuffer buffer = BufferUtils.newIntBuffer(1);
	
	/** @param enforcePotImages whether to enforce power of two images in OpenGL ES 1.0 or not. */
	public static void setEnforcePotImages (boolean enforcePotImages) {
		GLTexture.enforcePotImages = enforcePotImages;
	}
	
	public static boolean getEnforcePotImage () {
		return enforcePotImages;
	}
	
	protected static TextureData createTextureData(FileHandle file, Format format, boolean useMipMaps) {
		if (file == null)
			return null;
		if (file.name().endsWith(".etc1"))
			return new ETC1TextureData(file, useMipMaps);
		return new FileTextureData(file, null, format, useMipMaps);
	}
	
	protected static TextureData createTextureData(FileHandle file, boolean useMipMaps) {
		return createTextureData(file, null, useMipMaps);
	}
	
	protected static int createGLHandle () {
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl.glGenTextures(1, buffer);
		return buffer.get(0);
	}
	
	protected static void uploadImageData (int target, TextureData data) {
		if (data == null) {
			// FIXME: remove texture on target?
			return;
		}
		
		if (!data.isPrepared()) 
			data.prepare();
		
		if (enforcePotImages && Gdx.gl20 == null
			&& (!MathUtils.isPowerOfTwo(data.getWidth()) || !MathUtils.isPowerOfTwo(data.getHeight()))) {
			throw new GdxRuntimeException("Texture width and height must be powers of two: " + data.getWidth() + "x"
				+ data.getHeight());
		}
		
		final TextureDataType type = data.getType(); 
		if (type == TextureDataType.Compressed || type == TextureDataType.Float) {
			data.consumeCompressedData(target);
			return;
		}
		
		Pixmap pixmap = data.consumePixmap();
		boolean disposePixmap = data.disposePixmap();
		if (data.getFormat() != pixmap.getFormat()) {
			Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), data.getFormat());
			Blending blend = Pixmap.getBlending();
			Pixmap.setBlending(Blending.None);
			tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
			Pixmap.setBlending(blend);
			if(data.disposePixmap()) {
				pixmap.dispose();
			}
			pixmap = tmp;
			disposePixmap = true;
		}

		Gdx.gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
		if (data.useMipMaps()) {
			MipMapGenerator.generateMipMap(target, pixmap, pixmap.getWidth(), pixmap.getHeight());
		} else {
			Gdx.gl.glTexImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		}
		if (disposePixmap)
			pixmap.dispose();
	}
}

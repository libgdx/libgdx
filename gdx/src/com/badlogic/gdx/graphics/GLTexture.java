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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.FloatBuffer;

/** Class representing an OpenGL texture by its target and handle. Keeps track of its state like the TextureFilter and TextureWrap.
 * Also provides some (protected) static methods to create TextureData and upload image data.
 * @author badlogic, Xoppa */
public abstract class GLTexture implements Disposable {
	/** The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D */
	public final int glTarget;
	protected int glHandle;
	protected TextureFilter minFilter = TextureFilter.Nearest;
	protected TextureFilter magFilter = TextureFilter.Nearest;
	protected TextureWrap uWrap = TextureWrap.ClampToEdge;
	protected TextureWrap vWrap = TextureWrap.ClampToEdge;
	protected float anisotropicFilterLevel = 1.0f;
	private static float maxAnisotropicFilterLevel = 0;

	/** @return the width of the texture in pixels */
	public abstract int getWidth ();

	/** @return the height of the texture in pixels */
	public abstract int getHeight ();

	/** @return the depth of the texture in pixels */
	public abstract int getDepth ();

	/** Generates a new OpenGL texture with the specified target. */
	public GLTexture (int glTarget) {
		this(glTarget, Gdx.gl.glGenTexture ());
	}

	public GLTexture (int glTarget, int glHandle) {
		this.glTarget = glTarget;
		this.glHandle = glHandle;
	}

	/** @return whether this texture is managed or not. */
	public abstract boolean isManaged ();

	protected abstract void reload ();

	/** Binds this texture. The texture will be bound to the currently active texture unit specified via
	 * {@link GL20#glActiveTexture(int)}. */
	public void bind () {
		Gdx.gl.glBindTexture(glTarget, glHandle);
	}

	/** Binds the texture to the given texture unit. Sets the currently active texture unit via {@link GL20#glActiveTexture(int)}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS). */
	public void bind (int unit) {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + unit);
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
	public void unsafeSetWrap (TextureWrap u, TextureWrap v) {
		unsafeSetWrap(u, v, false);
	}

	/** Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
	 * @param u the u wrap
	 * @param v the v wrap
	 * @param force True to always set the values, even if they are the same as the current values. */
	public void unsafeSetWrap (TextureWrap u, TextureWrap v, boolean force) {
		if (u != null && (force || uWrap != u)) {
			Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
			uWrap = u;
		}
		if (v != null && (force || vWrap != v)) {
			Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
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
		Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
		Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
	}

	/** Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is bound and active!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	public void unsafeSetFilter (TextureFilter minFilter, TextureFilter magFilter) {
		unsafeSetFilter(minFilter, magFilter, false);
	}

	/** Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is bound and active!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter
	 * @param force True to always set the values, even if they are the same as the current values. */
	public void unsafeSetFilter (TextureFilter minFilter, TextureFilter magFilter, boolean force) {
		if (minFilter != null && (force || this.minFilter != minFilter)) {
			Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
			this.minFilter = minFilter;
		}
		if (magFilter != null && (force || this.magFilter != magFilter)) {
			Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
			this.magFilter = magFilter;
		}
	}

	/** Sets the {@link TextureFilter} for this texture for minification and magnification. This will bind this texture!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		bind();
		Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
		Gdx.gl.glTexParameteri(glTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
	}

	/**
	 * Sets the anisotropic filter level for the texture. Assumes the texture is bound and active!
	 *
	 * @param level The desired level of filtering. The maximum level supported by the device up to this value will be used.
	 * @return The actual level set, which may be lower than the provided value due to device limitations.
	 */
	public float unsafeSetAnisotropicFilter (float level) {
		return unsafeSetAnisotropicFilter(level, false);
	}

	/**
	 * Sets the anisotropic filter level for the texture. Assumes the texture is bound and active!
	 *
	 * @param level The desired level of filtering. The maximum level supported by the device up to this value will be used.
	 * @param force True to always set the value, even if it is the same as the current values.
	 * @return The actual level set, which may be lower than the provided value due to device limitations.
	 */
	public float unsafeSetAnisotropicFilter (float level, boolean force) {
		float max = getMaxAnisotropicFilterLevel();
		if (max == 1f)
			return 1f;
		level = Math.min(level, max);
		if (!force && MathUtils.isEqual(level, anisotropicFilterLevel, 0.1f))
			return anisotropicFilterLevel;
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, level);
		return anisotropicFilterLevel = level;
	}

	/**
	 * Sets the anisotropic filter level for the texture. This will bind the texture!
	 *
	 * @param level The desired level of filtering. The maximum level supported by the device up to this value will be used.
	 * @return The actual level set, which may be lower than the provided value due to device limitations.
	 */
	public float setAnisotropicFilter (float level) {
		float max = getMaxAnisotropicFilterLevel();
		if (max == 1f)
			return 1f;
		level = Math.min(level, max);
		if (MathUtils.isEqual(level, anisotropicFilterLevel, 0.1f))
			return level;
		bind();
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, level);
		return anisotropicFilterLevel = level;
	}

	/**
	 * @return The currently set anisotropic filtering level for the texture, or 1.0f if none has been set.
	 */
	public float getAnisotropicFilter () {
		return anisotropicFilterLevel;
	}

	/**
	 * @return The maximum supported anisotropic filtering level supported by the device.
	 */
	public static float getMaxAnisotropicFilterLevel () {
		if (maxAnisotropicFilterLevel > 0)
			return maxAnisotropicFilterLevel;
		if (Gdx.graphics.supportsExtension("GL_EXT_texture_filter_anisotropic")) {
			FloatBuffer buffer = BufferUtils.newFloatBuffer(16);
			buffer.position(0);
			buffer.limit(buffer.capacity());
			Gdx.gl20.glGetFloatv(GL20.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer);
			return maxAnisotropicFilterLevel = buffer.get(0);
		}
		return maxAnisotropicFilterLevel = 1f;
	}

	/** Destroys the OpenGL Texture as specified by the glHandle. */
	protected void delete () {
		if (glHandle != 0) {
			Gdx.gl.glDeleteTexture (glHandle);
			glHandle = 0;
		}
	}

	@Override
	public void dispose () {
		delete();
	}

	protected static void uploadImageData (int target, TextureData data) {
		uploadImageData(target, data, 0);
	}
	
	public static void uploadImageData (int target, TextureData data, int miplevel) {
		if (data == null) {
			// FIXME: remove texture on target?
			return;
		}

		if (!data.isPrepared()) data.prepare();

		final TextureDataType type = data.getType();
		if (type == TextureDataType.Custom) {
			data.consumeCustomData(target);
			return;
		}

		Pixmap pixmap = data.consumePixmap();
		boolean disposePixmap = data.disposePixmap();
		if (data.getFormat() != pixmap.getFormat()) {
			Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), data.getFormat());
			tmp.setBlending(Blending.None);
			tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
			if (data.disposePixmap()) {
				pixmap.dispose();
			}
			pixmap = tmp;
			disposePixmap = true;
		}

		Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
		if (data.useMipMaps()) {
			MipMapGenerator.generateMipMap(target, pixmap, pixmap.getWidth(), pixmap.getHeight());
		} else {
			Gdx.gl.glTexImage2D(target, miplevel, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		}
		if (disposePixmap) pixmap.dispose();
	}
}

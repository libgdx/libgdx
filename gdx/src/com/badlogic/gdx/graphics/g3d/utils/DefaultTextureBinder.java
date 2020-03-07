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

package com.badlogic.gdx.graphics.g3d.utils;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Class that you assign a range of texture units and binds textures for you within that range. It does some basic usage tracking
 * to avoid unnecessary bind calls.
 * @author xoppa */
public final class DefaultTextureBinder implements TextureBinder {
	public final static int ROUNDROBIN = 0;
	public final static int LRU = 1;
	/** GLES only supports up to 32 textures */
	public final static int MAX_GLES_UNITS = 32;
	/** The index of the first exclusive texture unit */
	private final int offset;
	/** The amount of exclusive textures that may be used */
	private final int count;
	/** The textures currently exclusive bound */
	private final GLTexture[] textures;
	/** Texture units ordered from most to least recently used */
	private int [] unitsLRU;
	/** The method of binding to use */
	private final int method;
	/** Flag to indicate the current texture is reused */
	private boolean reused;

	private int reuseCount = 0; // TODO remove debug code
	private int bindCount = 0; // TODO remove debug code

	/** Uses all available texture units and reuse weight of 3 */
	public DefaultTextureBinder (final int method) {
		this(method, 0);
	}

	/** Uses all remaining texture units and reuse weight of 3 */
	public DefaultTextureBinder (final int method, final int offset) {
		this(method, offset, -1);
	}

	public DefaultTextureBinder (final int method, final int offset, int count) {
		final int max = Math.min(getMaxTextureUnits(), MAX_GLES_UNITS);
		if (count < 0) count = max - offset;
		if (offset < 0 || count < 0 || (offset + count) > max)
			throw new GdxRuntimeException("Illegal arguments");
		this.method = method;
		this.offset = offset;
		this.count = count;
		this.textures = new GLTexture[count];
		this.unitsLRU = (method == LRU) ? new int[count] : null;
	}

	private static int getMaxTextureUnits () {
		IntBuffer buffer = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, buffer);
		return buffer.get(0);
	}

	@Override
	public void begin () {
		for (int i = 0; i < count; i++) {
			textures[i] = null;
			if (unitsLRU != null) unitsLRU[i] = i;
		}
	}

	@Override
	public void end () {
		/*
		 * No need to unbind and textures are set to null in begin() for(int i = 0; i < count; i++) { if (textures[i] != null) {
		 * Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + offset + i); Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0); textures[i] = null; }
		 * }
		 */
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	@Override
	public final int bind (final TextureDescriptor textureDesc) {
		return bindTexture(textureDesc, false);
	}

	private final TextureDescriptor tempDesc = new TextureDescriptor();

	@Override
	public final int bind (final GLTexture texture) {
		tempDesc.set(texture, null, null, null, null);
		return bindTexture(tempDesc, false);
	}

	private final int bindTexture (final TextureDescriptor textureDesc, final boolean rebind) {
		final int idx, result;
		final GLTexture texture = textureDesc.texture;
		reused = false;

		switch (method) {
		case ROUNDROBIN:
			result = offset + (idx = bindTextureRoundRobin(texture));
			break;
		case LRU:
			result = offset + (idx = bindTextureLRU(texture));
			break;
		default:
			return -1;
		}

		if (reused) {
			reuseCount++;
			if (rebind)
				texture.bind(result);
			else
				Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + result);
		} else
			bindCount++;
		texture.unsafeSetWrap(textureDesc.uWrap, textureDesc.vWrap);
		texture.unsafeSetFilter(textureDesc.minFilter, textureDesc.magFilter);
		return result;
	}

	private int currentTexture = 0;

	private final int bindTextureRoundRobin (final GLTexture texture) {
		for (int i = 0; i < count; i++) {
			final int idx = (currentTexture + i) % count;
			if (textures[idx] == texture) {
				reused = true;
				return idx;
			}
		}
		currentTexture = (currentTexture + 1) % count;
		textures[currentTexture] = texture;
		texture.bind(offset + currentTexture);
		return currentTexture;
	}

	private final int bindTextureLRU (final GLTexture texture) {
		int i;
		for (i = 0; i < count; i++) {
			final int idx = unitsLRU[i];
			if (textures[idx] == texture) {
				reused = true;
				break;
			}
			if (textures[idx] == null) {
				break;
			}
		}
		if (i >= count) i = count - 1;
		final int idx = unitsLRU[i];
		while (i > 0) {
			unitsLRU[i] = unitsLRU[i - 1];
			i--;
		}
		unitsLRU[0] = idx;
		if (!reused) {
			textures[idx] = texture;
			texture.bind(offset + idx);
		}
		return idx;
	}

	@Override
	public final int getBindCount () {
		return bindCount;
	}

	@Override
	public final int getReuseCount () {
		return reuseCount;
	}

	@Override
	public final void resetCounts () {
		bindCount = reuseCount = 0;
	}
}

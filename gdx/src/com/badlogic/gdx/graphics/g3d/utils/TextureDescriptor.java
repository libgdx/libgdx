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

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;

public class TextureDescriptor<T extends GLTexture> implements Comparable<TextureDescriptor<T>> {
	public T texture = null;
	public Texture.TextureFilter minFilter;
	public Texture.TextureFilter magFilter;
	public Texture.TextureWrap uWrap;
	public Texture.TextureWrap vWrap;

	// TODO add other values, see http://www.opengl.org/sdk/docs/man/xhtml/glTexParameter.xml

	public TextureDescriptor (final T texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter,
		final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		set(texture, minFilter, magFilter, uWrap, vWrap);
	}

	public TextureDescriptor (final T texture) {
		this(texture, null, null, null, null);
	}

	public TextureDescriptor () {
	}

	public void set (final T texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter,
		final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		this.texture = texture;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
	}

	public <V extends T> void set (final TextureDescriptor<V> other) {
		texture = other.texture;
		minFilter = other.minFilter;
		magFilter = other.magFilter;
		uWrap = other.uWrap;
		vWrap = other.vWrap;
	}

	@Override
	public boolean equals (Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof TextureDescriptor)) return false;
		final TextureDescriptor<?> other = (TextureDescriptor<?>)obj;
		return other.texture == texture && other.minFilter == minFilter && other.magFilter == magFilter && other.uWrap == uWrap
			&& other.vWrap == vWrap;
	}
	
	@Override
	public int hashCode () {
		long result = texture.glTarget;
		result = 811 * result + texture.getTextureObjectHandle();
		result = 811 * result + minFilter.getGLEnum();
		result = 811 * result + magFilter.getGLEnum();
		result = 811 * result + uWrap.getGLEnum();
		result = 811 * result + vWrap.getGLEnum();
		return (int)(result ^ (result >> 32));
	}

	@Override
	public int compareTo (TextureDescriptor<T> o) {
		if (o == this) return 0;
		if (texture.glTarget != o.texture.glTarget)
			return texture.glTarget - o.texture.glTarget;
		if (texture.getTextureObjectHandle() != o.texture.getTextureObjectHandle())
			return texture.getTextureObjectHandle() - o.texture.getTextureObjectHandle();
		if (minFilter != o.minFilter) return minFilter.getGLEnum() - o.minFilter.getGLEnum();
		if (magFilter != o.magFilter) return magFilter.getGLEnum() - o.magFilter.getGLEnum();
		if (uWrap != o.uWrap) return uWrap.getGLEnum() - o.uWrap.getGLEnum();
		if (vWrap != o.vWrap) return vWrap.getGLEnum() - o.vWrap.getGLEnum();
		return 0;
	}
}

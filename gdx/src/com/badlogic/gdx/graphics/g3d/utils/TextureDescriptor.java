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
		long result = (texture == null ? 0 : texture.glTarget);
		result = 811 * result + (texture == null ? 0 : texture.getTextureObjectHandle());
		result = 811 * result + (minFilter == null ? 0 : minFilter.getGLEnum());
		result = 811 * result + (magFilter == null ? 0 : magFilter.getGLEnum());
		result = 811 * result + (uWrap == null ? 0 : uWrap.getGLEnum());
		result = 811 * result + (vWrap == null ? 0 : vWrap.getGLEnum());
		return (int)(result ^ (result >> 32));
	}

	@Override
	public int compareTo (TextureDescriptor<T> o) {
		if (o == this) return 0;
		int t1 = texture == null ? 0 : texture.glTarget;
		int t2 = o.texture == null ? 0 : o.texture.glTarget;
		if (t1 != t2) return t1 - t2;
		int h1 = texture == null ? 0 : texture.getTextureObjectHandle();
		int h2 = o.texture == null ? 0 : o.texture.getTextureObjectHandle();
		if (h1 != h2) return h1 - h2;
		if (minFilter != o.minFilter)
			return (minFilter == null ? 0 : minFilter.getGLEnum()) - (o.minFilter == null ? 0 : o.minFilter.getGLEnum());
		if (magFilter != o.magFilter)
			return (magFilter == null ? 0 : magFilter.getGLEnum()) - (o.magFilter == null ? 0 : o.magFilter.getGLEnum());
		if (uWrap != o.uWrap) return (uWrap == null ? 0 : uWrap.getGLEnum()) - (o.uWrap == null ? 0 : o.uWrap.getGLEnum());
		if (vWrap != o.vWrap) return (vWrap == null ? 0 : vWrap.getGLEnum()) - (o.vWrap == null ? 0 : o.vWrap.getGLEnum());
		return 0;
	}
}

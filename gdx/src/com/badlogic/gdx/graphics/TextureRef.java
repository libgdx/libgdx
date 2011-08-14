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

/** <p>
 * A reference counted wrapper for a {@link Texture}. TextureRefs are managed by the {@link TextureDict}.
 * </p>
 * 
 * @author Dave Clayton <contact@redskyforge.com> */
public class TextureRef {
	public String Name;
	private int mRefCount;
	private Texture mTexture;

	/** Create a new TextureRef with the given name and texture.
	 * @param name The texture's name, typically its filesystem path.
	 * @param texture The texture it reference counts. */
	public TextureRef (String name, Texture texture) {
		Name = name;
		mTexture = texture;
		mRefCount = 1;
	}

	/** Adds a reference to the texture. */
	public void addRef () {
		mRefCount++;
	}

	/** Removes a reference to the texture. If the internal reference count reaches 0, the texture is disposed.
	 * @return the new reference count. */
	public int unload () {
		if (--mRefCount == 0) {
			mTexture.dispose();
			mTexture = null;
			// Note: This doesn't seem the best way to do this, but it works. Re-factoring welcome :)
			TextureDict.removeTexture(Name);
		}
		return mRefCount;
	}

	public void dispose () {
		mTexture.dispose();
	}

	/** Binds the texture in OpenGL. */
	public void bind () {
		mTexture.bind();
	}

	/** Gets the wrapped texture.
	 * @return the texture. */
	public Texture get () {
		return mTexture;
	}
}

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
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * Loads image data for a texture. Used with
 * {@link Graphics#newTexture(TextureData, TextureFilter, TextureFilter, TextureWrap, TextureWrap) newTexture}, this allows custom
 * image loading for managed textures. If the OpenGL context is lost, the TextureData will be asked to load again when the context
 * is restored. The TextureData doesn't necessary need to keep the image data in memory between loads.
 */
public interface TextureData {
	/**
	 * Loads the image data into the currently bound texture. Usually
	 * {@link GL10#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)} is used.
	 */
	public void load ();

	/**
	 * Returns the width of the texture, which must be a power of two. This will not be called before {@link #load()}.
	 */
	public int getWidth ();

	/**
	 * Returns the height of the texture, which must be a power of two. This will not be called before {@link #load()}.
	 */
	public int getHeight ();
}

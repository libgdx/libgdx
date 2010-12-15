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

/**
 * <p>
 * A Texture wraps a standard OpenGL ES texture.
 * </p>
 * 
 * <p>
 * It is constructed by an {@link Graphics} via one of the following methods:
 * <ul>
 * <li>{@link Graphics#newTexture(com.badlogic.gdx.files.FileHandle, TextureFilter, TextureFilter, TextureWrap, TextureWrap)}</li>
 * <li>{@link Graphics#newUnmanagedTexture(Pixmap, TextureFilter, TextureFilter, TextureWrap, TextureWrap)}</li>
 * <li>
 * {@link Graphics#newUnmanagedTexture(int, int, com.badlogic.gdx.graphics.Pixmap.Format, TextureFilter, TextureFilter, TextureWrap, TextureWrap)}
 * </li>
 * </ul>
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
public interface Texture {
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

	/**
	 * Binds this texture. The texture will be bound to the currently active texture unit specified via
	 * {@link GLCommon#glActiveTexture(int)}.
	 */
	public void bind ();

	/**
	 * Draws the given {@link Pixmap} to the texture at position x, y. No clipping is performed so you have to make sure that you
	 * draw only inside the texture region.
	 * 
	 * @param pixmap The Pixmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels
	 */
	public void draw (Pixmap pixmap, int x, int y);

	/**
	 * 
	 * @return the width of the texture in pixels
	 */
	public int getWidth ();

	/**
	 * 
	 * @return the height of the texture in pixels
	 */
	public int getHeight ();

	/**
	 * @return whether this texture is managed or not.
	 */
	public boolean isManaged ();

	/**
	 * Disposes all resources associated with the texture
	 */
	public void dispose ();

	/**
	 * @return the OpenGL texture object handle so you can change texture parameters.
	 */
	public int getTextureObjectHandle ();

	public void setWrap (TextureWrap x, TextureWrap y);
}

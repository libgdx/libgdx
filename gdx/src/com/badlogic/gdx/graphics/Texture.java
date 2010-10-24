/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics;

/**
 * <p>
 * A Texture wraps a standard OpenGL ES texture.
 * </p>
 * 
 * <p>
 * It is constructed by an {@link Graphics} via one of the {@link Graphics.newTexture()} methods.
 * </p>
 * 
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all textures get invalidated. This happens when a user switches to
 * another application or receives an incoming call. Managed textures get reloaded automatically.
 * </p>
 * 
 * <p>
 * A Texture has to be bound via the {@link Texture.bind()} method in order for it to be applied to geometry.
 * </p>
 * 
 * <p>
 * You can draw {@link Pixmap}s to a texture at any time. The changes will be automatically uploaded to texture memory. This is of
 * course not extremely fast so use it with care.
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
	 * Texture filter enum featuring the 3 most used filters.
	 * @author badlogicgames@gmail.com
	 * 
	 */
	public enum TextureFilter {
		Nearest, Linear, MipMap
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
	 * Binds this texture. You have to enable texturing via {@link Application.enable( RenderState.Texturing )} in order for the
	 * texture to actually be applied to geometry.
	 */
	public void bind ();

	/**
	 * Draws the given {@link Pixmap} to the texture at position x, y.
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
	 * @return
	 */
	public void dispose ();

	/**
	 * @return the OpenGL texture object handle so you can change texture parameters.
	 */
	public int getTextureObjectHandle ();
}

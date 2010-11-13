
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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Texture;

/**
 * Responsible for binding textures, may implement a strategy to avoid binding a texture
 * unnecessarily. A TextureBinder may decide to which texture unit it binds a texture.
 * @author badlogic
 *
 */
public interface TextureBinder {
	/**
	 * Prepares the binder for operation, must be matched with a call
	 * to {@link #end()}.
	 */
	public void begin();
	
	/**
	 * Disables all used texture units and unbinds textures. Resets the counts.
	 */
	public void end();
	
	/**
	 * Binds the texture to an available unit and applies the
	 * filters in the descriptor.
	 * @param textureDescriptor the {@link TextureDescriptor}
	 * @return the unit the texture was bound to
	 */
	public int bind(TextureDescriptor textureDescriptor);
	
	/**
	 * @return the number of binds actualy executed since the last call to {@link #resetCounts()}
	 */
	public int getBindCount();
	/**
	 * @return the number of binds that could be avoided by reuse
	 */
	public int getReuseCount();
	
	/**
	 * Resets the bind/reuse counts
	 */
	public void resetCounts();
}

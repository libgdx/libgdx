package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;

/**
 * @brief Generalises the concept of tile in a TiledMap
 *
 */
public interface TiledMapTile {

	public enum BlendMode {
		NONE,
		ALPHA
	}

	public int getId();
	public void setId(int id);
	
	/**
	 * @return the {@link BlendMode} to use for rendering the tile
	 */	
	public BlendMode getBlendMode();
	
	/**
	 * Sets the {@link BlendMode} to use for rendering the tile
	 * 
	 * @param blendMode the blend mode to use for rendering the tile
	 */
	public void setBlendMode(BlendMode blendMode);
	
	/**
	 * @return texture region used to render the tile
	 */
	public TextureRegion getTextureRegion();

	/**
	 * @return tile's properties set
	 */
	public MapProperties getProperties();
	
}

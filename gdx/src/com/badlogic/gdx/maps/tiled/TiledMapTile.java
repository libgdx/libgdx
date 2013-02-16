package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;

/**
 * @brief Generalises the concept of tile in a TiledMap
 *
 */
public interface TiledMapTile {

	/**
	 * @return texture region used to render the tile
	 */
	public TextureRegion getTextureRegion();

	/**
	 * @return tile's properties set
	 */
	public MapProperties getProperties();
	
}

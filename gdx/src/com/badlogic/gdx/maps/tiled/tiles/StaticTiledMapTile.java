package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;

/**
 * @brief Represents a non changing TiledMapTile (can be cached)
 */
public class StaticTiledMapTile implements TiledMapTile {

	private MapProperties properties;
	
	private TextureRegion textureRegion;	

	/**
	 * @return tile's properties set
	 */
	@Override
	public MapProperties getProperties() {
		return properties;
	}

	/**
	 * @return texture region used to render the tile
	 */
	@Override
	public TextureRegion getTextureRegion() {
		return textureRegion;
	}
	
	/**
	 * Creates a static tile with the given region
	 * 
	 * @param textureRegion
	 */
	public StaticTiledMapTile(TextureRegion textureRegion) {
		this.textureRegion = textureRegion;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param copy
	 */
	public StaticTiledMapTile(StaticTiledMapTile copy) {
		this.properties.putAll(copy.properties);
		this.textureRegion = copy.textureRegion;
	}
	
}

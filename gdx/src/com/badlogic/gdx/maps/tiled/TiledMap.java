package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.Map;

/**
 * @brief Represents a Tiled created map, adds the concept of tiles and tilesets
 * 
 * @see Map
 */
public class TiledMap extends Map {
	
	private TiledMapTileSets tilesets;
	
	/**
	 * @return collection of tilesets for this map
	 */
	public TiledMapTileSets getTileSets() {
		return tilesets;
	}
	
	/**
	 * Creates empty TiledMap
	 */
	public TiledMap() {
		tilesets = new TiledMapTileSets();
	}
}

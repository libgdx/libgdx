package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * @brief Represents a tiled map, adds the concept of tiles and tilesets
 * 
 * @see Map
 */
public class TiledMap extends Map {
	private TiledMapTileSets tilesets;
	private Array<? extends Disposable> ownedResources;
	
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
	
	/**
	 * @return map width (in tile)
	 */
	public int getWidth() {
		return properties.get("width", Integer.class);
	}
	
	/**
	 * @return map height (in tile)
	 */
	public int getHeight() {
		return properties.get("height", Integer.class);
	}
	
	public int getTileWidth() {
		return properties.get("tilewidth", Integer.class);
	}
	
	public int getTileHeight() {
		return properties.get("tileheight", Integer.class);
	}
	
	/**
	 * Used by loaders to set resources when loading the map
	 * directly, without {@link AssetManager}. To be disposed in
	 * {@link #dispose()}.
	 * @param resources
	 */
	public void setOwnedResources(Array<? extends Disposable> resources) {
		this.ownedResources = resources;
	}
	
	@Override
	public void dispose() {
		if(ownedResources != null) {
			for(Disposable resource: ownedResources) {
				resource.dispose();
			}
		}
	}
}

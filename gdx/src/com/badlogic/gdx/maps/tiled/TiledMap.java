package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.utils.Array;

/**
 * @brief Represents a tiled map, adds the concept of tiles and tilesets
 * 
 * @see Map
 */
public class TiledMap extends Map {
	private TiledMapTileSets tilesets;
	private Array<Texture> ownedTextures;
	
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
	 * Used by TmxMapLoader to set textures when loading the map
	 * directly, without {@link AssetManager}. To be disposed in
	 * {@link #dispose()}.
	 * @param textures
	 */
	public void setOwnedTextures(Array<Texture> textures) {
		this.ownedTextures = textures;
	}
	
	@Override
	public void dispose() {
		if(ownedTextures != null) {
			for(Texture texture: ownedTextures) {
				texture.dispose();
			}
		}
	}
}

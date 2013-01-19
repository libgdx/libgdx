package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.Map;

public class TiledMap extends Map {
	
	private TiledMapTileSets tilesets;
	
	public TiledMapTileSets getTileSets() {
		return tilesets;
	}
	
	public TiledMap() {
		tilesets = new TiledMapTileSets();
	}
}

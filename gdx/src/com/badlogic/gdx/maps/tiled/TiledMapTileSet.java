package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.IntMap;

public class TiledMapTileSet {
	
	private String name;
	
	private IntMap<TiledMapTile> tiles;

	private MapProperties properties;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public MapProperties getProperties() {
		return properties;
	}
	
	public TiledMapTileSet() {
		tiles = new IntMap<TiledMapTile>();
		properties = new MapProperties();
	}
	
	public TiledMapTile getTile(int id) {
		return tiles.get(id);
	}
	
	public void putTile(int id, TiledMapTile tile) {
		tiles.put(id, tile);
	}
	
	public void removeTile(int id) {
		tiles.remove(id);
	}
	
}

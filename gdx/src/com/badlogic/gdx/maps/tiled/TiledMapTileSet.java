package com.badlogic.gdx.maps.tiled;

import java.util.Iterator;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.IntMap;

/**
 * @brief Set of TiledMapTile instances used to compose a TiledMapLayer
 */
public class TiledMapTileSet implements Iterable<TiledMapTile> {
	
	private String name;
	
	private IntMap<TiledMapTile> tiles;

	private MapProperties properties;

	/**
	 * @return tileset's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name new name for the tileset
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return tileset's properties set
	 */
	public MapProperties getProperties() {
		return properties;
	}
	
	/**
	 * Creates empty tileset
	 */
	public TiledMapTileSet() {
		tiles = new IntMap<TiledMapTile>();
		properties = new MapProperties();
	}
	
	/**
	 * @param id
	 * @return tile matching id, null if it doesn't exist
	 */
	public TiledMapTile getTile(int id) {
		return tiles.get(id);
	}
	
	/**
	 * @return iterator to tiles in this tileset
	 */
	@Override
	public Iterator<TiledMapTile> iterator() {
		return tiles.values().iterator();
	}
	
	/**
	 * Adds or replaces tile with that id
	 * 
	 * @param id
	 * @param tile
	 */
	public void putTile(int id, TiledMapTile tile) {
		tiles.put(id, tile);
	}
	
	/**
	 * @param id tile's id to be removed
	 */
	public void removeTile(int id) {
		tiles.remove(id);
	}

	public int size() {
		return tiles.size;
	}
	
}


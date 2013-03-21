package com.badlogic.gdx.maps.tiled;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

/**
 * @brief Collection of TiledMapTileSet
 */
public class TiledMapTileSets implements Iterable<TiledMapTileSet> {
	
	private Array<TiledMapTileSet> tilesets;
	
	/**
	 * Creates empty collection of tilesets
	 */
	public TiledMapTileSets() {
		tilesets = new Array<TiledMapTileSet>();
	}
	
	/**
	 * @param index
	 * @return tileset at index
	 */
	public TiledMapTileSet getTileSet(int index) {
		return tilesets.get(index);
	}
	
	/**
	 * @param name
	 * @return tileset with matching name, null if it doesn't exist
	 */
	public TiledMapTileSet getTileSet(String name) {
		for (TiledMapTileSet tileset : tilesets) {
			if (name.equals(tileset.getName())) {
				return tileset;
			}
		}
		return null;
	}
	
	/**
	 * @param tileset set to be added to the collection
	 */
	public void addTileSet(TiledMapTileSet tileset) {
		tilesets.add(tileset);
	}
	
	/**
	 * Removes tileset at index
	 * 
	 * @param index
	 */
	public void removeTileSet(int index) {
		tilesets.removeIndex(index);
	}
	
	/**
	 * @param tileset set to be removed
	 */
	public void removeTileSet(TiledMapTileSet tileset) {
		tilesets.removeValue(tileset, true);
	}
	
	/**
	 * @param id
	 * @return tile with matching id, null if it doesn't exist
	 */
	public TiledMapTile getTile(int id) {
		for (TiledMapTileSet tileset : tilesets) {
			TiledMapTile tile = tileset.getTile(id);
			if (tile != null) {
				return tile;
			}
		}
		return null;
	}
	
	/**
	 * @return iterator to tilesets
	 */
	@Override
	public Iterator<TiledMapTileSet> iterator() {
		return tilesets.iterator();
	}
	
}

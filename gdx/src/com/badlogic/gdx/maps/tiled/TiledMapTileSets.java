package com.badlogic.gdx.maps.tiled;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

public class TiledMapTileSets implements Iterable<TiledMapTileSet> {
	
	private Array<TiledMapTileSet> tilesets;
	
	public TiledMapTileSets() {
		tilesets = new Array<TiledMapTileSet>();
	}
	
	public TiledMapTileSet getTileSet(int index) {
		return tilesets.get(index);
	}
	
	public TiledMapTileSet getTileSet(String name) {
		for (TiledMapTileSet tileset : tilesets) {
			if (name.equals(tileset.getName())) {
				return tileset;
			}
		}
		return null;
	}
	
	public void addTileSet(TiledMapTileSet tileset) {
		tilesets.add(tileset);
	}
	
	public void removeTileSet(int index) {
		tilesets.removeIndex(index);
	}
	
	public void removeTileSet(TiledMapTileSet tileset) {
		tilesets.removeValue(tileset, true);
	}
	
	public TiledMapTile getTile(int id) {
		for (TiledMapTileSet tileset : tilesets) {
			TiledMapTile tile = tileset.getTile(id);
			if (tile != null) {
				return tile;
			}
		}
		return null;
	}
	
	@Override
	public Iterator<TiledMapTileSet> iterator() {
		return tilesets.iterator();
	}
	
}

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.maps.tiled;

import java.util.Iterator;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.IntMap;

/** @brief Set of {@link TiledMapTile} instances used to compose a TiledMapLayer */
public class TiledMapTileSet implements Iterable<TiledMapTile> {

	private String name;

	private IntMap<TiledMapTile> tiles;

	private MapProperties properties;

	/** @return tileset's name */
	public String getName () {
		return name;
	}

	/** @param name new name for the tileset */
	public void setName (String name) {
		this.name = name;
	}

	/** @return tileset's properties set */
	public MapProperties getProperties () {
		return properties;
	}

	/** Creates empty tileset */
	public TiledMapTileSet () {
		tiles = new IntMap<TiledMapTile>();
		properties = new MapProperties();
	}

	/** Gets the {@link TiledMapTile} that has the given id.
	 * 
	 * @param id the id of the {@link TiledMapTile} to retrieve.
	 * @return tile matching id, null if it doesn't exist */
	public TiledMapTile getTile (int id) {
		return tiles.get(id);
	}

	/** @return iterator to tiles in this tileset */
	@Override
	public Iterator<TiledMapTile> iterator () {
		return tiles.values().iterator();
	}

	/** Adds or replaces tile with that id
	 * 
	 * @param id the id of the {@link TiledMapTile} to add or replace.
	 * @param tile the {@link TiledMapTile} to add or replace. */
	public void putTile (int id, TiledMapTile tile) {
		tiles.put(id, tile);
	}

	/** @param id tile's id to be removed */
	public void removeTile (int id) {
		tiles.remove(id);
	}

	/** @return the size of this TiledMapTileSet. */
	public int size () {
		return tiles.size;
	}
}

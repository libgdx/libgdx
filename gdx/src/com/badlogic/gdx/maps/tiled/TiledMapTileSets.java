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

import com.badlogic.gdx.utils.Array;

/** @brief Collection of {@link TiledMapTileSet} */
public class TiledMapTileSets implements Iterable<TiledMapTileSet> {

	private Array<TiledMapTileSet> tilesets;

	/** Creates an empty collection of tilesets. */
	public TiledMapTileSets () {
		tilesets = new Array<TiledMapTileSet>();
	}

	/** @param index index to get the desired {@link TiledMapTileSet} at.
	 * @return tileset at index */
	public TiledMapTileSet getTileSet (int index) {
		return tilesets.get(index);
	}

	/** @param name Name of the {@link TiledMapTileSet} to retrieve.
	 * @return tileset with matching name, null if it doesn't exist */
	public TiledMapTileSet getTileSet (String name) {
		for (TiledMapTileSet tileset : tilesets) {
			if (name.equals(tileset.getName())) {
				return tileset;
			}
		}
		return null;
	}

	/** @param tileset set to be added to the collection */
	public void addTileSet (TiledMapTileSet tileset) {
		tilesets.add(tileset);
	}

	/** Removes tileset at index
	 * 
	 * @param index index at which to remove a tileset. */
	public void removeTileSet (int index) {
		tilesets.removeIndex(index);
	}

	/** @param tileset set to be removed */
	public void removeTileSet (TiledMapTileSet tileset) {
		tilesets.removeValue(tileset, true);
	}

	/** @param id id of the {@link TiledMapTile} to get.
	 * @return tile with matching id, null if it doesn't exist */
	public TiledMapTile getTile (int id) {
		for (TiledMapTileSet tileset : tilesets) {
			TiledMapTile tile = tileset.getTile(id);
			if (tile != null) {
				return tile;
			}
		}
		return null;
	}

	/** @return iterator to tilesets */
	@Override
	public Iterator<TiledMapTileSet> iterator () {
		return tilesets.iterator();
	}

}

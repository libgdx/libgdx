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

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** @brief Represents a tiled map, adds the concept of tiles and tilesets.
 * 
 * @see Map */
public class TiledMap extends Map {
	private TiledMapTileSets tilesets;
	private Array<? extends Disposable> ownedResources;

	/** @return collection of tilesets for this map. */
	public TiledMapTileSets getTileSets () {
		return tilesets;
	}

	/** Creates an empty TiledMap. */
	public TiledMap () {
		tilesets = new TiledMapTileSets();
	}

	/** Used by loaders to set resources when loading the map directly, without {@link AssetManager}. To be disposed in
	 * {@link #dispose()}.
	 * @param resources */
	public void setOwnedResources (Array<? extends Disposable> resources) {
		this.ownedResources = resources;
	}

	@Override
	public void dispose () {
		if (ownedResources != null) {
			for (Disposable resource : ownedResources) {
				resource.dispose();
			}
		}
	}
}

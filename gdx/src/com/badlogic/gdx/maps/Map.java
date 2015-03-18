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

package com.badlogic.gdx.maps;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;

/** A generic level map implementation.</p>
 * 
 * A map has {@link MapProperties} which describe general attributes. Availability of properties depends on the type of map, e.g.
 * what format is was loaded from etc.</p>
 * 
 * A map has {@link MapLayers}. Map layers are ordered and indexed. A {@link MapLayer} contains {@link MapObjects} which represent
 * things within the layer. Different types of {@link MapObject} are available, e.g. {@link CircleMapObject},
 * {@link TextureMapObject}, and so on.</p>
 * 
 * A map can be rendered by a {@link MapRenderer}. A MapRenderer implementation may chose to only render specific MapObject or
 * MapLayer types.</p>
 * 
 * There are more specialized implementations of Map for specific use cases. e.g. the {@link TiledMap} class and its associated
 * classes add functionality specifically for tile maps on top of the basic map functionality.</p>
 * 
 * Maps must be disposed through a call to {@link #dispose()} when no longer used. */
public class Map implements Disposable {
	private MapLayers layers = new MapLayers();
	private MapProperties properties = new MapProperties();

	/** Creates empty map */
	public Map () {

	}

	/** @return the map's layers */
	public MapLayers getLayers () {
		return layers;
	}

	/** @return the map's properties */
	public MapProperties getProperties () {
		return properties;
	}

	/** Disposes all resources like {@link Texture} instances that the map may own. */
	@Override
	public void dispose () {
	}
}

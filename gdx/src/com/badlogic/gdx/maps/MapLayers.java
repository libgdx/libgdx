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

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** Ordered list of {@link MapLayer} instances owned by a {@link Map} */
public class MapLayers implements Iterable<MapLayer> {
	private Array<MapLayer> layers = new Array<MapLayer>();

	/** @param index
	 * @return the MapLayer at the specified index */
	public MapLayer get (int index) {
		return layers.get(index);
	}

	/** @param name
	 * @return the first layer having the specified name, if one exists, otherwise null */
	public MapLayer get (String name) {
		for (int i = 0, n = layers.size; i < n; i++) {
			MapLayer layer = layers.get(i);
			if (name.equals(layer.getName())) {
				return layer;
			}
		}
		return null;
	}

	/** Get the index of the layer having the specified name, or -1 if no such layer exists. */
	public int getIndex (String name) {
		return getIndex(get(name));
	}

	/** Get the index of the layer in the collection, or -1 if no such layer exists. */
	public int getIndex (MapLayer layer) {
		return layers.indexOf(layer, true);
	}

	/** @return number of layers in the collection */
	public int getCount () {
		return layers.size;
	}

	/** @param layer layer to be added to the set */
	public void add (MapLayer layer) {
		this.layers.add(layer);
	}

	/** @param index removes layer at index */
	public void remove (int index) {
		layers.removeIndex(index);
	}

	/** @param layer layer to be removed */
	public void remove (MapLayer layer) {
		layers.removeValue(layer, true);
	}

	/** @return the number of map layers **/
	public int size () {
		return layers.size;
	}

	/** @param type
	 * @return array with all the layers matching type */
	public <T extends MapLayer> Array<T> getByType (Class<T> type) {
		return getByType(type, new Array<T>());
	}

	/** @param type
	 * @param fill array to be filled with the matching layers
	 * @return array with all the layers matching type */
	public <T extends MapLayer> Array<T> getByType (Class<T> type, Array<T> fill) {
		fill.clear();
		for (int i = 0, n = layers.size; i < n; i++) {
			MapLayer layer = layers.get(i);
			if (ClassReflection.isInstance(type, layer)) {
				fill.add((T)layer);
			}
		}
		return fill;
	}

	/** @return iterator to set of layers */
	@Override
	public Iterator<MapLayer> iterator () {
		return layers.iterator();
	}

}

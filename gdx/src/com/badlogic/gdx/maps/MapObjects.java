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

/** @brief Collection of MapObject instances */
public class MapObjects implements Iterable<MapObject> {

	private Array<MapObject> objects;

	/** Creates an empty set of MapObject instances */
	public MapObjects () {
		objects = new Array<MapObject>();
	}

	/** @param index
	 * @return the MapObject at the specified index */
	public MapObject get (int index) {
		return objects.get(index);
	}

	/** @param name
	 * @return the first object having the specified name, if one exists, otherwise null */
	public MapObject get (String name) {
		for (int i = 0, n = objects.size; i < n; i++) {
			MapObject object = objects.get(i);
			if (name.equals(object.getName())) {
				return object;
			}
		}
		return null;
	}

	/** Get the index of the object having the specified name, or -1 if no such object exists. */
	public int getIndex (String name) {
		return getIndex(get(name));
	}

	/** Get the index of the object in the collection, or -1 if no such object exists. */
	public int getIndex (MapObject object) {
		return objects.indexOf(object, true);
	}

	/** @return number of objects in the collection */
	public int getCount () {
		return objects.size;
	}

	/** @param object instance to be added to the collection */
	public void add (MapObject object) {
		this.objects.add(object);
	}

	/** @param index removes MapObject instance at index */
	public void remove (int index) {
		objects.removeIndex(index);
	}

	/** @param object instance to be removed */
	public void remove (MapObject object) {
		objects.removeValue(object, true);
	}

	/** @param type class of the objects we want to retrieve
	 * @return array filled with all the objects in the collection matching type */
	public <T extends MapObject> Array<T> getByType (Class<T> type) {
		return getByType(type, new Array<T>());
	}

	/** @param type class of the objects we want to retrieve
	 * @param fill collection to put the returned objects in
	 * @return array filled with all the objects in the collection matching type */
	public <T extends MapObject> Array<T> getByType (Class<T> type, Array<T> fill) {
		fill.clear();
		for (int i = 0, n = objects.size; i < n; i++) {
			MapObject object = objects.get(i);
			if (ClassReflection.isInstance(type, object)) {
				fill.add((T)object);
			}
		}
		return fill;
	}

	/** @return iterator for the objects within the collection */
	@Override
	public Iterator<MapObject> iterator () {
		return objects.iterator();
	}

}

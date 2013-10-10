package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/**
 * @brief Collection of MapObject instances
 */
public class MapObjects implements Iterable<MapObject> {

	private Array<MapObject> objects;

	/**
	 * Creates and empty set of MapObject instances
	 */
	public MapObjects() {
		objects = new Array<MapObject>();
	}
	
	/**
	 * @param index
	 * @return MapObject at index
	 */
	public MapObject get(int index) {
		return objects.get(index);
	}
	
	/**
	 * @param name
	 * @return name matching object, null if it´s not in the set
	 */
	public MapObject get(String name) {
		for (MapObject object : objects) {
			if (name.equals(object.getName())) {
				return object;
			}
		}
		return null;
	}
	
	/**
	 * @param object instance to be added to the collection
	 */
	public void add(MapObject object) {
		this.objects.add(object);
	}
	
	/**
	 * @param index removes MapObject instance at index
	 */
	public void remove(int index) {
		objects.removeIndex(index);
	}
	
	/**
	 * @param object instance to be removed
	 */
	public void remove(MapObject object) {
		objects.removeValue(object, true);
	}
	
	/**
	 * @return number of objects in the collection
	 */
	public int getCount() {
		return objects.size;
	}

	/**
	 * @param type class of the objects we want to retrieve
	 * @return array filled with all the objects in the collection matching type
	 */
	public <T extends MapObject> Array<T> getByType(Class<T> type) {
		return getByType(type, new Array<T>());
	}
	
	/**
	 * @param type class of the objects we want to retrieve
	 * @param fill collection to put the returned objects in
	 * @return array filled with all the objects in the collection matching type
	 */
	public <T extends MapObject> Array<T> getByType(Class<T> type, Array<T> fill) {
		fill.clear();
		for (MapObject object : objects) {
			if (ClassReflection.isInstance(type, object)) {
				fill.add((T) object);
			}
		}
		return fill;
	}

	/**
	 * @return iterator for the objects within the collection
	 */
	@Override
	public Iterator<MapObject> iterator() {
		return objects.iterator();
	}
	
}

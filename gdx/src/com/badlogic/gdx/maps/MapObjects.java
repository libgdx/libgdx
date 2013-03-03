package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

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
	public MapObject getObject(int index) {
		return objects.get(index);
	}
	
	/**
	 * @param name
	 * @return name matching object, null if it´s not in the set
	 */
	public MapObject getObject(String name) {
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
	public void addObject(MapObject object) {
		this.objects.add(object);
	}
	
	/**
	 * @param index removes MapObject instance at index
	 */
	public void removeObject(int index) {
		objects.removeIndex(index);
	}
	
	/**
	 * @param object instance to be removed
	 */
	public void removeObject(MapObject object) {
		objects.removeValue(object, true);
	}
	
	/**
	 * @return number of objects in the collection
	 */
	public int getNumObjects() {
		return objects.size;
	}

	/**
	 * @param type class of the objects we want to retrieve
	 * @return array filled with all the objects in the collection matching type
	 */
	public <T extends MapObject> Array<T> getObjectsByType(Class<T> type) {
		return getObjectsByType(type, new Array<T>());	
	}
	
	/**
	 * @param type class of the objects we want to retrieve
	 * @param fill collection to put the returned objects in
	 * @return array filled with all the objects in the collection matching type
	 */
	public <T extends MapObject> Array<T> getObjectsByType(Class<T> type, Array<T> fill) {
		fill.clear();
		for (MapObject object : objects) {
			if (type.isInstance(object)) {
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

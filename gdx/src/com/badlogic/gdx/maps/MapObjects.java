package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

public class MapObjects implements Iterable<MapObject> {

	private Array<MapObject> objects;

	public MapObjects() {
		objects = new Array<MapObject>();		
	}
	public MapObject getObject(int index) {
		return objects.get(index);
	}
	
	public MapObject getObject(String name) {
		for (MapObject object : objects) {
			if (name.equals(object.getName())) {
				return object;
			}
		}
		return null;
	}
	
	public void addObject(MapObject object) {
		this.objects.add(object);
	}
	
	public void removeObject(int index) {
		objects.removeIndex(index);
	}
	
	public void removeObject(MapObject object) {
		objects.removeValue(object, true);
	}
	
	public int getNumObjects() {
		return objects.size;
	}

	public <T extends MapObject> Array<T> getObjectsByType(Class<T> type) {
		return getObjectsByType(type, new Array<T>());	
	}
	
	public <T extends MapObject> Array<T> getObjectsByType(Class<T> type, Array<T> fill) {
		fill.clear();
		for (MapObject object : objects) {
			if (type.isInstance(object)) {
				fill.add((T) object);
			}
		}
		return fill;
	}

	@Override
	public Iterator<MapObject> iterator() {
		return objects.iterator();
	}
	
}

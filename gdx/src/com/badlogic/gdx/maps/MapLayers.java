package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

public class MapLayers implements Iterable<MapLayer> {
	
	private Array<MapLayer> layers = new Array<MapLayer>();

	public MapLayers() {
	
	}
	
	public MapLayer getLayer(int index) {
		return layers.get(index);
	}
	
	public MapLayer getLayer(String name) {
		for (MapLayer layer : layers) {
			if (name.equals(layer.getName())) {
				return layer;
			}
		}
		return null;
	}
	
	public void addLayer(MapLayer layer) {
		this.layers.add(layer);
	}
	
	public void removeLayer(int index) {
		layers.removeIndex(index);
	}
	
	public void removeLayer(MapLayer layer) {
		layers.removeValue(layer, true);
	}

	public <T extends MapLayer> Array<T> getLayersByType(Class<T> type) {
		return getLayersByType(type, new Array<T>());	
	}
	
	public <T extends MapLayer> Array<T> getLayersByType(Class<T> type, Array<T> fill) {
		fill.clear();
		for (MapLayer layer : layers) {
			if (type.isInstance(layer)) {
				fill.add((T) layer);
			}
		}
		return fill;
	}

	@Override
	public Iterator<MapLayer> iterator() {
		return layers.iterator();
	}
	
}

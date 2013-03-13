package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

/**
 * Ordered list of {@link MapLayer} instances owned by a {@link Map}
 */
public class MapLayers implements Iterable<MapLayer> {
	private Array<MapLayer> layers = new Array<MapLayer>();

	/**
	 * @param index
	 * @return layer at index
	 */
	public MapLayer getLayer(int index) {
		return layers.get(index);
	}
	
	/**
	 * @param name
	 * @return first layer matching the name, null otherwise
	 */
	public MapLayer getLayer(String name) {
		for (MapLayer layer : layers) {
			if (name.equals(layer.getName())) {
				return layer;
			}
		}
		return null;
	}
	
	/** @return number of layers in the collection */
	public int getCount() {
		return layers.size;
	}

	/**
	 * @param layer layer to be added to the set
	 */
	public void addLayer(MapLayer layer) {
		this.layers.add(layer);
	}
	
	/**
	 * @param index removes layer at index
	 */
	public void removeLayer(int index) {
		layers.removeIndex(index);
	}
	
	/**
	 * @param layer layer to be removed
	 */
	public void removeLayer(MapLayer layer) {
		layers.removeValue(layer, true);
	}

	/**
	 * @param type
	 * @return array with all the layers matching type
	 */
	public <T extends MapLayer> Array<T> getLayersByType(Class<T> type) {
		return getLayersByType(type, new Array<T>());	
	}
	
	/**
	 * 
	 * @param type
	 * @param fill array to be filled with the matching layers
	 * @return array with all the layers matching type
	 */
	public <T extends MapLayer> Array<T> getLayersByType(Class<T> type, Array<T> fill) {
		fill.clear();
		for (MapLayer layer : layers) {
			if (type.isInstance(layer)) {
				fill.add((T) layer);
			}
		}
		return fill;
	}

	/**
	 * @return iterator to set of layers
	 */
	@Override
	public Iterator<MapLayer> iterator() {
		return layers.iterator();
	}
	
}

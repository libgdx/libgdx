package com.badlogic.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

/**
 * @brief set of MapLayer instances
 */
public class MapLayers implements Iterable<MapLayer> {
	
	private Array<MapLayer> layers = new Array<MapLayer>();

	/**
	 * Creates empty set of layers
	 */
	public MapLayers() {
	
	}
	
	/**
	 * @param index
	 * @return layer at index
	 */
	public MapLayer getLayer(int index) {
		return layers.get(index);
	}
	
	/**
	 * @param name
	 * @return matching layer if exists, otherwise, null
	 */
	public MapLayer getLayer(String name) {
		for (MapLayer layer : layers) {
			if (name.equals(layer.getName())) {
				return layer;
			}
		}
		return null;
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
			if (type.equals(layer.getClass())) {
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

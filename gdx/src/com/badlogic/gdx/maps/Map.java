package com.badlogic.gdx.maps;

import com.badlogic.gdx.utils.Disposable;

public class Map implements Disposable {
	
	private MapLayers layers = new MapLayers();
	private MapProperties properties = new MapProperties();
	
	public MapLayers getLayers() {
		return layers;
	}

	public MapProperties getProperties() {
		return properties;
	}
	
	public Map() {
		
	}

	@Override
	public void dispose () {
	}
}

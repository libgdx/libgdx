package com.badlogic.gdx.maps;

public class Map {
	
	private MapLayers layers;
	
	private MapProperties properties;
	
	public MapLayers getLayers() {
		return layers;
	}

	public MapProperties getProperties() {
		return properties;
	}
	
	public Map() {
		layers = new MapLayers();
		properties = new MapProperties();
	}
	
}

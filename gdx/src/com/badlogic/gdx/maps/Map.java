package com.badlogic.gdx.maps;

public class Map {
	
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
	
}

package com.badlogic.gdx.maps;

public class MapLayer {

	private String name = "";
	private float opacity = 1.0f;
	private boolean visible = true;
	private MapObjects objects = new MapObjects();
	private MapProperties properties = new MapProperties();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}
	
	public MapObjects getObjects() {
		return objects;
	}
	
	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public MapProperties getProperties() {
		return properties;
	}
	
	public MapLayer() {
		
	}

}

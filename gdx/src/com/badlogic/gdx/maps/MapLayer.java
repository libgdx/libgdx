package com.badlogic.gdx.maps;

public class MapLayer {

	private String name;
	
	private float opacity;
	
	private boolean visible;
	
	private MapObjects objects;

	private MapProperties properties;

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
		name = null;
		opacity = 1.0f;
		visible = true;
		objects = new MapObjects();		
		properties = new MapProperties();
	}
	
}

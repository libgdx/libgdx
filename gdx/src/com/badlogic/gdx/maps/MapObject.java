package com.badlogic.gdx.maps;

public class MapObject {

	private String name;
	
	private float opacity;
	
	private boolean visible;
	
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
	
	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public MapProperties getProperties() {
		return properties;
	}
	
	public MapObject() {
		visible = true;
		properties = new MapProperties();
	}
	
}

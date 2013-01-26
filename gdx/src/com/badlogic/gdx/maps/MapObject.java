package com.badlogic.gdx.maps;

import com.badlogic.gdx.graphics.Color;

public class MapObject {

	private String name = "";
	private float opacity = 1.0f;
	private boolean visible = true;
	private MapProperties properties = new MapProperties();
	private Color color = Color.WHITE.cpy();
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
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
		
	}
	
}

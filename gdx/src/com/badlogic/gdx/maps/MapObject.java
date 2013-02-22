package com.badlogic.gdx.maps;

import com.badlogic.gdx.graphics.Color;

/**
 * Generic Map entity with basic attributes like name, opacity, color 
 */
public class MapObject {
	private String name = "";
	private float opacity = 1.0f;
	private boolean visible = true;
	private MapProperties properties = new MapProperties();
	private Color color = Color.WHITE.cpy();
	
	/**
	 * @return object's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name new name for the object
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return object's color
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * @param color new color for the object
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return object's opacity
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity new opacity value for the object
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}
	
	/**
	 * @return whether the object is visible or not
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * @param visible toggles object's visibility
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * @return object's properties set
	 */
	public MapProperties getProperties() {
		return properties;
	}
}

package com.badlogic.gdx.maps;

/**
 * @brief Map layer containing a set of objects and properties
 */
public class MapLayer {

	private String name = "";
	private float opacity = 1.0f;
	private boolean visible = true;
	private MapObjects objects = new MapObjects();
	private MapProperties properties = new MapProperties();

	/**
	 * @return layer's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name new name for the layer
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return layer's opacity
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity new opacity for the layer
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}
	
	/**
	 * @return collection of objects contained in the layer
	 */
	public MapObjects getObjects() {
		return objects;
	}
	
	/**
	 * @return whether the layer is visible or not
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * @param visible toggles layer's visibility
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return layer's set of properties
	 */
	public MapProperties getProperties() {
		return properties;
	}
	
	/**
	 * Creates empty layer
	 */
	public MapLayer() {
		
	}

}

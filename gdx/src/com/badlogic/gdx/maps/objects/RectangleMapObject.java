package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;

/**
 * @brief Represents rectangle shaped map object
 */
public class RectangleMapObject extends MapObject {
	
	private Rectangle rectangle;
	
	/**
	 * @return rectangle shape
	 */
	public Rectangle getRectangle() {
		return rectangle;
	}
	
	/**
	 * Creates a rectangle object which lower left corner is at (0, 0) with width=1 and height=1
	 */
	public RectangleMapObject() {
		this(0.0f, 0.0f, 1.0f, 1.0f);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public RectangleMapObject(float x, float y, float width, float height) {
		super();
		rectangle = new Rectangle(x, y, width, height);
	}
	
}

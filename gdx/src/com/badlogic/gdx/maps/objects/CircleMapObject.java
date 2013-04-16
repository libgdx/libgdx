package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Circle;

/**
 * @brief represents circle shaped map objects
 */
public class CircleMapObject extends MapObject {
	
	private Circle circle;
	
	/**
	 * @return circle shape
	 */
	public Circle getCircle() {
		return circle;
	}
	
	/**
	 * Creates a circle map object at (0,0) with r=1.0
	 */
	public CircleMapObject() {
		this(0.0f, 0.0f, 1.0f);
	}
	
	/**
	 * Creates circle map object
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 */
	public CircleMapObject(float x, float y, float radius) {
		super();
		circle = new Circle(x, y, radius);
	}
}

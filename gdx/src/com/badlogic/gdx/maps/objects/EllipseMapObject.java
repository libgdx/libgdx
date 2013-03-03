package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;

public class EllipseMapObject extends MapObject {

	private Ellipse ellipse;
	
	/**
	 * @return ellipse shape
	 */
	public Ellipse getEllipse() {
		return ellipse;
	}
	
	/**
	 * Creates an ellipse object which lower left corner is at (0, 0) with width=1 and height=1
	 */
	public EllipseMapObject() {
		this(0.0f, 0.0f, 1.0f, 1.0f);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public EllipseMapObject(float x, float y, float width, float height) {
		super();
		ellipse = new Ellipse(x, y, width, height);
	}
	
}

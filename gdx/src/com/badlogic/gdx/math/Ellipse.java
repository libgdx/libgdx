
package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

/** A convenient 2D ellipse class, based on the circle class
 * @author tonyp7 */
public class Ellipse implements Serializable {

	public float x, y;
	public float width, height;

	private static final long serialVersionUID = 7381533206532032099L;

	/** Construct a new ellipse with all values set to zero */
	public Ellipse () {

	}

	public Ellipse (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Ellipse (Vector2 position, float width, float height) {
		this.x = position.x;
		this.y = position.y;
		this.width = width;
		this.height = height;
	}

	public boolean contains (float x, float y) {
		x = x - this.x;
		y = y - this.y;

		return (x * x) / (width * 0.5f * width * 0.5f) + (y * y) / (height * 0.5f * height * 0.5f) <= 1.0f;
	}

	public boolean contains (Vector2 point) {
		return contains(point.x, point.y);
	}

	public void set (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void set (Ellipse ellipse) { 
		x = ellipse.x;
		y = ellipse.y;
		width = ellipse.width;
		height = ellipse.height;
	}
	
}

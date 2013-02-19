
package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

/** A convenient 2D ellipse class, based on the circle class
 * @author tonyp7 */
public class Ellipse implements Serializable {

	public float x, y;
	public float width, height;
	private float halfWidth, halfHeight;

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
		halfWidth = width * 0.5f;
		halfHeight = height * 0.5f;

		return (x * x) / (halfWidth * halfWidth) + (y * y) / (halfHeight * halfHeight) <= 1.0f;
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

}

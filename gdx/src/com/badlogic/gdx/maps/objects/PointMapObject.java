
package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;

/** @brief Represents a point map object */
public class PointMapObject extends MapObject {

	private float x;
	private float y;

	/** @return point X */
	public float getX () {
		return x;
	}

	/** @return point Y */
	public float getY () {
		return y;
	}

	/** Creates a point object at (0, 0) */
	public PointMapObject () {
		this(0f, 0f);
	}

	/** Creates a point object with the given X and Y coordinates.
	 *
	 * @param x the point x
	 * @param y the point y */
	public PointMapObject (float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}
}

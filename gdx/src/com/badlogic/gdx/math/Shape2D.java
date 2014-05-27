/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.math;

/** Main class for 2d shapes
 * @see com.badlogic.gdx.math.Circle
 * @see com.badlogic.gdx.math.Polygon
 * @see com.badlogic.gdx.math.RectangleBased
 * @author seroperson */
public abstract class Shape2D<T extends Shape<Vector2, T>> extends Shape<Vector2, T> {

	public float x, y;

	public Shape2D () {
		setPosition(0f, 0f);
	}

	public Shape2D (float x, float y) {
		setPosition(x, y);
	}

	public Shape2D (Vector2 position) {
		this(position.x, position.y);
	}

	/** Copy constructor
	 * @param shape The shape to construct a copy of. */
	public Shape2D (T shape) {
		set(shape);
	}

	/** Sets the x and y-coordinates of shape */
	public void setPosition (float x, float y) {
		setX(x);
		setY(y);
	}

	@Override
	public void setPosition (Vector2 position) {
		setPosition(position.x, position.y);
	}

	public void setX (float x) {
		this.x = x;
	}

	public void setY (float y) {
		this.y = y;
	}

	public void translate (float x, float y) {
		setPosition(this.x + x, this.y + y);
	}

	@Override
	public void translate (Vector2 vector) {
		translate(vector.x, vector.y);
	}

	/** Checks whether or not this shape contains a given point.
	 * @return true if this shape contains the given point. */
	public abstract boolean contains (float x, float y);

	@Override
	public boolean contains (Vector2 point) {
		return contains(point.x, point.y);
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	@Override
	public Vector2 getPosition (Vector2 vector) {
		return vector.set(x, y);
	}

	@Override
	public String toString () {
		return x + ", " + y;
	}

}

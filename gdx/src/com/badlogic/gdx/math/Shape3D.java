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

public abstract class Shape3D<T extends Shape<Vector3, T>> implements Shape<Vector3, T> {

	public float x, y, z;

	public Shape3D () {
		setPosition(0f, 0f, 0f);
	}

	public Shape3D (float x, float y, float z) {
		setPosition(x, y, z);
	}

	public Shape3D (Vector3 position) {
		this(position.x, position.y, position.z);
	}

	/** Copy constructor
	 * @param shape The shape to construct a copy of. */
	public Shape3D (T shape) {
		set(shape);
	}

	/** Sets the x, y and z-coordinates of shape */
	public void setPosition (float x, float y, float z) {
		setX(x);
		setY(y);
		setZ(z);
	}

	@Override
	public void setPosition (Vector3 position) {
		setPosition(position.x, position.y, position.z);
	}

	public void setX (float x) {
		this.x = x;
	}

	public void setY (float y) {
		this.y = y;
	}

	public void setZ (float z) {
		this.z = z;
	}

	public void translate (float x, float y, float z) {
		setPosition(this.x + x, this.y + y, this.z + z);
	}

	@Override
	public void translate (Vector3 vector) {
		translate(vector.x, vector.y, vector.z);
	}

	/** Checks whether or not this shape contains a given point.
	 * @return true if this shape contains the given point. */
	public abstract boolean contains (float x, float y, float z);

	@Override
	public boolean contains (Vector3 point) {
		return contains(point.x, point.y, point.z);
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	public float getZ () {
		return z;
	}

	@Override
	public Vector3 getPosition (Vector3 vector) {
		return vector.set(x, y, z);
	}

	@Override
	public String toString () {
		return x + ", " + y + ", " + z;
	}

}

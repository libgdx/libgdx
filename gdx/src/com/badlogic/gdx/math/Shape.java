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

import java.io.Serializable;

/** Main class for all shapes
 * @see com.badlogic.gdx.math.Shape2D
 * @see com.badlogic.gdx.math.Shape3D
 * @author seroperson */
public abstract class Shape<V extends Vector<V>, T extends Shape<V, T>> implements Serializable {

	/** Sets the fields of shape from other shape */
	public abstract void set (T shape);

	/** Sets the coordinates of shape from vector
	 * @param position The position vector */
	public abstract void setPosition (V position);

	/** Adds to vector to current position
	 * @param vector vector to add */
	public abstract void translate (V vector);

	/** @return a copy of this shape */
	public abstract T cpy ();

	/** Checks whether or not this shape contains a given point.
	 * @param point The {@link Vector} that contains the point coordinates.
	 * @return true if this shape contains this point; false otherwise. */
	public abstract boolean contains (V point);

	/** return the Vector with coordinates of this rectangle
	 * @param position The Vector */
	public abstract V getPosition (V position);

}

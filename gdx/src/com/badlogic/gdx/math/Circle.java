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

/** A convenient 2D circle class.
 * @author mzechner */
public class Circle extends Shape<Circle> {

    public float radius;

	/** Constructs a new circle with all values set to zero */
	public Circle () {

	}

	/** Constructs a new circle with the given X and Y coordinates and the given radius.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param radius The radius of the circle */
	public Circle (float x, float y, float radius) {
        set(x, y, radius);
	}

	/** Constructs a new circle using a given {@link Vector2} that contains the desired X and Y coordinates, and a given radius.
	 * 
	 * @param position The position {@link Vector2}.
	 * @param radius The radius */
	public Circle (Vector2 position, float radius) {
        this(position.x, position.y, radius);
	}

	/** Copy constructor
	 * 
	 * @param circle The circle to construct a copy of. */
	public Circle (Circle circle) {
        this(circle.x, circle.y, circle.radius);
	}

	/** Sets a new location and radius for this circle.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param radius Circle radius */
	public void set (float x, float y, float radius) {
		setPosition(x, y);
		setRadius(radius);
	}

	/** Sets a new location and radius for this circle.
	 * 
	 * @param position Position {@link Vector2} for this circle.
	 * @param radius Circle radius */
	public void set (Vector2 position, float radius) {
		set(position.x, position.y, radius);
	}

	/** Sets a new location and radius for this circle, based upon another circle.
	 * 
	 * @param circle The circle to copy the position and radius of. */
    @Override
 	public void set (Circle circle) {
		set(circle.x, circle.y, circle.radius);
	}

	/** Sets the radius of circle
	 * @param radius The radius */
	public void setRadius (float radius) {
		this.radius = radius;
	}

    @Override
 	public boolean contains (float x, float y) {
		x = this.x - x;
		y = this.y - y;
		return x * x + y * y <= radius * radius;
	}

	/** @param c the other {@link Circle}
	 * @return whether this circle contains the other circle. */
    @Override
 	public boolean contains (Circle c) {
		float dx = x - c.x;
		float dy = y - c.y;
		// The distance to the furthest point on circle c is the distance
		// between the center of the two circles plus the radius.
		// We use the squared distance so we can avoid a sqrt.
		float maxDistanceSqrd = dx * dx + dy * dy + c.radius * c.radius;
		return maxDistanceSqrd <= radius * radius;
	}

	/** @param c the other {@link Circle}
	 * @return whether this circle overlaps the other circle. */
    @Override
 	public boolean overlaps (Circle c) {
		float dx = x - c.x;
		float dy = y - c.y;
		float distance = dx * dx + dy * dy;
		float radiusSum = radius + c.radius;
		return distance < radiusSum * radiusSum;
	}

    @Override
	public String toString () {
		return super.toString() + ", " + radius;
	}

}

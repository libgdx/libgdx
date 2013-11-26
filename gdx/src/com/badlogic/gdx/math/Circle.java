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

/** A convenient 2D circle class.
 * @author mzechner */
public class Circle implements Serializable {
	public float x, y;
	public float radius;

	/** Constructs a new circle with all values set to zero */
	public Circle () {

	}

	public Circle (float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public Circle (Vector2 position, float radius) {
		this.x = position.x;
		this.y = position.y;
		this.radius = radius;
	}

	public Circle (Circle circle) {
		this.x = circle.x;
		this.y = circle.y;
		this.radius = circle.radius;
	}

	public void set (float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
    
	public void set (Vector2 position, float radius) {
		this.x = position.x;
		this.y = position.y;
		this.radius = radius;
	}

	public void set (Circle circle) { 
		x = circle.x;
		y = circle.y;
		radius = circle.radius;
	}

	/** Sets the x and y-coordinates of circle center from vector
	 * @param position The position vector */
	public void setPosition (Vector2 position) {
		this.x = position.x;
		this.y = position.y;
	}

	/** Sets the x and y-coordinates of circle center
	 * @param x The x-coordinate
	 * @param y The y-coordinate */
	public void setPosition (float x, float y) {
		this.x = x;
		this.y = y;
	}

	/** Sets the x-coordinate of circle center
	 * @param x The x-coordinate */
	public void setX (float x) {
		this.x = x;
	}

	/** Sets the y-coordinate of circle center
	 * @param y The y-coordinate */
	public void setY (float y) {
		this.y = y;
	}

	/** Sets the radius of circle
	 * @param radius The radius */
	public void setRadius (float radius) {
		this.radius = radius;
	}

	public boolean contains (float x, float y) {
		x = this.x - x;
		y = this.y - y;
		return x * x + y * y <= radius * radius;
	}

	public boolean contains (Vector2 point) {
		float dx = x - point.x;
		float dy = y - point.y;
		return dx * dx + dy * dy <= radius * radius;
	}

	/** @param c the other {@link Circle}
	 * @return whether this circle contains the other circle. */
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
	public boolean overlaps (Circle c) {
		float dx = x - c.x;
		float dy = y - c.y;
		float distance = dx * dx + dy * dy;
		float radiusSum = radius + c.radius;
		return distance < radiusSum * radiusSum;
	}

	public String toString () {
		return x + "," + y + "," + radius;
	}
}

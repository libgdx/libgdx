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
 * @see com.badlogic.gdx.math.Circle
 * @see com.badlogic.gdx.math.Ellipse
 * @see com.badlogic.gdx.math.Polygon
 * @see com.badlogic.gdx.math.Rectangle
 * */
public abstract class Shape<T extends Shape<T>> implements Serializable {

    public float x, y;

    public Shape() {
        setPosition(0f, 0f);
    }

    public Shape(float x, float y) {
        setPosition(x, y);
    }

    public Shape(Vector2 position) {
        this(position.x, position.y);
    }

    public abstract void set(T shape);

    /** Sets the x and y-coordinates of shape
	 * @param x The x-coordinate
	 * @param y The y-coordinate */
    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    /** Sets the x and y-coordinates of shape from vector
	 * @param position The position vector */
    public void setPosition(Vector2 position) {
        setPosition(position.x, position.y);
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void translate(float x, float y) {
        setPosition(this.x + x, this.y + y);
    }

    public abstract boolean overlaps(T shape);

    public abstract boolean contains(T shape);

    /** Checks whether or not this shape contains a given point.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 *
	 * @return true if this circle contains the given point. */
    public abstract boolean contains(float x, float y);

    /** Checks whether or not this shape contains a given point.
	 *
	 * @param point The {@link Vector2} that contains the point coordinates.
	 *
	 * @return true if this circle contains this point; false otherwise. */
    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString () {
		return x + ", " + y;
	}

}

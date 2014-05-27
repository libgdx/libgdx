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

/** Main class for 2d shapes that have width and height
 * @see com.badlogic.gdx.math.Rectangle
 * @see com.badlogic.gdx.math.Ellipse
 * @author seroperson */
public abstract class RectangleBased<T extends RectangleBased<T>> extends Shape2D<T> {

	public float width, height;

	public RectangleBased () {
		this(0f, 0f, 0f, 0f);
	}

	public RectangleBased (float x, float y, float width, float height) {
		set(x, y, width, height);
	}

	public RectangleBased (Vector2 position, float width, float height) {
		this(position.x, position.y, width, height);
	}

    public RectangleBased(T shape) {
        super(shape);
    }

	public void setHeight (float height) {
		this.height = height;
	}

	public void setWidth (float width) {
		this.width = width;
	}

	/** Sets the width and height */
	public void setSize (float width, float height) {
		setWidth(width);
		setHeight(height);
	}

	/** Sets the squared size of this shape
	 * @param sizeXY The size */
	public void setSize (float sizeXY) {
		setSize(sizeXY, sizeXY);
	}

	/** Sets a new position and size for this shape */
	public void set (float x, float y, float width, float height) {
		setPosition(x, y);
		setSize(width, height);
	}

	public float getHeight () {
		return height;
	}

	public float getWidth () {
		return width;
	}

	/** @return the Vector2 with size of this shape
	 * @param size The Vector2 */
	public Vector2 getSize (Vector2 size) {
		return size.set(width, height);
	}

	public String toString () {
		return super.toString() + ", " + width + ", " + height;
	}

}

/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.general;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import static com.badlydrawngames.general.Rectangles.*;

public class CollisionGeometry {
	private final Array<Rectangle> runs;
	private final Rectangle r;

	/** Creates this collision geometry.
	 * @param runs the model-space rectangles that make up this object's collision geometry. */
	public CollisionGeometry (Array<Rectangle> runs) {
		this.runs = runs;
		r = new Rectangle();
	}

	/** Tests if this collision geometry would collide with a rectangle if it was at the given coordinates.
	 * @param other the rectangle to test against.
	 * @param x the x coordinate of this collision geometry.
	 * @param y the y coordinate of this collision geometry.
	 * @return true if in collision otherwise false. */
	public boolean intersects (Rectangle other, float x, float y) {
		boolean result = false;
		for (int i = 0; i < runs.size; i++) {
			Rectangle run = runs.get(i);
			setRectangle(r, x + run.x, y + run.y, run.width, run.height);
			if (Colliders.intersects(r, other)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/** Tests if this collision geometry is in collision with another.
	 * @param x this collision geometry's x coordinate.
	 * @param y this collision geometry's y coordinate.
	 * @param other the other collision geometry.
	 * @param otherX the other collision geometry's x coordinate.
	 * @param otherY the other collision geometry's y coordinate.
	 * @return true if in collision otherwise false. */
	public boolean intersects (float x, float y, CollisionGeometry other, float otherX, float otherY) {
		boolean result = false;
		for (int i = 0; i < runs.size; i++) {
			Rectangle run = runs.get(i);
			setRectangle(r, x + run.x, y + run.y, run.width, run.height);
			if (other.intersects(r, otherX, otherY)) {
				result = true;
				break;
			}
		}
		return result;
	}
}

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

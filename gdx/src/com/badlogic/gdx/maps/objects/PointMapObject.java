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
import com.badlogic.gdx.math.Vector2;

/** @brief Represents 2D points on the map */
public class PointMapObject extends MapObject {

	private final Vector2 point;

	/** creates a 2D point map object at (0, 0) */
	public PointMapObject () {
		this(0, 0);
	}

	/** Creates a 2D point map object
	 *
	 * @param x X coordinate
	 * @param y Y coordinate */
	public PointMapObject (float x, float y) {
		point = new Vector2(x, y);
	}

	/** @return 2D point on the map as {@link Vector2} */
	public Vector2 getPoint () {
		return point;
	}

}

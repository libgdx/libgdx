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
import com.badlogic.gdx.math.Rectangle;

/** @brief Represents a rectangle shaped map object */
public class RectangleMapObject extends MapObject {

	private Rectangle rectangle;

	/** @return rectangle shape */
	public Rectangle getRectangle () {
		return rectangle;
	}

	/** Creates a rectangle object which lower left corner is at (0, 0) with width=1 and height=1 */
	public RectangleMapObject () {
		this(0.0f, 0.0f, 1.0f, 1.0f);
	}

	/** Creates a {@link Rectangle} object with the given X and Y coordinates along with a given width and height.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param width Width of the {@link Rectangle} to be created.
	 * @param height Height of the {@link Rectangle} to be created. */
	public RectangleMapObject (float x, float y, float width, float height) {
		super();
		rectangle = new Rectangle(x, y, width, height);
	}

}

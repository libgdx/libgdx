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

package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;

/** A point in a 2D grid, with integer x and y coordinates
 * 
 * @author badlogic */
public class GridPoint2 {
	public int x;
	public int y;

	/** Constructs a new 2D grid point. */
	public GridPoint2 () {
	}

	/** Constructs a new 2D grid point.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate */
	public GridPoint2 (int x, int y) {
		this.x = x;
		this.y = y;
	}

	/** Copy constructor
	 * 
	 * @param point The 2D grid point to make a copy of. */
	public GridPoint2 (GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
	}

	/** Sets the coordinates of this 2D grid point to that of another.
	 * 
	 * @param point The 2D grid point to copy the coordinates of.
	 * 
	 * @return this 2D grid point for chaining. */
	public GridPoint2 set (GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
		return this;
	}

	/** Sets the coordinates of this 2D grid point.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * 
	 * @return this 2D grid point for chaining. */
	public GridPoint2 set (int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || o.getClass() != this.getClass()) return false;
		GridPoint2 g = (GridPoint2)o;
		return this.x == g.x && this.y == g.y;
	}

	@Override
	public int hashCode () {
		final int prime = 53;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		return result;
	}
}

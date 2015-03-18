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

/** A point in a 3D grid, with integer x and y coordinates
 * 
 * @author badlogic */
public class GridPoint3 {
	public int x;
	public int y;
	public int z;

	/** Constructs a 3D grid point with all coordinates pointing to the origin (0, 0, 0). */
	public GridPoint3 () {
	}

	/** Constructs a 3D grid point.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate */
	public GridPoint3 (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/** Copy constructor
	 * 
	 * @param point The 3D grid point to make a copy of. */
	public GridPoint3 (GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}

	/** Sets the coordinates of this 3D grid point to that of another.
	 * 
	 * @param point The 3D grid point to copy coordinates of.
	 * 
	 * @return this GridPoint3 for chaining. */
	public GridPoint3 set (GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
		return this;
	}

	/** Sets the coordinates of this GridPoint3D.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * 
	 * @return this GridPoint3D for chaining. */
	public GridPoint3 set (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || o.getClass() != this.getClass()) return false;
		GridPoint3 g = (GridPoint3)o;
		return this.x == g.x && this.y == g.y && this.z == g.z;
	}

	@Override
	public int hashCode () {
		final int prime = 17;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		result = prime * result + this.z;
		return result;
	}
}

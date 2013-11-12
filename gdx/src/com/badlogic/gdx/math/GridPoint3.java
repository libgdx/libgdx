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

/**
 * A point in a 3D grid, with integer x and y coordinates
 * @author badlogic
 *
 */
public class GridPoint3 {
	public int x;
	public int y;
	public int z;
	
	public GridPoint3() {
	}
	
	public GridPoint3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public GridPoint3(GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}
	
	public GridPoint3 set(GridPoint3 point) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
		return this;
	}
	
	public GridPoint3 set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
}
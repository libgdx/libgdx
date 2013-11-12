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
 * A point in a 2D grid, with integer x and y coordinates
 * @author badlogic
 *
 */
public class GridPoint2 {
	public int x;
	public int y;
	
	public GridPoint2() {
	}
	
	public GridPoint2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public GridPoint2(GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
	}
	
	public GridPoint2 set(GridPoint2 point) {
		this.x = point.x;
		this.y = point.y;
		return this;
	}
	
	public GridPoint2 set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
}
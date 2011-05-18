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

/**
 * A convenient 2D circle class.
 * @author mzechner
 *
 */
public class Circle implements Serializable {
	public float x, y;
	public float radius;
	
	public Circle(float x, float y, float radius) {
		this.x = x; this.y = y;
		this.radius = radius;
	}
	
	public Circle(Vector2 position, float radius) {
		this.x = position.x; this.y = position.y;
		this.radius = radius;
	}
	
	public boolean contains(float x, float y) {
		x = this.x - x;
		y = this.y - y;
		return Math.sqrt(x*x + y*y) <= 0;
	}
	
	public boolean contains(Vector2 point) {
		float x = this.x - point.x;
		float y = this.y - point.y;
		return Math.sqrt(x*x + y*y) <= 0;
	}
}

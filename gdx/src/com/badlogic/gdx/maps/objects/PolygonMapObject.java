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

 /**
 * 
 */
package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polygon;

/**
 * @brief represents polygon map objects
 */
public class PolygonMapObject extends MapObject {

	private Polygon polygon;
	
	/**
	 * @return polygon shape
	 */
	public Polygon getPolygon() {
		return polygon;
	}
	
	/**
	 * @param polygon new object's polygon shape
	 */
	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	/**
	 * Creates empty polygon map object
	 */
	public PolygonMapObject() {
		this(new float[0]);
	}
	
	/**
	 * @param vertices polygon defining vertices (at least 3)
	 */
	public PolygonMapObject(float[] vertices) {
		polygon = new Polygon(vertices);
	}
	
	/**
	 * @param polygon the polygon
	 */
	public PolygonMapObject(Polygon polygon) {
		this.polygon = polygon;
	}
	
}

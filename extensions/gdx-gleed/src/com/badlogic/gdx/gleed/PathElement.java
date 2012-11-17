/*******************************************************************************
 * Copyright 2012 David Saltares
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

package com.badlogic.gdx.gleed;

import com.badlogic.gdx.math.Polygon;

/**
 * @author David Saltares
 * @date 02/11/2012
 * 
 * @brief Path GLEED2D shape implementation 
 */
public class PathElement extends LevelObject {
	Polygon polygon;
	int lineWidth;
	
	PathElement() {
		super();
	}
	
	/**
	 * @return polygon shape formed by the path (it assumes it's closed)
	 */
	public Polygon getPolygon() {
		return polygon;
	}
	
	/**
	 * @return line width
	 */
	public int getLineWidth() {
		return lineWidth;
	}
}

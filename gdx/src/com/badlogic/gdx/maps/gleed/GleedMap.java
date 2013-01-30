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

package com.badlogic.gdx.maps.gleed;

import com.badlogic.gdx.maps.Map;

/**
 * @author David Saltares
 * 
 * @brief Models all the data of a GLEED generated level
 * 
 * It should be created using the GLEEDMapLoader
 */
public class GleedMap extends Map {
	
	private String m_name;
	
	/**
	 * Creates empty level
	 */
	public GleedMap() {
		this("");
	}
	
	/**
	 * Creates empty level
	 * 
	 * @param name name of the level
	 */
	public GleedMap(String name) {
		super();
		m_name = name;
	}
	
	/**
	 * @return name of the level
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * @param name new name for the level
	 */
	public void setName(String name) {
		m_name = name;
	}
}

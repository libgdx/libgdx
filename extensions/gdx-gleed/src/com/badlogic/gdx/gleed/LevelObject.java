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

import com.badlogic.gdx.graphics.Color;

/**
 * @author David Saltares
 * @date 02/11/2012
 * 
 * @brief Models a generic object in a GLEED2D level
 */
public class LevelObject {
	String name = "";
	boolean visible = true;
	Color color = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	Properties properties = new Properties();

	LevelObject() {}
	
	/**
	 * @return custom properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * @return name of the object
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return whether the object is visible or not
	 */
	public boolean getVisible() {
		return visible;
	}
	
	/**
	 * @return color tint of the object
	 */
	public Color getColor() {
		return color;
	}
}

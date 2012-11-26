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

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * @author David Saltares
 * @date 02/11/2012
 * 
 * @brief Holds read only GLEED2D level element properties
 *
 */
public class Properties {
	HashMap<String, String> strings = new HashMap<String, String> ();
	HashMap<String, Color> colors = new HashMap<String, Color>();
	HashMap<String, Boolean> booleans = new HashMap<String, Boolean>();
	HashMap<String, Vector2> vectors = new HashMap<String, Vector2>();
	
	Properties() {}
	
	/**
	 * @param key name of the string
	 * @return string related to the given key, null if not found
	 */
	public String getString(String key) {
		return getString(key, "");
	}
	
	/**
	 * @param key name of the string
	 * @param defaultValue return value if key doesn't exist
	 * @return string related to the given key, null if not found
	 */
	public String getString(String key, String defaultValue) {
		String value = strings.get(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		return value;
	}
	
	/**
	 * @param key name of the boolean
	 * @return boolean related to the given key, null if not found
	 */
	public Boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	/**
	 * @param key name of the boolean
	 * @param defaultValue return value if key doesn't exist
	 * @return boolean related to the given key, null if not found
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		Boolean value = booleans.get(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		return value;
	}
	
	/**
	 * @param key name of the 2D vector
	 * @return 2D vector related to the given key, null if not found
	 */
	public Vector2 getVector(String key) {
		return getVector(key, Vector2.Zero);
	}
	
	/**
	 * @param key name of the 2D vector
	 * @param defaultValue return value if key doesn't exist
	 * @return 2D vector related to the given key, null if not found
	 */
	public Vector2 getVector(String key, Vector2 defaultValue) {
		Vector2 value = vectors.get(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		return value;
	}
	
	/**
	 * @param key name of the color element
	 * @return color related to the given key, null if not found
	 */
	public Color getColor(String key) {
		return getColor(key, Color.BLACK);
	}
	
	/**
	 * @param key name of the color element
	 * @param defaultValue return value if key doesn't exist
	 * @return color related to the given key, null if not found
	 */
	public Color getColor(String key, Color defaultValue) {
		Color value = colors.get(key);
		
		if (value == null) {
			return defaultValue;
		}
		
		return value;
	}
	
	void load(Element element) {
		Element customProperty = element.getChildByName("CustomProperties");
		 
		if (customProperty != null) {
			Array<Element> properties = customProperty.getChildrenByName("Property");
			
			for (int i = 0; i < properties.size; ++i) {
				Element property = properties.get(i);
				String type = property.getAttribute("Type");
				
				if (type == null) {
					continue;
				}
				
				if (type.equals("string")) {
					strings.put(property.getAttribute("Name"), property.getChildByName("string").getText());
				}
				else if (type.equals("bool")) {
					booleans.put(property.getAttribute("Name"), Boolean.parseBoolean(property.getChildByName("boolean").getText()));
				}
				else if (type.equals("Vector2")) {
					Element vectorElement = property.getChildByName("Vector2");
					Vector2 v = new Vector2(Float.parseFloat(vectorElement.getChildByName("X").getText()),
											Float.parseFloat(vectorElement.getChildByName("Y").getText()));
					
					vectors.put(property.getAttribute("Name"), v);
				}
				else if (type.equals("Color")) {
					Element colorElement = property.getChildByName("Color");
					Color c = new Color(Float.parseFloat(colorElement.getChildByName("R").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("G").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("B").getText()) / 255.0f,
										Float.parseFloat(colorElement.getChildByName("A").getText()) / 255.0f);
					
					colors.put(property.getAttribute("Name"), c);
				}
			}
		}
	}
}

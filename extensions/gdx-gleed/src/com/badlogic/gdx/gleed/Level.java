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

import com.badlogic.gdx.utils.Array;

/**
 * @author David Saltares
 * @date 02/11/2012
 * 
 * @brief GLEED2D level information
 */
public class Level {
	Properties properties = new Properties();
	Array<Layer> layers = new Array<Layer>();
	String file = "";
	
	Level(String file) {
		this.file = file;
	}
	
	/**
	 * @return level custom properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * @return number of layers in the level
	 */
	public int getNumLayers() {
		return layers.size;
	}
	
	/**
	 * @return all layers
	 */
	public Array<Layer> getLayers() {
		return layers;
	}
	
	/**
	 * @param index index of the desired layer
	 * @return correspondent layer, null if index is invalid
	 */
	public Layer getLayer(int index) {
		if (index < 0 || index >= layers.size) {
			return null;
		}
		
		return layers.get(index);
	}
	
	/**
	 * @param name name of the desired layer
	 * @return correspondent layer, null if the layer doesn't exist
	 */
	public Layer getLayer(String name) {
		for (int i = 0; i < layers.size; ++i) {
			Layer layer = layers.get(i);
			
			if (layer.getProperties().getString("Name", "").equals(name)) {
				return layer;
			}
		}
		
		return null;
	}
}

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
 * @brief Represents a layer of a GLEED2D level
 */
public class Layer extends LevelObject {
	
	Array<LevelObject> objects = new Array<LevelObject>();
	Array<TextureElement> textures = new Array<TextureElement>();
	
	Layer() {}
	
	/**
	 * @return texture objects in the layer
	 */
	public Array<TextureElement> getTextures() {
		return textures;
	}
	
	/**
	 * @param name name of the desired texture element
	 * @return texture element if found, null if it doesn't exist
	 */
	public TextureElement getTexture(String name) {
		for (int i = 0; i < textures.size; ++i) {
			TextureElement texture = textures.get(i);
			
			if (texture.name.equals(name)) {
				return texture;
			}
		}
		
		return null;
	}
	
	/**
	 * @return all the level objects in the layer (shapes)
	 */
	public Array<LevelObject> getObjects() {
		return objects;
	}
	
	/**
	 * @param name name of the desired level object
	 * @return level object if found, null if it doesn't exist
	 */
	public LevelObject getObject(String name) {
		for (int i = 0; i < objects.size; ++i) {
			LevelObject levelObject = objects.get(i);
			
			if (levelObject.name.equals(name)) {
				return levelObject;
			}
		}
		
		return null;
	}
}

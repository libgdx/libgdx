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

package com.badlogic.gdx.maps;

/** Map layer containing a set of objects and properties */
public class MapLayer {
	private String name = "";
	private float opacity = 1.0f;
	private boolean visible = true;
	private MapObjects objects = new MapObjects();
	private MapProperties properties = new MapProperties();

	/** @return layer's name */
	public String getName () {
		return name;
	}

	/** @param name new name for the layer */
	public void setName (String name) {
		this.name = name;
	}

	/** @return layer's opacity */
	public float getOpacity () {
		return opacity;
	}

	/** @param opacity new opacity for the layer */
	public void setOpacity (float opacity) {
		this.opacity = opacity;
	}

	/** @return collection of objects contained in the layer */
	public MapObjects getObjects () {
		return objects;
	}

	/** @return whether the layer is visible or not */
	public boolean isVisible () {
		return visible;
	}

	/** @param visible toggles layer's visibility */
	public void setVisible (boolean visible) {
		this.visible = visible;
	}

	/** @return layer's set of properties */
	public MapProperties getProperties () {
		return properties;
	}
}

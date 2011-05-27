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
package com.badlogic.gdx.graphics.g2d.tiled;

import java.util.HashMap;

/**
 * Contains Tiled map layer information
 * @author David Fraska
 */
public class TiledLayer {
	public String name;

	/** Contains the layer properties with a key of the property name. */
	public HashMap<String, String> properties = new HashMap<String, String>(0);

	//public final int width, height;

	/** Contains the tile ids, addressed as [row][column]. */
	public int[][] tiles;

	/**
	 * Constructs a new TiledLayer, used by {@link TiledLoader}
	 * */
	TiledLayer () {
	}
	
	public int getWidth(){
		if(tiles[0] == null) return 0;
		return tiles[0].length;
	}
	
	public int getHeight(){
		return tiles.length;
	}
}

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

package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;

/**
 * @brief Generalises the concept of tile in a TiledMap
 *
 */
public interface TiledMapTile {

	public enum BlendMode {
		NONE,
		ALPHA
	}

	public int getId();
	public void setId(int id);
	
	/**
	 * @return the {@link BlendMode} to use for rendering the tile
	 */	
	public BlendMode getBlendMode();
	
	/**
	 * Sets the {@link BlendMode} to use for rendering the tile
	 * 
	 * @param blendMode the blend mode to use for rendering the tile
	 */
	public void setBlendMode(BlendMode blendMode);
	
	/**
	 * @return texture region used to render the tile
	 */
	public TextureRegion getTextureRegion();

	/**
	 * @return tile's properties set
	 */
	public MapProperties getProperties();
	
}
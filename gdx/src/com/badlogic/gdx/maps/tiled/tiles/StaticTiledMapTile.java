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

package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;

/**
 * @brief Represents a non changing TiledMapTile (can be cached)
 */
public class StaticTiledMapTile implements TiledMapTile {

	private int id;
	
	private BlendMode blendMode = BlendMode.ALPHA;
	
	private MapProperties properties;
	
	private TextureRegion textureRegion;	

	@Override
	public int getId () {
		return id;
	}

	@Override
	public void setId (int id) {
		this.id = id;
	}
	
	@Override
	public BlendMode getBlendMode () {
		return blendMode;
	}

	@Override
	public void setBlendMode (BlendMode blendMode) {
		this.blendMode = blendMode;
	}	
	
	/**
	 * @return tile's properties set
	 */
	@Override
	public MapProperties getProperties() {
		if (properties == null) {
			properties = new MapProperties();
		}
		return properties;
	}

	/**
	 * @return texture region used to render the tile
	 */
	@Override
	public TextureRegion getTextureRegion() {
		return textureRegion;
	}
	
	/**
	 * Creates a static tile with the given region
	 * 
	 * @param textureRegion
	 */
	public StaticTiledMapTile(TextureRegion textureRegion) {
		this.textureRegion = textureRegion;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param copy
	 */
	public StaticTiledMapTile(StaticTiledMapTile copy) {
		if (copy.properties != null) {
			getProperties().putAll(copy.properties);	
		}
		this.textureRegion = copy.textureRegion;
		this.id = copy.id;
	}
	
}
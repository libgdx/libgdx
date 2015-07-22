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

package com.badlogic.gdx.maps.tiled.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

/** A {@link MapObject} with a {@link TiledMapTile}. Can be both {@link StaticTiledMapTile} or {@link AnimatedTiledMapTile}. For
 * compatibility reasons, this extends {@link TextureMapObject}. Use {@link TiledMapTile#getTextureRegion()} instead of
 * {@link #getTextureRegion()}.
 * @author Daniel Holderbaum */
public class TiledMapTileMapObject extends TextureMapObject {

	private boolean flipHorizontally;
	private boolean flipVertically;

	private TiledMapTile tile;

	public TiledMapTileMapObject (TiledMapTile tile, boolean flipHorizontally, boolean flipVertically) {
		this.flipHorizontally = flipHorizontally;
		this.flipVertically = flipVertically;
		this.tile = tile;

		TextureRegion textureRegion = new TextureRegion(tile.getTextureRegion());
		textureRegion.flip(flipHorizontally, flipVertically);
		setTextureRegion(textureRegion);
	}

	public boolean isFlipHorizontally () {
		return flipHorizontally;
	}

	public void setFlipHorizontally (boolean flipHorizontally) {
		this.flipHorizontally = flipHorizontally;
	}

	public boolean isFlipVertically () {
		return flipVertically;
	}

	public void setFlipVertically (boolean flipVertically) {
		this.flipVertically = flipVertically;
	}

	public TiledMapTile getTile () {
		return tile;
	}

	public void setTile (TiledMapTile tile) {
		this.tile = tile;
	}

}

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;

/** @brief Represents a changing {@link TiledMapTile}. */
public class AnimatedTiledMapTile implements TiledMapTile {

	private static long lastTiledMapRenderTime = 0;

	private int id;

	private BlendMode blendMode = BlendMode.ALPHA;

	private MapProperties properties;

	private Array<StaticTiledMapTile> frameTiles;

	private float animationInterval;
	private long frameCount = 0;
	private static final long initialTimeOffset = TimeUtils.millis();

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

	@Override
	public TextureRegion getTextureRegion () {
		long currentFrame = (lastTiledMapRenderTime / (long)(animationInterval * 1000f)) % frameCount;
		return frameTiles.get((int)currentFrame).getTextureRegion();
	}

	@Override
	public float getOffsetX () {
		long currentFrame = (lastTiledMapRenderTime / (long)(animationInterval * 1000f)) % frameCount;
		return frameTiles.get((int)currentFrame).getOffsetX();
	}

	@Override
	public void setOffsetX (float offsetX) {
		throw new GdxRuntimeException("Cannot set offset of AnimatedTiledMapTile.");
	}

	@Override
	public float getOffsetY () {
		long currentFrame = (lastTiledMapRenderTime / (long)(animationInterval * 1000f)) % frameCount;
		return frameTiles.get((int)currentFrame).getOffsetY();
	}

	@Override
	public void setOffsetY (float offsetY) {
		throw new GdxRuntimeException("Cannot set offset of AnimatedTiledMapTile.");
	}

	@Override
	public MapProperties getProperties () {
		if (properties == null) {
			properties = new MapProperties();
		}
		return properties;
	}

	/** Function is called by BatchTiledMapRenderer render(), lastTiledMapRenderTime is used to keep all of the tiles in lock-step
	 * animation and avoids having to call TimeUtils.millis() in getTextureRegion() */
	public static void updateAnimationBaseTime () {
		lastTiledMapRenderTime = TimeUtils.millis() - initialTimeOffset;
	}

	/** Creates an animated tile with the given animation interval and frame tiles.
	 * 
	 * @param interval The interval between each individual frame tile.
	 * @param frameTiles An array of {@link StaticTiledMapTile}s that make up the animation. */
	public AnimatedTiledMapTile (float interval, Array<StaticTiledMapTile> frameTiles) {
		this.frameTiles = frameTiles;
		this.animationInterval = interval;
		this.frameCount = frameTiles.size;
	}

}

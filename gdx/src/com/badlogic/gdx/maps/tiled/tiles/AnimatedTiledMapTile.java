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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;

/** @brief Represents a changing {@link TiledMapTile}. */
public class AnimatedTiledMapTile implements TiledMapTile {

	private static long lastTiledMapRenderTime = 0;

	private int id;

	private BlendMode blendMode = BlendMode.ALPHA;

	private MapProperties properties;

	private StaticTiledMapTile[] frameTiles;

	private int[] animationIntervals;
	private int frameCount = 0;
	private int loopDuration;
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

	public int getCurrentFrameIndex () {
		int currentTime = (int)(lastTiledMapRenderTime % loopDuration);

		for (int i = 0; i < animationIntervals.length; ++i) {
			int animationInterval = animationIntervals[i];
			if (currentTime <= animationInterval) return i;
			currentTime -= animationInterval;
		}

		throw new GdxRuntimeException(
			"Could not determine current animation frame in AnimatedTiledMapTile.  This should never happen.");
	}

	public TiledMapTile getCurrentFrame () {
		return frameTiles[getCurrentFrameIndex()];
	}

	@Override
	public TextureRegion getTextureRegion () {
		return getCurrentFrame().getTextureRegion();
	}

	@Override
	public void setTextureRegion (TextureRegion textureRegion) {
		throw new GdxRuntimeException("Cannot set the texture region of AnimatedTiledMapTile.");
	}

	@Override
	public float getOffsetX () {
		return getCurrentFrame().getOffsetX();
	}

	@Override
	public void setOffsetX (float offsetX) {
		throw new GdxRuntimeException("Cannot set offset of AnimatedTiledMapTile.");
	}

	@Override
	public float getOffsetY () {
		return getCurrentFrame().getOffsetY();
	}

	@Override
	public void setOffsetY (float offsetY) {
		throw new GdxRuntimeException("Cannot set offset of AnimatedTiledMapTile.");
	}

	public int[] getAnimationIntervals () {
		return animationIntervals;
	}

	public void setAnimationIntervals (int[] intervals) {
		if (intervals.length == animationIntervals.length) {
			this.animationIntervals = intervals;

			loopDuration = 0;
			for (int i = 0; i < intervals.length; i++) {
				loopDuration += intervals[i];
			}

		} else {
			throw new GdxRuntimeException("Cannot set " + intervals.length
				+ " frame intervals. The given int[] must have a size of " + animationIntervals.length + ".");
		}
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
		this.frameTiles = new StaticTiledMapTile[frameTiles.size];
		this.frameCount = frameTiles.size;

		this.loopDuration = frameTiles.size * (int)(interval * 1000f);
		this.animationIntervals = new int[frameTiles.size];
		for (int i = 0; i < frameTiles.size; ++i) {
			this.frameTiles[i] = frameTiles.get(i);
			this.animationIntervals[i] = (int)(interval * 1000f);
		}
	}

	/** Creates an animated tile with the given animation intervals and frame tiles.
	 *
	 * @param intervals The intervals between each individual frame tile in milliseconds.
	 * @param frameTiles An array of {@link StaticTiledMapTile}s that make up the animation. */
	public AnimatedTiledMapTile (IntArray intervals, Array<StaticTiledMapTile> frameTiles) {
		this.frameTiles = new StaticTiledMapTile[frameTiles.size];
		this.frameCount = frameTiles.size;

		this.animationIntervals = intervals.toArray();
		this.loopDuration = 0;

		for (int i = 0; i < intervals.size; ++i) {
			this.frameTiles[i] = frameTiles.get(i);
			this.loopDuration += intervals.get(i);
		}
	}

	public StaticTiledMapTile[] getFrameTiles () {
		return frameTiles;
	}
}

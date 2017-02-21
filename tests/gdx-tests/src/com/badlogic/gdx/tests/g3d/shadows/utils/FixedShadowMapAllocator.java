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

package com.badlogic.gdx.tests.g3d.shadows.utils;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** FixedShadowMapAllocator behavior is naive. It separates the texture in several parts and for each light increments the region.
 * The larger the size, the better the quality or quantity.
 * <p>
 * Examples: <br />
 * If you set size to QUALITY_MAX and mapQuantity to NB_MAP_MIN, each depth map would be 2048*2048 (it's huge!).<br />
 * If you set size to QUALITY_MIN and mapQuantity to NB_MAP_MAX, each depth map would be 64*64.
 * </p>
 * @author realitix */
public class FixedShadowMapAllocator implements ShadowMapAllocator {
	/** Helpers to choose shadow map quality */
	public static final int QUALITY_MIN = 1024;
	public static final int QUALITY_MED = 2048;
	public static final int QUALITY_MAX = 4096;

	/** Helpers to choose number of supported shadows */
	public static final int QUANTITY_MAP_MIN = 4;
	public static final int QUANTITY_MAP_MED = 16;
	public static final int QUANTITY_MAP_MAX = 32;

	/** Shadow map size (Width = Height) */
	protected final int size;
	/** Quantity of renderable parts */
	protected final int mapQuantity;
	/** Current rendered part */
	protected int currentMap;
	/** Result region */
	protected ShadowMapRegion result = new ShadowMapRegion();
	/** Is in allocation state */
	protected boolean allocating = false;

	/** Create new FixedShadowMapAllocator
	 * @param size Size of shadow map
	 * @param nbMap Quantity of supported regions */
	public FixedShadowMapAllocator (int size, int nbMap) {
		this.size = size;
		this.mapQuantity = nbMap;
	}

	@Override
	public int getWidth () {
		return size;
	}

	@Override
	public int getHeight () {
		return size;
	}

	/** @return Quantity of supported regions. */
	public int getMapQuantity () {
		return mapQuantity;
	}

	@Override
	public void begin () {
		if (allocating) {
			throw new GdxRuntimeException("Allocator must end before begin");
		}
		allocating = true;
		currentMap = 0;
	}

	@Override
	public void end () {
		if (!allocating) {
			throw new GdxRuntimeException("Allocator must begin before end");
		}
		allocating = false;
	}

	@Override
	public ShadowMapRegion nextResult (BaseLight light) {
		if (!allocating) {
			throw new GdxRuntimeException("Allocator must begin before call");
		}

		int nbOnLine = (int)Math.round(Math.sqrt(mapQuantity));
		int i = currentMap % nbOnLine;
		int j = currentMap / nbOnLine;
		int sizeMap = size / nbOnLine;

		result.x = i * sizeMap;
		result.y = j * sizeMap;
		result.width = sizeMap;
		result.height = sizeMap;

		if (result.x >= size || result.y >= size) return null;

		currentMap += 1;

		return result;
	}
}

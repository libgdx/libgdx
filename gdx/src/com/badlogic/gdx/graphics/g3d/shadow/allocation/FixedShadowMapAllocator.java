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

package com.badlogic.gdx.graphics.g3d.shadow.allocation;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** The behavior of the FixedShadowMapAllocator is naive. It separates the texture in several parts and for each lights increments
 * the region
 * @author realitix */
public class FixedShadowMapAllocator implements ShadowMapAllocator {
	public static final int QUALITY_MIN = 1024;
	public static final int QUALITY_MED = 2048;
	public static final int QUALITY_MAX = 4096;

	public static final int NB_MAP_MIN = 4;
	public static final int NB_MAP_MED = 16;
	public static final int NB_MAP_MAX = 32;

	protected final int size;
	protected final int nbMap;
	protected int currentMap;
	protected ShadowMapRegion result = new ShadowMapRegion();
	protected boolean allocating = false;

	public FixedShadowMapAllocator (int size, int nbMap) {
		this.size = size;
		this.nbMap = nbMap;
	}

	@Override
	public int getSize () {
		return size;
	}

	public int getNbMap () {
		return nbMap;
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
	public ShadowMapRegion nextResult (Camera camera) {
		if (!allocating) {
			throw new GdxRuntimeException("Allocator must begin before call");
		}

		int nbOnLine = (int)Math.round(Math.sqrt(nbMap));
		int i = currentMap % nbOnLine;
		int j = currentMap / nbOnLine;
		int sizeMap = size / nbOnLine;

		result.x = i * sizeMap;
		result.y = j * sizeMap;
		result.width = sizeMap;
		result.height = sizeMap;

		currentMap += 1;

		return result;
	}
}

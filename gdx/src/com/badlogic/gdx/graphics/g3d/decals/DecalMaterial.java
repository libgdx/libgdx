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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Material used by the {@link Decal} class */
public class DecalMaterial {
	public static final int NO_BLEND = -1;
	protected TextureRegion textureRegion;
	protected int srcBlendFactor;
	protected int dstBlendFactor;

	/** Binds the material's texture to the OpenGL context and changes the glBlendFunc to the values used by it. */
	public void set () {
		textureRegion.getTexture().bind(0);
		if (!isOpaque()) {
			Gdx.gl.glBlendFunc(srcBlendFactor, dstBlendFactor);
		}
	}

	/** @return true if the material is completely opaque, false if it is not and therefor requires blending */
	public boolean isOpaque () {
		return srcBlendFactor == NO_BLEND;
	}

	public int getSrcBlendFactor () {
		return srcBlendFactor;
	}

	public int getDstBlendFactor () {
		return dstBlendFactor;
	}

	@Override
	public boolean equals (Object o) {
		if (o == null) return false;

		DecalMaterial material = (DecalMaterial)o;

		return dstBlendFactor == material.dstBlendFactor && srcBlendFactor == material.srcBlendFactor
			&& textureRegion.getTexture() == material.textureRegion.getTexture();

	}

	@Override
	public int hashCode () {
		int result = textureRegion.getTexture() != null ? textureRegion.getTexture().hashCode() : 0;
		result = 31 * result + srcBlendFactor;
		result = 31 * result + dstBlendFactor;
		return result;
	}
}

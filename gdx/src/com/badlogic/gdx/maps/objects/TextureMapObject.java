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

package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;

/** @brief Represents a map object containing a texture (region) */
public class TextureMapObject extends MapObject {

	private float x = 0.0f;
	private float y = 0.0f;
	private float originX = 0.0f;
	private float originY = 0.0f;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float rotation = 0.0f;
	private TextureRegion textureRegion = null;

	/** @return x axis coordinate */
	public float getX () {
		return x;
	}

	/** @param x new x axis coordinate */
	public void setX (float x) {
		this.x = x;
	}

	/** @return y axis coordinate */
	public float getY () {
		return y;
	}

	/** @param y new y axis coordinate */
	public void setY (float y) {
		this.y = y;
	}

	/** @return x axis origin */
	public float getOriginX () {
		return originX;
	}

	/** @param x new x axis origin */
	public void setOriginX (float x) {
		this.originX = x;
	}

	/** @return y axis origin */
	public float getOriginY () {
		return originY;
	}

	/** @param y new axis origin */
	public void setOriginY (float y) {
		this.originY = y;
	}

	/** @return x axis scale */
	public float getScaleX () {
		return scaleX;
	}

	/** @param x new x axis scale */
	public void setScaleX (float x) {
		this.scaleX = x;
	}

	/** @return y axis scale */
	public float getScaleY () {
		return scaleY;
	}

	/** @param y new y axis scale */
	public void setScaleY (float y) {
		this.scaleY = y;
	}

	/** @return texture's rotation in radians */
	public float getRotation () {
		return rotation;
	}

	/** @param rotation new texture's rotation in radians */
	public void setRotation (float rotation) {
		this.rotation = rotation;
	}

	/** @return region */
	public TextureRegion getTextureRegion () {
		return textureRegion;
	}

	/** @param region new texture region */
	public void setTextureRegion (TextureRegion region) {
		textureRegion = region;
	}

	/** Creates an empty texture map object */
	public TextureMapObject () {
		this(null);
	}

	/** Creates texture map object with the given region
	 * 
	 * @param textureRegion the {@link TextureRegion} to use. */
	public TextureMapObject (TextureRegion textureRegion) {
		super();
		this.textureRegion = textureRegion;
	}
}

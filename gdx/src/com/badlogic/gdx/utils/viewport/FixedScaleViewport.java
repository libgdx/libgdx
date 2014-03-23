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

package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;

/** A viewport where the world size is a fixed scale of the screen size (world = screen * scale). This is useful for defining a precise 'pixels per world unit' scale (scale = 1 / PPWU) and letting screen size determine the area of world displayed. */
public class FixedScaleViewport extends Viewport {

	/* The screen scale factor */
	private float scale;
	
	/** Creates a new viewport with the specified scale factor and a new {@link OrthographicCamera}.
	 * @param scale The scale factor. */
	public FixedScaleViewport (float scale) {
		this(scale, new OrthographicCamera());
	}

	/** Creates a new viewport with the specified scale factor and camera.
	 * @param scale The scale factor.
	 * @param camera The camera to manage with this viewport. */
	public FixedScaleViewport (float scale, Camera camera) {
		this.scale = scale;
		this.camera = camera;
	}
	
	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		viewportX = 0;
		viewportY = 0;
		viewportWidth = screenWidth;
		viewportHeight = screenHeight;
		worldWidth = screenWidth * scale;
		worldHeight = screenHeight * scale;
		super.update(screenWidth, screenHeight, centerCamera);
	}
	
	/** Returns the scale factor.
	 * @return The scale factor. */
	public float getScale() {
		return scale;
	}
	
	/** Sets the scale factor.
	 * @param scale The scale factor. */
	public void setScale(float scale) {
		this.scale = scale;
	}
}

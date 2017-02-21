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

/** A viewport where the world size is based on the size of the screen. By default 1 world unit == 1 screen pixel, but this ratio
 * can be {@link #setUnitsPerPixel(float) changed}.
 * @author Daniel Holderbaum
 * @author Nathan Sweet */
public class ScreenViewport extends Viewport {
	private float unitsPerPixel = 1;

	/** Creates a new viewport using a new {@link OrthographicCamera}. */
	public ScreenViewport () {
		this(new OrthographicCamera());
	}

	public ScreenViewport (Camera camera) {
		setCamera(camera);
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		setScreenBounds(0, 0, screenWidth, screenHeight);
		setWorldSize(screenWidth * unitsPerPixel, screenHeight * unitsPerPixel);
		apply(centerCamera);
	}

	public float getUnitsPerPixel () {
		return unitsPerPixel;
	}

	/** Sets the number of pixels for each world unit. Eg, a scale of 2.5 means there are 2.5 world units for every 1 screen pixel.
	 * Default is 1. */
	public void setUnitsPerPixel (float unitsPerPixel) {
		this.unitsPerPixel = unitsPerPixel;
	}
}

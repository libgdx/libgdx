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

/** A viewport where the world size is the size of the screen. The world is never scaled. 1 world unit == 1 screen pixel.
 * @author Daniel Holderbaum */
public class ScreenViewport extends Viewport {
	/** Creates a new viewport using a new {@link OrthographicCamera}. */
	public ScreenViewport () {
		this(new OrthographicCamera());
	}

	public ScreenViewport (Camera camera) {
		this.camera = camera;
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		viewportX = 0;
		viewportY = 0;
		viewportWidth = screenWidth;
		viewportHeight = screenHeight;
		worldWidth = screenWidth;
		worldHeight = screenHeight;
		super.update(screenWidth, screenHeight, centerCamera);
	}
}

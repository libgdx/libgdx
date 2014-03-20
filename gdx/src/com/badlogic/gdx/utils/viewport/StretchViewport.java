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

/** A viewport that stretches the world to the screen size, possibly distorting the aspect ratio.
 * @author Daniel Holderbaum */
public class StretchViewport extends Viewport {
	public StretchViewport (float worldWidth, float worldHeight) {
		this(worldWidth, worldHeight, new OrthographicCamera());
	}

	public StretchViewport (float worldWidth, float worldHeight, Camera camera) {
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
		this.camera = camera;
	}

	@Override
	public void update (int screenWidth, int screenHeight) {
		viewportX = 0;
		viewportY = 0;
		viewportWidth = screenWidth;
		viewportHeight = screenHeight;
		super.update(screenWidth, screenHeight);
	}
}

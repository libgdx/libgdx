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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

/** A viewport that keeps the world aspect ratio without black bars by extending the world in one direction. The world is first
 * scaled to fit within the viewport, then the shorter dimension is lengthened to fill the viewport.
 * @author Nathan Sweet */
public class ExtendViewport extends Viewport {
	private float originalWorldWidth, originalWorldHeight;

	/** Creates a new viewport using a new {@link OrthographicCamera}. */
	public ExtendViewport (float worldWidth, float worldHeight) {
		this(worldWidth, worldHeight, new OrthographicCamera());
	}

	public ExtendViewport (float worldWidth, float worldHeight, Camera camera) {
		originalWorldWidth = worldWidth;
		originalWorldHeight = worldHeight;
		this.camera = camera;
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		viewportX = 0;
		viewportY = 0;
		viewportWidth = screenWidth;
		viewportHeight = screenHeight;
		worldWidth = originalWorldWidth;
		worldHeight = originalWorldHeight;
		Vector2 scaled = Scaling.fit.apply(worldWidth, worldHeight, screenWidth, screenHeight);
		viewportWidth = Math.round(scaled.x);
		viewportHeight = Math.round(scaled.y);
		if (viewportWidth < screenWidth) {
			float toWorldSpace = worldHeight / viewportHeight;
			worldWidth += (screenWidth - viewportWidth) * toWorldSpace;
			viewportWidth = screenWidth;
		} else if (viewportHeight < screenHeight) {
			float toWorldSpace = worldWidth / viewportWidth;
			worldHeight += (screenHeight - viewportHeight) * toWorldSpace;
			viewportHeight = screenHeight;
		}
		super.update(screenWidth, screenHeight, centerCamera);
	}
}

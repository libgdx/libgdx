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

/** A viewport that keeps the world aspect ratio by extending the world in one direction. The world is first scaled to fit within
 * the viewport, then the shorter dimension is lengthened to fill the viewport. A maximum size can be specified to limit how much
 * the world is extended and black bars (letterboxing) are used for any remaining space.
 * @author Nathan Sweet */
public class ExtendViewport extends Viewport {
	private float minWorldWidth, minWorldHeight;
	private float maxWorldWidth, maxWorldHeight;

	/** Creates a new viewport using a new {@link OrthographicCamera} with no maximum world size. */
	public ExtendViewport (float minWorldWidth, float minWorldHeight) {
		this(minWorldWidth, minWorldHeight, 0, 0, new OrthographicCamera());
	}

	/** Creates a new viewport with no maximum world size. */
	public ExtendViewport (float minWorldWidth, float minWorldHeight, Camera camera) {
		this(minWorldWidth, minWorldHeight, 0, 0, camera);
	}

	/** Creates a new viewport using a new {@link OrthographicCamera} and a maximum world size.
	 * @see ExtendViewport#ExtendViewport(float, float, float, float, Camera) */
	public ExtendViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight) {
		this(minWorldWidth, minWorldHeight, maxWorldWidth, maxWorldHeight, new OrthographicCamera());
	}

	/** Creates a new viewport with a maximum world size.
	 * @param maxWorldWidth User 0 for no maximum width.
	 * @param maxWorldHeight User 0 for no maximum height. */
	public ExtendViewport (float minWorldWidth, float minWorldHeight, float maxWorldWidth, float maxWorldHeight, Camera camera) {
		this.minWorldWidth = minWorldWidth;
		this.minWorldHeight = minWorldHeight;
		this.maxWorldWidth = maxWorldWidth;
		this.maxWorldHeight = maxWorldHeight;
		setCamera(camera);
	}

	@Override
	public void update (int screenWidth, int screenHeight, boolean centerCamera) {
		// Fit min size to the screen.
		float worldWidth = minWorldWidth;
		float worldHeight = minWorldHeight;
		Vector2 scaled = Scaling.fit.apply(worldWidth, worldHeight, screenWidth, screenHeight);

		// Extend in the short direction.
		int viewportWidth = Math.round(scaled.x);
		int viewportHeight = Math.round(scaled.y);
		if (viewportWidth < screenWidth) {
			float toViewportSpace = viewportHeight / worldHeight;
			float toWorldSpace = worldHeight / viewportHeight;
			float lengthen = (screenWidth - viewportWidth) * toWorldSpace;
			if (maxWorldWidth > 0) lengthen = Math.min(lengthen, maxWorldWidth - minWorldWidth);
			worldWidth += lengthen;
			viewportWidth += Math.round(lengthen * toViewportSpace);
		} else if (viewportHeight < screenHeight) {
			float toViewportSpace = viewportWidth / worldWidth;
			float toWorldSpace = worldWidth / viewportWidth;
			float lengthen = (screenHeight - viewportHeight) * toWorldSpace;
			if (maxWorldHeight > 0) lengthen = Math.min(lengthen, maxWorldHeight - minWorldHeight);
			worldHeight += lengthen;
			viewportHeight += Math.round(lengthen * toViewportSpace);
		}

		setWorldSize(worldWidth, worldHeight);

		// Center.
		setScreenBounds((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2, viewportWidth, viewportHeight);

		apply(centerCamera);
	}

	public float getMinWorldWidth () {
		return minWorldWidth;
	}

	public void setMinWorldWidth (float minWorldWidth) {
		this.minWorldWidth = minWorldWidth;
	}

	public float getMinWorldHeight () {
		return minWorldHeight;
	}

	public void setMinWorldHeight (float minWorldHeight) {
		this.minWorldHeight = minWorldHeight;
	}

	public float getMaxWorldWidth () {
		return maxWorldWidth;
	}

	public void setMaxWorldWidth (float maxWorldWidth) {
		this.maxWorldWidth = maxWorldWidth;
	}

	public float getMaxWorldHeight () {
		return maxWorldHeight;
	}

	public void setMaxWorldHeight (float maxWorldHeight) {
		this.maxWorldHeight = maxWorldHeight;
	}
}

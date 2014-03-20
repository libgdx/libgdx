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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;

/** @author Daniel Holderbaum */
public class StaticViewport extends Viewport {

	public StaticViewport (Camera camera, float virtualWidth, float virtualHeight) {
		this.camera = camera;
		this.virtualWidth = virtualWidth;
		this.virtualHeight = virtualHeight;
		this.viewportWidth = Math.round(virtualWidth);
		this.viewportHeight = Math.round(virtualHeight);
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	protected void calculateViewport (int width, int height) {
		this.viewportX = (width - viewportWidth) / 2;
		this.viewportY = (height - viewportHeight) / 2;
	}
}

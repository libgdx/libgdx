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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

/** This is used to work with a fixed virtual viewport. It implements "letterboxing" which means that it will maintain the aspect
 * ratio of the virtual viewport while scaling it to fit the screen.
 * 
 * @author Daniel Holderbaum */
public class FixedViewport extends Viewport {

	/** Initializes this virtual viewport.
	 * 
	 * @param virtualWidth The constant width of this viewport.
	 * @param virtualHeight The constant height of this viewport. */
	public FixedViewport (int virtualWidth, int virtualHeight) {
		this.virtualWidth = virtualWidth;
		this.virtualHeight = virtualHeight;
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void calculateViewport (int width, int height) {
		Vector2 scaled = Scaling.fit.apply(virtualWidth, virtualHeight, width, height);
		viewportWidth = Math.round(scaled.x);
		viewportHeight = Math.round(scaled.y);
		// center the viewport in the middle of the screen
		viewportX = (width - viewportWidth) / 2;
		viewportY = (height - viewportHeight) / 2;
	}

	@Override
	protected void update (Stage stage) {
		stage.setViewport(virtualWidth, virtualHeight, true, viewportX, viewportY, viewportWidth, viewportHeight);
		Table rootTable = getRootTable(stage);
		if (rootTable != null) {
			rootTable.setSize(virtualWidth, virtualHeight);
		}
	}

}

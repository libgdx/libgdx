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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** @author Daniel Holderbaum */
public class ScreenViewport extends Viewport {

	public ScreenViewport () {
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void calculateViewport (int width, int height) {
		viewportX = 0;
		viewportY = 0;
		viewportWidth = width;
		viewportHeight = height;
		virtualWidth = width;
		virtualHeight = height;
	}

	@Override
	protected void update (Stage stage) {
		stage.setViewport(virtualWidth, virtualHeight, false);
		if (stage.getRoot().getChildren().size == 1 && stage.getRoot().getChildren().get(0) instanceof Table) {
			Table rootTable = (Table)stage.getRoot().getChildren().get(0);
			rootTable.setBounds(viewportX, viewportY, viewportWidth, viewportHeight);
			rootTable.invalidate();
		}
	}

}

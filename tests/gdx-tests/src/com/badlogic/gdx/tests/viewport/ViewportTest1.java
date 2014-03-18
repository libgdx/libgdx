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

package com.badlogic.gdx.tests.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.DoubleRatioViewport;
import com.badlogic.gdx.utils.viewport.FixedViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StaticViewport;
import com.badlogic.gdx.utils.viewport.StretchedViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This test makes use of the different kind of viewports, while using a stage and a root Table for the layout. */
public class ViewportTest1 extends GdxTest {

	private float delay;

	Array<Viewport> viewports = new Array<Viewport>(4);

	private Viewport viewport;
	private Stage stage;
	private Table root;
	private TextButton button;

	public void create () {
		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);
		root = new Table();

		stage.addActor(root);
		root.setBackground(skin.getDrawable("default-pane"));
		root.debug().defaults().space(6);
		root.add(new TextButton("Button 1", skin));
		button = new TextButton("Button 2", skin);
		root.add(button);
		root.add(new TextButton("Button 3", skin));

		viewports.add(new StretchedViewport(stage.getCamera(), 300, 200));
		viewports.add(new FixedViewport(stage.getCamera(), 300, 200));
		viewports.add(new ScreenViewport(stage.getCamera()));
		viewports.add(new StaticViewport(stage.getCamera(), 300, 200));
// viewports.add(new DoubleRatioViewport(300, 200, 600, 400));
		viewport = viewports.first();
	}

	public void render () {
		delay -= Gdx.graphics.getDeltaTime();
		// iterate through the viewports
		if (delay <= 0) {
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
				viewport = viewports.get((viewports.indexOf(viewport, true) + 1) % viewports.size);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				viewport.updateStage(stage);
				delay = 1f;
			}
		}

		button.setText(viewport.getClass().getSimpleName());

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		viewport.update(width, height);
		viewport.updateStage(stage);
// root.setSize(viewport.virtualWidth, viewport.virtualHeight);
// root.setBounds(viewport.viewportX, viewport.viewportY, viewport.virtualWidth, viewport.virtualHeight);
// root.setBounds((width- viewport.virtualWidth) / 2, (height- viewport.virtualHeight) / 2, viewport.virtualWidth,
// viewport.virtualHeight);
// root.setBounds((width- viewport.viewportWidth) / 2, (height- viewport.viewportHeight) / 2, viewport.viewportWidth,
// viewport.viewportHeight);
//		root.setBounds(viewport.viewportX, viewport.viewportY, viewport.viewportWidth, viewport.viewportHeight);
//		root.setBounds(0, 0, viewport.viewportWidth, viewport.viewportHeight);
//		root.invalidate();
	}

	public void dispose () {
		stage.dispose();
	}
}

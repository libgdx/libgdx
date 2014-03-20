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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Cycles viewports while rendering a stage with a root Table for the layout. */
public class ViewportTest1 extends GdxTest {
	Array<Viewport> viewports = new Array();
	Stage stage;
	private Label label;

	public void create () {
		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		label = new Label("", skin);

		Table root = new Table(skin);
		root.setFillParent(true);
		root.setBackground(skin.getDrawable("default-pane"));
		root.debug().defaults().space(6);
		root.add(new TextButton("Button 1", skin));
		root.add(new TextButton("Button 2", skin)).row();
		root.add("Press spacebar to change the viewport:").colspan(2).row();
		root.add(label).colspan(2);
		stage.addActor(root);

		int worldWidth = 300;
		int worldHeight = 200;

		Camera camera = stage.getCamera();
		viewports.add(new ScalingViewport(Scaling.stretch, worldWidth, worldHeight, camera));
		viewports.add(new ScalingViewport(Scaling.fill, worldWidth, worldHeight, camera));
		viewports.add(new ScalingViewport(Scaling.fit, worldWidth, worldHeight, camera));
		viewports.add(new ExtendViewport(worldWidth, worldHeight, camera));
		viewports.add(new ScreenViewport(camera));
		viewports.add(new ScalingViewport(Scaling.none, worldWidth, worldHeight, camera));
		stage.setViewport(viewports.first());

		Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
			public boolean keyDown (int keycode) {
				if (keycode == Input.Keys.SPACE) {
					Viewport viewport = viewports.get((viewports.indexOf(stage.getViewport(), true) + 1) % viewports.size);
					stage.setViewport(viewport);
					resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
				return false;
			}
		}, stage));
	}

	public void render () {
		label.setText(stage.getViewport().toString());
		stage.act();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		System.out.println(stage.getViewport());
		stage.getViewport().update(width, height);
	}

	public void dispose () {
		stage.dispose();
	}
}

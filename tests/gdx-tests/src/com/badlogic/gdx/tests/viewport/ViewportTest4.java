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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.DebugActor;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.DoubleRatioViewport;
import com.badlogic.gdx.utils.viewport.FixedViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This test makes use of the different kind of viewports, while using a stage and a camera. */
public class ViewportTest4 extends GdxTest {

	private float delay;

	Array<Viewport> viewports = new Array<Viewport>(4);

	private Viewport viewport;
	private Stage stage;
	private Label label;
	private OrthographicCamera camera;

	public void create () {
		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);

		label = new Label("Label", skin);
		// a background so we can recognise the black bars
		Actor background = new DebugActor(new Color(0.2f, 0.2f, 0.2f, 1));
		background.setSize(10000, 10000);
		stage.addActor(background);
		stage.addActor(label);
		Actor actor = new DebugActor(new Color(1, 0, 0, 1));
		actor.setSize(32, 32);
		actor.setOrigin(16, 16);
		actor.setRotation(45);
		actor.setPosition(100, 100);
		stage.addActor(actor);

		viewports.add(new StretchingViewport(300, 200));
		viewports.add(new FixedViewport(300, 200));
		viewports.add(new ScreenViewport());
//		viewports.add(new DoubleRatioViewport(300, 200, 600, 400));
		viewport = viewports.first();
		viewport.manage(stage);
	}

	public void render () {
		delay -= Gdx.graphics.getDeltaTime();
		// iterate through the viewports
		if (delay <= 0) {
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
				viewport = viewports.get((viewports.indexOf(viewport, true) + 1) % viewports.size);
				viewport.manage(stage);
				delay = 1f;
			}
		}

		label.setText(viewport.getClass().getSimpleName());

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		viewport.update(width, height);
	}

	public void dispose () {
		stage.dispose();
	}
}

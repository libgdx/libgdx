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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextAreaTest extends GdxTest {
	private Stage stage;
	private Skin skin;

	@Override
	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		TextArea textArea = new TextArea(
			"Text Area\nEssentially, a text field\nwith\nmultiple\nlines.\n"
				+ "It can even handle very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong lines.",
			skin);
		textArea.setX(10);
		textArea.setY(10);
		textArea.setWidth(200);
		textArea.setHeight(200);

		TextField textField = new TextField("Text field", skin);
		textField.setX(10);
		textField.setY(220);
		textField.setWidth(200);
		textField.setHeight(30);
		stage.addActor(textArea);
		stage.addActor(textField);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		Gdx.app.log("X", "FPS: " + Gdx.graphics.getFramesPerSecond());
		SpriteBatch spriteBatch = (SpriteBatch)stage.getBatch();
		Gdx.app.log("X", "render calls: " + spriteBatch.totalRenderCalls);
		spriteBatch.totalRenderCalls = 0;
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
}

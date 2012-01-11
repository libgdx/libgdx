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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextButtonTest extends GdxTest {
	
	private Stage stage;
	
	@Override
	public void create () {
		stage = new Stage(0, 0, false, new SpriteBatch());
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		for (int i = 0; i < 500; i++) {
			TextButton t = new TextButton("Button"+i, skin);
			t.x = MathUtils.random(0, Gdx.graphics.getWidth());
			t.y = MathUtils.random(0, Gdx.graphics.getHeight());
			t.width = MathUtils.random(50, 200);
			t.height = MathUtils.random(0, 100);
			stage.addActor(t);
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
		Gdx.app.log("X", "FPS: "+Gdx.graphics.getFramesPerSecond());
		Gdx.app.log("X", "GL20: "+Gdx.graphics.isGL20Available());
		Gdx.app.log("X", "render calls: " + stage.getSpriteBatch().totalRenderCalls);
		stage.getSpriteBatch().totalRenderCalls = 0;
	}

	@Override
	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
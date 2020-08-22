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

package com.badlogic.gdx.tests.gwt;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtInputTest extends GdxTest {
	ShapeRenderer renderer;
	int x = 0;
	int y = 0;

	@Override
	public void create () {
		renderer = new ShapeRenderer();
		Gdx.input.setInputProcessor(this);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.input.setCursorCatched(true);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.begin(ShapeType.Filled);
		if (Gdx.input.isTouched())
			renderer.setColor(Color.RED);
		else
			renderer.setColor(Color.GREEN);
		renderer.rect(Gdx.input.getX() - 15, Gdx.graphics.getHeight() - Gdx.input.getY() - 15, 30, 30);
		renderer.rect(x, y, 30, 30);
		renderer.end();
		
		if (Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
			Gdx.app.log("GwtInputTest", "key pressed: " + "ALT_LEFT");
		}
		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			Gdx.app.log("GwtInputTest", "key pressed: " + "CTRL_LEFT");
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			x -= 1;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			x += 1;
		}

		if (Gdx.input.isKeyPressed(Keys.UP)) {
			y += 1;
		}

		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			y -= 1;
		}
		if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
			Gdx.app.log("GwtInputTest", "button pressed: LEFT");
		}
		if (Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE)){
			Gdx.app.log("GwtInputTest", "button pressed: MIDDLE");
		}
		if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)){
			Gdx.app.log("GwtInputTest", "button pressed: RIGHT");
		}
	}

	@Override
	public boolean keyDown (int keycode) {
		Gdx.app.log("GdxInputTest", "key down: " + keycode);
		return super.keyDown(keycode);
	}

	@Override
	public boolean keyTyped (char character) {
		Gdx.app.log("GdxInputTest", "key typed: '" + character + "'");
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		Gdx.app.log("GdxInputTest", "key up: " + keycode);
		return false;
	}
}

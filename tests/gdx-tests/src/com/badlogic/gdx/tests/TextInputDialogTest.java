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
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextInputDialogTest extends GdxTest {
	String message;
	SpriteBatch batch;
	BitmapFont font;

	public void create () {
		message = "Touch screen for dialog";
		batch = new SpriteBatch();
		font = new BitmapFont();
		
		Gdx.input.getTextInput(new TextInputListener() {
			@Override
			public void input (String text) {
				message = "message: " + text + ", touch screen for new dialog";
			}

			@Override
			public void canceled () {
				message = "cancled by user";
			}
		}, "enter something funny", "funny", "something funny");
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, message, 10, 40);
		batch.end();

		if (Gdx.input.justTouched()) {
			
		}
	}
}

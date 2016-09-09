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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.CharArray;

/** Shows how to pull up the softkeyboard and process input from it.
 * @author mzechner */
public class SoftKeyboardTest extends GdxTest {
	SpriteBatch batch;
	BitmapFont font;
	SimpleCharSequence textBuffer;

	@Override
	public void create () {
		// we want to render the input, so we need
		// a sprite batch and a font
		batch = new SpriteBatch();
		font = new BitmapFont();
		textBuffer = new SimpleCharSequence();

		// we register an InputAdapter to listen for the keyboard
		// input. The on-screen keyboard might only generate
		// "key typed" events, depending on the backend.
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean keyTyped (char character) {
				// convert \r to \n
				if (character == '\r') character = '\n';

				// if we get \b, we remove the last inserted character
				if (character == '\b' && textBuffer.length() > 0) {
					textBuffer.delete();
				}

				// else we just insert the character
				textBuffer.add(character);
				return true;
			}
		});
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, textBuffer, 0, Gdx.graphics.getHeight() - 20);
		batch.end();

		// bring up the keyboard if we touch the screen
		if (Gdx.input.justTouched()) {
			Gdx.input.setOnscreenKeyboardVisible(true);
			textBuffer = new SimpleCharSequence();
		}
	}

	/** Let's create a very simple {@link CharSequence} implementation that can handle common text input operations.
	 * @author mzechner */
	public static class SimpleCharSequence implements CharSequence {
		CharArray chars = new CharArray();
		int cursor = -1;

		public void add (char c) {
			cursor++;
			if (cursor == -1)
				chars.add(c);
			else
				chars.insert(cursor, c);
		}

		public void delete () {
			if (chars.size == 0) return;
			chars.removeIndex(cursor - 1);
			cursor--;
		}

		@Override
		public char charAt (int index) {
			return chars.get(index);
		}

		@Override
		public int length () {
			return chars.size;
		}

		@Override
		public CharSequence subSequence (int arg0, int arg1) {
			return null;
		}
	}
}

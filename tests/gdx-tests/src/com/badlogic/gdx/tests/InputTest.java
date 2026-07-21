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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InputTest extends GdxTest implements InputProcessor {

	@Override
	public void create () {
// input = new RemoteInput();
		input.setInputProcessor(this);
// input.setCursorCatched(true);
//
// input.getTextInput(new Input.TextInputListener() {
// @Override
// public void input(String text) {
// app.log("Input test", "Input value: " + text);
// }
//
// @Override
// public void canceled() {
// app.log("Input test", "Canceled input text");
// }
// }, "Title", "Text", "Placeholder");
	}

	@Override
	public void render () {
		if (input.justTouched()) {
			app.log("Input Test", "just touched, button: " + (input.isButtonPressed(Buttons.LEFT) ? "left " : "")
				+ (input.isButtonPressed(Buttons.MIDDLE) ? "middle " : "")
				+ (input.isButtonPressed(Buttons.RIGHT) ? "right" : "") + (input.isButtonPressed(Buttons.BACK) ? "back" : "")
				+ (input.isButtonPressed(Buttons.FORWARD) ? "forward" : ""));
		}

		for (int i = 0; i < 10; i++) {
			if (input.getDeltaX(i) != 0 || input.getDeltaY(i) != 0) {
				app.log("Input Test", "delta[" + i + "]: " + input.getDeltaX(i) + ", " + input.getDeltaY(i));
			}
		}
// input.setCursorPosition(graphics.getWidth() / 2, graphics.getHeight() / 2);
// if(input.isTouched()) {
// app.log("Input Test", "is touched");
// }
	}

	@Override
	public boolean keyDown (int keycode) {
		app.log("Input Test", "key down: " + keycode);
		if (keycode == Keys.G) input.setCursorCatched(!input.isCursorCatched());
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		app.log("Input Test", "key typed: '" + character + "'");
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		app.log("Input Test", "key up: " + keycode);
		return false;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		app.log("Input Test", "touch down: " + x + ", " + y + ", button: " + getButtonString(button));
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		app.log("Input Test", "touch dragged: " + x + ", " + y + ", pointer: " + pointer);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		app.log("Input Test", "touch up: " + x + ", " + y + ", button: " + getButtonString(button));
		return false;
	}

	@Override
	public boolean mouseMoved (int x, int y) {
		app.log("Input Test", "touch moved: " + x + ", " + y);
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		app.log("Input Test", "scrolled: " + amountY);
		return false;
	}

	private String getButtonString (int button) {
		if (button == Buttons.LEFT) return "left";
		if (button == Buttons.RIGHT) return "right";
		if (button == Buttons.MIDDLE) return "middle";
		if (button == Buttons.BACK) return "back";
		if (button == Buttons.FORWARD) return "forward";
		return "unknown";
	}
}

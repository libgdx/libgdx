/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.tests.utils.GdxTest;

public class InputTest extends GdxTest implements InputProcessor {

	@Override public void create () {
		Gdx.input.setInputProcessor(this);
	}

	@Override public void render () {
		if(Gdx.input.justTouched()) {
			Gdx.app.log("Input Test", "just touched, button: " + (Gdx.input.isButtonPressed(Buttons.LEFT)?"left ":"")
																 + (Gdx.input.isButtonPressed(Buttons.MIDDLE)?"middle ":"")																 
																 + (Gdx.input.isButtonPressed(Buttons.RIGHT)?"right":""));
		}
	}

	@Override public boolean keyDown (int keycode) {
		Gdx.app.log("Input Test", "key down: " + keycode);
		return false;
	}

	@Override public boolean keyTyped (char character) {
		Gdx.app.log("Input Test", "key typed: '" + character + "'");
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		Gdx.app.log("Input Test", "key up: " + keycode);
		return false;
	}
	
	@Override public boolean touchDown (int x, int y, int pointer, int button) {
		Gdx.app.log("Input Test", "touch down: " + x + ", " + y + ", button: " + getButtonString(button));		
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		Gdx.app.log("Input Test", "touch dragged: " + x + ", " + y + ", pointer: " + pointer);		
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		Gdx.app.log("Input Test", "touch up: " + x + ", " + y + ", button: " + getButtonString(button));			
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		Gdx.app.log("Input Test", "touch moved: " + x + ", " + y);
		return false;
	}

	@Override public boolean scrolled (int amount) {
		Gdx.app.log("Input Test", "scrolled: " + amount);
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}
	
	private String getButtonString(int button) {
		if(button == Buttons.LEFT)
			return "left";
		if(button == Buttons.RIGHT)
			return "right";
		if(button == Buttons.MIDDLE)
			return "middle";
		return "left";
	}
}

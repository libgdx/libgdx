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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.box2d.Box2DTest;
import com.badlogic.gdx.tests.bullet.BulletTest;
import com.badlogic.gdx.tests.bullet.ConstraintsTest;
import com.badlogic.gdx.tests.bullet.MeshShapeTest;
import com.badlogic.gdx.tests.bullet.ShootTest;
import com.badlogic.gdx.tests.utils.GdxTest;

/** @author xoppa */
public class BulletTestCollection extends GdxTest implements InputProcessor, GestureListener {
	private final BulletTest[] tests = {new ShootTest(), new ConstraintsTest(), new MeshShapeTest()};
	
	private int testIndex = 0;
	
	private Application app = null;
	
	@Override
	public void render () {
		tests[testIndex].render();
	}
	
	@Override
	public void create () {
		if (app == null) {
			app = Gdx.app;
			tests[testIndex].create();
		}

		Gdx.input.setInputProcessor(new InputMultiplexer(this, new GestureDetector(this)));
	}
	
	@Override
	public void dispose () {
		tests[testIndex].dispose();
		app = null;
	}
	
	public void next() {
		app.log("TestCollection", "disposing test '" + tests[testIndex].getClass().getName());
		tests[testIndex].dispose();
		// This would be a good time for GC to kick in.
		System.gc();
		testIndex++;
		if (testIndex >= tests.length) testIndex = 0;
		tests[testIndex].create();
		app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName());
	}
	
	@Override
	public boolean keyDown (int keycode) {
		return tests[testIndex].keyDown(keycode);
	}

	@Override
	public boolean keyTyped (char character) {
		return tests[testIndex].keyTyped(character);
	}

	@Override
	public boolean keyUp (int keycode) {
		boolean result = tests[testIndex].keyUp(keycode); 
		if ((result == false) && (keycode == Keys.SPACE || keycode == Keys.MENU)) {
			next();
			result = true;
		}
		return result;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		return tests[testIndex].touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		return tests[testIndex].touchDragged(x, y, pointer);
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		return tests[testIndex].touchUp(x, y, pointer, button);
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

	@Override
	public boolean mouseMoved (int x, int y) {
		return tests[testIndex].mouseMoved (x, y);
	}

	@Override
	public boolean scrolled (int amount) {
		return tests[testIndex].scrolled (amount);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer, int button) {
		return tests[testIndex].touchDown (x, y, pointer, button);
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		return tests[testIndex].tap (x, y, count, button);
	}

	@Override
	public boolean longPress (float x, float y) {
		if (tests[testIndex].longPress (x, y) == false)
			next();
		return true;
	}

	@Override
	public boolean fling (float velocityX, float velocityY, int button) {
		return tests[testIndex].fling (velocityX, velocityY, button);
	}

	@Override
	public boolean pan (float x, float y, float deltaX, float deltaY) {
		return tests[testIndex].pan (x, y, deltaX, deltaY);
	}

	@Override
	public boolean zoom (float originalDistance, float currentDistance) {
		return tests[testIndex].zoom (originalDistance, currentDistance);
	}

	@Override
	public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
		return tests[testIndex].pinch (initialFirstPointer, initialSecondPointer, firstPointer, secondPointer);
	}
}

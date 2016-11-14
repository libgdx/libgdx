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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.box2d.ApplyForce;
import com.badlogic.gdx.tests.box2d.BodyTypes;
import com.badlogic.gdx.tests.box2d.Box2DTest;
import com.badlogic.gdx.tests.box2d.Bridge;
import com.badlogic.gdx.tests.box2d.Cantilever;
import com.badlogic.gdx.tests.box2d.Chain;
import com.badlogic.gdx.tests.box2d.CharacterCollision;
import com.badlogic.gdx.tests.box2d.CollisionFiltering;
import com.badlogic.gdx.tests.box2d.ContactListenerTest;
import com.badlogic.gdx.tests.box2d.ContinuousTest;
import com.badlogic.gdx.tests.box2d.ConveyorBelt;
import com.badlogic.gdx.tests.box2d.DebugRendererTest;
import com.badlogic.gdx.tests.box2d.OneSidedPlatform;
import com.badlogic.gdx.tests.box2d.Prismatic;
import com.badlogic.gdx.tests.box2d.Pyramid;
import com.badlogic.gdx.tests.box2d.SimpleTest;
import com.badlogic.gdx.tests.box2d.SphereStack;
import com.badlogic.gdx.tests.box2d.VaryingRestitution;
import com.badlogic.gdx.tests.box2d.VerticalStack;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Box2DTestCollection extends GdxTest implements InputProcessor, GestureListener {
	private final Box2DTest[] tests = {new DebugRendererTest(), new CollisionFiltering(), new Chain(), new Bridge(),
		new SphereStack(), new Cantilever(), new ApplyForce(), new ContinuousTest(), new Prismatic(), new CharacterCollision(),
		new BodyTypes(), new SimpleTest(), new Pyramid(), new OneSidedPlatform(), new VerticalStack(), new VaryingRestitution(),
		new ConveyorBelt()};

	private int testIndex = 0;

	private Application app = null;

	@Override
	public void render () {
		tests[testIndex].render();
	}

	@Override
	public void create () {
		if (this.app == null) {
			this.app = Gdx.app;
			Box2DTest test = tests[testIndex];
			test.create();
		}

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(new GestureDetector(this));
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void dispose () {
		tests[testIndex].dispose();
	}

	@Override
	public boolean keyDown (int keycode) {
		tests[testIndex].keyDown(keycode);

		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		tests[testIndex].keyTyped(character);
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		tests[testIndex].keyUp(keycode);
		return false;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		tests[testIndex].touchDown(x, y, pointer, button);
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		tests[testIndex].touchDragged(x, y, pointer);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		tests[testIndex].touchUp(x, y, pointer, button);
		return false;
	}

	@Override
	public boolean mouseMoved (int x, int y) {
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}

	@Override
	public boolean touchDown (float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		app.log("TestCollection", "disposing test '" + tests[testIndex].getClass().getName());
		tests[testIndex].dispose();
		testIndex++;
		if (testIndex >= tests.length) testIndex = 0;
		Box2DTest test = tests[testIndex];
		test.create();
		app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName());
		return false;
	}

	@Override
	public boolean longPress (float x, float y) {
		return false;
	}

	@Override
	public boolean fling (float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan (float x, float y, float deltaX, float deltaY) {
		return false;
	}

	@Override
	public boolean panStop (float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean zoom (float originalDistance, float currentDistance) {
		return false;
	}

	@Override
	public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
		return false;
	}

	@Override
	public void pinchStop () {
	}
}

/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.tests.box2d.ApplyForce;
import com.badlogic.gdx.tests.box2d.BodyTypes;
import com.badlogic.gdx.tests.box2d.Box2DTest;
import com.badlogic.gdx.tests.box2d.Bridge;
import com.badlogic.gdx.tests.box2d.Cantilever;
import com.badlogic.gdx.tests.box2d.Chain;
import com.badlogic.gdx.tests.box2d.CharacterCollision;
import com.badlogic.gdx.tests.box2d.CollisionFiltering;
import com.badlogic.gdx.tests.box2d.ContinuousTest;
import com.badlogic.gdx.tests.box2d.DebugRendererTest;
import com.badlogic.gdx.tests.box2d.OneSidedPlatform;
import com.badlogic.gdx.tests.box2d.Prismatic;
import com.badlogic.gdx.tests.box2d.Pyramid;
import com.badlogic.gdx.tests.box2d.SimpleTest;
import com.badlogic.gdx.tests.box2d.SphereStack;
import com.badlogic.gdx.tests.box2d.VaryingRestitution;
import com.badlogic.gdx.tests.box2d.VerticalStack;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Box2DTestCollection extends GdxTest implements InputProcessor {
	private final Box2DTest[] tests = {new DebugRendererTest(), new CollisionFiltering(), new Chain(), new Bridge(),
		new SphereStack(), new Cantilever(), new ApplyForce(), new ContinuousTest(), new Prismatic(), new CharacterCollision(),
		new BodyTypes(), new SimpleTest(), new Pyramid(), new OneSidedPlatform(), new VerticalStack(), new VaryingRestitution()};

	private int testIndex = 0;

	private Application app = null;


	@Override public void render () {
		tests[testIndex].render();
		Gdx.input.processEvents(this);
	}

	@Override public void create () {
		if (this.app == null) {
			this.app = Gdx.app;
			Box2DTest test = tests[testIndex];
			test.create();			
		}
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Keys.KEYCODE_SPACE) {
			app.log("TestCollection", "disposing test '" + tests[testIndex].getClass().getName());
			tests[testIndex].dispose();
			testIndex++;
			if (testIndex >= tests.length) testIndex = 0;
			Box2DTest test = tests[testIndex];
			test.create();
			app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName());
		}
		else {
			tests[testIndex].keyDown(keycode);
		}			

		return false;
	}

	@Override public boolean keyTyped (char character) {
		tests[testIndex].keyTyped(character);
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		tests[testIndex].keyUp(keycode);
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		tests[testIndex].touchDown(x, y, pointer);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		tests[testIndex].touchDragged(x, y, pointer);
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		tests[testIndex].touchUp(x, y, pointer);
		return false;
	}

	@Override
	public boolean needsGL20() {
		return false;
	}
}

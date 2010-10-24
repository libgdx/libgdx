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

package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;

public class TestCollection implements RenderListener, InputListener {
	private final Box2DTest[] tests = {new DebugRendererTest(), new CollisionFiltering(), new Chain(), new Bridge(),
		new SphereStack(), new Cantilever(), new ApplyForce(), new ContinuousTest(), new Prismatic(), new CharacterCollision(),
		new BodyTypes(), new SimpleTest(), new Pyramid(), new OneSidedPlatform(), new VerticalStack(), new VaryingRestitution()};

	private int testIndex = 0;

	private Application app = null;

	@Override public void dispose () {
		// TODO Auto-generated method stub

	}

	@Override public void render () {
		tests[testIndex].render();
	}

	@Override public void surfaceChanged (int width, int height) {
	}

	@Override public void surfaceCreated () {
		if (this.app == null) {
			this.app = Gdx.app;
			Box2DTest test = tests[testIndex];
			test.surfaceCreated();
			app.getInput().addInputListener(this);
		}
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Keys.KEYCODE_SPACE) {
			app.log("TestCollection", "disposing test '" + tests[testIndex].getClass().getName());
			tests[testIndex].dispose();
			testIndex++;
			if (testIndex >= tests.length) testIndex = 0;
			Box2DTest test = tests[testIndex];
			test.surfaceCreated();
			app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName());
		}

		return false;
	}

	@Override public boolean keyTyped (char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
}

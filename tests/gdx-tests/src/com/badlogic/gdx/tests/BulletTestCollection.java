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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.tests.bullet.*;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;

/** @author xoppa */
public class BulletTestCollection extends GdxTest implements InputProcessor, GestureListener {
	protected final BulletTest[] tests = {new BasicBulletTest(), new ShootTest(), new BasicShapesTest(), new KinematicTest(),
		new ConstraintsTest(), new MeshShapeTest(), new GimpactTest(), new ConvexHullTest(), new ConvexHullDistanceTest(),
		new RayCastTest(), new RayPickRagdollTest(), new InternalTickTest(), new CollisionWorldTest(), new CollisionTest(),
		new FrustumCullingTest(), new CollisionDispatcherTest(), new ContactCallbackTest(), new ContactCallbackTest2(),
		new ContactCacheTest(), new SoftBodyTest(), new SoftMeshTest(), new VehicleTest(), new CharacterTest(), new ImportTest(),
		new TriangleRaycastTest(), new OcclusionCullingTest(), new PairCacheTest()};

	protected int testIndex = 0;

	private Application app = null;

	private BitmapFont font;
	private Stage hud;
	private Label fpsLabel;
	private Label titleLabel;
	private Label instructLabel;
	private int loading = 0;
	private CameraInputController cameraController;

	@Override
	public void render () {
		if ((loading > 0) && (++loading > 2)) loadnext();

		tests[testIndex].render();
		fpsLabel.setText(tests[testIndex].performance);
		hud.draw();
	}

	@Override
	public void create () {
		if (app == null) {
			app = Gdx.app;
			tests[testIndex].create();
		}

		cameraController = new CameraInputController(tests[testIndex].camera);
		cameraController.activateKey = Keys.CONTROL_LEFT;
		cameraController.autoUpdate = false;
		cameraController.forwardTarget = false;
		cameraController.translateTarget = false;
		Gdx.input.setInputProcessor(new InputMultiplexer(cameraController, this, new GestureDetector(this)));

		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		hud = new Stage();
		hud.addActor(fpsLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE)));
		fpsLabel.setPosition(0, 0);
		hud.addActor(titleLabel = new Label(tests[testIndex].getClass().getSimpleName(), new Label.LabelStyle(font, Color.WHITE)));
		titleLabel.setY(hud.getHeight() - titleLabel.getHeight());
		hud.addActor(instructLabel = new Label("A\nB\nC\nD\nE\nF", new Label.LabelStyle(font, Color.WHITE)));
		instructLabel.setY(titleLabel.getY() - instructLabel.getHeight());
		instructLabel.setAlignment(Align.top | Align.left);
		instructLabel.setText(tests[testIndex].instructions);
	}

	@Override
	public void resize (int width, int height) {
		hud.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		tests[testIndex].dispose();
		app = null;
	}

	public void next () {
		titleLabel.setText("Loading...");
		loading = 1;
	}

	public void loadnext () {
		app.log("TestCollection", "disposing test '" + tests[testIndex].getClass().getName() + "'");
		tests[testIndex].dispose();
		// This would be a good time for GC to kick in.
		System.gc();
		testIndex++;
		if (testIndex >= tests.length) testIndex = 0;
		tests[testIndex].create();
		cameraController.camera = tests[testIndex].camera;
		app.log("TestCollection", "created test '" + tests[testIndex].getClass().getName() + "'");

		titleLabel.setText(tests[testIndex].getClass().getSimpleName());
		instructLabel.setText(tests[testIndex].instructions);
		loading = 0;
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
	public boolean mouseMoved (int x, int y) {
		return tests[testIndex].mouseMoved(x, y);
	}

	@Override
	public boolean scrolled (int amount) {
		return tests[testIndex].scrolled(amount);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer, int button) {
		return tests[testIndex].touchDown(x, y, pointer, button);
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		return tests[testIndex].tap(x, y, count, button);
	}

	@Override
	public boolean longPress (float x, float y) {
		return tests[testIndex].longPress(x, y);
	}

	@Override
	public boolean fling (float velocityX, float velocityY, int button) {
		if (tests[testIndex].fling(velocityX, velocityY, button) == false) next();
		return true;
	}

	@Override
	public boolean pan (float x, float y, float deltaX, float deltaY) {
		return tests[testIndex].pan(x, y, deltaX, deltaY);
	}

	@Override
	public boolean panStop (float x, float y, int pointer, int button) {
		return tests[testIndex].panStop(x, y, pointer, button);
	}

	@Override
	public boolean zoom (float originalDistance, float currentDistance) {
		return tests[testIndex].zoom(originalDistance, currentDistance);
	}

	@Override
	public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
		return tests[testIndex].pinch(initialFirstPointer, initialSecondPointer, firstPointer, secondPointer);
	}

	@Override
	public void pinchStop () {
	}
}

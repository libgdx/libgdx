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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GestureDetectorTest extends GdxTest implements ApplicationListener {
	Texture texture;
	SpriteBatch batch;
	OrthographicCamera camera;
	CameraController controller;
	GestureDetector gestureDetector;

	class CameraController implements GestureListener {
		float velX, velY;
		boolean flinging = false;
		float initialScale = 1;

		public boolean touchDown (float x, float y, int pointer, int button) {
			flinging = false;
			initialScale = camera.zoom;
			return false;
		}

		@Override
		public boolean tap (float x, float y, int count, int button) {
			Gdx.app.log("GestureDetectorTest", "tap at " + x + ", " + y + ", count: " + count);
			return false;
		}

		@Override
		public boolean longPress (float x, float y) {
			Gdx.app.log("GestureDetectorTest", "long press at " + x + ", " + y);
			return false;
		}

		@Override
		public boolean fling (float velocityX, float velocityY, int button) {
			Gdx.app.log("GestureDetectorTest", "fling " + velocityX + ", " + velocityY);
			flinging = true;
			velX = camera.zoom * velocityX * 0.5f;
			velY = camera.zoom * velocityY * 0.5f;
			return false;
		}

		@Override
		public boolean pan (float x, float y, float deltaX, float deltaY) {
			// Gdx.app.log("GestureDetectorTest", "pan at " + x + ", " + y);
			camera.position.add(-deltaX * camera.zoom, deltaY * camera.zoom, 0);
			return false;
		}

		@Override
		public boolean panStop (float x, float y, int pointer, int button) {
			Gdx.app.log("GestureDetectorTest", "pan stop at " + x + ", " + y);
			return false;
		}

		@Override
		public boolean zoom (float originalDistance, float currentDistance) {
			float ratio = originalDistance / currentDistance;
			camera.zoom = initialScale * ratio;
			System.out.println(camera.zoom);
			return false;
		}

		@Override
		public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
			return false;
		}

		public void update () {
			if (flinging) {
				velX *= 0.98f;
				velY *= 0.98f;
				camera.position.add(-velX * Gdx.graphics.getDeltaTime(), velY * Gdx.graphics.getDeltaTime(), 0);
				if (Math.abs(velX) < 0.01f) velX = 0;
				if (Math.abs(velY) < 0.01f) velY = 0;
			}
		}

		@Override
		public void pinchStop () {
		}
	}

	@Override
	public void create () {
		texture = new Texture("data/stones.jpg");
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		controller = new CameraController();
		gestureDetector = new GestureDetector(20, 40, 0.5f, 2, 0.15f, controller);
		Gdx.input.setInputProcessor(gestureDetector);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		controller.update();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(texture, 0, 0, texture.getWidth() * 2, texture.getHeight() * 2);
		batch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
	}
}

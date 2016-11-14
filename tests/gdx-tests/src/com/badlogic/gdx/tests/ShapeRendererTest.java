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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.PerspectiveCamController;

public class ShapeRendererTest extends GdxTest {

	ShapeRenderer renderer;
	PerspectiveCamera cam;
	PerspectiveCamController controller;
	SpriteBatch batch;
	BitmapFont font;

	public void create () {
		renderer = new ShapeRenderer();
		cam = new PerspectiveCamera(47, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 2);
		cam.near = 0.1f;
		controller = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(controller);
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		cam.update();
		renderer.setProjectionMatrix(cam.combined);
		renderer.identity();
		renderer.rotate(0, 1, 0, 20);
		renderer.translate(-0.5f, -0.5f, 0);

		MathUtils.random.setSeed(0);

		renderer.begin(ShapeType.Point);

		renderer.setColor(Color.PINK);
		for (int i = 0; i < 100; i++)
			renderer.point(MathUtils.random(0.0f, 1.0f), MathUtils.random(0.0f, 1.0f), 0);

		renderer.end();

		if (Gdx.input.isKeyPressed(Keys.F)) {
			renderer.begin(ShapeType.Filled);

			renderer.setColor(Color.RED);
			renderer.rect(0, 0, 1, 1);

			renderer.setColor(Color.BLUE);
			renderer.circle(0.2f, 0.2f, 0.5f, 40);

			renderer.setColor(Color.WHITE);
			renderer.box(0.1f, 0.1f, 0.1f, 0.3f, 0.25f, 0.1f);

			renderer.setColor(Color.GREEN);
			renderer.cone(0.6f, 0.6f, 0, 0.3f, 0.75f, 20);

			renderer.setColor(Color.MAGENTA);
			renderer.triangle(-0.1f, 0.1f, -0.6f, -0.1f, -0.3f, 0.5f);

			renderer.setColor(Color.GOLD);
			renderer.ellipse(0.7f, -0.1f, 0.3f, 0.1f, 45f, 40);
			renderer.ellipse(0.7f, -0.1f, 0.3f, 0.1f, 135f);

			renderer.end();
		} else {
			renderer.begin(ShapeType.Line);

			renderer.setColor(Color.RED);
			renderer.rect(0, 0, 1, 1);

			renderer.setColor(Color.BLUE);
			renderer.circle(0.2f, 0.2f, 0.5f, 40);

			renderer.setColor(Color.YELLOW);
			renderer.line(0, 0, 1, 1);

			renderer.setColor(Color.WHITE);
			renderer.box(0.1f, 0.1f, 0.1f, 0.3f, 0.25f, 0.1f);

			renderer.setColor(Color.GREEN);
			renderer.cone(0.6f, 0.6f, 0, 0.3f, 0.75f, 20);

			renderer.setColor(Color.MAGENTA);
			renderer.triangle(-0.1f, 0.1f, -0.6f, -0.1f, -0.3f, 0.5f);

			renderer.setColor(Color.CYAN);
			renderer.curve(0.0f, 0.25f, 0.2f, 0.3f, 0.3f, 0.6f, 0.1f, 0.5f, 30);
			
			renderer.setColor(Color.GOLD);
			renderer.ellipse(0.7f, -0.1f, 0.3f, 0.1f, 45f, 40);
			renderer.ellipse(0.7f, -0.1f, 0.3f, 0.1f, 135f);

			renderer.end();
		}

		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		renderer.dispose();
	}
}

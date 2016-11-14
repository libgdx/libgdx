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

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.TimeUtils;

public class IsometricTileTest extends GdxTest {
	static final int LAYERS = 1;
	static final int WIDTH = 4;
	static final int HEIGHT = 5;
	static final int TILES_PER_LAYER = WIDTH * HEIGHT;
	static final int TILE_WIDTH = 54;
	static final int TILE_HEIGHT = 54;
	static final int TILE_HEIGHT_DIAMOND = 28;
	static final int BOUND_X = HEIGHT * TILE_WIDTH / 2 + WIDTH * TILE_WIDTH / 2;
	static final int BOUND_Y = HEIGHT * TILE_HEIGHT_DIAMOND / 2 + WIDTH * TILE_HEIGHT_DIAMOND / 2;

	Texture texture;
	SpriteCache[] caches = new SpriteCache[LAYERS];
	int[] layers = new int[LAYERS];
	OrthographicCamera cam;
	OrthoCamController camController;
	ShapeRenderer renderer;
	long startTime = TimeUtils.nanoTime();

	@Override
	public void create () {
		cam = new OrthographicCamera(480, 320);
		camController = new OrthoCamController(cam);
		Gdx.input.setInputProcessor(camController);

		renderer = new ShapeRenderer();
		texture = new Texture(Gdx.files.internal("data/isotile.png"));

		Random rand = new Random();
		for (int i = 0; i < LAYERS; i++) {
			caches[i] = new SpriteCache();
			SpriteCache cache = caches[i];
			cache.beginCache();

			int colX = HEIGHT * TILE_WIDTH / 2 - TILE_WIDTH / 2;
			int colY = BOUND_Y - TILE_HEIGHT_DIAMOND;
			for (int x = 0; x < WIDTH; x++) {
				for (int y = 0; y < HEIGHT; y++) {
					int tileX = colX - y * TILE_WIDTH / 2;
					int tileY = colY - y * TILE_HEIGHT_DIAMOND / 2;
					cache.add(texture, tileX, tileY, rand.nextInt(2) * 54, 0, TILE_WIDTH, TILE_HEIGHT);
				}
				colX += TILE_WIDTH / 2;
				colY -= TILE_HEIGHT_DIAMOND / 2;
			}

			layers[i] = cache.endCache();
		}
	}

	@Override
	public void dispose () {
		renderer.dispose();
		texture.dispose();
		for (SpriteCache cache : caches)
			cache.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cam.update();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		for (int i = 0; i < LAYERS; i++) {
			SpriteCache cache = caches[i];
			cache.setProjectionMatrix(cam.combined);
			cache.begin();
			cache.draw(layers[i]);
			cache.end();
		}

		renderer.setProjectionMatrix(cam.combined);
		renderer.begin(ShapeType.Line);
		renderer.setColor(1, 0, 0, 1);
		renderer.line(0, 0, 500, 0);
		renderer.line(0, 0, 0, 500);

		renderer.setColor(0, 0, 1, 1);
		renderer.line(0, BOUND_Y, BOUND_X, BOUND_Y);

		renderer.setColor(0, 0, 1, 1);
		renderer.line(BOUND_X, 0, BOUND_X, BOUND_Y);

		renderer.end();
	}
}

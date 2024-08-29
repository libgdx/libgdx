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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.StringBuilder;

@GdxTestConfig
public class SpriteBatchPerformanceTest extends GdxTest {

	private Texture texture;
	private SpriteBatch spriteBatch;
	private WindowedMean counter = new WindowedMean(10000);
	private StringBuilder stringBuilder = new StringBuilder();

	private BitmapFont bitmapFont;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		spriteBatch = new SpriteBatch(8191);
		bitmapFont = new BitmapFont();
	}

	@Override
	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();

		// Accelerate the draws
		for (int j = 0; j < 100; j++) {

			// fill the batch
			for (int i = 0; i < 8190; i++) {
				spriteBatch.draw(texture, 0, 0, 1, 1);
			}

			long beforeFlush = System.nanoTime();

			spriteBatch.flush();
			Gdx.gl.glFlush();
			long afterFlush = System.nanoTime();

			counter.addValue(afterFlush - beforeFlush);

		}

		spriteBatch.end();

		spriteBatch.begin();
		stringBuilder.setLength(0);

		if (counter.hasEnoughData()) {
			stringBuilder.append("Mean Time ms: ");
			stringBuilder.append(counter.getMean() / 1e6);
		} else {
			stringBuilder.append("Please wait, collecting data...");
		}

		bitmapFont.draw(spriteBatch, stringBuilder, 0, 200);
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		spriteBatch.dispose();
	}

}

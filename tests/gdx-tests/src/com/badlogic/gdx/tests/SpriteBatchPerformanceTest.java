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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

@GdxTestConfig
public class SpriteBatchPerformanceTest extends GdxTest {

	private Texture texture;
	private SpriteBatch spriteBatch;
	private StringBuilder stringBuilder = new StringBuilder();

	private BitmapFont bitmapFont;

	private static class PerfTest {
		Mesh.VertexDataType vertexDataType;
		WindowedMean counter = new WindowedMean(10000);

		PerfTest (Mesh.VertexDataType type) {
			this.vertexDataType = type;
		}
	}

	private Array<PerfTest> perfTests = new Array<>();

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		spriteBatch = new SpriteBatch(8191);
		bitmapFont = new BitmapFont();

		if (Gdx.graphics.isGL30Available()) {
			perfTests.add(new PerfTest(Mesh.VertexDataType.VertexBufferObjectWithVAO));

			if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
				perfTests.add(new PerfTest(Mesh.VertexDataType.VertexBufferObject));
			}
		} else {
			perfTests.add(new PerfTest(Mesh.VertexDataType.VertexArray));
			perfTests.add(new PerfTest(Mesh.VertexDataType.VertexBufferObject));
		}

		GLProfiler glProfiler = new GLProfiler(Gdx.graphics);
		glProfiler.setListener(new GLErrorListener() {
			@Override
			public void onError (int error) {
				System.out.println("GLProfiler: error: " + error);
			}
		});
		glProfiler.enable();

	}

	@Override
	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int draws = 100;
		int spritesToFill = 8190;

		for (PerfTest perfTest : perfTests) {
			SpriteBatch.overrideVertexType = perfTest.vertexDataType;

			SpriteBatch testingBatch = new SpriteBatch(8191);
			Gdx.gl.glFlush();
			testingBatch.begin();
			for (int i = 0; i < draws; i++) {
				for (int j = 0; j < spritesToFill; j++) {
					testingBatch.draw(texture, 0, 0, 1, 1);
				}

				long beforeFlush = System.nanoTime();
				testingBatch.flush();
				Gdx.gl.glFlush();
				long afterFlush = System.nanoTime();

				perfTest.counter.addValue(afterFlush - beforeFlush);
			}

			testingBatch.end();
			testingBatch.dispose();
			Gdx.gl.glFlush();
		}

		spriteBatch.begin();

		stringBuilder.setLength(0);
		for (PerfTest perfTest : perfTests) {
			stringBuilder.append("Type: ");
			stringBuilder.append(perfTest.vertexDataType);
			stringBuilder.append("\n");

			if (perfTest.counter.hasEnoughData()) {
				stringBuilder.append("Mean Time ms: ");
				float nanoTimeMean = perfTest.counter.getMean();
				stringBuilder.append(nanoTimeMean / 1e6);
			} else {
				stringBuilder.append("Please wait, collecting data...");
			}
			stringBuilder.append("\n\n");
		}
		bitmapFont.draw(spriteBatch, stringBuilder, 0, 400);

		spriteBatch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		spriteBatch.dispose();
	}

}

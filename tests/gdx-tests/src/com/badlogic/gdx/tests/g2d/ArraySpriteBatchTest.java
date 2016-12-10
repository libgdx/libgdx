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

package com.badlogic.gdx.tests.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ArrayRegionBatch;
import com.badlogic.gdx.graphics.g2d.TextureArrayAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class ArraySpriteBatchTest extends GdxTest {
	 private OrthographicCamera camera;
	 private TextureArrayAtlas atlas;
	 private int regionCount;
	 private Array<TextureArrayAtlas.ArrayAtlasRegion> regionsToDraw;
	 private ArrayRegionBatch batch;
	 private static final int rowCount = 30;
	 private final FPSLogger fpsLogger = new FPSLogger();

	 @Override public void create () {
		  camera = new OrthographicCamera();

		  atlas = new TextureArrayAtlas("data/hugeAtlas/yoba.atlas");

		  regionCount = atlas.getRegions().size;
		  regionsToDraw = new Array<TextureArrayAtlas.ArrayAtlasRegion>(regionCount * rowCount);

		  for (int i = 0; i < rowCount; i++) {
				regionsToDraw.addAll(atlas.getRegions());
		  }

		  // for reproducibility reasons I want to always shuffle regions in the same order
		  MathUtils.random = new RandomXS128(42, 1337);
		  regionsToDraw.shuffle();
		  batch = new ArrayRegionBatch(regionsToDraw.size);
	 }

	 @Override public void dispose () {
		  batch.dispose();
		  atlas.dispose();
	 }

	 @Override public void resize (final int width, final int height) {
		  camera.setToOrtho(false, 1f, (float)height / width);
		  batch.setProjectionMatrix(camera.combined);
	 }

	 @Override public void render () {
		  fpsLogger.log();

		  Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0f);
		  Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		  final float width = 1f / regionCount;

		  batch.begin();
		  for (int i = 0; i < regionsToDraw.size; i++) {
				final TextureArrayAtlas.ArrayAtlasRegion region = regionsToDraw.get(i);

				//noinspection IntegerDivisionInFloatingPointContext
				batch.draw(region, //
					width * (i % regionCount), //
					0.025f * (i / regionCount), //
					width, (float)region.packedHeight / region.packedWidth * width);
		  }
		  batch.end();
	 }
}

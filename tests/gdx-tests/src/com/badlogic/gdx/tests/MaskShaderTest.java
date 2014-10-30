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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.MaskShader;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MaskShaderTest extends GdxTest {

	private Viewport viewport;
	private Batch batch;
	private AssetManager assetManager;
	private Array<Sprite> sprites;

	public void create () {
		viewport = new ExtendViewport(1000, 600);
		batch = new SpriteBatch(1000, new MaskShader());

		assetManager = new AssetManager();
		assetManager.load("data/compressed.atlas", TextureAtlas.class);
	}

	public void render () {
		if (sprites == null) {
			assetManager.finishLoading();

			TextureAtlas atlas = assetManager.get("data/compressed.atlas", TextureAtlas.class);

			sprites = atlas.createSprites();

			for (int i = 0; i < 4; i++) {
				Sprite sprite = sprites.get(i);
				sprite.setX((i - 2.5f) * 200);
				sprite.setY(-250 + i * 70);
			}
		}

		Gdx.gl.glClearColor(1.0f, 0.0f, 1.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		for (Sprite sprite : sprites)
			sprite.draw(batch);
		batch.end();
	}

	public void resize (int width, int height) {
		viewport.update(width, height);
	}

	public void dispose () {
		assetManager.dispose();
		batch.dispose();
	}
}

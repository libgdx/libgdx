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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpriteCacheOffsetTest extends GdxTest implements InputProcessor {
	private int tileMapWidth = 10;
	private int tileMapHeight = 5;
	private int tileSize = 32;
	private SpriteCache cache;
	private Texture texture;

	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		Sprite sprite = new Sprite(texture);
		sprite.setSize(tileSize, tileSize);

		cache = new SpriteCache(1000, false);
		for (int y = 0; y < tileMapHeight; y++) {
			cache.beginCache();
			for (int x = 0; x < tileMapWidth; x++) {
				sprite.setPosition(x * tileSize, y * tileSize);
				cache.add(sprite);
			}
			cache.endCache();
			sprite.rotate90(true);
		}
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cache.begin();
		for (int y = 1; y < tileMapHeight - 1; y++)
			cache.draw(y, 1, tileMapWidth - 2);
		cache.end();
	}

	@Override
	public void dispose () {
		cache.dispose();
		texture.dispose();
	}
}

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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpritePerformanceTest extends GdxTest {
	StringBuilder log = new StringBuilder();
	static final int SPRITES = 500;
	Sprite[] sprites;
	Texture texture;
	SpriteBatch vaBatch;
	SpriteBatch vboBatch;
	SpriteCache cache;
	int spritesHandle;
	float rotation = 0;

	long startTime;
	int frames;

	String[] modes = {"SpriteBatch blended", "SpriteBatch not blended", "SpriteBatch animated blended",
		"SpriteBatch animated not blended", "SpriteBatch VBO blended", "SpriteBatch VBO not blended",
		"SpriteBatch VBO animated blended", "SpriteBatch VBO animated not blended", "SpriteCache blended",
		"SpriteCache not blended"};
	int mode = 0;

	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		vaBatch = new SpriteBatch(1000);
		Mesh.forceVBO = true;
		vboBatch = new SpriteBatch(1000, 1);
		Mesh.forceVBO = false;
		cache = new SpriteCache();

		sprites = new Sprite[SPRITES];
		for (int i = 0; i < SPRITES; i++) {
			int x = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			int y = (int)(Math.random() * (Gdx.graphics.getHeight() - 32));

			sprites[i] = new Sprite(texture);
			sprites[i].setPosition(x, y);
		}

		cache.beginCache();
		for (int i = 0; i < SPRITES; i++) {
			cache.add(sprites[i]);
		}
		int spritesHandle = cache.endCache();

		startTime = System.nanoTime();
		frames = 0;
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		switch (mode) {
		case 0:
			renderSpriteBatch();
			break;
		case 1:
			renderSpriteBatchBlendDisabled();
			break;
		case 2:
			renderSpriteBatchAnimated();
			break;
		case 3:
			renderSpriteBatchAnimatedBlendDisabled();
			break;
		case 4:
			renderSpriteBatchVBO();
			break;
		case 5:
			renderSpriteBatchBlendDisabledVBO();
			break;
		case 6:
			renderSpriteBatchAnimatedVBO();
			break;
		case 7:
			renderSpriteBatchAnimatedBlendDisabledVBO();
			break;
		case 8:
			renderSpriteCache();
			break;
		case 9:
			renderSpriteCacheBlendDisabled();
			break;
		}

		int error = Gdx.gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			Gdx.app.log("SpritePerformanceTest", "gl error: " + error);
		}

		frames++;
		if (System.nanoTime() - startTime > 5000000000l) {
			Gdx.app.log("SpritePerformanceTest", "mode: " + modes[mode] + ", fps: " + frames / 5.0f);
			log.append("mode: " + modes[mode] + ", fps: " + frames / 5.0f + "\n");
			frames = 0;
			startTime = System.nanoTime();
			mode++;
			if (mode > 9) mode = 0;
		}
	}

	void renderSpriteBatch () {
		vaBatch.enableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchBlendDisabled () {
		vaBatch.disableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchAnimated () {
		rotation += 25 * Gdx.graphics.getDeltaTime();
		vaBatch.enableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].setRotation(rotation);
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchAnimatedBlendDisabled () {
		rotation += 25 * Gdx.graphics.getDeltaTime();
		vaBatch.disableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].setRotation(rotation);
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchVBO () {
		vaBatch.enableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchBlendDisabledVBO () {
		vaBatch.disableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchAnimatedVBO () {
		rotation += 25 * Gdx.graphics.getDeltaTime();
		vaBatch.enableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].setRotation(rotation);
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteBatchAnimatedBlendDisabledVBO () {
		rotation += 25 * Gdx.graphics.getDeltaTime();
		vaBatch.disableBlending();
		vaBatch.begin();
		for (int i = 0; i < SPRITES; i++) {
			sprites[i].setRotation(rotation);
			sprites[i].draw(vaBatch);
		}
		vaBatch.end();
	}

	void renderSpriteCache () {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		cache.begin();
		cache.draw(spritesHandle);
		cache.end();
	}

	void renderSpriteCacheBlendDisabled () {
		Gdx.gl.glDisable(GL10.GL_BLEND);
		cache.begin();
		cache.draw(spritesHandle);
		cache.end();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

}

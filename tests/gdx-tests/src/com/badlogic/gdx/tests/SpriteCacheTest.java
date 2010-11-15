/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteCache;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpriteCacheTest extends GdxTest implements InputProcessor {
	int SPRITES = 400 / 2;

	long startTime = System.nanoTime();
	int frames = 0;

	Texture texture;
	Texture texture2;
	SpriteCache spriteCache;
	SpriteCache.Cache normalCache, spritesCache;
	int renderMethod = 0;

	@Override public void render () {
		if (renderMethod == 0) renderNormal();
		;
		if (renderMethod == 1) renderSprites();

		Gdx.input.processEvents(this);
	}

	private void renderNormal () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float begin = 0;
		float end = 0;
		float draw1 = 0;

		long start = System.nanoTime();
		spriteCache.begin();
		begin = (System.nanoTime() - start) / 1000000000.0f;

		start = System.nanoTime();
		spriteCache.draw(normalCache);
		draw1 = (System.nanoTime() - start) / 1000000000.0f;

		start = System.nanoTime();
		spriteCache.end();
		end = (System.nanoTime() - start) / 1000000000.0f;

		if (System.nanoTime() - startTime > 1000000000) {
// Gdx.app.log( "SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1 +
// ", " + draw2 + ", " + drawText + ", " + end );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}

	private void renderSprites () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;

		long start = System.nanoTime();
		spriteCache.begin();
		begin = (System.nanoTime() - start) / 1000000000.0f;

		start = System.nanoTime();
		spriteCache.draw(spritesCache);
		draw1 = (System.nanoTime() - start) / 1000000000.0f;

		start = System.nanoTime();
		spriteCache.end();
		end = (System.nanoTime() - start) / 1000000000.0f;

		if (System.nanoTime() - startTime > 1000000000) {
// Gdx.app.log( "SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1 +
// ", " + draw2 + ", " + drawText + ", " + end );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}

	@Override public void create () {
		spriteCache = new SpriteCache(1000);

		Pixmap pixmap = Gdx.graphics.newPixmap(Gdx.files.getFileHandle("data/badlogicsmall.jpg", FileType.Internal));
		texture = Gdx.graphics.newUnmanagedTexture(32, 32, Format.RGB565, TextureFilter.Linear, TextureFilter.Linear,
			TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.draw(pixmap, 0, 0);
		pixmap.dispose();

		pixmap = Gdx.graphics.newPixmap(32, 32, Format.RGBA8888);
		pixmap.setColor(1, 1, 0, 0.5f);
		pixmap.fill();
		texture2 = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		pixmap.dispose();

		float sprites[] = new float[SPRITES * 6];
		float sprites2[] = new float[SPRITES * 6];
		Sprite[] sprites3 = new Sprite[SPRITES * 2];

		for (int i = 0; i < sprites.length; i += 6) {
			sprites[i] = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			sprites[i + 1] = (int)(Math.random() * (Gdx.graphics.getHeight() - 32));
			sprites[i + 2] = 0;
			sprites[i + 3] = 0;
			sprites[i + 4] = 32;
			sprites[i + 5] = 32;
			sprites2[i] = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			sprites2[i + 1] = (int)(Math.random() * (Gdx.graphics.getHeight() - 32));
			sprites2[i + 2] = 0;
			sprites2[i + 3] = 0;
			sprites2[i + 4] = 32;
			sprites2[i + 5] = 32;
		}

		for (int i = 0; i < SPRITES * 2; i++) {
			int x = (int)(Math.random() * (Gdx.graphics.getWidth() - 32));
			int y = (int)(Math.random() * (Gdx.graphics.getHeight() - 32));

			if (i >= SPRITES)
				sprites3[i] = new Sprite(texture2, 32, 32);
			else
				sprites3[i] = new Sprite(texture, 32, 32);
			sprites3[i].setPosition(x, y);
			sprites3[i].setOrigin(16, 16);
		}

		float scale = 1;
		float angle = 15;

		spriteCache.beginCache();
		for (int i = 0; i < sprites2.length; i += 6)
			spriteCache.add(texture2, sprites2[i], sprites2[i + 1], 16, 16, 32, 32, scale, scale, angle, 0, 0, 32, 32, Color.WHITE,
				false, false);
		for (int i = 0; i < sprites.length; i += 6)
			spriteCache.add(texture, sprites[i], sprites[i + 1], 16, 16, 32, 32, scale, scale, angle, 0, 0, 32, 32, Color.WHITE,
				false, false);
		normalCache = spriteCache.endCache();

		spriteCache.beginCache();
		for (int i = SPRITES; i < SPRITES << 1; i++) {
			sprites3[i].setRotation(angle);
			sprites3[i].setScale(scale);
			spriteCache.add(sprites3[i]);
		}
		for (int i = 0; i < SPRITES; i++) {
			sprites3[i].setRotation(angle);
			sprites3[i].setScale(scale);
			spriteCache.add(sprites3[i]);
		}
		spritesCache = spriteCache.endCache();
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		renderMethod = (renderMethod + 1) % 2;
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}

}

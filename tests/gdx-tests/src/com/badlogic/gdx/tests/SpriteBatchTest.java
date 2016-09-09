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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class SpriteBatchTest extends GdxTest implements InputProcessor {
	int SPRITES = 100 / 2;

	long startTime = TimeUtils.nanoTime();
	int frames = 0;

	Texture texture;
	Texture texture2;
// Font font;
	SpriteBatch spriteBatch;
	float sprites[] = new float[SPRITES * 6];
	float sprites2[] = new float[SPRITES * 6];
	Sprite[] sprites3 = new Sprite[SPRITES * 2];
	float angle = 0;
	float ROTATION_SPEED = 20;
	float scale = 1;
	float SCALE_SPEED = -1;
	int renderMethod = 0;

	@Override
	public void render () {
		if (renderMethod == 0) renderNormal();
		;
		if (renderMethod == 1) renderSprites();
	}

	private void renderNormal () {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;

		angle += ROTATION_SPEED * Gdx.graphics.getDeltaTime();
		scale += SCALE_SPEED * Gdx.graphics.getDeltaTime();
		if (scale < 0.5f) {
			scale = 0.5f;
			SCALE_SPEED = 1;
		}
		if (scale > 1.0f) {
			scale = 1.0f;
			SCALE_SPEED = -1;
		}

		long start = TimeUtils.nanoTime();
		spriteBatch.begin();
		begin = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		for (int i = 0; i < sprites.length; i += 6)
			spriteBatch.draw(texture, sprites[i], sprites[i + 1], 16, 16, 32, 32, scale, scale, angle, 0, 0, 32, 32, false, false);
		draw1 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		for (int i = 0; i < sprites2.length; i += 6)
			spriteBatch
				.draw(texture2, sprites2[i], sprites2[i + 1], 16, 16, 32, 32, scale, scale, angle, 0, 0, 32, 32, false, false);
		draw2 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
// spriteBatch.drawText(font, "Question?", 100, 300, Color.RED);
// spriteBatch.drawText(font, "and another this is a test", 200, 100, Color.WHITE);
// spriteBatch.drawText(font, "all hail and another this is a test", 200, 200, Color.WHITE);
// spriteBatch.drawText(font, "normal fps: " + Gdx.graphics.getFramesPerSecond(), 10, 30, Color.RED);
		drawText = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		spriteBatch.end();
		end = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1
				+ ", " + draw2 + ", " + drawText + ", " + end);
			frames = 0;
			startTime = TimeUtils.nanoTime();
		}
		frames++;

	}

	private void renderSprites () {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;

		long start = TimeUtils.nanoTime();
		spriteBatch.begin();
		begin = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		float angleInc = ROTATION_SPEED * Gdx.graphics.getDeltaTime();
		scale += SCALE_SPEED * Gdx.graphics.getDeltaTime();
		if (scale < 0.5f) {
			scale = 0.5f;
			SCALE_SPEED = 1;
		}
		if (scale > 1.0f) {
			scale = 1.0f;
			SCALE_SPEED = -1;
		}

		start = TimeUtils.nanoTime();
		for (int i = 0; i < SPRITES; i++) {
			if (angleInc != 0) sprites3[i].rotate(angleInc); // this is aids
			if (scale != 1) sprites3[i].setScale(scale); // this is aids
			sprites3[i].draw(spriteBatch);
		}
		draw1 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		for (int i = SPRITES; i < SPRITES << 1; i++) {
			if (angleInc != 0) sprites3[i].rotate(angleInc); // this is aids
			if (scale != 1) sprites3[i].setScale(scale); // this is aids
			sprites3[i].draw(spriteBatch);
		}
		draw2 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
// spriteBatch.drawText(font, "Question?", 100, 300, Color.RED);
// spriteBatch.drawText(font, "and another this is a test", 200, 100, Color.WHITE);
// spriteBatch.drawText(font, "all hail and another this is a test", 200, 200, Color.WHITE);
// spriteBatch.drawText(font, "Sprite fps: " + Gdx.graphics.getFramesPerSecond(), 10, 30, Color.RED);
		drawText = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		spriteBatch.end();
		end = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("SpriteBatch", "fps: " + frames + ", render calls: " + spriteBatch.renderCalls + ", " + begin + ", " + draw1
				+ ", " + draw2 + ", " + drawText + ", " + end);
			frames = 0;
			startTime = TimeUtils.nanoTime();
		}
		frames++;
	}

	@Override
	public void create () {
		spriteBatch = new SpriteBatch(1000);

		Pixmap pixmap = new Pixmap(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture = new Texture(32, 32, Format.RGB565);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		texture.draw(pixmap, 0, 0);
		pixmap.dispose();

		pixmap = new Pixmap(32, 32, Format.RGBA8888);
		pixmap.setColor(1, 1, 0, 0.5f);
		pixmap.fill();
		texture2 = new Texture(pixmap);
		pixmap.dispose();

// font = Gdx.graphics.newFont("Arial", 32, FontStyle.Plain);

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

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void resize (int width, int height) {
		Gdx.app.log("SpriteBatchTest", "resized: " + width + ", " + height);
	}

	@Override
	public boolean keyDown (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int newParam) {
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		renderMethod = (renderMethod + 1) % 2;
		return false;
	}

	@Override
	public boolean mouseMoved (int x, int y) {
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}

}

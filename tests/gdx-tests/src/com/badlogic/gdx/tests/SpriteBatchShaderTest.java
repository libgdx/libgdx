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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class SpriteBatchShaderTest extends GdxTest {
	int SPRITES = 400;

	long startTime = TimeUtils.nanoTime();
	int frames = 0;

	Texture texture;
	Texture texture2;
// Font font;
	SpriteBatch spriteBatch;
	int coords[] = new int[SPRITES * 2];
	int coords2[] = new int[SPRITES * 2];

	Color col = new Color(1, 1, 1, 0.6f);

	Mesh mesh;
	float vertices[] = new float[SPRITES * 6 * (2 + 2 + 4)];

	@Override
	public void render () {
		GL20 gl = Gdx.gl20;
		gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float begin = 0;
		float end = 0;
		float draw1 = 0;
		float draw2 = 0;
		float drawText = 0;

		long start = TimeUtils.nanoTime();
		spriteBatch.begin();
		begin = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		int len = coords.length;
		start = TimeUtils.nanoTime();
		for (int i = 0; i < len; i += 2)
			spriteBatch.draw(texture, coords[i], coords[i + 1], 0, 0, 32, 32);
		draw1 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
		spriteBatch.setColor(col);
		for (int i = 0; i < coords2.length; i += 2)
			spriteBatch.draw(texture2, coords2[i], coords2[i + 1], 0, 0, 32, 32);
		draw2 = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		start = TimeUtils.nanoTime();
// spriteBatch.drawText(font, "Question?", 100, 300, Color.RED);
// spriteBatch.drawText(font, "and another this is a test", 200, 100, Color.WHITE);
// spriteBatch.drawText(font, "all hail and another this is a test", 200, 200, Color.WHITE);
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
		spriteBatch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));

		Pixmap pixmap = new Pixmap(32, 32, Format.RGB565);
		pixmap.setColor(1, 1, 0, 0.7f);
		pixmap.fill();
		texture2 = new Texture(pixmap);
		pixmap.dispose();

		for (int i = 0; i < coords.length; i += 2) {
			coords[i] = (int)(Math.random() * Gdx.graphics.getWidth());
			coords[i + 1] = (int)(Math.random() * Gdx.graphics.getHeight());
			coords2[i] = (int)(Math.random() * Gdx.graphics.getWidth());
			coords2[i + 1] = (int)(Math.random() * Gdx.graphics.getHeight());
		}
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		texture.dispose();
		texture2.dispose();
	}
}

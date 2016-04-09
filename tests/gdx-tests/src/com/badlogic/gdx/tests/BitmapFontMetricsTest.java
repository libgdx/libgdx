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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontMetricsTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private TextureAtlas atlas;
	private BitmapFont font, smallFont;
	private ShapeRenderer renderer;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		atlas = new TextureAtlas("data/pack");
		smallFont = new BitmapFont();
		font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), atlas.findRegion("verdana39"), false);
		font = new BitmapFont(Gdx.files.internal("data/arial-32-pad.fnt"), false);
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
	}

	@Override
	public void render () {
		// red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		int viewHeight = Gdx.graphics.getHeight();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

		// String text = "Sphinx of black quartz, judge my vow.";
		String text = "Sphinx of black quartz.";
		font.setColor(Color.RED);

		float x = 100, y = 100;
		float alignmentWidth;

		smallFont.setColor(Color.BLACK);
		smallFont.draw(spriteBatch, "draw position", 20, viewHeight - 0);
		smallFont.setColor(Color.BLUE);
		smallFont.draw(spriteBatch, "bounds", 20, viewHeight - 20);
		smallFont.setColor(Color.MAGENTA);
		smallFont.draw(spriteBatch, "baseline", 20, viewHeight - 40);
		smallFont.setColor(Color.GREEN);
		smallFont.draw(spriteBatch, "x height", 20, viewHeight - 60);
		smallFont.setColor(Color.CYAN);
		smallFont.draw(spriteBatch, "ascent", 20, viewHeight - 80);
		smallFont.setColor(Color.RED);
		smallFont.draw(spriteBatch, "descent", 20, viewHeight - 100);
		smallFont.setColor(Color.ORANGE);
		smallFont.draw(spriteBatch, "line height", 20, viewHeight - 120);
		smallFont.setColor(Color.LIGHT_GRAY);
		smallFont.draw(spriteBatch, "cap height", 20, viewHeight - 140);

		font.setColor(Color.BLACK);
		GlyphLayout layout = font.draw(spriteBatch, text, x, y);

		spriteBatch.end();

		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.rect(x - 3, y - 3, 6, 6);
		renderer.end();

		float baseline = y - font.getCapHeight();
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.LIGHT_GRAY);
		renderer.line(0, y, 9999, y);
		renderer.setColor(Color.MAGENTA);
		renderer.line(0, baseline, 9999, baseline);
		renderer.setColor(Color.GREEN);
		renderer.line(0, baseline + font.getXHeight(), 9999, baseline + font.getXHeight());
		renderer.setColor(Color.CYAN);
		renderer.line(0, y + font.getAscent(), 9999, y + font.getAscent());
		renderer.setColor(Color.RED);
		renderer.line(0, baseline + font.getDescent(), 9999, baseline + font.getDescent());
		renderer.setColor(Color.ORANGE);
		renderer.line(0, y - font.getLineHeight(), 9999, y - font.getLineHeight());
		renderer.end();

		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLUE);
		renderer.rect(x, y, layout.width, -layout.height);
		renderer.end();
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		renderer.dispose();
		font.dispose();
		atlas.dispose();
	}
}

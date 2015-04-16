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
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BitmapFontTest extends GdxTest {
	private Stage stage;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private ShapeRenderer renderer;
	private BitmapFont multiPageFont;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), false);

		multiPageFont = new BitmapFont(Gdx.files.internal("data/multipagefont.fnt"));

		// Add user defined color
		Colors.put("PERU", Color.valueOf("CD853F"));

		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());

		stage = new Stage(new ScreenViewport());

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		BitmapFont labelFont = skin.get("default-font", BitmapFont.class);
		labelFont.getData().markupEnabled = true;

		// Notice that the last [] has been deliberately added to test the effect of excessive pop operations.
		// They are silently ignored, as expected.
		Label label = new Label("<<[BLUE]M[RED]u[YELLOW]l[GREEN]t[OLIVE]ic[]o[]l[]o[]r[]*[MAROON]Label[][] [Unknown Color]>>", skin);

		label.setPosition(100, 200);
		stage.addActor(label);

		Window window = new Window("[RED]Multicolor[GREEN] Title", skin);
		window.setPosition(400, 200);
		window.pack();
		stage.addActor(window);
	}

	@Override
	public void render () {
		// red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		int viewHeight = Gdx.graphics.getHeight();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

		String text = "Sphinx of black quartz, judge my vow.";
		font.setColor(Color.RED);

		float x = 100, y = 20;
		float alignmentWidth;

		if (false) {
			alignmentWidth = 0;
			font.draw(spriteBatch, text, x, viewHeight - y, alignmentWidth, Align.right, false);
		}

		if (true) {
			alignmentWidth = 280;
			font.draw(spriteBatch, text, x, viewHeight - y, alignmentWidth, Align.right, true);
		}

		font.draw(spriteBatch, "[", 50, 60, 100, Align.left, true);
		font.getData().markupEnabled = true;
		font.draw(spriteBatch, "[", 100, 60, 100, Align.left, true);
		font.getData().markupEnabled = false;

		// 'R' and 'p' are in different pages
		String txt2 = "this font uses " + multiPageFont.getRegions().size + " texture pages: RpRpRpRpRpNM";
		spriteBatch.renderCalls = 0;

		// regular draw function
		multiPageFont.setColor(Color.BLUE);
		multiPageFont.draw(spriteBatch, txt2, 10, 100);

		// expert usage.. drawing with bitmap font cache
		BitmapFontCache cache = multiPageFont.getCache();
		cache.clear();
		cache.setColor(Color.BLACK);
		cache.setText(txt2, 10, 50);
		cache.setColors(Color.PINK, 3, 6);
		cache.setColors(Color.ORANGE, 9, 12);
		cache.setColors(Color.GREEN, 16, txt2.length());
		cache.draw(spriteBatch, 5, txt2.length() - 5);

		cache.clear();
		cache.setColor(Color.BLACK);
		float textX = 10;
		textX += cache.setText("[black] ", textX, 150).width;
		multiPageFont.getData().markupEnabled = true;
		textX += cache.addText("[[[PINK]pink[]] ", textX, 150).width;
		textX += cache.addText("[PERU][[peru] ", textX, 150).width;
		cache.setColor(Color.GREEN);
		textX += cache.addText("green ", textX, 150).width;
		textX += cache.addText("[#A52A2A]br[#A52A2ADF]ow[#A52A2ABF]n f[#A52A2A9F]ad[#A52A2A7F]in[#A52A2A5F]g o[#A52A2A3F]ut ",
			textX, 150).width;
		multiPageFont.getData().markupEnabled = false;

		cache.draw(spriteBatch);

		// tinting
		cache.tint(new Color(1f, 1f, 1f, 0.3f));
		cache.translate(0f, 40f);
		cache.draw(spriteBatch);

		spriteBatch.end();
		// System.out.println(spriteBatch.renderCalls);

		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.rect(x, viewHeight - y - 200, alignmentWidth, 200);
		renderer.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
		stage.getViewport().update(width, height);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		renderer.dispose();
		font.dispose();

		// Restore predefined colors
		Colors.reset();
	}
}

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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BitmapFontUnicodeTest extends GdxTest {
	private Stage stage;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private ShapeRenderer renderer;
	private GlyphLayout layout;
	private Label label;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		// font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), false);
		font = new BitmapFont(Gdx.files.internal("data/noto-sans-symbol2.fnt"), false);
		// Add user defined color
		Colors.put("PERU", Color.valueOf("CD853F"));
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
		stage = new Stage(new ScreenViewport());

		Skin skin = new Skin(Gdx.files.internal("data/noto-sans-symbol2.json"));

		BitmapFont labelFont = skin.get("noto-sans-symbol2", BitmapFont.class);

		// Notice that the last [] has been deliberately added to test the effect of excessive pop operations.
		// They are silently ignored, as expected.
		label = new Label("✓⌚✕\uD83D\uDEE0▶▼", skin);
		label.setPosition(100, 200);
		stage.addActor(label);

		Window window = new Window("\uD83D\uDEE0✓⌚✕▶▼", skin);
		window.setPosition(400, 300);
		window.pack();
		stage.addActor(window);

		layout = new GlyphLayout();
	}

	@Override
	public void render () {
		// red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		int viewHeight = Gdx.graphics.getHeight();

		ScreenUtils.clear(0, 0, 0, 1);

		// Test wrapping or truncation with the font directly.
		if (true) {
			// BitmapFont font = label.getStyle().font;
			BitmapFont font = this.font;
			font.getData().markupEnabled = true;
			font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

			font.getData().setScale(2f);
			renderer.begin(ShapeType.Line);
			renderer.setColor(0, 1, 0, 1);
			float w = Gdx.input.getX() - 10;
			// w = 855;
			renderer.rect(10, 10, w, 500);
			renderer.end();

			spriteBatch.begin();
			String text = "✓⌚✕\uD83D\uDEE0▶▼";
			if (true) { // Test wrap.
				layout.setText(font, text, 0, text.length(), font.getColor(), w, Align.center, true, null);
			} else { // Test truncation.
				layout.setText(font, text, 0, text.length(), font.getColor(), w, Align.center, false, "...");
			}
			float meowy = (500 / 2 + layout.height / 2 + 5);
			font.draw(spriteBatch, layout, 10, 10 + meowy);
			spriteBatch.end();

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
			renderer.begin(ShapeType.Line);
			float c = 0.8f;

			// GlyphLayout bounds
			if (true) {
				renderer.setColor(c, c, c, 1);
				renderer.rect(10 + 0.5f * (w - layout.width), 10 + meowy, layout.width, -layout.height);
			}
			// GlyphRun bounds
			for (int i = 0, n = layout.runs.size; i < n; i++) {
				if (i % 3 == 0)
					renderer.setColor(c, 0, c, 1);
				else if (i % 2 == 0)
					renderer.setColor(0, c, c, 1);
				else
					renderer.setColor(c, c, 0, 1);
				GlyphRun r = layout.runs.get(i);
				renderer.rect(10 + r.x, 10 + meowy + r.y, r.width, -font.getLineHeight());
			}
			renderer.end();
			font.getData().setScale(1f);
			return;
		}

		// Test wrapping with label.
		if (false) {
			label.debug();
			label.getStyle().font = font;
			label.setStyle(label.getStyle());
			label.setText("✓⌚✕\uD83D\uDEE0▶▼");
			label.setWrap(true);
// label.setEllipsis(true);
			label.setAlignment(Align.center, Align.right);
			label.setWidth(Gdx.input.getX() - label.getX());
			label.setHeight(label.getPrefHeight());
		} else {
			// Test various font features.
			spriteBatch.begin();

			String text = "✓⌚✕\uD83D\uDEE0▶▼";
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
			spriteBatch.end();
			// System.out.println(spriteBatch.renderCalls);

			renderer.begin(ShapeType.Line);
			renderer.setColor(Color.BLACK);
			renderer.rect(x, viewHeight - y - 200, alignmentWidth, 200);
			renderer.end();
		}

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
		stage.getViewport().update(width, height, true);
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

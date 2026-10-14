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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BitmapFontUnicodeTest extends GdxTest {
	private static final String TEXT = "✓⌚✕\uD83D\uDEE0▶▼";

	private Stage stage;
	private Skin skin;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private ShapeRenderer renderer;
	private GlyphLayout layout;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/noto-sans-symbol2.fnt"), false);
		font.getData().setScale(2f);
		font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
		layout = new GlyphLayout();

		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("data/noto-sans-symbol2.json"));
		Label label = new Label(TEXT, skin);
		label.setPosition(100, 200);
		stage.addActor(label);

		Window window = new Window(TEXT, skin);
		window.setPosition(400, 300);
		window.pack();
		stage.addActor(window);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);

		float width = Math.max(1, Gdx.input.getX() - 10);
		layout.setText(font, TEXT, 0, TEXT.length(), Color.WHITE, width, Align.center, true, null);

		renderer.begin(ShapeType.Line);
		renderer.setColor(0, 1, 0, 1);
		renderer.rect(10, 10, width, 150);
		renderer.end();

		spriteBatch.begin();
		font.draw(spriteBatch, layout, 10, 10 + (150 + layout.height) / 2);
		spriteBatch.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
		spriteBatch.dispose();
		renderer.dispose();
		font.dispose();
	}
}

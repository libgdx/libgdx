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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;

/** Shows how to align single line, wrapped, and multi line text within a rectangle. */
public class BitmapFontAlignmentTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private Texture texture;
	private BitmapFont font;
	private BitmapFontCache cache;
	private Sprite logoSprite;
	int renderMode;
	GlyphLayout layout;

	@Override
	public void create () {
		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer, int newParam) {
				renderMode = (renderMode + 1) % 6;
				return false;
			}
		});

		spriteBatch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		logoSprite = new Sprite(texture);
		logoSprite.setColor(1, 1, 1, 0.6f);
		logoSprite.setBounds(50, 100, 400, 100);

		font = new BitmapFont(Gdx.files.getFileHandle("data/verdana39.fnt", FileType.Internal), Gdx.files.getFileHandle(
			"data/verdana39.png", FileType.Internal), false);
		cache = font.newFontCache();
		layout = new GlyphLayout();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.7f, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		logoSprite.draw(spriteBatch);
		switch (renderMode) {
		case 0:
			renderSingleLine();
			break;
		case 1:
			renderSingleLineCached();
			break;
		case 2:
			renderWrapped();
			break;
		case 3:
			renderWrappedCached();
			break;
		case 4:
			renderMultiLine();
			break;
		case 5:
			renderMultiLineCached();
			break;
		}
		spriteBatch.end();
	}

	private void renderSingleLine () {
		String text = "Single Line";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		layout.setText(font, text);
		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;

		font.draw(spriteBatch, text, x, y);
	}

	private void renderSingleLineCached () {
		String text = "Single Line Cached";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		GlyphLayout layout = cache.setText(text, 0, 0);
		cache.setColors(Color.BLUE, 1, 4);

		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	private void renderWrapped () {
		String text = "Wrapped Wrapped Wrapped Wrapped";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		layout.setText(font, text, Color.WHITE, width, Align.left, true);
		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;

		font.draw(spriteBatch, text, x, y, width, Align.left, true);

		// More efficient to draw the layout used for bounds:
		// font.draw(spriteBatch, layout, x, y);

		// Note that wrapped text can be aligned:
		// font.draw(spriteBatch, text, x, y, width, Align.center, true);
	}

	private void renderWrappedCached () {
		String text = "Wrapped Cached Wrapped Cached";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		GlyphLayout layout = cache.setText(text, 0, 0, width, Align.left, true);

		// Note that wrapped text can be aligned:
		// cache.setWrappedText(text, 0, 0, width, HAlignment.CENTER);

		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	private void renderMultiLine () {
		String text = "Multi\nLine";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		layout.setText(font, text);
		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;

		font.draw(spriteBatch, text, x, y);

		// Note that multi line text can be aligned:
		// font.draw(spriteBatch, text, x, y, width, Align.center, false);
	}

	private void renderMultiLineCached () {
		String text = "Multi Line\nCached";
		int lines = 2;
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		GlyphLayout layout = cache.setText(text, 0, 0);

		// Note that multi line text can be aligned:
		// cache.setText(text, 0, 0, width, Align.center, false);

		x += width / 2 - layout.width / 2;
		y += height / 2 + layout.height / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		font.dispose();
		texture.dispose();
	}
}

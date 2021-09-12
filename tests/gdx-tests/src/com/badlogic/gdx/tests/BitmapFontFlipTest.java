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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;

public class BitmapFontFlipTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private Texture texture;
	private BitmapFont font;
	private Sprite logoSprite;
	private Color red = new Color(1, 0, 0, 0);
	private BitmapFontCache cache1, cache2, cache3, cache4, cache5;
	private BitmapFontCache cacheScaled1, cacheScaled2, cacheScaled3, cacheScaled4, cacheScaled5;
	int renderMode;

	@Override
	public void create () {
		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer, int newParam) {
				renderMode = (renderMode + 1) % 4;
				return false;
			}
		});

		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1));

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		logoSprite = new Sprite(texture);
		logoSprite.flip(false, true);
		logoSprite.setPosition(0, 320 - 256);
		logoSprite.setColor(1, 1, 1, 0.5f);

		font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), Gdx.files.internal("data/verdana39.png"), true);

		cache1 = font.newFontCache();
		cache2 = font.newFontCache();
		cache3 = font.newFontCache();
		cache4 = font.newFontCache();
		cache5 = font.newFontCache();
		createCaches("cached", cache1, cache2, cache3, cache4, cache5);

		font.getData().setScale(1.33f);
		cacheScaled1 = font.newFontCache();
		cacheScaled2 = font.newFontCache();
		cacheScaled3 = font.newFontCache();
		cacheScaled4 = font.newFontCache();
		cacheScaled5 = font.newFontCache();
		createCaches("cache scaled", cacheScaled1, cacheScaled2, cacheScaled3, cacheScaled4, cacheScaled5);
	}

	private void createCaches (String type, BitmapFontCache cache1, BitmapFontCache cache2, BitmapFontCache cache3,
		BitmapFontCache cache4, BitmapFontCache cache5) {
		cache1.setText("(" + type + ")", 10, 320 - 66);

		String text = "Sphinx of black quartz,\njudge my vow.";
		cache2.setColor(Color.RED);
		cache2.setText(text, 5, 320 - 300);

		text = "How quickly\ndaft jumping zebras vex.";
		cache3.setColor(Color.BLUE);
		cache3.setText(text, 5, 320 - 200, 470, Align.center, false);

		text = "Kerning: LYA moo";
		cache4.setText(text, 210, 320 - 66, 0, text.length() - 3, 0, Align.left, false);

		text = "Forsaking monastic tradition, twelve jovial friars gave\nup their vocation for a questionable existence on the flying trapeze.";
		cache5.setColor(red);
		cache5.setText(text, 0, 320 - 300, 480, Align.center, false);
	}

	@Override
	public void render () {
		red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		logoSprite.draw(spriteBatch);
		switch (renderMode) {
		case 0:
			font.getData().setScale(1);
			renderNormal("normal");
			break;
		case 1:
			font.getData().setScale(1);
			renderCached();
			break;
		case 2:
			font.getData().setScale(red.a + 0.5f);
			renderNormal("normal scaled");
			break;
		case 3:
			font.getData().setScale(1);
			renderCachedScaled();
			break;
		}
		spriteBatch.end();
	}

	private void renderNormal (String type) {
		String text = "Forsaking monastic tradition, twelve jovial friars gave\nup their vocation for a questionable existence on the flying trapeze.";
		font.setColor(red);
		font.draw(spriteBatch, text, 0, 320 - 300, 480, Align.center, false);

		font.setColor(Color.WHITE);
		font.draw(spriteBatch, "(" + type + ")", 10, 320 - 66);

		if (red.a > 0.6f) return;

		text = "Sphinx of black quartz,\njudge my vow.";
		font.setColor(Color.RED);
		font.draw(spriteBatch, text, 5, 320 - 300);

		text = "How quickly\ndaft jumping zebras vex.";
		font.setColor(Color.BLUE);
		font.draw(spriteBatch, text, 5, 320 - 200, 470, Align.right, false);

		text = "Kerning: LYA moo";
		font.setColor(Color.WHITE);
		font.draw(spriteBatch, text, 210, 320 - 66, 0, text.length() - 3, 0, Align.left, false);
	}

	private void renderCached () {
		cache5.setColors(red);
		cache5.draw(spriteBatch);

		cache1.draw(spriteBatch);

		if (red.a > 0.6f) return;

		cache2.draw(spriteBatch);
		cache3.draw(spriteBatch);
		cache4.draw(spriteBatch);
	}

	private void renderCachedScaled () {
		cacheScaled5.setColors(red);
		cacheScaled5.draw(spriteBatch);

		cacheScaled1.draw(spriteBatch);

		if (red.a > 0.6f) return;

		cacheScaled2.draw(spriteBatch);
		cacheScaled3.draw(spriteBatch);
		cacheScaled4.draw(spriteBatch);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		font.dispose();
		texture.dispose();
	}
}

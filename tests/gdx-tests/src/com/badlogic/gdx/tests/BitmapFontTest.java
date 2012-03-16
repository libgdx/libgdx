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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private ShapeRenderer renderer;
	
	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		TextureAtlas textureAtlas = new TextureAtlas("data/pack");
		font = new BitmapFont(Gdx.files.internal("data/verdana39.fnt"), textureAtlas.findRegion("verdana39"), false);
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
	}

	@Override
	public void render () {
		// red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		int viewHeight = Gdx.graphics.getHeight();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

		String text = "Sphinx of black quartz, judge my vow.";
		font.setColor(Color.RED);

		float x = 100, y = 20;
		float alignmentWidth;

		if (false) {
			alignmentWidth = 0;
			font.drawMultiLine(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		if (false) {
			TextBounds bounds = font.getMultiLineBounds(text);
			alignmentWidth = bounds.width;
			font.drawMultiLine(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		if (true) {
			alignmentWidth = 280;
			// font.drawMultiLine(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
			font.drawWrapped(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		spriteBatch.end();

		renderer.begin(ShapeType.Rectangle);
		renderer.rect(x, viewHeight - y, x + alignmentWidth, 300);
		renderer.end();
	}

	public boolean needsGL20 () {
		return false;
	}
}

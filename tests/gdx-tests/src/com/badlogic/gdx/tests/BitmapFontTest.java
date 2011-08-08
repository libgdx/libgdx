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
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	ImmediateModeRenderer10 renderer;

	@Override public void create () {
		spriteBatch = new SpriteBatch();

		TextureAtlas textureAtlas = new TextureAtlas("data/main");
		font = new BitmapFont(Gdx.files.internal("data/calibri.fnt"), textureAtlas.findRegion("calibri"), false);

		renderer = new ImmediateModeRenderer10();
	}

	@Override public void render () {
		// red.a = (red.a + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

		int viewHeight = Gdx.graphics.getHeight();

		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();

		String text = "Sphinx of black quartz,\njudge my vow.";
		font.setColor(Color.RED);

		float x = 20, y = 20;
		float alignmentWidth;

		if (true) {
			alignmentWidth = 0;
			font.drawMultiLine(spriteBatch, text, 200, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		if (false) {
			TextBounds bounds = font.getMultiLineBounds(text);
			alignmentWidth = bounds.width;
			font.drawMultiLine(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		if (true) {
			alignmentWidth = 280;
			font.drawMultiLine(spriteBatch, text, x, viewHeight - y, alignmentWidth, HAlignment.RIGHT);
		}

		spriteBatch.end();

		drawRect(x, y, x + alignmentWidth, 300);
	}

	public void drawRect (float x1, float y1, float x2, float y2) {
		renderer.begin(GL10.GL_LINE_STRIP);
		renderer.vertex(x1, y1, 0);
		renderer.vertex(x1, y2, 0);
		renderer.vertex(x2, y2, 0);
		renderer.vertex(x2, y1, 0);
		renderer.vertex(x1, y1, 0);
		renderer.end();
	}

	public boolean needsGL20 () {
		return false;
	}
}

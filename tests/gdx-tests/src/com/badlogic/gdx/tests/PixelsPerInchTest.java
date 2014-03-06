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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PixelsPerInchTest extends GdxTest {

	BitmapFont font;
	SpriteBatch batch;
	Texture texture;

	@Override
	public void create () {
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		batch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		float width = (int)(Gdx.graphics.getPpcX() * 2);
		float height = (int)(Gdx.graphics.getPpcY() * 1);
		batch.draw(texture, 10, 100, width, height, 0, 0, 64, 32, false, false);
		font.draw(batch,
			"button is 2x1 cm (" + width + "x" + height + "px), ppi: (" + Gdx.graphics.getPpiX() + "," + Gdx.graphics.getPpiY()
				+ "), ppc: (" + Gdx.graphics.getPpcX() + "," + Gdx.graphics.getPpcY() + ")", 10, 50);
		batch.end();
	}

	@Override
	public void dispose () {
		font.dispose();
		batch.dispose();
		texture.dispose();
	}
}

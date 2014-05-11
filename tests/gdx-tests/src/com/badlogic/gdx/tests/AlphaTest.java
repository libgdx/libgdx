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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class AlphaTest extends GdxTest {
	SpriteBatch batch;
	Texture texture;

	@Override
	public void create () {
		Pixmap pixmap = new Pixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 0, 0, 1);
		pixmap.fill();

		texture = new Texture(pixmap, false);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		batch = new SpriteBatch();
		pixmap.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		
		Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0,  0,  Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int color = pixmap.getPixel(0, pixmap.getHeight() - 1);
		Gdx.app.log("AlphaTest", Integer.toHexString(color));
		pixmap.dispose();
	}

	@Override
	public void dispose () {
		batch.dispose();
		texture.dispose();
	}
}

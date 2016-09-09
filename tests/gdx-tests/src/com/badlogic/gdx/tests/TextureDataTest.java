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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextureDataTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private Texture texture;

	public void create () {
		spriteBatch = new SpriteBatch();
// texture = new Texture(new PixmapTextureData(new Pixmap(Gdx.files.internal("data/t8890.png")), null, false, true));
		texture = new Texture(new FileTextureData(Gdx.files.internal("data/t8890.png"), null, null, false));
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		spriteBatch.draw(texture, 100, 100);
		spriteBatch.end();
	}

	public boolean needsGL20 () {
		return false;
	}
}

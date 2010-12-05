/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AlphaTest extends GdxTest {
	SpriteBatch batch;
	Texture texture;	

	@Override public void create () {
		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(0, 1, 0, 0.7f);
		pixmap.fill();

		texture = Gdx.graphics.newUnmanagedTexture(pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);
		batch = new SpriteBatch();
	}
	
	@Override public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(texture, 0, 0, 256, 256, 0, 0, 256, 256, false, false);
		batch.end();
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}

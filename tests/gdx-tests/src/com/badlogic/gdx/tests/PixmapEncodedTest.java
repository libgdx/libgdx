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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Base64Coder;

public class PixmapEncodedTest extends GdxTest {
	SpriteBatch batch;
	Pixmap pixmap;
	Texture badlogic;

	public void create () {
		batch = new SpriteBatch();

		// Blue rectangle 256x256 px
		String content = "iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAYAAABccqhmAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAALEgAACxIB0t1"
			+ "+/AAAABZ0RVh0Q3JlYXRpb24gVGltZQAxMi8xNy8yMaButcoAAAAYdEVYdFNvZnR3YXJlAEFkb2JlIEZpcmV3b3Jrc0+zH04AAAM"
			+ "dSURBVHic7dQxAQAgDMCwgX/PQwZHEwW9emZ2B0i6vwOAfwwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgw"
			+ "AwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwgwAwh6r+AP+P/K8dAAAAABJRU5ErkJggg==";

		byte[] data = Base64Coder.decode(content);
		pixmap = new Pixmap(data, 0, data.length);
	}

	public void render () {
		if (badlogic == null) {
			if (pixmap.isLoaded()) {
				badlogic = new Texture(pixmap, pixmap.getFormat(), false);
			}
			return;
		}

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(badlogic, 0f, 0f);
		batch.end();
	}
}

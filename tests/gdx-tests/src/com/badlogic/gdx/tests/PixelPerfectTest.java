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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PixelPerfectTest extends GdxTest {
	SpriteBatch batch;
	OrthographicCamera cam;
	Texture tex;

	@Override
	public void create () {
		Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.BLUE);
		pixmap.fill();
		pixmap.setColor(Color.RED);
		pixmap.drawLine(0, 0, 15, 15);
		pixmap.drawLine(0, 15, 15, 0);

		tex = new Texture(pixmap);
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void resize (int width, int height) {
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		batch.draw(tex, 1, 1);
		batch.end();
	}
}

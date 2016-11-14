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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class ETC1Test extends GdxTest {
	OrthographicCamera camera;
	OrthoCamController controller;
	Texture img1;
	Texture img2;
	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create () {
		font = new BitmapFont();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		controller = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(controller);

		Pixmap pixmap = new Pixmap(32, 32, Format.RGB565);
		pixmap.setColor(1, 0, 0, 1);
		pixmap.fill();
		pixmap.setColor(0, 1, 0, 1);
		pixmap.drawLine(0, 0, 32, 32);
		pixmap.drawLine(0, 32, 32, 0);
		ETC1Data encodedImage = ETC1.encodeImagePKM(pixmap);
		pixmap.dispose();
		pixmap = ETC1.decodeImage(encodedImage, Format.RGB565);
		encodedImage.dispose();

		img1 = new Texture(pixmap);
		img2 = new Texture("data/test.etc1");
		batch = new SpriteBatch();
		pixmap.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(img2, -100, 0);
		batch.draw(img1, 0, 0);
		batch.end();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 30);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		img1.dispose();
		img2.dispose();
	}
}

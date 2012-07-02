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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.stbtt.TrueTypeFontFactory;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Experimental stb-truetype font factory. Do not use yet!
 * @author mzechner */
public class TTFFactoryTest extends GdxTest {

	public static final float WORLD_WIDTH = 12.5f;
	public static final float WORLD_HEIGHT = 7.5f;

	private SpriteBatch spriteBatch;
	private OrthographicCamera orthographicCamera;
	private float viewportWidth;
	private float viewportHeight;

	private BitmapFont fontAtlasDroid;

	public static final float FONT_SIZE = 0.5f;
	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";
	public static final String FONT_PATH = "data/DroidSerif-Regular.ttf";
	private String text = "True type font =) Test <3";
	private ShapeRenderer renderer;

	@Override
	public void resize (int width, int height) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		renderer = new ShapeRenderer();

		viewportWidth = Gdx.graphics.getWidth();
		viewportHeight = Gdx.graphics.getHeight();
		Gdx.gl.glViewport(0, 0, (int)viewportWidth, (int)viewportHeight);

		if (fontAtlasDroid != null) {
			fontAtlasDroid.dispose();
		}

		fontAtlasDroid = TrueTypeFontFactory.createBitmapFont(Gdx.files.internal(FONT_PATH), FONT_CHARACTERS, WORLD_WIDTH,
			WORLD_HEIGHT, FONT_SIZE, viewportWidth, viewportHeight);

		fontAtlasDroid.setColor(1f, 0f, 0f, 1f);

		this.orthographicCamera = new OrthographicCamera(viewportWidth, viewportHeight);
		this.orthographicCamera.position.set(viewportWidth / 2f, viewportHeight / 2, 0);

	}

	@Override
	public void create () {

		Gdx.gl.glClearColor(0f, 0f, 0f, 1);

		this.spriteBatch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		orthographicCamera.update();
		spriteBatch.setProjectionMatrix(orthographicCamera.combined);
		spriteBatch.begin();
		spriteBatch.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float fontPosXWorld = 0.5f;
		float fontPosYWorld = WORLD_HEIGHT / 2f;

		float wRatio = Gdx.graphics.getWidth() / WORLD_WIDTH;
		float hRatio = Gdx.graphics.getHeight() / WORLD_HEIGHT;

		int x = (int)(fontPosXWorld * wRatio + 0.5f);
		int y = (int)(fontPosYWorld * hRatio + 0.5f);
		TextBounds bounds = fontAtlasDroid.drawMultiLine(spriteBatch, text, x, y);
		spriteBatch.end();

		renderer.begin(ShapeType.Rectangle);
		renderer.rect(x, y, bounds.width, bounds.height);
		renderer.end();

	}
}
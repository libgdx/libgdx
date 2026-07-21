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

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class FullscreenTest extends GdxTest {
	SpriteBatch batch;
	Texture tex;
	boolean fullscreen = false;
	BitmapFont font;

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		tex = new Texture(files.internal("data/badlogic.jpg"));
		DisplayMode[] modes = graphics.getDisplayModes();
		for (DisplayMode mode : modes) {
			System.out.println(mode);
		}
		app.log("FullscreenTest", graphics.getBufferFormat().toString());
	}

	@Override
	public void resume () {

	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);

		batch.begin();
		batch.setColor(input.getX() < graphics.getSafeInsetLeft()
			|| input.getX() + tex.getWidth() > graphics.getWidth() - graphics.getSafeInsetRight() ? Color.RED
				: Color.WHITE);
		batch.draw(tex, input.getX(), graphics.getHeight() - input.getY());
		font.draw(batch, "" + graphics.getWidth() + ", " + graphics.getHeight(), 0, 20);
		batch.end();

		if (input.justTouched()) {
			if (fullscreen) {
				graphics.setWindowedMode(480, 320);
				batch.getProjectionMatrix().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
				gl.glViewport(0, 0, graphics.getBackBufferWidth(), graphics.getBackBufferHeight());
				fullscreen = false;
			} else {
				DisplayMode m = null;
				for (DisplayMode mode : graphics.getDisplayModes()) {
					if (m == null) {
						m = mode;
					} else {
						if (m.width < mode.width) {
							m = mode;
						}
					}
				}

				graphics.setFullscreenMode(graphics.getDisplayMode());
				batch.getProjectionMatrix().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
				gl.glViewport(0, 0, graphics.getBackBufferWidth(), graphics.getBackBufferHeight());
				fullscreen = true;
			}
		}
	}

	@Override
	public void resize (int width, int height) {
		app.log("FullscreenTest", "resized: " + width + ", " + height);
		app.log("FullscreenTest", "safe insets: " + graphics.getSafeInsetLeft() + "/" + graphics.getSafeInsetRight());
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void pause () {
		app.log("FullscreenTest", "paused");
	}

	@Override
	public void dispose () {
		app.log("FullscreenTest", "disposed");
	}
}

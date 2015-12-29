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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Cycles viewports while rendering with SpriteBatch, also shows how to draw in the black bars. */
public class ViewportTest2 extends GdxTest {
	Array<Viewport> viewports;
	Viewport viewport;
	Array<String> names;
	String name;

	private SpriteBatch batch;
	private Texture texture;
	private BitmapFont font;
	private OrthographicCamera camera;

	public void create () {
		font = new BitmapFont();
		font.setColor(0, 0, 0, 1);

		Pixmap pixmap = new Pixmap(16, 16, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		texture = new Texture(pixmap);

		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.position.set(100, 100, 0);
		camera.update();

		viewports = ViewportTest1.getViewports(camera);
		viewport = viewports.first();

		names = ViewportTest1.getViewportNames();
		name = names.first();

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean keyDown (int keycode) {
				if (keycode == Input.Keys.SPACE) {
					int index = (viewports.indexOf(viewport, true) + 1) % viewports.size;
					name = names.get(index);
					viewport = viewports.get(index);
					resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
				return false;
			}
		});
	}

	public void render () {
		batch.setProjectionMatrix(camera.projection);
		batch.setTransformMatrix(camera.view);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		// draw a white background so we are able to see the black bars
		batch.setColor(1, 1, 1, 1);
		batch.draw(texture, -4096, -4096, 4096, 4096, 8192, 8192, 1, 1, 0, 0, 0, 16, 16, false, false);

		batch.setColor(1, 0, 0, 1);
		batch.draw(texture, 150, 100, 16, 16, 32, 32, 1, 1, 45, 0, 0, 16, 16, false, false);

		font.draw(batch, viewport.getClass().getSimpleName(), 150, 100);
		batch.end();

		if (viewport instanceof ScalingViewport) {
			// This shows how to set the viewport to the whole screen and draw within the black bars.
			ScalingViewport scalingViewport = (ScalingViewport)viewport;
			int screenWidth = Gdx.graphics.getWidth();
			int screenHeight = Gdx.graphics.getHeight();
			HdpiUtils.glViewport(0, 0, screenWidth, screenHeight);
			batch.getProjectionMatrix().idt().setToOrtho2D(0, 0, screenWidth, screenHeight);
			batch.getTransformMatrix().idt();
			batch.begin();
			float leftGutterWidth = scalingViewport.getLeftGutterWidth();
			if (leftGutterWidth > 0) {
				batch.draw(texture, 0, 0, leftGutterWidth, screenHeight);
				batch.draw(texture, scalingViewport.getRightGutterX(), 0, scalingViewport.getRightGutterWidth(), screenHeight);
			}
			float bottomGutterHeight = scalingViewport.getBottomGutterHeight();
			if (bottomGutterHeight > 0) {
				batch.draw(texture, 0, 0, screenWidth, bottomGutterHeight);
				batch.draw(texture, 0, scalingViewport.getTopGutterY(), screenWidth, scalingViewport.getTopGutterHeight());
			}
			batch.end();
			viewport.update(screenWidth, screenHeight, true); // Restore viewport.
		}
	}

	public void resize (int width, int height) {
		System.out.println(name);
		viewport.update(width, height);
	}

	public void dispose () {
		texture.dispose();
		batch.dispose();
	}
}

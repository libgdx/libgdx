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

package com.badlogic.gdx.tests.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.DebugActor;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.DoubleRatioViewport;
import com.badlogic.gdx.utils.viewport.FixedViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StaticViewport;
import com.badlogic.gdx.utils.viewport.StretchedViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This test makes use of the different kind of viewports, while not using a stage, but a Spritebatch and a Camera. It also
 * disables the viewport to draw something in the area of the black bars. */
public class ViewportTest5 extends GdxTest {

	private float delay;

	Array<Viewport> viewports = new Array<Viewport>(4);
	private Viewport viewport;

	private SpriteBatch batch;
	private Texture texture;
	private Texture border;
	private BitmapFont font;

	private OrthographicCamera camera;

	public void create () {
		font = new BitmapFont();
		font.setColor(0, 0, 0, 1);

		Pixmap pixmap = new Pixmap(16, 16, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		texture = new Texture(pixmap);
		border = new Texture("data/walkanim.png");

		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.position.set(100, 100, 0);
		camera.update();

		viewports.add(new StretchedViewport(camera, 300, 200));
		viewports.add(new FixedViewport(camera, 300, 200));
		viewports.add(new ScreenViewport(camera));
		viewports.add(new StaticViewport(camera, 300, 200));
// viewports.add(new DoubleRatioViewport(300, 200, 600, 400));
		viewport = viewports.first();
	}

	public void render () {
		delay -= Gdx.graphics.getDeltaTime();
		// iterate through the viewports
		if (delay <= 0) {
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
				viewport = viewports.get((viewports.indexOf(viewport, true) + 1) % viewports.size);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				delay = 1f;
			}
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.enable();
		batch.setProjectionMatrix(camera.projection);
		batch.setTransformMatrix(camera.view);
		batch.begin();
		// draw a white background so we are able to see the black bars
		batch.setColor(1, 1, 1, 1);
		batch.draw(texture, -4096, -4096, 4096, 4096, 8192, 8192, 1, 1, 0, 0, 0, 16, 16, false, false);
		batch.setColor(1, 0, 0, 1);
		batch.draw(texture, 100, 100, 16, 16, 32, 32, 1, 1, 45, 0, 0, 16, 16, false, false);
		font.draw(batch, viewport.getClass().getSimpleName(), 100, 100);
		batch.end();

		viewport.disable();
		batch.setProjectionMatrix(new Matrix4().idt().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		batch.setTransformMatrix(new Matrix4().idt());
		batch.begin();
		batch.setColor(1, 1, 1, 1);
		// draw the left bar green
		batch.draw(border, 0, 0, viewport.viewportX / 2, Gdx.graphics.getHeight() / 2, viewport.viewportX,
			Gdx.graphics.getHeight(), 1, 1, 0, 0, 0, border.getWidth(), border.getHeight(), true, false);

		// draw the right bar green
		batch.draw(border, Gdx.graphics.getWidth() - viewport.viewportX, 0, viewport.viewportX / 2,
			Gdx.graphics.getHeight() / 2, viewport.viewportX, Gdx.graphics.getHeight(), 1, 1, 0, 0, 0, border.getWidth(),
			border.getHeight(), false, false);

		// draw the top bar green
		batch.draw(border, 0, Gdx.graphics.getHeight() - viewport.viewportY, Gdx.graphics.getWidth() / 2,
			viewport.viewportY / 2, Gdx.graphics.getWidth(), viewport.viewportY, 1, 1, 0, 0, 0, border.getWidth(),
			border.getHeight(), false, false);

		// draw the bottom bar green
		batch.draw(border, 0, 0, Gdx.graphics.getWidth() / 2, viewport.viewportY / 2, Gdx.graphics.getWidth(),
			viewport.viewportY, 1, 1, 0, 0, 0, border.getWidth(), border.getHeight(), false, true);

		batch.end();

	}

	public void resize (int width, int height) {
		viewport.update(width, height);
	}

	public void dispose () {
		texture.dispose();
		batch.dispose();
	}
}

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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.tests.utils.GdxTest;

public class CompassTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	BitmapFont font;
	SpriteBatch batch;
	ImmediateModeRenderer10 renderer;

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();
		renderer = new ImmediateModeRenderer10();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, getOrientationString(), 20, Gdx.graphics.getHeight() - 10);
		batch.end();

		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadIdentity();
		float aspect = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
		Gdx.gl10.glOrthof(-1, 1, -1 * aspect, 1 * aspect, 0, -1);
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
		Gdx.gl10.glLoadIdentity();
		Gdx.gl10.glRotatef(Gdx.input.getAzimuth() + 90, 0, 0, 1);
		renderer.begin(GL10.GL_TRIANGLES);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(-0.3f, -0.5f, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(0.3f, -0.5f, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(0, 0.5f, 0);
		renderer.end();
	}

	@Override
	public void resize (int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
		font.dispose();
	}

	private String getOrientationString () {
		StringBuilder builder = new StringBuilder();
		builder.append("\nazimuth: ");
		builder.append((int)Gdx.input.getAzimuth());
		builder.append("\npitch: ");
		builder.append((int)Gdx.input.getPitch());
		builder.append("\nroll: ");
		builder.append((int)Gdx.input.getRoll());
		return builder.toString();
	}
}

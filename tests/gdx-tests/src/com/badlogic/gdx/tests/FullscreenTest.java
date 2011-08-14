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
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FullscreenTest extends GdxTest {

	Mesh mesh;
	Texture texture;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		mesh = new Mesh(true, 3, 0, new VertexAttribute(Usage.Position, 3, "a_pos"), new VertexAttribute(Usage.TextureCoordinates,
			2, "a_tex"));
		mesh.setVertices(new float[] {-1, -1, 0, 0, 1, 0, 1, 0, 0.5f, 0, 1, -1, 0, 1, 1});

		DisplayMode[] modes = Gdx.graphics.getDisplayModes();
		for (DisplayMode mode : modes) {
			System.out.println(mode);
		}
		Gdx.graphics.setDisplayMode(800, 600, true);
		Gdx.app.log("FullscreenTest", Gdx.graphics.getBufferFormat().toString());
	}

	@Override
	public void resume () {

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor((float)Math.random(), 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		mesh.render(GL10.GL_TRIANGLES);

		if (Gdx.input.justTouched()) {
			Gdx.graphics.setDisplayMode(480, 320, false);
		}
	}

	@Override
	public void resize (int width, int height) {
		Gdx.app.log("FullscreenTest", "resized: " + width + ", " + height);
	}

	@Override
	public void pause () {
		Gdx.app.log("FullscreenTest", "paused");
	}

	@Override
	public void dispose () {
		Gdx.app.log("FullscreenTest", "disposed");
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
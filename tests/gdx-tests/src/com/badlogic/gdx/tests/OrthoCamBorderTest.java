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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tests.utils.GdxTest;

public class OrthoCamBorderTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	static final int WIDTH = 480;
	static final int HEIGHT = 320;
	OrthographicCamera cam;
	Rectangle glViewport;
	Mesh mesh;

	public void create () {
		mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 2, "a_pos"), new VertexAttribute(Usage.Color, 4, "a_col"));
		mesh.setVertices(new float[] {0, 0, 1, 0, 0, 1, WIDTH, 0, 0, 1, 0, 1, WIDTH, HEIGHT, 0, 0, 1, 1, 0, HEIGHT, 1, 0, 1, 1});
		mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});

		cam = new OrthographicCamera();
		cam.setViewport(WIDTH, HEIGHT);
		cam.getPosition().set(WIDTH / 2, HEIGHT / 2, 0);

		glViewport = calculateGLViewport(WIDTH, HEIGHT);
	}

	private Rectangle calculateGLViewport (int desiredWidth, int desiredHeight) {
		Rectangle viewport = new Rectangle();
		if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
			float aspect = (float)Gdx.graphics.getHeight() / HEIGHT;
			viewport.width = WIDTH * aspect;
			viewport.height = Gdx.graphics.getHeight();
			viewport.x = Gdx.graphics.getWidth() / 2 - viewport.width / 2;
			viewport.y = 0;
		} else {
			float aspect = (float)Gdx.graphics.getWidth() / WIDTH;
			viewport.width = Gdx.graphics.getWidth();
			viewport.height = HEIGHT * aspect;
			viewport.x = 0;
			viewport.y = Gdx.graphics.getHeight() / 2 - viewport.height / 2;
		}
		return viewport;
	}

	public void resize (int width, int height) {
		glViewport = calculateGLViewport(WIDTH, HEIGHT);
	}

	public void render () {
		GL10 gl = Gdx.gl10;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glViewport((int)glViewport.x, (int)glViewport.y, (int)glViewport.width, (int)glViewport.height);

		cam.setMatrices();
		mesh.render(GL10.GL_TRIANGLES);
	}
}

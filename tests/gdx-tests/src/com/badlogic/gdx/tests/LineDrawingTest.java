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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class LineDrawingTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	static final int MAX_LINES = 1000;
	OrthographicCamera camera;
	Mesh lineMesh;
	float[] lineVertices;
	int vertexIndex = 0;
	Vector3 unprojectedVertex = new Vector3();

	@Override
	public void create () {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		lineMesh = new Mesh(false, MAX_LINES * 2, 0, new VertexAttribute(Usage.Position, 2, "a_pos"));
		lineVertices = new float[MAX_LINES * 2 * 2];
	}

	@Override
	public void render () {
		// clear screen
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// update the camera (not necessary as we don't change its properties)
		// and apply the projection and modelview matrix.
		camera.update();
		camera.apply(Gdx.gl10);

		// render the lines so far, but only if we have at least 2
		// vertices (== 4 floats, 2 floats per vertex, x/y position
		if (vertexIndex >= 4) lineMesh.render(GL10.GL_LINE_STRIP);

		// if the screen was touched we add a new vertex to our
		// mesh and hence a new line. But we only do that if the
		// last added vertex is further away than 10 units.
		if (Gdx.input.isTouched()) {
			unprojectedVertex.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(unprojectedVertex);

			// add the new vertex only if it's 1) the first vertex or 2)
			// it is farther away than 10 units from the last added vertex.
			if (vertexIndex == 0 || unprojectedVertex.dst(lineVertices[vertexIndex - 2], lineVertices[vertexIndex - 1], 0) > 10) {
				lineVertices[vertexIndex++] = unprojectedVertex.x;
				lineVertices[vertexIndex++] = unprojectedVertex.y;
				lineMesh.setVertices(lineVertices, 0, vertexIndex);
			}
		} else {
			// else we reset the vertexIndex to 0 so that no line is drawn any longer
			// and the user can draw a new line
			vertexIndex = 0;
		}
	}
}
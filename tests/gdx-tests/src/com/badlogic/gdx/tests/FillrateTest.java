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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class FillrateTest extends GdxTest implements InputProcessor {
	Texture texture;
	Mesh mesh;
	int numFills = 1;
	long lastOut = TimeUtils.nanoTime();

	int mode = 0;
	float mean = 0;
	float frames = 0;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		mesh = new Mesh(true, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_pos"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));

		float[] vertices = new float[4 * 4];

		int idx = 0;
		vertices[idx++] = -1;
		vertices[idx++] = -1;
		vertices[idx++] = 0;
		vertices[idx++] = 0;

		vertices[idx++] = -1;
		vertices[idx++] = 1;
		vertices[idx++] = 0;
		vertices[idx++] = 1;

		vertices[idx++] = 1;
		vertices[idx++] = 1;
		vertices[idx++] = 1;
		vertices[idx++] = 1;

		vertices[idx++] = 1;
		vertices[idx++] = -1;
		vertices[idx++] = 1;
		vertices[idx++] = 0;

		short[] indices = {0, 1, 2, 2, 3, 0};
		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (mode == 3) {
			Gdx.graphics.getGL10().glDisable(GL10.GL_BLEND);
			Gdx.graphics.getGL10().glEnable(GL10.GL_ALPHA_TEST);
		}

		if (mode == 2) {
			Gdx.graphics.getGL10().glEnable(GL10.GL_BLEND);
			Gdx.graphics.getGL10().glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}

		if (mode >= 1) {
			Gdx.graphics.getGL10().glEnable(GL10.GL_TEXTURE_2D);
			texture.bind();
		}

		if (mode == 0) {
			Gdx.graphics.getGL10().glDisable(GL10.GL_BLEND);
			Gdx.graphics.getGL10().glDisable(GL10.GL_ALPHA_TEST);
			Gdx.graphics.getGL10().glDisable(GL10.GL_TEXTURE_2D);
		}

		Gdx.graphics.getGL10().glColor4f(1, 1, 1, 0.01f);

		for (int i = 0; i < numFills; i++)
			mesh.render(GL10.GL_TRIANGLES);

		mean += numFills;
		frames++;

		if (Gdx.graphics.getDeltaTime() < 1 / 60f) numFills++;

		if (TimeUtils.nanoTime() - lastOut >= 1000000000) {
			Gdx.app.log("FillrateTest", "fills: " + mean / frames + ", fps: " + frames + ", mode" + mode);
			mean = 0;
			frames = 0;
			lastOut = TimeUtils.nanoTime();
			if (Gdx.graphics.getFramesPerSecond() < 60) numFills--;
		}
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		mode++;
		if (mode > 3) mode = 0;
		numFills = 0;
		return false;
	}

	@Override
	public void dispose () {
		mesh.dispose();
		texture.dispose();
	}
}

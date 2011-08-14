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
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MeshMultitextureTest extends GdxTest {
	Texture tex1;
	Texture tex2;
	Mesh mesh;

	@Override
	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		gl.glActiveTexture(GL10.GL_TEXTURE0);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		tex1.bind();

		gl.glActiveTexture(GL10.GL_TEXTURE1);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		tex2.bind();
		Gdx.gl11.glTexEnvi(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
		Gdx.gl11.glTexEnvi(GL10.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_ADD);

		mesh.render(GL10.GL_TRIANGLES);
	}

	@Override
	public void create () {
		mesh = new Mesh(true, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords1"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords2"));

		mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 0, 1, 0, 1, 0.5f, -0.5f, 0, 1, 1, 1, 1, 0.5f, 0.5f, 0, 1, 0, 1, 0, -0.5f,
			0.5f, 0, 0, 0, 0, 0});
		mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});

		tex1 = new Texture(Gdx.files.internal("data/planet_earth.png"));
		tex2 = new Texture(Gdx.files.internal("data/planet_heavyclouds.jpg"));
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

}

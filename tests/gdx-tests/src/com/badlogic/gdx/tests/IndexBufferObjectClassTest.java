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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IndexBufferObjectClassTest extends GdxTest {
	Texture texture;
	VertexBufferObject vbo;
	IndexBufferObject ibo;

	@Override public boolean needsGL20 () {
		return false;
	}

	@Override public void dispose () {
		texture.dispose();
		vbo.dispose();
		ibo.dispose();
	}

	@Override public void render () {
		GL11 gl = Gdx.gl11;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		vbo.bind();
		ibo.bind();
		gl.glDrawElements(GL11.GL_TRIANGLES, 3, GL11.GL_UNSIGNED_SHORT, 0);
		ibo.unbind();
		vbo.unbind();
	}

	@Override public void create () {
		vbo = new VertexBufferObject(true, 3, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TexCoords"), new VertexAttribute(
				VertexAttributes.Usage.ColorPacked, 4, "a_Color"));
		float[] vertices = new float[] {-1, -1, 0, 0, Color.toFloatBits(1f, 0f, 0f, 1f), 0, 1, 0.5f, 1.0f,
			Color.toFloatBits(0f, 1f, 0f, 1f), 1, -1, 1, 0, Color.toFloatBits(0f, 0f, 1f, 1f)};
		vbo.setVertices(vertices, 0, vertices.length);

		ibo = new IndexBufferObject(true, 3);
		ibo.setIndices(new short[] {0, 1, 2}, 0, 3);

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
	}

	@Override public void resume () {
		vbo.invalidate();
		ibo.invalidate();
	}
}

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

import java.nio.ShortBuffer;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;

public class VertexArrayClassTest extends GdxTest {
	Texture texture;
	VertexArray va;
	ShortBuffer indices;

	@Override public boolean needsGL20 () {
		return false;
	}

	@Override public void dispose () {
		texture.dispose();
		va.dispose();
	}

	@Override public void render () {
		GL10 gl = Gdx.gl10;
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		va.bind();
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glTranslatef(0.5f, 0.5f, 0);
		gl.glDrawElements(GL10.GL_TRIANGLES, 3, GL10.GL_UNSIGNED_SHORT, indices);
		gl.glPopMatrix();
		va.unbind();
	}

	@Override public void create () {
		va = new VertexArray(3, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_TexCoords"), new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4,
			"a_Color"));
		float[] vertices = new float[] {-1, -1, 0, 0, Color.toFloatBits(1f, 0f, 0f, 1f), 0, 1, 0.5f, 1.0f,
			Color.toFloatBits(0f, 1f, 0f, 1f), 1, -1, 1, 0, Color.toFloatBits(0f, 0f, 1f, 1f)};
		va.setVertices(vertices, 0, vertices.length);
		indices = BufferUtils.newShortBuffer(3);
		indices.put(new short[] {0, 1, 2});
		indices.flip();

		texture = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal), TextureFilter.Linear,
			TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
	}
}

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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class VBOVATest extends GdxTest {

	static final int TRIANGLES = 2000;
	VertexBufferObject vbo;
	VertexBufferObjectSubData vbosd;
	IndexBufferObject ibo;
	IndexBufferObjectSubData ibosd;
	IndexBufferObject vaibo;
	VertexArray va;
	VertexData vertexBuffer;
	float[] vertices;
	short[] indices;
	int mode = 1;
	long startTime = 0;
	int frames = 0;
	boolean isStatic = false;

	@Override
	public void create () {
		int viewport[] = new int[4];
		Gdx.gl10.glGetIntegerv(GL11.GL_VIEWPORT, viewport, 0);
		Gdx.gl10.glGetIntegerv(GL11.GL_VIEWPORT, viewport, 0);
		VertexAttribute[] attributes = {new VertexAttribute(Usage.Position, 3, "a_pos")};
		vbo = new VertexBufferObject(false, TRIANGLES * 3, attributes);
		vbosd = new VertexBufferObjectSubData(false, TRIANGLES * 3, attributes);
		ibo = new IndexBufferObject(false, TRIANGLES * 3);
		ibosd = new IndexBufferObjectSubData(false, TRIANGLES * 3);
		vaibo = new IndexBufferObject(false, TRIANGLES * 3);

		va = new VertexArray(TRIANGLES * 3, attributes);
		vertices = new float[TRIANGLES * 3 * 3];
		indices = new short[TRIANGLES * 3];

		int len = vertices.length;
		float col = Color.WHITE.toFloatBits();
		for (int i = 0; i < len; i += 9) {
			float x = (float)Math.random() * 2 - 1f;
			float y = (float)Math.random() * 2 - 1f;
			vertices[i + 0] = -.01f + x;
			vertices[i + 1] = -.01f + y;
			vertices[i + 2] = 0;
			vertices[i + 3] = .01f + x;
			vertices[i + 4] = -.01f + y;
			vertices[i + 5] = 0;
			vertices[i + 6] = 0f + x;
			vertices[i + 7] = .01f + y;
			vertices[i + 8] = 0;
		}

		len = indices.length;
		for (int i = 0; i < len; i++) {
			indices[i] = (short)i;
		}

		startTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		switch (mode) {
		case 0:
		case 3:
			vertexBuffer = vbo;
			Gdx.gl11.glColor4f(1, 0, 0, 1);
			break;
		case 1:
		case 4:
			vertexBuffer = vbosd;
			Gdx.gl11.glColor4f(0, 1, 0, 1);
			break;
		case 2:
		case 5:
			vertexBuffer = va;
			Gdx.gl11.glColor4f(0, 0, 1, 1);
			break;
		}

		for (int i = 0; i < 5; i++) {
			if (!isStatic) vertexBuffer.setVertices(vertices, 0, vertices.length);
			vertexBuffer.bind();
			if (mode == 3) {
				ibo.bind();
				if (!isStatic) ibo.setIndices(indices, 0, indices.length);
			}
			if (mode == 4) {
				ibosd.bind();
				if (!isStatic) ibosd.setIndices(indices, 0, indices.length);
			}
			if (mode == 5) {
				vaibo.setIndices(indices, 0, indices.length);
			}

			if (mode <= 2) {
				Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLES, 0, TRIANGLES * 3);
			} else {
				if (mode > 4)
					Gdx.gl11.glDrawElements(GL11.GL_TRIANGLES, TRIANGLES * 3, GL11.GL_UNSIGNED_SHORT, ibo.getBuffer());
				else
					Gdx.gl11.glDrawElements(GL11.GL_TRIANGLES, TRIANGLES * 3, GL11.GL_UNSIGNED_SHORT, 0);
			}
			if (mode == 3) ibo.unbind();
			if (mode == 4) ibosd.unbind();
			vertexBuffer.unbind();
		}

		long endTime = TimeUtils.nanoTime();
		if (endTime - startTime >= 4000000000l) {
			double secs = (endTime - startTime) / 1000000000.0;
			double fps = frames / secs;
			Gdx.app.log("VBOVATest", vertexBuffer.getClass().getName() + ", " + isStatic + ", " + (mode > 2) + ", " + fps);
			mode++;
			if (mode > 5) {
				mode = 0;
				isStatic = !isStatic;
			}
			startTime = TimeUtils.nanoTime();
			frames = 0;
		}

		frames++;
	}

	@Override
	public void resume () {
		vbo.invalidate();
		vbosd.invalidate();
		ibo.invalidate();
		ibosd.invalidate();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

}

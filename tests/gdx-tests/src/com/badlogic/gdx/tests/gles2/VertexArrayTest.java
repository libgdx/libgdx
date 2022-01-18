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

package com.badlogic.gdx.tests.gles2;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;

/** Touch the screen to cycle over 9 test case : (2 triangles, first triangle, second triangle) x (short buffer/short, byte
 * buffer/short, byte buffer/byte). */
@GdxTestConfig(OnlyGL20 = true)
public class VertexArrayTest extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;
	int[][] testCases = {{0, 0, 6}, {0, 0, 3}, {0, 3, 3}, {1, 0, 6}, {1, 0, 3}, {1, 3, 3}, {2, 0, 6}, {2, 0, 3}, {2, 3, 3}};
	int testCase = 0;
	ByteBuffer byteBuffer;
	ByteBuffer shortsAsByteBuffer;

	@Override
	public void create () {
		String vertexShader = "attribute vec4 vPosition;    \n" + "void main()                  \n"
			+ "{                            \n" + "   gl_Position = vPosition;  \n" + "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n"
			+ "void main()                                  \n" + "{                                            \n"
			+ "  gl_FragColor = vec4 ( 1.0, 1.0, 1.0, 1.0 );\n" + "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh = new Mesh(VertexDataType.VertexArray, true, 4, 6, new VertexAttribute(Usage.Position, 3, "vPosition"));
		float[] vertices = {-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, -0.5f, 0.5f, 0.0f, 0.5f, 0.5f, 0.0f};
		mesh.setVertices(vertices);

		short[] indices = {0, 1, 2, 2, 1, 3};
		mesh.setIndices(indices);

		shortsAsByteBuffer = BufferUtils.newByteBuffer(12);
		ShortBuffer sb = shortsAsByteBuffer.asShortBuffer();
		sb.put(indices);
		sb.flip();

		byteBuffer = BufferUtils.newByteBuffer(6);
		byteBuffer.put(new byte[] {0, 1, 2, 2, 1, 3});
		byteBuffer.flip();
	}

	@Override
	public void render () {
		boolean log = false;
		if (Gdx.input.justTouched()) {
			testCase = (testCase + 1) % testCases.length;
			log = true;
		}
		int mode = testCases[testCase][0];
		int offset = testCases[testCase][1];
		int count = testCases[testCase][2];
		if (log) {
			Gdx.app.log("VertexArrayTest", "mode: " + mode + ", offset: " + offset + ", count: " + count);
		}

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		mesh.bind(shader);

		Buffer buffer;
		int type;
		if (mode == 0) {
			type = GL20.GL_UNSIGNED_SHORT;
			buffer = mesh.getIndicesBuffer();
			buffer.position(offset);
		} else if (mode == 1) {
			type = GL20.GL_UNSIGNED_SHORT;
			buffer = shortsAsByteBuffer;
			buffer.position(offset * 2);
		} else {
			type = GL20.GL_UNSIGNED_BYTE;
			buffer = byteBuffer;
			buffer.position(offset);
		}

		if (log) {
			Gdx.app.log("VertexArrayTest", "position: " + buffer.position());
		}

		Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, count, type, buffer);
		buffer.position(0);
		mesh.unbind(shader);
	}
}

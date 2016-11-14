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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IndexBufferObjectShaderTest extends GdxTest {
	Texture texture;
	ShaderProgram shader;
	VertexBufferObject vbo;
	IndexBufferObject ibo;

	@Override
	public void dispose () {
		texture.dispose();
		shader.dispose();
		vbo.dispose();
		ibo.dispose();
	}

	@Override
	public void render () {
// System.out.println( "render");

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
		shader.begin();
		shader.setUniformi("u_texture", 0);
		texture.bind();
		vbo.bind(shader);
		ibo.bind();
		Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, 3, GL20.GL_UNSIGNED_SHORT, 0);
		ibo.unbind();
		vbo.unbind(shader);
		shader.end();
	}

	@Override
	public void create () {
		String vertexShader = "attribute vec4 a_position;    \n" + "attribute vec4 a_color;\n" + "attribute vec2 a_texCoords;\n"
			+ "varying vec4 v_color;" + "varying vec2 v_texCoords;" + "void main()                  \n"
			+ "{                            \n" + "   v_color = vec4(a_color.x, a_color.y, a_color.z, 1); \n"
			+ "   v_texCoords = a_texCoords; \n" + "   gl_Position =  a_position;  \n" + "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n" + "uniform sampler2D u_texture;\n" + "void main()                                  \n"
			+ "{                                            \n" + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
			+ "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		vbo = new VertexBufferObject(true, 3, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"), new VertexAttribute(
				VertexAttributes.Usage.ColorPacked, 4, "a_color"));
		float[] vertices = new float[] {-1, -1, 0, 0, Color.toFloatBits(1f, 0f, 0f, 1f), 0, 1, 0.5f, 1.0f,
			Color.toFloatBits(0f, 1f, 0f, 1f), 1, -1, 1, 0, Color.toFloatBits(0f, 0f, 1f, 1f)};
		vbo.setVertices(vertices, 0, vertices.length);

		ibo = new IndexBufferObject(true, 3);
		ibo.setIndices(new short[] {0, 1, 2}, 0, 3);

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
	}

	@Override
	public void resume () {
		vbo.invalidate();
		ibo.invalidate();
	}

}

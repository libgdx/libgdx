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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;

public class HelloTriangle extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;

	@Override
	public void create () {
		String vertexShader = "attribute vec4 vPosition;    \n" + "void main()                  \n"
			+ "{                            \n" + "   gl_Position = vPosition;  \n" + "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n"
			+ "void main()                                  \n" + "{                                            \n"
			+ "  gl_FragColor = vec4 ( 1.0, 1.0, 1.0, 1.0 );\n" + "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh = new Mesh(true, 3, 0, new VertexAttribute(Usage.Position, 3, "vPosition"));
		float[] vertices = {0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f};
		mesh.setVertices(vertices);
	}

	@Override
	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		mesh.render(shader, GL20.GL_TRIANGLES);
	}
}

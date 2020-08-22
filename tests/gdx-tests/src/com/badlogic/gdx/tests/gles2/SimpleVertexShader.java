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
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SimpleVertexShader extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 projection = new Matrix4();
	Matrix4 view = new Matrix4();
	Matrix4 model = new Matrix4();
	Matrix4 combined = new Matrix4();
	Vector3 axis = new Vector3(1, 0, 1).nor();
	float angle = 45;

	@Override
	public void create () {
		// @off
		String vertexShader =
			  "uniform mat4 u_mvpMatrix;                   \n"
			+ "attribute vec4 a_position;                  \n"
			+ "void main()                                 \n"
			+ "{                                           \n"
			+ "   gl_Position = u_mvpMatrix * a_position;  \n"
			+ "}                            \n";
		String fragmentShader =
			"#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "void main()                                  \n"
			+ "{                                            \n"
			+ "  gl_FragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );\n"
			+ "}";
		// @on

		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh = Shapes.genCube();
		mesh.getVertexAttribute(Usage.Position).alias = "a_position";
	}

	@Override
	public void render () {
		angle += Gdx.graphics.getDeltaTime() * 40.0f;
		float aspect = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		projection.setToProjection(1.0f, 20.0f, 60.0f, aspect);
		view.idt().trn(0, 0, -2.0f);
		model.setToRotation(axis, angle);
		combined.set(projection).mul(view).mul(model);

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.setUniformMatrix("u_mvpMatrix", combined);
		mesh.render(shader, GL20.GL_TRIANGLES);

		Gdx.app.log("angle", "" + angle);
	}
}

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
package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GdxGwtTest extends GwtApplication implements ApplicationListener {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 matrix = new Matrix4();
	
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(500, 500);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return this;
	}

	@Override
	public void create () {
		String vertexShader = "attribute vec4 a_position;\n" +
			"uniform mat4 u_projView;\n" +
			"varying vec4 v_color;\n" +
			"void main() {\n" +
			"v_color = vec4(1, 0, 0, 1);\n"+
			"gl_Position = u_projView * a_position;\n" +
			"}\n";
		String fragmentShader = "#ifdef GL_ES\n" +
									 	"precision mediump float;\n" +
									 	"#endif\n" +
									 	"varying vec4 v_color;\n" +
									 	"void main() {\n" +
									 	"gl_FragColor = v_color;\n" +
									 	"}\n";
		shader = new ShaderProgram(vertexShader, fragmentShader);
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		mesh = new Mesh(VertexDataType.VertexBufferObject, true, 6, 0, VertexAttribute.Position());
		mesh.setVertices(new float[] { -0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0,
												 0.5f, 0.5f, 0, -0.5f, 0.5f, 0, -0.5f, -0.5f, 0 });
	}

	@Override
	public void resume () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, (float)Math.random(), 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.begin();
		shader.setUniformMatrix("u_projView", matrix);
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}

	@Override
	public void dispose () {
	}
}
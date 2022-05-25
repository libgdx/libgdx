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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class MeshWithCustomAttributesTest extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;

	@Override
	public void dispose () {
		mesh.dispose();
		shader.dispose();
	}

	@Override
	public void render () {
		ScreenUtils.clear(Color.BLACK, true);

		shader.bind();
		mesh.render(shader, GL20.GL_TRIANGLES);
	}

	@Override
	public void create () {
		//@off
		String vertexShader = 
			  "attribute vec4 a_position;    \n"
			+ "attribute vec4 a_color;\n"
			+ "varying vec4 v_color; \n"
			+ "void main()                  \n"
			+ "{                            \n"
			+ "   v_color = a_color; \n"
			+ "   gl_Position =  a_position;  \n"
			+ "}                            \n";
		String fragmentShader = 
			  "#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n" 
			+ "varying vec4 v_color;\n"
			+ "void main()                                  \n"
			+ "{                                            \n" 
			+ "  gl_FragColor = vec4(v_color.rgb, 1.0);\n"
			+ "}";

		mesh = new Mesh(true, 3, 0, 
			new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
			new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, GL20.GL_UNSIGNED_BYTE, true, "a_color"));
		
		float[] vertices = new float[] {
			-1, -1, Color.toFloatBits(254, 0, 0, 0),
			 0,  1, Color.toFloatBits(0, 254, 0, 0), 
			 1, -1, Color.toFloatBits(0, 0, 254, 0)};
		//@on
		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh.setVertices(vertices);
	}

}

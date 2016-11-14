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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ShaderMultitextureTest extends GdxTest {
	ShaderProgram shader;
	Texture texture;
	Texture texture2;
	Mesh mesh;

	@Override
	public void create () {
		String vertexShader = "attribute vec4 a_position;   \n" + "attribute vec2 a_texCoord;   \n"
			+ "varying vec2 v_texCoord;     \n" + "void main()                  \n" + "{                            \n"
			+ "   gl_Position = a_position; \n" + "   v_texCoord = a_texCoord;  \n" + "}                            \n";

		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n"
			+ "varying vec2 v_texCoord;                            \n" + "uniform sampler2D s_texture;                        \n"
			+ "uniform sampler2D s_texture2;                        \n" + "void main()                                         \n"
			+ "{                                                   \n"
			+ "  gl_FragColor = texture2D( s_texture, v_texCoord ) * texture2D( s_texture2, v_texCoord);\n"
			+ "}                                                   \n";
		shader = new ShaderProgram(vertexShader, fragmentShader);
		mesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 2, "a_position"), new VertexAttribute(
			Usage.TextureCoordinates, 2, "a_texCoord"));
		float[] vertices = {-0.5f, 0.5f, // Position 0
			0.0f, 0.0f, // TexCoord 0
			-0.5f, -0.5f, // Position 1
			0.0f, 1.0f, // TexCoord 1
			0.5f, -0.5f, // Position 2
			1.0f, 1.0f, // TexCoord 2
			0.5f, 0.5f, // Position 3
			1.0f, 0.0f // TexCoord 3
		};
		short[] indices = {0, 1, 2, 0, 2, 3};
		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		createTexture();
	}

	private void createTexture () {
		Pixmap pixmap = new Pixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		texture = new Texture(pixmap);
		pixmap.dispose();

		pixmap = new Pixmap(256, 256, Format.RGBA8888);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1);
		pixmap.drawLine(128, 0, 128, 256);
		texture2 = new Texture(pixmap);
		pixmap.dispose();
	}

	public void resume () {
		createTexture();
	}

	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
		texture.bind();

		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE1);
		texture2.bind();

		shader.begin();
		shader.setUniformi("s_texture", 0);
		shader.setUniformi("s_texture2", 1);

		mesh.render(shader, GL20.GL_TRIANGLES);

		shader.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		texture2.dispose();
		shader.dispose();
		mesh.dispose();
	}
}

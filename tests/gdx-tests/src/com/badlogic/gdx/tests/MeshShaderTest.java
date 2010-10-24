/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MeshShaderTest implements RenderListener {
	SpriteBatch spriteBatch;
	Font font;
	ShaderProgram shader;
	Mesh mesh;
	Texture texture;
	Matrix4 matrix = new Matrix4();

	@Override public void surfaceCreated () {
		if (shader == null) {
			String vertexShader = "attribute vec4 a_position;    \n" + "attribute vec4 a_color;\n" + "attribute vec2 a_texCoords;\n"
				+ "uniform mat4 u_worldView;\n" + "varying vec4 v_color;" + "varying vec2 v_texCoords;"
				+ "void main()                  \n" + "{                            \n"
				+ "   v_color = vec4(a_color.x, a_color.y, a_color.z, 1); \n" + "   v_texCoords = a_texCoords; \n"
				+ "   gl_Position =  u_worldView * a_position;  \n" + "}                            \n";
			String fragmentShader = "precision mediump float;\n" + "varying vec4 v_color;\n" + "varying vec2 v_texCoords;\n"
				+ "uniform sampler2D u_texture;\n" + "void main()                                  \n"
				+ "{                                            \n"
				+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" + "}";

			shader = new ShaderProgram(vertexShader, fragmentShader);
			if (shader.isCompiled() == false) {
				Gdx.app.log("ShaderTest", shader.getLog());
				System.exit(0);
			}

			mesh = new Mesh(true, false, 3, 3, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
				Usage.Color, 4, "a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));

			mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0, 0.5f, -0.5f, 0, 0, 1, 0, 1, 1, 0, 0, 0.5f, 0, 0, 0, 1,
				1, 0.5f, 1});
			mesh.setIndices(new short[] {0, 1, 2});

			texture = Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal), TextureFilter.MipMap,
				TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

			spriteBatch = new SpriteBatch();
			font = Gdx.graphics.newFont("Arial", 12, FontStyle.Plain);
		}
	}

	Vector3 axis = new Vector3(0, 0, 1);
	float angle = 0;

	@Override public void render () {
		angle += Gdx.graphics.getDeltaTime() * 45;
		matrix.setToRotation(axis, angle);

		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL20().glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.graphics.getGL20().glEnable(GL20.GL_TEXTURE_2D);
		texture.bind();
		shader.begin();
		shader.setUniformMatrix("u_worldView", matrix);
		shader.setUniformi("u_texture", 0);
		mesh.render(shader, GL10.GL_TRIANGLES);
		shader.end();

		spriteBatch.begin();
		spriteBatch.drawText(font, "This is a test", 100, 100, Color.RED);
		spriteBatch.end();
	}

	@Override public void surfaceChanged (int width, int height) {

	}

	@Override public void dispose () {
		Gdx.app.log("MeshShaderTEst", "disposed");
	}

}

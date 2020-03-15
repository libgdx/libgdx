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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Draws a triangle and a trapezoid. The trapezoid is intersection between two triangles, one stencil 
 * and the triangle shown on left.
 */
public class FrameBufferTest extends GdxTest {
	FrameBuffer stencilFrameBuffer;
	FrameBuffer frameBuffer;
	Mesh mesh;

	Mesh stencilMesh;
	ShaderProgram meshShader;
	Texture texture;
	SpriteBatch spriteBatch;

	@Override
	public void render () {
		frameBuffer.begin();
		Gdx.gl20.glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
		Gdx.gl20.glClearColor(0f, 1f, 0f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		texture.bind();
		meshShader.bind();
		meshShader.setUniformi("u_texture", 0);
		mesh.render(meshShader, GL20.GL_TRIANGLES);
		frameBuffer.end();

		stencilFrameBuffer.begin();
		Gdx.gl20.glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
		Gdx.gl20.glClearColor(1f, 1f, 0f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);

		Gdx.gl20.glEnable(GL20.GL_STENCIL_TEST);

		Gdx.gl20.glColorMask(false, false, false, false);
		Gdx.gl20.glDepthMask(false);
		Gdx.gl20.glStencilFunc(GL20.GL_NEVER, 1, 0xFF);
		Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_KEEP, GL20.GL_KEEP);

		Gdx.gl20.glStencilMask(0xFF);
		Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);

		meshShader.bind();
		stencilMesh.render(meshShader, GL20.GL_TRIANGLES);

		Gdx.gl20.glColorMask(true, true, true, true);
		Gdx.gl20.glDepthMask(true);
		Gdx.gl20.glStencilMask(0x00);
		Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);

		meshShader.bind();
		mesh.render(meshShader, GL20.GL_TRIANGLES);

		Gdx.gl20.glDisable(GL20.GL_STENCIL_TEST);
		stencilFrameBuffer.end();

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();
		spriteBatch.draw(frameBuffer.getColorBufferTexture(), 0, 0, 256, 256, 0, 0, frameBuffer.getColorBufferTexture().getWidth(),
			frameBuffer.getColorBufferTexture().getHeight(), false, true);

		spriteBatch.draw(stencilFrameBuffer.getColorBufferTexture(), 256, 256, 256, 256, 0, 0, frameBuffer.getColorBufferTexture()
			.getWidth(), frameBuffer.getColorBufferTexture().getHeight(), false, true);
		spriteBatch.end();
	}

	@Override
	public void create () {
		mesh = new Mesh(true, 3, 0, new VertexAttribute(Usage.Position, 3, "a_Position"), new VertexAttribute(Usage.ColorPacked, 4,
			"a_Color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));
		float c1 = Color.toFloatBits(255, 0, 0, 255);
		float c2 = Color.toFloatBits(255, 0, 0, 255);
		float c3 = Color.toFloatBits(0, 0, 255, 255);

		mesh.setVertices(new float[] {-0.5f, -0.5f, 0, c1, 0, 0, 0.5f, -0.5f, 0, c2, 1, 0, 0, 0.5f, 0, c3, 0.5f, 1});

		stencilMesh = new Mesh(true, 3, 0, new VertexAttribute(Usage.Position, 3, "a_Position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_Color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));
		stencilMesh.setVertices(new float[] {-0.5f, 0.5f, 0, c1, 0, 0, 0.5f, 0.5f, 0, c2, 1, 0, 0, -0.5f, 0, c3, 0.5f, 1});

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));

		spriteBatch = new SpriteBatch();
		frameBuffer = new FrameBuffer(Format.RGB565, 128, 128, false);
		stencilFrameBuffer = new FrameBuffer(Format.RGB565, 128, 128, true, true);
		createShader(Gdx.graphics);
	}

	private void createShader (Graphics graphics) {
		String vertexShader = "attribute vec4 a_Position;    \n" + "attribute vec4 a_Color;\n" + "attribute vec2 a_texCoords;\n"
			+ "varying vec4 v_Color;" + "varying vec2 v_texCoords; \n" +

			"void main()                  \n" + "{                            \n" + "   v_Color = a_Color;"
			+ "   v_texCoords = a_texCoords;\n" + "   gl_Position =   a_Position;  \n" + "}                            \n";
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "varying vec4 v_Color;\n"
			+ "varying vec2 v_texCoords; \n" + "uniform sampler2D u_texture;\n" +

			"void main()                                  \n" + "{                                            \n"
			+ "  gl_FragColor = v_Color * texture2D(u_texture, v_texCoords);\n" + "}";

		meshShader = new ShaderProgram(vertexShader, fragmentShader);
		if (meshShader.isCompiled() == false) throw new IllegalStateException(meshShader.getLog());
	}

	@Override
	public void dispose () {
		mesh.dispose();
		texture.dispose();
		frameBuffer.dispose();
		stencilFrameBuffer.dispose();
		stencilMesh.dispose();
		spriteBatch.dispose();
		meshShader.dispose();
	}

}

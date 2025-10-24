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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.Buffer;
import java.nio.FloatBuffer;

@GdxTestConfig(requireGL30 = true)
public class InstancedRenderingSpriteTest extends GdxTest {

	ShaderProgram shader;
	Mesh mesh;

	private final static int INSTANCE_COUNT = 100000;
	Texture texture;

	Viewport viewport;
	private GLProfiler glProfiler;

	SpriteBatch batch;
	private Sprite sprite;

	@Override
	public void create () {
		if (Gdx.gl30 == null) {
			throw new GdxRuntimeException("GLES 3.0 profile required for this test");
		}

		String ovs = ShaderProgram.prependVertexCode;
		String ofs = ShaderProgram.prependFragmentCode;
		ShaderProgram.prependVertexCode = "#version 300 es\n";
		ShaderProgram.prependFragmentCode = "#version 300 es\n";
		shader = new ShaderProgram(Gdx.files.internal("data/shaders/sprite-instanced.vert"),
			Gdx.files.internal("data/shaders/sprite-instanced.frag"));
		if (!shader.isCompiled()) {
			throw new GdxRuntimeException("Shader compile error: " + shader.getLog());
		}

		ShaderProgram.prependVertexCode = ovs;
		ShaderProgram.prependFragmentCode = ofs;

		mesh = new Mesh(true, 6, 0, new VertexAttribute(Usage.Position, 2, "a_position"),
			new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));

		// make two triangles with uvs of 0-1. If using texture reigon, pass in correct uvs

		// for use with GL_TRIANGLES
		float[] vertices = new float[] {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,

			1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f};

		mesh.setVertices(vertices);

		mesh.enableInstancedRendering(true, INSTANCE_COUNT, new VertexAttribute(Usage.Position, 2, "i_offset"));

		FloatBuffer offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 2);
		for (int i = 0; i < INSTANCE_COUNT; i++) {
			float x = MathUtils.random(0f, 10f);
			float y = MathUtils.random(0f, 10f);
			offsets.put(new float[] {x, y});
		}
		((Buffer)offsets).position(0);
		mesh.setInstanceData(offsets);

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		sprite = new Sprite(texture);
		sprite.setSize(1, 1);
		sprite.setPosition(5, 5);

		viewport = new ExtendViewport(10, 10);

		batch = new SpriteBatch();

		glProfiler = new GLProfiler(Gdx.graphics);
		glProfiler.enable();
	}

	@Override
	public void render () {
		glProfiler.reset();

		ScreenUtils.clear(0.0f, 0.0f, 0.0f, 1f);

		viewport.getCamera().position.set(5, 5, 0);
		viewport.apply();

		boolean testingInstancing = false;

		if (testingInstancing) {

			texture.bind(0);
			shader.bind();
			shader.setUniformi("u_texture", 0);
			shader.setUniformMatrix("u_projTrans", viewport.getCamera().combined);
			mesh.render(shader, GL30.GL_TRIANGLES);

		} else {
			batch.setProjectionMatrix(viewport.getCamera().combined);
			batch.begin();
			for (int i = 0; i < INSTANCE_COUNT; i++) {
				sprite.draw(batch);
			}
			batch.end();
		}

		int drawCalls = glProfiler.getDrawCalls();

		System.out.println("Draw Calls: " + drawCalls + " and " + Gdx.graphics.getFramesPerSecond() + " FPS");
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height);
	}
}

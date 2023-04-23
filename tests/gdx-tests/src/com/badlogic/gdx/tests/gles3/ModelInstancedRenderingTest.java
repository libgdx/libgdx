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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import java.nio.Buffer;
import java.nio.FloatBuffer;

@GdxTestConfig(requireGL30 = true)
public class ModelInstancedRenderingTest extends GdxTest {

	Mesh mesh;
	private ModelBatch batch;
	private OrthographicCamera camera;
	private Renderable renderable;

	private final static int INSTANCE_COUNT_SQRT = 100;
	private final static int INSTANCE_COUNT = INSTANCE_COUNT_SQRT * INSTANCE_COUNT_SQRT;

	@Override
	public void create () {
		if (Gdx.gl30 == null) {
			throw new GdxRuntimeException("GLES 3.0 profile required for this test");
		}

		mesh = new Mesh(true, 6, 0, new VertexAttribute(Usage.Position, 2, "a_position"));

		float size = 2f / (float)Math.sqrt(INSTANCE_COUNT) * 0.5f;

		float[] vertices = new float[] {0.0f, 0.0f, size, 0.0f, 0.0f, size,

			size, 0.0f, size, size, 0.0f, size};

		mesh.setVertices(vertices);

		mesh.enableInstancedRendering(true, INSTANCE_COUNT, new VertexAttribute(Usage.Position, 2, "i_offset"),
			new VertexAttribute(Usage.ColorUnpacked, 4, "i_color"));

		FloatBuffer offsets = BufferUtils.newFloatBuffer(INSTANCE_COUNT * 6);
		for (int x = 1; x <= INSTANCE_COUNT_SQRT; x++) {
			for (int y = 1; y <= INSTANCE_COUNT_SQRT; y++) {
				offsets.put(new float[] {x / (INSTANCE_COUNT_SQRT * 0.5f) - 1f, y / (INSTANCE_COUNT_SQRT * 0.5f) - 1f,
					x / (float)INSTANCE_COUNT_SQRT, y / (float)INSTANCE_COUNT_SQRT, 1f, 1f});
			}
		}
		((Buffer)offsets).position(0);
		mesh.setInstanceData(offsets);

		renderable = new Renderable();
		renderable.material = new Material();
		renderable.meshPart.set("quad instanced", mesh, 0, 6, GL20.GL_TRIANGLES);
		renderable.worldTransform.idt();
		renderable.shader = new BaseShader() {

			@Override
			public void init () {
				ShaderProgram.prependVertexCode = "#version 300 es\n";
				ShaderProgram.prependFragmentCode = "#version 300 es\n";
				program = new ShaderProgram(Gdx.files.internal("data/shaders/instanced-rendering.vert"),
					Gdx.files.internal("data/shaders/instanced-rendering.frag"));
				if (!program.isCompiled()) {
					throw new GdxRuntimeException("Shader compile error: " + program.getLog());
				}
				init(program, renderable);
			}

			@Override
			public int compareTo (Shader other) {
				return 0;
			}

			@Override
			public boolean canRender (Renderable instance) {
				return true;
			}
		};

		renderable.shader.init();

		camera = new OrthographicCamera();
		batch = new ModelBatch();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1f);

		batch.begin(camera);
		batch.render(renderable);
		batch.end();
	}

	@Override
	public void dispose () {
		mesh.dispose();
		batch.dispose();
		renderable.shader.dispose();
	}
}

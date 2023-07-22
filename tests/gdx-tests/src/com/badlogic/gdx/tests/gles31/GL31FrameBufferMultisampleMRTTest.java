/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

package com.badlogic.gdx.tests.gles31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(requireGL31 = true)
public class GL31FrameBufferMultisampleMRTTest extends GdxTest {

	private FrameBuffer fbo;
	private FrameBuffer fboMS;
	private SpriteBatch batch;
	private ShapeRenderer shapes;
	private ShaderProgram shader;

	@Override
	public void create () {

		int nbSamples = 4;

		fboMS = new FrameBufferBuilder(64, 64, nbSamples).addColorRenderBuffer(GL30.GL_RGBA8).addColorRenderBuffer(GL30.GL_RGBA8)
			.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24).build();

		fbo = new FrameBufferBuilder(64, 64).addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL30.GL_UNSIGNED_BYTE)
			.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE)
			.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL30.GL_UNSIGNED_INT).build();

		fbo.getTextureAttachments().get(0).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		fbo.getTextureAttachments().get(1).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		fbo.getTextureAttachments().get(2).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		batch = new SpriteBatch();

		ShaderProgram.prependVertexCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop)
			? "#version 140\n #extension GL_ARB_explicit_attrib_location : enable\n"
			: "#version 300 es\n";
		ShaderProgram.prependFragmentCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop)
			? "#version 140\n #extension GL_ARB_explicit_attrib_location : enable\n"
			: "#version 300 es\n";

		shader = new ShaderProgram(Gdx.files.internal("data/shaders/shape-renderer-mrt-vert.glsl").readString(),
			Gdx.files.internal("data/shaders/shape-renderer-mrt-frag.glsl").readString());

		shapes = new ShapeRenderer(3, shader);

	}

	@Override
	public void dispose () {
		fboMS.dispose();
		fbo.dispose();
		shader.dispose();
		batch.dispose();
		shapes.dispose();
	}

	@Override
	public void render () {

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		ScreenUtils.clear(Color.CLEAR, true);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 3);

		// render a shape into the non-multisample FBO and display it on the left
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR, true);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Filled);
		shapes.triangle(0.2f, 0.3f, .9f, .9f, .8f, 0.5f);
		shapes.end();
		fbo.end();

		batch.begin();
		batch.draw(fbo.getTextureAttachments().get(0), 0, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), 0, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), 0, 2, 1, 1, 0, 0, 1, 1);
		batch.end();

		// render a shape into the multisample FBO, transfer to the other one and display it on the right
		fboMS.begin();
		ScreenUtils.clear(Color.CLEAR, true);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Filled);
		shapes.triangle(0.2f, 0.3f, .9f, .9f, .8f, 0.5f);
		shapes.end();
		fboMS.end();

		fboMS.transfer(fbo);

		batch.begin();
		batch.draw(fbo.getTextureAttachments().get(0), 1, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), 1, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), 1, 2, 1, 1, 0, 0, 1, 1);
		batch.end();

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}
}

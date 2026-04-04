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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(requireGL30 = true)
public class GL30FrameBufferMultisampleTest extends GdxTest {

	private FrameBuffer fbo;
	private FrameBuffer fboMS;
	private SpriteBatch batch;
	private ShapeRenderer shapes;

	@Override
	public void create () {
		fboMS = new FrameBufferBuilder(64, 64, 4).addColorRenderBuffer(GL30.GL_RGBA8).build();
		fbo = new FrameBuffer(Format.RGBA8888, 64, 64, false);
		fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
	}

	@Override
	public void dispose () {
		fboMS.dispose();
		fbo.dispose();
		batch.dispose();
		shapes.dispose();
	}

	@Override
	public void render () {

		ScreenUtils.clear(Color.CLEAR);

		batch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		// render a line into the non multisample FBO and display it
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Line);
		shapes.line(0, 0, 1, .3f);
		shapes.end();
		fbo.end();

		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();

		// render a line into the multisample FBO, blit to the other FBO and display it
		fboMS.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Line);
		shapes.line(0, 0, 1, .3f);
		shapes.end();
		fboMS.end();

		fboMS.transfer(fbo);

		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 1, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}
}

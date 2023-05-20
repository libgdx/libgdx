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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(requireGL31 = true)
public class GL31FrameBufferMultisampleTest extends GdxTest {
	private static class FrameBufferMS implements Disposable {
		public int framebufferHandle;
		public int width, height;
		private int colorBufferHandle;

		public FrameBufferMS (Format format, int width, int height, int samples) {
			this.width = width;
			this.height = height;

			// create render buffer
			colorBufferHandle = Gdx.gl.glGenRenderbuffer();
			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, colorBufferHandle);
			Gdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, GL30.GL_RGBA8, width, height);

			// create frame buffer
			framebufferHandle = Gdx.gl.glGenFramebuffer();
			Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);

			// attach render buffer
			Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_RENDERBUFFER,
				colorBufferHandle);

			int result = Gdx.gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
			Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);

			if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
				throw new GdxRuntimeException("error");
			}
		}

		@Override
		public void dispose () {
			Gdx.gl.glDeleteFramebuffer(framebufferHandle);
			Gdx.gl.glDeleteRenderbuffer(colorBufferHandle);
		}

		public void begin () {
			bind();
			setFrameBufferViewport();
		}

		protected void setFrameBufferViewport () {
			Gdx.gl20.glViewport(0, 0, width, height);
		}

		public void end () {
			end(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		}

		public void end (int x, int y, int width, int height) {
			unbind();
			Gdx.gl20.glViewport(x, y, width, height);
		}

		public void bind () {
			Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		}

		public static void unbind () {
			Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
		}

		public int getHeight () {
			return height;
		}

		public int getWidth () {
			return width;
		}

		public int getFramebufferHandle () {
			return framebufferHandle;
		}
	}

	private FrameBuffer fbo;
	private FrameBufferMS fboMS;
	private SpriteBatch batch;
	private ShapeRenderer shapes;

	@Override
	public void create () {
		fboMS = new FrameBufferMS(Format.RGBA8888, 64, 64, 4);
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

		Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboMS.getFramebufferHandle());
		Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo.getFramebufferHandle());
		Gdx.gl30.glBlitFramebuffer(0, 0, fboMS.getWidth(), fboMS.getHeight(), 0, 0, fbo.getWidth(), fbo.getHeight(),
			GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
		Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);

		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 1, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}
}

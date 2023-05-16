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

package com.badlogic.gdx.tests.gles32;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(requireGL32 = true)
public class GL32AdvancedBlendingTest extends GdxTest {
	private Texture texture;
	private SpriteBatch batch;

	// see https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glBlendEquation.xhtml
	static int[] modes = {
		// @off
		GL32.GL_MULTIPLY,
		GL32.GL_SCREEN,
		GL32.GL_OVERLAY,
		GL32.GL_DARKEN,
		GL32.GL_LIGHTEN,
		GL32.GL_COLORDODGE,
		GL32.GL_COLORBURN,
		GL32.GL_HARDLIGHT,
		GL32.GL_SOFTLIGHT,
		GL32.GL_DIFFERENCE,
		GL32.GL_EXCLUSION
		// @on
	};
	int mode = 0;

	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
	}

	@Override
	public void render () {
		if (Gdx.input.justTouched()) {
			mode = (mode + 1) % modes.length;
		}

		ScreenUtils.clear(Color.CLEAR);
		batch.begin();

		batch.draw(texture, 0, 0, 1, 1);

		batch.flush();

		Gdx.gl.glBlendEquation(modes[mode]);
		batch.draw(texture, 0, 0, .5f, .5f);

		batch.end();

		Gdx.gl.glBlendEquation(GL20.GL_FUNC_ADD);
	}
}

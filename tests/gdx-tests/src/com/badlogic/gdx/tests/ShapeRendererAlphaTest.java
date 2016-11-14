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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Tests alpha blending with all ShapeRenderer shapes.
 * @author mzechner */
public class ShapeRendererAlphaTest extends GdxTest {
	ShapeRenderer renderer;

	@Override
	public void create () {
		renderer = new ShapeRenderer();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		renderer.begin(ShapeType.Line);
		renderer.setColor(1, 0, 0, 0.5f);
		renderer.rect(0, 0, 100, 200);
		renderer.end();

		renderer.begin(ShapeType.Filled);
		renderer.setColor(0, 1, 0, 0.5f);
		renderer.rect(200, 0, 100, 100);
		renderer.end();

		renderer.begin(ShapeType.Line);
		renderer.setColor(0, 1, 0, 0.5f);
		renderer.circle(400, 50, 50);
		renderer.end();

		renderer.begin(ShapeType.Filled);
		renderer.setColor(1, 0, 1, 0.5f);
		renderer.circle(500, 50, 50);
		renderer.end();
	}

	@Override
	public void dispose () {
		renderer.dispose();
	}
}

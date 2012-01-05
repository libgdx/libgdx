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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ImmediateModeRendererTest extends GdxTest {
	Matrix4 projMatrix = new Matrix4();
	ImmediateModeRenderer renderer;
	Texture texture;

	@Override
	public void dispose () {
		texture.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		renderer.begin(projMatrix, GL10.GL_TRIANGLES);
		renderer.texCoord(0, 0);
		renderer.color(1, 0, 0, 1);
		renderer.vertex(-0.5f, -0.5f, 0);
		renderer.texCoord(1, 0);
		renderer.color(0, 1, 0, 1);
		renderer.vertex(0.5f, -0.5f, 0);
		renderer.texCoord(0.5f, 1);
		renderer.color(0, 0, 1, 1);
		renderer.vertex(0f, 0.5f, 0);
		renderer.end();
	}

	@Override
	public void create () {
		if(Gdx.graphics.isGL20Available()) 
			renderer = new ImmediateModeRenderer20(false, true, 1);
		else
			renderer = new ImmediateModeRenderer10();
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}

}

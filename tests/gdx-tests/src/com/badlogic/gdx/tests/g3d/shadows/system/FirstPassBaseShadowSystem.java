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

package com.badlogic.gdx.tests.g3d.shadows.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.tests.g3d.shadows.utils.DirectionalAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.LightFilter;
import com.badlogic.gdx.tests.g3d.shadows.utils.NearFarAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.ShadowMapAllocator;

/** FirstPassBaseShadowSystem assumes that the first pass renders all depth map in one texture.
 * @author realitix */
public abstract class FirstPassBaseShadowSystem extends BaseShadowSystem {

	protected static int FIRST_PASS = 0;

	public FirstPassBaseShadowSystem () {
		super();
	}

	public FirstPassBaseShadowSystem (NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		super(nearFarAnalyzer, allocator, directionalAnalyzer, lightFilter);
	}

	@Override
	protected void init (int n) {
		if (n == FIRST_PASS) init1();
	}

	protected void init1 () {
		frameBuffers[FIRST_PASS] = new FrameBuffer(Pixmap.Format.RGBA8888, allocator.getWidth(), allocator.getHeight(), true);
	}

	@Override
	protected void beginPass (int n) {
		super.beginPass(n);
		if (n == FIRST_PASS) beginPass1();
	};

	protected void beginPass1 () {
		allocator.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	@Override
	protected void endPass (int n) {
		super.endPass(n);
		if (n == FIRST_PASS) endPass1();
	};

	protected void endPass1 () {
		allocator.end();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}
}

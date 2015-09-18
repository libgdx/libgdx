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

package com.badlogic.gdx.graphics.g3d.shadow.system.realistic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.shadow.allocation.ShadowMapAllocator;
import com.badlogic.gdx.graphics.g3d.shadow.directional.DirectionalAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.filter.LightFilter;
import com.badlogic.gdx.graphics.g3d.shadow.nearfar.NearFarAnalyzer;
import com.badlogic.gdx.graphics.g3d.shadow.system.BaseShadowSystem;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;

/** The Realistic shadow system creates real shadow. Indeed, with this sytem, a shadow is the absence of light. This system render
 * only one time for each light and then render the scene. Be careful with this system, the PointLight consumes 6 varying in the
 * shader so it can reach very fast the max varying and not compile. This system implements EnvironmentListener so the lights are
 * added when you add them in the environment.
 * @author realitix */
public class RealisticShadowSystem extends BaseShadowSystem {

	/** Quantity of pass before render the scene */
	public static final int PASS_QUANTITY = 1;
	public static final int PASS_1 = 0;

	public RealisticShadowSystem (Camera camera, Array<ModelInstance> instances) {
		super(camera, instances);
	}

	public RealisticShadowSystem (Camera camera, NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		super(camera, nearFarAnalyzer, allocator, directionalAnalyzer, lightFilter);
	}

	@Override
	public int getPassQuantity () {
		return PASS_QUANTITY;
	}

	@Override
	protected void init () {
		frameBuffers[PASS_1] = new FrameBuffer(Pixmap.Format.RGBA8888, allocator.getSize(), allocator.getSize(), true);
		passShaderProviders[PASS_1] = new Pass1ShaderProvider();
		mainShaderProvider = new MainShaderProvider(new MainShader.Config(this));
	}

	@Override
	protected void beginPass (int n) {
		switch (n) {
		case PASS_1:
			beginPass1();
		}
	}

	protected void beginPass1 () {
		allocator.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	@Override
	protected void endPass (int n) {
		switch (n) {
		case PASS_1:
			endPass1();
		}
	}

	protected void endPass1 () {
		allocator.end();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}

	public Texture getTexture () {
		return this.getTexture(PASS_1);
	}
}

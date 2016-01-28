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

package com.badlogic.gdx.tests.g3d.shadows.system.classical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.tests.g3d.shadows.system.FirstPassBaseShadowSystem;
import com.badlogic.gdx.tests.g3d.shadows.utils.DirectionalAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.LightFilter;
import com.badlogic.gdx.tests.g3d.shadows.utils.NearFarAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.ShadowMapAllocator;

/** Classical shadow system uses shadow accumulation method. For each light, a depth map is generated and a second pass accumulate
 * the shadows. Obviously, the second pass must use the same lighting system as the main rendering pass. Compared to Realistic
 * shadow system, it's heavier but has some advantages:
 *
 * <pre>
 * 1 - It supports point light shadowing.
 * 2 - It's easy to use in custom shader.
 * 3 - There is no constraint about shader varying.
 * </pre>
 * @author realitix */
public class ClassicalShadowSystem extends FirstPassBaseShadowSystem {

	public static final int PASS_QUANTITY = 2;
	public static final int SECOND_PASS = 1;

	/** true if it's the first light during second pass */
	protected boolean firstCallPass2;
	protected int nbCall = 0;

	public ClassicalShadowSystem () {
		super();
	}

	public ClassicalShadowSystem (NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		super(nearFarAnalyzer, allocator, directionalAnalyzer, lightFilter);
	}

	@Override
	public int getPassQuantity () {
		return PASS_QUANTITY;
	}

	@Override
	public void init (int n) {
		super.init(n);
		mainShaderProvider = new MainShaderProvider(new MainShader.Config(this));

		if (n == SECOND_PASS) init2();
	}

	@Override
	protected void init1 () {
		super.init1();
		passShaderProviders[FIRST_PASS] = new Pass1ShaderProvider();
	}

	protected void init2 () {
		frameBuffers[SECOND_PASS] = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
		passShaderProviders[SECOND_PASS] = new Pass2ShaderProvider(new Pass2Shader.Config(this));
	}

	@Override
	protected void beginPass (int n) {
		super.beginPass(n);
		if (n == SECOND_PASS) beginPass2();
	};

	protected void beginPass2 () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		firstCallPass2 = true;
		nbCall = 0;
	}

	@Override
	public Camera next () {
		if (currentPass == SECOND_PASS && nbCall > 0) firstCallPass2 = false;
		nbCall++;
		return super.next();
	}

	@Override
	protected Camera interceptCamera (LightProperties lp) {
		if (currentPass == SECOND_PASS) return this.camera;
		return lp.camera;
	}

	@Override
	protected void processViewport (LightProperties lp, boolean cameraViewport) {
		if (this.currentPass != SECOND_PASS) super.processViewport(lp, cameraViewport);
	}

	public Texture getMainTexture () {
		return getTexture(SECOND_PASS);
	}

	public boolean isFirstCallPass2 () {
		return firstCallPass2;
	}

	@Override
	public String toString () {
		return "ClassicalShadowSystem";
	}
}

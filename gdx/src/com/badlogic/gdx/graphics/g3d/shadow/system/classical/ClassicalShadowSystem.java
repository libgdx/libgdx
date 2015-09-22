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

package com.badlogic.gdx.graphics.g3d.shadow.system.classical;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Classical shadow system renders shadow using classical method of shadow accumulation. For each light, a depth map is generated
 * and a second pass accumulate the shadows. Compare to Realistic shadow system, it's heavier but has some advantages: 1 - It
 * supports point light shadowing. 2 - It's very easy to use in custom shader. 3 - There is no constraint about shader varying.
 * @author realitix */
public class ClassicalShadowSystem extends BaseShadowSystem {

	/** Quantity of pass before render the scene */
	public static final int PASS_QUANTITY = 2;
	public static final int PASS_1 = 0;
	public static final int PASS_2 = 1;

	protected boolean firstCallPass2;
	protected int nbCall = 0;

	public ClassicalShadowSystem (Camera camera, Array<ModelInstance> instances) {
		super(camera, instances);
	}

	public ClassicalShadowSystem (Camera camera, NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
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
		frameBuffers[PASS_2] = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		passShaderProviders[PASS_1] = new Pass1ShaderProvider();
		passShaderProviders[PASS_2] = new Pass2ShaderProvider(new Pass2Shader.Config(this));
		mainShaderProvider = new MainShaderProvider(new MainShader.Config(this));
	}

	@Override
	protected void beginPass (int n) {
		switch (n) {
		case PASS_1:
			beginPass1();
			break;
		case PASS_2:
			beginPass2();
		}
	}

	protected void beginPass1 () {
		allocator.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	protected void beginPass2 () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		firstCallPass2 = true;
		nbCall = 0;
	}

	@Override
	public Camera next () {
		if (currentPass == PASS_2 && nbCall > 0) {
			firstCallPass2 = false;
		}
		nbCall++;
		return super.next();
	}

	@Override
	protected Camera interceptCamera (LightProperties lp) {
		currentLightProperties = lp;

		// if it's the second pass, we return the main camera
		if (currentPass == PASS_2) {
			return this.camera;
		}
		return lp.camera;
	}

	@Override
	protected Vector2 processViewport (LightProperties lp) {
		if (this.currentPass == PASS_2) {
			return null;
		}
		return super.processViewport(lp);
	}

	@Override
	protected void endPass (int n) {
		switch (n) {
		case PASS_1:
			endPass1();
			break;
		case PASS_2:
			endPass2();
		}
	}

	protected void endPass1 () {
		allocator.end();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}

	protected void endPass2 () {
	}

	public Texture getMainTexture () {
		return getTexture(PASS_2);
	}

	public boolean isFirstCallPass2 () {
		return firstCallPass2;
	}

	public int getLightQuantity () {
		return (dirCameras.size + spotCameras.size + pointCameras.size);
	}

	@Override
	public String toString () {
		return "ClassicalShadowSystem";
	}
}

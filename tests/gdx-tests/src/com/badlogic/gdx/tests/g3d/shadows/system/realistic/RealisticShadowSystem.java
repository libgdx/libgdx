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

package com.badlogic.gdx.tests.g3d.shadows.system.realistic;

import java.util.Set;

import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.tests.g3d.shadows.system.FirstPassBaseShadowSystem;
import com.badlogic.gdx.tests.g3d.shadows.utils.DirectionalAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.LightFilter;
import com.badlogic.gdx.tests.g3d.shadows.utils.NearFarAnalyzer;
import com.badlogic.gdx.tests.g3d.shadows.utils.ShadowMapAllocator;

/** The Realistic shadow system creates real shadows. Indeed, with this system, a shadow is the absence of light. This system
 * performs only one render pass for each light and then render the scene.
 * @author realitix */
public class RealisticShadowSystem extends FirstPassBaseShadowSystem {

	/** Number of pass before render the scene */
	public static final int PASS_QUANTITY = 1;

	public RealisticShadowSystem () {
		super();
	}

	public RealisticShadowSystem (NearFarAnalyzer nearFarAnalyzer, ShadowMapAllocator allocator,
		DirectionalAnalyzer directionalAnalyzer, LightFilter lightFilter) {
		super(nearFarAnalyzer, allocator, directionalAnalyzer, lightFilter);
	}

	@Override
	public int getPassQuantity () {
		return PASS_QUANTITY;
	}

	@Override
	public void init () {
		super.init();
		mainShaderProvider = new MainShaderProvider(new MainShader.Config(this));
	}

	@Override
	protected void init1 () {
		super.init1();
		passShaderProviders[FIRST_PASS] = new Pass1ShaderProvider();
	}

	/** No point light support */
	@Override
	public void addLight (PointLight point, Set<CubemapSide> sides) {
	}

	/** @return First pass texture containing all depth maps. */
	public Texture getTexture () {
		return this.getTexture(FIRST_PASS);
	}

	@Override
	public String toString () {
		return "RealisticShadowSystem";
	}
}

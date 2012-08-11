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

package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class MaterialShaderHandler {

	private LightManager lightManager;

	public MaterialShaderHandler (LightManager lightManager) {
		this.lightManager = lightManager;
	}

	private final Array<Material> materialsWithShader = new Array<Material>(false, 64);

	public ShaderProgram getShader (Material material) {
		for (int i = 0; i < materialsWithShader.size; i++) {
			if (material.shaderEquals(materialsWithShader.get(i))) {
				return materialsWithShader.get(i).getShader();
			}
		}

		materialsWithShader.add(material);
		return ShaderFactory.createShader(material, lightManager);
	}

	public void dispose () {
		for (int i = 0; i < materialsWithShader.size; i++) {
			if (materialsWithShader.get(i).getShader() != null) {
				materialsWithShader.get(i).getShader().dispose();
				materialsWithShader.get(i).resetShader();
			}
		}
	}
}

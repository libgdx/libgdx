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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.lights.LightManager.LightQuality;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderFactory {

	static final String define = "#define ";
	static final String lightsNum = define + "LIGHTS_NUM ";

	static public ShaderProgram createShader (Material material, LightManager lights) {

		final StringBuilder flags = new StringBuilder(128);
		flags.append(lightsNum);
		flags.append(lights.maxLightsPerModel);
		flags.append("\n");

		if (material != null) {
			for (int i = 0; i < material.getNumberOfAttributes(); i++) {
				flags.append(define);
				flags.append(material.getAttribute(i).getShaderFlag());
				flags.append("\n");
			}
		}
		// TODO FIX light chose method
		String fileName;
		if (lights.quality == LightQuality.FRAGMENT)
			fileName = "light";
		else {
			fileName = "vertexpath";
		}
		final String vertexShader = Gdx.files.internal("data/shaders/" + fileName + ".vertex.glsl").readString();
		final String fragmentShader = Gdx.files.internal("data/shaders/" + fileName + ".fragment.glsl").readString();

		ShaderProgram.pedantic = false;
		final ShaderProgram shader = new ShaderProgram(flags + vertexShader, flags + fragmentShader);
		return shader;
	}
}

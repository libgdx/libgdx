
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
			for (int i = 0; i < material.attributes.size; i++) {
				flags.append(define);
				flags.append(material.attributes.get(i).getShaderFlag());
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

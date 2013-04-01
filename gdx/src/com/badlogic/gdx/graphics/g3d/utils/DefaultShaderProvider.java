package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.utils.Array;

public class DefaultShaderProvider extends BaseShaderProvider {
	public int maxLightsCount = 5;
	
	@Override
	protected Shader createShader(final Renderable renderable) {
		Gdx.app.log("DefaultShaderProvider", "Creating new shader");
		if (Gdx.graphics.isGL20Available())
			return new DefaultShader(renderable.material, renderable.lights == null ? -1 : maxLightsCount);
		return new GLES10Shader(maxLightsCount);
	}
}

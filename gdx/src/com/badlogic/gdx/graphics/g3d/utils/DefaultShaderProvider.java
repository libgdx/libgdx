package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.test.GLES10Shader;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.utils.Array;

public class DefaultShaderProvider extends BaseShaderProvider {	
	@Override
	protected Shader createShader(final Renderable renderable) {
		Gdx.app.log("DefaultShaderProvider", "Creating new shader");
		if (Gdx.graphics.isGL20Available())
			return new TestShader(renderable.material);
		return new GLES10Shader();
	}
}

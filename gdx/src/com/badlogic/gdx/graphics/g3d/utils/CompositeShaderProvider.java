package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.CompositeShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;

public class CompositeShaderProvider extends BaseShaderProvider {
	@Override
	protected Shader createShader (Renderable renderable) {
		Gdx.app.log("CompositeShaderProvider", "Creating new shader");
		return new CompositeShader(renderable);
	}
}

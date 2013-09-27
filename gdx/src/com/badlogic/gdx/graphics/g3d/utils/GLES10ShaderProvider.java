package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;

public class GLES10ShaderProvider extends BaseShaderProvider {
	@Override
	protected Shader createShader(final Renderable renderable) {
		return new GLES10Shader();
	}
}
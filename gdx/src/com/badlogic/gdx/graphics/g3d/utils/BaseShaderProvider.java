package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.utils.Array;

public abstract class BaseShaderProvider implements ShaderProvider {
	protected Array<Shader> shaders = new Array<Shader>();
	
	@Override
	public Shader getShader (Renderable renderable) {
		Shader suggestedShader = renderable.shader;
		if (suggestedShader != null && suggestedShader.canRender(renderable))
			return suggestedShader;
		for (Shader shader : shaders) {
			if (shader.canRender(renderable))
				return shader;
		}
		final Shader shader = createShader(renderable);
		shader.init();
		shaders.add(shader);
		return shader;
	}
	
	protected abstract Shader createShader(final Renderable renderable);

	@Override
	public void dispose () {
		for(Shader shader: shaders) {
			shader.dispose();
		}
		shaders.clear();
	}
}
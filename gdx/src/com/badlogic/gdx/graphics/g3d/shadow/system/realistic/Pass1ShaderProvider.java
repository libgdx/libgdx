package com.badlogic.gdx.graphics.g3d.shadow.system.realistic;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

/**
 * @author realitix
 */
public class Pass1ShaderProvider extends DefaultShaderProvider {
	@Override
	protected Shader createShader(final Renderable renderable) {
		return new Pass1Shader(renderable);
	}

	@Override
	public Shader getShader (Renderable renderable) {
		for (Shader shader : shaders) {
			if (shader.canRender(renderable)) return shader;
		}
		final Shader shader = createShader(renderable);
		shader.init();
		shaders.add(shader);
		return shader;
	}
}

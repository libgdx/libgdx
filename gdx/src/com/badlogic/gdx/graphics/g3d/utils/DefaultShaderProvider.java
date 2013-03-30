package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.test.GLES10Shader;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.utils.Array;

public class DefaultShaderProvider implements ShaderProvider {
	protected Array<Shader> shaders = new Array<Shader>();
	
	@Override
	public Shader getShader (Renderable renderable) {
		Shader suggestedShader = renderable.shader;
		if (suggestedShader != null && suggestedShader.canRender(renderable))
			return suggestedShader;
		for (int i = 0; i < shaders.size; i++) {
			final Shader shader = shaders.get(i);
			if (shader.canRender(renderable))
				return shader;
		}
		final Shader result = createShader(renderable.material);
		shaders.add(result);
		return result;
	}
	
	protected Shader createShader(final Material material) {
		Gdx.app.log("DefaultShaderProvider", "Creating new shader");
		if (Gdx.graphics.isGL20Available())
			return new TestShader(material);
		return new GLES10Shader();
	}

	@Override
	public void dispose () {
		for(Shader shader: shaders) {
			shader.dispose();
		}
	}
}

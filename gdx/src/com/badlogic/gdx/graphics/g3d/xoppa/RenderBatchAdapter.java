package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.xoppa.test.TestShader;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
import com.badlogic.gdx.utils.Array;

public class RenderBatchAdapter implements RenderBatchListener {
	protected Array<Shader> shaders = new Array<Shader>();
		
	@Override
	public int compare (final RenderInstance o1, final RenderInstance o2) {
		final boolean b1 = o1.material.isNeedBlending();
		final boolean b2 = o2.material.isNeedBlending();
		if (b1 != b2) 
			return b1 ? 1 : -1;
		final int result = (o1.shader == o2.shader) ? 0 : o1.shader.compareTo(o2.shader);
		return result != 0 ? result : o1.shader.compare(o1, o2);
	}

	@Override
	public Shader getShader (final RenderInstance instance, final Shader suggestedShader) {
		if (suggestedShader != null && suggestedShader.canRender(instance.material))
			return suggestedShader;
		for (int i = 0; i < shaders.size; i++) {
			final Shader shader = shaders.get(i);
			if (shader.canRender(instance.material))
				return shader;
		}
		final Shader result = createShader(instance.material);
		shaders.add(result);
		return result;
	}
	
	protected Shader createShader(final Material material) {
		Gdx.app.log("test", "Creating new shader");
		return TestShader.create(material);
	}
}

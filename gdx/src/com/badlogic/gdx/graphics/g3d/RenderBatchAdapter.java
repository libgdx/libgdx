package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.old.materials.Material;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.utils.Array;

public class RenderBatchAdapter implements RenderBatchListener {
	protected Array<Shader> shaders = new Array<Shader>();
		
	@Override
	public int compare (final RenderInstance o1, final RenderInstance o2) {
		final boolean b1 = o1.renderable.material.has(BlendingAttribute.Type);
		final boolean b2 = o2.renderable.material.has(BlendingAttribute.Type);
		if (b1 != b2) 
			return b1 ? 1 : -1;
		final int result = (o1.shader == o2.shader) ? 0 : o1.shader.compareTo(o2.shader);
		return result != 0 ? result : o1.shader.compare(o1, o2);
	}

	@Override
	public Shader getShader (final RenderInstance instance, final Shader suggestedShader) {
		if (suggestedShader != null && suggestedShader.canRender(instance))
			return suggestedShader;
		for (int i = 0; i < shaders.size; i++) {
			final Shader shader = shaders.get(i);
			if (shader.canRender(instance))
				return shader;
		}
		final Shader result = createShader(instance.renderable.material);
		shaders.add(result);
		return result;
	}
	
	protected Shader createShader(final NewMaterial material) {
		Gdx.app.log("test", "Creating new shader");
		return new TestShader(material);
	}
}

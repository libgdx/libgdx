package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.g3d.xoppa.test.TestShader;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;

public class RenderBatchAdapter implements RenderBatchListener {
	protected Shader defaultShader = null;
		
	@Override
	public int compare (RenderInstance o1, RenderInstance o2) {
		final boolean b1 = o1.material.isNeedBlending();
		final boolean b2 = o2.material.isNeedBlending();
		if (b1 != b2) 
			return b1 ? 1 : -1;
		if (o1.shader == o2.shader)
			return o1.shader.compare(o1, o2);
		return o1.shader.compareTo(o2.shader);
	}

	@Override
	public Shader getShader (RenderInstance instance) {
		if (defaultShader == null)
			defaultShader = new DefaultShader();
		return defaultShader;
	}

}

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.g3d.RenderInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;

public final class RenderInstancePool extends Pool<RenderInstance> {
	@Override
	protected final RenderInstance newObject () {
		return new RenderInstance();
	}
	
	public final RenderInstance obtain(final Renderable renderable, final Matrix4 transform, final float distance, final Light[] lights, final Shader shader) {
		final RenderInstance result = obtain();
		result.renderable = renderable;
		result.transform = transform;
		result.distance = distance;
		result.lights = lights;
		result.shader = shader;
		return result;
	}
}
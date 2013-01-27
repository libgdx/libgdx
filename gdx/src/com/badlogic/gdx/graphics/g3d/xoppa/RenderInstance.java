package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.xoppa.test.Light;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;

public class RenderInstance {
	public Renderable renderable;
	public Matrix4 transform;
	public float distance;
	public Shader shader;
	public Light[] lights;
	
	public final static class RenderInstancePool extends Pool<RenderInstance> {
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
	};
	
	public final static RenderInstancePool pool = new RenderInstancePool(); 
}
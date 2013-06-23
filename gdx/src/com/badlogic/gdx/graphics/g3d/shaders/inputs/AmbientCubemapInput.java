package com.badlogic.gdx.graphics.g3d.shaders.inputs;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.lights.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Input;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Input.Setter;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class AmbientCubemapInput extends Input implements BaseShader.Input.Setter {
	private final static float ones[] = {1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1,   1, 1, 1};
	public int directionalLightsOffset;
	public int pointLightsOffset;
	private final AmbientCubemap cacheAmbientCubemap = new AmbientCubemap();
	private final static Vector3 tmpV1 = new Vector3();
	
	public AmbientCubemapInput (int directionalLightsOffset, int pointLightsOffset, int scope, String name, long materialFlags, long vertexFlags, long userFlags) {
		super(scope, name, materialFlags, vertexFlags, userFlags);
		setter = this;
		this.directionalLightsOffset = directionalLightsOffset;
		this.pointLightsOffset = pointLightsOffset;
	}
	public AmbientCubemapInput (int directionalLightsOffset, int pointLightsOffset, int scope, String name, long materialFlags, long vertexFlags) {
		this(directionalLightsOffset, pointLightsOffset, scope, name, materialFlags, vertexFlags, 0);
	}
	public AmbientCubemapInput (int directionalLightsOffset, int pointLightsOffset, int scope, String name, long materialFlags) {
		this(directionalLightsOffset, pointLightsOffset, scope, name, materialFlags, 0, 0);
	}
	public AmbientCubemapInput (int directionalLightsOffset, int pointLightsOffset, int scope, String name) {
		this(directionalLightsOffset, pointLightsOffset, scope, name, 0, 0, 0);
	}
	
	@Override
	public void set (BaseShader shader, ShaderProgram program, Input input, Camera camera, RenderContext context, Renderable renderable) {
		if (renderable.lights == null) {
			program.setUniform3fv(location, ones, 0, ones.length);
		} else {
			renderable.worldTransform.getTranslation(tmpV1);
			cacheAmbientCubemap.set(renderable.lights.ambientLight);
				
			for (int i = directionalLightsOffset; i < renderable.lights.directionalLights.size; i++)
				cacheAmbientCubemap.add(renderable.lights.directionalLights.get(i).color, renderable.lights.directionalLights.get(i).direction);
				
			for (int i = pointLightsOffset; i < renderable.lights.pointLights.size; i++)
				cacheAmbientCubemap.add(renderable.lights.pointLights.get(i).color, renderable.lights.pointLights.get(i).position, tmpV1, renderable.lights.pointLights.get(i).intensity);
				
			cacheAmbientCubemap.clamp();
			
			program.setUniform3fv(location, cacheAmbientCubemap.data, 0, cacheAmbientCubemap.data.length);
		}
	}
}
package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Environment extends Attributes {
	/** @deprecated Experimental, likely to change, do not use! */
	public ShadowMap shadowMap;
	public final Array<DirectionalLight> directionalLights = new Array<DirectionalLight>();
	public final Array<PointLight> pointLights = new Array<PointLight>();
	
	public Environment() {}
	
	public Environment add(final BaseLight... lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}
	
	public Environment add(final Array<BaseLight> lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}

	public Environment add(BaseLight light) {
		if (light instanceof DirectionalLight)
			directionalLights.add((DirectionalLight)light);
		else if (light instanceof PointLight)
			pointLights.add((PointLight)light);
		else
			throw new GdxRuntimeException("Unknown light type");
		return this;
	}
}

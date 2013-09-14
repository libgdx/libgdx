package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Lights {
	public final Color ambientLight = new Color(0,0,0,1);
	public Color fog;
	/** @deprecated Experimental, likely to change, do not use! */
	public ShadowMap shadowMap;
	public Cubemap environmentCubemap;
	public final Array<DirectionalLight> directionalLights = new Array<DirectionalLight>();
	public final Array<PointLight> pointLights = new Array<PointLight>();
	
	public Lights() {}
	
	public Lights(final Color ambient) {
		ambientLight.set(ambient);
	}
	
	public Lights(final float ambientRed, final float ambientGreen, final float ambientBlue) {
		ambientLight.set(ambientRed, ambientGreen, ambientBlue, 1f);
	}
	
	public Lights(final Color ambient, final BaseLight... lights) {
		this(ambient);
		add(lights);
	}
	
	public Lights clear() {
		ambientLight.set(0,0,0,1);
		directionalLights.clear();
		pointLights.clear();
		return this;
	}
	
	public Lights add(final BaseLight... lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}
	
	public Lights add(final Array<BaseLight> lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}

	public Lights add(BaseLight light) {
		if (light instanceof DirectionalLight)
			directionalLights.add((DirectionalLight)light);
		else if (light instanceof PointLight)
			pointLights.add((PointLight)light);
		else
			throw new GdxRuntimeException("Unknown light type");
		return this;
	}
}

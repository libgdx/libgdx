/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Environment extends Attributes {
	/** @deprecated Experimental, likely to change, do not use! */
	public ShadowMap shadowMap;

	public Environment () {
	}

	public Environment add (final BaseLight... lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}

	public Environment add (final Array<BaseLight> lights) {
		for (final BaseLight light : lights)
			add(light);
		return this;
	}

	public Environment add (BaseLight light) {
		if (light instanceof DirectionalLight)
			add((DirectionalLight)light);
		else if (light instanceof PointLight) {
			add((PointLight)light);
		} else if (light instanceof SpotLight)
			add((SpotLight)light);
		else
			throw new GdxRuntimeException("Unknown light type");
		return this;
	}

	public Environment add (DirectionalLight light) {
		DirectionalLightsAttribute dirLights = ((DirectionalLightsAttribute)get(DirectionalLightsAttribute.Type));
		if (dirLights == null) set(dirLights = new DirectionalLightsAttribute());
		dirLights.lights.add(light);
		return this;
	}

	public Environment add (PointLight light) {
		PointLightsAttribute pointLights = ((PointLightsAttribute)get(PointLightsAttribute.Type));
		if (pointLights == null) set(pointLights = new PointLightsAttribute());
		pointLights.lights.add(light);
		return this;
	}

	public Environment add (SpotLight light) {
		SpotLightsAttribute spotLights = ((SpotLightsAttribute)get(SpotLightsAttribute.Type));
		if (spotLights == null) set(spotLights = new SpotLightsAttribute());
		spotLights.lights.add(light);
		return this;
	}

	public Environment remove (final BaseLight... lights) {
		for (final BaseLight light : lights)
			remove(light);
		return this;
	}

	public Environment remove (final Array<BaseLight> lights) {
		for (final BaseLight light : lights)
			remove(light);
		return this;
	}

	public Environment remove (BaseLight light) {
		if (light instanceof DirectionalLight)
			remove((DirectionalLight)light);
		else if (light instanceof PointLight)
			remove((PointLight)light);
		else if (light instanceof SpotLight)
			remove((SpotLight)light);
		else
			throw new GdxRuntimeException("Unknown light type");
		return this;
	}

	public Environment remove (DirectionalLight light) {
		if (has(DirectionalLightsAttribute.Type)) {
			DirectionalLightsAttribute dirLights = ((DirectionalLightsAttribute)get(DirectionalLightsAttribute.Type));
			dirLights.lights.removeValue(light, false);
			if (dirLights.lights.size == 0)
				remove(DirectionalLightsAttribute.Type);
		}
		return this;
	}

	public Environment remove (PointLight light) {
		if (has(PointLightsAttribute.Type)) {
			PointLightsAttribute pointLights = ((PointLightsAttribute)get(PointLightsAttribute.Type));
			pointLights.lights.removeValue(light, false);
			if (pointLights.lights.size == 0)
				remove(PointLightsAttribute.Type);
		}
		return this;
	}

	public Environment remove (SpotLight light) {
		if (has(SpotLightsAttribute.Type)) {
			SpotLightsAttribute spotLights = ((SpotLightsAttribute)get(SpotLightsAttribute.Type));
			spotLights.lights.removeValue(light, false);
			if (spotLights.lights.size == 0)
				remove(SpotLightsAttribute.Type);
		}
		return this;
	}
}

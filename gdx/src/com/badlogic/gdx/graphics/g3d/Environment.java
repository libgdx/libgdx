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
			directionalLights.add((DirectionalLight)light);
		else if (light instanceof PointLight)
			pointLights.add((PointLight)light);
		else
			throw new GdxRuntimeException("Unknown light type");
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
			directionalLights.removeValue((DirectionalLight)light, false);
		else if (light instanceof PointLight)
			pointLights.removeValue((PointLight)light, false);
		else
			throw new GdxRuntimeException("Unknown light type");
		return this;
	}

	@Override
	public void clear () {
		super.clear();
		directionalLights.clear();
		pointLights.clear();
	}
}

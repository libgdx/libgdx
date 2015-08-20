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

package com.badlogic.gdx.graphics.g3d.shadow.filter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.collision.BoundingBox;

/** This Filter allows lights thare are in camera frustum
 * @author realitix */
public class FrustumLightFilter implements LightFilter {

	/** Scene */
	protected Scene scene;
	/** tmp bounding box */
	protected BoundingBox bb = new BoundingBox();

	public FrustumLightFilter (Scene scene) {
		this.scene = scene;
	}

	@Override
	public boolean filter (int n, BaseLight light, Camera camera) {
		Frustum f1 = scene.getCamera().frustum;
		Frustum f2 = camera.frustum;
		bb.inf();

		for (int i = 0; i < f2.planePoints.length; i++) {
			bb.ext(f2.planePoints[i]);
		}

		if (f1.boundsInFrustum(bb)) {
			return true;
		}

		return false;
	}
}

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

package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;

/** Compute the DirectionalResult based on the bounding sphere of the frustum
 * @author realitix */
public class BoundingSphereDirectionalAnalyzer implements DirectionalAnalyzer {
	protected BoundingBox bb = new BoundingBox();
	protected Sphere sphere = new Sphere(new Vector3(), 0);
	protected Vector3 tmpV = new Vector3();
	protected Vector3 tmpV2 = new Vector3();

	@Override
	public Camera analyze (BaseLight light, Frustum frustum, Vector3 direction, Camera out) {
		bb.inf();
		for (int i = 0; i < frustum.planePoints.length; i++) {
			bb.ext(frustum.planePoints[i]);
		}
		bb.getBoundingSphere(sphere);

		// Position at sphere center
		tmpV.set(sphere.center);

		// Move back from 1.5*radius
		tmpV2.set(direction);
		tmpV2.scl(sphere.radius * 1.5f);

		out.direction.set(direction);
		out.position.set(tmpV.sub(tmpV2));
		out.near = 0.5f * sphere.radius;
		out.far = 2.5f * sphere.radius;
		out.up.set(direction.y, direction.z, direction.x);
		out.viewportWidth = sphere.radius;
		out.viewportHeight = sphere.radius;

		return out;
	}
}

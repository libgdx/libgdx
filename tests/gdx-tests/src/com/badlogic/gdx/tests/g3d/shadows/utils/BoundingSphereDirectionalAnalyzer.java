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

package com.badlogic.gdx.tests.g3d.shadows.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Compute directional camera based on frustum bounding sphere.
 * @author realitix */
public class BoundingSphereDirectionalAnalyzer implements DirectionalAnalyzer {
	/** Objects used for computation */
	protected BoundingBox bb = new BoundingBox();
	protected Vector3 tmpV = new Vector3();
	protected Vector3 tmpV2 = new Vector3();

	@Override
	public Camera analyze (DirectionalLight light, Camera out, Camera mainCamera) {
		bb.inf();

		// Create bounding box encompassing main camera frustum
		for (int i = 0; i < mainCamera.frustum.planePoints.length; i++) {
			bb.ext(mainCamera.frustum.planePoints[i]);
		}

		// Radius
		float radius = bb.getDimensions(tmpV).len() * 0.5f;

		// Center
		bb.getCenter(tmpV);

		// Move back from 1.5*radius
		tmpV2.set(light.direction);
		tmpV2.scl(radius * 1.5f);

		// Position out camera
		out.direction.set(light.direction);
		out.position.set(tmpV.sub(tmpV2));

		// Compute near and far
		out.near = 0.5f * radius;
		out.far = 2.5f * radius;

		// Compute up vector
		Vector3 d = light.direction;
		if (d.z < d.x + d.y)
			out.up.set(-light.direction.y, light.direction.x, light.direction.z);
		else
			out.up.set(light.direction.x, -light.direction.z, light.direction.y);

		// Compute viewport (orthographic camera)
		out.viewportWidth = radius;
		out.viewportHeight = radius;

		return out;
	}
}

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
import com.badlogic.gdx.math.collision.Sphere;

/** Compute directional camera based on frustum bounding sphere.
 * @author realitix */
public class BoundingSphereDirectionalAnalyzer implements DirectionalAnalyzer {
	/** Main camera */
	protected Camera camera;

	/** Objects used for computation */
	protected BoundingBox bb = new BoundingBox();
	protected Sphere sphere = new Sphere(new Vector3(), 0);
	protected Vector3 tmpV = new Vector3();
	protected Vector3 tmpV2 = new Vector3();

	/** @param camera Main camera */
	public BoundingSphereDirectionalAnalyzer (Camera camera) {
		this.camera = camera;
	}

	@Override
	public Camera analyze (DirectionalLight light, Camera out) {
		bb.inf();

		// Create bounding box encompassing main camera frustum
		for (int i = 0; i < camera.frustum.planePoints.length; i++) {
			bb.ext(camera.frustum.planePoints[i]);
		}

		// Convert to bounding sphere
		bb.getBoundingSphere(sphere);

		// Position at sphere center
		tmpV.set(sphere.center);

		// Move back from 1.5*radius
		tmpV2.set(light.direction);
		tmpV2.scl(sphere.radius * 1.5f);

		// Position out camera
		out.direction.set(light.direction);
		out.position.set(tmpV.sub(tmpV2));

		// Compute near and far
		out.near = 0.5f * sphere.radius;
		out.far = 2.5f * sphere.radius;

		// Compute up vector
		out.up.set(light.direction.y, light.direction.z, light.direction.x);

		// Compute viewport (orthographic camera)
		out.viewportWidth = sphere.radius;
		out.viewportHeight = sphere.radius;

		return out;
	}
}

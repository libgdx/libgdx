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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;
import com.badlogic.gdx.utils.Array;

/** Compute near and far plane based on instances passed in constructor. Model instance array should contains only instances in
 * camera frustum.
 * @author realitix */
public class AABBNearFarAnalyzer implements NearFarAnalyzer {

	/** Near and far initialization before computation. You should put the same values as the main camera */
	public static float CAMERA_NEAR = 0.1f;
	public static float CAMERA_FAR = 1000;

	/** Array of frustumed instances */
	protected Array<ModelInstance> instances;

	/** Objects used for computation */
	protected BoundingBox bb1 = new BoundingBox();
	protected BoundingBox bb2 = new BoundingBox();
	protected Sphere sphere = new Sphere(new Vector3(), 0);

	/** Instances should be in the camera frustum.
	 * @param instances Array of model instances */
	public AABBNearFarAnalyzer (Array<ModelInstance> instances) {
		this.instances = instances;
	}

	@Override
	public void analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for (ModelInstance instance : instances) {
			for (Node node : instance.nodes) {
				node.calculateBoundingBox(bb2);

				if (bb2.contains(camera.position)) {
					// @TODO If inside instance, loop over vertices
				} else if (camera.frustum.boundsInFrustum(bb2)) {
					node.extendBoundingBox(bb1);
				}
			}
		}

		computeResult(bb1, camera);
	}

	/** Initialize camera before computation
	 * @param camera Camera to compute. */
	protected void prepareCamera (Camera camera) {
		camera.near = AABBNearFarAnalyzer.CAMERA_NEAR;
		camera.far = AABBNearFarAnalyzer.CAMERA_FAR;
		camera.update();
	}

	/** Compute final result
	 * @param bb BoundingBox encompassing instances
	 * @param camera Camera to compute */
	protected void computeResult (BoundingBox bb, Camera camera) {
		bb1.getBoundingSphere(sphere);
		float distance = sphere.center.dst(camera.position);
		float near = distance - sphere.radius;
		float far = distance + sphere.radius;

		if (near <= 0) near = CAMERA_NEAR;
		if (far <= 0) far = CAMERA_FAR;

		camera.near = near;
		camera.far = far;
		camera.update();
	}
}

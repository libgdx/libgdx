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

package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;
import com.badlogic.gdx.utils.Array;

/** Compute the near and far plane base on the object's bounding box of the scene
 * @author realitix */
public class AABBNearFarAnalyzer implements NearFarAnalyzer {

	public static float CAMERA_NEAR = 0.1f;
	public static float CAMERA_FAR = 1000;

	protected Array<ModelInstance> instances;
	protected Vector2 result = new Vector2();
	protected BoundingBox bb1 = new BoundingBox();
	protected BoundingBox bb2 = new BoundingBox();
	protected Sphere sphere = new Sphere(new Vector3(), 0);

	public AABBNearFarAnalyzer (Array<ModelInstance> instances) {
		this.instances = instances;
	}

	@Override
	public Vector2 analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for (ModelInstance instance : instances) {
			for (Node node : instance.nodes) {
				node.calculateBoundingBox(bb2);

				if (bb2.contains(camera.position)) {
				} else if (camera.frustum.boundsInFrustum(bb2)) {
					node.extendBoundingBox(bb1);
				}
			}
		}

		return computeResult(bb1, camera.position);
	}

	protected void prepareCamera (Camera camera) {
		camera.near = AABBNearFarAnalyzer.CAMERA_NEAR;
		camera.far = AABBNearFarAnalyzer.CAMERA_FAR;
		camera.update();
	}

	protected Vector2 computeResult (BoundingBox bb, Vector3 cameraPosition) {
		bb1.getBoundingSphere(sphere);
		float distance = sphere.center.dst(cameraPosition);
		result.set(distance - sphere.radius, distance + sphere.radius);

		if (result.x <= 0) result.x = 1f;
		if (result.y <= 0) result.y = 1f;

		return result;
	}

}

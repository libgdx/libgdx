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
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Add cache to AABBNearFarAnalyzer. Do not use it if your meshes are dynamics.
 * @author realitix */
public class AABBCachedNearFarAnalyzer extends AABBNearFarAnalyzer {

	/** Array containing cached nodes */
	protected ObjectMap<Node, BoundingBox> cachedBoundingBoxes = new ObjectMap<Node, BoundingBox>();

	public AABBCachedNearFarAnalyzer (Array<ModelInstance> instances) {
		super(instances);
	}

	@Override
	public void analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for (ModelInstance instance : instances) {
			for (Node node : instance.nodes) {
				if (cachedBoundingBoxes.containsKey(node)) {
					bb2.set(cachedBoundingBoxes.get(node));
				} else {
					node.calculateBoundingBox(bb2);
					cachedBoundingBoxes.put(node, new BoundingBox(bb2));
				}

				if (bb2.contains(camera.position)) {
					// @TODO If inside instance, loop over vertices
				} else if (camera.frustum.boundsInFrustum(bb2)) {
					node.extendBoundingBox(bb1);
				}
			}
		}

		computeResult(bb1, camera);
	}
}

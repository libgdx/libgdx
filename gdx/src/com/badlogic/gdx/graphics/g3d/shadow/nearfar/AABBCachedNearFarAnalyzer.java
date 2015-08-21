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
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Compute the near and far plane base on the object's bounding box of the scene Faster than AABBNearFarAnalyzer because cache
 * BoundingBox Do not use it your mesh are dynamics
 * @author realitix */
public class AABBCachedNearFarAnalyzer extends AABBNearFarAnalyzer {

	protected ObjectMap<Node, BoundingBox> cachedBoundingBoxes = new ObjectMap<Node, BoundingBox>();

	public AABBCachedNearFarAnalyzer (Array<ModelInstance> instances) {
		super(instances);
	}

	@Override
	public Vector2 analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for (ModelInstance instance : instances) {
			for (Node node : instance.nodes) {
				if (cachedBoundingBoxes.containsKey(node))
					bb2.set(cachedBoundingBoxes.get(node));
				else
					node.calculateBoundingBox(bb2);

				if (bb2.contains(camera.position)) {
				} else if (camera.frustum.boundsInFrustum(bb2)) {
					node.extendBoundingBox(bb1);
				}
			}
		}

		return computeResult(bb1, camera.position);
	}
}

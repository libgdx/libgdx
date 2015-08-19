package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Compute the near and far plane base on the
 * object's bounding box of the scene
 * Faster than AABBNearFarAnalyzer because cache BoundingBox
 * Do not use it your mesh are dynamics
 * @author realitix
 */
public class AABBCachedNearFarAnalyzer extends AABBNearFarAnalyzer {

	protected ObjectMap<Node, BoundingBox> cachedBoundingBoxes = new ObjectMap<Node, BoundingBox>();

	public AABBCachedNearFarAnalyzer (Scene scene) {
		super(scene);
	}

	@Override
	public Vector2 analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for( ModelInstance instance: scene.getInstances() ) {
			for( Node node: instance.nodes ) {
				if( cachedBoundingBoxes.containsKey(node) )
					bb2.set(cachedBoundingBoxes.get(node));
				else
					node.calculateBoundingBox(bb2);

				if( bb2.contains(camera.position) ) {
				}
				else if( camera.frustum.boundsInFrustum(bb2) ) {
					node.extendBoundingBox(bb1);
				}
			}
		}

		return computeResult(bb1, camera.position);
	}
}

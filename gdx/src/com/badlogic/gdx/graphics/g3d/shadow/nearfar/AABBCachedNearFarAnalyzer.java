package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Compute the near and far plane base on the
 * object's bounding box of the scene
 * Faster than AABBNearFarAnalyzer because cache BoundingBox
 * Do not use it your mesh are dynamics
 * @author realitix
 */
public class AABBCachedNearFarAnalyzer extends AABBNearFarAnalyzer {

	protected ObjectMap<ModelInstance, BoundingBox> cachedBoundingBoxes = new ObjectMap<ModelInstance, BoundingBox>(); 

	public AABBCachedNearFarAnalyzer (Scene scene) {
		super(scene);
	}

	@Override
	public Vector2 analyze (BaseLight light, Camera camera) {
		prepareCamera(camera);

		bb1.inf();
		for( ModelInstance instance: scene.getInstances() ) {
			if( cachedBoundingBoxes.containsKey(instance) )
				bb2.set(cachedBoundingBoxes.get(instance));
			else
				instance.calculateBoundingBox(bb2);

			if( bb2.contains(camera.position) ) {
			}
			else if( camera.frustum.boundsInFrustum(bb2) ) {
				instance.extendBoundingBox(bb1);
			}
		}

		return computeResult(bb1, camera.position);
	}
}

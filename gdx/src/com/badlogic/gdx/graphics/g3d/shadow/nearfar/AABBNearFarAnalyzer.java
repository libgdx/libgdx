package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;

/**
 * Compute the near and far plane base on the
 * object's bounding box of the scene
 * @author realitix
 */
public class AABBNearFarAnalyzer implements NearFarAnalyzer {

	public static float CAMERA_NEAR = 0.1f;
	public static float CAMERA_FAR = 1000;

	private Scene scene;
	private Vector2 result = new Vector2();
	private BoundingBox bb1 = new BoundingBox();
	private BoundingBox bb2 = new BoundingBox();
	private Sphere sphere = new Sphere(new Vector3(), 0);

	public AABBNearFarAnalyzer(Scene scene) {
		this.scene = scene;
	}

	@Override
	public Vector2 analyze (Camera camera) {
		camera.near = AABBNearFarAnalyzer.CAMERA_NEAR;
		camera.far = AABBNearFarAnalyzer.CAMERA_FAR;
		camera.update();

		bb1.inf();
		for( ModelInstance instance: scene.getInstances() ) {
			instance.calculateBoundingBox(bb2);

			if( bb2.contains(camera.position) ) {
			}
			else if( camera.frustum.boundsInFrustum(bb2) ) {
				instance.extendBoundingBox(bb1);
			}
		}

		bb1.getBoundingSphere(sphere);
		float distance = sphere.center.dst(camera.position);
		result.set(distance - sphere.radius, distance + sphere.radius);
		//result.set(distance - 2*sphere.radius, distance + 2*sphere.radius);
		//result.set(distance - (sphere.radius + 0.1f*sphere.radius), distance + sphere.radius);

		if (result.x <= 0) result.x = 1f;
		if (result.y <= 0) result.y = 1f;

		return result;
	}

}

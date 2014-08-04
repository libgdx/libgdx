
package com.badlogic.gdx.tests.ai.steer.box2d;

import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {

	World world;
	B2SteerRaycastCallback callback;
	Vector2 inputRayEndPoint;

	public Box2dRaycastCollisionDetector (World world) {
		this(world, new B2SteerRaycastCallback());
	}

	public Box2dRaycastCollisionDetector (World world, B2SteerRaycastCallback callback) {
		this.world = world;
		this.callback = callback;
		this.inputRayEndPoint = new Vector2();
	}

	@Override
	public boolean findCollision (Ray<Vector2> outputRay, Ray<Vector2> inputRay) {
		callback.collided = false;
		if (!inputRay.direction.isZero()) {
			inputRayEndPoint.set(inputRay.origin).add(inputRay.direction);
			callback.outputRay = outputRay;
			world.rayCast(callback, inputRay.origin, inputRayEndPoint);
		}
		return callback.collided;
	}

	public static class B2SteerRaycastCallback implements RayCastCallback {
		public Ray<Vector2> outputRay;
		public boolean collided;

		public B2SteerRaycastCallback () {
		}

		@Override
		public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			outputRay.origin.set(point);
			outputRay.direction.set(normal);
			collided = true;
			return fraction;
		}
	}
}

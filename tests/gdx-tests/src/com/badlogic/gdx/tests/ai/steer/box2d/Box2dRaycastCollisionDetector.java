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

package com.badlogic.gdx.tests.ai.steer.box2d;

import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Collision;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

/** A raycast collision detector for box2d.
 * @author davebaol */
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
	public boolean findCollision (Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
		callback.collided = false;
		if (!inputRay.direction.isZero()) {
			inputRayEndPoint.set(inputRay.origin).add(inputRay.direction);
			callback.outputCollision = outputCollision;
			world.rayCast(callback, inputRay.origin, inputRayEndPoint);
		}
		return callback.collided;
	}

	public static class B2SteerRaycastCallback implements RayCastCallback {
		public Collision<Vector2> outputCollision;
		public boolean collided;

		public B2SteerRaycastCallback () {
		}

		@Override
		public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			outputCollision.set(point, normal);
			collided = true;
			return fraction;
		}
	}
}

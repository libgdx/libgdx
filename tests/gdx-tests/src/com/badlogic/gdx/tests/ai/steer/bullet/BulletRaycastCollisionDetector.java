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

package com.badlogic.gdx.tests.ai.steer.bullet;

import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Collision;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Ray;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.bullet.collision.ClosestNotMeRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.tests.ai.steer.box2d.Box2dRaycastCollisionDetector.B2SteerRaycastCallback;

/** A 3D {@link RaycastCollisionDetector} to be used with bullet physics. It reports the closest collision which is not the
 * supplied "me" collision object.
 * @author Daniel Holderbaum */
public class BulletRaycastCollisionDetector implements RaycastCollisionDetector<Vector3> {

	btCollisionWorld world;

	Vector3 rayFrom = new Vector3();
	Vector3 rayTo = new Vector3();
	ClosestRayResultCallback callback;

	public BulletRaycastCollisionDetector (btCollisionWorld world, btCollisionObject me) {
		this.world = world;
		this.callback = new ClosestNotMeRayResultCallback(me);
	}

	@Override
	public boolean findCollision (Collision<Vector3> outputCollision, Ray<Vector3> inputRay) {
		// reset because we reuse the callback
		callback.setCollisionObject(null);
		
		rayFrom.set(inputRay.origin);
		rayTo.set(rayFrom).add(inputRay.direction);
		world.rayTest(rayFrom, rayTo, callback);

		callback.getHitPointWorld(outputCollision.point);
		callback.getHitNormalWorld(outputCollision.normal);
		
		return callback.hasHit();
	}

}

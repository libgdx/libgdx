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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance.Collision;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.tests.ai.steer.SteeringActor;
import com.badlogic.gdx.utils.viewport.Viewport;

/** An {@link InputProcessor} that allows you to manually move a {@link SteeringBulletEntity}.
 * 
 * @author Daniel Holderbaum */
public class BulletTargetInputProcessor extends InputAdapter {

	SteeringBulletEntity target;
	Viewport viewport;
	btCollisionWorld world;
	Vector3 offset;

	public BulletTargetInputProcessor (SteeringBulletEntity target, Vector3 offset, Viewport viewport, btCollisionWorld world) {
		this.target = target;
		this.viewport = viewport;
		this.world = world;
		this.offset = offset;
	}

	private static final Collision<Vector3> output = new Collision<Vector3>(new Vector3(), new Vector3());

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			Ray pickRay = viewport.getPickRay(screenX, screenY);
			btCollisionObject body = rayTest(output, pickRay);

			if (body != null && body.userData != null && body.userData.equals("ground")) {
				target.transform.setToTranslation(output.point.add(offset));
				target.body.setWorldTransform(target.transform);
			}
		}

		// we still return false to let the following camera controller recognize the touch up event
		return false;
	}

	private static final Vector3 rayFrom = new Vector3();
	private static final Vector3 rayTo = new Vector3();
	private static final ClosestRayResultCallback callback = new ClosestRayResultCallback(rayFrom, rayTo);

	private btCollisionObject rayTest (Collision<Vector3> output, Ray ray) {
		rayFrom.set(ray.origin);
		// 500 meters max from the origin
		rayTo.set(ray.direction).scl(500f).add(rayFrom);

		// we reuse the ClosestRayResultCallback, thus we need to reset its
		// values
		callback.setCollisionObject(null);
		callback.setClosestHitFraction(1f);
		callback.setRayFromWorld(rayFrom);
		callback.setRayToWorld(rayTo);

		world.rayTest(rayFrom, rayTo, callback);

		if (callback.hasHit()) {
			callback.getHitPointWorld(output.point);
			callback.getHitNormalWorld(output.normal);
			return callback.getCollisionObject();
		}

		return null;
	}
}

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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

/** @author xoppa */
public class RayCastTest extends BaseBulletTest {
	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 5;
	final int BOXCOUNT_Z = 1;

	final float BOXOFFSET_X = 0f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = 2.5f;

	ClosestRayResultCallback rayTestCB;
	Vector3 rayFrom = new Vector3();
	Vector3 rayTo = new Vector3();

	@Override
	public void create () {
		super.create();
		instructions = "Tap a box to ray cast\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom";

		// Create the entities
		world.add("ground", -7f, 0f, -7f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					world.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z).setColor(0.5f + 0.5f * (float)Math.random(),
						0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
				}
			}
		}

		rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
	}

	@Override
	public void dispose () {
		if (rayTestCB != null) rayTestCB.dispose();
		rayTestCB = null;
		super.dispose();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		Ray ray = camera.getPickRay(x, y);
		rayFrom.set(ray.origin);
		rayTo.set(ray.direction).scl(50f).add(rayFrom); // 50 meters max from the origin

		// Because we reuse the ClosestRayResultCallback, we need reset it's values
		rayTestCB.setCollisionObject(null);
		rayTestCB.setClosestHitFraction(1f);
		rayTestCB.setRayFromWorld(rayFrom);
		rayTestCB.setRayToWorld(rayTo);

		world.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB);

		if (rayTestCB.hasHit()) {
			final btCollisionObject obj = rayTestCB.getCollisionObject();
			if (!obj.isStaticOrKinematicObject()) {
				final btRigidBody body = (btRigidBody)(obj);
				body.activate();
				body.applyCentralImpulse(tmpV2.set(ray.direction).scl(20f));
			}
		}
		return true;
	}
}

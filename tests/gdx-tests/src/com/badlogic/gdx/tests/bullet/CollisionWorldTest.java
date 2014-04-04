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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.ContactResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;

/** @author xoppa */
public class CollisionWorldTest extends BaseBulletTest {
	BulletEntity movingBox;
	boolean hit = false;
	Color normalColor = new Color();
	btCollisionObject other;

	public class TestContactResultCallback extends ContactResultCallback {
		@Override
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
			btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
			hit = true;
			other = colObj0Wrap.getCollisionObject() == movingBox.body ? colObj1Wrap.getCollisionObject() : colObj0Wrap
				.getCollisionObject();

			return 0f;
		}
	}

	TestContactResultCallback contactCB;

	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btCollisionWorld collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
		return new BulletWorld(collisionConfig, dispatcher, broadphase, null, collisionWorld);
	}

	@Override
	public void create () {
		super.create();

		instructions = "Long press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom";

		contactCB = new TestContactResultCallback();

		Model groundModel = world.getConstructor("ground").model;
		Model boxModel = world.getConstructor("box").model;

		world.addConstructor("collisionGround", new BulletConstructor(groundModel));
		world.addConstructor("collisionBox", new BulletConstructor(boxModel));

		world.add("collisionGround", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		world.add("collisionBox", 0f, 1f, 5f).setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			0.5f + 0.5f * (float)Math.random(), 1f);
		world.add("collisionBox", 0f, 1f, -5f).setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			0.5f + 0.5f * (float)Math.random(), 1f);
		world.add("collisionBox", 5f, 1f, 0f).setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			0.5f + 0.5f * (float)Math.random(), 1f);
		world.add("collisionBox", -5f, 1f, 0f).setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			0.5f + 0.5f * (float)Math.random(), 1f);
		movingBox = world.add("collisionBox", -5f, 1f, 0f);
		normalColor.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
			1f);
	}

	Color tmpColor = new Color();

	@Override
	public void render () {
		movingBox.transform.val[Matrix4.M03] = movingBox.transform.val[Matrix4.M13] = movingBox.transform.val[Matrix4.M23] = 0f;
		movingBox.transform.rotate(Vector3.Y, Gdx.graphics.getDeltaTime() * 45f);
		movingBox.transform.translate(-5f, 1f, 0f);
		movingBox.body.setWorldTransform(movingBox.transform);

		super.render();
	}

	@Override
	public void update () {
		super.update();
		// Not using dynamics, so update the collision world manually
		if (world.performanceCounter != null) world.performanceCounter.start();
		world.collisionWorld.performDiscreteCollisionDetection();
		if (world.performanceCounter != null) world.performanceCounter.stop();
	}

	@Override
	protected void renderWorld () {
		hit = false;
		other = null;
		world.collisionWorld.contactTest(movingBox.body, contactCB);
		movingBox.setColor(hit ? Color.RED : normalColor);

		BulletEntity e = null;
		if (other != null && other.userData != null && other.userData instanceof BulletEntity) {
			e = (BulletEntity)(other.userData);
			tmpColor.set(e.getColor());
			e.setColor(Color.RED);
		}

		super.renderWorld();

		if (e != null) e.setColor(tmpColor);
	}

	@Override
	public void dispose () {
		super.dispose();
		movingBox = null;
	}
}

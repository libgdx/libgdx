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

import javax.print.attribute.standard.DateTimeAtCompleted;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ContactCache;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.tests.bullet.ContactCallbackTest2.TestContactListener;
import com.badlogic.gdx.utils.Array;

public class ContactCacheTest extends BaseBulletTest {
	public static class TestContactListener extends ContactListener {
		public Array<BulletEntity> entities;

		@Override
		public void onContactStarted (int userValue0, boolean match0, int userValue1, boolean match1) {
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.RED);
				Gdx.app.log(Float.toString(time), "Contact started " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.RED);
				Gdx.app.log(Float.toString(time), "Contact started " + userValue1);
			}
		}

		@Override
		public void onContactEnded (int userValue0, boolean match0, int userValue1, boolean match1) {
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.BLUE);
				Gdx.app.log(Float.toString(time), "Contact ended " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.BLUE);
				Gdx.app.log(Float.toString(time), "Contact ended " + userValue1);
			}
		}
	}

	public static class TestContactCache extends ContactCache {
		public Array<BulletEntity> entities;

		@Override
		public void onContactStarted (btPersistentManifold manifold, boolean match0, boolean match1) {
			final int userValue0 = manifold.getBody0().getUserValue();
			final int userValue1 = manifold.getBody1().getUserValue();
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.RED);
				Gdx.app.log(Float.toString(time), "Contact started " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.RED);
				Gdx.app.log(Float.toString(time), "Contact started " + userValue1);
			}
		}

		@Override
		public void onContactEnded (btCollisionObject colObj0, boolean match0, btCollisionObject colObj1, boolean match1) {
			final int userValue0 = colObj0.getUserValue();
			final int userValue1 = colObj1.getUserValue();
			if (match0) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue0));
				e.setColor(Color.BLUE);
				Gdx.app.log(Float.toString(time), "Contact ended " + userValue0);
			}
			if (match1) {
				final BulletEntity e = (BulletEntity)(entities.get(userValue1));
				e.setColor(Color.BLUE);
				Gdx.app.log(Float.toString(time), "Contact ended " + userValue1);
			}
		}
	}

	final int SPHERECOUNT_X = 4;
	final int SPHERECOUNT_Y = 1;
	final int SPHERECOUNT_Z = 4;

	final float SPHEREOFFSET_X = -2f;
	final float SPHEREOFFSET_Y = 10f;
	final float SPHEREOFFSET_Z = -2f;

	final boolean USE_CONTACT_CACHE = true;

	TestContactListener contactListener;
	TestContactCache contactCache;
	public static float time;

	@Override
	public void create () {
		super.create();

		final Model sphereModel = modelBuilder.createSphere(1f, 1f, 1f, 8, 8,
			new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)), Usage.Position
				| Usage.Normal);
		disposables.add(sphereModel);
		final BulletConstructor sphereConstructor = new BulletConstructor(sphereModel, 0.5f, new btSphereShape(0.5f));
		sphereConstructor.bodyInfo.setRestitution(1f);
		world.addConstructor("sphere", sphereConstructor);

		final Model sceneModel = objLoader.loadModel(Gdx.files.internal("data/scene.obj"));
		disposables.add(sceneModel);
		final BulletConstructor sceneConstructor = new BulletConstructor(sceneModel, 0f, new btBvhTriangleMeshShape(
			sceneModel.meshParts));
		sceneConstructor.bodyInfo.setRestitution(0.25f);
		world.addConstructor("scene", sceneConstructor);

		final BulletEntity scene = world.add("scene", (new Matrix4()).setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, -90));
		scene.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		scene.body.setContactCallbackFlag(2);

		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);

		for (int x = 0; x < SPHERECOUNT_X; x++) {
			for (int y = 0; y < SPHERECOUNT_Y; y++) {
				for (int z = 0; z < SPHERECOUNT_Z; z++) {
					final BulletEntity e = (BulletEntity)world.add("sphere", SPHEREOFFSET_X + x * 3f, SPHEREOFFSET_Y + y * 3f,
						SPHEREOFFSET_Z + z * 3f);
					e.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(),
						0.5f + 0.5f * (float)Math.random(), 1f);

					e.body.setContactCallbackFilter(2);
				}
			}
		}

		if (USE_CONTACT_CACHE) {
			contactCache = new TestContactCache();
			contactCache.entities = world.entities;
			contactCache.setCacheTime(0.5f);
		} else {
			contactListener = new TestContactListener();
			contactListener.entities = world.entities;
		}
		time = 0;
	}

	@Override
	public void update () {
		float delta = Gdx.graphics.getRawDeltaTime();
		time += delta;
		super.update();
		if (contactCache != null) contactCache.update(delta);
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}

	@Override
	public void dispose () {
		// Deleting the active contact listener, also disables that particular type of contact listener.
		if (contactListener != null) contactListener.dispose();
		if (contactCache != null) contactCache.dispose();
		contactCache = null;
		contactListener = null;

		super.dispose();
	}
}

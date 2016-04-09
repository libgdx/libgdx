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
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;

/** Based on FrustumCullingTest by Xoppa.
 * 
 * @author jsjolund */
public class PairCacheTest extends BaseBulletTest {

	final static float BOX_X_MIN = -25;
	final static float BOX_Y_MIN = -25;
	final static float BOX_Z_MIN = -25;

	final static float BOX_X_MAX = 25;
	final static float BOX_Y_MAX = 25;
	final static float BOX_Z_MAX = 25;

	final static float SPEED_X = 360f / 7f;
	final static float SPEED_Y = 360f / 19f;
	final static float SPEED_Z = 360f / 13f;

	final static int BOXCOUNT = 100;

	private boolean useFrustumCam = false;

	private btPairCachingGhostObject ghostObject;
	private BulletEntity ghostEntity;
	private btPersistentManifoldArray manifoldArray;

	private float angleX, angleY, angleZ;

	private ShapeRenderer shapeRenderer;

	private PerspectiveCamera frustumCam;
	private PerspectiveCamera overviewCam;

	@Override
	public void create () {
		super.create();

		instructions = "Tap to toggle view\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom";

		world.addConstructor("collisionBox", new BulletConstructor(world.getConstructor("box").model));

		// Create the entities
		final float dX = BOX_X_MAX - BOX_X_MIN;
		final float dY = BOX_Y_MAX - BOX_Y_MIN;
		final float dZ = BOX_Z_MAX - BOX_Z_MIN;
		for (int i = 0; i < BOXCOUNT; i++)
			world.add("collisionBox", BOX_X_MIN + dX * (float)Math.random(), BOX_Y_MIN + dY * (float)Math.random(),
					BOX_Z_MIN + dZ * (float)Math.random()).setColor(0.25f + 0.5f * (float)Math.random(),
					0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);

		manifoldArray = new btPersistentManifoldArray();
		disposables.add(manifoldArray);

		overviewCam = camera;
		overviewCam.position.set(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX);
		overviewCam.lookAt(Vector3.Zero);
		overviewCam.far = 150f;
		overviewCam.update();

		frustumCam = new PerspectiveCamera(camera.fieldOfView, camera.viewportWidth, camera.viewportHeight);
		frustumCam.far = Vector3.len(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX);
		frustumCam.update();

		final Model ghostModel = FrustumCullingTest.createFrustumModel(frustumCam.frustum.planePoints);
		disposables.add(ghostModel);

		// The ghost object does not need to be shaped as a camera frustum, it can have any collision shape.
		ghostObject = FrustumCullingTest.createFrustumObject(frustumCam.frustum.planePoints);
		disposables.add(ghostObject);

		world.add(ghostEntity = new BulletEntity(ghostModel, ghostObject, 0, 0, 0));
		disposables.add(ghostEntity);

		shapeRenderer = new ShapeRenderer();
		disposables.add(shapeRenderer);

	}

	@Override
	public BulletWorld createWorld () {
		// No need to use dynamics for this test
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btDefaultCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
		btCollisionWorld collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
		return new BulletWorld(collisionConfig, dispatcher, broadphase, null, collisionWorld);
	}

	@Override
	public void render () {
		final float dt = Gdx.graphics.getDeltaTime();
		ghostEntity.transform.idt();
		ghostEntity.transform.rotate(Vector3.X, angleX = (angleX + dt * SPEED_X) % 360);
		ghostEntity.transform.rotate(Vector3.Y, angleY = (angleY + dt * SPEED_Y) % 360);
		ghostEntity.transform.rotate(Vector3.Z, angleZ = (angleZ + dt * SPEED_Z) % 360);

		// Transform the ghost object
		ghostEntity.body.setWorldTransform(ghostEntity.transform);

		// Transform the frustum cam
		frustumCam.direction.set(0, 0, -1);
		frustumCam.up.set(0, 1, 0);
		frustumCam.position.set(0, 0, 0);
		frustumCam.rotate(ghostEntity.transform);
		frustumCam.update();

		super.render();

		// Find all overlapping pairs which contain the ghost object and draw lines between the collision points.
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);

		btBroadphasePairArray arr = world.broadphase.getOverlappingPairCache().getOverlappingPairArray();
		int numPairs = arr.size();
		for (int i = 0; i < numPairs; ++i) {
			manifoldArray.clear();

			btBroadphasePair pair = arr.at(i);
			btBroadphaseProxy proxy0 = btBroadphaseProxy.obtain(pair.getPProxy0().getCPointer(), false);
			btBroadphaseProxy proxy1 = btBroadphaseProxy.obtain(pair.getPProxy1().getCPointer(), false);

			btBroadphasePair collisionPair = world.collisionWorld.getPairCache().findPair(proxy0, proxy1);

			if (collisionPair == null) continue;

			btCollisionAlgorithm algorithm = collisionPair.getAlgorithm();
			if (algorithm != null) algorithm.getAllContactManifolds(manifoldArray);

			for (int j = 0; j < manifoldArray.size(); j++) {
				btPersistentManifold manifold = manifoldArray.at(j);

				boolean isFirstBody = manifold.getBody0() == ghostObject;
				int otherObjectIndex = isFirstBody ? manifold.getBody1().getUserValue() : manifold.getBody0().getUserValue();
				Color otherObjectColor = world.entities.get(otherObjectIndex).getColor();

				for (int p = 0; p < manifold.getNumContacts(); ++p) {
					btManifoldPoint pt = manifold.getContactPoint(p);

					if (pt.getDistance() < 0.f) {
						if (isFirstBody) {
							pt.getPositionWorldOnA(tmpV2);
							pt.getPositionWorldOnB(tmpV1);
						} else {
							pt.getPositionWorldOnA(tmpV1);
							pt.getPositionWorldOnB(tmpV2);
						}
						shapeRenderer.line(tmpV1.x, tmpV1.y, tmpV1.z, tmpV2.x, tmpV2.y, tmpV2.z, otherObjectColor, Color.WHITE);
					}
				}
			}
			btBroadphaseProxy.free(proxy0);
			btBroadphaseProxy.free(proxy1);
		}
		shapeRenderer.end();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		useFrustumCam = !useFrustumCam;
		if (useFrustumCam)
			camera = frustumCam;
		else
			camera = overviewCam;
		return true;
	}

	@Override
	public void update () {
		super.update();
		// Not using dynamics, so update the collision world manually
		world.collisionWorld.performDiscreteCollisionDetection();
	}
}

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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.btBroadphasePair;
import com.badlogic.gdx.physics.bullet.btBroadphasePairArray;
import com.badlogic.gdx.physics.bullet.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.btCompoundShape;
import com.badlogic.gdx.physics.bullet.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.btManifoldArray;
import com.badlogic.gdx.physics.bullet.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.gdxBulletJNI;
import com.badlogic.gdx.utils.Array;

/** @author Xoppa */
public class FrustumCullingTest extends BaseBulletTest {
	/** Only show entities inside the frustum */
	final static int CULL_FRUSTUM = 1;
	/** Transform the render cam with the frustum */
	final static int FRUSTUM_CAM = 2;
	
	final static boolean USE_BULLET_FRUSTUM_CULLING = true;
	
	int state = 0; // 0 = No culling, look from above
	
	final static int BOXCOUNT = 200;
	
	final static float BOX_X_MIN = -25;
	final static float BOX_Y_MIN = -25;
	final static float BOX_Z_MIN = -25;
	
	final static float BOX_X_MAX = 25;
	final static float BOX_Y_MAX = 25;
	final static float BOX_Z_MAX = 25;
	
	final static float SPEED_X = 360f/7f;
	final static float SPEED_Y = 360f/19f;
	final static float SPEED_Z = 360f/13f;
	
	final static Vector3 tmpV = new Vector3();
	final static Matrix4 tmpM = new Matrix4();
	
	final static int ptrs[] = new int[512];
	final static Array<btCollisionObject> visibleObjects = new Array<btCollisionObject>(); 
	
	public static btPairCachingGhostObject createFrustumObject(final Vector3... points) {
		final btPairCachingGhostObject result = new TestPairCachingGhostObject();
		final boolean USE_COMPOUND = true;
		// Using a compound shape is not necessary, but it's good practice to create shapes around the center.
		if (USE_COMPOUND) {
			final Vector3 centerNear = new Vector3(points[2]).sub(points[0]).scl(0.5f).add(points[0]);
			final Vector3 centerFar = new Vector3(points[6]).sub(points[4]).scl(0.5f).add(points[4]);
			final Vector3 center = new Vector3(centerFar).sub(centerNear).scl(0.5f).add(centerNear);
			final btConvexHullShape hullShape = new btConvexHullShape();
			for (int i = 0; i < points.length; i++)
				hullShape.addPoint(tmpV.set(points[i]).sub(center));
			final btCompoundShape shape = new btCompoundShape();
			shape.addChildShape(tmpM.setToTranslation(center), hullShape);
			result.setCollisionShape(shape);
		} else {
			final btConvexHullShape shape = new btConvexHullShape();
			for (int i = 0; i < points.length; i++)
				shape.addPoint(points[i]);
			result.setCollisionShape(shape);
		}
		result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		return result;
	}
	
	public static Array<BulletEntity> getEntitiesCollidingWithObject(final BulletWorld world, final btCollisionObject object, final Array<BulletEntity> out, final btManifoldArray tmpArr) {
		// Fetch the array of contacts
		btBroadphasePairArray arr = world.broadphase.getOverlappingPairCache().getOverlappingPairArray();
		// Get the user values (which are indices in the entities array) of all objects colliding with the object
		final int n = arr.getCollisionObjectsValue(ptrs, object);
		// Fill the array of entities
		out.clear();
		for (int i = 0; i < n; i++)
			out.add(world.entities.get(ptrs[i]));
		return out;
	}
	
	public static Model createFrustumModel(final Vector3... p) {
		return ModelBuilder.createFromMesh(new float[] {
				p[0].x, p[0].y, p[0].z, 0, 0, 1, p[1].x, p[1].y, p[1].z, 0, 0, 1, p[2].x, p[2].y, p[2].z, 0, 0, 1, p[3].x, p[3].y, p[3].z, 0, 0, 1, // near
				p[4].x, p[4].y, p[4].z, 0, 0, -1, p[5].x, p[5].y, p[5].z, 0, 0, -1, p[6].x, p[6].y, p[6].z, 0, 0, -1, p[7].x, p[7].y, p[7].z, 0, 0, -1},// far
				new VertexAttribute[] { new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal")},
				new short[] {0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7},
				GL10.GL_LINES, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)));
	}
	
	private float angleX, angleY, angleZ;
	private btPairCachingGhostObject frustumObject;
	private BulletEntity frustumEntity;
	private final Array<BulletEntity> visibleEntities = new Array<BulletEntity>();
	private btManifoldArray tempManifoldArr;
	private PerspectiveCamera frustumCam;
	private PerspectiveCamera overviewCam;

	@Override
	public void create () {
		super.create();
		
		instructions = "Tap to toggle view\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom";
		
		tempManifoldArr = new btManifoldArray();

		world.addConstructor("collisionBox", new BulletConstructor(world.getConstructor("box").model));
		
		// Create the entities
		final float dX = BOX_X_MAX - BOX_X_MIN;
		final float dY = BOX_Y_MAX - BOX_Y_MIN;
		final float dZ = BOX_Z_MAX - BOX_Z_MIN;
		for (int i = 0; i < BOXCOUNT; i++)
			world.add("collisionBox", 
					BOX_X_MIN + dX * (float)Math.random(), 
					BOX_Y_MIN + dY * (float)Math.random(), 
					BOX_Z_MIN + dZ * (float)Math.random()
				).setColor(Color.GRAY);
		
		frustumCam = new PerspectiveCamera(camera.fieldOfView, camera.viewportWidth, camera.viewportHeight);
		frustumCam.far = Vector3.len(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX);
		frustumCam.update();
		
		overviewCam = camera;
		overviewCam.position.set(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX);
		overviewCam.lookAt(Vector3.Zero);
		overviewCam.far = 150f;
		overviewCam.update();
		
		final Model frustumModel = createFrustumModel(frustumCam.frustum.planePoints);
		disposables.add(frustumModel);
		frustumObject = createFrustumObject(frustumCam.frustum.planePoints);
		world.add(frustumEntity = new BulletEntity(frustumModel, frustumObject, 0, 0, 0));
		frustumEntity.setColor(Color.BLUE);
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
	public void update () {
		super.update();
		// Not using dynamics, so update the collision world manually
		if (USE_BULLET_FRUSTUM_CULLING) {
			if (world.performanceCounter != null)
				world.performanceCounter.start();
			world.collisionWorld.performDiscreteCollisionDetection();
			if (world.performanceCounter != null)
				world.performanceCounter.stop();
		}
	}
	
	@Override
	public void render () {
		final float dt = Gdx.graphics.getDeltaTime();
		frustumEntity.transform.idt();
		frustumEntity.transform.rotate(Vector3.X, angleX = (angleX + dt * SPEED_X) % 360);
		frustumEntity.transform.rotate(Vector3.Y, angleY = (angleY + dt * SPEED_Y) % 360);
		frustumEntity.transform.rotate(Vector3.Z, angleZ = (angleZ + dt * SPEED_Z) % 360);

		// Transform the ghost object
		frustumEntity.body.setWorldTransform(frustumEntity.transform);
		// Transform the frustum cam
		frustumCam.direction.set(0, 0, -1);
		frustumCam.up.set(0, 1, 0);
		frustumCam.position.set(0,0,0);
		frustumCam.rotate(frustumEntity.transform);
		frustumCam.update();

		super.render();
		
		performance.append(" visible: ").append(visibleEntities.size);
	}
	
	@Override
	protected void renderWorld () {
		if (world.performanceCounter != null)
			world.performanceCounter.start();
		if (USE_BULLET_FRUSTUM_CULLING)
			getEntitiesCollidingWithObject(world, frustumObject, visibleEntities, tempManifoldArr);
		else {
			visibleEntities.clear();
			for (int i = 0; i < world.entities.size; i++) {
				final BulletEntity e = world.entities.get(i);
				if (e == frustumEntity)
					continue;
				e.modelInstance.transform.getTranslation(tmpV);
				if (frustumCam.frustum.sphereInFrustum(tmpV, 1))
					visibleEntities.add(e);
			}
		}
		if (world.performanceCounter != null)
			world.performanceCounter.stop();
		
		for (int i = 0; i < visibleEntities.size; i++)
			visibleEntities.get(i).setColor(Color.RED);

		modelBatch.begin(camera);
		if ((state & CULL_FRUSTUM) == CULL_FRUSTUM) {
			world.render(modelBatch, lights, visibleEntities);
			world.render(modelBatch, lights, frustumEntity);
		} else
			world.render(modelBatch, lights);
		modelBatch.end();
		
		for (int i = 0; i < visibleEntities.size; i++)
			visibleEntities.get(i).setColor(Color.GRAY);
	}
	
	@Override
	protected void beginRender (boolean lighting) {
		super.beginRender(false);
	}
	
	@Override
	public void dispose () {
		frustumObject = null;
		
		super.dispose();
		
		if (tempManifoldArr != null)
			tempManifoldArr.dispose();
		tempManifoldArr = null;
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		state = (state + 1) % 3;
		if ((state & FRUSTUM_CAM) == FRUSTUM_CAM)
			camera = frustumCam;
		else
			camera = overviewCam;
		return true;
	}
	
	// Simple helper class to keep a reference to the collision shape
	public static class TestPairCachingGhostObject extends btPairCachingGhostObject {
		public btCollisionShape shape; 
		@Override
		public void setCollisionShape (btCollisionShape collisionShape) {
			shape = collisionShape;
			super.setCollisionShape(collisionShape);
		}
		@Override
		public void dispose () {
			super.dispose();
			if (shape != null)
				shape.dispose();
			shape = null;
		}
	}
}
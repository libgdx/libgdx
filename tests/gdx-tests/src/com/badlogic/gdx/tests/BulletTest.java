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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.btBoxShape;
import com.badlogic.gdx.physics.bullet.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.btCollisionShape;
import com.badlogic.gdx.physics.bullet.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.btMotionState;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** @author xoppa */
public class BulletTest extends GdxTest {

	static {
		new SharedLibraryLoader().load("gdx-bullet");
	}

	btDefaultCollisionConfiguration collisionConfiguration;
	btCollisionDispatcher dispatcher;
	btDbvtBroadphase broadphase;
	btSequentialImpulseConstraintSolver solver;
	btDiscreteDynamicsWorld dynamicsWorld;
	final Vector3 gravity = new Vector3(0, -10, 0);

	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 5;
	final int BOXCOUNT_Z = 1;

	final float BOXOFFSET_X = -2.5f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = 0f;

	BulletBody.ConstructInfo groundInfo;
	BulletBody.ConstructInfo boxInfo;

	Array<BulletBody> bodies = new Array<BulletBody>(BOXCOUNT_X * BOXCOUNT_Y * BOXCOUNT_Z);

	final float lightAmbient[] = new float[] {0.4f, 0.4f, 0.4f, 1f};
	final float lightPosition[] = new float[] {10f, 10f, 0f, 100f};
	final float lightDiffuse[] = new float[] {1f, 1f, 1f, 1f};

	PerspectiveCamera camera;
	Mesh groundMesh;
	Mesh boxMesh;

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		if (width > height)
			camera = new PerspectiveCamera(67f, 3f * width / height, 3f);
		else
			camera = new PerspectiveCamera(67f, 3f, 3f * height / width);
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0, 0, 0);
		camera.update();
	}

	@Override
	public void render () {
		dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5);

		camera.apply(Gdx.gl10);
		GL10 gl = Gdx.gl10;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);

		for (int i = 0; i < bodies.size; i++) {
			final BulletBody body = bodies.get(i);
			gl.glPushMatrix();
			gl.glMultMatrixf(body.transform.val, 0);
			body.mesh.render(GL10.GL_TRIANGLE_STRIP);
			gl.glPopMatrix();
		}
	}

	@Override
	public void create () {
		Gdx.input.setInputProcessor(this);

		// Create some simple meshes
		groundMesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_color"));
		groundMesh.setVertices(new float[] {20f, 0f, 20f, Color.toFloatBits(128, 128, 128, 255), 20f, 0f, -20f,
			Color.toFloatBits(128, 128, 128, 255), -20f, 0f, 20f, Color.toFloatBits(128, 128, 128, 255), -20f, 0f, -20f,
			Color.toFloatBits(128, 128, 128, 255)});
		groundMesh.setIndices(new short[] {0, 1, 2, 1, 2, 3});

		boxMesh = new Mesh(true, 8, 36, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_color"));
		boxMesh.setVertices(new float[] {0.5f, 0.5f, 0.5f, Color.toFloatBits(255, 0, 0, 255), 0.5f, 0.5f, -0.5f,
			Color.toFloatBits(255, 0, 0, 255), -0.5f, 0.5f, 0.5f, Color.toFloatBits(255, 0, 0, 255), -0.5f, 0.5f, -0.5f,
			Color.toFloatBits(255, 0, 0, 255), 0.5f, -0.5f, 0.5f, Color.toFloatBits(255, 0, 0, 255), 0.5f, -0.5f, -0.5f,
			Color.toFloatBits(255, 0, 0, 255), -0.5f, -0.5f, 0.5f, Color.toFloatBits(255, 0, 0, 255), -0.5f, -0.5f, -0.5f,
			Color.toFloatBits(255, 0, 0, 255)});
		boxMesh.setIndices(new short[] {0, 1, 2, 1, 2, 3, // top
			4, 5, 6, 5, 6, 7, // bottom
			0, 2, 4, 4, 6, 2, // front
			1, 3, 5, 5, 7, 3, // back
			2, 3, 6, 6, 7, 3, // left
			0, 1, 4, 4, 5, 1 // right
			});

		// Init the physics
		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		broadphase = new btDbvtBroadphase();
		solver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		dynamicsWorld.setGravity(gravity);

		// Init the construct info
		groundInfo = new BulletBody.ConstructInfo(0f, 40f, 0f, 40f);
		boxInfo = new BulletBody.ConstructInfo(1f, 1f, 1f, 1f);

		// Create the bodies
		BulletBody body = new BulletBody(groundMesh, groundInfo, 0f, 0f, 0f);
		bodies.add(body);
		dynamicsWorld.addRigidBody(body.body);

		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					body = new BulletBody(boxMesh, boxInfo, BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z);
					bodies.add(body);
					dynamicsWorld.addRigidBody(body.body);
				}
			}
		}
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		// Shoot a box
		Ray ray = camera.getPickRay(screenX, screenY);
		BulletBody body = new BulletBody(boxMesh, boxInfo, ray.origin.x, ray.origin.y, ray.origin.z);
		bodies.add(body);
		dynamicsWorld.addRigidBody(body.body);
		body.body.applyCentralImpulse(ray.direction.mul(30f));

		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		super.dispose();
		// Don't rely on the GC to finalize
		for (int i = 0; i < bodies.size; i++) {
			final BulletBody body = bodies.get(i);
			dynamicsWorld.removeRigidBody(body.body);
			body.dispose();
		}
		bodies.clear();

		groundInfo.dispose();
		groundInfo = null;
		boxInfo.dispose();
		boxInfo = null;

		dynamicsWorld.delete();
		dynamicsWorld = null;
		solver.delete();
		solver = null;
		broadphase.delete();
		broadphase = null;
		dispatcher.delete();
		dispatcher = null;
		collisionConfiguration.delete();
		collisionConfiguration = null;
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

	static class BulletBody extends btMotionState implements Disposable {
		public final Matrix4 transform = new Matrix4();
		public btRigidBody body;
		public Mesh mesh;

		public BulletBody (final Mesh mesh, final btRigidBodyConstructionInfo bodyInfo, final float x, final float y, final float z) {
			this.mesh = mesh;
			transform.idt().translate(x, y, z);
			body = new btRigidBody(bodyInfo);
			body.setMotionState(this);
		}

		public BulletBody (final Mesh mesh, final ConstructInfo constructInfo, final float x, final float y, final float z) {
			this(mesh, constructInfo.bodyInfo, x, y, z);
		}

		@Override
		public void getWorldTransform (final btTransform worldTrans) {
			worldTrans.setFromOpenGLMatrix(transform.val);
		}

		@Override
		public void setWorldTransform (final btTransform worldTrans) {
			worldTrans.getOpenGLMatrix(transform.val);
		}

		@Override
		public void dispose () {
			// Don't rely on the GC
			delete();
			if (body != null) body.delete();
			// And remove the reference
			body = null;
		}

		public static class ConstructInfo implements Disposable {
			public btRigidBodyConstructionInfo bodyInfo;
			public btCollisionShape shape;

			public ConstructInfo (final float mass, final float width, final float height, final float depth) {
				// We can safely use tmp to pass the dimensions
				shape = new btBoxShape(Vector3.tmp.set(width * 0.5f, height * 0.5f, depth * 0.5f));
				Vector3 localInertia;
				if (mass == 0)
					localInertia = Vector3.Zero;
				else {
					// Again we can safely use tmp to pass the local inertia
					shape.calculateLocalInertia(mass, Vector3.tmp);
					localInertia = Vector3.tmp;
				}
				// For now just pass null as the motionstate, we'll add that in the body itself
				bodyInfo = new btRigidBodyConstructionInfo(mass, null, shape, localInertia);
			}

			@Override
			public void dispose () {
				// Don't rely on the GC
				if (bodyInfo != null) bodyInfo.delete();
				if (shape != null) shape.delete();
				// Remove references so the GC can do it's work
				bodyInfo = null;
				shape = null;
			}
		}
	}
}

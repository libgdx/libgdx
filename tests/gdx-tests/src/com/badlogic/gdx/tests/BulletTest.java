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
import com.badlogic.gdx.math.collision.BoundingBox;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** @author xoppa */
public class BulletTest extends GdxTest {

	static {
		new SharedLibraryLoader().load("gdx-bullet");
	}

	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 5;
	final int BOXCOUNT_Z = 1;

	final float BOXOFFSET_X = -2.5f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = 0f;

	final float lightAmbient[] = new float[] {0.4f, 0.4f, 0.4f, 1f};
	final float lightPosition[] = new float[] {10f, 10f, 0f, 100f};
	final float lightDiffuse[] = new float[] {1f, 1f, 1f, 1f};

	PerspectiveCamera camera;
	World world;

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

		camera.apply(Gdx.gl10);
		
		world.update();
	}

	@Override
	public void create () {
		world = new World();
		
		Gdx.input.setInputProcessor(this);

		// Create some simple meshes
		final Mesh groundMesh = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
			Usage.ColorPacked, 4, "a_color"));
		groundMesh.setVertices(new float[] {20f, 0f, 20f, Color.toFloatBits(128, 128, 128, 255), 20f, 0f, -20f,
			Color.toFloatBits(128, 128, 128, 255), -20f, 0f, 20f, Color.toFloatBits(128, 128, 128, 255), -20f, 0f, -20f,
			Color.toFloatBits(128, 128, 128, 255)});
		groundMesh.setIndices(new short[] {0, 1, 2, 1, 2, 3});

		final Mesh boxMesh = new Mesh(true, 8, 36, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(
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

		// Add the constructers
		world.constructors.put("ground", new Entity.ConstructInfo(groundMesh, 0f)); // mass = 0: static body
		world.constructors.put("box", new Entity.ConstructInfo(boxMesh, 1f)); // mass = 1kg: dynamic body

		// Create the entities
		world.add("ground", 0f, 0f, 0f);

		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					world.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z);
				}
			}
		}
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		// Shoot a box
		Ray ray = camera.getPickRay(screenX, screenY);
		Entity entity = world.add("box", ray.origin.x, ray.origin.y, ray.origin.z);
		entity.body.applyCentralImpulse(ray.direction.mul(30f));

		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void dispose () {
		world.dispose();
		
		super.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
	
	static class World implements Disposable {
		public ObjectMap<String, Entity.ConstructInfo> constructors = new ObjectMap<String, Entity.ConstructInfo>();
		public Array<Entity> entities = new Array<Entity>();
		
		private final btDefaultCollisionConfiguration collisionConfiguration;
		private final btCollisionDispatcher dispatcher;
		private final btDbvtBroadphase broadphase;
		private final btSequentialImpulseConstraintSolver solver;
		public final btDiscreteDynamicsWorld dynamicsWorld;
		final Vector3 gravity = new Vector3(0, -10, 0);
		
		public World() {
			collisionConfiguration = new btDefaultCollisionConfiguration();
			dispatcher = new btCollisionDispatcher(collisionConfiguration);
			broadphase = new btDbvtBroadphase();
			solver = new btSequentialImpulseConstraintSolver();
			dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
			dynamicsWorld.setGravity(gravity);
		}
		
		public void add(final Entity entity) {
			entities.add(entity);
			dynamicsWorld.addRigidBody(entity.body);
		}
		
		public Entity add(final String type, float x, float y, float z) {
			final Entity entity = new Entity(constructors.get(type), x, y, z);
			add(entity);
			return entity;
		}
		
		public void update () {
			dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5);

			GL10 gl = Gdx.gl10;

			for (int i = 0; i < entities.size; i++) {
				final Entity entity = entities.get(i);
				gl.glPushMatrix();
				gl.glMultMatrixf(entity.worldTransform.transform.val, 0);
				entity.mesh.render(GL10.GL_TRIANGLES);
				gl.glPopMatrix();
			}
		}
		
		@Override
		public void dispose () {
			for (int i = 0; i < entities.size; i++) {
				final Entity entity = entities.get(i);
				dynamicsWorld.removeRigidBody(entity.body);
				entity.dispose();
			}
			entities.clear();
			
			for (Entity.ConstructInfo constructor : constructors.values()) {
				constructor.dispose();
			}
			constructors.clear();
			
			dynamicsWorld.delete();
			solver.delete();
			broadphase.delete();
			dispatcher.delete();
			collisionConfiguration.delete();
		}
	}

	static class Entity implements Disposable {
		public WorldTransform worldTransform;
		public btRigidBody body;
		public Mesh mesh;

		public Entity (final Mesh mesh, final btRigidBodyConstructionInfo bodyInfo, final float x, final float y, final float z) {
			this.mesh = mesh;
			worldTransform = new WorldTransform();
			worldTransform.transform.idt().translate(x, y, z);
			body = new btRigidBody(bodyInfo);
			body.setMotionState(worldTransform);
		}

		public Entity (final ConstructInfo constructInfo, final float x, final float y, final float z) {
			this(constructInfo.mesh, constructInfo.bodyInfo, x, y, z);
		}

		@Override
		public void dispose () {
			// Don't rely on the GC
			if (worldTransform != null) worldTransform.dispose();
			if (body != null) body.delete();
			// And remove the reference
			worldTransform = null;
			body = null;
		}
		
		public static class WorldTransform extends btMotionState implements Disposable {
			public final Matrix4 transform = new Matrix4();
			
			/**
			 * For dynamic and static bodies this method is called by bullet once to get the initial state of the body.
			 * For kinematic bodies this method is called on every update.
			 */
			@Override
			public void getWorldTransform (final btTransform worldTrans) {
				worldTrans.setFromOpenGLMatrix(transform.val);
			}

			/**
			 * For dynamic bodies this method is called by bullet every update to inform about the new position and rotation.
			 */
			@Override
			public void setWorldTransform (final btTransform worldTrans) {
				worldTrans.getOpenGLMatrix(transform.val);
			}
			
			@Override
			public void dispose () {
				delete();
			}
		}

		public static class ConstructInfo implements Disposable {
			public btRigidBodyConstructionInfo bodyInfo;
			public btCollisionShape shape;
			public Mesh mesh;
			
			private void create (final Mesh mesh, final float mass, final float width, final float height, final float depth) {
				this.mesh = mesh;
				
				// Create a simple boxshape
				shape = new btBoxShape(Vector3.tmp.set(width * 0.5f, height * 0.5f, depth * 0.5f));
				
				// Calculate the local inertia, bodies with no mass are static
				Vector3 localInertia;
				if (mass == 0)
					localInertia = Vector3.Zero;
				else {
					shape.calculateLocalInertia(mass, Vector3.tmp);
					localInertia = Vector3.tmp;
				}
				
				// For now just pass null as the motionstate, we'll add that to the body in the entity itself
				bodyInfo = new btRigidBodyConstructionInfo(mass, null, shape, localInertia);
			}
			
			public ConstructInfo (final Mesh mesh, final float mass, final float width, final float height, final float depth) {
				create(mesh, mass, width, height, depth);
			}
			
			public ConstructInfo (final Mesh mesh, final float mass) {
				final BoundingBox boundingBox = mesh.calculateBoundingBox();
				final Vector3 dimensions = boundingBox.getDimensions();
				create(mesh, mass, dimensions.x, dimensions.y, dimensions.z);
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

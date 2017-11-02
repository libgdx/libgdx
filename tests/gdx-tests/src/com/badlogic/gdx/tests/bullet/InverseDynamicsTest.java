package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.extras.SWIGTYPE_p_btMultiBody;
import com.badlogic.gdx.physics.bullet.inversedynamics.InverseDynamics;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Array;

public class InverseDynamicsTest extends BulletTest {
	ModelBatch modelBatch;
	Environment lights;
	ModelBuilder modelBuilder = new ModelBuilder();

	btCollisionConfiguration collisionConfiguration;
	btCollisionDispatcher dispatcher;
	btBroadphaseInterface broadphase;
	btConstraintSolver solver;
	btDynamicsWorld collisionWorld;
	Vector3 gravity = new Vector3(0, -9.81f, 0);
	Vector3 tempVector = new Vector3();

	Array<Model> models = new Array<Model>();
	Array<ModelInstance> instances = new Array<ModelInstance>();
	Array<btDefaultMotionState> motionStates = new Array<btDefaultMotionState>();
	Array<btRigidBody.btRigidBodyConstructionInfo> bodyInfos = new Array<btRigidBody.btRigidBodyConstructionInfo>();
	Array<btCollisionShape> shapes = new Array<btCollisionShape>();
	Array<btRigidBody> bodies = new Array<btRigidBody>();

	InverseDynamics inverseDynamics;

	@Override
	public void create () {
		super.create();
		instructions = "Swipe for next test";

		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.7f));

		// Set up the camera
		final float width = Gdx.graphics.getWidth();
		final float height = Gdx.graphics.getHeight();
		if (width > height)
			camera = new PerspectiveCamera(67f, 3f * width / height, 3f);
		else
			camera = new PerspectiveCamera(67f, 3f, 3f * height / width);
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0, 0, 0);
		camera.update();
		// Create the model batch
		modelBatch = new ModelBatch();
		// Create some basic models
		final Model groundModel = modelBuilder.createRect(
			20f,
			0f,
			-20f,
			-20f,
			0f,
			-20f,
			-20f,
			0f,
			20f,
			20f,
			0f,
			20f,
			0,
			1,
			0,
			new Material(ColorAttribute.createDiffuse(Color.BLUE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute
				.createShininess(16f)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		models.add(groundModel);
		final Model sphereModel = modelBuilder.createSphere(
			1f,
			1f,
			1f,
			10,
			10,
			new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute
				.createShininess(64f)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		models.add(sphereModel);
		// Load the bullet library
		BaseBulletTest.init(); // Normally use: Bullet.init();
		// Create the bullet world
		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		broadphase = new btDbvtBroadphase();
		solver = new btSequentialImpulseConstraintSolver();
		collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		collisionWorld.setGravity(gravity);
		// Create the shapes and body construction infos
		btCollisionShape groundShape = new btBoxShape(tempVector.set(20, 0, 20));
		shapes.add(groundShape);
		btRigidBody.btRigidBodyConstructionInfo groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero);
		bodyInfos.add(groundInfo);
		btCollisionShape sphereShape = new btSphereShape(0.5f);
		shapes.add(sphereShape);
		sphereShape.calculateLocalInertia(1f, tempVector);
		btRigidBody.btRigidBodyConstructionInfo sphereInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, null, sphereShape, tempVector);
		bodyInfos.add(sphereInfo);
		// Create the ground
		ModelInstance ground = new ModelInstance(groundModel);
		instances.add(ground);
		btDefaultMotionState groundMotionState = new btDefaultMotionState();
		groundMotionState.setWorldTransform(ground.transform);
		motionStates.add(groundMotionState);
		btRigidBody groundBody = new btRigidBody(groundInfo);
		groundBody.setMotionState(groundMotionState);
		bodies.add(groundBody);
		collisionWorld.addRigidBody(groundBody);
		// Create the spheres
		for (float x = -10f; x <= 10f; x += 2f) {
			for (float y = 5f; y <= 15f; y += 2f) {
				for (float z = 0f; z <= 0f; z += 2f) {
					ModelInstance sphere = new ModelInstance(sphereModel);
					instances.add(sphere);
					sphere.transform.trn(x + 0.1f * MathUtils.random(), y + 0.1f * MathUtils.random(), z + 0.1f * MathUtils.random());
					btDefaultMotionState sphereMotionState = new btDefaultMotionState();
					sphereMotionState.setWorldTransform(sphere.transform);
					motionStates.add(sphereMotionState);
					btRigidBody sphereBody = new btRigidBody(sphereInfo);
					sphereBody.setMotionState(sphereMotionState);
					bodies.add(sphereBody);
					collisionWorld.addRigidBody(sphereBody);
				}
			}
		}
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		fpsCounter.put(Gdx.graphics.getFramesPerSecond());

		performanceCounter.tick();
		performanceCounter.start();
		((btDynamicsWorld)collisionWorld).stepSimulation(Gdx.graphics.getDeltaTime(), 5);
		performanceCounter.stop();

		int c = motionStates.size;
		for (int i = 0; i < c; i++) {
			motionStates.get(i).getWorldTransform(instances.get(i).transform);
		}

		modelBatch.begin(camera);
		modelBatch.render(instances, lights);
		modelBatch.end();

		performance.setLength(0);
		performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
			.append((int)(performanceCounter.load.value * 100f)).append("%");
	}

	@Override
	public void dispose () {
		collisionWorld.dispose();
		solver.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfiguration.dispose();

		for (btRigidBody body : bodies)
			body.dispose();
		bodies.clear();
		for (btDefaultMotionState motionState : motionStates)
			motionState.dispose();
		motionStates.clear();
		for (btCollisionShape shape : shapes)
			shape.dispose();
		shapes.clear();
		for (btRigidBody.btRigidBodyConstructionInfo info : bodyInfos)
			info.dispose();
		bodyInfos.clear();

		modelBatch.dispose();
		instances.clear();
		for (Model model : models)
			model.dispose();
		models.clear();
	}
}

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class BulletTest {
	static {
		new SharedLibraryLoader().load("gdx-bullet");
	}

	public static void main(String[] args) {
		testMathTypes();
		testBounce();
	}

	public static void testMathTypes() {
		Matrix3 m = new Matrix3();
		m.idt();
		m.val[0] = 1;
		m.val[1] = 2;
		m.val[2] = 3;
		m.val[3] = 4;
		m.val[4] = 5;
		m.val[5] = 6;
		m.val[6] = 7;
		m.val[7] = 8;
		m.val[8] = 9;

		Quaternion q = new Quaternion();

		btTransform t = new btTransform();
		t.setIdentity();
		System.out.println(t.getOrigin());
		System.out.println(t.getBasis());
		System.out.println();

		t.setOrigin(new Vector3(1, 2, 3));
		t.setBasis(m);
		System.out.println(t.getOrigin());
		System.out.println(t.getBasis());
		System.out.println();

		System.out.println(t.getRotation());
		t.setRotation(q);
		System.out.println(t.getRotation());

		t.delete();
	}

	public static void testBounce() {

		// Create the collision and dynamics worlds
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(
				collisionConfiguration);
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld dynamicsWorld = new btDiscreteDynamicsWorld(
				dispatcher, broadphase, solver, collisionConfiguration);

		// Configure gravity at 10 ms/s/s toward -z
		dynamicsWorld.setGravity(new Vector3(0f, 0f, -10f));

		// Rigid object at +100m
		btRigidBody dropBody = createDropObject(100);
		dynamicsWorld.addRigidBody(dropBody);

		// Ground plane at +0m
		btRigidBody planeBody = createGroundPlane();
		dynamicsWorld.addRigidBody(planeBody);

		// Simulate 10 seconds
		int durationSeconds = 10;
		int hertz = 60;
		float timeStep = 1f / (float) hertz;
		for (int i = 0; i < hertz * durationSeconds; i++) {
			dynamicsWorld.stepSimulation(timeStep);
			if (i % hertz == 0) {
				printZ(dropBody);
			}
		}

		// Clean up
		dynamicsWorld.removeRigidBody(planeBody);
		planeBody.delete();
		dynamicsWorld.removeRigidBody(dropBody);
		dropBody.delete();
		dynamicsWorld.delete();
		solver.delete();
		broadphase.delete();
		dispatcher.delete();
		collisionConfiguration.delete();
	}

	private static btRigidBody createGroundPlane() {

		btTransform startTransform = new btTransform();
		startTransform.setIdentity();

		// Ground is the x,y plane
		btRigidBody body = new btRigidBody(0, new btDefaultMotionState(
				startTransform),
				new btStaticPlaneShape(new Vector3(0, 0, 1), 0), new Vector3(0,
						0, 0));

		// Transform values were copied into the rigid object, delete here
		startTransform.delete();
		
		return body;
	}

	private static btRigidBody createDropObject(float height) {

		float mass = 100f;

		btTransform startTransform = new btTransform();
		startTransform.setIdentity();
		startTransform.setOrigin(new Vector3(0, 0, height));

		Vector3 localInertia = new Vector3();

		btBoxShape shape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
		shape.calculateLocalInertia(mass, localInertia);

		btRigidBody body = new btRigidBody(mass, new btDefaultMotionState(
				startTransform), shape, localInertia);

		// Transform values were copied into the rigid object, delete here
		startTransform.delete();
		
		return body;
	}

	private static void printZ(btRigidBody body) {
		btTransform transform = body.getWorldTransform();
		System.out.println(transform.getOrigin().z);
		// We didn't create the transform, don't delete it
	}
}

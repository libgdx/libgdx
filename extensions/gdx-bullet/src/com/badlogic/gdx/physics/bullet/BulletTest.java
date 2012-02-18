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
		m.vals[0] = 1;
		m.vals[1] = 2;
		m.vals[2] = 3;
		m.vals[3] = 4;
		m.vals[4] = 5;
		m.vals[5] = 6;
		m.vals[6] = 7;
		m.vals[7] = 8;
		m.vals[8] = 9;

		Quaternion q = new Quaternion(Quaternion.idt());

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

		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(
				collisionConfiguration);
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();

		btDiscreteDynamicsWorld dynamicsWorld = new btDiscreteDynamicsWorld(
				dispatcher, broadphase, solver, collisionConfiguration);
		dynamicsWorld.setGravity(new Vector3(0f, 0f, -10f));

		btRigidBody dropBody = createDropObject(100);
		dynamicsWorld.addRigidBody(dropBody);

		btRigidBody planeBody = createGroundPlane();
		dynamicsWorld.addRigidBody(planeBody);

		int durationSeconds = 10;
		int hertz = 60;
		float timeStep = 1f / (float) hertz;
		for (int i = 0; i < hertz * durationSeconds; i++) {
			dynamicsWorld.stepSimulation(timeStep);
			if (i % hertz == 0) {
				printZ(dropBody);
			}
		}

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

		return body;
	}

	private static btRigidBody createDropObject(float height) {

		float mass = 100f;

		// 20 meters in the air (+z)
		btTransform startTransform = new btTransform();
		startTransform.setIdentity();
		startTransform.setOrigin(new Vector3(0, 0, height));

		Vector3 localInertia = new Vector3();

		btBoxShape shape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
		shape.calculateLocalInertia(mass, localInertia);

		btRigidBody body = new btRigidBody(mass, new btDefaultMotionState(
				startTransform), shape, localInertia);

		return body;
	}

	private static void printZ(btRigidBody body) {
		btTransform t = body.getWorldTransform();
		Vector3 v = t.getOrigin();
		System.out.println(v.z);
		t.delete();
	}
}

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

package com.badlogic.gdx.physics.box2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.Pool;

/** The world class manages all physics entities, dynamic simulation, and asynchronous queries. The world also contains efficient
 * memory management facilities.
 * @author mzechner */
public final class World implements Disposable {
	/** pool for bodies **/
	protected final Pool<Body> freeBodies = new Pool<Body>(100, 200) {
		@Override
		protected Body newObject () {
			return new Body(World.this, 0);
		}
	};

	/** pool for fixtures **/
	protected final Pool<Fixture> freeFixtures = new Pool<Fixture>(100, 200) {
		@Override
		protected Fixture newObject () {
			return new Fixture(null, 0);
		}
	};

	/** the address of the world instance **/
	private final long addr;

	/** all known bodies **/
	protected final LongMap<Body> bodies = new LongMap<Body>(100);

	/** all known fixtures **/
	protected final LongMap<Fixture> fixtures = new LongMap<Fixture>(100);

	/** all known joints **/
	protected final LongMap<Joint> joints = new LongMap<Joint>(100);

	/** Contact filter **/
	protected ContactFilter contactFilter = null;

	/** Contact listener **/
	protected ContactListener contactListener = null;

	/** Ray-cast the world for all fixtures in the path of the ray. The ray-cast ignores shapes that contain the starting point.
	 * @param callback a user implemented callback class.
	 * @param point1 the ray starting point
	 * @param point2 the ray ending point */
	public void rayCast (RayCastCallback callback, Vector2 point1, Vector2 point2) {
		rayCastCallback = callback;
		jniRayCast(addr, point1.x, point1.y, point2.x, point2.y);
	}

	private RayCastCallback rayCastCallback = null;

	private native void jniRayCast (long addr, float aX, float aY, float bX, float bY);

	private Vector2 rayPoint = new Vector2();
	private Vector2 rayNormal = new Vector2();

	private float reportRayFixture (long addr, float pX, float pY, float nX, float nY, float fraction) {
		if (rayCastCallback != null)
			return rayCastCallback.reportRayFixture(fixtures.get(addr), rayPoint.set(pX, pY), rayNormal.set(nX, nY), fraction);
		else
			return 0.0f;
	}

	/** Construct a world object.
	 * @param gravity the world gravity vector.
	 * @param doSleep improve performance by not simulating inactive bodies. */
	public World (Vector2 gravity, boolean doSleep) {
		addr = newWorld(gravity.x, gravity.y, doSleep);

		contacts.ensureCapacity(contactAddrs.length);
		freeContacts.ensureCapacity(contactAddrs.length);

		for (int i = 0; i < contactAddrs.length; i++)
			freeContacts.add(new Contact(this, 0));
	}

	private native long newWorld (float gravityX, float gravityY, boolean doSleep);

	/** Register a destruction listener. The listener is owned by you and must remain in scope. */
	public void setDestructionListener (DestructionListener listener) {

	}

	/** Register a contact filter to provide specific control over collision. Otherwise the default filter is used
	 * (b2_defaultFilter). The listener is owned by you and must remain in scope. */
	public void setContactFilter (ContactFilter filter) {
		this.contactFilter = filter;
	}

	/** Register a contact event listener. The listener is owned by you and must remain in scope. */
	public void setContactListener (ContactListener listener) {
		this.contactListener = listener;
	}

	/** Create a rigid body given a definition. No reference to the definition is retained.
	 * @warning This function is locked during callbacks. */
	public Body createBody (BodyDef def) {
		long bodyAddr = jniCreateBody(addr, def.type.getValue(), def.position.x, def.position.y, def.angle, def.linearVelocity.x,
			def.linearVelocity.y, def.angularVelocity, def.linearDamping, def.angularDamping, def.allowSleep, def.awake,
			def.fixedRotation, def.bullet, def.active, def.gravityScale);
		Body body = freeBodies.obtain();
		body.reset(bodyAddr);
		this.bodies.put(body.addr, body);
		return body;
	}

	private native long jniCreateBody (long addr, int type, float positionX, float positionY, float angle, float linearVelocityX,
		float linearVelocityY, float angularVelocity, float linearDamping, float angularDamping, boolean allowSleep, boolean awake,
		boolean fixedRotation, boolean bullet, boolean active, float intertiaScale);

	/** Destroy a rigid body given a definition. No reference to the definition is retained. This function is locked during
	 * callbacks.
	 * @warning This automatically deletes all associated shapes and joints.
	 * @warning This function is locked during callbacks. */
	public void destroyBody (Body body) {
		body.setUserData(null);
		this.bodies.remove(body.addr);
		for (int i = 0; i < body.getFixtureList().size(); i++) {
			this.fixtures.remove(body.getFixtureList().get(i).addr).setUserData(null);
		}
		for (int i = 0; i < body.getJointList().size(); i++)
			destroyJoint(body.getJointList().get(i).joint);
		jniDestroyBody(addr, body.addr);
		freeBodies.free(body);
	}

	private native void jniDestroyBody (long addr, long bodyAddr);

	/** Create a joint to constrain bodies together. No reference to the definition is retained. This may cause the connected bodies
	 * to cease colliding.
	 * @warning This function is locked during callbacks. */
	public Joint createJoint (JointDef def) {
		long jointAddr = createProperJoint(def);
		Joint joint = null;
		if (def.type == JointType.DistanceJoint) joint = new DistanceJoint(this, jointAddr);
		if (def.type == JointType.FrictionJoint) joint = new FrictionJoint(this, jointAddr);
		if (def.type == JointType.GearJoint) joint = new GearJoint(this, jointAddr);
		if (def.type == JointType.MouseJoint) joint = new MouseJoint(this, jointAddr);
		if (def.type == JointType.PrismaticJoint) joint = new PrismaticJoint(this, jointAddr);
		if (def.type == JointType.PulleyJoint) joint = new PulleyJoint(this, jointAddr);
		if (def.type == JointType.RevoluteJoint) joint = new RevoluteJoint(this, jointAddr);
		if (def.type == JointType.WeldJoint) joint = new WeldJoint(this, jointAddr);
		if (def.type == JointType.RopeJoint) joint = new RopeJoint(this, jointAddr);
		if (def.type == JointType.WheelJoint) joint = new WheelJoint(this, jointAddr);
		if (joint != null) joints.put(joint.addr, joint);
		JointEdge jointEdgeA = new JointEdge(def.bodyB, joint);
		JointEdge jointEdgeB = new JointEdge(def.bodyA, joint);
		joint.jointEdgeA = jointEdgeA;
		joint.jointEdgeB = jointEdgeB;
		def.bodyA.joints.add(jointEdgeA);
		def.bodyB.joints.add(jointEdgeB);
		return joint;
	}

	private long createProperJoint (JointDef def) {
		if (def.type == JointType.DistanceJoint) {
			DistanceJointDef d = (DistanceJointDef)def;
			return jniCreateDistanceJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.length, d.frequencyHz, d.dampingRatio);
		}
		if (def.type == JointType.FrictionJoint) {
			FrictionJointDef d = (FrictionJointDef)def;
			return jniCreateFrictionJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.maxForce, d.maxTorque);
		}
		if (def.type == JointType.GearJoint) {
			GearJointDef d = (GearJointDef)def;
			return jniCreateGearJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.joint1.addr, d.joint2.addr, d.ratio);
		}
		if (def.type == JointType.MouseJoint) {
			MouseJointDef d = (MouseJointDef)def;
			return jniCreateMouseJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.target.x, d.target.y, d.maxForce,
				d.frequencyHz, d.dampingRatio);
		}
		if (def.type == JointType.PrismaticJoint) {
			PrismaticJointDef d = (PrismaticJointDef)def;
			return jniCreatePrismaticJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.localAxisA.x, d.localAxisA.y, d.referenceAngle, d.enableLimit,
				d.lowerTranslation, d.upperTranslation, d.enableMotor, d.maxMotorForce, d.motorSpeed);
		}
		if (def.type == JointType.PulleyJoint) {
			PulleyJointDef d = (PulleyJointDef)def;
			return jniCreatePulleyJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.groundAnchorA.x, d.groundAnchorA.y,
				d.groundAnchorB.x, d.groundAnchorB.y, d.localAnchorA.x, d.localAnchorA.y, d.localAnchorB.x, d.localAnchorB.y,
				d.lengthA, d.lengthB, d.ratio);

		}
		if (def.type == JointType.RevoluteJoint) {
			RevoluteJointDef d = (RevoluteJointDef)def;
			return jniCreateRevoluteJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.referenceAngle, d.enableLimit, d.lowerAngle, d.upperAngle, d.enableMotor,
				d.motorSpeed, d.maxMotorTorque);
		}
		if (def.type == JointType.WeldJoint) {
			WeldJointDef d = (WeldJointDef)def;
			return jniCreateWeldJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.referenceAngle);
		}
		if(def.type == JointType.RopeJoint) {
			RopeJointDef d = (RopeJointDef)def;
			return jniCreateRopeJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.maxLength);
		}
		if(def.type == JointType.WheelJoint) {
			WheelJointDef d = (WheelJointDef)def;
			return jniCreateWheelJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.localAxisA.x, d.localAxisA.y, d.enableMotor, d.maxMotorTorque, d.motorSpeed, 
				d.frequencyHz, d.dampingRatio);
		}

		return 0;
	}

	private native long jniCreateWheelJoint (long addr, long bodyA, long Bodyb, boolean collideConnected, float localAnchorAX, float localAnchorAY,
		float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, boolean enableMotor, float maxMotorTorque, float motorSpeed, float frequencyHz,
		float dampingRatio);

	private native long jniCreateRopeJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float maxLength);

	private native long jniCreateDistanceJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float length, float frequencyHz, float dampingRatio);

	private native long jniCreateFrictionJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float maxForce, float maxTorque);

	private native long jniCreateGearJoint (long addr, long bodyA, long bodyB, boolean collideConnected, long joint1, long joint2,
		float ratio);

	private native long jniCreateMouseJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float targetX,
		float targetY, float maxForce, float frequencyHz, float dampingRatio);

	private native long jniCreatePrismaticJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, float referenceAngle,
		boolean enableLimit, float lowerTranslation, float upperTranslation, boolean enableMotor, float maxMotorForce,
		float motorSpeed);

	private native long jniCreatePulleyJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float groundAnchorAX,
		float groundAnchorAY, float groundAnchorBX, float groundAnchorBY, float localAnchorAX, float localAnchorAY,
		float localAnchorBX, float localAnchorBY, float lengthA, float lengthB, float ratio);

	private native long jniCreateRevoluteJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle, boolean enableLimit, float lowerAngle,
		float upperAngle, boolean enableMotor, float motorSpeed, float maxMotorTorque);

	private native long jniCreateWeldJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle);

	/** Destroy a joint. This may cause the connected bodies to begin colliding.
	 * @warning This function is locked during callbacks. */
	public void destroyJoint (Joint joint) {
		joints.remove(joint.addr);
		joint.jointEdgeA.other.joints.remove(joint.jointEdgeB);
		joint.jointEdgeB.other.joints.remove(joint.jointEdgeA);
		jniDestroyJoint(addr, joint.addr);
	}

	private native void jniDestroyJoint (long addr, long jointAddr);

	/** Take a time step. This performs collision detection, integration, and constraint solution.
	 * @param timeStep the amount of time to simulate, this should not vary.
	 * @param velocityIterations for the velocity constraint solver.
	 * @param positionIterations for the position constraint solver. */
	public void step (float timeStep, int velocityIterations, int positionIterations) {
		jniStep(addr, timeStep, velocityIterations, positionIterations);
	}

	private native void jniStep (long addr, float timeStep, int velocityIterations, int positionIterations);

	/**
	 *  Manually clear the force buffer on all bodies. By default, forces are cleared automatically
	 * after each call to Step. The default behavior is modified by calling SetAutoClearForces.
	 * The purpose of this function is to support sub-stepping. Sub-stepping is often used to maintain
	 * a fixed sized time step under a variable frame-rate.
	 * When you perform sub-stepping you will disable auto clearing of forces and instead call
	 * ClearForces after all sub-steps are complete in one pass of your game loop.
	 * {@link #setAutoClearForces(boolean)} */
	public void clearForces () {
		jniClearForces(addr);
	}

	private native void jniClearForces (long addr);

	/** Enable/disable warm starting. For testing. */
	public void setWarmStarting (boolean flag) {
		jniSetWarmStarting(addr, flag);
	}

	private native void jniSetWarmStarting (long addr, boolean flag);

	/** Enable/disable continuous physics. For testing. */
	public void setContinuousPhysics (boolean flag) {
		jniSetContiousPhysics(addr, flag);
	}

	private native void jniSetContiousPhysics (long addr, boolean flag);

	/** Get the number of broad-phase proxies. */
	public int getProxyCount () {
		return jniGetProxyCount(addr);
	}

	private native int jniGetProxyCount (long addr);

	/** Get the number of bodies. */
	public int getBodyCount () {
		return jniGetBodyCount(addr);
	}

	private native int jniGetBodyCount (long addr);

	/** Get the number of joints. */
	public int getJointCount () {
		return jniGetJointcount(addr);
	}

	private native int jniGetJointcount (long addr);

	/** Get the number of contacts (each may have 0 or more contact points). */
	public int getContactCount () {
		return jniGetContactCount(addr);
	}

	private native int jniGetContactCount (long addr);

	/** Change the global gravity vector. */
	public void setGravity (Vector2 gravity) {
		jniSetGravity(addr, gravity.x, gravity.y);
	}

	private native void jniSetGravity (long addr, float gravityX, float gravityY);

	/** Get the global gravity vector. */
	final float[] tmpGravity = new float[2];
	final Vector2 gravity = new Vector2();

	public Vector2 getGravity () {
		jniGetGravity(addr, tmpGravity);
		gravity.x = tmpGravity[0];
		gravity.y = tmpGravity[1];
		return gravity;
	}

	private native void jniGetGravity (long addr, float[] gravity);

	/** Is the world locked (in the middle of a time step). */
	public boolean isLocked () {
		return jniIsLocked(addr);
	}

	private native boolean jniIsLocked (long addr);

	/** Set flag to control automatic clearing of forces after each time step. */
	public void setAutoClearForces (boolean flag) {
		jniSetAutoClearForces(addr, flag);
	}

	private native void jniSetAutoClearForces (long addr, boolean flag);

	/** Get the flag that controls automatic clearing of forces after each time step. */
	public boolean getAutoClearForces () {
		return jniGetAutoClearForces(addr);
	}

	private native boolean jniGetAutoClearForces (long addr);

	/** Query the world for all fixtures that potentially overlap the provided AABB.
	 * @param callback a user implemented callback class.
	 * @param lowerX the x coordinate of the lower left corner
	 * @param lowerY the y coordinate of the lower left corner
	 * @param upperX the x coordinate of the upper right corner
	 * @param upperY the y coordinate of the upper right corner */
	public void QueryAABB (QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		queryCallback = callback;
		jniQueryAABB(addr, lowerX, lowerY, upperX, upperY);
	}

	private QueryCallback queryCallback = null;;

	private native void jniQueryAABB (long addr, float lowX, float lowY, float upX, float upY);

//
// /// Ray-cast the world for all fixtures in the path of the ray. Your callback
// /// controls whether you get the closest point, any point, or n-points.
// /// The ray-cast ignores shapes that contain the starting point.
// /// @param callback a user implemented callback class.
// /// @param point1 the ray starting point
// /// @param point2 the ray ending point
// void RayCast(b2RayCastCallback* callback, const b2Vec2& point1, const b2Vec2& point2) const;
//
// /// Get the world contact list. With the returned contact, use b2Contact::GetNext to get
// /// the next contact in the world list. A NULL contact indicates the end of the list.
// /// @return the head of the world contact list.
// /// @warning contacts are
// b2Contact* GetContactList();

	private long[] contactAddrs = new long[200];
	private final ArrayList<Contact> contacts = new ArrayList<Contact>();
	private final ArrayList<Contact> freeContacts = new ArrayList<Contact>();

	/** Returns the list of {@link Contact} instances produced by the last call to {@link #step(float, int, int)}. Note that the
	 * returned list will have O(1) access times when using indexing.
	 * contacts are created and destroyed in the middle of a time step.
	 * Use {@link ContactListener} to avoid missing contacts
	 * @return the contact list */
	public List<Contact> getContactList () {
		int numContacts = getContactCount();
		if (numContacts > contactAddrs.length) {
			int newSize = 2 * numContacts;
			contactAddrs = new long[newSize];
			contacts.ensureCapacity(newSize);
			freeContacts.ensureCapacity(newSize);
		}
		if (numContacts > freeContacts.size()) {
			int freeConts = freeContacts.size();
			for (int i = 0; i < numContacts - freeConts; i++)
				freeContacts.add(new Contact(this, 0));
		}
		jniGetContactList(addr, contactAddrs);

		contacts.clear();
		for (int i = 0; i < numContacts; i++) {
			Contact contact = freeContacts.get(i);
			contact.addr = contactAddrs[i];
			contacts.add(contact);
		}

		return contacts;
	}

	/** @return all bodies currently in the simulation */
	public Iterator<Body> getBodies () {
		return bodies.values();
	}

	/** @return all joints currently in the simulation */
	public Iterator<Joint> getJoints () {
		return joints.values();
	}

	private native void jniGetContactList (long addr, long[] contacts);

	public void dispose () {
		jniDispose(addr);
	}

	private native void jniDispose (long addr);

	/** Internal method called from JNI in case a contact happens
	 * @param fixtureA
	 * @param fixtureB
	 * @return whether the things collided */
	private boolean contactFilter (long fixtureA, long fixtureB) {
		if (contactFilter != null)
			return contactFilter.shouldCollide(fixtures.get(fixtureA), fixtures.get(fixtureB));
		else {
			Filter filterA = fixtures.get(fixtureA).getFilterData();
			Filter filterB = fixtures.get(fixtureB).getFilterData();

			if (filterA.groupIndex == filterB.groupIndex && filterA.groupIndex != 0) {
				return filterA.groupIndex > 0;
			}

			boolean collide = (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0;
			return collide;
		}
	}

	private final Contact contact = new Contact(this, 0);
	private final Manifold manifold = new Manifold(this, 0);
	private final ContactImpulse impulse = new ContactImpulse(this, 0);

	private void beginContact (long contactAddr) {
		contact.addr = contactAddr;
		if (contactListener != null) contactListener.beginContact(contact);
	}

	private void endContact (long contactAddr) {
		contact.addr = contactAddr;
		if (contactListener != null) contactListener.endContact(contact);
	}

	private void preSolve (long contactAddr, long manifoldAddr) {
		contact.addr = contactAddr;
		manifold.addr = manifoldAddr;
		if (contactListener != null) contactListener.preSolve(contact, manifold);
	}

	private void postSolve (long contactAddr, long impulseAddr) {
		contact.addr = contactAddr;
		impulse.addr = impulseAddr;
		if (contactListener != null) contactListener.postSolve(contact, impulse);
	}

	private boolean reportFixture (long addr) {
		if (queryCallback != null)
			return queryCallback.reportFixture(fixtures.get(addr));
		else
			return false;
	}

	/** Sets the box2d velocity threshold globally, for all World instances.
	 * @param threshold the threshold, default 1.0f */
	public static native void setVelocityThreshold (float threshold);

	/** @return the global box2d velocity threshold. */
	public static native float getVelocityThreshold ();
}

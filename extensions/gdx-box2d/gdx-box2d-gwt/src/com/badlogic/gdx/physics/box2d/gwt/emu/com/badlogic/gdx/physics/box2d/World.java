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

import java.util.Iterator;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.JointEdge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/** The world class manages all physics entities, dynamic simulation, and asynchronous queries. The world also contains efficient
 * memory management facilities.
 * @author mzechner */
public final class World implements Disposable {
	org.jbox2d.dynamics.World world;
	Vec2 tmp = new Vec2();
	Vector2 tmp2 = new Vector2();
	ObjectMap<org.jbox2d.dynamics.Body, Body> bodies = new ObjectMap<org.jbox2d.dynamics.Body, Body>();
	ObjectMap<org.jbox2d.dynamics.Fixture, Fixture> fixtures = new ObjectMap<org.jbox2d.dynamics.Fixture, Fixture>();
	ObjectMap<org.jbox2d.dynamics.joints.Joint, Joint> joints = new ObjectMap<org.jbox2d.dynamics.joints.Joint, Joint>();

	/** Construct a world object.
	 * @param gravity the world gravity vector.
	 * @param doSleep improve performance by not simulating inactive bodies. */
	public World (Vector2 gravity, boolean doSleep) {
		world = new org.jbox2d.dynamics.World(tmp.set(gravity.x, gravity.y));
		world.setAllowSleep(doSleep);
	}

	/** Register a destruction listener. The listener is owned by you and must remain in scope. */
	public void setDestructionListener (DestructionListener listener) {
	}

	/** Register a contact filter to provide specific control over collision. Otherwise the default filter is used
	 * (b2_defaultFilter). The listener is owned by you and must remain in scope. */
	public void setContactFilter (final ContactFilter filter) {
		if (filter != null) {
			world.setContactFilter(new org.jbox2d.callbacks.ContactFilter() {
				@Override
				public boolean shouldCollide (org.jbox2d.dynamics.Fixture fixtureA, org.jbox2d.dynamics.Fixture fixtureB) {
					return filter.shouldCollide(fixtures.get(fixtureA), fixtures.get(fixtureB));
				}
			});
		} else {
			world.setContactFilter(new org.jbox2d.callbacks.ContactFilter());
		}
	}

	/** Register a contact event listener. The listener is owned by you and must remain in scope. */
	Contact tmpContact = new Contact(this);
	Manifold tmpManifold = new Manifold();
	ContactImpulse tmpImpulse = new ContactImpulse();

	public void setContactListener (final ContactListener listener) {
		if (listener != null) {
			world.setContactListener(new org.jbox2d.callbacks.ContactListener() {
				@Override
				public void beginContact (org.jbox2d.dynamics.contacts.Contact contact) {
					tmpContact.contact = contact;
					listener.beginContact(tmpContact);
				}

				@Override
				public void endContact (org.jbox2d.dynamics.contacts.Contact contact) {
					tmpContact.contact = contact;
					listener.endContact(tmpContact);
				}

				@Override
				public void preSolve (org.jbox2d.dynamics.contacts.Contact contact, org.jbox2d.collision.Manifold oldManifold) {
					tmpContact.contact = contact;
					tmpManifold.manifold = oldManifold;
					listener.preSolve(tmpContact, tmpManifold);
				}

				@Override
				public void postSolve (org.jbox2d.dynamics.contacts.Contact contact, org.jbox2d.callbacks.ContactImpulse impulse) {
					tmpContact.contact = contact;
					tmpImpulse.impulse = impulse;
					listener.postSolve(tmpContact, tmpImpulse);
				}
			});
		} else {
			world.setContactListener(null);
		}
	}

	/** Create a rigid body given a definition. No reference to the definition is retained.
	 * @warning This function is locked during callbacks. */
	public Body createBody (BodyDef def) {
		org.jbox2d.dynamics.BodyDef bd = new org.jbox2d.dynamics.BodyDef();
		bd.active = def.active;
		bd.allowSleep = def.allowSleep;
		bd.angle = def.angle;
		bd.angularDamping = def.angularDamping;
		bd.angularVelocity = def.angularVelocity;
		bd.awake = def.awake;
		bd.bullet = def.bullet;
		bd.fixedRotation = def.fixedRotation;
		bd.gravityScale = def.gravityScale;
		bd.linearDamping = def.linearDamping;
		bd.linearVelocity.set(def.linearVelocity.x, def.linearVelocity.y);
		bd.position.set(def.position.x, def.position.y);
		if (def.type == BodyType.DynamicBody) bd.type = org.jbox2d.dynamics.BodyType.DYNAMIC;
		if (def.type == BodyType.StaticBody) bd.type = org.jbox2d.dynamics.BodyType.STATIC;
		if (def.type == BodyType.KinematicBody) bd.type = org.jbox2d.dynamics.BodyType.KINEMATIC;

		org.jbox2d.dynamics.Body b = world.createBody(bd);
		Body body = new Body(this, b);
		bodies.put(b, body);
		return body;
	}

	/** Destroy a rigid body given a definition. No reference to the definition is retained. This function is locked during
	 * callbacks.
	 * @warning This automatically deletes all associated shapes and joints.
	 * @warning This function is locked during callbacks. */
	public void destroyBody (Body body) {
		JointEdge jointEdge = body.body.getJointList();
		while (jointEdge != null) {
			JointEdge next = jointEdge.next;			
			world.destroyJoint(jointEdge.joint);
			joints.remove(jointEdge.joint);
			jointEdge = next;
		}
		world.destroyBody(body.body);
		bodies.remove(body.body);
		for (Fixture fixture : body.fixtures) {
			fixtures.remove(fixture.fixture);
		}
	}

	/** Create a joint to constrain bodies together. No reference to the definition is retained. This may cause the connected bodies
	 * to cease colliding.
	 * @warning This function is locked during callbacks. */
	public Joint createJoint (JointDef def) {
		org.jbox2d.dynamics.joints.JointDef jd = def.toJBox2d();
		org.jbox2d.dynamics.joints.Joint j = world.createJoint(jd);
		Joint joint = null;
		if (def.type == JointType.DistanceJoint) joint = new DistanceJoint(this, (org.jbox2d.dynamics.joints.DistanceJoint)j);
		if (def.type == JointType.FrictionJoint) joint = new FrictionJoint(this, (org.jbox2d.dynamics.joints.FrictionJoint)j);
		if (def.type == JointType.GearJoint) joint = new GearJoint(this, (org.jbox2d.dynamics.joints.GearJoint)j, ((GearJointDef) def).joint1, ((GearJointDef) def).joint2);
		if (def.type == JointType.MotorJoint) joint = new MotorJoint(this, (org.jbox2d.dynamics.joints.MotorJoint)j);
		if (def.type == JointType.MouseJoint) joint = new MouseJoint(this, (org.jbox2d.dynamics.joints.MouseJoint)j);
		if (def.type == JointType.PrismaticJoint) joint = new PrismaticJoint(this, (org.jbox2d.dynamics.joints.PrismaticJoint)j);
		if (def.type == JointType.PulleyJoint) joint = new PulleyJoint(this, (org.jbox2d.dynamics.joints.PulleyJoint)j);
		if (def.type == JointType.RevoluteJoint) joint = new RevoluteJoint(this, (org.jbox2d.dynamics.joints.RevoluteJoint)j);
		if (def.type == JointType.RopeJoint) joint = new RopeJoint(this, (org.jbox2d.dynamics.joints.RopeJoint)j);
		if (def.type == JointType.WeldJoint) joint = new WeldJoint(this, (org.jbox2d.dynamics.joints.WeldJoint)j);
		if (def.type == JointType.WheelJoint) joint = new WheelJoint(this, (org.jbox2d.dynamics.joints.WheelJoint)j);
		if (joint == null) throw new GdxRuntimeException("Joint type '" + def.type + "' not yet supported by GWT backend");
		joints.put(j, joint);
		return joint;
	}

	/** Destroy a joint. This may cause the connected bodies to begin colliding.
	 * @warning This function is locked during callbacks. */
	public void destroyJoint (Joint joint) {
		joint.setUserData(null);
		world.destroyJoint(joint.joint);
		joints.remove(joint.joint);
	}

	/** Take a time step. This performs collision detection, integration, and constraint solution.
	 * @param timeStep the amount of time to simulate, this should not vary.
	 * @param velocityIterations for the velocity constraint solver.
	 * @param positionIterations for the position constraint solver. */
	public void step (float timeStep, int velocityIterations, int positionIterations) {
		world.step(timeStep, velocityIterations, positionIterations);
	}

	/** Manually clear the force buffer on all bodies. By default, forces are cleared automatically after each call to Step. The
	 * default behavior is modified by calling SetAutoClearForces. The purpose of this function is to support sub-stepping.
	 * Sub-stepping is often used to maintain a fixed sized time step under a variable frame-rate. When you perform sub-stepping
	 * you will disable auto clearing of forces and instead call ClearForces after all sub-steps are complete in one pass of your
	 * game loop. {@link #setAutoClearForces(boolean)} */
	public void clearForces () {
		world.clearForces();
	}

	/** Enable/disable warm starting. For testing. */
	public void setWarmStarting (boolean flag) {
		world.setWarmStarting(flag);
	}

	/** Enable/disable continuous physics. For testing. */
	public void setContinuousPhysics (boolean flag) {
		world.setContinuousPhysics(flag);
	}

	/** Get the number of broad-phase proxies. */
	public int getProxyCount () {
		return world.getProxyCount();
	}

	/** Get the number of bodies. */
	public int getBodyCount () {
		return world.getBodyCount();
	}

	/** Get the number of joints. */
	public int getJointCount () {
		return world.getJointCount();
	}

	/** Get the number of contacts (each may have 0 or more contact points). */
	public int getContactCount () {
		return world.getContactCount();
	}

	/** Change the global gravity vector. */
	public void setGravity (Vector2 gravity) {
		world.setGravity(tmp.set(gravity.x, gravity.y));
	}

	public Vector2 getGravity () {
		Vec2 gravity = world.getGravity();
		return tmp2.set(gravity.x, gravity.y);
	}

	/** Is the world locked (in the middle of a time step). */
	public boolean isLocked () {
		return world.isLocked();
	}

	/** Set flag to control automatic clearing of forces after each time step. */
	public void setAutoClearForces (boolean flag) {
		world.setAutoClearForces(flag);
	}

	/** Get the flag that controls automatic clearing of forces after each time step. */
	public boolean getAutoClearForces () {
		return world.getAutoClearForces();
	}

	/** Query the world for all fixtures that potentially overlap the provided AABB.
	 * @param callback a user implemented callback class.
	 * @param lowerX the x coordinate of the lower left corner
	 * @param lowerY the y coordinate of the lower left corner
	 * @param upperX the x coordinate of the upper right corner
	 * @param upperY the y coordinate of the upper right corner */
	AABB aabb = new AABB();

	public void QueryAABB (final QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		// FIXME pool QueryCallback?
		aabb.lowerBound.set(lowerX, lowerY);
		aabb.upperBound.set(upperX, upperY);
		world.queryAABB(new org.jbox2d.callbacks.QueryCallback() {
			@Override
			public boolean reportFixture (org.jbox2d.dynamics.Fixture f) {
				Fixture fixture = fixtures.get(f);
				return callback.reportFixture(fixture);
			}
		}, aabb);
	}

	/** Returns the list of {@link Contact} instances produced by the last call to {@link #step(float, int, int)}. Note that the
	 * returned list will have O(1) access times when using indexing. contacts are created and destroyed in the middle of a time
	 * step. Use {@link ContactListener} to avoid missing contacts
	 * @return the contact list */
	Array<Contact> contacts = new Array<Contact>();

	public Array<Contact> getContactList () {
		// FIXME pool contacts
		org.jbox2d.dynamics.contacts.Contact contactList = world.getContactList();
		contacts.clear();
		while (contactList != null) {
			Contact contact = new Contact(this, contactList);
			contacts.add(contact);
			contactList = contactList.m_next;
		}
		return contacts;
	}

	/** @return all bodies currently in the simulation */
	public void getBodies (Array<Body> bodies) {
		bodies.clear();
		bodies.ensureCapacity(this.bodies.size);
		for (Iterator<Body> iter = this.bodies.values(); iter.hasNext();) {
			bodies.add(iter.next());
		}
	}

	/** @param fixtures an Array in which to place all fixtures currently in the simulation */
	public void getFixtures (Array<com.badlogic.gdx.physics.box2d.Fixture> fixtures) {
		fixtures.clear();
		fixtures.ensureCapacity(this.fixtures.size);
		for (Iterator<Fixture> iter = this.fixtures.values(); iter.hasNext();) {
			fixtures.add(iter.next());
		}
	}

	/** @return all joints currently in the simulation */
	public void getJoints (Array<Joint> joints) {
		joints.clear();
		joints.ensureCapacity(this.joints.size);
		for (Iterator<Joint> iter = this.joints.values(); iter.hasNext();) {
			joints.add(iter.next());
		}
	}

	public void dispose () {
	}

	/** Sets the box2d velocity threshold globally, for all World instances.
	 * @param threshold the threshold, default 1.0f */
	public static void setVelocityThreshold (float threshold) {
		Settings.velocityThreshold = threshold;
	}

	/** @return the global box2d velocity threshold. */
	public static float getVelocityThreshold () {
		return Settings.velocityThreshold;
	}

	/** Ray-cast the world for all fixtures in the path of the ray. The ray-cast ignores shapes that contain the starting point.
	 * @param callback a user implemented callback class.
	 * @param point1 the ray starting point
	 * @param point2 the ray ending point */
	Vec2 point1 = new Vec2();
	Vec2 point2 = new Vec2();
	Vector2 point = new Vector2();
	Vector2 normal = new Vector2();

	public void rayCast (final RayCastCallback callback, Vector2 point1, Vector2 point2) {
		rayCast(callback, point1.x, point1.y, point2.x, point2.y);
	}

	public void rayCast (final RayCastCallback callback, float point1X, float point1Y, float point2X, float point2Y) {
		// FIXME pool RayCastCallback?
		world.raycast(new org.jbox2d.callbacks.RayCastCallback() {
			@Override
			public float reportFixture (org.jbox2d.dynamics.Fixture f, Vec2 p, Vec2 n, float fraction) {
				return callback.reportRayFixture(fixtures.get(f), point.set(p.x, p.y), normal.set(n.x, n.y), fraction);
			}
		}, this.point1.set(point1X, point1Y), this.point2.set(point2X, point2Y));
	}
}

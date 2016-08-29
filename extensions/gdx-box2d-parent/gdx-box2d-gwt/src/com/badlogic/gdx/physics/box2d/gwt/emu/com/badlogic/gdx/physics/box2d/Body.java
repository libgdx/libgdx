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

import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

/** A rigid body. These are created via World.CreateBody.
 * @author mzechner */
public class Body {
	final World world;
	public final org.jbox2d.dynamics.Body body;
	final Vec2 tmp = new Vec2();
	final Vec2 tmp2 = new Vec2();
	final Array<Fixture> fixtures = new Array<Fixture>();
	final Array<JointEdge> joints = new Array<JointEdge>();

	/** Constructs a new body with the given address
	 * @param world the world
	 * @param addr the address */
	protected Body (World world, org.jbox2d.dynamics.Body body) {
		this.world = world;
		this.body = body;
	}

	/** Set the position of the body's origin and rotation. This breaks any contacts and wakes the other bodies. Manipulating a
	 * body's transform may cause non-physical behavior.
	 * @param position the world position of the body's local origin.
	 * @param angle the world rotation in radians. */
	public void setTransform (Vector2 position, float angle) {
		tmp.set(position.x, position.y);
		body.setTransform(tmp, angle);
	}

	/** Set the position of the body's origin and rotation. This breaks any contacts and wakes the other bodies. Manipulating a
	 * body's transform may cause non-physical behavior.
	 * @param x the world position on the x-axis
	 * @param y the world position on the y-axis
	 * @param angle the world rotation in radians. */
	public void setTransform (float x, float y, float angle) {
		tmp.set(x, y);
		body.setTransform(tmp, angle);
	}

	Transform transform = new Transform();

	/** Get the body transform for the body's origin. */
	public Transform getTransform () {
		org.jbox2d.common.Transform trans = body.getTransform();
		transform.vals[Transform.POS_X] = trans.p.x;
		transform.vals[Transform.POS_Y] = trans.p.y;
		transform.vals[Transform.COS] = trans.q.c;
		transform.vals[Transform.SIN] = trans.q.s;
		return transform;
	}

	final Vector2 position = new Vector2();

	/** Get the world body origin position.
	 * @return the world position of the body's origin. */
	public Vector2 getPosition () {
		Vec2 pos = body.getPosition();
		position.set(pos.x, pos.y);
		return position;
	}

	/** Get the angle in radians.
	 * @return the current world rotation angle in radians. */
	public float getAngle () {
		return body.getAngle();
	}

	/** Get the world position of the center of mass. */
	final Vector2 worldCenter = new Vector2();

	public Vector2 getWorldCenter () {
		Vec2 wc = body.getWorldCenter();
		return worldCenter.set(wc.x, wc.y);
	}

	/** Get the local position of the center of mass. */
	private final Vector2 localCenter = new Vector2();

	public Vector2 getLocalCenter () {
		Vec2 lc = body.getLocalCenter();
		localCenter.set(lc.x, lc.y);
		return localCenter;
	}

	/** Set the linear velocity of the center of mass. */
	public void setLinearVelocity (Vector2 v) {
		tmp.set(v.x, v.y);
		body.setLinearVelocity(tmp);
	}

	/** Set the linear velocity of the center of mass. */
	public void setLinearVelocity (float vX, float vY) {
		tmp.set(vX, vY);
		body.setLinearVelocity(tmp);
	}

	/** Get the linear velocity of the center of mass. */
	private final Vector2 linearVelocity = new Vector2();

	public Vector2 getLinearVelocity () {
		Vec2 lv = body.getLinearVelocity();
		linearVelocity.set(lv.x, lv.y);
		return linearVelocity;
	}

	/** Set the angular velocity. */
	public void setAngularVelocity (float omega) {
		body.setAngularVelocity(omega);
	}

	/** Get the angular velocity. */
	public float getAngularVelocity () {
		return body.getAngularVelocity();
	}

	/** Apply a force at a world point. If the force is not applied at the center of mass, it will generate a torque and affect the
	 * angular velocity. This wakes up the body.
	 * @param force the world force vector, usually in Newtons (N).
	 * @param point the world position of the point of application. */
	public void applyForce (Vector2 force, Vector2 point, boolean wrap) {
		tmp.set(force.x, force.y);
		tmp2.set(point.x, point.y);
		body.applyForce(tmp, tmp2);
	}

	/** Apply a force at a world point. If the force is not applied at the center of mass, it will generate a torque and affect the
	 * angular velocity. This wakes up the body.
	 * @param forceX the world force vector on x, usually in Newtons (N).
	 * @param forceY the world force vector on y, usually in Newtons (N).
	 * @param pointX the world position of the point of application on x.
	 * @param pointY the world position of the point of application on y. */
	public void applyForce (float forceX, float forceY, float pointX, float pointY, boolean wake) {
		tmp.set(forceX, forceY);
		tmp2.set(pointX, pointY);
		body.applyForce(tmp, tmp2);
	}

	/** Apply a force to the center of mass. This wakes up the body.
	 * @param force the world force vector, usually in Newtons (N). */
	public void applyForceToCenter (Vector2 force, boolean wake) {
		tmp.set(force.x, force.y);
		body.applyForceToCenter(tmp);
	}

	/** Apply a force to the center of mass. This wakes up the body.
	 * @param forceX the world force vector, usually in Newtons (N).
	 * @param forceY the world force vector, usually in Newtons (N). */
	public void applyForceToCenter (float forceX, float forceY, boolean wake) {
		tmp.set(forceX, forceY);
		body.applyForceToCenter(tmp);
	}

	/** Apply a torque. This affects the angular velocity without affecting the linear velocity of the center of mass. This wakes up
	 * the body.
	 * @param torque about the z-axis (out of the screen), usually in N-m. */
	public void applyTorque (float torque, boolean wake) {
		body.applyTorque(torque);
	}

	/** Apply an impulse at a point. This immediately modifies the velocity. It also modifies the angular velocity if the point of
	 * application is not at the center of mass. This wakes up the body.
	 * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
	 * @param point the world position of the point of application. */
	public void applyLinearImpulse (Vector2 impulse, Vector2 point, boolean wake) {
		tmp.set(impulse.x, impulse.y);
		tmp2.set(point.x, point.y);
		body.applyLinearImpulse(tmp, tmp2, wake);
	}

	/** Apply an impulse at a point. This immediately modifies the velocity. It also modifies the angular velocity if the point of
	 * application is not at the center of mass. This wakes up the body.
	 * @param impulseX the world impulse vector on the x-axis, usually in N-seconds or kg-m/s.
	 * @param impulseY the world impulse vector on the y-axis, usually in N-seconds or kg-m/s.
	 * @param pointX the world position of the point of application on the x-axis.
	 * @param pointY the world position of the point of application on the y-axis. */
	public void applyLinearImpulse (float impulseX, float impulseY, float pointX, float pointY, boolean wake) {
		tmp.set(impulseX, impulseY);
		tmp2.set(pointX, pointY);
		body.applyLinearImpulse(tmp, tmp2, wake);
	}

	/** Apply an angular impulse.
	 * @param impulse the angular impulse in units of kg*m*m/s */
	public void applyAngularImpulse (float impulse, boolean wake) {
		body.applyAngularImpulse(impulse);
	}

	/** Get the total mass of the body.
	 * @return the mass, usually in kilograms (kg). */
	public float getMass () {
		return body.getMass();
	}

	/** Get the rotational inertia of the body about the local origin.
	 * @return the rotational inertia, usually in kg-m^2. */
	public float getInertia () {
		return body.getInertia();
	}

	private final MassData massData = new MassData();
	private final org.jbox2d.collision.shapes.MassData massData2 = new org.jbox2d.collision.shapes.MassData();

	/** Get the mass data of the body.
	 * @return a struct containing the mass, inertia and center of the body. */
	public MassData getMassData () {
		body.getMassData(massData2);
		massData.center.set(massData2.center.x, massData2.center.y);
		massData.I = massData2.I;
		massData.mass = massData2.mass;
		return massData;
	}

	/** Set the mass properties to override the mass properties of the fixtures. Note that this changes the center of mass position.
	 * Note that creating or destroying fixtures can also alter the mass. This function has no effect if the body isn't dynamic.
	 * @param data the mass properties. */
	public void setMassData (MassData data) {
		massData2.center.set(data.center.x, data.center.y);
		massData2.I = data.I;
		massData2.mass = data.mass;
		body.setMassData(massData2);
	}

	/** This resets the mass properties to the sum of the mass properties of the fixtures. This normally does not need to be called
	 * unless you called SetMassData to override the mass and you later want to reset the mass. */
	public void resetMassData () {
		body.resetMassData();
	}

	private final Vector2 worldPoint = new Vector2();

	/** Get the world coordinates of a point given the local coordinates.
	 * @param localPoint a point on the body measured relative the the body's origin.
	 * @return the same point expressed in world coordinates. */
	public Vector2 getWorldPoint (Vector2 localPoint) {
		tmp.set(localPoint.x, localPoint.y);
		Vec2 wp = body.getWorldPoint(tmp);
		return worldPoint.set(wp.x, wp.y);
	}

	private final Vector2 worldVector = new Vector2();

	/** Get the world coordinates of a vector given the local coordinates.
	 * @param localVector a vector fixed in the body.
	 * @return the same vector expressed in world coordinates. */
	public Vector2 getWorldVector (Vector2 localVector) {
		tmp.set(localVector.x, localVector.y);
		Vec2 wv = body.getWorldVector(tmp);
		return worldVector.set(wv.x, wv.y);
	}

	public final Vector2 localPoint2 = new Vector2();

	/** Gets a local point relative to the body's origin given a world point.
	 * @param worldPoint a point in world coordinates.
	 * @return the corresponding local point relative to the body's origin. */
	public Vector2 getLocalPoint (Vector2 worldPoint) {
		tmp.set(worldPoint.x, worldPoint.y);
		Vec2 lp = body.getLocalPoint(tmp);
		return localPoint2.set(lp.x, lp.y);
	}

	public final Vector2 localVector = new Vector2();

	/** Gets a local vector given a world vector.
	 * @param worldVector a vector in world coordinates.
	 * @return the corresponding local vector. */
	public Vector2 getLocalVector (Vector2 worldVector) {
		tmp.set(worldVector.x, worldVector.y);
		Vec2 lv = body.getLocalVector(tmp);
		return localVector.set(lv.x, lv.y);
	}

	public final Vector2 linVelWorld = new Vector2();

	/** Get the world linear velocity of a world point attached to this body.
	 * @param worldPoint a point in world coordinates.
	 * @return the world velocity of a point. */
	public Vector2 getLinearVelocityFromWorldPoint (Vector2 worldPoint) {
		tmp.set(worldPoint.x, worldPoint.y);
		Vec2 lv = body.getLinearVelocityFromWorldPoint(tmp);
		return linVelWorld.set(lv.x, lv.y);
	}

	public final Vector2 linVelLoc = new Vector2();

	/** Get the world velocity of a local point.
	 * @param localPoint a point in local coordinates.
	 * @return the world velocity of a point. */
	public Vector2 getLinearVelocityFromLocalPoint (Vector2 localPoint) {
		tmp.set(localPoint.x, localPoint.y);
		Vec2 lv = body.getLinearVelocityFromLocalPoint(tmp);
		return linVelLoc.set(lv.x, lv.y);
	}

	/** Get the linear damping of the body. */
	public float getLinearDamping () {
		return body.getLinearDamping();
	}

	/** Set the linear damping of the body. */
	public void setLinearDamping (float linearDamping) {
		body.setLinearDamping(linearDamping);
	}

	/** Get the angular damping of the body. */
	public float getAngularDamping () {
		return body.getAngularDamping();
	}

	/** Set the angular damping of the body. */
	public void setAngularDamping (float angularDamping) {
		body.setAngularDamping(angularDamping);
	}

	/** Set the type of this body. This may alter the mass and velocity. */
	public void setType (BodyType type) {
		org.jbox2d.dynamics.BodyType t = org.jbox2d.dynamics.BodyType.DYNAMIC;
		if (type == BodyType.DynamicBody) t = org.jbox2d.dynamics.BodyType.DYNAMIC;
		if (type == BodyType.KinematicBody) t = org.jbox2d.dynamics.BodyType.KINEMATIC;
		if (type == BodyType.StaticBody) t = org.jbox2d.dynamics.BodyType.STATIC;
		body.setType(t);
	}

	/** Get the type of this body. */
	public BodyType getType () {
		org.jbox2d.dynamics.BodyType type = body.getType();
		if (type == org.jbox2d.dynamics.BodyType.DYNAMIC) return BodyType.DynamicBody;
		if (type == org.jbox2d.dynamics.BodyType.KINEMATIC) return BodyType.KinematicBody;
		if (type == org.jbox2d.dynamics.BodyType.STATIC) return BodyType.StaticBody;
		return BodyType.DynamicBody;
	}

	/** Should this body be treated like a bullet for continuous collision detection? */
	public void setBullet (boolean flag) {
		body.setBullet(flag);
	}

	/** Is this body treated like a bullet for continuous collision detection? */
	public boolean isBullet () {
		return body.isBullet();
	}

	/** You can disable sleeping on this body. If you disable sleeping, the */
	public void setSleepingAllowed (boolean flag) {
		body.setSleepingAllowed(flag);
	}

	/** Is this body allowed to sleep */
	public boolean isSleepingAllowed () {
		return body.isSleepingAllowed();
	}

	/** Set the sleep state of the body. A sleeping body has very low CPU cost.
	 * @param flag set to true to put body to sleep, false to wake it. */
	public void setAwake (boolean flag) {
		body.setAwake(flag);
	}

	/** Get the sleeping state of this body.
	 * @return true if the body is sleeping. */
	public boolean isAwake () {
		return body.isAwake();
	}

	/** Set the active state of the body. An inactive body is not simulated and cannot be collided with or woken up. If you pass a
	 * flag of true, all fixtures will be added to the broad-phase. If you pass a flag of false, all fixtures will be removed from
	 * the broad-phase and all contacts will be destroyed. Fixtures and joints are otherwise unaffected. You may continue to
	 * create/destroy fixtures and joints on inactive bodies. Fixtures on an inactive body are implicitly inactive and will not
	 * participate in collisions, ray-casts, or queries. Joints connected to an inactive body are implicitly inactive. An inactive
	 * body is still owned by a b2World object and remains in the body list. */
	public void setActive (boolean flag) {
		body.setActive(flag);
	}

	/** Get the active state of the body. */
	public boolean isActive () {
		return body.isActive();
	}

	/** Set this body to have fixed rotation. This causes the mass to be reset. */
	public void setFixedRotation (boolean flag) {
		body.setFixedRotation(flag);
	}

	/** Does this body have fixed rotation? */
	public boolean isFixedRotation () {
		return body.isFixedRotation();
	}

	/** Creates a fixture and attach it to this body. Use this function if you need to set some fixture parameters, like friction.
	 * Otherwise you can create the fixture directly from a shape. If the density is non-zero, this function automatically updates
	 * the mass of the body. Contacts are not created until the next time step.
	 * @param def the fixture definition.
	 * @warning This function is locked during callbacks. */
	public Fixture createFixture (FixtureDef def) {
		org.jbox2d.dynamics.FixtureDef fd = def.toJBox2d();
		org.jbox2d.dynamics.Fixture f = body.createFixture(fd);
		Fixture fixture = new Fixture(this, f);
		fixtures.add(fixture);
		world.fixtures.put(f, fixture);
		return fixture;
	}

	/** Creates a fixture from a shape and attach it to this body. This is a convenience function. Use b2FixtureDef if you need to
	 * set parameters like friction, restitution, user data, or filtering. If the density is non-zero, this function automatically
	 * updates the mass of the body.
	 * @param shape the shape to be cloned.
	 * @param density the shape density (set to zero for static bodies).
	 * @warning This function is locked during callbacks. */
	public Fixture createFixture (Shape shape, float density) {
		org.jbox2d.dynamics.Fixture f = body.createFixture(shape.shape, density);
		Fixture fixture = new Fixture(this, f);
		fixtures.add(fixture);
		world.fixtures.put(f, fixture);
		return fixture;
	}

	/** Destroy a fixture. This removes the fixture from the broad-phase and destroys all contacts associated with this fixture.
	 * This will automatically adjust the mass of the body if the body is dynamic and the fixture has positive density. All
	 * fixtures attached to a body are implicitly destroyed when the body is destroyed.
	 * @param fixture the fixture to be removed.
	 * @warning This function is locked during callbacks. */
	public void destroyFixture (Fixture fixture) {
		body.destroyFixture(fixture.fixture);
		fixtures.removeValue(fixture, true);
		world.fixtures.remove(fixture.fixture);
	}

	/** Get the list of all fixtures attached to this body. Do not modify the list! */
	public Array<Fixture> getFixtureList () {
		return fixtures;
	}

	/** Get the list of all joints attached to this body. Do not modify the list! */
	public Array<JointEdge> getJointList () {
		// FIXME wow this is bad...
		org.jbox2d.dynamics.joints.JointEdge jointEdge = body.getJointList();
		joints.clear();
		while (jointEdge != null) {
			JointEdge edge = new JointEdge(world.bodies.get(jointEdge.other), world.joints.get(jointEdge.joint));
			joints.add(edge);
			jointEdge = jointEdge.next;
		}
		return joints;
	}

	/** @return Get the gravity scale of the body. */
	public float getGravityScale () {
		return body.getGravityScale();
	}

	/** Sets the gravity scale of the body */
	public void setGravityScale (float scale) {
		body.setGravityScale(scale);
	}

	/** Get the parent world of this body. */
	public World getWorld () {
		return world;
	}

	private Object userData;

	/** Get the user data */
	public Object getUserData () {
		return userData;
	}

	/** Set the user data */
	public void setUserData (Object userData) {
		this.userData = userData;
	}
}

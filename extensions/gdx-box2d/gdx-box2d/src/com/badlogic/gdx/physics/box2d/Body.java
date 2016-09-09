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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

/** A rigid body. These are created via World.CreateBody.
 * @author mzechner */
public class Body {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	/** the address of the body **/
	protected long addr;

	/** temporary float array **/
	private final float[] tmp = new float[4];

	/** World **/
	private final World world;

	/** Fixtures of this body **/
	private Array<Fixture> fixtures = new Array<Fixture>(2);

	/** Joints of this body **/
	protected Array<JointEdge> joints = new Array<JointEdge>(2);

	/** user data **/
	private Object userData;

	/** Constructs a new body with the given address
	 * @param world the world
	 * @param addr the address */
	protected Body (World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	/** Resets this body after fetching it from the {@link World#freeBodies} Pool. */
	protected void reset (long addr) {
		this.addr = addr;
		this.userData = null;
		for (int i = 0; i < fixtures.size; i++)
			this.world.freeFixtures.free(fixtures.get(i));
		fixtures.clear();
		this.joints.clear();
	}

	/** Creates a fixture and attach it to this body. Use this function if you need to set some fixture parameters, like friction.
	 * Otherwise you can create the fixture directly from a shape. If the density is non-zero, this function automatically updates
	 * the mass of the body. Contacts are not created until the next time step.
	 * @param def the fixture definition.
	 * @warning This function is locked during callbacks. */
	public Fixture createFixture (FixtureDef def) {
		long fixtureAddr = jniCreateFixture(addr, def.shape.addr, def.friction, def.restitution, def.density, def.isSensor,
			def.filter.categoryBits, def.filter.maskBits, def.filter.groupIndex);
		Fixture fixture = this.world.freeFixtures.obtain();
		fixture.reset(this, fixtureAddr);
		this.world.fixtures.put(fixture.addr, fixture);
		this.fixtures.add(fixture);
		return fixture;
	}

	private native long jniCreateFixture (long addr, long shapeAddr, float friction, float restitution, float density,
		boolean isSensor, short filterCategoryBits, short filterMaskBits, short filterGroupIndex); /*
	b2Body* body = (b2Body*)addr;
	b2Shape* shape = (b2Shape*)shapeAddr;
	b2FixtureDef fixtureDef;

	fixtureDef.shape = shape;
	fixtureDef.friction = friction;
	fixtureDef.restitution = restitution;
	fixtureDef.density = density;
	fixtureDef.isSensor = isSensor;
	fixtureDef.filter.maskBits = filterMaskBits;
	fixtureDef.filter.categoryBits = filterCategoryBits;
	fixtureDef.filter.groupIndex = filterGroupIndex;

	return (jlong)body->CreateFixture( &fixtureDef );
	*/

	/** Creates a fixture from a shape and attach it to this body. This is a convenience function. Use b2FixtureDef if you need to
	 * set parameters like friction, restitution, user data, or filtering. If the density is non-zero, this function automatically
	 * updates the mass of the body.
	 * @param shape the shape to be cloned.
	 * @param density the shape density (set to zero for static bodies).
	 * @warning This function is locked during callbacks. */
	public Fixture createFixture (Shape shape, float density) {
		long fixtureAddr = jniCreateFixture(addr, shape.addr, density);
		Fixture fixture = this.world.freeFixtures.obtain();
		fixture.reset(this, fixtureAddr);
		this.world.fixtures.put(fixture.addr, fixture);
		this.fixtures.add(fixture);
		return fixture;
	}

	private native long jniCreateFixture (long addr, long shapeAddr, float density); /*
		b2Body* body = (b2Body*)addr;
		b2Shape* shape = (b2Shape*)shapeAddr;
		return (jlong)body->CreateFixture( shape, density );
	*/

	/** Destroy a fixture. This removes the fixture from the broad-phase and destroys all contacts associated with this fixture.
	 * This will automatically adjust the mass of the body if the body is dynamic and the fixture has positive density. All
	 * fixtures attached to a body are implicitly destroyed when the body is destroyed.
	 * @param fixture the fixture to be removed.
	 * @warning This function is locked during callbacks. */
	public void destroyFixture (Fixture fixture) {
		this.world.destroyFixture(this, fixture);
		fixture.setUserData(null);
		this.world.fixtures.remove(fixture.addr);
		this.fixtures.removeValue(fixture, true);
		this.world.freeFixtures.free(fixture);
	}

	/** Set the position of the body's origin and rotation. This breaks any contacts and wakes the other bodies. Manipulating a
	 * body's transform may cause non-physical behavior.
	 * @param position the world position of the body's local origin.
	 * @param angle the world rotation in radians. */
	public void setTransform (Vector2 position, float angle) {
		jniSetTransform(addr, position.x, position.y, angle);
	}

	/** Set the position of the body's origin and rotation. This breaks any contacts and wakes the other bodies. Manipulating a
	 * body's transform may cause non-physical behavior.
	 * @param x the world position on the x-axis
	 * @param y the world position on the y-axis
	 * @param angle the world rotation in radians. */
	public void setTransform (float x, float y, float angle) {
		jniSetTransform(addr, x, y, angle);
	}

	private native void jniSetTransform (long addr, float positionX, float positionY, float angle); /*
		b2Body* body = (b2Body*)addr;
		body->SetTransform(b2Vec2(positionX, positionY), angle);
	*/

	private final Transform transform = new Transform();
	
	/** Get the body transform for the body's origin. */
	public Transform getTransform () {
		jniGetTransform(addr, transform.vals);
		return transform;
	}

	private native void jniGetTransform (long addr, float[] vals); /*
		b2Body* body = (b2Body*)addr;
		b2Transform t = body->GetTransform();
		vals[0] = t.p.x;
		vals[1] = t.p.y;
		vals[2] = t.q.c;
		vals[3] = t.q.s;
	*/

	private final Vector2 position = new Vector2();

	/** Get the world body origin position.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @return the world position of the body's origin. */
	public Vector2 getPosition () {
		jniGetPosition(addr, tmp);
		position.x = tmp[0];
		position.y = tmp[1];
		return position;
	}

	private native void jniGetPosition (long addr, float[] position); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 p = body->GetPosition();
		position[0] = p.x;
		position[1] = p.y;
	*/

	/** Get the angle in radians.
	 * @return the current world rotation angle in radians. */
	public float getAngle () {
		return jniGetAngle(addr);
	}

	private native float jniGetAngle (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetAngle();
	*/

	private final Vector2 worldCenter = new Vector2();
	
	/** Get the world position of the center of mass.
	 * Note that the same Vector2 instance is returned each time this method is called. */
	public Vector2 getWorldCenter () {
		jniGetWorldCenter(addr, tmp);
		worldCenter.x = tmp[0];
		worldCenter.y = tmp[1];
		return worldCenter;
	}

	private native void jniGetWorldCenter (long addr, float[] worldCenter); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldCenter();
		worldCenter[0] = w.x;
		worldCenter[1] = w.y;
	*/

	private final Vector2 localCenter = new Vector2();
	
	/** Get the local position of the center of mass.
	 * Note that the same Vector2 instance is returned each time this method is called. */
	public Vector2 getLocalCenter () {
		jniGetLocalCenter(addr, tmp);
		localCenter.x = tmp[0];
		localCenter.y = tmp[1];
		return localCenter;
	}

	private native void jniGetLocalCenter (long addr, float[] localCenter); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalCenter();
		localCenter[0] = w.x;
		localCenter[1] = w.y;
	*/

	/** Set the linear velocity of the center of mass. */
	public void setLinearVelocity (Vector2 v) {
		jniSetLinearVelocity(addr, v.x, v.y);
	}

	/** Set the linear velocity of the center of mass. */
	public void setLinearVelocity (float vX, float vY) {
		jniSetLinearVelocity(addr, vX, vY);
	}

	private native void jniSetLinearVelocity (long addr, float x, float y); /*
		b2Body* body = (b2Body*)addr;
		body->SetLinearVelocity(b2Vec2(x, y));
	*/

	private final Vector2 linearVelocity = new Vector2();
	
	/** Get the linear velocity of the center of mass.
	 * Note that the same Vector2 instance is returned each time this method is called. */
	public Vector2 getLinearVelocity () {
		jniGetLinearVelocity(addr, tmp);
		linearVelocity.x = tmp[0];
		linearVelocity.y = tmp[1];
		return linearVelocity;
	}

	private native void jniGetLinearVelocity (long addr, float[] linearVelocity); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 l = body->GetLinearVelocity();
		linearVelocity[0] = l.x;
		linearVelocity[1] = l.y;
	*/

	/** Set the angular velocity. */
	public void setAngularVelocity (float omega) {
		jniSetAngularVelocity(addr, omega);
	}

	private native void jniSetAngularVelocity (long addr, float omega); /*
		b2Body* body = (b2Body*)addr;
		body->SetAngularVelocity(omega);
	*/

	/** Get the angular velocity. */
	public float getAngularVelocity () {
		return jniGetAngularVelocity(addr);
	}

	private native float jniGetAngularVelocity (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetAngularVelocity();
	*/

	/** Apply a force at a world point. If the force is not applied at the center of mass, it will generate a torque and affect the
	 * angular velocity. This wakes up the body.
	 * @param force the world force vector, usually in Newtons (N).
	 * @param point the world position of the point of application.
	 * @param wake up the body */
	public void applyForce (Vector2 force, Vector2 point, boolean wake) {
		jniApplyForce(addr, force.x, force.y, point.x, point.y, wake);
	}

	/** Apply a force at a world point. If the force is not applied at the center of mass, it will generate a torque and affect the
	 * angular velocity. This wakes up the body.
	 * @param forceX the world force vector on x, usually in Newtons (N).
	 * @param forceY the world force vector on y, usually in Newtons (N).
	 * @param pointX the world position of the point of application on x.
	 * @param pointY the world position of the point of application on y. 
	 * @param wake up the body*/
	public void applyForce (float forceX, float forceY, float pointX, float pointY, boolean wake) {
		jniApplyForce(addr, forceX, forceY, pointX, pointY, wake);
	}

	private native void jniApplyForce (long addr, float forceX, float forceY, float pointX, float pointY, boolean wake); /*
		b2Body* body = (b2Body*)addr;
		body->ApplyForce(b2Vec2(forceX, forceY), b2Vec2(pointX, pointY), wake);
	*/

	/** Apply a force to the center of mass. This wakes up the body.
	 * @param force the world force vector, usually in Newtons (N). */
	public void applyForceToCenter (Vector2 force, boolean wake) {
		jniApplyForceToCenter(addr, force.x, force.y, wake);
	}

	/** Apply a force to the center of mass. This wakes up the body.
	 * @param forceX the world force vector, usually in Newtons (N).
	 * @param forceY the world force vector, usually in Newtons (N). */
	public void applyForceToCenter (float forceX, float forceY, boolean wake) {
		jniApplyForceToCenter(addr, forceX, forceY, wake);
	}

	private native void jniApplyForceToCenter (long addr, float forceX, float forceY, boolean wake); /*
		b2Body* body = (b2Body*)addr;
		body->ApplyForceToCenter(b2Vec2(forceX, forceY), wake);
	*/

	/** Apply a torque. This affects the angular velocity without affecting the linear velocity of the center of mass. This wakes up
	 * the body.
	 * @param torque about the z-axis (out of the screen), usually in N-m.
	 * @param wake up the body */
	public void applyTorque (float torque, boolean wake) {
		jniApplyTorque(addr, torque, wake);
	}

	private native void jniApplyTorque (long addr, float torque, boolean wake); /*
		b2Body* body = (b2Body*)addr;
		body->ApplyTorque(torque, wake);
	*/

	/** Apply an impulse at a point. This immediately modifies the velocity. It also modifies the angular velocity if the point of
	 * application is not at the center of mass. This wakes up the body.
	 * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
	 * @param point the world position of the point of application. 
	 * @param wake up the body*/
	public void applyLinearImpulse (Vector2 impulse, Vector2 point, boolean wake) {
		jniApplyLinearImpulse(addr, impulse.x, impulse.y, point.x, point.y, wake);
	}

	/** Apply an impulse at a point. This immediately modifies the velocity. It also modifies the angular velocity if the point of
	 * application is not at the center of mass. This wakes up the body.
	 * @param impulseX the world impulse vector on the x-axis, usually in N-seconds or kg-m/s.
	 * @param impulseY the world impulse vector on the y-axis, usually in N-seconds or kg-m/s.
	 * @param pointX the world position of the point of application on the x-axis.
	 * @param pointY the world position of the point of application on the y-axis. 
	 * @param wake up the body*/
	public void applyLinearImpulse (float impulseX, float impulseY, float pointX, float pointY, boolean wake) {
		jniApplyLinearImpulse(addr, impulseX, impulseY, pointX, pointY, wake);
	}

	private native void jniApplyLinearImpulse (long addr, float impulseX, float impulseY, float pointX, float pointY, boolean wake); /*
		b2Body* body = (b2Body*)addr;
		body->ApplyLinearImpulse( b2Vec2( impulseX, impulseY ), b2Vec2( pointX, pointY ), wake);
	*/

	/** Apply an angular impulse.
	 * @param impulse the angular impulse in units of kg*m*m/s */
	public void applyAngularImpulse (float impulse, boolean wake) {
		jniApplyAngularImpulse(addr, impulse, wake);
	}

	private native void jniApplyAngularImpulse (long addr, float impulse, boolean wake); /*
		b2Body* body = (b2Body*)addr;
		body->ApplyAngularImpulse(impulse, wake);
	*/

	/** Get the total mass of the body.
	 * @return the mass, usually in kilograms (kg). */
	public float getMass () {
		return jniGetMass(addr);
	}

	private native float jniGetMass (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetMass();
	*/

	/** Get the rotational inertia of the body about the local origin.
	 * @return the rotational inertia, usually in kg-m^2. */
	public float getInertia () {
		return jniGetInertia(addr);
	}

	private native float jniGetInertia (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetInertia();
	*/

	private final MassData massData = new MassData();

	/** Get the mass data of the body.
	 * @return a struct containing the mass, inertia and center of the body. */
	public MassData getMassData () {
		jniGetMassData(addr, tmp);
		massData.mass = tmp[0];
		massData.center.x = tmp[1];
		massData.center.y = tmp[2];
		massData.I = tmp[3];
		return massData;
	}

	private native void jniGetMassData (long addr, float[] massData); /*
		b2Body* body = (b2Body*)addr;
		b2MassData m;
		body->GetMassData(&m);
		massData[0] = m.mass;
		massData[1] = m.center.x;
		massData[2] = m.center.y;
		massData[3] = m.I;
	*/

	/** Set the mass properties to override the mass properties of the fixtures. Note that this changes the center of mass position.
	 * Note that creating or destroying fixtures can also alter the mass. This function has no effect if the body isn't dynamic.
	 * @param data the mass properties. */
	public void setMassData (MassData data) {
		jniSetMassData(addr, data.mass, data.center.x, data.center.y, data.I);
	}

	private native void jniSetMassData (long addr, float mass, float centerX, float centerY, float I); /*
		b2Body* body = (b2Body*)addr;
		b2MassData m;
		m.mass = mass;
		m.center.x = centerX;
		m.center.y = centerY;
		m.I = I;
		body->SetMassData(&m);
	*/

	/** This resets the mass properties to the sum of the mass properties of the fixtures. This normally does not need to be called
	 * unless you called SetMassData to override the mass and you later want to reset the mass. */
	public void resetMassData () {
		jniResetMassData(addr);
	}

	private native void jniResetMassData (long addr); /*
		b2Body* body = (b2Body*)addr;
		body->ResetMassData();
	*/

	private final Vector2 localPoint = new Vector2();

	/** Get the world coordinates of a point given the local coordinates.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param localPoint a point on the body measured relative the the body's origin.
	 * @return the same point expressed in world coordinates. */
	public Vector2 getWorldPoint (Vector2 localPoint) {
		jniGetWorldPoint(addr, localPoint.x, localPoint.y, tmp);
		this.localPoint.x = tmp[0];
		this.localPoint.y = tmp[1];
		return this.localPoint;
	}

	private native void jniGetWorldPoint (long addr, float localPointX, float localPointY, float[] worldPoint); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldPoint( b2Vec2( localPointX, localPointY ) );
		worldPoint[0] = w.x;
		worldPoint[1] = w.y;
	*/

	private final Vector2 worldVector = new Vector2();

	/** Get the world coordinates of a vector given the local coordinates.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param localVector a vector fixed in the body.
	 * @return the same vector expressed in world coordinates. */
	public Vector2 getWorldVector (Vector2 localVector) {
		jniGetWorldVector(addr, localVector.x, localVector.y, tmp);
		worldVector.x = tmp[0];
		worldVector.y = tmp[1];
		return worldVector;
	}

	private native void jniGetWorldVector (long addr, float localVectorX, float localVectorY, float[] worldVector); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetWorldVector( b2Vec2( localVectorX, localVectorY ) );
		worldVector[0] = w.x;
		worldVector[1] = w.y;
	*/

	public final Vector2 localPoint2 = new Vector2();

	/** Gets a local point relative to the body's origin given a world point.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param worldPoint a point in world coordinates.
	 * @return the corresponding local point relative to the body's origin. */
	public Vector2 getLocalPoint (Vector2 worldPoint) {
		jniGetLocalPoint(addr, worldPoint.x, worldPoint.y, tmp);
		localPoint2.x = tmp[0];
		localPoint2.y = tmp[1];
		return localPoint2;
	}

	private native void jniGetLocalPoint (long addr, float worldPointX, float worldPointY, float[] localPoint); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalPoint( b2Vec2( worldPointX, worldPointY ) );
		localPoint[0] = w.x;
		localPoint[1] = w.y;
	*/

	public final Vector2 localVector = new Vector2();

	/** Gets a local vector given a world vector.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param worldVector a vector in world coordinates.
	 * @return the corresponding local vector. */
	public Vector2 getLocalVector (Vector2 worldVector) {
		jniGetLocalVector(addr, worldVector.x, worldVector.y, tmp);
		localVector.x = tmp[0];
		localVector.y = tmp[1];
		return localVector;
	}

	private native void jniGetLocalVector (long addr, float worldVectorX, float worldVectorY, float[] worldVector); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLocalVector( b2Vec2( worldVectorX, worldVectorY ) );
		worldVector[0] = w.x;
		worldVector[1] = w.y;
	*/

	public final Vector2 linVelWorld = new Vector2();

	/** Get the world linear velocity of a world point attached to this body.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param worldPoint a point in world coordinates.
	 * @return the world velocity of a point. */
	public Vector2 getLinearVelocityFromWorldPoint (Vector2 worldPoint) {
		jniGetLinearVelocityFromWorldPoint(addr, worldPoint.x, worldPoint.y, tmp);
		linVelWorld.x = tmp[0];
		linVelWorld.y = tmp[1];
		return linVelWorld;
	}

	private native void jniGetLinearVelocityFromWorldPoint (long addr, float worldPointX, float worldPointY, float[] linVelWorld); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLinearVelocityFromWorldPoint( b2Vec2( worldPointX, worldPointY ) );
		linVelWorld[0] = w.x;
		linVelWorld[1] = w.y;
	*/

	public final Vector2 linVelLoc = new Vector2();

	/** Get the world velocity of a local point.
	 * Note that the same Vector2 instance is returned each time this method is called.
	 * @param localPoint a point in local coordinates.
	 * @return the world velocity of a point. */
	public Vector2 getLinearVelocityFromLocalPoint (Vector2 localPoint) {
		jniGetLinearVelocityFromLocalPoint(addr, localPoint.x, localPoint.y, tmp);
		linVelLoc.x = tmp[0];
		linVelLoc.y = tmp[1];
		return linVelLoc;
	}

	private native void jniGetLinearVelocityFromLocalPoint (long addr, float localPointX, float localPointY, float[] linVelLoc); /*
		b2Body* body = (b2Body*)addr;
		b2Vec2 w = body->GetLinearVelocityFromLocalPoint( b2Vec2( localPointX, localPointY ) );
		linVelLoc[0] = w.x;
		linVelLoc[1] = w.y;
	*/

	/** Get the linear damping of the body. */
	public float getLinearDamping () {
		return jniGetLinearDamping(addr);
	}

	private native float jniGetLinearDamping (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetLinearDamping();
	*/

	/** Set the linear damping of the body. */
	public void setLinearDamping (float linearDamping) {
		jniSetLinearDamping(addr, linearDamping);
	}

	private native void jniSetLinearDamping (long addr, float linearDamping); /*
		b2Body* body = (b2Body*)addr;
		body->SetLinearDamping(linearDamping);
	*/

	/** Get the angular damping of the body. */
	public float getAngularDamping () {
		return jniGetAngularDamping(addr);
	}

	private native float jniGetAngularDamping (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetAngularDamping();
	*/

	/** Set the angular damping of the body. */
	public void setAngularDamping (float angularDamping) {
		jniSetAngularDamping(addr, angularDamping);
	}

	private native void jniSetAngularDamping (long addr, float angularDamping); /*
		b2Body* body = (b2Body*)addr;
		body->SetAngularDamping(angularDamping);
	*/

	/** Set the type of this body. This may alter the mass and velocity. */
	public void setType (BodyType type) {
		jniSetType(addr, type.getValue());
	}
	
	// @off
	/*JNI
inline b2BodyType getBodyType( int type )
{
	switch( type )
	{
	case 0: return b2_staticBody;
	case 1: return b2_kinematicBody;
	case 2: return b2_dynamicBody;
	default:
		return b2_staticBody;
	}
}	 
*/

	private native void jniSetType (long addr, int type); /*
		b2Body* body = (b2Body*)addr;
		body->SetType(getBodyType(type));
	*/

	/** Get the type of this body. */
	public BodyType getType () {
		int type = jniGetType(addr);
		if (type == 0) return BodyType.StaticBody;
		if (type == 1) return BodyType.KinematicBody;
		if (type == 2) return BodyType.DynamicBody;
		return BodyType.StaticBody;
	}

	private native int jniGetType (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetType();
	*/

	/** Should this body be treated like a bullet for continuous collision detection? */
	public void setBullet (boolean flag) {
		jniSetBullet(addr, flag);
	}

	private native void jniSetBullet (long addr, boolean flag); /*
		b2Body* body = (b2Body*)addr;
		body->SetBullet(flag);
	*/

	/** Is this body treated like a bullet for continuous collision detection? */
	public boolean isBullet () {
		return jniIsBullet(addr);
	}

	private native boolean jniIsBullet (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->IsBullet();
	*/

	/** You can disable sleeping on this body. If you disable sleeping, the */
	public void setSleepingAllowed (boolean flag) {
		jniSetSleepingAllowed(addr, flag);
	}

	private native void jniSetSleepingAllowed (long addr, boolean flag); /*
		b2Body* body = (b2Body*)addr;
		body->SetSleepingAllowed(flag);
	*/

	/** Is this body allowed to sleep */
	public boolean isSleepingAllowed () {
		return jniIsSleepingAllowed(addr);
	}

	private native boolean jniIsSleepingAllowed (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->IsSleepingAllowed();
	*/

	/** Set the sleep state of the body. A sleeping body has very low CPU cost.
	 * @param flag set to true to wake the body, false to put it to sleep. */
	public void setAwake (boolean flag) {
		jniSetAwake(addr, flag);
	}

	private native void jniSetAwake (long addr, boolean flag); /*
		b2Body* body = (b2Body*)addr;
		body->SetAwake(flag);
	*/

	/** Get the sleeping state of this body.
	 * @return true if the body is not sleeping. */
	public boolean isAwake () {
		return jniIsAwake(addr);
	}

	private native boolean jniIsAwake (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->IsAwake();
	*/

	/** Set the active state of the body. An inactive body is not simulated and cannot be collided with or woken up. If you pass a
	 * flag of true, all fixtures will be added to the broad-phase. If you pass a flag of false, all fixtures will be removed from
	 * the broad-phase and all contacts will be destroyed. Fixtures and joints are otherwise unaffected. You may continue to
	 * create/destroy fixtures and joints on inactive bodies. Fixtures on an inactive body are implicitly inactive and will not
	 * participate in collisions, ray-casts, or queries. Joints connected to an inactive body are implicitly inactive. An inactive
	 * body is still owned by a b2World object and remains in the body list. */
	public void setActive (boolean flag) {
		if (flag) {
			jniSetActive(addr, flag);
		} else {
			this.world.deactivateBody(this);
		}
	}

	private native void jniSetActive (long addr, boolean flag); /*
		b2Body* body = (b2Body*)addr;
		body->SetActive(flag);
	*/

	/** Get the active state of the body. */
	public boolean isActive () {
		return jniIsActive(addr);
	}

	private native boolean jniIsActive (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->IsActive();
	*/

	/** Set this body to have fixed rotation. This causes the mass to be reset. */
	public void setFixedRotation (boolean flag) {
		jniSetFixedRotation(addr, flag);
	}

	private native void jniSetFixedRotation (long addr, boolean flag); /*
		b2Body* body = (b2Body*)addr;
		body->SetFixedRotation(flag);
	*/

	/** Does this body have fixed rotation? */
	public boolean isFixedRotation () {
		return jniIsFixedRotation(addr);
	}

	private native boolean jniIsFixedRotation (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->IsFixedRotation();
	*/

	/** Get the list of all fixtures attached to this body. Do not modify the list! */
	public Array<Fixture> getFixtureList () {
		return fixtures;
	}

	/** Get the list of all joints attached to this body. Do not modify the list! */
	public Array<JointEdge> getJointList () {
		return joints;
	}

	/** Get the list of all contacts attached to this body.
	 * @warning this list changes during the time step and you may miss some collisions if you don't use b2ContactListener. Do not
	 *          modify the returned list! */
// Array<ContactEdge> getContactList()
// {
// return contacts;
// }

	/** @return Get the gravity scale of the body. */
	public float getGravityScale () {
		return jniGetGravityScale(addr);
	}

	private native float jniGetGravityScale (long addr); /*
		b2Body* body = (b2Body*)addr;
		return body->GetGravityScale();
	*/

	/** Sets the gravity scale of the body */
	public void setGravityScale (float scale) {
		jniSetGravityScale(addr, scale);
	}

	private native void jniSetGravityScale (long addr, float scale); /*
		b2Body* body = (b2Body*)addr;
		body->SetGravityScale(scale);
	*/

	/** Get the parent world of this body. */
	public World getWorld () {
		return world;
	}

	/** Get the user data */
	public Object getUserData () {
		return userData;
	}

	/** Set the user data */
	public void setUserData (Object userData) {
		this.userData = userData;
	}
}

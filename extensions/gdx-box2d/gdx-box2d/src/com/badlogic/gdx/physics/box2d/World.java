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
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.*;

import java.util.Iterator;

/** The world class manages all physics entities, dynamic simulation, and asynchronous queries. The world also contains efficient
 * memory management facilities.
 * @author mzechner */
public final class World implements Disposable {
	// @off
	/*JNI
#include <box2d/box2d.h>

static jclass worldClass = 0;
static jmethodID shouldCollideID = 0;
static jmethodID beginContactID = 0;
static jmethodID endContactID = 0;
static jmethodID preSolveID = 0;
static jmethodID postSolveID = 0;
static jmethodID reportFixtureID = 0;
static jmethodID reportRayFixtureID = 0;

class CustomRayCastCallback: public b2RayCastCallback
{
private:
	JNIEnv* env;
	jobject obj;

public:
	CustomRayCastCallback( JNIEnv *env, jobject obj )
	{
		this->env = env;
		this->obj = obj;
	}

	virtual float ReportFixture( b2Fixture* fixture, const b2Vec2& point, const b2Vec2& normal, float fraction)
	{
		return env->CallFloatMethod(obj, reportRayFixtureID, (jlong)fixture, (jfloat)point.x, (jfloat)point.y,
																(jfloat)normal.x, (jfloat)normal.y, (jfloat)fraction );
	}
};

class CustomContactFilter: public b2ContactFilter
{
private:
	JNIEnv* env;
	jobject obj;

public:
	CustomContactFilter( JNIEnv* env, jobject obj )
	{
		this->env = env;
		this->obj = obj;
	}

	virtual bool ShouldCollide(b2Fixture* fixtureA, b2Fixture* fixtureB)
	{
		if( shouldCollideID != 0 )
			return env->CallBooleanMethod( obj, shouldCollideID, (jlong)fixtureA, (jlong)fixtureB );
		else
			return true;
	}
};

class CustomContactListener: public b2ContactListener
{
private:
	JNIEnv* env;
	jobject obj;

public:
		CustomContactListener( JNIEnv* env, jobject obj )
		{
			this->env = env;
			this->obj = obj;
		}

		/// Called when two fixtures begin to touch.
		virtual void BeginContact(b2Contact* contact)
		{
			if( beginContactID != 0 )
				env->CallVoidMethod(obj, beginContactID, (jlong)contact );
		}

		/// Called when two fixtures cease to touch.
		virtual void EndContact(b2Contact* contact)
		{
			if( endContactID != 0 )
				env->CallVoidMethod(obj, endContactID, (jlong)contact);
		}
		
		/// This is called after a contact is updated.
		virtual void PreSolve(b2Contact* contact, const b2Manifold* oldManifold)
		{
			if( preSolveID != 0 )
				env->CallVoidMethod(obj, preSolveID, (jlong)contact, (jlong)oldManifold);
		}
	
		/// This lets you inspect a contact after the solver is finished.
		virtual void PostSolve(b2Contact* contact, const b2ContactImpulse* impulse)
		{
			if( postSolveID != 0 )
				env->CallVoidMethod(obj, postSolveID, (jlong)contact, (jlong)impulse);
		}
};

class CustomQueryCallback: public b2QueryCallback
{
private:
	JNIEnv* env;
	jobject obj;

public:
	CustomQueryCallback( JNIEnv* env, jobject obj )
	{
		this->env = env;
		this->obj = obj;
	}

	virtual bool ReportFixture( b2Fixture* fixture )
	{
		return env->CallBooleanMethod(obj, reportFixtureID, (jlong)fixture );
	}
}; 

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

b2ContactFilter defaultFilter;
	 */ // @on

	static {
		new SharedLibraryLoader().load("gdx-box2d");
	}

	/** pool for bodies **/
	private final Pool<Body> freeBodies = new Pool<Body>(100, 200) {
		@Override
		protected Body newObject () {
			return new Body(World.this, 0);
		}
	};

	/** pool for fixtures **/
	final Pool<Fixture> freeFixtures = new Pool<Fixture>(100, 200) {
		@Override
		protected Fixture newObject () {
			return new Fixture(null, 0);
		}
	};

	/** the address of the world instance **/
	private final long addr;

	/** all known bodies **/
	final LongMap<Body> bodies = new LongMap<>(100);

	/** all known fixtures **/
	final LongMap<Fixture> fixtures = new LongMap<>(100);

	/** all known joints **/
	private final LongMap<Joint> joints = new LongMap<>(100);

	/** Contact filter **/
	private ContactFilter contactFilter = null;

	/** Contact listener **/
	private ContactListener contactListener = null;

	public World (Vector2 gravity) {
		this(gravity, false);
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

	private native long newWorld (float gravityX, float gravityY, boolean doSleep); /*
		// @off
		// we leak one global ref. 
		if(!worldClass) {
			worldClass = (jclass)env->NewGlobalRef(env->GetObjectClass(object));
			beginContactID = env->GetMethodID(worldClass, "beginContact", "(J)V" );
			endContactID = env->GetMethodID( worldClass, "endContact", "(J)V" );
			preSolveID = env->GetMethodID( worldClass, "preSolve", "(JJ)V" );
			postSolveID = env->GetMethodID( worldClass, "postSolve", "(JJ)V" );
			reportFixtureID = env->GetMethodID(worldClass, "reportFixture", "(J)Z" );
			reportRayFixtureID = env->GetMethodID(worldClass, "reportRayFixture", "(JFFFFF)F" );
			shouldCollideID = env->GetMethodID( worldClass, "contactFilter", "(JJ)Z");
		}
	
		b2World* world = new b2World( b2Vec2( gravityX, gravityY ));
		world->SetAllowSleeping( doSleep );
		return (jlong)world;
	*/ // @on

	/** Register a destruction listener. The listener is owned by you and must remain in scope. */
	public void setDestructionListener (DestructionListener listener) {

	}

	/** Register a contact filter to provide specific control over collision. Otherwise the default filter is used
	 * (b2_defaultFilter). The listener is owned by you and must remain in scope. */
	public void setContactFilter (ContactFilter filter) {
		this.contactFilter = filter;
		setUseDefaultContactFilter(filter == null);
	}

	/** tells the native code not to call the Java world class if use is false **/
	private native void setUseDefaultContactFilter (boolean use); /*
																						 * // FIXME
																						 */

	/** Register a contact event listener. The listener is owned by you and must remain in scope. */
	public void setContactListener (ContactListener listener) {
		this.contactListener = listener;
	}

	/** Create a rigid body given a definition. No reference to the definition is retained. Bodies created by this method are
	 * pooled internally by the World object. They will be freed upon calling {@link World#destroyBody(Body)}
	 * @see Pool
	 * @warning This function is locked during callbacks. */
	public Body createBody (BodyDef def) {
		long bodyAddr = jniCreateBody(addr, def.type.value, def.position.x, def.position.y, def.angle, def.linearVelocity.x,
			def.linearVelocity.y, def.angularVelocity, def.linearDamping, def.angularDamping, def.allowSleep, def.awake,
			def.fixedRotation, def.bullet, def.enabled, def.gravityScale);
		Body body = freeBodies.obtain();
		body.reset(bodyAddr);
		this.bodies.put(body.addr, body);
		return body;
	}

	private native long jniCreateBody (long addr, int type, float positionX, float positionY, float angle, float linearVelocityX,
		float linearVelocityY, float angularVelocity, float linearDamping, float angularDamping, boolean allowSleep, boolean awake,
		boolean fixedRotation, boolean bullet, boolean enabled, float inertiaScale); /*
		// @off
		b2BodyDef bodyDef;
		bodyDef.type = getBodyType(type);
		bodyDef.position.Set( positionX, positionY );
		bodyDef.angle = angle;
		bodyDef.linearVelocity.Set( linearVelocityX, linearVelocityY );
		bodyDef.angularVelocity = angularVelocity;
		bodyDef.linearDamping = linearDamping;
		bodyDef.angularDamping = angularDamping;
		bodyDef.allowSleep = allowSleep;
		bodyDef.awake = awake;
		bodyDef.fixedRotation = fixedRotation;
		bodyDef.bullet = bullet;
		bodyDef.enabled = enabled;
		bodyDef.gravityScale = inertiaScale;
	
		b2World* world = (b2World*)addr;
		b2Body* body = world->CreateBody( &bodyDef );
		return (jlong)body;
	*/ // @on

	/** Destroy a rigid body given a definition. No reference to the definition is retained. This function is locked during
	 * callbacks.
	 * @warning This automatically deletes all associated shapes and joints.
	 * @warning This function is locked during callbacks. */
	public void destroyBody (Body body) {
		Array<JointEdge> jointList = body.getJointList();
		while (jointList.size > 0)
			destroyJoint(body.getJointList().get(0).joint);
		jniDestroyBody(addr, body.addr);
		body.setUserData(null);
		this.bodies.remove(body.addr);
		Array<Fixture> fixtureList = body.getFixtureList();
		while (fixtureList.size > 0) {
			Fixture fixtureToDelete = fixtureList.removeIndex(0);
			fixtureToDelete.setUserData(null);
			this.fixtures.remove(fixtureToDelete.addr);
			freeFixtures.free(fixtureToDelete);
		}

		freeBodies.free(body);
	}

	private native void jniDestroyBody (long addr, long bodyAddr); /*
		// @off
		b2World* world = (b2World*)addr;
		b2Body* body = (b2Body*)bodyAddr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->DestroyBody(body);
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	*/ // @on

	/** Internal method for fixture destruction with notifying custom contact listener
	 * @param body
	 * @param fixture */
	void destroyFixture (Body body, Fixture fixture) {
		jniDestroyFixture(addr, body.addr, fixture.addr);
	}

	private native void jniDestroyFixture (long addr, long bodyAddr, long fixtureAddr); /*
		// @off
		b2World* world = (b2World*)(addr);
		b2Body* body = (b2Body*)(bodyAddr);
		b2Fixture* fixture = (b2Fixture*)(fixtureAddr);
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env, object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		body->DestroyFixture(fixture);
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	*/ // @on

	/** Internal method for body deactivation with notifying custom contact listener
	 * @param body */
	void disableBody (Body body) {
		jniDisableBody(addr, body.addr);
	}

	private native void jniDisableBody (long addr, long bodyAddr); /*
		// @off
		b2World* world = (b2World*)(addr);
		b2Body* body = (b2Body*)(bodyAddr);	
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env, object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		body->SetEnabled(false);
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	*/ // @on

	/** Create a joint to constrain bodies together. No reference to the definition is retained. This may cause the connected
	 * bodies to cease colliding.
	 * @warning This function is locked during callbacks. */
	public Joint createJoint (JointDef def) {
		long jointAddr = createProperJoint(def);
		Joint joint = null;
		if (def.type == JointType.DistanceJoint) joint = new DistanceJoint(this, jointAddr);
		if (def.type == JointType.FrictionJoint) joint = new FrictionJoint(this, jointAddr);
		if (def.type == JointType.GearJoint)
			joint = new GearJoint(this, jointAddr, ((GearJointDef)def).joint1, ((GearJointDef)def).joint2);
		if (def.type == JointType.MotorJoint) joint = new MotorJoint(this, jointAddr);
		if (def.type == JointType.MouseJoint) joint = new MouseJoint(this, jointAddr);
		if (def.type == JointType.PrismaticJoint) joint = new PrismaticJoint(this, jointAddr);
		if (def.type == JointType.PulleyJoint) joint = new PulleyJoint(this, jointAddr);
		if (def.type == JointType.RevoluteJoint) joint = new RevoluteJoint(this, jointAddr);
		if (def.type == JointType.WeldJoint) joint = new WeldJoint(this, jointAddr);
		if (def.type == JointType.WheelJoint) joint = new WheelJoint(this, jointAddr);
		if (joint == null) throw new GdxRuntimeException("Unknown joint type: " + def.type);
		joints.put(joint.addr, joint);
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
				d.localAnchorB.x, d.localAnchorB.y, d.length, d.stiffness, d.damping);
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
		if (def.type == JointType.MotorJoint) {
			MotorJointDef d = (MotorJointDef)def;
			return jniCreateMotorJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.linearOffset.x, d.linearOffset.y,
				d.angularOffset, d.maxForce, d.maxTorque, d.correctionFactor);
		}
		if (def.type == JointType.MouseJoint) {
			MouseJointDef d = (MouseJointDef)def;
			return jniCreateMouseJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.target.x, d.target.y, d.maxForce,
				d.stiffness, d.damping);
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
				d.localAnchorB.x, d.localAnchorB.y, d.referenceAngle, d.stiffness, d.damping);
		}
		if (def.type == JointType.WheelJoint) {
			WheelJointDef d = (WheelJointDef)def;
			return jniCreateWheelJoint(addr, d.bodyA.addr, d.bodyB.addr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.localAxisA.x, d.localAxisA.y, d.enableLimit, d.lowerTranslation,
				d.upperTranslation, d.enableMotor, d.maxMotorTorque, d.motorSpeed, d.stiffness, d.damping);
		}

		return 0;
	}

	private native long jniCreateWheelJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, boolean enableLimit,
		float lowerTranslation, float upperTranslation, boolean enableMotor, float maxMotorTorque, float motorSpeed,
		float stiffness, float damping); /*
		// @off
		b2World* world = (b2World*)addr;
		b2WheelJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.localAxisA = b2Vec2(localAxisAX, localAxisAY);
		def.enableLimit = enableLimit;
		def.lowerTranslation = lowerTranslation;
		def.upperTranslation = upperTranslation;
		def.enableMotor = enableMotor;
		def.maxMotorTorque = maxMotorTorque;
		def.motorSpeed = motorSpeed;
		def.stiffness = stiffness;
		def.damping = damping;
		
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateDistanceJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float length, float stiffness, float damping); /*
		// @off
		b2World* world = (b2World*)addr;
		b2DistanceJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.length = length;
		def.stiffness = stiffness;
		def.damping = damping;
	
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateFrictionJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float maxForce, float maxTorque); /*
		// @off
		b2World* world = (b2World*)addr;
		b2FrictionJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.maxForce = maxForce;
		def.maxTorque = maxTorque;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateGearJoint (long addr, long bodyA, long bodyB, boolean collideConnected, long joint1, long joint2,
		float ratio); /*
		// @off
		b2World* world = (b2World*)addr;
		b2GearJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.joint1 = (b2Joint*)joint1;
		def.joint2 = (b2Joint*)joint2;
		def.ratio = ratio;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateMotorJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float linearOffsetX,
		float linearOffsetY, float angularOffset, float maxForce, float maxTorque, float correctionFactor); /*
		// @off
		b2World* world = (b2World*)addr;
		b2MotorJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.linearOffset = b2Vec2( linearOffsetX, linearOffsetY );
		def.angularOffset = angularOffset;
		def.maxForce = maxForce;
		def.maxTorque = maxTorque;
		def.correctionFactor = correctionFactor;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateMouseJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float targetX,
		float targetY, float maxForce, float stiffness, float damping); /*
		// @off
		b2World* world = (b2World*)addr;
		b2MouseJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.target = b2Vec2( targetX, targetY );
		def.maxForce = maxForce;
		def.stiffness = stiffness;
		def.damping = damping;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreatePrismaticJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, float referenceAngle,
		boolean enableLimit, float lowerTranslation, float upperTranslation, boolean enableMotor, float maxMotorForce,
		float motorSpeed); /*
		// @off
		b2World* world = (b2World*)addr;
		b2PrismaticJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.localAxisA = b2Vec2( localAxisAX, localAxisAY );
		def.referenceAngle = referenceAngle;
		def.enableLimit = enableLimit;
		def.lowerTranslation = lowerTranslation;
		def.upperTranslation = upperTranslation;
		def.enableMotor = enableMotor;
		def.maxMotorForce = maxMotorForce;
		def.motorSpeed = motorSpeed;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreatePulleyJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float groundAnchorAX,
		float groundAnchorAY, float groundAnchorBX, float groundAnchorBY, float localAnchorAX, float localAnchorAY,
		float localAnchorBX, float localAnchorBY, float lengthA, float lengthB, float ratio); /*
		// @off
		b2World* world = (b2World*)addr;
		b2PulleyJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.groundAnchorA = b2Vec2( groundAnchorAX, groundAnchorAY );
		def.groundAnchorB = b2Vec2( groundAnchorBX, groundAnchorBY );
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.lengthA = lengthA;
		def.lengthB = lengthB;
		def.ratio = ratio;
	
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateRevoluteJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle, boolean enableLimit, float lowerAngle,
		float upperAngle, boolean enableMotor, float motorSpeed, float maxMotorTorque); /*
		// @off
		b2World* world = (b2World*)addr;
		b2RevoluteJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.referenceAngle = referenceAngle;
		def.enableLimit = enableLimit;
		def.lowerAngle = lowerAngle;
		def.upperAngle = upperAngle;
		def.enableMotor = enableMotor;
		def.motorSpeed = motorSpeed;
		def.maxMotorTorque = maxMotorTorque;
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	private native long jniCreateWeldJoint (long addr, long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,
		float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle, float stiffness, float damping); /*
		// @off
		b2World* world = (b2World*)addr;
		b2WeldJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.referenceAngle = referenceAngle;
		def.stiffness = stiffness;
		def.damping = damping;
	
		return (jlong)world->CreateJoint(&def);
	*/ // @on

	/** Destroy a joint. This may cause the connected bodies to begin colliding.
	 * @warning This function is locked during callbacks. */
	public void destroyJoint (Joint joint) {
		joint.setUserData(null);
		joints.remove(joint.addr);
		joint.jointEdgeA.other.joints.removeValue(joint.jointEdgeB, true);
		joint.jointEdgeB.other.joints.removeValue(joint.jointEdgeA, true);
		jniDestroyJoint(addr, joint.addr);
	}

	private native void jniDestroyJoint (long addr, long jointAddr); /*
		// @off
		b2World* world = (b2World*)addr;
		b2Joint* joint = (b2Joint*)jointAddr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->DestroyJoint( joint );
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	*/ // @on

	/** Take a time step. This performs collision detection, integration, and constraint solution.
	 * @param timeStep the amount of time to simulate, this should not vary.
	 * @param velocityIterations for the velocity constraint solver.
	 * @param positionIterations for the position constraint solver. */
	public void step (float timeStep, int velocityIterations, int positionIterations) {
		jniStep(addr, timeStep, velocityIterations, positionIterations);
	}

	private native void jniStep (long addr, float timeStep, int velocityIterations, int positionIterations); /*
		// @off
		b2World* world = (b2World*)addr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->Step( timeStep, velocityIterations, positionIterations );
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	*/ // @on

	/** Manually clear the force buffer on all bodies. By default, forces are cleared automatically after each call to Step. The
	 * default behavior is modified by calling SetAutoClearForces. The purpose of this function is to support sub-stepping.
	 * Sub-stepping is often used to maintain a fixed sized time step under a variable frame-rate. When you perform sub-stepping
	 * you will disable auto clearing of forces and instead call ClearForces after all sub-steps are complete in one pass of your
	 * game loop. {@link #setAutoClearForces(boolean)} */
	public void clearForces () {
		jniClearForces(addr);
	}

	private native void jniClearForces (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		world->ClearForces();
	*/ // @on

	/** Enable/disable warm starting. For testing. */
	public void setWarmStarting (boolean flag) {
		jniSetWarmStarting(addr, flag);
	}

	private native void jniSetWarmStarting (long addr, boolean flag); /*
		// @off
		b2World* world = (b2World*)addr;
		world->SetWarmStarting(flag);
	*/ // @on

	/** Enable/disable continuous physics. For testing. */
	public void setContinuousPhysics (boolean flag) {
		jniSetContiousPhysics(addr, flag);
	}

	private native void jniSetContiousPhysics (long addr, boolean flag); /*
		// @off
		b2World* world = (b2World*)addr;
		world->SetContinuousPhysics(flag);
	*/ // @on

	/** Get the number of broad-phase proxies. */
	public int getProxyCount () {
		return jniGetProxyCount(addr);
	}

	private native int jniGetProxyCount (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->GetProxyCount();
	*/ // @on

	/** Get the number of bodies. */
	public int getBodyCount () {
		return jniGetBodyCount(addr);
	}

	private native int jniGetBodyCount (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->GetBodyCount();
	*/ // @on

	/** Get the number of fixtures. */
	public int getFixtureCount () {
		return fixtures.size;
	}

	/** Get the number of joints. */
	public int getJointCount () {
		return jniGetJointcount(addr);
	}

	private native int jniGetJointcount (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->GetJointCount();
	*/ // @on

	/** Get the number of contacts (each may have 0 or more contact points). */
	public int getContactCount () {
		return jniGetContactCount(addr);
	}

	private native int jniGetContactCount (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->GetContactCount();
	*/ // @on

	/** Change the global gravity vector. */
	public void setGravity (Vector2 gravity) {
		jniSetGravity(addr, gravity.x, gravity.y);
	}

	private native void jniSetGravity (long addr, float gravityX, float gravityY); /*
		// @off
		b2World* world = (b2World*)addr;
		world->SetGravity( b2Vec2( gravityX, gravityY ) );
	*/ // @on

	/** Get the global gravity vector. */
	final float[] tmpGravity = new float[2];
	final Vector2 gravity = new Vector2();

	public Vector2 getGravity () {
		jniGetGravity(addr, tmpGravity);
		gravity.x = tmpGravity[0];
		gravity.y = tmpGravity[1];
		return gravity;
	}

	private native void jniGetGravity (long addr, float[] gravity); /*
		// @off
		b2World* world = (b2World*)addr;
		b2Vec2 g = world->GetGravity();
		gravity[0] = g.x;
		gravity[1] = g.y;
	*/ // @on

	/** Is the world locked (in the middle of a time step). */
	public boolean isLocked () {
		return jniIsLocked(addr);
	}

	private native boolean jniIsLocked (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->IsLocked();
	*/ // @on

	/** Set flag to control automatic clearing of forces after each time step. */
	public void setAutoClearForces (boolean flag) {
		jniSetAutoClearForces(addr, flag);
	}

	private native void jniSetAutoClearForces (long addr, boolean flag); /*
		// @off
		b2World* world = (b2World*)addr;
		world->SetAutoClearForces(flag);
	*/ // @on

	/** Get the flag that controls automatic clearing of forces after each time step. */
	public boolean getAutoClearForces () {
		return jniGetAutoClearForces(addr);
	}

	private native boolean jniGetAutoClearForces (long addr); /*
		// @off
		b2World* world = (b2World*)addr;
		return world->GetAutoClearForces();
	*/ // @on

	/** Query the world for all fixtures that potentially overlap the provided AABB.
	 * @param callback a user implemented callback class.
	 * @param lowerX the x coordinate of the lower left corner
	 * @param lowerY the y coordinate of the lower left corner
	 * @param upperX the x coordinate of the upper right corner
	 * @param upperY the y coordinate of the upper right corner */
	public void queryAABB (QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		queryCallback = callback;
		jniQueryAABB(addr, lowerX, lowerY, upperX, upperY);
	}

	/** @deprecated use {@link #queryAABB} instead */
	@Deprecated
	public void QueryAABB (QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		queryAABB(callback, lowerX, lowerY, upperX, upperY);
	}

	public void queryAABB (QueryCallback callback, Vector2 lower, Vector2 upper) {
		queryAABB(callback, lower.x, lower.y, upper.x, upper.y);
	}

	private QueryCallback queryCallback = null;

	private native void jniQueryAABB (long addr, float lowX, float lowY, float upX, float upY); /*
		// @off
		b2World* world = (b2World*)addr;
		b2AABB aabb;
		aabb.lowerBound = b2Vec2( lowX, lowY );
		aabb.upperBound = b2Vec2( upX, upY );
	
		CustomQueryCallback callback( env, object );
		world->QueryAABB( &callback, aabb );
	*/ // @on

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
	private final Array<Contact> contacts = new Array<>();
	private final Array<Contact> freeContacts = new Array<>();

	/** Returns the list of {@link Contact} instances produced by the last call to {@link #step(float, int, int)}. Note that the
	 * returned list will have O(1) access times when using indexing. contacts are created and destroyed in the middle of a time
	 * step. Use {@link ContactListener} to avoid missing contacts
	 * @return the contact list */
	public Array<Contact> getContactList () {
		int numContacts = getContactCount();
		if (numContacts > contactAddrs.length) {
			int newSize = 2 * numContacts;
			contactAddrs = new long[newSize];
			contacts.ensureCapacity(newSize);
			freeContacts.ensureCapacity(newSize);
		}
		if (numContacts > freeContacts.size) {
			int freeConts = freeContacts.size;
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

	/** @param bodies an Array in which to place all bodies currently in the simulation */
	public void getBodies (Array<Body> bodies) {
		bodies.clear();
		bodies.ensureCapacity(this.bodies.size);
		for (Iterator<Body> iter = this.bodies.values(); iter.hasNext();) {
			bodies.add(iter.next());
		}
	}

	/** @param fixtures an Array in which to place all fixtures currently in the simulation */
	public void getFixtures (Array<Fixture> fixtures) {
		fixtures.clear();
		fixtures.ensureCapacity(this.fixtures.size);
		for (Iterator<Fixture> iter = this.fixtures.values(); iter.hasNext();) {
			fixtures.add(iter.next());
		}
	}

	/** @param joints an Array in which to place all joints currently in the simulation */
	public void getJoints (Array<Joint> joints) {
		joints.clear();
		joints.ensureCapacity(this.joints.size);
		for (Iterator<Joint> iter = this.joints.values(); iter.hasNext();) {
			joints.add(iter.next());
		}
	}

	private native void jniGetContactList (long addr, long[] contacts); /*
		// @off
		b2World* world = (b2World*)addr;
	
		b2Contact* contact = world->GetContactList();
		int i = 0;
		while( contact != 0 )
		{
			contacts[i++] = (long long)contact;
			contact = contact->GetNext();
		}
	*/ // @on

	public void dispose () {
		jniDispose(addr);
	}

	private native void jniDispose (long addr); /*
		// @off
		b2World* world = (b2World*)(addr);
		delete world;
	*/ // @on

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

			return (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0;
		}
	}

	private final Contact contact = new Contact(this, 0);
	private final Manifold manifold = new Manifold(0);
	private final ContactImpulse impulse = new ContactImpulse(this, 0);

	private void beginContact (long contactAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			contactListener.beginContact(contact);
		}
	}

	private void endContact (long contactAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			contactListener.endContact(contact);
		}
	}

	private void preSolve (long contactAddr, long manifoldAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			manifold.addr = manifoldAddr;
			contactListener.preSolve(contact, manifold);
		}
	}

	private void postSolve (long contactAddr, long impulseAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			impulse.addr = impulseAddr;
			contactListener.postSolve(contact, impulse);
		}
	}

	private boolean reportFixture (long addr) {
		if (queryCallback != null)
			return queryCallback.reportFixture(fixtures.get(addr));
		else
			return false;
	}

	/** Ray-cast the world for all fixtures in the path of the ray. The ray-cast ignores shapes that contain the starting point.
	 * @param callback a user implemented callback class.
	 * @param point1 the ray starting point
	 * @param point2 the ray ending point */
	public void rayCast (RayCastCallback callback, Vector2 point1, Vector2 point2) {
		rayCast(callback, point1.x, point1.y, point2.x, point2.y);
	}

	/** Ray-cast the world for all fixtures in the path of the ray. The ray-cast ignores shapes that contain the starting point.
	 * @param callback a user implemented callback class.
	 * @param point1X the ray starting point X
	 * @param point1Y the ray starting point Y
	 * @param point2X the ray ending point X
	 * @param point2Y the ray ending point Y */
	public void rayCast (RayCastCallback callback, float point1X, float point1Y, float point2X, float point2Y) {
		rayCastCallback = callback;
		jniRayCast(addr, point1X, point1Y, point2X, point2Y);
	}

	private RayCastCallback rayCastCallback = null;

	private native void jniRayCast (long addr, float aX, float aY, float bX, float bY); /*
		// @off
		b2World *world = (b2World*)addr;
		CustomRayCastCallback callback( env, object );	
		world->RayCast( &callback, b2Vec2(aX,aY), b2Vec2(bX,bY) );
	*/ // @on

	private final Vector2 rayPoint = new Vector2();
	private final Vector2 rayNormal = new Vector2();

	private float reportRayFixture (long addr, float pX, float pY, float nX, float nY, float fraction) {
		if (rayCastCallback != null) {
			rayPoint.x = pX;
			rayPoint.y = pY;
			rayNormal.x = nX;
			rayNormal.y = nY;
			return rayCastCallback.reportRayFixture(fixtures.get(addr), rayPoint, rayNormal, fraction);
		} else {
			return 0.0f;
		}
	}
}

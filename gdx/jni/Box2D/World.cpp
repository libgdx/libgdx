/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
#include "Box2D.h"
#include "World.h"
#ifdef ANDROID
#include <android/log.h>
#endif

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

	virtual float32 ReportFixture( b2Fixture* fixture, const b2Vec2& point, const b2Vec2& normal, float32 fraction)
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

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    newWorld
 * Signature: (FFZ)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_newWorld
(JNIEnv * env, jobject obj, jfloat gravityX, jfloat gravityY, jboolean doSleep)
{
	worldClass = env->GetObjectClass(obj);
	beginContactID = env->GetMethodID(worldClass, "beginContact", "(J)V" );
	endContactID = env->GetMethodID( worldClass, "endContact", "(J)V" );
	preSolveID = env->GetMethodID( worldClass, "preSolve", "(JJ)V" );
	postSolveID = env->GetMethodID( worldClass, "postSolve", "(JJ)V" );
	reportFixtureID = env->GetMethodID(worldClass, "reportFixture", "(J)Z" );
	reportRayFixtureID = env->GetMethodID(worldClass, "reportRayFixture", "(JFFFFF)F" );
	shouldCollideID = env->GetMethodID( worldClass, "contactFilter", "(JJ)Z");

	b2World* world = new b2World( b2Vec2( gravityX, gravityY ), doSleep );
	return (jlong)world;
}

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

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateBody
 * Signature: (JIFFFFFFFFZZZZZF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateBody
(JNIEnv *, jobject, jlong addr, jint type, jfloat positionX, jfloat positionY, jfloat angle, jfloat linearVelocityX, jfloat linearVelocityY, jfloat angularVelocity, jfloat linearDamping, jfloat angularDamping, jboolean allowSleep, jboolean awake, jboolean fixedRotation, jboolean bullet, jboolean active, jfloat inertiaScale)
{
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
	bodyDef.active = active;
	bodyDef.inertiaScale = inertiaScale;

	b2World* world = (b2World*)addr;
	b2Body* body = world->CreateBody( &bodyDef );
//#ifdef ANDROID
//	__android_log_write(ANDROID_LOG_ERROR,"Tag","HIIII");
//#endif
	return (jlong)body;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniDestroyBody
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDestroyBody
(JNIEnv *, jobject, jlong addr, jlong bodyAddr)
{
	b2World* world = (b2World*)addr;
	b2Body* body = (b2Body*)bodyAddr;
	world->DestroyBody(body);
}

inline b2JointType getJointType( int type )
{
	switch( type )
	{
	case 0: return e_revoluteJoint;
	case 1: return e_prismaticJoint;
	case 2: return e_distanceJoint;
	case 3: return e_pulleyJoint;
	case 4: return e_mouseJoint;
	case 5: return e_gearJoint;
	case 6: return e_lineJoint;
	case 7: return e_weldJoint;
	case 8: return e_frictionJoint;
	default:
		return e_unknownJoint;
	}
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateJoint
 * Signature: (JIJJZ)J
 */
/*JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateJoint
(JNIEnv *, jobject, jlong addr, jint type, jlong bodyAAddr, jlong bodyBAddr, jboolean collideConnected )
{
	b2World* world = (b2World*)addr;
	b2Body* bodyA = (b2Body*)bodyAAddr;
	b2Body* bodyB = (b2Body*)bodyBAddr;

	b2JointDef jointDef;
	jointDef.bodyA = bodyA;
	jointDef.bodyB = bodyB;
	jointDef.collideConnected = collideConnected;
	jointDef.type = getJointType( type );

	b2Joint* joint = world->CreateJoint( &jointDef );
	return (jlong)joint;
}*/

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateDistanceJoint
 * Signature: (JJJZFFFFFFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateDistanceJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat length, jfloat frequencyHz, jfloat dampingRatio)
{
	b2World* world = (b2World*)addr;
	b2DistanceJointDef def;
	def.bodyA = (b2Body*)bodyA;
	def.bodyB = (b2Body*)bodyB;
	def.collideConnected = collideConnected;
	def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
	def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
	def.length = length;
	def.frequencyHz = frequencyHz;
	def.dampingRatio = dampingRatio;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateFrictionJoint
 * Signature: (JJJZFFFFFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateFrictionJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat maxForce, jfloat maxTorque)
{
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
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateGearJoint
 * Signature: (JJJZJJF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateGearJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jlong joint1, jlong joint2, jfloat ratio)
{
	b2World* world = (b2World*)addr;
	b2GearJointDef def;
	def.bodyA = (b2Body*)bodyA;
	def.bodyB = (b2Body*)bodyB;
	def.collideConnected = collideConnected;
	def.joint1 = (b2Joint*)joint1;
	def.joint2 = (b2Joint*)joint2;
	def.ratio = ratio;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateLineJoint
 * Signature: (JJJZFFFFFFZFFZFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateLineJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat localAxisAX, jfloat localAxisAY, jboolean enableLimit, jfloat lowerTranslation, jfloat upperTranslation,
					  jboolean enableMotor, jfloat maxMotorForce, jfloat motorSpeed)
{
	b2World* world = (b2World*)addr;
	b2LineJointDef def;
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
	def.maxMotorForce = maxMotorForce;
	def.motorSpeed = motorSpeed;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateMouseJoint
 * Signature: (JJJZFFFFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateMouseJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat targetX, jfloat targetY, jfloat maxForce, jfloat frequencyHz, jfloat dampingRatio)
{
	b2World* world = (b2World*)addr;
	b2MouseJointDef def;
	def.bodyA = (b2Body*)bodyA;
	def.bodyB = (b2Body*)bodyB;
	def.collideConnected = collideConnected;
	def.target = b2Vec2( targetX, targetY );
	def.maxForce = maxForce;
	def.frequencyHz = frequencyHz;
	def.dampingRatio = dampingRatio;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreatePrismaticJoint
 * Signature: (JJJZFFFFFFFZFFZFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreatePrismaticJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat localAxisAX, jfloat localAxisAY, jfloat referenceAngle, jboolean enableLimit, jfloat lowerTranslation, jfloat upperTranslation,
					  jboolean enableMotor, jfloat maxMotorForce, jfloat motorSpeed)
{
	b2World* world = (b2World*)addr;
	b2PrismaticJointDef def;
	def.bodyA = (b2Body*)bodyA;
	def.bodyB = (b2Body*)bodyB;
	def.collideConnected = collideConnected;
	def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
	def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
	def.localAxis1 = b2Vec2( localAxisAX, localAxisAY );
	def.referenceAngle = referenceAngle;
	def.enableLimit = enableLimit;
	def.lowerTranslation = lowerTranslation;
	def.upperTranslation = upperTranslation;
	def.enableMotor = enableMotor;
	def.maxMotorForce = maxMotorForce;
	def.motorSpeed = motorSpeed;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreatePulleyJoint
 * Signature: (JJJZFFFFFFFFFFFFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreatePulleyJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat groundAnchorAX, jfloat groundAnchorAY, jfloat groundAnchorBX, jfloat groundAnchorBY,
					  jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat lengthA, jfloat maxLengthA,
					  jfloat lengthB, jfloat maxLengthB, jfloat ratio)
{
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
	def.maxLengthA = maxLengthA;
	def.lengthB = lengthB;
	def.maxLengthB = maxLengthB;
	def.ratio = ratio;

	return (jlong)world->CreateJoint(&def);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateRevoluteJoint
 * Signature: (JJJZFFFFFZFFZFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateRevoluteJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat referenceAngle, jboolean enableLimit, jfloat lowerAngle, jfloat upperAngle, jboolean enableMotor, jfloat motorSpeed, jfloat maxMotorTorque)
{
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
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniCreateWeldJoint
 * Signature: (JJJZFFFFF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateWeldJoint
  (JNIEnv *, jobject, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY,
					  jfloat referenceAngle)
{
	b2World* world = (b2World*)addr;
	b2WeldJointDef def;
	def.bodyA = (b2Body*)bodyA;
	def.bodyB = (b2Body*)bodyB;
	def.collideConnected = collideConnected;
	def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
	def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
	def.referenceAngle = referenceAngle;

	return (jlong)world->CreateJoint(&def);
}


/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniDestroyJoint
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDestroyJoint
(JNIEnv *, jobject, jlong addr, jlong jointAddr)
{
	b2World* world = (b2World*)addr;
	b2Joint* joint = (b2Joint*)jointAddr;

	world->DestroyJoint( joint );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniStep
 * Signature: (JFII)V
 */
b2ContactFilter defaultFilter;

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniStep
 (JNIEnv *env, jobject obj, jlong addr, jfloat timeStep, jint velocityIterations, jint positionIterations)
{
	b2World* world = (b2World*)addr;
	CustomContactFilter contactFilter(env, obj);
	CustomContactListener contactListener(env,obj);
	world->SetContactFilter(&contactFilter);
	world->SetContactListener(&contactListener);
	world->Step( timeStep, velocityIterations, positionIterations );
	world->SetContactFilter(&defaultFilter);
	world->SetContactListener(0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniQueryAABB
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniQueryAABB
  (JNIEnv *env, jobject obj, jlong addr, jfloat lowX, jfloat lowY, jfloat upX, jfloat upY)
{
	b2World* world = (b2World*)addr;
	b2AABB aabb;
	aabb.lowerBound = b2Vec2( lowX, lowY );
	aabb.upperBound = b2Vec2( upX, upY );

	CustomQueryCallback callback( env, obj );
	world->QueryAABB( &callback, aabb );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniClearForces
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniClearForces
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	world->ClearForces();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniSetWarmStarting
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetWarmStarting
(JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2World* world = (b2World*)addr;
	world->SetWarmStarting(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniSetContiousPhysics
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetContiousPhysics
(JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2World* world = (b2World*)addr;
	world->SetContinuousPhysics(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetProxyCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetProxyCount
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->GetProxyCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetBodyCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetBodyCount
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->GetBodyCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetJointcount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetJointcount
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->GetJointCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetContactCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetContactCount
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->GetContactCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniSetGravity
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetGravity
(JNIEnv *, jobject, jlong addr, jfloat gravityX, jfloat gravityY)
{
	b2World* world = (b2World*)addr;
	world->SetGravity( b2Vec2( gravityX, gravityY ) );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetGravity
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetGravity
(JNIEnv *env, jobject, jlong addr, jfloatArray gravity)
{
	b2World* world = (b2World*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(gravity, 0);
	b2Vec2 g = world->GetGravity();
	tmp[0] = g.x;
	tmp[1] = g.y;
	env->ReleasePrimitiveArrayCritical( gravity, tmp, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniIsLocked
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniIsLocked
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->IsLocked();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniSetAutoClearForces
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetAutoClearForces
(JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2World* world = (b2World*)addr;
	world->SetAutoClearForces(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetAutoClearForces
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetAutoClearForces
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)addr;
	return world->GetAutoClearForces();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniGetContactList
 * Signature: (J[J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetContactList
  (JNIEnv *env, jobject, jlong addr, jlongArray contacts)
{
	b2World* world = (b2World*)addr;
	jlong* tmp = (jlong*)env->GetPrimitiveArrayCritical( contacts, 0 );

	b2Contact* contact = world->GetContactList();
	int i = 0;
	while( contact != 0 )
	{
		tmp[i++] = (jlong)contact;
		contact = contact->GetNext();
	}

	env->ReleasePrimitiveArrayCritical( contacts, tmp, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniDispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDispose
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)(addr);
	delete world;
}

/*
 * Class:			com_badlogic_gdx_physics_box2d_World
 * Method:		jniRayCast
 * Signature:	(JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniRayCast
	(JNIEnv *env, jobject obj, jlong addr, jfloat aX, jfloat aY, jfloat bX, jfloat bY)
{
	b2World *world = (b2World*)addr;

	CustomRayCastCallback callback( env, obj );	
	world->RayCast( &callback, b2Vec2(aX,aY), b2Vec2(bX,bY) );
}
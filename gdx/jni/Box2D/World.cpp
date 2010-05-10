#include "Box2D.h"
#include "World.h"

static jclass worldClass = 0;
static jmethodID shouldCollideID = 0;
static jmethodID beginContactID = 0;
static jmethodID endContactID = 0;

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

		if( worldClass == 0 )
			worldClass = env->GetObjectClass(obj);
		if( shouldCollideID == 0 )
			shouldCollideID = env->GetMethodID( worldClass, "contactFilter", "(JJ)Z");
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

			if( worldClass == 0 )
				worldClass = env->GetObjectClass(obj);
			if( beginContactID == 0 )
				beginContactID = env->GetMethodID(worldClass, "beginContact", "(J)V" );
			if( endContactID == 0 )
				endContactID = env->GetMethodID( worldClass, "endContact", "(J)V" );
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
};

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    newWorld
 * Signature: (FFZ)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_newWorld
(JNIEnv *, jobject, jfloat gravityX, jfloat gravityY, jboolean doSleep)
{
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
	b2World* world = (b2World*)world;
	b2Body* body = (b2Body*)addr;
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
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateJoint
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
	b2Joint* joint = (b2Joint*)joint;

	world->DestroyJoint( joint );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_World
 * Method:    jniStep
 * Signature: (JFII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniStep
 (JNIEnv *env, jobject obj, jlong addr, jfloat timeStep, jint velocityIterations, jint positionIterations)
{
	b2World* world = (b2World*)addr;
	CustomContactFilter contactFilter(env, obj);
	CustomContactListener contactListener(env,obj);
	world->SetContactFilter(&contactFilter);
	world->SetContactListener(&contactListener);
	world->Step( timeStep, velocityIterations, positionIterations );
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
 * Method:    jniDispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDispose
(JNIEnv *, jobject, jlong addr)
{
	b2World* world = (b2World*)(addr);
	delete world;
}

#include <com.badlogic.gdx.physics.box2d.World.h>

//@line:56

#include <Box2D/Box2D.h>

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
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_newWorld(JNIEnv* env, jobject object, jfloat gravityX, jfloat gravityY, jboolean doSleep) {


//@line:237

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
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_setUseDefaultContactFilter(JNIEnv* env, jobject object, jboolean use) {


//@line:268

		// FIXME
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateBody(JNIEnv* env, jobject object, jlong addr, jint type, jfloat positionX, jfloat positionY, jfloat angle, jfloat linearVelocityX, jfloat linearVelocityY, jfloat angularVelocity, jfloat linearDamping, jfloat angularDamping, jboolean allowSleep, jboolean awake, jboolean fixedRotation, jboolean bullet, jboolean active, jfloat inertiaScale) {


//@line:294

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
		bodyDef.gravityScale = inertiaScale;
	
		b2World* world = (b2World*)addr;
		b2Body* body = world->CreateBody( &bodyDef );
		return (jlong)body;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDestroyBody(JNIEnv* env, jobject object, jlong addr, jlong bodyAddr) {


//@line:336

		b2World* world = (b2World*)addr;
		b2Body* body = (b2Body*)bodyAddr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->DestroyBody(body);
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDestroyFixture(JNIEnv* env, jobject object, jlong addr, jlong bodyAddr, jlong fixtureAddr) {


//@line:356

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
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDeactivateBody(JNIEnv* env, jobject object, jlong addr, jlong bodyAddr) {


//@line:376

		b2World* world = (b2World*)(addr);
		b2Body* body = (b2Body*)(bodyAddr);	
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env, object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		body->SetActive(false);
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateWheelJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat localAxisAX, jfloat localAxisAY, jboolean enableMotor, jfloat maxMotorTorque, jfloat motorSpeed, jfloat frequencyHz, jfloat dampingRatio) {


//@line:481

		b2World* world = (b2World*)addr;
		b2WheelJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.localAxisA = b2Vec2(localAxisAX, localAxisAY);
		def.enableMotor = enableMotor;
		def.maxMotorTorque = maxMotorTorque;
		def.motorSpeed = motorSpeed;
		def.frequencyHz = frequencyHz;
		def.dampingRatio = dampingRatio;
		
		return (jlong)world->CreateJoint(&def);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateRopeJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat maxLength) {


//@line:500

		b2World* world = (b2World*)addr;
		b2RopeJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.maxLength = maxLength;
	
		return (jlong)world->CreateJoint(&def);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateDistanceJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat length, jfloat frequencyHz, jfloat dampingRatio) {


//@line:514

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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateFrictionJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat maxForce, jfloat maxTorque) {


//@line:530

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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateGearJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jlong joint1, jlong joint2, jfloat ratio) {


//@line:544

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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateMotorJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat linearOffsetX, jfloat linearOffsetY, jfloat angularOffset, jfloat maxForce, jfloat maxTorque, jfloat correctionFactor) {


//@line:557

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
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateMouseJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat targetX, jfloat targetY, jfloat maxForce, jfloat frequencyHz, jfloat dampingRatio) {


//@line:572

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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreatePrismaticJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat localAxisAX, jfloat localAxisAY, jfloat referenceAngle, jboolean enableLimit, jfloat lowerTranslation, jfloat upperTranslation, jboolean enableMotor, jfloat maxMotorForce, jfloat motorSpeed) {


//@line:588

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
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreatePulleyJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat groundAnchorAX, jfloat groundAnchorAY, jfloat groundAnchorBX, jfloat groundAnchorBY, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat lengthA, jfloat lengthB, jfloat ratio) {


//@line:609

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
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateRevoluteJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat referenceAngle, jboolean enableLimit, jfloat lowerAngle, jfloat upperAngle, jboolean enableMotor, jfloat motorSpeed, jfloat maxMotorTorque) {


//@line:628

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

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniCreateWeldJoint(JNIEnv* env, jobject object, jlong addr, jlong bodyA, jlong bodyB, jboolean collideConnected, jfloat localAnchorAX, jfloat localAnchorAY, jfloat localAnchorBX, jfloat localAnchorBY, jfloat referenceAngle, jfloat frequencyHz, jfloat dampingRatio) {


//@line:647

		b2World* world = (b2World*)addr;
		b2WeldJointDef def;
		def.bodyA = (b2Body*)bodyA;
		def.bodyB = (b2Body*)bodyB;
		def.collideConnected = collideConnected;
		def.localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
		def.localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
		def.referenceAngle = referenceAngle;
		def.frequencyHz = frequencyHz;
		def.dampingRatio = dampingRatio;
	
		return (jlong)world->CreateJoint(&def);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDestroyJoint(JNIEnv* env, jobject object, jlong addr, jlong jointAddr) {


//@line:672

		b2World* world = (b2World*)addr;
		b2Joint* joint = (b2Joint*)jointAddr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->DestroyJoint( joint );
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniStep(JNIEnv* env, jobject object, jlong addr, jfloat timeStep, jint velocityIterations, jint positionIterations) {


//@line:692

		b2World* world = (b2World*)addr;
		CustomContactFilter contactFilter(env, object);
		CustomContactListener contactListener(env,object);
		world->SetContactFilter(&contactFilter);
		world->SetContactListener(&contactListener);
		world->Step( timeStep, velocityIterations, positionIterations );
		world->SetContactFilter(&defaultFilter);
		world->SetContactListener(0);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniClearForces(JNIEnv* env, jobject object, jlong addr) {


//@line:712

		b2World* world = (b2World*)addr;
		world->ClearForces();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetWarmStarting(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:722

		b2World* world = (b2World*)addr;
		world->SetWarmStarting(flag);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetContiousPhysics(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:732

		b2World* world = (b2World*)addr;
		world->SetContinuousPhysics(flag);
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetProxyCount(JNIEnv* env, jobject object, jlong addr) {


//@line:742

		b2World* world = (b2World*)addr;
		return world->GetProxyCount();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetBodyCount(JNIEnv* env, jobject object, jlong addr) {


//@line:752

		b2World* world = (b2World*)addr;
		return world->GetBodyCount();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetJointcount(JNIEnv* env, jobject object, jlong addr) {


//@line:767

		b2World* world = (b2World*)addr;
		return world->GetJointCount();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetContactCount(JNIEnv* env, jobject object, jlong addr) {


//@line:777

		b2World* world = (b2World*)addr;
		return world->GetContactCount();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetGravity(JNIEnv* env, jobject object, jlong addr, jfloat gravityX, jfloat gravityY) {


//@line:787

		b2World* world = (b2World*)addr;
		world->SetGravity( b2Vec2( gravityX, gravityY ) );
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetGravity(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_gravity) {
	float* gravity = (float*)env->GetPrimitiveArrayCritical(obj_gravity, 0);


//@line:803

		b2World* world = (b2World*)addr;
		b2Vec2 g = world->GetGravity();
		gravity[0] = g.x;
		gravity[1] = g.y;
	
	env->ReleasePrimitiveArrayCritical(obj_gravity, gravity, 0);

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniIsLocked(JNIEnv* env, jobject object, jlong addr) {


//@line:815

		b2World* world = (b2World*)addr;
		return world->IsLocked();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniSetAutoClearForces(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:825

		b2World* world = (b2World*)addr;
		world->SetAutoClearForces(flag);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetAutoClearForces(JNIEnv* env, jobject object, jlong addr) {


//@line:835

		b2World* world = (b2World*)addr;
		return world->GetAutoClearForces();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniQueryAABB(JNIEnv* env, jobject object, jlong addr, jfloat lowX, jfloat lowY, jfloat upX, jfloat upY) {


//@line:853

		b2World* world = (b2World*)addr;
		b2AABB aabb;
		aabb.lowerBound = b2Vec2( lowX, lowY );
		aabb.upperBound = b2Vec2( upX, upY );
	
		CustomQueryCallback callback( env, object );
		world->QueryAABB( &callback, aabb );
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniGetContactList(JNIEnv* env, jobject object, jlong addr, jlongArray obj_contacts) {
	long long* contacts = (long long*)env->GetPrimitiveArrayCritical(obj_contacts, 0);


//@line:938

		b2World* world = (b2World*)addr;
	
		b2Contact* contact = world->GetContactList();
		int i = 0;
		while( contact != 0 )
		{
			contacts[i++] = (long long)contact;
			contact = contact->GetNext();
		}
	
	env->ReleasePrimitiveArrayCritical(obj_contacts, contacts, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniDispose(JNIEnv* env, jobject object, jlong addr) {


//@line:954

		b2World* world = (b2World*)(addr);
		delete world;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_setVelocityThreshold(JNIEnv* env, jclass clazz, jfloat threshold) {


//@line:1014

		b2_velocityThreshold = threshold;
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_World_getVelocityThreshold(JNIEnv* env, jclass clazz) {


//@line:1019

		return b2_velocityThreshold;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_World_jniRayCast(JNIEnv* env, jobject object, jlong addr, jfloat aX, jfloat aY, jfloat bX, jfloat bY) {


//@line:1044

		b2World *world = (b2World*)addr;
		CustomRayCastCallback callback( env, object );	
		world->RayCast( &callback, b2Vec2(aX,aY), b2Vec2(bX,bY) );
	

}


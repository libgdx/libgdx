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
#include "Body.h"
#include <stdio.h>

#ifdef ANDROID
#include <android/log.h>
#endif

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniCreateFixture
 * Signature: (JJFFFZSSS)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniCreateFixture__JJFFFZSSS
(JNIEnv *, jobject, jlong addr, jlong shapeAddr, jfloat friction, jfloat restitution, jfloat density, jboolean isSensor, jshort categoryBits, jshort maskBits, jshort groupIndex)
{
	b2Body* body = (b2Body*)addr;
	b2Shape* shape = (b2Shape*)shapeAddr;
	b2FixtureDef fixtureDef;

#ifdef ANDROID
	//__android_log_print( ANDROID_LOG_INFO, "Box2DTest", "body: %d, shape: %d", body, shape );
#endif

	fixtureDef.shape = shape;
	fixtureDef.friction = friction;
	fixtureDef.restitution = restitution;
	fixtureDef.density = density;
	fixtureDef.isSensor = isSensor;
	fixtureDef.filter.maskBits = maskBits;
	fixtureDef.filter.categoryBits = categoryBits;
	fixtureDef.filter.groupIndex = groupIndex;

	return (jlong)body->CreateFixture( &fixtureDef );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniCreateFixture
 * Signature: (JJF)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniCreateFixture__JJF
(JNIEnv *, jobject, jlong addr, jlong shapeAddr, jfloat density)
{
	b2Body* body = (b2Body*)addr;
	b2Shape* shape = (b2Shape*)shapeAddr;
	return (jlong)body->CreateFixture( shape, density );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniDestroyFixture
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniDestroyFixture
  (JNIEnv *, jobject, jlong addr, jlong fixtureAddr)
{
	b2Body* body = (b2Body*)addr;
	b2Fixture* fixture = (b2Fixture*)fixtureAddr;
	body->DestroyFixture(fixture);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetTransform
 * Signature: (FFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetTransform
  (JNIEnv *, jobject, jlong addr, jfloat positionX, jfloat positionY, jfloat angle)
{
	b2Body* body = (b2Body*)addr;
	body->SetTransform(b2Vec2(positionX, positionY), angle);
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetTransform
  (JNIEnv *env, jobject, jlong addr, jfloatArray vals)
{
	b2Body* body = (b2Body*)addr;
	float* valOut = (float*)env->GetPrimitiveArrayCritical(vals, 0);
	b2Transform t = body->GetTransform();

	valOut[0] = t.position.x;
	valOut[1] = t.position.y;
	valOut[2] = t.R.col1.x;
	valOut[3] = t.R.col1.y;
	valOut[4] = t.R.col2.x;
	valOut[5] = t.R.col2.y;
	env->ReleasePrimitiveArrayCritical(vals, valOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetPosition
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetPosition
  (JNIEnv *env, jobject, jlong addr, jfloatArray position)
{
	b2Body* body = (b2Body*)addr;
	float* positionOut = (float*)env->GetPrimitiveArrayCritical(position, 0);
	b2Vec2 p = body->GetPosition();
	positionOut[0] = p.x;
	positionOut[1] = p.y;
	env->ReleasePrimitiveArrayCritical(position, positionOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetAngle
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngle
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetAngle();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetWorldCenter
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldCenter
  (JNIEnv *env, jobject, jlong addr, jfloatArray worldCenter)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(worldCenter, 0);
	b2Vec2 w = body->GetWorldCenter();
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(worldCenter, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLocalCenter
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalCenter
  (JNIEnv *env, jobject, jlong addr, jfloatArray localCenter)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(localCenter, 0);
	b2Vec2 w = body->GetLocalCenter();
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(localCenter, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetLinearVelocity
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetLinearVelocity
  (JNIEnv *, jobject, jlong addr, jfloat linearVelocityX, jfloat linearVelocityY)
{
	b2Body* body = (b2Body*)addr;
	body->SetLinearVelocity(b2Vec2(linearVelocityX, linearVelocityY) );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLinearVelocity
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocity
  (JNIEnv *env, jobject, jlong addr, jfloatArray linVel)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(linVel,0);
	b2Vec2 l = body->GetLinearVelocity();
	out[0] = l.x;
	out[1] = l.y;
	env->ReleasePrimitiveArrayCritical(linVel, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetAngularVelocity
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAngularVelocity
  (JNIEnv *, jobject, jlong addr, jfloat angularVelocity)
{
	b2Body* body = (b2Body*)addr;
	body->SetAngularVelocity(angularVelocity);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetAngularVelocity
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngularVelocity
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetAngularVelocity();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniApplyForce
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyForce
  (JNIEnv *, jobject, jlong addr, jfloat forceX, jfloat forceY, jfloat pointX, jfloat pointY )
{
	b2Body* body = (b2Body*)addr;
	body->ApplyForce( b2Vec2( forceX, forceY ), b2Vec2( pointX, pointY ) );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniApplyTorque
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyTorque
  (JNIEnv *, jobject, jlong addr, jfloat torque)
{
	b2Body* body = (b2Body*)addr;
	body->ApplyTorque(torque);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniApplyLinearImpulse
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyLinearImpulse
  (JNIEnv *, jobject, jlong addr, jfloat impulseX, jfloat impulseY, jfloat pointX, jfloat pointY)
{
	b2Body* body = (b2Body*)addr;
	body->ApplyLinearImpulse( b2Vec2( impulseX, impulseY ), b2Vec2( pointX, pointY ) );

}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniApplyAngularImpulse
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniApplyAngularImpulse
  (JNIEnv *, jobject, jlong addr, jfloat angularImpulse)
{
	b2Body* body = (b2Body*)addr;
	body->ApplyAngularImpulse(angularImpulse);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetMass
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetMass
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetMass();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetInertia
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetInertia
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetInertia();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetMassData
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetMassData
  (JNIEnv *env, jobject, jlong addr, jfloatArray massData)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(massData, 0);
	b2MassData m;
	body->GetMassData(&m);
	out[0] = m.mass;
	out[1] = m.center.x;
	out[2] = m.center.y;
	out[3] = m.I;
	env->ReleasePrimitiveArrayCritical(massData, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetMassData
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetMassData
  (JNIEnv *, jobject, jlong addr, jfloat mass, jfloat centerX, jfloat centerY, jfloat I)
{
	b2Body* body = (b2Body*)addr;
	b2MassData m;
	m.mass = mass;
	m.center.x = centerX;
	m.center.y = centerY;
	m.I = I;
	body->SetMassData(&m);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniResetMassData
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniResetMassData
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	body->ResetMassData();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetWorldPoint
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldPoint
  (JNIEnv *env, jobject, jlong addr, jfloat localPointX, jfloat localPointY, jfloatArray worldPoint)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(worldPoint, 0);
	b2Vec2 w = body->GetWorldPoint( b2Vec2( localPointX, localPointY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(worldPoint, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetWorldVector
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetWorldVector
(JNIEnv *env, jobject, jlong addr, jfloat localVectorX, jfloat localVectorY, jfloatArray worldVector)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(worldVector, 0);
	b2Vec2 w = body->GetWorldVector( b2Vec2( localVectorX, localVectorY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(worldVector, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLocalPoint
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalPoint
  (JNIEnv *env, jobject, jlong addr, jfloat worldPointX, jfloat worldPointY, jfloatArray localPoint)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(localPoint, 0);
	b2Vec2 w = body->GetLocalPoint( b2Vec2( worldPointX, worldPointY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(localPoint, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLocalVector
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLocalVector
  (JNIEnv *env, jobject, jlong addr, jfloat worldVectorX, jfloat worldVectorY, jfloatArray localVector)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(localVector, 0);
	b2Vec2 w = body->GetLocalVector( b2Vec2( worldVectorX, worldVectorY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(localVector, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLinearVelocityFromWorldPoint
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocityFromWorldPoint
  (JNIEnv *env, jobject, jlong addr, jfloat worldVectorX, jfloat worldVectorY, jfloatArray linVel)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(linVel, 0);
	b2Vec2 w = body->GetLinearVelocityFromWorldPoint( b2Vec2( worldVectorX, worldVectorY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(linVel, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLinearVelocityFromLocalPoint
 * Signature: (JFF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearVelocityFromLocalPoint
  (JNIEnv *env, jobject, jlong addr, jfloat localPointX, jfloat localPointY, jfloatArray linVel)
{
	b2Body* body = (b2Body*)addr;
	float* out = (float*)env->GetPrimitiveArrayCritical(linVel, 0);
	b2Vec2 w = body->GetLinearVelocityFromLocalPoint( b2Vec2( localPointX, localPointY ) );
	out[0] = w.x;
	out[1] = w.y;
	env->ReleasePrimitiveArrayCritical(linVel, out, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetLinearDamping
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetLinearDamping
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetLinearDamping();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetLinearDamping
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetLinearDamping
  (JNIEnv *, jobject, jlong addr, jfloat linearDamping)
{
	b2Body* body = (b2Body*)addr;
	body->SetLinearDamping(linearDamping);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetAngularDamping
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetAngularDamping
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetAngularDamping();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetAngularDamping
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAngularDamping
  (JNIEnv *, jobject, jlong addr, jfloat angularDamping)
{
	b2Body* body = (b2Body*)addr;
	body->SetAngularDamping(angularDamping);
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
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetType
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetType
  (JNIEnv *, jobject, jlong addr, jint type)
{
	b2Body* body = (b2Body*)addr;
	body->SetType(getBodyType(type));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniGetType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniGetType
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->GetType();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetBullet
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetBullet
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Body* body = (b2Body*)addr;
	body->SetBullet(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniIsBullet
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsBullet
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->IsBullet();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetSleepingAllowed
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetSleepingAllowed
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Body* body = (b2Body*)addr;
	body->SetSleepingAllowed(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniIsSleepingAllowed
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsSleepingAllowed
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->IsSleepingAllowed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetAwake
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetAwake
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Body* body = (b2Body*)addr;
	body->SetAwake(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniIsAwake
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsAwake
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->IsAwake();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetActive
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetActive
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Body* body = (b2Body*)addr;
	body->SetActive(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniIsActive
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsActive
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->IsActive();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniSetFixedRotation
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniSetFixedRotation
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Body* body = (b2Body*)addr;
	body->SetFixedRotation(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Body
 * Method:    jniIsFixedRotation
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Body_jniIsFixedRotation
  (JNIEnv *, jobject, jlong addr)
{
	b2Body* body = (b2Body*)addr;
	return body->IsFixedRotation();
}

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
#include "Joint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetType
  (JNIEnv *, jobject, jlong addr)
{
	b2Joint* joint = (b2Joint*)addr;
	return joint->GetType();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetBodyA
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetBodyA
  (JNIEnv *, jobject, jlong addr)
{
	b2Joint* joint = (b2Joint*)addr;
	return (jlong)joint->GetBodyA();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetBodyB
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetBodyB
  (JNIEnv *, jobject, jlong addr)
{
	b2Joint* joint = (b2Joint*)addr;
	return (jlong)joint->GetBodyB();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetAnchorA
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetAnchorA
  (JNIEnv *env, jobject, jlong addr, jfloatArray anchorA)
{
	b2Joint* joint = (b2Joint*)addr;
	float* anchorAOut = (float*)env->GetPrimitiveArrayCritical(anchorA, 0);
	b2Vec2 a = joint->GetAnchorA();
	anchorAOut[0] = a.x;
	anchorAOut[1] = a.y;
	env->ReleasePrimitiveArrayCritical(anchorA, anchorAOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetAnchorB
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetAnchorB
  (JNIEnv *env, jobject, jlong addr, jfloatArray anchorB)
{
	b2Joint* joint = (b2Joint*)addr;
	float* anchorBOut = (float*)env->GetPrimitiveArrayCritical(anchorB, 0);
	b2Vec2 a = joint->GetAnchorB();
	anchorBOut[0] = a.x;
	anchorBOut[1] = a.y;
	env->ReleasePrimitiveArrayCritical(anchorB, anchorBOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetReactionForce
 * Signature: (JF[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetReactionForce
  (JNIEnv *env, jobject, jlong addr, jfloat inv_dt, jfloatArray reactionForce)
{
	b2Joint* joint = (b2Joint*)addr;
	float* reactionForceOut = (float*)env->GetPrimitiveArrayCritical(reactionForce, 0);

	b2Vec2 f = joint->GetReactionForce(inv_dt);
	reactionForceOut[0] = f.x;
	reactionForceOut[1] = f.y;

	env->ReleasePrimitiveArrayCritical(reactionForce, reactionForceOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniGetReactionTorque
 * Signature: (JF)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniGetReactionTorque
  (JNIEnv *, jobject, jlong addr, jfloat inv_dt)
{
	b2Joint* joint = (b2Joint*)addr;
	return joint->GetReactionTorque(inv_dt);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Joint
 * Method:    jniIsActive
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Joint_jniIsActive
  (JNIEnv *, jobject, jlong addr)
{
	b2Joint* joint = (b2Joint*)addr;
	return joint->IsActive();
}

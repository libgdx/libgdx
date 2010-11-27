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
#include "MouseJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniSetTarget
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniSetTarget
  (JNIEnv *, jobject, jlong addr, jfloat x, jfloat y)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	joint->SetTarget( b2Vec2(x, y ) );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniGetTarget
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniGetTarget
  (JNIEnv *env, jobject, jlong addr, jfloatArray target)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(target, 0);
	tmp[0] = joint->GetTarget().x;
	tmp[1] = joint->GetTarget().y;
	env->ReleasePrimitiveArrayCritical( target, tmp, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniSetMaxForce
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniSetMaxForce
  (JNIEnv *, jobject, jlong addr, jfloat force )
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	joint->SetMaxForce( force );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniGetMaxForce
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniGetMaxForce
  (JNIEnv *, jobject, jlong addr)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	return joint->GetMaxForce();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniSetFrequency
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniSetFrequency
  (JNIEnv *, jobject, jlong addr, jfloat hz)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	joint->SetFrequency(hz);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniGetFrequency
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniGetFrequency
  (JNIEnv *, jobject, jlong addr)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	return joint->GetFrequency();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniSetDampingRatio
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniSetDampingRatio
  (JNIEnv *, jobject, jlong addr, jfloat ratio)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	joint->SetDampingRatio( ratio );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_MouseJoint
 * Method:    jniGetDampingRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MouseJoint_jniGetDampingRatio
  (JNIEnv *, jobject, jlong addr)
{
	b2MouseJoint* joint = (b2MouseJoint*)addr;
	return joint->GetDampingRatio();
}

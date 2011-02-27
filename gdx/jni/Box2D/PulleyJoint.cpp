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
#include "PulleyJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PulleyJoint
 * Method:    jniGetGroundAnchorA
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetGroundAnchorA
  (JNIEnv *env, jobject, jlong addr, jfloatArray anchor)
{
	b2PulleyJoint* joint = (b2PulleyJoint*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(anchor, 0);
	tmp[0] = joint->GetGroundAnchorA().x;
	tmp[1] = joint->GetGroundAnchorA().y;
	env->ReleasePrimitiveArrayCritical( anchor, tmp, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PulleyJoint
 * Method:    jniGetGroundAnchorB
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetGroundAnchorB
  (JNIEnv *env, jobject, jlong addr, jfloatArray anchor)
{
	b2PulleyJoint* joint = (b2PulleyJoint*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(anchor, 0);
	tmp[0] = joint->GetGroundAnchorB().x;
	tmp[1] = joint->GetGroundAnchorB().y;
	env->ReleasePrimitiveArrayCritical( anchor, tmp, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PulleyJoint
 * Method:    jniGetLength1
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetLength1
  (JNIEnv *, jobject, jlong addr)
{
	b2PulleyJoint* joint = (b2PulleyJoint*)addr;
	return joint->GetLength1();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PulleyJoint
 * Method:    jniGetLength2
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetLength2
  (JNIEnv *, jobject, jlong addr)
{
	b2PulleyJoint* joint = (b2PulleyJoint*)addr;
	return joint->GetLength2();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PulleyJoint
 * Method:    jniGetRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetRatio
  (JNIEnv *, jobject, jlong addr)
{
	b2PulleyJoint* joint = (b2PulleyJoint*)addr;
	return joint->GetRatio();
}

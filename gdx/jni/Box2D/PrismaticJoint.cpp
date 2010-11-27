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
#include "PrismaticJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetJointTranslation
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetJointTranslation
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetJointTranslation();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetJointSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetJointSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetJointSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniIsLimitEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniIsLimitEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->IsLimitEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniEnableLimit
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniEnableLimit
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	joint->EnableLimit(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetLowerLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetLowerLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetLowerLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetUpperLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetUpperLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetUpperLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniSetLimits
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetLimits
  (JNIEnv *, jobject, jlong addr, jfloat lower, jfloat upper )
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	joint->SetLimits(lower, upper );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniIsMotorEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniIsMotorEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->IsMotorEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniEnableMotor
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniEnableMotor
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	joint->EnableMotor(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniSetMotorSpeed
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetMotorSpeed
  (JNIEnv *, jobject, jlong addr, jfloat speed)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	joint->SetMotorSpeed(speed);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetMotorSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetMotorSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetMotorSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniSetMaxMotorForce
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetMaxMotorForce
  (JNIEnv *, jobject, jlong addr, jfloat force)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	joint->SetMaxMotorForce(force);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_PrismaticJoint
 * Method:    jniGetMotorForce
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetMotorForce
  (JNIEnv *, jobject, jlong addr)
{
	b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
	return joint->GetMotorForce();
}

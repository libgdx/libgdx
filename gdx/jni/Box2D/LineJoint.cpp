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
#include "LineJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetJointTranslation
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetJointTranslation
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetJointTranslation();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetJointSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetJointSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetJointSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniIsLimitEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniIsLimitEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->IsLimitEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniEnableLimit
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniEnableLimit
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	joint->EnableLimit( flag );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetLowerLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetLowerLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetLowerLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetUpperLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetUpperLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetUpperLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniSetLimits
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniSetLimits
  (JNIEnv *, jobject, jlong addr, jfloat lowerLimit, jfloat upperLimit)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	joint->SetLimits( lowerLimit, upperLimit );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniIsMotorEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniIsMotorEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->IsMotorEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniEnableMotor
 * Signature: (JZ)Z
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniEnableMotor
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	joint->EnableMotor(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniSetMotorEnabled
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniSetMotorSpeed
  (JNIEnv *, jobject, jlong addr, jfloat speed)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	joint->SetMotorSpeed( speed );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetMotorSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetMotorSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetMotorSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniSetMaxMotorForce
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniSetMaxMotorForce
  (JNIEnv *, jobject, jlong addr, jfloat force)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	joint->SetMaxMotorForce( force );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetMaxMotorForce
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetMaxMotorForce
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return 0; // FIXME this is not a bug, the method just isn't defined in b2LineJoint. strange shit
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_LineJoint
 * Method:    jniGetMotorForce
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_LineJoint_jniGetMotorForce
  (JNIEnv *, jobject, jlong addr)
{
	b2LineJoint* joint = (b2LineJoint*)addr;
	return joint->GetMotorForce();
}

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
#include "RevoluteJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetJointAngle
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointAngle
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetJointAngle();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetJointSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetJointSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniIsLimitEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsLimitEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->IsLimitEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniEnableLimit
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableLimit
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	joint->EnableLimit(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetLowerLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLowerLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetLowerLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetUpperLimit
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetUpperLimit
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetUpperLimit();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniSetLimits
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetLimits
  (JNIEnv *, jobject, jlong addr, jfloat lower, jfloat upper)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	joint->SetLimits(lower, upper );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniIsMotorEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsMotorEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->IsMotorEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniEnableMotor
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableMotor
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	joint->EnableMotor(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniSetMotorSpeed
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMotorSpeed
  (JNIEnv *, jobject, jlong addr, jfloat speed)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	joint->SetMotorSpeed(speed);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetMotorSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorSpeed
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetMotorSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniSetMaxMotorTorque
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMaxMotorTorque
  (JNIEnv *, jobject, jlong addr, jfloat torque)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	joint->SetMaxMotorTorque(torque);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_RevoluteJoint
 * Method:    jniGetMotorTorque
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorTorque
  (JNIEnv *, jobject, jlong addr)
{
	b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
	return joint->GetMotorTorque();
}

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
#include "WheelJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetJointTranslation
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetJointTranslation
  (JNIEnv *, jobject, jlong addr) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetJointTranslation();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetJointSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetJointSpeed
  (JNIEnv *, jobject, jlong addr) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetJointSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniIsMotorEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniIsMotorEnabled
  (JNIEnv *, jobject, jlong addr) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->IsMotorEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniEnableMotor
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniEnableMotor
  (JNIEnv *, jobject, jlong addr, jboolean flag) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	joint->EnableMotor(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniSetMotorSpeed
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniSetMotorSpeed
  (JNIEnv *, jobject, jlong addr, jfloat speed) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	joint->SetMotorSpeed(speed);
}
/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetMotorSpeed
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetMotorSpeed
  (JNIEnv *, jobject, jlong addr) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetMotorSpeed();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniSetMaxMotorTorque
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniSetMaxMotorTorque
  (JNIEnv *, jobject, jlong addr, jfloat torque) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	joint->SetMaxMotorTorque(torque);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetMaxMotorTorque
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetMaxMotorTorque
  (JNIEnv *, jobject, jlong addr) {
	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetMaxMotorTorque();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetMotorTorque
 * Signature: (JF)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetMotorTorque
  (JNIEnv *, jobject, jlong addr, jfloat invDt) {
  	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetMotorTorque(invDt);
}
  
/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniSetSpringFrequencyHz
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniSetSpringFrequencyHz
  (JNIEnv *, jobject, jlong addr, jfloat hz) {
	b2WheelJoint* joint = (b2WheelJoint*)addr;
	joint->SetSpringFrequencyHz(hz);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetSpringFrequencyHz
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetSpringFrequencyHz
  (JNIEnv *, jobject, jlong addr) {
	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetSpringFrequencyHz();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniSetSpringDampingRatio
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniSetSpringDampingRatio
  (JNIEnv *, jobject, jlong addr, jfloat ratio) {
	b2WheelJoint* joint = (b2WheelJoint*)addr;
	joint->SetSpringDampingRatio(ratio);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_WheelJoint
 * Method:    jniGetSpringDampingRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WheelJoint_jniGetSpringDampingRatio
  (JNIEnv *, jobject, jlong addr) {
	b2WheelJoint* joint = (b2WheelJoint*)addr;
	return joint->GetSpringDampingRatio();
}
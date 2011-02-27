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
#include "GearJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_GearJoint
 * Method:    jniSetRatio
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniSetRatio
  (JNIEnv *, jobject, jlong addr, jfloat ratio)
{
	b2GearJoint* joint =  (b2GearJoint*)addr;
	joint->SetRatio( ratio );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_GearJoint
 * Method:    jniGetRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniGetRatio
  (JNIEnv *, jobject, jlong addr)
{
	b2GearJoint* joint =  (b2GearJoint*)addr;
	return joint->GetRatio();
}

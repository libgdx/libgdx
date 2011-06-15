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
#include "ContactImpulse.h"

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ContactImpulse_jniGetNormalImpulses
  (JNIEnv *env, jobject, jlong addr, jfloatArray values) {
	b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	

	float* tmp = (float*)env->GetPrimitiveArrayCritical( values, 0 );
	tmp[0] = contactImpulse->normalImpulses[0];
	tmp[1] = contactImpulse->normalImpulses[1];
	
	env->ReleasePrimitiveArrayCritical( values, tmp, 0 );
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ContactImpulse_jniGetTangentImpulses
  (JNIEnv *env, jobject, jlong addr, jfloatArray values) {
  	b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	

	float* tmp = (float*)env->GetPrimitiveArrayCritical( values, 0 );
	tmp[0] = contactImpulse->tangentImpulses[0];
	tmp[1] = contactImpulse->tangentImpulses[1];
	
	env->ReleasePrimitiveArrayCritical( values, tmp, 0 );
}
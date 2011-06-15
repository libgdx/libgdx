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
#include "Manifold.h"

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Manifold_jniGetType
  (JNIEnv *, jobject, jlong addr) {
	b2Manifold* manifold = (b2Manifold*)addr;
	return manifold->type;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Manifold_jniGetPointCount
  (JNIEnv *, jobject, jlong addr) {
  	b2Manifold* manifold = (b2Manifold*)addr;
	return manifold->pointCount;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Manifold_jniGetLocalNormal
  (JNIEnv *env, jobject, jlong addr, jfloatArray values) {
	b2Manifold* manifold = (b2Manifold*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical( values, 0 );  
	tmp[0] = manifold->localNormal.x;
	tmp[1] = manifold->localNormal.y;
	env->ReleasePrimitiveArrayCritical( values, tmp, 0 );
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Manifold_jniGetLocalPoint
  (JNIEnv *env, jobject, jlong addr, jfloatArray values) {
	b2Manifold* manifold = (b2Manifold*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical( values, 0 );  
	tmp[0] = manifold->localPoint.x;
	tmp[1] = manifold->localPoint.y;
	env->ReleasePrimitiveArrayCritical( values, tmp, 0 );
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Manifold_jniGetPoint
  (JNIEnv *env, jobject, jlong addr, jfloatArray values, jint idx) {
  b2Manifold* manifold = (b2Manifold*)addr;
  
  float* tmp = (float*)env->GetPrimitiveArrayCritical( values, 0 );  
  tmp[0] = manifold->points[idx].localPoint.x;
  tmp[1] = manifold->points[idx].localPoint.y;
  tmp[2] = manifold->points[idx].normalImpulse;
  tmp[3] = manifold->points[idx].tangentImpulse;  
  env->ReleasePrimitiveArrayCritical( values, tmp, 0 );
  
  return (jint)manifold->points[idx].id.key;
}
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
#include "EdgeShape.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_EdgeShape
 * Method:    newEdgeShape
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_newEdgeShape
  (JNIEnv *, jobject) {
	return (jlong)(new b2EdgeShape());
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_EdgeShape
 * Method:    jniSet
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSet
  (JNIEnv *, jobject, jlong addr, jfloat v1x, jfloat v1y, jfloat v2x, jfloat v2y) {
  b2EdgeShape* edge = (b2EdgeShape*)addr;
  edge->Set(b2Vec2(v1x, v1y), b2Vec2(v2x, v2y));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_EdgeShape
 * Method:    jniGetVertex1
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex1
  (JNIEnv *env, jobject, jlong addr, jfloatArray verts)
{
	b2EdgeShape* edge = (b2EdgeShape*)addr; 
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verts, 0);
	vertices[0] = edge->m_vertex1.x;
	vertices[1] = edge->m_vertex1.y;
	env->ReleasePrimitiveArrayCritical(verts, vertices, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_EdgeShape
 * Method:    jniGetVertex2
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex2
  (JNIEnv *env, jobject, jlong addr, jfloatArray verts)
{
	b2EdgeShape* edge = (b2EdgeShape*)addr;
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verts, 0);
	vertices[0] = edge->m_vertex2.x;
	vertices[1] = edge->m_vertex2.y;
	env->ReleasePrimitiveArrayCritical(verts, vertices, 0 );
}
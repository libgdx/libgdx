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
#include "ChainShape.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    newChainShape
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_newChainShape
  (JNIEnv *, jobject) {
	return (jlong)(new b2ChainShape());
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniCreateLoop
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniCreateLoop
  (JNIEnv *env, jobject, jlong addr, jfloatArray verticesIn) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	int numVertices = env->GetArrayLength(verticesIn) / 2;
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verticesIn, 0);
	b2Vec2* verticesOut = new b2Vec2[numVertices];
	for( int i = 0; i < numVertices; i++ )
		verticesOut[i] = b2Vec2(vertices[i<<1], vertices[(i<<1)+1]);
	chain->CreateLoop( verticesOut, numVertices );
	delete verticesOut;
	env->ReleasePrimitiveArrayCritical(verticesIn, vertices, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniCreateChain
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniCreateChain
  (JNIEnv *env, jobject, jlong addr, jfloatArray verticesIn) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	int numVertices = env->GetArrayLength(verticesIn) / 2;
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verticesIn, 0);
	b2Vec2* verticesOut = new b2Vec2[numVertices];
	for( int i = 0; i < numVertices; i++ )
		verticesOut[i] = b2Vec2(vertices[i<<1], vertices[(i<<1)+1]);
	chain->CreateChain( verticesOut, numVertices );
	delete verticesOut;
	env->ReleasePrimitiveArrayCritical(verticesIn, vertices, 0 );
}
/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniSetPrevVertex
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniSetPrevVertex
  (JNIEnv *, jobject, jlong addr, jfloat x, jfloat y) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	chain->SetPrevVertex(b2Vec2(x, y));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniSetNextVertex
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniSetNextVertex
  (JNIEnv *, jobject, jlong addr, jfloat x, jfloat y) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	chain->SetNextVertex(b2Vec2(x, y));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniGetVertexCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniGetVertexCount
  (JNIEnv *, jobject, jlong addr) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	return chain->GetVertexCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_ChainShape
 * Method:    jniGetVertex
 * Signature: (JI[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniGetVertex
  (JNIEnv *env, jobject, jlong addr, jint index, jfloatArray verts) {
	b2ChainShape* chain = (b2ChainShape*)addr;
	const b2Vec2 v = chain->GetVertex( index );
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verts, 0);
	vertices[0] = v.x;
	vertices[1] = v.y;
	env->ReleasePrimitiveArrayCritical(verts, vertices, 0 );
}
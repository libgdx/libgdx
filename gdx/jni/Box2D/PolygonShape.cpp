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
#include "PolygonShape.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    newPolygonShape
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_newPolygonShape
(JNIEnv *, jobject)
{
	b2PolygonShape* poly = new b2PolygonShape();
	return (jlong)poly;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniSet
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSet
(JNIEnv *env, jobject, jlong addr, jfloatArray verticesIn)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	int numVertices = env->GetArrayLength(verticesIn) / 2;
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verticesIn, 0);
	b2Vec2* verticesOut = new b2Vec2[numVertices];
	for( int i = 0; i < numVertices; i++ )
		verticesOut[i] = b2Vec2(vertices[i<<1], vertices[(i<<1)+1]);
	poly->Set( verticesOut, numVertices );
	delete verticesOut;
	env->ReleasePrimitiveArrayCritical(verticesIn, vertices, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniSetAsBox
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSetAsBox__JFF
(JNIEnv *, jobject, jlong addr, jfloat hx, jfloat hy)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	poly->SetAsBox(hx, hy);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniSetAsBox
 * Signature: (JFFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSetAsBox__JFFFFF
(JNIEnv *, jobject, jlong addr, jfloat hx, jfloat hy, jfloat centerX, jfloat centerY, jfloat angle)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	poly->SetAsBox( hx, hy, b2Vec2( centerX, centerY ), angle );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniSetAsEdge
 * Signature: (JFFFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSetAsEdge
(JNIEnv *, jobject, jlong addr, jfloat v1x, jfloat v1y, jfloat v2x, jfloat v2y)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	poly->SetAsEdge(b2Vec2(v1x, v1y), b2Vec2(v2x,v2y));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniGetVertexCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniGetVertexCount
  (JNIEnv *, jobject, jlong addr)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	return poly->GetVertexCount();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_PolygonShape
 * Method:    jniGetVertex
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniGetVertex
  (JNIEnv *env, jobject, jlong addr, jint index, jfloatArray verts)
{
	b2PolygonShape* poly = (b2PolygonShape*)addr;
	const b2Vec2 v = poly->GetVertex( index );
	float* vertices = (float*)env->GetPrimitiveArrayCritical(verts, 0);
	vertices[0] = v.x;
	vertices[1] = v.y;
	env->ReleasePrimitiveArrayCritical(verts, vertices, 0 );
}


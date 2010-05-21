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



#include "Box2D.h"
#include "WorldManifold.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_WorldManifold
 * Method:    jniGetNormal
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_WorldManifold_jniGetNormal
  (JNIEnv *env, jobject, jlong addr, jfloatArray normal)
{
	b2WorldManifold* manifold = (b2WorldManifold*)addr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(normal, 0);
	tmp[0] = manifold->normal.x;
	tmp[1] = manifold->normal.y;
	env->ReleasePrimitiveArrayCritical( normal, tmp, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_WorldManifold
 * Method:    jniGetPoints
 * Signature: (J[F)V
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_WorldManifold_jniGetPoints
  (JNIEnv *env, jobject, jlong addr, jlong contactAddr, jfloatArray points)
{
	b2WorldManifold* manifold = (b2WorldManifold*)addr;
	b2Contact* contact = (b2Contact*)contactAddr;
	float* tmp = (float*)env->GetPrimitiveArrayCritical(points, 0);
	int numPoints = contact->GetManifold()->pointCount;
	float* t = tmp;
	for( int i = 0; i < numPoints; i++ )
	{
		(*t++) = manifold->points[i].x;
		(*t++) = manifold->points[i].y;
	}

	env->ReleasePrimitiveArrayCritical(points, tmp, 0 );
	return numPoints;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_WorldManifold
 * Method:    jniGetNumberOfContactPoints
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_WorldManifold_jniGetNumberOfContactPoints
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return contact->GetManifold()->pointCount;
}

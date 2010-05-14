#include "Box2D.h"
#include "Contact.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetWorldManifold
 * Signature: (J)J
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetWorldManifold
  (JNIEnv *env, jobject, jlong addr, jfloatArray mani)
{
	b2Contact* contact = (b2Contact*)addr;
	b2WorldManifold manifold;
	contact->GetWorldManifold(&manifold);
	int numPoints = contact->GetManifold()->pointCount;

	float* tmp = (float*)env->GetPrimitiveArrayCritical( mani, 0 );
	tmp[0] = manifold.normal.x;
	tmp[1] = manifold.normal.y;

	for( int i = 0; i < numPoints; i++ )
	{
		tmp[2 + i*2] = manifold.points[i].x;
		tmp[2 + i*2+1] = manifold.points[i].y;
	}

	env->ReleasePrimitiveArrayCritical( mani, tmp, 0 );

	return numPoints;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniIsTouching
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniIsTouching
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return contact->IsTouching();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniSetEnabled
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetEnabled
  (JNIEnv *, jobject, jlong addr, jboolean flag)
{
	b2Contact* contact = (b2Contact*)addr;
	contact->SetEnabled(flag);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniIsEnabled
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniIsEnabled
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return contact->IsEnabled();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetFixtureA
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFixtureA
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return (jlong)contact->GetFixtureA();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetFixtureB
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFixtureB
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return (jlong)contact->GetFixtureA();
}

#include "Box2D.h"
#include "Contact.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetWorldManifold
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetWorldManifold
  (JNIEnv *, jobject, jlong addr)
{
	b2Contact* contact = (b2Contact*)addr;
	return 0; //(jlong)contact->GetWorldManifold(b2WorldManiforl);
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

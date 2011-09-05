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
	return (jlong)contact->GetFixtureB();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetChildIndexA
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetChildIndexA
  (JNIEnv *, jobject, jlong addr) {
	b2Contact* contact = (b2Contact*)addr;
	return contact->GetChildIndexA();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetChildIndexB
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetChildIndexB
  (JNIEnv *, jobject, jlong addr){
	b2Contact* contact = (b2Contact*)addr;
	return contact->GetChildIndexB();
}
/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniSetFriction
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetFriction
  (JNIEnv *, jobject, jlong addr, jfloat friction) {
	b2Contact* contact = (b2Contact*)addr;
	contact->SetFriction(friction);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetFriction
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetFriction
  (JNIEnv *, jobject, jlong addr) {
	b2Contact* contact = (b2Contact*)addr;
	return contact->GetFriction();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniResetFriction
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniResetFriction
  (JNIEnv *, jobject, jlong addr) {
  	b2Contact* contact = (b2Contact*)addr;
	contact->ResetFriction();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniSetRestitution
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniSetRestitution
  (JNIEnv *, jobject, jlong addr, jfloat restitution) {
  	b2Contact* contact = (b2Contact*)addr;
	contact->SetRestitution(restitution);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniGetRestitution
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniGetRestitution
  (JNIEnv *, jobject, jlong addr) {
  	b2Contact* contact = (b2Contact*)addr;
	return contact->GetRestitution();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Contact
 * Method:    jniResetRestitution
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Contact_jniResetRestitution
  (JNIEnv *, jobject, jlong addr) {
  	b2Contact* contact = (b2Contact*)addr;
	contact->ResetRestitution();
}

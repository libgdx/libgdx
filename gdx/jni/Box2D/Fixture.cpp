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
#include "Fixture.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniGetType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetType
(JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	b2Shape::Type type = fixture->GetType();
	switch( type )
	{
	case b2Shape::e_circle: return 0;
	case b2Shape::e_polygon: return 1;
	default:
		return b2Shape::e_unknown;
	}
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniSetSensor
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetSensor
(JNIEnv *, jobject, jlong addr, jboolean sensor)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	fixture->SetSensor(sensor);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniIsSensor
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniIsSensor
  (JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return fixture->IsSensor();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniSetFilterData
 * Signature: (JSSS)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetFilterData
  (JNIEnv *, jobject, jlong addr, jshort categoryBits, jshort maskBits, jshort groupIndex)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	b2Filter filter;
	filter.categoryBits = categoryBits;
	filter.maskBits = maskBits;
	filter.groupIndex = groupIndex;
	fixture->SetFilterData(filter);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniGetFilterData
 * Signature: (J[S)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetFilterData
  (JNIEnv *env, jobject, jlong addr, jshortArray filter)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	unsigned short* filterOut = (unsigned short*)env->GetPrimitiveArrayCritical(filter, 0);
	b2Filter f = fixture->GetFilterData();
	filterOut[0] = f.maskBits;
	filterOut[1] = f.categoryBits;
	filterOut[2] = f.groupIndex;
	env->ReleasePrimitiveArrayCritical(filter, filterOut, 0);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniSetDensity
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetDensity
  (JNIEnv *, jobject, jlong addr, jfloat density)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	fixture->SetDensity(density);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniGetDensity
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetDensity
  (JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return fixture->GetDensity();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniGetFriction
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetFriction
  (JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return fixture->GetFriction();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniSetFriction
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetFriction
  (JNIEnv *, jobject, jlong addr, jfloat friction)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	fixture->SetFriction(friction);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniGetRestitution
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetRestitution
  (JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return fixture->GetRestitution();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniSetRestitution
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniSetRestitution
  (JNIEnv *, jobject, jlong addr, jfloat restitution)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	fixture->SetRestitution(restitution);
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Fixture
 * Method:    jniTestPoint
 * Signature: (JFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniTestPoint
  (JNIEnv *, jobject, jlong addr, jfloat x, jfloat y)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return fixture->TestPoint( b2Vec2( x, y ) );
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Fixture_jniGetShape
  (JNIEnv *, jobject, jlong addr)
{
	b2Fixture* fixture = (b2Fixture*)addr;
	return (jlong)fixture->GetShape();
}

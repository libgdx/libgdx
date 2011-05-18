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
#include "Shape.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_Shape
 * Method:    jniGetRadius
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniGetRadius
  (JNIEnv *, jobject, jlong addr)
{
	b2Shape* shape = (b2Shape*)addr;
	return shape->m_radius;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Shape
 * Method:    jniSetRadius
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniSetRadius
  (JNIEnv *, jobject, jlong addr, jfloat radius)
{
	b2Shape* shape = (b2Shape*)addr;
	shape->m_radius = radius;
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_Shape
 * Method:    jniDispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniDispose
  (JNIEnv *, jobject, jlong addr)
{
	b2Shape* shape = (b2Shape*)addr;
	delete shape;
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_Shape_jniGetType
  (JNIEnv *, jclass, jlong addr)
{
	b2Shape* shape = (b2Shape*)addr;
	if( shape->m_type == b2Shape::e_circle )
		return 0;
	else
		return 1;
}

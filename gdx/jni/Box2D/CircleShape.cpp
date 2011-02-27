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
#include "CircleShape.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_CircleShape
 * Method:    newCircleShape
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_CircleShape_newCircleShape
  (JNIEnv *, jobject)
{
	return (jlong)(new b2CircleShape( ));
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_CircleShape
 * Method:    jniGetPosition
 * Signature: (J[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_CircleShape_jniGetPosition
  (JNIEnv *env, jobject, jlong addr, jfloatArray positionOut)
{
	b2CircleShape* circle = (b2CircleShape*)addr;
	float* position = (float*)env->GetPrimitiveArrayCritical(positionOut, 0);
	position[0] = circle->m_p.x;
	position[1] = circle->m_p.y;
	env->ReleasePrimitiveArrayCritical(positionOut, position, 0 );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_CircleShape
 * Method:    jniSetPosition
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_CircleShape_jniSetPosition
  (JNIEnv *, jobject, jlong addr, jfloat positionX, jfloat positionY)
{
	b2CircleShape* circle = (b2CircleShape*)addr;
	circle->m_p.x = positionX;
	circle->m_p.y = positionY;
}

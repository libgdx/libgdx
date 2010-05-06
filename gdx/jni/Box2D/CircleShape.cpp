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

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

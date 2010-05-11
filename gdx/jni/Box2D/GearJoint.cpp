#include "Box2D.h"
#include "GearJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_GearJoint
 * Method:    jniSetRatio
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniSetRatio
  (JNIEnv *, jobject, jlong addr, jfloat ratio)
{
	b2GearJoint* joint =  (b2GearJoint*)addr;
	joint->SetRatio( ratio );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_GearJoint
 * Method:    jniGetRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniGetRatio
  (JNIEnv *, jobject, jlong addr)
{
	b2GearJoint* joint =  (b2GearJoint*)addr;
	return joint->GetRatio();
}

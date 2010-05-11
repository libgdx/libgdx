#include "Box2D.h"
#include "DistanceJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniSetLength
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetLength
  (JNIEnv *, jobject, jlong addr, jfloat len)
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	joint->SetLength( len );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniGetLength
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetLength
  (JNIEnv *, jobject, jlong addr)
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	return joint->GetLength();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniSetFrequency
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetFrequency
  (JNIEnv *, jobject, jlong addr, jfloat hz)
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	joint->SetFrequency( hz );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniGetFrequency
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetFrequency
  (JNIEnv *, jobject, jlong addr )
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	return joint->GetFrequency();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniSetDampingRatio
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetDampingRatio
  (JNIEnv *, jobject, jlong addr, jfloat dampingRatio)
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	joint->SetDampingRatio( dampingRatio );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_DistanceJoint
 * Method:    jniGetDampingRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetDampingRatio
  (JNIEnv *, jobject, jlong addr )
{
	b2DistanceJoint* joint = (b2DistanceJoint*)addr;
	return joint->GetDampingRatio();
}

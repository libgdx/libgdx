#include "Box2D.h"
#include "FrictionJoint.h"

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_FrictionJoint
 * Method:    jniSetMaxForce
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniSetMaxForce
  (JNIEnv *, jobject, jlong addr, jfloat maxForce)
{
	b2FrictionJoint* joint = (b2FrictionJoint*)addr;
	joint->SetMaxForce( maxForce );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_FrictionJoint
 * Method:    jniGetMaxForce
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniGetMaxForce
  (JNIEnv *, jobject, jlong addr)
{
	b2FrictionJoint* joint = (b2FrictionJoint*)addr;
	return joint->GetMaxForce();
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_FrictionJoint
 * Method:    jniSetMaxTorque
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniSetMaxTorque
  (JNIEnv *, jobject, jlong addr, jfloat torque)
{
	b2FrictionJoint* joint = (b2FrictionJoint*)addr;
	joint->SetMaxTorque( torque );
}

/*
 * Class:     com_badlogic_gdx_physics_box2d_joints_FrictionJoint
 * Method:    jniGetMaxTorque
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniGetMaxTorque
  (JNIEnv *, jobject, jlong addr)
{
	b2FrictionJoint* joint = (b2FrictionJoint*)addr;
	return joint->GetMaxTorque();
}

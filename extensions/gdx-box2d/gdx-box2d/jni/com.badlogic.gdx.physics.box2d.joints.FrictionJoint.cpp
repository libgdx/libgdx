#include <com.badlogic.gdx.physics.box2d.joints.FrictionJoint.h>

//@line:25

#include <Box2D/Box2D.h> 
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniSetMaxForce(JNIEnv* env, jobject object, jlong addr, jfloat force) {


//@line:38

		b2FrictionJoint* joint = (b2FrictionJoint*)addr;
		joint->SetMaxForce( force );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniGetMaxForce(JNIEnv* env, jobject object, jlong addr) {


//@line:48

		b2FrictionJoint* joint = (b2FrictionJoint*)addr;
		return joint->GetMaxForce();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniSetMaxTorque(JNIEnv* env, jobject object, jlong addr, jfloat torque) {


//@line:58

		b2FrictionJoint* joint = (b2FrictionJoint*)addr;
		joint->SetMaxTorque( torque );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_FrictionJoint_jniGetMaxTorque(JNIEnv* env, jobject object, jlong addr) {


//@line:68

		b2FrictionJoint* joint = (b2FrictionJoint*)addr;
		return joint->GetMaxTorque();
	

}


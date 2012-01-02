#include <com.badlogic.gdx.physics.box2d.joints.RevoluteJoint.h>

//@line:27

#include <Box2d/Box2D.h> 
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:40

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointAngle();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:50

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointSpeed();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsLimitEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:60

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsLimitEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableLimit(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:70

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableLimit(flag);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLowerLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:80

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetLowerLimit();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetUpperLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:90

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetUpperLimit();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetLimits(JNIEnv* env, jobject object, jlong addr, jfloat lower, jfloat upper) {


//@line:101

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetLimits(lower, upper );
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsMotorEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:111

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsMotorEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableMotor(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:121

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableMotor(flag);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMotorSpeed(JNIEnv* env, jobject object, jlong addr, jfloat speed) {


//@line:131

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMotorSpeed(speed);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:141

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorSpeed();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMaxMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat torque) {


//@line:151

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat invDt) {


//@line:161

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorTorque(invDt);
	

}


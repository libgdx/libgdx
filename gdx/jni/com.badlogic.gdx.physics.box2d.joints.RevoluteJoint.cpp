#include <com.badlogic.gdx.physics.box2d.joints.RevoluteJoint.h>

//@line:28

#include <Box2D/Box2D.h> 
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:41

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointAngle();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:51

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointSpeed();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsLimitEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:61

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsLimitEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableLimit(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:71

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableLimit(flag);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLowerLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:81

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetLowerLimit();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetUpperLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:91

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetUpperLimit();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetLimits(JNIEnv* env, jobject object, jlong addr, jfloat lower, jfloat upper) {


//@line:102

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetLimits(lower, upper );
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsMotorEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:112

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsMotorEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableMotor(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:122

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableMotor(flag);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMotorSpeed(JNIEnv* env, jobject object, jlong addr, jfloat speed) {


//@line:132

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMotorSpeed(speed);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:142

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorSpeed();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMaxMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat torque) {


//@line:152

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat invDt) {


//@line:162

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorTorque(invDt);
	

}


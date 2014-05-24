#include <com.badlogic.gdx.physics.box2d.joints.MotorJoint.h>

//@line:27

#include <Box2D/Box2D.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniGetLinearOffset(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_linearOffset) {
	float* linearOffset = (float*)env->GetPrimitiveArrayCritical(obj_linearOffset, 0);


//@line:44

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		linearOffset[0] = joint->GetLinearOffset().x;
		linearOffset[1] = joint->GetLinearOffset().y;
	
	env->ReleasePrimitiveArrayCritical(obj_linearOffset, linearOffset, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniSetLinearOffset(JNIEnv* env, jobject object, jlong addr, jfloat linearOffsetX, jfloat linearOffsetY) {


//@line:54

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		joint->SetLinearOffset(b2Vec2(linearOffsetX, linearOffsetY));
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniGetAngularOffset(JNIEnv* env, jobject object, jlong addr) {


//@line:63

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		return joint->GetAngularOffset();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniSetAngularOffset(JNIEnv* env, jobject object, jlong addr, jfloat angularOffset) {


//@line:72

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		joint->SetAngularOffset(angularOffset);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniGetMaxForce(JNIEnv* env, jobject object, jlong addr) {


//@line:81

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		return joint->GetMaxForce();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniSetMaxForce(JNIEnv* env, jobject object, jlong addr, jfloat maxForce) {


//@line:90

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		joint->SetMaxForce(maxForce);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniGetMaxTorque(JNIEnv* env, jobject object, jlong addr) {


//@line:99

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		return joint->GetMaxTorque();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniSetMaxTorque(JNIEnv* env, jobject object, jlong addr, jfloat maxTorque) {


//@line:108

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		joint->SetMaxTorque(maxTorque);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniGetCorrectionFactor(JNIEnv* env, jobject object, jlong addr) {


//@line:117

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		return joint->GetCorrectionFactor();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_MotorJoint_jniSetCorrectionFactor(JNIEnv* env, jobject object, jlong addr, jfloat correctionFactor) {


//@line:126

		b2MotorJoint* joint = (b2MotorJoint*)addr;
		joint->SetCorrectionFactor(correctionFactor);
	

}


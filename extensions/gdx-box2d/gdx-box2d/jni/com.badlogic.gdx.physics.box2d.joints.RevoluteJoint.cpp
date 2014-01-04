#include <com.badlogic.gdx.physics.box2d.joints.RevoluteJoint.h>

//@line:29

#include <Box2D/Box2D.h> 
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:46

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointAngle();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetJointSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:56

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointSpeed();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsLimitEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:66

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsLimitEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableLimit(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:76

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableLimit(flag);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLowerLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:86

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetLowerLimit();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetUpperLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:96

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetUpperLimit();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetLimits(JNIEnv* env, jobject object, jlong addr, jfloat lower, jfloat upper) {


//@line:107

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetLimits(lower, upper );
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniIsMotorEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:117

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsMotorEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniEnableMotor(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:127

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableMotor(flag);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMotorSpeed(JNIEnv* env, jobject object, jlong addr, jfloat speed) {


//@line:137

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMotorSpeed(speed);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:147

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorSpeed();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniSetMaxMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat torque) {


//@line:157

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMotorTorque(JNIEnv* env, jobject object, jlong addr, jfloat invDt) {


//@line:167

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorTorque(invDt);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLocalAnchorA(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:178

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetLocalAnchorB(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:190

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetReferenceAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:201

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetReferenceAngle();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RevoluteJoint_jniGetMaxMotorTorque(JNIEnv* env, jobject object, jlong addr) {


//@line:210

		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMaxMotorTorque();
	

}


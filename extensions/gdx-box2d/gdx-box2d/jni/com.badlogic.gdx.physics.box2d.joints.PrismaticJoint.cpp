#include <com.badlogic.gdx.physics.box2d.joints.PrismaticJoint.h>

//@line:28

#include <Box2D/Box2D.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetLocalAnchorA(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:46

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetLocalAnchorB(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:58

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetJointTranslation(JNIEnv* env, jobject object, jlong addr) {


//@line:69

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetJointTranslation();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetJointSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:79

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetJointSpeed();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniIsLimitEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:89

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->IsLimitEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniEnableLimit(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:99

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->EnableLimit(flag);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetLowerLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:109

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetLowerLimit();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetUpperLimit(JNIEnv* env, jobject object, jlong addr) {


//@line:119

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetUpperLimit();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetLimits(JNIEnv* env, jobject object, jlong addr, jfloat lower, jfloat upper) {


//@line:129

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetLimits(lower, upper );
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniIsMotorEnabled(JNIEnv* env, jobject object, jlong addr) {


//@line:139

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->IsMotorEnabled();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniEnableMotor(JNIEnv* env, jobject object, jlong addr, jboolean flag) {


//@line:149

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->EnableMotor(flag);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetMotorSpeed(JNIEnv* env, jobject object, jlong addr, jfloat speed) {


//@line:159

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetMotorSpeed(speed);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetMotorSpeed(JNIEnv* env, jobject object, jlong addr) {


//@line:169

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMotorSpeed();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniSetMaxMotorForce(JNIEnv* env, jobject object, jlong addr, jfloat force) {


//@line:179

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetMaxMotorForce(force);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetMotorForce(JNIEnv* env, jobject object, jlong addr, jfloat invDt) {


//@line:189

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMotorForce(invDt);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetMaxMotorForce(JNIEnv* env, jobject object, jlong addr) {


//@line:199

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMaxMotorForce();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PrismaticJoint_jniGetReferenceAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:209

		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetReferenceAngle();
	

}


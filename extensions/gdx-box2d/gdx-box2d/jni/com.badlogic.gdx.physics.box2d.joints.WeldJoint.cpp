#include <com.badlogic.gdx.physics.box2d.joints.WeldJoint.h>

//@line:27

		#include <Box2D/Box2D.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetLocalAnchorA(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:45

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetLocalAnchorB(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:57

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetReferenceAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:67

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetReferenceAngle();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetFrequency(JNIEnv* env, jobject object, jlong addr) {


//@line:76

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetFrequency();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniSetFrequency(JNIEnv* env, jobject object, jlong addr, jfloat hz) {


//@line:85

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		joint->SetFrequency(hz);
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetDampingRatio(JNIEnv* env, jobject object, jlong addr) {


//@line:94

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetDampingRatio();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniSetDampingRatio(JNIEnv* env, jobject object, jlong addr, jfloat ratio) {


//@line:103

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		joint->SetDampingRatio(ratio);
	

}


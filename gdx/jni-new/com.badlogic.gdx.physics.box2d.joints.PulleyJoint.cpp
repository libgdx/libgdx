#include <com.badlogic.gdx.physics.box2d.joints.PulleyJoint.h>

//@line:27

#include <Box2d/Box2D.h> 
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetGroundAnchorA(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:45

		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		anchor[0] = joint->GetGroundAnchorA().x;
		anchor[1] = joint->GetGroundAnchorA().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetGroundAnchorB(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_anchor) {
	float* anchor = (float*)env->GetPrimitiveArrayCritical(obj_anchor, 0);


//@line:60

		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		anchor[0] = joint->GetGroundAnchorB().x;
		anchor[1] = joint->GetGroundAnchorB().y;
	
	env->ReleasePrimitiveArrayCritical(obj_anchor, anchor, 0);

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetLength1(JNIEnv* env, jobject object, jlong addr) {


//@line:71

		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetLengthA();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetLength2(JNIEnv* env, jobject object, jlong addr) {


//@line:81

		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetLengthB();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_PulleyJoint_jniGetRatio(JNIEnv* env, jobject object, jlong addr) {


//@line:91

		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetRatio();
	

}


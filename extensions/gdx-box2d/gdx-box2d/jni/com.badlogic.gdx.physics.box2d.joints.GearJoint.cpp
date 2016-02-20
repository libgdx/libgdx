#include <com.badlogic.gdx.physics.box2d.joints.GearJoint.h>

//@line:29

#include <Box2D/Box2D.h> 
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniGetJoint1(JNIEnv* env, jobject object, jlong addr) {


//@line:47

		b2GearJoint* joint =  (b2GearJoint*)addr;
		b2Joint* joint1 = joint->GetJoint1();
		return (jlong)joint1;
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniGetJoint2(JNIEnv* env, jobject object, jlong addr) {


//@line:58

		b2GearJoint* joint =  (b2GearJoint*)addr;
		b2Joint* joint2 = joint->GetJoint2();
		return (jlong)joint2;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniSetRatio(JNIEnv* env, jobject object, jlong addr, jfloat ratio) {


//@line:69

		b2GearJoint* joint =  (b2GearJoint*)addr;
		joint->SetRatio( ratio );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_GearJoint_jniGetRatio(JNIEnv* env, jobject object, jlong addr) {


//@line:79

		b2GearJoint* joint =  (b2GearJoint*)addr;
		return joint->GetRatio();
	

}


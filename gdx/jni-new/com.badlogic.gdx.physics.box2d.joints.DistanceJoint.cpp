#include <com.badlogic.gdx.physics.box2d.joints.DistanceJoint.h>

//@line:25

#include <Box2d/Box2D.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetLength(JNIEnv* env, jobject object, jlong addr, jfloat length) {


//@line:38

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetLength( length );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetLength(JNIEnv* env, jobject object, jlong addr) {


//@line:48

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetLength();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetFrequency(JNIEnv* env, jobject object, jlong addr, jfloat hz) {


//@line:58

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetFrequency( hz );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetFrequency(JNIEnv* env, jobject object, jlong addr) {


//@line:68

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetFrequency();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniSetDampingRatio(JNIEnv* env, jobject object, jlong addr, jfloat ratio) {


//@line:78

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetDampingRatio( ratio );
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_DistanceJoint_jniGetDampingRatio(JNIEnv* env, jobject object, jlong addr) {


//@line:88

		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetDampingRatio();
	

}


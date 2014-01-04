#include <com.badlogic.gdx.physics.box2d.joints.RopeJoint.h>

//@line:28

#include <Box2D/Box2D.h>
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RopeJoint_jniGetMaxLength(JNIEnv* env, jobject object, jlong addr) {


//@line:41

		b2RopeJoint* rope = (b2RopeJoint*)addr;
		return rope->GetMaxLength();
	

}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RopeJoint_jniSetMaxLength(JNIEnv* env, jobject object, jlong addr, jfloat length) {


//@line:51

		b2RopeJoint* rope = (b2RopeJoint*)addr;
		rope->SetMaxLength(length);
	

}


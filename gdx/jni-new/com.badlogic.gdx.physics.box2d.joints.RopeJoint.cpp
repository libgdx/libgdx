#include <com.badlogic.gdx.physics.box2d.joints.RopeJoint.h>

//@line:12

#include <Box2d/Box2D.h>
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_RopeJoint_jniGetMaxLength(JNIEnv* env, jobject object, jlong addr) {


//@line:25

		b2RopeJoint* rope = (b2RopeJoint*)addr;
		return rope->GetMaxLength();
	

}


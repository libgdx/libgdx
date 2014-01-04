#include <com.badlogic.gdx.physics.box2d.joints.WeldJoint.h>

//@line:26

		#include <Box2D/Box2D.h>
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_box2d_joints_WeldJoint_jniGetReferenceAngle(JNIEnv* env, jobject object, jlong addr) {


//@line:38

		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetReferenceAngle();
	

}


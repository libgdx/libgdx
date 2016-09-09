#include <com.badlogic.gdx.physics.box2d.ContactImpulse.h>

//@line:24

#include <Box2D/Box2D.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ContactImpulse_jniGetNormalImpulses(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:44

		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->normalImpulses[0];
		values[1] = contactImpulse->normalImpulses[1];
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ContactImpulse_jniGetTangentImpulses(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:55

	  	b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->tangentImpulses[0];
		values[1] = contactImpulse->tangentImpulses[1];
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_ContactImpulse_jniGetCount(JNIEnv* env, jobject object, jlong addr) {


//@line:65

		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;
		return contactImpulse->count;
	

}


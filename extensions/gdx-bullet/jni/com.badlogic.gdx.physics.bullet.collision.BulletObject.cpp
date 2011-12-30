#include <com.badlogic.gdx.physics.bullet.collision.BulletObject.h>

//@line:8

	#include <Bullet-C-Api.h>
	#include <stdio.h>
	 JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_bullet_collision_BulletObject_testJni(JNIEnv* env, jclass clazz, jlong addr, jobject obj_buffer, jfloatArray obj_floats, jbyteArray obj_bytes, jstring obj_ohgod) {
	char* buffer = (char*)env->GetDirectBufferAddress(obj_buffer);
	char* ohgod = (char*)env->GetStringUTFChars(obj_ohgod, 0);
	float* floats = (float*)env->GetPrimitiveArrayCritical(obj_floats, 0);
	char* bytes = (char*)env->GetPrimitiveArrayCritical(obj_bytes, 0);


//@line:13

		printf("%f %d %s\n", floats[0], bytes[0], ohgod);
	
	env->ReleasePrimitiveArrayCritical(obj_floats, floats, 0);
	env->ReleasePrimitiveArrayCritical(obj_bytes, bytes, 0);
	env->ReleaseStringUTFChars(obj_ohgod, ohgod);

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_bullet_collision_BulletObject_testJni2(JNIEnv* env, jclass clazz, jfloat muh) {


//@line:17

		return (jint)muh + 2;
	

}


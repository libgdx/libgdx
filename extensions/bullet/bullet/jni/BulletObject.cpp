#include <BulletObject.h>

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_bullet_collision_BulletObject_disposeJni
(JNIEnv* env, jclass clazz, jlong addr, jobject obj_buffer, jfloatArray obj_floats, jbyteArray obj_bytes, jstring obj_ohgod) {
	char* buffer = (char*)env->GetDirectBufferAddress(obj_buffer);
char* ohgod = (char*)env->GetStringUTFChars(obj_ohgod, 0);	float* floats = (float*)env->GetPrimitiveArrayCritical(obj_floats, 0);
	char* bytes = (char*)env->GetPrimitiveArrayCritical(obj_bytes, 0);

	printf("Hello World %f %d %s\n", floats[0], bytes[0], ohgod);
	
	env->ReleasePrimitiveArrayCritical(obj_floats, floats, 0);
	env->ReleasePrimitiveArrayCritical(obj_bytes, bytes, 0);
	env->ReleaseStringUTFChars(obj_ohgod, ohgod);
}


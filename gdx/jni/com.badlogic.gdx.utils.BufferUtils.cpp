#include <com.badlogic.gdx.utils.BufferUtils.h>

//@line:264
 
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_freeMemory(JNIEnv* env, jclass clazz, jobject obj_buffer) {
	char* buffer = (char*)env->GetDirectBufferAddress(obj_buffer);


//@line:272

		free(buffer);
	

}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_utils_BufferUtils_newDisposableByteBuffer(JNIEnv* env, jclass clazz, jint numBytes) {


//@line:279

		char* ptr = (char*)malloc(numBytes);
		return env->NewDirectByteBuffer(ptr, numBytes);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_clear(JNIEnv* env, jclass clazz, jobject obj_buffer, jint numBytes) {
	char* buffer = (char*)env->GetDirectBufferAddress(obj_buffer);


//@line:285

		memset(buffer, 0, numBytes);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jobject obj_dst, jint numFloats, jint offset) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:289

		memcpy(dst, src + offset, numFloats << 2 );
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3BILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jbyteArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	char* src = (char*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:293

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3CILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jcharArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	unsigned short* src = (unsigned short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:297

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3SILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jshortArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	short* src = (short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:301

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3IILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jintArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	int* src = (int*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:305

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3JILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jlongArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	long long* src = (long long*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:309

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:313

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3DILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jdoubleArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);
	double* src = (double*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:317

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2ILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jobject obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* src = (unsigned char*)env->GetDirectBufferAddress(obj_src);
	unsigned char* dst = (unsigned char*)env->GetDirectBufferAddress(obj_dst);


//@line:321

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	

}


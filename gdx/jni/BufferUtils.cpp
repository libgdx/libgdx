/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
#include "BufferUtils.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static inline void copy(JNIEnv* env, jarray src, int srcOffset, jobject dst, int dstOffset, int numBytes) {
	char* pDst = (char*)env->GetDirectBufferAddress( dst );
	char* pSrc = (char*)env->GetPrimitiveArrayCritical(src, 0);
	memcpy( pDst + dstOffset, pSrc + srcOffset, numBytes);
	env->ReleasePrimitiveArrayCritical(src, pSrc, 0);
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_freeMemory
  (JNIEnv *env, jclass, jobject buffer) {
	free(env->GetDirectBufferAddress(buffer));
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jfloatArray src, jobject dst, jint numFloats, jint offset )
{
	float* pDst = (float*)env->GetDirectBufferAddress( dst );
	float* pSrc = (float*)env->GetPrimitiveArrayCritical(src, 0);
	
	memcpy( pDst, pSrc + offset, numFloats << 2 );
	env->ReleasePrimitiveArrayCritical(src, pSrc, 0);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([BILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3BILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jbyteArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([CILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3CILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jcharArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([SILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3SILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jshortArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	copy(env, src, srcOffset, dst, dstOffset, numBytes); 
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([IILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3IILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jintArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
  	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([JILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3JILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jlongArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
  	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([FILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jfloatArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([DILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3DILjava_nio_Buffer_2II
  (JNIEnv * env, jclass, jdoubleArray src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	copy(env, src, srcOffset, dst, dstOffset, numBytes);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: (Ljava/nio/Buffer;ILjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2ILjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jobject src, jint srcOffset, jobject dst, jint dstOffset, jint numBytes) {
	char* pSrc = (char*)env->GetDirectBufferAddress( src );  
	char* pDst = (char*)env->GetDirectBufferAddress( dst );
	memcpy( pDst + dstOffset, pSrc + srcOffset, numBytes);
}

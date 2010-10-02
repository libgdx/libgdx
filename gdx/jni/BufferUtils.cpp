#include "BufferUtils.h"
#include <stdio.h>
#include <string.h>

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jfloatArray src, jobject dst, jint numFloats, jint offset )
{
	float* pDst = (float*)env->GetDirectBufferAddress( dst );
	float* pSrc = (float*)env->GetPrimitiveArrayCritical(src, 0);


	memcpy( pDst, pSrc + (offset << 2), numFloats << 2 );

	env->ReleasePrimitiveArrayCritical(src, pSrc, 0);
}

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copy
 * Signature: (Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2Ljava_nio_Buffer_2I
  (JNIEnv *env, jclass, jobject src, jobject dst, jint numBytes )
{
	float* pSrc = (float*)env->GetDirectBufferAddress( src );
	float* pDst = (float*)env->GetDirectBufferAddress( dst );

	memcpy( pDst, pSrc, numBytes );
}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_utils_BufferUtils_int2float
  (JNIEnv *, jclass, jint value )
{
	return *((jfloat*)&value);
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_utils_BufferUtils_float2int
  (JNIEnv *, jclass, jfloat value)
{
	return *((jint*)&value);
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_utils_BufferUtils_bitEqual
  (JNIEnv *, jclass, jint value1, jfloat value2)
{
	jint v = *((jint*)&value2);
	return v == value1;
}

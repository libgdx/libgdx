#include "BufferUtils.h"
#include <stdio.h>
#include <string.h>

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II
  (JNIEnv *env, jclass, jfloatArray src, jobject dst, jint numFloats, jint offset )
{
	float* pDst = (float*)env->GetDirectBufferAddress( dst );
	float* pSrc = (float*)env->GetPrimitiveArrayCritical(src, 0);


	memcpy( pDst, pSrc + (offset << 2), numFloats << 2 );

	env->ReleasePrimitiveArrayCritical(src, pSrc, 0);

	return (int)pDst;
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

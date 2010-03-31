#include "AudioTools.h"

#define MAX_SHORT

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToFloat(JNIEnv *env, jclass, jobject source, jobject target, jint numSamples)
{
	short* src = (short*)env->GetDirectBufferAddress( source );
	float* dst = (float*)env->GetDirectBufferAddress( target );

	float inv = 1 / 32767.0f;
	for( int i = 0; i < numSamples; i++, src++, dst++ )
	{
		float val = (*src * inv);
		if( val < -1 )
			val = -1;
		if( val > 1 )
			val = 1;
		*dst = val;
	}
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_AudioTools
 * Method:    convertToShort
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToShort(JNIEnv *env, jclass, jobject source, jobject target, jint numSamples)
{
	float* src = (float*)env->GetDirectBufferAddress( source );
	short* dst = (short*)env->GetDirectBufferAddress( target );

	for( int i = 0; i < numSamples; i++, src++, dst++ )
		*dst = (short)(*src * 32767);
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_AudioTools
 * Method:    convertToMono
 * Signature: (Ljava/nio/ShortBuffer;Ljava/nio/ShortBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMono__Ljava_nio_ShortBuffer_2Ljava_nio_ShortBuffer_2I(JNIEnv *env, jclass, jobject source, jobject target, jint numSamples)
{
	short *src = (short*)env->GetDirectBufferAddress( source );
	short *dst = (short*)env->GetDirectBufferAddress( target );

	for( int i = 0; i < numSamples / 2; i++ )
	{
		int val = *(src++);
		val += *(src++);
		val >>= 1;
		*dst++ = val;
	}
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_AudioTools
 * Method:    convertToMono
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMono__Ljava_nio_FloatBuffer_2Ljava_nio_FloatBuffer_2I(JNIEnv *env, jclass, jobject source, jobject target, jint numSamples)
{
	float *src = (float*)env->GetDirectBufferAddress( source );
	float *dst = (float*)env->GetDirectBufferAddress( target );

	for( int i = 0; i < numSamples / 2; i++ )
	{
		float val = *(src++);
		val += *(src++);
		val /= 2;
		*dst++ = val;
	}
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_AudioTools
 * Method:    spectralFlux
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;II)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_spectralFlux(JNIEnv *env, jclass, jobject spectrumA, jobject spectrumB, jint numElements)
{
	float* src = (float*)env->GetDirectBufferAddress( spectrumA );
	float* dst = (float*)env->GetDirectBufferAddress( spectrumB );

	float flux = 0;
	for( int i = 0; i < numElements; i++ )
	{
		float value = *dst++ - *src++;
		flux += value < 0? 0: value;
	}
}

#include <com.badlogic.gdx.audio.analysis.AudioTools.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToFloat(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	short* source = (short*)env->GetDirectBufferAddress(obj_source);
	float* target = (float*)env->GetDirectBufferAddress(obj_target);


//@line:37

		float inv = 1 / 32767.0f;
		for( int i = 0; i < numSamples; i++, source++, target++ )
		{
			float val = (*source * inv);
			if( val < -1 )
				val = -1;
			if( val > 1 )
				val = 1;
			*target = val;
		}
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToShort(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	float* source = (float*)env->GetDirectBufferAddress(obj_source);
	short* target = (short*)env->GetDirectBufferAddress(obj_target);


//@line:57

		for( int i = 0; i < numSamples; i++, source++, target++ )
		*target = (short)(*source * 32767);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMonoShort(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	short* source = (short*)env->GetDirectBufferAddress(obj_source);
	short* target = (short*)env->GetDirectBufferAddress(obj_target);


//@line:69

		for( int i = 0; i < numSamples / 2; i++ )
		{
			int val = *(source++);
			val += *(source++);
			val >>= 1;
			*target++ = val;
		}
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMonoFloat(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	float* source = (float*)env->GetDirectBufferAddress(obj_source);
	float* target = (float*)env->GetDirectBufferAddress(obj_target);


//@line:86

		for( int i = 0; i < numSamples / 2; i++ )
		{
			float val = *(source++);
			val += *(source++);
			val /= 2;
			*target++ = val;
		}
	

}

static inline jfloat wrapped_Java_com_badlogic_gdx_audio_analysis_AudioTools_spectralFlux
(JNIEnv* env, jclass clazz, jobject obj_spectrumA, jobject obj_spectrumB, jint numSamples, float* spectrumA, float* spectrumB) {

//@line:103

		float flux = 0;
		for( int i = 0; i < numSamples; i++ )
		{
			float value = *spectrumB++ - *spectrumA++;
			flux += value < 0? 0: value;
		}
		// no cleanup required as we have direct buffers
		return flux;
	
}

JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_spectralFlux(JNIEnv* env, jclass clazz, jobject obj_spectrumA, jobject obj_spectrumB, jint numSamples) {
	float* spectrumA = (float*)env->GetDirectBufferAddress(obj_spectrumA);
	float* spectrumB = (float*)env->GetDirectBufferAddress(obj_spectrumB);

	jfloat JNI_returnValue = wrapped_Java_com_badlogic_gdx_audio_analysis_AudioTools_spectralFlux(env, clazz, obj_spectrumA, obj_spectrumB, numSamples, spectrumA, spectrumB);


	return JNI_returnValue;
}


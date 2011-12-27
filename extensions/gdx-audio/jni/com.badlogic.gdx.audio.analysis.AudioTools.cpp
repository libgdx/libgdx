#include <com.badlogic.gdx.audio.analysis.AudioTools.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToFloat
(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	char* source = (char*)env->GetDirectBufferAddress(obj_source);
	char* target = (char*)env->GetDirectBufferAddress(obj_target);

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
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToShort
(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	char* source = (char*)env->GetDirectBufferAddress(obj_source);
	char* target = (char*)env->GetDirectBufferAddress(obj_target);

	for( int i = 0; i < numSamples; i++, source++, target++ )
	*target = (short)(*source * 32767);
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMono__Ljava_nio_ShortBuffer_2Ljava_nio_ShortBuffer_2I
(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	char* source = (char*)env->GetDirectBufferAddress(obj_source);
	char* target = (char*)env->GetDirectBufferAddress(obj_target);

	for( int i = 0; i < numSamples / 2; i++ )
	{
		int val = *(source++);
		val += *(source++);
		val >>= 1;
		*target++ = val;
	}
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_convertToMono__Ljava_nio_FloatBuffer_2Ljava_nio_FloatBuffer_2I
(JNIEnv* env, jclass clazz, jobject obj_source, jobject obj_target, jint numSamples) {
	char* source = (char*)env->GetDirectBufferAddress(obj_source);
	char* target = (char*)env->GetDirectBufferAddress(obj_target);

	for( int i = 0; i < numSamples / 2; i++ )
	{
		float val = *(source++);
		val += *(source++);
		val /= 2;
		*target++ = val;
	}
	
}
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_audio_analysis_AudioTools_spectralFlux
(JNIEnv* env, jclass clazz, jobject obj_spectrumA, jobject obj_spectrumB, jint numSamples) {
	char* spectrumA = (char*)env->GetDirectBufferAddress(obj_spectrumA);
	char* spectrumB = (char*)env->GetDirectBufferAddress(obj_spectrumB);

	float flux = 0;
	for( int i = 0; i < numSamples; i++ )
	{
		float value = *spectrumB++ - *spectrumA++;
		flux += value < 0? 0: value;
	}
	// no cleanup required as we have direct buffers
	return flux;
	
}

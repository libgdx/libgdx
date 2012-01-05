#include <com.badlogic.gdx.audio.analysis.KissFFT.h>

//@line:58

	#include <kiss_fftr.h>
	#include <stdlib.h>
	#include <stdio.h>
	#include <math.h>
	
	#define MAX_SHORT 32767.0f
	
	static inline float scale( kiss_fft_scalar val )
	{
		if( val < 0 )
			return val * ( 1 / 32768.0f );
		else
			return val * ( 1 / 32767.0f );
	}
	
	struct KissFFT
	{
		kiss_fftr_cfg config;
		kiss_fft_cpx* spectrum;
		int numSamples;
	};
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_create(JNIEnv* env, jclass clazz, jint numSamples) {


//@line:85

		KissFFT* fft = new KissFFT();
		fft->config = kiss_fftr_alloc(numSamples,0,NULL,NULL);
		fft->spectrum = (kiss_fft_cpx*)malloc(sizeof(kiss_fft_cpx) * numSamples);
		fft->numSamples = numSamples;
		return (jlong)fft;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_destroy(JNIEnv* env, jclass clazz, jlong handle) {


//@line:95

		KissFFT* fft = (KissFFT*)handle;
		free(fft->config);
		free(fft->spectrum);
		free(fft);	
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_spectrum(JNIEnv* env, jclass clazz, jlong handle, jshortArray obj_samples, jfloatArray obj_spectrum) {
	short* samples = (short*)env->GetPrimitiveArrayCritical(obj_samples, 0);
	float* spectrum = (float*)env->GetPrimitiveArrayCritical(obj_spectrum, 0);


//@line:108

		KissFFT* fft = (KissFFT*)handle;
		kiss_fftr( fft->config, (kiss_fft_scalar*)samples, fft->spectrum );
	
		int len = fft->numSamples / 2 + 1;
		for( int i = 0; i < len; i++ )
		{
			float re = scale(fft->spectrum[i].r) * fft->numSamples;
			float im = scale(fft->spectrum[i].i) * fft->numSamples;
	
			if( i > 0 )
				spectrum[i] = sqrtf(re*re + im*im) / (fft->numSamples);
			else
				spectrum[i] = sqrtf(re*re + im*im) / (fft->numSamples);
		}
	
	env->ReleasePrimitiveArrayCritical(obj_samples, samples, 0);
	env->ReleasePrimitiveArrayCritical(obj_spectrum, spectrum, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getRealPart(JNIEnv* env, jclass clazz, jlong handle, jshortArray obj_real) {
	short* real = (short*)env->GetPrimitiveArrayCritical(obj_real, 0);


//@line:125

		KissFFT* fft = (KissFFT*)handle;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			real[i] = fft->spectrum[i].r;
	
	env->ReleasePrimitiveArrayCritical(obj_real, real, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getImagPart(JNIEnv* env, jclass clazz, jlong handle, jshortArray obj_imag) {
	short* imag = (short*)env->GetPrimitiveArrayCritical(obj_imag, 0);


//@line:131

		KissFFT* fft = (KissFFT*)handle;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			imag[i] = fft->spectrum[i].i;
	
	env->ReleasePrimitiveArrayCritical(obj_imag, imag, 0);

}


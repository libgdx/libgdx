#define FIXED_POINT
#include "kissfft/kiss_fftr.h"
#include "KissFFT.h"
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#define MAX_SHORT 32767.0f

struct KissFFT
{
	kiss_fftr_cfg config;
	kiss_fft_cpx* spectrum;
	int numSamples;
};

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_create  (JNIEnv *, jobject, jint numSamples)
{
	KissFFT* fft = new KissFFT();
	fft->config = kiss_fftr_alloc(numSamples,0,NULL,NULL);
	fft->spectrum = (kiss_fft_cpx*)malloc(sizeof(kiss_fft_cpx) * numSamples);
	fft->numSamples = numSamples;
	return (jlong)fft;
	return 0;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_destroy(JNIEnv *, jobject, jlong handle)
{
	KissFFT* fft = (KissFFT*)handle;
	free(fft->config);
	free(fft->spectrum);
	free(fft);
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getRealPart(JNIEnv *env, jobject, jlong handle, jobject real)
{
	KissFFT* fft = (KissFFT*)handle;
	short* target = (short*)env->GetDirectBufferAddress(real);
	for( int i = 0; i < fft->numSamples / 2; i++ )
		target[i] = fft->spectrum[i].r;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getImagPart(JNIEnv *env, jobject, jlong handle, jobject real)
{
	KissFFT* fft = (KissFFT*)handle;
	short* target = (short*)env->GetDirectBufferAddress(real);
	for( int i = 0; i < fft->numSamples / 2; i++ )
		target[i] = fft->spectrum[i].i;
}


JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_spectrum(JNIEnv *env, jobject, jlong handle, jobject source, jobject target)
{
	KissFFT* fft = (KissFFT*)handle;
	kiss_fft_scalar* samples = (kiss_fft_scalar*)env->GetDirectBufferAddress( source );
	float* spectrum = (float*)env->GetDirectBufferAddress( target );

	kiss_fftr( fft->config, samples, fft->spectrum );

	float SCALE = 1 / 32767.0f * 1024.0f;
	int len = fft->numSamples / 2 + 1;
	for( int i = 0; i < len; i++ )
	{
		float re = fft->spectrum[i].r * SCALE;
		float im = fft->spectrum[i].i * SCALE;

		spectrum[i] = sqrt(re*re + im*im);
	}
}

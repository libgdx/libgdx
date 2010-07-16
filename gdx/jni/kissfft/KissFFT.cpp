/**
* Copyright 2010 Mario Zechner (contact@badlogicgames.com)
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
#define FIXED_POINT
#include "kiss_fftr.h"
#include "KissFFT.h"
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

	int len = fft->numSamples / 2 + 1;

	for( int i = 0; i < len; i++ )
	{
		float re = scale(fft->spectrum[i].r) * fft->numSamples;
		float im = scale(fft->spectrum[i].i) * fft->numSamples;

		if( i > 0 )
			spectrum[i] = sqrtf(re*re + im*im) / (fft->numSamples / 2);
		else
			spectrum[i] = sqrtf(re*re + im*im) / (fft->numSamples);
	}
}


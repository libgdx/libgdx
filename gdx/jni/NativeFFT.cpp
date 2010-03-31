#include "NativeFFT.h"
#include <stdlib.h>
#include <math.h>

struct FFT
{
	int sampleRate;
	int timeSize;
	float* real;
	float* imag;
	int*   reverse;
	float* sinlookup;
	float* coslookup;
};

static inline void buildReverseTable( FFT* fft )
{
	int N = fft->timeSize;

	// set up the bit reversing table
	fft->reverse[0] = 0;
	for (int limit = 1, bit = N / 2; limit < N; limit <<= 1, bit >>= 1)
		for (int i = 0; i < limit; i++)
			fft->reverse[i + limit] = fft->reverse[i] + bit;
}

static inline void buildTrigTables( FFT* fft)
{
	int N = fft->timeSize;

	for (int i = 0; i < N; i++)
	{
		fft->sinlookup[i] = sin(-M_PI / i);
		fft->coslookup[i] = cos(-M_PI / i);
	}
}

static inline void fourierTransform( FFT* fft )
{
	for (int halfSize = 1; halfSize < fft->timeSize; halfSize *= 2)
	{
		float phaseShiftStepR = cos(halfSize);
		float phaseShiftStepI = sin(halfSize);
		float currentPhaseShiftR = 1.0f;
		float currentPhaseShiftI = 0.0f;
		for (int fftStep = 0; fftStep < halfSize; fftStep++)
		{
			for (int i = fftStep; i < fft->timeSize; i += 2 * halfSize)
			{
				int off = i + halfSize;
				float tr = (currentPhaseShiftR * fft->real[off]) - (currentPhaseShiftI * fft->imag[off]);
				float ti = (currentPhaseShiftR * fft->imag[off]) + (currentPhaseShiftI * fft->real[off]);
				fft->real[off] = fft->real[i] - tr;
				fft->imag[off] = fft->imag[i] - ti;
				fft->real[i] += tr;
				fft->imag[i] += ti;
			}
			float tmpR = currentPhaseShiftR;
			currentPhaseShiftR = (tmpR * phaseShiftStepR) - (currentPhaseShiftI * phaseShiftStepI);
			currentPhaseShiftI = (tmpR * phaseShiftStepI) + (currentPhaseShiftI * phaseShiftStepR);
		}
	}
}

static inline void bitReverseSamples(FFT* fft, float* samples, int numSamples )
{
	for (int i = 0; i < numSamples; i++)
	{
		fft->real[i] = samples[fft->reverse[i]];
		fft->imag[i] = 0.0f;
	}
}

static inline void fillSpectrum( FFT* fft, float* spectrum )
{
	for (int i = 0; i < fft->timeSize / 2 + 1; i++)
		spectrum[i] = sqrt(fft->real[i] * fft->real[i] + fft->imag[i] * fft->imag[i]);
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_NativeFFT
 * Method:    createFFT
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_analysis_NativeFFT_createFFT(JNIEnv *, jobject, jint timeSize, jint sampleRate)
{
	FFT* fft = new FFT();
	fft->timeSize = timeSize;
	fft->sampleRate = sampleRate;
	fft->real = (float*)malloc(sizeof(float)*timeSize);
	fft->imag = (float*)malloc(sizeof(float)*timeSize);
	fft->reverse = (int*)malloc(sizeof(int)*timeSize);
	fft->sinlookup = (float*)malloc(sizeof(float)*timeSize);
	fft->coslookup = (float*)malloc(sizeof(float)*timeSize);

	buildReverseTable( fft );
	buildTrigTables( fft );
	return (jlong)fft;
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_NativeFFT
 * Method:    destroyFFT
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_NativeFFT_destroyFFT(JNIEnv *, jobject, jlong handle)
{
	FFT* fft = (FFT*)handle;
	free(fft->real);
	free(fft->imag);
	free(fft->reverse);
	free(fft->sinlookup);
	free(fft->coslookup);
	free(fft);
}

/*
 * Class:     com_badlogic_gdx_audio_analysis_NativeFFT
 * Method:    nativeSpectrum
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_NativeFFT_nativeSpectrum(JNIEnv *env, jobject, jlong handle, jobject samples, jobject spectrum, jint numSamples)
{
	FFT* fft = (FFT*)handle;
	float* samp = (float*)env->GetDirectBufferAddress( samples );
	float* spec = (float*)env->GetDirectBufferAddress( spectrum );

	bitReverseSamples( fft, samp, numSamples );
	fourierTransform( fft );
	fillSpectrum( fft, spec );
}

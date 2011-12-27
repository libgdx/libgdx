/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
 ******************************************************************************/

package com.badlogic.gdx.audio.analysis;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.utils.Disposable;

/** A class for spectral analysis using native KissFFT
 * @author mzechner */
public class KissFFT implements Disposable {
	/** the pointer to the kiss fft object **/
	private final long addr;

	/** Creates a new fft instance that can analyse numSamples samples. timeSize must be a power of two.
	 * 
	 * @param numSamples the number of samples to be analysed. */
	public KissFFT (int numSamples) {
		addr = create(numSamples);
	}

	/** Calculates the frequency spectrum of the given samples. There must be as many samples as specified in the constructor of
	 * this class. Spectrum must hold timeSize / 2 + 1 elements
	 * 
	 * @param samples the samples
	 * @param spectrum the spectrum */
	public void spectrum (ShortBuffer samples, FloatBuffer spectrum) {
		spectrum(addr, samples, spectrum);
	}

	/** Releases all resources of this object */
	public void dispose () {
		destroy(addr);
	}

	public void getRealPart (ShortBuffer real) {
		getRealPart(addr, real);
	}

	public void getImagPart (ShortBuffer imag) {
		getImagPart(addr, imag);
	}
	
	/*JNI
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
	 */
	
	/** Creates a new kiss fft object
	 * @param numSamples the number of samples
	 * @return the handle to the kiss fft object */
	private static native long create (int numSamples); /*
		KissFFT* fft = new KissFFT();
		fft->config = kiss_fftr_alloc(numSamples,0,NULL,NULL);
		fft->spectrum = (kiss_fft_cpx*)malloc(sizeof(kiss_fft_cpx) * numSamples);
		fft->numSamples = numSamples;
		return (jlong)fft;
	*/
	
	/** Destroys a kiss fft object
	 * @param handle the handle to the kiss fft object */
	private static native void destroy (long handle); /*
		KissFFT* fft = (KissFFT*)handle;
		free(fft->config);
		free(fft->spectrum);
		free(fft);	
	*/
	
	/** Calculates the frequency spectrum of the given samples. There must be as many samples as specified in the constructor of
	 * this class. Spectrum must hold timeSize / 2 + 1 elements
	 * 
	 * @param handle the handle to the kiss fft object
	 * @param samples the samples in 16-bit signed PCM encoding
	 * @param spectrum the spectrum */
	private static native void spectrum (long handle, ShortBuffer samples, FloatBuffer spectrum); /*
		KissFFT* fft = (KissFFT*)handle;
		kiss_fftr( fft->config, (kiss_fft_scalar*)samples, fft->spectrum );
	
		int len = fft->numSamples / 2 + 1;
		float* out = (float*)spectrum;
	
		for( int i = 0; i < len; i++ )
		{
			float re = scale(fft->spectrum[i].r) * fft->numSamples;
			float im = scale(fft->spectrum[i].i) * fft->numSamples;
	
			if( i > 0 )
				out[i] = sqrtf(re*re + im*im) / (fft->numSamples / 2);
			else
				out[i] = sqrtf(re*re + im*im) / (fft->numSamples);
		}
	*/

	private static native void getRealPart (long handle, ShortBuffer real); /*
		KissFFT* fft = (KissFFT*)handle;
		short* out = (short*)real;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			out[i] = fft->spectrum[i].r;
	*/

	private static native void getImagPart (long handle, ShortBuffer imag); /*
		KissFFT* fft = (KissFFT*)handle;
		short* out = (short*)imag;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			out[i] = fft->spectrum[i].i;
	*/

// public static void main (String[] argv) {
// final float frequency = 440;
// float increment = (float)(2 * Math.PI) * frequency / 44100; // angular increment for each sample
// float angle = 0;
// short samples[] = new short[1024];
//
// for (int i = 0; i < samples.length; i++) {
// float value = (float)Math.sin(angle);
// samples[i] = (short)(value * 32767);
// angle += increment;
// }
//
// ShortBuffer samplesBuffer = AudioTools.allocateShortBuffer(1024, 1);
// ShortBuffer realBuffer = AudioTools.allocateShortBuffer(512, 1);
// ShortBuffer imagBuffer = AudioTools.allocateShortBuffer(512, 1);
// samplesBuffer.put(samples);
// FloatBuffer spectrum = AudioTools.allocateFloatBuffer(513, 1);
// KissFFT fft = new KissFFT(1024);
//
// fft.spectrum(samplesBuffer, spectrum);
//
// fft.getRealPart(realBuffer);
// fft.getImagPart(imagBuffer);
// short[] re = new short[512];
// short[] im = new short[512];
// float[] sp = new float[513];
// realBuffer.position(0);
// realBuffer.get(re);
// imagBuffer.position(0);
// imagBuffer.get(im);
// spectrum.position(0);
// spectrum.get(sp);
//
// for (int i = 0; i < 30; i++) {
// System.out.println(sp[i]);
// }
// }
}

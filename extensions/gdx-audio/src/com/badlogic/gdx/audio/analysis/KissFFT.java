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

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** A class for spectral analysis using native KissFFT
 * @author mzechner */
public class KissFFT implements Disposable {
	/** the pointer to the kiss fft object **/
	private final long addr;

	/** Creates a new fft instance that can analyse numSamples samples. timeSize must be a power of two.
	 * 
	 * @param numSamples the number of samples to be analysed. */
	public KissFFT (int numSamples) {
		new SharedLibraryLoader().load("gdx-audio");
		addr = create(numSamples);
	}

	/** Calculates the frequency spectrum of the given samples. There must be as many samples as specified in the constructor of
	 * this class. Spectrum must hold timeSize / 2 + 1 elements
	 * 
	 * @param samples the samples
	 * @param spectrum the spectrum */
	public void spectrum (short[] samples, float[] spectrum) {
		spectrum(addr, samples, spectrum);
	}

	/** Releases all resources of this object */
	public void dispose () {
		destroy(addr);
	}

	public void getRealPart (short[] real) {
		getRealPart(addr, real);
	}

	public void getImagPart (short[] imag) {
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
	private static native void spectrum (long handle, short[] samples, float[] spectrum); /*
		KissFFT* fft = (KissFFT*)handle;
		kiss_fftr( fft->config, (kiss_fft_scalar*)samples, fft->spectrum );
	
		int len = fft->numSamples / 2 + 1;
		for( int i = 0; i < len; i++ )
		{
			float re = scale(fft->spectrum[i].r) * fft->numSamples;
			float im = scale(fft->spectrum[i].i) * fft->numSamples;
	
			if( i > 0 )
				spectrum[i] = sqrtf(re*re + im*im);
			else
				spectrum[i] = sqrtf(re*re + im*im);
		}
	*/

	private static native void getRealPart (long handle, short[] real); /*
		KissFFT* fft = (KissFFT*)handle;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			real[i] = fft->spectrum[i].r;
	*/

	private static native void getImagPart (long handle, short[] imag); /*
		KissFFT* fft = (KissFFT*)handle;
		for( int i = 0; i < fft->numSamples / 2; i++ )
			imag[i] = fft->spectrum[i].i;
	*/
}

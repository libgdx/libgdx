/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.audio.analysis;

import java.nio.FloatBuffer;

/**
 * A native implementation of the Fast Fourier Transform, directly ported from the {@link FFT} class.
 * 
 * @author mzechner
 * 
 */
public class NativeFFT {
	/** the handle to the native fft instance **/
	private long handle;

	public NativeFFT (int timeSize, int sampleRate) {
		handle = createFFT(timeSize, sampleRate);
	}

	private native long createFFT (int timeSize, int sampleRate);

	private native void destroyFFT (long handle);

	private native void nativeSpectrum (long handle, FloatBuffer samples, FloatBuffer spectrum, int numSamples);

	public void spectrum (FloatBuffer samples, FloatBuffer spectrum, int numSamples) {
		nativeSpectrum(handle, samples, spectrum, numSamples);
	}

	public void dispose () {
		destroyFFT(handle);
	}
}

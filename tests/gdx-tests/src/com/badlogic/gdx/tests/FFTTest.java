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

package com.badlogic.gdx.tests;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import com.badlogic.gdx.audio.AudioBuild;
import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.analysis.FFT;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/**
 * Simples test that makes sure the output of KissFFT and FFT are (mostly)
 * equal.
 * @author mzechner
 *
 */
public class FFTTest {
	static int SAMPLES = 1024;

	public static void main (String[] argv) throws Exception {
		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
		short[] samples = AudioTools.generate(44100, 440, SAMPLES);
		float[] samplesFloat = AudioTools.generateFloat(44100, 440, SAMPLES);
		
		// Damien's FFT
		FFT fft = new FFT(SAMPLES, 44100);
		fft.forward(samplesFloat);
		float[] spectrum = fft.getSpectrum();		
		System.out.println(Arrays.toString(spectrum));

		// KissFFT
		spectrum = new float[SAMPLES / 2 + 1];
		KissFFT kfft = new KissFFT(SAMPLES);
		kfft.spectrum(samples, spectrum);
		System.out.println(Arrays.toString(spectrum));
		kfft.dispose();
	}
}

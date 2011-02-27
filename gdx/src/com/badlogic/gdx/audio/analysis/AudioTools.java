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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Class holding various static native methods for processing audio data.
 * 
 * @author mzechner
 * 
 */
public class AudioTools {
	/**
	 * Converts the 16-bit signed PCM data given in source to 32-bit float PCM in the range [-1,1]. It is assumed that there's
	 * numSamples elements available in both buffers. Source and target get read and written to from offset 0. All buffers must be
	 * direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples
	 */
	public static native void convertToFloat (ShortBuffer source, FloatBuffer target, int numSamples);

	/**
	 * Converts the 32-bit float PCM data given in source to 16-bit signed PCM in the range [-1,1]. It is assumed that there's
	 * numSamples elements available in both buffers. Source and target get read and written to from offset 0. All buffers must be
	 * direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples
	 */
	public static native void convertToShort (FloatBuffer source, FloatBuffer target, int numSamples);

	/**
	 * Converts the samples in source which are assumed to be interleaved left/right stereo samples to mono, writting the converted
	 * samples to target. Source is assumed to hold numSamples samples, target should hold numSamples / 2. Samples are read and
	 * written from position 0 up to numSamples. All buffers must be direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this)
	 */
	public static native void convertToMono (ShortBuffer source, ShortBuffer target, int numSamples);

	/**
	 * Converts the samples in source which are assumed to be interleaved left/right stereo samples to mono, writting the converted
	 * samples to target. Source is assumed to hold numSamples samples, target should hold numSamples / 2. Samples are read and
	 * written from position 0 up to numSamples. All buffers must be direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this)
	 */
	public static native void convertToMono (FloatBuffer source, FloatBuffer target, int numSamples);

	/**
	 * Calculates the spectral flux between the two given spectra. Both buffers are assumed to hold numSamples elements. Spectrum B
	 * is the current spectrum spectrum A the last spectrum. All buffers must be direct.
	 * 
	 * @param spectrumA the first spectrum
	 * @param spectrumB the second spectrum
	 * @param numSamples the number of elements
	 * @return the spectral flux
	 */
	public static native float spectralFlux (FloatBuffer spectrumA, FloatBuffer spectrumB, int numSamples);

	/**
	 * Allcoates a direct buffer for the given number of samples and channels. The final numer of samples is numSamples *
	 * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer
	 */
	public static FloatBuffer allocateFloatBuffer (int numSamples, int numChannels) {
		ByteBuffer b = ByteBuffer.allocateDirect(numSamples * numChannels * 4);
		b.order(ByteOrder.nativeOrder());
		return b.asFloatBuffer();
	}

	/**
	 * Allcoates a direct buffer for the given number of samples and channels. The final numer of samples is numSamples *
	 * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer
	 */
	public static ShortBuffer allocateShortBuffer (int numSamples, int numChannels) {
		ByteBuffer b = ByteBuffer.allocateDirect(numSamples * numChannels * 2);
		b.order(ByteOrder.nativeOrder());
		return b.asShortBuffer();
	}
}

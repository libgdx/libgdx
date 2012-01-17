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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** Class holding various static native methods for processing audio data.
 * 
 * @author mzechner */
public class AudioTools {
	static {
		new SharedLibraryLoader().load("gdx-audio");
	}
	
	/** Converts the 16-bit signed PCM data given in source to 32-bit float PCM in the range [-1,1]. It is assumed that there's
	 * numSamples elements available in both buffers. Source and target get read and written to from offset 0. All buffers must be
	 * direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples */
	public static native void convertToFloat (ShortBuffer source, FloatBuffer target, int numSamples); /*
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
	*/

	/** Converts the 32-bit float PCM data given in source to 16-bit signed PCM in the range [-1,1]. It is assumed that there's
	 * numSamples elements available in both buffers. Source and target get read and written to from offset 0. All buffers must be
	 * direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples */
	public static native void convertToShort (FloatBuffer source, ShortBuffer target, int numSamples); /*
		for( int i = 0; i < numSamples; i++, source++, target++ )
		*target = (short)(*source * 32767);
	*/

	/** Converts the samples in source which are assumed to be interleaved left/right stereo samples to mono, writting the converted
	 * samples to target. Source is assumed to hold numSamples samples, target should hold numSamples / 2. Samples are read and
	 * written from position 0 up to numSamples. All buffers must be direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this) */
	public static native void convertToMonoShort (ShortBuffer source, ShortBuffer target, int numSamples); /*
		for( int i = 0; i < numSamples / 2; i++ )
		{
			int val = *(source++);
			val += *(source++);
			val >>= 1;
			*target++ = val;
		}
	*/

	/** Converts the samples in source which are assumed to be interleaved left/right stereo samples to mono, writting the converted
	 * samples to target. Source is assumed to hold numSamples samples, target should hold numSamples / 2. Samples are read and
	 * written from position 0 up to numSamples. All buffers must be direct.
	 * 
	 * @param source the source buffer
	 * @param target the target buffer
	 * @param numSamples the number of samples to convert (target will have numSamples /2 filled after a call to this) */
	public static native void convertToMonoFloat (FloatBuffer source, FloatBuffer target, int numSamples); /*
		for( int i = 0; i < numSamples / 2; i++ )
		{
			float val = *(source++);
			val += *(source++);
			val /= 2;
			*target++ = val;
		}
	*/

	/** Calculates the spectral flux between the two given spectra. Both buffers are assumed to hold numSamples elements. Spectrum B
	 * is the current spectrum spectrum A the last spectrum. All buffers must be direct.
	 * 
	 * @param spectrumA the first spectrum
	 * @param spectrumB the second spectrum
	 * @param numSamples the number of elements
	 * @return the spectral flux */
	public static native float spectralFlux (FloatBuffer spectrumA, FloatBuffer spectrumB, int numSamples); /*
		float flux = 0;
		for( int i = 0; i < numSamples; i++ )
		{
			float value = *spectrumB++ - *spectrumA++;
			flux += value < 0? 0: value;
		}
		// no cleanup required as we have direct buffers
		return flux;
	*/

	/** Allcoates a direct buffer for the given number of samples and channels. The final numer of samples is numSamples *
	 * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer */
	public static FloatBuffer allocateFloatBuffer (int numSamples, int numChannels) {
		ByteBuffer b = ByteBuffer.allocateDirect(numSamples * numChannels * 4);
		b.order(ByteOrder.nativeOrder());
		return b.asFloatBuffer();
	}

	/** Allcoates a direct buffer for the given number of samples and channels. The final numer of samples is numSamples *
	 * numChannels.
	 * 
	 * @param numSamples the number of samples
	 * @param numChannels the number of channels
	 * @return the direct buffer */
	public static ShortBuffer allocateShortBuffer (int numSamples, int numChannels) {
		ByteBuffer b = ByteBuffer.allocateDirect(numSamples * numChannels * 2);
		b.order(ByteOrder.nativeOrder());
		return b.asShortBuffer();
	}

	static public void toShort (byte[] src, int offsetSrc, short[] dst, int offsetDst, int numBytes) {
		if (numBytes % 2 != 0) throw new GdxRuntimeException("bytes must be even (2 bytes 16-bit PCM expected)");
		for (int i = offsetSrc, ii = offsetDst; i < numBytes;) {
			int b1 = src[i++] & 0xff;
			int b2 = src[i++] & 0xff;
			dst[ii++] = (short)(b1 | (b2 << 8));
		}
	}

	static public void toFloat (byte[] src, int offsetSrc, float[] dst, int offsetDst, int numBytes) {
		if (numBytes % 2 != 0) throw new GdxRuntimeException("bytes must be even (2 bytes 16-bit PCM expected)");
		float scale = 1.0f / Short.MAX_VALUE;
		for (int i = offsetSrc, ii = offsetDst; i < numBytes;) {
			int b1 = src[i++] & 0xff;
			int b2 = src[i++] & 0xff;
			dst[ii++] = (short)(b1 | (b2 << 8)) * scale;
		}
	}

	static public void toFloat (short[] src, int offsetSrc, float[] dst, int offsetDst, int numBytes) {
		float scale = 1.0f / Short.MAX_VALUE;
		for (int i = offsetSrc, ii = offsetDst; i < numBytes; i++, ii++)
			dst[i] = src[ii] * scale;
	}
	
	/**
	 * Generates a mono PCM sample buffer for the given frequency and length in
	 * samples for use with AudioMixer.
	 * 
	 * @param frequency
	 * @param numSamples
	 * @return PCM data for a sine wave
	 */
	public static short[] generate(int samplingRate, int frequency, int numSamples) {
		short[] samples = new short[numSamples];
		float increment = (float) (2 * Math.PI) * frequency / (float)samplingRate;
		float angle = 0;

		for (int i = 0; i < numSamples; i++) {
			samples[i] = (short)(Math.sin(angle) * Short.MAX_VALUE);
			angle += increment;
		}
		
		return samples;
	}
	
	/**
	 * Generates a mono PCM sample buffer for the given frequency and length in
	 * seconds for use with AudioMixer.
	 * 
	 * @param frequency
	 * @param length
	 */
	public static short[] generate(int samplingRate, int frequency, float length) {
		int numSamples = (int) (samplingRate * length);
		return generate(samplingRate, frequency, numSamples);
	}
	
	/**
	 * Generates a mono PCM sample buffer for the given frequency and length in
	 * seconds for use with AudioMixer.
	 * 
	 * @param frequency
	 */
	public static float[] generateFloat(int samplingRate, int frequency, int numSamples) {
		float[] samples = new float[numSamples];
		float increment = (float) (2 * Math.PI) * frequency / (float)samplingRate;
		float angle = 0;

		for (int i = 0; i < numSamples; i++) {
			samples[i] = (float)Math.sin(angle);
			angle += increment;
		}
		return samples;
	}
	
	/**
	 * Generates a mono PCM sample buffer for the given frequency and length in
	 * seconds for use with AudioMixer.
	 * 
	 * @param frequency
	 * @param length
	 */
	public static float[] generateFloat(int samplingRate, int frequency, float length) {
		int numSamples = (int) (samplingRate * length);
		return generateFloat(samplingRate, frequency, numSamples);
	}
}

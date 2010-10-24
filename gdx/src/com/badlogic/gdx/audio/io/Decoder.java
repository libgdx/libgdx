/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.audio.io;

import java.nio.ShortBuffer;

/**
 * Interface for audio decoders that return successive amplitude frames. When a decoder is no longer used it has to be disposed.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public interface Decoder {
	/**
	 * Reads in samples.capacity() samples in 16-bit signed PCM format from the decoder. Returns the actual number read in. If this
	 * number is smaller than the capacity of the buffer then the end of stream has been reached.
	 * 
	 * @param samples The number of read samples.
	 */
	public int readSamples (ShortBuffer samples);

	/**
	 * Skips numSamples samples. If the decoded file is in stereo the left and right channel samples are counted as 2 samples.
	 * 
	 * @param numSamples the number of samples to skip
	 * @return the number of samples actually skipped. If this is < numSamples then the end of the file has been reached.
	 */
	public int skipSamples (int numSamples);

	/**
	 * @return the number of channels
	 */
	public int getNumChannels ();

	/**
	 * @return the sampling rate in herz, e.g. 44100
	 */
	public int getRate ();

	/**
	 * @return the length of the file in seconds
	 */
	public float getLength ();

	/**
	 * Disposes the decoder and frees all associated resources
	 */
	public void dispose ();
}

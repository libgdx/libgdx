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
 * A {@link Decoder} implementation that decodes MP3 files via libmpg123 natively.
 * 
 * @author mzechner
 * 
 */
public class Mpg123Decoder implements Decoder {
	public final long handle;

	/**
	 * Opens the given file for mp3 decoding. Throws an IllegalArugmentException in case the file could not be opened.
	 * 
	 * @param filename the filename
	 */
	public Mpg123Decoder (String filename) {
		handle = openFile(filename);

		if (handle == -1) throw new IllegalArgumentException("couldn't open file");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int readSamples (ShortBuffer samples) {
		int read = readSamples(handle, samples, samples.capacity());
		samples.position(0);
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int skipSamples (int numSamples) {
		return skipSamples(handle, numSamples);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumChannels () {
		return getNumChannels(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRate () {
		return getRate(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getLength () {
		return getLength(handle);
	}

	private native long openFile (String filename);

	private native int readSamples (long handle, ShortBuffer buffer, int numSamples);

	private native int skipSamples (long handle, int numSamples);

	private native int getNumChannels (long handle);

	private native int getRate (long handle);

	private native float getLength (long handle);

	private native void closeFile (long handle);

	/**
	 * {@inheritDoc}
	 */
	@Override public void dispose () {
		closeFile(handle);
	}
}

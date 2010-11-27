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
package com.badlogic.gdx.audio.io;

import java.nio.ShortBuffer;

/**
 * A {@link Decoder} implementation that decodes OGG Vorbis files using tremor
 * @author mzechner
 * 
 */
public class VorbisDecoder implements Decoder {
	/** the handle **/
	private final long handle;

	/**
	 * Opens the given file for ogg decoding. Throws an IllegalArugmentException in case the file could not be opened.
	 * 
	 * @param filename the filename
	 */
	public VorbisDecoder (String filename) {
		handle = openFile(filename);
		if (handle == 0) throw new IllegalArgumentException("couldn't open file '" + filename + "'");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void dispose () {
		closeFile(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public float getLength () {
		return getLength(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getNumChannels () {
		return getNumChannels(handle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getRate () {
		return getRate(handle);
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

	private native long openFile (String file);

	private native int getNumChannels (long handle);

	private native int getRate (long handle);

	private native float getLength (long handle);

	private native int readSamples (long handle, ShortBuffer buffer, int numSamples);

	private native int skipSamples (long handle, int numSamples);

	private native void closeFile (long handle);
}

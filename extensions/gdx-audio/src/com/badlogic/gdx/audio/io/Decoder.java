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

package com.badlogic.gdx.audio.io;

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

import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.utils.Disposable;

/** Abstract class for audio decoders that return successive amplitude frames. When a decoder is no longer used it has to be
 * disposed.
 * 
 * @author badlogicgames@gmail.com */
public abstract class Decoder implements Disposable {
	/** Reads in samples.capacity() samples in 16-bit signed PCM short format from the decoder. Returns the actual number read in.
	 * If this number is smaller than the capacity of the buffer then the end of stream has been reached.
	 * 
	 * @param samples The number of samples to read.
	 * @param offset the offset at which to start writting samples to
	 * @return the number of samples read, < numSamples means end of file */
	public abstract int readSamples (short[] samples, int offset, int numSamples);

	private short[] readBuffer;
	private int channels;
	/** Reads in samples.capacity() samples in 32-bit signed PCM float format from the decoder. Returns the actual number read in. 
	 * If this number is smaller than the capacity of the buffer then the end of stream has been reached. 
	 * 
	 * @param samples The number of samples to read.
	 * @param offset the offset at which to start writting samples to
	 * @return the number of samples read, < numSamples means end of file */
	public int readSamples(float[] samples, int offset, int numSamples) {
		if (readBuffer == null || readBuffer.length < numSamples)
			readBuffer = new short[numSamples];
		if (channels <= 0)
			channels = getChannels();
		int result = readSamples(readBuffer, 0, numSamples);
		if (result <= 0) 
			return result;
		AudioTools.toFloat(readBuffer, 0, samples, offset, result);
		return result;
	}
	
	/** Reads in the entire sound file into a single short[] array. */
	public short[] readAllSamples () {
		short[] out = new short[(int)Math.ceil(getLength() * getRate() * getChannels())];
		short[] buffer = new short[1024 * 5];
		int readSamples = 0;
		int totalSamples = 0;

		while ((readSamples = readSamples(buffer, 0, buffer.length)) > 0) {
			if (readSamples + totalSamples >= out.length) {
				short[] tmp = new short[readSamples + totalSamples];
				System.arraycopy(out, 0, tmp, 0, totalSamples);
				out = tmp;
			}
			System.arraycopy(buffer, 0, out, totalSamples, readSamples);
			totalSamples += readSamples;
		}

		if (out.length != totalSamples) {
			short[] tmp = new short[totalSamples];
			System.arraycopy(out, 0, tmp, 0, totalSamples);
			out = tmp;
		}
		return out;

	}

	/** Skips numSamples samples. If the decoded file is in stereo the left and right channel samples are counted as 2 samples.
	 * 
	 * @param numSamples the number of samples to skip
	 * @return the number of samples actually skipped. If this is < numSamples then the end of the file has been reached. */
	public abstract int skipSamples (int numSamples);
	
	/** @return	True if the methods {@link #setPosition(float)} and {@link #getPosition()} can be used, false otherwise. */
	public abstract boolean canSeek();
	
	/** Sets the current position within the file to the specified value if supported
	 * @return True if successful, false otherwise */
	public abstract boolean setPosition(float seconds);
	
	/** @return The current position (in seconds) within the decoder or negative if not supported. */
	public abstract float getPosition();

	/** @return the number of channels */
	public abstract int getChannels ();

	/** @return the sampling rate in herz, e.g. 44100 */
	public abstract int getRate ();

	/** @return the length of the file in seconds */
	public abstract float getLength ();

	/** Disposes the decoder and frees all associated resources */
	public abstract void dispose ();
}

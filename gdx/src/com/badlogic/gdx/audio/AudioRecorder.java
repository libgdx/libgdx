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

package com.badlogic.gdx.audio;

import com.badlogic.gdx.utils.Disposable;

/** An AudioRecorder allows to record input from an audio device. It has a sampling rate and is either stereo or mono. Samples are
 * returned in signed 16-bit PCM format. Stereo samples are interleaved in the order left channel, right channel. The
 * AudioRecorder has to be disposed if no longer needed via the {@link #dispose()}.
 * 
 * @author mzechner */
public interface AudioRecorder extends Disposable {
	/** Reads in numSamples samples into the array samples starting at offset. If the recorder is in stereo you have to multiply
	 * numSamples by 2.
	 * 
	 * @param samples the array to write the samples to
	 * @param offset the offset into the array
	 * @param numSamples the number of samples to be read */
	public void read (short[] samples, int offset, int numSamples);

	/** Disposes the AudioRecorder */
	public void dispose ();
}

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

package com.badlogic.gdx.backends.desktop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.audio.AudioRecorder;

/**
 * {@link AudioRecorder} implementation for the desktop using the java sound API.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class JoglAudioRecorder implements AudioRecorder {
	/** the line we read the audio from **/
	private TargetDataLine line;

	/** the buffer to temporarily store the samples **/
	private byte[] buffer = new byte[1024 * 4];

	public JoglAudioRecorder (int samplingRate, boolean isMono) {
		try {
			AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, samplingRate, 16, isMono ? 1 : 2, isMono ? 2 : 4,
				samplingRate, false);
			line = AudioSystem.getTargetDataLine(format);
			line.open(format, buffer.length);
			line.start();
		} catch (Exception ex) {
			throw new GdxRuntimeException("Couldn't create AudioRecorder", ex);
		}
	}

	@Override public void read (short[] samples, int offset, int numSamples) {
		if (buffer.length < numSamples * 2) buffer = new byte[numSamples * 2];

		int toRead = numSamples * 2;
		int read = 0;

		while (read != toRead)
			read += line.read(buffer, read, toRead - read);

		for (int i = 0, j = 0; i < numSamples * 2; i += 2, j++) {
			samples[offset + j] = (short)((buffer[i] << 8) | (buffer[i + 1] & 0xff));
		}
	}

	@Override public void dispose () {
		line.close();
	}
}

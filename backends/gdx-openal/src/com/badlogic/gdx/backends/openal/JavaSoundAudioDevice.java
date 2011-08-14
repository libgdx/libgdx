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

package com.badlogic.gdx.backends.openal;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author mzechner */
public class JavaSoundAudioDevice implements AudioDevice {
	private SourceDataLine line;
	private final boolean isMono;
	private byte[] bytes = new byte[44100 * 2 * 2];

	public JavaSoundAudioDevice (int samplingRate, boolean isMono) {
		this.isMono = isMono;

		try {
			AudioFormat format = new AudioFormat(samplingRate, 16, isMono ? 1 : 2, true, false);
			line = AudioSystem.getSourceDataLine(format);
			line.open(format, Math.min(1024, samplingRate / 10) * 2);
			line.start();
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error creating JavaSoundAudioDevice.", ex);
		}
	}

	public void dispose () {
		line.drain();
		line.close();
	}

	public boolean isMono () {
		return isMono;
	}

	public void writeSamples (short[] samples, int offset, int numSamples) {
		if (bytes.length < samples.length * 2) bytes = new byte[samples.length * 2];

		for (int i = offset, j = 0; i < offset + numSamples; i++, j += 2) {
			short value = samples[i];
			bytes[j] = (byte)(value & 0xff);
			bytes[j + 1] = (byte)(value >> 8);
		}

		int writtenBytes = line.write(bytes, 0, numSamples * 2);
		while (writtenBytes != numSamples * 2)
			writtenBytes += line.write(bytes, writtenBytes, numSamples * 2 - writtenBytes);
	}

	public void writeSamples (float[] samples, int offset, int numSamples) {
		if (bytes.length < samples.length * 2) bytes = new byte[samples.length * 2];

		for (int i = offset, j = 0; i < offset + numSamples; i++, j += 2) {
			float fValue = samples[i];
			if (fValue > 1) fValue = 1;
			if (fValue < -1) fValue = -1;
			short value = (short)(fValue * Short.MAX_VALUE);
			bytes[j] = (byte)(value & 0xff);
			bytes[j + 1] = (byte)(value >> 8);
		}

		int writtenBytes = line.write(bytes, 0, numSamples * 2);
		while (writtenBytes != numSamples * 2)
			writtenBytes += line.write(bytes, writtenBytes, numSamples * 2 - writtenBytes);
	}

	@Override
	public int getLatency () {
		return 0;
	}
}

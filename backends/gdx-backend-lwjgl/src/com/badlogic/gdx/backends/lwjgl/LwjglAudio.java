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

package com.badlogic.gdx.backends.lwjgl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * An implementation of the {@link Audio} interface for the desktop.
 * 
 * @author mzechner
 * 
 */
final class LwjglAudio implements Audio, Runnable {
	/** the audio line for sound effects **/
	private SourceDataLine line;

	/** The current buffers to play **/
	private final List<LwjglSoundBuffer> buffers = new ArrayList<LwjglSoundBuffer>();

	/** The sound effects thread **/
	private Thread thread;

	private volatile boolean run = false;

	/**
	 * Helper class for playing back sound effects concurrently.
	 * 
	 * @author mzechner
	 * 
	 */
	class LwjglSoundBuffer {
		private final float[] samples;
		private final AudioFormat format;
		private final float volume;
		private int writtenSamples = 0;

		public LwjglSoundBuffer (LwjglSound sound, float volume) throws Exception {
			samples = sound.getAudioData();
			format = sound.getAudioFormat();
			this.volume = volume;
		}

		/**
		 * Writes the next numFrames frames to the line for playback
		 * @return whether playback is done or not.
		 */
		public boolean writeSamples (int numSamples, float[] buffer) {
			if (format.getChannels() == 1) {
				int remainingSamples = Math.min(samples.length, writtenSamples + numSamples / 2);
				for (int i = writtenSamples, j = 0; i < remainingSamples; i++, j += 2) {
					buffer[j] += samples[i] * volume;
					buffer[j + 1] += samples[i] * volume;
					writtenSamples++;
				}
			} else {
				int remainingSamples = Math.min(samples.length, writtenSamples + numSamples);
				for (int i = writtenSamples, j = 0; i < remainingSamples; i += 2, j += 2) {
					buffer[j] += samples[i] * volume;
					buffer[j + 1] += samples[i + 1] * volume;
					writtenSamples += 2;
				}
			}

			if (writtenSamples >= samples.length)
				return false;
			else
				return true;
		}
	}

	LwjglAudio () {
		try {
			AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, false);
			line = AudioSystem.getSourceDataLine(format);
			line.open(format, 4410);
			line.start();
			thread = new Thread(this, "LWJGL Audio");
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AudioDevice newAudioDevice (boolean isMono) {
		return new LwjglAudioDevice(isMono);
	}

	public Music newMusic (FileHandle file) {
		try {
			LwjglMusic music = new LwjglMusic(((LwjglFileHandle)file));
			return music;
		} catch (Throwable e) {
			throw new GdxRuntimeException("Couldn't create Music instance from file '" + file + "'", e);
		}
	}

	public Sound newSound (FileHandle file) {
		try {
			LwjglSound sound = new LwjglSound(this, ((LwjglFileHandle)file));
			return sound;
		} catch (Exception e) {
			throw new GdxRuntimeException("Couldn't create Sound instance from file '" + file + "'", e);
		}
	}

	protected void enqueueSound (LwjglSound sound, float volume) {
		try {
			synchronized (this) {
				buffers.add(new LwjglSoundBuffer(sound, volume));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run () {

		int NUM_SAMPLES = 44100 * 2;
		float[] buffer = new float[NUM_SAMPLES];
		byte[] bytes = new byte[2 * NUM_SAMPLES];

		run = true;

		while (run) {
			int samplesToWrite = line.available() / 2;

			if (samplesToWrite > 0) {
				fillBuffer(buffer, bytes, samplesToWrite);
				int writtenBytes = line.write(bytes, 0, samplesToWrite * 2);
				while (writtenBytes != samplesToWrite * 2)
					writtenBytes += line.write(bytes, writtenBytes, samplesToWrite - writtenBytes);
			}

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void fillBuffer (float[] buffer, byte[] bytes, int samplesToWrite) {
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = 0.0f;
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = 0;

		int numBuffers = buffers.size();
		synchronized (this) {
			Iterator<LwjglSoundBuffer> bufferIter = buffers.iterator();
			while (bufferIter.hasNext()) {
				LwjglSoundBuffer soundBuffer = bufferIter.next();
				if (!soundBuffer.writeSamples(samplesToWrite, buffer)) bufferIter.remove();
			}
		}
		if (numBuffers > 0) {
			for (int i = 0, j = 0; i < samplesToWrite; i++, j += 2) {
				float fValue = buffer[i];
				if (fValue > 1) fValue = 1;
				if (fValue < -1) fValue = -1;
				short value = (short)(fValue * Short.MAX_VALUE);
				bytes[j] = (byte)(value | 0xff);
				bytes[j + 1] = (byte)(value >> 8);
			}
		}
	}

	public AudioRecorder newAudioRecoder (int samplingRate, boolean isMono) {
		return new LwjglAudioRecorder(samplingRate, isMono);
	}

	void dispose () {
		run = false;
		try {
			if (thread != null) thread.join();
			if (line != null) line.close();
		} catch (InterruptedException e) {
		}
	}
}

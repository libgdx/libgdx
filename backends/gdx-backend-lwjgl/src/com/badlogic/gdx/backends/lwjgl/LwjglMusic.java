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

package com.badlogic.gdx.backends.lwjgl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

final class LwjglMusic implements Music, Runnable {
	private final int Playing = 0;
	private final int Stopped = 1;
	private final int Paused = 2;

	private AtomicInteger state = new AtomicInteger(Stopped);
	private final Thread thread;
	private final FileHandle handle;
	private AudioInputStream ain;
	private final SourceDataLine line;
	private final byte[] buffer;
	private AtomicBoolean looping = new AtomicBoolean(false);
	private boolean disposed = false;

	public LwjglMusic (FileHandle handle) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		this.handle = handle;

		openAudioInputStream();
		AudioFormat audioFormat = ain.getFormat();
		line = AudioSystem.getSourceDataLine(audioFormat);
		line.open(audioFormat); // FIXME reduce latency, gotta reimplement the playback thread.
		line.start();
		buffer = new byte[10000 * ain.getFormat().getFrameSize()];
		ain.close();
		ain = null;

		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	private void openAudioInputStream () throws UnsupportedAudioFileException, IOException {
		ain = AudioSystem.getAudioInputStream(new BufferedInputStream(handle.read()));
		AudioFormat baseFormat = ain.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
			baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

		ain = AudioSystem.getAudioInputStream(decodedFormat, ain);
	}

	@Override public void dispose () {
		disposed = true;
		try {
			thread.join();
			line.close();
			ain.close();
		} catch (Exception e) {
			// nothing we can do here
		}
	}

	@Override public boolean isLooping () {
		return looping.get();
	}

	@Override public boolean isPlaying () {
		return state.get() == Playing;
	}

	@Override public void pause () {
		state.compareAndSet(Playing, Paused);
	}

	@Override public void play () {
		state.set(Playing);
	}

	@Override public void stop () {
		state.set(Stopped);
	}

	@Override public void setLooping (boolean isLooping) {
		looping.set(isLooping);
	}

	@Override public void setVolume (float volume) {
		try {
			volume = Math.min(1, volume);
			volume = Math.max(0, volume);
			FloatControl control = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			control.setValue(-80 + volume * 80);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}
	}

	@Override public void run () {
		int readBytes = 0;
		long readSamples = 0;

		while (!disposed) {
			int curState = state.get();
			if (curState == Playing) {
				try {
					if (ain == null) openAudioInputStream();
					readBytes = ain.read(buffer);

					if (readBytes != -1) {
						int writtenBytes = line.write(buffer, 0, readBytes);
						while (writtenBytes != readBytes)
							writtenBytes += line.write(buffer, writtenBytes, readBytes - writtenBytes);
						readSamples += readBytes / ain.getFormat().getFrameSize();
					} else {
						System.out.println("samples: " + readSamples);
						ain.close();
						if (!isLooping()) state.set(Stopped);
						else openAudioInputStream();
					}
				} catch (Exception ex) {
					try {
						ain.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					line.close();
					ex.printStackTrace();
					state.set(Stopped);
					return;
				}
			}
			else if (curState == Stopped && ain != null)
			{
				try {
					ain.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ain = null;
				line.flush();
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

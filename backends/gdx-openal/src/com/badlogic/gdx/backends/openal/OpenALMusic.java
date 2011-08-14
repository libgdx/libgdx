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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static org.lwjgl.openal.AL10.*;

/** @author Nathan Sweet */
public abstract class OpenALMusic implements Music {
	static private final int bufferSize = 4096 * 10;
	static private final int bufferCount = 3;
	static private final int bytesPerSample = 2;
	static private final byte[] tempBytes = new byte[bufferSize];
	static private final ByteBuffer tempBuffer = BufferUtils.createByteBuffer(bufferSize);

	private final OpenALAudio audio;
	private IntBuffer buffers;
	private int sourceID = -1;
	private int format, sampleRate;
	private boolean isLooping, isPlaying;
	private float volume = 1;
	private float renderedSeconds, secondsPerBuffer;

	protected final FileHandle file;

	public OpenALMusic (OpenALAudio audio, FileHandle file) {
		this.audio = audio;
		this.file = file;

		audio.music.add(this);
	}

	protected void setup (int channels, int sampleRate) {
		this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
		this.sampleRate = sampleRate;
		secondsPerBuffer = (float)bufferSize / bytesPerSample / channels / sampleRate;
	}

	public void play () {
		if (sourceID == -1) {
			sourceID = audio.obtainSource(true);
			if (sourceID == -1) return;
			if (buffers == null) {
				buffers = BufferUtils.createIntBuffer(bufferCount);
				alGenBuffers(buffers);
				if (alGetError() != AL_NO_ERROR) throw new GdxRuntimeException("Unabe to allocate audio buffers.");
			}
			alSourcei(sourceID, AL_LOOPING, AL_FALSE);
			alSourcef(sourceID, AL_GAIN, volume);
			for (int i = 0; i < bufferCount; i++) {
				int bufferID = buffers.get(i);
				if (!fill(bufferID)) break;
				alSourceQueueBuffers(sourceID, bufferID);
			}
			if (alGetError() != AL_NO_ERROR) {
				stop();
				return;
			}
		}
		alSourcePlay(sourceID);
		isPlaying = true;
	}

	public void stop () {
		if (sourceID == -1) return;
		reset();
		audio.freeSource(sourceID);
		sourceID = -1;
		renderedSeconds = 0;
		isPlaying = false;
	}

	public void pause () {
		if (sourceID != -1) alSourcePause(sourceID);
		isPlaying = false;
	}

	public boolean isPlaying () {
		if (sourceID == -1) return false;
		return isPlaying;
	}

	public void setLooping (boolean isLooping) {
		this.isLooping = isLooping;
	}

	public boolean isLooping () {
		return isLooping;
	}

	public void setVolume (float volume) {
		this.volume = volume;
		if (sourceID != -1) alSourcef(sourceID, AL_GAIN, volume);
	}

	public float getPosition () {
		if (sourceID == -1) return 0;
		return renderedSeconds + alGetSourcef(sourceID, AL11.AL_SEC_OFFSET);
	}

	/** Fills as much of the buffer as possible and returns the number of bytes filled. Returns <= 0 to indicate the end of the
	 * stream. */
	abstract protected int read (byte[] buffer);

	/** Resets the stream to the beginning. */
	abstract protected void reset ();

	public void update () {
		if (sourceID == -1) return;

		boolean end = false;
		int buffers = alGetSourcei(sourceID, AL_BUFFERS_PROCESSED);
		while (buffers-- > 0) {
			int bufferID = alSourceUnqueueBuffers(sourceID);
			if (bufferID == AL_INVALID_VALUE) break;
			renderedSeconds += secondsPerBuffer;
			if (end) continue;
			if (fill(bufferID))
				alSourceQueueBuffers(sourceID, bufferID);
			else
				end = true;
		}
		if (end && alGetSourcei(sourceID, AL_BUFFERS_QUEUED) == 0) stop();

		// A buffer underflow will cause the source to stop.
		if (isPlaying && alGetSourcei(sourceID, AL_SOURCE_STATE) != AL_PLAYING) alSourcePlay(sourceID);
	}

	private boolean fill (int bufferID) {
		tempBuffer.clear();
		int length = read(tempBytes);
		if (length <= 0) {
			if (isLooping) {
				reset();
				renderedSeconds = 0;
				length = read(tempBytes);
				if (length <= 0) return false;
			} else
				return false;
		}
		tempBuffer.put(tempBytes, 0, length).flip();
		alBufferData(bufferID, format, tempBuffer, sampleRate);
		return true;
	}

	public void dispose () {
		if (buffers == null) return;
		if (sourceID != -1) {
			reset();
			audio.music.removeValue(this, true);
			audio.freeSource(sourceID);
			sourceID = -1;
		}
		alDeleteBuffers(buffers);
		buffers = null;
	}
}

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

package com.badlogic.gdx.backends.lwjgl3.audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
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
	private float pan = 0;
	private float renderedSeconds, secondsPerBuffer;

	protected final FileHandle file;
	protected int bufferOverhead = 0;

	private OnCompletionListener onCompletionListener;

	public OpenALMusic (OpenALAudio audio, FileHandle file) {
		this.audio = audio;
		this.file = file;
		this.onCompletionListener = null;
	}

	protected void setup (int channels, int sampleRate) {
		this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
		this.sampleRate = sampleRate;
		secondsPerBuffer = (float)(bufferSize - bufferOverhead)  / (bytesPerSample * channels * sampleRate);
	}

	public void play () {
		if (audio.noDevice) return;
		if (sourceID == -1) {
			sourceID = audio.obtainSource(true);
			if (sourceID == -1) return;

			audio.music.add(this);

			if (buffers == null) {
				buffers = BufferUtils.createIntBuffer(bufferCount);
				alGenBuffers(buffers);
				if (alGetError() != AL_NO_ERROR) throw new GdxRuntimeException("Unable to allocate audio buffers.");
			}
			alSourcei(sourceID, AL_LOOPING, AL_FALSE);
			setPan(pan, volume);

			boolean filled = false; // Check if there's anything to actually play.
			for (int i = 0; i < bufferCount; i++) {
				int bufferID = buffers.get(i);
				if (!fill(bufferID)) break;
				filled = true;
				alSourceQueueBuffers(sourceID, bufferID);
			}
			if (!filled && onCompletionListener != null) onCompletionListener.onCompletion(this);

			if (alGetError() != AL_NO_ERROR) {
				stop();
				return;
			}
		}
		if (!isPlaying) {
			alSourcePlay(sourceID);
			isPlaying = true;
		}
	}

	public void stop () {
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		audio.music.removeValue(this, true);
		reset();
		audio.freeSource(sourceID);
		sourceID = -1;
		renderedSeconds = 0;
		isPlaying = false;
	}

	public void pause () {
		if (audio.noDevice) return;
		if (sourceID != -1) alSourcePause(sourceID);
		isPlaying = false;
	}

	public boolean isPlaying () {
		if (audio.noDevice) return false;
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
		if (audio.noDevice) return;
		if (sourceID != -1) alSourcef(sourceID, AL_GAIN, volume);
	}

	public float getVolume () {
		return this.volume;
	}

	public void setPan (float pan, float volume) {
		this.volume = volume;
		this.pan = pan;
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		alSource3f(sourceID, AL_POSITION, MathUtils.cos((pan - 1) * MathUtils.PI / 2), 0,
			MathUtils.sin((pan + 1) * MathUtils.PI / 2));
		alSourcef(sourceID, AL_GAIN, volume);
	}

	public void setPosition (float position) {
		if (audio.noDevice) return;
		if (sourceID == -1) return;
		boolean wasPlaying = isPlaying;
		isPlaying = false;
		alSourceStop(sourceID);
		alSourceUnqueueBuffers(sourceID, buffers);
		renderedSeconds += (secondsPerBuffer * bufferCount);
		if (position <= renderedSeconds) {
			reset();
			renderedSeconds = 0;
		}
		while (renderedSeconds < (position - secondsPerBuffer)) {
			if (read(tempBytes) <= 0) break;
			renderedSeconds += secondsPerBuffer;
		}
		boolean filled = false;
		for (int i = 0; i < bufferCount; i++) {
			int bufferID = buffers.get(i);
			if (!fill(bufferID)) break;
			filled = true;
			alSourceQueueBuffers(sourceID, bufferID);
		}
		if (!filled) {
			stop();
			if (onCompletionListener != null) onCompletionListener.onCompletion(this);
		}
		alSourcef(sourceID, AL11.AL_SEC_OFFSET, position - renderedSeconds);
		if (wasPlaying) {
			alSourcePlay(sourceID);
			isPlaying = true;
		}
	}

	public float getPosition () {
		if (audio.noDevice) return 0;
		if (sourceID == -1) return 0;
		return renderedSeconds + alGetSourcef(sourceID, AL11.AL_SEC_OFFSET);
	}

	/** Fills as much of the buffer as possible and returns the number of bytes filled. Returns <= 0 to indicate the end of the
	 * stream. */
	abstract public int read (byte[] buffer);

	/** Resets the stream to the beginning. */
	abstract public void reset ();

	/** By default, does just the same as reset(). Used to add special behaviour in Ogg.Music. */
	protected void loop () {
		reset();
	}

	public int getChannels () {
		return format == AL_FORMAT_STEREO16 ? 2 : 1;
	}

	public int getRate () {
		return sampleRate;
	}

	public void update () {
		if (audio.noDevice) return;
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
		if (end && alGetSourcei(sourceID, AL_BUFFERS_QUEUED) == 0) {
			stop();
			if (onCompletionListener != null) onCompletionListener.onCompletion(this);
		}

		// A buffer underflow will cause the source to stop.
		if (isPlaying && alGetSourcei(sourceID, AL_SOURCE_STATE) != AL_PLAYING) alSourcePlay(sourceID);
	}

	private boolean fill (int bufferID) {
		tempBuffer.clear();
		int length = read(tempBytes);
		if (length <= 0) {
			if (isLooping) {
				loop();
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
		stop();
		if (audio.noDevice) return;
		if (buffers == null) return;
		alDeleteBuffers(buffers);
		buffers = null;
		onCompletionListener = null;
	}

	public void setOnCompletionListener (OnCompletionListener listener) {
		onCompletionListener = listener;
	}

	public int getSourceId () {
		return sourceID;
	}
}

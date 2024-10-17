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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

/** @author Nathan Sweet */
public class OpenALSound implements Sound {
	private int bufferID = -1;
	private final OpenALLwjgl3Audio audio;
	private float duration;
	private int sampleRate, channels;
	private String type;

	public OpenALSound (OpenALLwjgl3Audio audio) {
		this.audio = audio;
	}

	/** Prepare our sound for playback!
	 * @param pcm Byte array of raw PCM data to be played.
	 * @param channels The number of channels for the sound. Most commonly 1 (for mono) or 2 (for stereo).
	 * @param bitDepth The number of bits in each sample. Normally 16. Can also be 8, 32, 64.
	 * @param sampleRate The number of samples to be played each second. Commonly 44100; can be anything within reason. */
	void setup (byte[] pcm, int channels, int bitDepth, int sampleRate) {
		int validBytes = pcm.length - (pcm.length % (channels * (bitDepth >> 3)));
		ByteBuffer buffer = BufferUtils.newByteBuffer(validBytes);
		buffer.put(pcm, 0, validBytes);
		((Buffer)buffer).flip();

		setup(buffer.asShortBuffer(), channels, bitDepth, sampleRate);
	}

	void setup (ShortBuffer pcm, int channels, int bitDepth, int sampleRate) {
		this.channels = channels;
		this.sampleRate = sampleRate;
		int sampleFrames = (pcm.limit() << 1) / (bitDepth >> 3) / channels;
		duration = sampleFrames / (float)sampleRate;

		if (bufferID == -1) {
			bufferID = alGenBuffers();
			int format = OpenALUtils.determineFormat(channels, bitDepth);
			alBufferData(bufferID, format, pcm, sampleRate);
		}
	}

	public long play () {
		return play(1);
	}

	public long play (float volume) {
		if (audio.noDevice) return 0;
		int sourceID = audio.obtainSource(false);
		if (sourceID == -1) {
			// Attempt to recover by stopping the least recently played sound
			audio.retain(this, true);
			sourceID = audio.obtainSource(false);
		} else
			audio.retain(this, false);
		// In case it still didn't work
		if (sourceID == -1) return -1;
		long soundId = audio.getSoundId(sourceID);
		alSourcei(sourceID, AL_BUFFER, bufferID);
		alSourcei(sourceID, AL_LOOPING, AL_FALSE);
		alSourcef(sourceID, AL_GAIN, volume);
		alSourcePlay(sourceID);
		return soundId;
	}

	public long loop () {
		return loop(1);
	}

	@Override
	public long loop (float volume) {
		if (audio.noDevice) return 0;
		int sourceID = audio.obtainSource(false);
		if (sourceID == -1) return -1;
		long soundId = audio.getSoundId(sourceID);
		alSourcei(sourceID, AL_BUFFER, bufferID);
		alSourcei(sourceID, AL_LOOPING, AL_TRUE);
		alSourcef(sourceID, AL_GAIN, volume);
		alSourcePlay(sourceID);
		return soundId;
	}

	public void stop () {
		if (audio.noDevice) return;
		audio.stopSourcesWithBuffer(bufferID);
	}

	public void dispose () {
		if (audio.noDevice) return;
		if (bufferID == -1) return;
		audio.freeBuffer(bufferID);
		alDeleteBuffers(bufferID);
		bufferID = -1;
		audio.forget(this);
	}

	@Override
	public void stop (long soundId) {
		if (audio.noDevice) return;
		audio.stopSound(soundId);
	}

	@Override
	public void pause () {
		if (audio.noDevice) return;
		audio.pauseSourcesWithBuffer(bufferID);
	}

	@Override
	public void pause (long soundId) {
		if (audio.noDevice) return;
		audio.pauseSound(soundId);
	}

	@Override
	public void resume () {
		if (audio.noDevice) return;
		audio.resumeSourcesWithBuffer(bufferID);
	}

	@Override
	public void resume (long soundId) {
		if (audio.noDevice) return;
		audio.resumeSound(soundId);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		if (audio.noDevice) return;
		audio.setSoundPitch(soundId, pitch);
	}

	@Override
	public void setVolume (long soundId, float volume) {
		if (audio.noDevice) return;
		audio.setSoundGain(soundId, volume);
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		if (audio.noDevice) return;
		audio.setSoundLooping(soundId, looping);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		if (audio.noDevice) return;
		audio.setSoundPan(soundId, pan, volume);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		long id = play();
		setPitch(id, pitch);
		setPan(id, pan, volume);
		return id;
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		long id = loop();
		setPitch(id, pitch);
		setPan(id, pan, volume);
		return id;
	}

	/** Returns the length of the sound in seconds. */
	public float duration () {
		return duration;
	}

	/** Returns the original sample rate of the sound in Hz. */
	public int getRate () {
		return sampleRate;
	}

	/** Returns the number of channels of the sound. Usually 1 for mono; 2 for stereo. */
	public int getChannels () {
		return channels;
	}

	/** @param type The type of audio, such as mp3, ogg or wav. */
	public void setType (String type) {
		this.type = type;
	}

	public String getType () {
		return type;
	}
}

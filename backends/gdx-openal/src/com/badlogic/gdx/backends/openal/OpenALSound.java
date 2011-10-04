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
import java.nio.ByteOrder;

import com.badlogic.gdx.audio.Sound;

import static org.lwjgl.openal.AL10.*;

/** @author Nathan Sweet */
public class OpenALSound implements Sound {
	private int bufferID = -1;
	private final OpenALAudio audio;

	public OpenALSound (OpenALAudio audio) {
		this.audio = audio;
	}

	void setup (byte[] pcm, int channels, int sampleRate) {
		int bytes = pcm.length - (pcm.length % (channels > 1 ? 4 : 2));
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes);
		buffer.order(ByteOrder.nativeOrder());
		buffer.put(pcm, 0, bytes);
		buffer.flip();

		if (bufferID == -1) {
			bufferID = alGenBuffers();
			alBufferData(bufferID, channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, buffer.asShortBuffer(), sampleRate);
		}
	}

	public long play () {
		return play(1);
	}

	public long play (float volume) {
		int sourceID = audio.obtainSource(false);
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
		audio.stopSourcesWithBuffer(bufferID);
	}

	public void dispose () {
		if (bufferID == -1) return;
		audio.freeBuffer(bufferID);
		alDeleteBuffers(bufferID);
		bufferID = -1;
	}

	@Override
	public void stop (long soundId) {
		audio.stopSound(soundId);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		audio.setSoundPitch(soundId, pitch);
	}

	@Override
	public void setVolume (long soundId, float volume) {
		audio.setSoundGain(soundId, volume);
	}


	@Override
	public void setLooping (long soundId, boolean looping) {
		audio.setSoundLooping(soundId, looping);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		audio.setSoundPan(soundId, pan, volume);
	}
}

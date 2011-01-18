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

package com.baglogic.gdx.openal;

import java.nio.ByteBuffer;

import com.badlogic.gdx.audio.Sound;

import static org.lwjgl.openal.AL10.*;

/**
 * @author Nathan Sweet
 */
public class OpenALSound implements Sound {
	private int bufferID = -1;
	private final OpenALAudio audio;
	private int streamID;

	public OpenALSound (OpenALAudio audio) {
		this.audio = audio;
	}

	void setup (byte[] pcm, int channels, int sampleRate) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length);
		buffer.put(pcm);
		buffer.flip();

		if (bufferID == -1) {
			bufferID = alGenBuffers();
			alBufferData(bufferID, channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, buffer, sampleRate);
		}
	}

	public void play () {
		play(1);
	}

	public void play (float volume) {
		streamID = audio.getIdleStreamID();
		if (streamID == -1) return;
		alSourceStop(streamID);
		alSourcei(streamID, AL_BUFFER, bufferID);
		alSourcei(streamID, AL_LOOPING, AL_FALSE);
		alSourcef(streamID, AL_GAIN, volume);
		alSourcePlay(streamID);
	}

	public void loop () {
		streamID = audio.getIdleStreamID();
		if (streamID == -1) return;
		alSourceStop(streamID);
		alSourcei(streamID, AL_BUFFER, bufferID);
		alSourcei(streamID, AL_LOOPING, AL_TRUE);
		alSourcePlay(streamID);
	}

	public void stop () {
		alSourceStop(streamID);
	}

	public void dispose () {
		alDeleteBuffers(bufferID);
	}
}

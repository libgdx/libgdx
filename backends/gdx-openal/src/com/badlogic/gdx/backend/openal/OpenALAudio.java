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

package com.badlogic.gdx.backend.openal;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

import static org.lwjgl.openal.AL10.*;

/**
 * @author Nathan Sweet
 */
public class OpenALAudio implements Audio {
	private int[] streams;

	Array<OpenALMusic> music = new Array(false, 1, OpenALMusic.class);

	public OpenALAudio () {
		this(16);
	}

	public OpenALAudio (int simultaneousStreams) {
		try {
			AL.create();
		} catch (LWJGLException ex) {
			throw new GdxRuntimeException("Error initializing OpenAL.", ex);
		}

		IntArray streams = new IntArray(false, simultaneousStreams);
		for (int i = 0; i < simultaneousStreams; i++) {
			int streamID = alGenSources();
			if (alGetError() != AL_NO_ERROR) break;
			streams.add(streamID);
		}
		this.streams = streams.items;

		FloatBuffer orientation = (FloatBuffer)BufferUtils.createFloatBuffer(6)
			.put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
		alListener(AL_ORIENTATION, orientation);
		FloatBuffer velocity = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		alListener(AL_VELOCITY, velocity);
		FloatBuffer position = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		alListener(AL_POSITION, position);
	}

	public OpenALSound newSound (FileHandle file) {
		String extension = file.extension();
		if (extension.equals("ogg")) {
			return new Ogg.Sound(this, file);
		} else if (extension.equals("mp3")) {
			return new Mp3.Sound(this, file);
		} else if (extension.equals("wav")) {
			return new Wav.Sound(this, file);
		}
		throw new GdxRuntimeException("Unknown file extension for sound: " + file);
	}

	public OpenALMusic newMusic (FileHandle file) {
		String extension = file.extension();
		if (extension.equals("ogg")) {
			return new Ogg.Music(this, file);
		} else if (extension.equals("mp3")) {
			return new Mp3.Music(this, file);
		} else if (extension.equals("wav")) {
			return new Wav.Music(this, file);
		}
		throw new GdxRuntimeException("Unknown file extension for music: " + file);
	}

	int getIdleStreamID () {
		for (int i = 0, n = streams.length; i < n; i++) {
			int streamID = streams[i];
			int state = alGetSourcei(streamID, AL_SOURCE_STATE);
			if (state != AL_PLAYING && state != AL_PAUSED) return streamID;
		}
		return -1;
	}

	public void update () {
		for (int i = 0; i < music.size; i++)
			music.items[i].update();
	}

	public void dispose () {
		for (int i = 0, n = streams.length; i < n; i++) {
			int streamID = streams[i];
			int state = alGetSourcei(streamID, AL_SOURCE_STATE);
			if (state != AL_STOPPED) alSourceStop(streamID);
			alDeleteSources(streamID);
		}
	}

	public AudioDevice newAudioDevice (boolean isMono) {
		return new JavaSoundAudioDevice(isMono);
	}

	public AudioRecorder newAudioRecoder (int samplingRate, boolean isMono) {
		return new JavaSoundAudioRecorder(samplingRate, isMono);
	}
}

package com.badlogic.gdx.backends.headless.mock.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class MockAudio implements Audio {

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		return new MockAudioDevice();
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		return new MockAudioRecorder();
	}

	@Override
	public Sound newSound(FileHandle fileHandle) {
		return new MockSound();
	}

	@Override
	public Music newMusic(FileHandle file) {
		return new MockMusic();
	}
}

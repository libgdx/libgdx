package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.GWT;

public class GwtAudio implements Audio {
	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("AudioDevice not supported by GWT backend");
	}

	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("AudioRecorder not supported by GWT backend");
	}

	@Override
	public Sound newSound (FileHandle fileHandle) {
		return new GwtSound(fileHandle);
	}

	@Override
	public Music newMusic (FileHandle file) {
		return new GwtMusic(file);
	}
}

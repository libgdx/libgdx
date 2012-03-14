package com.badlogic.gdx.backends.gwt;

import com.allen_sauer.gwt.voices.client.SoundController;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GwtAudio implements Audio {
	SoundController soundController;
	
	public GwtAudio() {
		soundController = new SoundController();
		soundController.setGwtVoicesSwfLocation(((GwtApplication)Gdx.app).getBaseUrl());
	}
	
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
		return new GwtSound(soundController, fileHandle);
	}

	@Override
	public Music newMusic (FileHandle file) {
		return new GwtMusic(soundController, file);
	}
}

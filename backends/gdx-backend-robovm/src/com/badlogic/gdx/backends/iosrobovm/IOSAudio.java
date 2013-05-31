package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IOSAudio implements Audio {

	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sound newSound (FileHandle fileHandle) {
		// TODO Auto-generated method stub
		return new IOSSound();
	}

	@Override
	public Music newMusic (FileHandle file) {
		// TODO Auto-generated method stub
		return new IOSMusic();
	}

}
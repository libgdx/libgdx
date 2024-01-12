
package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DisabledAndroidAudio implements AndroidAudio {

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void notifyMusicDisposed (AndroidMusic music) {
	}

	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("Android audio is not enabled by the application config");
	}

	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("Android audio is not enabled by the application config");
	}

	@Override
	public Sound newSound (FileHandle fileHandle) {
		throw new GdxRuntimeException("Android audio is not enabled by the application config");
	}

	@Override
	public Music newMusic (FileHandle file) {
		throw new GdxRuntimeException("Android audio is not enabled by the application config");
	}

	@Override
	public boolean switchOutputDevice (String deviceIdentifier) {
		return false;
	}

	@Override
	public String[] getAvailableOutputDevices () {
		return new String[0];
	}

	@Override
	public void dispose () {
	}
}

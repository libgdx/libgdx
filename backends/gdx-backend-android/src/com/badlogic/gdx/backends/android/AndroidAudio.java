package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.utils.Disposable;

public interface AndroidAudio extends Audio, Disposable {

	/** Pauses all playing sounds and musics **/
	void pause();

	/** Resumes all playing sounds and musics **/
	void resume();

	/** Notifies the AndroidAudio if an AndroidMusic is disposed **/
	void notifyMusicDisposed (AndroidMusic music);
}

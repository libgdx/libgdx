
package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Audio;

public interface AndroidAudio extends Audio {

	/** Pauses all playing sounds and musics **/
	void pause ();

	/** Resumes all playing sounds and musics **/
	void resume ();

	/** Notifies the AndroidAudio if an AndroidMusic is disposed **/
	void notifyMusicDisposed (AndroidMusic music);
}

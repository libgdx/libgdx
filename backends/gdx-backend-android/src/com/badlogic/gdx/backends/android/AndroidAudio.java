
package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Audio;

public interface AndroidAudio extends Audio {

	/** Notifies the AndroidAudio if an AndroidMusic is disposed **/
	void notifyMusicDisposed (AndroidMusic music);
}

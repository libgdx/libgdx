package com.badlogic.gdx.backends.lwjgl.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.utils.Disposable;

public interface LwjglAudio extends Audio, Disposable {

	void update();
}


package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.lwjgl.audio.LwjglAudio;

public interface LwjglApplicationBase extends Application {

	LwjglAudio createAudio (LwjglApplicationConfiguration config);

	LwjglInput createInput (LwjglApplicationConfiguration config);
}

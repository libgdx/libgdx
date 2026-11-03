
package com.badlogic.gdx.backends.sdl3;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.sdl3.audio.Lwjgl3Audio;

public interface Lwjgl3ApplicationBase extends Application {

	Lwjgl3Audio createAudio (Lwjgl3ApplicationConfiguration config);

	Lwjgl3Input createInput (Lwjgl3Window window);
}

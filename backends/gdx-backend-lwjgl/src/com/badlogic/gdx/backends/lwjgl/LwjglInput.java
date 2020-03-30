package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.Input;

public interface LwjglInput extends Input {

	void update();

	void processEvents();
}

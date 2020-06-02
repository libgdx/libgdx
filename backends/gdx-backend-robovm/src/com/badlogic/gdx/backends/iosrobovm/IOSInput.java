package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Input;

public interface IOSInput extends Input {

	/** Initializes peripherals (such as compass or accelerometer) */
	void setupPeripherals();

	/** Records touch events */
	void onTouch(long touches);

	/** Process all touch events that have been registered on #onTouch(). */
	void processEvents();
}

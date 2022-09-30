
package com.badlogic.gdx.backends.iosmoe;

import apple.foundation.NSSet;
import apple.uikit.UITouch;
import com.badlogic.gdx.Input;
import apple.uikit.UIKey;

public interface IOSInput extends Input {

	/** Initializes peripherals (such as compass or accelerometer) */
	void setupPeripherals ();

	/** Records touch events */
	void onTouch (NSSet<? extends UITouch> touches);

	/** Process all touch events that have been registered on #onTouch(). */
	void processEvents ();

	boolean onKey (UIKey key, boolean down);
}

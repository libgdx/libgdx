
package com.badlogic.gdx.backends.android.keyboardheight;

public interface KeyboardHeightProvider {

	void start ();

	void close ();

	void setKeyboardHeightObserver (KeyboardHeightObserver observer);

	int getKeyboardLandscapeHeight ();

	int getKeyboardPortraitHeight ();
}

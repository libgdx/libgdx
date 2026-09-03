
package com.badlogic.gdx.backends.iosrobovm.keyboard;

public interface KeyboardHeightProvider {

	/** The observer that will be notified when the keyboard height changes. */
	public interface IOSKeyboardObserver {
		/** Pushed on the frame after the keyboard notification, inside a UIView animation block matching the keyboard's own
		 * transition.
		 * @param opened whether an on-screen keyboard is on screen
		 * @param height keyboard height in screen points; 0 when closed */
		void onKeyboardHeightChanged (boolean opened, double height);
	}

	void start ();

	void close ();

	void setKeyboardHeightObserver (IOSKeyboardObserver observer);

	double getKeyboardLandscapeHeight ();

	double getKeyboardPortraitHeight ();
}

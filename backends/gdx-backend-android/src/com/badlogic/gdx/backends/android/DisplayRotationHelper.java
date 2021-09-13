
package com.badlogic.gdx.backends.android;

public interface DisplayRotationHelper {

	boolean getAndResetRefreshNeeded ();

	void onSurfaceChanged ();

	void onResume ();

	void onPause ();
}

package com.badlogic.gdx.backends.android;

import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import com.badlogic.gdx.Input;

public interface AndroidInput extends Input, OnTouchListener, OnKeyListener, OnGenericMotionListener {

	/** Called when the {@link AndroidApplication} is about to pause */
	void onPause();

	/** Called when the {@link AndroidApplication} is about to resume */
	void onResume();

	/** Called when an {@link AndroidDaydream} is about to start */
	void onDreamingStarted();

	/** Called when an {@link AndroidDaydream} is about to stop */
	void onDreamingStopped();

	/** Add a custom {@link OnKeyListener} */
	void addKeyListener (OnKeyListener listener);

	/** Add a custom {@link OnGenericMotionListener} */
	void addGenericMotionListener (OnGenericMotionListener listener);

	/** Process all events (touch, key) that have been registered by the listeners so that event processing is synchronous. */
	void processEvents();

	/** Shows/Hides soft screen keyboard */
	void setKeyboardAvailable(boolean available);

}

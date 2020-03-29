package com.badlogic.gdx.backends.android;

import android.view.MotionEvent;
import android.view.View;
import com.badlogic.gdx.Input;

public interface AndroidInput extends Input {

	void onPause();

	void onResume();

	void onDreamingStarted();

	void onDreamingStopped();

	boolean onTouch(View view, MotionEvent motionEvent);

	void addKeyListener (View.OnKeyListener listener);

	void processEvents();

	void setKeyboardAvailable(boolean available);

}

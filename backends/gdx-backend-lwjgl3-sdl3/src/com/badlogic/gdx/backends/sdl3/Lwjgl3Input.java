
package com.badlogic.gdx.backends.sdl3;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Disposable;

public interface Lwjgl3Input extends Input, Disposable {

	void windowHandleChanged (long windowHandle);

	void update ();

	void prepareNext ();

	void resetPollingStates ();

	void charCallback (long window, int codepoint);

	void scrollCallback (long window, double scrollX, double scrollY);

	void cursorPosCallback (long windowHandle, double x, double y);

	void mouseButtonCallback (long window, int button, boolean down);

	void keyCallback (long window, int key, int scancode, int mods, boolean repeat, boolean down);
}

package com.badlogic.gdx.backends.lwjgl3;

// FIXME wire this up with multi-window api
// as a parameter to app#newWindow
public interface Lwjgl3WindowListener {
	void minimized();
	void maximized(); // really need this?	
	void focusGained();
	void focusLost();
	/** @return whether the window should actually close **/
	boolean windowIsClosing();
}

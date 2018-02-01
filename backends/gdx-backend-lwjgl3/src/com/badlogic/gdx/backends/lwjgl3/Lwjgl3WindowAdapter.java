package com.badlogic.gdx.backends.lwjgl3;

/**
 * Convenience implementation of {@link Lwjgl3WindowListener}. Derive from this class
 * and only overwrite the methods you are interested in.
 * @author badlogic
 *
 */
public class Lwjgl3WindowAdapter implements Lwjgl3WindowListener {
	@Override
	public void created(Lwjgl3Window window) {
	}

	@Override
	public void iconified(boolean isIconified) {
	}

	@Override
	public void maximized(boolean isMaximized) {
	}

	@Override
	public void focusLost() {
	}

	@Override
	public void focusGained() {
	}

	@Override
	public boolean closeRequested() {
		return true;
	}
	
	@Override
	public void filesDropped(String[] files) {	
	}

	@Override
	public void refreshRequested() {
	}
}

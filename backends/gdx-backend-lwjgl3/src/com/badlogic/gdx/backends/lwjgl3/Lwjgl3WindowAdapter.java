package com.badlogic.gdx.backends.lwjgl3;

/**
 * Convenience implementation of {@link Lwjgl3WindowListener}. Derive from this class
 * and only overwrite the methods you are interested in.
 * @author badlogic
 *
 */
public class Lwjgl3WindowAdapter implements Lwjgl3WindowListener {
	@Override
	public void iconified() {
	}

	@Override
	public void deiconified() {
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
}

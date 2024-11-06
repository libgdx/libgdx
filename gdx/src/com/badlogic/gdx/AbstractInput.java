
package com.badlogic.gdx;

import com.badlogic.gdx.utils.IntSet;

/** Abstract implementation of the Input interface, providing functionalities for handling key presses, including checking if keys
 * are currently pressed or have just been pressed. This class is intended to be extended by platform-specific input
 * implementations. */

public abstract class AbstractInput implements Input {

	/** Array to store the state of keys that are currently pressed. */
	protected final boolean[] pressedKeys;

	/** Array to store the state of keys that have just been pressed */
	protected final boolean[] justPressedKeys;

	/** Set of key codes for which input events should be intercepted or "caught". */
	private final IntSet keysToCatch = new IntSet();

	/** Counter for the number of currently pressed keys. */
	protected int pressedKeyCount;

	/** Indicates if any key has just been pressed. */
	protected boolean keyJustPressed;

	public AbstractInput () {
		pressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
		justPressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
	}

	/** Checks if the specified key is currently pressed.
	 * 
	 * @param key the key code to check; {@link Input.Keys#ANY_KEY} checks if any key is pressed
	 * @return true if the specified key is pressed, false otherwise */
	@Override
	public boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return pressedKeyCount > 0;
		}
		if (key < 0 || key > Keys.MAX_KEYCODE) {
			return false;
		}
		return pressedKeys[key];
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyJustPressed;
		}
		if (key < 0 || key > Keys.MAX_KEYCODE) {
			return false;
		}
		return justPressedKeys[key];
	}

	/** Checks if the specified key was just pressed (i.e., pressed down in the current frame).
	 * 
	 * @param key the key code to check; {@link Input.Keys#ANY_KEY} checks if any key was just pressed
	 * @return true if the specified key was just pressed, false otherwise */
	@Override
	public void setCatchKey (int keycode, boolean catchKey) {
		if (!catchKey) {
			keysToCatch.remove(keycode);
		} else {
			keysToCatch.add(keycode);
		}
	}

	/** Checks if a specific key code is set to be caught.
	 * 
	 * @param keycode the key code to check
	 * @return true if the key code is caught, false otherwise */
	@Override
	public boolean isCatchKey (int keycode) {
		return keysToCatch.contains(keycode);
	}

}

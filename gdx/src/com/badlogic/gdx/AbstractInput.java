
package com.badlogic.gdx;

import com.badlogic.gdx.utils.IntSet;

public abstract class AbstractInput implements Input {
	protected final boolean[] pressedKeys;
	protected final boolean[] justPressedKeys;
	private final IntSet keysToCatch = new IntSet();
	protected int pressedKeyCount;
	protected boolean keyJustPressed;

	public AbstractInput () {
		pressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
		justPressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
	}

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

	@Override
	public boolean isCatchBackKey () {
		return keysToCatch.contains(Keys.BACK);
	}

	@Override
	public void setCatchBackKey (boolean catchBack) {
		setCatchKey(Keys.BACK, catchBack);
	}

	@Override
	public boolean isCatchMenuKey () {
		return keysToCatch.contains(Keys.MENU);
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
		setCatchKey(Keys.MENU, catchMenu);
	}

	@Override
	public void setCatchKey (int keycode, boolean catchKey) {
		if (!catchKey) {
			keysToCatch.remove(keycode);
		} else {
			keysToCatch.add(keycode);
		}
	}

	@Override
	public boolean isCatchKey (int keycode) {
		return keysToCatch.contains(keycode);
	}

}

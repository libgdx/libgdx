
package com.badlogic.gdx;

public class InputAdapter implements InputListener {
	public boolean keyDown (int keycode) {
		return false;
	}

	public boolean keyUp (int keycode) {
		return false;
	}

	public boolean keyTyped (char character) {
		return false;
	}

	public boolean touchDown (int x, int y, int pointer) {
		return false;
	}

	public boolean touchUp (int x, int y, int pointer) {
		return false;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}
}


package com.badlogic.gdx;

/**
 * An adapter class for {@link InputProcessor}. You can derive from this
 * and only override what you are interested in.
 * 
 * @author mzechner
 *
 */
public class InputAdapter implements InputProcessor {
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


package com.badlogic.gdx;

import java.util.ArrayList;

/**
 * An {@link InputProcessor} that delegates to an ordered list of other InputProcessors. Delegation for an event stops if a
 * processor returns true, which indicates that the event was handled.
 * @author Nathan Sweet <misc@n4te.com>
 */
public class InputMultiplexer implements InputProcessor {
	private ArrayList<InputProcessor> processors = new ArrayList(4);

	public void addProcessor (InputProcessor processor) {
		processors.add(processor);
	}

	public void removeProcessor (InputProcessor processor) {
		processors.remove(processor);
	}

	public boolean keyDown (int keycode) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).keyDown(keycode)) return true;
		return false;
	}

	public boolean keyUp (int keycode) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).keyUp(keycode)) return true;
		return false;
	}

	public boolean keyTyped (char character) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).keyTyped(character)) return true;
		return false;
	}

	public boolean touchDown (int x, int y, int pointer) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).touchDown(x, y, pointer)) return true;
		return false;
	}

	public boolean touchUp (int x, int y, int pointer) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).touchUp(x, y, pointer)) return true;
		return false;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.get(i).touchDragged(x, y, pointer)) return true;
		return false;
	}
}

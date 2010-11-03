
package com.badlogic.gdx;

import java.util.ArrayList;

public class InputMultiplexer implements InputProcessor {
	private ArrayList<InputProcessor> processors = new ArrayList(4);

	public void addProcessor (InputProcessor processor) {
		processors.add(processor);
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


package com.badlogic.gdx.twl.renderer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;

/**
 * @author Nathan Sweet <misc@n4te.com>
 */
public class TwlInputProcessor implements InputProcessor {
	private final GUI gui;

	public TwlInputProcessor (GUI gui) {
		this.gui = gui;
	}

	public boolean keyDown (int keycode) {
		keycode = getTwlKeyCode(keycode);
		return gui.handleKey(keycode, (char)0, true);
	}

	public boolean keyUp (int keycode) {
		keycode = getTwlKeyCode(keycode);
		return gui.handleKey(keycode, (char)0, false);
	}

	public boolean keyTyped (char character) {
		boolean handled = gui.handleKey(0, character, true);
		return gui.handleKey(0, character, false) || handled;
	}

	public boolean touchDown (int x, int y, int pointer) {
		return gui.handleMouse(x, y, pointer, true);
	}

	public boolean touchUp (int x, int y, int pointer) {
		return gui.handleMouse(x, y, pointer, false);
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return gui.handleMouse(x, y, -1, true);
	}

	static public int getTwlKeyCode (int gdxKeyCode) {
		if (gdxKeyCode == Input.Keys.KEYCODE_0) return Event.KEY_0;
		if (gdxKeyCode == Input.Keys.KEYCODE_1) return Event.KEY_1;
		if (gdxKeyCode == Input.Keys.KEYCODE_2) return Event.KEY_2;
		if (gdxKeyCode == Input.Keys.KEYCODE_3) return Event.KEY_3;
		if (gdxKeyCode == Input.Keys.KEYCODE_4) return Event.KEY_4;
		if (gdxKeyCode == Input.Keys.KEYCODE_5) return Event.KEY_5;
		if (gdxKeyCode == Input.Keys.KEYCODE_6) return Event.KEY_6;
		if (gdxKeyCode == Input.Keys.KEYCODE_7) return Event.KEY_7;
		if (gdxKeyCode == Input.Keys.KEYCODE_8) return Event.KEY_8;
		if (gdxKeyCode == Input.Keys.KEYCODE_9) return Event.KEY_9;
		if (gdxKeyCode == Input.Keys.KEYCODE_A) return Event.KEY_A;
		if (gdxKeyCode == Input.Keys.KEYCODE_B) return Event.KEY_B;
		if (gdxKeyCode == Input.Keys.KEYCODE_C) return Event.KEY_C;
		if (gdxKeyCode == Input.Keys.KEYCODE_D) return Event.KEY_D;
		if (gdxKeyCode == Input.Keys.KEYCODE_E) return Event.KEY_E;
		if (gdxKeyCode == Input.Keys.KEYCODE_F) return Event.KEY_F;
		if (gdxKeyCode == Input.Keys.KEYCODE_G) return Event.KEY_G;
		if (gdxKeyCode == Input.Keys.KEYCODE_H) return Event.KEY_H;
		if (gdxKeyCode == Input.Keys.KEYCODE_I) return Event.KEY_I;
		if (gdxKeyCode == Input.Keys.KEYCODE_J) return Event.KEY_J;
		if (gdxKeyCode == Input.Keys.KEYCODE_K) return Event.KEY_K;
		if (gdxKeyCode == Input.Keys.KEYCODE_L) return Event.KEY_L;
		if (gdxKeyCode == Input.Keys.KEYCODE_M) return Event.KEY_M;
		if (gdxKeyCode == Input.Keys.KEYCODE_N) return Event.KEY_N;
		if (gdxKeyCode == Input.Keys.KEYCODE_O) return Event.KEY_O;
		if (gdxKeyCode == Input.Keys.KEYCODE_P) return Event.KEY_P;
		if (gdxKeyCode == Input.Keys.KEYCODE_Q) return Event.KEY_Q;
		if (gdxKeyCode == Input.Keys.KEYCODE_R) return Event.KEY_R;
		if (gdxKeyCode == Input.Keys.KEYCODE_S) return Event.KEY_S;
		if (gdxKeyCode == Input.Keys.KEYCODE_T) return Event.KEY_T;
		if (gdxKeyCode == Input.Keys.KEYCODE_U) return Event.KEY_U;
		if (gdxKeyCode == Input.Keys.KEYCODE_V) return Event.KEY_V;
		if (gdxKeyCode == Input.Keys.KEYCODE_W) return Event.KEY_W;
		if (gdxKeyCode == Input.Keys.KEYCODE_X) return Event.KEY_X;
		if (gdxKeyCode == Input.Keys.KEYCODE_Y) return Event.KEY_Y;
		if (gdxKeyCode == Input.Keys.KEYCODE_Z) return Event.KEY_Z;
		if (gdxKeyCode == Input.Keys.KEYCODE_ALT_LEFT) return Event.KEY_LMETA;
		if (gdxKeyCode == Input.Keys.KEYCODE_ALT_RIGHT) return Event.KEY_RMETA;
		if (gdxKeyCode == Input.Keys.KEYCODE_BACKSLASH) return Event.KEY_BACKSLASH;
		if (gdxKeyCode == Input.Keys.KEYCODE_COMMA) return Event.KEY_COMMA;
		if (gdxKeyCode == Input.Keys.KEYCODE_DEL) return Event.KEY_DELETE;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_LEFT) return Event.KEY_LEFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_RIGHT) return Event.KEY_RIGHT;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_UP) return Event.KEY_UP;
		if (gdxKeyCode == Input.Keys.KEYCODE_DPAD_DOWN) return Event.KEY_DOWN;
		if (gdxKeyCode == Input.Keys.KEYCODE_ENTER) return Event.KEY_RETURN;
		if (gdxKeyCode == Input.Keys.KEYCODE_HOME) return Event.KEY_HOME;
		if (gdxKeyCode == Input.Keys.KEYCODE_MINUS) return Event.KEY_MINUS;
		if (gdxKeyCode == Input.Keys.KEYCODE_PERIOD) return Event.KEY_PERIOD;
		if (gdxKeyCode == Input.Keys.KEYCODE_PLUS) return Event.KEY_ADD;
		if (gdxKeyCode == Input.Keys.KEYCODE_SEMICOLON) return Event.KEY_SEMICOLON;
		if (gdxKeyCode == Input.Keys.KEYCODE_SHIFT_LEFT) return Event.KEY_LSHIFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_SHIFT_RIGHT) return Event.KEY_RSHIFT;
		if (gdxKeyCode == Input.Keys.KEYCODE_SLASH) return Event.KEY_SLASH;
		if (gdxKeyCode == Input.Keys.KEYCODE_SPACE) return Event.KEY_SPACE;
		if (gdxKeyCode == Input.Keys.KEYCODE_TAB) return Event.KEY_TAB;
		return Event.KEY_NONE;
	}
}

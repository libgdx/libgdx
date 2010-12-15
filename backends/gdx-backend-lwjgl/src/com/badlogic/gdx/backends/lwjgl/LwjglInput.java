/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.lwjgl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

/**
 * An implementation of the {@link Input} interface hooking a Jogl panel for input.
 * 
 * @author mzechner
 * 
 */
final class LwjglInput implements Input {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;

		int type;
		int x;
		int y;
		int pointer;
	}

	Pool<KeyEvent> freeKeyEvents = new Pool<KeyEvent>(new PoolObjectFactory<KeyEvent>() {

		@Override public KeyEvent createObject () {
			return new KeyEvent();
		}
	}, 1000);

	Pool<TouchEvent> freeTouchEvents = new Pool<TouchEvent>(new PoolObjectFactory<TouchEvent>() {

		@Override public TouchEvent createObject () {
			return new TouchEvent();
		}
	}, 1000);

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	boolean mousePressed = false;
	int mouseX = 0;
	int mouseY = 0;
	int pressedKeys = 0;

	private InputProcessor processor;

	public float getAccelerometerX () {
		return 0;
	}

	public float getAccelerometerY () {
		return 0;
	}

	public float getAccelerometerZ () {
		return 0;
	}

	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run () {
				String output = JOptionPane.showInputDialog(null, title, text);
				listener.input(output);
			}
		});
	}

	public int getX () {
		return Mouse.getX();
	}

	public int getY () {
		return Gdx.graphics.getHeight() - Mouse.getY();
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY)
			return pressedKeys > 0;
		else
			return Keyboard.isKeyDown(getLwjglKeyCode(key));
	}

	public boolean isTouched () {
		boolean button = Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2);
		return button;
	}

	public int getX (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getX();
	}

	public int getY (int pointer) {
		if (pointer > 0)
			return 0;
		else
			return getY();
	}

	public boolean isTouched (int pointer) {
		if (pointer > 0)
			return false;
		else
			return isTouched();
	}

	public boolean supportsMultitouch () {
		return false;
	}

	@Override public void setOnscreenKeyboardVisible (boolean visible) {

	}

	@Override public boolean supportsOnscreenKeyboard () {
		return false;
	}

	@Override public void setCatchBackKey (boolean catchBack) {

	}

	void processEvents () {
		synchronized (this) {
			if (processor != null) {
				InputProcessor processor = this.processor;
				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.keyDown(e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						processor.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						processor.keyTyped(e.keyChar);
					}
					freeKeyEvents.free(e);
				}

				len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					switch (e.type) {
					case TouchEvent.TOUCH_DOWN:
						processor.touchDown(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_UP:
						processor.touchUp(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						processor.touchDragged(e.x, e.y, e.pointer);
					}
					freeTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					freeTouchEvents.free(touchEvents.get(i));
				}

				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					freeKeyEvents.free(keyEvents.get(i));
				}
			}

			keyEvents.clear();
			touchEvents.clear();
		}
	}

	public static int getGdxKeyCode (int lwjglKeyCode) {
		switch (lwjglKeyCode) {
		case Keyboard.KEY_0:
			return Input.Keys.KEYCODE_0;
		case Keyboard.KEY_1:
			return Input.Keys.KEYCODE_1;
		case Keyboard.KEY_2:
			return Input.Keys.KEYCODE_2;
		case Keyboard.KEY_3:
			return Input.Keys.KEYCODE_3;
		case Keyboard.KEY_4:
			return Input.Keys.KEYCODE_4;
		case Keyboard.KEY_5:
			return Input.Keys.KEYCODE_5;
		case Keyboard.KEY_6:
			return Input.Keys.KEYCODE_6;
		case Keyboard.KEY_7:
			return Input.Keys.KEYCODE_7;
		case Keyboard.KEY_8:
			return Input.Keys.KEYCODE_8;
		case Keyboard.KEY_9:
			return Input.Keys.KEYCODE_9;
		case Keyboard.KEY_A:
			return Input.Keys.KEYCODE_A;
		case Keyboard.KEY_B:
			return Input.Keys.KEYCODE_B;
		case Keyboard.KEY_C:
			return Input.Keys.KEYCODE_C;
		case Keyboard.KEY_D:
			return Input.Keys.KEYCODE_D;
		case Keyboard.KEY_E:
			return Input.Keys.KEYCODE_E;
		case Keyboard.KEY_F:
			return Input.Keys.KEYCODE_F;
		case Keyboard.KEY_G:
			return Input.Keys.KEYCODE_G;
		case Keyboard.KEY_H:
			return Input.Keys.KEYCODE_H;
		case Keyboard.KEY_I:
			return Input.Keys.KEYCODE_I;
		case Keyboard.KEY_J:
			return Input.Keys.KEYCODE_J;
		case Keyboard.KEY_K:
			return Input.Keys.KEYCODE_K;
		case Keyboard.KEY_L:
			return Input.Keys.KEYCODE_L;
		case Keyboard.KEY_M:
			return Input.Keys.KEYCODE_M;
		case Keyboard.KEY_N:
			return Input.Keys.KEYCODE_N;
		case Keyboard.KEY_O:
			return Input.Keys.KEYCODE_O;
		case Keyboard.KEY_P:
			return Input.Keys.KEYCODE_P;
		case Keyboard.KEY_Q:
			return Input.Keys.KEYCODE_Q;
		case Keyboard.KEY_R:
			return Input.Keys.KEYCODE_R;
		case Keyboard.KEY_S:
			return Input.Keys.KEYCODE_S;
		case Keyboard.KEY_T:
			return Input.Keys.KEYCODE_T;
		case Keyboard.KEY_U:
			return Input.Keys.KEYCODE_U;
		case Keyboard.KEY_V:
			return Input.Keys.KEYCODE_V;
		case Keyboard.KEY_W:
			return Input.Keys.KEYCODE_W;
		case Keyboard.KEY_X:
			return Input.Keys.KEYCODE_X;
		case Keyboard.KEY_Y:
			return Input.Keys.KEYCODE_Y;
		case Keyboard.KEY_Z:
			return Input.Keys.KEYCODE_Z;
		case Keyboard.KEY_LMETA:
			return Input.Keys.KEYCODE_ALT_LEFT;
		case Keyboard.KEY_RMETA:
			return Input.Keys.KEYCODE_ALT_RIGHT;
		case Keyboard.KEY_BACKSLASH:
			return Input.Keys.KEYCODE_BACKSLASH;
		case Keyboard.KEY_COMMA:
			return Input.Keys.KEYCODE_COMMA;
		case Keyboard.KEY_DELETE:
			return Input.Keys.KEYCODE_DEL;
		case Keyboard.KEY_LEFT:
			return Input.Keys.KEYCODE_DPAD_LEFT;
		case Keyboard.KEY_RIGHT:
			return Input.Keys.KEYCODE_DPAD_RIGHT;
		case Keyboard.KEY_UP:
			return Input.Keys.KEYCODE_DPAD_UP;
		case Keyboard.KEY_DOWN:
			return Input.Keys.KEYCODE_DPAD_DOWN;
		case Keyboard.KEY_RETURN:
			return Input.Keys.KEYCODE_ENTER;
		case Keyboard.KEY_HOME:
			return Input.Keys.KEYCODE_HOME;
		case Keyboard.KEY_MINUS:
			return Input.Keys.KEYCODE_MINUS;
		case Keyboard.KEY_PERIOD:
			return Input.Keys.KEYCODE_PERIOD;
		case Keyboard.KEY_ADD:
			return Input.Keys.KEYCODE_PLUS;
		case Keyboard.KEY_SEMICOLON:
			return Input.Keys.KEYCODE_SEMICOLON;
		case Keyboard.KEY_LSHIFT:
			return Input.Keys.KEYCODE_SHIFT_LEFT;
		case Keyboard.KEY_RSHIFT:
			return Input.Keys.KEYCODE_SHIFT_RIGHT;
		case Keyboard.KEY_SLASH:
			return Input.Keys.KEYCODE_SLASH;
		case Keyboard.KEY_SPACE:
			return Input.Keys.KEYCODE_SPACE;
		case Keyboard.KEY_TAB:
			return Input.Keys.KEYCODE_TAB;
		default:
			return Input.Keys.KEYCODE_UNKNOWN;
		}
	}

	public static int getLwjglKeyCode (int gdxKeyCode) {
		switch (gdxKeyCode) {
		case Input.Keys.KEYCODE_0:
			return Keyboard.KEY_0;
		case Input.Keys.KEYCODE_1:
			return Keyboard.KEY_1;
		case Input.Keys.KEYCODE_2:
			return Keyboard.KEY_2;
		case Input.Keys.KEYCODE_3:
			return Keyboard.KEY_3;
		case Input.Keys.KEYCODE_4:
			return Keyboard.KEY_4;
		case Input.Keys.KEYCODE_5:
			return Keyboard.KEY_5;
		case Input.Keys.KEYCODE_6:
			return Keyboard.KEY_6;
		case Input.Keys.KEYCODE_7:
			return Keyboard.KEY_7;
		case Input.Keys.KEYCODE_8:
			return Keyboard.KEY_8;
		case Input.Keys.KEYCODE_9:
			return Keyboard.KEY_9;
		case Input.Keys.KEYCODE_A:
			return Keyboard.KEY_A;
		case Input.Keys.KEYCODE_B:
			return Keyboard.KEY_B;
		case Input.Keys.KEYCODE_C:
			return Keyboard.KEY_C;
		case Input.Keys.KEYCODE_D:
			return Keyboard.KEY_D;
		case Input.Keys.KEYCODE_E:
			return Keyboard.KEY_E;
		case Input.Keys.KEYCODE_F:
			return Keyboard.KEY_F;
		case Input.Keys.KEYCODE_G:
			return Keyboard.KEY_G;
		case Input.Keys.KEYCODE_H:
			return Keyboard.KEY_H;
		case Input.Keys.KEYCODE_I:
			return Keyboard.KEY_I;
		case Input.Keys.KEYCODE_J:
			return Keyboard.KEY_J;
		case Input.Keys.KEYCODE_K:
			return Keyboard.KEY_K;
		case Input.Keys.KEYCODE_L:
			return Keyboard.KEY_L;
		case Input.Keys.KEYCODE_M:
			return Keyboard.KEY_M;
		case Input.Keys.KEYCODE_N:
			return Keyboard.KEY_N;
		case Input.Keys.KEYCODE_O:
			return Keyboard.KEY_O;
		case Input.Keys.KEYCODE_P:
			return Keyboard.KEY_P;
		case Input.Keys.KEYCODE_Q:
			return Keyboard.KEY_Q;
		case Input.Keys.KEYCODE_R:
			return Keyboard.KEY_R;
		case Input.Keys.KEYCODE_S:
			return Keyboard.KEY_S;
		case Input.Keys.KEYCODE_T:
			return Keyboard.KEY_T;
		case Input.Keys.KEYCODE_U:
			return Keyboard.KEY_U;
		case Input.Keys.KEYCODE_V:
			return Keyboard.KEY_V;
		case Input.Keys.KEYCODE_W:
			return Keyboard.KEY_W;
		case Input.Keys.KEYCODE_X:
			return Keyboard.KEY_X;
		case Input.Keys.KEYCODE_Y:
			return Keyboard.KEY_Y;
		case Input.Keys.KEYCODE_Z:
			return Keyboard.KEY_Z;
		case Input.Keys.KEYCODE_ALT_LEFT:
			return Keyboard.KEY_LMETA;
		case Input.Keys.KEYCODE_ALT_RIGHT:
			return Keyboard.KEY_RMETA;
		case Input.Keys.KEYCODE_BACKSLASH:
			return Keyboard.KEY_BACKSLASH;
		case Input.Keys.KEYCODE_COMMA:
			return Keyboard.KEY_COMMA;
		case Input.Keys.KEYCODE_DEL:
			return Keyboard.KEY_DELETE;
		case Input.Keys.KEYCODE_DPAD_LEFT:
			return Keyboard.KEY_LEFT;
		case Input.Keys.KEYCODE_DPAD_RIGHT:
			return Keyboard.KEY_RIGHT;
		case Input.Keys.KEYCODE_DPAD_UP:
			return Keyboard.KEY_UP;
		case Input.Keys.KEYCODE_DPAD_DOWN:
			return Keyboard.KEY_DOWN;
		case Input.Keys.KEYCODE_ENTER:
			return Keyboard.KEY_RETURN;
		case Input.Keys.KEYCODE_HOME:
			return Keyboard.KEY_HOME;
		case Input.Keys.KEYCODE_MINUS:
			return Keyboard.KEY_MINUS;
		case Input.Keys.KEYCODE_PERIOD:
			return Keyboard.KEY_PERIOD;
		case Input.Keys.KEYCODE_PLUS:
			return Keyboard.KEY_ADD;
		case Input.Keys.KEYCODE_SEMICOLON:
			return Keyboard.KEY_SEMICOLON;
		case Input.Keys.KEYCODE_SHIFT_LEFT:
			return Keyboard.KEY_LSHIFT;
		case Input.Keys.KEYCODE_SHIFT_RIGHT:
			return Keyboard.KEY_RSHIFT;
		case Input.Keys.KEYCODE_SLASH:
			return Keyboard.KEY_SLASH;
		case Input.Keys.KEYCODE_SPACE:
			return Keyboard.KEY_SPACE;
		case Input.Keys.KEYCODE_TAB:
			return Keyboard.KEY_TAB;
		default:
			return Keyboard.KEY_NONE;
		}
	}

	public void update () {
		updateMouse();
		updateKeyboard();
	}

	void updateMouse () {
		if (Mouse.isCreated()) {
			int x = Mouse.getX();
			int y = Gdx.graphics.getHeight() - Mouse.getY();
			while (Mouse.next()) {
				if (isButtonPressed()) {
					if (mousePressed == false) {
						mousePressed = true;
						TouchEvent event = freeTouchEvents.newObject();
						event.x = x;
						event.y = y;
						event.pointer = 0;
						event.type = TouchEvent.TOUCH_DOWN;
						touchEvents.add(event);
						mouseX = x;
						mouseY = y;
					} else {
						if (mouseX != x || mouseY != y) {
							TouchEvent event = freeTouchEvents.newObject();
							event.x = x;
							event.y = y;
							event.pointer = 0;
							event.type = TouchEvent.TOUCH_DRAGGED;
							touchEvents.add(event);
							mouseX = x;
							mouseY = y;
						}
					}
				} else {
					if (mousePressed == true) {
						mouseX = x;
						mouseY = y;
						mousePressed = false;
						TouchEvent event = freeTouchEvents.newObject();
						event.x = x;
						event.y = y;
						event.pointer = 0;
						event.type = TouchEvent.TOUCH_UP;
						touchEvents.add(event);
					}
				}
			}
		}
	}

	boolean isButtonPressed () {
		for (int i = 0; i < Mouse.getButtonCount(); i++)
			if (Mouse.isButtonDown(i)) return true;
		return false;
	}

	void updateKeyboard () {
		if (Keyboard.isCreated()) {
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					int keyCode = getGdxKeyCode(Keyboard.getEventKey());
					char keyChar = Keyboard.getEventCharacter();

					KeyEvent event = freeKeyEvents.newObject();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_DOWN;
					keyEvents.add(event);

					event = freeKeyEvents.newObject();
					event.keyCode = 0;
					event.keyChar = keyChar;
					event.type = KeyEvent.KEY_TYPED;
					keyEvents.add(event);
					pressedKeys++;
				} else {
					int keyCode = LwjglInput.getGdxKeyCode(Keyboard.getEventKey());

					KeyEvent event = freeKeyEvents.newObject();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_UP;
					keyEvents.add(event);

					pressedKeys--;
				}
			}
		}
	}

	@Override public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}
}

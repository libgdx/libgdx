/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.backends.lwjgl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.utils.Pool;

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
		static final int TOUCH_SCROLLED = 3;
		static final int TOUCH_MOVED = 4;

		int type;
		int x;
		int y;
		int scrollAmount;
		int button;
		int pointer;
	}

	Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
		protected KeyEvent newObject () {
			return new KeyEvent();
		}
	};

	Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
		protected TouchEvent newObject () {
			return new TouchEvent();
		}
	};

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	boolean mousePressed = false;
	int mouseX, mouseY;
	int pressedKeys = 0;	
	boolean justTouched = false;
	Set<Integer> pressedButtons = new HashSet<Integer>();
	InputProcessor processor;
	char lastKeyCharPressed;
	float keyRepeatTimer;

	public LwjglInput () {
		Keyboard.enableRepeatEvents(false);
	}

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
				if(output != null) 
					listener.input(output);
				else
					listener.cancled();
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
					usedKeyEvents.free(e);
				}

				len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent e = touchEvents.get(i);
					switch (e.type) {
					case TouchEvent.TOUCH_DOWN:
						processor.touchDown(e.x, e.y, e.pointer, e.button);
						break;
					case TouchEvent.TOUCH_UP:
						processor.touchUp(e.x, e.y, e.pointer, e.button);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						processor.touchDragged(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_MOVED:
						processor.touchMoved(e.x, e.y);
						break;
					case TouchEvent.TOUCH_SCROLLED:
						processor.scrolled(e.scrollAmount);
					}
					usedTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					usedTouchEvents.free(touchEvents.get(i));
				}

				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					usedKeyEvents.free(keyEvents.get(i));
				}
			}

			keyEvents.clear();
			touchEvents.clear();
		}
	}

	public static int getGdxKeyCode (int lwjglKeyCode) {
		switch (lwjglKeyCode) {
		case Keyboard.KEY_0:
			return Input.Keys.NUM_0;
		case Keyboard.KEY_1:
			return Input.Keys.NUM_1;
		case Keyboard.KEY_2:
			return Input.Keys.NUM_2;
		case Keyboard.KEY_3:
			return Input.Keys.NUM_3;
		case Keyboard.KEY_4:
			return Input.Keys.NUM_4;
		case Keyboard.KEY_5:
			return Input.Keys.NUM_5;
		case Keyboard.KEY_6:
			return Input.Keys.NUM_6;
		case Keyboard.KEY_7:
			return Input.Keys.NUM_7;
		case Keyboard.KEY_8:
			return Input.Keys.NUM_8;
		case Keyboard.KEY_9:
			return Input.Keys.NUM_9;
		case Keyboard.KEY_A:
			return Input.Keys.A;
		case Keyboard.KEY_B:
			return Input.Keys.B;
		case Keyboard.KEY_C:
			return Input.Keys.C;
		case Keyboard.KEY_D:
			return Input.Keys.D;
		case Keyboard.KEY_E:
			return Input.Keys.E;
		case Keyboard.KEY_F:
			return Input.Keys.F;
		case Keyboard.KEY_G:
			return Input.Keys.G;
		case Keyboard.KEY_H:
			return Input.Keys.H;
		case Keyboard.KEY_I:
			return Input.Keys.I;
		case Keyboard.KEY_J:
			return Input.Keys.J;
		case Keyboard.KEY_K:
			return Input.Keys.K;
		case Keyboard.KEY_L:
			return Input.Keys.L;
		case Keyboard.KEY_M:
			return Input.Keys.M;
		case Keyboard.KEY_N:
			return Input.Keys.N;
		case Keyboard.KEY_O:
			return Input.Keys.O;
		case Keyboard.KEY_P:
			return Input.Keys.P;
		case Keyboard.KEY_Q:
			return Input.Keys.Q;
		case Keyboard.KEY_R:
			return Input.Keys.R;
		case Keyboard.KEY_S:
			return Input.Keys.S;
		case Keyboard.KEY_T:
			return Input.Keys.T;
		case Keyboard.KEY_U:
			return Input.Keys.U;
		case Keyboard.KEY_V:
			return Input.Keys.V;
		case Keyboard.KEY_W:
			return Input.Keys.W;
		case Keyboard.KEY_X:
			return Input.Keys.X;
		case Keyboard.KEY_Y:
			return Input.Keys.Y;
		case Keyboard.KEY_Z:
			return Input.Keys.Z;
		case Keyboard.KEY_LMETA:
			return Input.Keys.ALT_LEFT;
		case Keyboard.KEY_RMETA:
			return Input.Keys.ALT_RIGHT;
		case Keyboard.KEY_BACKSLASH:
			return Input.Keys.BACKSLASH;
		case Keyboard.KEY_COMMA:
			return Input.Keys.COMMA;
		case Keyboard.KEY_DELETE:
			return Input.Keys.FORWARD_DEL;
		case Keyboard.KEY_LEFT:
			return Input.Keys.DPAD_LEFT;
		case Keyboard.KEY_RIGHT:
			return Input.Keys.DPAD_RIGHT;
		case Keyboard.KEY_UP:
			return Input.Keys.DPAD_UP;
		case Keyboard.KEY_DOWN:
			return Input.Keys.DPAD_DOWN;
		case Keyboard.KEY_RETURN:
			return Input.Keys.ENTER;
		case Keyboard.KEY_HOME:
			return Input.Keys.HOME;
		case Keyboard.KEY_MINUS:
			return Input.Keys.MINUS;
		case Keyboard.KEY_PERIOD:
			return Input.Keys.PERIOD;
		case Keyboard.KEY_ADD:
			return Input.Keys.PLUS;
		case Keyboard.KEY_SEMICOLON:
			return Input.Keys.SEMICOLON;
		case Keyboard.KEY_LSHIFT:
			return Input.Keys.SHIFT_LEFT;
		case Keyboard.KEY_RSHIFT:
			return Input.Keys.SHIFT_RIGHT;
		case Keyboard.KEY_SLASH:
			return Input.Keys.SLASH;
		case Keyboard.KEY_SPACE:
			return Input.Keys.SPACE;
		case Keyboard.KEY_TAB:
			return Input.Keys.TAB;
		case Keyboard.KEY_LCONTROL:
			return Input.Keys.CONTROL_LEFT;
		case Keyboard.KEY_RCONTROL:
			return Input.Keys.CONTROL_RIGHT;
		case Keyboard.KEY_ESCAPE:
			return Input.Keys.ESCAPE;
		case Keyboard.KEY_END:
			return Input.Keys.END;
		case Keyboard.KEY_INSERT:
			return Input.Keys.INSERT;
		case Keyboard.KEY_NUMPAD5:
			return Input.Keys.DPAD_CENTER;
		case Keyboard.KEY_BACK:
			return Input.Keys.DEL;		
		default:
			return Input.Keys.UNKNOWN;
		}
	}

	public static int getLwjglKeyCode (int gdxKeyCode) {
		switch (gdxKeyCode) {
		case Input.Keys.NUM_0:
			return Keyboard.KEY_0;
		case Input.Keys.NUM_1:
			return Keyboard.KEY_1;
		case Input.Keys.NUM_2:
			return Keyboard.KEY_2;
		case Input.Keys.NUM_3:
			return Keyboard.KEY_3;
		case Input.Keys.NUM_4:
			return Keyboard.KEY_4;
		case Input.Keys.NUM_5:
			return Keyboard.KEY_5;
		case Input.Keys.NUM_6:
			return Keyboard.KEY_6;
		case Input.Keys.NUM_7:
			return Keyboard.KEY_7;
		case Input.Keys.NUM_8:
			return Keyboard.KEY_8;
		case Input.Keys.NUM_9:
			return Keyboard.KEY_9;
		case Input.Keys.A:
			return Keyboard.KEY_A;
		case Input.Keys.B:
			return Keyboard.KEY_B;
		case Input.Keys.C:
			return Keyboard.KEY_C;
		case Input.Keys.D:
			return Keyboard.KEY_D;
		case Input.Keys.E:
			return Keyboard.KEY_E;
		case Input.Keys.F:
			return Keyboard.KEY_F;
		case Input.Keys.G:
			return Keyboard.KEY_G;
		case Input.Keys.H:
			return Keyboard.KEY_H;
		case Input.Keys.I:
			return Keyboard.KEY_I;
		case Input.Keys.J:
			return Keyboard.KEY_J;
		case Input.Keys.K:
			return Keyboard.KEY_K;
		case Input.Keys.L:
			return Keyboard.KEY_L;
		case Input.Keys.M:
			return Keyboard.KEY_M;
		case Input.Keys.N:
			return Keyboard.KEY_N;
		case Input.Keys.O:
			return Keyboard.KEY_O;
		case Input.Keys.P:
			return Keyboard.KEY_P;
		case Input.Keys.Q:
			return Keyboard.KEY_Q;
		case Input.Keys.R:
			return Keyboard.KEY_R;
		case Input.Keys.S:
			return Keyboard.KEY_S;
		case Input.Keys.T:
			return Keyboard.KEY_T;
		case Input.Keys.U:
			return Keyboard.KEY_U;
		case Input.Keys.V:
			return Keyboard.KEY_V;
		case Input.Keys.W:
			return Keyboard.KEY_W;
		case Input.Keys.X:
			return Keyboard.KEY_X;
		case Input.Keys.Y:
			return Keyboard.KEY_Y;
		case Input.Keys.Z:
			return Keyboard.KEY_Z;
		case Input.Keys.ALT_LEFT:
			return Keyboard.KEY_LMETA;
		case Input.Keys.ALT_RIGHT:
			return Keyboard.KEY_RMETA;
		case Input.Keys.BACKSLASH:
			return Keyboard.KEY_BACKSLASH;
		case Input.Keys.COMMA:
			return Keyboard.KEY_COMMA;
		case Input.Keys.FORWARD_DEL:
			return Keyboard.KEY_DELETE;
		case Input.Keys.DPAD_LEFT:
			return Keyboard.KEY_LEFT;
		case Input.Keys.DPAD_RIGHT:
			return Keyboard.KEY_RIGHT;
		case Input.Keys.DPAD_UP:
			return Keyboard.KEY_UP;
		case Input.Keys.DPAD_DOWN:
			return Keyboard.KEY_DOWN;
		case Input.Keys.ENTER:
			return Keyboard.KEY_RETURN;
		case Input.Keys.HOME:
			return Keyboard.KEY_HOME;
		case Input.Keys.MINUS:
			return Keyboard.KEY_MINUS;
		case Input.Keys.PERIOD:
			return Keyboard.KEY_PERIOD;
		case Input.Keys.PLUS:
			return Keyboard.KEY_ADD;
		case Input.Keys.SEMICOLON:
			return Keyboard.KEY_SEMICOLON;
		case Input.Keys.SHIFT_LEFT:
			return Keyboard.KEY_LSHIFT;
		case Input.Keys.SHIFT_RIGHT:
			return Keyboard.KEY_RSHIFT;
		case Input.Keys.SLASH:
			return Keyboard.KEY_SLASH;
		case Input.Keys.SPACE:
			return Keyboard.KEY_SPACE;
		case Input.Keys.TAB:
			return Keyboard.KEY_TAB;
		case Input.Keys.DEL:
			return Keyboard.KEY_BACK;
		default:
			return Keyboard.KEY_NONE;
		}
	}

	public void update () {
		updateMouse();
		updateKeyboard();
	}

	private int toGdxButton(int button) {
		if(button == 0)
			return Buttons.LEFT;
		if(button == 1)
			return Buttons.RIGHT;
		if(button == 2)
			return Buttons.MIDDLE;
		return Buttons.LEFT;
			
	}
	
	void updateMouse () {
		justTouched = false;
		if (Mouse.isCreated()) {
			while (Mouse.next()) {
				int x = Mouse.getEventX();
				int y = Gdx.graphics.getHeight() - Mouse.getEventY() - 1;
				int button = Mouse.getEventButton();
				
				TouchEvent event = usedTouchEvents.obtain();
				event.x = x;
				event.y = y;
				event.button = toGdxButton(button);
				event.pointer = 0;
				
				// could be drag, scroll or move
				if(button == -1) {
					if(Mouse.getEventDWheel() != 0) {
						event.type = TouchEvent.TOUCH_SCROLLED;
						event.scrollAmount = (int)-Math.signum(Mouse.getEventDWheel());
					} else if(pressedButtons .size() > 0) {									
						event.type = TouchEvent.TOUCH_DRAGGED;						
					} else {
						event.type = TouchEvent.TOUCH_MOVED;
					}
				} else {
					// nope, it's a down or up event.
					if(Mouse.getEventButtonState()) {
						event.type = TouchEvent.TOUCH_DOWN;
						pressedButtons.add(event.button);
						justTouched = true;
					} else {
						event.type = TouchEvent.TOUCH_UP;
						pressedButtons.remove(event.button);
					}
				}
				
				touchEvents.add(event);
				mouseX = event.x;
				mouseY = event.y;
			}
		}
	}

	void updateKeyboard () {
		if (lastKeyCharPressed != 0) {
			keyRepeatTimer -= Gdx.graphics.getDeltaTime();
			if (keyRepeatTimer < 0) {
				keyRepeatTimer = 0.15f;

				KeyEvent event = usedKeyEvents.obtain();
				event.keyCode = 0;
				event.keyChar = lastKeyCharPressed;
				event.type = KeyEvent.KEY_TYPED;
				keyEvents.add(event);
			}
		}

		if (Keyboard.isCreated()) {
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					int keyCode = getGdxKeyCode(Keyboard.getEventKey());
					char keyChar = Keyboard.getEventCharacter();

					KeyEvent event = usedKeyEvents.obtain();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_DOWN;
					keyEvents.add(event);
			
					event = usedKeyEvents.obtain();
					event.keyCode = 0;
					event.keyChar = keyChar;
					event.type = KeyEvent.KEY_TYPED;
					keyEvents.add(event);

					pressedKeys++;
					lastKeyCharPressed = keyChar;
					keyRepeatTimer = 0.4f;
				} else {
					int keyCode = LwjglInput.getGdxKeyCode(Keyboard.getEventKey());

					KeyEvent event = usedKeyEvents.obtain();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_UP;
					keyEvents.add(event);

					pressedKeys--;
					lastKeyCharPressed = 0;
				}
			}
		}
	}

	@Override public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}
	
	@Override public InputProcessor getInputProcessor() {
		return this.processor;
	}

	@Override public void vibrate (int milliseconds) {
	}

	@Override public boolean justTouched () {
		return justTouched;
	}
	
	private int toLwjglButton(int button) {
		if(button == Buttons.LEFT)
			return 0;
		if(button == Buttons.RIGHT)
			return 1;
		if(button == Buttons.MIDDLE)
			return 2;
		return 0;
	}

	@Override public boolean isButtonPressed (int button) {
		return Mouse.isButtonDown(toLwjglButton(button));
	}

	@Override public void vibrate (long[] pattern, int repeat) {
	}

	@Override public void cancelVibrate () {
	}

	@Override public float getAzimuth () {
		return 0;
	}

	@Override public float getPitch () {
		return 0;
	}

	@Override public float getRoll () {
		return 0;
	}
	
	@Override public boolean isPeripheralAvailable (Peripheral peripheral) {		
		if(peripheral == Peripheral.HardwareKeyboard) return true;
		return false;
	}
}

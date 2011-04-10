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
package com.badlogic.gdx.backends.angle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.anglejni.ESLoop;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;

public class AngleInput implements Input {

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
		static final int TOUCH_MOVED = 3;
		static final int TOUCH_SCROLL = 4;

		int type;
		int x;
		int y;
		int button;
		int scrollAmount;
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
	boolean justTouched = false;
	int mouseX = 0;
	int mouseY = 0;
	HashSet<Integer> pressedKeys = new HashSet<Integer>();
	HashSet<Integer> pressedButtons = new HashSet<Integer>();

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
		return mouseY;
	}

	public int getY () {
		return mouseX;
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		if(key == Keys.ANY_KEY)
			return pressedKeys.size() > 0;
		else
			return pressedKeys.contains(key);
	}

	public boolean isTouched () {
		return mousePressed;
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
			justTouched = false;
			
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
						justTouched = true;
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
					case TouchEvent.TOUCH_SCROLL:
						processor.scrolled(e.scrollAmount);
						break;
					}
					usedTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent event = touchEvents.get(i);
					if(event.type == TouchEvent.TOUCH_DOWN);
						justTouched = true;
					usedTouchEvents.free(event);
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

	boolean isButtonPressed () {
		return mousePressed;
	}

	@Override public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}
	
	@Override public InputProcessor getInputProcessor() {
		return this.processor;
	}

	void registerKeyEvent (int action, int key, int uniCode) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = (char)uniCode;
			event.keyCode = toGdxKeyCode(key);

			switch (action) {
			case ESLoop.ES_KEY_DOWN:
				event.type = KeyEvent.KEY_DOWN;
				pressedKeys.add(event.keyCode);
				break;
			case ESLoop.ES_KEY_UP:
				event.type = KeyEvent.KEY_UP;
				pressedKeys.remove(event.keyCode);
				break;
			case ESLoop.ES_KEY_TYPED:
				event.type = KeyEvent.KEY_TYPED;
				break;				
			}

			keyEvents.add(event);
		}
	}

	void registerMouseEvent (int action, int x, int y, int button) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.x = x;
			event.y = y;
			event.pointer = 0;

			switch (action) {
			case ESLoop.ES_MOUSE_DOWN:
				event.type = TouchEvent.TOUCH_DOWN;
				mousePressed = true;
				justTouched = true;
				if((button & ESLoop.ES_MOUSE_LBUTTON) != 0) {
					pressedButtons.add(Buttons.LEFT);
					event.button = Buttons.LEFT;
				}
				if((button & ESLoop.ES_MOUSE_RBUTTON) != 0) {
					pressedButtons.add(Buttons.RIGHT);
					event.button = Buttons.RIGHT;
				}
				if((button & ESLoop.ES_MOUSE_MBUTTON) != 0) {
					pressedButtons.add(Buttons.MIDDLE);
					event.button = Buttons.MIDDLE;
				}
				break;
			case ESLoop.ES_MOUSE_UP:
				event.type = TouchEvent.TOUCH_UP;
				mousePressed = false;
				if((button & ESLoop.ES_MOUSE_LBUTTON) != 0) {
					pressedButtons.remove(Buttons.LEFT);
					event.button = Buttons.LEFT;					
				}
				if((button & ESLoop.ES_MOUSE_RBUTTON) != 0) {
					pressedButtons.remove(Buttons.RIGHT);
					event.button = Buttons.RIGHT;
				}
				if((button & ESLoop.ES_MOUSE_MBUTTON) != 0) {
					pressedButtons.remove(Buttons.MIDDLE);
					event.button = Buttons.MIDDLE;
				}
				break;
			case ESLoop.ES_MOUSE_MOVE:
				if(!mousePressed)
					event.type = TouchEvent.TOUCH_MOVED;
				else
					event.type = TouchEvent.TOUCH_DRAGGED;
				break;			
			case ESLoop.ES_MOUSE_WHEEL:
				event.type = TouchEvent.TOUCH_SCROLL;
				event.scrollAmount = -(int)Math.signum(x);
				break;
			}
			
			touchEvents.add(event);
		}
	}

	int toGdxKeyCode (int keyCode) {
		switch(keyCode) {
		case 0x08: return Keys.KEYCODE_BACK;
		case 0x09: return Keys.KEYCODE_TAB;
		case 0x0d: return Keys.KEYCODE_ENTER;
		case 0x10: return Keys.KEYCODE_SHIFT_LEFT;
		case 0x11: return Keys.KEYCODE_CONTROL_LEFT;
		case 0x12: return Keys.KEYCODE_ALT_LEFT;
//		case 0x13: return Keys.KEYCODE_PAUSE;
//		case 0x14: return Keys.KEYCODE_CAPS;
		case 0x18: return Keys.KEYCODE_ESCAPE;
		case 0x20: return Keys.KEYCODE_SPACE;
		case 0x23: return Keys.KEYCODE_END;
		case 0x24: return Keys.KEYCODE_HOME;
		case 0x25: return Keys.KEYCODE_DPAD_LEFT;
		case 0x26: return Keys.KEYCODE_DPAD_UP;
		case 0x27: return Keys.KEYCODE_DPAD_RIGHT;
		case 0x28: return Keys.KEYCODE_DPAD_DOWN;
		case 0x2d: return Keys.KEYCODE_INSERT;
		case 0x2e: return Keys.KEYCODE_DEL;
		case 0x30: return Keys.KEYCODE_0;
		case 0x31: return Keys.KEYCODE_1;
		case 0x32: return Keys.KEYCODE_2;
		case 0x33: return Keys.KEYCODE_3;
		case 0x34: return Keys.KEYCODE_4;
		case 0x35: return Keys.KEYCODE_5;
		case 0x36: return Keys.KEYCODE_6;
		case 0x37: return Keys.KEYCODE_7;
		case 0x38: return Keys.KEYCODE_8;
		case 0x39: return Keys.KEYCODE_9;
		case 0x41: return Keys.KEYCODE_A;
		case 0x42: return Keys.KEYCODE_B;
		case 0x43: return Keys.KEYCODE_C;
		case 0x44: return Keys.KEYCODE_D;
		case 0x45: return Keys.KEYCODE_E;
		case 0x46: return Keys.KEYCODE_F;
		case 0x47: return Keys.KEYCODE_G;
		case 0x48: return Keys.KEYCODE_H;
		case 0x49: return Keys.KEYCODE_I;
		case 0x4a: return Keys.KEYCODE_J;
		case 0x4b: return Keys.KEYCODE_K;
		case 0x4c: return Keys.KEYCODE_L;
		case 0x4d: return Keys.KEYCODE_M;
		case 0x4e: return Keys.KEYCODE_N;
		case 0x4f: return Keys.KEYCODE_O;
		case 0x50: return Keys.KEYCODE_P;
		case 0x51: return Keys.KEYCODE_Q;
		case 0x52: return Keys.KEYCODE_R;
		case 0x53: return Keys.KEYCODE_S;
		case 0x54: return Keys.KEYCODE_T;
		case 0x55: return Keys.KEYCODE_U;
		case 0x56: return Keys.KEYCODE_V;
		case 0x57: return Keys.KEYCODE_W;
		case 0x58: return Keys.KEYCODE_X;
		case 0x59: return Keys.KEYCODE_Y;
		case 0x5a: return Keys.KEYCODE_Z;	
		case 0x60: return Keys.KEYCODE_0;		
		case 0x61: return Keys.KEYCODE_1;
		case 0x62: return Keys.KEYCODE_2;
		case 0x63: return Keys.KEYCODE_3;
		case 0x64: return Keys.KEYCODE_4;
		case 0x65: return Keys.KEYCODE_5;
		case 0x66: return Keys.KEYCODE_6;
		case 0x67: return Keys.KEYCODE_7;
		case 0x68: return Keys.KEYCODE_8;
		case 0x69: return Keys.KEYCODE_9;		
		case 0x6a: return Keys.KEYCODE_STAR;
		case 0x6b: return Keys.KEYCODE_PLUS;
		case 0x6d: return Keys.KEYCODE_MINUS;
		case 0x6e: return Keys.KEYCODE_PERIOD;
		case 0x6f: return Keys.KEYCODE_SLASH;
		case 0xa0: return Keys.KEYCODE_SHIFT_LEFT;
		case 0xa1: return Keys.KEYCODE_SHIFT_RIGHT;
		case 0xa2: return Keys.KEYCODE_CONTROL_LEFT;
		case 0xa3: return Keys.KEYCODE_CONTROL_RIGHT;
		case 0xae: return Keys.KEYCODE_VOLUME_DOWN;
		case 0xaf: return Keys.KEYCODE_VOLUME_UP;		
			default: 
				return Keys.KEYCODE_UNKNOWN;
		}		
	}

	@Override public boolean supportsVibrator () {
		return false;
	}

	@Override public void vibrate (int milliseconds) {
		
	}

	@Override public boolean justTouched () {
		return justTouched;
	}		
	
	@Override public boolean isButtonPressed (int button) {		
		return pressedButtons.contains(button);
	}

	@Override public void vibrate (long[] pattern, int repeat) {
	}

	@Override public void cancelVibrate () {
	}

	@Override public boolean supportsCompass () {
		return false;
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
}

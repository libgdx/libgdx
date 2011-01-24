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

	void registerKeyEvent (int action, int key, int uniCode) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = (char)uniCode;
			event.keyCode = translateKey(key);

			switch (action) {
			case ESLoop.ES_KEY_DOWN:
				event.type = KeyEvent.KEY_DOWN;
				break;
			case ESLoop.ES_KEY_UP:
				event.type = KeyEvent.KEY_UP;
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

	int translateKey (int keyCode) {
		return keyCode;
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

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
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

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
	HashSet<Integer> pressedKeys = new HashSet<Integer>();

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
		// FIXME
		return 0;
	}

	public int getY () {
		// FIXME
		return 0;
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		// FIXME
		return false;
	}

	public boolean isTouched () {
		// FIXME
		return false;
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

	boolean isButtonPressed () {
		return mousePressed;
	}

	@Override public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}

	void registerKeyEvent (int action, int key, int uniCode) {
		synchronized (this) {
			KeyEvent event = freeKeyEvents.newObject();
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
		if (button != 1) return;

		synchronized (this) {
			TouchEvent event = freeTouchEvents.newObject();
			event.x = x;
			event.y = y;
			event.pointer = 0;

			switch (action) {
			case ESLoop.ES_MOUSE_DOWN:
				event.type = TouchEvent.TOUCH_DOWN;
				break;
			case ESLoop.ES_MOUSE_UP:
				event.type = TouchEvent.TOUCH_UP;
				break;
			case ESLoop.ES_MOUSE_MOVE:
				event.type = TouchEvent.TOUCH_DRAGGED;
				break;
			}
		}
	}

	int translateKey (int keyCode) {
		// FIXME
		return keyCode;
	}
}

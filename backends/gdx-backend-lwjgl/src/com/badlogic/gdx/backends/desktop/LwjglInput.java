/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.desktop;

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

	Pool<KeyEvent> freeKeyEvents = new Pool<KeyEvent>(
			new PoolObjectFactory<KeyEvent>() {

				@Override
				public KeyEvent createObject() {
					return new KeyEvent();
				}
			}, 1000);

	Pool<TouchEvent> freeTouchEvents = new Pool<TouchEvent>(
			new PoolObjectFactory<TouchEvent>() {

				@Override
				public TouchEvent createObject() {
					return new TouchEvent();
				}
			}, 1000);

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	boolean mousePressed = false;
	int mouseX = 0;
	int mouseY = 0;
	int pressedKeys = 0;
	
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
		if( key == Input.Keys.ANY_KEY )
			return pressedKeys > 0;
		else
			return Keyboard.isKeyDown(getKeyCodeReverse(key));
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

	@Override
	public void processEvents(InputProcessor listener) {
		synchronized(this) {
			if(listener!=null) {						
				for(KeyEvent e: keyEvents) {
					switch(e.type) {
					case KeyEvent.KEY_DOWN:
						listener.keyDown(e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						listener.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						listener.keyTyped(e.keyChar);
					}
					freeKeyEvents.free(e);					
				}					
				
				for(TouchEvent e: touchEvents) {
					switch(e.type) {
					case TouchEvent.TOUCH_DOWN:
						listener.touchDown(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_UP:
						listener.touchUp(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						listener.touchDragged(e.x, e.y, e.pointer);
					}
					freeTouchEvents.free(e);
				}
			}
			
			keyEvents.clear();
			touchEvents.clear();
		}
	}
	
	public static int getKeyCode (int keyCode) {
		if (keyCode == Keyboard.KEY_0) return Input.Keys.KEYCODE_0;
		if (keyCode == Keyboard.KEY_1) return Input.Keys.KEYCODE_1;
		if (keyCode == Keyboard.KEY_2) return Input.Keys.KEYCODE_2;
		if (keyCode == Keyboard.KEY_3) return Input.Keys.KEYCODE_3;
		if (keyCode == Keyboard.KEY_4) return Input.Keys.KEYCODE_4;
		if (keyCode == Keyboard.KEY_5) return Input.Keys.KEYCODE_5;
		if (keyCode == Keyboard.KEY_6) return Input.Keys.KEYCODE_6;
		if (keyCode == Keyboard.KEY_7) return Input.Keys.KEYCODE_7;
		if (keyCode == Keyboard.KEY_8) return Input.Keys.KEYCODE_8;
		if (keyCode == Keyboard.KEY_9) return Input.Keys.KEYCODE_9;
		if (keyCode == Keyboard.KEY_A) return Input.Keys.KEYCODE_A;
		if (keyCode == Keyboard.KEY_B) return Input.Keys.KEYCODE_B;
		if (keyCode == Keyboard.KEY_C) return Input.Keys.KEYCODE_C;
		if (keyCode == Keyboard.KEY_D) return Input.Keys.KEYCODE_D;
		if (keyCode == Keyboard.KEY_E) return Input.Keys.KEYCODE_E;
		if (keyCode == Keyboard.KEY_F) return Input.Keys.KEYCODE_F;
		if (keyCode == Keyboard.KEY_G) return Input.Keys.KEYCODE_G;
		if (keyCode == Keyboard.KEY_H) return Input.Keys.KEYCODE_H;
		if (keyCode == Keyboard.KEY_I) return Input.Keys.KEYCODE_I;
		if (keyCode == Keyboard.KEY_J) return Input.Keys.KEYCODE_J;
		if (keyCode == Keyboard.KEY_K) return Input.Keys.KEYCODE_K;
		if (keyCode == Keyboard.KEY_L) return Input.Keys.KEYCODE_L;
		if (keyCode == Keyboard.KEY_M) return Input.Keys.KEYCODE_M;
		if (keyCode == Keyboard.KEY_N) return Input.Keys.KEYCODE_N;
		if (keyCode == Keyboard.KEY_O) return Input.Keys.KEYCODE_O;
		if (keyCode == Keyboard.KEY_P) return Input.Keys.KEYCODE_P;
		if (keyCode == Keyboard.KEY_Q) return Input.Keys.KEYCODE_Q;
		if (keyCode == Keyboard.KEY_R) return Input.Keys.KEYCODE_R;
		if (keyCode == Keyboard.KEY_S) return Input.Keys.KEYCODE_S;
		if (keyCode == Keyboard.KEY_T) return Input.Keys.KEYCODE_T;
		if (keyCode == Keyboard.KEY_U) return Input.Keys.KEYCODE_U;
		if (keyCode == Keyboard.KEY_V) return Input.Keys.KEYCODE_V;
		if (keyCode == Keyboard.KEY_W) return Input.Keys.KEYCODE_W;
		if (keyCode == Keyboard.KEY_X) return Input.Keys.KEYCODE_X;
		if (keyCode == Keyboard.KEY_Y) return Input.Keys.KEYCODE_Y;
		if (keyCode == Keyboard.KEY_Z) return Input.Keys.KEYCODE_Z;
		if (keyCode == Keyboard.KEY_LMETA) return Input.Keys.KEYCODE_ALT_LEFT;
		if (keyCode == Keyboard.KEY_RMETA) return Input.Keys.KEYCODE_ALT_RIGHT;
		if (keyCode == Keyboard.KEY_BACKSLASH) return Input.Keys.KEYCODE_BACKSLASH;
		if (keyCode == Keyboard.KEY_COMMA) return Input.Keys.KEYCODE_COMMA;
		if (keyCode == Keyboard.KEY_DELETE) return Input.Keys.KEYCODE_DEL;
		if (keyCode == Keyboard.KEY_LEFT) return Input.Keys.KEYCODE_DPAD_LEFT;
		if (keyCode == Keyboard.KEY_RIGHT) return Input.Keys.KEYCODE_DPAD_RIGHT;
		if (keyCode == Keyboard.KEY_UP) return Input.Keys.KEYCODE_DPAD_UP;
		if (keyCode == Keyboard.KEY_DOWN) return Input.Keys.KEYCODE_DPAD_DOWN;
		if (keyCode == Keyboard.KEY_RETURN) return Input.Keys.KEYCODE_ENTER;
		if (keyCode == Keyboard.KEY_HOME) return Input.Keys.KEYCODE_HOME;
		if (keyCode == Keyboard.KEY_MINUS) return Input.Keys.KEYCODE_MINUS;
		if (keyCode == Keyboard.KEY_PERIOD) return Input.Keys.KEYCODE_PERIOD;
		if (keyCode == Keyboard.KEY_ADD) return Input.Keys.KEYCODE_PLUS;
		if (keyCode == Keyboard.KEY_SEMICOLON) return Input.Keys.KEYCODE_SEMICOLON;
		if (keyCode == Keyboard.KEY_LSHIFT) return Input.Keys.KEYCODE_SHIFT_LEFT;
		if (keyCode == Keyboard.KEY_RSHIFT) return Input.Keys.KEYCODE_SHIFT_RIGHT;
		if (keyCode == Keyboard.KEY_SLASH) return Input.Keys.KEYCODE_SLASH;
		if (keyCode == Keyboard.KEY_SPACE) return Input.Keys.KEYCODE_SPACE;
		if (keyCode == Keyboard.KEY_TAB) return Input.Keys.KEYCODE_TAB;
		return Input.Keys.KEYCODE_UNKNOWN;
	}

	public static int getKeyCodeReverse (int keyCode) {
		if (keyCode == Input.Keys.KEYCODE_0) return Keyboard.KEY_0;
		if (keyCode == Input.Keys.KEYCODE_1) return Keyboard.KEY_1;
		if (keyCode == Input.Keys.KEYCODE_2) return Keyboard.KEY_2;
		if (keyCode == Input.Keys.KEYCODE_3) return Keyboard.KEY_3;
		if (keyCode == Input.Keys.KEYCODE_4) return Keyboard.KEY_4;
		if (keyCode == Input.Keys.KEYCODE_5) return Keyboard.KEY_5;
		if (keyCode == Input.Keys.KEYCODE_6) return Keyboard.KEY_6;
		if (keyCode == Input.Keys.KEYCODE_7) return Keyboard.KEY_7;
		if (keyCode == Input.Keys.KEYCODE_8) return Keyboard.KEY_8;
		if (keyCode == Input.Keys.KEYCODE_9) return Keyboard.KEY_9;
		if (keyCode == Input.Keys.KEYCODE_A) return Keyboard.KEY_A;
		if (keyCode == Input.Keys.KEYCODE_B) return Keyboard.KEY_B;
		if (keyCode == Input.Keys.KEYCODE_C) return Keyboard.KEY_C;
		if (keyCode == Input.Keys.KEYCODE_D) return Keyboard.KEY_D;
		if (keyCode == Input.Keys.KEYCODE_E) return Keyboard.KEY_E;
		if (keyCode == Input.Keys.KEYCODE_F) return Keyboard.KEY_F;
		if (keyCode == Input.Keys.KEYCODE_G) return Keyboard.KEY_G;
		if (keyCode == Input.Keys.KEYCODE_H) return Keyboard.KEY_H;
		if (keyCode == Input.Keys.KEYCODE_I) return Keyboard.KEY_I;
		if (keyCode == Input.Keys.KEYCODE_J) return Keyboard.KEY_J;
		if (keyCode == Input.Keys.KEYCODE_K) return Keyboard.KEY_K;
		if (keyCode == Input.Keys.KEYCODE_L) return Keyboard.KEY_L;
		if (keyCode == Input.Keys.KEYCODE_M) return Keyboard.KEY_M;
		if (keyCode == Input.Keys.KEYCODE_N) return Keyboard.KEY_N;
		if (keyCode == Input.Keys.KEYCODE_O) return Keyboard.KEY_O;
		if (keyCode == Input.Keys.KEYCODE_P) return Keyboard.KEY_P;
		if (keyCode == Input.Keys.KEYCODE_Q) return Keyboard.KEY_Q;
		if (keyCode == Input.Keys.KEYCODE_R) return Keyboard.KEY_R;
		if (keyCode == Input.Keys.KEYCODE_S) return Keyboard.KEY_S;
		if (keyCode == Input.Keys.KEYCODE_T) return Keyboard.KEY_T;
		if (keyCode == Input.Keys.KEYCODE_U) return Keyboard.KEY_U;
		if (keyCode == Input.Keys.KEYCODE_V) return Keyboard.KEY_V;
		if (keyCode == Input.Keys.KEYCODE_W) return Keyboard.KEY_W;
		if (keyCode == Input.Keys.KEYCODE_X) return Keyboard.KEY_X;
		if (keyCode == Input.Keys.KEYCODE_Y) return Keyboard.KEY_Y;
		if (keyCode == Input.Keys.KEYCODE_Z) return Keyboard.KEY_Z;
		if (keyCode == Input.Keys.KEYCODE_ALT_LEFT) return Keyboard.KEY_LMETA;
		if (keyCode == Input.Keys.KEYCODE_ALT_RIGHT) return Keyboard.KEY_RMETA;
		if (keyCode == Input.Keys.KEYCODE_BACKSLASH) return Keyboard.KEY_BACKSLASH;
		if (keyCode == Input.Keys.KEYCODE_COMMA) return Keyboard.KEY_COMMA;
		if (keyCode == Input.Keys.KEYCODE_DEL) return Keyboard.KEY_DELETE;
		if (keyCode == Input.Keys.KEYCODE_DPAD_LEFT) return Keyboard.KEY_LEFT;
		if (keyCode == Input.Keys.KEYCODE_DPAD_RIGHT) return Keyboard.KEY_RIGHT;
		if (keyCode == Input.Keys.KEYCODE_DPAD_UP) return Keyboard.KEY_UP;
		if (keyCode == Input.Keys.KEYCODE_DPAD_DOWN) return Keyboard.KEY_DOWN;
		if (keyCode == Input.Keys.KEYCODE_ENTER) return Keyboard.KEY_RETURN;
		if (keyCode == Input.Keys.KEYCODE_HOME) return Keyboard.KEY_HOME;
		if (keyCode == Input.Keys.KEYCODE_MINUS) return Keyboard.KEY_MINUS;
		if (keyCode == Input.Keys.KEYCODE_PERIOD) return Keyboard.KEY_PERIOD;
		if (keyCode == Input.Keys.KEYCODE_PLUS) return Keyboard.KEY_ADD;
		if (keyCode == Input.Keys.KEYCODE_SEMICOLON) return Keyboard.KEY_SEMICOLON;
		if (keyCode == Input.Keys.KEYCODE_SHIFT_LEFT) return Keyboard.KEY_LSHIFT;
		if (keyCode == Input.Keys.KEYCODE_SHIFT_RIGHT) return Keyboard.KEY_RSHIFT;
		if (keyCode == Input.Keys.KEYCODE_SLASH) return Keyboard.KEY_SLASH;
		if (keyCode == Input.Keys.KEYCODE_SPACE) return Keyboard.KEY_SPACE;
		if (keyCode == Input.Keys.KEYCODE_TAB) return Keyboard.KEY_TAB;		
		return Keyboard.KEY_NONE;
	}

	public void update() {
		updateMouse();
		updateKeyboard();
	}
	
	void updateMouse() {
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

	
	void updateKeyboard() {
		if (Keyboard.isCreated()) {
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					int keyCode = getKeyCode(Keyboard.getEventKey());
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
				}				
				else {
					int keyCode = LwjglInput.getKeyCode(Keyboard.getEventKey());					
					
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
}

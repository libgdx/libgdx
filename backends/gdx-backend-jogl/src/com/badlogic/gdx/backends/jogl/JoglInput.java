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

package com.badlogic.gdx.backends.jogl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.nativewindow.util.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;

public class JoglInput implements Input, MouseListener, KeyListener {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		long timeStamp;
		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;
		static final int TOUCH_MOVED = 3;
		static final int TOUCH_SCROLLED = 4;

		long timeStamp;
		int type;
		int x;
		int y;
		int pointer;
		int button;
		float scrollAmount;
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
	int touchX = 0;
	int touchY = 0;
	int deltaX = 0;
	int deltaY = 0;
	boolean touchDown = false;
	boolean justTouched = false;
	Set<Integer> keys = new HashSet<Integer>();
	Set<Integer> pressedButtons = new HashSet<Integer>();
	InputProcessor processor;
	GLWindow canvas;
	boolean catched = false;
	long currentEventTimeStamp;

	public JoglInput (GLWindow canvas) {
		setListeners(canvas);
	}

	public void setListeners (GLWindow canvas) {
		if (this.canvas != null) {
			canvas.removeMouseListener(this);
			canvas.removeKeyListener(this);
		}
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		this.canvas = canvas;
	}

	@Override
	public float getAccelerometerX () {
		return 0;
	}

	@Override
	public float getAccelerometerY () {
		return 0;
	}

	@Override
	public float getAccelerometerZ () {
		return 0;
	}

	@Override
	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		throw new UnsupportedOperationException("getTextInput not supported by JoglInput, rather use JoglAWTInput");
	}

	
	public void getPlaceholderTextInput (final TextInputListener listener, final String title, final String placeholder) {
		throw new UnsupportedOperationException("getPlaceholderTextInput not supported by JoglInput, rather use JoglAWTInput");
	}

	@Override
	public int getX () {
		return touchX;
	}

	@Override
	public int getX (int pointer) {
		if (pointer == 0)
			return touchX;
		else
			return 0;
	}

	@Override
	public int getY () {
		return touchY;
	}

	@Override
	public int getY (int pointer) {
		if (pointer == 0)
			return touchY;
		else
			return 0;
	}

	@Override
	public boolean isKeyPressed (int key) {
		synchronized (this) {
			if (key == Input.Keys.ANY_KEY)
				return keys.size() > 0;
			else
				return keys.contains(key);
		}
	}

	@Override
	public boolean isTouched () {
		return touchDown;
	}

	@Override
	public boolean isTouched (int pointer) {
		if (pointer == 0)
			return touchDown;
		else
			return false;
	}

	void processEvents () {
		synchronized (this) {
			justTouched = false;

			if (processor != null) {
				InputProcessor processor = this.processor;

				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
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
					currentEventTimeStamp = e.timeStamp;
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
						processor.mouseMoved(e.x, e.y);
						break;
					case TouchEvent.TOUCH_SCROLLED:
						processor.scrolled((int) e.scrollAmount);
						break;
					}
					usedTouchEvents.free(e);
				}
			} else {
				int len = touchEvents.size();
				for (int i = 0; i < len; i++) {
					TouchEvent event = touchEvents.get(i);
					if (event.type == TouchEvent.TOUCH_DOWN) justTouched = true;
					usedTouchEvents.free(event);
				}

				len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					usedKeyEvents.free(keyEvents.get(i));
				}
			}

			if (touchEvents.size() == 0) {
				deltaX = 0;
				deltaY = 0;
			}

			keyEvents.clear();
			touchEvents.clear();
		}
	}

	@Override
	public void setCatchBackKey (boolean catchBack) {

	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {

	}

	@Override
	public void mouseDragged (MouseEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DRAGGED;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			checkCatched(e);
		}
	}

	@Override
	public void mouseMoved (MouseEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_MOVED;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			checkCatched(e);
		}
	}

	@Override
	public void mouseClicked (MouseEvent arg0) {
	}

	@Override
	public void mouseEntered (MouseEvent e) {
		touchX = e.getX();
		touchY = e.getY();
		checkCatched(e);
	}

	@Override
	public void mouseExited (MouseEvent e) {
		checkCatched(e);
	}

	private void checkCatched (MouseEvent e) {
		if (catched && canvas.isVisible()) {
			Point point=new Point();
			canvas.getLocationOnScreen(point);
			int x = Math.max(0, Math.min(e.getX(), canvas.getWidth()) - 1) + point.getX();
			int y = Math.max(0, Math.min(e.getY(), canvas.getHeight()) - 1) + point.getY();
			if (e.getX() < 0 || e.getX() >= canvas.getWidth() || e.getY() < 0 || e.getY() >= canvas.getHeight()) {
				canvas.warpPointer(x, y);
			}
		}
	}

	private int toGdxButton (int swingButton) {
		if (swingButton == MouseEvent.BUTTON1) return Buttons.LEFT;
		if (swingButton == MouseEvent.BUTTON2) return Buttons.MIDDLE;
		if (swingButton == MouseEvent.BUTTON3) return Buttons.RIGHT;
		return Buttons.LEFT;
	}

	@Override
	public void mousePressed (MouseEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DOWN;
			event.button = toGdxButton(e.getButton());
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			touchDown = true;
			pressedButtons.add(event.button);
		}
	}

	@Override
	public void mouseReleased (MouseEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.button = toGdxButton(e.getButton());
			event.type = TouchEvent.TOUCH_UP;
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);

			deltaX = event.x - touchX;
			deltaY = event.y - touchY;
			touchX = event.x;
			touchY = event.y;
			pressedButtons.remove(event.button);
			if (pressedButtons.size() == 0) touchDown = false;
		}
	}

	@Override
	public void mouseWheelMoved (MouseEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.type = TouchEvent.TOUCH_SCROLLED;
			// JogAmp JOGL NEWT wheel UP == libgdx wheel DOWN
			event.scrollAmount = -1.0f * e.getWheelRotation();
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);
		}
	}

	@Override
	public void keyPressed (com.jogamp.newt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_DOWN;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			keys.add(event.keyCode);
		}
	}

	@Override
	public void keyReleased (com.jogamp.newt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_UP;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			keys.remove(event.keyCode);
		}
	}

	@Override
	public void keyTyped (com.jogamp.newt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = e.getKeyChar();
			event.keyCode = 0;
			event.type = KeyEvent.KEY_TYPED;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
		}
	}

	protected static int translateKeyCode (int keyCode) {
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_ADD) return Input.Keys.PLUS;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_SUBTRACT) return Input.Keys.MINUS;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_0) return Input.Keys.NUM_0;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_1) return Input.Keys.NUM_1;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_2) return Input.Keys.NUM_2;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_3) return Input.Keys.NUM_3;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_4) return Input.Keys.NUM_4;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_5) return Input.Keys.NUM_5;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_6) return Input.Keys.NUM_6;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_7) return Input.Keys.NUM_7;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_8) return Input.Keys.NUM_8;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_9) return Input.Keys.NUM_9;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_A) return Input.Keys.A;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_B) return Input.Keys.B;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_C) return Input.Keys.C;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_D) return Input.Keys.D;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_E) return Input.Keys.E;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F) return Input.Keys.F;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_G) return Input.Keys.G;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_H) return Input.Keys.H;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_I) return Input.Keys.I;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_J) return Input.Keys.J;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_K) return Input.Keys.K;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_L) return Input.Keys.L;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_M) return Input.Keys.M;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_N) return Input.Keys.N;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_O) return Input.Keys.O;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_P) return Input.Keys.P;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_Q) return Input.Keys.Q;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_R) return Input.Keys.R;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_S) return Input.Keys.S;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_T) return Input.Keys.T;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_U) return Input.Keys.U;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_V) return Input.Keys.V;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_W) return Input.Keys.W;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_X) return Input.Keys.X;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_Y) return Input.Keys.Y;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_Z) return Input.Keys.Z;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_ALT) return Input.Keys.ALT_LEFT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_ALT_GRAPH) return Input.Keys.ALT_RIGHT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_BACK_SLASH) return Input.Keys.BACKSLASH;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_COMMA) return Input.Keys.COMMA;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_DELETE) return Input.Keys.DEL;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_LEFT) return Input.Keys.DPAD_LEFT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_RIGHT) return Input.Keys.DPAD_RIGHT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_UP) return Input.Keys.DPAD_UP;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_DOWN) return Input.Keys.DPAD_DOWN;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_ENTER) return Input.Keys.ENTER;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_HOME) return Input.Keys.HOME;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_MINUS) return Input.Keys.MINUS;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_PERIOD) return Input.Keys.PERIOD;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_PLUS) return Input.Keys.PLUS;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_SEMICOLON) return Input.Keys.SEMICOLON;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_SHIFT) return Input.Keys.SHIFT_LEFT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_SLASH) return Input.Keys.SLASH;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_SPACE) return Input.Keys.SPACE;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_TAB) return Input.Keys.TAB;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE) return Input.Keys.DEL;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_CONTROL) return Input.Keys.CONTROL_LEFT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_ESCAPE) return Input.Keys.ESCAPE;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_END) return Input.Keys.END;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_INSERT) return Input.Keys.INSERT;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD5) return Input.Keys.DPAD_CENTER;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_PAGE_UP) return Input.Keys.PAGE_UP;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_PAGE_DOWN) return Input.Keys.PAGE_DOWN;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F1) return Input.Keys.F1;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F2) return Input.Keys.F2;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F3) return Input.Keys.F3;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F4) return Input.Keys.F4;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F5) return Input.Keys.F5;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F6) return Input.Keys.F6;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F7) return Input.Keys.F7;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F8) return Input.Keys.F8;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F9) return Input.Keys.F9;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F10) return Input.Keys.F10;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F11) return Input.Keys.F11;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_F12) return Input.Keys.F12;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_COLON) return Input.Keys.COLON;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD0) return Input.Keys.NUM_0;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD1) return Input.Keys.NUM_1;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD2) return Input.Keys.NUM_2;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD3) return Input.Keys.NUM_3;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD4) return Input.Keys.NUM_4;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD5) return Input.Keys.NUM_5;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD6) return Input.Keys.NUM_6;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD7) return Input.Keys.NUM_7;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD8) return Input.Keys.NUM_8;
		if (keyCode == com.jogamp.newt.event.KeyEvent.VK_NUMPAD9) return Input.Keys.NUM_9;

		return Input.Keys.UNKNOWN;
	}

	@Override
	public void setInputProcessor (InputProcessor processor) {
		synchronized (this) {
			this.processor = processor;
		}
	}

	@Override
	public InputProcessor getInputProcessor () {
		return this.processor;
	}

	@Override
	public void vibrate (int milliseconds) {
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isButtonPressed (int button) {
		return pressedButtons.contains(button);
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
	}

	@Override
	public void cancelVibrate () {
	}

	@Override
	public float getAzimuth () {
		return 0;
	}

	@Override
	public float getPitch () {
		return 0;
	}

	@Override
	public float getRoll () {
		return 0;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		if (peripheral == Peripheral.HardwareKeyboard) return true;
		return false;
	}

	@Override
	public int getRotation () {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation () {
		return Orientation.Landscape;
	}

	@Override
	public void setCursorCatched (boolean catched) {
		this.catched = catched;
		showCursor(!catched);
	}

	private void showCursor (boolean visible) {
		canvas.setPointerVisible(visible);
	}

	@Override
	public boolean isCursorCatched () {
		return catched;
	}

	@Override
	public int getDeltaX () {
		return deltaX;
	}

	@Override
	public int getDeltaX (int pointer) {
		if (pointer == 0) return deltaX;
		return 0;
	}

	@Override
	public int getDeltaY () {
		return deltaY;
	}

	@Override
	public int getDeltaY (int pointer) {
		if (pointer == 0) return deltaY;
		return 0;
	}

	@Override
	public void setCursorPosition (int x, int y) {
		//TODO use canvas.getLocationOnScreen(Point)?
		canvas.warpPointer(x, y);
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
	}

	@Override
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
	}
}

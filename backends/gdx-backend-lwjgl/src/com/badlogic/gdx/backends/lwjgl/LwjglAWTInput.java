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

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;

public class LwjglAWTInput implements Input, MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {
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
		int scrollAmount;
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

	private final LwjglAWTCanvas lwjglAwtCanvas;
	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	int touchX = 0;
	int touchY = 0;
	int deltaX = 0;
	int deltaY = 0;
	boolean touchDown = false;
	boolean justTouched = false;
	int keyCount = 0;
	boolean[] keys = new boolean[256];
	boolean keyJustPressed = false;
	boolean[] justPressedKeys = new boolean[256];
	boolean[] justPressedButtons = new boolean[5];
	IntSet pressedButtons = new IntSet();
	InputProcessor processor;
	Canvas canvas;
	boolean catched = false;
	Robot robot = null;
	long currentEventTimeStamp;

	public LwjglAWTInput (LwjglAWTCanvas lwjglAwtCanvas) {
		this.lwjglAwtCanvas = lwjglAwtCanvas;
		setListeners(lwjglAwtCanvas.getCanvas());
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (HeadlessException e) {
		} catch (AWTException e) {
		}
	}

	public void setListeners (Canvas canvas) {
		if (this.canvas != null) {
			canvas.removeMouseListener(this);
			canvas.removeMouseMotionListener(this);
			canvas.removeMouseWheelListener(this);
			canvas.removeKeyListener(this);
		}
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addKeyListener(this);
		canvas.setFocusTraversalKeysEnabled(false);
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

	public void getTextInput (final TextInputListener listener, final String title, final String text, final String hint) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				JPanel panel = new JPanel(new FlowLayout());

				JPanel textPanel = new JPanel() {
					public boolean isOptimizedDrawingEnabled () {
						return false;
					};
				};

				textPanel.setLayout(new OverlayLayout(textPanel));
				panel.add(textPanel);

				final JTextField textField = new JTextField(20);
				textField.setText(text);
				textField.setAlignmentX(0.0f);
				textPanel.add(textField);

				final JLabel placeholderLabel = new JLabel(hint);
				placeholderLabel.setForeground(Color.GRAY);
				placeholderLabel.setAlignmentX(0.0f);
				textPanel.add(placeholderLabel, 0);

				textField.getDocument().addDocumentListener(new DocumentListener() {

					@Override
					public void removeUpdate (DocumentEvent arg0) {
						this.updated();
					}

					@Override
					public void insertUpdate (DocumentEvent arg0) {
						this.updated();
					}

					@Override
					public void changedUpdate (DocumentEvent arg0) {
						this.updated();
					}

					private void updated () {
						if (textField.getText().length() == 0)
							placeholderLabel.setVisible(true);
						else
							placeholderLabel.setVisible(false);
					}
				});

				JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null,
					null);

				pane.setInitialValue(null);
				pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());

				Border border = textField.getBorder();
				placeholderLabel.setBorder(new EmptyBorder(border.getBorderInsets(textField)));

				JDialog dialog = pane.createDialog(null, title);
				pane.selectInitialValue();

				dialog.addWindowFocusListener(new WindowFocusListener() {

					@Override
					public void windowLostFocus (WindowEvent arg0) {
					}

					@Override
					public void windowGainedFocus (WindowEvent arg0) {
						textField.requestFocusInWindow();
					}
				});

				dialog.setVisible(true);
				dialog.dispose();

				Object selectedValue = pane.getValue();

				if (selectedValue != null && (selectedValue instanceof Integer)
					&& ((Integer)selectedValue).intValue() == JOptionPane.OK_OPTION) {
					listener.input(textField.getText());
				} else {
					listener.canceled();
				}

			}
		});
	}

	@Override
	public int getMaxPointers () {
		return 1;
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
	public synchronized boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyCount > 0;
		}
		if (key < 0 || key > 255) {
			return false;
		}
		return keys[key];
	}

	@Override
	public synchronized boolean isKeyJustPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyJustPressed;
		}
		if (key < 0 || key > 255) {
			return false;
		}
		return justPressedKeys[key];
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

	@Override
	public float getPressure () {
		return getPressure(0);
	}

	@Override
	public float getPressure (int pointer) {
		return isTouched(pointer) ? 1 : 0;
	}

	void processEvents () {
		synchronized (this) {
			if (justTouched) {
				justTouched = false;
				for (int i = 0; i < justPressedButtons.length; i++) {
					justPressedButtons[i] = false;
				}
			}
			if (keyJustPressed) {
				keyJustPressed = false;
				for (int i = 0; i < justPressedKeys.length; i++) {
					justPressedKeys[i] = false;
				}
			}

			if (processor != null) {
				InputProcessor processor = this.processor;

				int len = keyEvents.size();
				for (int i = 0; i < len; i++) {
					KeyEvent e = keyEvents.get(i);
					currentEventTimeStamp = e.timeStamp;
					switch (e.type) {
					case KeyEvent.KEY_DOWN:
						processor.keyDown(e.keyCode);
						keyJustPressed = true;
						justPressedKeys[e.keyCode] = true;
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
						justPressedButtons[e.button] = true;
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
						processor.scrolled(e.scrollAmount);
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

			if (touchEvents.isEmpty()) {
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
	public boolean isCatchBackKey () {
		return false;
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {

	}

	@Override
	public boolean isCatchMenuKey () {
		return false;
	}

	@Override
	public void setCatchKey (int keycode, boolean catchKey) {

	}

	@Override
	public boolean isCatchKey (int keycode) {
		return false;
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
			lwjglAwtCanvas.graphics.requestRendering();
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
			lwjglAwtCanvas.graphics.requestRendering();
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
		lwjglAwtCanvas.graphics.requestRendering();
	}

	@Override
	public void mouseExited (MouseEvent e) {
		checkCatched(e);
		lwjglAwtCanvas.graphics.requestRendering();
	}

	private void checkCatched (MouseEvent e) {
		if (catched && robot != null && canvas.isShowing()) {
			int x = Math.max(0, Math.min(e.getX(), canvas.getWidth()) - 1) + canvas.getLocationOnScreen().x;
			int y = Math.max(0, Math.min(e.getY(), canvas.getHeight()) - 1) + canvas.getLocationOnScreen().y;
			if (e.getX() < 0 || e.getX() >= canvas.getWidth() || e.getY() < 0 || e.getY() >= canvas.getHeight()) {
				robot.mouseMove(x, y);
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
			lwjglAwtCanvas.graphics.requestRendering();
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
			if (pressedButtons.size == 0) touchDown = false;
			lwjglAwtCanvas.graphics.requestRendering();
		}
	}

	@Override
	public void mouseWheelMoved (MouseWheelEvent e) {
		synchronized (this) {
			TouchEvent event = usedTouchEvents.obtain();
			event.pointer = 0;
			event.type = TouchEvent.TOUCH_SCROLLED;
			event.scrollAmount = e.getWheelRotation();
			event.timeStamp = System.nanoTime();
			touchEvents.add(event);
			lwjglAwtCanvas.graphics.requestRendering();
		}
	}

	@Override
	public void keyPressed (java.awt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_DOWN;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			if (!keys[event.keyCode]) {
				keyCount++;
				keys[event.keyCode] = true;
			}
			lwjglAwtCanvas.graphics.requestRendering();
		}
	}

	@Override
	public void keyReleased (java.awt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_UP;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			if (keys[event.keyCode]) {
				keyCount--;
				keys[event.keyCode] = false;
			}
			lwjglAwtCanvas.graphics.requestRendering();
		}
	}

	@Override
	public void keyTyped (java.awt.event.KeyEvent e) {
		synchronized (this) {
			KeyEvent event = usedKeyEvents.obtain();
			event.keyChar = e.getKeyChar();
			event.keyCode = 0;
			event.type = KeyEvent.KEY_TYPED;
			event.timeStamp = System.nanoTime();
			keyEvents.add(event);
			lwjglAwtCanvas.graphics.requestRendering();
		}
	}

	protected static int translateKeyCode (int keyCode) {
		switch (keyCode) {
		case java.awt.event.KeyEvent.VK_0:
			return Input.Keys.NUM_0;
		case java.awt.event.KeyEvent.VK_1:
			return Input.Keys.NUM_1;
		case java.awt.event.KeyEvent.VK_2:
			return Input.Keys.NUM_2;
		case java.awt.event.KeyEvent.VK_3:
			return Input.Keys.NUM_3;
		case java.awt.event.KeyEvent.VK_4:
			return Input.Keys.NUM_4;
		case java.awt.event.KeyEvent.VK_5:
			return Input.Keys.NUM_5;
		case java.awt.event.KeyEvent.VK_6:
			return Input.Keys.NUM_6;
		case java.awt.event.KeyEvent.VK_7:
			return Input.Keys.NUM_7;
		case java.awt.event.KeyEvent.VK_8:
			return Input.Keys.NUM_8;
		case java.awt.event.KeyEvent.VK_9:
			return Input.Keys.NUM_9;
		case java.awt.event.KeyEvent.VK_A:
			return Input.Keys.A;
		case java.awt.event.KeyEvent.VK_B:
			return Input.Keys.B;
		case java.awt.event.KeyEvent.VK_C:
			return Input.Keys.C;
		case java.awt.event.KeyEvent.VK_D:
			return Input.Keys.D;
		case java.awt.event.KeyEvent.VK_E:
			return Input.Keys.E;
		case java.awt.event.KeyEvent.VK_F:
			return Input.Keys.F;
		case java.awt.event.KeyEvent.VK_G:
			return Input.Keys.G;
		case java.awt.event.KeyEvent.VK_H:
			return Input.Keys.H;
		case java.awt.event.KeyEvent.VK_I:
			return Input.Keys.I;
		case java.awt.event.KeyEvent.VK_J:
			return Input.Keys.J;
		case java.awt.event.KeyEvent.VK_K:
			return Input.Keys.K;
		case java.awt.event.KeyEvent.VK_L:
			return Input.Keys.L;
		case java.awt.event.KeyEvent.VK_M:
			return Input.Keys.M;
		case java.awt.event.KeyEvent.VK_N:
			return Input.Keys.N;
		case java.awt.event.KeyEvent.VK_O:
			return Input.Keys.O;
		case java.awt.event.KeyEvent.VK_P:
			return Input.Keys.P;
		case java.awt.event.KeyEvent.VK_Q:
			return Input.Keys.Q;
		case java.awt.event.KeyEvent.VK_R:
			return Input.Keys.R;
		case java.awt.event.KeyEvent.VK_S:
			return Input.Keys.S;
		case java.awt.event.KeyEvent.VK_T:
			return Input.Keys.T;
		case java.awt.event.KeyEvent.VK_U:
			return Input.Keys.U;
		case java.awt.event.KeyEvent.VK_V:
			return Input.Keys.V;
		case java.awt.event.KeyEvent.VK_W:
			return Input.Keys.W;
		case java.awt.event.KeyEvent.VK_X:
			return Input.Keys.X;
		case java.awt.event.KeyEvent.VK_Y:
			return Input.Keys.Y;
		case java.awt.event.KeyEvent.VK_Z:
			return Input.Keys.Z;
		case java.awt.event.KeyEvent.VK_ALT:
			return Input.Keys.ALT_LEFT;
		case java.awt.event.KeyEvent.VK_ALT_GRAPH:
			return Input.Keys.ALT_RIGHT;
		case java.awt.event.KeyEvent.VK_BACK_SLASH:
			return Input.Keys.BACKSLASH;
		case java.awt.event.KeyEvent.VK_COMMA:
			return Input.Keys.COMMA;
		case java.awt.event.KeyEvent.VK_DELETE:
			return Input.Keys.FORWARD_DEL;
		case java.awt.event.KeyEvent.VK_LEFT:
			return Input.Keys.DPAD_LEFT;
		case java.awt.event.KeyEvent.VK_RIGHT:
			return Input.Keys.DPAD_RIGHT;
		case java.awt.event.KeyEvent.VK_UP:
			return Input.Keys.DPAD_UP;
		case java.awt.event.KeyEvent.VK_DOWN:
			return Input.Keys.DPAD_DOWN;
		case java.awt.event.KeyEvent.VK_ENTER:
			return Input.Keys.ENTER;
		case java.awt.event.KeyEvent.VK_HOME:
			return Input.Keys.HOME;
		case java.awt.event.KeyEvent.VK_MINUS:
			return Input.Keys.MINUS;
		case java.awt.event.KeyEvent.VK_PERIOD:
			return Input.Keys.PERIOD;
		case java.awt.event.KeyEvent.VK_PLUS:
		case java.awt.event.KeyEvent.VK_ADD:
			return Input.Keys.PLUS;
		case java.awt.event.KeyEvent.VK_SEMICOLON:
			return Input.Keys.SEMICOLON;
		case java.awt.event.KeyEvent.VK_SHIFT:
			return Input.Keys.SHIFT_LEFT;
		case java.awt.event.KeyEvent.VK_SLASH:
			return Input.Keys.SLASH;
		case java.awt.event.KeyEvent.VK_SPACE:
			return Input.Keys.SPACE;
		case java.awt.event.KeyEvent.VK_TAB:
			return Input.Keys.TAB;
		case java.awt.event.KeyEvent.VK_BACK_SPACE:
			return Input.Keys.DEL;
		case java.awt.event.KeyEvent.VK_QUOTE:
			return Input.Keys.APOSTROPHE;
		case java.awt.event.KeyEvent.VK_ASTERISK:
		case java.awt.event.KeyEvent.VK_MULTIPLY:
			return Input.Keys.STAR;
		case java.awt.event.KeyEvent.VK_SUBTRACT:
			return Input.Keys.MINUS;
		case java.awt.event.KeyEvent.VK_CONTROL:
			return Input.Keys.CONTROL_LEFT;
		case java.awt.event.KeyEvent.VK_ESCAPE:
			return Input.Keys.ESCAPE;
		case java.awt.event.KeyEvent.VK_END:
			return Input.Keys.END;
		case java.awt.event.KeyEvent.VK_INSERT:
			return Input.Keys.INSERT;
		case java.awt.event.KeyEvent.VK_PAGE_UP:
			return Input.Keys.PAGE_UP;
		case java.awt.event.KeyEvent.VK_PAGE_DOWN:
			return Input.Keys.PAGE_DOWN;
		case java.awt.event.KeyEvent.VK_F1:
			return Input.Keys.F1;
		case java.awt.event.KeyEvent.VK_F2:
			return Input.Keys.F2;
		case java.awt.event.KeyEvent.VK_F3:
			return Input.Keys.F3;
		case java.awt.event.KeyEvent.VK_F4:
			return Input.Keys.F4;
		case java.awt.event.KeyEvent.VK_F5:
			return Input.Keys.F5;
		case java.awt.event.KeyEvent.VK_F6:
			return Input.Keys.F6;
		case java.awt.event.KeyEvent.VK_F7:
			return Input.Keys.F7;
		case java.awt.event.KeyEvent.VK_F8:
			return Input.Keys.F8;
		case java.awt.event.KeyEvent.VK_F9:
			return Input.Keys.F9;
		case java.awt.event.KeyEvent.VK_F10:
			return Input.Keys.F10;
		case java.awt.event.KeyEvent.VK_F11:
			return Input.Keys.F11;
		case java.awt.event.KeyEvent.VK_F12:
			return Input.Keys.F12;
		case java.awt.event.KeyEvent.VK_COLON:
			return Input.Keys.COLON;
		case java.awt.event.KeyEvent.VK_NUMPAD0:
			return Input.Keys.NUM_0;
		case java.awt.event.KeyEvent.VK_NUMPAD1:
			return Input.Keys.NUM_1;
		case java.awt.event.KeyEvent.VK_NUMPAD2:
			return Input.Keys.NUM_2;
		case java.awt.event.KeyEvent.VK_NUMPAD3:
			return Input.Keys.NUM_3;
		case java.awt.event.KeyEvent.VK_NUMPAD4:
			return Input.Keys.NUM_4;
		case java.awt.event.KeyEvent.VK_NUMPAD5:
			return Input.Keys.NUM_5;
		case java.awt.event.KeyEvent.VK_NUMPAD6:
			return Input.Keys.NUM_6;
		case java.awt.event.KeyEvent.VK_NUMPAD7:
			return Input.Keys.NUM_7;
		case java.awt.event.KeyEvent.VK_NUMPAD8:
			return Input.Keys.NUM_8;
		case java.awt.event.KeyEvent.VK_NUMPAD9:
			return Input.Keys.NUM_9;
		}
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
	public boolean isButtonJustPressed(int button) {
		if(button < 0 || button >= justPressedButtons.length) return false;
		return justPressedButtons[button];
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
		if (!visible) {
			Toolkit t = Toolkit.getDefaultToolkit();
			Image i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Cursor noCursor = t.createCustomCursor(i, new Point(0, 0), "none");
			JFrame frame = findJFrame(canvas);
			frame.setCursor(noCursor);
		} else {
			JFrame frame = findJFrame(canvas);
			frame.setCursor(Cursor.getDefaultCursor());
		}
	}

	protected static JFrame findJFrame (Component component) {
		Container parent = component.getParent();
		while (parent != null) {
			if (parent instanceof JFrame) {
				return (JFrame)parent;
			}
			parent = parent.getParent();
		}

		return null;
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
		if (robot != null) {
			robot.mouseMove(canvas.getLocationOnScreen().x + x, canvas.getLocationOnScreen().y + y);
		}
	}

	@Override
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
	}

	@Override
	public float getGyroscopeX () {
		return 0;
	}

	@Override
	public float getGyroscopeY () {
		return 0;
	}

	@Override
	public float getGyroscopeZ () {
		return 0;
	}
}

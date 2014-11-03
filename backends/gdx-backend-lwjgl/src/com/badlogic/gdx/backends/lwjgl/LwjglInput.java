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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntSet;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.*;

/** An implementation of the {@link Input} interface hooking a LWJGL panel for input.
 *
 * @author mzechner */
final public class LwjglInput implements Input {
	static public float keyRepeatInitialTime = 0.4f;
	static public float keyRepeatTime = 0.1f;

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	boolean mousePressed = false;
	int mouseX, mouseY;
	int deltaX, deltaY;
	int pressedKeys = 0;
	boolean keyJustPressed = false;
	boolean[] justPressedKeys = new boolean[256];
	boolean justTouched = false;
	IntSet pressedButtons = new IntSet();
	InputProcessor processor;
	char lastKeyCharPressed;
	float keyRepeatTimer;
	long currentEventTimeStamp;
	float deltaTime;
	long lastTime;

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

    private static final IntIntMap lwjglToGdxKeysMap = new IntIntMap( 256 );
    private static final IntIntMap gdxToLwjglKeysMap = new IntIntMap( 256 );

    private static final int[][] keyMapping = {
        { Keyboard.KEY_LBRACKET, Input.Keys.LEFT_BRACKET },
        { Keyboard.KEY_RBRACKET, Input.Keys.RIGHT_BRACKET },
        { Keyboard.KEY_GRAVE, Input.Keys.GRAVE },
        { Keyboard.KEY_RMETA, Input.Keys.META_RIGHT },
        { Keyboard.KEY_LMETA, Input.Keys.META_LEFT },
        { Keyboard.KEY_NUMLOCK, Input.Keys.NUMLOCK },
        { Keyboard.KEY_NUMPADEQUALS, Input.Keys.NUMPAD_EQUALS },
        { Keyboard.KEY_NUMPADCOMMA, Input.Keys.NUMPAD_COMMA },
        { Keyboard.KEY_NUMPADENTER, Input.Keys.NUMPAD_ENTER },
        { Keyboard.KEY_ADD, Input.Keys.NUMPAD_ADD },
        { Keyboard.KEY_SUBTRACT, Input.Keys.NUMPAD_SUBTRACT },
        { Keyboard.KEY_MULTIPLY, Input.Keys.NUMPAD_MULTIPLY },
        { Keyboard.KEY_DIVIDE, Input.Keys.NUMPAD_DIVIDE },
        { Keyboard.KEY_DECIMAL, Input.Keys.NUMPAD_DOT },
        { Keyboard.KEY_NUMPAD0, Input.Keys.NUMPAD_0 },
        { Keyboard.KEY_NUMPAD1, Input.Keys.NUMPAD_1 },
        { Keyboard.KEY_NUMPAD2, Input.Keys.NUMPAD_2 },
        { Keyboard.KEY_NUMPAD3, Input.Keys.NUMPAD_3 },
        { Keyboard.KEY_NUMPAD4, Input.Keys.NUMPAD_4 },
        { Keyboard.KEY_NUMPAD5, Input.Keys.NUMPAD_5 },
        { Keyboard.KEY_NUMPAD6, Input.Keys.NUMPAD_6 },
        { Keyboard.KEY_NUMPAD7, Input.Keys.NUMPAD_7 },
        { Keyboard.KEY_NUMPAD8, Input.Keys.NUMPAD_8 },
        { Keyboard.KEY_NUMPAD9, Input.Keys.NUMPAD_9 },
        { Keyboard.KEY_0, Input.Keys.NUM_0 },
        { Keyboard.KEY_1, Input.Keys.NUM_1 },
        { Keyboard.KEY_2, Input.Keys.NUM_2 },
        { Keyboard.KEY_3, Input.Keys.NUM_3 },
        { Keyboard.KEY_4, Input.Keys.NUM_4 },
        { Keyboard.KEY_5, Input.Keys.NUM_5 },
        { Keyboard.KEY_6, Input.Keys.NUM_6 },
        { Keyboard.KEY_7, Input.Keys.NUM_7 },
        { Keyboard.KEY_8, Input.Keys.NUM_8 },
        { Keyboard.KEY_9, Input.Keys.NUM_9 },
        { Keyboard.KEY_A, Input.Keys.A },
        { Keyboard.KEY_B, Input.Keys.B },
        { Keyboard.KEY_C, Input.Keys.C },
        { Keyboard.KEY_D, Input.Keys.D },
        { Keyboard.KEY_E, Input.Keys.E },
        { Keyboard.KEY_F, Input.Keys.F },
        { Keyboard.KEY_G, Input.Keys.G },
        { Keyboard.KEY_H, Input.Keys.H },
        { Keyboard.KEY_I, Input.Keys.I },
        { Keyboard.KEY_J, Input.Keys.J },
        { Keyboard.KEY_K, Input.Keys.K },
        { Keyboard.KEY_L, Input.Keys.L },
        { Keyboard.KEY_M, Input.Keys.M },
        { Keyboard.KEY_N, Input.Keys.N },
        { Keyboard.KEY_O, Input.Keys.O },
        { Keyboard.KEY_P, Input.Keys.P },
        { Keyboard.KEY_Q, Input.Keys.Q },
        { Keyboard.KEY_R, Input.Keys.R },
        { Keyboard.KEY_S, Input.Keys.S },
        { Keyboard.KEY_T, Input.Keys.T },
        { Keyboard.KEY_U, Input.Keys.U },
        { Keyboard.KEY_V, Input.Keys.V },
        { Keyboard.KEY_W, Input.Keys.W },
        { Keyboard.KEY_X, Input.Keys.X },
        { Keyboard.KEY_Y, Input.Keys.Y },
        { Keyboard.KEY_Z, Input.Keys.Z },
        { Keyboard.KEY_AT, Input.Keys.AT },
        { Keyboard.KEY_LMENU, Input.Keys.ALT_LEFT },
        { Keyboard.KEY_RMENU, Input.Keys.ALT_RIGHT },
        { Keyboard.KEY_EQUALS, Input.Keys.EQUALS },
        { Keyboard.KEY_MINUS, Input.Keys.MINUS },
        { Keyboard.KEY_BACKSLASH, Input.Keys.BACKSLASH },
        { Keyboard.KEY_COMMA, Input.Keys.COMMA },
        { Keyboard.KEY_DELETE, Input.Keys.FORWARD_DEL },
        { Keyboard.KEY_LEFT, Input.Keys.DPAD_LEFT },
        { Keyboard.KEY_RIGHT, Input.Keys.DPAD_RIGHT },
        { Keyboard.KEY_UP, Input.Keys.DPAD_UP },
        { Keyboard.KEY_DOWN, Input.Keys.DPAD_DOWN },
        { Keyboard.KEY_RETURN, Input.Keys.ENTER },
        { Keyboard.KEY_HOME, Input.Keys.HOME },
        { Keyboard.KEY_PERIOD, Input.Keys.PERIOD },
        { Keyboard.KEY_SEMICOLON, Input.Keys.SEMICOLON },
        { Keyboard.KEY_LSHIFT, Input.Keys.SHIFT_LEFT },
        { Keyboard.KEY_RSHIFT, Input.Keys.SHIFT_RIGHT },
        { Keyboard.KEY_SLASH, Input.Keys.SLASH },
        { Keyboard.KEY_SPACE, Input.Keys.SPACE },
        { Keyboard.KEY_TAB, Input.Keys.TAB },
        { Keyboard.KEY_LCONTROL, Input.Keys.CONTROL_LEFT },
        { Keyboard.KEY_RCONTROL, Input.Keys.CONTROL_RIGHT },
        { Keyboard.KEY_NEXT, Input.Keys.PAGE_DOWN },
        { Keyboard.KEY_PRIOR, Input.Keys.PAGE_UP },
        { Keyboard.KEY_ESCAPE, Input.Keys.ESCAPE },
        { Keyboard.KEY_END, Input.Keys.END },
        { Keyboard.KEY_INSERT, Input.Keys.INSERT },
        { Keyboard.KEY_BACK, Input.Keys.DEL },
        { Keyboard.KEY_APOSTROPHE, Input.Keys.APOSTROPHE },
        { Keyboard.KEY_F1, Input.Keys.F1 },
        { Keyboard.KEY_F2, Input.Keys.F2 },
        { Keyboard.KEY_F3, Input.Keys.F3 },
        { Keyboard.KEY_F4, Input.Keys.F4 },
        { Keyboard.KEY_F5, Input.Keys.F5 },
        { Keyboard.KEY_F6, Input.Keys.F6 },
        { Keyboard.KEY_F7, Input.Keys.F7 },
        { Keyboard.KEY_F8, Input.Keys.F8 },
        { Keyboard.KEY_F9, Input.Keys.F9 },
        { Keyboard.KEY_F10, Input.Keys.F10 },
        { Keyboard.KEY_F11, Input.Keys.F11 },
        { Keyboard.KEY_F12, Input.Keys.F12 },
        { Keyboard.KEY_COLON, Input.Keys.COLON }, //
    };

    static {
        for( int[] map : keyMapping ) {
            lwjglToGdxKeysMap.put( map[0], map[1] );
            gdxToLwjglKeysMap.put( map[1], map[0] );
        }
    }

    public static int getGdxKeyCode( int lwjglKeyCode ) {
        return lwjglToGdxKeysMap.get( lwjglKeyCode, Input.Keys.UNKNOWN );
    }

    public static int getLwjglKeyCode( int gdxKeyCode ) {
        return gdxToLwjglKeysMap.get( gdxKeyCode, Keyboard.KEY_NONE );
    }

	public LwjglInput () {
		Keyboard.enableRepeatEvents(false);
		Mouse.setClipMouseCoordinatesToWindow(false);
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
				final String output = JOptionPane.showInputDialog(null, title, text);
				if (output != null)
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							listener.input(output);
						}
					});
				else
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							listener.canceled();
						}
					});
			}
		});
	}

	public void getPlaceholderTextInput (final TextInputListener listener, final String title, final String placeholder) {
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
				textField.setAlignmentX(0.0f);
				textPanel.add(textField);

				final JLabel placeholderLabel = new JLabel(placeholder);
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

	public int getX () {
		return Mouse.getX();
	}

	public int getY () {
		return Gdx.graphics.getHeight() - 1 - Mouse.getY();
	}

	public boolean isAccelerometerAvailable () {
		return false;
	}

	public boolean isKeyPressed (int key) {
		if (!Keyboard.isCreated()) return false;

		if (key == Input.Keys.ANY_KEY)
			return pressedKeys > 0;
		else
			return Keyboard.isKeyDown(getLwjglKeyCode(key));
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		if (key == Input.Keys.ANY_KEY) {
			return keyJustPressed;
		}
		if (key < 0 || key > 255) {
			return false;
		}
		return justPressedKeys[key];
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

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {

	}

	@Override
	public void setCatchBackKey (boolean catchBack) {

	}

	@Override
	public boolean isCatchBackKey () {
		return false;
	}

	void processEvents () {
		synchronized (this) {
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

	public void update () {
		updateTime();
		updateMouse();
		updateKeyboard();
	}

	private int toGdxButton (int button) {
		if (button == 0) return Buttons.LEFT;
		if (button == 1) return Buttons.RIGHT;
		if (button == 2) return Buttons.MIDDLE;
		if (button == 3) return Buttons.BACK;
		if (button == 4) return Buttons.FORWARD;
		return -1;
	}

	void updateTime () {
		long thisTime = System.nanoTime();
		deltaTime = (thisTime - lastTime) / 1000000000.0f;
		lastTime = thisTime;
	}

	void updateMouse () {
		justTouched = false;
		if (Mouse.isCreated()) {
			int events = 0;
			while (Mouse.next()) {
				events++;
				int x = Mouse.getEventX();
				int y = Gdx.graphics.getHeight() - Mouse.getEventY() - 1;
				int button = Mouse.getEventButton();
				int gdxButton = toGdxButton(button);
				if (button != -1 && gdxButton == -1) continue; // Ignore unknown button.

				TouchEvent event = usedTouchEvents.obtain();
				event.x = x;
				event.y = y;
				event.button = gdxButton;
				event.pointer = 0;
				event.timeStamp = Mouse.getEventNanoseconds();

				// could be drag, scroll or move
				if (button == -1) {
					if (Mouse.getEventDWheel() != 0) {
						event.type = TouchEvent.TOUCH_SCROLLED;
						event.scrollAmount = (int)-Math.signum(Mouse.getEventDWheel());
					} else if (pressedButtons.size > 0) {
						event.type = TouchEvent.TOUCH_DRAGGED;
					} else {
						event.type = TouchEvent.TOUCH_MOVED;
					}
				} else {
					// nope, it's a down or up event.
					if (Mouse.getEventButtonState()) {
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
				deltaX = Mouse.getEventDX();
				deltaY = Mouse.getEventDY();
			}

			if (events == 0) {
				deltaX = 0;
				deltaY = 0;
			} else {
				Gdx.graphics.requestRendering();
			}
		}
	}

	void updateKeyboard () {
		if (keyJustPressed) {
			keyJustPressed = false;
			for (int i = 0; i < justPressedKeys.length; i++) {
				justPressedKeys[i] = false;
			}
		}
		if (lastKeyCharPressed != 0) {
			keyRepeatTimer -= deltaTime;
			if (keyRepeatTimer < 0) {
				keyRepeatTimer = keyRepeatTime;

				KeyEvent event = usedKeyEvents.obtain();
				event.keyCode = 0;
				event.keyChar = lastKeyCharPressed;
				event.type = KeyEvent.KEY_TYPED;
				event.timeStamp = System.nanoTime(); // FIXME this should use the repeat time plus the timestamp of the original
				keyEvents.add(event);
				Gdx.graphics.requestRendering();
			}
		}

		if (Keyboard.isCreated()) {
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					int keyCode = getGdxKeyCode(Keyboard.getEventKey());
					char keyChar = Keyboard.getEventCharacter();
					long timeStamp = Keyboard.getEventNanoseconds();

					switch (keyCode) {
					case Keys.DEL:
						keyChar = 8;
						break;
					case Keys.FORWARD_DEL:
						keyChar = 127;
						break;
					}

					KeyEvent event = usedKeyEvents.obtain();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_DOWN;
					event.timeStamp = timeStamp;
					keyEvents.add(event);

					event = usedKeyEvents.obtain();
					event.keyCode = 0;
					event.keyChar = keyChar;
					event.type = KeyEvent.KEY_TYPED;
					event.timeStamp = timeStamp;
					keyEvents.add(event);

					pressedKeys++;
					keyJustPressed = true;
					justPressedKeys[keyCode] = true;
					lastKeyCharPressed = keyChar;
					keyRepeatTimer = keyRepeatInitialTime;
				} else {
					int keyCode = LwjglInput.getGdxKeyCode(Keyboard.getEventKey());

					KeyEvent event = usedKeyEvents.obtain();
					event.keyCode = keyCode;
					event.keyChar = 0;
					event.type = KeyEvent.KEY_UP;
					event.timeStamp = Keyboard.getEventNanoseconds();
					keyEvents.add(event);

					pressedKeys--;
					lastKeyCharPressed = 0;
				}
				Gdx.graphics.requestRendering();
			}
		}
	}

	@Override
	public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
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

	private int toLwjglButton (int button) {
		switch (button) {
		case Buttons.LEFT:
			return 0;
		case Buttons.RIGHT:
			return 1;
		case Buttons.MIDDLE:
			return 2;
		case Buttons.BACK:
			return 3;
		case Buttons.FORWARD:
			return 4;
		}
		return 0;
	}

	@Override
	public boolean isButtonPressed (int button) {
		return Mouse.isButtonDown(toLwjglButton(button));
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
		Mouse.setGrabbed(catched);
	}

	@Override
	public boolean isCursorCatched () {
		return Mouse.isGrabbed();
	}

	@Override
	public int getDeltaX () {
		return deltaX;
	}

	@Override
	public int getDeltaX (int pointer) {
		if (pointer == 0)
			return deltaX;
		else
			return 0;
	}

	@Override
	public int getDeltaY () {
		return -deltaY;
	}

	@Override
	public int getDeltaY (int pointer) {
		if (pointer == 0)
			return -deltaY;
		else
			return 0;
	}

	@Override
	public void setCursorPosition (int x, int y) {
		Mouse.setCursorPosition(x, Gdx.graphics.getHeight() - 1 - y);
	}

	@Override
	public void setCursorImage (Pixmap pixmap, int xHotspot, int yHotspot) {
		try {
			if (pixmap == null) {
				Mouse.setNativeCursor(null);
				return;
			}

			if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
				throw new GdxRuntimeException("Cursor image pixmap is not in RGBA8888 format.");
			}

			if ((pixmap.getWidth() & (pixmap.getWidth() - 1)) != 0) {
				throw new GdxRuntimeException("Cursor image pixmap width of " + pixmap.getWidth()
					+ " is not a power-of-two greater than zero.");
			}

			if ((pixmap.getHeight() & (pixmap.getHeight() - 1)) != 0) {
				throw new GdxRuntimeException("Cursor image pixmap height of " + pixmap.getHeight()
					+ " is not a power-of-two greater than zero.");
			}

			if (xHotspot < 0 || xHotspot >= pixmap.getWidth()) {
				throw new GdxRuntimeException("xHotspot coordinate of " + xHotspot + " is not within image width bounds: [0, "
					+ pixmap.getWidth() + ").");
			}

			if (yHotspot < 0 || yHotspot >= pixmap.getHeight()) {
				throw new GdxRuntimeException("yHotspot coordinate of " + yHotspot + " is not within image height bounds: [0, "
					+ pixmap.getHeight() + ").");
			}

			// Convert from RGBA8888 to ARGB8888 and flip vertically
			IntBuffer pixelBuffer = pixmap.getPixels().asIntBuffer();
			int[] pixelsRGBA = new int[pixelBuffer.capacity()];
			pixelBuffer.get(pixelsRGBA);
			int[] pixelsARGBflipped = new int[pixelBuffer.capacity()];
			int pixel;
			if (pixelBuffer.order() == ByteOrder.BIG_ENDIAN) {
				for (int y = 0; y < pixmap.getHeight(); ++y) {
					for (int x = 0; x < pixmap.getWidth(); ++x) {
						pixel = pixelsRGBA[x + (y * pixmap.getWidth())];
						pixelsARGBflipped[x + ((pixmap.getHeight() - 1 - y) * pixmap.getWidth())] = ((pixel >> 8) & 0x00FFFFFF)
							| ((pixel << 24) & 0xFF000000);
					}
				}
			} else {
				for (int y = 0; y < pixmap.getHeight(); ++y) {
					for (int x = 0; x < pixmap.getWidth(); ++x) {
						pixel = pixelsRGBA[x + (y * pixmap.getWidth())];
						pixelsARGBflipped[x + ((pixmap.getHeight() - 1 - y) * pixmap.getWidth())] = ((pixel & 0xFF) << 16)
							| ((pixel & 0xFF0000) >> 16) | (pixel & 0xFF00FF00);
					}
				}
			}

			Mouse.setNativeCursor(new Cursor(pixmap.getWidth(), pixmap.getHeight(), xHotspot, pixmap.getHeight() - yHotspot - 4, 1,
				IntBuffer.wrap(pixelsARGBflipped), null));
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Could not set cursor image.", e);
		}
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
		// TODO Auto-generated method stub

	}

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
		static final int TOUCH_SCROLLED = 3;
		static final int TOUCH_MOVED = 4;

		long timeStamp;
		int type;
		int x;
		int y;
		int scrollAmount;
		int button;
		int pointer;
	}
}

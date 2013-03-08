
package com.badlogic.gdx.backends.jglfw;

import static com.badlogic.jglfw.Glfw.*;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputProcessorQueue;
import com.badlogic.jglfw.GlfwCallbackAdapter;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** An implementation of the {@link Input} interface hooking GLFW panel for input.
 * @author mzechner
 * @author Nathan Sweet */
public class JglfwInput implements Input {
	final JglfwApplication app;
	final InputProcessorQueue processorQueue;
	InputProcessor processor;
	int pressedKeys = 0;
	boolean justTouched;
	int deltaX, deltaY;
	long currentEventTime;

	public JglfwInput (final JglfwApplication app, boolean queueEvents) {
		this.app = app;

		InputProcessor inputProcessor = new InputProcessor() {
			private int mouseX, mouseY;

			public boolean keyDown (int keycode) {
				pressedKeys++;
				app.graphics.requestRendering();
				return processor != null ? processor.keyDown(keycode) : false;
			}

			public boolean keyUp (int keycode) {
				pressedKeys--;
				app.graphics.requestRendering();
				return processor != null ? processor.keyUp(keycode) : false;
			}

			public boolean keyTyped (char character) {
				app.graphics.requestRendering();
				return processor != null ? processor.keyTyped(character) : false;
			}

			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				justTouched = true;
				app.graphics.requestRendering();
				return processor != null ? processor.touchDown(screenX, screenY, pointer, button) : false;
			}

			public boolean touchUp (int screenX, int screenY, int pointer, int button) {
				app.graphics.requestRendering();
				return processor != null ? processor.touchUp(screenX, screenY, pointer, button) : false;
			}

			public boolean touchDragged (int screenX, int screenY, int pointer) {
				deltaX = screenX - mouseX;
				deltaY = screenY - mouseY;
				mouseX = screenX;
				mouseY = screenY;
				app.graphics.requestRendering();
				return processor != null ? processor.touchDragged(mouseX, mouseY, 0) : false;
			}

			public boolean mouseMoved (int screenX, int screenY) {
				deltaX = screenX - mouseX;
				deltaY = screenY - mouseX;
				mouseX = screenX;
				mouseY = screenY;
				app.graphics.requestRendering();
				return processor != null ? processor.mouseMoved(mouseX, mouseY) : false;
			}

			public boolean scrolled (int amount) {
				app.graphics.requestRendering();
				return processor != null ? processor.scrolled(amount) : false;
			}
		};

		if (queueEvents)
			inputProcessor = processorQueue = new InputProcessorQueue(inputProcessor);
		else
			processorQueue = null;

		app.callbacks.add(new GlfwInputProcessor(inputProcessor));
	}

	public void update () {
		justTouched = false;
		if (processorQueue != null)
			processorQueue.drain(); // Main loop is handled elsewhere and events are queued.
		else {
			currentEventTime = System.nanoTime();
			glfwPollEvents(); // Use GLFW main loop to process events.
		}
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

	public int getX () {
		return glfwGetCursorPosX(app.graphics.window);
	}

	public int getX (int pointer) {
		return pointer > 0 ? 0 : getX();
	}

	public int getY () {
		return glfwGetCursorPosY(app.graphics.window);
	}

	public int getY (int pointer) {
		return pointer > 0 ? 0 : getY();
	}

	public int getDeltaX () {
		return deltaX;
	}

	public int getDeltaX (int pointer) {
		return pointer > 0 ? 0 : deltaX;
	}

	public int getDeltaY () {
		return deltaY;
	}

	public int getDeltaY (int pointer) {
		return pointer > 0 ? 0 : deltaY;
	}

	public boolean isTouched () {
		return glfwGetMouseButton(app.graphics.window, 0) || glfwGetMouseButton(app.graphics.window, 1)
			|| glfwGetMouseButton(app.graphics.window, 2);
	}

	public boolean isTouched (int pointer) {
		return pointer > 0 ? false : isTouched();
	}

	public boolean justTouched () {
		return justTouched;
	}

	public boolean isButtonPressed (int button) {
		return glfwGetMouseButton(app.graphics.window, button);
	}

	public boolean isKeyPressed (int key) {
		if (key == Input.Keys.ANY_KEY) return pressedKeys > 0;
		return glfwGetKey(app.graphics.window, getJglfwKeyCode(key));
	}

	public void setOnscreenKeyboardVisible (boolean visible) {
	}

	public void vibrate (int milliseconds) {
	}

	public void vibrate (long[] pattern, int repeat) {
	}

	public void cancelVibrate () {
	}

	public float getAzimuth () {
		return 0;
	}

	public float getPitch () {
		return 0;
	}

	public float getRoll () {
		return 0;
	}

	public void getRotationMatrix (float[] matrix) {
	}

	public long getCurrentEventTime () {
		return processorQueue != null ? processorQueue.getCurrentEventTime() : currentEventTime;
	}

	public void setCatchBackKey (boolean catchBack) {
	}

	public void setCatchMenuKey (boolean catchMenu) {
	}

	public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}

	public InputProcessor getInputProcessor () {
		return processor;
	}

	public boolean isPeripheralAvailable (Peripheral peripheral) {
		return peripheral == Peripheral.HardwareKeyboard;
	}

	public int getRotation () {
		return 0;
	}

	public Orientation getNativeOrientation () {
		return Orientation.Landscape;
	}

	public void setCursorCatched (boolean captured) {
		glfwSetInputMode(app.graphics.window, GLFW_CURSOR_MODE, captured ? GLFW_CURSOR_CAPTURED : GLFW_CURSOR_NORMAL);
	}

	public boolean isCursorCatched () {
		return glfwGetInputMode(app.graphics.window, GLFW_CURSOR_MODE) == GLFW_CURSOR_CAPTURED;
	}

	public void setCursorPosition (int x, int y) {
		glfwSetCursorPos(app.graphics.window, x, y);
	}

	public void getTextInput (final TextInputListener listener, final String title, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run () {
				final String output = JOptionPane.showInputDialog(null, title, text);
				app.postRunnable(new Runnable() {
					public void run () {
						if (output != null)
							listener.input(output);
						else
							listener.canceled();
					}
				});
			}
		});
	}

	public void getPlaceholderTextInput (final TextInputListener listener, final String title, final String placeholder) {
		SwingUtilities.invokeLater(new Runnable() {
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
					public void removeUpdate (DocumentEvent event) {
						this.updated();
					}

					public void insertUpdate (DocumentEvent event) {
						this.updated();
					}

					public void changedUpdate (DocumentEvent event) {
						this.updated();
					}

					private void updated () {
						placeholderLabel.setVisible(textField.getText().length() == 0);
					}
				});

				JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null,
					null);
				pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());
				pane.selectInitialValue();

				placeholderLabel.setBorder(new EmptyBorder(textField.getBorder().getBorderInsets(textField)));

				JDialog dialog = pane.createDialog(null, title);
				dialog.addWindowFocusListener(new WindowFocusListener() {
					public void windowLostFocus (WindowEvent arg0) {
					}

					public void windowGainedFocus (WindowEvent arg0) {
						textField.requestFocusInWindow();
					}
				});
				dialog.setVisible(true);
				dialog.dispose();

				Object selectedValue = pane.getValue();
				if (selectedValue != null && (selectedValue instanceof Integer) && (Integer)selectedValue == JOptionPane.OK_OPTION)
					listener.input(textField.getText());
				else
					listener.canceled();
			}
		});
	}

	static char characterForKeyCode (int key) {
		// Map certain key codes to character codes.
		switch (key) {
		case Keys.BACKSPACE:
			return 8;
		case Keys.TAB:
			return '\t';
		case Keys.FORWARD_DEL:
			return 127;
		}
		return 0;
	}

	static public int getGdxKeyCode (int lwjglKeyCode) {
		switch (lwjglKeyCode) {
		case GLFW_KEY_LEFT_BRACKET:
			return Input.Keys.LEFT_BRACKET;
		case GLFW_KEY_RIGHT_BRACKET:
			return Input.Keys.RIGHT_BRACKET;
		case GLFW_KEY_GRAVE_ACCENT:
			return Input.Keys.GRAVE;
		case GLFW_KEY_KP_MULTIPLY:
			return Input.Keys.STAR;
		case GLFW_KEY_NUM_LOCK:
			return Input.Keys.NUM;
		case GLFW_KEY_KP_DECIMAL:
			return Input.Keys.PERIOD;
		case GLFW_KEY_KP_DIVIDE:
			return Input.Keys.SLASH;
		case GLFW_KEY_MENU:
			return Input.Keys.SYM;
		case GLFW_KEY_KP_EQUAL:
			return Input.Keys.EQUALS;
		case GLFW_KEY_EQUAL:
			return Input.Keys.EQUALS;
		case GLFW_KEY_KP_ENTER:
			return Input.Keys.ENTER;
		case GLFW_KEY_0:
			return Input.Keys.NUM_0;
		case GLFW_KEY_1:
			return Input.Keys.NUM_1;
		case GLFW_KEY_2:
			return Input.Keys.NUM_2;
		case GLFW_KEY_3:
			return Input.Keys.NUM_3;
		case GLFW_KEY_4:
			return Input.Keys.NUM_4;
		case GLFW_KEY_5:
			return Input.Keys.NUM_5;
		case GLFW_KEY_6:
			return Input.Keys.NUM_6;
		case GLFW_KEY_7:
			return Input.Keys.NUM_7;
		case GLFW_KEY_8:
			return Input.Keys.NUM_8;
		case GLFW_KEY_9:
			return Input.Keys.NUM_9;
		case GLFW_KEY_A:
			return Input.Keys.A;
		case GLFW_KEY_B:
			return Input.Keys.B;
		case GLFW_KEY_C:
			return Input.Keys.C;
		case GLFW_KEY_D:
			return Input.Keys.D;
		case GLFW_KEY_E:
			return Input.Keys.E;
		case GLFW_KEY_F:
			return Input.Keys.F;
		case GLFW_KEY_G:
			return Input.Keys.G;
		case GLFW_KEY_H:
			return Input.Keys.H;
		case GLFW_KEY_I:
			return Input.Keys.I;
		case GLFW_KEY_J:
			return Input.Keys.J;
		case GLFW_KEY_K:
			return Input.Keys.K;
		case GLFW_KEY_L:
			return Input.Keys.L;
		case GLFW_KEY_M:
			return Input.Keys.M;
		case GLFW_KEY_N:
			return Input.Keys.N;
		case GLFW_KEY_O:
			return Input.Keys.O;
		case GLFW_KEY_P:
			return Input.Keys.P;
		case GLFW_KEY_Q:
			return Input.Keys.Q;
		case GLFW_KEY_R:
			return Input.Keys.R;
		case GLFW_KEY_S:
			return Input.Keys.S;
		case GLFW_KEY_T:
			return Input.Keys.T;
		case GLFW_KEY_U:
			return Input.Keys.U;
		case GLFW_KEY_V:
			return Input.Keys.V;
		case GLFW_KEY_W:
			return Input.Keys.W;
		case GLFW_KEY_X:
			return Input.Keys.X;
		case GLFW_KEY_Y:
			return Input.Keys.Y;
		case GLFW_KEY_Z:
			return Input.Keys.Z;
		case GLFW_KEY_LALT:
			return Input.Keys.ALT_LEFT;
		case GLFW_KEY_RALT:
			return Input.Keys.ALT_RIGHT;
		case GLFW_KEY_BACKSLASH:
			return Input.Keys.BACKSLASH;
		case GLFW_KEY_COMMA:
			return Input.Keys.COMMA;
		case GLFW_KEY_DELETE:
			return Input.Keys.FORWARD_DEL;
		case GLFW_KEY_LEFT:
			return Input.Keys.DPAD_LEFT;
		case GLFW_KEY_RIGHT:
			return Input.Keys.DPAD_RIGHT;
		case GLFW_KEY_UP:
			return Input.Keys.DPAD_UP;
		case GLFW_KEY_DOWN:
			return Input.Keys.DPAD_DOWN;
		case GLFW_KEY_ENTER:
			return Input.Keys.ENTER;
		case GLFW_KEY_HOME:
			return Input.Keys.HOME;
		case GLFW_KEY_MINUS:
			return Input.Keys.MINUS;
		case GLFW_KEY_PERIOD:
			return Input.Keys.PERIOD;
		case GLFW_KEY_KP_ADD:
			return Input.Keys.PLUS;
		case GLFW_KEY_SEMICOLON:
			return Input.Keys.SEMICOLON;
		case GLFW_KEY_LSHIFT:
			return Input.Keys.SHIFT_LEFT;
		case GLFW_KEY_RSHIFT:
			return Input.Keys.SHIFT_RIGHT;
		case GLFW_KEY_SLASH:
			return Input.Keys.SLASH;
		case GLFW_KEY_SPACE:
			return Input.Keys.SPACE;
		case GLFW_KEY_TAB:
			return Input.Keys.TAB;
		case GLFW_KEY_LEFT_CONTROL:
			return Input.Keys.CONTROL_LEFT;
		case GLFW_KEY_RIGHT_CONTROL:
			return Input.Keys.CONTROL_RIGHT;
		case GLFW_KEY_ESCAPE:
			return Input.Keys.ESCAPE;
		case GLFW_KEY_END:
			return Input.Keys.END;
		case GLFW_KEY_INSERT:
			return Input.Keys.INSERT;
		case GLFW_KEY_BACKSPACE:
			return Input.Keys.DEL;
		case GLFW_KEY_KP_SUBTRACT:
			return Input.Keys.MINUS;
		case GLFW_KEY_APOSTROPHE:
			return Input.Keys.APOSTROPHE;
		case GLFW_KEY_F1:
			return Input.Keys.F1;
		case GLFW_KEY_F2:
			return Input.Keys.F2;
		case GLFW_KEY_F3:
			return Input.Keys.F3;
		case GLFW_KEY_F4:
			return Input.Keys.F4;
		case GLFW_KEY_F5:
			return Input.Keys.F5;
		case GLFW_KEY_F6:
			return Input.Keys.F6;
		case GLFW_KEY_F7:
			return Input.Keys.F7;
		case GLFW_KEY_F8:
			return Input.Keys.F8;
		case GLFW_KEY_F9:
			return Input.Keys.F9;
		case GLFW_KEY_F10:
			return Input.Keys.F10;
		case GLFW_KEY_F11:
			return Input.Keys.F11;
		case GLFW_KEY_F12:
			return Input.Keys.F12;
		case GLFW_KEY_KP_0:
			return Input.Keys.NUM_0;
		case GLFW_KEY_KP_1:
			return Input.Keys.NUM_1;
		case GLFW_KEY_KP_2:
			return Input.Keys.NUM_2;
		case GLFW_KEY_KP_3:
			return Input.Keys.NUM_3;
		case GLFW_KEY_KP_4:
			return Input.Keys.NUM_4;
		case GLFW_KEY_KP_5:
			return Input.Keys.NUM_5;
		case GLFW_KEY_KP_6:
			return Input.Keys.NUM_6;
		case GLFW_KEY_KP_7:
			return Input.Keys.NUM_7;
		case GLFW_KEY_KP_8:
			return Input.Keys.NUM_8;
		case GLFW_KEY_KP_9:
			return Input.Keys.NUM_9;
		default:
			return Input.Keys.UNKNOWN;
		}
	}

	static public int getJglfwKeyCode (int gdxKeyCode) {
		switch (gdxKeyCode) {
		case Input.Keys.LEFT_BRACKET:
			return GLFW_KEY_LEFT_BRACKET;
		case Input.Keys.RIGHT_BRACKET:
			return GLFW_KEY_RIGHT_BRACKET;
		case Input.Keys.GRAVE:
			return GLFW_KEY_GRAVE_ACCENT;
		case Input.Keys.STAR:
			return GLFW_KEY_KP_MULTIPLY;
		case Input.Keys.NUM:
			return GLFW_KEY_NUM_LOCK;
		case Input.Keys.EQUALS:
			return GLFW_KEY_MENU;
		case Input.Keys.SYM:
			return GLFW_KEY_MENU;
		case Input.Keys.NUM_0:
			return GLFW_KEY_0;
		case Input.Keys.NUM_1:
			return GLFW_KEY_1;
		case Input.Keys.NUM_2:
			return GLFW_KEY_2;
		case Input.Keys.NUM_3:
			return GLFW_KEY_3;
		case Input.Keys.NUM_4:
			return GLFW_KEY_4;
		case Input.Keys.NUM_5:
			return GLFW_KEY_5;
		case Input.Keys.NUM_6:
			return GLFW_KEY_6;
		case Input.Keys.NUM_7:
			return GLFW_KEY_7;
		case Input.Keys.NUM_8:
			return GLFW_KEY_8;
		case Input.Keys.NUM_9:
			return GLFW_KEY_9;
		case Input.Keys.A:
			return GLFW_KEY_A;
		case Input.Keys.B:
			return GLFW_KEY_B;
		case Input.Keys.C:
			return GLFW_KEY_C;
		case Input.Keys.D:
			return GLFW_KEY_D;
		case Input.Keys.E:
			return GLFW_KEY_E;
		case Input.Keys.F:
			return GLFW_KEY_F;
		case Input.Keys.G:
			return GLFW_KEY_G;
		case Input.Keys.H:
			return GLFW_KEY_H;
		case Input.Keys.I:
			return GLFW_KEY_I;
		case Input.Keys.J:
			return GLFW_KEY_J;
		case Input.Keys.K:
			return GLFW_KEY_K;
		case Input.Keys.L:
			return GLFW_KEY_L;
		case Input.Keys.M:
			return GLFW_KEY_M;
		case Input.Keys.N:
			return GLFW_KEY_N;
		case Input.Keys.O:
			return GLFW_KEY_O;
		case Input.Keys.P:
			return GLFW_KEY_P;
		case Input.Keys.Q:
			return GLFW_KEY_Q;
		case Input.Keys.R:
			return GLFW_KEY_R;
		case Input.Keys.S:
			return GLFW_KEY_S;
		case Input.Keys.T:
			return GLFW_KEY_T;
		case Input.Keys.U:
			return GLFW_KEY_U;
		case Input.Keys.V:
			return GLFW_KEY_V;
		case Input.Keys.W:
			return GLFW_KEY_W;
		case Input.Keys.X:
			return GLFW_KEY_X;
		case Input.Keys.Y:
			return GLFW_KEY_Y;
		case Input.Keys.Z:
			return GLFW_KEY_Z;
		case Input.Keys.ALT_LEFT:
			return GLFW_KEY_LALT;
		case Input.Keys.ALT_RIGHT:
			return GLFW_KEY_RALT;
		case Input.Keys.BACKSLASH:
			return GLFW_KEY_BACKSLASH;
		case Input.Keys.COMMA:
			return GLFW_KEY_COMMA;
		case Input.Keys.FORWARD_DEL:
			return GLFW_KEY_DELETE;
		case Input.Keys.DPAD_LEFT:
			return GLFW_KEY_LEFT;
		case Input.Keys.DPAD_RIGHT:
			return GLFW_KEY_RIGHT;
		case Input.Keys.DPAD_UP:
			return GLFW_KEY_UP;
		case Input.Keys.DPAD_DOWN:
			return GLFW_KEY_DOWN;
		case Input.Keys.ENTER:
			return GLFW_KEY_ENTER;
		case Input.Keys.HOME:
			return GLFW_KEY_HOME;
		case Input.Keys.MINUS:
			return GLFW_KEY_MINUS;
		case Input.Keys.PERIOD:
			return GLFW_KEY_PERIOD;
		case Input.Keys.PLUS:
			return GLFW_KEY_KP_ADD;
		case Input.Keys.SEMICOLON:
			return GLFW_KEY_SEMICOLON;
		case Input.Keys.SHIFT_LEFT:
			return GLFW_KEY_LSHIFT;
		case Input.Keys.SHIFT_RIGHT:
			return GLFW_KEY_RSHIFT;
		case Input.Keys.SLASH:
			return GLFW_KEY_SLASH;
		case Input.Keys.SPACE:
			return GLFW_KEY_SPACE;
		case Input.Keys.TAB:
			return GLFW_KEY_TAB;
		case Input.Keys.DEL:
			return GLFW_KEY_BACKSPACE;
		case Input.Keys.CONTROL_LEFT:
			return GLFW_KEY_LEFT_CONTROL;
		case Input.Keys.CONTROL_RIGHT:
			return GLFW_KEY_RIGHT_CONTROL;
		case Input.Keys.ESCAPE:
			return GLFW_KEY_ESCAPE;
		case Input.Keys.F1:
			return GLFW_KEY_F1;
		case Input.Keys.F2:
			return GLFW_KEY_F2;
		case Input.Keys.F3:
			return GLFW_KEY_F3;
		case Input.Keys.F4:
			return GLFW_KEY_F4;
		case Input.Keys.F5:
			return GLFW_KEY_F5;
		case Input.Keys.F6:
			return GLFW_KEY_F6;
		case Input.Keys.F7:
			return GLFW_KEY_F7;
		case Input.Keys.F8:
			return GLFW_KEY_F8;
		case Input.Keys.F9:
			return GLFW_KEY_F9;
		case Input.Keys.F10:
			return GLFW_KEY_F10;
		case Input.Keys.F11:
			return GLFW_KEY_F11;
		case Input.Keys.F12:
			return GLFW_KEY_F12;
		default:
			return 0;
		}
	}

	/** Receives GLFW input and calls InputProcessor methods.
	 * @author Nathan Sweet */
	static class GlfwInputProcessor extends GlfwCallbackAdapter {
		private int mouseX, mouseY, mousePressed;
		private char lastCharacter;
		private InputProcessor processor;

		public GlfwInputProcessor (InputProcessor processor) {
			if (processor == null) throw new IllegalArgumentException("processor cannot be null.");
			this.processor = processor;
		}

		public void key (long window, int key, int action) {
			switch (action) {
			case GLFW_PRESS:
				key = getGdxKeyCode(key);
				processor.keyDown(key);

				lastCharacter = 0;
				char character = characterForKeyCode(key);
				if (character != 0) character(window, character);
				break;

			case GLFW_RELEASE:
				processor.keyUp(getGdxKeyCode(key));
				break;

			case GLFW_REPEAT:
				if (lastCharacter != 0) processor.keyTyped(lastCharacter);
				break;
			}
		}

		public void character (long window, char character) {
			lastCharacter = character;
			processor.keyTyped(character);
		}

		public void scroll (long window, double scrollX, double scrollY) {
			processor.scrolled((int)-Math.signum(scrollY));
		}

		public void mouseButton (long window, int button, boolean pressed) {
			if (pressed) {
				mousePressed++;
				processor.touchDown(mouseX, mouseY, 0, button);
			} else {
				mousePressed--;
				processor.touchUp(mouseX, mouseY, 0, button);
			}
		}

		public void cursorPos (long window, int x, int y) {
			mouseX = x;
			mouseY = y;
			if (mousePressed > 0)
				processor.touchDragged(x, y, 0);
			else
				processor.mouseMoved(x, y);
		}
	}
}

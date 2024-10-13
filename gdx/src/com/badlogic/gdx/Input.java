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

package com.badlogic.gdx;

import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * <p>
 * Interface to the input facilities. This allows polling the state of the keyboard, the touch screen and the accelerometer. On
 * some backends (desktop, gwt, etc) the touch screen is replaced by mouse input. The accelerometer is of course not available on
 * all backends.
 * </p>
 * 
 * <p>
 * Instead of polling for events, one can process all input events with an {@link InputProcessor}. You can set the InputProcessor
 * via the {@link #setInputProcessor(InputProcessor)} method. It will be called before the {@link ApplicationListener#render()}
 * method in each frame.
 * </p>
 * 
 * <p>
 * Keyboard keys are translated to the constants in {@link Keys} transparently on all systems. Do not use system specific key
 * constants.
 * </p>
 * 
 * <p>
 * The class also offers methods to use (and test for the presence of) other input systems like vibration, compass, on-screen
 * keyboards, and cursor capture. Support for simple input dialogs is also provided.
 * </p>
 * 
 * @author mzechner */
public interface Input {
	/** Callback interface for {@link Input#getTextInput(TextInputListener, String, String, String)}
	 * 
	 * @author mzechner */
	static public interface TextInputListener {
		public void input (String text);

		public void canceled ();
	}

	/** Mouse buttons.
	 * @author mzechner */
	static public class Buttons {
		public static final int LEFT = 0;
		public static final int RIGHT = 1;
		public static final int MIDDLE = 2;
		public static final int BACK = 3;
		public static final int FORWARD = 4;
	}

	/** Keys.
	 * 
	 * @author mzechner */
	static public class Keys {
		public static final int ANY_KEY = -1;
		public static final int NUM_0 = 7;
		public static final int NUM_1 = 8;
		public static final int NUM_2 = 9;
		public static final int NUM_3 = 10;
		public static final int NUM_4 = 11;
		public static final int NUM_5 = 12;
		public static final int NUM_6 = 13;
		public static final int NUM_7 = 14;
		public static final int NUM_8 = 15;
		public static final int NUM_9 = 16;
		public static final int A = 29;
		public static final int ALT_LEFT = 57;
		public static final int ALT_RIGHT = 58;
		public static final int APOSTROPHE = 75;
		public static final int AT = 77;
		public static final int B = 30;
		public static final int BACK = 4;
		public static final int BACKSLASH = 73;
		public static final int C = 31;
		public static final int CALL = 5;
		public static final int CAMERA = 27;
		public static final int CAPS_LOCK = 115;
		public static final int CLEAR = 28;
		public static final int COMMA = 55;
		public static final int D = 32;
		public static final int DEL = 67;
		public static final int BACKSPACE = 67;
		public static final int FORWARD_DEL = 112;
		public static final int DPAD_CENTER = 23;
		public static final int DPAD_DOWN = 20;
		public static final int DPAD_LEFT = 21;
		public static final int DPAD_RIGHT = 22;
		public static final int DPAD_UP = 19;
		public static final int CENTER = 23;
		public static final int DOWN = 20;
		public static final int LEFT = 21;
		public static final int RIGHT = 22;
		public static final int UP = 19;
		public static final int E = 33;
		public static final int ENDCALL = 6;
		public static final int ENTER = 66;
		public static final int ENVELOPE = 65;
		public static final int EQUALS = 70;
		public static final int EXPLORER = 64;
		public static final int F = 34;
		public static final int FOCUS = 80;
		public static final int G = 35;
		public static final int GRAVE = 68;
		public static final int H = 36;
		public static final int HEADSETHOOK = 79;
		public static final int HOME = 3;
		public static final int I = 37;
		public static final int J = 38;
		public static final int K = 39;
		public static final int L = 40;
		public static final int LEFT_BRACKET = 71;
		public static final int M = 41;
		public static final int MEDIA_FAST_FORWARD = 90;
		public static final int MEDIA_NEXT = 87;
		public static final int MEDIA_PLAY_PAUSE = 85;
		public static final int MEDIA_PREVIOUS = 88;
		public static final int MEDIA_REWIND = 89;
		public static final int MEDIA_STOP = 86;
		public static final int MENU = 82;
		public static final int MINUS = 69;
		public static final int MUTE = 91;
		public static final int N = 42;
		public static final int NOTIFICATION = 83;
		public static final int NUM = 78;
		public static final int O = 43;
		public static final int P = 44;
		public static final int PAUSE = 121; // aka break
		public static final int PERIOD = 56;
		public static final int PLUS = 81;
		public static final int POUND = 18;
		public static final int POWER = 26;
		public static final int PRINT_SCREEN = 120; // aka SYSRQ
		public static final int Q = 45;
		public static final int R = 46;
		public static final int RIGHT_BRACKET = 72;
		public static final int S = 47;
		public static final int SCROLL_LOCK = 116;
		public static final int SEARCH = 84;
		public static final int SEMICOLON = 74;
		public static final int SHIFT_LEFT = 59;
		public static final int SHIFT_RIGHT = 60;
		public static final int SLASH = 76;
		public static final int SOFT_LEFT = 1;
		public static final int SOFT_RIGHT = 2;
		public static final int SPACE = 62;
		public static final int STAR = 17;
		public static final int SYM = 63; // on MacOS, this is Command (⌘)
		public static final int T = 48;
		public static final int TAB = 61;
		public static final int U = 49;
		public static final int UNKNOWN = 0;
		public static final int V = 50;
		public static final int VOLUME_DOWN = 25;
		public static final int VOLUME_UP = 24;
		public static final int W = 51;
		public static final int X = 52;
		public static final int Y = 53;
		public static final int Z = 54;
		public static final int META_ALT_LEFT_ON = 16;
		public static final int META_ALT_ON = 2;
		public static final int META_ALT_RIGHT_ON = 32;
		public static final int META_SHIFT_LEFT_ON = 64;
		public static final int META_SHIFT_ON = 1;
		public static final int META_SHIFT_RIGHT_ON = 128;
		public static final int META_SYM_ON = 4;
		public static final int CONTROL_LEFT = 129;
		public static final int CONTROL_RIGHT = 130;
		public static final int ESCAPE = 111;
		public static final int END = 123;
		public static final int INSERT = 124;
		public static final int PAGE_UP = 92;
		public static final int PAGE_DOWN = 93;
		public static final int PICTSYMBOLS = 94;
		public static final int SWITCH_CHARSET = 95;
		public static final int BUTTON_CIRCLE = 255;
		public static final int BUTTON_A = 96;
		public static final int BUTTON_B = 97;
		public static final int BUTTON_C = 98;
		public static final int BUTTON_X = 99;
		public static final int BUTTON_Y = 100;
		public static final int BUTTON_Z = 101;
		public static final int BUTTON_L1 = 102;
		public static final int BUTTON_R1 = 103;
		public static final int BUTTON_L2 = 104;
		public static final int BUTTON_R2 = 105;
		public static final int BUTTON_THUMBL = 106;
		public static final int BUTTON_THUMBR = 107;
		public static final int BUTTON_START = 108;
		public static final int BUTTON_SELECT = 109;
		public static final int BUTTON_MODE = 110;

		public static final int NUMPAD_0 = 144;
		public static final int NUMPAD_1 = 145;
		public static final int NUMPAD_2 = 146;
		public static final int NUMPAD_3 = 147;
		public static final int NUMPAD_4 = 148;
		public static final int NUMPAD_5 = 149;
		public static final int NUMPAD_6 = 150;
		public static final int NUMPAD_7 = 151;
		public static final int NUMPAD_8 = 152;
		public static final int NUMPAD_9 = 153;

		public static final int NUMPAD_DIVIDE = 154;
		public static final int NUMPAD_MULTIPLY = 155;
		public static final int NUMPAD_SUBTRACT = 156;
		public static final int NUMPAD_ADD = 157;
		public static final int NUMPAD_DOT = 158;
		public static final int NUMPAD_COMMA = 159;
		public static final int NUMPAD_ENTER = 160;
		public static final int NUMPAD_EQUALS = 161;
		public static final int NUMPAD_LEFT_PAREN = 162;
		public static final int NUMPAD_RIGHT_PAREN = 163;
		public static final int NUM_LOCK = 143;

// public static final int BACKTICK = 0;
// public static final int TILDE = 0;
// public static final int UNDERSCORE = 0;
// public static final int DOT = 0;
// public static final int BREAK = 0;
// public static final int PIPE = 0;
// public static final int EXCLAMATION = 0;
// public static final int QUESTIONMARK = 0;

// ` | VK_BACKTICK
// ~ | VK_TILDE
// : | VK_COLON
// _ | VK_UNDERSCORE
// . | VK_DOT
// (break) | VK_BREAK
// | | VK_PIPE
// ! | VK_EXCLAMATION
// ? | VK_QUESTION
		public static final int COLON = 243;
		public static final int F1 = 131;
		public static final int F2 = 132;
		public static final int F3 = 133;
		public static final int F4 = 134;
		public static final int F5 = 135;
		public static final int F6 = 136;
		public static final int F7 = 137;
		public static final int F8 = 138;
		public static final int F9 = 139;
		public static final int F10 = 140;
		public static final int F11 = 141;
		public static final int F12 = 142;
		public static final int F13 = 183;
		public static final int F14 = 184;
		public static final int F15 = 185;
		public static final int F16 = 186;
		public static final int F17 = 187;
		public static final int F18 = 188;
		public static final int F19 = 189;
		public static final int F20 = 190;
		public static final int F21 = 191;
		public static final int F22 = 192;
		public static final int F23 = 193;
		public static final int F24 = 194;

		public static final int MAX_KEYCODE = 255;

		/** @return a human readable representation of the keycode. The returned value can be used in
		 *         {@link Input.Keys#valueOf(String)} */
		public static String toString (int keycode) {
			if (keycode < 0) throw new IllegalArgumentException("keycode cannot be negative, keycode: " + keycode);
			if (keycode > MAX_KEYCODE) throw new IllegalArgumentException("keycode cannot be greater than 255, keycode: " + keycode);
			switch (keycode) {
			// META* variables should not be used with this method.
			case UNKNOWN:
				return "Unknown";
			case SOFT_LEFT:
				return "Soft Left";
			case SOFT_RIGHT:
				return "Soft Right";
			case HOME:
				return "Home";
			case BACK:
				return "Back";
			case CALL:
				return "Call";
			case ENDCALL:
				return "End Call";
			case NUM_0:
				return "0";
			case NUM_1:
				return "1";
			case NUM_2:
				return "2";
			case NUM_3:
				return "3";
			case NUM_4:
				return "4";
			case NUM_5:
				return "5";
			case NUM_6:
				return "6";
			case NUM_7:
				return "7";
			case NUM_8:
				return "8";
			case NUM_9:
				return "9";
			case STAR:
				return "*";
			case POUND:
				return "#";
			case UP:
				return "Up";
			case DOWN:
				return "Down";
			case LEFT:
				return "Left";
			case RIGHT:
				return "Right";
			case CENTER:
				return "Center";
			case VOLUME_UP:
				return "Volume Up";
			case VOLUME_DOWN:
				return "Volume Down";
			case POWER:
				return "Power";
			case CAMERA:
				return "Camera";
			case CLEAR:
				return "Clear";
			case A:
				return "A";
			case B:
				return "B";
			case C:
				return "C";
			case D:
				return "D";
			case E:
				return "E";
			case F:
				return "F";
			case G:
				return "G";
			case H:
				return "H";
			case I:
				return "I";
			case J:
				return "J";
			case K:
				return "K";
			case L:
				return "L";
			case M:
				return "M";
			case N:
				return "N";
			case O:
				return "O";
			case P:
				return "P";
			case Q:
				return "Q";
			case R:
				return "R";
			case S:
				return "S";
			case T:
				return "T";
			case U:
				return "U";
			case V:
				return "V";
			case W:
				return "W";
			case X:
				return "X";
			case Y:
				return "Y";
			case Z:
				return "Z";
			case COMMA:
				return ",";
			case PERIOD:
				return ".";
			case ALT_LEFT:
				return "L-Alt";
			case ALT_RIGHT:
				return "R-Alt";
			case SHIFT_LEFT:
				return "L-Shift";
			case SHIFT_RIGHT:
				return "R-Shift";
			case TAB:
				return "Tab";
			case SPACE:
				return "Space";
			case SYM:
				return "SYM";
			case EXPLORER:
				return "Explorer";
			case ENVELOPE:
				return "Envelope";
			case ENTER:
				return "Enter";
			case DEL:
				return "Delete"; // also BACKSPACE
			case GRAVE:
				return "`";
			case MINUS:
				return "-";
			case EQUALS:
				return "=";
			case LEFT_BRACKET:
				return "[";
			case RIGHT_BRACKET:
				return "]";
			case BACKSLASH:
				return "\\";
			case SEMICOLON:
				return ";";
			case APOSTROPHE:
				return "'";
			case SLASH:
				return "/";
			case AT:
				return "@";
			case NUM:
				return "Num";
			case HEADSETHOOK:
				return "Headset Hook";
			case FOCUS:
				return "Focus";
			case PLUS:
				return "Plus";
			case MENU:
				return "Menu";
			case NOTIFICATION:
				return "Notification";
			case SEARCH:
				return "Search";
			case MEDIA_PLAY_PAUSE:
				return "Play/Pause";
			case MEDIA_STOP:
				return "Stop Media";
			case MEDIA_NEXT:
				return "Next Media";
			case MEDIA_PREVIOUS:
				return "Prev Media";
			case MEDIA_REWIND:
				return "Rewind";
			case MEDIA_FAST_FORWARD:
				return "Fast Forward";
			case MUTE:
				return "Mute";
			case PAGE_UP:
				return "Page Up";
			case PAGE_DOWN:
				return "Page Down";
			case PICTSYMBOLS:
				return "PICTSYMBOLS";
			case SWITCH_CHARSET:
				return "SWITCH_CHARSET";
			case BUTTON_A:
				return "A Button";
			case BUTTON_B:
				return "B Button";
			case BUTTON_C:
				return "C Button";
			case BUTTON_X:
				return "X Button";
			case BUTTON_Y:
				return "Y Button";
			case BUTTON_Z:
				return "Z Button";
			case BUTTON_L1:
				return "L1 Button";
			case BUTTON_R1:
				return "R1 Button";
			case BUTTON_L2:
				return "L2 Button";
			case BUTTON_R2:
				return "R2 Button";
			case BUTTON_THUMBL:
				return "Left Thumb";
			case BUTTON_THUMBR:
				return "Right Thumb";
			case BUTTON_START:
				return "Start";
			case BUTTON_SELECT:
				return "Select";
			case BUTTON_MODE:
				return "Button Mode";
			case FORWARD_DEL:
				return "Forward Delete";
			case CONTROL_LEFT:
				return "L-Ctrl";
			case CONTROL_RIGHT:
				return "R-Ctrl";
			case ESCAPE:
				return "Escape";
			case END:
				return "End";
			case INSERT:
				return "Insert";
			case NUMPAD_0:
				return "Numpad 0";
			case NUMPAD_1:
				return "Numpad 1";
			case NUMPAD_2:
				return "Numpad 2";
			case NUMPAD_3:
				return "Numpad 3";
			case NUMPAD_4:
				return "Numpad 4";
			case NUMPAD_5:
				return "Numpad 5";
			case NUMPAD_6:
				return "Numpad 6";
			case NUMPAD_7:
				return "Numpad 7";
			case NUMPAD_8:
				return "Numpad 8";
			case NUMPAD_9:
				return "Numpad 9";
			case COLON:
				return ":";
			case F1:
				return "F1";
			case F2:
				return "F2";
			case F3:
				return "F3";
			case F4:
				return "F4";
			case F5:
				return "F5";
			case F6:
				return "F6";
			case F7:
				return "F7";
			case F8:
				return "F8";
			case F9:
				return "F9";
			case F10:
				return "F10";
			case F11:
				return "F11";
			case F12:
				return "F12";
			case F13:
				return "F13";
			case F14:
				return "F14";
			case F15:
				return "F15";
			case F16:
				return "F16";
			case F17:
				return "F17";
			case F18:
				return "F18";
			case F19:
				return "F19";
			case F20:
				return "F20";
			case F21:
				return "F21";
			case F22:
				return "F22";
			case F23:
				return "F23";
			case F24:
				return "F24";
			case NUMPAD_DIVIDE:
				return "Num /";
			case NUMPAD_MULTIPLY:
				return "Num *";
			case NUMPAD_SUBTRACT:
				return "Num -";
			case NUMPAD_ADD:
				return "Num +";
			case NUMPAD_DOT:
				return "Num .";
			case NUMPAD_COMMA:
				return "Num ,";
			case NUMPAD_ENTER:
				return "Num Enter";
			case NUMPAD_EQUALS:
				return "Num =";
			case NUMPAD_LEFT_PAREN:
				return "Num (";
			case NUMPAD_RIGHT_PAREN:
				return "Num )";
			case NUM_LOCK:
				return "Num Lock";
			case CAPS_LOCK:
				return "Caps Lock";
			case SCROLL_LOCK:
				return "Scroll Lock";
			case PAUSE:
				return "Pause";
			case PRINT_SCREEN:
				return "Print";
			// BUTTON_CIRCLE unhandled, as it conflicts with the more likely to be pressed F12
			default:
				// key name not found
				return null;
			}
		}

		private static ObjectIntMap<String> keyNames;

		/** @param keyname the keyname returned by the {@link Keys#toString(int)} method
		 * @return the int keycode */
		public static int valueOf (String keyname) {
			if (keyNames == null) initializeKeyNames();
			return keyNames.get(keyname, -1);
		}

		/** lazily intialized in {@link Keys#valueOf(String)} */
		private static void initializeKeyNames () {
			keyNames = new ObjectIntMap<String>();
			for (int i = 0; i < 256; i++) {
				String name = toString(i);
				if (name != null) keyNames.put(name, i);
			}
		}
	}

	/** Enumeration of potentially available peripherals. Use with {@link Input#isPeripheralAvailable(Peripheral)}.
	 * @author mzechner */
	public enum Peripheral {
		HardwareKeyboard, OnscreenKeyboard, MultitouchScreen, Accelerometer, Compass, Vibrator, HapticFeedback, Gyroscope, RotationVector, Pressure
	}

	/** @return The acceleration force in m/s^2 applied to the device in the X axis, including the force of gravity */
	public float getAccelerometerX ();

	/** @return The acceleration force in m/s^2 applied to the device in the Y axis, including the force of gravity */
	public float getAccelerometerY ();

	/** @return The acceleration force in m/s^2 applied to the device in the Z axis, including the force of gravity */
	public float getAccelerometerZ ();

	/** @return The rate of rotation in rad/s around the X axis */
	public float getGyroscopeX ();

	/** @return The rate of rotation in rad/s around the Y axis */
	public float getGyroscopeY ();

	/** @return The rate of rotation in rad/s around the Z axis */
	public float getGyroscopeZ ();

	/** @return The maximum number of pointers supported */
	public int getMaxPointers ();

	/** @return The x coordinate of the last touch on touch screen devices and the current mouse position on desktop for the first
	 *         pointer in screen coordinates. The screen origin is the top left corner. */
	public int getX ();

	/** Returns the x coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
	 * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 * 
	 * @param pointer the pointer id.
	 * @return the x coordinate */
	public int getX (int pointer);

	/** @return the different between the current pointer location and the last pointer location on the x-axis. */
	public int getDeltaX ();

	/** @return the different between the current pointer location and the last pointer location on the x-axis. */
	public int getDeltaX (int pointer);

	/** @return The y coordinate of the last touch on touch screen devices and the current mouse position on desktop for the first
	 *         pointer in screen coordinates. The screen origin is the top left corner. */
	public int getY ();

	/** Returns the y coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
	 * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 * 
	 * @param pointer the pointer id.
	 * @return the y coordinate */
	public int getY (int pointer);

	/** @return the different between the current pointer location and the last pointer location on the y-axis. */
	public int getDeltaY ();

	/** @return the different between the current pointer location and the last pointer location on the y-axis. */
	public int getDeltaY (int pointer);

	/** @return whether the screen is currently touched. */
	public boolean isTouched ();

	/** @return whether a new touch down event just occurred. */
	public boolean justTouched ();

	/** Whether the screen is currently touched by the pointer with the given index. Pointers are indexed from 0 to n. The pointer
	 * id identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 *
	 * @param pointer the pointer
	 * @return whether the screen is touched by the pointer */
	public boolean isTouched (int pointer);

	/** @return the pressure of the first pointer */
	public float getPressure ();

	/** Returns the pressure of the given pointer, where 0 is untouched. On Android it should be up to 1.0, but it can go above
	 * that slightly and its not consistent between devices. On iOS 1.0 is the normal touch and significantly more of hard touch.
	 * Check relevant manufacturer documentation for details. Check availability with
	 * {@link Input#isPeripheralAvailable(Peripheral)}. If not supported, returns 1.0 when touched.
	 *
	 * @param pointer the pointer id.
	 * @return the pressure */
	public float getPressure (int pointer);

	/** Whether a given button is pressed or not. Button constants can be found in {@link Buttons}. On Android only the
	 * Buttons#LEFT constant is meaningful before version 4.0.
	 * @param button the button to check.
	 * @return whether the button is down or not. */
	public boolean isButtonPressed (int button);

	/** Returns whether a given button has just been pressed. Button constants can be found in {@link Buttons}. On Android only the
	 * Buttons#LEFT constant is meaningful before version 4.0. On WebGL (GWT), only LEFT, RIGHT and MIDDLE buttons are supported.
	 *
	 * @param button the button to check.
	 * @return true or false. */
	public boolean isButtonJustPressed (int button);

	/** Returns whether the key is pressed.
	 * 
	 * @param key The key code as found in {@link Input.Keys}.
	 * @return true or false. */
	public boolean isKeyPressed (int key);

	/** Returns whether the key has just been pressed.
	 * 
	 * @param key The key code as found in {@link Input.Keys}.
	 * @return true or false. */
	public boolean isKeyJustPressed (int key);

	/** System dependent method to input a string of text. A dialog box will be created with the given title and the given text as
	 * a message for the user. Will use the Default keyboard type. Once the dialog has been closed the provided
	 * {@link TextInputListener} will be called on the rendering thread.
	 * 
	 * @param listener The TextInputListener.
	 * @param title The title of the text input dialog.
	 * @param text The message presented to the user. */
	public void getTextInput (TextInputListener listener, String title, String text, String hint);

	/** System dependent method to input a string of text. A dialog box will be created with the given title and the given text as
	 * a message for the user. Once the dialog has been closed the provided {@link TextInputListener} will be called on the
	 * rendering thread.
	 *
	 * @param listener The TextInputListener.
	 * @param title The title of the text input dialog.
	 * @param text The message presented to the user.
	 * @param type which type of keyboard we wish to display */
	public void getTextInput (TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type);

	/** Sets the on-screen keyboard visible if available. Will use the Default keyboard type.
	 * 
	 * @param visible visible or not */
	public void setOnscreenKeyboardVisible (boolean visible);

	/** Sets the on-screen keyboard visible if available.
	 *
	 * @param visible visible or not
	 * @param type which type of keyboard we wish to display. Can be null when hiding */
	public void setOnscreenKeyboardVisible (boolean visible, OnscreenKeyboardType type);

	static interface InputStringValidator {
		/** @param toCheck The string that should be validated
		 * @return true, if the string is acceptable, false if not. */
		boolean validate (String toCheck);
	}

	/** Sets the on-screen keyboard visible if available.
	 *
	 * @param configuration The configuration for the native input field */
	public void openTextInputField (NativeInputConfiguration configuration);

	/** Closes the native input field and applies the result to the input wrapper.
	 * @param sendReturn Whether a "return" key should be send after processing */
	public void closeTextInputField (boolean sendReturn);

	static interface KeyboardHeightObserver {
		void onKeyboardHeightChanged (int height);
	}

	/** This will set a keyboard height callback. This will get called, whenever the keyboard height changes. Note: When using
	 * openTextInputField, it will report the height of the native input field too. */
	public void setKeyboardHeightObserver (KeyboardHeightObserver observer);

	public enum OnscreenKeyboardType {
		Default, NumberPad, PhonePad, Email, Password, URI
	}

	/** Generates a simple haptic effect of a given duration or a vibration effect on devices without haptic capabilities. Note
	 * that on Android backend you'll need the permission
	 * <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for this to work.
	 * On iOS backend you'll need to set <code>useHaptics = true</code> for devices with haptics capabilities to use them.
	 * 
	 * @param milliseconds the number of milliseconds to vibrate. */
	public void vibrate (int milliseconds);

	/** Generates a simple haptic effect of a given duration and default amplitude. Note that on Android backend you'll need the
	 * permission <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for
	 * this to work. On iOS backend you'll need to set <code>useHaptics = true</code> for devices with haptics capabilities to use
	 * them.
	 *
	 * @param milliseconds the duration of the haptics effect
	 * @param fallback whether to use non-haptic vibrator on devices without haptics capabilities (or haptics disabled). Fallback
	 *           non-haptic vibrations may ignore length parameter in some backends. */
	public void vibrate (int milliseconds, boolean fallback);

	/** Generates a simple haptic effect of a given duration and amplitude. Note that on Android backend you'll need the permission
	 * <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for this to work.
	 * On iOS backend you'll need to set <code>useHaptics = true</code> for devices with haptics capabilities to use them.
	 *
	 * @param milliseconds the duration of the haptics effect
	 * @param amplitude the amplitude/strength of the haptics effect. Valid values in the range [0, 255].
	 * @param fallback whether to use non-haptic vibrator on devices without haptics capabilities (or haptics disabled). Fallback
	 *           non-haptic vibrations may ignore length and/or amplitude parameters in some backends. */
	public void vibrate (int milliseconds, int amplitude, boolean fallback);

	/** Generates a simple haptic effect of a type. VibrationTypes are length/amplitude haptic effect presets that depend on each
	 * device and are defined by manufacturers. Should give most consistent results across devices and OSs. Note that on Android
	 * backend you'll need the permission <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your
	 * manifest file in order for this to work. On iOS backend you'll need to set <code>useHaptics = true</code> for devices with
	 * haptics capabilities to use them.
	 *
	 * @param vibrationType the type of vibration */
	public void vibrate (VibrationType vibrationType);

	public enum VibrationType {
		LIGHT, MEDIUM, HEAVY;
	}

	/** The azimuth is the angle of the device's orientation around the z-axis. The positive z-axis points towards the earths
	 * center.
	 * 
	 * @see <a
	 *      href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],
	 *      float[], float[], float[])</a>
	 * @return the azimuth in degrees */
	public float getAzimuth ();

	/** The pitch is the angle of the device's orientation around the x-axis. The positive x-axis roughly points to the west and is
	 * orthogonal to the z- and y-axis.
	 * @see <a
	 *      href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],
	 *      float[], float[], float[])</a>
	 * @return the pitch in degrees */
	public float getPitch ();

	/** The roll is the angle of the device's orientation around the y-axis. The positive y-axis points to the magnetic north pole
	 * of the earth.
	 * @see <a
	 *      href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],
	 *      float[], float[], float[])</a>
	 * @return the roll in degrees */
	public float getRoll ();

	/** Returns the rotation matrix describing the devices rotation as per
	 * <a href= "http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[],
	 * float[], float[])" >SensorManager#getRotationMatrix(float[], float[], float[], float[])</a>. Does not manipulate the matrix
	 * if the platform does not have an accelerometer.
	 * @param matrix */
	public void getRotationMatrix (float[] matrix);

	/** @return the time of the event currently reported to the {@link InputProcessor}. */
	public long getCurrentEventTime ();

	/** Sets whether the given key on Android or GWT should be caught. No effect on other platforms. All keys that are not caught
	 * may be handled by other apps or background processes on Android, or may trigger default browser behaviour on GWT. For
	 * example, media or volume buttons are handled by background media players if present, or Space key triggers a scroll. All
	 * keys you need to control your game should be caught to prevent unintended behaviour.
	 *
	 * @param keycode keycode to catch
	 * @param catchKey whether to catch the given keycode */
	public void setCatchKey (int keycode, boolean catchKey);

	/** @param keycode keycode to check if caught
	 * @return true if the given keycode is configured to be caught */
	public boolean isCatchKey (int keycode);

	/** Sets the {@link InputProcessor} that will receive all touch and key input events. It will be called before the
	 * {@link ApplicationListener#render()} method each frame.
	 * 
	 * @param processor the InputProcessor */
	public void setInputProcessor (InputProcessor processor);

	/** @return the currently set {@link InputProcessor} or null. */
	public InputProcessor getInputProcessor ();

	/** Queries whether a {@link Peripheral} is currently available. In case of Android and the {@link Peripheral#HardwareKeyboard}
	 * this returns the whether the keyboard is currently slid out or not.
	 * 
	 * @param peripheral the {@link Peripheral}
	 * @return whether the peripheral is available or not. */
	public boolean isPeripheralAvailable (Peripheral peripheral);

	/** @return the rotation of the device with respect to its native orientation. */
	public int getRotation ();

	/** @return the native orientation of the device. */
	public Orientation getNativeOrientation ();

	public enum Orientation {
		Landscape, Portrait
	}

	/** Only viable on the desktop. Will confine the mouse cursor location to the window and hide the mouse cursor. X and y
	 * coordinates are still reported as if the mouse was not catched.
	 * @param catched whether to catch or not to catch the mouse cursor */
	public void setCursorCatched (boolean catched);

	/** @return whether the mouse cursor is catched. */
	public boolean isCursorCatched ();

	/** Only viable on the desktop. Will set the mouse cursor location to the given window coordinates (origin top-left corner).
	 * @param x the x-position
	 * @param y the y-position */
	public void setCursorPosition (int x, int y);
}

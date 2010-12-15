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

package com.badlogic.gdx;

/**
 * <p>
 * Interface to the input facilities. This allows to poll the state of the keyboard, touch screen and accelerometer. On the
 * desktop the touch screen is replaced by mouse input, the accelerometer is of course not available.
 * </p>
 * 
 * <p>
 * Additionally one can process events with an {@link InputProcessor} with this module. You can set the InputProcessor via the
 * {@link #setInputProcessor(InputProcessor)} method. It will be called before the {@link ApplicationListener#render()} method in
 * each frame.
 * </p>
 * 
 * <p>
 * Keyboard keys are translated to the constants in {@link Keys} transparantely on all systems. Do not use system specific key
 * constants.
 * </p>
 * 
 * @author mzechner
 * 
 */
public interface Input {
	/**
	 * Callback interface for {@link Input#getTextInput(TextInputListener, String, String)}
	 * 
	 * @author mzechner
	 * 
	 */
	public interface TextInputListener {
		public void input (String text);
	}

	/**
	 * Keys.
	 * 
	 * @author mzechner
	 * 
	 */
	public class Keys {
		public static final int ANY_KEY = -1;
		public static final int KEYCODE_0 = 7;
		public static final int KEYCODE_1 = 8;
		public static final int KEYCODE_2 = 9;
		public static final int KEYCODE_3 = 10;
		public static final int KEYCODE_4 = 11;
		public static final int KEYCODE_5 = 12;
		public static final int KEYCODE_6 = 13;
		public static final int KEYCODE_7 = 14;
		public static final int KEYCODE_8 = 15;
		public static final int KEYCODE_9 = 16;
		public static final int KEYCODE_A = 29;
		public static final int KEYCODE_ALT_LEFT = 57;
		public static final int KEYCODE_ALT_RIGHT = 58;
		public static final int KEYCODE_APOSTROPHE = 75;
		public static final int KEYCODE_AT = 77;
		public static final int KEYCODE_B = 30;
		public static final int KEYCODE_BACK = 4;
		public static final int KEYCODE_BACKSLASH = 73;
		public static final int KEYCODE_C = 31;
		public static final int KEYCODE_CALL = 5;
		public static final int KEYCODE_CAMERA = 27;
		public static final int KEYCODE_CLEAR = 28;
		public static final int KEYCODE_COMMA = 55;
		public static final int KEYCODE_D = 32;
		public static final int KEYCODE_DEL = 67;
		public static final int KEYCODE_DPAD_CENTER = 23;
		public static final int KEYCODE_DPAD_DOWN = 20;
		public static final int KEYCODE_DPAD_LEFT = 21;
		public static final int KEYCODE_DPAD_RIGHT = 22;
		public static final int KEYCODE_DPAD_UP = 19;
		public static final int KEYCODE_E = 33;
		public static final int KEYCODE_ENDCALL = 6;
		public static final int KEYCODE_ENTER = 66;
		public static final int KEYCODE_ENVELOPE = 65;
		public static final int KEYCODE_EQUALS = 70;
		public static final int KEYCODE_EXPLORER = 64;
		public static final int KEYCODE_F = 34;
		public static final int KEYCODE_FOCUS = 80;
		public static final int KEYCODE_G = 35;
		public static final int KEYCODE_GRAVE = 68;
		public static final int KEYCODE_H = 36;
		public static final int KEYCODE_HEADSETHOOK = 79;
		public static final int KEYCODE_HOME = 3;
		public static final int KEYCODE_I = 37;
		public static final int KEYCODE_J = 38;
		public static final int KEYCODE_K = 39;
		public static final int KEYCODE_L = 40;
		public static final int KEYCODE_LEFT_BRACKET = 71;
		public static final int KEYCODE_M = 41;
		public static final int KEYCODE_MEDIA_FAST_FORWARD = 90;
		public static final int KEYCODE_MEDIA_NEXT = 87;
		public static final int KEYCODE_MEDIA_PLAY_PAUSE = 85;
		public static final int KEYCODE_MEDIA_PREVIOUS = 88;
		public static final int KEYCODE_MEDIA_REWIND = 89;
		public static final int KEYCODE_MEDIA_STOP = 86;
		public static final int KEYCODE_MENU = 82;
		public static final int KEYCODE_MINUS = 69;
		public static final int KEYCODE_MUTE = 91;
		public static final int KEYCODE_N = 42;
		public static final int KEYCODE_NOTIFICATION = 83;
		public static final int KEYCODE_NUM = 78;
		public static final int KEYCODE_O = 43;
		public static final int KEYCODE_P = 44;
		public static final int KEYCODE_PERIOD = 56;
		public static final int KEYCODE_PLUS = 81;
		public static final int KEYCODE_POUND = 18;
		public static final int KEYCODE_POWER = 26;
		public static final int KEYCODE_Q = 45;
		public static final int KEYCODE_R = 46;
		public static final int KEYCODE_RIGHT_BRACKET = 72;
		public static final int KEYCODE_S = 47;
		public static final int KEYCODE_SEARCH = 84;
		public static final int KEYCODE_SEMICOLON = 74;
		public static final int KEYCODE_SHIFT_LEFT = 59;
		public static final int KEYCODE_SHIFT_RIGHT = 60;
		public static final int KEYCODE_SLASH = 76;
		public static final int KEYCODE_SOFT_LEFT = 1;
		public static final int KEYCODE_SOFT_RIGHT = 2;
		public static final int KEYCODE_SPACE = 62;
		public static final int KEYCODE_STAR = 17;
		public static final int KEYCODE_SYM = 63;
		public static final int KEYCODE_T = 48;
		public static final int KEYCODE_TAB = 61;
		public static final int KEYCODE_U = 49;
		public static final int KEYCODE_UNKNOWN = 0;
		public static final int KEYCODE_V = 50;
		public static final int KEYCODE_VOLUME_DOWN = 25;
		public static final int KEYCODE_VOLUME_UP = 24;
		public static final int KEYCODE_W = 51;
		public static final int KEYCODE_X = 52;
		public static final int KEYCODE_Y = 53;
		public static final int KEYCODE_Z = 54;
		public static final int META_ALT_LEFT_ON = 16;
		public static final int META_ALT_ON = 2;
		public static final int META_ALT_RIGHT_ON = 32;
		public static final int META_SHIFT_LEFT_ON = 64;
		public static final int META_SHIFT_ON = 1;
		public static final int META_SHIFT_RIGHT_ON = 128;
		public static final int META_SYM_ON = 4;
	}

	/**
	 * @return whether an accelerometer is available
	 */
	public boolean isAccelerometerAvailable ();

	/**
	 * @return The value of the accelerometer on its x-axis. ranges between [-10,10].
	 */
	public float getAccelerometerX ();

	/**
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerY ();

	/**
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerZ ();

	/**
	 * @return the last touch x coordinate in screen coordinates. The screen origin is the top left corner.
	 */
	public int getX ();

	/**
	 * Returns the x coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
	 * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 * 
	 * @param pointer the pointer id.
	 * @return the x coordinate
	 */
	public int getX (int pointer);

	/**
	 * @return the last touch y coordinate in screen coordinates. The screen origin is the top left corner.
	 */
	public int getY ();

	/**
	 * Returns the y coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
	 * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 * 
	 * @param pointer the pointer id.
	 * @return the y coordinate
	 */
	public int getY (int pointer);

	/**
	 * @return whether the screen is currently touched.
	 */
	public boolean isTouched ();

	/**
	 * Whether the screen is currently touched by the pointer with the given index. Pointers are indexed from 0 to n. The pointer
	 * id identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
	 * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
	 * the touch screen the first free index will be used.
	 * 
	 * @param pointer the pointer
	 * @return whether the screen is touched by the pointer
	 */
	public boolean isTouched (int pointer);

	/**
	 * Returns whether the key is pressed.
	 * 
	 * @param key The key code as found in {@link Input.Keys}.
	 * @return true or false.
	 */
	public boolean isKeyPressed (int key);

	/**
	 * System dependent method to input a string of text. A dialog box will be created with the given title and the given text as a
	 * message for the user. Once the dialog has been closed the provided {@link TextInputListener} will be called but not
	 * necessarily in the rendering thread in which all the {@link ApplicationListener} methods are called. You have to synchronize
	 * this yourself.
	 * 
	 * @param listener The TextInputListener.
	 * @param title The title of the text input dialog.
	 * @param text The message presented to the user.
	 */
	public void getTextInput (TextInputListener listener, String title, String text);

	/**
	 * Sets the on-screen keyboard visible if available.
	 * 
	 * @param visible visible or not
	 */
	public void setOnscreenKeyboardVisible (boolean visible);

	/**
	 * @return whether an on-screen keyboard is available or not
	 */
	public boolean supportsOnscreenKeyboard ();

	/**
	 * Returns whether multitouch is supported by the device. Note that this also includes the "broken" multitouch on the Droid or
	 * Nexus One!
	 * 
	 * @return whether multitouch is supported
	 */
	public boolean supportsMultitouch ();

	/**
	 * Sets whether the BACK button on Android should be caught. This will prevent the app from being paused. Will have no effect
	 * on the desktop.
	 * 
	 * @param catchBack whether to catch the back button
	 */
	public void setCatchBackKey (boolean catchBack);

	/**
	 * Sets the {@link InputProcessor} that will receive all touch and key input events. It will be called before the
	 * {@link ApplicationListener#render()} method each frame.
	 * 
	 * @param processor the InputProcessor
	 */
	public void setInputProcessor (InputProcessor processor);
}

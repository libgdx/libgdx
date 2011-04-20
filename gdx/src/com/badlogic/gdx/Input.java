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
 * <p>
 * The class also offers methods to let the phone vibrate as well as getting compass readings.
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
		public void cancled();
	}

	/**
	 * Mouse buttons.
	 * @author mzechner
	 * 
	 */
	public class Buttons {
		public static final int LEFT = 0;
		public static final int RIGHT = 1;
		public static final int MIDDLE = 2;
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
		public static final int KEYCODE_FORWARD_DEL = 112;
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
		public static final int KEYCODE_CONTROL_LEFT = 129;
		public static final int KEYCODE_CONTROL_RIGHT = 130;
		public static final int KEYCODE_ESCAPE = 131;
		public static final int KEYCODE_END = 132;
		public static final int KEYCODE_INSERT = 133;
		public static final int KEYCODE_PAGE_UP = 92;
		public static final int KEYCODE_PAGE_DOWN = 93;
		public static final int KEYCODE_PICTSYMBOLS = 94;
		public static final int KEYCODE_SWITCH_CHARSET = 95;
		public static final int KEYCODE_BUTTON_A = 96;
		public static final int KEYCODE_BUTTON_B = 97;
		public static final int KEYCODE_BUTTON_C = 98;
		public static final int KEYCODE_BUTTON_X = 99;
		public static final int KEYCODE_BUTTON_Y = 100;
		public static final int KEYCODE_BUTTON_Z = 101;
		public static final int KEYCODE_BUTTON_L1 = 102;
		public static final int KEYCODE_BUTTON_R1 = 103;
		public static final int KEYCODE_BUTTON_L2 = 104;
		public static final int KEYCODE_BUTTON_R2 = 105;
		public static final int KEYCODE_BUTTON_THUMBL = 106;
		public static final int KEYCODE_BUTTON_THUMBR = 107;
		public static final int KEYCODE_BUTTON_START = 108;
		public static final int KEYCODE_BUTTON_SELECT = 109;
		public static final int KEYCODE_BUTTON_MODE = 110;
	}
	
	/**
	 * Enumeration of potentially available peripherals. Use with
	 * {@link Input#isPeripheralAvailable(Peripheral)}.
	 * @author mzechner
	 *
	 */
	public enum Peripheral {
		HardwareKeyboard,
		OnscreenKeyboard,				
		MultitouchScreen,
		Accelerometer,
		Compass,
		Vibrator
	}

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
	 * @return whether a new touch down event just occured.
	 */
	public boolean justTouched ();

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
	 * Whether a given button is pressed or not. Button constants can be found in {@link Buttons}. On Android only the Button#LEFT
	 * constant is meaningful.
	 * @param button the button to check.
	 * @return whether the button is down or not.
	 */
	public boolean isButtonPressed (int button);

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
	 * Vibrates for the given amount of time. Note that you'll need the permission
	 * <code> <uses-permission android:name="android.permission.VIBRATE" /></code> in your manifest file in order for this to work.
	 * 
	 * @param milliseconds the number of milliseconds to vibrate.
	 */
	public void vibrate (int milliseconds);
	
	/**
	 * Vibrate with a given pattern.
	 * Pass in an array of ints that are the times at which to turn on or off the vibrator. The first one is how long to wait before turning it on, and then after that it alternates. If you want to repeat, pass the index into the pattern at which to start the repeat.
	 * @param pattern an array of longs of times to turn the vibrator on or off.
	 * @param repeat the index into pattern at which to repeat, or -1 if you don't want to repeat.
	 */
	public void vibrate (long[] pattern, int repeat);
	
	/**
	 * Stops the vibrator
	 */
	public void cancelVibrate();
	
	/**
	 * The azimuth is the angle of the device's orientation around the z-axis. The
	 * positive z-axis points towards the earths center.
	 * 
	 * @see <a href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])</a>
	 * @return the azimuth in degrees
	 */
	public float getAzimuth();
	
	/** 
	 * The pitch is the angle of the device's orientation around the x-axis. The
	 * positive x-axis roughly points to the west and is orthogonal to the z- and y-axis.
	 * @see <a href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])</a>
	 * @return the pitch in degrees
	 */
	public float getPitch();
	
	/**
	 * The roll is the angle of the device's orientation around the y-axis. The
	 * positive y-axis points to the magnetic north pole of the earth.
	 * @see <a href="http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])">http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])</a>
	 * @return the roll in degrees
	 */
	public float getRoll();	
	
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
	
	/**
	 * @return the currently set {@link InputProcessor} or null.
	 */
	public InputProcessor getInputProcessor();
	
	/**
	 * Queries whether a {@link Peripheral} is currently available. In case of Android
	 * and the {@link Peripheral#HardwareKeyboard} this returns the whether the keyboard
	 * is currently slid out or not. 
	 * 
	 * @param peripheral the {@link Peripheral}
	 * @return whether the peripheral is available or not.
	 */
	public boolean isPeripheralAvailable(Peripheral peripheral);
}

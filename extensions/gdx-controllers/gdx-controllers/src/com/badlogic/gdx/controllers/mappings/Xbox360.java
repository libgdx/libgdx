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

package com.badlogic.gdx.controllers.mappings;

import com.badlogic.gdx.controllers.Controller;

/** Button and axis indices for the Xbox 360 {@link Controller}.
 * <p>
 * A few limitations still remain because some actions are mapped to different controls depending on the operating system:
 * <ul>
 * <li>On Windows, the Xbox button ({@link #BUTTON_XBOX}) is not available (the big button between the back and the start button).
 * <li>On OS X, the directional pad (D-pad) is mapped to 4 individual buttons instead of a POV (point-of-view) control.
 * <li>The back triggers behave differently on all platforms.
 * <ul>
 * <li>Windows: The left trigger maps to values between 0 and 1 and the right trigger maps to values between 0 and -1. Both values
 * are added together to create {@link #AXIS_BACK_TRIGGERS}. For example if the left trigger is completely pressed while the right
 * trigger isn't pressed this axis has the value 1. If only the right trigger is pressed then this axis has the value -1. However,
 * if both triggers are pressed this axis has value 0 again, because both values cancel each other out.
 * <li>OS X: The triggers are mapped to individual axes ({@link #AXIS_LEFT_TRIGGER} and {@link #AXIS_RIGHT_TRIGGER} ). Possible
 * values range from 0 for not pressed to 1 for fully pressed.
 * <li>Linux: The triggers are mapped to individual axes ({@link #AXIS_LEFT_TRIGGER} and {@link #AXIS_RIGHT_TRIGGER}). Possible
 * values range from -1 for not pressed to 1 for fully pressed.
 * </ul>
 * </ul>
 * @author Green Lightning */
public class Xbox360 {

	public static final float STICK_DEADZONE = 0.1f;
	public static final int UNDEFINED = -1;

	private static final OperatingSystem OS = getOperatingSystem(System.getProperty("os.name"));

	private static OperatingSystem getOperatingSystem (String name) {
		if (name == null) {
			return OperatingSystem.UNKNOWN;
		}
		name = name.toLowerCase();
		if (name.contains("win")) {
			return OperatingSystem.WINDOWS;
		}
		if (name.contains("mac")) {
			return OperatingSystem.OSX;
		}
		if (name.contains("nux")) {
			return OperatingSystem.LINUX;
		}
		return OperatingSystem.UNKNOWN;
	}

	/** <b>Warning:</b> Only available on Windows.
	 * <p>
	 * On windows the triggers on the back work a little differently than one might expect.
	 * <p>
	 * The left trigger maps to values between 0 and 1 and the right trigger maps to values between 0 and -1. Both values are added
	 * together and form this axis.
	 * <p>
	 * For example if the left trigger is completely pressed while the right trigger isn't pressed this axis has the value 1. If
	 * only the right trigger is pressed then this axis has the value -1. However, if both triggers are pressed this axis has value
	 * 0 again, because both values cancel each other out.
	 * @see #AXIS_LEFT_TRIGGER
	 * @see #AXIS_RIGHT_TRIGGER */
	public static final int AXIS_BACK_TRIGGERS = OS.axisBackTriggers;
	/** <b>Warning:</b> Not available on Windows and works differently on OS X and Linux.
	 * <p>
	 * <ul>
	 * <li>On OS X values range from 0 for not pressed to 1 for fully pressed.
	 * <li>On Linux values range from -1 for not pressed to 1 for fully pressed.
	 * </ul>
	 * @see #AXIS_BACK_TRIGGERS */
	public static final int AXIS_LEFT_TRIGGER = OS.axisLeftTrigger;
	/** <b>Warning:</b> Not available on Windows and works differently on OS X and Linux.
	 * <p>
	 * <ul>
	 * <li>On OS X values range from 0 for not pressed to 1 for fully pressed.
	 * <li>On Linux values range from -1 for not pressed to 1 for fully pressed.
	 * </ul>
	 * @see #AXIS_BACK_TRIGGERS */
	public static final int AXIS_RIGHT_TRIGGER = OS.axisRightTrigger;

	public static final int BUTTON_LEFT_BUMPER = OS.buttonLeftBumper;
	public static final int BUTTON_RIGHT_BUMPER = OS.buttonRightBumper;

	public static final int BUTTON_LEFT_STICK = OS.buttonLeftStick;
	public static final int AXIS_LEFT_X = OS.axisLeftX;
	public static final int AXIS_LEFT_Y = OS.axisLeftY;

	public static final int BUTTON_BACK = OS.buttonBack;
	/** <b>Warning:</b> Not available on Windows. */
	public static final int BUTTON_XBOX = OS.buttonXbox;
	public static final int BUTTON_START = OS.buttonStart;

	public static final int BUTTON_A = OS.buttonA;
	public static final int BUTTON_B = OS.buttonB;
	public static final int BUTTON_X = OS.buttonX;
	public static final int BUTTON_Y = OS.buttonY;

	/** <b>Warning:</b> Not available on OS X.
	 * @see #BUTTON_DPAD_UP
	 * @see #BUTTON_DPAD_DOWN
	 * @see #BUTTON_DPAD_LEFT
	 * @see #BUTTON_DPAD_RIGHT */
	public static final int POV_DPAD = OS.povDPad;

	/** <b>Warning:</b> Only available on OS X.
	 * @see #POV_DPAD */
	public static final int BUTTON_DPAD_UP = OS.buttonDPadUp;
	/** <b>Warning:</b> Only available on OS X.
	 * @see #POV_DPAD */
	public static final int BUTTON_DPAD_DOWN = OS.buttonDPadDown;
	/** <b>Warning:</b> Only available on OS X.
	 * @see #POV_DPAD */
	public static final int BUTTON_DPAD_LEFT = OS.buttonDPadLeft;
	/** <b>Warning:</b> Only available on OS X.
	 * @see #POV_DPAD */
	public static final int BUTTON_DPAD_RIGHT = OS.buttonDPadRight;

	public static final int BUTTON_RIGHT_STICK = OS.buttonRightStick;
	public static final int AXIS_RIGHT_X = OS.axisRightX;
	public static final int AXIS_RIGHT_Y = OS.axisRightY;

	private static enum OperatingSystem {
		UNKNOWN, WINDOWS {
			{
				axisBackTriggers = 4;

				buttonLeftBumper = 4;
				buttonRightBumper = 5;

				buttonLeftStick = 8;
				axisLeftX = 1;
				axisLeftY = 0;

				buttonBack = 6;
				buttonStart = 7;

				buttonA = 0;
				buttonB = 1;
				buttonX = 2;
				buttonY = 3;

				povDPad = 0;

				buttonRightStick = 9;
				axisRightX = 3;
				axisRightY = 2;
			}
		},
		OSX {
			{
				axisLeftTrigger = 0;
				axisRightTrigger = 1;

				buttonLeftBumper = 8;
				buttonRightBumper = 9;

				buttonLeftStick = 6;
				axisLeftX = 2;
				axisLeftY = 3;

				buttonBack = 5;
				buttonXbox = 10;
				buttonStart = 4;

				buttonA = 11;
				buttonB = 12;
				buttonX = 13;
				buttonY = 14;

				buttonDPadUp = 0;
				buttonDPadDown = 1;
				buttonDPadLeft = 2;
				buttonDPadRight = 3;

				buttonRightStick = 7;
				axisRightX = 4;
				axisRightY = 5;
			}
		},
		LINUX {
			{
				axisLeftTrigger = 2;
				axisRightTrigger = 5;

				buttonLeftBumper = 4;
				buttonRightBumper = 5;

				buttonLeftStick = 9;
				axisLeftX = 0;
				axisLeftY = 1;

				buttonBack = 6;
				buttonXbox = 8;
				buttonStart = 7;

				buttonA = 0;
				buttonB = 1;
				buttonX = 2;
				buttonY = 3;

				povDPad = 0;

				buttonRightStick = 10;
				axisRightX = 3;
				axisRightY = 4;
			}
		};

		public int axisBackTriggers = UNDEFINED;
		public int axisLeftTrigger = UNDEFINED;
		public int axisRightTrigger = UNDEFINED;

		public int buttonLeftBumper = UNDEFINED;
		public int buttonRightBumper = UNDEFINED;

		public int buttonLeftStick = UNDEFINED;
		public int axisLeftX = UNDEFINED;
		public int axisLeftY = UNDEFINED;

		public int buttonBack = UNDEFINED;
		public int buttonXbox = UNDEFINED;
		public int buttonStart = UNDEFINED;

		public int buttonA = UNDEFINED;
		public int buttonB = UNDEFINED;
		public int buttonX = UNDEFINED;
		public int buttonY = UNDEFINED;

		public int povDPad = UNDEFINED;
		public int buttonDPadUp = UNDEFINED;
		public int buttonDPadDown = UNDEFINED;
		public int buttonDPadLeft = UNDEFINED;
		public int buttonDPadRight = UNDEFINED;

		public int buttonRightStick = UNDEFINED;
		public int axisRightX = UNDEFINED;
		public int axisRightY = UNDEFINED;
	}

}

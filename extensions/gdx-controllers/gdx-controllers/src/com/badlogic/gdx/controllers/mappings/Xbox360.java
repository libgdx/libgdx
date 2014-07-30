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
 * @author Green Lightning */
public class Xbox360 {

	public static final float STICK_DEADZONE = 0.1f;
	public static final int UNDEFINED = -1;
	private static final OperatingSystem os;

	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			os = OperatingSystem.WINDOWS;
		} else if (osName.contains("mac")) {
			os = OperatingSystem.OSX;
		} else {
			os = OperatingSystem.UNKNOWN;
		}
	}

	/** On windows the triggers on the back work a little differently than one might expect.
	 * <p>
	 * The left trigger maps to values between 0 and 1 and the right trigger maps to values between 0 and -1. Both values are added
	 * together and form this axis. For example if the left trigger is completely pressed while the right trigger isn't pressed
	 * this axis has the value 1. If only the right trigger is pressed then this axis has the value -1. However, if both triggers
	 * are pressed this axis has value 0 again, because both values cancel each other out. */
	public static final int AXIS_BACK_TRIGGERS = os.axisBackTriggers;
	public static final int AXIS_LEFT_TRIGGER = os.axisLeftTrigger;
	public static final int AXIS_RIGHT_TRIGGER = os.axisRightTrigger;

	public static final int BUTTON_LEFT_BUMPER = os.buttonLeftBumper;
	public static final int BUTTON_RIGHT_BUMPER = os.buttonRightBumper;

	public static final int BUTTON_LEFT_STICK = os.buttonLeftStick;
	public static final int AXIS_LEFT_X = os.axisLeftX;
	public static final int AXIS_LEFT_Y = os.axisLeftY;

	public static final int BUTTON_BACK = os.buttonBack;
	public static final int BUTTON_XBOX = os.buttonXbox;
	public static final int BUTTON_START = os.buttonStart;

	public static final int BUTTON_A = os.buttonA;
	public static final int BUTTON_B = os.buttonB;
	public static final int BUTTON_X = os.buttonX;
	public static final int BUTTON_Y = os.buttonY;

	public static final int POV_DPAD = os.povDPad;

	public static final int BUTTON_DPAD_UP = os.buttonDPadUp;
	public static final int BUTTON_DPAD_DOWN = os.buttonDPadDown;
	public static final int BUTTON_DPAD_LEFT = os.buttonDPadLeft;
	public static final int BUTTON_DPAD_RIGHT = os.buttonDPadRight;

	public static final int BUTTON_RIGHT_STICK = os.buttonRightStick;
	public static final int AXIS_RIGHT_X = os.axisRightX;
	public static final int AXIS_RIGHT_Y = os.axisRightY;

	enum OperatingSystem {
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

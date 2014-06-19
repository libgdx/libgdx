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

import java.lang.reflect.Field;

import com.badlogic.gdx.controllers.Controller;

/** Button and axis indices for the OUYA {@link Controller}.
 * @author mzechner */
public class Ouya {
	public static final String ID = "OUYA Game Controller";
	public static final int BUTTON_O;
	public static final int BUTTON_U;
	public static final int BUTTON_Y;
	public static final int BUTTON_A;
	public static final int BUTTON_MENU;
	public static final int BUTTON_DPAD_UP;
	public static final int BUTTON_DPAD_DOWN;
	public static final int BUTTON_DPAD_RIGHT;
	public static final int BUTTON_DPAD_LEFT;
	public static final int BUTTON_L1;
	public static final int BUTTON_L2;
	public static final int BUTTON_L3;
	public static final int BUTTON_R1;
	public static final int BUTTON_R2;
	public static final int BUTTON_R3;
	public static final int AXIS_LEFT_X;
	public static final int AXIS_LEFT_Y;
	public static final int AXIS_LEFT_TRIGGER;
	public static final int AXIS_RIGHT_X;
	public static final int AXIS_RIGHT_Y;
	public static final int AXIS_RIGHT_TRIGGER;
	public static final float STICK_DEADZONE = 0.25F;
	
	/** whether the app is running on a real Ouya device **/
	public static final boolean runningOnOuya;

	/** To allow users to simulate running on Ouya **/
	public static boolean simulateRunningOnOuya = false;
	
	static {
		boolean isOuya = false;
		try {
			Class<?> buildClass = Class.forName("android.os.Build");
			Field deviceField = buildClass.getDeclaredField("DEVICE");
			Object device = deviceField.get(null);
			isOuya = "ouya_1_1".equals(device) || "cardhu".equals(device);
		} catch (Exception e) {
		}
		runningOnOuya = isOuya;

		BUTTON_O = 96;
		BUTTON_U = 99;
		BUTTON_Y = 100;
		BUTTON_A = 97;
		BUTTON_MENU = 82;
		BUTTON_DPAD_UP = 19;
		BUTTON_DPAD_DOWN = 20;
		BUTTON_DPAD_RIGHT = 22;
		BUTTON_DPAD_LEFT = 21;
		BUTTON_L1 = 104;
		BUTTON_L2 = 102;
		BUTTON_L3 = 106;
		BUTTON_R1 = 105;
		BUTTON_R2 = 103;
		BUTTON_R3 = 107;
		AXIS_LEFT_X = 0;
		AXIS_LEFT_Y = 1;
		AXIS_LEFT_TRIGGER = 2;
		AXIS_RIGHT_X = 3;
		AXIS_RIGHT_Y = 4;
		AXIS_RIGHT_TRIGGER = 5;

	}
	
	/** To allow users to have a single check to see if running on the Ouya 
		that also consideres the simulation flag **/
	public static boolean isRunningOnOuya()
	{
		return runningOnOuya || simulateRunningOnOuya;
	}
}

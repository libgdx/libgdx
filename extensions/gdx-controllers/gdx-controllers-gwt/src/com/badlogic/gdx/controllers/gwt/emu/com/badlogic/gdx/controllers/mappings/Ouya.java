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

/**
 * Button and axis indices for the OUYA {@link Controller}.
 * @author mzechner
 *
 */
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
	
	static {
		runningOnOuya = false;
	
		// Based on the "Standard" gamepad mapping
		BUTTON_O = 0;
		BUTTON_U = 1;
		BUTTON_Y = 2;
		BUTTON_A = 3;
		BUTTON_DPAD_UP = 12;
		BUTTON_DPAD_DOWN = 13;
		BUTTON_DPAD_RIGHT = 14;
		BUTTON_DPAD_LEFT = 15;
		BUTTON_L1 = 4;
		BUTTON_L2 = 6;
		BUTTON_L3 = 17; // unverified
		BUTTON_R1 = 5;
		BUTTON_R2 = 7;
		BUTTON_R3 = 18; // unverified
		AXIS_LEFT_X = 0;
		AXIS_LEFT_Y = 1;
		AXIS_LEFT_TRIGGER = 10;
		AXIS_RIGHT_X = 3;
		AXIS_RIGHT_Y = 4;
		AXIS_RIGHT_TRIGGER = 11;
		BUTTON_MENU = 108; // unverified
	}
}

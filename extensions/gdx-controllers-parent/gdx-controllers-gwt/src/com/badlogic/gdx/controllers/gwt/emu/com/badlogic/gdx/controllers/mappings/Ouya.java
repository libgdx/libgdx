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

/** Button and axis indices for the OUYA {@link Controller}.
 * @author mzechner */
public class Ouya {
	public static final String ID = "OUYA Game Controller";
	public static final int BUTTON_O = 0;
	public static final int BUTTON_U = 1;
	public static final int BUTTON_Y = 2;
	public static final int BUTTON_A = 3;
	public static final int BUTTON_DPAD_UP = 12;
	public static final int BUTTON_DPAD_DOWN = 13;
	public static final int BUTTON_DPAD_RIGHT = 14;
	public static final int BUTTON_DPAD_LEFT = 15;
	public static final int BUTTON_L1 = 4;
	public static final int BUTTON_L2 = 6;
	public static final int BUTTON_L3 = 17; // unverified
	public static final int BUTTON_R1 = 5;
	public static final int BUTTON_R2 = 7;
	public static final int BUTTON_R3 = 18; // unverified
	public static final int AXIS_LEFT_X = 0;
	public static final int AXIS_LEFT_Y = 1;
	public static final int AXIS_LEFT_TRIGGER = 10;
	public static final int AXIS_RIGHT_X = 3;
	public static final int AXIS_RIGHT_Y = 4;
	public static final int AXIS_RIGHT_TRIGGER = 11;
	public static final int BUTTON_MENU = 108; // unverified
	public static final float STICK_DEADZONE = 0.25F;
	/** whether the app is running on a real Ouya device **/
	public static final boolean runningOnOuya = false;
}

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
import com.badlogic.gdx.controllers.PovDirection;

/** Button and axis indices for the Xbox 360 {@link Controller}.
 * @author Green Lightning */
public class Xbox360 {

	public static final float STICK_DEADZONE = 0.1f;
	public static final int UNDEFINED = -1;

	public static final int AXIS_BACK_TRIGGERS = UNDEFINED;
	public static final int AXIS_LEFT_TRIGGER = 10;
	public static final int AXIS_RIGHT_TRIGGER = 11;

	public static final int BUTTON_LEFT_BUMPER = 4;
	public static final int BUTTON_RIGHT_BUMPER = 5;

	public static final int BUTTON_LEFT_STICK = 10;
	public static final int AXIS_LEFT_X = 0;
	public static final int AXIS_LEFT_Y = 1;

	public static final int BUTTON_BACK = 8;
	public static final int BUTTON_XBOX = 16;
	public static final int BUTTON_START = 9;

	public static final int BUTTON_A = 0;
	public static final int BUTTON_B = 1;
	public static final int BUTTON_X = 2;
	public static final int BUTTON_Y = 3;

	public static final int POV_DPAD = UNDEFINED;

	public static final int BUTTON_DPAD_UP = 12;
	public static final int BUTTON_DPAD_DOWN = 13;
	public static final int BUTTON_DPAD_LEFT = 14;
	public static final int BUTTON_DPAD_RIGHT = 15;

	public static final int BUTTON_RIGHT_STICK = 11;
	public static final int AXIS_RIGHT_X = 2;
	public static final int AXIS_RIGHT_Y = 3;

	public static PovDirection getDPad(Controller controller) {
		int x = 0, y = 0;
		if (isDPadLeftPressed(controller))
			x--;
		if (isDPadRightPressed(controller))
			x++;
		if (isDPadDownPressed(controller))
			y--;
		if (isDPadUpPressed(controller))
			y++;
		switch (3 * x + y) {
			case 3 * -1 + -1:
				return PovDirection.southWest;
			case 3 * -1 + 0:
				return PovDirection.west;
			case 3 * -1 + 1:
				return PovDirection.northWest;
			case 3 * 0 + -1:
				return PovDirection.south;
			case 3 * 0 + 0:
				return PovDirection.center;
			case 3 * 0 + 1:
				return PovDirection.north;
			case 3 * 1 + -1:
				return PovDirection.southEast;
			case 3 * 1 + 0:
				return PovDirection.east;
			case 3 * 1 + 1:
				return PovDirection.northEast;
			default:
				return PovDirection.center;
		}
	}

	public static boolean isDPadUpPressed(Controller controller) {
		return controller.getButton(BUTTON_DPAD_UP);
	}

	public static boolean isDPadDownPressed(Controller controller) {
		return controller.getButton(BUTTON_DPAD_DOWN);
	}

	public static boolean isDPadLeftPressed(Controller controller) {
		return controller.getButton(BUTTON_DPAD_LEFT);
	}

	public static boolean isDPadRightPressed(Controller controller) {
		return controller.getButton(BUTTON_DPAD_RIGHT);
	}

	public static float getLeftTrigger(Controller controller) {
		return controller.getAxis(AXIS_LEFT_TRIGGER);
	}

	public static float getRightTrigger(Controller controller) {
		return controller.getAxis(AXIS_RIGHT_TRIGGER);
	}

	public static float getBackTriggers(Controller controller) {
		return getLeftTrigger(controller) - getRightTrigger(controller);
	}

}

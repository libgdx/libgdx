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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** Mappings for the Xbox series of controllers.
 * 
 * See <a href="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/360_controller.svg/450px-360_controller.svg.png">this
 * image</a> which describes each button and axes.
 * 
 * All codes are for buttons expect the L_STICK_XXX, R_STICK_XXX, L_TRIGGER and R_TRIGGER codes, which are axes.
 * 
 * @author badlogic */
public class Xbox {
	// Buttons
	public static final int A;
	public static final int B;
	public static final int X;
	public static final int Y;
	public static final int GUIDE;
	public static final int L_BUMPER;
	public static final int R_BUMPER;
	public static final int BACK;
	public static final int START;
	public static final int DPAD_UP;
	public static final int DPAD_DOWN;
	public static final int DPAD_LEFT;
	public static final int DPAD_RIGHT;
	public static final int L_STICK;
	public static final int R_STICK;

	// Axes
	/** left trigger, -1 if not pressed, 1 if pressed **/
	public static final int L_TRIGGER;
	/** right trigger, -1 if not pressed, 1 if pressed **/
	public static final int R_TRIGGER;
	/** left stick vertical axis, -1 if up, 1 if down **/
	public static final int L_STICK_VERTICAL_AXIS;
	/** left stick horizontal axis, -1 if left, 1 if right **/
	public static final int L_STICK_HORIZONTAL_AXIS;
	/** right stick vertical axis, -1 if up, 1 if down **/
	public static final int R_STICK_VERTICAL_AXIS;
	/** right stick horizontal axis, -1 if left, 1 if right **/
	public static final int R_STICK_HORIZONTAL_AXIS;

	static {
		if (SharedLibraryLoader.isWindows) {
			if(Gdx.graphics.getType() == Graphics.GraphicsType.LWJGL3) {
				A = 0;
				B = 1;
				X = 2;
				Y = 3;
				GUIDE = -1;
				L_BUMPER = 4;
				R_BUMPER = 5;
				BACK = 6;
				START = 7;
				DPAD_UP = -1;
				DPAD_DOWN = -1;
				DPAD_LEFT = -1;
				DPAD_RIGHT = -1;
				L_TRIGGER = 4;
				R_TRIGGER = 5;
				L_STICK_VERTICAL_AXIS = 1;
				L_STICK_HORIZONTAL_AXIS = 0;
				L_STICK = 8;
				R_STICK_VERTICAL_AXIS = 3;
				R_STICK_HORIZONTAL_AXIS = 2;
				R_STICK = 9;
			} else {
				A = 0;
				B = 1;
				X = 2;
				Y = 3;
				GUIDE = -1;
				L_BUMPER = 4;
				R_BUMPER = 5;
				BACK = 6;
				START = 7;
				DPAD_UP = -1;
				DPAD_DOWN = -1;
				DPAD_LEFT = -1;
				DPAD_RIGHT = -1;
				L_TRIGGER = 4; // 0..1
				R_TRIGGER = 4; // 0..-1
				L_STICK_VERTICAL_AXIS = 0;
				L_STICK_HORIZONTAL_AXIS = 1;
				L_STICK = 8;
				R_STICK_VERTICAL_AXIS = 2;
				R_STICK_HORIZONTAL_AXIS = 3;
				R_STICK = 9;
			}
		} else if (SharedLibraryLoader.isLinux) {
			A = 0;
			B = 1;
			X = 2;
			Y = 3;
			GUIDE = 8;
			L_BUMPER = 4;
			R_BUMPER = 5;
			BACK = 6;
			START = 7;
			DPAD_UP = -1;
			DPAD_DOWN = -1;
			DPAD_LEFT = -1;
			DPAD_RIGHT = -1;
			L_TRIGGER = 2;
			R_TRIGGER = 5;
			L_STICK_VERTICAL_AXIS = 1;
			L_STICK_HORIZONTAL_AXIS = 0;
			L_STICK = 9;
			R_STICK_VERTICAL_AXIS = 4;
			R_STICK_HORIZONTAL_AXIS = 3;
			R_STICK = 10;
		} else if (SharedLibraryLoader.isMac) {
			A = 11;
			B = 12;
			X = 13;
			Y = 14;
			GUIDE = 10;
			L_BUMPER = 8;
			R_BUMPER = 9;
			BACK = 5;
			START = 4;
			DPAD_UP = 0;
			DPAD_DOWN = 1;
			DPAD_LEFT = 2;
			DPAD_RIGHT = 3;
			L_TRIGGER = 0;
			R_TRIGGER = 1;
			L_STICK_VERTICAL_AXIS = 3;
			L_STICK_HORIZONTAL_AXIS = 2;
			L_STICK = -1;
			R_STICK_VERTICAL_AXIS = 5;
			R_STICK_HORIZONTAL_AXIS = 4;
			R_STICK = -1;
		} else if (SharedLibraryLoader.isAndroid) {
			A = 96;
			B = 97;
			X = 99;
			Y = 100;
			GUIDE = 110;
			L_BUMPER = 102;
			R_BUMPER = 103;
			L_TRIGGER = 2;
			R_TRIGGER = 5;
			BACK = 109;
			START = 108;
			DPAD_UP = -1;
			DPAD_DOWN = -1;
			DPAD_LEFT = -1;
			DPAD_RIGHT = -1;
			L_STICK_VERTICAL_AXIS = 1;
			L_STICK_HORIZONTAL_AXIS = 0;
			L_STICK = 106;
			R_STICK_VERTICAL_AXIS = 4;
			R_STICK_HORIZONTAL_AXIS = 3;
			R_STICK = 107;
		} else {
			A = -1;
			B = -1;
			X = -1;
			Y = -1;
			GUIDE = -1;
			L_BUMPER = -1;
			R_BUMPER = -1;
			L_TRIGGER = -1;
			R_TRIGGER = -1;
			BACK = -1;
			START = -1;
			DPAD_UP = -1;
			DPAD_DOWN = -1;
			DPAD_LEFT = -1;
			DPAD_RIGHT = -1;
			L_STICK_VERTICAL_AXIS = -1;
			L_STICK_HORIZONTAL_AXIS = -1;
			L_STICK = -1;
			R_STICK_VERTICAL_AXIS = -1;
			R_STICK_HORIZONTAL_AXIS = -1;
			R_STICK = -1;
		}
	}
	
	/** @return whether the {@link Controller} is an Xbox controller
	 */
	public static boolean isXboxController(Controller controller) {
		String controllerName = controller.getName().toLowerCase();
		return (controllerName.contains("xbox") || controllerName.contains("x-box"));
	}
}

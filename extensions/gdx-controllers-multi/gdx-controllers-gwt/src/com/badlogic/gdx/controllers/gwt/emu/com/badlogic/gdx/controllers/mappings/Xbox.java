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
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** Mappings for the Xbox series of controllers. Based on "standard" controller mapping.
 * 
 * See <a href="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/360_controller.svg/450px-360_controller.svg.png">this
 * image</a> which describes each button and axes.
 * 
 * All codes are for buttons expect the L_STICK_XXX, R_STICK_XXX which are axes.
 * 
 * @author mobidevelop */
public class Xbox {
	// Buttons
	public static final int A = 0;
	public static final int B = 1;
	public static final int X = 2;
	public static final int Y = 3;
	public static final int GUIDE = 16;
	public static final int L_BUMPER = 4;
	public static final int R_BUMPER = 5;
    public static final int L_TRIGGER = 6; // FIXME: In desktop this is mapped as an Axis, but is a button on HTML?
    public static final int R_TRIGGER = 7; // FIXME: In desktop this is mapped as an Axis, but is a button on HTML?
	public static final int BACK = 8;
	public static final int START = 9;
	public static final int DPAD_UP = 12;
	public static final int DPAD_DOWN = 13;
	public static final int DPAD_LEFT = 14;
	public static final int DPAD_RIGHT = 15;
    
	// Axes
	/** left stick vertical axis, -1 if up, 1 if down **/
	public static final int L_STICK_VERTICAL_AXIS = 1;
	/** left stick horizontal axis, -1 if left, 1 if right **/
	public static final int L_STICK_HORIZONTAL_AXIS = 0;
	/** right stick vertical axis, -1 if up, 1 if down **/
	public static final int R_STICK_VERTICAL_AXIS = 3;
	/** right stick horizontal axis, -1 if left, 1 if right **/
	public static final int R_STICK_HORIZONTAL_AXIS = 2;
	
	/** @return whether the {@link Controller} is an Xbox controller
	 */
	public static boolean isXboxController(Controller controller) {
		return controller.getName().contains("Xbox");
	}
}

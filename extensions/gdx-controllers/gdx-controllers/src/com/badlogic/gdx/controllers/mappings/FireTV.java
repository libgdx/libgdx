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

/** Button and axis indices for the Amazon Kindle Fire TV {@link Controller}.
 * @author witnessmenow */
public class FireTV 
{
	
	/** whether the app is running on a real FireTV device **/
	public static final boolean runningOnFireTV;
	
	/** To allow users to simulate running on FireTV **/
	public static boolean simulateRunningOnFireTV = false;
	
	public static final String ID = "Amazon Fire Game Controller";
	public static final int BUTTON_A;
	public static final int BUTTON_X;
	public static final int BUTTON_Y;
	public static final int BUTTON_B;
	public static final int BUTTON_MENU;
	public static final int BUTTON_BACK;
	
	public static final int BUTTON_L1;
	public static final int BUTTON_L3;
	public static final int BUTTON_R1;
	public static final int BUTTON_R3;
	
	public static final int BUTTON_REWIND;
	public static final int BUTTON_PLAY_PAUSE;
	public static final int BUTTON_FAST_FOREWARD;
	
	public static final int AXIS_LEFT_X;
	public static final int AXIS_LEFT_Y;
	public static final int AXIS_L2_TRIGGER;
	public static final int AXIS_RIGHT_X;
	public static final int AXIS_RIGHT_Y;
	public static final int AXIS_R2_TRIGGER;
	
	public static final int AXIS_DPAD_X;
	public static final int AXIS_DPAD_Y;
	
	public static final float STICK_DEADZONE = 0.25F;

	static {
		boolean isAFT= false;
		try {
			Class<?> buildClass = Class.forName("android.os.Build");
			Field manufacturerField = buildClass.getDeclaredField("MANUFACTURER");
			String manufacturer = manufacturerField.get(null).toString();
			
			Field modelField = buildClass.getDeclaredField("MODEL");
			String model = modelField.get(null).toString();
			
			//Documentation says to only check first 3 letters for forward compatibility
			model = model.substring(0, 3);
			
			isAFT = "Amazon".equals(manufacturer) && "AFT".equals(model);
		} catch (Exception e) {
		}
		
		runningOnFireTV = isAFT;
		
		
		BUTTON_A = 96;
		BUTTON_X = 99;
		BUTTON_Y = 100;
		BUTTON_B = 97;
		BUTTON_MENU = 82;
		BUTTON_L1 = 102;
		BUTTON_R1 = 103;
		BUTTON_R3 = 107;
		BUTTON_L3 = 106;
		BUTTON_BACK = 4;
		BUTTON_REWIND = 89;
		BUTTON_PLAY_PAUSE = 85;
		BUTTON_FAST_FOREWARD = 90;
		

		AXIS_LEFT_X = 0;
		AXIS_LEFT_Y = 1;
		AXIS_L2_TRIGGER = 5;
		AXIS_RIGHT_X = 2;
		AXIS_RIGHT_Y = 3;
		AXIS_R2_TRIGGER = 4;
		
		AXIS_DPAD_Y = 7;
		AXIS_DPAD_X = 6;
	}
	
	/** To allow users to have a single check to see if running on the FireTV 
		that also consideres the simulation flag **/
	public static boolean isRunningOnFireTV()
	{
		return runningOnFireTV || simulateRunningOnFireTV;
	}
}

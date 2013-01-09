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
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

/** @author Nathan Sweet */
public interface Controller {
	public boolean getButton (int buttonCode);

	public float getAxis (int axisCode);

	public PovDirection getPov (int povCode);

	public boolean getSliderX (int sliderCode);

	public boolean getSliderY (int sliderCode);

	public Vector3 getAccelerometer (int accelerometerCode);

	public void setAccelerometerSensitivity (float sensitivity);

	public String getName ();

	public void addListener (ControllerListener listener);

	public void removeListener (ControllerListener listener);
	
	public static class Ouya {
//		public static final int BUTTON_O;
//		public static final int BUTTON_U;
//		public static final int BUTTON_Y;
//		public static final int BUTTON_A;
	}
}
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
	public boolean getButton (int buttonIndex);

	public float getAxis (int axisIndex);

	public PovDirection getPov (int povIndex);

	public boolean getSliderX (int sliderIndex);

	public boolean getSliderY (int sliderIndex);

	public Vector3 getAccelerometer (int accelerometerIndex);

	public void setAccelerometerSensitivity (float sensitivity);

	public int getControlCount (ControlType type);

	public String getName ();

	public void addListener (ControllerListener listener);

	public void removeListener (ControllerListener listener);
}
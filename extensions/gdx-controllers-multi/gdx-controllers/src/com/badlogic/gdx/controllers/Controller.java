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

/** Represents a connected controller. Provides methods to query the state of buttons, axes, POVs, sliders and accelerometers on
 * the controller. Multiple {@link ControllerListener} instances can be registered with the Controller to receive events in case
 * the controller's state changes. Listeners will be invoked on the rendering thread.
 * 
 * @author Nathan Sweet */
public interface Controller {
	/** @param buttonCode
	 * @return whether the button is pressed. */
	public boolean getButton (int buttonCode);

	/** @param axisCode
	 * @return the value of the axis, between -1 and 1 */
	public float getAxis (int axisCode);

	/** @param povCode
	 * @return the {@link PovDirection} */
	public PovDirection getPov (int povCode);

	/** @param sliderCode
	 * @return whether the slider is pressed */
	public boolean getSliderX (int sliderCode);

	/** @param sliderCode
	 * @return whether the slider is pressed */
	public boolean getSliderY (int sliderCode);

	/** @param accelerometerCode
	 * @return the accelerometer values on the 3 axis, in m/s^2 */
	public Vector3 getAccelerometer (int accelerometerCode);

	/** @param sensitivity the accelerometer sensitive, 0 (lowest) to 1 (highest) */
	public void setAccelerometerSensitivity (float sensitivity);

	/** @return the device name */
	public String getName ();

	/** Adds a new {@link ControllerListener} to this {@link Controller}. The listener will receive calls in case the state of the
	 * controller changes. The listener will be invoked on the rendering thread.
	 * @param listener */
	public void addListener (ControllerListener listener);

	/** Removes the given {@link ControllerListener}
	 * @param listener */
	public void removeListener (ControllerListener listener);
}

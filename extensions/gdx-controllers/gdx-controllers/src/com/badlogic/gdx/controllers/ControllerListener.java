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

import java.util.ResourceBundle.Control;

import com.badlogic.gdx.math.Vector3;

/** Registered with {@link Controllers} or a specific
 * {@link Controller} instance to receive controller events
 * @author Nathan Sweet */
public interface ControllerListener {
	/**
	 * A {@link Controller} got connected.
	 * @param controller
	 */
	public void connected(Controller controller);
	
	/**
	 * A {@link Controller} got disconnected.
	 * @param controller
	 */
	public void disconnected(Controller controller);
	
	/**
	 * A button on the {@link Controller} was pressed. The 
	 * buttonIndex is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts button constants for known controllers.
	 * @param controller
	 * @param buttonIndex
	 * @return whether to hand the event to other listeners.
	 */
	public boolean buttonDown (Controller controller, int buttonIndex);

	/**
	 * A button on the {@link Controller} was released. The
	 * buttonIndex is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts button constants for known controllers.
	 * @param controller
	 * @param buttonIndex
	 * @return whether to hand the event to other listeners.
	 */
	public boolean buttonUp (Controller controller, int buttonIndex);

	/**
	 * An axis on the {@link Controller} moved. The axisIndex is controller specific.
	 * The axis value is in the range [-1, 1]. The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts axes constants for known controllers.
	 * @param controller
	 * @param axisIndex
	 * @param value the axis value, -1 to 1
	 * @return whether to hand the event to other listeners.
	 */
	public boolean axisMoved (Controller controller, int axisIndex, float value);

	/**
	 * A POV on the {@link Controller} moved. The povIndex is controller specific.
	 * The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts POV constants for known controllers.
	 * @param controller
	 * @param povIndex
	 * @param value
	 * @return whether to hand the event to other listeners.
	 */
	public boolean povMoved (Controller controller, int povIndex, PovDirection value);

	/**
	 * An x-slider on the {@link Controller} moved. The sliderIndex is controller specific.
	 * The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts slider constants for known controllers.
	 * @param controller
	 * @param sliderIndex
	 * @param value
	 * @return whether to hand the event to other listeners.
	 */
	public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value);

	/**
	 * An y-slider on the {@link Controller} moved. The sliderIndex is controller specific.
	 * The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts slider constants for known controllers.
	 * @param controller
	 * @param sliderIndex
	 * @param value
	 * @return whether to hand the event to other listeners.
	 */
	public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value);

	/**
	 * An accelerometer value on the {@link Controller} changed. The accelerometerIndex is
	 * controller specific. The <code>com.badlogic.gdx.controllers.mapping</code>
	 * package hosts slider constants for known controllers. The value is a {@link Vector3}
	 * representing the acceleration on a 3-axis accelerometer in m/s^2.
	 * @param controller
	 * @param accelerometerIndex
	 * @param value
	 * @return whether to hand the event to other listeners.
	 */
	public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value);
}
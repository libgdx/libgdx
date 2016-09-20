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

/** Registered with {@link Controllers} or a specific {@link Controller} instance to receive events.
 * @author Nathan Sweet */
public interface ControllerListener {
	/** A {@link Controller} got connected.
	 * @param controller */
	public void connected (Controller controller);

	/** A {@link Controller} got disconnected.
	 * @param controller */
	public void disconnected (Controller controller);

	/** A button on the {@link Controller} was pressed. The buttonCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts button constants for known controllers.
	 * @param controller
	 * @param buttonCode
	 * @return whether to hand the event to other listeners. */
	public boolean buttonDown (Controller controller, int buttonCode);

	/** A button on the {@link Controller} was released. The buttonCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts button constants for known controllers.
	 * @param controller
	 * @param buttonCode
	 * @return whether to hand the event to other listeners. */
	public boolean buttonUp (Controller controller, int buttonCode);

	/** An axis on the {@link Controller} moved. The axisCode is controller specific. The axis value is in the range [-1, 1]. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts axes constants for known controllers.
	 * @param controller
	 * @param axisCode
	 * @param value the axis value, -1 to 1
	 * @return whether to hand the event to other listeners. */
	public boolean axisMoved (Controller controller, int axisCode, float value);

	/** A POV on the {@link Controller} moved. The povCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts POV constants for known controllers.
	 * @param controller
	 * @param povCode
	 * @param value
	 * @return whether to hand the event to other listeners. */
	public boolean povMoved (Controller controller, int povCode, PovDirection value);

	/** An x-slider on the {@link Controller} moved. The sliderCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers.
	 * @param controller
	 * @param sliderCode
	 * @param value
	 * @return whether to hand the event to other listeners. */
	public boolean xSliderMoved (Controller controller, int sliderCode, boolean value);

	/** An y-slider on the {@link Controller} moved. The sliderCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers.
	 * @param controller
	 * @param sliderCode
	 * @param value
	 * @return whether to hand the event to other listeners. */
	public boolean ySliderMoved (Controller controller, int sliderCode, boolean value);

	/** An accelerometer value on the {@link Controller} changed. The accelerometerCode is controller specific. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers. The value is a
	 * {@link Vector3} representing the acceleration on a 3-axis accelerometer in m/s^2.
	 * @param controller
	 * @param accelerometerCode
	 * @param value
	 * @return whether to hand the event to other listeners. */
	public boolean accelerometerMoved (Controller controller, int accelerometerCode, Vector3 value);
}

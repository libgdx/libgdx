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

package com.badlogic.gdx.controllers.desktop.ois;

import com.badlogic.gdx.controllers.desktop.ois.OisJoystick.OisPov;

/** @author mzechner
 * @author Nathan Sweet */
public interface OisListener {
	public void buttonPressed (OisJoystick joystick, int buttonIndex);

	public void buttonReleased (OisJoystick joystick, int buttonIndex);

	public void axisMoved (OisJoystick joystick, int axisIndex, float value);

	public void povMoved (OisJoystick joystick, int povIndex, OisPov value);

	public void xSliderMoved (OisJoystick joystick, int sliderIndex, boolean value);

	public void ySliderMoved (OisJoystick joystick, int sliderIndex, boolean value);
}

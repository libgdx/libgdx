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

/** A base implementation for {@link ControllerListener}. Subclass this if you are only interested in a few specific events.
 * @author mzechner */
public class ControllerAdapter implements ControllerListener {
	@Override
	public boolean buttonDown (Controller controller, int buttonIndex) {
		return false;
	}

	@Override
	public boolean buttonUp (Controller controller, int buttonIndex) {
		return false;
	}

	@Override
	public boolean axisMoved (Controller controller, int axisIndex, float value) {
		return false;
	}

	@Override
	public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
		return false;
	}

	@Override
	public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value) {
		return false;
	}

	@Override
	public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
		return false;
	}

	@Override
	public void connected (Controller controller) {
	}

	@Override
	public void disconnected (Controller controller) {
	}
}

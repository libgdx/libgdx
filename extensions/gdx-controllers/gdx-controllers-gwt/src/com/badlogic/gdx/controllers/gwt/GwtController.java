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

package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.gwt.support.Gamepad;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntFloatMap;

public class GwtController implements Controller {

	private int index;
	
	private String name;
	
	protected final float[] axes;
	
	protected final IntFloatMap buttons = new IntFloatMap();
	
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public GwtController(int index, String name) {
		this.index = index;
		this.name = name;
		
		Gamepad gamepad = Gamepad.getGamepad(index);
		axes = new float[gamepad.getAxes().length()];
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean getButton(int buttonCode) {
		return buttons.get(buttonCode, 0) >= 0.5f;
	}

	public float getButtonAmount(int buttonCode) {
		return buttons.get(buttonCode, 0);
	}
	
	@Override
	public float getAxis(int axisIndex) {
		if(axisIndex < 0 || axisIndex >= axes.length) return 0;
		return axes[axisIndex];
	}

	@Override
	public PovDirection getPov(int povCode) {
		return PovDirection.center;
	}

	@Override
	public boolean getSliderX(int sliderCode) {
		return false;
	}

	@Override
	public boolean getSliderY(int sliderCode) {
		return false;
	}

	@Override
	public Vector3 getAccelerometer(int accelerometerCode) {
		return Vector3.Zero;
	}

	@Override
	public void setAccelerometerSensitivity(float sensitivity) {
		// Nope
	}

	@Override
	public void addListener(ControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(ControllerListener listener) {
		this.listeners.removeValue(listener, true);
	}
	
	public Array<ControllerListener> getListeners() {
		return listeners;
	}
}
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

package com.badlogic.gdx.controllers.android;

import android.view.InputDevice;
import android.view.InputDevice.MotionRange;
import android.view.MotionEvent;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

public class AndroidController implements Controller {
	private final int deviceId;
	private boolean attached;
	private final String name;
	protected final IntIntMap buttons = new IntIntMap();
	protected final float[] axes;
	protected final int[] axesIds;
	protected int pov = 0;
	private boolean povAxis;
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public AndroidController(int deviceId, String name) {
		this.deviceId = deviceId;
		this.name = name;
		
		InputDevice device = InputDevice.getDevice(deviceId);
		int numAxes = 0;
		for (MotionRange range : device.getMotionRanges()) {
			if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
				if (range.getAxis() != MotionEvent.AXIS_HAT_X && range.getAxis() != MotionEvent.AXIS_HAT_Y) {
					numAxes += 1;
				} else {
					povAxis = true;
				}
			}
		}

		axesIds = new int[numAxes];
		axes = new float[numAxes];
		int i = 0;
		for (MotionRange range : device.getMotionRanges()) {
			if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
				if (range.getAxis() != MotionEvent.AXIS_HAT_X && range.getAxis() != MotionEvent.AXIS_HAT_Y) {
					axesIds[i++] = range.getAxis();
				}
			}
		}
	}

	public boolean isAttached () {
		return attached;
	}

	public boolean hasPovAxis() {
		return povAxis;
	}

	public void setAttached (boolean attached) {
		this.attached = attached;
	}

	public int getDeviceId () {
		return deviceId;
	}
	
	@Override
	public boolean getSliderX (int sliderIndex) {
		return false;
	}

	@Override
	public boolean getSliderY (int sliderIndex) {
		return false;
	}

	@Override
	public Vector3 getAccelerometer (int accelerometerIndex) {
		return Vector3.Zero;
	}

	@Override
	public void setAccelerometerSensitivity (float sensitivity) {
	}

	@Override
	public void addListener (ControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener (ControllerListener listener) {
		this.listeners.removeValue(listener, true);
	}
	
	public Array<ControllerListener> getListeners() {
		return this.listeners;
	}

	@Override
	public boolean getButton (int buttonIndex) {
		return buttons.containsKey(buttonIndex);
	}

	@Override
	public float getAxis (int axisIndex) {
		if(axisIndex < 0 || axisIndex >= axes.length) return 0;
		return axes[axisIndex];
	}

	@Override
	public PovDirection getPov (int povIndex) {
		if (povIndex != 0) return PovDirection.center;
		switch (pov) {
			case 0x00000000:
				return PovDirection.center;
			case 0x00000001:
				return PovDirection.north;
			case 0x00000010:
				return PovDirection.south;
			case 0x00000100:
				return PovDirection.east;
			case 0x00001000:
				return PovDirection.west;
			case 0x00000101:
				return PovDirection.northEast;
			case 0x00000110:
				return PovDirection.southEast;
			case 0x00001001:
				return PovDirection.northWest;
			case 0x00001010:
				return PovDirection.southWest;
			default:
				throw new RuntimeException("Unexpected POV value : " + pov);
		}
	}

	@Override
	public String getName () {
		return name;
	}
}
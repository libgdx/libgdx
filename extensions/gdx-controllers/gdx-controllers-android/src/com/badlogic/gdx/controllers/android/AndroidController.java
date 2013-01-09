package com.badlogic.gdx.controllers.android;

import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntIntMap;

public class AndroidController implements Controller {
	private final int deviceId;
	private boolean attached;
	private final String name;
	protected final IntIntMap buttons = new IntIntMap();
	protected final IntFloatMap axes = new IntFloatMap();
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public AndroidController(int deviceId, String name) {
		this.deviceId = deviceId;
		this.name = name;
	}

	public boolean isAttached () {
		return attached;
	}

	public void setAttached (boolean attached) {
		this.attached = attached;
	}

	public int getDeviceId () {
		return deviceId;
	}
	
	@Override
	public boolean getSliderX (int sliderIndex) {
		throw new GdxRuntimeException("Invalid slider index");
	}

	@Override
	public boolean getSliderY (int sliderIndex) {
		throw new GdxRuntimeException("Invalid slider index");
	}

	@Override
	public Vector3 getAccelerometer (int accelerometerIndex) {
		throw new GdxRuntimeException("Invalid accelerometer index");
	}

	@Override
	public void setAccelerometerSensitivity (float sensitivity) {
		throw new GdxRuntimeException("Invalid accelerometer index");
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
		return axes.get(axisIndex, 0);
	}

	@Override
	public PovDirection getPov (int povIndex) {
		return PovDirection.center;
	}

	@Override
	public String getName () {
		return name;
	}
}
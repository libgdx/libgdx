package com.badlogic.gdx.controllers.android;

import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AndroidController implements Controller {
	private final int deviceId;
	private boolean attached;
	private final String name;
	private final boolean[] buttons;
	private final PovDirection[] povs;
	private final float[] axes;
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public AndroidController(int deviceId, String name) {
		this.deviceId = deviceId;
		this.name = name;
		this.buttons = new boolean[0];
		this.povs = new PovDirection[0];
		this.axes = new float[0];
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

	@Override
	public boolean getButton (int buttonIndex) {
		return buttons[buttonIndex];
	}

	@Override
	public float getAxis (int axisIndex) {
		return axes[axisIndex];
	}

	@Override
	public PovDirection getPov (int povIndex) {
		return povs[povIndex];
	}

	@Override
	public String getName () {
		return name;
	}
}
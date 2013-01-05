
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

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

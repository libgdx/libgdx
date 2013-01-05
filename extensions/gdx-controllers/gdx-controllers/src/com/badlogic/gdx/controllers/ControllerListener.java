
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

/** @author Nathan Sweet */
public interface ControllerListener {
	public boolean buttonDown (Controller controller, int buttonIndex);

	public boolean buttonUp (Controller controller, int buttonIndex);

	public boolean axisMoved (Controller controller, int axisIndex, float value);

	public boolean povMoved (Controller controller, int povId, PovDirection value);

	public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value);

	public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value);

	public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value);
}

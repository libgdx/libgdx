
package com.badlogic.gdx.controllers;

public interface ControllerListener {
	public boolean buttonDown (Controller controller, int buttonIndex);

	public boolean buttonUp (Controller controller, int buttonIndex);

	public boolean axisMoved (Controller controller, int axisIndex);

	public boolean povMoved (Controller controller, int povId, PovDirection direction);

	public boolean sliderMoved (Controller controller, int sliderIndex);

	public boolean accelerometerMoved (Controller controller, int accelerometerIndex);
}

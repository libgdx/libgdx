
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

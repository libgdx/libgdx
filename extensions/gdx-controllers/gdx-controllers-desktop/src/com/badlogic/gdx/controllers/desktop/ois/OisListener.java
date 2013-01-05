
package com.badlogic.gdx.controllers.desktop.ois;

import com.badlogic.gdx.controllers.desktop.ois.OisJoystick.OisPov;

public interface OisListener {
	public void buttonPressed (OisJoystick joystick, int button);

	public void buttonReleased (OisJoystick joystick, int button);

	public void axisMoved (OisJoystick joystick, int axis, float value);

	public void povMoved (OisJoystick joystick, int pov, OisPov direction);

	public void sliderMoved (OisJoystick joystick, int slider, boolean x, boolean y);
}

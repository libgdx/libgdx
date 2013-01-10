package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

/**
 * A base implementation for {@link ControllerListener}. Subclass this
 * if you are only intersted in a few specific events.
 * @author mzechner
 *
 */
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

package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;

/**
 * Used on platforms that don't support the extenions, e.g. HTML5
 * and iOS.
 * @author mzechner
 *
 */
public class ControllerManagerStub implements ControllerManager {
	Array<Controller> controllers = new Array<Controller>();
	@Override
	public Array<Controller> getControllers () {
		return controllers;
	}

	@Override
	public void addListener (ControllerListener listener) {
	}

	@Override
	public void removeListener (ControllerListener listener) {
	}
}

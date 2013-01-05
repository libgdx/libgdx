
package com.badlogic.gdx.controllers.android;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class AndroidControllerManager implements ControllerManager {
	final Array<Controller> controllers = new Array();
	final Array<ControllerListener> listeners = new Array();

	public AndroidControllerManager () {
		new OuyaControllers(this);
	}

	public Array<Controller> getControllers () {
		return controllers;
	}

	public void addListener (ControllerListener listener) {
		listeners.add(listener);
	}

	public void removeListener (ControllerListener listener) {
		listeners.removeValue(listener, true);
	}
}

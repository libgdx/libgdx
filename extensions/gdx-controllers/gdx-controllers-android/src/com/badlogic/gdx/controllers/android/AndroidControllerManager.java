
package com.badlogic.gdx.controllers.android;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class AndroidControllerManager implements ControllerManager {
	private final Array<Controller> controllers = new Array();
	private final Array<Controller> workingControllers = new Array();
	final Array<ControllerListener> listeners = new Array();

	public AndroidControllerManager () {
//		new AndroidControllers(this);
	}

	public synchronized Array<Controller> getControllers () {
		return controllers;
	}

	public synchronized void addListener (ControllerListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener (ControllerListener listener) {
		listeners.removeValue(listener, true);
	}
	
	synchronized void addController(Controller controller) {
		if(controllers.contains(controller, true)) return;
		else controllers.add(controller);
	}
	
	synchronized void removeController(Controller controller) {
		controllers.removeValue(controller, true);
	}
}

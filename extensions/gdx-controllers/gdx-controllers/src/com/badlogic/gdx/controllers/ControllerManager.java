
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public interface ControllerManager {
	public Array<Controller> getControllers ();

	public void addListener (ControllerListener listener);

	public void removeListener (ControllerListener listener);
}

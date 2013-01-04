
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;

public interface Controllers {
	public Array<Controller> getControllers ();

	public void addListener (ControllerListener listener);

	public void removeListener (ControllerListener listener);
}

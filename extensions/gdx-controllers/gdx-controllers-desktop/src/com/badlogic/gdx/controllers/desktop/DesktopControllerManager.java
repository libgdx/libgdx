/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.controllers.desktop;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** @author Nathan Sweet */
public class DesktopControllerManager implements ControllerManager {
	final Array<Controller> controllers = new Array();
	final Array<ControllerListener> listeners = new Array();

	public DesktopControllerManager () {
		new SharedLibraryLoader().load("gdx-controllers-desktop");
		new OisControllers(this);
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

	@Override
	public void clearListeners () {
		listeners.clear();
	}

	@Override
	public Array<ControllerListener> getListeners () {
		return listeners;
	}
}

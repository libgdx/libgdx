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
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author Nathan Sweet */
public class Controllers {
	static private ControllerManager manager;

	static public Array<Controller> getControllers () {
		if (manager == null) initialize();
		return manager.getControllers();
	}

	static public void addListener (ControllerListener listener) {
		if (manager == null) initialize();
		manager.addListener(listener);
	}

	static public void removeListener (ControllerListener listener) {
		if (manager == null) initialize();
		manager.removeListener(listener);
	}

	static private void initialize () {
		String className;
		switch (Gdx.app.getType()) {
		case Android:
			className = "com.badlogic.gdx.controllers.android.AndroidControllerManager";
			break;
		case Desktop:
			className = "com.badlogic.gdx.controllers.desktop.DesktopControllerManager";
			break;
		default:
			throw new GdxRuntimeException("No controller manager is available for: " + Gdx.app.getType());
		}
		try {
			manager = (ControllerManager)Class.forName(className).newInstance();
		} catch (Throwable ex) {
			throw new GdxRuntimeException("Error creating controller manager: " + className, ex);
		}
	}
}
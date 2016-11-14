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

package com.badlogic.gdx.controllers.android;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidInput;

/**
 * Used on +4.1 to get events on device connects/disconnects.
 * @author mzechner
 *
 */
public class ControllerLifeCycleListener implements LifecycleListener, InputDeviceListener {
	private static final String TAG = "ControllerLifeCycleListener";
	private final InputManager inputManager;
	private final AndroidControllers controllers;
	
	public ControllerLifeCycleListener(AndroidControllers controllers) {
		this.controllers = controllers;
		this.inputManager = (InputManager)((Context)Gdx.app).getSystemService(Context.INPUT_SERVICE);
		Gdx.app.addLifecycleListener(this);
		inputManager.registerInputDeviceListener(this, ((AndroidApplication)Gdx.app).handler);
	}

	@Override
	public void resume () {
		inputManager.registerInputDeviceListener(this, ((AndroidApplication)Gdx.app).handler);
		Gdx.app.log(TAG, "controller life cycle listener resumed");
	}
	
	@Override
	public void pause () {
		inputManager.unregisterInputDeviceListener(this);
		Gdx.app.log(TAG, "controller life cycle listener paused");
	}
	
	@Override
	public void onInputDeviceAdded (int deviceId) {
		controllers.addController(deviceId, true);
		Gdx.app.log(TAG, "device " + deviceId + " added");
	}

	@Override
	public void onInputDeviceRemoved (int deviceId) {
		controllers.removeController(deviceId);
		Gdx.app.log(TAG, "device " + deviceId + " removed");
	}
	
	@Override
	public void onInputDeviceChanged (int deviceId) {
	}

	@Override
	public void dispose () {
	}
}
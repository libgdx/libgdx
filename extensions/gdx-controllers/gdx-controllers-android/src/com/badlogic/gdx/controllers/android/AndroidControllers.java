package com.badlogic.gdx.controllers.android;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnGenericMotionListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidInput.PauseResumeListener;
import com.badlogic.gdx.backends.android.AndroidInputThreePlus;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.Pool;

public class AndroidControllers implements PauseResumeListener, InputDeviceListener, ControllerManager, OnKeyListener, OnGenericMotionListener {
	private final static String TAG = "AndroidControllers";
	private final InputManager inputManager;
	private final IntMap<AndroidController> controllerMap = new IntMap<AndroidController>();
	private final Array<Controller> controllers = new Array<Controller>();
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	private final Array<AndroidControllerEvent> eventQueue = new Array<AndroidControllerEvent>();
	private final Pool<AndroidControllerEvent> eventPool = new Pool<AndroidControllerEvent>() {
		@Override
		protected AndroidControllerEvent newObject () {
			return new AndroidControllerEvent();
		}
	};
	
	public AndroidControllers() {
		this.inputManager = (InputManager)((Context)Gdx.app).getSystemService(Context.INPUT_SERVICE);
		
		// register this instance with pause/resume events and call
		// resume so that the currently attached devices are initialized
		((AndroidInput)Gdx.input).addPauseResumeListener(this);
		resume();
		setupEventQueue();
		((AndroidInput)Gdx.input).addKeyListener(this);
		((AndroidInputThreePlus)Gdx.input).addGenericMotionListener(this);
		Gdx.app.log(TAG, "controllers: " + inputManager.getInputDeviceIds().length);
	}
	
	private void setupEventQueue() {
		new Runnable() {
			@Override
			public void run () {
				synchronized(eventQueue) {
					for(AndroidControllerEvent event: eventQueue) {
						
					}
					eventPool.freeAll(eventQueue);
					eventPool.clear();
				}
				Gdx.app.postRunnable(this);
			}
		}.run();
	}
	
	@Override
	public boolean onGenericMotion (View view, MotionEvent motionEvent) {
		AndroidController controller = controllerMap.get(motionEvent.getDeviceId());
		if(controller != null) {
			synchronized(eventQueue) {
				AndroidControllerEvent event = eventPool.obtain();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKey (View view, int keyCode, KeyEvent keyEvent) {
		AndroidController controller = controllerMap.get(keyEvent.getDeviceId());
		if(controller != null) {
			synchronized(eventQueue) {
				AndroidControllerEvent event = eventPool.obtain();
				event.controller = controller;
				if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					event.type = AndroidControllerEvent.BUTTON_DOWN;
				} else {
					event.type = AndroidControllerEvent.BUTTON_UP;
				}
				event.code = keyCode;
				eventQueue.add(event);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void addController(int deviceId) {
		InputDevice device = inputManager.getInputDevice(deviceId);
		if(!isController(device)) return;
		String name = device.getName();
		AndroidController controller = new AndroidController(deviceId, name);
		controllerMap.put(deviceId, controller);
		synchronized(eventQueue) {
			AndroidControllerEvent event = eventPool.obtain();
			event.type = AndroidControllerEvent.CONNECTED;
			event.controller = controller;
			eventQueue.add(event);
		}
	}
	
	private void removeController(int deviceId) {
		AndroidController controller = controllerMap.remove(deviceId);
		if(controller != null) {
			synchronized(eventQueue) {
				AndroidControllerEvent event = eventPool.obtain();
				event.type = AndroidControllerEvent.DISCONNECTED;
				event.controller = controller;
				eventQueue.add(event);
			}
		}
	}
	
	private boolean isController(InputDevice device) {
		return ((device.getSources() & InputDevice.SOURCE_GAMEPAD) | 
				 (device.getSources() & InputDevice.SOURCE_JOYSTICK)) != 0;
	}
	
	@Override
	public void resume () {
		inputManager.registerInputDeviceListener(this, ((AndroidApplication)Gdx.app).handler);
		
		// gather all joysticks and gamepads, remove any disconnected ones
		IntMap<AndroidController> removedControllers = new IntMap<AndroidController>();
		removedControllers.putAll(controllerMap);
		
		for(int deviceId: inputManager.getInputDeviceIds()) {
			InputDevice device = inputManager.getInputDevice(deviceId);
			AndroidController controller = controllerMap.get(deviceId);
			if(controller != null) {
				removedControllers.remove(deviceId);
			} else {
				addController(deviceId);
			}
		}
		
		for(Entry<AndroidController> entry: removedControllers.entries()) {
			removeController(entry.key);
		}
	}
	
	@Override
	public void pause () {
		inputManager.unregisterInputDeviceListener(this);
		Gdx.app.log(TAG, "controllers paused");
	}
	
	@Override
	public void onInputDeviceAdded (int deviceId) {
		addController(deviceId);
		Gdx.app.log(TAG, "device " + deviceId + " added");
	}

	@Override
	public void onInputDeviceRemoved (int deviceId) {
		removeController(deviceId);
		Gdx.app.log(TAG, "device " + deviceId + " removed");
	}
	
	@Override
	public void onInputDeviceChanged (int deviceId) {
	}

	@Override
	public Array<Controller> getControllers () {
		return controllers;
	}

	@Override
	public void addListener (ControllerListener listener) {
		synchronized(eventQueue) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener (ControllerListener listener) {
		synchronized(eventQueue) {
			listeners.removeValue(listener, true);
		}
	}
}

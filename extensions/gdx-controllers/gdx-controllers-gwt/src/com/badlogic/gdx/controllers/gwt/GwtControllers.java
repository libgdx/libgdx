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

package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.controllers.gwt.support.Gamepad;
import com.badlogic.gdx.controllers.gwt.support.GamepadSupport;
import com.badlogic.gdx.controllers.gwt.support.GamepadSupportListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.google.gwt.core.client.JsArrayNumber;

public class GwtControllers implements ControllerManager, GamepadSupportListener {

	private final IntMap<GwtController> controllerMap = new IntMap<GwtController>();
	private final Array<Controller> controllers = new Array<Controller>();
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	private final Array<GwtControllerEvent> eventQueue = new Array<GwtControllerEvent>();
	private final Pool<GwtControllerEvent> eventPool = new Pool<GwtControllerEvent>() {
		@Override
		protected GwtControllerEvent newObject () {
			return new GwtControllerEvent();
		}
	};
	
	public GwtControllers() {
		GamepadSupport.init(this);
		setupEventQueue();
	}
	
	public void setupEventQueue() {
		new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run () {
				synchronized(eventQueue) {
					for(GwtControllerEvent event: eventQueue) {
						switch(event.type) {
							case GwtControllerEvent.CONNECTED:
								controllers.add(event.controller);
								for(ControllerListener listener: listeners) {
									listener.connected(event.controller);
								}
								break;
							case GwtControllerEvent.DISCONNECTED:
								controllers.removeValue(event.controller, true);
								for(ControllerListener listener: listeners) {
									listener.disconnected(event.controller);
								}
								for(ControllerListener listener: event.controller.getListeners()) {
									listener.disconnected(event.controller);
								}
								break;
							case GwtControllerEvent.BUTTON_DOWN:
								event.controller.buttons.put(event.code, event.amount);
								for(ControllerListener listener: listeners) {
									if(listener.buttonDown(event.controller, event.code)) break;
								}
								for(ControllerListener listener: event.controller.getListeners()) {
									if(listener.buttonDown(event.controller, event.code)) break;
								}
								break;
							case GwtControllerEvent.BUTTON_UP:
								event.controller.buttons.remove(event.code, event.amount);
								for(ControllerListener listener: listeners) {
									if(listener.buttonUp(event.controller, event.code)) break;
								}
								for(ControllerListener listener: event.controller.getListeners()) {
									if(listener.buttonUp(event.controller, event.code)) break;
								}
								break;
							case GwtControllerEvent.AXIS:
								event.controller.axes[event.code] = event.amount;
								for(ControllerListener listener: listeners) {
									if(listener.axisMoved(event.controller, event.code, event.amount)) break;
								}
								for(ControllerListener listener: event.controller.getListeners()) {
									if(listener.axisMoved(event.controller, event.code, event.amount)) break;
								}
								break;
							default:
						}
					}
					eventPool.freeAll(eventQueue);
					eventQueue.clear();
				}
				Gdx.app.postRunnable(this);
			}
		}.run();
	}
	
	@Override
	public Array<Controller> getControllers() {
		return controllers;
	}

	@Override
	public void addListener(ControllerListener listener) {
		synchronized(eventQueue) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(ControllerListener listener) {
		synchronized(eventQueue) {
			listeners.removeValue(listener, true);
		}
	}

	@Override
	public void onGamepadConnected(int index) {
		Gamepad gamepad = Gamepad.getGamepad(index);
		GwtController controller = new GwtController(gamepad.getIndex(), gamepad.getId());
		controllerMap.put(index, controller);
		synchronized(eventQueue) {
			GwtControllerEvent event = eventPool.obtain();
			event.type = GwtControllerEvent.CONNECTED;
			event.controller = controller;
			eventQueue.add(event);
		}
	}

	@Override
	public void onGamepadDisconnected(int index) {
		GwtController controller = controllerMap.remove(index);
		if(controller != null) {
			synchronized(eventQueue) {
				GwtControllerEvent event = eventPool.obtain();
				event.type = GwtControllerEvent.DISCONNECTED;
				event.controller = controller;
				eventQueue.add(event);
			}
		}
	}
	
	@Override
	public void onGamepadUpdated(int index) {
		Gamepad gamepad = Gamepad.getGamepad(index);
		GwtController controller = controllerMap.get(index);
		if (gamepad != null && controller != null) {
			// Determine what changed
			JsArrayNumber axes = gamepad.getAxes();
			JsArrayNumber buttons = gamepad.getButtons();
			synchronized(eventQueue) {
				for (int i = 0, j = axes.length(); i < j; i++) {
					float oldAxis = controller.getAxis(i);
					float newAxis = (float) axes.get(i);
					if (oldAxis != newAxis) {
						GwtControllerEvent event = eventPool.obtain();
						event.type = GwtControllerEvent.AXIS;
						event.controller = controller;
						event.code = i;						
						event.amount = newAxis;
						eventQueue.add(event);
					}
				}			
				for (int i = 0, j = buttons.length(); i < j; i++) {
					float oldButton = controller.getButtonAmount(i);
					float newButton = (float) buttons.get(i);
					if (oldButton != newButton) {
						if ((oldButton < 0.5f && newButton < 0.5f) || (oldButton >= 0.5f && newButton >= 0.5f)) {
							controller.buttons.put(i, newButton);
							continue;
						}

						GwtControllerEvent event = eventPool.obtain();
						event.type = newButton >= 0.5f ? GwtControllerEvent.BUTTON_DOWN : GwtControllerEvent.BUTTON_UP;
						event.controller = controller;
						event.code = i;						
						event.amount = newButton;
						eventQueue.add(event);
					}					
				}				
			}
		}
	}

	@Override
	public void clearListeners () {
		listeners.clear();
	}

}